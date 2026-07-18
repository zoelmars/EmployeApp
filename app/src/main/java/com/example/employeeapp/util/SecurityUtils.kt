package com.example.employeeapp.util



import java.io.File

object SecurityUtils {

    private val ROOT_INDICATORS = listOf(
        "/system/app/Superuser.apk",
        "/system/app/SuperSU.apk",
        "/system/bin/su",
        "/system/xbin/su",
        "/sbin/su",
        "/data/local/su",
        "/data/local/bin/su",
        "/system/sd/xbin/su",
        "/system/bin/failsafe/su"
    )

    fun isDeviceRooted(): Boolean {
        for (path in ROOT_INDICATORS) {
            if (File(path).exists()) {
                return true
            }
        }

        return try {
            val process = Runtime.getRuntime().exec(arrayOf("/system/xbin/which", "su"))
            val exitCode = process.waitFor()
            exitCode == 0
        } catch (e: Exception) {
            false
        }
    }
}