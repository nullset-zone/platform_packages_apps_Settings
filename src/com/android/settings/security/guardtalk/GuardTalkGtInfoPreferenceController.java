/*
 * Copyright (C) 2026 The GuardTalkOS Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.settings.security.guardtalk;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import androidx.preference.Preference;

import com.android.settings.R;
import com.android.settings.core.BasePreferenceController;

/**
 * GuardTalk Security → GT Info (F-SEC-ACTIVATE-UI).
 *
 * <p>Browse-only deep-link to {@code com.guardtalk.validator.ValidatorActivity}.
 * No password gate. Never a stub / "Coming soon" row when the package is present.
 */
public class GuardTalkGtInfoPreferenceController extends BasePreferenceController {

    private static final String TAG = "GtInfoPref";
    private static final String GT_INFO_PACKAGE = "com.guardtalk.validator";
    private static final String GT_INFO_ACTIVITY = "com.guardtalk.validator.ValidatorActivity";

    public GuardTalkGtInfoPreferenceController(Context context, String preferenceKey) {
        super(context, preferenceKey);
    }

    @Override
    public int getAvailabilityStatus() {
        if (!mContext.getResources().getBoolean(R.bool.config_use_guardtalk_security_dashboard)) {
            return UNSUPPORTED_ON_DEVICE;
        }
        if (!isGtInfoInstalled()) {
            // Honest: package missing on this product image.
            return UNSUPPORTED_ON_DEVICE;
        }
        return AVAILABLE;
    }

    @Override
    public void updateState(Preference preference) {
        super.updateState(preference);
        preference.setEnabled(true);
        preference.setSelectable(true);
        preference.setSummary(R.string.guardtalk_gt_info_summary);
    }

    @Override
    public boolean handlePreferenceTreeClick(Preference preference) {
        if (!TextUtils.equals(preference.getKey(), getPreferenceKey())) {
            return false;
        }
        final Intent intent = new Intent();
        intent.setClassName(GT_INFO_PACKAGE, GT_INFO_ACTIVITY);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        try {
            mContext.startActivity(intent);
        } catch (Exception e) {
            Log.e(TAG, "Failed to launch GT Info → Validator", e);
            Toast.makeText(mContext, R.string.guardtalk_gt_info_toast_launch_failed,
                    Toast.LENGTH_SHORT).show();
        }
        return true;
    }

    private boolean isGtInfoInstalled() {
        try {
            mContext.getPackageManager().getPackageInfo(GT_INFO_PACKAGE, 0);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }
}
