# Contributing to Dynmap (airgalaxie Modernized Fork)

## 📜 Project Origins and History
This repository is a modern, heavily modified fork of the `airgalaxie/dynmap` project, continuing the work of JLyne and the original Dynmap project created by Mike Primm/webbukkit. While we respect and preserve the core structure of the original project, this branch is optimized strictly for modern Paper server environments.

## 🛠 Target Environments & Java Runtime
To ensure absolute production stability while allowing technological sandbox testing, the runtime strategy is split:
- **Production Baseline:** Codebase and releases are strictly aligned with modern, active **Java LTS (Long-Term Support)** versions required by the target Paper ecosystem. All production-targeted code must remain compatible with these stable environments.
- **Experimental Sandbox:** Local development and experimental branches actively explore future language features, utilizing the **latest stable or Early-Access (EA) JDK releases** to test future-proof optimizations.
- **Build Infrastructure:** The build system utilizes the latest major **Gradle** toolchain releases to manage modern dependency compilation dynamically.

## ⚠️ Contribution & Scope Rules
- **Paper Ecosystem Exclusive:** Support for other legacy platforms (Spigot, Fabric, etc.) and obsolete workarounds has been removed to keep the codebase clean and performant.
- **Modernized Frontend:** Web assets utilize modern major releases of **jQuery** and **Leaflet** to permanently resolve legacy security vulnerabilities and performance flaws.
- **Stripped Legacy Features:** Internal webservers, legacy databases (Postgres/S3), and old chat protocols are removed.
- **Pull Requests:** Keep PRs highly focused. Avoid pure style, reformatting, or pretty-printing changes, as they obscure security reviews and trigger unnecessary merge conflicts.

## ⚖️ Licensing
The project remains fully licensed under the **Apache Public License v2 (APL v2)**. By contributing, you agree that your code falls under this license.

## 🤝 Support Disclaimer
In line with upstream policies: **No official support is provided.** Please do not contact the original Dynmap team or prior maintainers regarding the modifications in this fork.
