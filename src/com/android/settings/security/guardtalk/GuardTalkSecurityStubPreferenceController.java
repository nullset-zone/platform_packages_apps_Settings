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

import com.android.settings.R;
import com.android.settings.core.BasePreferenceController;

/**
 * Availability gate for GuardTalk Security stub rows.
 *
 * <p>Rows remain visible (but non-interactive in XML) when the GuardTalk Security
 * dashboard is enabled, so Frontend/QA can validate hierarchy without Phase-2 logic.
 */
public class GuardTalkSecurityStubPreferenceController extends BasePreferenceController {

    public GuardTalkSecurityStubPreferenceController(Context context, String preferenceKey) {
        super(context, preferenceKey);
    }

    @Override
    public int getAvailabilityStatus() {
        return mContext.getResources().getBoolean(R.bool.config_use_guardtalk_security_dashboard)
                ? AVAILABLE
                : UNSUPPORTED_ON_DEVICE;
    }
}
