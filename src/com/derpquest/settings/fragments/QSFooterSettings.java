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
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
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
import com.android.settings.search.Indexable;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settingslib.search.SearchIndexable;

import com.derpquest.settings.preferences.SystemSettingEditTextPreference;
import com.derpquest.settings.preferences.SystemSettingSwitchPreference;

import com.android.internal.logging.nano.MetricsProto;
import com.android.internal.logging.nano.MetricsProto.MetricsEvent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SearchIndexable
public class QSFooterSettings extends SettingsPreferenceFragment implements
        Preference.OnPreferenceChangeListener, Indexable {

    private static final String DERP_FOOTER_TEXT_STRING = "derp_footer_text_string";
    private static final String KEY_ALWAYS_SETTINGS = "qs_always_show_settings";
    private static final String KEY_DRAG_HANDLE = "qs_drag_handle";

    private SystemSettingEditTextPreference mFooterString;
    private SystemSettingSwitchPreference mAlwaysSettings;
    private SystemSettingSwitchPreference mDragHandle;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.derpquest_settings_qs_footer);
        final Resources res = getResources();
        final ContentResolver resolver = getActivity().getContentResolver();
        final PreferenceScreen prefScreen = getPreferenceScreen();

        mFooterString = (SystemSettingEditTextPreference) findPreference(DERP_FOOTER_TEXT_STRING);
        mFooterString.setOnPreferenceChangeListener(this);
        String footerString = Settings.System.getString(resolver,
                DERP_FOOTER_TEXT_STRING);
        if (footerString != null && footerString != "")
            mFooterString.setText(footerString);
        else {
            mFooterString.setText("#DerpFest");
            Settings.System.putString(resolver,
                    Settings.System.DERP_FOOTER_TEXT_STRING, "#DerpFest");
        }

        mAlwaysSettings = (SystemSettingSwitchPreference) findPreference(KEY_ALWAYS_SETTINGS);
        mAlwaysSettings.setOnPreferenceChangeListener(this);

        mDragHandle = (SystemSettingSwitchPreference) findPreference(KEY_DRAG_HANDLE);
        mDragHandle.setOnPreferenceChangeListener(this);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        final ContentResolver resolver = getContentResolver();
        if (preference == mFooterString) {
            String value = (String) newValue;
            if (value != "" && value != null)
                Settings.System.putString(resolver,
                        Settings.System.DERP_FOOTER_TEXT_STRING, value);
            else {
                mFooterString.setText("#DerpFest");
                Settings.System.putString(resolver,
                        Settings.System.DERP_FOOTER_TEXT_STRING, "#DerpFest");
            }
            return true;
        } else if (preference == mAlwaysSettings) {
            Boolean value = (Boolean) newValue;
            Settings.System.putInt(resolver,
                    Settings.System.QS_ALWAYS_SHOW_SETTINGS, value ? 1 : 0);
            return true;
        } else if (preference == mDragHandle) {
            Boolean value = (Boolean) newValue;
            Settings.System.putInt(resolver,
                    Settings.System.QS_DRAG_HANDLE, value ? 1 : 0);
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
                    sir.xmlResId = R.xml.derpquest_settings_qs_footer;
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
