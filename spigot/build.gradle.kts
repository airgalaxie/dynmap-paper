plugins {
    alias(libs.plugins.shadow)
    id("dynmap.java-conventions")
}

description = "dynmap"

dependencies {
    implementation(project(":dynmap-api"))
    implementation(project(":DynmapCore", "shadow"))
    implementation(project(":bukkit-helper"))

    compileOnly(libs.paperApi)
    compileOnly(libs.luckpermsApi)
    compileOnly(libs.vaultApi) { exclude("org.bukkit", "bukkit") }
    compileOnly(libs.groupManager)
    compileOnly(libs.jsonSimple)
}

tasks {
    processResources {
        // replace stuff in mcmod.info, nothing else
        filesMatching("paper-plugin.yml") {
            // replace version and mcversion
            expand(
                    "buildnumber" to project.parent!!.ext.get("buildNumber").toString(),
                    "version" to project.version
            )
        }
    }

    jar {
        archiveClassifier = "unshaded"
    }

    shadowJar {
        dependencies {
            include(dependency(":dynmap-api"))
            include(dependency(":DynmapCore"))
            include(dependency(":bukkit-helper"))
        }

        destinationDirectory = file("../target")
        archiveBaseName = "Dynmap"
        archiveClassifier = "spigot"
    }

    artifacts {
        archives(shadowJar)
    }
}
