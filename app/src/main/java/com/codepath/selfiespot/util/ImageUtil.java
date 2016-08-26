package com.codepath.selfiespot.util;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ImageUtil {
    private static final int MAX_DIMENSION_SIZE = 1080;
    private static String DIRECTORY_IMAGE = "selfie_spots";

    public static int[] getImageSize(final Uri uri) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(new File(uri.getPath()).getAbsolutePath(), options);
        int imageHeight = options.outHeight;
        int imageWidth = options.outWidth;
        return new int[]{imageWidth, imageHeight};
    }

    public static int[] getResizedImageSize(final int[] actualDimensions) {
        int actualWidth = actualDimensions[0];
        int actualHeight = actualDimensions[1];

        int desiredWidth = actualWidth;
        int desiredHeight = actualHeight;

        if (actualWidth > MAX_DIMENSION_SIZE || actualHeight > MAX_DIMENSION_SIZE) {
            if (actualWidth > actualHeight) {
                desiredWidth = MAX_DIMENSION_SIZE;
                desiredHeight = (int) ((float) actualHeight / (float) actualWidth * MAX_DIMENSION_SIZE);
            } else {
                desiredHeight = MAX_DIMENSION_SIZE;
                desiredWidth = (int) ((float) actualWidth / (float) actualHeight * MAX_DIMENSION_SIZE);
            }
        }
        return new int[]{desiredWidth, desiredHeight};
    }

    public static Uri savePicture(final Context context, final Bitmap bitmap) {
        final File mediaStorageDir = getStorageDir();

        final String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        final File mediaFile = new File(
                mediaStorageDir.getPath() + File.separator + "IMG_" + timeStamp + ".jpg"
        );

        // Saving the bitmap
        try {
            final ByteArrayOutputStream out = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);

            final FileOutputStream stream = new FileOutputStream(mediaFile);
            stream.write(out.toByteArray());
            stream.close();

        } catch (IOException exception) {
            exception.printStackTrace();
        }

        // Mediascanner need to scan for the image saved
        final Intent mediaScannerIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        final Uri fileContentUri = Uri.fromFile(mediaFile);
        mediaScannerIntent.setData(fileContentUri);
        context.sendBroadcast(mediaScannerIntent);

        return fileContentUri;
    }

    public static File getStorageDir() {
        File mediaStorageDir = new File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), DIRECTORY_IMAGE
        );

        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                return null;
            }
        }
        return mediaStorageDir;
    }
}
