/*
 * Copyright (C) 2021 The Android Open Source Project
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

package com.android.settings.security;

import android.content.Context;

import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;

import com.android.settings.R;
import com.android.settings.security.guardtalk.GuardTalkSecurityDashboardFragment;
import com.android.settingslib.guardtalk.GuardTalkSettingsVisibilityKeys;

/** Implementation for {@code SecuritySettingsFeatureProvider}. */
public class SecuritySettingsFeatureProviderImpl implements SecuritySettingsFeatureProvider {

    private final boolean mUseGuardTalkSecurityDashboard;

    public SecuritySettingsFeatureProviderImpl() {
        this(false);
    }

    public SecuritySettingsFeatureProviderImpl(Context context) {
        this(context.getResources().getBoolean(R.bool.config_use_guardtalk_security_dashboard));
    }

    @VisibleForTesting
    SecuritySettingsFeatureProviderImpl(boolean useGuardTalkSecurityDashboard) {
        mUseGuardTalkSecurityDashboard = useGuardTalkSecurityDashboard;
    }

    @Override
    public boolean hasAlternativeSecuritySettingsFragment() {
        return mUseGuardTalkSecurityDashboard;
    }

    @Override
    @Nullable
    public String getAlternativeSecuritySettingsFragmentClassname() {
        if (!mUseGuardTalkSecurityDashboard) {
            return null;
        }
        return GuardTalkSettingsVisibilityKeys.GUARDTALK_SECURITY_DASHBOARD_FRAGMENT;
    }

    @Override
    @Nullable
    public String getAlternativeAdvancedSettingsCategoryKey() {
        return null;
    }

    /** Package-visible for tests that assert the fragment class name. */
    @VisibleForTesting
    static String guardTalkFragmentClassName() {
        return GuardTalkSecurityDashboardFragment.class.getName();
    }
}
