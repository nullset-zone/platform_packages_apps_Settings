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

import static com.android.internal.widget.LockPatternUtils.StrongAuthTracker
        .STRONG_AUTH_REQUIRED_AFTER_USER_LOCKDOWN;

import android.content.Context;
import android.os.RemoteException;
import android.os.UserHandle;
import android.text.TextUtils;
import android.util.Log;
import android.view.IWindowManager;
import android.view.WindowManagerGlobal;

import androidx.preference.Preference;

import com.android.internal.widget.LockPatternUtils;
import com.android.settings.R;
import com.android.settings.core.BasePreferenceController;
import com.android.settings.guardtalk.GuardTalkSensorPrivacyHelper;
import com.android.settingslib.guardtalk.GuardTalkSensorPrivacyKeys;

/**
 * Wires GuardTalk Security → Lockdown (T-SEC-P2-SENSOR).
 *
 * <p>Triggers platform lockdown (strong auth + lock now). Does <em>not</em>
 * register a Quick Settings tile — power-menu Lockdown and this Settings row
 * only. Framework fail-closed sensors/network apply via
 * {@code GuardTalkSensorPrivacyHooks}.
 */
public class GuardTalkLockdownPreferenceController extends BasePreferenceController {

    private static final String TAG = "GtLockdownPref";

    private final LockPatternUtils mLockPatternUtils;

    public GuardTalkLockdownPreferenceController(Context context, String preferenceKey) {
        super(context, preferenceKey);
        mLockPatternUtils = new LockPatternUtils(context);
    }

    @Override
    public int getAvailabilityStatus() {
        if (!mContext.getResources().getBoolean(R.bool.config_use_guardtalk_security_dashboard)) {
            return UNSUPPORTED_ON_DEVICE;
        }
        if (!mLockPatternUtils.isSecure(mContext.getUserId())) {
            return DISABLED_FOR_USER;
        }
        return AVAILABLE;
    }

    @Override
    public void updateState(Preference preference) {
        super.updateState(preference);
        preference.setEnabled(true);
        preference.setSelectable(true);
        if (GuardTalkSensorPrivacyHelper.isLockdownActive(mContext)) {
            preference.setSummary(R.string.guardtalk_security_lockdown_summary_active);
        } else if (GuardTalkSensorPrivacyHelper.isLockdownFailClosedEnabled(mContext)) {
            preference.setSummary(R.string.guardtalk_security_lockdown_summary_ready);
        } else {
            // Immediate lockdown action still works — honest inactive, not stub.
            preference.setSummary(R.string.guardtalk_security_status_inactive);
        }
    }

    @Override
    public boolean handlePreferenceTreeClick(Preference preference) {
        if (!TextUtils.equals(preference.getKey(), getPreferenceKey())
                && !TextUtils.equals(preference.getKey(),
                        GuardTalkSensorPrivacyKeys.PREF_LOCKDOWN)) {
            return false;
        }
        // Same path as SystemUI GlobalActions LockDownAction — no QS tile.
        mLockPatternUtils.requireStrongAuth(
                STRONG_AUTH_REQUIRED_AFTER_USER_LOCKDOWN, UserHandle.USER_ALL);
        try {
            final IWindowManager wm = WindowManagerGlobal.getWindowManagerService();
            if (wm != null) {
                wm.lockNow(null);
            }
        } catch (RemoteException e) {
            Log.e(TAG, "Failed to lock device for lockdown", e);
        }
        preference.setSummary(R.string.guardtalk_security_lockdown_summary_active);
        return true;
    }
}
