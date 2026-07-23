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

/**
 * Apps-menu keep/hide + maintenance-gate policy (F-SEC-P1-ABOUT-APPS).
 *
 * <p>Overlay via {@code GuardTalkSettingsOverlay}. Unknown keys fail open.
 */
public final class GuardTalkAppsVisibility {

    public static final String KEY_DEFAULT_APPS = "default_apps";
    public static final String KEY_SPECIAL_ACCESS = "special_access";
    public static final String KEY_ASPECT_RATIO_APPS = "aspect_ratio_apps";
    public static final String KEY_HIBERNATED_APPS = "hibernated_apps";
    public static final String KEY_APP_BATTERY_USAGE = "app_battery_usage";

    private GuardTalkAppsVisibility() {}

    public static boolean isShown(@NonNull Context context, @NonNull String preferenceKey) {
        final int boolRes = boolResForKey(preferenceKey);
        if (boolRes == 0) {
            return true;
        }
        return context.getResources().getBoolean(boolRes);
    }

    /** True when Special app access requires a GT Config maintenance session. */
    public static boolean specialAccessRequiresMaintenance(@NonNull Context context) {
        return context.getResources()
                .getBoolean(R.bool.config_apps_special_access_requires_maintenance);
    }

    @BoolRes
    private static int boolResForKey(@NonNull String preferenceKey) {
        switch (preferenceKey) {
            case KEY_DEFAULT_APPS:
                return R.bool.config_show_apps_default_apps;
            case KEY_SPECIAL_ACCESS:
                return R.bool.config_show_apps_special_access;
            case KEY_ASPECT_RATIO_APPS:
                return R.bool.config_show_apps_aspect_ratio;
            case KEY_HIBERNATED_APPS:
                return R.bool.config_show_apps_hibernated;
            case KEY_APP_BATTERY_USAGE:
                return R.bool.config_show_apps_battery_usage;
            default:
                return 0;
        }
    }
}
