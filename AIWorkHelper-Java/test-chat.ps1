# 测试 AI 聊天接口 - 天气查询
$host = "http://localhost:8888"
$url = "$host/v1/chat"

# 由于需要 JWT Token，我们先尝试直接调用（如果接口需要认证会返回错误）
$headers = @{
    "Content-Type" = "application/json"
}

# 测试查询北京天气
$body = @{
    prompts = "今天北京的天气怎么样？"
    chatType = 0
    relationId = ""
    startTime = $null
    endTime = $null
} | ConvertTo-Json

Write-Host "正在调用 AI 聊天接口查询天气..." -ForegroundColor Green
Write-Host "请求 URL: $url"
Write-Host "请求内容：$body`n"

try {
    $response = Invoke-RestMethod -Uri $url -Method Post -Headers $headers -Body $body
    
    Write-Host "`n【响应成功】" -ForegroundColor Cyan
    Write-Host "状态码：200"
    Write-Host "响应内容:"
    $response | ConvertTo-Json -Depth 10 | Write-Host
    
} catch {
    $errorResponse = $_.Exception.Response
    $statusCode = [int]$errorResponse.StatusCode
    
    Write-Host "`n【请求失败】" -ForegroundColor Red
    Write-Host "状态码：$statusCode"
    
    if ($statusCode -eq 401) {
        Write-Host "错误：未授权访问 - 需要先登录获取 JWT Token" -ForegroundColor Yellow
        Write-Host "`n请先调用登录接口：" -ForegroundColor Yellow
        Write-Host "POST $host/v1/login" -ForegroundColor Yellow
        Write-Host "Body: { `"name`": `"admin`", `"password`": `"admin`" }" -ForegroundColor Yellow
    } elseif ($statusCode -eq 404) {
        Write-Host "错误：接口不存在" -ForegroundColor Yellow
    } else {
        Write-Host "错误信息：$($errorResponse.StatusDescription)"
    }
}
