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
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.android.settings.R;
import com.android.settingslib.drawer.Tile;

/**
 * GuardTalk policy for Privacy dashboard hides (T-UIHIDE-KEYS).
 *
 * <p>Hides Health Connect ("Health and fitness" / locale variants) injected
 * IA_SETTINGS tile from Privacy UI and Settings search indexing of injected
 * tiles. Does not uninstall Health Connect / HealthFitness APEX or services.
 *
 * <p>See {@code vendor/guardtalk/docs/SETTINGS_VISIBILITY_POLICY.md}.
 */
public final class GuardTalkPrivacyVisibility {

    /** Health Connect Settings controller package (IA_SETTINGS injector). */
    public static final String PACKAGE_HEALTH_CONNECT_CONTROLLER =
            "com.android.healthconnect.controller";

    private GuardTalkPrivacyVisibility() {}

    /** Returns true when Health Connect Privacy entry must be hidden. */
    public static boolean isHealthConnectUiHidden(@NonNull Context context) {
        return !context.getResources().getBoolean(R.bool.config_show_health_connect_settings);
    }

    /**
     * Returns true when an injected dashboard {@code tile} must be omitted
     * from Privacy / More Security &amp; Privacy UI.
     */
    public static boolean shouldHideInjectedTile(
            @NonNull Context context, @Nullable Tile tile) {
        if (tile == null || !isHealthConnectUiHidden(context)) {
            return false;
        }
        return shouldHidePackage(context, tile.getPackageName());
    }

    /**
     * Returns true when {@code packageName} must be omitted from Settings
     * injected-tile search while Health Connect UI suppression is active.
     */
    public static boolean shouldHidePackage(
            @NonNull Context context, @Nullable String packageName) {
        if (TextUtils.isEmpty(packageName) || !isHealthConnectUiHidden(context)) {
            return false;
        }
        return PACKAGE_HEALTH_CONNECT_CONTROLLER.equals(packageName);
    }
}
