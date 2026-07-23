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

import android.app.settings.SettingsEnums;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;

import androidx.preference.Preference;

import com.android.settings.R;
import com.android.settings.core.BasePreferenceController;
import com.android.settings.guardtalk.GuardTalkLockPolicyHelper;
import com.android.settings.security.ScreenLockPreferenceDetailsUtils;
import com.android.settingslib.guardtalk.GuardTalkLockPolicyKeys;

/**
 * Wires GuardTalk Security → Device lock to ChooseLockGeneric
 * (F-SEC-P2-SECURITY-SCREENS / PASSWORD_ONLY_LOCK_POLICY).
 */
public class GuardTalkDeviceLockPreferenceController extends BasePreferenceController {

    private final ScreenLockPreferenceDetailsUtils mScreenLockUtils;

    public GuardTalkDeviceLockPreferenceController(Context context, String preferenceKey) {
        super(context, preferenceKey);
        mScreenLockUtils = new ScreenLockPreferenceDetailsUtils(context);
    }

    @Override
    public int getAvailabilityStatus() {
        if (!mContext.getResources().getBoolean(R.bool.config_use_guardtalk_security_dashboard)) {
            return UNSUPPORTED_ON_DEVICE;
        }
        if (!mScreenLockUtils.isAvailable()) {
            return UNSUPPORTED_ON_DEVICE;
        }
        return AVAILABLE;
    }

    @Override
    public void updateState(Preference preference) {
        super.updateState(preference);
        preference.setEnabled(true);
        preference.setSelectable(true);
        final CharSequence summary = mScreenLockUtils.getSummary(mContext.getUserId());
        if (!TextUtils.isEmpty(summary)) {
            preference.setSummary(summary);
        } else if (GuardTalkLockPolicyHelper.isPasswordOnlyLockEnabled(mContext)) {
            preference.setSummary(R.string.guardtalk_security_device_lock_summary_password_only);
        } else {
            preference.setSummary(R.string.guardtalk_security_stub_summary);
        }
    }

    @Override
    public boolean handlePreferenceTreeClick(Preference preference) {
        if (!TextUtils.equals(preference.getKey(), getPreferenceKey())
                && !TextUtils.equals(preference.getKey(),
                        GuardTalkLockPolicyKeys.PREF_DEVICE_LOCK)) {
            return false;
        }
        final Intent intent = mScreenLockUtils.getLaunchChooseLockGenericFragmentIntent(
                SettingsEnums.SECURITY);
        mContext.startActivity(intent);
        return true;
    }
}
