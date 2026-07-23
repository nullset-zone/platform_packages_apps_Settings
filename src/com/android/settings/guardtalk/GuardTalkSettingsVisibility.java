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

import androidx.annotation.BoolRes;
import androidx.annotation.NonNull;

import com.android.settings.R;
import com.android.settingslib.guardtalk.GuardTalkSettingsVisibilityKeys;

/**
 * Runtime helper for GuardTalk Main Settings keep/hide policy (T-SEC-P1-SETTINGS).
 *
 * <p>Reads overlayable {@code config_show_top_level_*} bools. Frontend task
 * {@code F-SEC-P1-SETTINGS-UI} sets keep/hide values via
 * {@code GuardTalkSettingsOverlay}.
 */
public final class GuardTalkSettingsVisibility {

    private GuardTalkSettingsVisibility() {}

    /** Returns whether a top-level Settings preference key should be shown. */
    public static boolean isTopLevelShown(@NonNull Context context, @NonNull String preferenceKey) {
        final int boolRes = boolResForKey(preferenceKey);
        if (boolRes == 0) {
            // Unknown key: fail open (show) so unrelated preferences are not hidden.
            return true;
        }
        return context.getResources().getBoolean(boolRes);
    }

    /** Convenience for controllers: AVAILABLE vs UNSUPPORTED_ON_DEVICE mapping helper. */
    public static boolean isConfigEnabled(@NonNull Context context, @BoolRes int boolRes) {
        return context.getResources().getBoolean(boolRes);
    }

    public static boolean useGuardTalkSecurityDashboard(@NonNull Context context) {
        return context.getResources().getBoolean(R.bool.config_use_guardtalk_security_dashboard);
    }

    @BoolRes
    private static int boolResForKey(@NonNull String preferenceKey) {
        switch (preferenceKey) {
            case GuardTalkSettingsVisibilityKeys.PREF_TOP_LEVEL_NETWORK:
                return R.bool.config_show_top_level_network;
            case GuardTalkSettingsVisibilityKeys.PREF_TOP_LEVEL_CONNECTED_DEVICES:
                return R.bool.config_show_top_level_connected_devices;
            case GuardTalkSettingsVisibilityKeys.PREF_TOP_LEVEL_APPS:
                return R.bool.config_show_top_level_apps;
            case GuardTalkSettingsVisibilityKeys.PREF_TOP_LEVEL_NOTIFICATIONS:
                return R.bool.config_show_top_level_notifications;
            case GuardTalkSettingsVisibilityKeys.PREF_TOP_LEVEL_SOUND:
                return R.bool.config_show_top_level_sound;
            case GuardTalkSettingsVisibilityKeys.PREF_TOP_LEVEL_PRIORITY_MODES:
                return R.bool.config_show_top_level_priority_modes;
            case GuardTalkSettingsVisibilityKeys.PREF_TOP_LEVEL_DISPLAY:
                return R.bool.config_show_top_level_display;
            case GuardTalkSettingsVisibilityKeys.PREF_TOP_LEVEL_STORAGE:
                return R.bool.config_show_top_level_storage;
            case GuardTalkSettingsVisibilityKeys.PREF_TOP_LEVEL_BATTERY:
                return R.bool.config_show_top_level_battery;
            case GuardTalkSettingsVisibilityKeys.PREF_TOP_LEVEL_SYSTEM:
                return R.bool.config_show_top_level_system;
            case GuardTalkSettingsVisibilityKeys.PREF_TOP_LEVEL_ABOUT_DEVICE:
                return R.bool.config_show_top_level_about_device;
            case GuardTalkSettingsVisibilityKeys.PREF_TOP_LEVEL_SAFETY_CENTER:
                return R.bool.config_show_top_level_safety_center;
            case GuardTalkSettingsVisibilityKeys.PREF_TOP_LEVEL_SECURITY:
                return R.bool.config_show_top_level_security;
            case GuardTalkSettingsVisibilityKeys.PREF_TOP_LEVEL_PRIVACY:
                return R.bool.config_show_top_level_privacy;
            case GuardTalkSettingsVisibilityKeys.PREF_TOP_LEVEL_LOCATION:
                return R.bool.config_show_top_level_location;
            case GuardTalkSettingsVisibilityKeys.PREF_TOP_LEVEL_ACCOUNTS:
                return R.bool.config_show_top_level_accounts;
            case GuardTalkSettingsVisibilityKeys.PREF_TOP_LEVEL_EMERGENCY:
                return R.bool.config_show_emergency_settings;
            case GuardTalkSettingsVisibilityKeys.PREF_TOP_LEVEL_ACCESSIBILITY:
                return R.bool.config_show_top_level_accessibility;
            default:
                return 0;
        }
    }
}
