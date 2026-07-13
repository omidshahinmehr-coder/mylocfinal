package ir.example.locationsms

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager

class SmsReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Telephony.Sms.Intents.SMS_RECEIVED_ACTION) return

        val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent) ?: return
        if (messages.isEmpty()) return

        val sender = messages[0].originatingAddress ?: return
        // A long SMS can arrive as multiple parts; join them to get the full text
        val fullBody = messages.joinToString(separator = "") { it.messageBody ?: "" }

        if (fullBody.trim().equals("sendloc", ignoreCase = true)) {
            val settings = SettingsRepository(context)
            val allowedNumbers = settings.getAllowedNumbersList()

            // Only reply if the sender is in the whitelist. If the list is
            // empty, nothing is trusted yet, so no reply is sent.
            if (!PhoneUtils.isAllowed(sender, allowedNumbers)) {
                return
            }

            val data = Data.Builder()
                .putString(ReplyLocationWorker.KEY_PHONE, sender)
                .build()

            val request = OneTimeWorkRequestBuilder<ReplyLocationWorker>()
                .setInputData(data)
                .build()

            WorkManager.getInstance(context).enqueue(request)
        }
    }
}
