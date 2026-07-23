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

package com.android.settings.security.guardtalk;

import android.app.Activity;
import android.app.settings.SettingsEnums;
import android.content.Context;
import android.content.Intent;
import android.guardtalk.GuardTalkConfigGateManager;
import android.guardtalk.GuardTalkConfigMutations;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.preference.Preference;

import com.android.settings.R;
import com.android.settings.dashboard.DashboardFragment;
import com.android.settings.guardtalk.GuardTalkConfigGateClient;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settingslib.search.SearchIndexable;

import java.util.HashMap;
import java.util.Map;

/**
 * GuardTalkOS Security dashboard (T-SEC-P1-SETTINGS / F-SEC-P2 / F-SEC-P4).
 *
 * <p>Hosts Security surfaces and the GT Config password gate entry. Must not
 * host network controls. When
 * {@code config_security_mutations_require_password} is true, Security
 * mutation rows require a main-device-password session before opening
 * (F-SEC-P4-SYSTEM-UI).
 */
@SearchIndexable
public class GuardTalkSecurityDashboardFragment extends DashboardFragment {

    private static final String TAG = "GuardTalkSecurityDash";
    private static final String KEY_GT_CONFIG = "guardtalk_gt_config";
    private static final String GT_CONFIG_PACKAGE = "com.guardtalk.config";
    private static final String GT_CONFIG_ACTIVITY = "com.guardtalk.config.ConfigActivity";

    private static final Map<String, String> MUTATION_KEYS = new HashMap<>();

    static {
        MUTATION_KEYS.put("guardtalk_security_device_lock",
                GuardTalkConfigMutations.SECURITY_DEVICE_LOCK);
        MUTATION_KEYS.put("guardtalk_security_sensor_privacy",
                GuardTalkConfigMutations.SECURITY_SENSOR_PRIVACY);
        MUTATION_KEYS.put("guardtalk_security_usb_protection",
                GuardTalkConfigMutations.SECURITY_USB_PROTECTION);
        MUTATION_KEYS.put("guardtalk_security_lockdown",
                GuardTalkConfigMutations.SECURITY_LOCKDOWN);
        MUTATION_KEYS.put("guardtalk_security_auto_reboot",
                GuardTalkConfigMutations.SECURITY_AUTO_REBOOT);
        MUTATION_KEYS.put("guardtalk_security_duress",
                GuardTalkConfigMutations.SECURITY_DURESS_CONFIG);
        MUTATION_KEYS.put("guardtalk_security_secure_wipe",
                GuardTalkConfigMutations.SECURITY_SECURE_WIPE);
    }

    private boolean mLaunchGtConfigAfterConfirm;
    @Nullable private String mPendingMutationPreferenceKey;

    @Override
    public int getMetricsCategory() {
        return SettingsEnums.SECURITY;
    }

    @Override
    protected int getPreferenceScreenResId() {
        return R.xml.guardtalk_security_dashboard;
    }

    @Override
    protected String getLogTag() {
        return TAG;
    }

    @Override
    public int getHelpResource() {
        return R.string.help_url_security;
    }

    @Override
    public void onResume() {
        super.onResume();
        // Refresh GT Config locked/unlocked summary after session TTL changes.
        updatePreferenceStates();
    }

