#!/bin/bash

# Vibtime Release Build Script
# 用於生成發布版本的腳本

echo "🚀 開始建置 Vibtime 發布版本..."

# 檢查是否在正確的目錄
if [ ! -f "app/build.gradle.kts" ]; then
    echo "❌ 錯誤：請在專案根目錄執行此腳本"
    exit 1
fi

# 檢查是否有 keystore.properties 檔案
if [ ! -f "keystore.properties" ]; then
    echo "⚠️  警告：未找到 keystore.properties 檔案，將使用 debug 簽名"
    echo "   正式發布前請建立 keystore.properties 檔案並配置簽名金鑰"
fi

# 清理之前的建置
echo "🧹 清理之前的建置..."
./gradlew clean

# 建置 Release APK
echo "📱 建置 Release APK..."
./gradlew assembleRelease

# 檢查 APK 是否建置成功
if [ -f "app/build/outputs/apk/release/app-release.apk" ]; then
    echo "✅ Release APK 建置成功：app/build/outputs/apk/release/app-release.apk"
    
    # 顯示 APK 資訊
    echo "📊 APK 資訊："
    ls -lh "app/build/outputs/apk/release/app-release.apk"
else
    echo "❌ Release APK 建置失敗"
    exit 1
fi

# 建置 Release AAB (Android App Bundle)
echo "📦 建置 Release AAB..."
./gradlew bundleRelease

# 檢查 AAB 是否建置成功
if [ -f "app/build/outputs/bundle/release/app-release.aab" ]; then
    echo "✅ Release AAB 建置成功：app/build/outputs/bundle/release/app-release.aab"
    
    # 顯示 AAB 資訊
    echo "📊 AAB 資訊："
    ls -lh "app/build/outputs/bundle/release/app-release.aab"
else
    echo "❌ Release AAB 建置失敗"
    exit 1
fi

echo ""
echo "🎉 發布版本建置完成！"
echo ""
echo "📁 檔案位置："
echo "   APK: app/build/outputs/apk/release/app-release.apk"
echo "   AAB: app/build/outputs/bundle/release/app-release.aab"
echo ""
echo "📝 注意事項："
echo "   - AAB 檔案用於 Google Play Store 發布"
echo "   - APK 檔案用於直接安裝或第三方商店"
echo "   - 請測試發布版本確保功能正常"
echo "   - 建議使用 AAB 格式發布到 Google Play Store"
