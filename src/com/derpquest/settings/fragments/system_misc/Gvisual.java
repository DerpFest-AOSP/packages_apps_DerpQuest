/*
 * Copyright (C) 2021 DerpFest
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.derpquest.settings.fragments.system_misc;

import static android.os.UserHandle.USER_SYSTEM;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.om.IOverlayManager;
import android.content.om.OverlayInfo;
import android.content.pm.PackageManager;
import android.os.ServiceManager;
import android.os.RemoteException;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.provider.SearchIndexableResource;
import android.provider.Settings;

import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceScreen;
import androidx.preference.Preference.OnPreferenceChangeListener;
import androidx.preference.SwitchPreference;

import com.android.internal.logging.nano.MetricsProto;
import com.android.settings.R;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settingslib.search.SearchIndexable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.android.internal.util.derp.ThemesUtils;
import com.android.internal.util.derp.derpUtils;

@SearchIndexable
public class Gvisual extends SettingsPreferenceFragment implements
         OnPreferenceChangeListener {

    private static final String PREF_SB_HEIGHT = "statusbar_height";
    private static final String PREF_NB_COLOR = "navbar_color";
    private static final String PREF_HD_SIZE = "header_size";

    private IOverlayManager mOverlayManager;
    private IOverlayManager mOverlayService;
    private ListPreference mSbHeight;
    private ListPreference mnbSwitch;
    private ListPreference mhdSize;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.gvisual);

        PreferenceScreen prefScreen = getPreferenceScreen();
        ContentResolver resolver = getActivity().getContentResolver();

        mOverlayService = IOverlayManager.Stub
                .asInterface(ServiceManager.getService(Context.OVERLAY_SERVICE));

        setupNavbarSwitchPref();
        setupHeaderSwitchPref();

        mSbHeight = (ListPreference) findPreference(PREF_SB_HEIGHT);
        int sbHeightValue = getOverlayPosition(ThemesUtils.STATUSBAR_HEIGHT);
        if (sbHeightValue != -1) {
            mSbHeight.setValue(String.valueOf(sbHeightValue + 2));
        } else {
            mSbHeight.setValue("1");
        }
        mSbHeight.setSummary(mSbHeight.getEntry());
        mSbHeight.setOnPreferenceChangeListener(this);
    }

    public void handleOverlays(String packagename, Boolean state, IOverlayManager mOverlayManager) {
        try {
            mOverlayService.setEnabled(packagename, state, USER_SYSTEM);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object objValue) {
        ContentResolver resolver = getActivity().getContentResolver();
        if (preference == mnbSwitch){
        String nbSwitch = (String) objValue;
        final Context context = getContext();
        switch (nbSwitch) {
            case "1":
            handleOverlays(ThemesUtils.NAVBAR_COLOR_ORCD, false, mOverlayManager);
            handleOverlays(ThemesUtils.NAVBAR_COLOR_OPRD, false, mOverlayManager);
            handleOverlays(ThemesUtils.NAVBAR_COLOR_PURP, false, mOverlayManager);
            handleOverlays(ThemesUtils.NAVBAR_COLOR_BLUE, false, mOverlayManager);
            handleOverlays(ThemesUtils.NAVBAR_COLOR_ROSE, false, mOverlayManager);
            break;
            case "2":
            handleOverlays(ThemesUtils.NAVBAR_COLOR_ORCD, true, mOverlayManager);
            handleOverlays(ThemesUtils.NAVBAR_COLOR_OPRD, false, mOverlayManager);
            handleOverlays(ThemesUtils.NAVBAR_COLOR_PURP, false, mOverlayManager);
            handleOverlays(ThemesUtils.NAVBAR_COLOR_BLUE, false, mOverlayManager);
            handleOverlays(ThemesUtils.NAVBAR_COLOR_ROSE, false, mOverlayManager);
            break;
            case "3":
            handleOverlays(ThemesUtils.NAVBAR_COLOR_ORCD, false, mOverlayManager);
            handleOverlays(ThemesUtils.NAVBAR_COLOR_OPRD, true, mOverlayManager);
            handleOverlays(ThemesUtils.NAVBAR_COLOR_PURP, false, mOverlayManager);
            handleOverlays(ThemesUtils.NAVBAR_COLOR_BLUE, false, mOverlayManager);
            handleOverlays(ThemesUtils.NAVBAR_COLOR_ROSE, false, mOverlayManager);
            break;
            case "4":
            handleOverlays(ThemesUtils.NAVBAR_COLOR_ORCD, false, mOverlayManager);
            handleOverlays(ThemesUtils.NAVBAR_COLOR_OPRD, false, mOverlayManager);
            handleOverlays(ThemesUtils.NAVBAR_COLOR_PURP, true, mOverlayManager);
            handleOverlays(ThemesUtils.NAVBAR_COLOR_BLUE, false, mOverlayManager);
            handleOverlays(ThemesUtils.NAVBAR_COLOR_ROSE, false, mOverlayManager);
            break;
            case "5":
            handleOverlays(ThemesUtils.NAVBAR_COLOR_ORCD, false, mOverlayManager);
            handleOverlays(ThemesUtils.NAVBAR_COLOR_OPRD, false, mOverlayManager);
            handleOverlays(ThemesUtils.NAVBAR_COLOR_PURP, false, mOverlayManager);
            handleOverlays(ThemesUtils.NAVBAR_COLOR_BLUE, true, mOverlayManager);
            handleOverlays(ThemesUtils.NAVBAR_COLOR_ROSE, false, mOverlayManager);
            break;
            case "6":
            handleOverlays(ThemesUtils.NAVBAR_COLOR_ORCD, false, mOverlayManager);
            handleOverlays(ThemesUtils.NAVBAR_COLOR_OPRD, false, mOverlayManager);
            handleOverlays(ThemesUtils.NAVBAR_COLOR_PURP, false, mOverlayManager);
            handleOverlays(ThemesUtils.NAVBAR_COLOR_BLUE, false, mOverlayManager);
            handleOverlays(ThemesUtils.NAVBAR_COLOR_ROSE, true, mOverlayManager);
            break;
        }
        try {
             mOverlayService.reloadAndroidAssets(UserHandle.USER_CURRENT);
             mOverlayService.reloadAssets("com.android.settings", UserHandle.USER_CURRENT);
             mOverlayService.reloadAssets("com.android.systemui", UserHandle.USER_CURRENT);
         } catch (RemoteException ignored) {
         }
        return true;
        } else if (preference == mhdSize){
        String hdsize = (String) objValue;
        final Context context = getContext();
        switch (hdsize) {
            case "1":
            handleOverlays(ThemesUtils.HEADER_LARGE, false, mOverlayManager);
            handleOverlays(ThemesUtils.HEADER_XLARGE, false, mOverlayManager);
            break;
            case "2":
            handleOverlays(ThemesUtils.HEADER_LARGE, true, mOverlayManager);
            handleOverlays(ThemesUtils.HEADER_XLARGE, false, mOverlayManager);
            break;
            case "3":
            handleOverlays(ThemesUtils.HEADER_LARGE, false, mOverlayManager);
            handleOverlays(ThemesUtils.HEADER_XLARGE, true, mOverlayManager);
            break;
        }
        try {
             mOverlayService.reloadAndroidAssets(UserHandle.USER_CURRENT);
             mOverlayService.reloadAssets("com.android.settings", UserHandle.USER_CURRENT);
             mOverlayService.reloadAssets("com.android.systemui", UserHandle.USER_CURRENT);
         } catch (RemoteException ignored) {
         }
        return true;
        } else if (preference == mSbHeight) {
        String sbheight = (String) objValue;
        int sbheightValue = Integer.parseInt(sbheight);
        mSbHeight.setValue(String.valueOf(sbheightValue));
        String overlayName = getOverlayName(ThemesUtils.STATUSBAR_HEIGHT);
            if (overlayName != null) {
                handleOverlays(overlayName, false, mOverlayManager);
            }
            if (sbheightValue > 1) {
                handleOverlays(ThemesUtils.STATUSBAR_HEIGHT[sbheightValue -2],
                        true, mOverlayManager);
        }
        mSbHeight.setSummary(mSbHeight.getEntry());
        return true;
        }
        return false;
    }

    private int getOverlayPosition(String[] overlays) {
        int position = -1;
        for (int i = 0; i < overlays.length; i++) {
            String overlay = overlays[i];
            if (derpUtils.isThemeEnabled(overlay)) {
                position = i;
            }
        }
        return position;
    }

    private String getOverlayName(String[] overlays) {
        String overlayName = null;
        for (int i = 0; i < overlays.length; i++) {
            String overlay = overlays[i];
            if (derpUtils.isThemeEnabled(overlay)) {
                overlayName = overlay;
            }
        }
        return overlayName;
    }

    private void setupNavbarSwitchPref() {
        mnbSwitch = (ListPreference) findPreference(PREF_NB_COLOR);
        mnbSwitch.setOnPreferenceChangeListener(this);
        if (derpUtils.isNavbarColor("com.gnonymous.gvisualmod.pgm_rose")){
            mnbSwitch.setValue("6");
        } else if (derpUtils.isNavbarColor("com.gnonymous.gvisualmod.pgm_blue")){
            mnbSwitch.setValue("5");
        } else if (derpUtils.isNavbarColor("com.gnonymous.gvisualmod.pgm_purp")){
            mnbSwitch.setValue("4");
        } else if (derpUtils.isNavbarColor("com.gnonymous.gvisualmod.pgm_oprd")){
            mnbSwitch.setValue("3");
        } else if (derpUtils.isNavbarColor("com.gnonymous.gvisualmod.pgm_orcd")){
            mnbSwitch.setValue("2");
        }
        else{
            mnbSwitch.setValue("1");
        }
    }

    private void setupHeaderSwitchPref() {
        mhdSize = (ListPreference) findPreference(PREF_HD_SIZE);
        mhdSize.setOnPreferenceChangeListener(this);
        if (derpUtils.isThemeEnabled("com.android.theme.header.xlarge")){
            mhdSize.setValue("3");
        } else if (derpUtils.isThemeEnabled("com.android.theme.header.large")){
            mhdSize.setValue("2");
        }
        else{
            mhdSize.setValue("1");
        }
    }

    @Override
    public int getMetricsCategory() {
        return MetricsProto.MetricsEvent.DERP;
    }

    public static final BaseSearchIndexProvider SEARCH_INDEX_DATA_PROVIDER =
            new BaseSearchIndexProvider() {

                @Override
                public List<SearchIndexableResource> getXmlResourcesToIndex(
                        Context context, boolean enabled) {
                    final SearchIndexableResource sir = new SearchIndexableResource(context);
                    sir.xmlResId = R.xml.gvisual;
                    return Arrays.asList(sir);
                }

                @Override
                public List<String> getNonIndexableKeys(Context context) {
                    ArrayList<String> result = new ArrayList<String>();
                    return result;
                }
            };
}
