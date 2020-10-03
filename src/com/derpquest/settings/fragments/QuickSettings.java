/*
 *  Copyright (C) 2020 DerpFest
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
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.UserHandle;
import android.provider.SearchIndexableResource;
import android.provider.Settings;

import androidx.preference.ListPreference;
import androidx.preference.SwitchPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceScreen;
import androidx.preference.Preference.OnPreferenceChangeListener;

import com.android.settings.R;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settingslib.search.SearchIndexable;
import com.android.settings.SettingsPreferenceFragment;

import com.android.internal.logging.nano.MetricsProto;
import com.android.internal.logging.nano.MetricsProto.MetricsEvent;

import com.derp.support.preference.SystemSettingEditTextPreference;
import com.derp.support.preference.SystemSettingMasterSwitchPreference;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@SearchIndexable
public class QuickSettings extends SettingsPreferenceFragment implements
        Preference.OnPreferenceChangeListener {

    private static final String QS_FOOTER_TEXT_STRING = "qs_footer_text_string";
    private static final String BRIGHTNESS_SLIDER = "qs_show_brightness";

    private SystemSettingEditTextPreference mFooterString;
    private SystemSettingMasterSwitchPreference mBrightnessSlider;

    @Override
    public int getMetricsCategory() {
        return MetricsEvent.DERP;
    }

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        addPreferencesFromResource(R.xml.quicksettings);
        PreferenceScreen prefSet = getPreferenceScreen();
        ContentResolver resolver = getActivity().getContentResolver();

        mBrightnessSlider = (SystemSettingMasterSwitchPreference)
                findPreference(BRIGHTNESS_SLIDER);
        mBrightnessSlider.setOnPreferenceChangeListener(this);
        boolean enabled = Settings.System.getInt(resolver,
                BRIGHTNESS_SLIDER, 1) == 1;
        mBrightnessSlider.setChecked(enabled);

        mFooterString = (SystemSettingEditTextPreference) findPreference(QS_FOOTER_TEXT_STRING);
        mFooterString.setOnPreferenceChangeListener(this);
        String footerString = Settings.System.getString(resolver,
                QS_FOOTER_TEXT_STRING);
        if (footerString != null && footerString != "")
            mFooterString.setText(footerString);
        else {
            mFooterString.setText("#StayDerped");
            Settings.System.putString(resolver,
                    Settings.System.QS_FOOTER_TEXT_STRING, "#StayDerped");
        }
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        ContentResolver resolver = getActivity().getContentResolver();
        if (preference == mFooterString) {
            String value = (String) newValue;
            if (value != "" && value != null)
                Settings.System.putString(resolver,
                        Settings.System.QS_FOOTER_TEXT_STRING, value);
            else {
                mFooterString.setText("#StayDerped");
                Settings.System.putString(resolver,
                        Settings.System.QS_FOOTER_TEXT_STRING, "#StayDerped");
            }
            return true;
        } else if (preference == mBrightnessSlider) {
            Boolean value = (Boolean) newValue;
            Settings.System.putInt(resolver,
                    BRIGHTNESS_SLIDER, value ? 1 : 0);
            return true;
        }
        return false;
    }

    /**
     * For Search.
     */
    public static final SearchIndexProvider SEARCH_INDEX_DATA_PROVIDER =
        new BaseSearchIndexProvider() {

            @Override
            public List<SearchIndexableResource> getXmlResourcesToIndex(
                    Context context, boolean enabled) {
                    SearchIndexableResource sir = new SearchIndexableResource(context);
                    sir.xmlResId = R.xml.quicksettings;
                    return Arrays.asList(sir);
            }

            @Override
            public List<String> getNonIndexableKeys(Context context) {
                ArrayList<String> result = new ArrayList<String>();
                return result;
            }
        };
}