    @Override
    public boolean onPreferenceTreeClick(Preference preference) {
        final String key = preference.getKey();
        if (KEY_GT_CONFIG.equals(key)) {
            return handleGtConfigClick();
        }
        if (mutationsRequirePassword() && MUTATION_KEYS.containsKey(key)) {
            return handleMutationClick(preference, key);
        }
        return super.onPreferenceTreeClick(preference);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (GuardTalkConfigGateClient.handleActivityResult(
                requireContext(), requestCode, resultCode)) {
            if (resultCode == Activity.RESULT_OK) {
                Toast.makeText(getContext(), R.string.guardtalk_gt_config_toast_unlocked,
                        Toast.LENGTH_SHORT).show();
                if (mLaunchGtConfigAfterConfirm) {
                    mLaunchGtConfigAfterConfirm = false;
                    launchGtConfig(requireContext());
                } else if (mPendingMutationPreferenceKey != null) {
                    final String pending = mPendingMutationPreferenceKey;
                    mPendingMutationPreferenceKey = null;
                    final Preference pref = findPreference(pending);
                    if (pref != null && isMutationAuthorized(pending)) {
                        // Session open — let controllers handle the real navigation.
                        super.onPreferenceTreeClick(pref);
                    } else {
                        Toast.makeText(getContext(), R.string.guardtalk_gt_config_toast_denied,
                                Toast.LENGTH_SHORT).show();
                    }
                }
            } else {
                mLaunchGtConfigAfterConfirm = false;
                mPendingMutationPreferenceKey = null;
            }
            updatePreferenceStates();
            return;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private boolean handleGtConfigClick() {
        final Context context = requireContext();
        if (GuardTalkConfigGateManager.isAuthorized(context)) {
            launchGtConfig(context);
            return true;
        }
        mLaunchGtConfigAfterConfirm = true;
        mPendingMutationPreferenceKey = null;
        if (!GuardTalkConfigGateClient.startConfirm(this)) {
            mLaunchGtConfigAfterConfirm = false;
            Toast.makeText(context, R.string.guardtalk_gt_config_toast_denied,
                    Toast.LENGTH_SHORT).show();
        }
        return true;
    }

    private boolean handleMutationClick(Preference preference, String key) {
        if (isMutationAuthorized(key)) {
            return super.onPreferenceTreeClick(preference);
        }
        mPendingMutationPreferenceKey = key;
        mLaunchGtConfigAfterConfirm = false;
        if (!GuardTalkConfigGateClient.startConfirm(this)) {
            mPendingMutationPreferenceKey = null;
            Toast.makeText(requireContext(), R.string.guardtalk_gt_config_toast_denied,
                    Toast.LENGTH_SHORT).show();
        }
        return true;
    }

    private boolean mutationsRequirePassword() {
        return requireContext().getResources().getBoolean(
                R.bool.config_security_mutations_require_password);
    }

    private boolean isMutationAuthorized(String preferenceKey) {
        final String mutation = MUTATION_KEYS.get(preferenceKey);
        if (mutation == null) {
            return false;
        }
        try {
            GuardTalkConfigGateClient.assertAuthorized(requireContext(), mutation);
            return true;
        } catch (SecurityException e) {
            return false;
        }
    }

    /**
     * Opens GuardTalkConfig after a verified gate session. Fail-closed if the
     * session cannot authorize {@code gt_config_write}.
     */
    private void launchGtConfig(Context context) {
        try {
            GuardTalkConfigGateClient.assertAuthorized(
                    context, GuardTalkConfigMutations.GT_CONFIG_WRITE);
        } catch (SecurityException e) {
            Log.w(TAG, "GT Config launch blocked (fail-closed)", e);
            Toast.makeText(context, R.string.guardtalk_gt_config_toast_denied,
                    Toast.LENGTH_SHORT).show();
            return;
        }
        final Intent intent = new Intent();
        intent.setClassName(GT_CONFIG_PACKAGE, GT_CONFIG_ACTIVITY);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        try {
            startActivity(intent);
        } catch (Exception e) {
            Log.e(TAG, "Failed to launch GuardTalkConfig", e);
            Toast.makeText(context, R.string.guardtalk_gt_config_toast_launch_failed,
                    Toast.LENGTH_SHORT).show();
        }
    }

    public static final BaseSearchIndexProvider SEARCH_INDEX_DATA_PROVIDER =
            new BaseSearchIndexProvider(R.xml.guardtalk_security_dashboard) {
                @Override
                protected boolean isPageSearchEnabled(Context context) {
                    return context.getResources().getBoolean(
                            R.bool.config_use_guardtalk_security_dashboard)
                            && context.getResources().getBoolean(
                                    R.bool.config_show_top_level_security);
                }
            };
}
