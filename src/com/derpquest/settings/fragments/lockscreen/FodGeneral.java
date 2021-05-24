/*
 * Copyright (C) 2021 ShapeShiftOS
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

package com.derpquest.settings.fragments.lockscreen;

import android.app.Activity;
import android.content.Context;
import android.content.ContentResolver;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.SystemProperties;
import android.provider.SearchIndexableResource;
import android.provider.Settings;
import androidx.preference.*;

import com.android.settings.R;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settingslib.search.Indexable;
import com.android.settingslib.search.SearchIndexable;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.Utils;

import com.android.internal.logging.nano.MetricsProto;

import android.hardware.biometrics.BiometricSourceType;
import android.hardware.fingerprint.FingerprintManager;

import com.derp.support.preference.SystemSettingSwitchPreference;
import com.derp.support.preference.SystemSettingListPreference;

import com.android.internal.util.custom.FodUtils;

import java.util.ArrayList;
import java.util.List;

@SearchIndexable
public class FodGeneral extends SettingsPreferenceFragment implements
        Preference.OnPreferenceChangeListener, Indexable {

    private ContentResolver mResolver;
    private static final String ANIMA_LIST = "fod_recognizing_animation_list";
    private static final String ANIMA_TOGGLE = "fod_recognizing_animation";

    private static final String FOOTER = "custom_fod_icon_footer";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.fod_general);
        PreferenceScreen prefScreen = getPreferenceScreen();
        mResolver = getActivity().getContentResolver();
        Context mContext = getContext();
        final PackageManager mPm = getActivity().getPackageManager();

        findPreference(FOOTER).setTitle(R.string.custom_fod_icon_explainer);

        SystemSettingSwitchPreference AnimaTogglePref = (SystemSettingSwitchPreference) findPreference("fod_recognizing_animation");
        SystemSettingListPreference AnimaListPref = (SystemSettingListPreference) findPreference("fod_recognizing_animation_list");

        if (!com.android.internal.util.derp.derpUtils.isPackageInstalled(mContext,"com.derp.fod.animations")) {
            prefScreen.removePreference(AnimaTogglePref);
            prefScreen.removePreference(AnimaListPref);
        }

    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        ContentResolver resolver = getActivity().getContentResolver();
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
                sir.xmlResId = R.xml.fod_general;
                result.add(sir);
                return result;
            }

            @Override
            public List<String> getNonIndexableKeys(Context context) {
                final List<String> keys = super.getNonIndexableKeys(context);
                return keys;
            }
    };
}
