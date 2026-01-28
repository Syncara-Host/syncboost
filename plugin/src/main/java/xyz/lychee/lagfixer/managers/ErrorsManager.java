package xyz.lychee.lagfixer.managers;

import com.google.common.collect.Iterators;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import lombok.Data;
import lombok.Getter;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.Logger;
import org.apache.logging.log4j.core.filter.AbstractFilter;
import org.bukkit.Bukkit;
import xyz.lychee.lagfixer.LagFixer;
import xyz.lychee.lagfixer.commands.BenchmarkCommand;
import xyz.lychee.lagfixer.objects.AbstractManager;
import xyz.lychee.lagfixer.objects.AbstractMonitor;

import javax.net.ssl.HttpsURLConnection;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Getter
public class ErrorsManager extends AbstractManager {
    private static @Getter ErrorsManager instance;
    private final Gson gson = new Gson();
    private final UUID uuid = UUID.randomUUID();
    private final HashMap<ThrowableKey, Error> errors = new HashMap<>();
    private final Pattern pattern = Pattern.compile("https://spark\\.lucko\\.me/.{10}");
    private final AbstractFilter filter;
    private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();

    private final ConcurrentLinkedQueue<SendTask> sendQueue = new ConcurrentLinkedQueue<>();
    private boolean isSending = false;

    public ErrorsManager(LagFixer plugin) {
        super(plugin);
        instance = this;
        this.filter = new AbstractFilter() {
            public Filter.Result filter(LogEvent event) {
                if (event.getLoggerName().equals(getPlugin().getLogger().getName())) {
                    return Filter.Result.NEUTRAL;
                }

                if (event.getThrown() != null) {
                    return checkError(event.getThrown()) ? Filter.Result.NEUTRAL : Filter.Result.DENY;
                }

                Matcher matcher = pattern.matcher(event.getMessage().getFormattedMessage());
                if (matcher.find()) {
                    // Spark profiler URL detected - log for local reference
                    getPlugin().getLogger()
                            .info("\u00267Spark profiler detected: " + matcher.group()
                                    + " - Use this for performance analysis.");
                }
                return Filter.Result.NEUTRAL;
            }
        };
    }

    @Override
    public void load() {
        Logger logger = (Logger) LogManager.getRootLogger();
        if (!Iterators.contains(logger.getFilters(), this.filter)) {
            logger.addFilter(this.filter);
        }

        this.executor.scheduleAtFixedRate(this::processQueue, 1, 1, TimeUnit.MINUTES);

        this.getPlugin().getLogger().info(" \u00268• \u0026rStarted listening console for SyncBoost errors!");
    }

    @Override
    public void disable() {
        this.executor.shutdownNow();
    }

    private void processQueue() {
        if (isSending || sendQueue.isEmpty()) {
            return;
        }

        SendTask task = sendQueue.poll();
        if (task != null) {
            isSending = true;
            try {
                task.send();
            } finally {
                isSending = false;
            }
        }
    }

    /**
     * Dodaje zadanie do kolejki jeśli nie ma go już w kolejce
     */
    private void addToQueue(SendTask task) {
        for (SendTask queuedTask : sendQueue) {
            if (queuedTask.equals(task)) {
                return;
            }
        }
        sendQueue.offer(task);
    }

    public boolean checkError(Throwable t) {
        if (t == null)
            return true;

        ThrowableKey key = new ThrowableKey(t);
        List<String> stackTrace = this.filterStackTrace(t);
        if (stackTrace.isEmpty() || this.errors.containsKey(key))
            return true;

        StringBuilder message = new StringBuilder();
        message.append("SyncBoost error message:\n");
        message.append("\n\u00268\u0026m-------------------------------\u0026r");
        message.append("\n");
        message.append("\n\u0026fAn error occurred in SyncBoost:");
        message.append("\n \u00267-> \u0026c").append(t.getClass().getSimpleName()).append(": ").append(t.getMessage());
        for (String str : stackTrace) {
            message.append("\n \u00267| \u0026c").append(str);
        }
        message.append("\n");
        message.append("\n\u0026fPlease report this issue at:");
        message.append("\n\u0026e\u0026nhttps://github.com/Syncara-Host/syncboost/issues\u0026r");
        message.append("\n");
        message.append("\n\u0026fFor documentation and guides, visit:");
        message.append("\n\u0026b\u0026nhttps://github.com/Syncara-Host/syncboost/wiki\u0026r");
        message.append("\n");
        message.append("\n\u00268\u0026m-------------------------------\n");
        this.getPlugin().getLogger().warning(message.toString());

        this.errors.put(key, new Error(stackTrace, t));

        // External reporting disabled - errors are logged locally only
        // Users can report issues at: https://github.com/Syncara-Host/syncboost/issues

        return false;
    }

    @Override
    public boolean isEnabled() {
        return this.getPlugin().getConfig().getBoolean("main.errors_reporter");
    }

    public void sendStackTraces() {
        addToQueue(new ErrorsSendTask());
    }

    public void sendProfiler(String url) {
        addToQueue(new ProfilerSendTask(url));
    }

    public void sendBenchmark(BenchmarkCommand.Benchmark benchmark) {
        addToQueue(new BenchmarkSendTask(benchmark));
    }

