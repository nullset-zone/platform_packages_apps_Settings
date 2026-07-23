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

package com.android.settings.deviceinfo;

import android.content.Context;

import androidx.annotation.NonNull;

import com.android.settings.core.BasePreferenceController;
import com.android.settings.guardtalk.GuardTalkAboutPhoneVisibility;

/** About → Legal information keep/hide (F-SEC-P1-ABOUT-APPS). */
public class GuardTalkLegalInfoPreferenceController extends BasePreferenceController {

    public GuardTalkLegalInfoPreferenceController(
            @NonNull Context context, @NonNull String preferenceKey) {
        super(context, preferenceKey);
    }

    @Override
    public int getAvailabilityStatus() {
        return GuardTalkAboutPhoneVisibility.isShown(
                mContext, GuardTalkAboutPhoneVisibility.KEY_LEGAL_CONTAINER)
                ? AVAILABLE
                : UNSUPPORTED_ON_DEVICE;
    }
}
