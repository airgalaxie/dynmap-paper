# Dynmap Paper v26.2

This is a heavily modified **fork** of the [airgalaxie/dynmap-paper](https://github.com/airgalaxie/dynmap-paper) repository, which builds upon the work of [JLyne's fork](https://github.com/JLyne/dynmap) and is based on the original [Dynmap project](https://github.com/webbukkit/dynmap).

> [!NOTE]
> **Development Environment:** Active local development on **Linux** with **Java 26** and **Gradle 9.6.0**.  
> **Production Compatibility:** The Gradle toolchain targets **Java 25**, so release builds must remain Java 25 compatible.  
> **Current Branch:** `main` / `26.2`  
> **Paper Target:** `26.2.build.+`  
> **Compatibility Goal:** Keep this fork compatible with the Paper **26.2.x** release line for as long as possible.  
> **Status:** 26.2 release process; Paper upstream is resolved dynamically through `26.2.build.+`.

---

## Side-by-Side Comparison of Project Scope


| Original dynmap Repository                          | This fork (main / 26.2 branch)                                                              |
|:----------------------------------------------------|:--------------------------------------------------------------------------------------------|
| **Target Platform:** Paper 1.21.10 baseline         | **Target Platform:** Paper 26.2.x, dynamically tracking `26.2.build.+`                      |
| **Compiler / Runtime:** Legacy Java target          | **Compiler / Runtime:** Developed with Java 26 locally, built for **Java 25** compatibility |
| **Build System:** Outdated Gradle build             | **Build System:** Upgraded to **Gradle 9.6.0**                                              |
| **Web Framework:** Deprecated/legacy jQuery version | **Web Framework:** Upgraded to **jQuery 4.0.x** (fixing legacy vulnerabilities)             |
| **Map Engine:** Old Leaflet 1.7.x library           | **Map Engine:** Upgraded to **Leaflet 1.9.4** (smooth rendering, modern browser fixes)      |
| **Render Configurations:** Outdated model txt-files | **Render Configurations:** Fixed critical bugs in bundled model definition files            |

---

## Main Changes in this Fork (Summary)

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
- Replacement of the legacy internal webserver with a small static webserver plus documented storage-backed endpoints for SQL storage
- Removal of obsolete permission providers and SkinsRestorer integration
- Removal of various outdated workarounds (Log4Shell, Spout lighting, etc)
- Removal of Postgres and S3 storage types

## Configuration and Platform Notes

*   **Configuration:** Dynmap's configuration is primarily managed through `configuration.txt` and other related files within the plugin's data folder. Please refer to the official Dynmap documentation for detailed configuration instructions. This fork does not introduce new configuration methods, but focuses on updating underlying technologies.
*   **Webserver Default:** The Dynmap webserver configuration is present, and the bundled default configuration follows the original Dynmap default with `disable-webserver: false`. Deploy the generated web files to an external web server (e.g., Nginx, Apache), or use the internal static webserver if you want Dynmap to serve the generated web UI itself.
*   **Platform Support:** This project targets the **Paper 26.2.x** server line and dynamically resolves the latest matching `26.2.build.+` API target at build time. Compatibility with later 26.2.x builds is intended, but should be re-tested when Paper changes APIs or server internals during the alpha/RC phase. There are currently **no builds or explicit support for Fabric** or other Minecraft server platforms. Attempting to use this fork on unsupported platforms may lead to unexpected behavior or failures.

## Web Chat Boundary

Browser-to-server web chat is disabled in this fork. The web UI message display remains enabled: `chat`, `chatbox`, and `chatballoon` client components are still used for server-to-web messages, join/quit notices, player chat display, and map chat balloons. Do not remove those components when disabling browser chat input. Marker popups and labels are handled separately by the marker component and marker JSON files.

## JDBC Drivers

Database storage classes for MySQL and MariaDB are present, but JDBC drivers are not bundled into the plugin. Dynmap first checks whether the configured driver is already available on the server classpath. If not, set `storage/driver-jar` and optionally `storage/driver-class` in `configuration.txt` to load a driver JAR from Dynmap's data folder, for example `drivers/mariadb-java-client.jar`.

## External Web Server and Live Data URLs

This fork keeps the existing Dynmap-style external web server workflow intact. The generated web UI reads live data through the URLs written to `standalone/config.js`. By default, those URLs point to the generated standalone JSON files:

```yaml
url:
    # configuration: "standalone/dynmap_config.json?_={timestamp}"
    # update: "standalone/dynmap_{world}.json?_={timestamp}"
```

Existing installations can keep using their current file-based setup. Advanced deployments that already route live data through a reverse proxy or another endpoint can override only these URLs in `configuration.txt`:

```yaml
url:
    configuration: "up/configuration"
    update: "up/world/{world}/{timestamp}"
```

This is the supported compatibility path for custom routing. The project should not require users to rebuild a working Nginx, Apache, or CDN setup just to follow the fork. If a deployment serves the web UI from one machine while the backend writes live JSON over NFS or another shared filesystem, short-lived `404` responses for live JSON usually indicate infrastructure timing or cache visibility issues. In that case, prefer local filesystem/NFS/Nginx tuning or URL overrides over changing the global project defaults.

## Internal Webserver Storage Endpoints

The internal Java webserver still serves normal web assets directly from `webpath`. For SQL-style storage, it also exposes optional storage-backed endpoints so users can run MySQL/MariaDB without an external PHP webserver and without exporting duplicate tile files.

This is a deliberate fork-level change from original Dynmap and is controlled by:

```yaml
webserver-storage-endpoints: true
```

When the internal webserver is enabled and the storage default URLs would otherwise point at `standalone/*.php`, `standalone/config.js` is generated with Java-backed routes:

```yaml
configuration: "up/configuration"
update: "up/world/{world}/{timestamp}"
tiles: "storage/tiles/"
markers: "storage/markers/"
```

Those endpoints read through the existing `MapStorage` API:

- live configuration and world updates from standalone storage
- map tiles from storage by world and tile URI
- marker JSON, marker icons, and player faces from storage

External deployments are kept compatible. If `disable-webserver: true`, or if explicit `url:` values are configured, the original external URLs remain in use, including the original SQL PHP endpoints such as `standalone/MySQL_update.php` and `standalone/MySQL_tiles.php`.

## External PHP SQL Endpoints

The bundled MySQL standalone PHP endpoints are kept for external Apache/Nginx/PHP deployments. MariaDB storage uses the same MySQL-compatible endpoint names and request parameters:

```text
standalone/MySQL_configuration.php
standalone/MySQL_update.php?world={world}&ts={timestamp}
standalone/MySQL_tiles.php?tile=
standalone/MySQL_markers.php?marker=
```

The PHP code now uses modern PHP 8.x style with strict types, `__DIR__` includes, typed helper functions, `http_response_code`, consistent JSON error responses for JSON endpoints, prepared statements, `utf8mb4`, and path validation. `MySQL_config.php` is still generated by Dynmap and remains compatible with the original variable names.

---

### Support Disclaimer
Just like the original repositories: No official support will be provided. Please do not contact the original [Dynmap Team](https://github.com/webbukkit/dynmap), [JLyne](https://github.com/JLyne/dynmap), or the maintainers of this specific fork ([airgalaxie/dynmap-paper](https://github.com/airgalaxie/dynmap-paper)) for help regarding the changes implemented in this specific fork.
