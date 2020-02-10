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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

@SearchIndexable
public class BatteryInfoSettings extends SettingsPreferenceFragment implements
        Preference.OnPreferenceChangeListener, Indexable {

    private static final String KEY_CHARGE_INFO_FONT = "lockscreen_battery_info_font";

    private ListPreference mChargingInfoFont;

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        addPreferencesFromResource(R.xml.derpquest_settings_battery_info);
        final PreferenceScreen prefScreen = getPreferenceScreen();
        ContentResolver resolver = getActivity().getContentResolver();
        Resources resources = getResources();

        mChargingInfoFont = (ListPreference) findPreference(KEY_CHARGE_INFO_FONT);
        mChargingInfoFont.setValue(String.valueOf(Settings.System.getInt(
                getContentResolver(), Settings.System.LOCKSCREEN_BATTERY_INFO_FONT, 28)));
        mChargingInfoFont.setSummary(mChargingInfoFont.getEntry());
        mChargingInfoFont.setOnPreferenceChangeListener(this);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        ContentResolver resolver = getActivity().getContentResolver();
        if (preference == mChargingInfoFont) {
            int value = Integer.valueOf((String) newValue);
            Settings.System.putInt(resolver,
                    Settings.System.LOCKSCREEN_BATTERY_INFO_FONT, value);
            mChargingInfoFont.setValue(String.valueOf(value));
            mChargingInfoFont.setSummary(mChargingInfoFont.getEntry());
            return true;
        }
        return false;
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
                        sir.xmlResId = R.xml.derpquest_settings_battery_info;
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
