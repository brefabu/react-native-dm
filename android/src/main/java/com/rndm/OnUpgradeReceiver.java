package com.rndm;

import android.app.Activity;
import android.app.ActivityOptions;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class OnUpgradeReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(final Context context, final Intent intent) {
        Activity mActivity = getCurrentActivity();
        Intent launchIntent = context.getPackageManager().getLaunchIntentForPackage("com.project");

      if (mActivity != null) {
        DevicePolicyManager myDevicePolicyManager = (DevicePolicyManager) mActivity.getSystemService(Context.DEVICE_POLICY_SERVICE);
        ComponentName mDPM = new ComponentName(mActivity, MyAdmin.class);

        if (myDevicePolicyManager.isDeviceOwnerApp(mActivity.getPackageName())) {
            myDevicePolicyManager.reboot(mDPM);

            if (launchIntent != null) {
                context.startActivity(launchIntent);
            }
        }
      }
    }
}