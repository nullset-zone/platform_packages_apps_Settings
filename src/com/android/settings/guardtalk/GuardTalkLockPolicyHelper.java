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
import android.guardtalk.GuardTalkLockPolicy;
import android.os.SystemProperties;

import androidx.annotation.NonNull;

import com.android.settings.R;
import com.android.settingslib.guardtalk.GuardTalkLockPolicyKeys;

/**
 * Settings-side GuardTalk lock policy helper (T-SEC-P2-LOCK).
 *
 * <p>Overlay bool OR product prop ⇒ enabled (fail-closed toward restriction).
 * Server enforcement uses props only via {@link GuardTalkLockPolicy}.
 *
 * <p>See {@code vendor/guardtalk/docs/PASSWORD_ONLY_LOCK_POLICY.md}.
 */
public final class GuardTalkLockPolicyHelper {

    private GuardTalkLockPolicyHelper() {}

    /** Password-only enrollment (PIN/pattern/biometric/Smart Lock blocked). */
    public static boolean isPasswordOnlyLockEnabled(@NonNull Context context) {
        if (context.getResources().getBoolean(R.bool.config_guardtalk_password_only_lock)) {
            return true;
        }
        return SystemProperties.getBoolean(GuardTalkLockPolicyKeys.PROP_PASSWORD_ONLY_LOCK, false);
    }

    /** Smart Lock / trust agents blocked. */
    public static boolean isSmartLockBlocked(@NonNull Context context) {
        if (isPasswordOnlyLockEnabled(context)) {
            return true;
        }
        if (context.getResources().getBoolean(R.bool.config_guardtalk_block_smart_lock)) {
            return true;
        }
        return false;
    }

    /** Biometric enrollment surfaces blocked in Settings. */
    public static boolean isBiometricEnrollBlocked(@NonNull Context context) {
        if (isPasswordOnlyLockEnabled(context)) {
            return true;
        }
        if (context.getResources().getBoolean(R.bool.config_guardtalk_block_biometric_enroll)) {
            return true;
        }
        return false;
    }

    /** Max lock-after-timeout (ms) for Settings clamp UI. */
    public static long getMaxLockAfterTimeoutMs(@NonNull Context context) {
        if (!isPasswordOnlyLockEnabled(context)) {
            return Long.MAX_VALUE;
        }
        return GuardTalkLockPolicy.getMaxLockAfterTimeoutMs();
    }
}
