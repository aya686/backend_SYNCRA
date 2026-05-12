$base = "C:\Users\Tybo\OneDrive - ESPRIT\Bureau\syncra_ms2\MS2"
$src = "$base\projets\.github\workflows\deploy.yml"

# events
Copy-Item $src "$base\events\.github\workflows\deploy.yml" -Force
Set-Location "$base\events"
git add .github; git commit -m "fix CI/CD"; git push origin events_back

# gesuser
Copy-Item $src "$base\gesuser\.github\workflows\deploy.yml" -Force
Set-Location "$base\gesuser"
git add .github; git commit -m "fix CI/CD"; git push origin GesUserBack

# ecommerce
Copy-Item $src "$base\ecommerce\.github\workflows\deploy.yml" -Force
Set-Location "$base\ecommerce"
git add .github; git commit -m "fix CI/CD"; git push origin e-commerce

# hichem
Copy-Item $src "$base\hichem\.github\workflows\deploy.yml" -Force
Set-Location "$base\hichem"
git add .github; git commit -m "fix CI/CD"; git push origin hichemBack

# wellbeing
Copy-Item $src "$base\wellbeing\.github\workflows\deploy.yml" -Force
Set-Location "$base\wellbeing"
git add .github; git commit -m "fix CI/CD"; git push origin wellbeing_back

# gateway
Copy-Item $src "$base\gateway\.github\workflows\deploy.yml" -Force
Set-Location "$base\gateway"
git add .github; git commit -m "fix CI/CD"; git push origin api-getway

# eureka
Copy-Item $src "$base\eurika_server_taib\.github\workflows\deploy.yml" -Force
Set-Location "$base\eurika_server_taib"
git add .github; git commit -m "fix CI/CD"; git push origin eurika_server_taib

# collab
Copy-Item $src "$base\collab\.github\workflows\deploy.yml" -Force
Set-Location "$base\collab"
git add .github; git commit -m "fix CI/CD"; git push origin PI_Taib