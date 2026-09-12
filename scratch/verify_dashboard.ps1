$loginBody = @{
    email = "admin@ecommerce.com"
    password = "password123"
} | ConvertTo-Json

$loginResponse = Invoke-RestMethod -Uri "http://127.0.0.1:8080/api/v1/auth/login" -Method Post -ContentType "application/json" -Body $loginBody
$token = $loginResponse.data.token
Write-Host "Admin login successful. Token acquired."

$headers = @{
    Authorization = "Bearer $token"
}

Write-Host "`n=== 1. All-Time Dashboard Statistics ==="
$stats = Invoke-RestMethod -Uri "http://127.0.0.1:8080/api/v1/admin/statistics/dashboard" -Method Get -Headers $headers
$stats.data | ConvertTo-Json -Depth 5

Write-Host "`n=== 2. Date Filtered Dashboard Statistics (Past 7 Days) ==="
$today = (Get-Date).ToString("yyyy-MM-dd")
$past7 = (Get-Date).AddDays(-7).ToString("yyyy-MM-dd")
$filteredStats = Invoke-RestMethod -Uri "http://127.0.0.1:8080/api/v1/admin/statistics/dashboard?startDate=$past7&endDate=$today" -Method Get -Headers $headers
$filteredStats.data | ConvertTo-Json -Depth 5
