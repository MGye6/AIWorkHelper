@echo off
chcp 65001 >nul
echo ========================================
echo AIWorkHelper - 天气查询测试
echo ========================================
echo.

set HOST=http://localhost:8080

echo [测试] 调用 Chat API 查询北京天气
echo URL: %HOST%/v1/chat/chat
echo Body: {"prompts":"今天北京的天气怎么样？","chatType":0}
echo.

curl.exe -X POST "%HOST%/v1/chat/chat" ^
  -H "Content-Type: application/json" ^
  -d "{\"prompts\":\"今天北京的天气怎么样？\",\"chatType\":0}" ^
  --no-progress-meter ^
  | jq .

echo.
echo ========================================
echo 测试完成
echo ========================================
