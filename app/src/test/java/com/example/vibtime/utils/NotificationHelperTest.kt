package com.example.vibtime.utils

import org.junit.Test
import org.junit.Assert.*

class NotificationHelperTest {

    @Test
    fun testNotificationHelperExists() {
        // 基本測試：確保 NotificationHelper 類存在且可以訪問
        assertNotNull(NotificationHelper)
    }

    @Test
    fun testNotificationHelperConstants() {
        // 測試常量定義
        assertNotNull(NotificationHelper.CHANNEL_ID_VIBRATION_SERVICE)
        assertNotNull(NotificationHelper.CHANNEL_ID_GENERAL)
        assertNotNull(NotificationHelper.CHANNEL_ID_ALERTS)
    }
}
