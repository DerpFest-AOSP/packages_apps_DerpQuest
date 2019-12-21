/*
 * Copyright (C) 2017-2019 The PixelDust Project
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

import com.android.internal.logging.nano.MetricsProto;

import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.Preference.OnPreferenceChangeListener;
import androidx.preference.PreferenceScreen;
import androidx.preference.PreferenceCategory;
import androidx.preference.SwitchPreference;
import com.android.internal.util.aosip.DeviceUtils;
import com.android.settings.R;

import com.derpquest.settings.preferences.AppMultiSelectListPreference;
import com.derpquest.settings.preferences.ScrollAppsViewPreference;
import com.derpquest.settings.preferences.SystemSettingSwitchPreference;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import com.android.settings.SettingsPreferenceFragment;

public class MiscSettings extends SettingsPreferenceFragment implements
        OnPreferenceChangeListener {

    private static final String KEY_ASPECT_RATIO_APPS_ENABLED = "aspect_ratio_apps_enabled";
    private static final String KEY_ASPECT_RATIO_APPS_LIST = "aspect_ratio_apps_list";
    private static final String KEY_ASPECT_RATIO_CATEGORY = "aspect_ratio_category";
    private static final String KEY_ASPECT_RATIO_APPS_LIST_SCROLLER = "aspect_ratio_apps_list_scroller";
    private static final String SYSTEM_PROXI_CHECK_ENABLED = "system_proxi_check_enabled";

    private AppMultiSelectListPreference mAspectRatioAppsSelect;
    private ScrollAppsViewPreference mAspectRatioApps;


    @Override
    public void onCreate(Bundle icicle) {
      super.onCreate(icicle);

      addPreferencesFromResource(R.xml.derpquest_settings_misc);

      final PreferenceCategory aspectRatioCategory =
          (PreferenceCategory) getPreferenceScreen().findPreference(KEY_ASPECT_RATIO_CATEGORY);
      final boolean supportMaxAspectRatio =
          getResources().getBoolean(com.android.internal.R.bool.config_haveHigherAspectRatioScreen);
      if (!supportMaxAspectRatio) {
          getPreferenceScreen().removePreference(aspectRatioCategory);
      } else {
        mAspectRatioAppsSelect =
            (AppMultiSelectListPreference) findPreference(KEY_ASPECT_RATIO_APPS_LIST);
        mAspectRatioApps =
            (ScrollAppsViewPreference) findPreference(KEY_ASPECT_RATIO_APPS_LIST_SCROLLER);
        final String valuesString = Settings.System.getString(getContentResolver(),
            Settings.System.OMNI_ASPECT_RATIO_APPS_LIST);
        List<String> valuesList = new ArrayList<String>();
        if (!TextUtils.isEmpty(valuesString)) {
          valuesList.addAll(Arrays.asList(valuesString.split(":")));
          mAspectRatioApps.setVisible(true);
          mAspectRatioApps.setValues(valuesList);
        } else {
          mAspectRatioApps.setVisible(false);
        }
        mAspectRatioAppsSelect.setValues(valuesList);
        mAspectRatioAppsSelect.setOnPreferenceChangeListener(this);
      }

      boolean supportPowerButtonProxyCheck = getResources().getBoolean(com.android.internal.R.bool.config_proxiSensorWakupCheck);
      SystemSettingSwitchPreference proxyCheckPreference = (SystemSettingSwitchPreference) findPreference(SYSTEM_PROXI_CHECK_ENABLED);
      if (!DeviceUtils.deviceSupportsProximitySensor(getActivity()) || !supportPowerButtonProxyCheck) {
          getPreferenceScreen().removePreference(proxyCheckPreference);
      }
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object objValue) {
      if (preference == mAspectRatioAppsSelect) {
        Collection<String> valueList = (Collection<String>) objValue;
        mAspectRatioApps.setVisible(false);
        if (valueList != null) {
          Settings.System.putString(getContentResolver(),
              Settings.System.OMNI_ASPECT_RATIO_APPS_LIST, TextUtils.join(":", valueList));
          mAspectRatioApps.setVisible(true);
          mAspectRatioApps.setValues(valueList);
        } else {
          Settings.System.putString(getContentResolver(),
              Settings.System.OMNI_ASPECT_RATIO_APPS_LIST, "");
        }
        return true;
      }
      return false;
    }

    @Override
    public int getMetricsCategory() {
      return MetricsProto.MetricsEvent.OWLSNEST;
    }
}
