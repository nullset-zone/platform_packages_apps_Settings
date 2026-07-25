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
import android.provider.Settings;
import android.text.TextUtils;

import androidx.preference.Preference;

import com.android.settings.R;
import com.android.settings.core.BasePreferenceController;
import com.android.settings.guardtalk.GuardTalkSensorPrivacyHelper;
import com.android.settingslib.guardtalk.GuardTalkSensorPrivacyKeys;

/**
 * Wires GuardTalk Security → Sensor privacy status
 * (F-SEC-P2-SECURITY-SCREENS / SENSOR_PRIVACY_LOCKDOWN_POLICY).
 *
 * <p>Enforcement is framework fail-closed; this row surfaces status and deep-links
 * to platform Privacy controls for manual mic/camera toggles when unlocked.
 */
public class GuardTalkSensorPrivacyPreferenceController extends BasePreferenceController {

    public GuardTalkSensorPrivacyPreferenceController(Context context, String preferenceKey) {
        super(context, preferenceKey);
    }

    @Override
    public int getAvailabilityStatus() {
        if (!mContext.getResources().getBoolean(R.bool.config_use_guardtalk_security_dashboard)) {
            return UNSUPPORTED_ON_DEVICE;
        }
        return AVAILABLE;
    }

    @Override
    public void updateState(Preference preference) {
        super.updateState(preference);
        preference.setEnabled(true);
        preference.setSelectable(true);
        if (GuardTalkSensorPrivacyHelper.areSensorsForceDenied(mContext)) {
            preference.setSummary(
                    R.string.guardtalk_security_sensor_privacy_summary_locked);
        } else if (GuardTalkSensorPrivacyHelper.isSensorPrivacyWhenLockedEnabled(mContext)) {
            preference.setSummary(
                    R.string.guardtalk_security_sensor_privacy_summary_policy);
        } else {
            // Privacy settings deep-link still works — honest inactive, not stub.
            preference.setSummary(R.string.guardtalk_security_status_inactive);
        }
    }

    @Override
    public boolean handlePreferenceTreeClick(Preference preference) {
        if (!TextUtils.equals(preference.getKey(), getPreferenceKey())
                && !TextUtils.equals(preference.getKey(),
                        GuardTalkSensorPrivacyKeys.PREF_SENSOR_PRIVACY)) {
            return false;
        }
        // Deep-link to platform privacy / mic toggle surface when available.
        final Intent intent = new Intent(Settings.ACTION_PRIVACY_SETTINGS);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        mContext.startActivity(intent);
        return true;
    }
}
