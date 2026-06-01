package com.gandhinagar.committee.messaging

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast

/** One-tap pre-filled messaging helpers (WhatsApp + SMS). */
object Messaging {

    /** Normalize to a wa.me friendly number. Assumes India (+91) if 10 digits. */
    private fun waNumber(phone: String): String {
        val digits = phone.filter { it.isDigit() }
        return if (digits.length == 10) "91$digits" else digits
    }

    fun sendWhatsApp(context: Context, phone: String, message: String) {
        val url = "https://wa.me/${waNumber(phone)}?text=${Uri.encode(message)}"
        try {
            val i = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(i)
        } catch (e: Exception) {
            Toast.makeText(context, "WhatsApp not available", Toast.LENGTH_SHORT).show()
        }
    }

    fun sendSms(context: Context, phone: String, message: String) {
        try {
            val i = Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:$phone"))
            i.putExtra("sms_body", message)
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(i)
        } catch (e: Exception) {
            Toast.makeText(context, "SMS app not available", Toast.LENGTH_SHORT).show()
        }
    }

    fun interestReminder(memberName: String, monthlyInterest: Double): String =
        "Dear $memberName, this is a reminder from GANDHINAGAR COMMITTEE to pay your " +
        "monthly loan interest of Rs. ${"%.0f".format(monthlyInterest)}. Thank you."

    fun membershipReminder(memberName: String, amountDue: Double): String =
        "Dear $memberName, your GANDHINAGAR COMMITTEE membership of " +
        "Rs. ${"%.0f".format(amountDue)} is pending. Please pay at the earliest. Thank you."
}
