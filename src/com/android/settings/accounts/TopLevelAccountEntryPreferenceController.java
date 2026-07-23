/*
 * Copyright (C) 2018 The Android Open Source Project
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

package com.android.settings.accounts;

import android.content.Context;

import com.android.settings.R;
import com.android.settings.core.BasePreferenceController;

public class TopLevelAccountEntryPreferenceController extends BasePreferenceController {
    public TopLevelAccountEntryPreferenceController(Context context, String preferenceKey) {
        super(context, preferenceKey);
    }

    @Override
    public int getAvailabilityStatus() {
        // T-SEC-P1-SETTINGS: dedicated keep/hide hook (replaces config_show_sim_info
        // reuse). GuardTalkSettingsOverlay sets
        // config_show_top_level_accounts=false. Reversible via overlay.
        if (!mContext.getResources().getBoolean(
                com.android.settings.R.bool.config_show_top_level_accounts)) {
            return UNSUPPORTED_ON_DEVICE;
        }
        return AVAILABLE;
    }

    @Override
    public CharSequence getSummary() {
        return mContext.getString(R.string.account_dashboard_default_summary);
    }
}
