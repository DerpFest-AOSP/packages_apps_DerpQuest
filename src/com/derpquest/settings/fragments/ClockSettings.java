/*
 * Copyright (C) 2017-2020 The PixelDust Project
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
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.UserHandle;
import android.provider.SearchIndexableResource;
import android.provider.Settings;
import android.text.format.DateFormat;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceGroup;
import androidx.preference.PreferenceScreen;
import androidx.preference.PreferenceCategory;
import androidx.preference.Preference.OnPreferenceChangeListener;
import androidx.preference.PreferenceFragment;
import androidx.preference.SwitchPreference;

import com.android.internal.logging.nano.MetricsProto;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settings.search.Indexable;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.R;
import com.android.settings.Utils;
import com.android.settingslib.search.SearchIndexable;

import com.derpquest.settings.preferences.CustomSeekBarPreference;
import com.derpquest.settings.preferences.SystemSettingMasterSwitchPreference;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

@SearchIndexable
public class ClockSettings extends SettingsPreferenceFragment implements
        OnPreferenceChangeListener, Indexable {

    private static final String STATUS_BAR_CLOCK_SECONDS = "status_bar_clock_seconds";
    private static final String STATUS_BAR_CLOCK_STYLE = "statusbar_clock_style";
    private static final String STATUS_BAR_AM_PM = "status_bar_am_pm";
    private static final String STATUS_BAR_CLOCK_SIZE  = "status_bar_clock_size";
    private static final String STATUS_BAR_CLOCK_FONT_STYLE  = "status_bar_clock_font_style";

    private ListPreference mStatusBarClockStyle;
    private ListPreference mStatusBarAmPm;
    private CustomSeekBarPreference mClockSize;
    private ListPreference mClockFontStyle;

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        addPreferencesFromResource(R.xml.derpquest_settings_clock);
        PreferenceScreen prefSet = getPreferenceScreen();
        final ContentResolver resolver = getActivity().getContentResolver();

        mStatusBarClockStyle = (ListPreference) findPreference(STATUS_BAR_CLOCK_STYLE);
        int clockStyle = Settings.System.getInt(resolver,
                Settings.System.STATUSBAR_CLOCK_STYLE, 0);
        mStatusBarClockStyle.setValue(String.valueOf(clockStyle));
        mStatusBarClockStyle.setSummary(mStatusBarClockStyle.getEntry());
        mStatusBarClockStyle.setOnPreferenceChangeListener(this);

        mClockSize = (CustomSeekBarPreference) findPreference(STATUS_BAR_CLOCK_SIZE);
        int clockSize = Settings.System.getInt(resolver,
                Settings.System.STATUS_BAR_CLOCK_SIZE, 14);
        mClockSize.setValue(clockSize / 1);
        mClockSize.setOnPreferenceChangeListener(this);

        mClockFontStyle = (ListPreference) findPreference(STATUS_BAR_CLOCK_FONT_STYLE);
        int showClockFont = Settings.System.getInt(resolver,
                Settings.System.STATUS_BAR_CLOCK_FONT_STYLE, 28);
        mClockFontStyle.setValue(String.valueOf(showClockFont));
        mClockFontStyle.setSummary(mClockFontStyle.getEntry());
        mClockFontStyle.setOnPreferenceChangeListener(this);

        mStatusBarAmPm = (ListPreference) findPreference(STATUS_BAR_AM_PM);
        if (DateFormat.is24HourFormat(getActivity())) {
            mStatusBarAmPm.setEnabled(false);
            mStatusBarAmPm.setSummary(R.string.status_bar_am_pm_info);
        } else {
            int statusBarAmPm = Settings.System.getInt(resolver,
                    Settings.System.STATUSBAR_CLOCK_AM_PM_STYLE, 2);
            mStatusBarAmPm.setValue(String.valueOf(statusBarAmPm));
            mStatusBarAmPm.setSummary(mStatusBarAmPm.getEntry());
            mStatusBarAmPm.setOnPreferenceChangeListener(this);
        }
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object objValue) {
        if (preference == mStatusBarClockStyle) {
            int clockStyle = Integer.parseInt((String) objValue);
            int index = mStatusBarClockStyle.findIndexOfValue((String) objValue);
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.STATUSBAR_CLOCK_STYLE, clockStyle);
            mStatusBarClockStyle.setSummary(mStatusBarClockStyle.getEntries()[index]);
            return true;
        } else if (preference == mStatusBarAmPm) {
            int statusBarAmPm = Integer.valueOf((String) objValue);
            int index = mStatusBarAmPm.findIndexOfValue((String) objValue);
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.STATUSBAR_CLOCK_AM_PM_STYLE, statusBarAmPm);
            mStatusBarAmPm.setSummary(mStatusBarAmPm.getEntries()[index]);
            return true;
        } else if (preference == mClockSize) {
            int width = ((Integer)objValue).intValue();
            Settings.System.putInt(getContentResolver(),
                    Settings.System.STATUS_BAR_CLOCK_SIZE, width);
            return true;
        } else if (preference == mClockFontStyle) {
            int showClockFont = Integer.valueOf((String) objValue);
            int index = mClockFontStyle.findIndexOfValue((String) objValue);
            Settings.System.putInt(getContentResolver(), Settings.System.
                STATUS_BAR_CLOCK_FONT_STYLE, showClockFont);
            mClockFontStyle.setSummary(mClockFontStyle.getEntries()[index]);
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
                    sir.xmlResId = R.xml.derpquest_settings_clock;
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
