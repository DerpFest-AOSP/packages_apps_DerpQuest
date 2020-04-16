/*
 * Copyright (C) 2020 DerpFest ROM
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

import android.content.ContentResolver;
import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.provider.SearchIndexableResource;
import android.provider.Settings;

import androidx.preference.ListPreference;
import androidx.preference.SwitchPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceScreen;
import androidx.preference.Preference.OnPreferenceChangeListener;

import com.android.internal.logging.nano.MetricsProto;
import com.android.internal.logging.nano.MetricsProto.MetricsEvent;
import com.android.settings.R;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settings.search.Indexable;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settingslib.search.SearchIndexable;
import com.derpquest.settings.Utils;

import com.derpquest.settings.preferences.AmbientLightSettingsPreview;
import com.derpquest.settings.preferences.GlobalSettingMasterSwitchPreference;
import com.derpquest.settings.preferences.SystemSettingMasterSwitchPreference;
import com.derpquest.settings.preferences.SystemSettingSeekBarPreference;

import net.margaritov.preference.colorpicker.ColorPickerPreference;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SearchIndexable
public class NotificationsSettings extends SettingsPreferenceFragment implements
        OnPreferenceChangeListener, Indexable {

    private static final String INCALL_VIB_OPTIONS = "incall_vib_options";
    private static final String FLASH_ON_CALL = "flash_on_call_options";
    private static final String PULSE_AMBIENT_LIGHT = "pulse_ambient_light";
    private static final String PREF_HEADS_UP = "heads_up_settings";

    private Preference mChargingLeds;
    private SystemSettingMasterSwitchPreference mFlashOnCall;
    private SystemSettingMasterSwitchPreference mAmbientLight;
    private GlobalSettingMasterSwitchPreference mHeadsUp;

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        addPreferencesFromResource(R.xml.derpquest_settings_notifications);
        final Resources res = getResources();
        final ContentResolver resolver = getActivity().getContentResolver();
        final PreferenceScreen prefScreen = getPreferenceScreen();

        Preference incallVibCategory = (Preference) findPreference(INCALL_VIB_OPTIONS);
        if (!Utils.isVoiceCapable(getActivity())) {
            prefScreen.removePreference(incallVibCategory);
        }

        mFlashOnCall = (SystemSettingMasterSwitchPreference)
                findPreference(FLASH_ON_CALL);
        if (!Utils.isVoiceCapable(getActivity())) {
            prefScreen.removePreference(mFlashOnCall);
        } else {
            mFlashOnCall.setOnPreferenceChangeListener(this);
            boolean enabled = Settings.System.getInt(resolver,
                    Settings.System.FLASH_ON_CALL_WAITING, 0) == 1;
            mFlashOnCall.setChecked(enabled);
        }

        mAmbientLight = (SystemSettingMasterSwitchPreference)
                findPreference(PULSE_AMBIENT_LIGHT);
        mAmbientLight.setOnPreferenceChangeListener(this);
        boolean enabled = Settings.System.getInt(resolver,
                Settings.System.OMNI_PULSE_AMBIENT_LIGHT, 0) == 1;
        mAmbientLight.setChecked(enabled);

        mChargingLeds = (Preference) findPreference("charging_light");
        if (mChargingLeds != null
                && !res.getBoolean(
                        com.android.internal.R.bool.config_intrusiveBatteryLed)) {
            prefScreen.removePreference(mChargingLeds);
        }

        int defaultDoze = res.getInteger(
                com.android.internal.R.integer.config_screenBrightnessDoze);
        int defaultPulse = res.getInteger(
                com.android.internal.R.integer.config_screenBrightnessPulse);
        if (defaultPulse == -1) {
            defaultPulse = defaultDoze;
        }

        mHeadsUp = (GlobalSettingMasterSwitchPreference)
                findPreference(PREF_HEADS_UP);
        mHeadsUp.setChecked(Settings.Global.getInt(resolver,
                Settings.Global.HEADS_UP_NOTIFICATIONS_ENABLED, 1) == 1);
        mHeadsUp.setOnPreferenceChangeListener(this);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        final ContentResolver resolver = getContentResolver();
        if (preference == mFlashOnCall) {
            Boolean value = (Boolean) newValue;
            Settings.System.putInt(resolver,
                    Settings.System.FLASH_ON_CALL_WAITING, value ? 1 : 0);
            return true;
        } else if (preference == mAmbientLight) {
            Boolean value = (Boolean) newValue;
            Settings.System.putInt(resolver,
                    Settings.System.OMNI_PULSE_AMBIENT_LIGHT, value ? 1 : 0);
            return true;
        } else if (preference == mHeadsUp) {
            Boolean value = (Boolean) newValue;
            Settings.Global.putInt(resolver,
                    Settings.Global.HEADS_UP_NOTIFICATIONS_ENABLED, value ? 1 : 0);
            return true;
        }
        return false;
    }

    @Override
    public int getMetricsCategory() {
        return MetricsProto.MetricsEvent.OWLSNEST;
    }

    public static final SearchIndexProvider SEARCH_INDEX_DATA_PROVIDER =
            new BaseSearchIndexProvider() {
                @Override
                public List<SearchIndexableResource> getXmlResourcesToIndex(Context context,
                                                                            boolean enabled) {
                    ArrayList<SearchIndexableResource> result =
                            new ArrayList<SearchIndexableResource>();

                    SearchIndexableResource sir = new SearchIndexableResource(context);
                    sir.xmlResId = R.xml.derpquest_settings_notifications;
                    result.add(sir);
                    return result;
                }

                @Override
                public List<String> getNonIndexableKeys(Context context) {
                    List<String> keys = super.getNonIndexableKeys(context);
                    return keys;
                }
            };
}
