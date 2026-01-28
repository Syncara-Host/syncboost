plugins {
    id("java")
    id("com.gradleup.shadow") version "9.3.0"
}

val spigotRepo = "https://hub.spigotmc.org/nexus/content/repositories/snapshots/";
val paperRepo = "https://repo.papermc.io/repository/maven-public/";
val sonatypeRepo = "https://oss.sonatype.org/content/groups/public/";
val jitpack = "https://jitpack.io"
val mojang = "https://libraries.minecraft.net";

version = "1.5.0-FoliaReady"
extra["syncboost_version"] = version
extra["syncboost_build"] = "2"

dependencies {
    implementation(project(":plugin"))

    // Legacy NMS versions (1.17.1 - 1.20.4) - Java 17
    // Note: v1_16_R3 excluded - requires Java 8 BuildTools setup
    implementation(project(":nms:v1_17_R1"))
    implementation(project(":nms:v1_18_R2"))
    implementation(project(":nms:v1_19_R3"))
    implementation(project(":nms:v1_20_R1"))
    implementation(project(":nms:v1_20_R2"))
    implementation(project(":nms:v1_20_R3"))

    // Modern NMS versions (1.20.5+)
    implementation(project(":nms:v1_20_R4"))
    implementation(project(":nms:v1_21_R1"))
    implementation(project(":nms:v1_21_R2"))
    implementation(project(":nms:v1_21_R3"))
    implementation(project(":nms:v1_21_R4"))
    implementation(project(":nms:v1_21_R5"))
    implementation(project(":nms:v1_21_R6"))
    implementation(project(":nms:v1_21_R7"))

    implementation(project(":support:common"))
    implementation(project(":support:spigot"))
    implementation(project(":support:paper"))
}

tasks {
    build {
        dependsOn(shadowJar)
    }

    shadowJar {
        archiveBaseName.set("SyncBoost")
        archiveClassifier.set("")
        archiveVersion.set("")

        relocate("net.kyori", "host.syncara.syncboost.libs.kyori")
        relocate("org.bstats", "host.syncara.syncboost.libs.bstats")
        // Output directory - update this path as needed
        // destinationDirectory.set(file("build/libs"))
    }
}

allprojects {
    group = "host.syncara";

    apply(plugin = "java")

    repositories {
        mavenLocal()
        mavenCentral()
        maven(spigotRepo)
        maven(paperRepo)
        maven(sonatypeRepo)
        maven(mojang)
        maven(jitpack)
    }

    dependencies {
        compileOnly("org.projectlombok:lombok:1.18.40")
        annotationProcessor("org.projectlombok:lombok:1.18.40")
    }

    tasks {
        compileJava {
            options.encoding = "UTF-8"
        }
    }
}


java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}