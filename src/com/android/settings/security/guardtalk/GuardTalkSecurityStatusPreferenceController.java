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
import android.text.TextUtils;

import androidx.preference.Preference;

import com.android.settings.R;
import com.android.settings.core.BasePreferenceController;
import com.android.settings.core.SubSettingLauncher;
import com.android.settings.guardtalk.GuardTalkSecurityStatusHelper;
import com.android.settingslib.guardtalk.GuardTalkSecurityStatusKeys;

/**
 * GuardTalk Security → Security status entry (F-SEC-P5-STATUS-UI).
 *
 * <p><strong>Post-unlock only.</strong> Hidden while locked / pre-first-unlock.
 * Never surfaces Duress status (lock screen must not reveal Duress).
 */
public class GuardTalkSecurityStatusPreferenceController extends BasePreferenceController {

    public GuardTalkSecurityStatusPreferenceController(Context context, String preferenceKey) {
        super(context, preferenceKey);
    }

    @Override
    public int getAvailabilityStatus() {
        if (!mContext.getResources().getBoolean(R.bool.config_use_guardtalk_security_dashboard)) {
            return UNSUPPORTED_ON_DEVICE;
        }
        // Post-unlock only — never expose status shell on lock / pre-CE-unlock.
        if (!GuardTalkSecurityStatusHelper.isPostUnlockStatusAllowed(mContext)) {
            return CONDITIONALLY_UNAVAILABLE;
        }
        return AVAILABLE;
    }

    @Override
    public void updateState(Preference preference) {
        super.updateState(preference);
        preference.setEnabled(true);
        preference.setSelectable(true);
        preference.setSummary(R.string.guardtalk_security_status_summary);
    }

    @Override
    public boolean handlePreferenceTreeClick(Preference preference) {
        if (!TextUtils.equals(preference.getKey(), getPreferenceKey())
                && !TextUtils.equals(preference.getKey(),
                        GuardTalkSecurityStatusKeys.PREF_SECURITY_STATUS)) {
            return false;
        }
        // Deny navigation when pre-unlock (do not open status).
        if (!GuardTalkSecurityStatusHelper.isPostUnlockStatusAllowed(mContext)) {
            return true;
        }
        new SubSettingLauncher(mContext)
                .setDestination(GuardTalkSecurityStatusFragment.class.getName())
                .setTitleRes(R.string.guardtalk_security_status_title)
                .setSourceMetricsCategory(getMetricsCategory())
                .launch();
        return true;
    }
}
