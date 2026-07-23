/*
 * Copyright (C) 2021 The Android Open Source Project
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

package com.android.settings.safetycenter;

import android.app.settings.SettingsEnums;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;

import androidx.preference.Preference;

import com.android.settings.core.BasePreferenceController;
import com.android.settings.core.SubSettingLauncher;
import com.android.settings.safetycenter.SafetyCenterUtils;
import com.android.settings.safetycenter.ui.SafetyCenterFragment;
import com.android.settings.flags.Flags;


/** Controller for the SafetyCenter entry in top level Settings. */
public class TopLevelSafetyCenterEntryPreferenceController extends BasePreferenceController {

    private static final String TAG = "TopLevelSafetyCenterEntryPreferenceController";

    public TopLevelSafetyCenterEntryPreferenceController(Context context, String preferenceKey) {
        super(context, preferenceKey);
    }

    @Override
    public int getAvailabilityStatus() {
        // T-SEC-P1-SETTINGS: dedicated keep/hide hook (replaces config_show_sim_info
        // reuse). GuardTalkSettingsOverlay sets
        // config_show_top_level_safety_center=false. Reversible via overlay.
        if (!mContext.getResources().getBoolean(
                com.android.settings.R.bool.config_show_top_level_safety_center)) {
            return UNSUPPORTED_ON_DEVICE;
        }
        if (SafetyCenterManagerWrapper.get().isEnabled(mContext)) {
            return AVAILABLE;
        }
        return CONDITIONALLY_UNAVAILABLE;
    }

    @Override
    public boolean handlePreferenceTreeClick(Preference preference) {
        if (!TextUtils.equals(preference.getKey(), getPreferenceKey())) {
            return super.handlePreferenceTreeClick(preference);
        }
        try {
            if (Flags.enableSafetyCenterNewUi()) {
                Log.d(TAG, "Launching SafetyCenter in Settings");
                new SubSettingLauncher(mContext)
                    .setDestination(SafetyCenterFragment.class.getName())
                    .setSourceMetricsCategory(SettingsEnums.SETTINGS_HOMEPAGE)
                    .launch();
            } else {
                Log.d(TAG, "Launching SafetyCenter in PermissionController");
                mContext.startActivity(new Intent(Intent.ACTION_SAFETY_CENTER)
                        .setPackage(
                                mContext.getPackageManager().getPermissionControllerPackageName()));
            }
        } catch (ActivityNotFoundException e) {
            Log.e(TAG, "Unable to open safety center", e);
            return false;
        }
        return true;
    }
}
