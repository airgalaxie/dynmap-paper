rootProject.name = "dynmap-common"

include(":spigot")
include(":bukkit-helper")
include(":dynmap-api")
include(":DynmapCore")
include(":DynmapCoreAPI")

project(":spigot").projectDir = file("$rootDir/spigot")
project(":bukkit-helper").projectDir = file("$rootDir/bukkit-helper")
project(":dynmap-api").projectDir = file("$rootDir/dynmap-api")
project(":DynmapCore").projectDir = file("$rootDir/DynmapCore")
project(":DynmapCoreAPI").projectDir = file("$rootDir/DynmapCoreAPI")
