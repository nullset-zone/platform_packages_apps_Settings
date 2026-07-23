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

import android.app.Activity;
import android.app.KeyguardManager;
import android.content.Context;
import android.content.Intent;
import android.guardtalk.GuardTalkConfigGateManager;
import android.guardtalk.GuardTalkConfigMutations;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

/**
 * Settings-side helper for the GT Config password gate.
 *
 * <p>Frontend (`F-SEC-P1-ABOUT-APPS` / GT Config UX) should:
 * <ol>
 *   <li>Launch {@link #createConfirmIntent} via fragment startActivityForResult</li>
 *   <li>On {@link Activity#RESULT_OK}, call {@link #onCredentialConfirmed}</li>
 *   <li>Before Security mutations, call {@link #assertAuthorized} or
 *       {@link GuardTalkConfigGateManager#isMutationAuthorized}</li>
 * </ol>
 *
 * @see vendor/guardtalk/docs/GT_CONFIG_PASSWORD_GATE_API.md
 */
public final class GuardTalkConfigGateClient {

    private static final String TAG = "GtConfigGateClient";

    /** Request code for GT Config / maintenance credential confirmation. */
    public static final int REQUEST_CONFIRM_FOR_GT_CONFIG = 7601;

    private GuardTalkConfigGateClient() {}

    /**
     * Builds a confirm-device-credential intent, or null when no secure lock
     * is set (fail-closed — caller must not authorize).
     */
    @Nullable
    public static Intent createConfirmIntent(@NonNull Context context) {
        final KeyguardManager km = context.getSystemService(KeyguardManager.class);
        if (km == null) {
            Log.e(TAG, "KeyguardManager missing");
            return null;
        }
        if (!km.isDeviceSecure()) {
            Log.w(TAG, "Device has no secure lock — cannot open GT Config session");
            return null;
        }
        return km.createConfirmDeviceCredentialIntent(
                context.getString(com.android.settings.R.string.guardtalk_gt_config_title),
                context.getString(com.android.settings.R.string.guardtalk_gt_config_confirm_details));
    }

    /** Starts credential confirmation from a Settings fragment. */
    public static boolean startConfirm(@NonNull Fragment fragment) {
        final Intent intent = createConfirmIntent(fragment.requireContext());
        if (intent == null) {
            return false;
        }
        fragment.startActivityForResult(intent, REQUEST_CONFIRM_FOR_GT_CONFIG);
        return true;
    }

    /**
     * Handles activity result from {@link #REQUEST_CONFIRM_FOR_GT_CONFIG}.
     *
     * @return true if the request code was consumed
     */
    public static boolean handleActivityResult(
            @NonNull Context context, int requestCode, int resultCode) {
        if (requestCode != REQUEST_CONFIRM_FOR_GT_CONFIG) {
            return false;
        }
        if (resultCode == Activity.RESULT_OK) {
            final boolean opened = GuardTalkConfigGateManager.onDeviceCredentialConfirmed(context);
            if (!opened) {
                Log.e(TAG, "Credential OK but gate refused session (fail-closed)");
            }
        }
        return true;
    }

    /**
     * Fail-closed assert before applying a Security / GT Config mutation from Settings.
     *
     * @throws SecurityException when unauthorized
     */
    public static void assertAuthorized(@NonNull Context context, @NonNull String mutationKey) {
        GuardTalkConfigGateManager.assertMutationAuthorized(context.getUserId(), mutationKey);
    }

    /** Convenience for maintenance-gated Apps/Network entries. */
    public static boolean isMaintenanceAuthorized(@NonNull Context context) {
        return GuardTalkConfigGateManager.isMutationAuthorized(
                context.getUserId(), GuardTalkConfigMutations.MAINTENANCE_ACCESS);
    }
}
