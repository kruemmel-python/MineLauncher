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
