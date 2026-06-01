# Gandhinagar Committee — Android App

A native Android (Kotlin + Jetpack Compose) app to manage a ~50-member committee:
membership tracking, loans with interest, monthly reminders, and daily Excel backups.

## Features

**Home tab**
- Opening balance and current balance (opening − total open loans).
- Edit opening balance and the per-member loan limit (default ₹10,000, adjustable).
- "Export & Share Excel now" button.

**Members tab**
- Add / edit / delete members.
- Membership = ₹2,000 (editable per member), due date defaults to **May 30** (editable).
- Paid status shown **green (PAID)** / **red (UNPAID)** — tap the chip to toggle.
- If unpaid past the due date, a **10%-per-month penalty** is added automatically and shown.
- One-tap WhatsApp membership reminder.

**Loans tab**
- One member can hold **multiple loans** at once.
- Interest = **10% of principal per month**.
- Warns and blocks if a member's total open loans exceed the loan limit (₹10,000).
- Enter a **closing date** to close a loan; closed loans get **no more reminders**.
- One-tap WhatsApp **and** SMS interest reminders (pre-filled message, you tap send).

**Defaulters tab**
- Lists current-month open-loan defaulters, **sorted by due date**, with each interest due date.
- Overdue dates shown in red. Closed loans are excluded.

**Daily backup**
- A WorkManager job exports all data to an `.xlsx` file **every day at 8:00 PM** (Summary, Members, Loans sheets), saved to the app's external files folder. Use the Home button any time to export and share with committee members.

## Messaging note
You chose **one-tap pre-filled** messaging: the app opens WhatsApp / the SMS app with the
message already typed; you tap send. (Fully silent auto-send isn't allowed for WhatsApp and
is heavily restricted for SMS on modern Android.) Phone numbers are assumed Indian (+91) when
10 digits are entered.

## How to build the APK

1. Install **Android Studio** (Hedgehog or newer).
2. **File → Open** this `GandhinagarCommittee` folder. Android Studio will generate the Gradle
   wrapper and download dependencies on first sync (internet required).
   - If prompted, accept the suggested Android SDK 34 install.
3. To install on a phone: connect it (USB debugging on) and press **Run ▶**.
4. To get a shareable APK: **Build → Build Bundle(s)/APK(s) → Build APK(s)**. The file appears
   in `app/build/outputs/apk/debug/app-debug.apk`. For Play Store / signed release, use
   **Build → Generate Signed Bundle / APK**.

> The Gradle wrapper `.jar` isn't bundled; Android Studio creates it automatically on open.
> From a terminal you can instead run `gradle wrapper` once (if Gradle is installed), then `./gradlew assembleDebug`.

## Tech
Kotlin, Jetpack Compose (Material 3), Room (local on-device storage), WorkManager, Apache POI.
Min SDK 24, target SDK 34. All data is stored locally on the device.
