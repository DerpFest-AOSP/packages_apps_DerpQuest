/*
 * Copyright (C) 2021 DerpFest
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

package com.derpquest.settings.fragments.ui;

import android.provider.SearchIndexableResource;

import static android.os.UserHandle.USER_CURRENT;
import static android.os.UserHandle.USER_SYSTEM;

import android.content.ContentResolver;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.RemoteException;
import android.provider.Settings;
import android.os.ServiceManager;
import android.os.UserHandle;
import android.content.Context;
import android.content.om.IOverlayManager;
import com.android.settings.R;

import com.android.settings.display.OverlayCategoryPreferenceController;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settingslib.search.SearchIndexable;

import com.android.settings.SettingsPreferenceFragment;
import com.android.internal.logging.nano.MetricsProto;
import androidx.preference.ListPreference;
import androidx.preference.SwitchPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceScreen;
import androidx.preference.Preference.OnPreferenceChangeListener;

import com.derpquest.settings.utils.Utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.derp.support.preference.SystemSettingListPreference;

@SearchIndexable
public class QuickSettings extends SettingsPreferenceFragment implements OnPreferenceChangeListener {

    private static final String CLEAR_ALL_ICON_STYLE  = "clear_all_icon_style";
    public static final String[] CLEAR_ALL_ICONS = {
        "com.android.theme.systemui_clearall_oos"
    };
    private Handler mHandler;
    private IOverlayManager mOverlayManager;
    private IOverlayManager mOverlayService;
   

    private SystemSettingListPreference mClearAll;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.quick_settings);
        PreferenceScreen prefSet = getPreferenceScreen();
        ContentResolver resolver = getActivity().getContentResolver();

        mOverlayService = IOverlayManager.Stub
                .asInterface(ServiceManager.getService(Context.OVERLAY_SERVICE));

        mClearAll = (SystemSettingListPreference) findPreference(CLEAR_ALL_ICON_STYLE);
        mCustomSettingsObserver.observe();
    }

    private CustomSettingsObserver mCustomSettingsObserver = new CustomSettingsObserver(mHandler);
    private class CustomSettingsObserver extends ContentObserver {

        CustomSettingsObserver(Handler handler) {
            super(handler);
        }

        void observe() {
            Context mContext = getContext();
            ContentResolver resolver = mContext.getContentResolver();
            resolver.registerContentObserver(Settings.System.getUriFor(
                    Settings.System.CLEAR_ALL_ICON_STYLE  ),
                    false, this, UserHandle.USER_ALL);
        }

        @Override
        public void onChange(boolean selfChange, Uri uri) {
            if (uri.equals(Settings.System.getUriFor(Settings.System.CLEAR_ALL_ICON_STYLE))) {
                updateClearAll();
            }
        }
    }

    private void updateClearAll() {
        ContentResolver resolver = getActivity().getContentResolver();
        boolean ClearAllDefault = Settings.System.getIntForUser(getContext().getContentResolver(),
                Settings.System.CLEAR_ALL_ICON_STYLE , 0, UserHandle.USER_CURRENT) == 0;
        boolean ClearAllOOS = Settings.System.getIntForUser(getContext().getContentResolver(),
                Settings.System.CLEAR_ALL_ICON_STYLE , 0, UserHandle.USER_CURRENT) == 1;

        if (ClearAllDefault) {
            setDefaultClearAll(mOverlayManager);
        } else if (ClearAllOOS) {
            enableClearAll(mOverlayManager, "com.android.theme.systemui_clearall_oos");
        }
    }

    public static void setDefaultClearAll(IOverlayManager overlayManager) {
        for (int i = 0; i < CLEAR_ALL_ICONS.length; i++) {
            String icons = CLEAR_ALL_ICONS[i];
            try {
                overlayManager.setEnabled(icons, false, USER_SYSTEM);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    public static void enableClearAll(IOverlayManager overlayManager, String overlayName) {
        try {
            for (int i = 0; i < CLEAR_ALL_ICONS.length; i++) {
                String icons = CLEAR_ALL_ICONS[i];
                try {
                    overlayManager.setEnabled(icons, false, USER_SYSTEM);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
            overlayManager.setEnabled(overlayName, true, USER_SYSTEM);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

   

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
		if (preference == mClearAll) {
            mCustomSettingsObserver.observe();
            Utils.showSystemUiRestartDialog(getContext());
            return true;
        }
        return false;
    }

    @Override
    public int getMetricsCategory() {
        return MetricsProto.MetricsEvent.DERP;
    }


    /**
     * For Search.
     */
    public static final SearchIndexProvider SEARCH_INDEX_DATA_PROVIDER =
            new BaseSearchIndexProvider() {

                @Override
                public List<SearchIndexableResource> getXmlResourcesToIndex(
                        Context context, boolean enabled) {
                    final SearchIndexableResource sir = new SearchIndexableResource(context);
                    sir.xmlResId = R.xml.quick_settings;
                    return Arrays.asList(sir);
                }

                @Override
                public List<String> getNonIndexableKeys(Context context) {
                    final List<String> keys = super.getNonIndexableKeys(context);
                    return keys;
                }
    };
}
