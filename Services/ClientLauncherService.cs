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
