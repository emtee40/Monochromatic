package uk.co.richyhbm.monochromatic.Utilities

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.SharedPreferences
import android.os.BatteryManager
import android.preference.PreferenceManager
import uk.co.richyhbm.monochromatic.R
import java.util.*


class Settings(val context: Context) {
    private val settings = PreferenceManager.getDefaultSharedPreferences(context)

    private fun getBoolean(keyId: Int, defaultValue: Boolean) = settings.getBoolean(context.getString(keyId), defaultValue)

    private fun getIntValue(keyId: Int, defaultValue: Int): Int {
        return try {
            settings.getInt(context.getString(keyId), defaultValue)
        }catch (e: Exception) {
            defaultValue
        }
    }

    private fun setBoolean(keyId: Int, value: Boolean) {
        settings.edit()
            .putBoolean(context.getString(keyId), value)
            .apply()
    }

    private fun setInt(keyId: Int, value: Int) {
        settings.edit()
            .putInt(context.getString(keyId), value)
            .apply()
    }

    fun registerPreferenceChangeListener(listener: SharedPreferences.OnSharedPreferenceChangeListener) {
        settings.registerOnSharedPreferenceChangeListener(listener)
    }

    fun isEnabled() = getBoolean(R.string.settings_key_monochromatic_enabled, false) && Permissions.hasSecureSettingsPermission(context)

    fun setEnabled(b: Boolean) {
        setBoolean(R.string.settings_key_monochromatic_enabled, b)
    }

    private fun isAlwaysOn() = getBoolean(R.string.settings_key_always_on, false)

    fun shouldDisableOnScreenOff() = getBoolean(R.string.settings_key_disable_with_screen_off, false)

    fun shouldEnableAtTime() = getBoolean(R.string.settings_key_enable_with_time, false)

    fun setEnableTime(minutesAfterMidnight: Int) {
        return setInt(R.string.settings_key_enable_time, minutesAfterMidnight)
    }

    fun getEnableTime() = getIntValue(R.string.settings_key_enable_time, 0)

    fun setDisableTime(minutesAfterMidnight: Int) {
        return setInt(R.string.settings_key_disable_time, minutesAfterMidnight)
    }

    fun getDisableTime() = getIntValue(R.string.settings_key_disable_time, 0)

    private fun isNowInEnabledTime(): Boolean {
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)
        val nowTime = hour * 60 + minute
        return if(getDisableTime() < getEnableTime())
            getEnableTime() < nowTime || nowTime < getDisableTime()
        else
            getEnableTime() < nowTime && nowTime < getDisableTime()
    }

    private fun isTimeAllowed(): Boolean = shouldEnableAtTime() && isNowInEnabledTime()

    fun shouldEnableAtLowBattery() = getBoolean(R.string.settings_key_enable_with_low_battery, false)

    fun getLowBatteryLevel() = getIntValue(R.string.settings_key_enable_with_low_battery_amount, 15)

    fun setLowBatteryLevel(amount: Int) {
        setInt(R.string.settings_key_enable_with_low_battery_amount, amount)
    }

    private fun getBatteryLevel(): Int {
        val batteryIntent = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED)) ?: return 50

        val level: Int = batteryIntent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
        val scale: Int = batteryIntent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)

        return if (level == -1 || scale == -1) 50 else (level.toFloat() / scale.toFloat() * 100.0f).toInt()
    }

    private fun isBatteryAllowed(): Boolean = shouldEnableAtLowBattery() && (getBatteryLevel() <= getLowBatteryLevel())

    fun isAllowed(): Boolean = isAlwaysOn() || isTimeAllowed() || isBatteryAllowed()

    fun setSeenNotificationDialog() {
        setBoolean(R.string.settings_key_show_notification_dialog, true)
    }

    fun seenNotificationDialog() =
        getBoolean(R.string.settings_key_show_notification_dialog, false)

}