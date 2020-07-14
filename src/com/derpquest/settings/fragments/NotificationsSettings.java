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
import android.util.Log;

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
import com.derpquest.settings.preferences.CustomSeekBarPreference;
import com.derpquest.settings.preferences.GlobalSettingMasterSwitchPreference;
import com.derpquest.settings.preferences.SystemSettingListPreference;
import com.derpquest.settings.preferences.SystemSettingMasterSwitchPreference;
import com.derpquest.settings.preferences.SystemSettingSeekBarPreference;
import com.derpquest.settings.preferences.SystemSettingSwitchPreference;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SearchIndexable
public class NotificationsSettings extends SettingsPreferenceFragment implements
        OnPreferenceChangeListener, Indexable {

    private static final String TAG = "NotificationsSettings";
    private static final String INCALL_VIB_OPTIONS = "incall_vib_options";
    private static final String PULSE_AMBIENT_LIGHT = "pulse_ambient_light";
    private static final String PREF_HEADS_UP = "heads_up_settings";
    private static final String PREF_FLASH_ON_CALL = "flashlight_on_call";
    private static final String PREF_FLASH_ON_CALL_DND = "flashlight_on_call_ignore_dnd";
    private static final String PREF_FLASH_ON_CALL_RATE = "flashlight_on_call_rate";

    private Preference mChargingLeds;
    private SystemSettingMasterSwitchPreference mAmbientLight;
    private GlobalSettingMasterSwitchPreference mHeadsUp;
    private SystemSettingListPreference mFlashOnCall;
    private SystemSettingSwitchPreference mFlashOnCallIgnoreDND;
    private CustomSeekBarPreference mFlashOnCallRate;

    private boolean skipSummaryUpdate;

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

        mAmbientLight = (SystemSettingMasterSwitchPreference)
                findPreference(PULSE_AMBIENT_LIGHT);
        mAmbientLight.setOnPreferenceChangeListener(this);
        boolean enabled = Settings.System.getInt(resolver,
                Settings.System.OMNI_PULSE_AMBIENT_LIGHT, 0) == 1;
        mAmbientLight.setChecked(enabled);
        updateAmbientLightSummary(enabled);

        mChargingLeds = (Preference) findPreference("charging_light");
        if (mChargingLeds != null
                && !res.getBoolean(
                        com.android.internal.R.bool.config_intrusiveBatteryLed)) {
            prefScreen.removePreference(mChargingLeds);
        }

        mHeadsUp = (GlobalSettingMasterSwitchPreference)
                findPreference(PREF_HEADS_UP);
        enabled = Settings.Global.getInt(resolver,
                Settings.Global.HEADS_UP_NOTIFICATIONS_ENABLED, 1) == 1;
        mHeadsUp.setChecked(enabled);
        mHeadsUp.setOnPreferenceChangeListener(this);
        updateHeadsUpSummary(enabled);

        mFlashOnCallRate = (CustomSeekBarPreference)
                findPreference(PREF_FLASH_ON_CALL_RATE);
        int value = Settings.System.getInt(resolver,
                Settings.System.FLASHLIGHT_ON_CALL_RATE, 1);
        mFlashOnCallRate.setValue(value);
        mFlashOnCallRate.setOnPreferenceChangeListener(this);

        mFlashOnCallIgnoreDND = (SystemSettingSwitchPreference)
                findPreference(PREF_FLASH_ON_CALL_DND);
        value = Settings.System.getInt(resolver,
                Settings.System.FLASHLIGHT_ON_CALL, 0);
        mFlashOnCallIgnoreDND.setVisible(value > 1);
        mFlashOnCallRate.setVisible(value != 0);

        mFlashOnCall = (SystemSettingListPreference)
                findPreference(PREF_FLASH_ON_CALL);
        mFlashOnCall.setOnPreferenceChangeListener(this);

        skipSummaryUpdate = true; // avoid being called twice on onResume
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!skipSummaryUpdate) {
            ContentResolver resolver = getActivity().getContentResolver();
            updateAmbientLightSummary(Settings.System.getInt(resolver,
                    Settings.System.OMNI_PULSE_AMBIENT_LIGHT, 0) == 1);
            updateHeadsUpSummary(Settings.Global.getInt(resolver,
                    Settings.Global.HEADS_UP_NOTIFICATIONS_ENABLED, 1) == 1);
        } else {
            skipSummaryUpdate = false;
        }
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        final ContentResolver resolver = getContentResolver();
        if (preference == mAmbientLight) {
            Boolean value = (Boolean) newValue;
            Settings.System.putInt(resolver,
                    Settings.System.OMNI_PULSE_AMBIENT_LIGHT, value ? 1 : 0);
            updateAmbientLightSummary(value);
            return true;
        } else if (preference == mHeadsUp) {
            Boolean value = (Boolean) newValue;
            Settings.Global.putInt(resolver,
                    Settings.Global.HEADS_UP_NOTIFICATIONS_ENABLED, value ? 1 : 0);
            updateHeadsUpSummary(value);
            return true;
        } else if (preference == mFlashOnCall) {
            int value = Integer.parseInt((String) newValue);
            Settings.System.putInt(resolver,
                    Settings.System.FLASHLIGHT_ON_CALL, value);
            mFlashOnCallIgnoreDND.setVisible(value > 1);
            mFlashOnCallRate.setVisible(value != 0);
            return true;
        } else if (preference == mFlashOnCallRate) {
            int value = (Integer) newValue;
            Settings.System.putInt(resolver,
                    Settings.System.FLASHLIGHT_ON_CALL_RATE, value);
            return true;
        }
        return false;
    }

    private void updateAmbientLightSummary(boolean enabled) {
        Resources res = getResources();
        ContentResolver resolver = getContentResolver();
        boolean onAOD = Settings.System.getInt(resolver,
                Settings.System.OMNI_AMBIENT_NOTIFICATION_LIGHT_ENABLED, 0) == 1;
        int duration = Settings.System.getInt(resolver,
                Settings.System.PULSE_AMBIENT_LIGHT_DURATION, 2);
        int colorMode = 3;
        boolean colorModeAutomatic = Settings.System.getInt(resolver,
                Settings.System.OMNI_NOTIFICATION_PULSE_COLOR_AUTOMATIC, 0) != 0;
        boolean colorModeAccent = Settings.System.getInt(resolver,
                Settings.System.OMNI_AMBIENT_NOTIFICATION_LIGHT_ACCENT, 0) != 0;
        boolean colorModeWall = Settings.System.getInt(resolver,
                Settings.System.PULSE_AMBIENT_AUTO_COLOR, 0) != 0;
        if (colorModeAutomatic) {
            colorMode = 0;
        } else if (colorModeAccent) {
            colorMode = 1;
        } else if (colorModeWall) {
            colorMode = 2;
        }
        try {
            mAmbientLight.setSummary(String.format(
                    res.getString(R.string.pulse_ambient_light_summary),
                    enabled ? res.getString(R.string.on) : res.getString(R.string.off),
                    onAOD ? res.getString(R.string.shown) : res.getString(R.string.hidden),
                    String.valueOf(duration), res.getStringArray(
                    R.array.ambient_notification_light_color_mode_entries)[colorMode]));
        } catch (Exception e) {
            Log.e(TAG, "Translation error in pulse_ambient_light_summary");
            mAmbientLight.setSummary(res.getString(R.string.translation_error));
        }
    }

    private void updateHeadsUpSummary(boolean enabled) {
        Resources res = getResources();
        ContentResolver resolver = getContentResolver();
        int snooze = Settings.System.getInt(resolver,
                Settings.System.HEADS_UP_NOTIFICATION_SNOOZE, 3) / 60000;
        int timeout = Settings.System.getInt(resolver,
                Settings.System.HEADS_UP_TIMEOUT, 5) / 1000;
        try {
            mHeadsUp.setSummary(String.format(
                    res.getString(R.string.heads_up_settings_summary),
                    enabled ? res.getString(R.string.on) : res.getString(R.string.off),
                    String.valueOf(snooze), String.valueOf(timeout)));
        } catch (Exception e) {
            Log.e(TAG, "Translation error in heads_up_settings_summary");
            mHeadsUp.setSummary(res.getString(R.string.translation_error));
        }
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
