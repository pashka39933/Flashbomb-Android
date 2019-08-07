package com.koszelew.flashbomb.Utils.Networking;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.PowerManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.crashlytics.android.Crashlytics;
import com.koszelew.flashbomb.Activities.MainActivity;
import com.koszelew.flashbomb.Activities.SplashScreenActivity;
import com.koszelew.flashbomb.R;
import com.onesignal.NotificationExtenderService;
import com.onesignal.OSNotificationReceivedResult;
import com.onesignal.OneSignal;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

public class NotificationProcessingHandler extends NotificationExtenderService {

    // Current activity
    public static Activity activity;

    // Help flags
    private boolean CustomNotification = false;
    private boolean KeepNotifications = false;

    // Notification processing callback
    @Override
    protected boolean onNotificationProcessing(final OSNotificationReceivedResult notification) {

        // If not session notification, return
        if (notification.payload.additionalData == null || !notification.payload.additionalData.has("latest_session_id")) {
            CustomNotification = true;
            KeepNotifications = true;
            return false;
        } else {
            ((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE)).cancelAll();
        }

        // Process notification
        Long currentTimestamp = Math.round((double) System.currentTimeMillis() / 1000);
        ProcessNotification(notification, currentTimestamp);

        // Return true to inform that notification is already handled
        return true;

    }

