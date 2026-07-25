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
import android.ext.settings.ExtSettings;
import android.guardtalk.GuardTalkAutoRebootPolicy;
import android.os.SystemProperties;

import androidx.annotation.NonNull;

import com.android.settings.R;
import com.android.settingslib.guardtalk.GuardTalkAutoRebootKeys;

import java.util.concurrent.TimeUnit;

/**
 * Settings-side GuardTalk Auto-reboot helper (T-SEC-P2-AUTOREBOOT).
 *
 * <p>Overlay bool OR product prop ⇒ profiles enabled. Server enforcement uses
 * props via {@link GuardTalkAutoRebootPolicy}.
 *
 * <p>See {@code vendor/guardtalk/docs/AUTO_REBOOT_POLICY.md}.
 */
public final class GuardTalkAutoRebootHelper {

    private GuardTalkAutoRebootHelper() {}

    /** GuardTalk profiles + exclusion windows are active. */
    public static boolean isProfilesEnabled(@NonNull Context context) {
        if (context.getResources().getBoolean(R.bool.config_guardtalk_auto_reboot_profiles)) {
            return true;
        }
        // Overlay OR product prop OR framework policy (avoid stub "Coming soon").
        return SystemProperties.getBoolean(
                GuardTalkAutoRebootKeys.PROP_AUTO_REBOOT_PROFILES, false)
                || GuardTalkAutoRebootPolicy.isProfilesEnabled();
    }

    /** Effective profile timeout in milliseconds (clamped when profiles on). */
    public static int getEffectiveTimeoutMillis(@NonNull Context context) {
        final int raw = ExtSettings.AUTO_REBOOT_TIMEOUT.get(context);
        if (!isProfilesEnabled(context)) {
            return raw;
        }
        return GuardTalkAutoRebootPolicy.clampToProfileMillis(raw);
    }

    /** True when Auto-reboot profile is Off. */
    public static boolean isOff(@NonNull Context context) {
        return getEffectiveTimeoutMillis(context) <= 0;
    }

    /**
     * Human-readable profile label for Security dashboard summary.
     * Returns hours for on profiles, or {@code null} when Off / unknown.
     */
    public static Integer getProfileHoursOrNull(@NonNull Context context) {
        final int ms = getEffectiveTimeoutMillis(context);
        if (ms <= 0) {
            return null;
        }
        final long hours = TimeUnit.MILLISECONDS.toHours(ms);
        if (hours <= 0 || TimeUnit.HOURS.toMillis(hours) != ms) {
            return null;
        }
        return (int) hours;
    }
}
