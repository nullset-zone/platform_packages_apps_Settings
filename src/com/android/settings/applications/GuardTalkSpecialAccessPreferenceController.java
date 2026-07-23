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

package com.android.settings.applications;

import android.content.Context;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.preference.Preference;

import com.android.settings.R;
import com.android.settings.core.BasePreferenceController;
import com.android.settings.guardtalk.GuardTalkAppsVisibility;
import com.android.settings.guardtalk.GuardTalkConfigGateClient;

/**
 * Apps → Special app access visibility + maintenance password gate
 * (F-SEC-P1-ABOUT-APPS). Click handling is owned by {@link AppDashboardFragment}
 * so credential confirmation can use fragment startActivityForResult.
 */
public class GuardTalkSpecialAccessPreferenceController extends BasePreferenceController {

    public GuardTalkSpecialAccessPreferenceController(
            @NonNull Context context, @NonNull String preferenceKey) {
        super(context, preferenceKey);
    }

    @Override
    public int getAvailabilityStatus() {
        return GuardTalkAppsVisibility.isShown(mContext, GuardTalkAppsVisibility.KEY_SPECIAL_ACCESS)
                ? AVAILABLE
                : UNSUPPORTED_ON_DEVICE;
    }

    @Override
    public void updateState(Preference preference) {
        super.updateState(preference);
        if (!GuardTalkAppsVisibility.specialAccessRequiresMaintenance(mContext)) {
            preference.setSummary(null);
            return;
        }
        if (GuardTalkConfigGateClient.isMaintenanceAuthorized(mContext)) {
            preference.setSummary(R.string.guardtalk_maintenance_summary_authorized);
        } else {
            preference.setSummary(R.string.guardtalk_maintenance_summary_locked);
        }
    }

    /** Returns true when the key is Special Access and the click was consumed. */
    public static boolean isSpecialAccessKey(String key) {
        return TextUtils.equals(key, GuardTalkAppsVisibility.KEY_SPECIAL_ACCESS);
    }
}
