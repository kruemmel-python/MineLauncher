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
