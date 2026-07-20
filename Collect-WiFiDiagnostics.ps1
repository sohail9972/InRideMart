# ===========================================================
# Fix-Intel9560.ps1
# Safe repair for Intel Wireless-AC 9560 (Code 10 / Netwtw08)
# ===========================================================

# Requires Administrator
if (-not ([Security.Principal.WindowsPrincipal] `
    [Security.Principal.WindowsIdentity]::GetCurrent()).IsInRole(
    [Security.Principal.WindowsBuiltInRole]::Administrator))
{
    Write-Host "Run PowerShell as Administrator." -ForegroundColor Red
    exit
}

Write-Host ""
Write-Host "==========================================="
Write-Host " Intel Wireless Driver Recovery Tool"
Write-Host "==========================================="
Write-Host ""

# ----------------------------------------------------------
# Create Restore Point
# ----------------------------------------------------------

Write-Host "Creating Restore Point..."

try {
    Enable-ComputerRestore -Drive "C:\" -ErrorAction SilentlyContinue
    Checkpoint-Computer -Description "Before Intel WiFi Repair" `
        -RestorePointType MODIFY_SETTINGS
}
catch {
    Write-Warning "Restore point could not be created."
}

# ----------------------------------------------------------
# Backup Registry
# ----------------------------------------------------------

$backup = "$env:USERPROFILE\Desktop\WiFi_Backup"

if (!(Test-Path $backup))
{
    New-Item $backup -ItemType Directory | Out-Null
}

reg export `
"HKLM\SYSTEM\CurrentControlSet\Services\Netwtw08" `
"$backup\Netwtw08.reg" /y

reg export `
"HKLM\SYSTEM\CurrentControlSet\Control\Class\{4d36e972-e325-11ce-bfc1-08002be10318}" `
"$backup\NetworkClass.reg" /y

Write-Host "Registry backed up."

# ----------------------------------------------------------
# Stop WLAN Services
# ----------------------------------------------------------

Write-Host "Stopping WLAN Service..."

Stop-Service WlanSvc -Force -ErrorAction SilentlyContinue

# ----------------------------------------------------------
# Disable Adapter
# ----------------------------------------------------------

Write-Host "Disabling Intel WiFi..."

Disable-PnpDevice `
-InstanceId "PCI\VEN_8086&DEV_A370&SUBSYS_00348086&REV_10\3&11583659&0&A3" `
-Confirm:$false `
-ErrorAction SilentlyContinue

Start-Sleep 3

# ----------------------------------------------------------
# Remove Device
# ----------------------------------------------------------

Write-Host "Removing Device..."

pnputil /remove-device `
"PCI\VEN_8086&DEV_A370&SUBSYS_00348086&REV_10\3&11583659&0&A3"

Start-Sleep 5

# ----------------------------------------------------------
# Rescan Hardware
# ----------------------------------------------------------

Write-Host "Scanning for hardware..."

pnputil /scan-devices

Start-Sleep 5

# ----------------------------------------------------------
# Reset Network Stack
# ----------------------------------------------------------

Write-Host "Resetting Winsock..."

netsh winsock reset

Write-Host "Resetting TCP/IP..."

netsh int ip reset

Write-Host "Resetting Firewall..."

netsh advfirewall reset

Write-Host "Resetting Network..."

netcfg -d

# ----------------------------------------------------------
# Restart WLAN
# ----------------------------------------------------------

Start-Service WlanSvc

# ----------------------------------------------------------
# Enable Adapter
# ----------------------------------------------------------

Enable-PnpDevice `
-InstanceId "PCI\VEN_8086&DEV_A370&SUBSYS_00348086&REV_10\3&11583659&0&A3" `
-Confirm:$false `
-ErrorAction SilentlyContinue

# ----------------------------------------------------------
# Final Status
# ----------------------------------------------------------

Write-Host ""
Write-Host "Current Device Status"
Write-Host "----------------------"

Get-PnpDevice -Class Net |
Where-Object FriendlyName -like "*Intel*" |
Format-Table FriendlyName,Status

Write-Host ""
Write-Host "Driver Information"
Write-Host "----------------------"

Get-CimInstance Win32_PnPSignedDriver |
Where-Object DeviceName -like "*9560*" |
Select DeviceName,
DriverVersion,
DriverDate,
InfName |
Format-Table

Write-Host ""
Write-Host "Done."
Write-Host "Please REBOOT the computer."