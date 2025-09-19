package com.example.vibtime.utils

import org.junit.Test
import org.junit.Assert.*

class ServiceManagerTest {

    @Test
    fun testServiceManagerExists() {
        // 基本測試：確保 ServiceManager 類存在且可以訪問
        assertNotNull(ServiceManager)
    }

    @Test
    fun testNotificationHelperExists() {
        // 基本測試：確保 NotificationHelper 類存在且可以訪問
        assertNotNull(NotificationHelper)
    }

    @Test
    fun testVibrationWorkManagerExists() {
        // 基本測試：確保 VibrationWorkManager 類存在且可以訪問
        assertNotNull(VibrationWorkManager)
    }

    @Test
    fun testExactAlarmManagerExists() {
        // 基本測試：確保 ExactAlarmManager 類存在且可以訪問
        assertNotNull(ExactAlarmManager)
    }
}
