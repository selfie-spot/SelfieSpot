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
import com.google.android.gms.location.GeofencingEvent;
import com.parse.GetCallback;
import com.parse.ParseException;

import java.util.Date;
import java.util.concurrent.atomic.AtomicInteger;

import javax.inject.Inject;

/**
 *  IntentService for handling incoming intents that are generated as a result of requesting
 *  activity updates using
 *  {@link com.google.android.gms.location.LocationServices#GeofencingApi#addGeofences}.
 */
public class GeofenceTransitionsIntentService extends IntentService {
    private static final String TAG = GeofenceTransitionsIntentService.class.getSimpleName();
    private static final long MIN_NOTIFICATION_INTERVAL_IN_MILLIS = 24 * 60 * 60 * 1000; // 24 hours

    // TODO - find a way to generate this uniquely
    private static AtomicInteger sNotificationId = new AtomicInteger(0);

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
        SelfieSpot.getQuery().getInBackground(selfieSpotId, new GetCallback<SelfieSpot>() {
            @Override
            public void done(final SelfieSpot selfieSpot, final ParseException e) {
                if (e != null || selfieSpot == null) {
                    Log.d(TAG, "Unable to retrieve SelfieSpot: " + selfieSpotId, e);
                } else {
                    if (canSendNotification(selfieSpotId)) {
                        sendNotification(selfieSpotId, selfieSpot);
                    } else {
                        final Date date = getLastNotificationTimeForSelfieSpot(selfieSpotId);
                        final String dateString;
                        if (date != null) {
                            dateString = date.toString();
                        } else {
                            dateString = "N/A";
                        }
                        Log.d(TAG, "Last notification sent to: " + selfieSpotId + "; at: " + dateString);
                    }
                }
            }
        });
    }

    private void sendNotification(final String selfieSpotId, final SelfieSpot selfieSpot) {
        final Intent resultIntent = TempDetailSelfieSpotActivity.createIntent(this, selfieSpotId);
        final TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addParentStack(SelfieSpotsMapActivity.class);
        stackBuilder.addNextIntent(resultIntent);
        final PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

        final NotificationCompat.Builder builder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.ic_launcher)
                        .setContentTitle("Time for a Selfie!")
                        .setContentText(getNotificationText(selfieSpot))
                        .setContentIntent(resultPendingIntent)
                        .setAutoCancel(true)
                        .setDefaults(Notification.DEFAULT_ALL);

        final NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        // TODO - use the truncated system millis as notification id?
        mNotificationManager.notify(sNotificationId.incrementAndGet(), builder.build());
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
}
