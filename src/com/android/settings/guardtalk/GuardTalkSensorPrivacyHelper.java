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

package com.android.settings.guardtalk;

import android.app.KeyguardManager;
import android.content.Context;
import android.guardtalk.GuardTalkSensorPrivacyPolicy;
import android.os.SystemProperties;
import android.os.UserManager;

import androidx.annotation.NonNull;

import com.android.internal.widget.LockPatternUtils;
import com.android.settings.R;
import com.android.settingslib.guardtalk.GuardTalkSensorPrivacyKeys;

/**
 * Settings-side GuardTalk sensor privacy / lockdown helper (T-SEC-P2-SENSOR).
 *
 * <p>Overlay bool OR product prop ⇒ enabled (fail-closed toward restriction).
 * Server enforcement uses props via {@link GuardTalkSensorPrivacyPolicy}.
 *
 * <p>See {@code vendor/guardtalk/docs/SENSOR_PRIVACY_LOCKDOWN_POLICY.md}.
 */
public final class GuardTalkSensorPrivacyHelper {

    private GuardTalkSensorPrivacyHelper() {}

    /** Mic/camera denied when locked or pre-first-unlock. */
    public static boolean isSensorPrivacyWhenLockedEnabled(@NonNull Context context) {
        if (context.getResources().getBoolean(R.bool.config_guardtalk_sensor_privacy_when_locked)) {
            return true;
        }
        // Overlay OR product prop OR framework policy (avoid stub "Coming soon").
        return SystemProperties.getBoolean(
                GuardTalkSensorPrivacyKeys.PROP_SENSOR_PRIVACY_WHEN_LOCKED, false)
                || GuardTalkSensorPrivacyPolicy.isSensorPrivacyWhenLockedEnabled();
    }

    /** Lockdown forces sensors + network fail-closed. */
    public static boolean isLockdownFailClosedEnabled(@NonNull Context context) {
        if (context.getResources().getBoolean(R.bool.config_guardtalk_lockdown_fail_closed)) {
            return true;
        }
        if (SystemProperties.get(GuardTalkSensorPrivacyKeys.PROP_LOCKDOWN_FAIL_CLOSED, "")
                .isEmpty()) {
            return isSensorPrivacyWhenLockedEnabled(context)
                    || GuardTalkSensorPrivacyPolicy.isLockdownFailClosedEnabled();
        }
        return SystemProperties.getBoolean(
                GuardTalkSensorPrivacyKeys.PROP_LOCKDOWN_FAIL_CLOSED, false)
                || GuardTalkSensorPrivacyPolicy.isLockdownFailClosedEnabled();
    }

    /** True when sensors are currently force-denied by policy. */
    public static boolean areSensorsForceDenied(@NonNull Context context) {
        if (!isSensorPrivacyWhenLockedEnabled(context) && !isLockdownFailClosedEnabled(context)) {
            return false;
        }
        final int userId = context.getUserId();
        final KeyguardManager km = context.getSystemService(KeyguardManager.class);
        final UserManager um = context.getSystemService(UserManager.class);
        final boolean deviceLocked = km != null && km.isDeviceLocked(userId);
        final boolean userUnlocked = um != null && um.isUserUnlocked(userId);
        final LockPatternUtils lpu = new LockPatternUtils(context);
        final int strongAuth = lpu.getStrongAuthForUser(userId);
        return GuardTalkSensorPrivacyPolicy.mustDenySensors(
                deviceLocked, userUnlocked, strongAuth);
    }

    /** True when user lockdown strong-auth flag is set. */
    public static boolean isLockdownActive(@NonNull Context context) {
        final LockPatternUtils lpu = new LockPatternUtils(context);
        return GuardTalkSensorPrivacyPolicy.isLockdownActive(
                lpu.getStrongAuthForUser(context.getUserId()));
    }
}
