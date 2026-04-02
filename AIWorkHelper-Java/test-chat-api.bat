@echo off
echo ========================================
echo AIWorkHelper Chat API Test
echo ========================================
echo.

REM 设置主机地址
set HOST=http://localhost:8888

echo [Step 1] Testing login...
echo URL: %HOST%/v1/login
echo Body: {"name":"admin","password":"admin"}
echo.

REM 尝试登录并获取 Token
curl.exe -X POST %HOST%/v1/login -H "Content-Type: application/json" -d "{\"name\":\"admin\",\"password\":\"admin\"}" > login_response.json 2>&1

if exist login_response.json (
    echo Login Response:
    type login_response.json
    echo.
    
    REM 从响应中提取 token（简化处理，假设有 token 字段）
    for /f "tokens=*" %%a in ('type login_response.json ^| findstr "token"') do set LINE=%%a
    
    echo.
    echo [Step 2] Testing weather query...
    echo URL: %HOST%/v1/chat
    echo Body: {"prompts":"今天北京的天气怎么样？","chatType":0}
    echo.
    
    REM 如果有 token，使用 token 调用
    if defined TOKEN (
        curl.exe -X POST %HOST%/v1/chat -H "Content-Type: application/json" -H "Authorization: Bearer %TOKEN%" -d "{\"prompts\":\"今天北京的天气怎么样？\",\"chatType\":0}"
    ) else (
        echo Trying without authentication...
        curl.exe -X POST %HOST%/v1/chat -H "Content-Type: application/json" -d "{\"prompts\":\"今天北京的天气怎么样？\",\"chatType\":0}"
    )
) else (
    echo Login request failed or no response file created
)

echo.
echo ========================================
echo Test completed
echo ========================================
