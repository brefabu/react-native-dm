package com.rndm;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class OnUpgradeReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(final Context context, final Intent intent) {
        Intent launchIntent = context.getPackageManager().getLaunchIntentForPackage("com.project");
        if (launchIntent != null) {
            context.startActivity(launchIntent);
        }
    }
}