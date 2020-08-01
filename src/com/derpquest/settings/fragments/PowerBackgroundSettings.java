/*
 * Copyright (C) 2020 DerpFest Project
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
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.os.UserHandle;
import android.provider.SearchIndexableResource;
import android.provider.Settings;
import android.util.Log;

import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceScreen;
import androidx.preference.Preference.OnPreferenceChangeListener;

import com.android.settings.R;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settings.search.Indexable;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settingslib.search.SearchIndexable;
import com.android.internal.logging.nano.MetricsProto;
import com.android.internal.logging.nano.MetricsProto.MetricsEvent;

import com.derpquest.settings.preferences.SystemSettingListPreference;
import com.derpquest.settings.preferences.SystemSettingSeekBarPreference;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SearchIndexable
public class PowerBackgroundSettings extends SettingsPreferenceFragment implements
        Preference.OnPreferenceChangeListener, Indexable {

    private static final String TAG = "PowerBackgroundSettings";

    private static final String KEY_BG_STYLE = "power_menu_bg_style";
    private static final String KEY_BG_BLUR_RADIUS = "power_menu_bg_blur_radius";

    private SystemSettingListPreference mBgStyle;
    private SystemSettingSeekBarPreference mBlurRadius;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.derpquest_settings_power_bg);
        final Resources res = getResources();
        final ContentResolver resolver = getActivity().getContentResolver();
        final PreferenceScreen prefScreen = getPreferenceScreen();

        mBlurRadius = (SystemSettingSeekBarPreference) findPreference(KEY_BG_BLUR_RADIUS);
        mBlurRadius.setOnPreferenceChangeListener(this);
        int value = Settings.System.getInt(resolver, KEY_BG_BLUR_RADIUS, 100);
        mBlurRadius.setValue(value);

        mBgStyle = (SystemSettingListPreference) findPreference(KEY_BG_STYLE);
        mBgStyle.setOnPreferenceChangeListener(this);
        value = Settings.System.getInt(resolver, KEY_BG_STYLE, 0);
        mBgStyle.setValue(String.valueOf(value));
        mBlurRadius.setEnabled(value != 1 && value != 2); // if filter is blur
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference == mBgStyle) {
            int value = Integer.parseInt((String) newValue);
            Settings.System.putInt(getActivity().getContentResolver(),
                    KEY_BG_STYLE, value);
            mBlurRadius.setEnabled(value != 1 && value != 2); // if filter is blur
            return true;
        } else if (preference == mBlurRadius) {
            int value = (Integer) newValue;
            Settings.System.putInt(getActivity().getContentResolver(),
                    KEY_BG_BLUR_RADIUS, value);
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
                    sir.xmlResId = R.xml.derpquest_settings_power_bg;
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
