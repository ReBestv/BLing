param(
    [string]$ProjectRoot = (Resolve-Path (Join-Path $PSScriptRoot "..")).Path,
    [int]$JpegQuality = 95,
    [switch]$Force
)

$ErrorActionPreference = "Stop"

$drawableDir = Join-Path $ProjectRoot "app/src/main/res/drawable-nodpi"
$backupDir = Join-Path $ProjectRoot "app/src/main/res_backup"
$sourcePng = Join-Path $drawableDir "ic_launcher_foreground.png"
$targetJpg = Join-Path $drawableDir "ic_launcher_foreground.jpg"
$backupFile = Join-Path $backupDir ("ic_launcher_foreground.png.bak-{0:yyyyMMdd-HHmmss}" -f (Get-Date))

function Get-ImageKind {
    param([string]$Path)

    $stream = [System.IO.File]::OpenRead($Path)
    try {
        $bytes = New-Object byte[] 8
        [void]$stream.Read($bytes, 0, $bytes.Length)
    } finally {
        $stream.Dispose()
    }

    if ($bytes[0] -eq 0xFF -and $bytes[1] -eq 0xD8 -and $bytes[2] -eq 0xFF) {
        return "jpeg"
    }

    if (
        $bytes[0] -eq 0x89 -and
        $bytes[1] -eq 0x50 -and
        $bytes[2] -eq 0x4E -and
        $bytes[3] -eq 0x47
    ) {
        return "png"
    }

    return "unknown"
}

function Convert-PngToJpeg {
    param(
        [string]$Source,
        [string]$Target,
        [int]$Quality
    )

    Add-Type -AssemblyName System.Drawing

    $image = [System.Drawing.Image]::FromFile($Source)
    try {
        $codec = [System.Drawing.Imaging.ImageCodecInfo]::GetImageEncoders() |
            Where-Object { $_.MimeType -eq "image/jpeg" } |
            Select-Object -First 1

        if ($null -eq $codec) {
            throw "JPEG encoder was not found."
        }

        $qualityParam = New-Object System.Drawing.Imaging.EncoderParameter(
            [System.Drawing.Imaging.Encoder]::Quality,
            [int64]$Quality
        )
        $encoderParams = New-Object System.Drawing.Imaging.EncoderParameters(1)
        $encoderParams.Param[0] = $qualityParam

        $image.Save($Target, $codec, $encoderParams)
    } finally {
        $image.Dispose()
    }
}

if (!(Test-Path -LiteralPath $drawableDir)) {
    throw "Drawable directory not found: $drawableDir"
}

if (!(Test-Path -LiteralPath $backupDir)) {
    New-Item -ItemType Directory -Path $backupDir | Out-Null
}

if (!(Test-Path -LiteralPath $sourcePng)) {
    if (Test-Path -LiteralPath $targetJpg) {
        Write-Host "Launcher foreground is already normalized:"
        Write-Host $targetJpg
        exit 0
    }

    throw "Source image not found: $sourcePng"
}

if ((Test-Path -LiteralPath $targetJpg) -and !$Force) {
    throw "Target already exists: $targetJpg. Re-run with -Force to replace it."
}

$kind = Get-ImageKind -Path $sourcePng
Copy-Item -LiteralPath $sourcePng -Destination $backupFile

if ($kind -eq "jpeg") {
    if (Test-Path -LiteralPath $targetJpg) {
        Remove-Item -LiteralPath $targetJpg -Force
    }

    Move-Item -LiteralPath $sourcePng -Destination $targetJpg
    Write-Host "Renamed JPEG bytes from .png to .jpg."
} elseif ($kind -eq "png") {
    Convert-PngToJpeg -Source $sourcePng -Target $targetJpg -Quality $JpegQuality
    Remove-Item -LiteralPath $sourcePng -Force
    Write-Host "Converted PNG to JPEG."
} else {
    throw "Unsupported image format. Backup was written to: $backupFile"
}

$newKind = Get-ImageKind -Path $targetJpg
if ($newKind -ne "jpeg") {
    throw "Output verification failed: $targetJpg is not JPEG."
}

Write-Host "Output:"
Write-Host $targetJpg
Write-Host "Backup:"
Write-Host $backupFile
Write-Host "No XML update is needed because @drawable/ic_launcher_foreground resolves by resource name."
