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

import android.app.settings.SettingsEnums;
import android.content.Context;
import android.guardtalk.GuardTalkSecurityStatus.Snapshot;
import android.os.Bundle;

import androidx.preference.Preference;

import com.android.settings.R;
import com.android.settings.dashboard.DashboardFragment;
import com.android.settings.guardtalk.GuardTalkSecurityStatusHelper;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settingslib.guardtalk.GuardTalkSecurityStatusKeys;
import com.android.settingslib.search.SearchIndexable;

/**
 * Post-unlock Security status (F-SEC-P5-STATUS-UI).
 *
 * <p>Binds live summaries from {@link GuardTalkSecurityStatusHelper#collect}.
 * Finishes when pre-unlock. <strong>Never includes Duress</strong> — Duress must
 * not appear as lock-screen-visible status. No network rows.
 */
@SearchIndexable
public class GuardTalkSecurityStatusFragment extends DashboardFragment {

    private static final String TAG = "GtSecurityStatus";

    @Override
    public int getMetricsCategory() {
        return SettingsEnums.SECURITY;
    }

    @Override
    protected int getPreferenceScreenResId() {
        return R.xml.guardtalk_security_status;
    }

    @Override
    protected String getLogTag() {
        return TAG;
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        super.onCreatePreferences(savedInstanceState, rootKey);
        if (!GuardTalkSecurityStatusHelper.isPostUnlockStatusAllowed(requireContext())) {
            finish();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        final Context context = requireContext();
        if (!GuardTalkSecurityStatusHelper.isPostUnlockStatusAllowed(context)) {
            finish();
            return;
        }
        refreshStatusSummaries(context);
    }

    /**
     * Bind live summaries from the aggregate snapshot. Intentionally omits Duress.
     * Uses every Snapshot field except Duress (none exist by API contract).
     */
    private void refreshStatusSummaries(Context context) {
        final Snapshot s = GuardTalkSecurityStatusHelper.collect(context);
        if (!s.postUnlockAllowed) {
            finish();
            return;
        }

        setSummary(GuardTalkSecurityStatusKeys.PREF_STATUS_DEVICE_LOCK,
                s.passwordOnlyLock
                        ? R.string.guardtalk_security_device_lock_summary_password_only
                        : R.string.guardtalk_security_status_inactive);

        if (s.sensorsForceDenied) {
            setSummary(GuardTalkSecurityStatusKeys.PREF_STATUS_SENSOR_PRIVACY,
                    R.string.guardtalk_security_sensor_privacy_summary_locked);
        } else if (s.sensorPrivacyPolicy) {
            setSummary(GuardTalkSecurityStatusKeys.PREF_STATUS_SENSOR_PRIVACY,
                    R.string.guardtalk_security_sensor_privacy_summary_policy);
        } else {
            setSummary(GuardTalkSecurityStatusKeys.PREF_STATUS_SENSOR_PRIVACY,
                    R.string.guardtalk_security_status_inactive);
        }

        if (s.usbDataForceDenied) {
            setSummary(GuardTalkSecurityStatusKeys.PREF_STATUS_USB_PROTECTION,
                    R.string.guardtalk_security_usb_protection_summary_locked);
        } else if (s.usbProtectionPolicy) {
            setSummary(GuardTalkSecurityStatusKeys.PREF_STATUS_USB_PROTECTION,
                    R.string.guardtalk_security_usb_protection_summary_policy);
        } else {
            setSummary(GuardTalkSecurityStatusKeys.PREF_STATUS_USB_PROTECTION,
                    R.string.guardtalk_security_status_inactive);
        }

        if (s.lockdownActive) {
            setSummary(GuardTalkSecurityStatusKeys.PREF_STATUS_LOCKDOWN,
                    R.string.guardtalk_security_lockdown_summary_active);
        } else if (s.lockdownFailClosed) {
            setSummary(GuardTalkSecurityStatusKeys.PREF_STATUS_LOCKDOWN,
                    R.string.guardtalk_security_lockdown_summary_ready);
        } else {
            setSummary(GuardTalkSecurityStatusKeys.PREF_STATUS_LOCKDOWN,
                    R.string.guardtalk_security_status_inactive);
        }

        final Preference autoReboot =
                findPreference(GuardTalkSecurityStatusKeys.PREF_STATUS_AUTO_REBOOT);
        if (autoReboot != null) {
            if (!s.autoRebootProfiles || s.autoRebootHours <= 0) {
                autoReboot.setSummary(R.string.guardtalk_security_auto_reboot_summary_off);
            } else {
                autoReboot.setSummary(context.getResources().getQuantityString(
                        R.plurals.guardtalk_security_auto_reboot_summary_hours,
                        s.autoRebootHours, s.autoRebootHours));
            }
        }

        final Preference antiBrute =
                findPreference(GuardTalkSecurityStatusKeys.PREF_STATUS_ANTI_BRUTEFORCE);
        if (antiBrute != null) {
            if (s.antiBruteforceThreshold > 0) {
                antiBrute.setVisible(true);
                antiBrute.setSummary(context.getResources().getQuantityString(
                        R.plurals.guardtalk_security_anti_bruteforce_summary,
                        s.antiBruteforceThreshold, s.antiBruteforceThreshold));
            } else {
                antiBrute.setVisible(false);
            }
        }

        setSummary(GuardTalkSecurityStatusKeys.PREF_STATUS_SECURE_WIPE,
                s.secureWipeAvailable
                        ? R.string.guardtalk_security_status_secure_wipe_available
                        : R.string.guardtalk_security_status_unavailable);

        bindClipboardSummary(context, s);
        setSummary(GuardTalkSecurityStatusKeys.PREF_STATUS_PRIVACY_TMPFS,
                s.privacyTmpfs
                        ? R.string.guardtalk_security_privacy_tmpfs_summary
                        : R.string.guardtalk_security_status_inactive);
        bindFilesSummary(s);
        bindHardeningSummary(s);
    }

    private void bindClipboardSummary(Context context, Snapshot s) {
        final Preference clipboard =
                findPreference(GuardTalkSecurityStatusKeys.PREF_STATUS_CLIPBOARD);
        if (clipboard == null) {
            return;
        }
        if (!s.clipboardClear) {
            clipboard.setSummary(R.string.guardtalk_security_status_inactive);
            return;
        }
        final long timeoutMs = s.clipboardClearTimeoutMs;
        if (timeoutMs <= 0L) {
            clipboard.setSummary(R.string.guardtalk_security_clipboard_summary);
            return;
        }
        final int timeoutSec = (int) Math.max(1L, (timeoutMs + 999L) / 1000L);
        clipboard.setSummary(context.getResources().getQuantityString(
                R.plurals.guardtalk_security_clipboard_summary_timeout,
                timeoutSec, timeoutSec));
    }

    private void bindFilesSummary(Snapshot s) {
        if (!s.filesPolicy) {
            setSummary(GuardTalkSecurityStatusKeys.PREF_STATUS_FILES,
                    R.string.guardtalk_security_status_inactive);
            return;
        }
        if (s.filesTrash && s.filesCriticalProtect) {
            setSummary(GuardTalkSecurityStatusKeys.PREF_STATUS_FILES,
                    R.string.guardtalk_security_files_summary);
        } else if (s.filesTrash) {
            setSummary(GuardTalkSecurityStatusKeys.PREF_STATUS_FILES,
                    R.string.guardtalk_security_files_summary_trash);
        } else if (s.filesCriticalProtect) {
            setSummary(GuardTalkSecurityStatusKeys.PREF_STATUS_FILES,
                    R.string.guardtalk_security_files_summary_protect);
        } else {
            setSummary(GuardTalkSecurityStatusKeys.PREF_STATUS_FILES,
                    R.string.guardtalk_security_files_summary_policy);
        }
    }

    private void bindHardeningSummary(Snapshot s) {
        final Preference hardening =
                findPreference(GuardTalkSecurityStatusKeys.PREF_STATUS_HARDENING);
        if (hardening == null) {
            return;
        }
        if (!s.productionHardening) {
            hardening.setSummary(R.string.guardtalk_security_status_inactive);
            return;
        }
        final boolean production =
                s.productionProfile && s.debuggableOff;
        if (production && s.blockUnknownSources) {
            hardening.setSummary(
                    R.string.guardtalk_security_status_hardening_production_block);
        } else if (production) {
            hardening.setSummary(
                    R.string.guardtalk_security_status_hardening_production);
        } else if (s.blockUnknownSources) {
            hardening.setSummary(
                    R.string.guardtalk_security_status_hardening_enabled_block);
        } else {
            hardening.setSummary(
                    R.string.guardtalk_security_status_hardening_enabled);
        }
    }

    private void setSummary(String key, int summaryRes) {
        final Preference pref = findPreference(key);
        if (pref != null) {
            pref.setSummary(summaryRes);
        }
    }

    public static final BaseSearchIndexProvider SEARCH_INDEX_DATA_PROVIDER =
            new BaseSearchIndexProvider(R.xml.guardtalk_security_status) {
                @Override
                protected boolean isPageSearchEnabled(Context context) {
                    return context.getResources().getBoolean(
                            R.bool.config_use_guardtalk_security_dashboard)
                            && GuardTalkSecurityStatusHelper.isPostUnlockStatusAllowed(context);
                }
            };
}
