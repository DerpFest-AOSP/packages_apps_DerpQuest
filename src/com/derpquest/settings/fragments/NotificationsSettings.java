/*
 * Copyright (C) 2017-2019 The PixelDust Project
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
package com.derpquest.settings.fragments;

import android.os.Bundle;
import android.provider.Settings;
import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceScreen;
import androidx.preference.Preference.OnPreferenceChangeListener;

import com.android.internal.logging.nano.MetricsProto;
import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.derpquest.settings.Utils;

import com.derpquest.settings.preferences.CustomSeekBarPreference;
import com.derpquest.settings.preferences.SystemSettingSeekBarPreference;
import net.margaritov.preference.colorpicker.ColorPickerPreference;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NotificationsSettings extends SettingsPreferenceFragment implements OnPreferenceChangeListener {

    private static final String INCALL_VIB_OPTIONS = "incall_vib_options";
    private static final String FLASH_ON_CALL_WAITING_DELAY = "flash_on_call_waiting_delay";
    private static final String FLASH_ON_CALL_CATEGORY = "flash_on_call_category";
    private static final String PULSE_AMBIENT_LIGHT_COLOR = "pulse_ambient_light_color";
    private static final String KEY_PULSE_BRIGHTNESS = "ambient_pulse_brightness";
    private static final String KEY_DOZE_BRIGHTNESS = "ambient_doze_brightness";

    private CustomSeekBarPreference mPulseBrightness;
    private CustomSeekBarPreference mDozeBrightness;
    private ColorPickerPreference mEdgeLightColorPreference;
    private Preference mChargingLeds;
    private SystemSettingSeekBarPreference mFlashOnCallWaitingDelay;

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        addPreferencesFromResource(R.xml.derpquest_settings_notifications);
        PreferenceScreen prefScreen = getPreferenceScreen();

        PreferenceCategory incallVibCategory = (PreferenceCategory) findPreference(INCALL_VIB_OPTIONS);
        if (!Utils.isVoiceCapable(getActivity())) {
            prefScreen.removePreference(incallVibCategory);
        }

        PreferenceCategory flashOnCallCategory = (PreferenceCategory) findPreference(FLASH_ON_CALL_CATEGORY);
        if (!Utils.isVoiceCapable(getActivity())) {
            prefScreen.removePreference(flashOnCallCategory);
        }

        mFlashOnCallWaitingDelay = (SystemSettingSeekBarPreference) findPreference(FLASH_ON_CALL_WAITING_DELAY);
        mFlashOnCallWaitingDelay.setValue(Settings.System.getInt(getActivity().getContentResolver(),
              Settings.System.FLASH_ON_CALLWAITING_DELAY, 200));
        mFlashOnCallWaitingDelay.setOnPreferenceChangeListener(this);

        mChargingLeds = (Preference) findPreference("charging_light");
        if (mChargingLeds != null
                && !getResources().getBoolean(
                        com.android.internal.R.bool.config_intrusiveBatteryLed)) {
            prefScreen.removePreference(mChargingLeds);
        }

        mEdgeLightColorPreference = (ColorPickerPreference) findPreference(PULSE_AMBIENT_LIGHT_COLOR);
        int edgeLightColor = Settings.System.getInt(getContentResolver(),
                Settings.System.PULSE_AMBIENT_LIGHT_COLOR, 0xFF3980FF);
        mEdgeLightColorPreference.setNewPreviewColor(edgeLightColor);
        mEdgeLightColorPreference.setAlphaSliderEnabled(false);
        String edgeLightColorHex = String.format("#%08x", (0xFF3980FF & edgeLightColor));
        if (edgeLightColorHex.equals("#ff3980ff")) {
            mEdgeLightColorPreference.setSummary(R.string.default_string);
        } else {
            mEdgeLightColorPreference.setSummary(edgeLightColorHex);
        }
        mEdgeLightColorPreference.setOnPreferenceChangeListener(this);

        int defaultDoze = getResources().getInteger(
                com.android.internal.R.integer.config_screenBrightnessDoze);
        int defaultPulse = getResources().getInteger(
                com.android.internal.R.integer.config_screenBrightnessPulse);
        if (defaultPulse == -1) {
            defaultPulse = defaultDoze;
        }

        mPulseBrightness = (CustomSeekBarPreference) findPreference(KEY_PULSE_BRIGHTNESS);
        int value = Settings.System.getInt(getContentResolver(),
                Settings.System.PULSE_BRIGHTNESS, defaultPulse);
        mPulseBrightness.setValue(value);
        mPulseBrightness.setOnPreferenceChangeListener(this);

        mDozeBrightness = (CustomSeekBarPreference) findPreference(KEY_DOZE_BRIGHTNESS);
        value = Settings.System.getInt(getContentResolver(),
                Settings.System.DOZE_BRIGHTNESS, defaultDoze);
        mDozeBrightness.setValue(value);
        mDozeBrightness.setOnPreferenceChangeListener(this);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference == mFlashOnCallWaitingDelay) {
            int val = (Integer) newValue;
            Settings.System.putInt(getContentResolver(), Settings.System.FLASH_ON_CALLWAITING_DELAY, val);
            return true;
        } else if (preference == mEdgeLightColorPreference) {
            String hex = ColorPickerPreference.convertToARGB(
                    Integer.valueOf(String.valueOf(newValue)));
            if (hex.equals("#ff3980ff")) {
                preference.setSummary(R.string.default_string);
            } else {
                preference.setSummary(hex);
            }
            int intHex = ColorPickerPreference.convertToColorInt(hex);
            Settings.System.putInt(getContentResolver(),
                    Settings.System.PULSE_AMBIENT_LIGHT_COLOR, intHex);
            return true;
        } else if (preference == mPulseBrightness) {
            int value = (Integer) newValue;
            Settings.System.putInt(getContentResolver(),
                    Settings.System.PULSE_BRIGHTNESS, value);
            return true;
        } else if (preference == mDozeBrightness) {
            int value = (Integer) newValue;
            Settings.System.putInt(getContentResolver(),
                    Settings.System.DOZE_BRIGHTNESS, value);
            return true;
        }
        return false;
    }


    @Override
    public int getMetricsCategory() {
        return MetricsProto.MetricsEvent.OWLSNEST;
    }
}
