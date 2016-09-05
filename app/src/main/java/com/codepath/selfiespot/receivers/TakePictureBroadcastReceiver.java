package com.codepath.selfiespot.receivers;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.provider.MediaStore;
import android.util.Log;

// {@link BroadcastReceiver} subclass to open camera and cancel the notification
public class TakePictureBroadcastReceiver extends BroadcastReceiver {
    private static final String TAG = TakePictureBroadcastReceiver.class.getSimpleName();

    private static final String EXTRA_NOTIFICATION_ID =
            TakePictureBroadcastReceiver.class.getSimpleName() + ":NOTIFICATION_ID";

    public static Intent createIntent(final int notificationId) {
        final Intent intent = new Intent("com.codepath.selfiespot.camera");
        // retrieving int extra somehow is overflowing, so, put and retrieve it as a string
        // and convert to int
        intent.putExtra(EXTRA_NOTIFICATION_ID, notificationId);
        return intent;
    }

    @Override
    public void onReceive(final Context context, final Intent intent) {
        final int notificationId = intent.getIntExtra(EXTRA_NOTIFICATION_ID, -1);
        final NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(notificationId);
        Log.d(TAG, "Cancelled notification: " + notificationId);

        // open camera
        final Intent cameraIntent = new Intent(MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA);
        cameraIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(cameraIntent);
    }
}
