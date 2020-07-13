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

import android.app.Activity;
import android.content.Context;
import android.content.ContentResolver;
import android.app.WallpaperManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources;
import android.hardware.fingerprint.FingerprintManager;
import android.net.Uri;
import android.os.Bundle;
import androidx.preference.SwitchPreference;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceScreen;
import android.provider.SearchIndexableResource;
import android.provider.Settings;

import com.android.internal.logging.nano.MetricsProto;
import com.android.settings.R;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settings.search.Indexable;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settingslib.search.SearchIndexable;

import com.derpquest.settings.preferences.CustomSeekBarPreference;
import com.derpquest.settings.preferences.SecureSettingMasterSwitchPreference;
import com.derpquest.settings.preferences.SystemSettingListPreference;
import com.derpquest.settings.preferences.SystemSettingMasterSwitchPreference;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import net.margaritov.preference.colorpicker.ColorPickerPreference;

@SearchIndexable
public class LockScreenSettings extends SettingsPreferenceFragment implements
        Preference.OnPreferenceChangeListener, Indexable {

    private static final String FINGERPRINT_VIB = "fingerprint_success_vib";
    private static final String FOD_ICON_PICKER_CATEGORY = "fod_icon_picker";
    private static final String FOD_PRESSED_STATE = "fod_pressed_state";
    private static final String FOD_SOLID_COLOR = "fod_solid_color";

    private static final String LOCKSCREEN_BATTERY_INFO = "lockscreen_battery_info";
    private static final String BATTERY_BAR = "sysui_keyguard_show_battery_bar";
    private static final String LOCKSCREEN_CLOCK = "lockscreen_clock";
    private static final String LOCKSCREEN_INFO = "lockscreen_info";
    private static final String MEDIA_ART = "lockscreen_media_metadata";
    private static final String LOCKSCREEN_VISUALIZER_ENABLED = "lockscreen_visualizer_enabled";

    private SystemSettingMasterSwitchPreference mBatteryInfo;
    private SystemSettingMasterSwitchPreference mBatteryBar;
    private SystemSettingMasterSwitchPreference mClockEnabled;
    private SystemSettingMasterSwitchPreference mInfoEnabled;
    private SystemSettingMasterSwitchPreference mMediaArt;
    private SecureSettingMasterSwitchPreference mVisualizerEnabled;

    private FingerprintManager mFingerprintManager;
    private PreferenceCategory mFODIconPickerCategory;
    private SystemSettingListPreference mFODPressedState;
    private ColorPickerPreference mFODIconColor;
    private SwitchPreference mFingerprintVib;

    private boolean skipSummaryUpdate;

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        addPreferencesFromResource(R.xml.derpquest_settings_lockscreen);
        final PreferenceScreen prefScreen = getPreferenceScreen();
        ContentResolver resolver = getActivity().getContentResolver();
        Resources resources = getResources();

        mBatteryInfo = (SystemSettingMasterSwitchPreference)
                findPreference(LOCKSCREEN_BATTERY_INFO);
        mBatteryInfo.setOnPreferenceChangeListener(this);
        boolean enabled = Settings.System.getInt(resolver,
                LOCKSCREEN_BATTERY_INFO, 1) == 1;
        mBatteryInfo.setChecked(enabled);
        updateBatteryInfoSUmmary(enabled);

        mBatteryBar = (SystemSettingMasterSwitchPreference) findPreference(BATTERY_BAR);
        mBatteryBar.setOnPreferenceChangeListener(this);
        enabled = Settings.System.getInt(resolver, BATTERY_BAR, 0) == 1;
        mBatteryBar.setChecked(enabled);
        updateBatteryBarSummary(enabled);

        mClockEnabled = (SystemSettingMasterSwitchPreference) findPreference(LOCKSCREEN_CLOCK);
        mClockEnabled.setOnPreferenceChangeListener(this);
        boolean clockEnabled = Settings.System.getInt(resolver,
                LOCKSCREEN_CLOCK, 1) == 1;
        mClockEnabled.setChecked(enabled);
        updateClockSummary(enabled);

        mInfoEnabled = (SystemSettingMasterSwitchPreference) findPreference(LOCKSCREEN_INFO);
        mInfoEnabled.setOnPreferenceChangeListener(this);
        enabled = Settings.System.getInt(resolver, LOCKSCREEN_INFO, 1) != 0;
        mInfoEnabled.setChecked(enabled);
        mInfoEnabled.setEnabled(clockEnabled);
        updateDateSummary(enabled);

        mMediaArt = (SystemSettingMasterSwitchPreference) findPreference(MEDIA_ART);
        mMediaArt.setOnPreferenceChangeListener(this);
        enabled = Settings.System.getInt(resolver, MEDIA_ART, 1) == 1;
        mMediaArt.setChecked(enabled);
        updateMediaArtSummary(enabled);

        mVisualizerEnabled = (SecureSettingMasterSwitchPreference) findPreference(LOCKSCREEN_VISUALIZER_ENABLED);
        mVisualizerEnabled.setOnPreferenceChangeListener(this);
        enabled = Settings.Secure.getInt(resolver,
                Settings.Secure.LOCKSCREEN_VISUALIZER_ENABLED, 0) == 1;
        mVisualizerEnabled.setChecked(enabled);
        updateVisualizerSummary(enabled);

        mFingerprintManager = (FingerprintManager) getActivity().getSystemService(Context.FINGERPRINT_SERVICE);
        mFingerprintVib = (SwitchPreference) findPreference(FINGERPRINT_VIB);
        if (mFingerprintManager == null) {
            prefScreen.removePreference(mFingerprintVib);
        } else {
            mFingerprintVib.setChecked((Settings.System.getInt(getContentResolver(),
                Settings.System.FINGERPRINT_SUCCESS_VIB, 1) == 1));
            mFingerprintVib.setOnPreferenceChangeListener(this);
        }

        mFODPressedState = (SystemSettingListPreference) findPreference(FOD_PRESSED_STATE);
        int value = Settings.System.getInt(getContentResolver(),
                Settings.System.FOD_PRESSED_STATE, 0);
        mFODPressedState.setValue(String.valueOf(value));
        mFODPressedState.setOnPreferenceChangeListener(this);

        mFODIconColor = (ColorPickerPreference) findPreference(FOD_SOLID_COLOR);
        mFODIconColor.setEnabled(value == 2); // if mFODPressedState index == 2
        try {
            Resources res = getContext().getPackageManager().getResourcesForApplication(
                    "com.android.systemui");
            int defaultColor = res.getColor(res.getIdentifier(
                    "com.android.systemui:color/config_fodColor", null, null));
            mFODIconColor.setDefaultColor(defaultColor);
            value = Settings.System.getInt(getContentResolver(),
                    Settings.System.FOD_SOLID_COLOR, defaultColor);
            mFODIconColor.setNewPreviewColor(value);
            mFODIconColor.setOnPreferenceChangeListener(this);
        } catch (Exception e) {
            e.printStackTrace();
            mFODIconColor.setEnabled(false);
        }

        mFODIconPickerCategory = (PreferenceCategory)
                findPreference(FOD_ICON_PICKER_CATEGORY);
        if (mFODIconPickerCategory != null
                && !getResources().getBoolean(com.android.internal.R.bool.config_needCustomFODView))
            prefScreen.removePreference(mFODIconPickerCategory);

        skipSummaryUpdate = true; // avoid being called twice on onResume
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!skipSummaryUpdate) {
            ContentResolver resolver = getActivity().getContentResolver();
            updateBatteryInfoSUmmary(Settings.System.getInt(resolver,
                    LOCKSCREEN_BATTERY_INFO, 1) == 1);
            updateBatteryBarSummary(Settings.System.getInt(resolver,
                    BATTERY_BAR, 0) == 1);
            updateClockSummary(Settings.System.getInt(resolver,
                    LOCKSCREEN_CLOCK, 1) == 1);
            updateDateSummary(Settings.System.getInt(resolver,
                    LOCKSCREEN_INFO, 1) != 0);
            updateMediaArtSummary(Settings.System.getInt(resolver,
                    MEDIA_ART, 1) == 1);
            updateVisualizerSummary(Settings.Secure.getInt(resolver,
                    Settings.Secure.LOCKSCREEN_VISUALIZER_ENABLED, 0) == 1);
        } else {
            skipSummaryUpdate = false;
        }
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        ContentResolver resolver = getActivity().getContentResolver();
        if (preference == mBatteryInfo) {
            boolean value = (Boolean) newValue;
            Settings.System.putInt(getContentResolver(),
		            LOCKSCREEN_BATTERY_INFO, value ? 1 : 0);
            updateBatteryInfoSUmmary(value);
            return true;
        } else if (preference == mBatteryBar) {
            boolean value = (Boolean) newValue;
            Settings.System.putInt(getContentResolver(),
		            BATTERY_BAR, value ? 1 : 0);
            updateBatteryBarSummary(value);
            return true;
        } else if (preference == mClockEnabled) {
            boolean value = (Boolean) newValue;
            Settings.System.putInt(getContentResolver(),
		            LOCKSCREEN_CLOCK, value ? 1 : 0);
            mInfoEnabled.setEnabled(value);
            updateClockSummary(value);
            return true;
        } else if (preference == mInfoEnabled) {
            boolean value = (Boolean) newValue;
            Settings.System.putInt(getContentResolver(),
		            LOCKSCREEN_INFO, value ? 1 : 0);
            updateDateSummary(value);
            return true;
        } else if (preference == mMediaArt) {
            boolean value = (Boolean) newValue;
            Settings.System.putInt(getContentResolver(),
                MEDIA_ART, value ? 1 : 0);
            updateMediaArtSummary(value);
            return true;
        } else if (preference == mFingerprintVib) {
            boolean value = (Boolean) newValue;
            Settings.System.putInt(resolver,
                    Settings.System.FINGERPRINT_SUCCESS_VIB, value ? 1 : 0);
            return true;
        } else if (preference == mVisualizerEnabled) {
            boolean value = (Boolean) newValue;
            Settings.Secure.putInt(resolver,
                    LOCKSCREEN_VISUALIZER_ENABLED, value ? 1 : 0);
            updateVisualizerSummary(value);
            return true;
        } else if (preference == mFODPressedState) {
            int value = Integer.valueOf((String) newValue);
            Settings.System.putInt(resolver,
                    Settings.System.FOD_PRESSED_STATE, value);
            mFODIconColor.setEnabled(value == 2);
            return true;
        } else if (preference == mFODIconColor) {
            int value = (Integer) newValue;
            Settings.System.putInt(resolver,
                    Settings.System.FOD_SOLID_COLOR, value);
            return true;
        }
        return false;
    }

    private void updateBatteryInfoSUmmary(boolean enabled) {
        Resources res = getResources();
        ContentResolver resolver = getActivity().getContentResolver();
        int font = Settings.System.getInt(resolver,
                Settings.System.LOCKSCREEN_BATTERY_INFO_FONT, 28);
        mBatteryInfo.setSummary(String.format(
                res.getString(R.string.lockscreen_battery_info_summary),
                enabled ? res.getString(R.string.on) : res.getString(R.string.off),
                res.getStringArray(R.array.lock_clock_fonts_entries)[font]));
    }

    private void updateBatteryBarSummary(boolean enabled) {
        Resources res = getResources();
        ContentResolver resolver = getActivity().getContentResolver();
        boolean always = Settings.System.getInt(resolver,
                Settings.System.SYSUI_KEYGUARD_SHOW_BATTERY_BAR_ALWAYS, 0) == 1;
        String color = ColorPickerPreference.convertToARGB(
                Settings.System.getInt(resolver,
                Settings.System.SYSUI_KEYGUARD_BATTERY_BAR_COLOR, 0xffffffff));
        mBatteryBar.setSummary(String.format(
                res.getString(R.string.tuner_keyguard_show_battery_bar_summary),
                enabled ? res.getString(R.string.on) : res.getString(R.string.off),
                always ? res.getString(R.string.tuner_keyguard_battery_bar_always_showing)
                : res.getString(R.string.tuner_keyguard_battery_bar_not_always_showing), color));
    }

    private void updateClockSummary(boolean enabled) {
        Resources res = getResources();
        ContentResolver resolver = getActivity().getContentResolver();
        int style = Settings.Secure.getInt(resolver,
                Settings.Secure.LOCKSCREEN_CLOCK_SELECTION, 0);
        int font = Settings.System.getInt(resolver,
                Settings.System.LOCK_CLOCK_FONTS, 28);
        int size = Settings.System.getInt(resolver,
                Settings.System.LOCKCLOCK_FONT_SIZE, 54);
        mClockEnabled.setSummary(String.format(
                res.getString(R.string.lockscreen_clock_summary),
                enabled ? res.getString(R.string.on) : res.getString(R.string.off),
                res.getStringArray(R.array.lockscreen_clock_titles)[style],
                res.getStringArray(R.array.lock_clock_fonts_entries)[font],
                String.valueOf(size)));
    }

    private void updateDateSummary(boolean enabled) {
        Resources res = getResources();
        ContentResolver resolver = getActivity().getContentResolver();
        int style = Settings.Secure.getInt(resolver,
                Settings.Secure.LOCKSCREEN_DATE_SELECTION, 0);
        int font = Settings.System.getInt(resolver,
                Settings.System.LOCK_DATE_FONTS, 28);
        int size = Settings.System.getInt(resolver,
                Settings.System.LOCKDATE_FONT_SIZE, 18);
        mInfoEnabled.setSummary(String.format(
                res.getString(R.string.lockscreen_clock_summary),
                enabled ? res.getString(R.string.on) : res.getString(R.string.off),
                res.getStringArray(R.array.lockscreen_date_selection_entries)[style],
                res.getStringArray(R.array.lock_clock_fonts_entries)[font],
                String.valueOf(size)));
    }

    private void updateMediaArtSummary(boolean enabled) {
        Resources res = getResources();
        ContentResolver resolver = getActivity().getContentResolver();
        int filter = Settings.System.getInt(resolver,
                Settings.System.LOCKSCREEN_ALBUM_ART_FILTER, 0);
        mMediaArt.setSummary(String.format(
                res.getString(R.string.media_art_summary),
                enabled ? res.getString(R.string.on) : res.getString(R.string.off),
                res.getStringArray(R.array.lockscreen_cover_filter_entries)[filter]));
    }

    private void updateVisualizerSummary(boolean enabled) {
        Resources res = getResources();
        ContentResolver resolver = getActivity().getContentResolver();
        boolean onAOD = Settings.System.getInt(resolver,
                Settings.System.AMBIENT_VISUALIZER_ENABLED, 0) == 1;
        int lines = Settings.Secure.getInt(resolver,
                Settings.Secure.LOCKSCREEN_SOLID_UNITS_COUNT, 32);
        int sanity = Settings.Secure.getInt(resolver,
                Settings.Secure.LOCKSCREEN_SOLID_FUDGE_FACTOR, 16);
        int opacity = Settings.Secure.getInt(resolver,
                Settings.Secure.LOCKSCREEN_SOLID_UNITS_OPACITY, 140);
        mVisualizerEnabled.setSummary(String.format(
                res.getString(R.string.lockscreen_visualizer_enable_summary),
                enabled ? res.getString(R.string.on) : res.getString(R.string.off),
                onAOD ? res.getString(R.string.shown) : res.getString(R.string.hidden),
                String.valueOf(lines), String.valueOf(sanity), String.valueOf(opacity)));
    }

    @Override
    public int getMetricsCategory() {
        return MetricsProto.MetricsEvent.OWLSNEST;
    }

    public static final Indexable.SearchIndexProvider SEARCH_INDEX_DATA_PROVIDER =
            new BaseSearchIndexProvider() {
                @Override
                public List<SearchIndexableResource> getXmlResourcesToIndex(Context context,
                        boolean enabled) {
                    ArrayList<SearchIndexableResource> result =
                            new ArrayList<SearchIndexableResource>();
                     SearchIndexableResource sir = new SearchIndexableResource(context);
                    sir.xmlResId = R.xml.derpquest_settings_lockscreen;
                    result.add(sir);
                    return result;
                }
                @Override
                public List<String> getNonIndexableKeys(Context context) {
                    ArrayList<String> result = new ArrayList<String>();
                    return result;
                }
    };
}
