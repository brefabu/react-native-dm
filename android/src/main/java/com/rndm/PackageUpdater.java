package com.app.devicemanager;

import android.app.PendingIntent;
import android.app.admin.DeviceAdminReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageInstaller;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;


public class PackageUpdater extends DeviceAdminReceiver {
    // A lot of the code can be found on
    // https://github.com/googlesamples/android-testdpc/blob/8b83e20f71da3e478444e47ba0b35946ec981520
    // /src/main/java/com/afwsamples/testdpc/common/PackageInstallationUtils.javahttps://github.com/
    // googlesamples/android-testdpc/blob/8b83e20f71da3e478444e47ba0b35946ec981520/src/main/java/com/
    // afwsamples/testdpc/common/PackageInstallationUtils.java
    public static final String ACTION_INSTALL_COMPLETE = "com.rndm.INSTALL_COMPLETE";

    public static void downloadPackage(Context context, String fileUrl, String fileName) {
        // The DownloadManager is retarded and can't access local storage.
        // This code simply downloads a file and puts it in the local storage (context.getFilesDir())
        // Then installs it.

        // Create a new executor
        Executor executor = Executors.newSingleThreadExecutor();

        // Submit a task to the executor to download the file
        executor.execute(() -> {
            try {
                String filePath = context.getFilesDir() + File.separator + fileName;
                File file = new File(filePath);
                if (file.exists()) {
                    boolean result = file.delete();
                    if (!result)
                        Log.e("PackageUpdater", "Failed to delete old file");
                }
                URL url = new URL(fileUrl);
                BufferedInputStream inputStream = new BufferedInputStream(url.openStream());
                FileOutputStream fileOutput = new FileOutputStream(context.getFilesDir() +
                        File.separator + fileName);

                // Create a buffer to read the data from the input stream
                byte[] buffer = new byte[1024];
                // Read the data from the input stream and store it in the buffer
                int bufferLength;
                // Read the data from the input stream and store it in the buffer
                while ((bufferLength = inputStream.read(buffer)) > 0) {
                    // Write the data from the buffer to the file output stream
                    fileOutput.write(buffer, 0, bufferLength);
                }
                fileOutput.close();
                inputStream.close();
                installPackage(context, file, null);

            } catch (IOException e) {
                Log.e("PackageUpdater", e.getMessage());
            }
        });
    }

    public static void installPackage(Context context, File file, String packageName)
            throws IOException {
        final PackageInstaller packageInstaller = context.getPackageManager().getPackageInstaller();
        final PackageInstaller.SessionParams params =
                new PackageInstaller.SessionParams(PackageInstaller.SessionParams.MODE_FULL_INSTALL);
        params.setAppPackageName(packageName);
        // set params
        final int sessionId = packageInstaller.createSession(params);
        final PackageInstaller.Session session = packageInstaller.openSession(sessionId);
        final OutputStream out = session.openWrite("TestDPC", 0, -1);
        final InputStream in = new FileInputStream(file);
        final byte[] buffer = new byte[65536];
        int c;
        while ((c = in.read(buffer)) != -1) {
            out.write(buffer, 0, c);
        }
        session.fsync(out);
        in.close();
        out.close();

        session.commit(createInstallIntentSender(context, sessionId));
        Log.i("PackageUpdater", "Installing app");
    }

    @SuppressWarnings("UnspecifiedImmutableFlag") // TODO(b/210723613): proper fix
    private static IntentSender createInstallIntentSender(Context context, int sessionId) {
        final PendingIntent pendingIntent =
                PendingIntent.getBroadcast(context, sessionId, new Intent(ACTION_INSTALL_COMPLETE),
                        PendingIntent.FLAG_IMMUTABLE);
        return pendingIntent.getIntentSender();
    }
}





