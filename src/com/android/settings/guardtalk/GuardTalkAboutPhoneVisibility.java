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
 * About-phone keep/hide policy hooks (T-SEC-P1-DEVOPTS / F-SEC-P1-ABOUT-APPS).
 *
 * <p>Maps {@code my_device_info.xml} preference keys to overlayable config
 * bools. Frontend sets values via {@code GuardTalkSettingsOverlay}. Unknown
 * keys fail open (shown).
 *
 * <p>See {@code vendor/guardtalk/docs/ABOUT_PHONE_DEVOPTS_POLICY.md}.
 */
public final class GuardTalkAboutPhoneVisibility {

    public static final String KEY_DEVICE_NAME = "device_name";
    public static final String KEY_BRANDED_ACCOUNT = "branded_account";
    public static final String KEY_PHONE_NUMBER = "phone_number";
    public static final String KEY_DEVICE_MODEL = "device_model";
    public static final String KEY_FIRMWARE_VERSION = "firmware_version";
    public static final String KEY_BATTERY_INFO = "battery_info";
    public static final String KEY_WIFI_IP_ADDRESS = "wifi_ip_address";
    public static final String KEY_WIFI_MAC_ADDRESS = "wifi_mac_address";
    public static final String KEY_SAVED_WIFI_MAC = "saved_accesspoints_wifi_mac_address";
    public static final String KEY_MANUAL = "manual";
    public static final String KEY_BUILD_NUMBER = "build_number";
    public static final String KEY_SIM_STATUS = "sim_status";
    public static final String KEY_EID_INFO = "eid_info";
    public static final String KEY_IMEI_INFO = "imei_info";
    public static final String KEY_LEGAL_CONTAINER = "legal_container";
    public static final String KEY_REGULATORY_INFO = "regulatory_info";
    public static final String KEY_SAFETY_INFO = "safety_info";
    public static final String KEY_BT_ADDRESS = "bt_address";
    public static final String KEY_UP_TIME = "up_time";
    public static final String KEY_DEVICE_FEEDBACK = "device_feedback";
    public static final String KEY_FCC_EQUIPMENT_ID = "fcc_equipment_id";

    private GuardTalkAboutPhoneVisibility() {}

    /** Returns whether an About-phone preference key should be shown. */
    public static boolean isShown(@NonNull Context context, @NonNull String preferenceKey) {
        final int boolRes = boolResForKey(preferenceKey);
        if (boolRes == 0) {
            return true;
        }
        return context.getResources().getBoolean(boolRes);
    }

    public static boolean isBuildNumberShown(@NonNull Context context) {
        return context.getResources().getBoolean(R.bool.config_show_about_build_number);
    }

    @BoolRes
    private static int boolResForKey(@NonNull String preferenceKey) {
        switch (preferenceKey) {
            case KEY_DEVICE_NAME:
                return R.bool.config_show_device_name;
            case KEY_BRANDED_ACCOUNT:
                return R.bool.config_show_branded_account_in_device_info;
            case KEY_PHONE_NUMBER:
            case KEY_SIM_STATUS:
            case KEY_EID_INFO:
            case KEY_IMEI_INFO:
                return R.bool.config_show_sim_info;
            case KEY_DEVICE_MODEL:
                return R.bool.config_show_device_model;
            case KEY_FIRMWARE_VERSION:
                return R.bool.config_show_about_firmware_version;
            case KEY_BATTERY_INFO:
                return R.bool.config_show_about_battery_info;
            case KEY_WIFI_IP_ADDRESS:
                return R.bool.config_show_wifi_ip_address;
            case KEY_WIFI_MAC_ADDRESS:
            case KEY_SAVED_WIFI_MAC:
                return R.bool.config_show_wifi_mac_address;
            case KEY_MANUAL:
                return R.bool.config_show_manual;
            case KEY_BUILD_NUMBER:
                return R.bool.config_show_about_build_number;
            case KEY_LEGAL_CONTAINER:
                return R.bool.config_show_about_legal_info;
            case KEY_REGULATORY_INFO:
                return R.bool.config_show_regulatory_info;
            case KEY_SAFETY_INFO:
                return R.bool.config_show_about_safety_info;
            case KEY_BT_ADDRESS:
                return R.bool.config_show_about_bt_address;
            case KEY_UP_TIME:
                return R.bool.config_show_about_uptime;
            case KEY_DEVICE_FEEDBACK:
                return R.bool.config_show_about_device_feedback;
            case KEY_FCC_EQUIPMENT_ID:
                return R.bool.config_show_about_fcc_equipment_id;
            default:
                return 0;
        }
    }
}
