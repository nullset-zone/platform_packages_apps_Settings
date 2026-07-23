/*
 * Copyright (C) 2016 The Android Open Source Project
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

package com.android.settings.applications.specialaccess;

import static android.app.admin.DevicePolicyResources.Strings.Settings.CONNECTED_WORK_AND_PERSONAL_APPS_TITLE;
import static android.app.admin.DevicePolicyResources.Strings.Settings.MANAGE_DEVICE_ADMIN_APPS;

import android.app.Activity;
import android.app.settings.SettingsEnums;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.android.settings.R;
import com.android.settings.dashboard.DashboardFragment;
import com.android.settings.guardtalk.GuardTalkAppsVisibility;
import com.android.settings.guardtalk.GuardTalkConfigGateClient;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settingslib.search.SearchIndexable;

/**
 * Special app access dashboard.
 *
 * <p>F-SEC-P1-ABOUT-APPS: when
 * {@code config_apps_special_access_requires_maintenance} is true, deep links /
 * search launches must confirm the device credential and open a GT Config
 * maintenance session before this page stays open (fail-closed).
 */
// LINT.IfChange
@SearchIndexable
public class SpecialAccessSettings extends DashboardFragment {

    private static final String TAG = "SpecialAccessSettings";

    private boolean mAwaitingMaintenanceConfirm;

    @Override
    protected String getLogTag() {
        return TAG;
    }

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        replaceEnterpriseStringTitle("interact_across_profiles",
                CONNECTED_WORK_AND_PERSONAL_APPS_TITLE, R.string.interact_across_profiles_title);
        replaceEnterpriseStringTitle("device_administrators",
                MANAGE_DEVICE_ADMIN_APPS, R.string.manage_device_admin);
        maybeRequestMaintenanceGate();
    }

    @Override
    public void onResume() {
        super.onResume();
        // Session may have expired while backgrounded — re-gate fail-closed.
        if (!mAwaitingMaintenanceConfirm
                && GuardTalkAppsVisibility.specialAccessRequiresMaintenance(requireContext())
                && !GuardTalkConfigGateClient.isMaintenanceAuthorized(requireContext())) {
            maybeRequestMaintenanceGate();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (GuardTalkConfigGateClient.handleActivityResult(
                requireContext(), requestCode, resultCode)) {
            mAwaitingMaintenanceConfirm = false;
            if (resultCode == Activity.RESULT_OK
                    && GuardTalkConfigGateClient.isMaintenanceAuthorized(requireContext())) {
                return;
            }
            Toast.makeText(getContext(), R.string.guardtalk_maintenance_toast_denied,
                    Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    /**
     * Blocks Special Access when opened outside the Apps preference click path
     * (search, {@code SpecialAccessSettingsActivity}, etc.).
     */
    private void maybeRequestMaintenanceGate() {
        final Context context = requireContext();
        if (!GuardTalkAppsVisibility.specialAccessRequiresMaintenance(context)) {
            return;
        }
        if (GuardTalkConfigGateClient.isMaintenanceAuthorized(context)) {
            return;
        }
        if (mAwaitingMaintenanceConfirm) {
            return;
        }
        mAwaitingMaintenanceConfirm = true;
        if (!GuardTalkConfigGateClient.startConfirm(this)) {
            mAwaitingMaintenanceConfirm = false;
            Toast.makeText(context, R.string.guardtalk_maintenance_toast_denied,
                    Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    @Override
    protected int getPreferenceScreenResId() {
        return R.xml.special_access;
    }

    @Override
    public int getMetricsCategory() {
        return SettingsEnums.SPECIAL_ACCESS;
    }

    @Override
    public @Nullable String getPreferenceScreenBindingKey(@NonNull Context context) {
        return SpecialAccessSettingsScreen.KEY;
    }

    public static final BaseSearchIndexProvider SEARCH_INDEX_DATA_PROVIDER =
            new BaseSearchIndexProvider(R.xml.special_access) {
                @Override
                protected boolean isPageSearchEnabled(Context context) {
                    return GuardTalkAppsVisibility.isShown(
                            context, GuardTalkAppsVisibility.KEY_SPECIAL_ACCESS);
                }
            };
}
// LINT.ThenChange(SpecialAccessSettingsScreen.kt)
