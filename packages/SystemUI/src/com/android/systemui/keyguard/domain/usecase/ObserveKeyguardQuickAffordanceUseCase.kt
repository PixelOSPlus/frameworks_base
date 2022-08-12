/*
 * Copyright (C) 2022 The Android Open Source Project
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

package com.android.systemui.keyguard.domain.usecase

import com.android.systemui.keyguard.domain.model.KeyguardQuickAffordanceModel
import com.android.systemui.keyguard.domain.model.KeyguardQuickAffordancePosition
import com.android.systemui.keyguard.domain.quickaffordance.KeyguardQuickAffordanceConfig
import com.android.systemui.keyguard.domain.quickaffordance.KeyguardQuickAffordanceRegistry
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

/** Defines interface for use-case for observing the model of a quick affordance in the keyguard. */
interface ObserveKeyguardQuickAffordanceUseCase {
    operator fun invoke(
        position: KeyguardQuickAffordancePosition
    ): Flow<KeyguardQuickAffordanceModel>
}

class ObserveKeyguardQuickAffordanceUseCaseImpl
@Inject
constructor(
    private val registry: KeyguardQuickAffordanceRegistry,
    private val isDozingUseCase: ObserveIsDozingUseCase,
    private val isKeyguardShowingUseCase: ObserveIsKeyguardShowingUseCase,
) : ObserveKeyguardQuickAffordanceUseCase {
    override fun invoke(
        position: KeyguardQuickAffordancePosition
    ): Flow<KeyguardQuickAffordanceModel> {
        return combine(
            affordance(position),
            isDozingUseCase(),
            isKeyguardShowingUseCase(),
        ) { affordance, isDozing, isKeyguardShowing ->
            if (!isDozing && isKeyguardShowing) {
                affordance
            } else {
                KeyguardQuickAffordanceModel.Hidden
            }
        }
    }

    private fun affordance(
        position: KeyguardQuickAffordancePosition
    ): Flow<KeyguardQuickAffordanceModel> {
        val configs = registry.getAll(position)
        return combine(configs.map { config -> config.state }) { states ->
            val index =
                states.indexOfFirst { state ->
                    state is KeyguardQuickAffordanceConfig.State.Visible
                }
            val visibleState =
                if (index != -1) {
                    states[index] as KeyguardQuickAffordanceConfig.State.Visible
                } else {
                    null
                }
            KeyguardQuickAffordanceModel.from(visibleState, configs[index]::class)
        }
    }
}
