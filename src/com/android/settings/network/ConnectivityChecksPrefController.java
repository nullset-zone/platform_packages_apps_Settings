package com.android.settings.network;

import android.content.Context;
import android.ext.settings.ConnChecksSetting;

import androidx.preference.PreferenceScreen;

import com.android.settings.R;
import com.android.settings.ext.IntSettingPrefController;
import com.android.settings.ext.RadioButtonPickerFragment2;

public class ConnectivityChecksPrefController extends IntSettingPrefController {

    public ConnectivityChecksPrefController(Context ctx, String key) {
        super(ctx, key, ConnChecksSetting.SYS_PROP);
    }

    // T-CONNCHECK-LOCK: hide the connectivity-check row entirely.
    // The GuardTalkOS default persist.sys.connectivity_checks=0 (VAL_GRAPHENEOS,
    // ConnChecksSetting.java:13) already forces GrapheneOS-only connectivity
    // checks at the DnsResolver layer. Hiding the row removes the user's
    // ability to switch to VAL_STANDARD (Google servers) or VAL_DISABLED via
    // Settings. The IntSettingPrefController base delegates to
    // ExtSettingControllerHelper.getAvailabilityStatus(); overriding here
    // returns UNSUPPORTED_ON_DEVICE, which both hides the preference and
    // de-indexes it from Settings search. Reversible: restore the base
    // delegation (return helper.getAvailabilityStatus()) to revert (Law 11).
    @Override
    public int getAvailabilityStatus() {
        return UNSUPPORTED_ON_DEVICE;
    }

    @Override
    protected void getEntries(Entries entries) {
        entries.add(R.string.conn_checks_grapheneos_server, ConnChecksSetting.VAL_GRAPHENEOS);
        entries.add(R.string.conn_checks_google_server, ConnChecksSetting.VAL_STANDARD);
        entries.add(R.string.conn_checks_disabled, ConnChecksSetting.VAL_DISABLED);
    }

    @Override
    public void addPrefsAfterList(RadioButtonPickerFragment2 fragment, PreferenceScreen screen) {
        addFooterPreference(screen, R.string.conn_checks_footer);
    }
}