    // Method to further notification processing
    private void ProcessNotification(OSNotificationReceivedResult notification, Long current_timestamp) {

        // Notification construction
        int notificationId = new Random().nextInt(10000) + 10000;
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(getApplicationContext());
        notificationBuilder
                .setContentTitle(getString(R.string.flashtime))
                .setShowWhen(false)
                .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.ic_onesignal_large_icon_default))
                .setSmallIcon(R.drawable.ic_stat_onesignal_default)
                .setAutoCancel(true)
                .setPriority(Notification.PRIORITY_MAX)
                .setOngoing(false);

        // Count all timestamps and time params
        SharedPreferences sharedPref = getSharedPreferences(getString(R.string.PREFERENCES_TAG), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        final Long sessionId = Long.parseLong(notification.payload.additionalData.optString("latest_session_id", "-1"));
        final Long nextSessionId = Long.parseLong(notification.payload.additionalData.optString("next_session_id", "-1"));
        final Long roundedNextSessionId = nextSessionId > 0 ? (nextSessionId - (nextSessionId % 1800) + ((nextSessionId % 1800 < 960) ? 0 : 1800)) : 0;
        long previousSessionId = Long.parseLong(sharedPref.getString(getString(R.string.PREFERENCES_ACTIVATED_SESSION_ID_KEY), "-1"));
        long notificationDelay = current_timestamp - sessionId;
        final String sessionTimeFormatted = new SimpleDateFormat("HH:mm").format(new Date(sessionId * 1000));
        final String roundedNextSessionTimeFormatted = nextSessionId > 0 ? (new SimpleDateFormat("HH:mm").format(new Date(roundedNextSessionId * 1000))) : "";

        // If session is active
        if (sessionId > -1 && sessionId > previousSessionId) {

            // Tutorial hints
            editor.putBoolean(getString(R.string.PREFERENCES_BEST_LEAFLET_ACTIVE), true);

            if (notificationDelay < getResources().getInteger(R.integer.SESSION_TIME_IN_SECONDS)) {
                // Put params in prefs
                editor.putString(getString(R.string.PREFERENCES_ACTIVATED_SESSION_ID_KEY), sessionId.toString());
                Long endTimestamp = current_timestamp + getResources().getInteger(R.integer.SESSION_TIME_IN_SECONDS);
                editor.putString(getString(R.string.PREFERENCES_ACTIVATED_SESSION_ID_END_KEY), endTimestamp.toString());
                editor.commit();

                // Fire start notification
                notificationBuilder
                        .setContentText(sessionTimeFormatted)
                        .setSound(Uri.parse("android.resource://com.koszelew.flashbomb/" + R.raw.onesignal_default_sound))
                        .setVibrate(new long[]{0, 120, 100, 130, 100, 140, 100, 150, 100, 900, 0})
                        .setProgress(getResources().getInteger(R.integer.SESSION_TIME_IN_SECONDS), getResources().getInteger(R.integer.SESSION_TIME_IN_SECONDS), false)
                        .setColor(getResources().getColor(R.color.yellow));
                Intent resultIntent = new Intent(getApplicationContext(), SplashScreenActivity.class);
                resultIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                resultIntent.putExtra("FromNotification", true);
                PendingIntent resultPendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, resultIntent, PendingIntent.FLAG_CANCEL_CURRENT);
                notificationBuilder.setContentIntent(resultPendingIntent);
                ((PowerManager) getSystemService(Context.POWER_SERVICE)).newWakeLock(PowerManager.FULL_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP, "FlashbombWakeLock").acquire(15000);
                ((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE)).notify(notificationId, notificationBuilder.build());

                // If app in focus, mark top bar
                if (notification.isAppInFocus) {
                    activity.runOnUiThread(new Runnable() {
                        public void run() {

                            if (activity.getClass() == MainActivity.class)
                                ((MainActivity) activity).SetSessionActiveState();

                        }
                    });
                    try {
                        Thread.sleep(3000);
                    } catch (Exception e) {
                        Log.e("Flashbomb-Log", "catch", e);
                        Crashlytics.logException(e);
                    }
                    return;
                }

                // Wait and fire end notification
                try {

                    editor.putBoolean(getString(R.string.PREFERENCES_APP_LAUNCHED_KEY), false);
                    editor.commit();

                    boolean vibration = sharedPref.getBoolean(getString(R.string.PREFERENCES_VIBRATION_NOTIFICATIONS), false);

                    Thread.sleep(2000);
                    int progress = getResources().getInteger(R.integer.SESSION_TIME_IN_SECONDS) - 2;
                    while (progress > 0) {

                        if (sharedPref.getBoolean(getString(R.string.PREFERENCES_APP_LAUNCHED_KEY), false)) {
                            ((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE)).cancelAll();
                            return;
                        }

                        Thread.sleep(1000);
                        notificationBuilder
                                .setContentText(sessionTimeFormatted)
                                .setSound(null)
                                .setVibrate(vibration ? new long[]{0, 35, 0} : null)
                                .setPriority(Notification.PRIORITY_DEFAULT)
                                .setProgress(getResources().getInteger(R.integer.SESSION_TIME_IN_SECONDS), --progress, false);
                        ((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE)).notify(notificationId, notificationBuilder.build());
                    }
                    notificationBuilder
                            .setContentTitle(getString(R.string.late_notification_info).replace("{1}", sessionTimeFormatted))
                            .setContentText(nextSessionId < 0 ? getString(R.string.late_notification_next_info_unknown) : getString(R.string.late_notification_next_info).replace("{1}", roundedNextSessionTimeFormatted))
                            .setSound(null)
                            .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.ic_onesignal_large_icon_fail))
                            .setVibrate(new long[]{0, 400, 100, 100, 100, 50, 0})
                            .setLights(0xffff0000, 500, 1000)
                            .setProgress(0, 0, false)
                            .setColor(getResources().getColor(R.color.red_blacklist))
                            .setPriority(Notification.PRIORITY_MAX)
                            .setOngoing(false);
                    ((PowerManager) getSystemService(Context.POWER_SERVICE)).newWakeLock(PowerManager.FULL_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP, "FlashbombWakeLock").acquire(10000);
                    ((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE)).notify(notificationId, notificationBuilder.build());
                    KeepNotifications = true;

                } catch (Exception e) {
                    Log.e("Flashbomb-Log", "catch", e);
                    Crashlytics.logException(e);
                }

            } else {

                // Fire end notification
                notificationBuilder
                        .setContentTitle(getString(R.string.late_notification_info).replace("{1}", sessionTimeFormatted))
                        .setContentText(nextSessionId < 0 ? getString(R.string.late_notification_next_info_unknown) : getString(R.string.late_notification_next_info).replace("{1}", roundedNextSessionTimeFormatted))
                        .setSound(null)
                        .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.ic_onesignal_large_icon_fail))
                        .setVibrate(new long[]{0, 400, 100, 100, 100, 50, 0})
                        .setLights(0xffff0000, 500, 1000)
                        .setColor(getResources().getColor(R.color.red_blacklist))
                        .setOngoing(false);
                ((PowerManager) getSystemService(Context.POWER_SERVICE)).newWakeLock(PowerManager.FULL_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP, "FlashbombWakeLock").acquire(10000);
                ((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE)).notify(notificationId, notificationBuilder.build());
                KeepNotifications = true;

            }

        } else {
            stopSelf();
        }

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (!CustomNotification)
            OneSignal.clearOneSignalNotifications();
        if (!KeepNotifications)
            ((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE)).cancelAll();
    }

}
