package com.android.settings.system;

import android.content.Context;
import android.content.Intent;
import android.ext.LogViewerApp;
import android.text.TextUtils;

import androidx.preference.Preference;

import com.android.settings.R;
import com.android.settings.core.BasePreferenceController;
import com.android.settings.ext.ExtSettingControllerHelper;

public class LogcatLinkPrefController extends BasePreferenceController {

    public LogcatLinkPrefController(Context context, String preferenceKey) {
        super(context, preferenceKey);
    }

    @Override
    public int getAvailabilityStatus() {
        // F-SEC-P4-SYSTEM-UI: System keep/hide — view logs (forensics surface).
        if (!mContext.getResources().getBoolean(R.bool.config_show_view_logs)) {
            return UNSUPPORTED_ON_DEVICE;
        }
        return ExtSettingControllerHelper.getGlobalSettingAvailability(mContext);
    }

    @Override
    public boolean handlePreferenceTreeClick(Preference preference) {
        if (!TextUtils.equals(preference.getKey(), getPreferenceKey())) {
            return false;
        }

        Intent i = LogViewerApp.getLogcatIntent();
        mContext.startActivity(i);
        return true;
    }
}
