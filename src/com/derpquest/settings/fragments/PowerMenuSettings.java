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

import android.content.Context;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.UserInfo;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.UserHandle;
import android.os.UserManager;
import android.provider.SearchIndexableResource;
import android.provider.Settings;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceScreen;
import androidx.preference.Preference.OnPreferenceChangeListener;
import androidx.preference.SwitchPreference;

import com.android.internal.logging.nano.MetricsProto;
import com.android.settings.R;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settings.search.Indexable;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settingslib.search.SearchIndexable;

import com.derpquest.settings.preferences.SystemSettingMasterSwitchPreference;
import com.derpquest.settings.Utils;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;

@SearchIndexable
public class PowerMenuSettings extends SettingsPreferenceFragment
                implements Preference.OnPreferenceChangeListener, Indexable {

    private static final String TAG = "PowerMenuSettings";

    private static final String KEY_POWERMENU_TORCH = "powermenu_torch";
    private static final String KEY_POWER_MENU_BG = "power_menu_bg";

    private SwitchPreference mPowermenuTorch;
    private SystemSettingMasterSwitchPreference mPowerMenuBg;

    private boolean skipSummaryUpdate;

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        addPreferencesFromResource(R.xml.derpquest_settings_power);

        final ContentResolver resolver = getActivity().getContentResolver();
        final PreferenceScreen prefScreen = getPreferenceScreen();

        mPowermenuTorch = (SwitchPreference) findPreference(KEY_POWERMENU_TORCH);
        mPowermenuTorch.setOnPreferenceChangeListener(this);
        if (!Utils.deviceSupportsFlashLight(getActivity())) {
            prefScreen.removePreference(mPowermenuTorch);
        } else {
        mPowermenuTorch.setChecked((Settings.System.getInt(resolver,
                Settings.System.POWERMENU_TORCH, 0) == 1));
        }

        mPowerMenuBg = (SystemSettingMasterSwitchPreference) findPreference(KEY_POWER_MENU_BG);
        mPowerMenuBg.setOnPreferenceChangeListener(this);
        boolean enabled = Settings.System.getInt(resolver,
                Settings.System.POWER_MENU_BG, 0) == 1;
        mPowerMenuBg.setChecked(enabled);

        updatePowerMenuBgSummary(enabled);
        skipSummaryUpdate = true;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!skipSummaryUpdate) {
            updatePowerMenuBgSummary(
                    Settings.System.getInt(getActivity().getContentResolver(),
                    Settings.System.POWER_MENU_BG, 0) == 1);
        } else {
            skipSummaryUpdate = false;
        }
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        final ContentResolver resolver = getActivity().getContentResolver();
        if (preference == mPowermenuTorch) {
            boolean value = (Boolean) newValue;
            Settings.System.putInt(resolver,
                    Settings.System.POWERMENU_TORCH, value ? 1 : 0);
            return true;
        } else if (preference == mPowerMenuBg) {
            boolean value = (Boolean) newValue;
            Settings.System.putInt(resolver,
                    Settings.System.POWER_MENU_BG, value ? 1 : 0);
            updatePowerMenuBgSummary(value);
            return true;
        }
        return false;
    }

    private void updatePowerMenuBgSummary(boolean enabled) {
        final ContentResolver resolver = getActivity().getContentResolver();
        final Resources res = getResources();
        int filter = Settings.System.getInt(resolver,
                Settings.System.POWER_MENU_BG_STYLE, 0);
        try {
            mPowerMenuBg.setSummary(String.format(
                    res.getString(R.string.power_menu_bg_summary),
                    enabled ? res.getString(R.string.on) : res.getString(R.string.off),
                    res.getStringArray(R.array.power_menu_bg_style_entries)[filter]));
            if (filter != 1 && filter != 2) { // if filter is blur
                int radius = Settings.System.getInt(resolver,
                        Settings.System.POWER_MENU_BG_BLUR_RADIUS, 100);
                mPowerMenuBg.setSummary(mPowerMenuBg.getSummary() +
                        ", " + res.getString(R.string.intensity) +
                        " " + String.valueOf(radius) +
                        res.getString(R.string.unit_percent));
            }
        } catch (Exception e) {
            Log.e(TAG, "Translation error in power_menu_bg_summary");
            mPowerMenuBg.setSummary(res.getString(R.string.translation_error));
        }
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
                    sir.xmlResId = R.xml.derpquest_settings_power;
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
