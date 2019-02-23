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
public class StatusBarSettings extends SettingsPreferenceFragment implements
        OnPreferenceChangeListener, Indexable {

    private static final String NETWORK_TRAFFIC_STATE = "network_traffic_state";
    private static final String STATUS_BAR_CLOCK = "status_bar_clock";
    private static final String STATUS_BAR_DATE = "status_bar_date";
    private static final String STATUS_BAR_LOGO = "status_bar_logo";
    private static final String BATTERY_ICON_STYLE = "battery_icon_style";
    private static final String NOTIFICATION_TICKER = "status_bar_show_ticker";
    private static final String BATTERY_BAR = "battery_bar_settings";

    private SystemSettingMasterSwitchPreference mNetTrafficState;
    private SystemSettingMasterSwitchPreference mStatusBarClock;
    private SystemSettingMasterSwitchPreference mStatusBarDate;
    private SystemSettingMasterSwitchPreference mStatusBarLogo;
    private SystemSettingMasterSwitchPreference mBatteryIconStyle;
    private SystemSettingMasterSwitchPreference mNotificationTicker;
    private SystemSettingMasterSwitchPreference mBatteryBar;

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        addPreferencesFromResource(R.xml.derpquest_settings_statusbar);
        PreferenceScreen prefSet = getPreferenceScreen();
        final ContentResolver resolver = getActivity().getContentResolver();

        mNetTrafficState = (SystemSettingMasterSwitchPreference)
                findPreference(NETWORK_TRAFFIC_STATE);
        mNetTrafficState.setOnPreferenceChangeListener(this);
        boolean enabled = Settings.System.getInt(resolver,
                Settings.System.NETWORK_TRAFFIC_STATE, 0) == 1;
        mNetTrafficState.setChecked(enabled);

        mStatusBarClock = (SystemSettingMasterSwitchPreference)
                findPreference(STATUS_BAR_CLOCK);
        mStatusBarClock.setOnPreferenceChangeListener(this);
        enabled = Settings.System.getInt(resolver,
                Settings.System.STATUS_BAR_CLOCK, 1) == 1;
        mStatusBarClock.setChecked(enabled);

        mStatusBarDate = (SystemSettingMasterSwitchPreference)
                findPreference(STATUS_BAR_DATE);
        mStatusBarDate.setOnPreferenceChangeListener(this);
        mStatusBarDate.setEnabled(enabled);
        enabled = Settings.System.getInt(resolver,
                Settings.System.STATUSBAR_CLOCK_DATE_DISPLAY, 0) > 0;
        mStatusBarDate.setChecked(enabled);

        mStatusBarLogo = (SystemSettingMasterSwitchPreference)
                findPreference(STATUS_BAR_LOGO);
        mStatusBarLogo.setOnPreferenceChangeListener(this);
        enabled = Settings.System.getInt(resolver,
                Settings.System.STATUS_BAR_LOGO, 0) > 0;
        mStatusBarLogo.setChecked(enabled);

        mBatteryIconStyle = (SystemSettingMasterSwitchPreference)
                findPreference(BATTERY_ICON_STYLE);
        mBatteryIconStyle.setOnPreferenceChangeListener(this);
        enabled = Settings.System.getInt(resolver,
                Settings.System.STATUS_BAR_BATTERY_STYLE, 0) < 4;
        mBatteryIconStyle.setChecked(enabled);

        mNotificationTicker = (SystemSettingMasterSwitchPreference)
                findPreference(NOTIFICATION_TICKER);
        mNotificationTicker.setOnPreferenceChangeListener(this);
        enabled = Settings.System.getInt(resolver,
                Settings.System.STATUS_BAR_SHOW_TICKER, 0) == 1;
        mNotificationTicker.setChecked(enabled);

        mBatteryBar = (SystemSettingMasterSwitchPreference)
                findPreference(BATTERY_BAR);
        mBatteryBar.setOnPreferenceChangeListener(this);
        enabled = Settings.System.getInt(resolver,
                Settings.System.BATTERY_BAR_LOCATION, 0) != 0;
        mBatteryBar.setChecked(enabled);

    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object objValue) {
        if (preference == mNetTrafficState) {
            boolean enabled = (boolean) objValue;
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.NETWORK_TRAFFIC_STATE, enabled ? 1 : 0);
            return true;
        } else if (preference == mStatusBarClock) {
            boolean enabled = (boolean) objValue;
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.STATUS_BAR_CLOCK, enabled ? 1 : 0);
            mStatusBarDate.setEnabled(enabled);
            return true;
        } else if (preference == mStatusBarDate) {
            boolean enabled = (boolean) objValue;
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.STATUSBAR_CLOCK_DATE_DISPLAY, enabled ? 1 : 0);
            return true;
        } else if (preference == mStatusBarLogo) {
            boolean enabled = (boolean) objValue;
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.STATUS_BAR_LOGO, enabled ? 1 : 0);
            return true;
        } else if (preference == mBatteryIconStyle) {
            boolean enabled = (boolean) objValue;
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.STATUS_BAR_BATTERY_STYLE, enabled ? 0 : 4);
            return true;
        } else if (preference == mNotificationTicker) {
            boolean enabled = (boolean) objValue;
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.STATUS_BAR_SHOW_TICKER, enabled ? 1 : 0);
            return true;
        } else if (preference == mBatteryBar) {
            boolean enabled = (boolean) objValue;
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.BATTERY_BAR_LOCATION, enabled ? 1 : 0);
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
                    sir.xmlResId = R.xml.derpquest_settings_statusbar;
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
