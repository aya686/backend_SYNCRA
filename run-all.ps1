# run-all.ps1

$base = "C:\Users\Tybo\OneDrive - ESPRIT\Bureau\syncra_ms2\MS2"

$services = @(

    @{name="Eureka";    path="eurika_server_taib"},

    @{name="Projets";   path="projets"},

    @{name="GesUser";   path="gesuser"},

    @{name="Ecommerce"; path="ecommerce"},

    @{name="Wellbeing"; path="wellbeing"},

    @{name="Hichem";    path="hichem"},

    @{name="Events";    path="events"},

    @{name="PI_Taib";    path="pi_taib"},

    @{name="Gateway";   path="gateway"}

)

foreach ($service in $services) {

    $fullPath = "$base\$($service.path)"

    Write-Host "Starting $($service.name)..." -ForegroundColor Green

    Start-Process powershell -ArgumentList "-NoExit", "-Command", "cd '$fullPath'; mvn spring-boot:run"

    

    # Attendre 10s après Eureka pour qu'il soit UP avant les autres

    if ($service.name -eq "Eureka") {

        Write-Host "Waiting for Eureka to start..." -ForegroundColor Yellow

        Start-Sleep -Seconds 15

    } else {

        Start-Sleep -Seconds 3

    }

}

Write-Host "All services started! Check http://localhost:8761" -ForegroundColor Cyan