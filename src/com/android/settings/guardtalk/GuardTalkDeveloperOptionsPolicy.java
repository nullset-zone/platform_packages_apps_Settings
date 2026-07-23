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
import android.os.SystemProperties;

import androidx.annotation.NonNull;

import com.android.settings.R;

/**
 * GuardTalk policy for Developer Options unlock (T-SEC-P1-DEVOPTS).
 *
 * <p>When blocked, About → Build number stays visible but seven taps must not
 * enable Developer Options. Product property
 * {@code ro.guardtalk.block_developer_options} reinforces the overlay bool.
 *
 * <p>See {@code vendor/guardtalk/docs/ABOUT_PHONE_DEVOPTS_POLICY.md}.
 */
public final class GuardTalkDeveloperOptionsPolicy {

    /** Product property (tokay GuardTalk layer). */
    public static final String PROP_BLOCK_DEVELOPER_OPTIONS =
            "ro.guardtalk.block_developer_options";

    private GuardTalkDeveloperOptionsPolicy() {}

    /**
     * Returns true when GuardTalk policy forbids unlocking / showing Developer
     * Options via Settings.
     */
    public static boolean isUnlockBlocked(@NonNull Context context) {
        if (context.getResources()
                .getBoolean(R.bool.config_guardtalk_block_developer_options_unlock)) {
            return true;
        }
        return SystemProperties.getBoolean(PROP_BLOCK_DEVELOPER_OPTIONS, false);
    }
}
