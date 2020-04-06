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
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.net.Uri;
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

import com.derpquest.settings.preferences.SystemSettingMasterSwitchPreference;
import com.derpquest.settings.preferences.SystemSettingSeekBarPreference;
import com.derpquest.settings.preferences.SystemSettingSwitchPreference;
import com.derpquest.settings.preferences.CustomSeekBarPreference;

import com.android.internal.logging.nano.MetricsProto;
import com.android.internal.logging.nano.MetricsProto.MetricsEvent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SearchIndexable
public class QuickSettings extends SettingsPreferenceFragment implements
        Preference.OnPreferenceChangeListener, Indexable {

    private static final String TAG = "QuickSettings";
    private static final String KEY_QS_PANEL_ALPHA = "qs_panel_alpha";
    private static final String QS_BLUR = "qs_blur";
    private static final String QS_BG_STYLE = "qs_panel_bg_override";
    private static final String BRIGHTNESS_SLIDER = "qs_show_brightness";
    private static final String QS_CUSTOM_HEADER = "status_bar_custom_header";
    private static final String STATUS_BAR_CUSTOM_HEADER_IMAGE = "status_bar_custom_header_image";
    private static final String STATUS_BAR_CUSTOM_HEADER_PROVIDER = "custom_header_provider";
    private static final String STATUS_BAR_CUSTOM_HEADER_PACK = "daylight_header_pack";

    private SystemSettingSeekBarPreference mQsPanelAlpha;

    private SystemSettingMasterSwitchPreference mQsBlurSettings;
    private SystemSettingMasterSwitchPreference mQsBGStyle;
    private SystemSettingMasterSwitchPreference mBrightnessSlider;
    private SystemSettingMasterSwitchPreference mCustomHeader;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.derpquest_settings_quicksettings);
        final Resources res = getResources();
        final ContentResolver resolver = getActivity().getContentResolver();
        final PreferenceScreen prefScreen = getPreferenceScreen();

        mQsPanelAlpha = (SystemSettingSeekBarPreference) findPreference(KEY_QS_PANEL_ALPHA);
        int qsPanelAlpha = Settings.System.getInt(resolver,
                Settings.System.QS_PANEL_BG_ALPHA, 255);
        mQsPanelAlpha.setValue((int)(((double) qsPanelAlpha / 255) * 100));
        mQsPanelAlpha.setOnPreferenceChangeListener(this);

        mQsBlurSettings = (SystemSettingMasterSwitchPreference)
                findPreference(QS_BLUR);
        mQsBlurSettings.setOnPreferenceChangeListener(this);
        boolean enabled = Settings.System.getInt(resolver,
                Settings.System.QS_BLUR, 0) == 1;
        mQsBlurSettings.setChecked(enabled);

        mQsBGStyle = (SystemSettingMasterSwitchPreference)
                findPreference(QS_BG_STYLE);
        mQsBGStyle.setOnPreferenceChangeListener(this);
        // NOTE: Reverse logic
        enabled = Settings.System.getInt(resolver,
                Settings.System.QS_PANEL_BG_USE_FW, 1) == 0;
        mQsBGStyle.setChecked(enabled);

        mBrightnessSlider = (SystemSettingMasterSwitchPreference)
                findPreference(BRIGHTNESS_SLIDER);
        mBrightnessSlider.setOnPreferenceChangeListener(this);
        enabled = Settings.System.getInt(resolver,
                BRIGHTNESS_SLIDER, 1) == 1;
        mBrightnessSlider.setChecked(enabled);

        mCustomHeader = (SystemSettingMasterSwitchPreference)
                findPreference(QS_CUSTOM_HEADER);
        mCustomHeader.setOnPreferenceChangeListener(this);
        enabled = Settings.System.getInt(resolver,
                Settings.System.OMNI_STATUS_BAR_CUSTOM_HEADER, 0) == 1;
        mCustomHeader.setChecked(enabled);

        // Making sure that, If enabled, A header is also selected
        if (enabled && (Settings.System.getString(resolver,
                Settings.System.OMNI_STATUS_BAR_CUSTOM_HEADER_IMAGE) == null ||
                Settings.System.getString(resolver,
                Settings.System.OMNI_STATUS_BAR_CUSTOM_HEADER_IMAGE) == "")) {
            Settings.System.putString(resolver,
                    STATUS_BAR_CUSTOM_HEADER_PROVIDER, "static");
            Settings.System.putString(resolver,
                    STATUS_BAR_CUSTOM_HEADER_PACK, "org.omnirom.omnistyle/org.omnirom.omnistyle.derp_art");
            Settings.System.putString(resolver,
                    STATUS_BAR_CUSTOM_HEADER_IMAGE, "org.omnirom.omnistyle/derp_header_04");
        }

    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        final ContentResolver resolver = getContentResolver();
        if (preference == mQsPanelAlpha) {
            int bgAlpha = (Integer) newValue;
            int trueValue = (int) (((double) bgAlpha / 100) * 255);
            Settings.System.putInt(resolver,
                    Settings.System.QS_PANEL_BG_ALPHA, trueValue);
            return true;
        } else if (preference == mQsBlurSettings) {
            Boolean value = (Boolean) newValue;
            Settings.System.putInt(resolver,
                    Settings.System.QS_BLUR, value ? 1 : 0);
            return true;
        } else if (preference == mQsBGStyle) {
            // NOTE: Reverse logic
            Boolean value = (Boolean) newValue;
            Settings.System.putInt(resolver,
                    Settings.System.QS_PANEL_BG_USE_FW, value ? 0 : 1);
            return true;
        } else if (preference == mBrightnessSlider) {
            Boolean value = (Boolean) newValue;
            Settings.System.putInt(resolver,
                    BRIGHTNESS_SLIDER, value ? 1 : 0);
            return true;
        } else if (preference == mCustomHeader) {
            Boolean value = (Boolean) newValue;
            Settings.System.putInt(resolver,
                    Settings.System.OMNI_STATUS_BAR_CUSTOM_HEADER, value ? 1 : 0);
            // Making sure that, If enabled, A header is also selected
            if (value && (Settings.System.getString(resolver,
                    Settings.System.OMNI_STATUS_BAR_CUSTOM_HEADER_IMAGE) == null ||
                    Settings.System.getString(resolver,
                    Settings.System.OMNI_STATUS_BAR_CUSTOM_HEADER_IMAGE) == "")) {
                Settings.System.putString(resolver,
                        STATUS_BAR_CUSTOM_HEADER_PROVIDER, "static");
                Settings.System.putString(resolver,
                        STATUS_BAR_CUSTOM_HEADER_PACK, "org.omnirom.omnistyle/org.omnirom.omnistyle.derp_art");
                Settings.System.putString(resolver,
                        STATUS_BAR_CUSTOM_HEADER_IMAGE, "org.omnirom.omnistyle/derp_header_04");
            }
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
                    sir.xmlResId = R.xml.derpquest_settings_quicksettings;
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
