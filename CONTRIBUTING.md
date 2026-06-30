# Contributing to Dynmap Paper v26.2

## Project Origins and History
This repository is a modern, heavily modified fork of the `airgalaxie/dynmap-paper` project, continuing the work of JLyne and the original Dynmap project created by Mike Primm/webbukkit. While we respect and preserve the core structure of the original project, this branch is optimized for the Paper 26.2.x server runtime while keeping Bukkit/Dynmap API integrations usable where practical.

## Target Environments and Java Runtime
The runtime and build strategy is intentionally narrow:
- **Production Baseline:** Release builds target Java 25 through the Gradle toolchain. Production-targeted code must remain Java 25 compatible.
- **Development Environment:** Active local development uses Linux, Java 26, and Gradle 9.6.0.
- **Paper Runtime Target:** Dependencies track the Paper 26.2.x line through `26.2.build.+`.
- **Plugin API Target:** External plugin integrations should continue to use the published Bukkit/Dynmap API where possible.
- **Build Infrastructure:** Keep Gradle and dependency changes focused and documented.

## Contribution and Scope Rules
- **Paper 26.2.x Runtime:** The current plugin artifact targets Paper 26.2.x and is not a Spigot server build.
- **Bukkit/Spigot API Compatibility:** Preserve the public Dynmap Bukkit API and common integration behavior for other plugins where practical.
- **Other Platforms:** Fabric and legacy non-Bukkit platforms are out of scope.
- **Modernized Frontend:** Web assets use modern jQuery and Leaflet versions. Keep frontend changes compatible with the existing Dynmap web UI flow.
- **Internal Webserver:** The fork includes a small internal static webserver and optional storage-backed endpoints. Preserve external web server compatibility and existing URL override behavior.
- **Removed Legacy Storage:** PostgreSQL and S3 storage are not implemented in this fork.
- **Web Chat and Login:** Browser-to-server chat input and login support are disabled. Do not remove the display components used for server-to-web messages, player chat display, join/quit notices, marker labels, or popups.
- **JDBC Drivers:** MySQL and MariaDB storage must not require bundled JDBC drivers. Driver loading through `storage/driver-jar` and `storage/driver-class` must remain supported.
- **External PHP Endpoints:** Keep the MySQL-compatible standalone PHP endpoints usable for external Apache/Nginx/PHP deployments.
- **Repository Hygiene:** Do not commit generated build output, Gradle caches, reports, `.class` files, or release artifacts unless explicitly required.
- **Pull Requests:** Keep PRs highly focused. Avoid pure style, reformatting, or pretty-printing changes, as they obscure reviews and trigger unnecessary merge conflicts.

## Licensing
The project remains fully licensed under the **Apache Public License v2 (APL v2)**. By contributing, you agree that your code falls under this license.

## Support Disclaimer
In line with upstream policies: **No official support is provided.** Please do not contact the original Dynmap team or prior maintainers regarding the modifications in this fork.
