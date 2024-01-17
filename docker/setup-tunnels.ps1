# Execute the cleanAndStartDocker.ps1 script with bypassed execution policy
# powershell -ExecutionPolicy Bypass -File .\setup-tunnels.ps1

Get-WmiObject Win32_Process | Where-Object {$_.CommandLine -like "*ssh*"} | Select-Object CommandLine

$sshProcesses = Get-Process | Where-Object {$_.ProcessName -like "*ssh*"}
foreach ($proc in $sshProcesses) {
    Write-Host "Zatrzymywanie procesu SSH o ID: $($proc.Id)"
    Stop-Process -Id $proc.Id -Force
}

# Definiuj bazowe parametry SSH
$sshCommand = "ssh -N -f "

# Dodaj przekierowania portów do polecenia
$portForwarding = ""
foreach ($port in 3001..3006) {
    $portForwarding += "-L $($port):172.20.73.4:$($port) "
}

# Końcowe połączenie SSH
$sshCommand += $portForwarding + "9szkaradek@taurus.fis.agh.edu.pl"

# Wykonaj komendę
Write-Host "Uruchamianie tuneli SSH dla portów od 3001 do 3006..."
Invoke-Expression $sshCommand

# Wyświetl komunikat
Write-Host "Wszystkie tunele zostały utworzone."