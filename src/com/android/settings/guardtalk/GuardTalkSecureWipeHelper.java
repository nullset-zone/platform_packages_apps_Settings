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
import android.guardtalk.GuardTalkSecureWipePolicy;
import android.os.SystemProperties;

import androidx.annotation.NonNull;

import com.android.settings.R;
import com.android.settingslib.guardtalk.GuardTalkSecureWipeKeys;

/**
 * Settings-side GuardTalk Secure wipe / Duress helper (T-SEC-P2-WIPE).
 *
 * <p>See {@code vendor/guardtalk/docs/SECURE_WIPE_SERVICE.md}.
 */
public final class GuardTalkSecureWipeHelper {

    private GuardTalkSecureWipeHelper() {}

    /** Overlay bool OR product prop ⇒ secure wipe / duress UI enabled. */
    public static boolean isSecureWipeEnabled(@NonNull Context context) {
        if (context.getResources().getBoolean(R.bool.config_guardtalk_secure_wipe_enabled)) {
            return true;
        }
        return SystemProperties.getBoolean(GuardTalkSecureWipeKeys.PROP_SECURE_WIPE_ENABLED, false)
                || GuardTalkSecureWipePolicy.isSecureWipeEnabled();
    }

    /** Anti-bruteforce wipe threshold (default 10). */
    public static int getAntiBruteforceWipeThreshold(@NonNull Context context) {
        if (!isSecureWipeEnabled(context)) {
            return 0;
        }
        return GuardTalkSecureWipePolicy.getAntiBruteforceWipeThreshold();
    }
}
