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
import android.text.TextUtils;

import androidx.preference.Preference;

import com.android.settings.R;
import com.android.settings.core.BasePreferenceController;
import com.android.settings.guardtalk.GuardTalkAutoRebootHelper;
import com.android.settings.safetycenter.ExploitProtectionFragment;
import com.android.settingslib.guardtalk.GuardTalkAutoRebootKeys;

/**
 * Wires GuardTalk Security → Auto-reboot status
 * (F-SEC-P2-SECURITY-SCREENS / AUTO_REBOOT_POLICY).
 *
 * <p>Enforcement is framework/init via GrapheneOS {@code AutoReboot}; this row
 * surfaces the active profile and deep-links to Exploit protection (Auto reboot
 * picker). Not a Quick Settings tile — Phase 3 owns QS registration.
 */
public class GuardTalkAutoRebootPreferenceController extends BasePreferenceController {

    public GuardTalkAutoRebootPreferenceController(Context context, String preferenceKey) {
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
        if (!GuardTalkAutoRebootHelper.isProfilesEnabled(mContext)) {
            preference.setSummary(R.string.guardtalk_security_stub_summary);
            return;
        }
        if (GuardTalkAutoRebootHelper.isOff(mContext)) {
            preference.setSummary(R.string.guardtalk_security_auto_reboot_summary_off);
            return;
        }
        final Integer hours = GuardTalkAutoRebootHelper.getProfileHoursOrNull(mContext);
        if (hours != null) {
            preference.setSummary(mContext.getResources().getQuantityString(
                    R.plurals.guardtalk_security_auto_reboot_summary_hours,
                    hours, hours));
        } else {
            preference.setSummary(R.string.guardtalk_security_auto_reboot_summary_on);
        }
    }

    @Override
    public boolean handlePreferenceTreeClick(Preference preference) {
        if (!TextUtils.equals(preference.getKey(), getPreferenceKey())
                && !TextUtils.equals(preference.getKey(),
                        GuardTalkAutoRebootKeys.PREF_AUTO_REBOOT)) {
            return false;
        }
        final Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.setClassName(mContext.getPackageName(),
                com.android.settings.Settings.ExploitProtectionActivity.class.getName());
        intent.putExtra(com.android.settings.SettingsActivity.EXTRA_SHOW_FRAGMENT,
                ExploitProtectionFragment.class.getName());
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        mContext.startActivity(intent);
        return true;
    }
}
