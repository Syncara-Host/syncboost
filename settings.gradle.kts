rootProject.name = "SyncBoost"

include("plugin")

// Note: v1_16_R3 is excluded - requires Java 8 buildtools setup
// To build v1_16_R3: run BuildTools for 1.16.5, then uncomment below
// include("nms:v1_16_R3")  // 1.16.5 - requires Java 8

// Legacy NMS versions (use paperweight-userdev + Java 17 toolchain)
include("nms:v1_17_R1")  // 1.17.1
include("nms:v1_18_R2")  // 1.18.2
include("nms:v1_19_R3")  // 1.19.4
include("nms:v1_20_R1")  // 1.20-1.20.1
include("nms:v1_20_R2")  // 1.20.2
include("nms:v1_20_R3")  // 1.20.3-1.20.4

// Modern NMS versions (use paperweight-userdev, no BuildTools needed)
include("nms:v1_20_R4")  // 1.20.5-1.20.6
include("nms:v1_21_R1")  // 1.21-1.21.1
include("nms:v1_21_R2")  // 1.21.2-1.21.3
include("nms:v1_21_R3")  // 1.21.4
include("nms:v1_21_R4")  // 1.21.5
include("nms:v1_21_R5")  // 1.21.6-1.21.8
include("nms:v1_21_R6")  // 1.21.9-1.21.10
include("nms:v1_21_R7")  // 1.21.11

include("support:paper")
include("support:spigot")
include("support:common")