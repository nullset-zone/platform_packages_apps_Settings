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

import androidx.preference.Preference;

import com.android.settings.R;
import com.android.settings.core.BasePreferenceController;
import com.android.settings.guardtalk.GuardTalkSecureWipeHelper;

/**
 * Wires GuardTalk Security → Anti-bruteforce policy row (F-SEC-P2-SECURITY-SCREENS).
 *
 * <p>Informational only — wipe@N is enforced by the HW-backed counter + shared
 * {@code SecureWipeEngine}. Not a Quick Settings tile.
 */
public class GuardTalkAntiBruteforcePreferenceController extends BasePreferenceController {

    public GuardTalkAntiBruteforcePreferenceController(Context context, String preferenceKey) {
        super(context, preferenceKey);
    }

    @Override
    public int getAvailabilityStatus() {
        if (!mContext.getResources().getBoolean(R.bool.config_use_guardtalk_security_dashboard)) {
            return UNSUPPORTED_ON_DEVICE;
        }
        if (!GuardTalkSecureWipeHelper.isSecureWipeEnabled(mContext)) {
            return DISABLED_FOR_USER;
        }
        if (GuardTalkSecureWipeHelper.getAntiBruteforceWipeThreshold(mContext) <= 0) {
            return DISABLED_FOR_USER;
        }
        return AVAILABLE;
    }

    @Override
    public void updateState(Preference preference) {
        super.updateState(preference);
        // Policy is enforced by framework — row is read-only informational.
        preference.setEnabled(true);
        preference.setSelectable(false);
        final int threshold = GuardTalkSecureWipeHelper.getAntiBruteforceWipeThreshold(mContext);
        preference.setSummary(mContext.getResources().getQuantityString(
                R.plurals.guardtalk_security_anti_bruteforce_summary,
                threshold, threshold));
    }
}
