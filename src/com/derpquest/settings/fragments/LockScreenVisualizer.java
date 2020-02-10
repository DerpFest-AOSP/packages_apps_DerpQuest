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

import android.content.Context;
import android.content.ContentResolver;
import android.app.WallpaperManager;
import android.content.Intent;
import android.content.res.Resources;
import android.hardware.fingerprint.FingerprintManager;
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

import com.derpquest.settings.preferences.SecureSettingSeekBarPreference;
import com.derpquest.settings.preferences.SecureSettingSwitchPreference;
import com.derpquest.settings.preferences.SystemSettingSwitchPreference;

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
public class LockScreenVisualizer extends SettingsPreferenceFragment implements
        Preference.OnPreferenceChangeListener, Indexable {

    private static final String KEY_AMBIENT_VIS = "ambient_visualizer";
    private static final String KEY_LAVALAMP = "lockscreen_lavalamp_enabled";
    private static final String KEY_LAVALAMP_SPEED = "lockscreen_lavalamp_speed";
    private static final String KEY_AUTOCOLOR = "lockscreen_visualizer_autocolor";
    private static final String KEY_SOLID_UNITS = "lockscreen_solid_units_count";
    private static final String KEY_FUDGE_FACTOR = "lockscreen_solid_fudge_factor";
    private static final String KEY_OPACITY = "lockscreen_solid_units_opacity";
    private static final String KEY_COLOR = "lockscreen_visualizer_color";

    private static final int DEFAULT_COLOR = 0xffffffff;

    private SystemSettingSwitchPreference mAmbientVisualizer;
    private SecureSettingSwitchPreference mAutoColor;
    private SecureSettingSwitchPreference mLavaLamp;
    private SecureSettingSeekBarPreference mSolidUnits;
    private SecureSettingSeekBarPreference mFudgeFactor;
    private SecureSettingSeekBarPreference mOpacity;
    private ColorPickerPreference mColor;

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        addPreferencesFromResource(R.xml.derpquest_settings_visualizer);
        final PreferenceScreen prefScreen = getPreferenceScreen();
        ContentResolver resolver = getActivity().getContentResolver();
        Resources resources = getResources();

        boolean mAmbientEnabled = Settings.System.getInt(resolver,
                Settings.System.AMBIENT_VISUALIZER_ENABLED, 0) == 1;
        mAmbientVisualizer = (SystemSettingSwitchPreference) findPreference(KEY_AMBIENT_VIS);
        mAmbientVisualizer.setChecked(mAmbientEnabled);

        boolean mLavaLampEnabled = Settings.Secure.getInt(resolver,
                Settings.Secure.LOCKSCREEN_LAVALAMP_ENABLED, 1) != 0;
        boolean mAutoColorEnabled = Settings.Secure.getInt(resolver,
                Settings.Secure.LOCKSCREEN_VISUALIZER_AUTOCOLOR, 1) != 0;
        mAutoColor = (SecureSettingSwitchPreference) findPreference(KEY_AUTOCOLOR);
        mAutoColor.setOnPreferenceChangeListener(this);
        mAutoColor.setChecked(mAutoColorEnabled);

        if (mLavaLampEnabled) {
            mAutoColor.setSummary(getActivity().getString(
                    R.string.lockscreen_autocolor_lavalamp));
        } else {
            mAutoColor.setSummary(getActivity().getString(
                    R.string.lockscreen_autocolor_summary));
        }

        mLavaLamp = (SecureSettingSwitchPreference) findPreference(KEY_LAVALAMP);
        mLavaLamp.setOnPreferenceChangeListener(this);

        mSolidUnits = (SecureSettingSeekBarPreference) findPreference(KEY_SOLID_UNITS);
        mSolidUnits.setOnPreferenceChangeListener(this);
        mSolidUnits.setValue(Settings.Secure.getInt(resolver,
                Settings.Secure.LOCKSCREEN_SOLID_UNITS_COUNT, 32));

        mFudgeFactor = (SecureSettingSeekBarPreference) findPreference(KEY_FUDGE_FACTOR);
        mFudgeFactor.setOnPreferenceChangeListener(this);
        mFudgeFactor.setValue(Settings.Secure.getInt(resolver,
                Settings.Secure.LOCKSCREEN_SOLID_FUDGE_FACTOR, 16));

        mOpacity = (SecureSettingSeekBarPreference) findPreference(KEY_OPACITY);
        mOpacity.setOnPreferenceChangeListener(this);
        mOpacity.setValue(Settings.Secure.getInt(resolver,
                Settings.Secure.LOCKSCREEN_SOLID_UNITS_OPACITY, 140));

        mColor = (ColorPickerPreference) findPreference(KEY_COLOR);
        mColor.setOnPreferenceChangeListener(this);
        int intColor = Settings.Secure.getInt(getContentResolver(),
                Settings.Secure.LOCKSCREEN_VISUALIZER_COLOR, DEFAULT_COLOR);
        String hexColor = String.format("#%08x", (DEFAULT_COLOR & intColor));
        mColor.setSummary(hexColor);
        mColor.setNewPreviewColor(intColor);

        UpdateEnablement(resolver);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        ContentResolver resolver = getActivity().getContentResolver();
        if (preference == mLavaLamp) {
            boolean value = (Boolean) newValue;
            Settings.Secure.putInt(getContentResolver(),
                    Settings.Secure.LOCKSCREEN_LAVALAMP_ENABLED, value ? 1 : 0);
            if (value) {
                mAutoColor.setSummary(getActivity().getString(
                        R.string.lockscreen_autocolor_lavalamp));
            } else {
                mAutoColor.setSummary(getActivity().getString(
                        R.string.lockscreen_autocolor_summary));
            }
            UpdateEnablement(resolver);
            return true;
        } else if (preference == mAutoColor) {
            boolean value = (Boolean) newValue;
            Settings.Secure.putInt(getContentResolver(),
                    Settings.Secure.LOCKSCREEN_VISUALIZER_AUTOCOLOR, value ? 1 : 0);
            UpdateEnablement(resolver);
            return true;
        } else if (preference == mSolidUnits) {
            int value = (int) newValue;
            Settings.Secure.putInt(getContentResolver(),
                    Settings.Secure.LOCKSCREEN_SOLID_UNITS_COUNT, value);
            return true;
        } else if (preference == mFudgeFactor) {
            int value = (int) newValue;
            Settings.Secure.putInt(getContentResolver(),
                    Settings.Secure.LOCKSCREEN_SOLID_FUDGE_FACTOR, value);
            return true;
        } else if (preference == mOpacity) {
            int value = (int) newValue;
            Settings.Secure.putInt(getContentResolver(),
                    Settings.Secure.LOCKSCREEN_SOLID_UNITS_OPACITY, value);
            return true;
        } else if (preference == mColor) {
            String hex = ColorPickerPreference.convertToARGB(
                    Integer.valueOf(String.valueOf(newValue)));
            preference.setSummary(hex);
            int intHex = ColorPickerPreference.convertToColorInt(hex);
            Settings.Secure.putInt(resolver,
                    Settings.Secure.LOCKSCREEN_VISUALIZER_COLOR, intHex);
            return true;
        }
        return false;
    }

    // Updates enablement of lockscreen visualizer toggles
    private void UpdateEnablement(ContentResolver resolver) {
        boolean mLavaLampEnabled = Settings.Secure.getInt(resolver,
                Settings.Secure.LOCKSCREEN_LAVALAMP_ENABLED, 1) != 0;
        boolean mAutoColorEnabled = Settings.Secure.getInt(resolver,
                Settings.Secure.LOCKSCREEN_VISUALIZER_AUTOCOLOR, 1) != 0;
        boolean visualizerEnabled = Settings.Secure.getInt(resolver,
                Settings.Secure.LOCKSCREEN_VISUALIZER_ENABLED, 0) != 0;
        mAmbientVisualizer.setEnabled(visualizerEnabled);
        mAmbientVisualizer.setSummary(visualizerEnabled ?
                R.string.ambient_visualizer_summary : R.string.enable_first);
        mLavaLamp.setEnabled(visualizerEnabled);
        mAutoColor.setEnabled(visualizerEnabled && !mLavaLampEnabled);
        mSolidUnits.setEnabled(visualizerEnabled);
        mFudgeFactor.setEnabled(visualizerEnabled);
        mOpacity.setEnabled(visualizerEnabled);
        mColor.setEnabled(visualizerEnabled && !mAutoColorEnabled && !mLavaLampEnabled);
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
                    sir.xmlResId = R.xml.derpquest_settings_visualizer;
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