    private JsonObject createJson() {
        UpdaterManager updater = UpdaterManager.getInstance();
        SupportManager support = SupportManager.getInstance();
        AbstractMonitor monitor = support.getMonitor();
        JsonObject jo = new JsonObject();

        jo.addProperty("bukkit", Bukkit.getName() + " " + Bukkit.getServer().getBukkitVersion());
        jo.addProperty("version", this.getPlugin().getDescription().getVersion());
        jo.addProperty("uuid", this.uuid.toString());
        jo.addProperty("entities", support.getEntities());
        jo.addProperty("creatures", support.getCreatures());
        jo.addProperty("items", support.getItems());
        jo.addProperty("projectiles", support.getProjectiles());
        jo.addProperty("vehicles", support.getVehicles());
        jo.addProperty("players", Bukkit.getOnlinePlayers().size());
        jo.addProperty("maxplayers", Bukkit.getMaxPlayers());
        jo.addProperty("cpuprocess", monitor.getCpuProcess());
        jo.addProperty("cpusystem", monitor.getCpuSystem());
        jo.addProperty("ramused", monitor.getRamUsed());
        jo.addProperty("ramtotal", monitor.getRamTotal());
        jo.addProperty("ramfree", monitor.getRamFree());
        jo.addProperty("tps", monitor.getTps());
        jo.addProperty("mspt", monitor.getMspt());
        jo.addProperty("current_version", updater.getCurrentVersion());
        jo.addProperty("latest_version", updater.getLatestVersion());
        jo.addProperty("difference_version", updater.getDifference());

        return jo;
    }

    /**
     * External reporting has been disabled for SyncBoost.
     * All error tracking is now local-only.
     * 
     * For support, please visit:
     * - Issues: https://github.com/Syncara-Host/syncboost/issues
     * - Wiki: https://github.com/Syncara-Host/syncboost/wiki
     */
    private void connect(String params, JsonObject jsonObject) {
        // External reporting disabled - no data is sent to external servers
    }

    private List<String> filterStackTrace(Throwable ex) {
        List<String> list = new ArrayList<>();
        for (StackTraceElement e : ex.getStackTrace()) {
            if (!e.getClassName().contains("lagfixer"))
                continue;

            list.add(String.format("%s -> %s() at %d line", e.getFileName(), e.getMethodName(), e.getLineNumber()));
        }
        return list;
    }

    private interface SendTask {
        void send();

        boolean equals(Object obj);
    }

    public static final class ThrowableKey {
        private final Class<? extends Throwable> type;
        private final String message;
        private final ThrowableKey causeKey;

        public ThrowableKey(Throwable t) {
            this.type = t.getClass();
            this.message = t.getMessage();
            this.causeKey = t.getCause() == null ? null : new ThrowableKey(t.getCause());
        }

        @Override
        public boolean equals(Object o) {
            if (this == o)
                return true;
            if (!(o instanceof ThrowableKey))
                return false;
            ThrowableKey that = (ThrowableKey) o;
            return type.equals(that.type) &&
                    Objects.equals(message, that.message) &&
                    Objects.equals(causeKey, that.causeKey);
        }

        @Override
        public int hashCode() {
            return Objects.hash(type, message, causeKey);
        }

        @Override
        public String toString() {
            return type.getSimpleName() + ": " + message + (causeKey != null ? " <- " + causeKey : "");
        }
    }

    private class ErrorsSendTask implements SendTask {
        @Override
        public void send() {
            JsonObject jsonObject = createJson();
            JsonArray errorsArray = new JsonArray();

            for (Error error : errors.values()) {
                if (!error.isReported())
                    error.handle(errorsArray);
            }

            jsonObject.add("errors", errorsArray);
            connect("/errors?plugin=" + getPlugin().getName(), jsonObject);
        }

        @Override
        public boolean equals(Object obj) {
            return obj instanceof ErrorsSendTask;
        }
    }

    private class ProfilerSendTask implements SendTask {
        private final String url;

        public ProfilerSendTask(String url) {
            this.url = url;
        }

        @Override
        public void send() {
            JsonObject jsonObject = createJson();
            jsonObject.addProperty("profiler", url);
            connect("/profilers?plugin=" + getPlugin().getName(), jsonObject);
        }

        @Override
        public boolean equals(Object obj) {
            return obj instanceof ProfilerSendTask;
        }
    }

    private class BenchmarkSendTask implements SendTask {
        private final BenchmarkCommand.Benchmark benchmark;

        public BenchmarkSendTask(BenchmarkCommand.Benchmark benchmark) {
            this.benchmark = benchmark;
        }

        @Override
        public void send() {
            JsonObject jsonObject = createJson();
            jsonObject.add("benchmark", gson.toJsonTree(benchmark));
            connect("/benchmarks?plugin=" + getPlugin().getName(), jsonObject);
        }

        @Override
        public boolean equals(Object obj) {
            return obj instanceof BenchmarkSendTask;
        }
    }

    @Data
    public class Error {
        private final String message;
        private final String stackTrace;
        private final String fullStackTrace;
        private transient boolean reported;

        public Error(List<String> stackTrace, Throwable ex) {
            this.message = ex.getClass().getSimpleName() + ": " + ex.getMessage();
            this.stackTrace = String.join("\n", stackTrace);
            this.fullStackTrace = ExceptionUtils.getStackTrace(ex);
            this.reported = false;
        }

        public void handle(JsonArray arr) {
            arr.add(gson.toJsonTree(this, Error.class));
            this.reported = true;
        }
    }
}