# Dynmap v26.1.2++ (Modernized Fork)

This is a heavily modified **fork** of the airgalaxie repository, which builds upon the work of [JLyne](https://github.com) and is based on the original [Dynmap](https://github.com) project.

> [!NOTE]
> **Development Environment:** Active development and compilation on **Linux** using **Java 26** and **Gradle 9.5.1**.  
> **Current Production Build:** v26.1.2++ (Stable & Modernized)  
> **In Development:** Version v26.2 is currently active and in progress.

---

## 🔄 Side-by-Side Comparison of Project Scope


| Original dynmap Repository                          | airgalaxie New Modernized Fork (v26.1.2++)                                                  |
|:----------------------------------------------------|:--------------------------------------------------------------------------------------------|
| **Target Platform:** Paper 1.21.10 only             | **Target Platform:** Fully compatible with Paper 1.21.10 & 26.1.2+ architectures            |
| **Compiler / Runtime:** Legacy Java target          | **Compiler / Runtime:** Built with **Java 26** on **Linux**                                 |
| **Build System:** Outdated Gradle build             | **Build System:** Upgraded to **Gradle 9.5.1**                                              |
| **Web Framework:** Deprecated/legacy jQuery version | **Web Framework:** Upgraded to **jQuery 4.0.x** (fixing legacy vulnerabilities)             |
| **Map Engine:** Old Leaflet 1.7.x library           | **Map Engine:** Upgraded to **Leaflet 1.9.4** (smooth rendering, modern browser fixes)      |
| **Render Configurations:** Outdated model txt-files | **Render Configurations:** Fixed critical bugs directly inside the **`Modelsxx.txt`** files |

---

## 🛠️ Main Changes in this Fork (Summary)

* **Java 26 & Gradle 9.5.1**: Cutting-edge Linux-based compilation environment for maximum performance and future-proofing.
* **jQuery 4.0.x & Leaflet 1.9.4**: Replaced outdated frontend web assets to resolve performance drops and security flaws.
* **Model Text Fixes**: Corrected specific structure and block rendering bugs in the `Modelsxx.txt` configuration data.

### ⚠️ Support Disclaimer
Just like the original repositories: No official support will be provided. Please do not contact the original [Dynmap Team](https://github.com), [JLyne](https://github.com), or airgalaxie for help regarding the changes implemented in this specific fork.
