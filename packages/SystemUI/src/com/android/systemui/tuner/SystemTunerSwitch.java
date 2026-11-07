/*
 * Copyright (C) 2026 The Android Open Source Project
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

package com.android.systemui.tuner;

import android.content.Context;
import android.util.AttributeSet;

import com.android.systemui.Dependency;

/**
 * A {@link TunerSwitch} that writes prefixed tuner keys through {@link TunerService}.
 *
 * <p>The base {@link TunerSwitch} always writes to {@link android.provider.Settings.Secure},
 * which breaks keys such as {@code system:data_disabled_icon}. This subclass keeps the normal
 * tuner switch behavior while routing the write through {@link TunerService#setValue(String, int)}.
 */
public class SystemTunerSwitch extends TunerSwitch {
    public SystemTunerSwitch(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected boolean persistBoolean(boolean value) {
        Dependency.get(TunerService.class).setValue(getKey(), value ? 1 : 0);
        return true;
    }
}
