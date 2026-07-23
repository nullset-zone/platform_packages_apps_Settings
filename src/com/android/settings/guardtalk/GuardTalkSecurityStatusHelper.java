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

import android.content.Context;
import android.guardtalk.GuardTalkFilesPolicy;
import android.guardtalk.GuardTalkPrivacyPolicy;
import android.guardtalk.GuardTalkProductionHardeningPolicy;
import android.guardtalk.GuardTalkSecurityStatus;
import android.guardtalk.GuardTalkSecurityStatus.Snapshot;
import android.guardtalk.GuardTalkSecureWipePolicy;

import androidx.annotation.NonNull;

/**
 * Security status helpers (T-SEC-P5-STATUS → F-SEC-P5-STATUS-UI).
 *
 * <p>Aggregates Phase-2/4/5 GuardTalk policy helpers into a post-unlock
 * {@link Snapshot}. Status UI is <strong>post-unlock only</strong>. Never
 * surface Duress status on the lock screen or in this aggregate (policy:
 * {@code DURESS_AND_ANTI_BRUTEFORCE_POLICY.md},
 * {@code SECURITY_STATUS_API.md}).
 */
public final class GuardTalkSecurityStatusHelper {

    private GuardTalkSecurityStatusHelper() {}

    /**
     * True when Security status may be shown: CE unlocked and keyguard not
     * holding the device locked.
     */
    public static boolean isPostUnlockStatusAllowed(@NonNull Context context) {
        return GuardTalkSecurityStatus.isPostUnlockStatusAllowed(context);
    }

    /**
     * Collects aggregate status for Settings / Frontend. Returns
     * {@link Snapshot#denied()} when pre-unlock. <strong>Never</strong>
     * includes Duress armed state.
     */
    @NonNull
    public static Snapshot collect(@NonNull Context context) {
        if (!isPostUnlockStatusAllowed(context)) {
            return Snapshot.denied();
        }

        final boolean autoProfiles = GuardTalkAutoRebootHelper.isProfilesEnabled(context);
        final boolean autoOff = !autoProfiles || GuardTalkAutoRebootHelper.isOff(context);
        int autoHours = 0;
        if (!autoOff) {
            final Integer hours = GuardTalkAutoRebootHelper.getProfileHoursOrNull(context);
            if (hours != null) {
                autoHours = hours;
            }
        }

        // Overlay-aware Settings helpers for lock / sensor / USB / wipe;
        // framework policies for privacy / files / hardening markers.
        final boolean secureWipe =
                GuardTalkSecureWipeHelper.isSecureWipeEnabled(context)
                        || GuardTalkSecureWipePolicy.isSecureWipeEnabled();
        final int antiThreshold = secureWipe
                ? GuardTalkSecureWipeHelper.getAntiBruteforceWipeThreshold(context)
                : 0;

        return Snapshot.create(
                /* postUnlockAllowed= */ true,
                GuardTalkLockPolicyHelper.isPasswordOnlyLockEnabled(context),
                GuardTalkSensorPrivacyHelper.isSensorPrivacyWhenLockedEnabled(context),
                GuardTalkSensorPrivacyHelper.areSensorsForceDenied(context),
                GuardTalkUsbProtectionHelper.isFailClosedEnabled(context),
                GuardTalkUsbProtectionHelper.isUsbDataForceDenied(context),
                GuardTalkSensorPrivacyHelper.isLockdownFailClosedEnabled(context),
                GuardTalkSensorPrivacyHelper.isLockdownActive(context),
                autoProfiles,
                autoHours,
                secureWipe,
                antiThreshold,
                GuardTalkPrivacyPolicy.isPrivacyTmpfsEnabled(),
                GuardTalkPrivacyPolicy.isClipboardClearEnabled(),
                GuardTalkPrivacyPolicy.getClipboardClearTimeoutMs(),
                GuardTalkFilesPolicy.isEnabled(),
                GuardTalkFilesPolicy.isTrashEnabled(),
                GuardTalkFilesPolicy.isCriticalProtectEnabled(),
                GuardTalkProductionHardeningPolicy.isEnabled(),
                GuardTalkProductionHardeningPolicy.isProductionProfile(),
                GuardTalkProductionHardeningPolicy.isDebuggableOff(),
                GuardTalkProductionHardeningPolicy.mustBlockUnknownSources());
    }
}
