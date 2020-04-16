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
import android.text.TextUtils;
import android.util.Log;
import android.view.View;

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
import com.android.settingslib.search.SearchIndexable;

import com.derpquest.settings.preferences.AppMultiSelectListPreference;
import com.derpquest.settings.preferences.CustomSeekBarPreference;
import com.derpquest.settings.preferences.ScrollAppsViewPreference;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

@SearchIndexable
public class HeadsUp extends SettingsPreferenceFragment implements
        OnPreferenceChangeListener, Indexable {

    private static final String PREF_HEADS_UP_SNOOZE_TIME = "heads_up_snooze_time";
    private static final String PREF_HEADS_UP_TIME_OUT = "heads_up_time_out";
    private static final String PREF_STOPLIST_APPS_LIST_SCROLLER = "stoplist_apps_list_scroller";
    private static final String PREF_BLACKLIST_APPS_LIST_SCROLLER = "blacklist_apps_list_scroller";
    private static final String PREF_ADD_STOPLIST_PACKAGES = "add_stoplist_packages";
    private static final String PREF_ADD_BLACKLIST_PACKAGES = "add_blacklist_packages";

    private AppMultiSelectListPreference mAddStoplistPref;
    private AppMultiSelectListPreference mAddBlacklistPref;
    private ScrollAppsViewPreference mStoplistScroller;
    private ScrollAppsViewPreference mBlacklistScroller;

    private CustomSeekBarPreference mHeadsUpSnoozeTime;
    private CustomSeekBarPreference mHeadsUpTimeOut;

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        addPreferencesFromResource(R.xml.derpquest_settings_heads_up);
        PreferenceScreen prefSet = getPreferenceScreen();
        final ContentResolver resolver = getActivity().getContentResolver();

        Resources systemUiResources;
        try {
            systemUiResources = getPackageManager().getResourcesForApplication("com.android.systemui");
        } catch (Exception e) {
            return;
        }

        mHeadsUpSnoozeTime = (CustomSeekBarPreference) findPreference(PREF_HEADS_UP_SNOOZE_TIME);
        mHeadsUpSnoozeTime.setOnPreferenceChangeListener(this);
        int headsUpSnooze = Settings.System.getInt(getContentResolver(),
                Settings.System.HEADS_UP_NOTIFICATION_SNOOZE, 3);
        mHeadsUpSnoozeTime.setValue(headsUpSnooze / 60000); // milliseconds to minutes

        mHeadsUpTimeOut = (CustomSeekBarPreference) findPreference(PREF_HEADS_UP_TIME_OUT);
        mHeadsUpTimeOut.setOnPreferenceChangeListener(this);
        int headsUpTimeOut = Settings.System.getInt(getContentResolver(),
                Settings.System.HEADS_UP_TIMEOUT, 5);
        mHeadsUpTimeOut.setValue(headsUpTimeOut / 1000); // milliseconds to seconds

        mStoplistScroller = (ScrollAppsViewPreference) findPreference(PREF_STOPLIST_APPS_LIST_SCROLLER);
        mBlacklistScroller = (ScrollAppsViewPreference) findPreference(PREF_BLACKLIST_APPS_LIST_SCROLLER);

        mAddStoplistPref =  (AppMultiSelectListPreference) findPreference(PREF_ADD_STOPLIST_PACKAGES);
        mAddBlacklistPref = (AppMultiSelectListPreference) findPreference(PREF_ADD_BLACKLIST_PACKAGES);

        final String valuesStoplist = Settings.System.getString(resolver,
                Settings.System.HEADS_UP_STOPLIST_VALUES);
        if (!TextUtils.isEmpty(valuesStoplist)) {
            Collection<String> stopList = Arrays.asList(valuesStoplist.split(":"));
            mStoplistScroller.setVisible(true);
            mStoplistScroller.setValues(stopList);
            mAddStoplistPref.setValues(stopList);
        } else {
            mStoplistScroller.setVisible(false);
        }

        final String valuesBlacklist = Settings.System.getString(resolver,
                Settings.System.HEADS_UP_BLACKLIST_VALUES);
        if (!TextUtils.isEmpty(valuesBlacklist)) {
            Collection<String> blackList = Arrays.asList(valuesBlacklist.split(":"));
            mBlacklistScroller.setVisible(true);
            mBlacklistScroller.setValues(blackList);
            mAddBlacklistPref.setValues(blackList);
        } else {
            mBlacklistScroller.setVisible(false);
        }

        mAddStoplistPref.setOnPreferenceChangeListener(this);
        mAddBlacklistPref.setOnPreferenceChangeListener(this);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object objValue) {
        if (preference == mHeadsUpSnoozeTime) {
            int headsUpSnooze = (int) objValue;
            Settings.System.putInt(getContentResolver(),
                    Settings.System.HEADS_UP_NOTIFICATION_SNOOZE,
                    headsUpSnooze * 60000); // minutes to milliseconds
            return true;
        } else if (preference == mHeadsUpTimeOut) {
            int headsUpTimeOut = (int) objValue;
            Settings.System.putInt(getContentResolver(),
                    Settings.System.HEADS_UP_TIMEOUT,
                    headsUpTimeOut * 1000); // seconds to milliseconds
            return true;
        } else if (preference == mAddStoplistPref) {
            Collection<String> valueList = (Collection<String>) objValue;
            mStoplistScroller.setVisible(false);
            if (valueList != null) {
                Settings.System.putString(getContentResolver(),
                        Settings.System.HEADS_UP_STOPLIST_VALUES,
                        TextUtils.join(":", valueList));
                mStoplistScroller.setVisible(true);
                mStoplistScroller.setValues(valueList);
            } else {
                Settings.System.putString(getContentResolver(),
                        Settings.System.HEADS_UP_STOPLIST_VALUES, "");
            }
            return true;
        } else if (preference == mAddBlacklistPref) {
            Collection<String> valueList = (Collection<String>) objValue;
            mBlacklistScroller.setVisible(false);
            if (valueList != null) {
                Settings.System.putString(getContentResolver(),
                        Settings.System.HEADS_UP_BLACKLIST_VALUES,
                        TextUtils.join(":", valueList));
                mBlacklistScroller.setVisible(true);
                mBlacklistScroller.setValues(valueList);
            } else {
                Settings.System.putString(getContentResolver(),
                        Settings.System.HEADS_UP_BLACKLIST_VALUES, "");
            }
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
                    sir.xmlResId = R.xml.derpquest_settings_heads_up;
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
