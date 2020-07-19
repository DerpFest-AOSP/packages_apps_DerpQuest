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
import android.net.Uri;
import android.os.Bundle;
import android.provider.SearchIndexableResource;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

import androidx.preference.ListPreference;
import androidx.preference.SwitchPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceScreen;
import androidx.preference.Preference.OnPreferenceClickListener;
import androidx.preference.Preference.OnPreferenceChangeListener;

import com.android.internal.logging.nano.MetricsProto;
import com.android.internal.logging.nano.MetricsProto.MetricsEvent;
import com.android.settings.R;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settings.search.Indexable;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settingslib.search.SearchIndexable;
import com.derpquest.settings.Utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SearchIndexable
public class About extends SettingsPreferenceFragment implements
        OnPreferenceChangeListener, OnPreferenceClickListener, Indexable {

    private PreferenceScreen mDerpLogo;
    private PreferenceCategory mDeviceLinks;
    private Preference mDeviceFW;
    private Preference mDeviceRecovery;
    private Toast mDerpHitToast;

    private int mDerpLogoHitCountdown = 10;

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        addPreferencesFromResource(R.xml.derpquest_settings_about);
        final Resources res = getResources();
        final ContentResolver resolver = getActivity().getContentResolver();
        final PreferenceScreen prefScreen = getPreferenceScreen();

        mDeviceLinks = (PreferenceCategory) findPreference("about_device_links");
        mDeviceFW = (Preference) findPreference("device_fw");
        mDeviceRecovery = (Preference) findPreference("device_recovery");

        boolean hasFWLink = !(res.getString(R.string.about_device_fw_link).equals(""));
        if (!hasFWLink)
            mDeviceFW.setVisible(false);

        boolean hasRecoveryLink = !(res.getString(R.string.about_device_recovery_link).equals(""));
        if (!hasRecoveryLink)
            mDeviceRecovery.setVisible(false);

        if (!hasFWLink && !hasRecoveryLink) {
            mDeviceLinks.setVisible(false);
        } else {
            String defaultBrowser = null;
            try {
                Intent browserIntent = new Intent("android.intent.action.VIEW", Uri.parse("https://"));
                ResolveInfo resolveInfo = getPackageManager().resolveActivity(browserIntent,PackageManager.MATCH_DEFAULT_ONLY);
                defaultBrowser = resolveInfo.activityInfo.packageName;
            } catch (Exception e) {
                // nothing to do. defaultBrowser already set to null
            }

            // if we have no default browser set we disable the buttons and let the user know
            if (defaultBrowser == null) {
                mDeviceFW.setEnabled(false);
                mDeviceFW.setSummary(res.getString(R.string.no_browser));
                mDeviceRecovery.setEnabled(false);
                mDeviceRecovery.setSummary(res.getString(R.string.no_browser));
            }
        }

        mDerpLogo = (PreferenceScreen) findPreference("derp_logo");
        mDerpLogo.setOnPreferenceClickListener(this);
        mDerpHitToast = null;
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        return false;
    }

    @Override
    public boolean onPreferenceClick(Preference preference) {
        if (mDerpLogoHitCountdown > 0) {
            mDerpLogoHitCountdown--;
            if (mDerpLogoHitCountdown == 0) {
                if (mDerpHitToast != null) {
                    mDerpHitToast.cancel();
                }
                mDerpHitToast = Toast.makeText(getContext(),
                        getResources().getString(R.string.derpd_done),
                        Toast.LENGTH_SHORT);
                mDerpHitToast.show();
                throw new RuntimeException("BOOYAH!"); // Crash :)
            } else {
                if (mDerpHitToast != null) {
                    mDerpHitToast.cancel();
                }
                mDerpHitToast = Toast.makeText(getContext(),
                        getResources().getQuantityString(
                        R.plurals.show_derped_countdown, mDerpLogoHitCountdown,
                        mDerpLogoHitCountdown), Toast.LENGTH_SHORT);
                mDerpHitToast.show();
            }
        }
        return true;
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
                    sir.xmlResId = R.xml.derpquest_settings_about;
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
