package com.android.settings.security;

import android.content.Context;
import android.ext.settings.ExtSettings;
import android.guardtalk.GuardTalkAutoRebootPolicy;

import androidx.preference.PreferenceScreen;

import com.android.settings.R;
import com.android.settings.ext.IntSettingPrefController;
import com.android.settings.ext.RadioButtonPickerFragment2;
import com.android.settings.guardtalk.GuardTalkAutoRebootHelper;

import static java.util.concurrent.TimeUnit.DAYS;
import static java.util.concurrent.TimeUnit.HOURS;
import static java.util.concurrent.TimeUnit.MINUTES;

public class AutoRebootPrefController extends IntSettingPrefController {

    public AutoRebootPrefController(Context ctx, String key) {
        super(ctx, key, ExtSettings.AUTO_REBOOT_TIMEOUT);
    }

    @Override
    public void addPrefsBeforeList(RadioButtonPickerFragment2 fragment, PreferenceScreen screen) {
        final int footer = GuardTalkAutoRebootHelper.isProfilesEnabled(mContext)
                ? R.string.guardtalk_auto_reboot_footer
                : R.string.auto_reboot_footer;
        addFooterPreference(screen, footer,
                "https://guardtalk.io/features#auto-reboot");
    }

    @Override
    protected void getEntries(Entries entries) {
        // GuardTalk profiles (design §12): Off, 1h, 2h, 4h, 8h only.
        if (GuardTalkAutoRebootHelper.isProfilesEnabled(mContext)) {
            entries.add(R.string.switch_off_text, 0);
            entries.add(1, HOURS);
            entries.add(2, HOURS);
            entries.add(4, HOURS);
            entries.add(8, HOURS);
            return;
        }
        entries.add(R.string.switch_off_text, 0);
        entries.add(3, DAYS);
        entries.add(2, DAYS);
        entries.add(36, HOURS);
        entries.add(1, DAYS);
        entries.add(18, HOURS);
        entries.add(12, HOURS);
        entries.add(8, HOURS);
        entries.add(4, HOURS);
        entries.add(2, HOURS);
        entries.add(1, HOURS);
        entries.add(30, MINUTES);
        entries.add(10, MINUTES);
    }

    @Override
    protected boolean setValue(int val) {
        if (GuardTalkAutoRebootHelper.isProfilesEnabled(mContext)) {
            val = GuardTalkAutoRebootPolicy.clampToProfileMillis(val);
        }
        return super.setValue(val);
    }

    @Override
    public void updateState(androidx.preference.Preference preference) {
        // Persist clamped profile so the radio list has a selected entry when
        // the Global default (e.g. 18h) is outside GuardTalk profiles.
        if (GuardTalkAutoRebootHelper.isProfilesEnabled(mContext)) {
            final int raw = ExtSettings.AUTO_REBOOT_TIMEOUT.get(mContext);
            final int clamped = GuardTalkAutoRebootPolicy.clampToProfileMillis(raw);
            if (clamped != raw) {
                ExtSettings.AUTO_REBOOT_TIMEOUT.put(mContext, clamped);
            }
        }
        super.updateState(preference);
    }

    @Override
    protected boolean isCredentialConfirmationRequired() {
        return true;
    }
}
