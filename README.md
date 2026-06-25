# Dynmap Paper v26.2

This is a heavily modified **fork** of the [airgalaxie/dynmap-paper](https://github.com/airgalaxie/dynmap-paper) repository, which builds upon the work of [JLyne's fork](https://github.com/JLyne/dynmap) and is based on the original [Dynmap project](https://github.com/webbukkit/dynmap).

> [!NOTE]
> **Development Environment:** Active local development on **Linux** with **Java 26** and **Gradle 9.6.0**.  
> **Production Compatibility:** The Gradle toolchain targets **Java 25**, so release builds must remain Java 25 compatible.  
> **Current Branch:** `main` / `26.2`  
> **Paper Target:** `26.2.build.+`  
> **Compatibility Goal:** Keep this fork compatible with the Paper **26.2.x** release line for as long as possible.  
> **Status:** 26.2 release process; Paper upstream is still consumed from the current alpha/RC build line.

---

## 🔄 Side-by-Side Comparison of Project Scope


| Original dynmap Repository                          | airgalaxie New Modernized Fork (main / 26.2 branch)                                         |
|:----------------------------------------------------|:--------------------------------------------------------------------------------------------|
| **Target Platform:** Paper 1.21.10 baseline         | **Target Platform:** Paper 26.2.x, dynamically tracking `26.2.build.+`                      |
| **Compiler / Runtime:** Legacy Java target          | **Compiler / Runtime:** Developed with Java 26 locally, built for **Java 25** compatibility |
| **Build System:** Outdated Gradle build             | **Build System:** Upgraded to **Gradle 9.6.0**                                              |
| **Web Framework:** Deprecated/legacy jQuery version | **Web Framework:** Upgraded to **jQuery 4.0.x** (fixing legacy vulnerabilities)             |
| **Map Engine:** Old Leaflet 1.7.x library           | **Map Engine:** Upgraded to **Leaflet 1.9.4** (smooth rendering, modern browser fixes)      |
| **Render Configurations:** Outdated model txt-files | **Render Configurations:** Fixed critical bugs directly inside the **`Modelsxx.txt`** files |

---

## 🛠️ Main Changes in this Fork (Summary)

*   **Paper 26.2.x Compatibility Goal**: Uses the dynamic Paper dependency selector `26.2.build.+` for the 26.2 release process, while keeping the codebase aligned with the broader 26.2.x release line where practical.
*   **Java 26 Development / Java 25 Production Compatibility**: Local development uses Java 26, while the Gradle toolchain keeps the produced plugin aligned with Java 25 compatibility.
*   **jQuery 4.0.x & Leaflet 1.9.4**: Replaced outdated frontend web assets to resolve performance drops and security flaws.
*   **Model Text Fixes**: Corrected specific structure and block rendering bugs in the `Modelsxx.txt` configuration data.

---

Changes include:
- Removal of all platform support except Paper 26.2.x
- Now a Mojang mapped Paper plugin
- Removal of web chat
- Removal of login support
- Removal of internal webserver
- Removal of obsolete permission providers and SkinsRestorer integration
- Removal of various outdated workarounds (Log4Shell, Spout lighting, etc)
- Removal of Postgres and S3 storage types

## 📝 Configuration and Platform Notes

*   **Configuration:** Dynmap's configuration is primarily managed through `configuration.txt` and other related files within the plugin's data folder. Please refer to the official Dynmap documentation for detailed configuration instructions. This fork does not introduce new configuration methods, but focuses on updating underlying technologies.
*   **Webserver Default:** The Dynmap webserver configuration is present, but the bundled default configuration sets `disable-webserver: true`. Deploy the generated web files to an external web server (e.g., Nginx, Apache), or explicitly reconfigure Dynmap if you want to use its internal webserver.
*   **Platform Support:** This project targets the **Paper 26.2.x** server line and dynamically resolves the latest matching `26.2.build.+` API target at build time. Compatibility with later 26.2.x builds is intended, but should be re-tested when Paper changes APIs or server internals during the alpha/RC phase. There are currently **no builds or explicit support for Fabric** or other Minecraft server platforms. Attempting to use this fork on unsupported platforms may lead to unexpected behavior or failures.

---

### ⚠️ Support Disclaimer
Just like the original repositories: No official support will be provided. Please do not contact the original [Dynmap Team](https://github.com/webbukkit/dynmap), [JLyne](https://github.com/JLyne/dynmap), or the maintainers of this specific fork ([airgalaxie/dynmap-paper](https://github.com/airgalaxie/dynmap-paper)) for help regarding the changes implemented in this specific fork.
