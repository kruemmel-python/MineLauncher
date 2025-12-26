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
