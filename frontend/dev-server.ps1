param(
    [int]$Port = 4173
)

$root = Split-Path -Parent $MyInvocation.MyCommand.Path
$listener = [System.Net.HttpListener]::new()
$listener.Prefixes.Add("http://localhost:$Port/")
$listener.Start()

Write-Host "AstroPI frontend disponible en http://localhost:$Port/"

$contentTypes = @{
    ".html" = "text/html; charset=utf-8"
    ".js" = "application/javascript; charset=utf-8"
    ".css" = "text/css; charset=utf-8"
    ".json" = "application/json; charset=utf-8"
    ".png" = "image/png"
    ".jpg" = "image/jpeg"
    ".jpeg" = "image/jpeg"
    ".svg" = "image/svg+xml"
}

try {
    while ($listener.IsListening) {
        $context = $listener.GetContext()
        $requestPath = $context.Request.Url.AbsolutePath.TrimStart("/")

        if ([string]::IsNullOrWhiteSpace($requestPath)) {
            $requestPath = "index.html"
        }

        $filePath = Join-Path $root $requestPath

        if (-not (Test-Path $filePath)) {
            $filePath = Join-Path $root "index.html"
        }

        try {
            $extension = [System.IO.Path]::GetExtension($filePath).ToLowerInvariant()
            $contentType = $contentTypes[$extension]

            if (-not $contentType) {
                $contentType = "application/octet-stream"
            }

            $bytes = [System.IO.File]::ReadAllBytes($filePath)
            $context.Response.ContentType = $contentType
            $context.Response.ContentLength64 = $bytes.Length
            $context.Response.OutputStream.Write($bytes, 0, $bytes.Length)
            $context.Response.StatusCode = 200
        } catch {
            $errorBytes = [System.Text.Encoding]::UTF8.GetBytes("Error interno del servidor")
            $context.Response.StatusCode = 500
            $context.Response.ContentType = "text/plain; charset=utf-8"
            $context.Response.ContentLength64 = $errorBytes.Length
            $context.Response.OutputStream.Write($errorBytes, 0, $errorBytes.Length)
        } finally {
            $context.Response.OutputStream.Close()
        }
    }
} finally {
    $listener.Stop()
    $listener.Close()
}
