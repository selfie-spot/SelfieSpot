package com.codepath.selfiespot.services;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;

import com.codepath.selfiespot.R;
import com.codepath.selfiespot.SelfieSpotApplication;
import com.codepath.selfiespot.activities.SelfieSpotsMapActivity;
import com.codepath.selfiespot.activities.TempDetailSelfieSpotActivity;
import com.codepath.selfiespot.models.SelfieSpot;
import com.codepath.selfiespot.receivers.TakePictureBroadcastReceiver;
import com.google.android.gms.location.GeofencingEvent;
import com.parse.ParseException;

import java.util.Date;

import javax.inject.Inject;

/**
 *  IntentService for handling incoming intents that are generated as a result of requesting
 *  activity updates using
 *  {@link com.google.android.gms.location.LocationServices#GeofencingApi#addGeofences}.
 */
public class GeofenceTransitionsIntentService extends IntentService {
    private static final String TAG = GeofenceTransitionsIntentService.class.getSimpleName();
    private static final long MIN_NOTIFICATION_INTERVAL_IN_MILLIS = 24 * 60 * 60 * 1000; // 24 hours

    @Inject
    SharedPreferences mSharedPreferences;

    @Override
    public void onCreate() {
        super.onCreate();
        SelfieSpotApplication.from(this).getComponent().inject(this);
    }

    public GeofenceTransitionsIntentService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(final Intent intent) {
        final GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);
        if (geofencingEvent.hasError()) {
            final String errorMessage = "Geofence event error: " + geofencingEvent.getErrorCode();;
            Log.e(TAG, errorMessage);
            return;
        }

        final String selfieSpotId = geofencingEvent.getTriggeringGeofences().get(0).getRequestId();
        validateAndSendNotification(selfieSpotId);
    }

    private void validateAndSendNotification(final String selfieSpotId) {
        try {
            final SelfieSpot selfieSpot = SelfieSpot.getQuery().get(selfieSpotId);
            if (selfieSpot == null) {
                Log.d(TAG, "Unable to retrieve SelfieSpot: " + selfieSpotId);
                return;
            }
            if (canSendNotification(selfieSpotId)) {
                sendNotification(selfieSpotId, selfieSpot);
                setLastNotificationTimeForSelfieSpot(selfieSpotId);
            } else {
                final Date date = getLastNotificationTimeForSelfieSpot(selfieSpotId);
                final String dateString;
                if (date != null) {
                    dateString = date.toString();
                } else {
                    dateString = "N/A";
                }
                Log.d(TAG, "NO Notification sent, last notification sent for " + selfieSpotId + "; at " + dateString);
            }
        } catch (final ParseException e) {
            Log.d(TAG, "Unable to retrieve SelfieSpot: " + selfieSpotId, e);
        }
    }

    private void sendNotification(final String selfieSpotId, final SelfieSpot selfieSpot) {
        final Intent resultIntent = TempDetailSelfieSpotActivity.createIntent(this, selfieSpotId);
        final TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addParentStack(SelfieSpotsMapActivity.class);
        stackBuilder.addNextIntent(resultIntent);
        final PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

        final int requestId = createRequestId();
        final Intent takePictureIntent = TakePictureBroadcastReceiver.createIntent(requestId);
        final PendingIntent takePicturePendingIntent = PendingIntent.getBroadcast(this, 0, takePictureIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        final NotificationCompat.Builder builder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.ic_launcher)
                        .setContentTitle("Time for a Selfie!")
                        .setStyle(new NotificationCompat.BigTextStyle().bigText(getNotificationText(selfieSpot)))
                        .setContentIntent(resultPendingIntent)
                        .setAutoCancel(true)
                        .setDefaults(Notification.DEFAULT_ALL)
                        .addAction(android.R.drawable.ic_menu_camera, "Take Selfie", takePicturePendingIntent);

        final NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        Log.d(TAG, "Sending notification: " + requestId);
        notificationManager.notify(requestId, builder.build());
    }

    private boolean canSendNotification(final String selfieSpotId) {
        final Date lastNotifiedForSelfieSpot = getLastNotificationTimeForSelfieSpot(selfieSpotId);
        final long now = System.currentTimeMillis();

        return (lastNotifiedForSelfieSpot == null || (now - lastNotifiedForSelfieSpot.getTime() >= MIN_NOTIFICATION_INTERVAL_IN_MILLIS));
    }

    private void setLastNotificationTimeForSelfieSpot(final String selfieSpotId) {
        final long now = System.currentTimeMillis();
        mSharedPreferences.edit().putLong(getKeyForLastNotificationTime(selfieSpotId), now).commit();
    }

    private Date getLastNotificationTimeForSelfieSpot(final String selfieSpotId) {
        final Long lastNotifiedInMillis = mSharedPreferences.getLong(getKeyForLastNotificationTime(selfieSpotId), -1);
        if (lastNotifiedInMillis == -1) {
            return  null;
        }
        return new Date(lastNotifiedInMillis);
    }

    private String getKeyForLastNotificationTime(final String selfieSpotId) {
        return selfieSpotId + ":LAST_NOTIFICATION_IN_MILLIS";
    }

    private String getNotificationText(final SelfieSpot selfieSpot) {
        final String notificationText = "You are near one of your favorite SelfieSpot: " + selfieSpot.getName();
        return notificationText;
    }

    private int createRequestId(){
        int requestId = (int) System.currentTimeMillis();

        if (requestId < 0) {
            requestId = - requestId;
        }

        return requestId;
    }
}
