# Dynmap v26.2

This is a heavily modified **fork** of the [airgalaxie/dynmap](https://github.com/airgalaxie/dynmap) repository, which builds upon the work of [JLyne's fork](https://github.com/JLyne/dynmap) and is based on the original [Dynmap project](https://github.com/webbukkit/dynmap).

> [!NOTE]
> **Development Environment:** Active development and compilation on **Linux** using **Java 26** and **Gradle 9.5.1**.  
> **Current Production Build:** v26.1.2++ (Stable & Modernized)  
> **In Development:** Version v26.2 is currently active and in progress.

---

## 🔄 Side-by-Side Comparison of Project Scope


| Original dynmap Repository                          | airgalaxie New Modernized Fork (v26.1.2++)                                                  |
|:----------------------------------------------------|:--------------------------------------------------------------------------------------------|
| **Target Platform:** Paper 1.21.10 only             | **Target Platform:** Fully compatible with Paper 1.21.10 & 26.2 architectures               |
| **Compiler / Runtime:** Legacy Java target          | **Compiler / Runtime:** Built with **Java 26** on **Linux**                                 |
| **Build System:** Outdated Gradle build             | **Build System:** Upgraded to **Gradle 9.5.1**                                              |
| **Web Framework:** Deprecated/legacy jQuery version | **Web Framework:** Upgraded to **jQuery 4.0.x** (fixing legacy vulnerabilities)             |
| **Map Engine:** Old Leaflet 1.7.x library           | **Map Engine:** Upgraded to **Leaflet 1.9.4** (smooth rendering, modern browser fixes)      |
| **Render Configurations:** Outdated model txt-files | **Render Configurations:** Fixed critical bugs directly inside the **`Modelsxx.txt`** files |

---

## 🛠️ Main Changes in this Fork (Summary)

*   **Java 26 & Gradle 9.5.1**: Cutting-edge Linux-based compilation environment for maximum performance and future-proofing.
*   **jQuery 4.0.x & Leaflet 1.9.4**: Replaced outdated frontend web assets to resolve performance drops and security flaws.
*   **Model Text Fixes**: Corrected specific structure and block rendering bugs in the `Modelsxx.txt` configuration data.

---

## 📝 Configuration and Platform Notes

*   **Configuration:** Dynmap's configuration is primarily managed through `configuration.txt` and other related files within the plugin's data folder. Please refer to the official Dynmap documentation for detailed configuration instructions. This fork does not introduce new configuration methods, but focuses on updating underlying technologies.
*   **No Internal Webserver:** This fork, like the original Dynmap, does **not** include an internal webserver. You need to deploy the generated web files to an external web server (e.g., Nginx, Apache) for the map to be accessible via a web browser.
*   **Platform Support:** This project is specifically built and tested for **Paper** servers (versions 1.21.10 & 26.1.2+ architectures). There are currently **no builds or explicit support for Fabric** or other Minecraft server platforms. Attempting to use this fork on unsupported platforms may lead to unexpected behavior or failures.

---

### ⚠️ Support Disclaimer
Just like the original repositories: No official support will be provided. Please do not contact the original [Dynmap Team](https://github.com/webbukkit/dynmap), [JLyne](https://github.com/JLyne/dynmap), or the maintainers of this specific fork ([airgalaxie/dynmap](https://github.com/airgalaxie/dynmap)) for help regarding the changes implemented in this specific fork.
