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
