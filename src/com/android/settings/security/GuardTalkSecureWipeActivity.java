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

package com.android.settings.security;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.UserHandle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.android.internal.widget.LockPatternUtils;
import com.android.internal.widget.LockscreenCredential;
import com.android.settings.R;
import com.android.settings.password.ChooseLockSettingsHelper;

/**
 * Secure wipe confirm UX (F-SEC-P2-SECURITY-SCREENS).
 *
 * <p>Flow: device password → irreversible warning → final erase confirm →
 * {@link LockPatternUtils#requestSecureWipe}. Shared {@code SecureWipeEngine}
 * performs FBE/KeyMint crypto-erase (not flash overwrite). Not a QS tile.
 */
public class GuardTalkSecureWipeActivity extends Activity {
    private static final String TAG = "GtSecureWipeAct";
    private static final int REQ_CONFIRM = 1001;
    private static final String STATE_ASKED = "asked";
    private static final String STATE_STAGE = "stage";
    private static final int STAGE_CREDENTIAL = 0;
    private static final int STAGE_WARNING = 1;
    private static final int STAGE_CONFIRM = 2;

    private LockPatternUtils mLockPatternUtils;
    private boolean mAskedForCredential;
    private int mStage = STAGE_CREDENTIAL;
    @Nullable private LockscreenCredential mCredential;
    @Nullable private AlertDialog mDialog;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mLockPatternUtils = new LockPatternUtils(this);
        if (savedInstanceState != null) {
            mAskedForCredential = savedInstanceState.getBoolean(STATE_ASKED, false);
            mStage = savedInstanceState.getInt(STATE_STAGE, STAGE_CREDENTIAL);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(STATE_ASKED, mAskedForCredential);
        outState.putInt(STATE_STAGE, mStage);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!mAskedForCredential) {
            final ChooseLockSettingsHelper.Builder b = new ChooseLockSettingsHelper.Builder(this);
            b.setRequestCode(REQ_CONFIRM);
            b.setReturnCredentials(true);
            b.setForegroundOnly(true);
            b.setUserId(UserHandle.myUserId());
            if (!b.show()) {
                Log.e(TAG, "Confirm credential UI unavailable");
                finish();
                return;
            }
            mAskedForCredential = true;
        }
    }

    @Override
    protected void onDestroy() {
        dismissDialog();
        zeroizeCredential();
        super.onDestroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode != REQ_CONFIRM) {
            return;
        }
        if (resultCode != RESULT_OK || data == null) {
            finish();
            return;
        }
        final LockscreenCredential credential =
                data.getParcelableExtra(
                        ChooseLockSettingsHelper.EXTRA_KEY_PASSWORD, LockscreenCredential.class);
        if (credential == null) {
            finish();
            return;
        }
        mCredential = credential;
        mStage = STAGE_WARNING;
        showIrreversibleWarning();
    }

    /** First confirm: irreversible crypto-erase warning. */
    private void showIrreversibleWarning() {
        dismissDialog();
        mDialog = new AlertDialog.Builder(this)
                .setTitle(R.string.guardtalk_security_secure_wipe_warning_title)
                .setMessage(R.string.guardtalk_security_secure_wipe_warning_message)
                .setCancelable(true)
                .setNegativeButton(android.R.string.cancel, (d, w) -> abortAndFinish())
                .setPositiveButton(R.string.guardtalk_security_secure_wipe_continue,
                        (d, w) -> {
                            mStage = STAGE_CONFIRM;
                            showFinalConfirm();
                        })
                .setOnCancelListener(d -> abortAndFinish())
                .create();
        mDialog.setCanceledOnTouchOutside(false);
        mDialog.show();
    }

    /** Second confirm: explicit erase action. */
    private void showFinalConfirm() {
        dismissDialog();
        final LockscreenCredential credential = mCredential;
        if (credential == null) {
            finish();
            return;
        }
        mDialog = new AlertDialog.Builder(this)
                .setTitle(R.string.guardtalk_security_secure_wipe_confirm_title)
                .setMessage(R.string.guardtalk_security_secure_wipe_confirm_message)
                .setCancelable(true)
                .setNegativeButton(android.R.string.cancel, (d, w) -> abortAndFinish())
                .setPositiveButton(R.string.guardtalk_security_secure_wipe_erase, (d, w) -> {
                    try {
                        mLockPatternUtils.requestSecureWipe(credential);
                        // Does not return on success (device shuts down).
                    } catch (RuntimeException e) {
                        Log.e(TAG, "requestSecureWipe failed", e);
                        Toast.makeText(this, R.string.guardtalk_security_secure_wipe_failed,
                                Toast.LENGTH_LONG).show();
                        abortAndFinish();
                    }
                })
                .setOnCancelListener(d -> abortAndFinish())
                .create();
        mDialog.setCanceledOnTouchOutside(false);
        mDialog.show();
    }

    private void abortAndFinish() {
        dismissDialog();
        zeroizeCredential();
        finish();
    }

    private void dismissDialog() {
        if (mDialog != null) {
            if (mDialog.isShowing()) {
                mDialog.dismiss();
            }
            mDialog = null;
        }
    }

    private void zeroizeCredential() {
        if (mCredential != null) {
            mCredential.zeroize();
            mCredential = null;
        }
    }
}
