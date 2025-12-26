# Project Code Dump

This document contains a dump of all the files from the uploaded project.

---

## `MineLauncher/C_launcher.csproj`

```
<Project Sdk="Microsoft.NET.Sdk">
  <PropertyGroup>
    <OutputType>WinExe</OutputType>
    <TargetFramework>net8.0-windows</TargetFramework>
    <UseWindowsForms>true</UseWindowsForms>
    <ImplicitUsings>enable</ImplicitUsings>
    <Nullable>enable</Nullable>
    <RootNamespace>CLauncher</RootNamespace>
  </PropertyGroup>

  <ItemGroup>
    <PackageReference Include="CmlLib.Core" Version="4.0.6" />
  </ItemGroup>

</Project>

```

---

## `MineLauncher/MainForm.cs`

```csharp
using System.Diagnostics;
using CLauncher.Models;
using CLauncher.Services;

namespace CLauncher;

public sealed class MainForm : Form
{
    private readonly string _basePath;
    private readonly ConfigService _configService;
    private readonly DownloadService _downloadService;
    private readonly ServerService _serverService;
    private readonly LauncherService _launcherService;
    private readonly ClientLauncherService _clientLauncherService;

    private LauncherConfig _config;

    private readonly Button _initButton;
    private readonly Button _openConfigButton;
    private readonly Button _downloadButton;
    private readonly Button _launchButton;
    private readonly Button _launchClientButton;
    private readonly Button _showConfigButton;
    private readonly Button _saveConfigButton;
    private readonly TextBox _statusBox;
    private readonly TextBox _configEditor;

    public MainForm(
        string basePath,
        ConfigService configService,
        DownloadService downloadService,
        ServerService serverService,
        LauncherService launcherService,
        ClientLauncherService clientLauncherService)
    {
        _basePath = basePath;
        _configService = configService;
        _downloadService = downloadService;
        _serverService = serverService;
        _launcherService = launcherService;
        _clientLauncherService = clientLauncherService;

        Text = "C_launcher";
        Width = 900;
        Height = 600;
        StartPosition = FormStartPosition.CenterScreen;
        DoubleBuffered = true;
        BackColor = Color.FromArgb(15, 17, 26);
        ForeColor = Color.WhiteSmoke;

        _initButton = new Button { Text = "Config erstellen", Width = 150 };
        _openConfigButton = new Button { Text = "Config öffnen", Width = 150 };
        _downloadButton = new Button { Text = "Server downloaden", Width = 150 };
        _launchButton = new Button { Text = "Server starten", Width = 150 };
        _launchClientButton = new Button { Text = "Client starten", Width = 150 };
        _showConfigButton = new Button { Text = "Config laden", Width = 150 };
        _saveConfigButton = new Button { Text = "Config speichern", Width = 150 };

        StyleButton(_initButton);
        StyleButton(_openConfigButton);
        StyleButton(_downloadButton);
        StyleButton(_launchButton);
        StyleButton(_launchClientButton);
        StyleButton(_showConfigButton);
        StyleButton(_saveConfigButton);

        _statusBox = new TextBox
        {
            Multiline = true,
            ReadOnly = true,
            ScrollBars = ScrollBars.Vertical,
            Dock = DockStyle.Fill,
            BackColor = Color.FromArgb(12, 14, 22),
            ForeColor = Color.Gainsboro,
            BorderStyle = BorderStyle.FixedSingle,
            Font = new Font("Consolas", 10, FontStyle.Regular)
        };

        _configEditor = new TextBox
        {
            Multiline = true,
            ScrollBars = ScrollBars.Vertical,
            Dock = DockStyle.Fill,
            BackColor = Color.FromArgb(12, 14, 22),
            ForeColor = Color.Gainsboro,
            BorderStyle = BorderStyle.FixedSingle,
            Font = new Font("Consolas", 10, FontStyle.Regular)
        };

        var buttonPanel = new FlowLayoutPanel
        {
            Dock = DockStyle.Top,
            Height = 120,
            Padding = new Padding(10),
            AutoSize = false,
            BackColor = Color.FromArgb(30, 34, 48)
        };

        buttonPanel.Controls.AddRange(new Control[]
        {
            _initButton,
            _openConfigButton,
            _showConfigButton,
            _saveConfigButton,
            _downloadButton,
            _launchButton,
            _launchClientButton
        });

        var contentPanel = new SplitContainer
        {
            Dock = DockStyle.Fill,
            Orientation = Orientation.Vertical,
            SplitterDistance = 420,
            BackColor = Color.FromArgb(15, 17, 26)
        };

        contentPanel.Panel1.Controls.Add(_statusBox);
        contentPanel.Panel2.Controls.Add(_configEditor);

        Controls.Add(contentPanel);
        Controls.Add(buttonPanel);

        _initButton.Click += async (_, _) => await RunActionAsync(InitConfigAsync);
        _openConfigButton.Click += (_, _) => OpenConfig();
        _showConfigButton.Click += (_, _) => ShowConfig();
        _saveConfigButton.Click += (_, _) => SaveConfigFromEditor();
        _downloadButton.Click += async (_, _) => await RunActionAsync(DownloadServerAsync);
        _launchButton.Click += async (_, _) => await RunActionAsync(LaunchServerAsync);
        _launchClientButton.Click += async (_, _) => await RunActionAsync(LaunchClientAsync);

        _config = _configService.LoadOrCreate();
        AppendStatus("Launcher bereit. Config geladen.");
        AppendStatus($"Config: {_configService.ConfigPath}");
        LoadConfigToEditor();
    }

    private static void StyleButton(Button button)
    {
        button.Height = 40;
        button.Margin = new Padding(6);
        button.FlatStyle = FlatStyle.Flat;
        button.BackColor = Color.FromArgb(45, 110, 210);
        button.ForeColor = Color.WhiteSmoke;
        button.Font = new Font("Segoe UI", 10, FontStyle.Bold);
        button.FlatAppearance.BorderSize = 1;
        button.FlatAppearance.BorderColor = Color.FromArgb(90, 140, 230);
        button.FlatAppearance.MouseDownBackColor = Color.FromArgb(35, 85, 170);
        button.FlatAppearance.MouseOverBackColor = Color.FromArgb(60, 130, 230);
    }

    private Task InitConfigAsync()
    {
        _configService.Save(_config);
        AppendStatus("Config gespeichert.");
        LoadConfigToEditor();
        return Task.CompletedTask;
    }

    private void OpenConfig()
    {
        if (!File.Exists(_configService.ConfigPath))
        {
            _configService.Save(_config);
        }

        Process.Start(new ProcessStartInfo
        {
            FileName = _configService.ConfigPath,
            UseShellExecute = true
        });
    }

    private void ShowConfig()
    {
        if (!File.Exists(_configService.ConfigPath))
        {
            _configService.Save(_config);
        }

        LoadConfigToEditor();
    }

    private void LoadConfigToEditor()
    {
        _config = _configService.LoadOrCreate();
        _configEditor.Text = _configService.Serialize(_config);
    }

    private void SaveConfigFromEditor()
    {
        try
        {
            var parsedConfig = _configService.Deserialize(_configEditor.Text);
            _configService.Save(parsedConfig);
            _config = parsedConfig;
            AppendStatus("Config gespeichert.");
        }
        catch (Exception ex)
        {
            AppendStatus($"Fehler beim Speichern: {ex.Message}");
        }
    }

    private async Task DownloadServerAsync()
    {
        _config = _configService.LoadOrCreate();

        var serverDir = _serverService.ResolveServerDirectory(_config, _basePath);
        var jarPath = _serverService.GetServerJarPath(_config, serverDir);

        PaperBuildInfo? buildInfo = null;
        string? vanillaUrl = null;
        if (_config.Server.Type.Equals("paper", StringComparison.OrdinalIgnoreCase))
        {
            var build = _config.Server.PaperBuild
                ?? (await _downloadService.GetLatestPaperBuildAsync(_config.Server.Version, CancellationToken.None)).Build;
            buildInfo = new PaperBuildInfo(_config.Server.Version, build);
        }
        else if (_config.Server.Type.Equals("vanilla", StringComparison.OrdinalIgnoreCase))
        {
            vanillaUrl = await _downloadService.GetVanillaServerUrlAsync(_config.Server.Version, CancellationToken.None);
        }

        var url = _serverService.BuildDownloadUrl(_config.Server, buildInfo, vanillaUrl);
        AppendStatus($"Download: {url}");

        await _downloadService.DownloadFileAsync(url, jarPath, CancellationToken.None);
        _serverService.EnsureEula(_config.Server, serverDir);
        _serverService.EnsureServerProperties(_config, serverDir);
        AppendStatus("Download abgeschlossen.");
    }

    private async Task LaunchServerAsync()
    {
        _config = _configService.LoadOrCreate();

        var serverDir = _serverService.ResolveServerDirectory(_config, _basePath);
        var jarPath = _serverService.GetServerJarPath(_config, serverDir);

        if (!File.Exists(jarPath))
        {
            await DownloadServerAsync();
        }

        _serverService.EnsureEula(_config.Server, serverDir);
        _serverService.EnsureServerProperties(_config, serverDir);

        AppendStatus("Server wird gestartet...");
        _launcherService.LaunchServer(_config, serverDir, jarPath);
    }

    private async Task LaunchClientAsync()
    {
        _config = _configService.LoadOrCreate();
        AppendStatus("Client wird gestartet...");
        await _clientLauncherService.LaunchClientAsync(
            _config.Game.OfflineUsername,
            _config.Game.ClientVersion);
    }

    private async Task RunActionAsync(Func<Task> action)
    {
        SetButtonsEnabled(false);
        try
        {
            await action();
        }
        catch (Exception ex)
        {
            AppendStatus($"Fehler: {ex.Message}");
        }
        finally
        {
            SetButtonsEnabled(true);
        }
    }

    private void SetButtonsEnabled(bool enabled)
    {
        _initButton.Enabled = enabled;
        _openConfigButton.Enabled = enabled;
        _showConfigButton.Enabled = enabled;
        _saveConfigButton.Enabled = enabled;
        _downloadButton.Enabled = enabled;
        _launchButton.Enabled = enabled;
        _launchClientButton.Enabled = enabled;
    }

    private void AppendStatus(string message)
    {
        _statusBox.AppendText($"[{DateTime.Now:HH:mm:ss}] {message}{Environment.NewLine}");
    }
}

```

---

## `MineLauncher/Models/LauncherConfig.cs`

```csharp
namespace CLauncher.Models;

public sealed class LauncherConfig
{
    public string InstallRoot { get; set; } = "servers";
    public ServerConfig Server { get; set; } = new();
    public JavaConfig Java { get; set; } = new();
    public GameConfig Game { get; set; } = new();
}

public sealed class ServerConfig
{
    public string Name { get; set; } = "paper-server";
    public string Type { get; set; } = "paper"; // vanilla | paper | bukkit | craftbukkit
    public string Version { get; set; } = "1.20.4";
    public int? PaperBuild { get; set; }
    public bool OnlineMode { get; set; } = true;
    public int Port { get; set; } = 25565;
    public bool AcceptEula { get; set; } = false;
    public string JarFileName { get; set; } = "server.jar";
}

public sealed class JavaConfig
{
    public string Path { get; set; } = "java";
    public string MinMemory { get; set; } = "1G";
    public string MaxMemory { get; set; } = "2G";
}

public sealed class GameConfig
{
    public string ClientVersion { get; set; } = "1.20.4";
    public string GameDirectory { get; set; } = "client";
    public int MaxMemoryMb { get; set; } = 2048;
    public bool OfflineMode { get; set; } = true;
    public string OfflineUsername { get; set; } = "Player";
}

```

---

## `MineLauncher/Program.cs`

```csharp
using CLauncher.Services;

namespace CLauncher;

internal static class Program
{
    [STAThread]
    private static void Main()
    {
        ApplicationConfiguration.Initialize();

        var basePath = Directory.GetCurrentDirectory();
        var configPath = Path.Combine(basePath, "launcher-config.json");

        using var httpClient = new HttpClient();
        var configService = new ConfigService(configPath);
        var downloadService = new DownloadService(httpClient);
        var serverService = new ServerService();
        var launcherService = new LauncherService();
        var clientLauncherService = new ClientLauncherService();

        if (Environment.GetCommandLineArgs().Length > 1)
        {
            var args = Environment.GetCommandLineArgs().Skip(1).ToArray();
            var command = args[0].ToLowerInvariant();
            var config = configService.LoadOrCreate();

            if (command == "play")
            {
                var playerName = args.Length > 1 ? args[1] : config.Game.OfflineUsername;
                clientLauncherService
                    .LaunchClientAsync(playerName, config.Game.ClientVersion)
                    .GetAwaiter()
                    .GetResult();
                return;
            }
        }

        Application.Run(new MainForm(
            basePath,
            configService,
            downloadService,
            serverService,
            launcherService,
            clientLauncherService));
    }
}

```

---

## `MineLauncher/README.md`

```markdown
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

```

---

## `MineLauncher/RPG_Handbuch.md`

```markdown
# MineLauncherRPG – Handbuch (In‑Game)

Dieses Handbuch beschreibt die Bedienung **in‑game** für Spieler und Admins sowie das Erstellen eines Admins.

---

## 1) Admin erstellen (Berechtigungen)

Das Plugin nutzt Bukkit‑Permissions. Du kannst Adminrechte auf zwei Wegen vergeben:

### Variante A: OP (schnell)
1. **Konsole** öffnen.
2. `op <Spielername>` ausführen.
3. Der Spieler hat jetzt Zugriff auf alle `rpg.*` Rechte.

### Variante B: Permissions (empfohlen)
Wenn du ein Permissions‑Plugin nutzt (z. B. LuckPerms):
1. Erstelle eine Gruppe `rpg-admin`.
2. Vergib folgende Permissions:
   - `rpg.admin.*` (volle Adminrechte)
   - oder gezielt:
     - `rpg.admin` (Admin-Menü)
     - `rpg.editor` (Editor‑Werkzeuge)
     - `rpg.debug` (Debug‑Overlay)
     - `rpg.mod` (Moderationstools)
3. Füge den Spieler der Gruppe hinzu.

> **Hinweis:** Ohne Permissions‑Plugin kannst du einzelne Spieler nur über `op` voll berechtigen.

---

## 2) Spieler‑Handbuch (In‑Game)

### 2.1 Hauptmenü öffnen
- **Befehl:** `/rpg`
- Öffnet das zentrale RPG‑Menü mit Charakter, Skills, Quests, Fraktionen.

### 2.2 Charakter & Progression
- **Level & XP** steigen durch:
  - Kämpfe (Mobs)
  - Crafting
  - Blöcke abbauen
- Attribute wirken automatisch auf:
  - Leben, Angriffsschaden, Angriffsgeschwindigkeit, Bewegung

### 2.3 Skills
- **GUI:** `/rpg` → **Skills**
- **Skill lernen:** Klick auf Skill‑Eintrag (benötigt Skillpunkte)
- **Skill nutzen:** `/rpg skill <id>`
  - Beispiele: `dash`, `heal`, `taunt`

### 2.4 Quests
- **GUI:** `/rpg` → **Quests**
- **Quest annehmen:** Klick im Quest‑Menü
- **Quest abbrechen:** `/rpg quest abandon <id>`
- **Quest abschließen (manuell prüfen):** `/rpg quest complete <id>`

### 2.5 Klasse wählen
- **Liste:** `/rpg class list`
- **Wählen:** `/rpg class choose <id>`

### 2.6 Respec (Skill‑Reset)
- **Befehl:** `/rpg respec`
- Setzt Skills/Attribute zurück und gibt Skillpunkte neu aus.

### 2.7 Party
- **Party erstellen:** `/party create`
- **Einladen:** `/party invite <Spieler>`
- **Beitreten:** `/party join <Leader>`
- **Verlassen:** `/party leave`

---

## 3) Admin‑Handbuch (In‑Game)

### 3.1 Admin‑Menü
- **Befehl:** `/rpgadmin`
- Öffnet das Admin‑Dashboard (Zonen, NPCs, Quests, Loot, Skills, Debug).

### 3.2 Editor‑Wand (Regionen setzen)
- **Befehl:** `/rpgadmin wand`
- **Links‑Klick Block:** Pos1 setzen
- **Rechts‑Klick Block:** Pos2 setzen

### 3.3 Zonen (Regionen)
- **Zone erstellen:**
  ```
  /rpgadmin zone create <id>
  ```
  > Nutzt Pos1/Pos2 der Editor‑Wand.
- **Level‑Range setzen:**
  ```
  /rpgadmin zone setlevel <id> <min> <max>
  ```
- **Modifier setzen (Slow/Damage):**
  ```
  /rpgadmin zone setmod <id> <slow> <damage>
  ```

### 3.4 NPCs
- **NPC erstellen:**
  ```
  /rpgadmin npc create <id> <role>
  ```
  **Rollen:** `QUESTGIVER`, `VENDOR`, `TRAINER`, `TELEPORTER`, `BANKER`, `FACTION_AGENT`
- **Dialog setzen:**
  ```
  /rpgadmin npc dialog <id>
  ```
  Danach die Dialogzeile in den Chat schreiben.

### 3.5 Quests
- **Quest erstellen:**
  ```
  /rpgadmin quest create <id> <name>
  ```
- **Quest‑Step hinzufügen:**
  ```
  /rpgadmin quest addstep <id> <type> <target> <amount>
  ```
  **Typen:** `KILL`, `COLLECT`, `TALK`, `EXPLORE`, `CRAFT`, `USE_ITEM`, `DEFEND`, `ESCORT`

### 3.6 Loot‑Tabellen
- **Loot‑Table erstellen:**
  ```
  /rpgadmin loot create <id> <appliesTo>
  ```
  `appliesTo` = z. B. `ZOMBIE`, `SKELETON`
- **Eintrag hinzufügen:**
  ```
  /rpgadmin loot addentry <id> <material> <chance> <min> <max> <rarity>
  ```
  **Rarity:** `COMMON`, `UNCOMMON`, `RARE`, `EPIC`, `LEGENDARY`

### 3.7 Debug‑Overlay
- **GUI:** `/rpgadmin` → Debug
- Zeigt Zone & Quest‑Status in der Actionbar.

---

## 4) Tipps für Live‑Betrieb

- **Audit‑Log:** Änderungen werden in `plugins/RPGPlugin/audit.log` protokolliert.
- **Daten‑Files:**
  - `players.yml`, `quests.yml`, `zones.yml`, `npcs.yml`, `loot.yml`, `skills.yml`, `classes.yml`, `factions.yml`
- **Empfehlung:** Änderungen in‑game vornehmen, dann die YAML‑Dateien in Git versionieren.

---

## 5) Quick‑Start (Minimal)
1. Admin erstellen (`op` oder Permissions).
2. `/rpgadmin wand` → Pos1/Pos2 setzen.
3. `/rpgadmin zone create startzone`
4. `/rpgadmin npc create guide QUESTGIVER`
5. `/rpgadmin quest create starter "Erste Schritte"`
6. `/rpgadmin quest addstep starter KILL ZOMBIE 3`
7. Spieler nutzt `/rpg` → Quests → annehmen.

Fertig! 🎉

```

---

## `MineLauncher/Services/ClientLauncherService.cs`

```csharp
using CmlLib.Core;
using CmlLib.Core.Auth;
using CmlLib.Core.ProcessBuilder;

namespace CLauncher.Services;

public sealed class ClientLauncherService
{
    public async Task LaunchClientAsync(string playerName, string versionString)
    {
        var path = new MinecraftPath(Path.Combine(Directory.GetCurrentDirectory(), "client_files"));
        var launcher = new MinecraftLauncher(path);

        Console.WriteLine($"Initialisiere Launcher in: {path.BasePath}");

        var versions = await launcher.GetAllVersionsAsync();
        var selectedVersion = versions.FirstOrDefault(version => version.Name == versionString);
        if (selectedVersion == null)
        {
            Console.WriteLine($"Version {versionString} nicht lokal gefunden. Versuche Download...");
        }

        Console.WriteLine($"Bereite Start von Version {versionString} vor...");

        var session = MSession.CreateOfflineSession(playerName);

        var launchOption = new MLaunchOption
        {
            Session = session,
            MaximumRamMb = 2048,
            ServerIp = "localhost",
            ServerPort = 25565
        };

        var process = await launcher.InstallAndBuildProcessAsync(versionString, launchOption);

        Console.WriteLine("Starte Minecraft Client...");

        process.Start();

        Console.WriteLine("Client gestartet! Dieses Fenster bleibt offen, bis der Client beendet wird.");
        await process.WaitForExitAsync();
    }
}

```

---

## `MineLauncher/Services/ConfigService.cs`

```csharp
using System.Text.Json;
using CLauncher.Models;

namespace CLauncher.Services;

public sealed class ConfigService
{
    private static readonly JsonSerializerOptions JsonOptions = new()
    {
        WriteIndented = true,
        PropertyNamingPolicy = JsonNamingPolicy.CamelCase
    };

    public string ConfigPath { get; }

    public ConfigService(string configPath)
    {
        ConfigPath = configPath;
    }

    public LauncherConfig LoadOrCreate()
    {
        if (!File.Exists(ConfigPath))
        {
            var config = new LauncherConfig();
            Save(config);
            return config;
        }

        var json = File.ReadAllText(ConfigPath);
        return Deserialize(json);
    }

    public void Save(LauncherConfig config)
    {
        var json = Serialize(config);
        File.WriteAllText(ConfigPath, json);
    }

    public string Serialize(LauncherConfig config)
    {
        return JsonSerializer.Serialize(config, JsonOptions);
    }

    public LauncherConfig Deserialize(string json)
    {
        return JsonSerializer.Deserialize<LauncherConfig>(json, JsonOptions) ?? new LauncherConfig();
    }
}

```

---

## `MineLauncher/Services/DownloadService.cs`

```csharp
using System.Net.Http.Json;

namespace CLauncher.Services;

public sealed class DownloadService
{
    private readonly HttpClient _httpClient;

    public DownloadService(HttpClient httpClient)
    {
        _httpClient = httpClient;
    }

    public async Task DownloadFileAsync(string url, string destinationPath, CancellationToken cancellationToken)
    {
        Directory.CreateDirectory(Path.GetDirectoryName(destinationPath) ?? ".");

        using var response = await _httpClient.GetAsync(url, HttpCompletionOption.ResponseHeadersRead, cancellationToken);
        response.EnsureSuccessStatusCode();

        await using var contentStream = await response.Content.ReadAsStreamAsync(cancellationToken);
        await using var fileStream = new FileStream(destinationPath, FileMode.Create, FileAccess.Write, FileShare.None);
        await contentStream.CopyToAsync(fileStream, cancellationToken);
    }

    public async Task<PaperBuildInfo> GetLatestPaperBuildAsync(string version, CancellationToken cancellationToken)
    {
        var response = await _httpClient.GetFromJsonAsync<PaperVersionResponse>(
            $"https://api.papermc.io/v2/projects/paper/versions/{version}",
            cancellationToken);

        if (response?.Builds is null || response.Builds.Count == 0)
        {
            throw new InvalidOperationException($"No Paper builds found for version {version}.");
        }

        var latestBuild = response.Builds.Max();
        return new PaperBuildInfo(version, latestBuild);
    }

    public async Task<string> GetVanillaServerUrlAsync(string version, CancellationToken cancellationToken)
    {
        var manifest = await _httpClient.GetFromJsonAsync<VersionManifest>(
            "https://piston-meta.mojang.com/mc/game/version_manifest.json",
            cancellationToken);

        var versionEntry = manifest?.Versions.FirstOrDefault(item => item.Id == version);
        if (versionEntry is null)
        {
            throw new InvalidOperationException($"Minecraft version {version} not found in manifest.");
        }

        var versionDetails = await _httpClient.GetFromJsonAsync<VersionDetails>(
            versionEntry.Url,
            cancellationToken);

        var serverUrl = versionDetails?.Downloads?.Server?.Url;
        if (string.IsNullOrWhiteSpace(serverUrl))
        {
            throw new InvalidOperationException($"Server download URL missing for version {version}.");
        }

        return serverUrl;
    }

    private sealed class PaperVersionResponse
    {
        public List<int> Builds { get; set; } = new();
    }

    private sealed class VersionManifest
    {
        public List<VersionEntry> Versions { get; set; } = new();
    }

    private sealed class VersionEntry
    {
        public string Id { get; set; } = string.Empty;
        public string Url { get; set; } = string.Empty;
    }

    private sealed class VersionDetails
    {
        public DownloadSection? Downloads { get; set; }
    }

    private sealed class DownloadSection
    {
        public DownloadItem? Server { get; set; }
    }

    private sealed class DownloadItem
    {
        public string Url { get; set; } = string.Empty;
    }
}

public readonly record struct PaperBuildInfo(string Version, int Build);

```

---

## `MineLauncher/Services/LauncherService.cs`

```csharp
using System.Diagnostics;
using CLauncher.Models;

namespace CLauncher.Services;

public sealed class LauncherService
{
    public void LaunchServer(LauncherConfig config, string serverDirectory, string jarPath)
    {
        var args = $"-Xms{config.Java.MinMemory} -Xmx{config.Java.MaxMemory} -jar \"{jarPath}\" nogui";

        var startInfo = new ProcessStartInfo
        {
            FileName = config.Java.Path,
            Arguments = args,
            WorkingDirectory = serverDirectory,
            UseShellExecute = false
        };

        var process = Process.Start(startInfo);
        if (process is null)
        {
            throw new InvalidOperationException("Failed to start the Java process.");
        }
    }
}

```

---

## `MineLauncher/Services/ServerService.cs`

```csharp
using CLauncher.Models;

namespace CLauncher.Services;

public sealed class ServerService
{
    public string ResolveServerDirectory(LauncherConfig config, string basePath)
    {
        return Path.Combine(basePath, config.InstallRoot, config.Server.Name);
    }

    public string GetServerJarPath(LauncherConfig config, string serverDirectory)
    {
        return Path.Combine(serverDirectory, config.Server.JarFileName);
    }

    public string BuildDownloadUrl(ServerConfig serverConfig, PaperBuildInfo? paperBuildInfo, string? vanillaUrl)
    {
        return serverConfig.Type.ToLowerInvariant() switch
        {
            "paper" => BuildPaperUrl(paperBuildInfo ?? throw new InvalidOperationException("Paper build info missing.")),
            "bukkit" => $"https://download.getbukkit.org/bukkit/bukkit-{serverConfig.Version}.jar",
            "craftbukkit" => $"https://download.getbukkit.org/craftbukkit/craftbukkit-{serverConfig.Version}.jar",
            "vanilla" => vanillaUrl ?? throw new InvalidOperationException("Vanilla server URL missing."),
            _ => throw new InvalidOperationException($"Unknown server type: {serverConfig.Type}")
        };
    }

    public void EnsureServerProperties(LauncherConfig config, string serverDirectory)
    {
        Directory.CreateDirectory(serverDirectory);
        var path = Path.Combine(serverDirectory, "server.properties");

        var properties = new Dictionary<string, string>(StringComparer.OrdinalIgnoreCase)
        {
            ["online-mode"] = config.Server.OnlineMode ? "true" : "false",
            ["server-port"] = config.Server.Port.ToString()
        };

        var existing = new Dictionary<string, string>(StringComparer.OrdinalIgnoreCase);
        if (File.Exists(path))
        {
            foreach (var line in File.ReadAllLines(path))
            {
                if (string.IsNullOrWhiteSpace(line) || line.TrimStart().StartsWith("#", StringComparison.Ordinal))
                {
                    continue;
                }

                var split = line.Split('=', 2);
                if (split.Length == 2)
                {
                    existing[split[0].Trim()] = split[1].Trim();
                }
            }
        }

        foreach (var pair in properties)
        {
            existing[pair.Key] = pair.Value;
        }

        var lines = existing.Select(pair => $"{pair.Key}={pair.Value}");
        File.WriteAllLines(path, lines);
    }

    public void EnsureEula(ServerConfig config, string serverDirectory)
    {
        var path = Path.Combine(serverDirectory, "eula.txt");
        var value = config.AcceptEula ? "true" : "false";
        File.WriteAllText(path, $"# Generated by C_launcher\neula={value}\n");
    }

    private static string BuildPaperUrl(PaperBuildInfo buildInfo)
    {
        return $"https://api.papermc.io/v2/projects/paper/versions/{buildInfo.Version}/builds/{buildInfo.Build}/downloads/paper-{buildInfo.Version}-{buildInfo.Build}.jar";
    }
}

```

---

## `MineLauncher/plugins/RPGPlugin/pom.xml`

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0
                             http://maven.apache.org/xsd/maven-4.0.0.xsd">

    <modelVersion>4.0.0</modelVersion>

    <groupId>com.example</groupId>
    <artifactId>rpg-plugin</artifactId>
    <version>1.0.0</version>
    <name>MineLauncherRPG</name>

    <properties>
        <java.version>17</java.version>
        <paper.version>1.20.4-R0.1-SNAPSHOT</paper.version>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
    </properties>

    <repositories>
        <repository>
            <id>papermc</id>
            <url>https://repo.papermc.io/repository/maven-public/</url>
        </repository>
    </repositories>

    <dependencies>
        <dependency>
            <groupId>io.papermc.paper</groupId>
            <artifactId>paper-api</artifactId>
            <version>${paper.version}</version>
            <scope>provided</scope>
        </dependency>
        <dependency>
            <groupId>com.zaxxer</groupId>
            <artifactId>HikariCP</artifactId>
            <version>5.1.0</version>
        </dependency>
        <dependency>
            <groupId>org.postgresql</groupId>
            <artifactId>postgresql</artifactId>
            <version>42.7.2</version>
        </dependency>
        <dependency>
            <groupId>com.google.code.gson</groupId>
            <artifactId>gson</artifactId>
            <version>2.11.0</version>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-compiler-plugin</artifactId>
                <version>3.11.0</version>
                <configuration>
                    <release>${java.version}</release>
                </configuration>
            </plugin>
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-shade-plugin</artifactId>
                <version>3.5.2</version>
                <executions>
                    <execution>
                        <phase>package</phase>
                        <goals>
                            <goal>shade</goal>
                        </goals>
                        <configuration>
                            <createDependencyReducedPom>false</createDependencyReducedPom>
                            <shadedArtifactAttached>false</shadedArtifactAttached>
                            <outputFile>${project.build.directory}/${project.artifactId}-${project.version}.jar</outputFile>
                            <filters>
                                <filter>
                                    <artifact>*:*</artifact>
                                    <excludes>
                                        <exclude>META-INF/*.SF</exclude>
                                        <exclude>META-INF/*.DSA</exclude>
                                        <exclude>META-INF/*.RSA</exclude>
                                        <exclude>module-info.class</exclude>
                                        <exclude>META-INF/versions/**</exclude>
                                    </excludes>
                                </filter>
                            </filters>
                        </configuration>
                    </execution>
                </executions>
            </plugin>
        </plugins>
    </build>

</project>

```

---

## `MineLauncher/plugins/RPGPlugin/src/main/java/com/example/rpg/RPGPlugin.java`

```java
package com.example.rpg;

import com.example.rpg.command.PartyCommand;
import com.example.rpg.command.RPGAdminCommand;
import com.example.rpg.command.RPGCommand;
import com.example.rpg.command.AuctionCommand;
import com.example.rpg.command.DungeonCommand;
import com.example.rpg.command.GuildCommand;
import com.example.rpg.command.PvpCommand;
import com.example.rpg.command.TradeCommand;
import com.example.rpg.db.DatabaseService;
import com.example.rpg.db.PlayerDao;
import com.example.rpg.db.SqlPlayerDao;
import com.example.rpg.gui.GuiManager;
import com.example.rpg.gui.SkillTreeGui;
import com.example.rpg.listener.ArenaListener;
import com.example.rpg.listener.CombatListener;
import com.example.rpg.listener.CustomMobListener;
import com.example.rpg.listener.DamageIndicatorListener;
import com.example.rpg.listener.GuiListener;
import com.example.rpg.listener.ItemStatListener;
import com.example.rpg.listener.NpcListener;
import com.example.rpg.listener.NpcProtectionListener;
import com.example.rpg.listener.PlayerListener;
import com.example.rpg.listener.ProfessionListener;
import com.example.rpg.listener.SkillHotbarListener;
import com.example.rpg.listener.ZoneListener;
import com.example.rpg.manager.ArenaManager;
import com.example.rpg.manager.BehaviorTreeManager;
import com.example.rpg.manager.ClassManager;
import com.example.rpg.manager.DungeonManager;
import com.example.rpg.manager.GuildManager;
import com.example.rpg.manager.FactionManager;
import com.example.rpg.manager.ItemStatManager;
import com.example.rpg.manager.LootManager;
import com.example.rpg.manager.MobManager;
import com.example.rpg.manager.NpcManager;
import com.example.rpg.manager.PartyManager;
import com.example.rpg.manager.PlayerDataManager;
import com.example.rpg.manager.SkillTreeManager;
import com.example.rpg.manager.QuestManager;
import com.example.rpg.manager.AuctionHouseManager;
import com.example.rpg.manager.ShopManager;
import com.example.rpg.manager.SkillHotbarManager;
import com.example.rpg.manager.SkillManager;
import com.example.rpg.manager.SpawnerManager;
import com.example.rpg.manager.ZoneManager;
import com.example.rpg.manager.TradeManager;
import com.example.rpg.manager.ProfessionManager;
import com.example.rpg.util.ItemGenerator;
import com.example.rpg.util.AuditLog;
import com.example.rpg.util.PromptManager;
import com.example.rpg.skill.SkillEffectRegistry;
import com.example.rpg.skill.SkillEffectType;
import com.example.rpg.skill.effects.DamageEffect;
import com.example.rpg.skill.effects.HealEffect;
import com.example.rpg.skill.effects.ParticleEffect;
import com.example.rpg.skill.effects.PotionStatusEffect;
import com.example.rpg.skill.effects.ProjectileEffect;
import com.example.rpg.skill.effects.SoundEffect;
import com.example.rpg.skill.effects.VelocityEffect;
import com.example.rpg.skill.effects.AggroEffect;
import com.example.rpg.skill.effects.XpEffect;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class RPGPlugin extends JavaPlugin {
    private DatabaseService databaseService;
    private PlayerDataManager playerDataManager;
    private QuestManager questManager;
    private ZoneManager zoneManager;
    private NpcManager npcManager;
    private LootManager lootManager;
    private MobManager mobManager;
    private SkillManager skillManager;
    private SkillHotbarManager skillHotbarManager;
    private ClassManager classManager;
    private SpawnerManager spawnerManager;
    private ShopManager shopManager;
    private AuctionHouseManager auctionHouseManager;
    private TradeManager tradeManager;
    private ProfessionManager professionManager;
    private DungeonManager dungeonManager;
    private ArenaManager arenaManager;
    private BehaviorTreeManager behaviorTreeManager;
    private GuildManager guildManager;
    private FactionManager factionManager;
    private PartyManager partyManager;
    private GuiManager guiManager;
    private SkillTreeGui skillTreeGui;
    private SkillTreeManager skillTreeManager;
    private ItemStatManager itemStatManager;
    private PromptManager promptManager;
    private ItemGenerator itemGenerator;
    private SkillEffectRegistry skillEffects;
    private final Set<UUID> debugPlayers = new HashSet<>();
    private CustomMobListener customMobListener;
    private final java.util.Map<UUID, Long> actionBarErrorUntil = new java.util.HashMap<>();
    private final java.util.Map<UUID, String> actionBarErrorMessage = new java.util.HashMap<>();
    private NamespacedKey questKey;
    private NamespacedKey skillKey;
    private NamespacedKey wandKey;
    private AuditLog auditLog;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        databaseService = new DatabaseService(this);
        databaseService.initTables();
        PlayerDao playerDao = new SqlPlayerDao(databaseService);
        playerDataManager = new PlayerDataManager(this, playerDao);
        questManager = new QuestManager(this);
        zoneManager = new ZoneManager(this);
        npcManager = new NpcManager(this);
        lootManager = new LootManager(this);
        mobManager = new MobManager(this);
        behaviorTreeManager = new BehaviorTreeManager(this);
        skillManager = new SkillManager(this);
        skillHotbarManager = new SkillHotbarManager(playerDataManager);
        classManager = new ClassManager(this);
        factionManager = new FactionManager(this);
        spawnerManager = new SpawnerManager(this);
        shopManager = new ShopManager(this);
        auctionHouseManager = new AuctionHouseManager(this);
        tradeManager = new TradeManager();
        professionManager = new ProfessionManager(this);
        dungeonManager = new DungeonManager(this);
        arenaManager = new ArenaManager(this);
        guildManager = new GuildManager(this);
        partyManager = new PartyManager();
        promptManager = new PromptManager();
        itemStatManager = new ItemStatManager(this);
        itemGenerator = new ItemGenerator(this, itemStatManager);
        questKey = new NamespacedKey(this, "quest_id");
        skillKey = new NamespacedKey(this, "skill_id");
        wandKey = new NamespacedKey(this, "editor_wand");
        skillEffects = new SkillEffectRegistry()
            .register(SkillEffectType.HEAL, new HealEffect())
            .register(SkillEffectType.DAMAGE, new DamageEffect())
            .register(SkillEffectType.PROJECTILE, new ProjectileEffect())
            .register(SkillEffectType.POTION, new PotionStatusEffect())
            .register(SkillEffectType.SOUND, new SoundEffect())
            .register(SkillEffectType.XP, new XpEffect())
            .register(SkillEffectType.PARTICLE, new ParticleEffect())
            .register(SkillEffectType.VELOCITY, new VelocityEffect())
            .register(SkillEffectType.AGGRO, new AggroEffect());
        guiManager = new GuiManager(playerDataManager, questManager, skillManager, classManager, factionManager, questKey, skillKey);
        skillTreeManager = new SkillTreeManager(skillManager);
        skillTreeGui = new SkillTreeGui(this);
        auditLog = new AuditLog(this);

        Bukkit.getPluginManager().registerEvents(new PlayerListener(this), this);
        Bukkit.getPluginManager().registerEvents(new CombatListener(this), this);
        Bukkit.getPluginManager().registerEvents(new ZoneListener(this), this);
        Bukkit.getPluginManager().registerEvents(new DamageIndicatorListener(this), this);
        Bukkit.getPluginManager().registerEvents(new NpcListener(this), this);
        Bukkit.getPluginManager().registerEvents(new NpcProtectionListener(this), this);
        Bukkit.getPluginManager().registerEvents(new GuiListener(this), this);
        Bukkit.getPluginManager().registerEvents(new SkillHotbarListener(this), this);
        Bukkit.getPluginManager().registerEvents(new ProfessionListener(this), this);
        Bukkit.getPluginManager().registerEvents(new ItemStatListener(this), this);
        Bukkit.getPluginManager().registerEvents(new ArenaListener(this), this);
        customMobListener = new CustomMobListener(this);
        Bukkit.getPluginManager().registerEvents(customMobListener, this);

        getCommand("rpg").setExecutor(new RPGCommand(this));
        getCommand("rpgadmin").setExecutor(new RPGAdminCommand(this));
        getCommand("party").setExecutor(new PartyCommand(this));
        getCommand("p").setExecutor(new PartyCommand(this));
        getCommand("auction").setExecutor(new AuctionCommand(this));
        getCommand("trade").setExecutor(new TradeCommand(this));
        getCommand("dungeon").setExecutor(new DungeonCommand(this));
        getCommand("guild").setExecutor(new GuildCommand(this));
        getCommand("g").setExecutor(new GuildCommand(this));
        getCommand("pvp").setExecutor(new PvpCommand(this));

        npcManager.spawnAll();
        startDebugTask();
        startManaRegenTask();
        startHudTask();
    }

    @Override
    public void onDisable() {
        if (playerDataManager != null) {
            playerDataManager.saveAll();
        }
        if (questManager != null) {
            questManager.saveAll();
        }
        if (zoneManager != null) {
            zoneManager.saveAll();
        }
        if (npcManager != null) {
            npcManager.saveAll();
        }
        if (lootManager != null) {
            lootManager.saveAll();
        }
        if (mobManager != null) {
            mobManager.saveAll();
        }
        if (skillManager != null) {
            skillManager.saveAll();
        }
        if (classManager != null) {
            classManager.saveAll();
        }
        if (factionManager != null) {
            factionManager.saveAll();
        }
        if (spawnerManager != null) {
            spawnerManager.saveAll();
        }
        if (shopManager != null) {
            shopManager.saveAll();
        }
        if (auctionHouseManager != null) {
            auctionHouseManager.listings().values().forEach(auctionHouseManager::saveListing);
        }
        if (guildManager != null) {
            guildManager.saveAll();
        }
        if (dungeonManager != null) {
            getLogger().info("Cleaning up dungeon worlds...");
            dungeonManager.shutdown();
        }
        if (databaseService != null) {
            databaseService.shutdown();
        }
    }

    public PlayerDataManager playerDataManager() {
        return playerDataManager;
    }

    public QuestManager questManager() {
        return questManager;
    }

    public ZoneManager zoneManager() {
        return zoneManager;
    }

    public NpcManager npcManager() {
        return npcManager;
    }

    public LootManager lootManager() {
        return lootManager;
    }

    public MobManager mobManager() {
        return mobManager;
    }

    public BehaviorTreeManager behaviorTreeManager() {
        return behaviorTreeManager;
    }

    public SkillManager skillManager() {
        return skillManager;
    }

    public SkillHotbarManager skillHotbarManager() {
        return skillHotbarManager;
    }

    public ClassManager classManager() {
        return classManager;
    }

    public FactionManager factionManager() {
        return factionManager;
    }

    public PartyManager partyManager() {
        return partyManager;
    }

    public GuiManager guiManager() {
        return guiManager;
    }

    public PromptManager promptManager() {
        return promptManager;
    }

    public ItemGenerator itemGenerator() {
        return itemGenerator;
    }

    public SkillEffectRegistry skillEffects() {
        return skillEffects;
    }

    public SpawnerManager spawnerManager() {
        return spawnerManager;
    }

    public ShopManager shopManager() {
        return shopManager;
    }

    public AuctionHouseManager auctionHouseManager() {
        return auctionHouseManager;
    }

    public TradeManager tradeManager() {
        return tradeManager;
    }

    public ProfessionManager professionManager() {
        return professionManager;
    }

    public DungeonManager dungeonManager() {
        return dungeonManager;
    }

    public ArenaManager arenaManager() {
        return arenaManager;
    }

    public GuildManager guildManager() {
        return guildManager;
    }

    public SkillTreeManager skillTreeManager() {
        return skillTreeManager;
    }

    public SkillTreeGui skillTreeGui() {
        return skillTreeGui;
    }

    public ItemStatManager itemStatManager() {
        return itemStatManager;
    }

    public CustomMobListener customMobListener() {
        return customMobListener;
    }

    public AuditLog auditLog() {
        return auditLog;
    }

    public NamespacedKey questKey() {
        return questKey;
    }

    public NamespacedKey skillKey() {
        return skillKey;
    }

    public NamespacedKey wandKey() {
        return wandKey;
    }

    public boolean useSkill(Player player, String skillId) {
        var skill = skillManager.getSkill(skillId);
        if (skill == null) {
            notifySkillError(player, "Unbekannter Skill");
            return false;
        }
        var profile = playerDataManager.getProfile(player);
        if (!profile.learnedSkills().containsKey(skillId)) {
            notifySkillError(player, "Skill nicht gelernt");
            return false;
        }
        if (skill.type() == com.example.rpg.model.SkillType.PASSIVE) {
            notifySkillError(player, "Passiver Skill ist aktiv");
            return false;
        }
        long now = System.currentTimeMillis();
        long last = profile.skillCooldowns().getOrDefault(skillId, 0L);
        if (now - last < skill.cooldown() * 1000L) {
            long remaining = (skill.cooldown() * 1000L - (now - last)) / 1000L;
            notifySkillError(player, "Cooldown: " + remaining + "s");
            return false;
        }
        if (profile.mana() < skill.manaCost()) {
            notifySkillError(player, "Nicht genug Mana");
            return false;
        }
        profile.setMana(profile.mana() - skill.manaCost());
        for (var effect : skill.effects()) {
            skillEffects.apply(effect, player, profile);
        }
        profile.skillCooldowns().put(skillId, now);
        player.sendMessage("§aSkill benutzt: " + skill.name());
        return true;
    }

    public boolean useMobSkill(org.bukkit.entity.LivingEntity caster, Player target, String skillId) {
        var skill = skillManager.getSkill(skillId);
        if (skill == null) {
            return false;
        }
        for (var effect : skill.effects()) {
            switch (effect.type()) {
                case DAMAGE -> {
                    double amount = parseDouble(effect.params().getOrDefault("amount", 4));
                    target.damage(amount, caster);
                }
                case PROJECTILE -> {
                    String type = String.valueOf(effect.params().getOrDefault("type", "SNOWBALL")).toUpperCase();
                    if ("SMALL_FIREBALL".equals(type)) {
                        caster.launchProjectile(org.bukkit.entity.SmallFireball.class);
                    } else {
                        caster.launchProjectile(org.bukkit.entity.Snowball.class);
                    }
                }
                case POTION -> {
                    String type = String.valueOf(effect.params().getOrDefault("type", "SLOW")).toUpperCase();
                    int duration = (int) parseDouble(effect.params().getOrDefault("duration", 60));
                    int amplifier = (int) parseDouble(effect.params().getOrDefault("amplifier", 0));
                    var potion = org.bukkit.potion.PotionEffectType.getByName(type);
                    if (potion != null) {
                        target.addPotionEffect(new org.bukkit.potion.PotionEffect(potion, duration, amplifier));
                    }
                }
                case PARTICLE -> {
                    String particleName = String.valueOf(effect.params().getOrDefault("type", "SMOKE")).toUpperCase();
                    int count = (int) parseDouble(effect.params().getOrDefault("count", 10));
                    double speed = parseDouble(effect.params().getOrDefault("speed", 0.01));
                    org.bukkit.Particle particle;
                    try {
                        particle = org.bukkit.Particle.valueOf(particleName);
                    } catch (IllegalArgumentException e) {
                        particle = org.bukkit.Particle.SMOKE_NORMAL;
                    }
                    caster.getWorld().spawnParticle(particle, caster.getLocation().add(0, 1.0, 0), count, 0.3, 0.6, 0.3, speed);
                }
                case SOUND -> {
                    String soundName = String.valueOf(effect.params().getOrDefault("sound", "ENTITY_ZOMBIE_HURT")).toUpperCase();
                    float volume = (float) parseDouble(effect.params().getOrDefault("volume", 1.0));
                    float pitch = (float) parseDouble(effect.params().getOrDefault("pitch", 1.0));
                    org.bukkit.Sound sound;
                    try {
                        sound = org.bukkit.Sound.valueOf(soundName);
                    } catch (IllegalArgumentException e) {
                        sound = org.bukkit.Sound.ENTITY_ZOMBIE_HURT;
                    }
                    caster.getWorld().playSound(caster.getLocation(), sound, volume, pitch);
                }
                default -> {
                }
            }
        }
        return true;
    }

    private double parseDouble(Object raw) {
        try {
            return Double.parseDouble(String.valueOf(raw));
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }

    public void notifySkillError(Player player, String message) {
        actionBarErrorUntil.put(player.getUniqueId(), System.currentTimeMillis() + 2000L);
        actionBarErrorMessage.put(player.getUniqueId(), message);
        player.sendActionBar("§c" + message);
    }

    private void startHudTask() {
        Bukkit.getScheduler().runTaskTimer(this, () -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                var profile = playerDataManager.getProfile(player);
                String health = String.format("❤ Leben: %.0f/%.0f", player.getHealth(),
                    player.getAttribute(org.bukkit.attribute.Attribute.GENERIC_MAX_HEALTH) != null
                        ? player.getAttribute(org.bukkit.attribute.Attribute.GENERIC_MAX_HEALTH).getValue()
                        : 20.0);
                String mana = "🔵 Mana: " + profile.mana() + "/" + profile.maxMana();
                String gold = "💰 " + profile.gold();
                Long until = actionBarErrorUntil.get(player.getUniqueId());
                if (until != null && until > System.currentTimeMillis()) {
                    String msg = actionBarErrorMessage.getOrDefault(player.getUniqueId(), "Fehler");
                    player.sendActionBar("§c" + msg);
                } else {
                    player.sendActionBar("§f" + health + " §7| §f" + mana + " §7| §6" + gold);
                }

                int slot = player.getInventory().getHeldItemSlot() + 1;
                String skillId = skillHotbarManager.getBinding(profile, slot);
                if (skillId != null) {
                    var skill = skillManager.getSkill(skillId);
                    if (skill != null && skill.cooldown() > 0) {
                        long last = profile.skillCooldowns().getOrDefault(skillId, 0L);
                        long remaining = skill.cooldown() * 1000L - (System.currentTimeMillis() - last);
                        if (remaining > 0) {
                            float progress = Math.max(0f, Math.min(1f, remaining / (skill.cooldown() * 1000f)));
                            player.setExp(progress);
                            player.setLevel((int) Math.ceil(remaining / 1000f));
                        } else {
                            player.setExp(0f);
                            player.setLevel(profile.level());
                        }
                    }
                }
            }
        }, 10L, 10L);
    }

    public boolean toggleDebug(UUID uuid) {
        if (debugPlayers.contains(uuid)) {
            debugPlayers.remove(uuid);
            return false;
        }
        debugPlayers.add(uuid);
        return true;
    }

    private void startDebugTask() {
        Bukkit.getScheduler().runTaskTimer(this, () -> {
            for (UUID uuid : debugPlayers) {
                Player player = Bukkit.getPlayer(uuid);
                if (player == null) {
                    continue;
                }
                var zone = zoneManager.getZoneAt(player.getLocation());
                String zoneName = zone != null ? zone.name() : "Keine Zone";
                player.sendActionBar("§7Zone: §f" + zoneName + " §7Quest: §f" + playerDataManager.getProfile(player).activeQuests().size());
            }
        }, 20L, 40L);
    }

    private void startManaRegenTask() {
        Bukkit.getScheduler().runTaskTimer(this, () -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                var profile = playerDataManager.getProfile(player);
                profile.setMana(Math.min(profile.maxMana(), profile.mana() + 5));
            }
        }, 20L, 40L);
    }

    public boolean completeQuestIfReady(Player player, com.example.rpg.model.Quest quest, com.example.rpg.model.QuestProgress progress) {
        if (progress.completed()) {
            return false;
        }
        boolean done = true;
        for (int i = 0; i < quest.steps().size(); i++) {
            int required = quest.steps().get(i).amount();
            if (progress.getStepProgress(i) < required) {
                done = false;
                break;
            }
        }
        if (!done) {
            return false;
        }
        progress.setCompleted(true);
        var profile = playerDataManager.getProfile(player);
        profile.completedQuests().add(quest.id());
        profile.activeQuests().remove(quest.id());
        profile.addXp(quest.reward().xp());
        profile.setSkillPoints(profile.skillPoints() + quest.reward().skillPoints());
        quest.reward().factionRep().forEach((id, amount) ->
            profile.factionRep().put(id, profile.factionRep().getOrDefault(id, 0) + amount)
        );
        player.sendMessage("§aQuest abgeschlossen: " + quest.name());
        return true;
    }
}

```

---

## `MineLauncher/plugins/RPGPlugin/src/main/java/com/example/rpg/behavior/BehaviorContext.java`

```java
package com.example.rpg.behavior;

import com.example.rpg.RPGPlugin;
import com.example.rpg.model.MobDefinition;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

public class BehaviorContext {
    private final RPGPlugin plugin;
    private final LivingEntity mob;
    private final MobDefinition definition;
    private Player target;
    private final Map<String, Long> cooldowns = new HashMap<>();

    public BehaviorContext(RPGPlugin plugin, LivingEntity mob, MobDefinition definition) {
        this.plugin = plugin;
        this.mob = mob;
        this.definition = definition;
    }

    public RPGPlugin plugin() {
        return plugin;
    }

    public LivingEntity mob() {
        return mob;
    }

    public MobDefinition definition() {
        return definition;
    }

    public Player target() {
        return target;
    }

    public void setTarget(Player target) {
        this.target = target;
    }

    public Map<String, Long> cooldowns() {
        return cooldowns;
    }

    public UUID mobId() {
        return mob.getUniqueId();
    }
}

```

---

## `MineLauncher/plugins/RPGPlugin/src/main/java/com/example/rpg/behavior/BehaviorNode.java`

```java
package com.example.rpg.behavior;

import java.util.UUID;

public abstract class BehaviorNode {
    private final String id;

    protected BehaviorNode(String id) {
        this.id = id;
    }

    public String id() {
        return id;
    }

    public abstract BehaviorStatus tick(BehaviorContext context);

    protected String key(UUID entityId) {
        return id + ":" + entityId;
    }
}

```

---

## `MineLauncher/plugins/RPGPlugin/src/main/java/com/example/rpg/behavior/BehaviorStatus.java`

```java
package com.example.rpg.behavior;

public enum BehaviorStatus {
    SUCCESS,
    FAILURE,
    RUNNING
}

```

---

## `MineLauncher/plugins/RPGPlugin/src/main/java/com/example/rpg/behavior/CastSkillNode.java`

```java
package com.example.rpg.behavior;

import org.bukkit.entity.Player;

public class CastSkillNode extends BehaviorNode {
    private final String skillId;

    public CastSkillNode(String id, String skillId) {
        super(id);
        this.skillId = skillId;
    }

    @Override
    public BehaviorStatus tick(BehaviorContext context) {
        Player target = context.target();
        if (target == null) {
            return BehaviorStatus.FAILURE;
        }
        boolean success = context.plugin().useMobSkill(context.mob(), target, skillId);
        return success ? BehaviorStatus.SUCCESS : BehaviorStatus.FAILURE;
    }
}

```

---

## `MineLauncher/plugins/RPGPlugin/src/main/java/com/example/rpg/behavior/CompositeNode.java`

```java
package com.example.rpg.behavior;

import java.util.ArrayList;
import java.util.List;

public abstract class CompositeNode extends BehaviorNode {
    private final List<BehaviorNode> children = new ArrayList<>();

    protected CompositeNode(String id) {
        super(id);
    }

    public List<BehaviorNode> children() {
        return children;
    }
}

```

---

## `MineLauncher/plugins/RPGPlugin/src/main/java/com/example/rpg/behavior/CooldownNode.java`

```java
package com.example.rpg.behavior;

public class CooldownNode extends BehaviorNode {
    private final BehaviorNode child;
    private final long cooldownMillis;

    public CooldownNode(String id, BehaviorNode child, long cooldownMillis) {
        super(id);
        this.child = child;
        this.cooldownMillis = cooldownMillis;
    }

    @Override
    public BehaviorStatus tick(BehaviorContext context) {
        long now = System.currentTimeMillis();
        String key = key(context.mobId());
        Long last = context.cooldowns().get(key);
        if (last != null && now - last < cooldownMillis) {
            return BehaviorStatus.FAILURE;
        }
        BehaviorStatus status = child.tick(context);
        if (status == BehaviorStatus.SUCCESS) {
            context.cooldowns().put(key, now);
        }
        return status;
    }
}

```

---

## `MineLauncher/plugins/RPGPlugin/src/main/java/com/example/rpg/behavior/FleeNode.java`

```java
package com.example.rpg.behavior;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

public class FleeNode extends BehaviorNode {
    public FleeNode(String id) {
        super(id);
    }

    @Override
    public BehaviorStatus tick(BehaviorContext context) {
        Player target = context.target();
        if (target == null) {
            return BehaviorStatus.FAILURE;
        }
        Location mobLoc = context.mob().getLocation();
        Location targetLoc = target.getLocation();
        Vector direction = mobLoc.toVector().subtract(targetLoc.toVector());
        if (direction.lengthSquared() == 0) {
            return BehaviorStatus.FAILURE;
        }
        direction.normalize().multiply(0.35);
        context.mob().setVelocity(direction);
        return BehaviorStatus.RUNNING;
    }
}

```

---

## `MineLauncher/plugins/RPGPlugin/src/main/java/com/example/rpg/behavior/HealSelfNode.java`

```java
package com.example.rpg.behavior;

public class HealSelfNode extends BehaviorNode {
    private final double amount;

    public HealSelfNode(String id, double amount) {
        super(id);
        this.amount = amount;
    }

    @Override
    public BehaviorStatus tick(BehaviorContext context) {
        double maxHealth = Math.max(1, context.definition().health());
        double next = Math.min(maxHealth, context.mob().getHealth() + amount);
        context.mob().setHealth(next);
        return BehaviorStatus.SUCCESS;
    }
}

```

---

## `MineLauncher/plugins/RPGPlugin/src/main/java/com/example/rpg/behavior/HealthBelowNode.java`

```java
package com.example.rpg.behavior;

public class HealthBelowNode extends BehaviorNode {
    private final double threshold;

    public HealthBelowNode(String id, double threshold) {
        super(id);
        this.threshold = threshold;
    }

    @Override
    public BehaviorStatus tick(BehaviorContext context) {
        double maxHealth = Math.max(1, context.definition().health());
        double current = context.mob().getHealth();
        return (current / maxHealth) < threshold ? BehaviorStatus.SUCCESS : BehaviorStatus.FAILURE;
    }
}

```

---

## `MineLauncher/plugins/RPGPlugin/src/main/java/com/example/rpg/behavior/InverterNode.java`

```java
package com.example.rpg.behavior;

public class InverterNode extends BehaviorNode {
    private final BehaviorNode child;

    public InverterNode(String id, BehaviorNode child) {
        super(id);
        this.child = child;
    }

    @Override
    public BehaviorStatus tick(BehaviorContext context) {
        BehaviorStatus status = child.tick(context);
        return switch (status) {
            case SUCCESS -> BehaviorStatus.FAILURE;
            case FAILURE -> BehaviorStatus.SUCCESS;
            case RUNNING -> BehaviorStatus.RUNNING;
        };
    }
}

```

---

## `MineLauncher/plugins/RPGPlugin/src/main/java/com/example/rpg/behavior/MeleeAttackNode.java`

```java
package com.example.rpg.behavior;

import org.bukkit.entity.Player;

public class MeleeAttackNode extends BehaviorNode {
    public MeleeAttackNode(String id) {
        super(id);
    }

    @Override
    public BehaviorStatus tick(BehaviorContext context) {
        Player target = context.target();
        if (target == null) {
            return BehaviorStatus.FAILURE;
        }
        if (target.getLocation().distanceSquared(context.mob().getLocation()) > 9) {
            return BehaviorStatus.FAILURE;
        }
        target.damage(context.definition().damage(), context.mob());
        return BehaviorStatus.SUCCESS;
    }
}

```

---

## `MineLauncher/plugins/RPGPlugin/src/main/java/com/example/rpg/behavior/SelectorNode.java`

```java
package com.example.rpg.behavior;

public class SelectorNode extends CompositeNode {
    public SelectorNode(String id) {
        super(id);
    }

    @Override
    public BehaviorStatus tick(BehaviorContext context) {
        for (BehaviorNode child : children()) {
            BehaviorStatus status = child.tick(context);
            if (status == BehaviorStatus.SUCCESS || status == BehaviorStatus.RUNNING) {
                return status;
            }
        }
        return BehaviorStatus.FAILURE;
    }
}

```

---

## `MineLauncher/plugins/RPGPlugin/src/main/java/com/example/rpg/behavior/SequenceNode.java`

```java
package com.example.rpg.behavior;

public class SequenceNode extends CompositeNode {
    public SequenceNode(String id) {
        super(id);
    }

    @Override
    public BehaviorStatus tick(BehaviorContext context) {
        for (BehaviorNode child : children()) {
            BehaviorStatus status = child.tick(context);
            if (status == BehaviorStatus.FAILURE) {
                return BehaviorStatus.FAILURE;
            }
            if (status == BehaviorStatus.RUNNING) {
                return BehaviorStatus.RUNNING;
            }
        }
        return BehaviorStatus.SUCCESS;
    }
}

```

---

## `MineLauncher/plugins/RPGPlugin/src/main/java/com/example/rpg/behavior/TargetDistanceAboveNode.java`

```java
package com.example.rpg.behavior;

import org.bukkit.entity.Player;

public class TargetDistanceAboveNode extends BehaviorNode {
    private final double distance;

    public TargetDistanceAboveNode(String id, double distance) {
        super(id);
        this.distance = distance;
    }

    @Override
    public BehaviorStatus tick(BehaviorContext context) {
        Player target = context.target();
        if (target == null) {
            return BehaviorStatus.FAILURE;
        }
        double dist = target.getLocation().distance(context.mob().getLocation());
        return dist > distance ? BehaviorStatus.SUCCESS : BehaviorStatus.FAILURE;
    }
}

```

---

## `MineLauncher/plugins/RPGPlugin/src/main/java/com/example/rpg/behavior/WalkToTargetNode.java`

```java
package com.example.rpg.behavior;

import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;

public class WalkToTargetNode extends BehaviorNode {
    public WalkToTargetNode(String id) {
        super(id);
    }

    @Override
    public BehaviorStatus tick(BehaviorContext context) {
        Player target = context.target();
        if (target == null) {
            return BehaviorStatus.FAILURE;
        }
        if (context.mob() instanceof Mob mob) {
            mob.setTarget(target);
            mob.getPathfinder().moveTo(target);
            return BehaviorStatus.RUNNING;
        }
        return BehaviorStatus.FAILURE;
    }
}

```

---

## `MineLauncher/plugins/RPGPlugin/src/main/java/com/example/rpg/command/AuctionCommand.java`

```java
package com.example.rpg.command;

import com.example.rpg.RPGPlugin;
import com.example.rpg.model.AuctionListing;
import com.example.rpg.util.Text;
import java.util.UUID;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class AuctionCommand implements CommandExecutor {
    private final RPGPlugin plugin;

    public AuctionCommand(RPGPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Nur Spieler.");
            return true;
        }
        if (args.length < 1) {
            player.sendMessage(Text.mm("<gray>/auction <list|sell|buy>"));
            return true;
        }
        switch (args[0].toLowerCase()) {
            case "list" -> listAuctions(player);
            case "sell" -> sellAuction(player, args);
            case "buy" -> buyAuction(player, args);
            default -> player.sendMessage(Text.mm("<gray>/auction <list|sell|buy>"));
        }
        return true;
    }

    private void listAuctions(Player player) {
        if (plugin.auctionHouseManager().listings().isEmpty()) {
            player.sendMessage(Text.mm("<yellow>Keine Auktionen verfügbar."));
            return;
        }
        player.sendMessage(Text.mm("<gold>Auktionen:"));
        for (AuctionListing listing : plugin.auctionHouseManager().listings().values()) {
            player.sendMessage(Text.mm("<gray>" + listing.id() + " - <gold>" + listing.price() + "</gold> Gold"));
        }
    }

    private void sellAuction(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(Text.mm("<gray>/auction sell <price>"));
            return;
        }
        int price;
        try {
            price = Integer.parseInt(args[1]);
        } catch (NumberFormatException e) {
            player.sendMessage(Text.mm("<red>Preis ungültig."));
            return;
        }
        if (price <= 0) {
            player.sendMessage(Text.mm("<red>Preis muss > 0 sein."));
            return;
        }
        ItemStack item = player.getInventory().getItemInMainHand();
        if (item == null || item.getType().isAir()) {
            player.sendMessage(Text.mm("<red>Halte ein Item in der Hand."));
            return;
        }
        String data = plugin.auctionHouseManager().serializeItem(item);
        if (data == null) {
            player.sendMessage(Text.mm("<red>Item konnte nicht gespeichert werden."));
            return;
        }
        String id = UUID.randomUUID().toString().substring(0, 8);
        AuctionListing listing = new AuctionListing(id);
        listing.setSeller(player.getUniqueId());
        listing.setPrice(price);
        listing.setItemData(data);
        plugin.auctionHouseManager().addListing(listing);
        player.getInventory().setItemInMainHand(null);
        player.sendMessage(Text.mm("<green>Auktion erstellt: " + id));
    }

    private void buyAuction(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(Text.mm("<gray>/auction buy <id>"));
            return;
        }
        String id = args[1];
        AuctionListing listing = plugin.auctionHouseManager().getListing(id);
        if (listing == null) {
            player.sendMessage(Text.mm("<red>Auktion nicht gefunden."));
            return;
        }
        var buyerProfile = plugin.playerDataManager().getProfile(player);
        if (buyerProfile.gold() < listing.price()) {
            player.sendMessage(Text.mm("<red>Nicht genug Gold."));
            return;
        }
        ItemStack item = plugin.auctionHouseManager().deserializeItem(listing.itemData());
        if (item == null) {
            player.sendMessage(Text.mm("<red>Item nicht verfügbar."));
            return;
        }
        buyerProfile.setGold(buyerProfile.gold() - listing.price());
        player.getInventory().addItem(item);
        if (listing.seller() != null) {
            var seller = plugin.getServer().getPlayer(listing.seller());
            if (seller != null) {
                var sellerProfile = plugin.playerDataManager().getProfile(seller);
                sellerProfile.setGold(sellerProfile.gold() + listing.price());
                seller.sendMessage(Text.mm("<green>Dein Item wurde verkauft für " + listing.price() + " Gold."));
            }
        }
        plugin.auctionHouseManager().removeListing(id);
        player.sendMessage(Text.mm("<green>Item gekauft."));
    }
}

```

---

## `MineLauncher/plugins/RPGPlugin/src/main/java/com/example/rpg/command/DungeonCommand.java`

```java
package com.example.rpg.command;

import com.example.rpg.RPGPlugin;
import com.example.rpg.util.Text;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class DungeonCommand implements CommandExecutor {
    private final RPGPlugin plugin;

    public DungeonCommand(RPGPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Nur Spieler.");
            return true;
        }
        if (args.length < 1) {
            player.sendMessage(Text.mm("<gray>/dungeon <enter|leave|generate>"));
            return true;
        }
        switch (args[0].toLowerCase()) {
            case "enter" -> enterDungeon(player);
            case "leave" -> leaveDungeon(player);
            case "generate" -> generateDungeon(player, args);
            default -> player.sendMessage(Text.mm("<gray>/dungeon <enter|leave|generate>"));
        }
        return true;
    }

    private void enterDungeon(Player player) {
        Location spawn = plugin.dungeonManager().getEntrance();
        if (spawn == null) {
            player.sendMessage(Text.mm("<red>Dungeon nicht konfiguriert."));
            return;
        }
        plugin.dungeonManager().enterDungeon(player);
        player.sendMessage(Text.mm("<green>Dungeon betreten."));
    }

    private void leaveDungeon(Player player) {
        plugin.dungeonManager().leaveDungeon(player);
        player.sendMessage(Text.mm("<yellow>Dungeon verlassen."));
    }

    private void generateDungeon(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(Text.mm("<gray>/dungeon generate <theme>"));
            return;
        }
        String theme = args[1];
        var party = plugin.partyManager().getParty(player.getUniqueId());
        java.util.List<Player> members = new java.util.ArrayList<>();
        if (party.isPresent()) {
            for (java.util.UUID memberId : party.get().members()) {
                Player member = player.getServer().getPlayer(memberId);
                if (member != null) {
                    members.add(member);
                }
            }
            Player leader = player.getServer().getPlayer(party.get().leader());
            if (leader != null && !members.contains(leader)) {
                members.add(leader);
            }
        } else {
            members.add(player);
        }
        plugin.dungeonManager().generateDungeon(player, theme, members);
    }
}

```

---

## `MineLauncher/plugins/RPGPlugin/src/main/java/com/example/rpg/command/GuildCommand.java`

```java
package com.example.rpg.command;

import com.example.rpg.RPGPlugin;
import com.example.rpg.model.Guild;
import com.example.rpg.model.GuildMemberRole;
import com.example.rpg.model.GuildQuest;
import com.example.rpg.model.PlayerProfile;
import com.example.rpg.util.Text;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class GuildCommand implements CommandExecutor {
    private final RPGPlugin plugin;

    public GuildCommand(RPGPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Nur Spieler.");
            return true;
        }
        if (label.equalsIgnoreCase("g")) {
            if (args.length == 0) {
                player.sendMessage(Text.mm("<gray>/g <message>"));
                return true;
            }
            String message = join(args, 0);
            guildChat(player, new String[] {"chat", message});
            return true;
        }
        if (args.length == 0) {
            player.sendMessage(Text.mm("<gray>/guild <create|invite|accept|leave|disband|info|chat|bank|quest>"));
            return true;
        }
        switch (args[0].toLowerCase()) {
            case "create" -> createGuild(player, args);
            case "invite" -> invitePlayer(player, args);
            case "accept" -> acceptInvite(player);
            case "leave" -> leaveGuild(player);
            case "disband" -> disbandGuild(player);
            case "info" -> guildInfo(player);
            case "chat" -> guildChat(player, args);
            case "bank" -> bankCommand(player, args);
            case "quest" -> questCommand(player, args);
            default -> player.sendMessage(Text.mm("<gray>/guild <create|invite|accept|leave|disband|info|chat|bank|quest>"));
        }
        return true;
    }

    private void createGuild(Player player, String[] args) {
        if (args.length < 3) {
            player.sendMessage(Text.mm("<gray>/guild create <id> <name>"));
            return;
        }
        if (plugin.guildManager().isMember(player.getUniqueId())) {
            player.sendMessage(Text.mm("<red>Du bist bereits in einer Gilde."));
            return;
        }
        String id = args[1].toLowerCase();
        String name = join(args, 2);
        if (plugin.guildManager().guildById(id).isPresent()) {
            player.sendMessage(Text.mm("<red>Gilden-ID existiert bereits."));
            return;
        }
        plugin.guildManager().createGuild(id, name, player);
        player.sendMessage(Text.mm("<green>Gilde erstellt: " + name));
    }

    private void invitePlayer(Player player, String[] args) {
        Optional<Guild> guildOpt = plugin.guildManager().guildFor(player.getUniqueId());
        if (guildOpt.isEmpty()) {
            player.sendMessage(Text.mm("<red>Du bist in keiner Gilde."));
            return;
        }
        Guild guild = guildOpt.get();
        if (!isOfficerOrLeader(guild, player.getUniqueId())) {
            player.sendMessage(Text.mm("<red>Keine Rechte, um einzuladen."));
            return;
        }
        if (args.length < 2) {
            player.sendMessage(Text.mm("<gray>/guild invite <player>"));
            return;
        }
        Player target = player.getServer().getPlayer(args[1]);
        if (target == null) {
            player.sendMessage(Text.mm("<red>Spieler nicht online."));
            return;
        }
        if (plugin.guildManager().isMember(target.getUniqueId())) {
            player.sendMessage(Text.mm("<red>Spieler ist bereits in einer Gilde."));
            return;
        }
        plugin.guildManager().invite(target.getUniqueId(), guild.id());
        player.sendMessage(Text.mm("<green>Einladung gesendet."));
        target.sendMessage(Text.mm("<yellow>Gildeneinladung von " + guild.name() + ". /guild accept"));
    }

    private void acceptInvite(Player player) {
        if (plugin.guildManager().isMember(player.getUniqueId())) {
            player.sendMessage(Text.mm("<red>Du bist bereits in einer Gilde."));
            return;
        }
        Optional<Guild> guild = plugin.guildManager().acceptInvite(player.getUniqueId());
        if (guild.isEmpty()) {
            player.sendMessage(Text.mm("<red>Keine Einladung gefunden."));
            return;
        }
        player.sendMessage(Text.mm("<green>Du bist der Gilde beigetreten."));
    }

    private void leaveGuild(Player player) {
        if (!plugin.guildManager().isMember(player.getUniqueId())) {
            player.sendMessage(Text.mm("<red>Du bist in keiner Gilde."));
            return;
        }
        plugin.guildManager().leaveGuild(player.getUniqueId());
        player.sendMessage(Text.mm("<yellow>Gilde verlassen."));
    }

    private void disbandGuild(Player player) {
        Optional<Guild> guildOpt = plugin.guildManager().guildFor(player.getUniqueId());
        if (guildOpt.isEmpty()) {
            player.sendMessage(Text.mm("<red>Du bist in keiner Gilde."));
            return;
        }
        Guild guild = guildOpt.get();
        if (!guild.leader().equals(player.getUniqueId())) {
            player.sendMessage(Text.mm("<red>Nur der Gildenleiter kann auflösen."));
            return;
        }
        plugin.guildManager().disbandGuild(guild);
        player.sendMessage(Text.mm("<yellow>Gilde aufgelöst."));
    }

    private void guildInfo(Player player) {
        Optional<Guild> guildOpt = plugin.guildManager().guildFor(player.getUniqueId());
        if (guildOpt.isEmpty()) {
            player.sendMessage(Text.mm("<red>Du bist in keiner Gilde."));
            return;
        }
        Guild guild = guildOpt.get();
        String members = guild.members().keySet().stream()
            .map(uuid -> {
                Player online = player.getServer().getPlayer(uuid);
                return online != null ? online.getName() : uuid.toString().substring(0, 8);
            })
            .collect(Collectors.joining(", "));
        player.sendMessage(Text.mm("<gold>Gilde: <white>" + guild.name()));
        player.sendMessage(Text.mm("<gray>Mitglieder: <white>" + members));
        player.sendMessage(Text.mm("<gray>Gildenbank: <gold>" + guild.bankGold() + "</gold> Gold"));
    }

    private void guildChat(Player player, String[] args) {
        Optional<Guild> guildOpt = plugin.guildManager().guildFor(player.getUniqueId());
        if (guildOpt.isEmpty()) {
            player.sendMessage(Text.mm("<red>Du bist in keiner Gilde."));
            return;
        }
        if (args.length < 2) {
            player.sendMessage(Text.mm("<gray>/guild chat <message>"));
            return;
        }
        Guild guild = guildOpt.get();
        String message = join(args, 1);
        for (UUID member : guild.members().keySet()) {
            Player target = player.getServer().getPlayer(member);
            if (target != null) {
                target.sendMessage(Text.mm("<aqua>[Gilde] <white>" + player.getName() + ": " + message));
            }
        }
    }

    private void bankCommand(Player player, String[] args) {
        Optional<Guild> guildOpt = plugin.guildManager().guildFor(player.getUniqueId());
        if (guildOpt.isEmpty()) {
            player.sendMessage(Text.mm("<red>Du bist in keiner Gilde."));
            return;
        }
        Guild guild = guildOpt.get();
        if (args.length < 2) {
            player.sendMessage(Text.mm("<gray>/guild bank <balance|deposit|withdraw>"));
            return;
        }
        switch (args[1].toLowerCase()) {
            case "balance" -> player.sendMessage(Text.mm("<gold>Gildenbank: " + guild.bankGold() + " Gold"));
            case "deposit" -> depositGuild(player, guild, args);
            case "withdraw" -> withdrawGuild(player, guild, args);
            default -> player.sendMessage(Text.mm("<gray>/guild bank <balance|deposit|withdraw>"));
        }
    }

    private void depositGuild(Player player, Guild guild, String[] args) {
        if (args.length < 3) {
            player.sendMessage(Text.mm("<gray>/guild bank deposit <amount>"));
            return;
        }
        Integer amount = parseAmount(player, args[2]);
        if (amount == null) {
            return;
        }
        PlayerProfile profile = plugin.playerDataManager().getProfile(player);
        if (profile.gold() < amount) {
            player.sendMessage(Text.mm("<red>Nicht genug Gold."));
            return;
        }
        profile.setGold(profile.gold() - amount);
        plugin.guildManager().deposit(guild, amount);
        player.sendMessage(Text.mm("<green>" + amount + " Gold eingezahlt."));
    }

    private void withdrawGuild(Player player, Guild guild, String[] args) {
        if (!isOfficerOrLeader(guild, player.getUniqueId())) {
            player.sendMessage(Text.mm("<red>Keine Rechte zum Abheben."));
            return;
        }
        if (args.length < 3) {
            player.sendMessage(Text.mm("<gray>/guild bank withdraw <amount>"));
            return;
        }
        Integer amount = parseAmount(player, args[2]);
        if (amount == null) {
            return;
        }
        if (!plugin.guildManager().withdraw(guild, amount)) {
            player.sendMessage(Text.mm("<red>Gildenbank hat nicht genug Gold."));
            return;
        }
        PlayerProfile profile = plugin.playerDataManager().getProfile(player);
        profile.setGold(profile.gold() + amount);
        player.sendMessage(Text.mm("<green>" + amount + " Gold abgehoben."));
    }

    private void questCommand(Player player, String[] args) {
        Optional<Guild> guildOpt = plugin.guildManager().guildFor(player.getUniqueId());
        if (guildOpt.isEmpty()) {
            player.sendMessage(Text.mm("<red>Du bist in keiner Gilde."));
            return;
        }
        Guild guild = guildOpt.get();
        if (args.length < 2) {
            player.sendMessage(Text.mm("<gray>/guild quest <list|create|progress|complete>"));
            return;
        }
        switch (args[1].toLowerCase()) {
            case "list" -> listQuests(player, guild);
            case "create" -> createQuest(player, guild, args);
            case "progress" -> progressQuest(player, guild, args);
            case "complete" -> completeQuest(player, guild, args);
            default -> player.sendMessage(Text.mm("<gray>/guild quest <list|create|progress|complete>"));
        }
    }

    private void listQuests(Player player, Guild guild) {
        if (guild.quests().isEmpty()) {
            player.sendMessage(Text.mm("<yellow>Keine Gilden-Quests verfügbar."));
            return;
        }
        player.sendMessage(Text.mm("<gold>Gilden-Quests:</gold>"));
        for (GuildQuest quest : guild.quests().values()) {
            String status = quest.completed() ? "<green>abgeschlossen" : "<yellow>" + quest.progress() + "/" + quest.goal();
            player.sendMessage(Text.mm("<gray>" + quest.id() + " - <white>" + quest.name() + " <gray>(" + status + "<gray>)"));
        }
    }

    private void createQuest(Player player, Guild guild, String[] args) {
        if (!isOfficerOrLeader(guild, player.getUniqueId())) {
            player.sendMessage(Text.mm("<red>Keine Rechte, um Quests zu erstellen."));
            return;
        }
        if (args.length < 4) {
            player.sendMessage(Text.mm("<gray>/guild quest create <id> <goal> <name>"));
            return;
        }
        String id = args[2].toLowerCase();
        Integer goal = parseAmount(player, args[3]);
        if (goal == null) {
            return;
        }
        if (guild.quests().containsKey(id)) {
            player.sendMessage(Text.mm("<red>Quest-ID existiert bereits."));
            return;
        }
        String name = args.length > 4 ? join(args, 4) : id;
        GuildQuest quest = new GuildQuest(id);
        quest.setName(name);
        quest.setDescription("Gildenquest");
        quest.setGoal(goal);
        quest.setProgress(0);
        quest.setCompleted(false);
        guild.quests().put(id, quest);
        plugin.guildManager().saveAll();
        player.sendMessage(Text.mm("<green>Gilden-Quest erstellt."));
    }

    private void progressQuest(Player player, Guild guild, String[] args) {
        if (!isOfficerOrLeader(guild, player.getUniqueId())) {
            player.sendMessage(Text.mm("<red>Keine Rechte, um Fortschritt zu setzen."));
            return;
        }
        if (args.length < 4) {
            player.sendMessage(Text.mm("<gray>/guild quest progress <id> <amount>"));
            return;
        }
        GuildQuest quest = guild.quests().get(args[2]);
        if (quest == null) {
            player.sendMessage(Text.mm("<red>Quest nicht gefunden."));
            return;
        }
        Integer amount = parseAmount(player, args[3]);
        if (amount == null) {
            return;
        }
        quest.setProgress(quest.progress() + amount);
        if (quest.progress() >= quest.goal()) {
            quest.setCompleted(true);
        }
        plugin.guildManager().saveAll();
        player.sendMessage(Text.mm("<green>Quest-Fortschritt aktualisiert."));
    }

    private void completeQuest(Player player, Guild guild, String[] args) {
        if (!isOfficerOrLeader(guild, player.getUniqueId())) {
            player.sendMessage(Text.mm("<red>Keine Rechte, um Quests abzuschließen."));
            return;
        }
        if (args.length < 3) {
            player.sendMessage(Text.mm("<gray>/guild quest complete <id>"));
            return;
        }
        GuildQuest quest = guild.quests().get(args[2]);
        if (quest == null) {
            player.sendMessage(Text.mm("<red>Quest nicht gefunden."));
            return;
        }
        quest.setCompleted(true);
        plugin.guildManager().saveAll();
        player.sendMessage(Text.mm("<green>Quest abgeschlossen."));
    }

    private boolean isOfficerOrLeader(Guild guild, UUID member) {
        GuildMemberRole role = guild.members().get(member);
        return role == GuildMemberRole.LEADER || role == GuildMemberRole.OFFICER;
    }

    private Integer parseAmount(Player player, String input) {
        int amount;
        try {
            amount = Integer.parseInt(input);
        } catch (NumberFormatException e) {
            player.sendMessage(Text.mm("<red>Betrag ungültig."));
            return null;
        }
        if (amount <= 0) {
            player.sendMessage(Text.mm("<red>Betrag muss > 0 sein."));
            return null;
        }
        return amount;
    }

    private String join(String[] args, int start) {
        StringBuilder builder = new StringBuilder();
        for (int i = start; i < args.length; i++) {
            if (i > start) {
                builder.append(" ");
            }
            builder.append(args[i]);
        }
        return builder.toString();
    }
}

```

---

## `MineLauncher/plugins/RPGPlugin/src/main/java/com/example/rpg/command/PartyCommand.java`

```java
package com.example.rpg.command;

import com.example.rpg.RPGPlugin;
import com.example.rpg.model.Party;
import com.example.rpg.util.Text;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class PartyCommand implements CommandExecutor {
    private final RPGPlugin plugin;

    public PartyCommand(RPGPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Nur Spieler.");
            return true;
        }
        if (args.length == 0) {
            player.sendMessage(Text.mm("<gray>/party <create|invite|join|leave|chat>"));
            return true;
        }
        switch (args[0].toLowerCase()) {
            case "create" -> createParty(player);
            case "invite" -> invitePlayer(player, args);
            case "join" -> joinParty(player, args);
            case "leave" -> leaveParty(player);
            case "chat" -> partyChat(player, args);
            default -> player.sendMessage(Text.mm("<gray>/party <create|invite|join|leave|chat>"));
        }
        return true;
    }

    private void createParty(Player player) {
        if (plugin.partyManager().getParty(player.getUniqueId()).isPresent()) {
            player.sendMessage(Text.mm("<yellow>Du bist bereits in einer Party."));
            return;
        }
        plugin.partyManager().createParty(player.getUniqueId());
        player.sendMessage(Text.mm("<green>Party erstellt."));
    }

    private void invitePlayer(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(Text.mm("<gray>/party invite <player>"));
            return;
        }
        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            player.sendMessage(Text.mm("<red>Spieler nicht online."));
            return;
        }
        plugin.partyManager().getParty(player.getUniqueId()).ifPresentOrElse(party -> {
            target.sendMessage(Text.mm("<yellow>Party Einladung von " + player.getName() + ". Benutze /party join " + player.getName()));
        }, () -> player.sendMessage(Text.mm("<red>Du hast keine Party.")));
    }

    private void joinParty(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(Text.mm("<gray>/party join <leader>"));
            return;
        }
        Player leader = Bukkit.getPlayer(args[1]);
        if (leader == null) {
            player.sendMessage(Text.mm("<red>Leader nicht online."));
            return;
        }
        plugin.partyManager().getParty(leader.getUniqueId()).ifPresentOrElse(party -> {
            if (plugin.partyManager().getParty(player.getUniqueId()).isPresent()) {
                player.sendMessage(Text.mm("<yellow>Du bist bereits in einer Party."));
                return;
            }
            plugin.partyManager().addMember(party, player.getUniqueId());
            leader.sendMessage(Text.mm("<green>" + player.getName() + " ist beigetreten."));
            player.sendMessage(Text.mm("<green>Du bist der Party beigetreten."));
        }, () -> player.sendMessage(Text.mm("<red>Party nicht gefunden.")));
    }

    private void leaveParty(Player player) {
        UUID uuid = player.getUniqueId();
        if (plugin.partyManager().getParty(uuid).isEmpty()) {
            player.sendMessage(Text.mm("<yellow>Du bist in keiner Party."));
            return;
        }
        plugin.partyManager().removeMember(uuid);
        player.sendMessage(Text.mm("<green>Party verlassen."));
    }

    private void partyChat(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(Text.mm("<gray>/party chat <message>"));
            return;
        }
        plugin.partyManager().getParty(player.getUniqueId()).ifPresentOrElse(party -> {
            String message = String.join(" ", java.util.Arrays.copyOfRange(args, 1, args.length));
            for (UUID member : party.members()) {
                Player target = Bukkit.getPlayer(member);
                if (target != null) {
                    target.sendMessage(Text.mm("<aqua>[Party] " + player.getName() + ": <white>" + message));
                }
            }
        }, () -> player.sendMessage(Text.mm("<yellow>Du bist in keiner Party.")));
    }
}

```

---

## `MineLauncher/plugins/RPGPlugin/src/main/java/com/example/rpg/command/PvpCommand.java`

```java
package com.example.rpg.command;

import com.example.rpg.RPGPlugin;
import com.example.rpg.model.PlayerProfile;
import com.example.rpg.util.Text;
import java.util.List;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class PvpCommand implements CommandExecutor {
    private final RPGPlugin plugin;

    public PvpCommand(RPGPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Nur Spieler.");
            return true;
        }
        if (args.length < 1) {
            player.sendMessage(Text.mm("<gray>/pvp <join|top>"));
            return true;
        }
        switch (args[0].toLowerCase()) {
            case "join" -> plugin.arenaManager().joinQueue(player);
            case "top" -> showTop(player);
            default -> player.sendMessage(Text.mm("<gray>/pvp <join|top>"));
        }
        return true;
    }

    private void showTop(Player player) {
        List<PlayerProfile> profiles = plugin.arenaManager().topPlayers(10);
        player.sendMessage(Text.mm("<gold>PvP Rangliste:"));
        int index = 1;
        for (PlayerProfile profile : profiles) {
            String name = plugin.getServer().getOfflinePlayer(profile.uuid()).getName();
            if (name == null) {
                name = profile.uuid().toString().substring(0, 8);
            }
            player.sendMessage(Text.mm("<gray>" + index++ + ". <white>" + name
                + " <gold>(" + profile.elo() + ")"));
        }
    }
}

```

---

## `MineLauncher/plugins/RPGPlugin/src/main/java/com/example/rpg/command/RPGAdminCommand.java`

```java
package com.example.rpg.command;

import com.example.rpg.RPGPlugin;
import com.example.rpg.model.LootEntry;
import com.example.rpg.model.LootTable;
import com.example.rpg.model.Npc;
import com.example.rpg.model.NpcRole;
import com.example.rpg.model.Quest;
import com.example.rpg.model.QuestStep;
import com.example.rpg.model.QuestStepType;
import com.example.rpg.model.Rarity;
import com.example.rpg.model.Skill;
import com.example.rpg.model.SkillCategory;
import com.example.rpg.model.SkillType;
import com.example.rpg.model.Zone;
import com.example.rpg.skill.SkillEffectConfig;
import com.example.rpg.skill.SkillEffectType;
import com.example.rpg.util.ItemBuilder;
import com.example.rpg.util.Text;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

public class RPGAdminCommand implements CommandExecutor {
    private final RPGPlugin plugin;

    public RPGAdminCommand(RPGPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Nur Spieler.");
            return true;
        }
        if (!player.hasPermission("rpg.admin")) {
            player.sendMessage(Text.mm("<red>Keine Rechte."));
            return true;
        }
        if (args.length == 0) {
            plugin.guiManager().openAdminMenu(player);
            return true;
        }
        switch (args[0].toLowerCase()) {
            case "wand" -> giveWand(player);
            case "zone" -> handleZone(player, args);
            case "npc" -> handleNpc(player, args);
            case "quest" -> handleQuest(player, args);
            case "loot" -> handleLoot(player, args);
            case "skill" -> handleSkill(player, args);
            case "mob" -> handleMob(player, args);
            case "spawner" -> handleSpawner(player, args);
            default -> player.sendMessage(Text.mm("<gray>/rpgadmin <wand|zone|npc|quest|loot|skill|mob|spawner>"));
        }
        return true;
    }

    private void giveWand(Player player) {
        ItemStack wand = new ItemBuilder(Material.STICK)
            .name(Text.mm("<yellow>Editor Wand"))
            .loreLine(Text.mm("<gray>Links: Pos1, Rechts: Pos2"))
            .build();
        var meta = wand.getItemMeta();
        meta.getPersistentDataContainer().set(plugin.wandKey(), PersistentDataType.BYTE, (byte) 1);
        wand.setItemMeta(meta);
        player.getInventory().addItem(wand);
        player.sendMessage(Text.mm("<green>Editor Wand erhalten."));
    }

    private void handleZone(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(Text.mm("<gray>/rpgadmin zone <create|setlevel|setmod>"));
            return;
        }
        switch (args[1].toLowerCase()) {
            case "create" -> createZone(player, args);
            case "setlevel" -> setZoneLevel(player, args);
            case "setmod" -> setZoneModifiers(player, args);
            default -> player.sendMessage(Text.mm("<gray>/rpgadmin zone <create|setlevel|setmod>"));
        }
    }

    private void createZone(Player player, String[] args) {
        if (args.length < 3) {
            player.sendMessage(Text.mm("<gray>/rpgadmin zone create <id>"));
            return;
        }
        Location pos1 = readPosition(player, "pos1");
        Location pos2 = readPosition(player, "pos2");
        if (pos1 == null || pos2 == null) {
            player.sendMessage(Text.mm("<red>Setze Pos1/Pos2 mit der Wand."));
            return;
        }
        String id = args[2];
        Zone zone = new Zone(id);
        zone.setName(id);
        zone.setWorld(pos1.getWorld().getName());
        zone.setBounds(pos1, pos2);
        plugin.zoneManager().zones().put(id, zone);
        plugin.zoneManager().saveZone(zone);
        plugin.auditLog().log(player, "Zone erstellt: " + id);
        player.sendMessage(Text.mm("<green>Zone erstellt: " + id));
    }

    private void setZoneLevel(Player player, String[] args) {
        if (args.length < 5) {
            player.sendMessage(Text.mm("<gray>/rpgadmin zone setlevel <id> <min> <max>"));
            return;
        }
        Zone zone = plugin.zoneManager().getZone(args[2]);
        if (zone == null) {
            player.sendMessage(Text.mm("<red>Zone nicht gefunden."));
            return;
        }
        Integer min = parseInt(args[3]);
        Integer max = parseInt(args[4]);
        if (min == null || max == null || min < 1 || max < min) {
            player.sendMessage(Text.mm("<red>Ungültiger Levelbereich. Beispiel: <white>/rpgadmin zone setlevel <id> 1 30</white>"));
            return;
        }
        zone.setMinLevel(min);
        zone.setMaxLevel(max);
        plugin.zoneManager().saveZone(zone);
        plugin.auditLog().log(player, "Zone Level gesetzt: " + zone.id());
        player.sendMessage(Text.mm("<green>Zone Level aktualisiert."));
    }

    private void setZoneModifiers(Player player, String[] args) {
        if (args.length < 5) {
            player.sendMessage(Text.mm("<gray>/rpgadmin zone setmod <id> <slow> <damage>"));
            return;
        }
        Zone zone = plugin.zoneManager().getZone(args[2]);
        if (zone == null) {
            player.sendMessage(Text.mm("<red>Zone nicht gefunden."));
            return;
        }
        Double slow = parseDouble(args[3]);
        Double dmg = parseDouble(args[4]);
        if (slow == null || dmg == null || slow <= 0.0 || dmg <= 0.0) {
            player.sendMessage(Text.mm("<red>Ungültige Werte. Beispiel: <white>/rpgadmin zone setmod <id> 0.8 1.2</white>"));
            return;
        }
        zone.setSlowMultiplier(slow);
        zone.setDamageMultiplier(dmg);
        plugin.zoneManager().saveZone(zone);
        plugin.auditLog().log(player, "Zone Modifikatoren gesetzt: " + zone.id());
        player.sendMessage(Text.mm("<green>Zone Modifikatoren aktualisiert."));
    }

    private void handleNpc(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(Text.mm("<gray>/rpgadmin npc <create|dialog|linkquest|linkshop>"));
            return;
        }
        switch (args[1].toLowerCase()) {
            case "create" -> createNpc(player, args);
            case "dialog" -> setNpcDialog(player, args);
            case "linkquest" -> linkNpcQuest(player, args);
            case "linkshop" -> linkNpcShop(player, args);
            default -> player.sendMessage(Text.mm("<gray>/rpgadmin npc <create|dialog|linkquest|linkshop>"));
        }
    }

    private void createNpc(Player player, String[] args) {
        if (args.length < 4) {
            player.sendMessage(Text.mm("<gray>/rpgadmin npc create <id> <role>"));
            return;
        }
        String id = args[2];
        Optional<NpcRole> roleOpt = parseEnum(NpcRole.class, args[3]);
        if (roleOpt.isEmpty()) {
            player.sendMessage(Text.mm("<red>Unbekannte Rolle. Erlaubt: <white>" + java.util.Arrays.toString(NpcRole.values())));
            return;
        }
        NpcRole role = roleOpt.get();
        Npc npc = new Npc(id);
        npc.setName(id);
        npc.setRole(role);
        npc.setLocation(player.getLocation());
        npc.setDialog(List.of("Hallo!", "Ich habe eine Aufgabe für dich."));
        plugin.npcManager().npcs().put(id, npc);
        plugin.npcManager().spawnNpc(npc);
        plugin.npcManager().saveNpc(npc);
        plugin.auditLog().log(player, "NPC erstellt: " + id);
        player.sendMessage(Text.mm("<green>NPC erstellt: " + id));
    }

    private void setNpcDialog(Player player, String[] args) {
        if (args.length < 3) {
            player.sendMessage(Text.mm("<gray>/rpgadmin npc dialog <id>"));
            return;
        }
        String id = args[2];
        Npc npc = plugin.npcManager().getNpc(id);
        if (npc == null) {
            player.sendMessage(Text.mm("<red>NPC nicht gefunden."));
            return;
        }
        plugin.promptManager().prompt(player, Text.mm("<yellow>Dialogzeile eingeben:"), input -> {
            npc.setDialog(List.of(input));
            plugin.npcManager().saveNpc(npc);
            plugin.auditLog().log(player, "NPC Dialog gesetzt: " + id);
            player.sendMessage(Text.mm("<green>Dialog gespeichert."));
        });
    }

    private void linkNpcQuest(Player player, String[] args) {
        if (args.length < 4) {
            player.sendMessage(Text.mm("<gray>/rpgadmin npc linkquest <npcId> <questId>"));
            return;
        }
        String npcId = args[2];
        String questId = args[3];
        Npc npc = plugin.npcManager().getNpc(npcId);
        if (npc == null) {
            player.sendMessage(Text.mm("<red>NPC nicht gefunden."));
            return;
        }
        Quest quest = plugin.questManager().getQuest(questId);
        if (quest == null) {
            player.sendMessage(Text.mm("<red>Quest nicht gefunden."));
            return;
        }
        npc.setQuestLink(questId);
        plugin.npcManager().saveNpc(npc);
        plugin.auditLog().log(player, "NPC Quest verlinkt: " + npcId + " -> " + questId);
        player.sendMessage(Text.mm("<green>NPC verlinkt mit Quest: " + quest.name()));
    }

    private void linkNpcShop(Player player, String[] args) {
        if (args.length < 4) {
            player.sendMessage(Text.mm("<gray>/rpgadmin npc linkshop <npcId> <shopId>"));
            return;
        }
        String npcId = args[2];
        String shopId = args[3];
        Npc npc = plugin.npcManager().getNpc(npcId);
        if (npc == null) {
            player.sendMessage(Text.mm("<red>NPC nicht gefunden."));
            return;
        }
        if (plugin.shopManager().getShop(shopId) == null) {
            player.sendMessage(Text.mm("<red>Shop nicht gefunden."));
            return;
        }
        npc.setShopId(shopId);
        plugin.npcManager().saveNpc(npc);
        plugin.auditLog().log(player, "NPC Shop verlinkt: " + npcId + " -> " + shopId);
        player.sendMessage(Text.mm("<green>NPC verlinkt mit Shop: " + shopId));
    }

    private void handleQuest(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(Text.mm("<gray>/rpgadmin quest <create|addstep>"));
            return;
        }
        switch (args[1].toLowerCase()) {
            case "create" -> createQuest(player, args);
            case "addstep" -> addQuestStep(player, args);
            default -> player.sendMessage(Text.mm("<gray>/rpgadmin quest <create|addstep>"));
        }
    }

    private void createQuest(Player player, String[] args) {
        if (args.length < 4) {
            player.sendMessage(Text.mm("<gray>/rpgadmin quest create <id> <name>"));
            return;
        }
        String id = args[2];
        String name = args[3];
        Quest quest = new Quest(id);
        quest.setName(name);
        quest.setDescription("Neue Quest");
        quest.setRepeatable(false);
        quest.setMinLevel(1);
        quest.setSteps(new java.util.ArrayList<>());
        plugin.questManager().quests().put(id, quest);
        plugin.questManager().saveQuest(quest);
        plugin.auditLog().log(player, "Quest erstellt: " + id);
        player.sendMessage(Text.mm("<green>Quest erstellt: " + id));
    }

    private void addQuestStep(Player player, String[] args) {
        if (args.length < 6) {
            player.sendMessage(Text.mm("<gray>/rpgadmin quest addstep <id> <type> <target> <amount>"));
            return;
        }
        Quest quest = plugin.questManager().getQuest(args[2]);
        if (quest == null) {
            player.sendMessage(Text.mm("<red>Quest nicht gefunden."));
            return;
        }
        Optional<QuestStepType> typeOpt = parseEnum(QuestStepType.class, args[3]);
        if (typeOpt.isEmpty()) {
            player.sendMessage(Text.mm("<red>Unbekannter Step-Typ. Erlaubt: <white>" + java.util.Arrays.toString(QuestStepType.values())));
            return;
        }
        QuestStepType type = typeOpt.get();
        String target = args[4];
        Integer amount = parseInt(args[5]);
        if (amount == null || amount < 1) {
            player.sendMessage(Text.mm("<red>Amount muss >= 1 sein.</red>"));
            return;
        }
        quest.steps().add(new QuestStep(type, target, amount));
        plugin.questManager().saveQuest(quest);
        plugin.auditLog().log(player, "Quest Step hinzugefügt: " + quest.id());
        player.sendMessage(Text.mm("<green>Quest Step hinzugefügt."));
    }

    private void handleLoot(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(Text.mm("<gray>/rpgadmin loot <create|addentry>"));
            return;
        }
        switch (args[1].toLowerCase()) {
            case "create" -> createLoot(player, args);
            case "addentry" -> addLootEntry(player, args);
            default -> player.sendMessage(Text.mm("<gray>/rpgadmin loot <create|addentry>"));
        }
    }

    private void createLoot(Player player, String[] args) {
        if (args.length < 4) {
            player.sendMessage(Text.mm("<gray>/rpgadmin loot create <id> <appliesTo>"));
            return;
        }
        String id = args[2];
        String appliesTo = args[3];
        LootTable table = new LootTable(id);
        table.setAppliesTo(appliesTo);
        plugin.lootManager().tables().put(id, table);
        plugin.lootManager().saveTable(table);
        plugin.auditLog().log(player, "Loot Table erstellt: " + id);
        player.sendMessage(Text.mm("<green>Loot Table erstellt."));
    }

    private void addLootEntry(Player player, String[] args) {
        if (args.length < 8) {
            player.sendMessage(Text.mm("<gray>/rpgadmin loot addentry <id> <material> <chance> <min> <max> <rarity>"));
            return;
        }
        LootTable table = plugin.lootManager().getTable(args[2]);
        if (table == null) {
            player.sendMessage(Text.mm("<red>Loot Table nicht gefunden."));
            return;
        }
        String material = args[3];
        Material mat = Material.matchMaterial(material.toUpperCase(Locale.ROOT));
        if (mat == null) {
            player.sendMessage(Text.mm("<red>Unbekanntes Material: <white>" + material + "</white>"));
            return;
        }
        Double chance = parseDouble(args[4]);
        Integer min = parseInt(args[5]);
        Integer max = parseInt(args[6]);
        Optional<Rarity> rarityOpt = parseEnum(Rarity.class, args[7]);
        if (chance == null || min == null || max == null || rarityOpt.isEmpty()) {
            player.sendMessage(Text.mm("<red>Ungültige Parameter. Beispiel: <white>/rpgadmin loot addentry <id> IRON_NUGGET 0.5 1 3 COMMON</white>"));
            return;
        }
        if (chance < 0.0 || chance > 1.0 || min < 1 || max < min) {
            player.sendMessage(Text.mm("<red>Chance 0..1 und min/max gültig setzen.</red>"));
            return;
        }
        Rarity rarity = rarityOpt.get();
        table.entries().add(new LootEntry(mat.name(), chance, min, max, rarity));
        plugin.lootManager().saveTable(table);
        plugin.auditLog().log(player, "Loot Entry hinzugefügt: " + table.id());
        player.sendMessage(Text.mm("<green>Loot Entry hinzugefügt."));
    }

    private void handleSkill(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(Text.mm("<gray>/rpgadmin skill <create|set|addeffect>"));
            return;
        }
        switch (args[1].toLowerCase()) {
            case "create" -> createSkill(player, args);
            case "set" -> setSkillValue(player, args);
            case "addeffect" -> addSkillEffect(player, args);
            default -> player.sendMessage(Text.mm("<gray>/rpgadmin skill <create|set|addeffect>"));
        }
    }

    private void handleMob(Player player, String[] args) {
        if (args.length < 3) {
            player.sendMessage(Text.mm("<gray>/rpgadmin mob spawn <mobId>"));
            return;
        }
        if (!args[1].equalsIgnoreCase("spawn")) {
            player.sendMessage(Text.mm("<gray>/rpgadmin mob spawn <mobId>"));
            return;
        }
        String mobId = args[2];
        var mobDef = plugin.mobManager().getMob(mobId);
        if (mobDef == null) {
            player.sendMessage(Text.mm("<red>Mob nicht gefunden."));
            return;
        }
        var world = player.getWorld();
        org.bukkit.entity.EntityType type;
        try {
            type = org.bukkit.entity.EntityType.valueOf(mobDef.type().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            player.sendMessage(Text.mm("<red>Ungültiger Mob-Typ.</red>"));
            return;
        }
        var entity = world.spawnEntity(player.getLocation(), type);
        if (entity instanceof org.bukkit.entity.LivingEntity living) {
            plugin.customMobListener().applyDefinition(living, mobDef);
            plugin.mobManager().saveMob(mobDef);
            plugin.auditLog().log(player, "Mob gespawnt: " + mobId);
            player.sendMessage(Text.mm("<green>Mob gespawnt: " + mobId));
        } else {
            entity.remove();
            player.sendMessage(Text.mm("<red>Mob-Typ ist kein LivingEntity."));
        }
    }

    private void handleSpawner(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(Text.mm("<gray>/rpgadmin spawner <create|addmob|setlimit>"));
            return;
        }
        switch (args[1].toLowerCase()) {
            case "create" -> createSpawner(player, args);
            case "addmob" -> addSpawnerMob(player, args);
            case "setlimit" -> setSpawnerLimit(player, args);
            default -> player.sendMessage(Text.mm("<gray>/rpgadmin spawner <create|addmob|setlimit>"));
        }
    }

    private void createSpawner(Player player, String[] args) {
        if (args.length < 4) {
            player.sendMessage(Text.mm("<gray>/rpgadmin spawner create <id> <zoneId>"));
            return;
        }
        String id = args[2];
        String zoneId = args[3];
        if (plugin.zoneManager().getZone(zoneId) == null) {
            player.sendMessage(Text.mm("<red>Zone nicht gefunden."));
            return;
        }
        var spawner = new com.example.rpg.model.Spawner(id);
        spawner.setZoneId(zoneId);
        spawner.setMaxMobs(6);
        spawner.setSpawnInterval(200);
        plugin.spawnerManager().spawners().put(id, spawner);
        plugin.spawnerManager().saveSpawner(spawner);
        plugin.auditLog().log(player, "Spawner erstellt: " + id);
        player.sendMessage(Text.mm("<green>Spawner erstellt: " + id));
    }

    private void addSpawnerMob(Player player, String[] args) {
        if (args.length < 5) {
            player.sendMessage(Text.mm("<gray>/rpgadmin spawner addmob <id> <mobId> <chance>"));
            return;
        }
        var spawner = plugin.spawnerManager().getSpawner(args[2]);
        if (spawner == null) {
            player.sendMessage(Text.mm("<red>Spawner nicht gefunden."));
            return;
        }
        String mobId = args[3];
        if (plugin.mobManager().getMob(mobId) == null) {
            player.sendMessage(Text.mm("<red>Mob nicht gefunden."));
            return;
        }
        Double chance = parseDouble(args[4]);
        if (chance == null || chance <= 0) {
            player.sendMessage(Text.mm("<red>Chance ungültig.</red>"));
            return;
        }
        spawner.mobs().put(mobId, chance);
        plugin.spawnerManager().saveSpawner(spawner);
        plugin.auditLog().log(player, "Spawner Mob hinzugefügt: " + spawner.id());
        player.sendMessage(Text.mm("<green>Spawner Mob hinzugefügt."));
    }

    private void setSpawnerLimit(Player player, String[] args) {
        if (args.length < 4) {
            player.sendMessage(Text.mm("<gray>/rpgadmin spawner setlimit <id> <amount>"));
            return;
        }
        var spawner = plugin.spawnerManager().getSpawner(args[2]);
        if (spawner == null) {
            player.sendMessage(Text.mm("<red>Spawner nicht gefunden."));
            return;
        }
        Integer limit = parseInt(args[3]);
        if (limit == null || limit < 0) {
            player.sendMessage(Text.mm("<red>Limit ungültig.</red>"));
            return;
        }
        spawner.setMaxMobs(limit);
        plugin.spawnerManager().saveSpawner(spawner);
        plugin.auditLog().log(player, "Spawner Limit gesetzt: " + spawner.id());
        player.sendMessage(Text.mm("<green>Spawner Limit aktualisiert."));
    }

    private void createSkill(Player player, String[] args) {
        if (args.length < 3) {
            player.sendMessage(Text.mm("<gray>/rpgadmin skill create <id>"));
            return;
        }
        String id = args[2].toLowerCase(Locale.ROOT);
        Skill skill = new Skill(id);
        skill.setName(id);
        skill.setType(SkillType.ACTIVE);
        skill.setCategory(SkillCategory.ATTACK);
        skill.setCooldown(10);
        skill.setManaCost(10);
        skill.setEffects(new java.util.ArrayList<>());
        plugin.skillManager().skills().put(id, skill);
        plugin.skillManager().saveSkill(skill);
        plugin.auditLog().log(player, "Skill erstellt: " + id);
        player.sendMessage(Text.mm("<green>Skill erstellt: " + id));
    }

    private void setSkillValue(Player player, String[] args) {
        if (args.length < 5) {
            player.sendMessage(Text.mm("<gray>/rpgadmin skill set <id> <cooldown|mana|category|type|name|requires> <value>"));
            return;
        }
        Skill skill = plugin.skillManager().getSkill(args[2]);
        if (skill == null) {
            player.sendMessage(Text.mm("<red>Skill nicht gefunden."));
            return;
        }
        String field = args[3].toLowerCase(Locale.ROOT);
        String value = args[4];
        switch (field) {
            case "cooldown" -> {
                Integer cd = parseInt(value);
                if (cd == null || cd < 0) {
                    player.sendMessage(Text.mm("<red>Cooldown ungültig.</red>"));
                    return;
                }
                skill.setCooldown(cd);
            }
            case "mana" -> {
                Integer mana = parseInt(value);
                if (mana == null || mana < 0) {
                    player.sendMessage(Text.mm("<red>Mana ungültig.</red>"));
                    return;
                }
                skill.setManaCost(mana);
            }
            case "category" -> {
                Optional<SkillCategory> category = parseEnum(SkillCategory.class, value);
                if (category.isEmpty()) {
                    player.sendMessage(Text.mm("<red>Unbekannte Kategorie.</red>"));
                    return;
                }
                skill.setCategory(category.get());
            }
            case "type" -> {
                Optional<SkillType> type = parseEnum(SkillType.class, value);
                if (type.isEmpty()) {
                    player.sendMessage(Text.mm("<red>Unbekannter Typ.</red>"));
                    return;
                }
                skill.setType(type.get());
            }
            case "name" -> skill.setName(value);
            case "requires" -> skill.setRequiredSkill(value.equalsIgnoreCase("none") ? null : value);
            default -> {
                player.sendMessage(Text.mm("<red>Unbekanntes Feld.</red>"));
                return;
            }
        }
        plugin.skillManager().saveSkill(skill);
        plugin.auditLog().log(player, "Skill gesetzt: " + skill.id() + " " + field + "=" + value);
        player.sendMessage(Text.mm("<green>Skill aktualisiert."));
    }

    private void addSkillEffect(Player player, String[] args) {
        if (args.length < 4) {
            player.sendMessage(Text.mm("<gray>/rpgadmin skill addeffect <id> <effectType> <param:value>..."));
            return;
        }
        Skill skill = plugin.skillManager().getSkill(args[2]);
        if (skill == null) {
            player.sendMessage(Text.mm("<red>Skill nicht gefunden."));
            return;
        }
        Optional<SkillEffectType> typeOpt = parseEnum(SkillEffectType.class, args[3]);
        if (typeOpt.isEmpty()) {
            player.sendMessage(Text.mm("<red>Unbekannter Effekt-Typ. Erlaubt: <white>"
                + java.util.Arrays.toString(SkillEffectType.values())));
            return;
        }
        java.util.Map<String, Object> params = new java.util.HashMap<>();
        for (int i = 4; i < args.length; i++) {
            String token = args[i];
            if (!token.contains(":")) {
                continue;
            }
            String[] parts = token.split(":", 2);
            params.put(parts[0], parseParamValue(parts[1]));
        }
        skill.effects().add(new SkillEffectConfig(typeOpt.get(), params));
        plugin.skillManager().saveSkill(skill);
        plugin.auditLog().log(player, "Skill Effekt hinzugefügt: " + skill.id() + " " + typeOpt.get());
        player.sendMessage(Text.mm("<green>Effekt hinzugefügt."));
    }

    private Location readPosition(Player player, String key) {
        NamespacedKey namespacedKey = new NamespacedKey(plugin, key);
        String value = player.getPersistentDataContainer().get(namespacedKey, PersistentDataType.STRING);
        if (value == null) {
            return null;
        }
        String[] parts = value.split(",");
        if (parts.length < 4) {
            return null;
        }
        return new Location(player.getServer().getWorld(parts[0]),
            Double.parseDouble(parts[1]), Double.parseDouble(parts[2]), Double.parseDouble(parts[3]));
    }

    // -----------------------
    // Parsing-Helper (crash-sicher)
    // -----------------------
    private static Integer parseInt(String raw) {
        try {
            return Integer.parseInt(raw);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static Double parseDouble(String raw) {
        try {
            return Double.parseDouble(raw);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static <E extends Enum<E>> Optional<E> parseEnum(Class<E> type, String raw) {
        if (raw == null) {
            return Optional.empty();
        }
        String key = raw.trim().toUpperCase(Locale.ROOT);
        try {
            return Optional.of(Enum.valueOf(type, key));
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }
    }

    private static Object parseParamValue(String raw) {
        try {
            if (raw.contains(".")) {
                return Double.parseDouble(raw);
            }
            return Integer.parseInt(raw);
        } catch (NumberFormatException e) {
            return raw;
        }
    }
}

```

---

## `MineLauncher/plugins/RPGPlugin/src/main/java/com/example/rpg/command/RPGCommand.java`

```java
package com.example.rpg.command;

import com.example.rpg.RPGPlugin;
import com.example.rpg.model.ClassDefinition;
import com.example.rpg.model.PlayerProfile;
import com.example.rpg.model.Skill;
import com.example.rpg.util.Text;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class RPGCommand implements CommandExecutor {
    private final RPGPlugin plugin;

    public RPGCommand(RPGPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Nur Spieler.");
            return true;
        }
        if (args.length == 0) {
            plugin.guiManager().openPlayerMenu(player);
            return true;
        }
        switch (args[0].toLowerCase()) {
            case "skill" -> handleSkill(player, args);
            case "quest" -> handleQuest(player, args);
            case "respec" -> handleRespec(player);
            case "class" -> handleClass(player, args);
            case "bind" -> handleBind(player, args);
            case "money" -> handleMoney(player);
            case "pay" -> handlePay(player, args);
            case "profession" -> handleProfession(player, args);
            case "skilltree" -> plugin.skillTreeGui().open(player);
            default -> player.sendMessage(Text.mm("<gray>/rpg <skill|quest|respec|class|bind|money|pay|profession|skilltree>"));
        }
        return true;
    }

    private void handleSkill(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(Text.mm("<gray>/rpg skill <id>"));
            return;
        }
        String skillId = args[1].toLowerCase();
        plugin.useSkill(player, skillId);
    }

    private void handleQuest(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(Text.mm("<gray>/rpg quest <accept|abandon|list>"));
            return;
        }
        PlayerProfile profile = plugin.playerDataManager().getProfile(player);
        switch (args[1].toLowerCase()) {
            case "list" -> plugin.guiManager().openQuestList(player);
            case "abandon" -> {
                if (args.length < 3) {
                    player.sendMessage(Text.mm("<gray>/rpg quest abandon <id>"));
                    return;
                }
                String questId = args[2];
                profile.activeQuests().remove(questId);
                player.sendMessage(Text.mm("<yellow>Quest abgebrochen: " + questId));
            }
            case "complete" -> {
                if (args.length < 3) {
                    player.sendMessage(Text.mm("<gray>/rpg quest complete <id>"));
                    return;
                }
                String questId = args[2];
                var quest = plugin.questManager().getQuest(questId);
                var progress = profile.activeQuests().get(questId);
                if (quest == null || progress == null) {
                    player.sendMessage(Text.mm("<red>Quest nicht aktiv."));
                    return;
                }
                if (!plugin.completeQuestIfReady(player, quest, progress)) {
                    player.sendMessage(Text.mm("<yellow>Quest noch nicht abgeschlossen."));
                }
            }
            default -> player.sendMessage(Text.mm("<gray>/rpg quest <list|abandon|complete>"));
        }
    }

    private void handleRespec(Player player) {
        PlayerProfile profile = plugin.playerDataManager().getProfile(player);
        profile.learnedSkills().clear();
        profile.setSkillPoints(profile.level() * 2);
        profile.stats().replaceAll((stat, value) -> 5);
        profile.applyAttributes(player);
        player.sendMessage(Text.mm("<green>Respec durchgeführt. Skillpunkte zurückgesetzt."));
    }

    private void handleClass(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(Text.mm("<gray>/rpg class <list|choose>"));
            return;
        }
        if (args[1].equalsIgnoreCase("list")) {
            player.sendMessage(Text.mm("<yellow>Klassen: " + plugin.classManager().classes().keySet()));
            return;
        }
        if (args[1].equalsIgnoreCase("choose")) {
            if (args.length < 3) {
                player.sendMessage(Text.mm("<gray>/rpg class choose <id>"));
                return;
            }
            String id = args[2];
            ClassDefinition definition = plugin.classManager().getClass(id);
            if (definition == null) {
                player.sendMessage(Text.mm("<red>Unbekannte Klasse."));
                return;
            }
            PlayerProfile profile = plugin.playerDataManager().getProfile(player);
            profile.setClassId(id);
            for (String skill : definition.startSkills()) {
                profile.learnedSkills().put(skill, 1);
            }
            player.sendMessage(Text.mm("<green>Klasse gewählt: " + definition.name()));
            return;
        }
        player.sendMessage(Text.mm("<gray>/rpg class <list|choose>"));
    }

    private void handleBind(Player player, String[] args) {
        if (args.length < 3) {
            player.sendMessage(Text.mm("<gray>/rpg bind <slot 1-9> <skillId>"));
            return;
        }
        Integer slot;
        try {
            slot = Integer.parseInt(args[1]);
        } catch (NumberFormatException e) {
            player.sendMessage(Text.mm("<red>Slot muss 1-9 sein."));
            return;
        }
        if (slot < 1 || slot > 9) {
            player.sendMessage(Text.mm("<red>Slot muss 1-9 sein."));
            return;
        }
        String skillId = args[2].toLowerCase();
        Skill skill = plugin.skillManager().getSkill(skillId);
        if (skill == null) {
            player.sendMessage(Text.mm("<red>Unbekannter Skill."));
            return;
        }
        PlayerProfile profile = plugin.playerDataManager().getProfile(player);
        if (!profile.learnedSkills().containsKey(skillId)) {
            player.sendMessage(Text.mm("<red>Skill nicht gelernt."));
            return;
        }
        plugin.skillHotbarManager().bindSkill(profile, slot, skillId);
        player.sendMessage(Text.mm("<green>Skill gebunden: Slot " + slot + " -> " + skill.name()));
    }

    private void handleMoney(Player player) {
        PlayerProfile profile = plugin.playerDataManager().getProfile(player);
        player.sendMessage(Text.mm("<gold>Gold: <white>" + profile.gold()));
    }

    private void handlePay(Player player, String[] args) {
        if (args.length < 3) {
            player.sendMessage(Text.mm("<gray>/rpg pay <player> <amount>"));
            return;
        }
        Player target = player.getServer().getPlayer(args[1]);
        if (target == null) {
            player.sendMessage(Text.mm("<red>Spieler nicht online."));
            return;
        }
        Integer amount;
        try {
            amount = Integer.parseInt(args[2]);
        } catch (NumberFormatException e) {
            player.sendMessage(Text.mm("<red>Betrag ungültig."));
            return;
        }
        if (amount <= 0) {
            player.sendMessage(Text.mm("<red>Betrag muss > 0 sein."));
            return;
        }
        PlayerProfile senderProfile = plugin.playerDataManager().getProfile(player);
        PlayerProfile targetProfile = plugin.playerDataManager().getProfile(target);
        if (senderProfile.gold() < amount) {
            player.sendMessage(Text.mm("<red>Nicht genug Gold."));
            return;
        }
        senderProfile.setGold(senderProfile.gold() - amount);
        targetProfile.setGold(targetProfile.gold() + amount);
        player.sendMessage(Text.mm("<green>Du hast <gold>" + amount + "</gold> Gold an " + target.getName() + " gesendet."));
        target.sendMessage(Text.mm("<green>Du hast <gold>" + amount + "</gold> Gold von " + player.getName() + " erhalten."));
    }

    private void handleProfession(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(Text.mm("<gray>/rpg profession <list|set>"));
            return;
        }
        PlayerProfile profile = plugin.playerDataManager().getProfile(player);
        switch (args[1].toLowerCase()) {
            case "list" -> {
                if (profile.professions().isEmpty()) {
                    player.sendMessage(Text.mm("<yellow>Keine Berufe freigeschaltet."));
                    return;
                }
                String summary = profile.professions().entrySet().stream()
                    .filter(entry -> entry.getKey().endsWith("_level"))
                    .map(entry -> entry.getKey().replace("_level", "") + ": " + entry.getValue())
                    .collect(java.util.stream.Collectors.joining(", "));
                player.sendMessage(Text.mm("<gold>Berufe: <white>" + summary));
            }
            case "set" -> {
                if (args.length < 4) {
                    player.sendMessage(Text.mm("<gray>/rpg profession set <name> <level>"));
                    return;
                }
                Integer level;
                try {
                    level = Integer.parseInt(args[3]);
                } catch (NumberFormatException e) {
                    player.sendMessage(Text.mm("<red>Level ungültig."));
                    return;
                }
                plugin.professionManager().setLevel(profile, args[2].toLowerCase(), level);
                player.sendMessage(Text.mm("<green>Beruf gesetzt."));
            }
            default -> player.sendMessage(Text.mm("<gray>/rpg profession <list|set>"));
        }
    }
}

```

---

## `MineLauncher/plugins/RPGPlugin/src/main/java/com/example/rpg/command/TradeCommand.java`

```java
package com.example.rpg.command;

import com.example.rpg.RPGPlugin;
import com.example.rpg.model.TradeRequest;
import com.example.rpg.util.Text;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class TradeCommand implements CommandExecutor {
    private final RPGPlugin plugin;

    public TradeCommand(RPGPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Nur Spieler.");
            return true;
        }
        if (args.length < 1) {
            player.sendMessage(Text.mm("<gray>/trade <request|accept|offer|requestgold|ready|cancel>"));
            return true;
        }
        switch (args[0].toLowerCase()) {
            case "request" -> requestTrade(player, args);
            case "accept" -> acceptTrade(player);
            case "offer" -> offerGold(player, args);
            case "requestgold" -> requestGold(player, args);
            case "ready" -> readyTrade(player);
            case "cancel" -> cancelTrade(player);
            default -> player.sendMessage(Text.mm("<gray>/trade <request|accept|offer|requestgold|ready|cancel>"));
        }
        return true;
    }

    private void requestTrade(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(Text.mm("<gray>/trade request <player>"));
            return;
        }
        Player target = player.getServer().getPlayer(args[1]);
        if (target == null) {
            player.sendMessage(Text.mm("<red>Spieler nicht online."));
            return;
        }
        plugin.tradeManager().requestTrade(player.getUniqueId(), target.getUniqueId());
        player.sendMessage(Text.mm("<green>Handel angefragt."));
        target.sendMessage(Text.mm("<yellow>Handelsanfrage von " + player.getName() + ". /trade accept"));
    }

    private void acceptTrade(Player player) {
        TradeRequest request = plugin.tradeManager().getRequest(player.getUniqueId());
        if (request == null) {
            player.sendMessage(Text.mm("<red>Keine Anfrage."));
            return;
        }
        player.sendMessage(Text.mm("<green>Handel akzeptiert. Beide Seiten können Gold setzen."));
    }

    private void offerGold(Player player, String[] args) {
        TradeRequest request = plugin.tradeManager().getRequest(player.getUniqueId());
        if (request == null) {
            player.sendMessage(Text.mm("<red>Kein Handel aktiv."));
            return;
        }
        if (args.length < 2) {
            player.sendMessage(Text.mm("<gray>/trade offer <gold>"));
            return;
        }
        int amount;
        try {
            amount = Integer.parseInt(args[1]);
        } catch (NumberFormatException e) {
            player.sendMessage(Text.mm("<red>Betrag ungültig."));
            return;
        }
        request.setGoldOffer(amount);
        player.sendMessage(Text.mm("<green>Du bietest " + amount + " Gold."));
    }

    private void requestGold(Player player, String[] args) {
        TradeRequest request = plugin.tradeManager().getRequest(player.getUniqueId());
        if (request == null) {
            player.sendMessage(Text.mm("<red>Kein Handel aktiv."));
            return;
        }
        if (args.length < 2) {
            player.sendMessage(Text.mm("<gray>/trade requestgold <gold>"));
            return;
        }
        int amount;
        try {
            amount = Integer.parseInt(args[1]);
        } catch (NumberFormatException e) {
            player.sendMessage(Text.mm("<red>Betrag ungültig."));
            return;
        }
        request.setGoldRequest(amount);
        player.sendMessage(Text.mm("<green>Du verlangst " + amount + " Gold."));
    }

    private void readyTrade(Player player) {
        TradeRequest request = plugin.tradeManager().getRequest(player.getUniqueId());
        if (request == null) {
            player.sendMessage(Text.mm("<red>Kein Handel aktiv."));
            return;
        }
        if (request.requester().equals(player.getUniqueId())) {
            request.setRequesterReady(true);
        } else {
            request.setTargetReady(true);
        }
        if (request.requesterReady() && request.targetReady()) {
            completeTrade(request);
        } else {
            player.sendMessage(Text.mm("<green>Bereit gesetzt. Warte auf den Handelspartner."));
        }
    }

    private void completeTrade(TradeRequest request) {
        Player requester = plugin.getServer().getPlayer(request.requester());
        Player target = plugin.getServer().getPlayer(request.target());
        if (requester == null || target == null) {
            return;
        }
        var requesterProfile = plugin.playerDataManager().getProfile(requester);
        var targetProfile = plugin.playerDataManager().getProfile(target);
        if (requesterProfile.gold() < request.goldOffer() || targetProfile.gold() < request.goldRequest()) {
            requester.sendMessage(Text.mm("<red>Handel fehlgeschlagen: nicht genug Gold."));
            target.sendMessage(Text.mm("<red>Handel fehlgeschlagen: nicht genug Gold."));
            plugin.tradeManager().clear(request.requester());
            return;
        }
        requesterProfile.setGold(requesterProfile.gold() - request.goldOffer() + request.goldRequest());
        targetProfile.setGold(targetProfile.gold() - request.goldRequest() + request.goldOffer());
        requester.sendMessage(Text.mm("<green>Handel abgeschlossen."));
        target.sendMessage(Text.mm("<green>Handel abgeschlossen."));
        plugin.tradeManager().clear(request.requester());
    }

    private void cancelTrade(Player player) {
        plugin.tradeManager().clear(player.getUniqueId());
        player.sendMessage(Text.mm("<yellow>Handel abgebrochen."));
    }
}

```

---

## `MineLauncher/plugins/RPGPlugin/src/main/java/com/example/rpg/db/DatabaseService.java`

```java
package com.example.rpg.db;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public class DatabaseService {
    private final JavaPlugin plugin;
    private HikariDataSource dataSource;
    private final ExecutorService executor;
    private final String jdbcUrl;
    private final String databaseName;
    private final String username;
    private final String password;

    public DatabaseService(JavaPlugin plugin) {
        this.plugin = plugin;
        FileConfiguration config = plugin.getConfig();
        String host = config.getString("database.host", "localhost");
        int port = config.getInt("database.port", 5432);
        String database = config.getString("database.name", "rpg");
        this.databaseName = database;
        this.username = config.getString("database.user", "rpg");
        this.password = config.getString("database.password", "minecraft");
        int poolSize = config.getInt("database.poolSize", 10);

        this.jdbcUrl = "jdbc:postgresql://" + host + ":" + port + "/" + database;

        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            plugin.getLogger().severe("PostgreSQL JDBC driver not found.");
        }

        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setJdbcUrl(jdbcUrl);
        hikariConfig.setDriverClassName("org.postgresql.Driver");
        hikariConfig.setUsername(this.username);
        hikariConfig.setPassword(this.password);
        hikariConfig.setMaximumPoolSize(poolSize);
        hikariConfig.setPoolName("MineLauncherRPG");
        hikariConfig.setAutoCommit(true);
        this.dataSource = createDataSource(hikariConfig);
        this.executor = Executors.newFixedThreadPool(Math.max(2, poolSize));
    }

    public Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    public ExecutorService executor() {
        return executor;
    }

    public void initTables() {
        if (dataSource == null) {
            plugin.getLogger().severe("Database not available. Skipping table initialization.");
            return;
        }
        String playersTable = """
            CREATE TABLE IF NOT EXISTS rpg_players (
                uuid UUID PRIMARY KEY,
                level INT,
                xp INT,
                skill_points INT,
                mana INT,
                max_mana INT,
                class_id TEXT,
                gold INT,
                guild_id TEXT,
                elo INT,
                professions JSONB,
                stats JSONB,
                learned_skills JSONB,
                active_quests JSONB,
                completed_quests JSONB,
                faction_rep JSONB,
                skill_cooldowns JSONB,
                skill_bindings JSONB
            )
            """;
        String skillsTable = """
            CREATE TABLE IF NOT EXISTS rpg_skills (
                player_uuid UUID PRIMARY KEY,
                data JSONB
            )
            """;
        String questsTable = """
            CREATE TABLE IF NOT EXISTS rpg_quests (
                player_uuid UUID PRIMARY KEY,
                data JSONB
            )
            """;
        try (Connection connection = getConnection(); Statement statement = connection.createStatement()) {
            statement.execute(playersTable);
            statement.execute(skillsTable);
            statement.execute(questsTable);
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to init database tables: " + e.getMessage());
        }
    }

    public void shutdown() {
        executor.shutdown();
        if (dataSource != null) {
            dataSource.close();
        }
    }

    private HikariDataSource createDataSource(HikariConfig hikariConfig) {
        try {
            return new HikariDataSource(hikariConfig);
        } catch (RuntimeException ex) {
            if (ex.getMessage() != null && ex.getMessage().contains("existiert nicht")) {
                plugin.getLogger().warning("Database not found. Attempting to create '" + databaseName + "'.");
                if (createDatabase()) {
                    return new HikariDataSource(hikariConfig);
                }
            }
            plugin.getLogger().severe("Failed to initialize database: " + ex.getMessage());
            return null;
        }
    }

    private boolean createDatabase() {
        String adminUrl = jdbcUrl.replace("/" + databaseName, "/postgres");
        try (Connection connection = java.sql.DriverManager.getConnection(adminUrl, username, password);
             java.sql.PreparedStatement exists = connection.prepareStatement(
                 "SELECT 1 FROM pg_database WHERE datname = ?")) {
            exists.setString(1, databaseName);
            try (java.sql.ResultSet resultSet = exists.executeQuery()) {
                if (resultSet.next()) {
                    return true;
                }
            }
            try (java.sql.Statement statement = connection.createStatement()) {
                statement.execute("CREATE DATABASE \"" + databaseName + "\"");
                return true;
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to create database: " + e.getMessage());
            return false;
        }
    }
}

```

---

## `MineLauncher/plugins/RPGPlugin/src/main/java/com/example/rpg/db/PlayerDao.java`

```java
package com.example.rpg.db;

import com.example.rpg.model.PlayerProfile;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface PlayerDao {
    CompletableFuture<Void> savePlayer(PlayerProfile profile);
    CompletableFuture<PlayerProfile> loadPlayer(UUID uuid);
}

```

---

## `MineLauncher/plugins/RPGPlugin/src/main/java/com/example/rpg/db/SqlPlayerDao.java`

```java
package com.example.rpg.db;

import com.example.rpg.model.PlayerProfile;
import com.example.rpg.model.QuestProgress;
import com.example.rpg.model.RPGStat;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class SqlPlayerDao implements PlayerDao {
    private final DatabaseService databaseService;
    private final Gson gson = new Gson();
    private final Type mapStringInt = new TypeToken<Map<String, Integer>>() {}.getType();
    private final Type mapStringLong = new TypeToken<Map<String, Long>>() {}.getType();
    private final Type mapIntString = new TypeToken<Map<Integer, String>>() {}.getType();
    private final Type listString = new TypeToken<List<String>>() {}.getType();
    private final Type mapStringObject = new TypeToken<Map<String, Object>>() {}.getType();

    public SqlPlayerDao(DatabaseService databaseService) {
        this.databaseService = databaseService;
    }

    @Override
    public CompletableFuture<Void> savePlayer(PlayerProfile profile) {
        return CompletableFuture.supplyAsync(() -> {
            String sql = """
                INSERT INTO rpg_players (uuid, level, xp, skill_points, mana, max_mana, class_id, gold, guild_id, elo,
                    professions, stats, learned_skills, active_quests, completed_quests, faction_rep, skill_cooldowns, skill_bindings)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?::jsonb, ?::jsonb, ?::jsonb, ?::jsonb, ?::jsonb, ?::jsonb, ?::jsonb, ?::jsonb)
                ON CONFLICT (uuid) DO UPDATE SET
                    level = EXCLUDED.level,
                    xp = EXCLUDED.xp,
                    skill_points = EXCLUDED.skill_points,
                    mana = EXCLUDED.mana,
                    max_mana = EXCLUDED.max_mana,
                    class_id = EXCLUDED.class_id,
                    gold = EXCLUDED.gold,
                    guild_id = EXCLUDED.guild_id,
                    elo = EXCLUDED.elo,
                    professions = EXCLUDED.professions,
                    stats = EXCLUDED.stats,
                    learned_skills = EXCLUDED.learned_skills,
                    active_quests = EXCLUDED.active_quests,
                    completed_quests = EXCLUDED.completed_quests,
                    faction_rep = EXCLUDED.faction_rep,
                    skill_cooldowns = EXCLUDED.skill_cooldowns,
                    skill_bindings = EXCLUDED.skill_bindings
                """;
            try (Connection connection = databaseService.getConnection();
                 PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setObject(1, profile.uuid());
                statement.setInt(2, profile.level());
                statement.setInt(3, profile.xp());
                statement.setInt(4, profile.skillPoints());
                statement.setInt(5, profile.mana());
                statement.setInt(6, profile.maxMana());
                statement.setString(7, profile.classId());
                statement.setInt(8, profile.gold());
                statement.setString(9, profile.guildId());
                statement.setInt(10, profile.elo());
                statement.setString(11, gson.toJson(profile.professions()));
                statement.setString(12, gson.toJson(statsToMap(profile.stats())));
                statement.setString(13, gson.toJson(profile.learnedSkills()));
                statement.setString(14, gson.toJson(questsToMap(profile.activeQuests())));
                statement.setString(15, gson.toJson(profile.completedQuests().stream().toList()));
                statement.setString(16, gson.toJson(profile.factionRep()));
                statement.setString(17, gson.toJson(profile.skillCooldowns()));
                statement.setString(18, gson.toJson(profile.skillBindings()));
                statement.executeUpdate();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
            return null;
        }, databaseService.executor());
    }

    @Override
    public CompletableFuture<PlayerProfile> loadPlayer(UUID uuid) {
        return CompletableFuture.supplyAsync(() -> {
            String sql = "SELECT * FROM rpg_players WHERE uuid = ?";
            try (Connection connection = databaseService.getConnection();
                 PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setObject(1, uuid);
                try (ResultSet resultSet = statement.executeQuery()) {
                    if (!resultSet.next()) {
                        return null;
                    }
                    PlayerProfile profile = new PlayerProfile(uuid);
                    profile.setLevel(resultSet.getInt("level"));
                    profile.setXp(resultSet.getInt("xp"));
                    profile.setSkillPoints(resultSet.getInt("skill_points"));
                    profile.setMana(resultSet.getInt("mana"));
                    profile.setMaxMana(resultSet.getInt("max_mana"));
                    profile.setClassId(resultSet.getString("class_id"));
                    profile.setGold(resultSet.getInt("gold"));
                    profile.setGuildId(resultSet.getString("guild_id"));
                    profile.setElo(resultSet.getInt("elo"));
                    applyMap(resultSet.getString("professions"), profile.professions(), mapStringInt);
                    Map<String, Integer> stats = fromJson(resultSet.getString("stats"), mapStringInt);
                    if (stats != null) {
                        for (Map.Entry<String, Integer> entry : stats.entrySet()) {
                            try {
                                profile.stats().put(RPGStat.valueOf(entry.getKey()), entry.getValue());
                            } catch (IllegalArgumentException ignored) {
                            }
                        }
                    }
                    applyMap(resultSet.getString("learned_skills"), profile.learnedSkills(), mapStringInt);
                    loadQuests(resultSet.getString("active_quests"), profile);
                    Set<String> completed = fromJson(resultSet.getString("completed_quests"),
                        new TypeToken<Set<String>>() {}.getType());
                    if (completed != null) {
                        profile.completedQuests().addAll(completed);
                    }
                    applyMap(resultSet.getString("faction_rep"), profile.factionRep(), mapStringInt);
                    applyMap(resultSet.getString("skill_cooldowns"), profile.skillCooldowns(), mapStringLong);
                    Map<Integer, String> bindings = fromJson(resultSet.getString("skill_bindings"), mapIntString);
                    if (bindings != null) {
                        profile.skillBindings().putAll(bindings);
                    }
                    return profile;
                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }, databaseService.executor());
    }

    private Map<String, Integer> statsToMap(Map<RPGStat, Integer> stats) {
        Map<String, Integer> mapped = new HashMap<>();
        for (Map.Entry<RPGStat, Integer> entry : stats.entrySet()) {
            mapped.put(entry.getKey().name(), entry.getValue());
        }
        return mapped;
    }

    private Map<String, Object> questsToMap(Map<String, QuestProgress> quests) {
        Map<String, Object> data = new HashMap<>();
        for (QuestProgress progress : quests.values()) {
            Map<String, Object> quest = new HashMap<>();
            Map<String, Integer> steps = new HashMap<>();
            for (Map.Entry<Integer, Integer> entry : progress.stepProgress().entrySet()) {
                steps.put(String.valueOf(entry.getKey()), entry.getValue());
            }
            quest.put("steps", steps);
            quest.put("completed", progress.completed());
            data.put(progress.questId(), quest);
        }
        return data;
    }

    private void loadQuests(String json, PlayerProfile profile) {
        Map<String, Object> data = fromJson(json, mapStringObject);
        if (data == null) {
            return;
        }
        for (Map.Entry<String, Object> entry : data.entrySet()) {
            QuestProgress progress = new QuestProgress(entry.getKey());
            if (entry.getValue() instanceof Map<?, ?> map) {
                Object stepsObj = map.get("steps");
                if (stepsObj instanceof Map<?, ?> steps) {
                    for (Map.Entry<?, ?> stepEntry : steps.entrySet()) {
                        try {
                            int step = Integer.parseInt(String.valueOf(stepEntry.getKey()));
                            int value = Integer.parseInt(String.valueOf(stepEntry.getValue()));
                            progress.incrementStep(step, value);
                        } catch (NumberFormatException ignored) {
                        }
                    }
                }
                Object completed = map.get("completed");
                if (completed instanceof Boolean done) {
                    progress.setCompleted(done);
                }
            }
            profile.activeQuests().put(entry.getKey(), progress);
        }
    }

    private <T> void applyMap(String json, Map<String, T> target, Type type) {
        Map<String, T> data = fromJson(json, type);
        if (data != null) {
            target.putAll(data);
        }
    }

    private <T> T fromJson(String json, Type type) {
        if (json == null || json.isBlank()) {
            return null;
        }
        return gson.fromJson(json, type);
    }
}

```

---

## `MineLauncher/plugins/RPGPlugin/src/main/java/com/example/rpg/dungeon/DungeonGenerator.java`

```java
package com.example.rpg.dungeon;

import com.example.rpg.RPGPlugin;
import com.example.rpg.model.MobDefinition;
import com.example.rpg.model.Spawner;
import com.example.rpg.dungeon.wfc.Pattern;
import com.example.rpg.dungeon.wfc.WfcGenerator;
import com.example.rpg.util.Text;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.function.Consumer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.TextDisplay;
import org.bukkit.scheduler.BukkitRunnable;

public class DungeonGenerator {
    private final RPGPlugin plugin;
    private final Random random = new Random();
    private final WfcGenerator wfcGenerator;

    public DungeonGenerator(RPGPlugin plugin) {
        this.plugin = plugin;
        this.wfcGenerator = new WfcGenerator();
    }

    public DungeonInstance generate(String theme, List<Player> party) {
        String worldName = "dungeon_" + System.currentTimeMillis();
        World world = plugin.getServer().createWorld(new WorldCreator(worldName));
        int roomSize = 9;
        int grid = 4;
        int baseY = 60;
        List<Location> roomCenters = new ArrayList<>();
        for (int x = 0; x < grid; x++) {
            for (int z = 0; z < grid; z++) {
                int startX = x * (roomSize + 2);
                int startZ = z * (roomSize + 2);
                carveRoom(world, startX, baseY, startZ, roomSize, Material.STONE_BRICKS);
                roomCenters.add(new Location(world, startX + roomSize / 2.0, baseY + 1, startZ + roomSize / 2.0));
                if (x > 0) {
                    carveCorridor(world, startX - 1, baseY, startZ + roomSize / 2, Material.COBBLESTONE);
                }
                if (z > 0) {
                    carveCorridor(world, startX + roomSize / 2, baseY, startZ - 1, Material.COBBLESTONE);
                }
            }
        }
        Location start = roomCenters.get(0).clone();
        Location bossRoom = roomCenters.get(roomCenters.size() - 1).clone();
        spawnSpawners(roomCenters);
        spawnBoss(bossRoom);
        spawnSigns(start, bossRoom, theme);
        for (Player player : party) {
            player.teleport(start);
            player.sendMessage(Text.mm("<green>Dungeon generiert: " + theme));
        }
        return new DungeonInstance(world, start, bossRoom);
    }

    public void generateWfc(String theme, List<Player> party, Consumer<DungeonInstance> callback) {
        String worldName = "dungeon_" + System.currentTimeMillis();
        World world = plugin.getServer().createWorld(new WorldCreator(worldName));
        int width = 10;
        int height = 3;
        int depth = 10;
        int originY = 60;
        wfcGenerator.generate(width, height, depth).thenAccept(patterns -> {
            new BukkitRunnable() {
                @Override
                public void run() {
                    if (patterns == null) {
                        return;
                    }
                    Location start = new Location(world, 1, originY + 2, 1);
                    Location bossRoom = buildFromPatterns(world, patterns, originY);
                    DungeonInstance instance = new DungeonInstance(world, start, bossRoom);
                    for (Player player : party) {
                        player.teleport(start);
                        player.sendMessage(Text.mm("<green>Dungeon generiert: " + theme));
                    }
                    callback.accept(instance);
                }
            }.runTask(plugin);
        });
    }

    private Location buildFromPatterns(World world, Pattern[][][] patterns, int originY) {
        Location start = null;
        Location farthest = null;
        double bestDistance = 0;
        int cellSize = 2;
        for (int x = 0; x < patterns.length; x++) {
            for (int y = 0; y < patterns[x].length; y++) {
                for (int z = 0; z < patterns[x][y].length; z++) {
                    Pattern pattern = patterns[x][y][z];
                    if (pattern == null) {
                        continue;
                    }
                    int baseX = x * cellSize;
                    int baseY = originY + y * cellSize;
                    int baseZ = z * cellSize;
                    placePattern(world, pattern, baseX, baseY, baseZ);
                    if ("FLOOR".equals(pattern.socketDown())) {
                        Location center = new Location(world, baseX + 0.5, baseY + 1, baseZ + 0.5);
                        if (start == null) {
                            start = center;
                        }
                        double distance = start != null ? start.distanceSquared(center) : 0;
                        if (distance > bestDistance) {
                            bestDistance = distance;
                            farthest = center;
                        }
                    }
                }
            }
        }
        if (farthest == null) {
            farthest = new Location(world, 1, originY + 2, 1);
        }
        spawnBoss(farthest);
        return farthest;
    }

    private void placePattern(World world, Pattern pattern, int baseX, int baseY, int baseZ) {
        Material[] blocks = pattern.blocks();
        int index = 0;
        for (int x = 0; x < 2; x++) {
            for (int y = 0; y < 2; y++) {
                for (int z = 0; z < 2; z++) {
                    Material material = blocks[index++];
                    world.getBlockAt(baseX + x, baseY + y, baseZ + z).setType(material, false);
                }
            }
        }
    }

    private void carveRoom(World world, int startX, int startY, int startZ, int size, Material material) {
        for (int x = startX; x < startX + size; x++) {
            for (int z = startZ; z < startZ + size; z++) {
                world.getBlockAt(x, startY, z).setType(material);
                for (int y = 1; y <= 4; y++) {
                    world.getBlockAt(x, startY + y, z).setType(Material.AIR);
                }
            }
        }
        for (int x = startX; x < startX + size; x++) {
            for (int z = startZ; z < startZ + size; z++) {
                if (x == startX || x == startX + size - 1 || z == startZ || z == startZ + size - 1) {
                    world.getBlockAt(x, startY + 5, z).setType(material);
                }
            }
        }
    }

    private void carveCorridor(World world, int startX, int startY, int startZ, Material material) {
        for (int i = 0; i < 3; i++) {
            world.getBlockAt(startX + i, startY, startZ).setType(material);
            for (int y = 1; y <= 3; y++) {
                world.getBlockAt(startX + i, startY + y, startZ).setType(Material.AIR);
            }
        }
    }

    private void spawnSpawners(List<Location> roomCenters) {
        for (int i = 1; i < roomCenters.size() - 1; i++) {
            Location location = roomCenters.get(i);
            Spawner spawner = plugin.spawnerManager().spawners().values().stream().findFirst().orElse(null);
            if (spawner == null) {
                spawnFallbackMob(location);
                continue;
            }
            if (spawner.mobs().isEmpty()) {
                spawnFallbackMob(location);
                continue;
            }
            String mobId = spawner.mobs().keySet().iterator().next();
            MobDefinition mob = plugin.mobManager().getMob(mobId);
            if (mob == null) {
                spawnFallbackMob(location);
                continue;
            }
            var entity = location.getWorld().spawnEntity(location, EntityType.valueOf(mob.type().toUpperCase()));
            if (entity instanceof org.bukkit.entity.LivingEntity living) {
                plugin.customMobListener().applyDefinition(living, mob);
            }
        }
    }

    private void spawnFallbackMob(Location location) {
        location.getWorld().spawnEntity(location, EntityType.ZOMBIE);
    }

    private void spawnBoss(Location bossRoom) {
        MobDefinition boss = plugin.mobManager().getMob("boss_zombie");
        if (boss == null) {
            return;
        }
        var entity = bossRoom.getWorld().spawnEntity(bossRoom, EntityType.valueOf(boss.type().toUpperCase()));
        if (entity instanceof org.bukkit.entity.LivingEntity living) {
            plugin.customMobListener().applyDefinition(living, boss);
            TextDisplay display = bossRoom.getWorld().spawn(bossRoom.clone().add(0, 2, 0), TextDisplay.class);
            Component bossName = LegacyComponentSerializer.legacySection().deserialize(boss.name());
            display.text(Component.text("Boss: ").append(bossName));
        }
    }

    private void spawnSigns(Location start, Location bossRoom, String theme) {
        TextDisplay startSign = start.getWorld().spawn(start.clone().add(0, 2, 0), TextDisplay.class);
        startSign.text(Text.mm("<gold>Dungeon: " + theme));
        TextDisplay bossSign = bossRoom.getWorld().spawn(bossRoom.clone().add(0, 2, 0), TextDisplay.class);
        bossSign.text(Text.mm("<red>Boss-Raum"));
    }
}

```

---

## `MineLauncher/plugins/RPGPlugin/src/main/java/com/example/rpg/dungeon/DungeonInstance.java`

```java
package com.example.rpg.dungeon;

import org.bukkit.Location;
import org.bukkit.World;

public class DungeonInstance {
    private final World world;
    private final Location start;
    private final Location bossRoom;

    public DungeonInstance(World world, Location start, Location bossRoom) {
        this.world = world;
        this.start = start;
        this.bossRoom = bossRoom;
    }

    public World world() {
        return world;
    }

    public Location start() {
        return start;
    }

    public Location bossRoom() {
        return bossRoom;
    }
}

```

---

## `MineLauncher/plugins/RPGPlugin/src/main/java/com/example/rpg/dungeon/wfc/Direction.java`

```java
package com.example.rpg.dungeon.wfc;

public enum Direction {
    UP,
    DOWN,
    NORTH,
    SOUTH,
    EAST,
    WEST;

    public Direction opposite() {
        return switch (this) {
            case UP -> DOWN;
            case DOWN -> UP;
            case NORTH -> SOUTH;
            case SOUTH -> NORTH;
            case EAST -> WEST;
            case WEST -> EAST;
        };
    }
}

```

---

## `MineLauncher/plugins/RPGPlugin/src/main/java/com/example/rpg/dungeon/wfc/Pattern.java`

```java
package com.example.rpg.dungeon.wfc;

import java.util.EnumMap;
import java.util.Map;
import org.bukkit.Material;

public class Pattern {
    private final String id;
    private final Material[] blocks;
    private final Map<Direction, String> sockets = new EnumMap<>(Direction.class);
    private final double weight;

    public Pattern(String id, Material[] blocks, double weight) {
        this.id = id;
        this.blocks = blocks;
        this.weight = weight;
    }

    public String id() {
        return id;
    }

    public Material[] blocks() {
        return blocks;
    }

    public double weight() {
        return weight;
    }

    public void setSocket(Direction direction, String socket) {
        sockets.put(direction, socket);
    }

    public String socket(Direction direction) {
        return sockets.getOrDefault(direction, "AIR");
    }

    public String socketDown() {
        return socket(Direction.DOWN);
    }
}

```

---

## `MineLauncher/plugins/RPGPlugin/src/main/java/com/example/rpg/dungeon/wfc/PatternLoader.java`

```java
package com.example.rpg.dungeon.wfc;

import java.util.ArrayList;
import java.util.List;
import org.bukkit.Material;

public class PatternLoader {
    public List<Pattern> loadDefaultPatterns() {
        List<Pattern> patterns = new ArrayList<>();

        Pattern air = new Pattern("air", fill(Material.AIR), 1.0);
        for (Direction direction : Direction.values()) {
            air.setSocket(direction, "AIR");
        }
        patterns.add(air);

        Pattern floor = new Pattern("floor", floorBlocks(), 1.5);
        floor.setSocket(Direction.DOWN, "FLOOR");
        floor.setSocket(Direction.UP, "AIR");
        floor.setSocket(Direction.NORTH, "AIR");
        floor.setSocket(Direction.SOUTH, "AIR");
        floor.setSocket(Direction.EAST, "AIR");
        floor.setSocket(Direction.WEST, "AIR");
        patterns.add(floor);

        Pattern wallNorth = new Pattern("wall_north", wallBlocks(Direction.NORTH), 1.0);
        wallNorth.setSocket(Direction.DOWN, "FLOOR");
        wallNorth.setSocket(Direction.UP, "AIR");
        wallNorth.setSocket(Direction.NORTH, "WALL");
        wallNorth.setSocket(Direction.SOUTH, "AIR");
        wallNorth.setSocket(Direction.EAST, "AIR");
        wallNorth.setSocket(Direction.WEST, "AIR");
        patterns.add(wallNorth);

        Pattern corridor = new Pattern("corridor_ns", corridorBlocks(), 1.2);
        corridor.setSocket(Direction.DOWN, "FLOOR");
        corridor.setSocket(Direction.UP, "AIR");
        corridor.setSocket(Direction.NORTH, "OPEN");
        corridor.setSocket(Direction.SOUTH, "OPEN");
        corridor.setSocket(Direction.EAST, "WALL");
        corridor.setSocket(Direction.WEST, "WALL");
        patterns.add(corridor);

        return patterns;
    }

    private Material[] fill(Material material) {
        Material[] blocks = new Material[8];
        for (int i = 0; i < blocks.length; i++) {
            blocks[i] = material;
        }
        return blocks;
    }

    private Material[] floorBlocks() {
        Material[] blocks = new Material[8];
        int index = 0;
        for (int x = 0; x < 2; x++) {
            for (int y = 0; y < 2; y++) {
                for (int z = 0; z < 2; z++) {
                    blocks[index++] = y == 0 ? Material.STONE_BRICKS : Material.AIR;
                }
            }
        }
        return blocks;
    }

    private Material[] wallBlocks(Direction direction) {
        Material[] blocks = floorBlocks();
        int index = 0;
        for (int x = 0; x < 2; x++) {
            for (int y = 0; y < 2; y++) {
                for (int z = 0; z < 2; z++) {
                    if (y == 1 && isWallCell(direction, x, z)) {
                        blocks[index] = Material.COBBLESTONE_WALL;
                    }
                    index++;
                }
            }
        }
        return blocks;
    }

    private boolean isWallCell(Direction direction, int x, int z) {
        return switch (direction) {
            case NORTH -> z == 0;
            case SOUTH -> z == 1;
            case EAST -> x == 1;
            case WEST -> x == 0;
            default -> false;
        };
    }

    private Material[] corridorBlocks() {
        return floorBlocks();
    }
}

```

---

## `MineLauncher/plugins/RPGPlugin/src/main/java/com/example/rpg/dungeon/wfc/WaveGrid.java`

```java
package com.example.rpg.dungeon.wfc;

import java.util.ArrayList;
import java.util.List;

public class WaveGrid {
    private final List<Pattern>[][][] possibilities;
    private final boolean[][][] collapsed;

    @SuppressWarnings("unchecked")
    public WaveGrid(int width, int height, int depth, List<Pattern> initial) {
        possibilities = new List[width][height][depth];
        collapsed = new boolean[width][height][depth];
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                for (int z = 0; z < depth; z++) {
                    possibilities[x][y][z] = new ArrayList<>(initial);
                }
            }
        }
    }

    public List<Pattern> possibilities(int x, int y, int z) {
        return possibilities[x][y][z];
    }

    public void setPossibilities(int x, int y, int z, List<Pattern> list) {
        possibilities[x][y][z] = list;
    }

    public boolean collapsed(int x, int y, int z) {
        return collapsed[x][y][z];
    }

    public void setCollapsed(int x, int y, int z, boolean value) {
        collapsed[x][y][z] = value;
    }

    public int width() {
        return possibilities.length;
    }

    public int height() {
        return possibilities[0].length;
    }

    public int depth() {
        return possibilities[0][0].length;
    }
}

```

---

## `MineLauncher/plugins/RPGPlugin/src/main/java/com/example/rpg/dungeon/wfc/WfcGenerator.java`

```java
package com.example.rpg.dungeon.wfc;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.Random;
import java.util.concurrent.CompletableFuture;

public class WfcGenerator {
    private final PatternLoader patternLoader = new PatternLoader();
    private final Random random = new Random();

    public CompletableFuture<Pattern[][][]> generate(int width, int height, int depth) {
        return CompletableFuture.supplyAsync(() -> {
            List<Pattern> patterns = patternLoader.loadDefaultPatterns();
            for (int attempt = 0; attempt < 5; attempt++) {
                Pattern[][][] result = runAttempt(width, height, depth, patterns);
                if (result != null) {
                    return result;
                }
            }
            return null;
        });
    }

    private Pattern[][][] runAttempt(int width, int height, int depth, List<Pattern> patterns) {
        WaveGrid grid = new WaveGrid(width, height, depth, patterns);
        while (true) {
            int[] cell = findLowestEntropyCell(grid);
            if (cell == null) {
                break;
            }
            int x = cell[0];
            int y = cell[1];
            int z = cell[2];
            Pattern chosen = pickWeighted(grid.possibilities(x, y, z));
            grid.setPossibilities(x, y, z, List.of(chosen));
            grid.setCollapsed(x, y, z, true);
            if (!propagate(grid, x, y, z)) {
                return null;
            }
        }
        Pattern[][][] patternsResult = new Pattern[width][height][depth];
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                for (int z = 0; z < depth; z++) {
                    List<Pattern> options = grid.possibilities(x, y, z);
                    patternsResult[x][y][z] = options.isEmpty() ? null : options.get(0);
                }
            }
        }
        return patternsResult;
    }

    private int[] findLowestEntropyCell(WaveGrid grid) {
        int bestX = -1;
        int bestY = -1;
        int bestZ = -1;
        int bestEntropy = Integer.MAX_VALUE;
        for (int x = 0; x < grid.width(); x++) {
            for (int y = 0; y < grid.height(); y++) {
                for (int z = 0; z < grid.depth(); z++) {
                    if (grid.collapsed(x, y, z)) {
                        continue;
                    }
                    int size = grid.possibilities(x, y, z).size();
                    if (size == 0) {
                        return new int[] {x, y, z};
                    }
                    if (size < bestEntropy) {
                        bestEntropy = size;
                        bestX = x;
                        bestY = y;
                        bestZ = z;
                    }
                }
            }
        }
        if (bestX == -1) {
            return null;
        }
        return new int[] {bestX, bestY, bestZ};
    }

    private Pattern pickWeighted(List<Pattern> options) {
        double total = options.stream().mapToDouble(Pattern::weight).sum();
        double roll = random.nextDouble() * total;
        double current = 0;
        for (Pattern pattern : options) {
            current += pattern.weight();
            if (roll <= current) {
                return pattern;
            }
        }
        return options.get(0);
    }

    private boolean propagate(WaveGrid grid, int startX, int startY, int startZ) {
        Queue<int[]> queue = new ArrayDeque<>();
        queue.add(new int[] {startX, startY, startZ});
        while (!queue.isEmpty()) {
            int[] cell = queue.poll();
            int x = cell[0];
            int y = cell[1];
            int z = cell[2];
            for (Direction direction : Direction.values()) {
                int nx = x + offsetX(direction);
                int ny = y + offsetY(direction);
                int nz = z + offsetZ(direction);
                if (!inside(grid, nx, ny, nz)) {
                    continue;
                }
                List<Pattern> neighborOptions = grid.possibilities(nx, ny, nz);
                List<Pattern> filtered = new ArrayList<>();
                for (Pattern option : neighborOptions) {
                    if (compatible(grid.possibilities(x, y, z), option, direction)) {
                        filtered.add(option);
                    }
                }
                if (filtered.isEmpty()) {
                    return false;
                }
                if (filtered.size() != neighborOptions.size()) {
                    grid.setPossibilities(nx, ny, nz, filtered);
                    queue.add(new int[] {nx, ny, nz});
                }
            }
        }
        return true;
    }

    private boolean compatible(List<Pattern> sourceOptions, Pattern neighbor, Direction direction) {
        for (Pattern source : sourceOptions) {
            String socketA = source.socket(direction);
            String socketB = neighbor.socket(direction.opposite());
            if (socketA.equals(socketB)) {
                return true;
            }
        }
        return false;
    }

    private boolean inside(WaveGrid grid, int x, int y, int z) {
        return x >= 0 && x < grid.width()
            && y >= 0 && y < grid.height()
            && z >= 0 && z < grid.depth();
    }

    private int offsetX(Direction direction) {
        return switch (direction) {
            case EAST -> 1;
            case WEST -> -1;
            default -> 0;
        };
    }

    private int offsetY(Direction direction) {
        return switch (direction) {
            case UP -> 1;
            case DOWN -> -1;
            default -> 0;
        };
    }

    private int offsetZ(Direction direction) {
        return switch (direction) {
            case SOUTH -> 1;
            case NORTH -> -1;
            default -> 0;
        };
    }
}

```

---

## `MineLauncher/plugins/RPGPlugin/src/main/java/com/example/rpg/gui/GuiHolders.java`

```java
package com.example.rpg.gui;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.NotNull;

/**
 * Robuste Identifikation von GUIs:
 * Nicht über Inventory-Titel (anfällig für Farbe/Locale),
 * sondern über InventoryHolder-Typen.
 */
public final class GuiHolders {
    private GuiHolders() {}

    public static final class PlayerMenuHolder implements InventoryHolder {
        @Override
        public @NotNull Inventory getInventory() {
            throw new UnsupportedOperationException();
        }
    }

    public static final class AdminMenuHolder implements InventoryHolder {
        @Override
        public @NotNull Inventory getInventory() {
            throw new UnsupportedOperationException();
        }
    }

    public static final class QuestListHolder implements InventoryHolder {
        @Override
        public @NotNull Inventory getInventory() {
            throw new UnsupportedOperationException();
        }
    }

    public static final class SkillListHolder implements InventoryHolder {
        @Override
        public @NotNull Inventory getInventory() {
            throw new UnsupportedOperationException();
        }
    }

    public static final class SkillTreeHolder implements InventoryHolder {
        @Override
        public @NotNull Inventory getInventory() {
            throw new UnsupportedOperationException();
        }
    }

    public static final class ShopHolder implements InventoryHolder {
        private final String shopId;

        public ShopHolder(String shopId) {
            this.shopId = shopId;
        }

        public String shopId() {
            return shopId;
        }

        @Override
        public @NotNull Inventory getInventory() {
            throw new UnsupportedOperationException();
        }
    }
}

```

---

## `MineLauncher/plugins/RPGPlugin/src/main/java/com/example/rpg/gui/GuiManager.java`

```java
package com.example.rpg.gui;

import com.example.rpg.manager.ClassManager;
import com.example.rpg.manager.FactionManager;
import com.example.rpg.manager.PlayerDataManager;
import com.example.rpg.manager.QuestManager;
import com.example.rpg.manager.SkillManager;
import com.example.rpg.model.PlayerProfile;
import com.example.rpg.model.Quest;
import com.example.rpg.model.ShopDefinition;
import com.example.rpg.model.ShopItem;
import com.example.rpg.util.ItemBuilder;
import com.example.rpg.util.Text;
import java.util.ArrayList;
import java.util.List;
import net.kyori.adventure.text.Component;
import org.bukkit.NamespacedKey;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

public class GuiManager {
    private final PlayerDataManager playerDataManager;
    private final QuestManager questManager;
    private final SkillManager skillManager;
    private final ClassManager classManager;
    private final FactionManager factionManager;
    private final NamespacedKey questKey;
    private final NamespacedKey skillKey;

    public GuiManager(PlayerDataManager playerDataManager, QuestManager questManager, SkillManager skillManager,
                      ClassManager classManager, FactionManager factionManager, NamespacedKey questKey, NamespacedKey skillKey) {
        this.playerDataManager = playerDataManager;
        this.questManager = questManager;
        this.skillManager = skillManager;
        this.classManager = classManager;
        this.factionManager = factionManager;
        this.questKey = questKey;
        this.skillKey = skillKey;
    }

    public void openPlayerMenu(Player player) {
        Inventory inv = Bukkit.createInventory(new GuiHolders.PlayerMenuHolder(), 27, Component.text("RPG Menü"));
        PlayerProfile profile = playerDataManager.getProfile(player);

        inv.setItem(10, new ItemBuilder(Material.PLAYER_HEAD)
            .name(Text.mm("<gold>Charakter"))
            .loreLine(Text.mm("<gray>Level: <white>" + profile.level()))
            .loreLine(Text.mm("<gray>XP: <white>" + profile.xp() + "/" + profile.xpNeeded()))
            .loreLine(Text.mm("<gray>Klasse: <white>" + resolveClassName(profile.classId())))
            .build());

        inv.setItem(12, new ItemBuilder(Material.NETHER_STAR)
            .name(Text.mm("<aqua>Skills"))
            .loreLine(Text.mm("<gray>Skillpunkte: <white>" + profile.skillPoints()))
            .build());

        inv.setItem(14, new ItemBuilder(Material.WRITABLE_BOOK)
            .name(Text.mm("<green>Quests"))
            .loreLine(Text.mm("<gray>Aktiv: <white>" + profile.activeQuests().size()))
            .build());

        inv.setItem(16, new ItemBuilder(Material.EMERALD)
            .name(Text.mm("<yellow>Fraktionen"))
            .loreLine(Text.mm("<gray>Ruf verwalten"))
            .loreLine(Text.mm("<gray>Aktiv: <white>" + profile.factionRep().size() + "/" + factionManager.factions().size()))
            .build());

        player.openInventory(inv);
    }

    public void openAdminMenu(Player player) {
        Inventory inv = Bukkit.createInventory(new GuiHolders.AdminMenuHolder(), 27, Component.text("RPG Admin"));
        inv.setItem(10, new ItemBuilder(Material.COMPASS)
            .name(Text.mm("<gold>Zonen-Editor"))
            .loreLine(Text.mm("<gray>Regionen verwalten"))
            .build());
        inv.setItem(11, new ItemBuilder(Material.VILLAGER_SPAWN_EGG)
            .name(Text.mm("<green>NPC-Editor"))
            .loreLine(Text.mm("<gray>NPCs platzieren"))
            .build());
        inv.setItem(12, new ItemBuilder(Material.WRITABLE_BOOK)
            .name(Text.mm("<aqua>Quest-Editor"))
            .loreLine(Text.mm("<gray>Quests erstellen"))
            .build());
        inv.setItem(13, new ItemBuilder(Material.CHEST)
            .name(Text.mm("<yellow>Loot-Tabellen"))
            .loreLine(Text.mm("<gray>Loot konfigurieren"))
            .build());
        inv.setItem(14, new ItemBuilder(Material.BLAZE_POWDER)
            .name(Text.mm("<light_purple>Skills & Klassen"))
            .loreLine(Text.mm("<gray>Skills verwalten"))
            .build());
        inv.setItem(15, new ItemBuilder(Material.REDSTONE)
            .name(Text.mm("<red>Debug Overlay"))
            .loreLine(Text.mm("<gray>Region/Quest Debug"))
            .build());
        player.openInventory(inv);
    }

    public void openQuestList(Player player) {
        Inventory inv = Bukkit.createInventory(new GuiHolders.QuestListHolder(), 27, Component.text("Quests"));
        int slot = 0;
        for (Quest quest : questManager.quests().values()) {
            if (slot >= inv.getSize()) {
                break;
            }
            ItemStack item = new ItemBuilder(Material.BOOK)
                .name(Text.mm("<green>" + quest.name()))
                .loreLine(Text.mm("<gray>" + quest.description()))
                .loreLine(Text.mm("<gray>Min Level: <white>" + quest.minLevel()))
                .loreLine(Text.mm("<yellow>Klicke zum Annehmen"))
                .build();
            ItemMeta meta = item.getItemMeta();
            meta.getPersistentDataContainer().set(questKey, PersistentDataType.STRING, quest.id());
            item.setItemMeta(meta);
            inv.setItem(slot++, item);
        }
        player.openInventory(inv);
    }

    public void openSkillList(Player player) {
        Inventory inv = Bukkit.createInventory(new GuiHolders.SkillListHolder(), 27, Component.text("Skills"));
        PlayerProfile profile = playerDataManager.getProfile(player);
        int slot = 0;
        for (var entry : skillManager.skills().entrySet()) {
            if (slot >= inv.getSize()) {
                break;
            }
            String id = entry.getKey();
            var skill = entry.getValue();
            List<Component> lore = new ArrayList<>();
            lore.add(Text.mm("<gray>Kategorie: <white>" + skill.category()));
            lore.add(Text.mm("<gray>Typ: <white>" + skill.type()));
            lore.add(Text.mm("<gray>Cooldown: <white>" + skill.cooldown() + "s"));
            lore.add(Text.mm("<gray>Mana: <white>" + skill.manaCost()));
            lore.add(Text.mm("<gray>Rang: <white>" + profile.learnedSkills().getOrDefault(id, 0)));
            if (skill.requiredSkill() != null) {
                lore.add(Text.mm("<gray>Voraussetzung: <white>" + skill.requiredSkill()));
            }
            if (!skill.effects().isEmpty()) {
                for (var effect : skill.effects()) {
                    lore.add(Text.mm("<gray>Effekt: <white>" + effect.describe()));
                }
            }
            lore.add(Text.mm("<yellow>Klick: Skill lernen"));
            ItemStack item = new ItemBuilder(Material.ENCHANTED_BOOK)
                .name(Text.mm("<aqua>" + skill.name()))
                .loreLines(lore)
                .build();
            ItemMeta meta = item.getItemMeta();
            meta.getPersistentDataContainer().set(skillKey, PersistentDataType.STRING, id);
            item.setItemMeta(meta);
            inv.setItem(slot++, item);
        }
        player.openInventory(inv);
    }

    public void openShop(Player player, ShopDefinition shop) {
        Inventory inv = Bukkit.createInventory(new GuiHolders.ShopHolder(shop.id()), 27, Component.text(shop.title()));
        for (ShopItem item : shop.items().values()) {
            Material material = Material.matchMaterial(item.material());
            if (material == null) {
                continue;
            }
            ItemBuilder builder = new ItemBuilder(material);
            if (item.name() != null && !item.name().isBlank()) {
                builder.name(net.kyori.adventure.text.Component.text(
                    org.bukkit.ChatColor.translateAlternateColorCodes('&', item.name())));
            }
            if (item.buyPrice() > 0) {
                builder.loreLine(Text.mm("<gray>Kaufen: <gold>" + item.buyPrice() + " Gold"));
            }
            if (item.sellPrice() > 0) {
                builder.loreLine(Text.mm("<gray>Verkaufen: <gold>" + item.sellPrice() + " Gold"));
            }
            inv.setItem(item.slot(), builder.build());
        }
        player.openInventory(inv);
    }

    private String resolveClassName(String classId) {
        if (classId == null) {
            return "Keine";
        }
        var definition = classManager.getClass(classId);
        return definition != null ? definition.name() : classId;
    }
}

```

---

## `MineLauncher/plugins/RPGPlugin/src/main/java/com/example/rpg/gui/SkillTreeGui.java`

```java
package com.example.rpg.gui;

import com.example.rpg.RPGPlugin;
import com.example.rpg.manager.SkillTreeManager;
import com.example.rpg.model.PlayerProfile;
import com.example.rpg.model.Skill;
import com.example.rpg.util.ItemBuilder;
import com.example.rpg.util.Text;
import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.Map;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

public class SkillTreeGui {
    private final RPGPlugin plugin;

    public SkillTreeGui(RPGPlugin plugin) {
        this.plugin = plugin;
    }

    public void open(Player player) {
        Inventory inv = Bukkit.createInventory(new GuiHolders.SkillTreeHolder(), 54, Component.text("Skillbaum"));
        SkillTreeManager treeManager = plugin.skillTreeManager();
        treeManager.rebuild();
        Map<String, Integer> slots = layout(treeManager);
        PlayerProfile profile = plugin.playerDataManager().getProfile(player);
        for (Map.Entry<String, Integer> entry : slots.entrySet()) {
            Skill skill = plugin.skillManager().getSkill(entry.getKey());
            if (skill == null) {
                continue;
            }
            boolean learned = profile.learnedSkills().containsKey(skill.id());
            boolean unlocked = skill.requiredSkill() == null
                || profile.learnedSkills().containsKey(skill.requiredSkill());
            Material material = learned ? Material.ENCHANTED_BOOK : unlocked ? Material.BOOK : Material.BARRIER;
            ItemBuilder builder = new ItemBuilder(material)
                .name(Text.mm(learned ? "<green>" + skill.name() : unlocked ? "<yellow>" + skill.name() : "<red>" + skill.name()))
                .loreLine(Text.mm("<gray>Mana: <white>" + skill.manaCost()))
                .loreLine(Text.mm("<gray>Cooldown: <white>" + skill.cooldown() + "s"))
                .loreLine(Text.mm("<gray>Voraussetzung: <white>" + (skill.requiredSkill() == null ? "Keine" : skill.requiredSkill())));
            if (learned) {
                builder.loreLine(Text.mm("<green>Bereits gelernt"));
            } else if (unlocked) {
                builder.loreLine(Text.mm("<yellow>Klick zum Lernen"));
            } else {
                builder.loreLine(Text.mm("<red>Gesperrt"));
            }
            ItemStack item = builder.build();
            ItemMeta meta = item.getItemMeta();
            meta.getPersistentDataContainer().set(plugin.skillKey(), PersistentDataType.STRING, skill.id());
            item.setItemMeta(meta);
            inv.setItem(entry.getValue(), item);
        }
        for (int slot : slots.values()) {
            int linkSlot = slot + 1;
            if (linkSlot < inv.getSize() && inv.getItem(linkSlot) == null) {
                inv.setItem(linkSlot, new ItemBuilder(Material.GRAY_STAINED_GLASS_PANE).name(Component.text(" ")).build());
            }
        }
        player.openInventory(inv);
    }

    private Map<String, Integer> layout(SkillTreeManager treeManager) {
        Map<String, Integer> slots = new HashMap<>();
        int[] depthIndex = new int[6];
        ArrayDeque<SkillTreeManager.SkillNode> queue = new ArrayDeque<>(treeManager.roots());
        while (!queue.isEmpty()) {
            SkillTreeManager.SkillNode node = queue.poll();
            int depth = depth(node);
            int row = Math.min(depth, 5);
            int col = depthIndex[row]++;
            int slot = row * 9 + Math.min(col * 2, 8);
            slots.put(node.skill().id(), slot);
            for (SkillTreeManager.SkillNode child : node.children()) {
                queue.add(child);
            }
        }
        return slots;
    }

    private int depth(SkillTreeManager.SkillNode node) {
        int depth = 0;
        SkillTreeManager.SkillNode current = node;
        while (current.parent() != null) {
            depth++;
            current = current.parent();
        }
        return depth;
    }
}

```

---

## `MineLauncher/plugins/RPGPlugin/src/main/java/com/example/rpg/listener/ArenaListener.java`

```java
package com.example.rpg.listener;

import com.example.rpg.RPGPlugin;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

public class ArenaListener implements Listener {
    private final RPGPlugin plugin;

    public ArenaListener(RPGPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        plugin.arenaManager().handleDeath(player);
    }
}

```

---

## `MineLauncher/plugins/RPGPlugin/src/main/java/com/example/rpg/listener/CombatListener.java`

```java
package com.example.rpg.listener;

import com.example.rpg.RPGPlugin;
import com.example.rpg.model.LootEntry;
import com.example.rpg.model.LootTable;
import com.example.rpg.model.PlayerProfile;
import com.example.rpg.model.Quest;
import com.example.rpg.model.QuestProgress;
import com.example.rpg.model.QuestStep;
import com.example.rpg.model.QuestStepType;
import java.util.Random;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.inventory.ItemStack;

public class CombatListener implements Listener {
    private final RPGPlugin plugin;
    private final Random random = new Random();

    public CombatListener(RPGPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onFriendlyFire(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player damager)) {
            return;
        }
        if (!(event.getEntity() instanceof Player target)) {
            return;
        }
        plugin.partyManager().getParty(damager.getUniqueId()).ifPresent(party -> {
            if (party.members().contains(target.getUniqueId())) {
                event.setCancelled(true);
            }
        });
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        Player killer = event.getEntity().getKiller();
        if (killer == null) {
            return;
        }
        int xp = 10 + event.getEntity().getType().ordinal() % 10;
        var partyOpt = plugin.partyManager().getParty(killer.getUniqueId());
        java.util.List<Player> recipients = new java.util.ArrayList<>();
        if (partyOpt.isPresent()) {
            for (java.util.UUID memberId : partyOpt.get().members()) {
                Player member = plugin.getServer().getPlayer(memberId);
                if (member != null && member.getWorld().equals(killer.getWorld())
                    && member.getLocation().distanceSquared(killer.getLocation()) <= 30 * 30) {
                    recipients.add(member);
                }
            }
        } else {
            recipients.add(killer);
        }
        boolean split = plugin.getConfig().getBoolean("rpg.party.xpSplit", true);
        int share = split ? Math.max(1, xp / Math.max(1, recipients.size())) : xp;
        for (Player member : recipients) {
            PlayerProfile profile = plugin.playerDataManager().getProfile(member);
            profile.addXp(share);
            profile.applyAttributes(member);
        }

        LootTable table = plugin.lootManager().getTableFor(event.getEntity().getType().name());
        if (table != null) {
            for (LootEntry entry : table.entries()) {
                if (random.nextDouble() <= entry.chance()) {
                    Material material = Material.matchMaterial(entry.material());
                    if (material != null) {
                        int level = plugin.playerDataManager().getProfile(killer).level();
                        ItemStack item = plugin.itemGenerator().createRpgItem(material, entry.rarity(), level);
                        item.setAmount(entry.minAmount() + random.nextInt(Math.max(1, entry.maxAmount() - entry.minAmount() + 1)));
                        event.getDrops().add(item);
                    }
                }
            }
        }

        for (Player member : recipients) {
            PlayerProfile profile = plugin.playerDataManager().getProfile(member);
            for (QuestProgress progress : profile.activeQuests().values()) {
                Quest quest = plugin.questManager().getQuest(progress.questId());
                if (quest == null) {
                    continue;
                }
                for (int i = 0; i < quest.steps().size(); i++) {
                    QuestStep step = quest.steps().get(i);
                    if (step.type() == QuestStepType.KILL && step.target().equalsIgnoreCase(event.getEntity().getType().name())) {
                        progress.incrementStepClamped(i, 1, step.amount());
                    }
                }
                plugin.completeQuestIfReady(member, quest, progress);
            }
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        PlayerProfile profile = plugin.playerDataManager().getProfile(player);
        profile.addXp(1);
        profile.applyAttributes(player);
    }

    @EventHandler
    public void onCraft(CraftItemEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }
        PlayerProfile profile = plugin.playerDataManager().getProfile(player);
        profile.addXp(2);
        profile.applyAttributes(player);
    }
}

```

---

## `MineLauncher/plugins/RPGPlugin/src/main/java/com/example/rpg/listener/CustomMobListener.java`

```java
package com.example.rpg.listener;

import com.example.rpg.RPGPlugin;
import com.example.rpg.behavior.BehaviorContext;
import com.example.rpg.behavior.BehaviorNode;
import com.example.rpg.model.LootEntry;
import com.example.rpg.model.LootTable;
import com.example.rpg.model.MobDefinition;
import com.example.rpg.model.PlayerProfile;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Display;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.TextDisplay;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

public class CustomMobListener implements Listener {
    private final RPGPlugin plugin;
    private final NamespacedKey mobKey;
    private final Random random = new Random();
    private final Map<UUID, BukkitTask> behaviorTasks = new HashMap<>();
    private final Map<UUID, BehaviorContext> behaviorContexts = new HashMap<>();
    private final Map<UUID, TextDisplay> healthBars = new HashMap<>();

    public CustomMobListener(RPGPlugin plugin) {
        this.plugin = plugin;
        this.mobKey = new NamespacedKey(plugin, "custom_mob_id");
    }

    public NamespacedKey mobKey() {
        return mobKey;
    }

    @EventHandler
    public void onDamage(EntityDamageByEntityEvent event) {
        Entity damager = event.getDamager();
        if (!(damager instanceof LivingEntity living)) {
            return;
        }
        String mobId = getMobId(living);
        if (mobId == null) {
            return;
        }
        MobDefinition mob = plugin.mobManager().getMob(mobId);
        if (mob != null) {
            event.setDamage(mob.damage());
        }
    }

    @EventHandler
    public void onDeath(EntityDeathEvent event) {
        LivingEntity entity = event.getEntity();
        String mobId = getMobId(entity);
        if (mobId == null) {
            return;
        }
        removeHealthBar(entity);
        BukkitTask task = behaviorTasks.remove(entity.getUniqueId());
        if (task != null) {
            task.cancel();
        }
        behaviorContexts.remove(entity.getUniqueId());
        MobDefinition mob = plugin.mobManager().getMob(mobId);
        if (mob == null) {
            return;
        }
        Player killer = entity.getKiller();
        if (killer != null) {
            PlayerProfile profile = plugin.playerDataManager().getProfile(killer);
            profile.addXp(mob.xp());
            profile.applyAttributes(killer);
        }
        if (mob.lootTable() != null) {
            LootTable table = plugin.lootManager().getTable(mob.lootTable());
            if (table != null) {
                for (LootEntry entry : table.entries()) {
                    if (random.nextDouble() <= entry.chance()) {
                        Material material = Material.matchMaterial(entry.material());
                        if (material != null) {
                            ItemStack item = plugin.itemGenerator().createRpgItem(material, entry.rarity(), 1);
                            item.setAmount(entry.minAmount() + random.nextInt(Math.max(1, entry.maxAmount() - entry.minAmount() + 1)));
                            event.getDrops().add(item);
                        }
                    }
                }
            }
        }
    }

    public void applyDefinition(LivingEntity entity, MobDefinition mob) {
        String name = mob.name();
        entity.customName(null);
        entity.setCustomNameVisible(false);
        if (entity.getAttribute(Attribute.GENERIC_MAX_HEALTH) != null) {
            entity.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(mob.health());
        }
        entity.setHealth(mob.health());
        entity.getPersistentDataContainer().set(mobKey, PersistentDataType.STRING, mob.id());
        if (mob.mainHand() != null) {
            Material material = Material.matchMaterial(mob.mainHand());
            if (material != null) {
                entity.getEquipment().setItemInMainHand(new ItemStack(material));
            }
        }
        if (mob.helmet() != null) {
            Material material = Material.matchMaterial(mob.helmet());
            if (material != null) {
                entity.getEquipment().setHelmet(new ItemStack(material));
            }
        }
        attachHealthBar(entity, mob, entity.getHealth());
        startBehaviorLoop(entity, mob);
    }

    private String getMobId(LivingEntity entity) {
        if (!entity.getPersistentDataContainer().has(mobKey, PersistentDataType.STRING)) {
            return null;
        }
        return entity.getPersistentDataContainer().get(mobKey, PersistentDataType.STRING);
    }

    @EventHandler
    public void onMobDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof LivingEntity living)) {
            return;
        }
        String mobId = getMobId(living);
        if (mobId == null) {
            return;
        }
        MobDefinition mob = plugin.mobManager().getMob(mobId);
        if (mob == null) {
            return;
        }
        double nextHealth = Math.max(0, living.getHealth() - event.getFinalDamage());
        updateHealthBar(living, mob, nextHealth);
    }

    @EventHandler
    public void onMobHeal(EntityRegainHealthEvent event) {
        if (!(event.getEntity() instanceof LivingEntity living)) {
            return;
        }
        String mobId = getMobId(living);
        if (mobId == null) {
            return;
        }
        MobDefinition mob = plugin.mobManager().getMob(mobId);
        if (mob == null) {
            return;
        }
        double maxHealth = mob.health();
        double nextHealth = Math.min(maxHealth, living.getHealth() + event.getAmount());
        updateHealthBar(living, mob, nextHealth);
    }

    private void startBehaviorLoop(LivingEntity entity, MobDefinition mob) {
        BehaviorNode root = plugin.behaviorTreeManager().getTree(mob.behaviorTree());
        BehaviorContext context = new BehaviorContext(plugin, entity, mob);
        behaviorContexts.put(entity.getUniqueId(), context);
        BukkitTask task = plugin.getServer().getScheduler().runTaskTimer(plugin, () -> {
            if (entity.isDead() || !entity.isValid()) {
                BukkitTask running = behaviorTasks.remove(entity.getUniqueId());
                if (running != null) {
                    running.cancel();
                }
                behaviorContexts.remove(entity.getUniqueId());
                return;
            }
            Player target = findTarget(entity);
            context.setTarget(target);
            if (target == null) {
                return;
            }
            root.tick(context);
        }, 1L, 1L);
        behaviorTasks.put(entity.getUniqueId(), task);
    }

    private Player findTarget(LivingEntity entity) {
        return entity.getWorld().getPlayers().stream()
            .filter(player -> player.getLocation().distanceSquared(entity.getLocation()) <= 400)
            .min((a, b) -> Double.compare(a.getLocation().distanceSquared(entity.getLocation()),
                b.getLocation().distanceSquared(entity.getLocation())))
            .orElse(null);
    }

    private void attachHealthBar(LivingEntity entity, MobDefinition mob, double health) {
        removeHealthBar(entity);
        TextDisplay display = entity.getWorld().spawn(entity.getLocation().add(0, 1.6, 0), TextDisplay.class);
        display.setBillboard(Display.Billboard.CENTER);
        display.setSeeThrough(true);
        display.setShadowed(true);
        display.text(net.kyori.adventure.text.Component.text(buildHealthText(mob, health)));
        entity.addPassenger(display);
        healthBars.put(entity.getUniqueId(), display);
    }

    private void updateHealthBar(LivingEntity entity, MobDefinition mob, double health) {
        TextDisplay display = healthBars.get(entity.getUniqueId());
        if (display == null || display.isDead()) {
            attachHealthBar(entity, mob, health);
            return;
        }
        display.text(net.kyori.adventure.text.Component.text(buildHealthText(mob, health)));
    }

    private void removeHealthBar(LivingEntity entity) {
        TextDisplay display = healthBars.remove(entity.getUniqueId());
        if (display != null && !display.isDead()) {
            display.remove();
        }
    }

    private String buildHealthText(MobDefinition mob, double health) {
        double maxHealth = Math.max(1, mob.health());
        int bars = 10;
        int filled = (int) Math.round((health / maxHealth) * bars);
        filled = Math.min(bars, Math.max(0, filled));
        int empty = bars - filled;
        StringBuilder bar = new StringBuilder();
        bar.append("§7[§a");
        bar.append("|".repeat(filled));
        bar.append("§c");
        bar.append("|".repeat(empty));
        bar.append("§7]");
        return mob.name() + " " + bar + " §f" + Math.round(health) + "/" + Math.round(maxHealth) + " HP";
    }
}

```

---

## `MineLauncher/plugins/RPGPlugin/src/main/java/com/example/rpg/listener/DamageIndicatorListener.java`

```java
package com.example.rpg.listener;

import com.example.rpg.RPGPlugin;
import java.util.Random;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Location;
import org.bukkit.entity.Display;
import org.bukkit.entity.Entity;
import org.bukkit.entity.TextDisplay;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;

public class DamageIndicatorListener implements Listener {
    private final RPGPlugin plugin;
    private final Random random = new Random();

    public DamageIndicatorListener(RPGPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onDamage(EntityDamageEvent event) {
        if (event.getFinalDamage() <= 0) {
            return;
        }
        Entity entity = event.getEntity();
        if (entity instanceof TextDisplay) {
            return;
        }
        NamedTextColor color = isMagicDamage(event.getCause()) ? NamedTextColor.AQUA : NamedTextColor.RED;
        String text = "-" + Math.round(event.getFinalDamage()) + " ❤";
        spawnIndicator(entity.getLocation(), text, color);
    }

    @EventHandler
    public void onRegain(EntityRegainHealthEvent event) {
        if (event.getAmount() <= 0) {
            return;
        }
        Entity entity = event.getEntity();
        if (entity instanceof TextDisplay) {
            return;
        }
        String text = "+" + Math.round(event.getAmount()) + " ❤";
        spawnIndicator(entity.getLocation(), text, NamedTextColor.GREEN);
    }

    private boolean isMagicDamage(EntityDamageEvent.DamageCause cause) {
        return switch (cause) {
            case MAGIC, DRAGON_BREATH, WITHER, POISON -> true;
            default -> false;
        };
    }

    private void spawnIndicator(Location base, String text, NamedTextColor color) {
        Location location = base.clone().add(offset(), 1.2 + offset(), offset());
        TextDisplay display = base.getWorld().spawn(location, TextDisplay.class);
        display.text(Component.text(text, color));
        display.setBillboard(Display.Billboard.CENTER);
        display.setSeeThrough(true);
        display.setShadowed(true);

        plugin.getServer().getScheduler().runTaskTimer(plugin, task -> {
            if (display.isDead()) {
                task.cancel();
                return;
            }
            display.teleport(display.getLocation().add(0, 0.04, 0));
        }, 0L, 1L);

        plugin.getServer().getScheduler().runTaskLater(plugin, display::remove, 20L);
    }

    private double offset() {
        return (random.nextDouble() - 0.5) * 0.6;
    }
}

```

---

## `MineLauncher/plugins/RPGPlugin/src/main/java/com/example/rpg/listener/GuiListener.java`

```java
package com.example.rpg.listener;

import com.example.rpg.RPGPlugin;
import com.example.rpg.gui.GuiHolders;
import com.example.rpg.model.PlayerProfile;
import com.example.rpg.model.Quest;
import com.example.rpg.model.QuestProgress;
import com.example.rpg.model.Skill;
import com.example.rpg.util.Text;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.inventory.Inventory;
import org.bukkit.Sound;
import org.bukkit.Material;

public class GuiListener implements Listener {
    private final RPGPlugin plugin;

    public GuiListener(RPGPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }
        ItemStack current = event.getCurrentItem();
        if (current == null) {
            return;
        }
        var holder = event.getInventory().getHolder();
        if (holder instanceof GuiHolders.PlayerMenuHolder) {
            event.setCancelled(true);
            switch (event.getSlot()) {
                case 12 -> plugin.guiManager().openSkillList(player);
                case 14 -> plugin.guiManager().openQuestList(player);
                default -> {
                }
            }
            return;
        }
        if (holder instanceof GuiHolders.AdminMenuHolder) {
            event.setCancelled(true);
            if (event.getSlot() == 15) {
                boolean enabled = plugin.toggleDebug(player.getUniqueId());
                player.sendMessage(Text.mm(enabled ? "<green>Debug aktiviert." : "<red>Debug deaktiviert."));
            }
            return;
        }
        if (holder instanceof GuiHolders.QuestListHolder) {
            event.setCancelled(true);
            Quest quest = resolveQuest(current);
            if (quest == null) {
                return;
            }
            PlayerProfile profile = plugin.playerDataManager().getProfile(player);
            if (profile.level() < quest.minLevel()) {
                player.sendMessage(Text.mm("<red>Du brauchst Level " + quest.minLevel() + "."));
                return;
            }
            if (profile.activeQuests().containsKey(quest.id())) {
                player.sendMessage(Text.mm("<yellow>Quest bereits aktiv."));
                return;
            }
            if (profile.completedQuests().contains(quest.id()) && !quest.repeatable()) {
                player.sendMessage(Text.mm("<red>Quest bereits abgeschlossen."));
                return;
            }
            profile.activeQuests().put(quest.id(), new QuestProgress(quest.id()));
            player.sendMessage(Text.mm("<green>Quest angenommen: " + quest.name()));
            return;
        }
        if (holder instanceof GuiHolders.SkillListHolder) {
            event.setCancelled(true);
            Skill skill = resolveSkill(current);
            if (skill == null) {
                return;
            }
            PlayerProfile profile = plugin.playerDataManager().getProfile(player);
            if (profile.skillPoints() <= 0) {
                player.sendMessage(Text.mm("<red>Keine Skillpunkte."));
                return;
            }
            if (skill.requiredSkill() != null && !profile.learnedSkills().containsKey(skill.requiredSkill())) {
                player.sendMessage(Text.mm("<red>Du musst zuerst " + skill.requiredSkill() + " lernen."));
                return;
            }
            profile.learnedSkills().put(skill.id(), profile.learnedSkills().getOrDefault(skill.id(), 0) + 1);
            profile.setSkillPoints(profile.skillPoints() - 1);
            player.sendMessage(Text.mm("<green>Skill gelernt: " + skill.name()));
            plugin.guiManager().openSkillList(player);
            return;
        }
        if (holder instanceof GuiHolders.SkillTreeHolder) {
            event.setCancelled(true);
            Skill skill = resolveSkill(current);
            if (skill == null) {
                return;
            }
            PlayerProfile profile = plugin.playerDataManager().getProfile(player);
            if (profile.skillPoints() <= 0) {
                player.sendMessage(Text.mm("<red>Keine Skillpunkte."));
                return;
            }
            if (skill.requiredSkill() != null && !profile.learnedSkills().containsKey(skill.requiredSkill())) {
                player.sendMessage(Text.mm("<red>Du musst zuerst " + skill.requiredSkill() + " lernen."));
                return;
            }
            if (profile.learnedSkills().containsKey(skill.id())) {
                player.sendMessage(Text.mm("<yellow>Bereits gelernt."));
                return;
            }
            profile.learnedSkills().put(skill.id(), 1);
            profile.setSkillPoints(profile.skillPoints() - 1);
            player.sendMessage(Text.mm("<green>Skill gelernt: " + skill.name()));
            plugin.skillTreeGui().open(player);
            return;
        }
        if (holder instanceof GuiHolders.ShopHolder shopHolder) {
            event.setCancelled(true);
            handleShopClick(player, event.getInventory(), event.getSlot(), current, shopHolder, event.isRightClick());
        }
    }

    private Quest resolveQuest(ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return null;
        }
        String questId = meta.getPersistentDataContainer().get(plugin.questKey(), PersistentDataType.STRING);
        if (questId == null) {
            return null;
        }
        return plugin.questManager().getQuest(questId);
    }

    private Skill resolveSkill(ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return null;
        }
        String skillId = meta.getPersistentDataContainer().get(plugin.skillKey(), PersistentDataType.STRING);
        if (skillId == null) {
            return null;
        }
        return plugin.skillManager().getSkill(skillId);
    }

    private void handleShopClick(Player player, Inventory inventory, int slot, ItemStack clicked,
                                 GuiHolders.ShopHolder holder, boolean rightClick) {
        var shop = plugin.shopManager().getShop(holder.shopId());
        if (shop == null) {
            player.sendMessage(Text.mm("<red>Shop nicht gefunden."));
            return;
        }
        var shopItem = shop.items().get(slot);
        if (shopItem == null) {
            return;
        }
        var profile = plugin.playerDataManager().getProfile(player);
        Material material = Material.matchMaterial(shopItem.material());
        if (material == null) {
            player.sendMessage(Text.mm("<red>Item ungültig."));
            return;
        }
        if (rightClick) {
            int sellPrice = shopItem.sellPrice();
            if (sellPrice <= 0) {
                player.sendMessage(Text.mm("<red>Dieses Item kann nicht verkauft werden."));
                return;
            }
            if (!player.getInventory().contains(material)) {
                player.sendMessage(Text.mm("<red>Du hast dieses Item nicht."));
                return;
            }
            removeOne(player.getInventory(), material);
            profile.setGold(profile.gold() + sellPrice);
            player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1f, 1.2f);
            player.sendMessage(Text.mm("<green>Verkauft für <gold>" + sellPrice + "</gold> Gold."));
        } else {
            int buyPrice = shopItem.buyPrice();
            if (buyPrice <= 0) {
                player.sendMessage(Text.mm("<red>Dieses Item kann nicht gekauft werden."));
                return;
            }
            if (profile.gold() < buyPrice) {
                player.sendMessage(Text.mm("<red>Nicht genug Gold."));
                return;
            }
            profile.setGold(profile.gold() - buyPrice);
            player.getInventory().addItem(new ItemStack(material));
            player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1f, 1.2f);
            player.sendMessage(Text.mm("<green>Gekauft für <gold>" + buyPrice + "</gold> Gold."));
        }
        player.updateInventory();
        plugin.playerDataManager().saveProfile(profile);
    }

    private void removeOne(Inventory inventory, Material material) {
        for (int i = 0; i < inventory.getSize(); i++) {
            ItemStack stack = inventory.getItem(i);
            if (stack == null || stack.getType() != material) {
                continue;
            }
            if (stack.getAmount() > 1) {
                stack.setAmount(stack.getAmount() - 1);
            } else {
                inventory.setItem(i, null);
            }
            return;
        }
    }
}

```

---

## `MineLauncher/plugins/RPGPlugin/src/main/java/com/example/rpg/listener/ItemStatListener.java`

```java
package com.example.rpg.listener;

import com.example.rpg.RPGPlugin;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerJoinEvent;

public class ItemStatListener implements Listener {
    private final RPGPlugin plugin;

    public ItemStatListener(RPGPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }
        plugin.getServer().getScheduler().runTask(plugin, () -> plugin.itemStatManager().updateSetBonus(player));
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        plugin.itemStatManager().updateSetBonus(event.getPlayer());
    }
}

```

---

## `MineLauncher/plugins/RPGPlugin/src/main/java/com/example/rpg/listener/NpcListener.java`

```java
package com.example.rpg.listener;

import com.example.rpg.RPGPlugin;
import com.example.rpg.model.Npc;
import com.example.rpg.model.NpcRole;
import com.example.rpg.util.Text;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.persistence.PersistentDataType;

public class NpcListener implements Listener {
    private final RPGPlugin plugin;

    public NpcListener(RPGPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInteract(PlayerInteractEntityEvent event) {
        Entity entity = event.getRightClicked();
        if (!entity.getPersistentDataContainer().has(plugin.npcManager().npcKey(), PersistentDataType.STRING)) {
            return;
        }
        String npcId = entity.getPersistentDataContainer().get(plugin.npcManager().npcKey(), PersistentDataType.STRING);
        if (npcId == null) {
            return;
        }
        Npc npc = plugin.npcManager().getNpc(npcId);
        if (npc == null) {
            return;
        }
        Player player = event.getPlayer();
        if (!npc.dialog().isEmpty()) {
            player.sendMessage(Text.mm("<gold>" + npc.name() + ":"));
            for (String line : npc.dialog()) {
                player.sendMessage(Text.mm("<gray>" + line));
            }
        }
        if (npc.role() == NpcRole.QUESTGIVER && npc.questLink() != null) {
            player.sendMessage(Text.mm("<yellow>Quest verfügbar: <white>" + npc.questLink()));
            plugin.guiManager().openQuestList(player);
        }
        if (npc.role() == NpcRole.VENDOR && npc.shopId() != null) {
            var shop = plugin.shopManager().getShop(npc.shopId());
            if (shop == null) {
                player.sendMessage(Text.mm("<red>Shop nicht gefunden."));
                return;
            }
            plugin.guiManager().openShop(player, shop);
        }
    }
}

```

---

## `MineLauncher/plugins/RPGPlugin/src/main/java/com/example/rpg/listener/NpcProtectionListener.java`

```java
package com.example.rpg.listener;

import com.example.rpg.RPGPlugin;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.persistence.PersistentDataType;

/**
 * Schutz: NPCs sollen nicht beschädigt, nicht getargetet und nicht "interaktiv kaputt" gemacht werden.
 * (Ohne externe Plugins.)
 */
public class NpcProtectionListener implements Listener {
    private final RPGPlugin plugin;

    public NpcProtectionListener(RPGPlugin plugin) {
        this.plugin = plugin;
    }

    private boolean isNpc(Entity entity) {
        return entity.getPersistentDataContainer().has(plugin.npcManager().npcKey(), PersistentDataType.STRING);
    }

    @EventHandler
    public void onDamage(EntityDamageEvent event) {
        if (isNpc(event.getEntity())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onTarget(EntityTargetEvent event) {
        if (event.getTarget() != null && isNpc(event.getTarget())) {
            event.setCancelled(true);
        }
    }

    // Intentionally no PlayerInteractAtEntityEvent cancel to avoid breaking normal right-click.
}

```

---

## `MineLauncher/plugins/RPGPlugin/src/main/java/com/example/rpg/listener/PlayerListener.java`

```java
package com.example.rpg.listener;

import com.example.rpg.RPGPlugin;
import com.example.rpg.model.PlayerProfile;
import com.example.rpg.util.Text;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.bukkit.Material;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;

public class PlayerListener implements Listener {
    private final RPGPlugin plugin;
    private final Map<UUID, Float> walkSpeed = new HashMap<>();
    private final Map<UUID, Float> flySpeed = new HashMap<>();

    public PlayerListener(RPGPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        freeze(player);
        plugin.playerDataManager().loadProfileAsync(player.getUniqueId()).whenComplete((profile, error) -> {
            plugin.getServer().getScheduler().runTask(plugin, () -> {
                if (!player.isOnline()) {
                    return;
                }
                PlayerProfile resolved = profile != null ? profile : plugin.playerDataManager().getProfile(player);
                resolved.applyAttributes(player);
                unfreeze(player);
            });
        });
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        if (plugin.promptManager().handle(player, event.getMessage())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onWandUse(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (!player.hasPermission("rpg.editor")) {
            return;
        }
        ItemStack item = event.getItem();
        if (item == null || item.getType() != Material.STICK) {
            return;
        }
        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return;
        }
        if (!meta.getPersistentDataContainer().has(plugin.wandKey(), PersistentDataType.BYTE)) {
            return;
        }
        if (event.getClickedBlock() == null) {
            return;
        }
        if (event.getAction() == Action.LEFT_CLICK_BLOCK) {
            player.sendMessage(Text.mm("<green>Pos1 gesetzt: " + event.getClickedBlock().getLocation().toVector()));
            player.getPersistentDataContainer().set(
                new org.bukkit.NamespacedKey(plugin, "pos1"),
                org.bukkit.persistence.PersistentDataType.STRING,
                serializeLocation(event.getClickedBlock().getLocation())
            );
        } else if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            player.sendMessage(Text.mm("<green>Pos2 gesetzt: " + event.getClickedBlock().getLocation().toVector()));
            player.getPersistentDataContainer().set(
                new org.bukkit.NamespacedKey(plugin, "pos2"),
                org.bukkit.persistence.PersistentDataType.STRING,
                serializeLocation(event.getClickedBlock().getLocation())
            );
        }
    }

    private String serializeLocation(org.bukkit.Location location) {
        return location.getWorld().getName() + "," + location.getBlockX() + "," + location.getBlockY() + "," + location.getBlockZ();
    }

    private void freeze(Player player) {
        walkSpeed.put(player.getUniqueId(), player.getWalkSpeed());
        flySpeed.put(player.getUniqueId(), player.getFlySpeed());
        player.setWalkSpeed(0f);
        player.setFlySpeed(0f);
        player.setInvulnerable(true);
        player.setCollidable(false);
    }

    private void unfreeze(Player player) {
        Float walk = walkSpeed.remove(player.getUniqueId());
        Float fly = flySpeed.remove(player.getUniqueId());
        player.setWalkSpeed(walk != null ? walk : 0.2f);
        player.setFlySpeed(fly != null ? fly : 0.1f);
        player.setInvulnerable(false);
        player.setCollidable(true);
    }
}

```

---

## `MineLauncher/plugins/RPGPlugin/src/main/java/com/example/rpg/listener/ProfessionListener.java`

```java
package com.example.rpg.listener;

import com.example.rpg.RPGPlugin;
import com.example.rpg.model.PlayerProfile;
import com.example.rpg.util.Text;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.inventory.ItemStack;

public class ProfessionListener implements Listener {
    private final RPGPlugin plugin;

    public ProfessionListener(RPGPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        if (event.isCancelled()) {
            return;
        }
        Material material = event.getBlock().getType();
        String materialKey = material.name();
        PlayerProfile profile = plugin.playerDataManager().getProfile(event.getPlayer());
        int miningXp = plugin.professionManager().xpForMaterial("mining", materialKey);
        int herbalismXp = plugin.professionManager().xpForMaterial("herbalism", materialKey);
        if (miningXp > 0) {
            plugin.professionManager().addXp(profile, "mining", miningXp, event.getPlayer());
        }
        if (herbalismXp > 0) {
            plugin.professionManager().addXp(profile, "herbalism", herbalismXp, event.getPlayer());
        }
    }

    @EventHandler
    public void onCraft(CraftItemEvent event) {
        if (!(event.getWhoClicked() instanceof org.bukkit.entity.Player player)) {
            return;
        }
        ItemStack result = event.getRecipe().getResult();
        if (result == null || result.getType().isAir()) {
            return;
        }
        String materialKey = result.getType().name();
        PlayerProfile profile = plugin.playerDataManager().getProfile(player);
        int required = plugin.professionManager().requiredLevelForCraft("blacksmithing", materialKey);
        if (required > 0 && plugin.professionManager().getLevel(profile, "blacksmithing") < required) {
            event.setCancelled(true);
            player.sendMessage(Text.mm("<red>Benötigtes Schmiede-Level: " + required));
            return;
        }
        int xp = plugin.professionManager().xpForMaterial("blacksmithing", materialKey);
        if (xp > 0) {
            plugin.professionManager().addXp(profile, "blacksmithing", xp, player);
        }
    }
}

```

---

## `MineLauncher/plugins/RPGPlugin/src/main/java/com/example/rpg/listener/SkillHotbarListener.java`

```java
package com.example.rpg.listener;

import com.example.rpg.RPGPlugin;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;

public class SkillHotbarListener implements Listener {
    private final RPGPlugin plugin;

    public SkillHotbarListener(RPGPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) {
            return;
        }
        Action action = event.getAction();
        if (action != Action.RIGHT_CLICK_AIR && action != Action.RIGHT_CLICK_BLOCK) {
            return;
        }
        var player = event.getPlayer();
        int slot = player.getInventory().getHeldItemSlot() + 1;
        var profile = plugin.playerDataManager().getProfile(player);
        String skillId = plugin.skillHotbarManager().getBinding(profile, slot);
        if (skillId == null || skillId.isBlank()) {
            return;
        }
        plugin.useSkill(player, skillId);
    }
}

```

---

## `MineLauncher/plugins/RPGPlugin/src/main/java/com/example/rpg/listener/ZoneListener.java`

```java
package com.example.rpg.listener;

import com.example.rpg.RPGPlugin;
import com.example.rpg.model.PlayerProfile;
import com.example.rpg.model.Quest;
import com.example.rpg.model.QuestProgress;
import com.example.rpg.model.QuestStep;
import com.example.rpg.model.QuestStepType;
import com.example.rpg.model.Zone;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class ZoneListener implements Listener {
    private final RPGPlugin plugin;
    private final Map<UUID, String> lastZone = new HashMap<>();

    public ZoneListener(RPGPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        Zone zone = plugin.zoneManager().getZoneAt(event.getTo());
        String zoneId = zone != null ? zone.id() : null;
        String previous = lastZone.get(player.getUniqueId());
        if ((zoneId == null && previous != null) || (zoneId != null && !zoneId.equals(previous))) {
            lastZone.put(player.getUniqueId(), zoneId);
            if (zone != null) {
                player.sendMessage(ChatColor.AQUA + "Zone betreten: " + zone.name());
                if (zone.slowMultiplier() < 1.0) {
                    player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 200, 0));
                }
                handleExploreQuests(player, zone);
            } else {
                player.sendMessage(ChatColor.GRAY + "Zone verlassen.");
            }
        }
    }

    private void handleExploreQuests(Player player, Zone zone) {
        PlayerProfile profile = plugin.playerDataManager().getProfile(player);
        for (QuestProgress progress : profile.activeQuests().values()) {
            Quest quest = plugin.questManager().getQuest(progress.questId());
            if (quest == null) {
                continue;
            }
            for (int i = 0; i < quest.steps().size(); i++) {
                QuestStep step = quest.steps().get(i);
                if (step.type() == QuestStepType.EXPLORE && step.target().equalsIgnoreCase(zone.id())) {
                    progress.incrementStepClamped(i, 1, step.amount());
                }
            }
            plugin.completeQuestIfReady(player, quest, progress);
        }
    }
}

```

---

## `MineLauncher/plugins/RPGPlugin/src/main/java/com/example/rpg/manager/ArenaManager.java`

```java
package com.example.rpg.manager;

import com.example.rpg.RPGPlugin;
import com.example.rpg.model.Arena;
import com.example.rpg.model.ArenaStatus;
import com.example.rpg.model.PlayerProfile;
import com.example.rpg.util.EloCalculator;
import com.example.rpg.util.Text;
import java.io.File;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Queue;
import java.util.UUID;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

public class ArenaManager {
    private final RPGPlugin plugin;
    private final File file;
    private final YamlConfiguration config;
    private final Map<String, Arena> arenas = new HashMap<>();
    private final Map<UUID, Arena> arenaByPlayer = new HashMap<>();
    private final Queue<UUID> queue = new ArrayDeque<>();

    public ArenaManager(RPGPlugin plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "arenas.yml");
        this.config = YamlConfiguration.loadConfiguration(file);
        if (!file.exists()) {
            seedDefaults();
        }
        load();
    }

    public void joinQueue(Player player) {
        if (arenaByPlayer.containsKey(player.getUniqueId())) {
            player.sendMessage(Text.mm("<red>Du bist bereits in einer Arena."));
            return;
        }
        if (queue.contains(player.getUniqueId())) {
            player.sendMessage(Text.mm("<yellow>Du bist bereits in der Warteschlange."));
            return;
        }
        queue.add(player.getUniqueId());
        player.sendMessage(Text.mm("<green>Du bist der PvP-Warteschlange beigetreten."));
        tryStartMatch();
    }

    public void removeFromQueue(Player player) {
        queue.remove(player.getUniqueId());
    }

    public Optional<Arena> arenaFor(Player player) {
        return Optional.ofNullable(arenaByPlayer.get(player.getUniqueId()));
    }

    public void handleDeath(Player loser) {
        Arena arena = arenaByPlayer.get(loser.getUniqueId());
        if (arena == null) {
            return;
        }
        Player winner = plugin.getServer().getPlayer(other(arena, loser.getUniqueId()));
        endMatch(arena, winner, loser);
    }

    public List<PlayerProfile> topPlayers(int limit) {
        List<PlayerProfile> profiles = new ArrayList<>(plugin.playerDataManager().profiles().values());
        profiles.sort(Comparator.comparingInt(PlayerProfile::elo).reversed());
        return profiles.subList(0, Math.min(limit, profiles.size()));
    }

    private void tryStartMatch() {
        if (queue.size() < 2) {
            return;
        }
        Arena arena = arenas.values().stream()
            .filter(a -> a.status() == ArenaStatus.WAITING)
            .findFirst()
            .orElse(null);
        if (arena == null) {
            return;
        }
        UUID playerOne = queue.poll();
        UUID playerTwo = queue.poll();
        Player p1 = plugin.getServer().getPlayer(playerOne);
        Player p2 = plugin.getServer().getPlayer(playerTwo);
        if (p1 == null || p2 == null) {
            if (p1 != null) {
                queue.add(p1.getUniqueId());
            }
            if (p2 != null) {
                queue.add(p2.getUniqueId());
            }
            return;
        }
        arena.setPlayerOne(playerOne);
        arena.setPlayerTwo(playerTwo);
        arena.setStatus(ArenaStatus.FIGHTING);
        arenaByPlayer.put(playerOne, arena);
        arenaByPlayer.put(playerTwo, arena);
        teleportPlayers(arena, p1, p2);
        p1.sendMessage(Text.mm("<gold>PvP-Kampf gestartet!"));
        p2.sendMessage(Text.mm("<gold>PvP-Kampf gestartet!"));
    }

    private void teleportPlayers(Arena arena, Player p1, Player p2) {
        World world = plugin.getServer().getWorld(arena.world());
        if (world == null) {
            return;
        }
        p1.teleport(new Location(world, arena.spawn1x() + 0.5, arena.spawn1y(), arena.spawn1z() + 0.5));
        p2.teleport(new Location(world, arena.spawn2x() + 0.5, arena.spawn2y(), arena.spawn2z() + 0.5));
    }

    private void endMatch(Arena arena, Player winner, Player loser) {
        arena.setStatus(ArenaStatus.ENDING);
        if (winner != null && loser != null) {
            PlayerProfile winnerProfile = plugin.playerDataManager().getProfile(winner);
            PlayerProfile loserProfile = plugin.playerDataManager().getProfile(loser);
            int winnerNew = EloCalculator.calculateNewRating(winnerProfile.elo(), loserProfile.elo(), 1.0, 32);
            int loserNew = EloCalculator.calculateNewRating(loserProfile.elo(), winnerProfile.elo(), 0.0, 32);
            winnerProfile.setElo(winnerNew);
            loserProfile.setElo(loserNew);
            winner.sendMessage(Text.mm("<green>Du hast gewonnen! Neuer ELO: " + winnerNew));
            loser.sendMessage(Text.mm("<red>Du hast verloren! Neuer ELO: " + loserNew));
            plugin.playerDataManager().saveProfile(winnerProfile);
            plugin.playerDataManager().saveProfile(loserProfile);
        }
        arenaByPlayer.remove(arena.playerOne());
        arenaByPlayer.remove(arena.playerTwo());
        arena.setPlayerOne(null);
        arena.setPlayerTwo(null);
        arena.setStatus(ArenaStatus.WAITING);
    }

    private UUID other(Arena arena, UUID player) {
        if (arena.playerOne() != null && arena.playerOne().equals(player)) {
            return arena.playerTwo();
        }
        return arena.playerOne();
    }

    private void load() {
        arenas.clear();
        for (String id : config.getKeys(false)) {
            ConfigurationSection section = config.getConfigurationSection(id);
            if (section == null) {
                continue;
            }
            Arena arena = new Arena(id);
            arena.setWorld(section.getString("world", "world"));
            arena.setX1(section.getInt("pos1.x"));
            arena.setY1(section.getInt("pos1.y"));
            arena.setZ1(section.getInt("pos1.z"));
            arena.setX2(section.getInt("pos2.x"));
            arena.setY2(section.getInt("pos2.y"));
            arena.setZ2(section.getInt("pos2.z"));
            arena.setSpawn1x(section.getInt("spawn1.x"));
            arena.setSpawn1y(section.getInt("spawn1.y"));
            arena.setSpawn1z(section.getInt("spawn1.z"));
            arena.setSpawn2x(section.getInt("spawn2.x"));
            arena.setSpawn2y(section.getInt("spawn2.y"));
            arena.setSpawn2z(section.getInt("spawn2.z"));
            arenas.put(id, arena);
        }
    }

    private void seedDefaults() {
        config.set("arena1.world", "world");
        config.set("arena1.pos1.x", -10);
        config.set("arena1.pos1.y", 60);
        config.set("arena1.pos1.z", -10);
        config.set("arena1.pos2.x", 10);
        config.set("arena1.pos2.y", 70);
        config.set("arena1.pos2.z", 10);
        config.set("arena1.spawn1.x", -5);
        config.set("arena1.spawn1.y", 65);
        config.set("arena1.spawn1.z", 0);
        config.set("arena1.spawn2.x", 5);
        config.set("arena1.spawn2.y", 65);
        config.set("arena1.spawn2.z", 0);
        save();
    }

    private void save() {
        try {
            config.save(file);
        } catch (IOException e) {
            plugin.getLogger().warning("Failed to save arenas.yml: " + e.getMessage());
        }
    }
}

```

---

## `MineLauncher/plugins/RPGPlugin/src/main/java/com/example/rpg/manager/AuctionHouseManager.java`

```java
package com.example.rpg.manager;

import com.example.rpg.model.AuctionListing;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;

public class AuctionHouseManager {
    private final JavaPlugin plugin;
    private final File file;
    private final YamlConfiguration config;
    private final Map<String, AuctionListing> listings = new HashMap<>();

    public AuctionHouseManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "auctions.yml");
        this.config = YamlConfiguration.loadConfiguration(file);
        load();
    }

    public Map<String, AuctionListing> listings() {
        return listings;
    }

    public AuctionListing getListing(String id) {
        return listings.get(id);
    }

    public void addListing(AuctionListing listing) {
        listings.put(listing.id(), listing);
        saveListing(listing);
    }

    public void removeListing(String id) {
        listings.remove(id);
        config.set(id, null);
        save();
    }

    public void saveListing(AuctionListing listing) {
        ConfigurationSection section = config.createSection(listing.id());
        section.set("seller", listing.seller() != null ? listing.seller().toString() : null);
        section.set("price", listing.price());
        section.set("item", listing.itemData());
        save();
    }

    public String serializeItem(ItemStack item) {
        try (java.io.ByteArrayOutputStream output = new java.io.ByteArrayOutputStream();
             BukkitObjectOutputStream dataOut = new BukkitObjectOutputStream(output)) {
            dataOut.writeObject(item);
            return java.util.Base64.getEncoder().encodeToString(output.toByteArray());
        } catch (IOException e) {
            plugin.getLogger().warning("Failed to serialize item: " + e.getMessage());
            return null;
        }
    }

    public ItemStack deserializeItem(String data) {
        if (data == null) {
            return null;
        }
        try (java.io.ByteArrayInputStream input = new java.io.ByteArrayInputStream(java.util.Base64.getDecoder().decode(data));
             BukkitObjectInputStream dataIn = new BukkitObjectInputStream(input)) {
            Object obj = dataIn.readObject();
            return obj instanceof ItemStack item ? item : null;
        } catch (IOException | ClassNotFoundException e) {
            plugin.getLogger().warning("Failed to deserialize item: " + e.getMessage());
            return null;
        }
    }

    private void load() {
        listings.clear();
        for (String id : config.getKeys(false)) {
            ConfigurationSection section = config.getConfigurationSection(id);
            if (section == null) {
                continue;
            }
            AuctionListing listing = new AuctionListing(id);
            String seller = section.getString("seller", null);
            listing.setSeller(seller != null ? UUID.fromString(seller) : null);
            listing.setPrice(section.getInt("price", 0));
            listing.setItemData(section.getString("item", null));
            listings.put(id, listing);
        }
    }

    private void save() {
        try {
            config.save(file);
        } catch (IOException e) {
            plugin.getLogger().warning("Failed to save auctions.yml: " + e.getMessage());
        }
    }
}

```

---

## `MineLauncher/plugins/RPGPlugin/src/main/java/com/example/rpg/manager/BehaviorTreeManager.java`

```java
package com.example.rpg.manager;

import com.example.rpg.behavior.BehaviorNode;
import com.example.rpg.behavior.CastSkillNode;
import com.example.rpg.behavior.CooldownNode;
import com.example.rpg.behavior.FleeNode;
import com.example.rpg.behavior.HealthBelowNode;
import com.example.rpg.behavior.HealSelfNode;
import com.example.rpg.behavior.InverterNode;
import com.example.rpg.behavior.MeleeAttackNode;
import com.example.rpg.behavior.SelectorNode;
import com.example.rpg.behavior.SequenceNode;
import com.example.rpg.behavior.TargetDistanceAboveNode;
import com.example.rpg.behavior.WalkToTargetNode;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public class BehaviorTreeManager {
    private final JavaPlugin plugin;
    private final File folder;
    private final Map<String, BehaviorNode> trees = new HashMap<>();

    public BehaviorTreeManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.folder = new File(plugin.getDataFolder(), "behaviors");
        if (!folder.exists()) {
            folder.mkdirs();
        }
        seedSkeletonKing();
        loadAll();
    }

    public BehaviorNode getTree(String name) {
        if (name == null) {
            return defaultTree();
        }
        return trees.getOrDefault(name, defaultTree());
    }

    private void loadAll() {
        trees.clear();
        File[] files = folder.listFiles((dir, file) -> file.endsWith(".yml"));
        if (files == null) {
            return;
        }
        for (File file : files) {
            YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
            BehaviorNode root = parseNode(config, "root");
            String key = file.getName().replace(".yml", "");
            if (root != null) {
                trees.put(key, root);
            }
        }
    }

    private BehaviorNode parseNode(ConfigurationSection section, String fallbackId) {
        if (section == null) {
            return null;
        }
        String type = section.getString("type", "selector");
        String id = section.getString("id", fallbackId + "-" + UUID.randomUUID());
        return buildNode(type, id, section);
    }

    private BehaviorNode buildNode(String type, String id, ConfigurationSection section) {
        return switch (type.toLowerCase()) {
            case "selector" -> buildComposite(new SelectorNode(id), section);
            case "sequence" -> buildComposite(new SequenceNode(id), section);
            case "inverter" -> {
                BehaviorNode child = parseChild(section, "child", id);
                yield child != null ? new InverterNode(id, child) : null;
            }
            case "cooldown" -> {
                BehaviorNode child = parseChild(section, "child", id);
                long cooldown = (long) (section.getDouble("cooldownSeconds", 5) * 1000);
                yield child != null ? new CooldownNode(id, child, cooldown) : null;
            }
            case "melee_attack" -> new MeleeAttackNode(id);
            case "cast_skill" -> new CastSkillNode(id, section.getString("skill", "ember_shot"));
            case "flee" -> new FleeNode(id);
            case "heal_self" -> new HealSelfNode(id, section.getDouble("amount", 6));
            case "walk_to_target" -> new WalkToTargetNode(id);
            case "health_below" -> new HealthBelowNode(id, section.getDouble("threshold", 0.2));
            case "target_distance_above" -> new TargetDistanceAboveNode(id, section.getDouble("distance", 10));
            default -> null;
        };
    }

    private BehaviorNode buildComposite(com.example.rpg.behavior.CompositeNode node, ConfigurationSection section) {
        List<Map<?, ?>> children = section.getMapList("children");
        for (int i = 0; i < children.size(); i++) {
            Map<?, ?> data = children.get(i);
            if (!(data.get("type") instanceof String childType)) {
                continue;
            }
            YamlConfiguration childConfig = new YamlConfiguration();
            for (Map.Entry<?, ?> entry : data.entrySet()) {
                childConfig.set(String.valueOf(entry.getKey()), entry.getValue());
            }
            BehaviorNode child = buildNode(childType, node.id() + "-child-" + i, childConfig);
            if (child != null) {
                node.children().add(child);
            }
        }
        return node;
    }

    private BehaviorNode parseChild(ConfigurationSection section, String key, String id) {
        ConfigurationSection childSection = section.getConfigurationSection(key);
        if (childSection != null) {
            return parseNode(childSection, id + "-child");
        }
        return null;
    }

    private BehaviorNode defaultTree() {
        SelectorNode root = new SelectorNode("default-root");
        SequenceNode chase = new SequenceNode("default-chase");
        chase.children().add(new TargetDistanceAboveNode("default-dist", 2));
        chase.children().add(new WalkToTargetNode("default-walk"));
        root.children().add(chase);
        root.children().add(new MeleeAttackNode("default-melee"));
        return root;
    }

    private void seedSkeletonKing() {
        File file = new File(folder, "skeleton_king.yml");
        if (file.exists()) {
            return;
        }
        YamlConfiguration config = new YamlConfiguration();
        config.set("type", "selector");
        List<Map<String, Object>> children = new ArrayList<>();

        Map<String, Object> emergency = new HashMap<>();
        emergency.put("type", "sequence");
        List<Map<String, Object>> emergencyChildren = new ArrayList<>();
        emergencyChildren.add(Map.of("type", "health_below", "threshold", 0.2));
        emergencyChildren.add(Map.of("type", "cast_skill", "skill", "shield_wall"));
        emergencyChildren.add(Map.of("type", "heal_self", "amount", 8));
        emergency.put("children", emergencyChildren);
        children.add(emergency);

        Map<String, Object> ranged = new HashMap<>();
        ranged.put("type", "sequence");
        List<Map<String, Object>> rangedChildren = new ArrayList<>();
        rangedChildren.add(Map.of("type", "target_distance_above", "distance", 10));
        rangedChildren.add(Map.of("type", "cast_skill", "skill", "ember_shot"));
        ranged.put("children", rangedChildren);
        children.add(ranged);

        children.add(Map.of("type", "melee_attack"));
        config.set("children", children);
        try {
            config.save(file);
        } catch (IOException e) {
            plugin.getLogger().warning("Failed to seed skeleton_king.yml: " + e.getMessage());
        }
    }
}

```

---

## `MineLauncher/plugins/RPGPlugin/src/main/java/com/example/rpg/manager/ClassManager.java`

```java
package com.example.rpg.manager;

import com.example.rpg.model.ClassDefinition;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public class ClassManager {
    private final JavaPlugin plugin;
    private final File file;
    private final YamlConfiguration config;
    private final Map<String, ClassDefinition> classes = new HashMap<>();

    public ClassManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "classes.yml");
        this.config = YamlConfiguration.loadConfiguration(file);
        if (!file.exists()) {
            seedDefaults();
        }
        load();
    }

    public ClassDefinition getClass(String id) {
        return classes.get(id);
    }

    public Map<String, ClassDefinition> classes() {
        return classes;
    }

    public void saveClass(ClassDefinition definition) {
        ConfigurationSection section = config.createSection(definition.id());
        section.set("name", definition.name());
        section.set("startSkills", definition.startSkills());
        save();
    }

    public void saveAll() {
        config.getKeys(false).forEach(key -> config.set(key, null));
        for (ClassDefinition definition : classes.values()) {
            saveClass(definition);
        }
        save();
    }

    private void load() {
        classes.clear();
        for (String id : config.getKeys(false)) {
            ConfigurationSection section = config.getConfigurationSection(id);
            if (section == null) {
                continue;
            }
            ClassDefinition definition = new ClassDefinition(id);
            definition.setName(section.getString("name", id));
            definition.setStartSkills(section.getStringList("startSkills"));
            classes.put(id, definition);
        }
    }

    private void seedDefaults() {
        ClassDefinition warrior = new ClassDefinition("warrior");
        warrior.setName("Krieger");
        warrior.setStartSkills(List.of("taunt"));

        ClassDefinition ranger = new ClassDefinition("ranger");
        ranger.setName("Ranger");
        ranger.setStartSkills(List.of("dash"));

        ClassDefinition mage = new ClassDefinition("mage");
        mage.setName("Magier");
        mage.setStartSkills(List.of("heal"));

        classes.put(warrior.id(), warrior);
        classes.put(ranger.id(), ranger);
        classes.put(mage.id(), mage);
        saveAll();
    }

    private void save() {
        try {
            config.save(file);
        } catch (IOException e) {
            plugin.getLogger().warning("Failed to save classes.yml: " + e.getMessage());
        }
    }
}

```

---

## `MineLauncher/plugins/RPGPlugin/src/main/java/com/example/rpg/manager/DungeonManager.java`

```java
package com.example.rpg.manager;

import com.example.rpg.RPGPlugin;
import com.example.rpg.dungeon.DungeonGenerator;
import com.example.rpg.dungeon.DungeonInstance;
import com.example.rpg.util.WorldUtils;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

public class DungeonManager {
    private final RPGPlugin plugin;
    private final FileConfiguration config;
    private Location entrance;
    private Location exit;
    private final Map<UUID, Location> returnLocations = new HashMap<>();
    private final DungeonGenerator generator;
    private final Map<UUID, DungeonInstance> activeInstances = new HashMap<>();
    private final List<DungeonInstance> allInstances = new ArrayList<>();

    public DungeonManager(RPGPlugin plugin) {
        this.plugin = plugin;
        this.config = plugin.getConfig();
        this.generator = new DungeonGenerator(plugin);
        load();
    }

    public Location getEntrance() {
        return entrance;
    }

    public void enterDungeon(org.bukkit.entity.Player player) {
        returnLocations.put(player.getUniqueId(), player.getLocation());
        if (entrance != null) {
            player.teleport(entrance);
        }
    }

    public void leaveDungeon(org.bukkit.entity.Player player) {
        Location back = returnLocations.remove(player.getUniqueId());
        activeInstances.remove(player.getUniqueId());
        if (back != null) {
            player.teleport(back);
            return;
        }
        if (exit != null) {
            player.teleport(exit);
            return;
        }
        if (!plugin.getServer().getWorlds().isEmpty()) {
            player.teleport(plugin.getServer().getWorlds().get(0).getSpawnLocation());
        }
    }

    public void generateDungeon(Player player, String theme, List<Player> party) {
        java.util.function.Consumer<DungeonInstance> onGenerated = instance -> {
            allInstances.add(instance);
            for (Player member : party) {
                returnLocations.put(member.getUniqueId(), member.getLocation());
                activeInstances.put(member.getUniqueId(), instance);
            }
            plugin.getServer().getScheduler().runTaskLater(plugin, () -> closeDungeon(instance), 20L * 60L * 15L);
        };

        if ("wfc".equalsIgnoreCase(theme)) {
            generator.generateWfc(theme, party, onGenerated);
            return;
        }
        DungeonInstance instance = generator.generate(theme, party);
        onGenerated.accept(instance);
    }

    public void closeDungeon(DungeonInstance instance) {
        if (!allInstances.contains(instance)) {
            return;
        }
        WorldUtils.unloadAndDeleteWorld(instance.world(), exit != null ? exit : entrance);
        allInstances.remove(instance);
        activeInstances.values().removeIf(active -> active.equals(instance));
    }

    public void shutdown() {
        for (DungeonInstance instance : new ArrayList<>(allInstances)) {
            WorldUtils.unloadAndDeleteWorld(instance.world(), exit);
        }
        allInstances.clear();
        activeInstances.clear();
    }

    private void load() {
        String world = config.getString("dungeon.entrance.world", null);
        if (world != null && plugin.getServer().getWorld(world) != null) {
            entrance = new Location(plugin.getServer().getWorld(world),
                config.getDouble("dungeon.entrance.x"),
                config.getDouble("dungeon.entrance.y"),
                config.getDouble("dungeon.entrance.z"));
        }
        String exitWorld = config.getString("dungeon.exit.world", null);
        if (exitWorld != null && plugin.getServer().getWorld(exitWorld) != null) {
            exit = new Location(plugin.getServer().getWorld(exitWorld),
                config.getDouble("dungeon.exit.x"),
                config.getDouble("dungeon.exit.y"),
                config.getDouble("dungeon.exit.z"));
        }
    }
}

```

---

## `MineLauncher/plugins/RPGPlugin/src/main/java/com/example/rpg/manager/FactionManager.java`

```java
package com.example.rpg.manager;

import com.example.rpg.model.Faction;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public class FactionManager {
    private final JavaPlugin plugin;
    private final File file;
    private final YamlConfiguration config;
    private final Map<String, Faction> factions = new HashMap<>();

    public FactionManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "factions.yml");
        this.config = YamlConfiguration.loadConfiguration(file);
        if (!file.exists()) {
            seedDefaults();
        }
        load();
    }

    public Faction getFaction(String id) {
        return factions.get(id);
    }

    public Map<String, Faction> factions() {
        return factions;
    }

    public void saveFaction(Faction faction) {
        ConfigurationSection section = config.createSection(faction.id());
        section.set("name", faction.name());
        save();
    }

    public void saveAll() {
        config.getKeys(false).forEach(key -> config.set(key, null));
        for (Faction faction : factions.values()) {
            saveFaction(faction);
        }
        save();
    }

    private void load() {
        factions.clear();
        for (String id : config.getKeys(false)) {
            ConfigurationSection section = config.getConfigurationSection(id);
            if (section == null) {
                continue;
            }
            Faction faction = new Faction(id);
            faction.setName(section.getString("name", id));
            factions.put(id, faction);
        }
    }

    private void seedDefaults() {
        Faction faction = new Faction("adventurers");
        faction.setName("Abenteurergilde");
        factions.put(faction.id(), faction);
        saveAll();
    }

    private void save() {
        try {
            config.save(file);
        } catch (IOException e) {
            plugin.getLogger().warning("Failed to save factions.yml: " + e.getMessage());
        }
    }
}

```

---

## `MineLauncher/plugins/RPGPlugin/src/main/java/com/example/rpg/manager/GuildManager.java`

```java
package com.example.rpg.manager;

import com.example.rpg.RPGPlugin;
import com.example.rpg.model.Guild;
import com.example.rpg.model.GuildMemberRole;
import com.example.rpg.model.GuildQuest;
import com.example.rpg.model.PlayerProfile;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

public class GuildManager {
    private final RPGPlugin plugin;
    private final File file;
    private final YamlConfiguration config;
    private final Map<String, Guild> guilds = new HashMap<>();
    private final Map<UUID, String> guildByMember = new HashMap<>();
    private final Map<UUID, String> pendingInvites = new HashMap<>();

    public GuildManager(RPGPlugin plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "guilds.yml");
        this.config = YamlConfiguration.loadConfiguration(file);
        load();
    }

    public Optional<Guild> guildById(String id) {
        return Optional.ofNullable(guilds.get(id));
    }

    public Optional<Guild> guildFor(UUID member) {
        String id = guildByMember.get(member);
        return id == null ? Optional.empty() : guildById(id);
    }

    public boolean isMember(UUID member) {
        return guildByMember.containsKey(member);
    }

    public void createGuild(String id, String name, Player leader) {
        Guild guild = new Guild(id);
        guild.setName(name);
        guild.setLeader(leader.getUniqueId());
        guild.members().put(leader.getUniqueId(), GuildMemberRole.LEADER);
        guilds.put(id, guild);
        guildByMember.put(leader.getUniqueId(), id);
        PlayerProfile profile = plugin.playerDataManager().getProfile(leader);
        profile.setGuildId(id);
        saveGuild(guild);
    }

    public void disbandGuild(Guild guild) {
        for (UUID member : guild.members().keySet()) {
            guildByMember.remove(member);
            PlayerProfile profile = plugin.playerDataManager().getProfile(member);
            profile.setGuildId(null);
        }
        guilds.remove(guild.id());
        config.set(guild.id(), null);
        save();
    }

    public void invite(UUID target, String guildId) {
        pendingInvites.put(target, guildId);
    }

    public Optional<Guild> acceptInvite(UUID playerId) {
        String guildId = pendingInvites.remove(playerId);
        if (guildId == null) {
            return Optional.empty();
        }
        Guild guild = guilds.get(guildId);
        if (guild == null) {
            return Optional.empty();
        }
        guild.members().put(playerId, GuildMemberRole.MEMBER);
        guildByMember.put(playerId, guildId);
        PlayerProfile profile = plugin.playerDataManager().getProfile(playerId);
        profile.setGuildId(guildId);
        saveGuild(guild);
        return Optional.of(guild);
    }

    public void leaveGuild(UUID member) {
        String guildId = guildByMember.remove(member);
        if (guildId == null) {
            return;
        }
        Guild guild = guilds.get(guildId);
        if (guild == null) {
            return;
        }
        if (guild.leader() != null && guild.leader().equals(member)) {
            disbandGuild(guild);
            return;
        }
        guild.members().remove(member);
        PlayerProfile profile = plugin.playerDataManager().getProfile(member);
        profile.setGuildId(null);
        saveGuild(guild);
    }

    public void setRole(Guild guild, UUID member, GuildMemberRole role) {
        guild.members().put(member, role);
        saveGuild(guild);
    }

    public void deposit(Guild guild, int amount) {
        guild.setBankGold(guild.bankGold() + amount);
        saveGuild(guild);
    }

    public boolean withdraw(Guild guild, int amount) {
        if (guild.bankGold() < amount) {
            return false;
        }
        guild.setBankGold(guild.bankGold() - amount);
        saveGuild(guild);
        return true;
    }

    public void saveAll() {
        for (Guild guild : guilds.values()) {
            saveGuild(guild);
        }
        save();
    }

    private void load() {
        guilds.clear();
        guildByMember.clear();
        for (String id : config.getKeys(false)) {
            ConfigurationSection section = config.getConfigurationSection(id);
            if (section == null) {
                continue;
            }
            Guild guild = new Guild(id);
            guild.setName(section.getString("name", id));
            String leader = section.getString("leader", null);
            if (leader != null) {
                guild.setLeader(UUID.fromString(leader));
            }
            guild.setBankGold(section.getInt("bankGold", 0));
            ConfigurationSection members = section.getConfigurationSection("members");
            if (members != null) {
                for (String uuid : members.getKeys(false)) {
                    try {
                        GuildMemberRole role = GuildMemberRole.valueOf(members.getString(uuid, "MEMBER"));
                        UUID memberId = UUID.fromString(uuid);
                        guild.members().put(memberId, role);
                        guildByMember.put(memberId, id);
                    } catch (IllegalArgumentException ignored) {
                    }
                }
            }
            ConfigurationSection quests = section.getConfigurationSection("quests");
            if (quests != null) {
                for (String questId : quests.getKeys(false)) {
                    ConfigurationSection questSection = quests.getConfigurationSection(questId);
                    if (questSection == null) {
                        continue;
                    }
                    GuildQuest quest = new GuildQuest(questId);
                    quest.setName(questSection.getString("name", questId));
                    quest.setDescription(questSection.getString("description", ""));
                    quest.setGoal(questSection.getInt("goal", 1));
                    quest.setProgress(questSection.getInt("progress", 0));
                    quest.setCompleted(questSection.getBoolean("completed", false));
                    guild.quests().put(questId, quest);
                }
            }
            guilds.put(id, guild);
        }
    }

    private void saveGuild(Guild guild) {
        ConfigurationSection section = config.createSection(guild.id());
        section.set("name", guild.name());
        section.set("leader", guild.leader() != null ? guild.leader().toString() : null);
        section.set("bankGold", guild.bankGold());
        ConfigurationSection members = section.createSection("members");
        for (Map.Entry<UUID, GuildMemberRole> entry : guild.members().entrySet()) {
            members.set(entry.getKey().toString(), entry.getValue().name());
        }
        ConfigurationSection quests = section.createSection("quests");
        for (GuildQuest quest : guild.quests().values()) {
            ConfigurationSection questSection = quests.createSection(quest.id());
            questSection.set("name", quest.name());
            questSection.set("description", quest.description());
            questSection.set("goal", quest.goal());
            questSection.set("progress", quest.progress());
            questSection.set("completed", quest.completed());
        }
        save();
    }

    private void save() {
        try {
            config.save(file);
        } catch (IOException e) {
            plugin.getLogger().warning("Failed to save guilds.yml: " + e.getMessage());
        }
    }
}

```

---

## `MineLauncher/plugins/RPGPlugin/src/main/java/com/example/rpg/manager/ItemStatManager.java`

```java
package com.example.rpg.manager;

import com.example.rpg.util.Text;
import java.util.List;
import java.util.Map;
import java.util.Random;
import net.kyori.adventure.text.Component;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class ItemStatManager {
    private final Random random = new Random();
    private final NamespacedKey strengthKey;
    private final NamespacedKey critKey;
    private final NamespacedKey healthKey;
    private final NamespacedKey setIdKey;
    private final Map<String, PotionEffectType> setBonuses = Map.of(
        "ember", PotionEffectType.FIRE_RESISTANCE,
        "guardian", PotionEffectType.DAMAGE_RESISTANCE,
        "swift", PotionEffectType.SPEED
    );

    public ItemStatManager(JavaPlugin plugin) {
        this.strengthKey = new NamespacedKey(plugin, "stat_strength");
        this.critKey = new NamespacedKey(plugin, "stat_crit");
        this.healthKey = new NamespacedKey(plugin, "stat_health");
        this.setIdKey = new NamespacedKey(plugin, "set_id");
    }

    public void applyAffixes(ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return;
        }
        String prefix = randomFrom(List.of("Brennendes", "Gefrorenes", "Stählernen", "Mystisches"));
        String suffix = randomFrom(List.of("der Stärke", "der Präzision", "des Lebens"));
        meta.displayName(Component.text(prefix + " " + prettyName(item.getType().name()) + " " + suffix));

        PersistentDataContainer data = meta.getPersistentDataContainer();
        data.set(strengthKey, PersistentDataType.INTEGER, 1 + random.nextInt(4));
        data.set(critKey, PersistentDataType.DOUBLE, 0.02 + random.nextDouble() * 0.08);
        data.set(healthKey, PersistentDataType.INTEGER, 2 + random.nextInt(6));
        data.set(setIdKey, PersistentDataType.STRING, randomFrom(setBonuses.keySet().stream().toList()));

        updateLore(meta);
        item.setItemMeta(meta);
    }

    public void updateLore(ItemMeta meta) {
        PersistentDataContainer data = meta.getPersistentDataContainer();
        int strength = data.getOrDefault(strengthKey, PersistentDataType.INTEGER, 0);
        double crit = data.getOrDefault(critKey, PersistentDataType.DOUBLE, 0.0);
        int health = data.getOrDefault(healthKey, PersistentDataType.INTEGER, 0);
        String setId = data.get(setIdKey, PersistentDataType.STRING);
        meta.lore(List.of(
            Text.mm("<gray>Stärke: <white>" + strength),
            Text.mm("<gray>Krit-Chance: <white>" + String.format("%.1f%%", crit * 100)),
            Text.mm("<gray>Leben: <white>" + health),
            setId != null ? Text.mm("<gold>Set: " + setId + " (4 Teile)") : Text.mm("<gray>Kein Set")
        ));
    }

    public void updateSetBonus(Player player) {
        Map<String, Integer> counts = new java.util.HashMap<>();
        for (ItemStack item : player.getInventory().getArmorContents()) {
            if (item == null || item.getItemMeta() == null) {
                continue;
            }
            String setId = item.getItemMeta().getPersistentDataContainer().get(setIdKey, PersistentDataType.STRING);
            if (setId == null) {
                continue;
            }
            counts.put(setId, counts.getOrDefault(setId, 0) + 1);
        }
        for (Map.Entry<String, PotionEffectType> entry : setBonuses.entrySet()) {
            PotionEffectType type = entry.getValue();
            if (type == null) {
                continue;
            }
            if (counts.getOrDefault(entry.getKey(), 0) >= 4) {
                player.addPotionEffect(new PotionEffect(type, 220, 0, true, false));
            } else {
                player.removePotionEffect(type);
            }
        }
    }

    public NamespacedKey strengthKey() {
        return strengthKey;
    }

    public NamespacedKey critKey() {
        return critKey;
    }

    public NamespacedKey healthKey() {
        return healthKey;
    }

    public NamespacedKey setIdKey() {
        return setIdKey;
    }

    private String randomFrom(List<String> values) {
        return values.get(random.nextInt(values.size()));
    }

    private String prettyName(String material) {
        return material.toLowerCase().replace("_", " ");
    }
}

```

---

## `MineLauncher/plugins/RPGPlugin/src/main/java/com/example/rpg/manager/LootManager.java`

```java
package com.example.rpg.manager;

import com.example.rpg.model.LootEntry;
import com.example.rpg.model.LootTable;
import com.example.rpg.model.Rarity;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public class LootManager {
    private final JavaPlugin plugin;
    private final File file;
    private final YamlConfiguration config;
    private final Map<String, LootTable> tables = new HashMap<>();

    public LootManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "loot.yml");
        this.config = YamlConfiguration.loadConfiguration(file);
        if (!file.exists()) {
            seedDefaults();
        }
        load();
    }

    public Map<String, LootTable> tables() {
        return tables;
    }

    public LootTable getTable(String id) {
        return tables.get(id);
    }

    public LootTable getTableFor(String key) {
        for (LootTable table : tables.values()) {
            if (table.appliesTo().equalsIgnoreCase(key)) {
                return table;
            }
        }
        return null;
    }

    public void saveTable(LootTable table) {
        ConfigurationSection section = config.createSection(table.id());
        section.set("appliesTo", table.appliesTo());
        List<Map<String, Object>> entries = new ArrayList<>();
        for (LootEntry entry : table.entries()) {
            Map<String, Object> map = new HashMap<>();
            map.put("material", entry.material());
            map.put("chance", entry.chance());
            map.put("minAmount", entry.minAmount());
            map.put("maxAmount", entry.maxAmount());
            map.put("rarity", entry.rarity().name());
            entries.add(map);
        }
        section.set("entries", entries);
        save();
    }

    public void saveAll() {
        config.getKeys(false).forEach(key -> config.set(key, null));
        for (LootTable table : tables.values()) {
            saveTable(table);
        }
        save();
    }

    private void load() {
        tables.clear();
        for (String id : config.getKeys(false)) {
            ConfigurationSection section = config.getConfigurationSection(id);
            if (section == null) {
                continue;
            }
            LootTable table = new LootTable(id);
            table.setAppliesTo(section.getString("appliesTo", "ZOMBIE"));
            List<LootEntry> entries = new ArrayList<>();
            for (Map<?, ?> raw : section.getMapList("entries")) {
                Object materialValue = raw.containsKey("material") ? raw.get("material") : "IRON_NUGGET";
                Object chanceValue = raw.containsKey("chance") ? raw.get("chance") : 0.3;
                Object minValue = raw.containsKey("minAmount") ? raw.get("minAmount") : 1;
                Object maxValue = raw.containsKey("maxAmount") ? raw.get("maxAmount") : 1;
                Object rarityValue = raw.containsKey("rarity") ? raw.get("rarity") : "COMMON";
                String material = String.valueOf(materialValue);
                double chance = Double.parseDouble(String.valueOf(chanceValue));
                int minAmount = Integer.parseInt(String.valueOf(minValue));
                int maxAmount = Integer.parseInt(String.valueOf(maxValue));
                Rarity rarity = Rarity.valueOf(String.valueOf(rarityValue));
                entries.add(new LootEntry(material, chance, minAmount, maxAmount, rarity));
            }
            table.setEntries(entries);
            tables.put(id, table);
        }
    }

    private void seedDefaults() {
        LootTable table = new LootTable("forest_mobs");
        table.setAppliesTo("ZOMBIE");
        table.setEntries(List.of(
            new LootEntry("IRON_NUGGET", 0.5, 1, 3, Rarity.COMMON),
            new LootEntry("EMERALD", 0.15, 1, 1, Rarity.RARE)
        ));
        tables.put(table.id(), table);
        saveAll();
    }

    private void save() {
        try {
            config.save(file);
        } catch (IOException e) {
            plugin.getLogger().warning("Failed to save loot.yml: " + e.getMessage());
        }
    }
}

```

---

## `MineLauncher/plugins/RPGPlugin/src/main/java/com/example/rpg/manager/MobManager.java`

```java
package com.example.rpg.manager;

import com.example.rpg.model.MobDefinition;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public class MobManager {
    private final JavaPlugin plugin;
    private final File file;
    private final YamlConfiguration config;
    private final Map<String, MobDefinition> mobs = new HashMap<>();

    public MobManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "mobs.yml");
        this.config = YamlConfiguration.loadConfiguration(file);
        if (!file.exists()) {
            seedDefaults();
        }
        load();
    }

    public MobDefinition getMob(String id) {
        return mobs.get(id);
    }

    public Map<String, MobDefinition> mobs() {
        return mobs;
    }

    public void saveMob(MobDefinition mob) {
        ConfigurationSection section = config.createSection(mob.id());
        section.set("name", mob.name());
        section.set("type", mob.type());
        section.set("health", mob.health());
        section.set("damage", mob.damage());
        section.set("mainHand", mob.mainHand());
        section.set("helmet", mob.helmet());
        section.set("skills", mob.skills());
        section.set("skillIntervalSeconds", mob.skillIntervalSeconds());
        section.set("xp", mob.xp());
        section.set("lootTable", mob.lootTable());
        section.set("behaviorTree", mob.behaviorTree());
        save();
    }

    public void saveAll() {
        config.getKeys(false).forEach(key -> config.set(key, null));
        for (MobDefinition mob : mobs.values()) {
            saveMob(mob);
        }
        save();
    }

    private void load() {
        mobs.clear();
        for (String id : config.getKeys(false)) {
            ConfigurationSection section = config.getConfigurationSection(id);
            if (section == null) {
                continue;
            }
            MobDefinition mob = new MobDefinition(id);
            mob.setName(section.getString("name", id));
            mob.setType(section.getString("type", "ZOMBIE"));
            mob.setHealth(section.getDouble("health", 40));
            mob.setDamage(section.getDouble("damage", 6));
            mob.setMainHand(section.getString("mainHand", null));
            mob.setHelmet(section.getString("helmet", null));
            mob.setSkills(section.getStringList("skills"));
            mob.setSkillIntervalSeconds(section.getInt("skillIntervalSeconds", 8));
            mob.setXp(section.getInt("xp", 50));
            mob.setLootTable(section.getString("lootTable", null));
            mob.setBehaviorTree(section.getString("behaviorTree", null));
            mobs.put(id, mob);
        }
    }

    private void seedDefaults() {
        MobDefinition zombie = new MobDefinition("boss_zombie");
        zombie.setName("§cSeuchenbringer");
        zombie.setType("ZOMBIE");
        zombie.setHealth(60);
        zombie.setDamage(8);
        zombie.setMainHand("IRON_SWORD");
        zombie.setHelmet("IRON_HELMET");
        zombie.setSkills(List.of("ember_shot", "whirlwind"));
        zombie.setSkillIntervalSeconds(10);
        zombie.setXp(120);
        zombie.setLootTable("forest_mobs");
        mobs.put(zombie.id(), zombie);

        MobDefinition skeletonKing = new MobDefinition("skeleton_king");
        skeletonKing.setName("§cSkelettkönig");
        skeletonKing.setType("SKELETON");
        skeletonKing.setHealth(80);
        skeletonKing.setDamage(10);
        skeletonKing.setMainHand("DIAMOND_SWORD");
        skeletonKing.setHelmet("GOLDEN_HELMET");
        skeletonKing.setSkills(List.of("shield_wall", "ember_shot"));
        skeletonKing.setSkillIntervalSeconds(8);
        skeletonKing.setXp(180);
        skeletonKing.setLootTable("forest_mobs");
        skeletonKing.setBehaviorTree("skeleton_king");
        mobs.put(skeletonKing.id(), skeletonKing);
        saveAll();
    }

    private void save() {
        try {
            config.save(file);
        } catch (IOException e) {
            plugin.getLogger().warning("Failed to save mobs.yml: " + e.getMessage());
        }
    }
}

```

---

## `MineLauncher/plugins/RPGPlugin/src/main/java/com/example/rpg/manager/NpcManager.java`

```java
package com.example.rpg.manager;

import com.example.rpg.model.Npc;
import com.example.rpg.model.NpcRole;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Villager;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

public class NpcManager {
    private final JavaPlugin plugin;
    private final File file;
    private final YamlConfiguration config;
    private final Map<String, Npc> npcs = new HashMap<>();
    private final NamespacedKey npcKey;

    public NpcManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "npcs.yml");
        this.config = YamlConfiguration.loadConfiguration(file);
        this.npcKey = new NamespacedKey(plugin, "npc_id");
        load();
    }

    public NamespacedKey npcKey() {
        return npcKey;
    }

    public Map<String, Npc> npcs() {
        return npcs;
    }

    public Npc getNpc(String id) {
        return npcs.get(id);
    }

    public void spawnAll() {
        for (Npc npc : npcs.values()) {
            spawnNpc(npc);
        }
    }

    public void spawnNpc(Npc npc) {
        World world = Bukkit.getWorld(npc.world());
        if (world == null) {
            return;
        }
        Location location = npc.toLocation(world);
        Villager villager = (Villager) world.spawnEntity(location, EntityType.VILLAGER);
        villager.customName(Component.text(npc.name()));
        villager.setCustomNameVisible(true);
        villager.setAI(false);
        villager.setInvulnerable(true);
        villager.setSilent(true);
        villager.getPersistentDataContainer().set(npcKey, PersistentDataType.STRING, npc.id());
        npc.setUuid(villager.getUniqueId());
    }

    public void saveNpc(Npc npc) {
        ConfigurationSection section = config.createSection(npc.id());
        section.set("name", npc.name());
        section.set("role", npc.role().name());
        section.set("world", npc.world());
        section.set("x", npc.x());
        section.set("y", npc.y());
        section.set("z", npc.z());
        section.set("yaw", npc.yaw());
        section.set("pitch", npc.pitch());
        section.set("dialog", npc.dialog());
        section.set("questLink", npc.questLink());
        section.set("shopId", npc.shopId());
        section.set("uuid", npc.uuid() != null ? npc.uuid().toString() : null);
        save();
    }

    public void saveAll() {
        config.getKeys(false).forEach(key -> config.set(key, null));
        for (Npc npc : npcs.values()) {
            saveNpc(npc);
        }
        save();
    }

    private void load() {
        npcs.clear();
        for (String id : config.getKeys(false)) {
            ConfigurationSection section = config.getConfigurationSection(id);
            if (section == null) {
                continue;
            }
            Npc npc = new Npc(id);
            npc.setName(section.getString("name", id));
            npc.setRole(NpcRole.valueOf(section.getString("role", "QUESTGIVER")));
            npc.setWorld(section.getString("world", "world"));
            npc.setDialog(section.getStringList("dialog"));
            npc.setQuestLink(section.getString("questLink", null));
            npc.setShopId(section.getString("shopId", null));
            npc.setUuid(section.contains("uuid") ? UUID.fromString(section.getString("uuid")) : null);
            World world = Bukkit.getWorld(npc.world());
            double x = section.getDouble("x");
            double y = section.getDouble("y");
            double z = section.getDouble("z");
            float yaw = (float) section.getDouble("yaw");
            float pitch = (float) section.getDouble("pitch");
            if (world != null) {
                npc.setLocation(new Location(world, x, y, z, yaw, pitch));
            } else {
                npc.setRawLocation(npc.world(), x, y, z, yaw, pitch);
            }
            npcs.put(id, npc);
        }
    }

    private void save() {
        try {
            config.save(file);
        } catch (IOException e) {
            plugin.getLogger().warning("Failed to save npcs.yml: " + e.getMessage());
        }
    }
}

```

---

## `MineLauncher/plugins/RPGPlugin/src/main/java/com/example/rpg/manager/PartyManager.java`

```java
package com.example.rpg.manager;

import com.example.rpg.model.Party;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class PartyManager {
    private final Map<UUID, Party> partiesByMember = new HashMap<>();

    public Party createParty(UUID leader) {
        Party party = new Party(leader);
        partiesByMember.put(leader, party);
        return party;
    }

    public Optional<Party> getParty(UUID member) {
        return Optional.ofNullable(partiesByMember.get(member));
    }

    public void addMember(Party party, UUID member) {
        party.addMember(member);
        partiesByMember.put(member, party);
    }

    public void removeMember(UUID member) {
        Party party = partiesByMember.get(member);
        if (party == null) {
            return;
        }

        party.removeMember(member);
        partiesByMember.remove(member);

        if (party.leader().equals(member)) {
            for (UUID uuid : party.members()) {
                partiesByMember.remove(uuid);
            }
            party.members().clear();
            return;
        }

        if (party.members().isEmpty()) {
            partiesByMember.remove(party.leader());
        }
    }
}

```

---

## `MineLauncher/plugins/RPGPlugin/src/main/java/com/example/rpg/manager/PlayerDataManager.java`

```java
package com.example.rpg.manager;

import com.example.rpg.db.PlayerDao;
import com.example.rpg.model.PlayerProfile;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class PlayerDataManager {
    private final JavaPlugin plugin;
    private final PlayerDao playerDao;
    private final Map<UUID, PlayerProfile> profiles = new HashMap<>();

    public PlayerDataManager(JavaPlugin plugin, PlayerDao playerDao) {
        this.plugin = plugin;
        this.playerDao = playerDao;
    }

    public PlayerProfile getProfile(UUID uuid) {
        return profiles.computeIfAbsent(uuid, PlayerProfile::new);
    }

    public PlayerProfile getProfile(Player player) {
        return getProfile(player.getUniqueId());
    }

    public Map<UUID, PlayerProfile> profiles() {
        return profiles;
    }

    public CompletableFuture<PlayerProfile> loadProfileAsync(UUID uuid) {
        return playerDao.loadPlayer(uuid).exceptionally(error -> {
            plugin.getLogger().warning("Failed to load player " + uuid + ": " + error.getMessage());
            return null;
        }).thenApply(profile -> {
            PlayerProfile resolved = profile != null ? profile : new PlayerProfile(uuid);
            profiles.put(uuid, resolved);
            return resolved;
        });
    }

    public void saveProfile(PlayerProfile profile) {
        playerDao.savePlayer(profile).exceptionally(error -> {
            plugin.getLogger().warning("Failed to save player " + profile.uuid() + ": " + error.getMessage());
            return null;
        });
    }

    public void saveAll() {
        for (PlayerProfile profile : profiles.values()) {
            saveProfile(profile);
        }
    }
}

```

---

## `MineLauncher/plugins/RPGPlugin/src/main/java/com/example/rpg/manager/ProfessionManager.java`

```java
package com.example.rpg.manager;

import com.example.rpg.model.PlayerProfile;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class ProfessionManager {
    private final JavaPlugin plugin;
    private final File file;
    private final YamlConfiguration config;
    private final Map<String, ProfessionDefinition> professions = new HashMap<>();

    public ProfessionManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "professions.yml");
        this.config = YamlConfiguration.loadConfiguration(file);
        if (!file.exists()) {
            seedDefaults();
        }
        load();
    }

    public int getLevel(PlayerProfile profile, String profession) {
        return profile.professions().getOrDefault(profession + "_level", 1);
    }

    public void setLevel(PlayerProfile profile, String profession, int level) {
        profile.professions().put(profession + "_level", Math.max(1, level));
    }

    public Map<String, Integer> professions(PlayerProfile profile) {
        return profile.professions();
    }

    public int addXp(PlayerProfile profile, String profession, int xp, Player player) {
        int currentXp = profile.professions().getOrDefault(profession + "_xp", 0);
        int newXp = currentXp + Math.max(0, xp);
        profile.professions().put(profession + "_xp", newXp);
        int level = profile.professions().getOrDefault(profession + "_level", 1);
        int oldLevel = level;
        int threshold = level * 100;
        while (newXp >= threshold) {
            newXp -= threshold;
            level++;
            threshold = level * 100;
        }
        profile.professions().put(profession + "_level", level);
        profile.professions().put(profession + "_xp", newXp);
        if (player != null && level > oldLevel) {
            fireLevelRewards(profession, level, player);
        }
        return level;
    }

    public int xpForMaterial(String profession, String material) {
        ProfessionDefinition definition = professions.get(profession);
        if (definition == null) {
            return 0;
        }
        return definition.xpSources().getOrDefault(material, 0);
    }

    public int requiredLevelForCraft(String profession, String resultMaterial) {
        ProfessionDefinition definition = professions.get(profession);
        if (definition == null) {
            return 0;
        }
        return definition.craftRequirements().getOrDefault(resultMaterial, 0);
    }

    public Map<String, ProfessionDefinition> definitions() {
        return professions;
    }

    private void fireLevelRewards(String profession, int level, Player player) {
        ProfessionDefinition definition = professions.get(profession);
        if (definition == null) {
            return;
        }
        List<String> commands = definition.levelRewards().get(level);
        if (commands == null) {
            return;
        }
        for (String command : commands) {
            String resolved = command.replace("{player}", player.getName());
            plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(), resolved);
        }
        player.sendMessage(com.example.rpg.util.Text.mm("<gold>Beruf " + definition.displayName()
            + " Level " + level + " erreicht!"));
    }

    private void load() {
        professions.clear();
        ConfigurationSection root = config.getConfigurationSection("professions");
        if (root == null) {
            return;
        }
        for (String id : root.getKeys(false)) {
            ConfigurationSection section = root.getConfigurationSection(id);
            if (section == null) {
                continue;
            }
            ProfessionDefinition definition = new ProfessionDefinition(id);
            definition.setDisplayName(section.getString("display", id));
            ConfigurationSection xpSources = section.getConfigurationSection("xpSources");
            if (xpSources != null) {
                for (String material : xpSources.getKeys(false)) {
                    definition.xpSources().put(material, xpSources.getInt(material, 0));
                }
            }
            ConfigurationSection craftReq = section.getConfigurationSection("craftRequirements");
            if (craftReq != null) {
                for (String material : craftReq.getKeys(false)) {
                    definition.craftRequirements().put(material, craftReq.getInt(material, 0));
                }
            }
            ConfigurationSection rewards = section.getConfigurationSection("levelRewards");
            if (rewards != null) {
                for (String levelKey : rewards.getKeys(false)) {
                    try {
                        int lvl = Integer.parseInt(levelKey);
                        definition.levelRewards().put(lvl, rewards.getStringList(levelKey));
                    } catch (NumberFormatException ignored) {
                    }
                }
            }
            professions.put(id, definition);
        }
    }

    private void seedDefaults() {
        config.set("professions.mining.display", "Bergbau");
        config.set("professions.mining.xpSources.COAL_ORE", 5);
        config.set("professions.mining.xpSources.IRON_ORE", 8);
        config.set("professions.mining.xpSources.DIAMOND_ORE", 15);
        config.set("professions.mining.levelRewards.5", List.of("give {player} iron_pickaxe 1"));

        config.set("professions.herbalism.display", "Kräuterkunde");
        config.set("professions.herbalism.xpSources.WHEAT", 4);
        config.set("professions.herbalism.xpSources.CARROTS", 4);
        config.set("professions.herbalism.xpSources.NETHER_WART", 8);
        config.set("professions.herbalism.levelRewards.5", List.of("give {player} golden_apple 1"));

        config.set("professions.blacksmithing.display", "Schmiedekunst");
        config.set("professions.blacksmithing.xpSources.IRON_SWORD", 10);
        config.set("professions.blacksmithing.xpSources.DIAMOND_SWORD", 20);
        config.set("professions.blacksmithing.craftRequirements.IRON_SWORD", 3);
        config.set("professions.blacksmithing.craftRequirements.DIAMOND_SWORD", 6);
        config.set("professions.blacksmithing.levelRewards.5", List.of("give {player} anvil 1"));
        save();
    }

    private void save() {
        try {
            config.save(file);
        } catch (IOException e) {
            plugin.getLogger().warning("Failed to save professions.yml: " + e.getMessage());
        }
    }

    public static class ProfessionDefinition {
        private final String id;
        private String displayName;
        private final Map<String, Integer> xpSources = new HashMap<>();
        private final Map<String, Integer> craftRequirements = new HashMap<>();
        private final Map<Integer, List<String>> levelRewards = new HashMap<>();

        public ProfessionDefinition(String id) {
            this.id = id;
        }

        public String id() {
            return id;
        }

        public String displayName() {
            return displayName;
        }

        public void setDisplayName(String displayName) {
            this.displayName = displayName;
        }

        public Map<String, Integer> xpSources() {
            return xpSources;
        }

        public Map<String, Integer> craftRequirements() {
            return craftRequirements;
        }

        public Map<Integer, List<String>> levelRewards() {
            return levelRewards;
        }
    }
}

```

---

## `MineLauncher/plugins/RPGPlugin/src/main/java/com/example/rpg/manager/QuestManager.java`

```java
package com.example.rpg.manager;

import com.example.rpg.model.Quest;
import com.example.rpg.model.QuestReward;
import com.example.rpg.model.QuestStep;
import com.example.rpg.model.QuestStepType;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public class QuestManager {
    private final JavaPlugin plugin;
    private final File file;
    private final YamlConfiguration config;
    private final Map<String, Quest> quests = new HashMap<>();

    public QuestManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "quests.yml");
        this.config = YamlConfiguration.loadConfiguration(file);
        if (!file.exists()) {
            seedDefaults();
        }
        load();
    }

    public Quest getQuest(String id) {
        return quests.get(id);
    }

    public Map<String, Quest> quests() {
        return quests;
    }

    public void saveQuest(Quest quest) {
        ConfigurationSection section = config.createSection(quest.id());
        section.set("name", quest.name());
        section.set("description", quest.description());
        section.set("repeatable", quest.repeatable());
        section.set("minLevel", quest.minLevel());
        List<Map<String, Object>> steps = new ArrayList<>();
        for (QuestStep step : quest.steps()) {
            Map<String, Object> map = new HashMap<>();
            map.put("type", step.type().name());
            map.put("target", step.target());
            map.put("amount", step.amount());
            steps.add(map);
        }
        section.set("steps", steps);
        QuestReward reward = quest.reward();
        section.set("reward.xp", reward.xp());
        section.set("reward.skillPoints", reward.skillPoints());
        section.set("reward.factionRep", reward.factionRep());
        save();
    }

    public void saveAll() {
        config.getKeys(false).forEach(key -> config.set(key, null));
        for (Quest quest : quests.values()) {
            saveQuest(quest);
        }
        save();
    }

    private void load() {
        quests.clear();
        for (String id : config.getKeys(false)) {
            ConfigurationSection section = config.getConfigurationSection(id);
            if (section == null) {
                continue;
            }
            Quest quest = new Quest(id);
            quest.setName(section.getString("name", id));
            quest.setDescription(section.getString("description", ""));
            quest.setRepeatable(section.getBoolean("repeatable", false));
            quest.setMinLevel(section.getInt("minLevel", 1));
            List<QuestStep> steps = new ArrayList<>();
            for (Map<?, ?> raw : section.getMapList("steps")) {
                Object typeValue = raw.containsKey("type") ? raw.get("type") : "KILL";
                Object targetValue = raw.containsKey("target") ? raw.get("target") : "ZOMBIE";
                Object amountValue = raw.containsKey("amount") ? raw.get("amount") : 1;
                String typeName = String.valueOf(typeValue);
                String target = String.valueOf(targetValue);
                int amount = Integer.parseInt(String.valueOf(amountValue));
                QuestStepType type = QuestStepType.valueOf(typeName);
                steps.add(new QuestStep(type, target, amount));
            }
            quest.setSteps(steps);
            QuestReward reward = new QuestReward();
            reward.setXp(section.getInt("reward.xp", 50));
            reward.setSkillPoints(section.getInt("reward.skillPoints", 1));
            ConfigurationSection factionRep = section.getConfigurationSection("reward.factionRep");
            if (factionRep != null) {
                Map<String, Integer> rep = new HashMap<>();
                for (String faction : factionRep.getKeys(false)) {
                    rep.put(faction, factionRep.getInt(faction));
                }
                reward.setFactionRep(rep);
            }
            quest.setReward(reward);
            quests.put(id, quest);
        }
    }

    private void seedDefaults() {
        Quest quest = new Quest("starter_hunt");
        quest.setName("Wolfsplage");
        quest.setDescription("Jage 3 Wölfe und kehre zurück.");
        quest.setRepeatable(false);
        quest.setMinLevel(1);
        quest.setSteps(List.of(new QuestStep(QuestStepType.KILL, "WOLF", 3)));
        QuestReward reward = new QuestReward();
        reward.setXp(120);
        reward.setSkillPoints(1);
        quest.setReward(reward);
        quests.put(quest.id(), quest);
        saveAll();
    }

    private void save() {
        try {
            config.save(file);
        } catch (IOException e) {
            plugin.getLogger().warning("Failed to save quests.yml: " + e.getMessage());
        }
    }
}

```

---

## `MineLauncher/plugins/RPGPlugin/src/main/java/com/example/rpg/manager/ShopManager.java`

```java
package com.example.rpg.manager;

import com.example.rpg.model.ShopDefinition;
import com.example.rpg.model.ShopItem;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public class ShopManager {
    private final JavaPlugin plugin;
    private final File file;
    private final YamlConfiguration config;
    private final Map<String, ShopDefinition> shops = new HashMap<>();

    public ShopManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "shops.yml");
        this.config = YamlConfiguration.loadConfiguration(file);
        if (!file.exists()) {
            seedDefaults();
        }
        load();
    }

    public ShopDefinition getShop(String id) {
        return shops.get(id);
    }

    public Map<String, ShopDefinition> shops() {
        return shops;
    }

    public void saveShop(ShopDefinition shop) {
        ConfigurationSection section = config.createSection(shop.id());
        section.set("title", shop.title());
        List<Map<String, Object>> items = new java.util.ArrayList<>();
        for (ShopItem item : shop.items().values()) {
            Map<String, Object> map = new HashMap<>();
            map.put("slot", item.slot());
            map.put("material", item.material());
            map.put("name", item.name());
            map.put("buyPrice", item.buyPrice());
            map.put("sellPrice", item.sellPrice());
            items.add(map);
        }
        section.set("items", items);
        save();
    }

    public void saveAll() {
        config.getKeys(false).forEach(key -> config.set(key, null));
        for (ShopDefinition shop : shops.values()) {
            saveShop(shop);
        }
        save();
    }

    private void load() {
        shops.clear();
        for (String id : config.getKeys(false)) {
            ConfigurationSection section = config.getConfigurationSection(id);
            if (section == null) {
                continue;
            }
            ShopDefinition shop = new ShopDefinition(id);
            shop.setTitle(section.getString("title", id));
            Map<Integer, ShopItem> items = new HashMap<>();
            for (Map<?, ?> raw : section.getMapList("items")) {
                ShopItem item = new ShopItem();
                Object slotValue = raw.containsKey("slot") ? raw.get("slot") : 0;
                Object materialValue = raw.containsKey("material") ? raw.get("material") : "STONE";
                Object nameValue = raw.containsKey("name") ? raw.get("name") : "";
                Object buyValue = raw.containsKey("buyPrice") ? raw.get("buyPrice") : 0;
                Object sellValue = raw.containsKey("sellPrice") ? raw.get("sellPrice") : 0;
                item.setSlot(Integer.parseInt(String.valueOf(slotValue)));
                item.setMaterial(String.valueOf(materialValue));
                item.setName(String.valueOf(nameValue));
                item.setBuyPrice(Integer.parseInt(String.valueOf(buyValue)));
                item.setSellPrice(Integer.parseInt(String.valueOf(sellValue)));
                items.put(item.slot(), item);
            }
            shop.setItems(items);
            shops.put(id, shop);
        }
    }

    private void seedDefaults() {
        ShopDefinition shop = new ShopDefinition("blacksmith");
        shop.setTitle("Dorfschmied");
        ShopItem sword = new ShopItem();
        sword.setSlot(0);
        sword.setMaterial("IRON_SWORD");
        sword.setName("&7Eisenschwert");
        sword.setBuyPrice(100);
        sword.setSellPrice(20);
        ShopItem potion = new ShopItem();
        potion.setSlot(1);
        potion.setMaterial("POTION");
        potion.setName("&aHeiltrank");
        potion.setBuyPrice(50);
        potion.setSellPrice(10);
        Map<Integer, ShopItem> items = new HashMap<>();
        items.put(sword.slot(), sword);
        items.put(potion.slot(), potion);
        shop.setItems(items);
        shops.put(shop.id(), shop);
        saveAll();
    }

    private void save() {
        try {
            config.save(file);
        } catch (IOException e) {
            plugin.getLogger().warning("Failed to save shops.yml: " + e.getMessage());
        }
    }
}

```

---

## `MineLauncher/plugins/RPGPlugin/src/main/java/com/example/rpg/manager/SkillHotbarManager.java`

```java
package com.example.rpg.manager;

import com.example.rpg.model.PlayerProfile;

public class SkillHotbarManager {
    private final PlayerDataManager playerDataManager;

    public SkillHotbarManager(PlayerDataManager playerDataManager) {
        this.playerDataManager = playerDataManager;
    }

    public void bindSkill(PlayerProfile profile, int slot, String skillId) {
        profile.skillBindings().put(slot, skillId);
        playerDataManager.saveProfile(profile);
    }

    public String getBinding(PlayerProfile profile, int slot) {
        return profile.skillBindings().get(slot);
    }
}

```

---

## `MineLauncher/plugins/RPGPlugin/src/main/java/com/example/rpg/manager/SkillManager.java`

```java
package com.example.rpg.manager;

import com.example.rpg.model.Skill;
import com.example.rpg.model.SkillCategory;
import com.example.rpg.model.SkillType;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public class SkillManager {
    private final JavaPlugin plugin;
    private final File file;
    private final YamlConfiguration config;
    private final Map<String, Skill> skills = new HashMap<>();

    public SkillManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "skills.yml");
        this.config = YamlConfiguration.loadConfiguration(file);
        if (!file.exists()) {
            seedDefaults();
        }
        load();
    }

    public Skill getSkill(String id) {
        return skills.get(id);
    }

    public Map<String, Skill> skills() {
        return skills;
    }

    public void saveSkill(Skill skill) {
        ConfigurationSection section = config.createSection(skill.id());
        section.set("name", skill.name());
        section.set("type", skill.type().name());
        section.set("category", skill.category().name());
        section.set("cooldown", skill.cooldown());
        section.set("manaCost", skill.manaCost());
        section.set("effects", serializeEffects(skill.effects()));
        section.set("parent", skill.requiredSkill());
        save();
    }

    public void saveAll() {
        config.getKeys(false).forEach(key -> config.set(key, null));
        for (Skill skill : skills.values()) {
            saveSkill(skill);
        }
        save();
    }

    private void load() {
        skills.clear();
        for (String id : config.getKeys(false)) {
            ConfigurationSection section = config.getConfigurationSection(id);
            if (section == null) {
                continue;
            }
            Skill skill = new Skill(id);
            skill.setName(section.getString("name", id));
            skill.setType(SkillType.valueOf(section.getString("type", "ACTIVE")));
            skill.setCategory(SkillCategory.valueOf(section.getString("category", "ATTACK")));
            skill.setCooldown(section.getInt("cooldown", 10));
            skill.setManaCost(section.getInt("manaCost", 20));
            String parent = section.getString("parent", null);
            if (parent == null) {
                parent = section.getString("requiredSkill", null);
            }
            skill.setRequiredSkill(parent);
            skill.setEffects(loadEffects(section));
            skills.put(id, skill);
        }
    }

    private void seedDefaults() {
        Skill healPulse = new Skill("heal_pulse");
        healPulse.setName("Heilpuls");
        healPulse.setType(SkillType.ACTIVE);
        healPulse.setCategory(SkillCategory.HEALING);
        healPulse.setCooldown(20);
        healPulse.setManaCost(20);
        healPulse.setEffects(List.of(effectConfig("HEAL", Map.of("amount", 4))));

        Skill greaterHeal = new Skill("greater_heal");
        greaterHeal.setName("Große Heilung");
        greaterHeal.setType(SkillType.ACTIVE);
        greaterHeal.setCategory(SkillCategory.HEALING);
        greaterHeal.setCooldown(30);
        greaterHeal.setManaCost(35);
        greaterHeal.setEffects(List.of(effectConfig("HEAL", Map.of("amount", 8))));
        greaterHeal.setRequiredSkill("heal_pulse");

        Skill divineBlessing = new Skill("divine_blessing");
        divineBlessing.setName("Segen");
        divineBlessing.setType(SkillType.ACTIVE);
        divineBlessing.setCategory(SkillCategory.HEALING);
        divineBlessing.setCooldown(45);
        divineBlessing.setManaCost(45);
        divineBlessing.setEffects(List.of(effectConfig("HEAL", Map.of("amount", 12)),
            effectConfig("SOUND", Map.of("sound", "BLOCK_BEACON_POWER_SELECT", "volume", 1.0, "pitch", 1.0))));
        divineBlessing.setRequiredSkill("greater_heal");

        Skill emberShot = new Skill("ember_shot");
        emberShot.setName("Flammenstoß");
        emberShot.setType(SkillType.ACTIVE);
        emberShot.setCategory(SkillCategory.MAGIC);
        emberShot.setCooldown(12);
        emberShot.setManaCost(18);
        emberShot.setEffects(List.of(effectConfig("PROJECTILE", Map.of("type", "SMALL_FIREBALL")),
            effectConfig("SOUND", Map.of("sound", "ENTITY_BLAZE_SHOOT", "volume", 1.0, "pitch", 1.2))));

        Skill frostBolt = new Skill("frost_bolt");
        frostBolt.setName("Frostbolzen");
        frostBolt.setType(SkillType.ACTIVE);
        frostBolt.setCategory(SkillCategory.MAGIC);
        frostBolt.setCooldown(18);
        frostBolt.setManaCost(25);
        frostBolt.setEffects(List.of(effectConfig("PROJECTILE", Map.of("type", "SNOWBALL")),
            effectConfig("POTION", Map.of("type", "SLOW", "duration", 60, "amplifier", 1, "radius", 6)),
            effectConfig("SOUND", Map.of("sound", "BLOCK_GLASS_BREAK", "volume", 0.8, "pitch", 1.4))));
        frostBolt.setRequiredSkill("ember_shot");

        Skill arcaneBurst = new Skill("arcane_burst");
        arcaneBurst.setName("Arkane Explosion");
        arcaneBurst.setType(SkillType.ACTIVE);
        arcaneBurst.setCategory(SkillCategory.MAGIC);
        arcaneBurst.setCooldown(30);
        arcaneBurst.setManaCost(35);
        arcaneBurst.setEffects(List.of(effectConfig("DAMAGE", Map.of("amount", 6, "radius", 5, "maxTargets", 10)),
            effectConfig("SOUND", Map.of("sound", "ENTITY_ILLUSIONER_CAST_SPELL", "volume", 1.0, "pitch", 1.2))));
        arcaneBurst.setRequiredSkill("frost_bolt");

        Skill powerStrike = new Skill("power_strike");
        powerStrike.setName("Machtstoß");
        powerStrike.setType(SkillType.ACTIVE);
        powerStrike.setCategory(SkillCategory.ATTACK);
        powerStrike.setCooldown(8);
        powerStrike.setManaCost(10);
        powerStrike.setEffects(List.of(effectConfig("DAMAGE", Map.of("amount", 8, "radius", 3, "maxTargets", 1)),
            effectConfig("SOUND", Map.of("sound", "ENTITY_PLAYER_ATTACK_STRONG", "volume", 1.0, "pitch", 1.0))));

        Skill whirlwind = new Skill("whirlwind");
        whirlwind.setName("Wirbelwind");
        whirlwind.setType(SkillType.ACTIVE);
        whirlwind.setCategory(SkillCategory.ATTACK);
        whirlwind.setCooldown(20);
        whirlwind.setManaCost(20);
        whirlwind.setEffects(List.of(effectConfig("DAMAGE", Map.of("amount", 5, "radius", 4, "maxTargets", 10)),
            effectConfig("SOUND", Map.of("sound", "ENTITY_PLAYER_ATTACK_SWEEP", "volume", 1.0, "pitch", 0.8))));
        whirlwind.setRequiredSkill("power_strike");

        Skill execute = new Skill("execute");
        execute.setName("Hinrichtung");
        execute.setType(SkillType.ACTIVE);
        execute.setCategory(SkillCategory.ATTACK);
        execute.setCooldown(35);
        execute.setManaCost(30);
        execute.setEffects(List.of(effectConfig("DAMAGE", Map.of("amount", 12, "radius", 3, "maxTargets", 1)),
            effectConfig("SOUND", Map.of("sound", "ENTITY_WITHER_SKELETON_HURT", "volume", 1.0, "pitch", 0.9))));
        execute.setRequiredSkill("whirlwind");

        Skill shieldWall = new Skill("shield_wall");
        shieldWall.setName("Schildwall");
        shieldWall.setType(SkillType.ACTIVE);
        shieldWall.setCategory(SkillCategory.DEFENSE);
        shieldWall.setCooldown(25);
        shieldWall.setManaCost(15);
        shieldWall.setEffects(List.of(effectConfig("POTION", Map.of("type", "DAMAGE_RESISTANCE", "duration", 120, "amplifier", 0)),
            effectConfig("SOUND", Map.of("sound", "ITEM_SHIELD_BLOCK", "volume", 1.0, "pitch", 1.0))));

        Skill fortify = new Skill("fortify");
        fortify.setName("Bollwerk");
        fortify.setType(SkillType.ACTIVE);
        fortify.setCategory(SkillCategory.DEFENSE);
        fortify.setCooldown(35);
        fortify.setManaCost(25);
        fortify.setEffects(List.of(effectConfig("POTION", Map.of("type", "DAMAGE_RESISTANCE", "duration", 200, "amplifier", 1)),
            effectConfig("POTION", Map.of("type", "ABSORPTION", "duration", 200, "amplifier", 1)),
            effectConfig("SOUND", Map.of("sound", "BLOCK_ANVIL_USE", "volume", 0.7, "pitch", 1.0))));
        fortify.setRequiredSkill("shield_wall");

        Skill deflect = new Skill("deflect");
        deflect.setName("Abwehrhaltung");
        deflect.setType(SkillType.ACTIVE);
        deflect.setCategory(SkillCategory.DEFENSE);
        deflect.setCooldown(45);
        deflect.setManaCost(30);
        deflect.setEffects(List.of(effectConfig("POTION", Map.of("type", "FIRE_RESISTANCE", "duration", 200, "amplifier", 0)),
            effectConfig("POTION", Map.of("type", "DAMAGE_RESISTANCE", "duration", 200, "amplifier", 0)),
            effectConfig("SOUND", Map.of("sound", "ITEM_SHIELD_BLOCK", "volume", 1.0, "pitch", 1.2))));
        deflect.setRequiredSkill("fortify");

        Skill miningFocus = new Skill("mining_focus");
        miningFocus.setName("Bergbau-Fokus");
        miningFocus.setType(SkillType.ACTIVE);
        miningFocus.setCategory(SkillCategory.PROFESSION);
        miningFocus.setCooldown(60);
        miningFocus.setManaCost(15);
        miningFocus.setEffects(List.of(effectConfig("POTION", Map.of("type", "FAST_DIGGING", "duration", 300, "amplifier", 1)),
            effectConfig("XP", Map.of("amount", 10)),
            effectConfig("SOUND", Map.of("sound", "BLOCK_STONE_HIT", "volume", 0.8, "pitch", 1.0))));

        Skill craftingInsight = new Skill("crafting_insight");
        craftingInsight.setName("Handwerkskunst");
        craftingInsight.setType(SkillType.ACTIVE);
        craftingInsight.setCategory(SkillCategory.PROFESSION);
        craftingInsight.setCooldown(60);
        craftingInsight.setManaCost(20);
        craftingInsight.setEffects(List.of(effectConfig("POTION", Map.of("type", "LUCK", "duration", 300, "amplifier", 1)),
            effectConfig("XP", Map.of("amount", 10)),
            effectConfig("SOUND", Map.of("sound", "BLOCK_ANVIL_PLACE", "volume", 0.8, "pitch", 1.1))));
        craftingInsight.setRequiredSkill("mining_focus");

        Skill alchemyMastery = new Skill("alchemy_mastery");
        alchemyMastery.setName("Alchemie-Meister");
        alchemyMastery.setType(SkillType.ACTIVE);
        alchemyMastery.setCategory(SkillCategory.PROFESSION);
        alchemyMastery.setCooldown(90);
        alchemyMastery.setManaCost(30);
        alchemyMastery.setEffects(List.of(effectConfig("POTION", Map.of("type", "REGENERATION", "duration", 120, "amplifier", 0)),
            effectConfig("XP", Map.of("amount", 15)),
            effectConfig("SOUND", Map.of("sound", "BLOCK_BREWING_STAND_BREW", "volume", 0.8, "pitch", 1.0))));
        alchemyMastery.setRequiredSkill("crafting_insight");

        skills.put(healPulse.id(), healPulse);
        skills.put(greaterHeal.id(), greaterHeal);
        skills.put(divineBlessing.id(), divineBlessing);
        skills.put(emberShot.id(), emberShot);
        skills.put(frostBolt.id(), frostBolt);
        skills.put(arcaneBurst.id(), arcaneBurst);
        skills.put(powerStrike.id(), powerStrike);
        skills.put(whirlwind.id(), whirlwind);
        skills.put(execute.id(), execute);
        skills.put(shieldWall.id(), shieldWall);
        skills.put(fortify.id(), fortify);
        skills.put(deflect.id(), deflect);
        skills.put(miningFocus.id(), miningFocus);
        skills.put(craftingInsight.id(), craftingInsight);
        skills.put(alchemyMastery.id(), alchemyMastery);
        saveAll();
    }

    private List<Map<String, Object>> serializeEffects(List<com.example.rpg.skill.SkillEffectConfig> effects) {
        List<Map<String, Object>> list = new ArrayList<>();
        for (com.example.rpg.skill.SkillEffectConfig config : effects) {
            Map<String, Object> map = new HashMap<>();
            map.put("type", config.type().name());
            map.put("params", config.params());
            list.add(map);
        }
        return list;
    }

    private List<com.example.rpg.skill.SkillEffectConfig> loadEffects(ConfigurationSection section) {
        List<com.example.rpg.skill.SkillEffectConfig> effects = new ArrayList<>();
        for (Map<?, ?> raw : section.getMapList("effects")) {
            Object typeValue = raw.containsKey("type") ? raw.get("type") : "HEAL";
            com.example.rpg.skill.SkillEffectType type = com.example.rpg.skill.SkillEffectType.valueOf(String.valueOf(typeValue));
            Map<String, Object> params = new HashMap<>();
            Object paramsValue = raw.get("params");
            if (paramsValue instanceof Map<?, ?> paramMap) {
                for (Map.Entry<?, ?> entry : paramMap.entrySet()) {
                    params.put(String.valueOf(entry.getKey()), entry.getValue());
                }
            }
            effects.add(new com.example.rpg.skill.SkillEffectConfig(type, params));
        }
        if (effects.isEmpty() && section.contains("effect")) {
            String legacy = section.getString("effect", "");
            effects.add(mapLegacyEffect(legacy));
        }
        return effects;
    }

    private com.example.rpg.skill.SkillEffectConfig mapLegacyEffect(String legacy) {
        if (legacy == null) {
            return new com.example.rpg.skill.SkillEffectConfig(com.example.rpg.skill.SkillEffectType.HEAL, Map.of("amount", 4));
        }
        return switch (legacy) {
            case "heal_small" -> effectConfig("HEAL", Map.of("amount", 4));
            case "heal_medium" -> effectConfig("HEAL", Map.of("amount", 8));
            case "heal_large" -> effectConfig("HEAL", Map.of("amount", 12));
            case "fireball" -> effectConfig("PROJECTILE", Map.of("type", "SMALL_FIREBALL"));
            case "frostbolt" -> effectConfig("PROJECTILE", Map.of("type", "SNOWBALL"));
            case "arcane_blast" -> effectConfig("DAMAGE", Map.of("amount", 6, "radius", 5, "maxTargets", 10));
            case "power_strike" -> effectConfig("DAMAGE", Map.of("amount", 8, "radius", 3, "maxTargets", 1));
            case "whirlwind" -> effectConfig("DAMAGE", Map.of("amount", 5, "radius", 4, "maxTargets", 10));
            case "execute" -> effectConfig("DAMAGE", Map.of("amount", 12, "radius", 3, "maxTargets", 1));
            case "dash" -> effectConfig("VELOCITY", Map.of("forward", 1.2, "up", 0.3, "add", false));
            case "taunt" -> effectConfig("AGGRO", Map.of("radius", 8));
            default -> effectConfig("HEAL", Map.of("amount", 4));
        };
    }

    private com.example.rpg.skill.SkillEffectConfig effectConfig(String type, Map<String, Object> params) {
        return new com.example.rpg.skill.SkillEffectConfig(
            com.example.rpg.skill.SkillEffectType.valueOf(type),
            params
        );
    }

    private void save() {
        try {
            config.save(file);
        } catch (IOException e) {
            plugin.getLogger().warning("Failed to save skills.yml: " + e.getMessage());
        }
    }
}

```

---

## `MineLauncher/plugins/RPGPlugin/src/main/java/com/example/rpg/manager/SkillTreeManager.java`

```java
package com.example.rpg.manager;

import com.example.rpg.model.Skill;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SkillTreeManager {
    private final SkillManager skillManager;
    private final Map<String, SkillNode> nodes = new HashMap<>();

    public SkillTreeManager(SkillManager skillManager) {
        this.skillManager = skillManager;
        rebuild();
    }

    public void rebuild() {
        nodes.clear();
        for (Skill skill : skillManager.skills().values()) {
            nodes.put(skill.id(), new SkillNode(skill));
        }
        for (SkillNode node : nodes.values()) {
            String parentId = node.skill().requiredSkill();
            if (parentId != null) {
                SkillNode parent = nodes.get(parentId);
                if (parent != null) {
                    parent.children().add(node);
                    node.setParent(parent);
                }
            }
        }
    }

    public List<SkillNode> roots() {
        List<SkillNode> roots = new ArrayList<>();
        for (SkillNode node : nodes.values()) {
            if (node.parent() == null) {
                roots.add(node);
            }
        }
        roots.sort(Comparator.comparing(n -> n.skill().id()));
        return roots;
    }

    public Map<String, SkillNode> nodes() {
        return nodes;
    }

    public static class SkillNode {
        private final Skill skill;
        private SkillNode parent;
        private final List<SkillNode> children = new ArrayList<>();

        public SkillNode(Skill skill) {
            this.skill = skill;
        }

        public Skill skill() {
            return skill;
        }

        public SkillNode parent() {
            return parent;
        }

        public void setParent(SkillNode parent) {
            this.parent = parent;
        }

        public List<SkillNode> children() {
            return children;
        }
    }
}

```

---

## `MineLauncher/plugins/RPGPlugin/src/main/java/com/example/rpg/manager/SpawnerManager.java`

```java
package com.example.rpg.manager;

import com.example.rpg.RPGPlugin;
import com.example.rpg.model.MobDefinition;
import com.example.rpg.model.Spawner;
import com.example.rpg.model.Zone;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.persistence.PersistentDataType;

public class SpawnerManager {
    private final RPGPlugin plugin;
    private final File file;
    private final YamlConfiguration config;
    private final Map<String, Spawner> spawners = new HashMap<>();
    private final Random random = new Random();

    public SpawnerManager(RPGPlugin plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "spawners.yml");
        this.config = YamlConfiguration.loadConfiguration(file);
        if (!file.exists()) {
            seedDefaults();
        }
        load();
        startTask();
    }

    public Spawner getSpawner(String id) {
        return spawners.get(id);
    }

    public Map<String, Spawner> spawners() {
        return spawners;
    }

    public void saveSpawner(Spawner spawner) {
        ConfigurationSection section = config.createSection(spawner.id());
        section.set("zoneId", spawner.zoneId());
        section.set("maxMobs", spawner.maxMobs());
        section.set("spawnInterval", spawner.spawnInterval());
        section.set("mobs", spawner.mobs());
        save();
    }

    public void saveAll() {
        config.getKeys(false).forEach(key -> config.set(key, null));
        for (Spawner spawner : spawners.values()) {
            saveSpawner(spawner);
        }
        save();
    }

    private void load() {
        spawners.clear();
        for (String id : config.getKeys(false)) {
            ConfigurationSection section = config.getConfigurationSection(id);
            if (section == null) {
                continue;
            }
            Spawner spawner = new Spawner(id);
            spawner.setZoneId(section.getString("zoneId", null));
            spawner.setMaxMobs(section.getInt("maxMobs", 6));
            spawner.setSpawnInterval(section.getInt("spawnInterval", 200));
            ConfigurationSection mobsSection = section.getConfigurationSection("mobs");
            if (mobsSection != null) {
                Map<String, Double> mobs = new HashMap<>();
                for (String mobId : mobsSection.getKeys(false)) {
                    mobs.put(mobId, mobsSection.getDouble(mobId, 1.0));
                }
                spawner.setMobs(mobs);
            }
            spawners.put(id, spawner);
        }
    }

    private void seedDefaults() {
        Spawner spawner = new Spawner("forest_spawner");
        spawner.setZoneId("startzone");
        spawner.setMaxMobs(6);
        spawner.setSpawnInterval(200);
        spawner.setMobs(Map.of("boss_zombie", 1.0));
        spawners.put(spawner.id(), spawner);
        saveAll();
    }

    private void startTask() {
        plugin.getServer().getScheduler().runTaskTimer(plugin, () -> {
            long now = plugin.getServer().getCurrentTick();
            for (Spawner spawner : spawners.values()) {
                if (spawner.spawnInterval() <= 0) {
                    continue;
                }
                if (now % spawner.spawnInterval() != 0) {
                    continue;
                }
                if (spawner.zoneId() == null) {
                    continue;
                }
                Zone zone = plugin.zoneManager().getZone(spawner.zoneId());
                if (zone == null) {
                    continue;
                }
                if (!hasPlayersInZone(zone)) {
                    continue;
                }
                int current = countMobsInZone(zone);
                if (current >= spawner.maxMobs()) {
                    continue;
                }
                MobDefinition mob = pickMob(spawner);
                if (mob == null) {
                    continue;
                }
                spawnMobInZone(zone, mob);
            }
        }, 40L, 40L);
    }

    private boolean hasPlayersInZone(Zone zone) {
        World world = plugin.getServer().getWorld(zone.world());
        if (world == null) {
            return false;
        }
        return world.getPlayers().stream().anyMatch(player -> zone.contains(player.getLocation()));
    }

    private int countMobsInZone(Zone zone) {
        World world = plugin.getServer().getWorld(zone.world());
        if (world == null) {
            return 0;
        }
        return (int) world.getLivingEntities().stream()
            .filter(entity -> entity.getPersistentDataContainer()
                .has(plugin.customMobListener().mobKey(), PersistentDataType.STRING))
            .filter(entity -> zone.contains(entity.getLocation()))
            .count();
    }

    private MobDefinition pickMob(Spawner spawner) {
        if (spawner.mobs().isEmpty()) {
            return null;
        }
        double total = spawner.mobs().values().stream().mapToDouble(Double::doubleValue).sum();
        double roll = random.nextDouble() * total;
        double current = 0;
        for (Map.Entry<String, Double> entry : spawner.mobs().entrySet()) {
            current += entry.getValue();
            if (roll <= current) {
                return plugin.mobManager().getMob(entry.getKey());
            }
        }
        String fallback = spawner.mobs().keySet().iterator().next();
        return plugin.mobManager().getMob(fallback);
    }

    private void spawnMobInZone(Zone zone, MobDefinition mob) {
        World world = plugin.getServer().getWorld(zone.world());
        if (world == null) {
            return;
        }
        int x = randomBetween(zone.x1(), zone.x2());
        int z = randomBetween(zone.z1(), zone.z2());
        int y = world.getHighestBlockYAt(x, z);
        Location location = new Location(world, x + 0.5, y + 1, z + 0.5);
        var type = org.bukkit.entity.EntityType.valueOf(mob.type().toUpperCase());
        var entity = world.spawnEntity(location, type);
        if (entity instanceof org.bukkit.entity.LivingEntity living) {
            plugin.customMobListener().applyDefinition(living, mob);
        } else {
            entity.remove();
        }
    }

    private int randomBetween(int min, int max) {
        int low = Math.min(min, max);
        int high = Math.max(min, max);
        return low + random.nextInt(Math.max(1, high - low + 1));
    }

    private void save() {
        try {
            config.save(file);
        } catch (IOException e) {
            plugin.getLogger().warning("Failed to save spawners.yml: " + e.getMessage());
        }
    }
}

```

---

## `MineLauncher/plugins/RPGPlugin/src/main/java/com/example/rpg/manager/TradeManager.java`

```java
package com.example.rpg.manager;

import com.example.rpg.model.TradeRequest;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class TradeManager {
    private final Map<UUID, TradeRequest> requests = new HashMap<>();

    public void requestTrade(UUID requester, UUID target) {
        TradeRequest request = new TradeRequest(requester, target);
        requests.put(requester, request);
        requests.put(target, request);
    }

    public TradeRequest getRequest(UUID player) {
        return requests.get(player);
    }

    public void clear(UUID player) {
        TradeRequest request = requests.remove(player);
        if (request != null) {
            requests.remove(request.requester());
            requests.remove(request.target());
        }
    }
}

```

---

## `MineLauncher/plugins/RPGPlugin/src/main/java/com/example/rpg/manager/ZoneManager.java`

```java
package com.example.rpg.manager;

import com.example.rpg.model.Zone;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public class ZoneManager {
    private final JavaPlugin plugin;
    private final File file;
    private final YamlConfiguration config;
    private final Map<String, Zone> zones = new HashMap<>();

    public ZoneManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "zones.yml");
        this.config = YamlConfiguration.loadConfiguration(file);
        load();
    }

    public Map<String, Zone> zones() {
        return zones;
    }

    public Zone getZone(String id) {
        return zones.get(id);
    }

    public Zone getZoneAt(Location location) {
        for (Zone zone : zones.values()) {
            if (zone.contains(location)) {
                return zone;
            }
        }
        return null;
    }

    public void saveZone(Zone zone) {
        ConfigurationSection section = config.createSection(zone.id());
        section.set("name", zone.name());
        section.set("world", zone.world());
        section.set("minLevel", zone.minLevel());
        section.set("maxLevel", zone.maxLevel());
        section.set("slowMultiplier", zone.slowMultiplier());
        section.set("damageMultiplier", zone.damageMultiplier());
        section.set("x1", zone.x1());
        section.set("y1", zone.y1());
        section.set("z1", zone.z1());
        section.set("x2", zone.x2());
        section.set("y2", zone.y2());
        section.set("z2", zone.z2());
        save();
    }

    public void saveAll() {
        config.getKeys(false).forEach(key -> config.set(key, null));
        for (Zone zone : zones.values()) {
            saveZone(zone);
        }
        save();
    }

    private void load() {
        zones.clear();
        for (String id : config.getKeys(false)) {
            ConfigurationSection section = config.getConfigurationSection(id);
            if (section == null) {
                continue;
            }
            Zone zone = new Zone(id);
            zone.setName(section.getString("name", id));
            zone.setWorld(section.getString("world", "world"));
            zone.setMinLevel(section.getInt("minLevel", 1));
            zone.setMaxLevel(section.getInt("maxLevel", 60));
            zone.setSlowMultiplier(section.getDouble("slowMultiplier", 1.0));
            zone.setDamageMultiplier(section.getDouble("damageMultiplier", 1.0));
            zone.setCoordinates(
                section.getInt("x1"), section.getInt("y1"), section.getInt("z1"),
                section.getInt("x2"), section.getInt("y2"), section.getInt("z2")
            );
            zones.put(id, zone);
        }
    }

    private void save() {
        try {
            config.save(file);
        } catch (IOException e) {
            plugin.getLogger().warning("Failed to save zones.yml: " + e.getMessage());
        }
    }
}

```

---

## `MineLauncher/plugins/RPGPlugin/src/main/java/com/example/rpg/model/Arena.java`

```java
package com.example.rpg.model;

import java.util.UUID;

public class Arena {
    private final String id;
    private String world;
    private int x1;
    private int y1;
    private int z1;
    private int x2;
    private int y2;
    private int z2;
    private int spawn1x;
    private int spawn1y;
    private int spawn1z;
    private int spawn2x;
    private int spawn2y;
    private int spawn2z;
    private ArenaStatus status = ArenaStatus.WAITING;
    private UUID playerOne;
    private UUID playerTwo;

    public Arena(String id) {
        this.id = id;
    }

    public String id() {
        return id;
    }

    public String world() {
        return world;
    }

    public void setWorld(String world) {
        this.world = world;
    }

    public int x1() {
        return x1;
    }

    public void setX1(int x1) {
        this.x1 = x1;
    }

    public int y1() {
        return y1;
    }

    public void setY1(int y1) {
        this.y1 = y1;
    }

    public int z1() {
        return z1;
    }

    public void setZ1(int z1) {
        this.z1 = z1;
    }

    public int x2() {
        return x2;
    }

    public void setX2(int x2) {
        this.x2 = x2;
    }

    public int y2() {
        return y2;
    }

    public void setY2(int y2) {
        this.y2 = y2;
    }

    public int z2() {
        return z2;
    }

    public void setZ2(int z2) {
        this.z2 = z2;
    }

    public int spawn1x() {
        return spawn1x;
    }

    public void setSpawn1x(int spawn1x) {
        this.spawn1x = spawn1x;
    }

    public int spawn1y() {
        return spawn1y;
    }

    public void setSpawn1y(int spawn1y) {
        this.spawn1y = spawn1y;
    }

    public int spawn1z() {
        return spawn1z;
    }

    public void setSpawn1z(int spawn1z) {
        this.spawn1z = spawn1z;
    }

    public int spawn2x() {
        return spawn2x;
    }

    public void setSpawn2x(int spawn2x) {
        this.spawn2x = spawn2x;
    }

    public int spawn2y() {
        return spawn2y;
    }

    public void setSpawn2y(int spawn2y) {
        this.spawn2y = spawn2y;
    }

    public int spawn2z() {
        return spawn2z;
    }

    public void setSpawn2z(int spawn2z) {
        this.spawn2z = spawn2z;
    }

    public ArenaStatus status() {
        return status;
    }

    public void setStatus(ArenaStatus status) {
        this.status = status;
    }

    public UUID playerOne() {
        return playerOne;
    }

    public void setPlayerOne(UUID playerOne) {
        this.playerOne = playerOne;
    }

    public UUID playerTwo() {
        return playerTwo;
    }

    public void setPlayerTwo(UUID playerTwo) {
        this.playerTwo = playerTwo;
    }
}

```

---

## `MineLauncher/plugins/RPGPlugin/src/main/java/com/example/rpg/model/ArenaStatus.java`

```java
package com.example.rpg.model;

public enum ArenaStatus {
    WAITING,
    FIGHTING,
    ENDING
}

```

---

## `MineLauncher/plugins/RPGPlugin/src/main/java/com/example/rpg/model/AuctionListing.java`

```java
package com.example.rpg.model;

import java.util.UUID;

public class AuctionListing {
    private final String id;
    private UUID seller;
    private String itemData;
    private int price;

    public AuctionListing(String id) {
        this.id = id;
    }

    public String id() {
        return id;
    }

    public UUID seller() {
        return seller;
    }

    public void setSeller(UUID seller) {
        this.seller = seller;
    }

    public String itemData() {
        return itemData;
    }

    public void setItemData(String itemData) {
        this.itemData = itemData;
    }

    public int price() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }
}

```

---

## `MineLauncher/plugins/RPGPlugin/src/main/java/com/example/rpg/model/ClassDefinition.java`

```java
package com.example.rpg.model;

import java.util.ArrayList;
import java.util.List;

public class ClassDefinition {
    private final String id;
    private String name;
    private List<String> startSkills = new ArrayList<>();

    public ClassDefinition(String id) {
        this.id = id;
    }

    public String id() {
        return id;
    }

    public String name() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<String> startSkills() {
        return startSkills;
    }

    public void setStartSkills(List<String> startSkills) {
        this.startSkills = startSkills;
    }
}

```

---

## `MineLauncher/plugins/RPGPlugin/src/main/java/com/example/rpg/model/Faction.java`

```java
package com.example.rpg.model;

public class Faction {
    private final String id;
    private String name;

    public Faction(String id) {
        this.id = id;
    }

    public String id() {
        return id;
    }

    public String name() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}

```

---

## `MineLauncher/plugins/RPGPlugin/src/main/java/com/example/rpg/model/Guild.java`

```java
package com.example.rpg.model;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Guild {
    private final String id;
    private String name;
    private UUID leader;
    private int bankGold;
    private final Map<UUID, GuildMemberRole> members = new HashMap<>();
    private final Map<String, GuildQuest> quests = new HashMap<>();

    public Guild(String id) {
        this.id = id;
    }

    public String id() {
        return id;
    }

    public String name() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public UUID leader() {
        return leader;
    }

    public void setLeader(UUID leader) {
        this.leader = leader;
    }

    public int bankGold() {
        return bankGold;
    }

    public void setBankGold(int bankGold) {
        this.bankGold = Math.max(0, bankGold);
    }

    public Map<UUID, GuildMemberRole> members() {
        return members;
    }

    public Map<String, GuildQuest> quests() {
        return quests;
    }
}

```

---

## `MineLauncher/plugins/RPGPlugin/src/main/java/com/example/rpg/model/GuildMemberRole.java`

```java
package com.example.rpg.model;

public enum GuildMemberRole {
    LEADER,
    OFFICER,
    MEMBER
}

```

---

## `MineLauncher/plugins/RPGPlugin/src/main/java/com/example/rpg/model/GuildQuest.java`

```java
package com.example.rpg.model;

public class GuildQuest {
    private final String id;
    private String name;
    private String description;
    private int goal;
    private int progress;
    private boolean completed;

    public GuildQuest(String id) {
        this.id = id;
    }

    public String id() {
        return id;
    }

    public String name() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String description() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int goal() {
        return goal;
    }

    public void setGoal(int goal) {
        this.goal = Math.max(1, goal);
    }

    public int progress() {
        return progress;
    }

    public void setProgress(int progress) {
        this.progress = Math.max(0, progress);
    }

    public boolean completed() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }
}

```

---

## `MineLauncher/plugins/RPGPlugin/src/main/java/com/example/rpg/model/LootEntry.java`

```java
package com.example.rpg.model;

public class LootEntry {
    private String material;
    private double chance;
    private int minAmount;
    private int maxAmount;
    private Rarity rarity;

    public LootEntry(String material, double chance, int minAmount, int maxAmount, Rarity rarity) {
        this.material = material;
        this.chance = chance;
        this.minAmount = minAmount;
        this.maxAmount = maxAmount;
        this.rarity = rarity;
    }

    public String material() {
        return material;
    }

    public void setMaterial(String material) {
        this.material = material;
    }

    public double chance() {
        return chance;
    }

    public void setChance(double chance) {
        this.chance = chance;
    }

    public int minAmount() {
        return minAmount;
    }

    public void setMinAmount(int minAmount) {
        this.minAmount = minAmount;
    }

    public int maxAmount() {
        return maxAmount;
    }

    public void setMaxAmount(int maxAmount) {
        this.maxAmount = maxAmount;
    }

    public Rarity rarity() {
        return rarity;
    }

    public void setRarity(Rarity rarity) {
        this.rarity = rarity;
    }
}

```

---

## `MineLauncher/plugins/RPGPlugin/src/main/java/com/example/rpg/model/LootTable.java`

```java
package com.example.rpg.model;

import java.util.ArrayList;
import java.util.List;

public class LootTable {
    private final String id;
    private String appliesTo;
    private List<LootEntry> entries = new ArrayList<>();

    public LootTable(String id) {
        this.id = id;
    }

    public String id() {
        return id;
    }

    public String appliesTo() {
        return appliesTo;
    }

    public void setAppliesTo(String appliesTo) {
        this.appliesTo = appliesTo;
    }

    public List<LootEntry> entries() {
        return entries;
    }

    public void setEntries(List<LootEntry> entries) {
        this.entries = entries;
    }
}

```

---

## `MineLauncher/plugins/RPGPlugin/src/main/java/com/example/rpg/model/MobDefinition.java`

```java
package com.example.rpg.model;

import java.util.ArrayList;
import java.util.List;

public class MobDefinition {
    private final String id;
    private String name;
    private String type;
    private double health;
    private double damage;
    private String mainHand;
    private String helmet;
    private List<String> skills = new ArrayList<>();
    private int skillIntervalSeconds;
    private int xp;
    private String lootTable;
    private String behaviorTree;

    public MobDefinition(String id) {
        this.id = id;
    }

    public String id() {
        return id;
    }

    public String name() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String type() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public double health() {
        return health;
    }

    public void setHealth(double health) {
        this.health = health;
    }

    public double damage() {
        return damage;
    }

    public void setDamage(double damage) {
        this.damage = damage;
    }

    public String mainHand() {
        return mainHand;
    }

    public void setMainHand(String mainHand) {
        this.mainHand = mainHand;
    }

    public String helmet() {
        return helmet;
    }

    public void setHelmet(String helmet) {
        this.helmet = helmet;
    }

    public List<String> skills() {
        return skills;
    }

    public void setSkills(List<String> skills) {
        this.skills = skills;
    }

    public int skillIntervalSeconds() {
        return skillIntervalSeconds;
    }

    public void setSkillIntervalSeconds(int skillIntervalSeconds) {
        this.skillIntervalSeconds = skillIntervalSeconds;
    }

    public int xp() {
        return xp;
    }

    public void setXp(int xp) {
        this.xp = xp;
    }

    public String lootTable() {
        return lootTable;
    }

    public void setLootTable(String lootTable) {
        this.lootTable = lootTable;
    }

    public String behaviorTree() {
        return behaviorTree;
    }

    public void setBehaviorTree(String behaviorTree) {
        this.behaviorTree = behaviorTree;
    }
}

```

---

## `MineLauncher/plugins/RPGPlugin/src/main/java/com/example/rpg/model/Npc.java`

```java
package com.example.rpg.model;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.bukkit.Location;

public class Npc {
    private final String id;
    private UUID uuid;
    private String name;
    private NpcRole role;
    private String world;
    private double x;
    private double y;
    private double z;
    private float yaw;
    private float pitch;
    private List<String> dialog = new ArrayList<>();
    private String questLink;
    private String shopId;

    public Npc(String id) {
        this.id = id;
    }

    public String id() {
        return id;
    }

    public UUID uuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    public String name() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public NpcRole role() {
        return role;
    }

    public void setRole(NpcRole role) {
        this.role = role;
    }

    public String world() {
        return world;
    }

    public void setWorld(String world) {
        this.world = world;
    }

    public void setLocation(Location location) {
        this.world = location.getWorld().getName();
        this.x = location.getX();
        this.y = location.getY();
        this.z = location.getZ();
        this.yaw = location.getYaw();
        this.pitch = location.getPitch();
    }

    public void setRawLocation(String world, double x, double y, double z, float yaw, float pitch) {
        this.world = world;
        this.x = x;
        this.y = y;
        this.z = z;
        this.yaw = yaw;
        this.pitch = pitch;
    }

    public Location toLocation(org.bukkit.World world) {
        return new Location(world, x, y, z, yaw, pitch);
    }

    public double x() {
        return x;
    }

    public double y() {
        return y;
    }

    public double z() {
        return z;
    }

    public float yaw() {
        return yaw;
    }

    public float pitch() {
        return pitch;
    }

    public List<String> dialog() {
        return dialog;
    }

    public void setDialog(List<String> dialog) {
        this.dialog = dialog;
    }

    public String questLink() {
        return questLink;
    }

    public void setQuestLink(String questLink) {
        this.questLink = questLink;
    }

    public String shopId() {
        return shopId;
    }

    public void setShopId(String shopId) {
        this.shopId = shopId;
    }
}

```

---

## `MineLauncher/plugins/RPGPlugin/src/main/java/com/example/rpg/model/NpcRole.java`

```java
package com.example.rpg.model;

public enum NpcRole {
    QUESTGIVER,
    VENDOR,
    TRAINER,
    TELEPORTER,
    BANKER,
    FACTION_AGENT
}

```

---

## `MineLauncher/plugins/RPGPlugin/src/main/java/com/example/rpg/model/Party.java`

```java
package com.example.rpg.model;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class Party {
    private final UUID leader;
    private final Set<UUID> members = new HashSet<>();

    public Party(UUID leader) {
        this.leader = leader;
        members.add(leader);
    }

    public UUID leader() {
        return leader;
    }

    public Set<UUID> members() {
        return members;
    }

    public void addMember(UUID uuid) {
        members.add(uuid);
    }

    public void removeMember(UUID uuid) {
        members.remove(uuid);
    }
}

```

---

## `MineLauncher/plugins/RPGPlugin/src/main/java/com/example/rpg/model/PlayerProfile.java`

```java
package com.example.rpg.model;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;

public class PlayerProfile {
    private final UUID uuid;
    private int level = 1;
    private int xp = 0;
    private int skillPoints = 0;
    private int mana = 100;
    private int maxMana = 100;
    private String classId;
    private final Map<RPGStat, Integer> stats = new EnumMap<>(RPGStat.class);
    private final Map<String, Integer> learnedSkills = new HashMap<>();
    private final Map<String, QuestProgress> activeQuests = new HashMap<>();
    private final Set<String> completedQuests = new HashSet<>();
    private final Map<String, Integer> factionRep = new HashMap<>();
    /**
     * Skill-Cooldowns persistent: skillId -> lastUseMillis
     */
    private final Map<String, Long> skillCooldowns = new HashMap<>();
    private final Map<Integer, String> skillBindings = new HashMap<>();
    private int gold = 0;
    private final Map<String, Integer> professions = new HashMap<>();
    private String guildId;
    private int elo = 1000;

    public PlayerProfile(UUID uuid) {
        this.uuid = uuid;
        for (RPGStat stat : RPGStat.values()) {
            stats.put(stat, 5);
        }
    }

    public UUID uuid() {
        return uuid;
    }

    public int level() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public int xp() {
        return xp;
    }

    public void setXp(int xp) {
        this.xp = xp;
    }

    public int skillPoints() {
        return skillPoints;
    }

    public void setSkillPoints(int skillPoints) {
        this.skillPoints = skillPoints;
    }

    public int mana() {
        return mana;
    }

    public void setMana(int mana) {
        this.mana = mana;
    }

    public int maxMana() {
        return maxMana;
    }

    public void setMaxMana(int maxMana) {
        this.maxMana = maxMana;
    }

    public String classId() {
        return classId;
    }

    public void setClassId(String classId) {
        this.classId = classId;
    }

    public Map<RPGStat, Integer> stats() {
        return stats;
    }

    public Map<String, Integer> learnedSkills() {
        return learnedSkills;
    }

    public Map<String, QuestProgress> activeQuests() {
        return activeQuests;
    }

    public Set<String> completedQuests() {
        return completedQuests;
    }

    public Map<String, Integer> factionRep() {
        return factionRep;
    }

    public Map<String, Long> skillCooldowns() {
        return skillCooldowns;
    }

    public Map<Integer, String> skillBindings() {
        return skillBindings;
    }

    public int gold() {
        return gold;
    }

    public void setGold(int gold) {
        this.gold = Math.max(0, gold);
    }

    public Map<String, Integer> professions() {
        return professions;
    }

    public String guildId() {
        return guildId;
    }

    public void setGuildId(String guildId) {
        this.guildId = guildId;
    }

    public int elo() {
        return elo;
    }

    public void setElo(int elo) {
        this.elo = Math.max(0, elo);
    }

    public void addXp(int amount) {
        xp += amount;
        while (xp >= xpNeeded()) {
            xp -= xpNeeded();
            level++;
            skillPoints += 2;
        }
    }

    public int xpNeeded() {
        return 100 + (level - 1) * 50;
    }

    public void applyAttributes(Player player) {
        int strength = stats.getOrDefault(RPGStat.STRENGTH, 5);
        int dex = stats.getOrDefault(RPGStat.DEXTERITY, 5);
        int con = stats.getOrDefault(RPGStat.CONSTITUTION, 5);
        int intel = stats.getOrDefault(RPGStat.INTELLIGENCE, 5);

        if (player.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE) != null) {
            player.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE).setBaseValue(1.0 + strength * 0.2);
        }
        if (player.getAttribute(Attribute.GENERIC_MAX_HEALTH) != null) {
            player.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(20.0 + con * 0.8);
        }
        if (player.getAttribute(Attribute.GENERIC_ATTACK_SPEED) != null) {
            player.getAttribute(Attribute.GENERIC_ATTACK_SPEED).setBaseValue(4.0 + dex * 0.05);
        }
        if (player.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED) != null) {
            player.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).setBaseValue(0.1 + dex * 0.002);
        }
        maxMana = 100 + intel * 5;
        mana = Math.min(mana, maxMana);
    }
}

```

---

## `MineLauncher/plugins/RPGPlugin/src/main/java/com/example/rpg/model/Profession.java`

```java
package com.example.rpg.model;

public enum Profession {
    GATHERING,
    CRAFTING
}

```

---

## `MineLauncher/plugins/RPGPlugin/src/main/java/com/example/rpg/model/Quest.java`

```java
package com.example.rpg.model;

import java.util.ArrayList;
import java.util.List;

public class Quest {
    private final String id;
    private String name;
    private String description;
    private boolean repeatable;
    private int minLevel;
    private List<QuestStep> steps = new ArrayList<>();
    private QuestReward reward = new QuestReward();

    public Quest(String id) {
        this.id = id;
    }

    public String id() {
        return id;
    }

    public String name() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String description() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean repeatable() {
        return repeatable;
    }

    public void setRepeatable(boolean repeatable) {
        this.repeatable = repeatable;
    }

    public int minLevel() {
        return minLevel;
    }

    public void setMinLevel(int minLevel) {
        this.minLevel = minLevel;
    }

    public List<QuestStep> steps() {
        return steps;
    }

    public void setSteps(List<QuestStep> steps) {
        this.steps = steps;
    }

    public QuestReward reward() {
        return reward;
    }

    public void setReward(QuestReward reward) {
        this.reward = reward;
    }
}

```

---

## `MineLauncher/plugins/RPGPlugin/src/main/java/com/example/rpg/model/QuestProgress.java`

```java
package com.example.rpg.model;

import java.util.HashMap;
import java.util.Map;

public class QuestProgress {
    private final String questId;
    private final Map<Integer, Integer> stepProgress = new HashMap<>();
    private boolean completed;

    public QuestProgress(String questId) {
        this.questId = questId;
    }

    public String questId() {
        return questId;
    }

    public Map<Integer, Integer> stepProgress() {
        return stepProgress;
    }

    public void incrementStep(int index, int amount) {
        stepProgress.put(index, stepProgress.getOrDefault(index, 0) + amount);
    }

    /**
     * Erhöht den Fortschritt, aber nie über "required" hinaus.
     * Damit bleibt Progress stabil, und Auswertungen werden deterministisch.
     */
    public void incrementStepClamped(int index, int amount, int required) {
        int current = stepProgress.getOrDefault(index, 0);
        int next = current + Math.max(0, amount);
        if (required > 0) {
            next = Math.min(required, next);
        }
        stepProgress.put(index, next);
    }

    public int getStepProgress(int index) {
        return stepProgress.getOrDefault(index, 0);
    }

    public boolean completed() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }
}

```

---

## `MineLauncher/plugins/RPGPlugin/src/main/java/com/example/rpg/model/QuestReward.java`

```java
package com.example.rpg.model;

import java.util.HashMap;
import java.util.Map;

public class QuestReward {
    private int xp;
    private int skillPoints;
    private Map<String, Integer> factionRep = new HashMap<>();

    public int xp() {
        return xp;
    }

    public void setXp(int xp) {
        this.xp = xp;
    }

    public int skillPoints() {
        return skillPoints;
    }

    public void setSkillPoints(int skillPoints) {
        this.skillPoints = skillPoints;
    }

    public Map<String, Integer> factionRep() {
        return factionRep;
    }

    public void setFactionRep(Map<String, Integer> factionRep) {
        this.factionRep = factionRep;
    }
}

```

---

## `MineLauncher/plugins/RPGPlugin/src/main/java/com/example/rpg/model/QuestStep.java`

```java
package com.example.rpg.model;

public class QuestStep {
    private QuestStepType type;
    private String target;
    private int amount;

    public QuestStep(QuestStepType type, String target, int amount) {
        this.type = type;
        this.target = target;
        this.amount = amount;
    }

    public QuestStepType type() {
        return type;
    }

    public void setType(QuestStepType type) {
        this.type = type;
    }

    public String target() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    public int amount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }
}

```

---

## `MineLauncher/plugins/RPGPlugin/src/main/java/com/example/rpg/model/QuestStepType.java`

```java
package com.example.rpg.model;

public enum QuestStepType {
    KILL,
    COLLECT,
    TALK,
    EXPLORE,
    CRAFT,
    USE_ITEM,
    DEFEND,
    ESCORT
}

```

---

## `MineLauncher/plugins/RPGPlugin/src/main/java/com/example/rpg/model/RPGStat.java`

```java
package com.example.rpg.model;

public enum RPGStat {
    STRENGTH,
    DEXTERITY,
    CONSTITUTION,
    INTELLIGENCE,
    LUCK
}

```

---

## `MineLauncher/plugins/RPGPlugin/src/main/java/com/example/rpg/model/Rarity.java`

```java
package com.example.rpg.model;

import net.kyori.adventure.text.format.NamedTextColor;

public enum Rarity {
    COMMON(NamedTextColor.WHITE, 1.0),
    UNCOMMON(NamedTextColor.GREEN, 0.6),
    RARE(NamedTextColor.BLUE, 0.35),
    EPIC(NamedTextColor.DARK_PURPLE, 0.15),
    LEGENDARY(NamedTextColor.GOLD, 0.05);

    private final NamedTextColor color;
    private final double weight;

    Rarity(NamedTextColor color, double weight) {
        this.color = color;
        this.weight = weight;
    }

    public NamedTextColor color() {
        return color;
    }

    public double weight() {
        return weight;
    }
}

```

---

## `MineLauncher/plugins/RPGPlugin/src/main/java/com/example/rpg/model/ShopDefinition.java`

```java
package com.example.rpg.model;

import java.util.HashMap;
import java.util.Map;

public class ShopDefinition {
    private final String id;
    private String title;
    private Map<Integer, ShopItem> items = new HashMap<>();

    public ShopDefinition(String id) {
        this.id = id;
    }

    public String id() {
        return id;
    }

    public String title() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Map<Integer, ShopItem> items() {
        return items;
    }

    public void setItems(Map<Integer, ShopItem> items) {
        this.items = items;
    }
}

```

---

## `MineLauncher/plugins/RPGPlugin/src/main/java/com/example/rpg/model/ShopItem.java`

```java
package com.example.rpg.model;

public class ShopItem {
    private int slot;
    private String material;
    private String name;
    private int buyPrice;
    private int sellPrice;

    public int slot() {
        return slot;
    }

    public void setSlot(int slot) {
        this.slot = slot;
    }

    public String material() {
        return material;
    }

    public void setMaterial(String material) {
        this.material = material;
    }

    public String name() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int buyPrice() {
        return buyPrice;
    }

    public void setBuyPrice(int buyPrice) {
        this.buyPrice = buyPrice;
    }

    public int sellPrice() {
        return sellPrice;
    }

    public void setSellPrice(int sellPrice) {
        this.sellPrice = sellPrice;
    }
}

```

---

## `MineLauncher/plugins/RPGPlugin/src/main/java/com/example/rpg/model/Skill.java`

```java
package com.example.rpg.model;

public class Skill {
    private final String id;
    private String name;
    private SkillType type;
    private SkillCategory category;
    private int cooldown;
    private int manaCost;
    private String requiredSkill;
    private java.util.List<com.example.rpg.skill.SkillEffectConfig> effects = new java.util.ArrayList<>();

    public Skill(String id) {
        this.id = id;
    }

    public String id() {
        return id;
    }

    public String name() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public SkillType type() {
        return type;
    }

    public void setType(SkillType type) {
        this.type = type;
    }

    public SkillCategory category() {
        return category;
    }

    public void setCategory(SkillCategory category) {
        this.category = category;
    }

    public int cooldown() {
        return cooldown;
    }

    public void setCooldown(int cooldown) {
        this.cooldown = cooldown;
    }

    public int manaCost() {
        return manaCost;
    }

    public void setManaCost(int manaCost) {
        this.manaCost = manaCost;
    }

    public java.util.List<com.example.rpg.skill.SkillEffectConfig> effects() {
        return effects;
    }

    public void setEffects(java.util.List<com.example.rpg.skill.SkillEffectConfig> effects) {
        this.effects = effects;
    }

    public String requiredSkill() {
        return requiredSkill;
    }

    public void setRequiredSkill(String requiredSkill) {
        this.requiredSkill = requiredSkill;
    }
}

```

---

## `MineLauncher/plugins/RPGPlugin/src/main/java/com/example/rpg/model/SkillCategory.java`

```java
package com.example.rpg.model;

public enum SkillCategory {
    HEALING,
    MAGIC,
    ATTACK,
    DEFENSE,
    PROFESSION
}

```

---

## `MineLauncher/plugins/RPGPlugin/src/main/java/com/example/rpg/model/SkillType.java`

```java
package com.example.rpg.model;

public enum SkillType {
    ACTIVE,
    PASSIVE
}

```

---

## `MineLauncher/plugins/RPGPlugin/src/main/java/com/example/rpg/model/Spawner.java`

```java
package com.example.rpg.model;

import java.util.HashMap;
import java.util.Map;

public class Spawner {
    private final String id;
    private String zoneId;
    private int maxMobs;
    private int spawnInterval;
    private Map<String, Double> mobs = new HashMap<>();

    public Spawner(String id) {
        this.id = id;
    }

    public String id() {
        return id;
    }

    public String zoneId() {
        return zoneId;
    }

    public void setZoneId(String zoneId) {
        this.zoneId = zoneId;
    }

    public int maxMobs() {
        return maxMobs;
    }

    public void setMaxMobs(int maxMobs) {
        this.maxMobs = maxMobs;
    }

    public int spawnInterval() {
        return spawnInterval;
    }

    public void setSpawnInterval(int spawnInterval) {
        this.spawnInterval = spawnInterval;
    }

    public Map<String, Double> mobs() {
        return mobs;
    }

    public void setMobs(Map<String, Double> mobs) {
        this.mobs = mobs;
    }
}

```

---

## `MineLauncher/plugins/RPGPlugin/src/main/java/com/example/rpg/model/TradeRequest.java`

```java
package com.example.rpg.model;

import java.util.UUID;

public class TradeRequest {
    private final UUID requester;
    private final UUID target;
    private int goldOffer;
    private int goldRequest;
    private boolean requesterReady;
    private boolean targetReady;

    public TradeRequest(UUID requester, UUID target) {
        this.requester = requester;
        this.target = target;
    }

    public UUID requester() {
        return requester;
    }

    public UUID target() {
        return target;
    }

    public int goldOffer() {
        return goldOffer;
    }

    public void setGoldOffer(int goldOffer) {
        this.goldOffer = goldOffer;
    }

    public int goldRequest() {
        return goldRequest;
    }

    public void setGoldRequest(int goldRequest) {
        this.goldRequest = goldRequest;
    }

    public boolean requesterReady() {
        return requesterReady;
    }

    public void setRequesterReady(boolean requesterReady) {
        this.requesterReady = requesterReady;
    }

    public boolean targetReady() {
        return targetReady;
    }

    public void setTargetReady(boolean targetReady) {
        this.targetReady = targetReady;
    }
}

```

---

## `MineLauncher/plugins/RPGPlugin/src/main/java/com/example/rpg/model/Zone.java`

```java
package com.example.rpg.model;

import org.bukkit.Location;

public class Zone {
    private final String id;
    private String name;
    private String world;
    private int minLevel;
    private int maxLevel;
    private double slowMultiplier = 1.0;
    private double damageMultiplier = 1.0;
    private int x1;
    private int y1;
    private int z1;
    private int x2;
    private int y2;
    private int z2;

    public Zone(String id) {
        this.id = id;
    }

    public String id() {
        return id;
    }

    public String name() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String world() {
        return world;
    }

    public void setWorld(String world) {
        this.world = world;
    }

    public int minLevel() {
        return minLevel;
    }

    public void setMinLevel(int minLevel) {
        this.minLevel = minLevel;
    }

    public int maxLevel() {
        return maxLevel;
    }

    public void setMaxLevel(int maxLevel) {
        this.maxLevel = maxLevel;
    }

    public double slowMultiplier() {
        return slowMultiplier;
    }

    public void setSlowMultiplier(double slowMultiplier) {
        this.slowMultiplier = slowMultiplier;
    }

    public double damageMultiplier() {
        return damageMultiplier;
    }

    public void setDamageMultiplier(double damageMultiplier) {
        this.damageMultiplier = damageMultiplier;
    }

    public void setBounds(Location pos1, Location pos2) {
        this.x1 = Math.min(pos1.getBlockX(), pos2.getBlockX());
        this.y1 = Math.min(pos1.getBlockY(), pos2.getBlockY());
        this.z1 = Math.min(pos1.getBlockZ(), pos2.getBlockZ());
        this.x2 = Math.max(pos1.getBlockX(), pos2.getBlockX());
        this.y2 = Math.max(pos1.getBlockY(), pos2.getBlockY());
        this.z2 = Math.max(pos1.getBlockZ(), pos2.getBlockZ());
    }

    public boolean contains(Location location) {
        if (location == null || !location.getWorld().getName().equals(world)) {
            return false;
        }
        int x = location.getBlockX();
        int y = location.getBlockY();
        int z = location.getBlockZ();
        return x >= x1 && x <= x2 && y >= y1 && y <= y2 && z >= z1 && z <= z2;
    }

    public int x1() {
        return x1;
    }

    public int y1() {
        return y1;
    }

    public int z1() {
        return z1;
    }

    public int x2() {
        return x2;
    }

    public int y2() {
        return y2;
    }

    public int z2() {
        return z2;
    }

    public void setCoordinates(int x1, int y1, int z1, int x2, int y2, int z2) {
        this.x1 = x1;
        this.y1 = y1;
        this.z1 = z1;
        this.x2 = x2;
        this.y2 = y2;
        this.z2 = z2;
    }
}

```

---

## `MineLauncher/plugins/RPGPlugin/src/main/java/com/example/rpg/skill/SkillEffect.java`

```java
package com.example.rpg.skill;

import com.example.rpg.model.PlayerProfile;
import java.util.List;
import java.util.Map;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;

public interface SkillEffect {
    void apply(Player player, PlayerProfile profile, Map<String, Object> params);

    default List<Component> describe(Map<String, Object> params) {
        return List.of();
    }
}

```

---

## `MineLauncher/plugins/RPGPlugin/src/main/java/com/example/rpg/skill/SkillEffectConfig.java`

```java
package com.example.rpg.skill;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class SkillEffectConfig {
    private SkillEffectType type;
    private Map<String, Object> params = new HashMap<>();

    public SkillEffectConfig(SkillEffectType type, Map<String, Object> params) {
        this.type = type;
        if (params != null) {
            this.params.putAll(params);
        }
    }

    public SkillEffectType type() {
        return type;
    }

    public void setType(SkillEffectType type) {
        this.type = type;
    }

    public Map<String, Object> params() {
        return params;
    }

    public void setParams(Map<String, Object> params) {
        this.params = params;
    }

    public String describe() {
        if (params.isEmpty()) {
            return type.name();
        }
        String joined = params.entrySet().stream()
            .map(entry -> entry.getKey() + "=" + entry.getValue())
            .collect(Collectors.joining(", "));
        return type.name() + " (" + joined + ")";
    }
}

```

---

## `MineLauncher/plugins/RPGPlugin/src/main/java/com/example/rpg/skill/SkillEffectRegistry.java`

```java
package com.example.rpg.skill;

import java.util.EnumMap;
import java.util.Map;

public class SkillEffectRegistry {
    private final Map<SkillEffectType, SkillEffect> effects = new EnumMap<>(SkillEffectType.class);

    public SkillEffectRegistry register(SkillEffectType type, SkillEffect effect) {
        effects.put(type, effect);
        return this;
    }

    public void apply(SkillEffectConfig config, org.bukkit.entity.Player player,
                      com.example.rpg.model.PlayerProfile profile) {
        SkillEffect effect = effects.get(config.type());
        if (effect == null) {
            return;
        }
        effect.apply(player, profile, config.params());
    }
}

```

---

## `MineLauncher/plugins/RPGPlugin/src/main/java/com/example/rpg/skill/SkillEffectType.java`

```java
package com.example.rpg.skill;

public enum SkillEffectType {
    HEAL,
    DAMAGE,
    PROJECTILE,
    POTION,
    SOUND,
    XP,
    PARTICLE,
    VELOCITY,
    AGGRO
}

```

---

## `MineLauncher/plugins/RPGPlugin/src/main/java/com/example/rpg/skill/effects/AggroEffect.java`

```java
package com.example.rpg.skill.effects;

import com.example.rpg.model.PlayerProfile;
import com.example.rpg.skill.SkillEffect;
import java.util.Map;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;

public class AggroEffect implements SkillEffect {
    @Override
    public void apply(Player player, PlayerProfile profile, Map<String, Object> params) {
        double radius = parseDouble(params.getOrDefault("radius", 8));
        player.getNearbyEntities(radius, radius, radius).stream()
            .filter(entity -> entity instanceof Mob)
            .map(entity -> (Mob) entity)
            .forEach(mob -> mob.setTarget(player));
    }

    private double parseDouble(Object raw) {
        try {
            return Double.parseDouble(String.valueOf(raw));
        } catch (NumberFormatException e) {
            return 8.0;
        }
    }
}

```

---

## `MineLauncher/plugins/RPGPlugin/src/main/java/com/example/rpg/skill/effects/DamageEffect.java`

```java
package com.example.rpg.skill.effects;

import com.example.rpg.model.PlayerProfile;
import com.example.rpg.skill.SkillEffect;
import java.util.Comparator;
import java.util.Map;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

public class DamageEffect implements SkillEffect {
    @Override
    public void apply(Player player, PlayerProfile profile, Map<String, Object> params) {
        double amount = parseDouble(params.getOrDefault("amount", 4));
        double radius = parseDouble(params.getOrDefault("radius", 0));
        int maxTargets = parseInt(params.getOrDefault("maxTargets", 1));
        if (radius > 0) {
            player.getNearbyEntities(radius, radius, radius).stream()
                .filter(entity -> entity instanceof LivingEntity && !(entity instanceof Player))
                .sorted(Comparator.comparingDouble(entity -> entity.getLocation().distanceSquared(player.getLocation())))
                .limit(Math.max(1, maxTargets))
                .map(entity -> (LivingEntity) entity)
                .forEach(target -> target.damage(amount, player));
            return;
        }

        Entity target = player.getNearbyEntities(3, 2, 3).stream()
            .filter(entity -> entity instanceof LivingEntity && !(entity instanceof Player))
            .min(Comparator.comparingDouble(entity -> entity.getLocation().distanceSquared(player.getLocation())))
            .orElse(null);
        if (target instanceof LivingEntity living) {
            living.damage(amount, player);
        }
    }

    private double parseDouble(Object raw) {
        try {
            return Double.parseDouble(String.valueOf(raw));
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }

    private int parseInt(Object raw) {
        try {
            return Integer.parseInt(String.valueOf(raw));
        } catch (NumberFormatException e) {
            return 1;
        }
    }
}

```

---

## `MineLauncher/plugins/RPGPlugin/src/main/java/com/example/rpg/skill/effects/HealEffect.java`

```java
package com.example.rpg.skill.effects;

import com.example.rpg.model.PlayerProfile;
import com.example.rpg.skill.SkillEffect;
import java.util.Map;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;

public class HealEffect implements SkillEffect {
    @Override
    public void apply(Player player, PlayerProfile profile, Map<String, Object> params) {
        double amount = parseDouble(params.getOrDefault("amount", 4));
        double maxHealth = player.getAttribute(Attribute.GENERIC_MAX_HEALTH) != null
            ? player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue()
            : 20.0;
        double newHealth = Math.min(maxHealth, player.getHealth() + amount);
        player.setHealth(newHealth);
    }

    private double parseDouble(Object raw) {
        try {
            return Double.parseDouble(String.valueOf(raw));
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }
}

```

---

## `MineLauncher/plugins/RPGPlugin/src/main/java/com/example/rpg/skill/effects/ParticleEffect.java`

```java
package com.example.rpg.skill.effects;

import com.example.rpg.model.PlayerProfile;
import com.example.rpg.skill.SkillEffect;
import java.util.Map;
import org.bukkit.Particle;
import org.bukkit.entity.Player;

public class ParticleEffect implements SkillEffect {
    @Override
    public void apply(Player player, PlayerProfile profile, Map<String, Object> params) {
        String typeName = String.valueOf(params.getOrDefault("type", "SPELL")).toUpperCase();
        int count = parseInt(params.getOrDefault("count", 10));
        double speed = parseDouble(params.getOrDefault("speed", 0.01));
        Particle particle;
        try {
            particle = Particle.valueOf(typeName);
        } catch (IllegalArgumentException e) {
            particle = Particle.SPELL;
        }
        player.getWorld().spawnParticle(particle, player.getLocation().add(0, 1.0, 0), count, 0.3, 0.6, 0.3, speed);
    }

    private int parseInt(Object raw) {
        try {
            return Integer.parseInt(String.valueOf(raw));
        } catch (NumberFormatException e) {
            return 10;
        }
    }

    private double parseDouble(Object raw) {
        try {
            return Double.parseDouble(String.valueOf(raw));
        } catch (NumberFormatException e) {
            return 0.01;
        }
    }
}

```

---

## `MineLauncher/plugins/RPGPlugin/src/main/java/com/example/rpg/skill/effects/PotionStatusEffect.java`

```java
package com.example.rpg.skill.effects;

import com.example.rpg.model.PlayerProfile;
import com.example.rpg.skill.SkillEffect;
import java.util.Map;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class PotionStatusEffect implements SkillEffect {
    @Override
    public void apply(Player player, PlayerProfile profile, Map<String, Object> params) {
        String typeName = String.valueOf(params.getOrDefault("type", "SPEED")).toUpperCase();
        int duration = parseInt(params.getOrDefault("duration", 100));
        int amplifier = parseInt(params.getOrDefault("amplifier", 0));
        double radius = parseDouble(params.getOrDefault("radius", 0));
        PotionEffectType type = PotionEffectType.getByName(typeName);
        if (type == null) {
            return;
        }
        PotionEffect effect = new PotionEffect(type, duration, amplifier);
        if (radius > 0) {
            player.getNearbyEntities(radius, radius, radius).stream()
                .filter(entity -> entity instanceof LivingEntity)
                .map(entity -> (LivingEntity) entity)
                .forEach(target -> target.addPotionEffect(effect));
            return;
        }
        player.addPotionEffect(effect);
    }

    private int parseInt(Object raw) {
        try {
            return Integer.parseInt(String.valueOf(raw));
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private double parseDouble(Object raw) {
        try {
            return Double.parseDouble(String.valueOf(raw));
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }
}

```

---

## `MineLauncher/plugins/RPGPlugin/src/main/java/com/example/rpg/skill/effects/ProjectileEffect.java`

```java
package com.example.rpg.skill.effects;

import com.example.rpg.model.PlayerProfile;
import com.example.rpg.skill.SkillEffect;
import java.util.Map;
import org.bukkit.entity.Player;
import org.bukkit.entity.SmallFireball;
import org.bukkit.entity.Snowball;

public class ProjectileEffect implements SkillEffect {
    @Override
    public void apply(Player player, PlayerProfile profile, Map<String, Object> params) {
        String type = String.valueOf(params.getOrDefault("type", "SNOWBALL")).toUpperCase();
        switch (type) {
            case "SMALL_FIREBALL" -> player.launchProjectile(SmallFireball.class);
            case "SNOWBALL" -> player.launchProjectile(Snowball.class);
            default -> player.launchProjectile(Snowball.class);
        }
    }
}

```

---

## `MineLauncher/plugins/RPGPlugin/src/main/java/com/example/rpg/skill/effects/SoundEffect.java`

```java
package com.example.rpg.skill.effects;

import com.example.rpg.model.PlayerProfile;
import com.example.rpg.skill.SkillEffect;
import java.util.Map;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

public class SoundEffect implements SkillEffect {
    @Override
    public void apply(Player player, PlayerProfile profile, Map<String, Object> params) {
        String soundName = String.valueOf(params.getOrDefault("sound", "ENTITY_PLAYER_LEVELUP")).toUpperCase();
        float volume = parseFloat(params.getOrDefault("volume", 1.0));
        float pitch = parseFloat(params.getOrDefault("pitch", 1.0));
        Sound sound;
        try {
            sound = Sound.valueOf(soundName);
        } catch (IllegalArgumentException e) {
            sound = Sound.ENTITY_PLAYER_LEVELUP;
        }
        player.getWorld().playSound(player.getLocation(), sound, volume, pitch);
    }

    private float parseFloat(Object raw) {
        try {
            return Float.parseFloat(String.valueOf(raw));
        } catch (NumberFormatException e) {
            return 1.0f;
        }
    }
}

```

---

## `MineLauncher/plugins/RPGPlugin/src/main/java/com/example/rpg/skill/effects/VelocityEffect.java`

```java
package com.example.rpg.skill.effects;

import com.example.rpg.model.PlayerProfile;
import com.example.rpg.skill.SkillEffect;
import java.util.Map;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

public class VelocityEffect implements SkillEffect {
    @Override
    public void apply(Player player, PlayerProfile profile, Map<String, Object> params) {
        double forward = parseDouble(params.getOrDefault("forward", 1.2));
        double up = parseDouble(params.getOrDefault("up", 0.3));
        boolean add = parseBoolean(params.getOrDefault("add", false));
        Vector direction = player.getLocation().getDirection().multiply(forward);
        direction.setY(up);
        if (add) {
            player.setVelocity(player.getVelocity().add(direction));
        } else {
            player.setVelocity(direction);
        }
    }

    private double parseDouble(Object raw) {
        try {
            return Double.parseDouble(String.valueOf(raw));
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }

    private boolean parseBoolean(Object raw) {
        return Boolean.parseBoolean(String.valueOf(raw));
    }
}

```

---

## `MineLauncher/plugins/RPGPlugin/src/main/java/com/example/rpg/skill/effects/XpEffect.java`

```java
package com.example.rpg.skill.effects;

import com.example.rpg.model.PlayerProfile;
import com.example.rpg.skill.SkillEffect;
import java.util.Map;
import org.bukkit.entity.Player;

public class XpEffect implements SkillEffect {
    @Override
    public void apply(Player player, PlayerProfile profile, Map<String, Object> params) {
        int amount = parseInt(params.getOrDefault("amount", 0));
        if (amount > 0) {
            profile.addXp(amount);
        }
    }

    private int parseInt(Object raw) {
        try {
            return Integer.parseInt(String.valueOf(raw));
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}

```

---

## `MineLauncher/plugins/RPGPlugin/src/main/java/com/example/rpg/util/AuditLog.java`

```java
package com.example.rpg.util;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.Instant;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

public class AuditLog {
    private final File file;
    private final JavaPlugin plugin;

    public AuditLog(JavaPlugin plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "audit.log");
    }

    public void log(CommandSender sender, String action) {
        String line = Instant.now() + " | " + sender.getName() + " | " + action + System.lineSeparator();
        try (FileWriter writer = new FileWriter(file, true)) {
            writer.write(line);
        } catch (IOException e) {
            plugin.getLogger().warning("Audit log failed: " + e.getMessage());
        }
    }
}

```

---

## `MineLauncher/plugins/RPGPlugin/src/main/java/com/example/rpg/util/EloCalculator.java`

```java
package com.example.rpg.util;

public final class EloCalculator {
    private EloCalculator() {}

    public static int calculateNewRating(int rating, int opponentRating, double score, int kFactor) {
        double expected = 1.0 / (1.0 + Math.pow(10.0, (opponentRating - rating) / 400.0));
        return (int) Math.round(rating + kFactor * (score - expected));
    }
}

```

---

## `MineLauncher/plugins/RPGPlugin/src/main/java/com/example/rpg/util/ItemBuilder.java`

```java
package com.example.rpg.util;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class ItemBuilder {
    private final ItemStack item;
    private final ItemMeta meta;
    private final List<Component> lore = new ArrayList<>();

    public ItemBuilder(Material material) {
        this.item = new ItemStack(material);
        this.meta = item.getItemMeta();
    }

    public ItemBuilder name(Component name) {
        meta.displayName(name);
        return this;
    }

    public ItemBuilder loreLine(Component line) {
        lore.add(line);
        return this;
    }

    public ItemBuilder loreLines(List<Component> lines) {
        lore.addAll(lines);
        return this;
    }

    public ItemStack build() {
        if (!lore.isEmpty()) {
            meta.lore(lore.stream().collect(Collectors.toList()));
        }
        item.setItemMeta(meta);
        return item;
    }
}

```

---

## `MineLauncher/plugins/RPGPlugin/src/main/java/com/example/rpg/util/ItemGenerator.java`

```java
package com.example.rpg.util;

import com.example.rpg.model.Rarity;
import java.util.List;
import java.util.Random;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

public class ItemGenerator {
    private final Random random = new Random();
    private final NamespacedKey itemKey;
    private final NamespacedKey rarityKey;
    private final com.example.rpg.manager.ItemStatManager itemStatManager;

    public ItemGenerator(JavaPlugin plugin, com.example.rpg.manager.ItemStatManager itemStatManager) {
        this.itemKey = new NamespacedKey(plugin, "rpg_item");
        this.rarityKey = new NamespacedKey(plugin, "rpg_rarity");
        this.itemStatManager = itemStatManager;
    }

    public ItemStack createRpgItem(Material material, Rarity rarity, int minLevel) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(Component.text(rarity.name() + " " + material.name()).color(rarity.color()));
        meta.lore(List.of(
            Component.text("Rarity: " + rarity.name()).color(rarity.color()),
            Component.text("Level " + minLevel)
        ));
        meta.getPersistentDataContainer().set(itemKey, PersistentDataType.INTEGER, 1);
        meta.getPersistentDataContainer().set(rarityKey, PersistentDataType.STRING, rarity.name());
        item.setItemMeta(meta);
        itemStatManager.applyAffixes(item);
        item.setAmount(1 + random.nextInt(1));
        return item;
    }
}

```

---

## `MineLauncher/plugins/RPGPlugin/src/main/java/com/example/rpg/util/PromptManager.java`

```java
package com.example.rpg.util;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;

public class PromptManager {
    private final Map<UUID, Consumer<String>> prompts = new HashMap<>();

    public void prompt(Player player, Component message, Consumer<String> handler) {
        prompts.put(player.getUniqueId(), handler);
        player.sendMessage(message);
    }

    public boolean handle(Player player, String message) {
        Consumer<String> handler = prompts.remove(player.getUniqueId());
        if (handler == null) {
            return false;
        }
        handler.accept(message);
        return true;
    }

    public void cancel(Player player) {
        prompts.remove(player.getUniqueId());
    }
}

```

---

## `MineLauncher/plugins/RPGPlugin/src/main/java/com/example/rpg/util/Text.java`

```java
package com.example.rpg.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;

public final class Text {
    private static final MiniMessage MINI = MiniMessage.miniMessage();

    private Text() {
    }

    public static Component mm(String input) {
        return MINI.deserialize(input);
    }
}

```

---

## `MineLauncher/plugins/RPGPlugin/src/main/java/com/example/rpg/util/WorldUtils.java`

```java
package com.example.rpg.util;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.stream.Stream;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

public class WorldUtils {
    public static void unloadAndDeleteWorld(World world, Location fallback) {
        if (world == null) {
            return;
        }

        for (Player player : world.getPlayers()) {
            if (fallback != null) {
                player.teleport(fallback);
            } else if (!Bukkit.getWorlds().isEmpty()) {
                player.teleport(Bukkit.getWorlds().get(0).getSpawnLocation());
            }
            player.sendMessage(Text.mm("<yellow>Der Dungeon löst sich auf..."));
        }

        Bukkit.unloadWorld(world, false);
        File worldFolder = world.getWorldFolder();
        deleteDirectory(worldFolder);
    }

    private static void deleteDirectory(File directory) {
        if (directory == null || !directory.exists()) {
            return;
        }
        try (Stream<Path> walk = Files.walk(directory.toPath())) {
            walk.sorted(Comparator.reverseOrder())
                .map(Path::toFile)
                .forEach(File::delete);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

```

---

## `MineLauncher/plugins/RPGPlugin/src/main/resources/config.yml`

```
rpg:
  dataVersion: 1
  xp:
    mobBase: 10
    blockBreak: 1
    craft: 2
  manaRegenPerTick: 5
  party:
    xpSplit: true
database:
  host: localhost
  port: 5432
  name: rpg
  user: rpg
  password: minecraft
  poolSize: 10
dungeon:
  entrance:
    world: world
    x: 0
    y: 64
    z: 0
  exit:
    world: world
    x: 0
    y: 64
    z: 0

```

---

## `MineLauncher/plugins/RPGPlugin/src/main/resources/plugin.yml`

```
name: MineLauncherRPG
main: com.example.rpg.RPGPlugin
version: 1.0.0
author: LauncherTeam
api-version: 1.20
commands:
  rpg:
    description: Open RPG menu and use RPG commands
    usage: /rpg
  rpgadmin:
    description: Open RPG admin tools
    usage: /rpgadmin
  party:
    description: Party management
    usage: /party
  p:
    description: Party chat shortcut
    usage: /p <message>
  auction:
    description: Auction house
    usage: /auction <list|sell|buy>
  trade:
    description: Player trade
    usage: /trade <request|accept|offer|requestgold|ready|cancel>
  dungeon:
    description: Dungeon management
    usage: /dungeon <enter|leave|generate>
  guild:
    description: Guild management
    usage: /guild <create|invite|accept|leave|disband|info|chat|bank|quest>
  g:
    description: Guild chat shortcut
    usage: /g <message>
  pvp:
    description: PvP arenas
    usage: /pvp <join|top>
permissions:
  rpg.admin.*:
    description: Full RPG admin permissions
    default: op
    children:
      rpg.admin: true
      rpg.editor: true
      rpg.debug: true
  rpg.editor.*:
    description: Full RPG editor permissions
    default: op
    children:
      rpg.editor: true
  rpg.mod.*:
    description: RPG moderation permissions
    default: op
    children:
      rpg.mod: true
  rpg.admin:
    description: Access admin menu
    default: op
  rpg.editor:
    description: Access editor tools
    default: op
  rpg.mod:
    description: Access moderation tools
    default: op
  rpg.debug:
    description: Access debug overlay
    default: op

```

---

## `MineLauncher/plugins/WorldCreatorPlugin/README.md`

```markdown
# WorldCreatorPlugin

Ein Bukkit/Spigot-Plugin, das ein Ingame-Menü öffnet, um neue Welten zu erstellen (Leere Welt, Wasserwelt, Sky-Inseln, Dschungel, Wüste) und den Spieler direkt zu teleportieren.

## Build

```bash
mvn -f plugins/WorldCreatorPlugin/pom.xml package
```

Die JAR liegt danach unter `plugins/WorldCreatorPlugin/target/`.

## Nutzung

- Plugin in den Server-Ordner `plugins/` legen.
- Server starten.
- Ingame `/worlds` eingeben, um das Menü zu öffnen.
- Auf ein Symbol klicken, um eine Welt zu erstellen und zu betreten.

```

---

## `MineLauncher/plugins/WorldCreatorPlugin/pom.xml`

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0
                             http://maven.apache.org/xsd/maven-4.0.0.xsd">

    <modelVersion>4.0.0</modelVersion>

    <groupId>com.example</groupId>
    <artifactId>world-creator-plugin</artifactId>
    <version>1.0.0</version>
    <name>WorldCreatorPlugin</name>

    <properties>
        <!-- Paper 1.20.4 benötigt Java 17 -->
        <java.version>17</java.version>
        <paper.version>1.20.4-R0.1-SNAPSHOT</paper.version>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
    </properties>

    <repositories>
        <repository>
            <id>papermc</id>
            <url>https://repo.papermc.io/repository/maven-public/</url>
        </repository>
    </repositories>

    <dependencies>
        <dependency>
            <groupId>io.papermc.paper</groupId>
            <artifactId>paper-api</artifactId>
            <version>${paper.version}</version>
            <scope>provided</scope>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-compiler-plugin</artifactId>
                <version>3.11.0</version>
                <configuration>
                    <!-- verhindert falschen Bytecode -->
                    <release>${java.version}</release>
                </configuration>
            </plugin>
        </plugins>
    </build>

</project>

```

---

## `MineLauncher/plugins/WorldCreatorPlugin/src/main/java/com/example/worldcreator/FixedBiomeProvider.java`

```java
package com.example.worldcreator;

import java.util.List;
import org.bukkit.block.Biome;
import org.bukkit.generator.BiomeProvider;
import org.bukkit.generator.WorldInfo;

public final class FixedBiomeProvider extends BiomeProvider {
    private final Biome biome;

    public FixedBiomeProvider(Biome biome) {
        this.biome = biome;
    }

    @Override
    public Biome getBiome(WorldInfo worldInfo, int x, int y, int z) {
        return biome;
    }

    @Override
    public List<Biome> getBiomes(WorldInfo worldInfo) {
        return List.of(biome);
    }
}

```

---

## `MineLauncher/plugins/WorldCreatorPlugin/src/main/java/com/example/worldcreator/SkyIslandsChunkGenerator.java`

```java
package com.example.worldcreator;

import java.util.Random;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.generator.ChunkGenerator;

public final class SkyIslandsChunkGenerator extends ChunkGenerator {

    @Override
    public ChunkData generateChunkData(
            World world,
            Random random,
            int chunkX,
            int chunkZ,
            BiomeGrid biome
    ) {
        var chunkData = createChunkData(world);

        // deterministische Chunk-Zufallsquelle
        var seededRandom = new Random(world.getSeed() ^ (chunkX * 341873128712L) ^ (chunkZ * 132897987541L));

        if (seededRandom.nextDouble() < 0.35) {
            int centerX = seededRandom.nextInt(16);
            int centerZ = seededRandom.nextInt(16);
            int centerY = 90 + seededRandom.nextInt(30);
            int radius = 4 + seededRandom.nextInt(5);

            for (int x = 0; x < 16; x++) {
                for (int z = 0; z < 16; z++) {
                    for (int y = Math.max(1, centerY - radius); y <= centerY + radius; y++) {
                        double dx = x - centerX;
                        double dy = y - centerY;
                        double dz = z - centerZ;

                        double dist = Math.sqrt(dx * dx + dy * dy + dz * dz);
                        if (dist <= radius) {
                            chunkData.setBlock(
                                    x, y, z,
                                    (y == centerY + radius - 1) ? Material.GRASS_BLOCK : Material.STONE
                            );
                        }
                    }
                }
            }
        }

        return chunkData;
    }
}

```

---

## `MineLauncher/plugins/WorldCreatorPlugin/src/main/java/com/example/worldcreator/VoidChunkGenerator.java`

```java
package com.example.worldcreator;

import java.util.Random;
import org.bukkit.World;
import org.bukkit.generator.ChunkGenerator;

public final class VoidChunkGenerator extends ChunkGenerator {

    @Override
    public ChunkData generateChunkData(
            World world,
            Random random,
            int chunkX,
            int chunkZ,
            BiomeGrid biome
    ) {
        return createChunkData(world);
    }
}

```

---

## `MineLauncher/plugins/WorldCreatorPlugin/src/main/java/com/example/worldcreator/WaterChunkGenerator.java`

```java
package com.example.worldcreator;

import java.util.Random;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.generator.ChunkGenerator;

public final class WaterChunkGenerator extends ChunkGenerator {
    private static final int SEA_LEVEL = 62;

    @Override
    public ChunkData generateChunkData(
            World world,
            Random random,
            int chunkX,
            int chunkZ,
            BiomeGrid biome
    ) {
        var chunkData = createChunkData(world);

        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                chunkData.setBlock(x, 0, z, Material.BEDROCK);
                for (int y = 1; y <= SEA_LEVEL; y++) {
                    chunkData.setBlock(x, y, z, Material.WATER);
                }
            }
        }

        return chunkData;
    }
}

```

---

## `MineLauncher/plugins/WorldCreatorPlugin/src/main/java/com/example/worldcreator/WorldCreatorPlugin.java`

```java
package com.example.worldcreator;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.EnumMap;
import java.util.Map;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.block.Biome;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

public final class WorldCreatorPlugin extends JavaPlugin implements Listener {
    private static final String MENU_TITLE = ChatColor.DARK_AQUA + "World Creator";
    private static final DateTimeFormatter NAME_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss");

    private NamespacedKey worldTypeKey;
    private final Map<WorldTypeOption, Integer> slotMap = new EnumMap<>(WorldTypeOption.class);

    @Override
    public void onEnable() {
        worldTypeKey = new NamespacedKey(this, "world-type");
        Bukkit.getPluginManager().registerEvents(this, this);

        slotMap.put(WorldTypeOption.VOID, 10);
        slotMap.put(WorldTypeOption.WATER, 11);
        slotMap.put(WorldTypeOption.SKY_ISLANDS, 12);
        slotMap.put(WorldTypeOption.JUNGLE, 14);
        slotMap.put(WorldTypeOption.DESERT, 15);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Nur Spieler können dieses Kommando nutzen.");
            return true;
        }

        player.openInventory(buildMenu());
        return true;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }

        if (!event.getView().getTitle().equals(MENU_TITLE)) {
            return;
        }

        event.setCancelled(true);
        var clicked = event.getCurrentItem();
        if (clicked == null || clicked.getType() == Material.AIR) {
            return;
        }

        var meta = clicked.getItemMeta();
        if (meta == null) {
            return;
        }

        var typeName = meta.getPersistentDataContainer().get(worldTypeKey, PersistentDataType.STRING);
        if (typeName == null) {
            return;
        }

        var option = WorldTypeOption.valueOf(typeName);
        createWorldAndTeleport(player, option);
    }

    private Inventory buildMenu() {
        var inventory = Bukkit.createInventory(null, 27, MENU_TITLE);
        for (var entry : slotMap.entrySet()) {
            inventory.setItem(entry.getValue(), createMenuItem(entry.getKey()));
        }
        return inventory;
    }

    private ItemStack createMenuItem(WorldTypeOption option) {
        var item = new ItemStack(option.getIcon());
        var meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.AQUA + option.getDisplayName());
            meta.getPersistentDataContainer().set(worldTypeKey, PersistentDataType.STRING, option.name());
            item.setItemMeta(meta);
        }
        return item;
    }

    private void createWorldAndTeleport(Player player, WorldTypeOption option) {
        var worldName = option.name().toLowerCase() + "-" + NAME_FORMAT.format(LocalDateTime.now());
        var creator = new WorldCreator(worldName);

        switch (option) {
            case VOID -> creator.generator(new VoidChunkGenerator());
            case WATER -> creator.generator(new WaterChunkGenerator());
            case SKY_ISLANDS -> creator.generator(new SkyIslandsChunkGenerator());
            case JUNGLE -> creator.biomeProvider(new FixedBiomeProvider(Biome.JUNGLE));
            case DESERT -> creator.biomeProvider(new FixedBiomeProvider(Biome.DESERT));
        }

        World world = creator.createWorld();
        if (world == null) {
            player.sendMessage(ChatColor.RED + "Welt konnte nicht erstellt werden.");
            return;
        }

        player.sendMessage(ChatColor.GREEN + "Welt erstellt: " + world.getName());
        player.teleport(world.getSpawnLocation());
    }
}

```

---

## `MineLauncher/plugins/WorldCreatorPlugin/src/main/java/com/example/worldcreator/WorldTypeOption.java`

```java
package com.example.worldcreator;

import org.bukkit.Material;

public enum WorldTypeOption {
    VOID("Leere Welt", Material.GLASS),
    WATER("Wasserwelt", Material.WATER_BUCKET),
    SKY_ISLANDS("Sky Inseln", Material.ELYTRA),
    JUNGLE("Dschungel", Material.JUNGLE_LOG),
    DESERT("Wüste", Material.SAND);

    private final String displayName;
    private final Material icon;

    WorldTypeOption(String displayName, Material icon) {
        this.displayName = displayName;
        this.icon = icon;
    }

    public String getDisplayName() {
        return displayName;
    }

    public Material getIcon() {
        return icon;
    }
}

```

---

## `MineLauncher/plugins/WorldCreatorPlugin/src/main/resources/plugin.yml`

```
name: WorldCreatorPlugin
main: com.example.worldcreator.WorldCreatorPlugin
version: 1.0.0
author: LauncherTeam
api-version: 1.20
commands:
  worlds:
    description: Open world creator menu
    usage: /worlds

```

---

## `MineLauncher/plugins/WorldCreatorPlugin/target/classes/com/example/worldcreator/FixedBiomeProvider.class`

```
[BINARY_FILE:MineLauncher/plugins/WorldCreatorPlugin/target/classes/com/example/worldcreator/FixedBiomeProvider.class]
```

---

## `MineLauncher/plugins/WorldCreatorPlugin/target/classes/com/example/worldcreator/SkyIslandsChunkGenerator.class`

```
[BINARY_FILE:MineLauncher/plugins/WorldCreatorPlugin/target/classes/com/example/worldcreator/SkyIslandsChunkGenerator.class]
```

---

## `MineLauncher/plugins/WorldCreatorPlugin/target/classes/com/example/worldcreator/VoidChunkGenerator.class`

```
[BINARY_FILE:MineLauncher/plugins/WorldCreatorPlugin/target/classes/com/example/worldcreator/VoidChunkGenerator.class]
```

---

## `MineLauncher/plugins/WorldCreatorPlugin/target/classes/com/example/worldcreator/WaterChunkGenerator.class`

```
[BINARY_FILE:MineLauncher/plugins/WorldCreatorPlugin/target/classes/com/example/worldcreator/WaterChunkGenerator.class]
```

---

## `MineLauncher/plugins/WorldCreatorPlugin/target/classes/com/example/worldcreator/WorldCreatorPlugin$1.class`

```
[BINARY_FILE:MineLauncher/plugins/WorldCreatorPlugin/target/classes/com/example/worldcreator/WorldCreatorPlugin$1.class]
```

---

## `MineLauncher/plugins/WorldCreatorPlugin/target/classes/com/example/worldcreator/WorldCreatorPlugin.class`

```
[BINARY_FILE:MineLauncher/plugins/WorldCreatorPlugin/target/classes/com/example/worldcreator/WorldCreatorPlugin.class]
```

---

## `MineLauncher/plugins/WorldCreatorPlugin/target/classes/com/example/worldcreator/WorldTypeOption.class`

```
[BINARY_FILE:MineLauncher/plugins/WorldCreatorPlugin/target/classes/com/example/worldcreator/WorldTypeOption.class]
```

---

## `MineLauncher/plugins/WorldCreatorPlugin/target/classes/plugin.yml`

```
name: WorldCreatorPlugin
main: com.example.worldcreator.WorldCreatorPlugin
version: 1.0.0
author: LauncherTeam
api-version: 1.20
commands:
  worlds:
    description: Open world creator menu
    usage: /worlds

```

---

## `MineLauncher/plugins/WorldCreatorPlugin/target/maven-archiver/pom.properties`

```
artifactId=world-creator-plugin
groupId=com.example
version=1.0.0

```

---

## `MineLauncher/plugins/WorldCreatorPlugin/target/maven-status/maven-compiler-plugin/compile/default-compile/createdFiles.lst`

```
com\example\worldcreator\WorldTypeOption.class
com\example\worldcreator\FixedBiomeProvider.class
com\example\worldcreator\WaterChunkGenerator.class
com\example\worldcreator\WorldCreatorPlugin.class
com\example\worldcreator\WorldCreatorPlugin$1.class
com\example\worldcreator\VoidChunkGenerator.class
com\example\worldcreator\SkyIslandsChunkGenerator.class

```

---

## `MineLauncher/plugins/WorldCreatorPlugin/target/maven-status/maven-compiler-plugin/compile/default-compile/inputFiles.lst`

```
D:\Minecraft_Launcher\CC_MINECRAFT\plugins\WorldCreatorPlugin\src\main\java\com\example\worldcreator\SkyIslandsChunkGenerator.java
D:\Minecraft_Launcher\CC_MINECRAFT\plugins\WorldCreatorPlugin\src\main\java\com\example\worldcreator\WorldCreatorPlugin.java
D:\Minecraft_Launcher\CC_MINECRAFT\plugins\WorldCreatorPlugin\src\main\java\com\example\worldcreator\FixedBiomeProvider.java
D:\Minecraft_Launcher\CC_MINECRAFT\plugins\WorldCreatorPlugin\src\main\java\com\example\worldcreator\VoidChunkGenerator.java
D:\Minecraft_Launcher\CC_MINECRAFT\plugins\WorldCreatorPlugin\src\main\java\com\example\worldcreator\WorldTypeOption.java
D:\Minecraft_Launcher\CC_MINECRAFT\plugins\WorldCreatorPlugin\src\main\java\com\example\worldcreator\WaterChunkGenerator.java

```

---

## `MineLauncher/plugins/WorldCreatorPlugin/target/world-creator-plugin-1.0.0.jar`

```
[BINARY_FILE:MineLauncher/plugins/WorldCreatorPlugin/target/world-creator-plugin-1.0.0.jar]
```

---

