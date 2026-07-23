/*
 * Copyright (C) 2024 The Android Open Source Project
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

package com.android.settings.network

import android.content.Context
import android.text.BidiFormatter
import com.android.settings.R
import com.android.settings.Utils
import com.android.settings.activityembedding.ActivityEmbeddingUtils
import com.android.settings.core.BasePreferenceController
import com.android.settings.network.telephony.SimRepository

class TopLevelNetworkEntryPreferenceController
@JvmOverloads
constructor(
    context: Context,
    preferenceKey: String,
    private val simRepository: SimRepository = SimRepository(context),
    private val isDemoUser: () -> Boolean = { Utils.isDemoUser(context) },
    private val isEmbeddingActivityEnabled: () -> Boolean = {
        ActivityEmbeddingUtils.isEmbeddingActivityEnabled(context)
    },
) : BasePreferenceController(context, preferenceKey) {

    override fun getAvailabilityStatus(): Int {
        // T-SEC-P1-SETTINGS: dedicated keep/hide hook for Network & internet.
        // Network controls stay under Network — never under Security.
        if (!mContext.resources.getBoolean(R.bool.config_show_top_level_network)) {
            return UNSUPPORTED_ON_DEVICE
        }
        // TODO(b/281597506): Update the ActivityEmbeddingUtils.isEmbeddingActivityEnabled
        //                    while getting the new API.
        return if (isDemoUser() && !isEmbeddingActivityEnabled()) {
            UNSUPPORTED_ON_DEVICE
        } else {
            AVAILABLE
        }
    }

    override fun getSummary(): CharSequence {
        val summaryResId =
            if (simRepository.showMobileNetworkPageEntrance()) {
                R.string.network_dashboard_summary_mobile
            } else {
                R.string.network_dashboard_summary_no_mobile
            }
        return BidiFormatter.getInstance().unicodeWrap(mContext.getString(summaryResId))
    }
}
