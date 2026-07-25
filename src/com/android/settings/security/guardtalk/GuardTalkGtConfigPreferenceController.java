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
import android.guardtalk.GuardTalkConfigGateManager;
import android.text.format.DateUtils;

import androidx.preference.Preference;

import com.android.settings.R;
import com.android.settings.core.BasePreferenceController;

/**
 * GT Config row (F-SEC-ACTIVATE-UI).
 *
 * <p>Browse launch is ungated. Summary reflects write-session state: Apply
 * still requires a password session inside GuardTalkConfig (fail-closed).
 */
public class GuardTalkGtConfigPreferenceController extends BasePreferenceController {

    public GuardTalkGtConfigPreferenceController(Context context, String preferenceKey) {
        super(context, preferenceKey);
    }

    @Override
    public int getAvailabilityStatus() {
        return mContext.getResources().getBoolean(R.bool.config_use_guardtalk_security_dashboard)
                ? AVAILABLE
                : UNSUPPORTED_ON_DEVICE;
    }

    @Override
    public void updateState(Preference preference) {
        super.updateState(preference);
        preference.setEnabled(true);
        preference.setSelectable(true);
        final int userId = mContext.getUserId();
        if (!GuardTalkConfigGateManager.hasSecureLockScreen(userId)) {
            preference.setSummary(R.string.guardtalk_gt_config_summary_no_password);
            return;
        }
        final long remaining = GuardTalkConfigGateManager.getSessionRemainingMillis(userId);
        if (remaining > 0L) {
            preference.setSummary(mContext.getString(
                    R.string.guardtalk_gt_config_summary_authorized,
                    DateUtils.formatElapsedTime(remaining / 1000L)));
        } else {
            preference.setSummary(R.string.guardtalk_gt_config_summary_browse);
        }
    }
}
