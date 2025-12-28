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
        try
        {
            return JsonSerializer.Deserialize<LauncherConfig>(json, JsonOptions) ?? new LauncherConfig();
        }
        catch (JsonException)
        {
            return new LauncherConfig();
        }
    }
}
