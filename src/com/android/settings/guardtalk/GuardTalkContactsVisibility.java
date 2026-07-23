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
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.android.settings.R;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * GuardTalk policy for Contacts / Contacts Storage UI suppression
 * (T-SEC-P1-CONTACTS).
 *
 * <p>When enabled, Contacts and Contacts Storage are omitted from Settings
 * Apps lists, Settings search, and the Contacts storage preference. Packages
 * remain installed ({@code ContactsProvider} / Contacts APKs are not removed).
 *
 * <p>See {@code vendor/guardtalk/docs/CONTACTS_UI_SUPPRESSION.md}.
 */
public final class GuardTalkContactsVisibility {

    /** Product property reinforcement (tokay GuardTalk layer). */
    public static final String PROP_HIDE_CONTACTS_UI = "ro.guardtalk.hide_contacts_ui";

    public static final String PACKAGE_CONTACTS = "com.android.contacts";
    public static final String PACKAGE_CONTACTS_STORAGE = "com.android.providers.contacts";

    private GuardTalkContactsVisibility() {}

    /** Returns true when GuardTalk policy hides Contacts UI surfaces. */
    public static boolean isUiHidden(@NonNull Context context) {
        if (context.getResources().getBoolean(R.bool.config_guardtalk_hide_contacts_ui)) {
            return true;
        }
        return SystemProperties.getBoolean(PROP_HIDE_CONTACTS_UI, false);
    }

    /**
     * Returns true when {@code packageName} must be omitted from Settings app
     * lists / Show all apps while Contacts UI suppression is active.
     */
    public static boolean shouldHidePackage(
            @NonNull Context context, @Nullable String packageName) {
        if (TextUtils.isEmpty(packageName) || !isUiHidden(context)) {
            return false;
        }
        return getHiddenPackages(context).contains(packageName);
    }

    /** Packages suppressed from Settings app lists when UI hide is active. */
    @NonNull
    public static Set<String> getHiddenPackages(@NonNull Context context) {
        if (!isUiHidden(context)) {
            return Collections.emptySet();
        }
        final String[] configured =
                context.getResources()
                        .getStringArray(R.array.config_guardtalk_hidden_contacts_packages);
        if (configured == null || configured.length == 0) {
            return defaultHiddenPackages();
        }
        return new HashSet<>(Arrays.asList(configured));
    }

    @NonNull
    private static Set<String> defaultHiddenPackages() {
        final Set<String> packages = new HashSet<>(2);
        packages.add(PACKAGE_CONTACTS);
        packages.add(PACKAGE_CONTACTS_STORAGE);
        return packages;
    }
}
