package ir.example.locationsms

import android.content.Context
import android.util.Base64

class SettingsRepository(context: Context) {
    private val prefs = context.getSharedPreferences("location_sms_prefs", Context.MODE_PRIVATE)

    fun savePhoneNumber(phone: String) {
        prefs.edit().putString(KEY_PHONE, phone).apply()
    }

    fun getPhoneNumber(): String? = prefs.getString(KEY_PHONE, null)

    fun saveIntervalMinutes(minutes: Long) {
        prefs.edit().putLong(KEY_INTERVAL, minutes).apply()
    }

    fun getIntervalMinutes(): Long = prefs.getLong(KEY_INTERVAL, 60L)

    fun saveAutoSendEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_ENABLED, enabled).apply()
    }

    fun isAutoSendEnabled(): Boolean = prefs.getBoolean(KEY_ENABLED, false)

    // --- Allowed sender numbers (whitelist for "sendloc") ---

    fun saveAllowedNumbersRaw(raw: String) {
        prefs.edit().putString(KEY_ALLOWED, raw).apply()
    }

    fun getAllowedNumbersRaw(): String = prefs.getString(KEY_ALLOWED, "") ?: ""

    fun getAllowedNumbersList(): List<String> =
        getAllowedNumbersRaw()
            .split(",", "،", "\n")
            .map { it.trim() }
            .filter { it.isNotEmpty() }

    // --- App-open password ---

    fun isPasswordSet(): Boolean = prefs.contains(KEY_PASS_HASH)

    fun setPassword(password: String) {
        val salt = PasswordUtils.generateSalt()
        val hash = PasswordUtils.hash(password, salt)
        prefs.edit()
            .putString(KEY_PASS_SALT, Base64.encodeToString(salt, Base64.NO_WRAP))
            .putString(KEY_PASS_HASH, hash)
            .apply()
    }

    fun verifyPassword(password: String): Boolean {
        val saltB64 = prefs.getString(KEY_PASS_SALT, null) ?: return false
        val storedHash = prefs.getString(KEY_PASS_HASH, null) ?: return false
        val salt = Base64.decode(saltB64, Base64.NO_WRAP)
        val hash = PasswordUtils.hash(password, salt)
        return hash == storedHash
    }

    companion object {
        private const val KEY_PHONE = "phone_number"
        private const val KEY_INTERVAL = "interval_minutes"
        private const val KEY_ENABLED = "auto_send_enabled"
        private const val KEY_ALLOWED = "allowed_numbers"
        private const val KEY_PASS_SALT = "pass_salt"
        private const val KEY_PASS_HASH = "pass_hash"
    }
}
