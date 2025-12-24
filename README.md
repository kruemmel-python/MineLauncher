# C_launcher

Ein einfacher C#-Launcher mit GUI für Minecraft-Server (Vanilla, Paper, Bukkit, CraftBukkit) mit Online- und Offline-Modus. Zusätzlich ist ein eigener Client-Launcher integriert, der die Minecraft-Dateien lädt und das Spiel im Offline-Modus startet.

## Voraussetzungen

- Windows
- .NET 8 SDK
- Java (im `PATH` oder im Config-Pfad gesetzt)

## Nutzung

```bash
# im Repo-Root:
dotnet run --project C_launcher
# oder im Ordner C_launcher:
dotnet run --project C_launcher.csproj
```

Die GUI erstellt/öffnet die Konfiguration und bietet Download + Start des Servers sowie Laden/Start des Clients.

Optional kann der Client auch per CLI gestartet werden:

```bash
dotnet run --project C_launcher -- play [Name]
```

## Konfiguration

Die Datei `launcher-config.json` enthält alle Einstellungen:

- `server.type`: `vanilla`, `paper`, `bukkit` oder `craftbukkit`
- `server.version`: Minecraft-Version
- `server.paperBuild`: Optional, Paper-Buildnummer (leer = latest)
- `server.onlineMode`: `true` für Online, `false` für Offline
- `server.acceptEula`: `true` wenn die EULA akzeptiert ist
- `server.port`: Server-Port
- `java.path`: z. B. `java` oder ein voller Pfad
- `java.minMemory` / `java.maxMemory`: RAM-Settings
- `game.clientVersion`: Minecraft-Version für den Client (muss zum Server passen)
- `game.gameDirectory`: Speicherort der Client-Dateien (relativ oder absolut)
- `game.maxMemoryMb`: RAM für den Client
- `game.offlineMode`: `true` startet den Client im Offline-Modus
- `game.offlineUsername`: Name für Offline-Login (Default: `Player`)

Alle Dateien werden im Unterordner `servers/<server.name>` abgelegt.
