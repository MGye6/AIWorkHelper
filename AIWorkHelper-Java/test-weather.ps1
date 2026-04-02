# Test Gaode Weather API
$apiKey = "5b5abc7dabdaeb801a9428bc89a21854"
$city = "北京"
$url = "https://restapi.amap.com/v3/weather/weatherInfo?key=$apiKey&city=$city&extensions=base"

Write-Host "Querying weather for $city..." -ForegroundColor Green
$response = Invoke-RestMethod -Uri $url -Method Get

if ($response.status -eq "1") {
    Write-Host "`nWeather Query Success!" -ForegroundColor Cyan
    if ($response.lives -and $response.lives.Count -gt 0) {
        $weather = $response.lives[0]
        Write-Host "Province: $($weather.province)"
        Write-Host "City: $($weather.city)"
        Write-Host "Weather: $($weather.weather)"
        Write-Host "Temperature: $($weather.temperature)C"
        Write-Host "Wind Direction: $($weather.winddirection)"
        Write-Host "Wind Power: $($weather.windpower) level"
        Write-Host "Humidity: $($weather.humidity)%"
    } else {
        Write-Host "No weather data available"
    }
} else {
    Write-Host "`nWeather Query Failed!" -ForegroundColor Red
    Write-Host "Error Info: $($response.info)"
    Write-Host "Error Code: $($response.infocode)"
}
