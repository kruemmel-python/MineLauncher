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
