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

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.SELinux;
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
import android.util.Log;

import com.derpquest.settings.preferences.AppMultiSelectListPreference;
import com.derpquest.settings.preferences.ScrollAppsViewPreference;
import com.derpquest.settings.preferences.SystemSettingSwitchPreference;
import com.derpquest.settings.Utils;
import com.derpquest.settings.utils.SuShell;
import com.derpquest.settings.utils.SuTask;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import com.android.settings.SettingsPreferenceFragment;

public class MiscSettings extends SettingsPreferenceFragment implements
        OnPreferenceChangeListener {

    private static final String TAG = "MiscSettings";

    private static final String KEY_ASPECT_RATIO_APPS_ENABLED = "aspect_ratio_apps_enabled";
    private static final String KEY_ASPECT_RATIO_APPS_LIST = "aspect_ratio_apps_list";
    private static final String KEY_ASPECT_RATIO_CATEGORY = "aspect_ratio_category";
    private static final String KEY_ASPECT_RATIO_APPS_LIST_SCROLLER = "aspect_ratio_apps_list_scroller";
    private static final String SYSTEM_PROXI_CHECK_ENABLED = "system_proxi_check_enabled";
    private static final String SELINUX_CATEGORY = "selinux";
    private static final String SELINUX_EXPLANATION = "selinux_explanation";
    private static final String PREF_SELINUX_MODE = "selinux_mode";
    private static final String PREF_SELINUX_PERSISTENCE = "selinux_persistence";

    private AppMultiSelectListPreference mAspectRatioAppsSelect;
    private ScrollAppsViewPreference mAspectRatioApps;
    private SwitchPreference mSelinuxMode;
    private SwitchPreference mSelinuxPersistence;

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

      // SELinux
      Preference selinuxCategory = findPreference(SELINUX_CATEGORY);
      Preference selinuxExp = findPreference(SELINUX_EXPLANATION);
      mSelinuxMode = (SwitchPreference) findPreference(PREF_SELINUX_MODE);
      mSelinuxMode.setChecked(SELinux.isSELinuxEnforced());

      mSelinuxPersistence =
          (SwitchPreference) findPreference(PREF_SELINUX_PERSISTENCE);
      mSelinuxPersistence.setChecked(getContext()
          .getSharedPreferences("selinux_pref", Context.MODE_PRIVATE)
          .contains(PREF_SELINUX_MODE));

      // Disabling root required switches if unrooted and letting the user know
      if (!Utils.isRooted(getContext())) {
        Log.e(TAG, "Root not found");
        mSelinuxMode.setEnabled(false);
        mSelinuxPersistence.setEnabled(false);
        mSelinuxPersistence.setChecked(false);
        selinuxExp.setSummary(selinuxExp.getSummary() + "\n" +
            getResources().getString(R.string.selinux_unrooted_summary));
      } else {
        mSelinuxPersistence.setOnPreferenceChangeListener(this);
        mSelinuxMode.setOnPreferenceChangeListener(this);
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
      } else if (preference == mSelinuxMode) {
        boolean enabled = (Boolean) objValue;
        new SwitchSelinuxTask(getActivity()).execute(enabled);
        setSelinuxEnabled(enabled, mSelinuxPersistence.isChecked());
        return true;
      } else if (preference == mSelinuxPersistence) {
        setSelinuxEnabled(mSelinuxMode.isChecked(), (Boolean) objValue);
        return true;
      }
      return false;
    }

    @Override
    public int getMetricsCategory() {
      return MetricsProto.MetricsEvent.OWLSNEST;
    }

    private void setSelinuxEnabled(boolean status, boolean persistent) {
      SharedPreferences.Editor editor = getContext()
          .getSharedPreferences("selinux_pref", Context.MODE_PRIVATE).edit();
      if (persistent) {
        editor.putBoolean(PREF_SELINUX_MODE, status);
      } else {
        editor.remove(PREF_SELINUX_MODE);
      }
      editor.apply();
      mSelinuxMode.setChecked(status);
    }

    private class SwitchSelinuxTask extends SuTask<Boolean> {
      public SwitchSelinuxTask(Context context) {
        super(context);
      }
      @Override
      protected void sudoInBackground(Boolean... params) throws SuShell.SuDeniedException {
        if (params.length != 1) {
          Log.e(TAG, "SwitchSelinuxTask: invalid params count");
          return;
        }
        if (params[0]) {
          SuShell.runWithSuCheck("setenforce 1");
        } else {
          SuShell.runWithSuCheck("setenforce 0");
        }
      }

      @Override
      protected void onPostExecute(Boolean result) {
        super.onPostExecute(result);
        if (!result) {
          // Did not work, so restore actual value
          setSelinuxEnabled(SELinux.isSELinuxEnforced(), mSelinuxPersistence.isChecked());
        }
      }
    }
}
