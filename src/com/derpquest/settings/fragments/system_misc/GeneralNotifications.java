/*
 * Copyright (C) 2020 DerpFest
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

package com.derpquest.settings.fragments.system_misc;

import android.content.ContentResolver;
import android.content.Context;
import android.content.res.Resources;
import android.content.Intent;
import android.os.Bundle;
import android.os.UserHandle;
import android.provider.SearchIndexableResource;
import android.provider.Settings;
import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceScreen;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.Preference.OnPreferenceChangeListener;

import com.android.settings.R;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settingslib.search.Indexable;
import com.android.settingslib.search.SearchIndexable;
import com.android.settings.SettingsPreferenceFragment;

import com.android.internal.logging.nano.MetricsProto;

import com.derp.support.preference.SystemSettingMasterSwitchPreference;
import com.derp.support.preference.SystemSettingSwitchPreference;

import com.derpquest.settings.utils.Utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SearchIndexable
public class GeneralNotifications extends SettingsPreferenceFragment implements
        Preference.OnPreferenceChangeListener, Indexable {

    private static final String INCALL_VIB_OPTIONS = "incall_vib_options";
    private static final String ALERT_SLIDER_PREF = "alert_slider_notifications";
    private static final String GAMING_MODE_ENABLED = "gaming_mode_enabled";

    private Preference mAlertSlider;
    private SystemSettingMasterSwitchPreference mGamingMode;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.general_notifications);
        PreferenceScreen prefScreen = getPreferenceScreen();
        final ContentResolver resolver = getActivity().getContentResolver();
        final Resources res = getResources();

        PreferenceCategory incallVibCategory = (PreferenceCategory) findPreference(INCALL_VIB_OPTIONS);
        if (!Utils.isVoiceCapable(getActivity())) {
            prefScreen.removePreference(incallVibCategory);
        }

        mAlertSlider = (Preference) prefScreen.findPreference(ALERT_SLIDER_PREF);
        boolean mAlertSliderAvailable = res.getBoolean(
                com.android.internal.R.bool.config_hasAlertSlider);
        if (!mAlertSliderAvailable)
            prefScreen.removePreference(mAlertSlider);

        mGamingMode = (SystemSettingMasterSwitchPreference) findPreference(GAMING_MODE_ENABLED);
        boolean gameEnabled = Settings.System.getInt(
        getActivity().getApplicationContext().getContentResolver(),
                Settings.System.GAMING_MODE_ENABLED, 0) == 1;
        updateGameModeEnabledUpdatePrefs(gameEnabled);
        mGamingMode.setOnPreferenceChangeListener(this);
    }

    private void updateGameModeEnabledUpdatePrefs(boolean gameEnabled) {
        mGamingMode.setChecked(gameEnabled);
    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference == mGamingMode) {
            boolean gameEnabled = (Boolean) newValue;
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.GAMING_MODE_ENABLED, gameEnabled ? 1 : 0);
            updateGameModeEnabledUpdatePrefs(gameEnabled);
            return true;
        }
        return false;
    }

    @Override
    public int getMetricsCategory() {
        return MetricsProto.MetricsEvent.DERP;
    }

    public static final SearchIndexProvider SEARCH_INDEX_DATA_PROVIDER =
        new BaseSearchIndexProvider() {
            @Override
            public List<SearchIndexableResource> getXmlResourcesToIndex(Context context,
                    boolean enabled) {
                final ArrayList<SearchIndexableResource> result = new ArrayList<>();
                final SearchIndexableResource sir = new SearchIndexableResource(context);
                sir.xmlResId = R.xml.general_notifications;
                result.add(sir);
                return result;
            }

            @Override
            public List<String> getNonIndexableKeys(Context context) {
                final List<String> keys = super.getNonIndexableKeys(context);
                    final Resources res = context.getResources();
                    boolean mAlertSliderAvailable = res.getBoolean(
                            com.android.internal.R.bool.config_hasAlertSlider);
                    if (!mAlertSliderAvailable)
                        keys.add(ALERT_SLIDER_PREF);
                return keys;
            }
    };
}
