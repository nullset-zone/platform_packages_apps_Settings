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
import android.guardtalk.GuardTalkUsbProtectionPolicy;
import android.os.SystemProperties;
import android.os.UserManager;

import androidx.annotation.NonNull;

import com.android.settings.R;
import com.android.settingslib.guardtalk.GuardTalkUsbProtectionKeys;

/**
 * Settings-side GuardTalk USB protection helper (T-SEC-P2-USB).
 *
 * <p>Overlay bool OR product prop ⇒ enabled (fail-closed toward restriction).
 * Server enforcement uses props via {@link GuardTalkUsbProtectionPolicy}.
 *
 * <p>See {@code vendor/guardtalk/docs/USB_PROTECTION_POLICY.md}.
 */
public final class GuardTalkUsbProtectionHelper {

    private GuardTalkUsbProtectionHelper() {}

    /** USB data denied when locked or pre-first-unlock. */
    public static boolean isFailClosedEnabled(@NonNull Context context) {
        if (context.getResources().getBoolean(
                R.bool.config_guardtalk_usb_protection_fail_closed)) {
            return true;
        }
        return SystemProperties.getBoolean(
                GuardTalkUsbProtectionKeys.PROP_USB_PROTECTION_FAIL_CLOSED, false);
    }

    /** True when USB data is currently force-denied by policy. */
    public static boolean isUsbDataForceDenied(@NonNull Context context) {
        if (!isFailClosedEnabled(context)) {
            return false;
        }
        final int userId = context.getUserId();
        final KeyguardManager km = context.getSystemService(KeyguardManager.class);
        final UserManager um = context.getSystemService(UserManager.class);
        // Fail-closed: missing services ⇒ treat as locked / not unlocked.
        final boolean deviceLocked = km == null || km.isDeviceLocked(userId);
        final boolean userUnlocked = um != null && um.isUserUnlocked(userId);
        return GuardTalkUsbProtectionPolicy.mustDenyUsbData(deviceLocked, userUnlocked);
    }
}
