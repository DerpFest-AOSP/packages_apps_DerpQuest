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
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.database.ContentObserver;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.os.SystemProperties;
import android.os.Handler;
import android.os.UserHandle;
import android.provider.SearchIndexableResource;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
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

import java.io.FileDescriptor;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@SearchIndexable
public class FodGeneral extends SettingsPreferenceFragment implements
        Preference.OnPreferenceChangeListener, Indexable {

    private ContentResolver mResolver;
    private static final String ANIMA_LIST = "fod_recognizing_animation_list";
    private static final String ANIMA_TOGGLE = "fod_recognizing_animation";
    private static final String FINGERPRINT_CUSTOM_ICON = "custom_fingerprint_icon";
    private static final String FINGERPRINT_ICON_ANIME = "fod_icon_animation";
    private static final int GET_CUSTOM_FP_ICON = 69;
    private Preference mFilePicker;
    private SystemSettingSwitchPreference mIconAnima;

    private Handler mHandler;

    private static final String FOOTER = "custom_fod_icon_footer";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.fod_general);
        PreferenceScreen prefScreen = getPreferenceScreen();
        mResolver = getActivity().getContentResolver();
        Context mContext = getContext();
        final PackageManager mPm = getActivity().getPackageManager();

        mFilePicker = (Preference) findPreference(FINGERPRINT_CUSTOM_ICON);
        mIconAnima = (SystemSettingSwitchPreference) findPreference(FINGERPRINT_ICON_ANIME);

        findPreference(FOOTER).setTitle(R.string.custom_fod_icon_png_explainer);

        boolean isFODDevice = getResources().getBoolean(com.android.internal.R.bool.config_needCustomFODView);
        if (!isFODDevice){
            removePreference(FINGERPRINT_CUSTOM_ICON);
        } else {
            final String customIconURI = Settings.System.getString(getContext().getContentResolver(),
                Settings.System.OMNI_CUSTOM_FP_ICON);

            if (!TextUtils.isEmpty(customIconURI)) {
                setPickerIcon(customIconURI);
                mFilePicker.setSummary(customIconURI);
            }

            mFilePicker.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    Intent intent = new Intent(Intent.ACTION_PICK);
                    intent.setType("image/png");

                    startActivityForResult(intent, GET_CUSTOM_FP_ICON);

                    return true;
                }
            });
        }

        SystemSettingSwitchPreference AnimaTogglePref = (SystemSettingSwitchPreference) findPreference("fod_recognizing_animation");
        SystemSettingListPreference AnimaListPref = (SystemSettingListPreference) findPreference("fod_recognizing_animation_list");

        if (AnimaTogglePref != null && AnimaListPref != null
                && !com.android.internal.util.derp.derpUtils.isPackageInstalled(mContext,"com.derp.fod.animations")) {
            prefScreen.removePreference(AnimaTogglePref);
            prefScreen.removePreference(AnimaListPref);
        }

        mCustomSettingsObserver.observe();
        mCustomSettingsObserver.update();
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
                    Settings.System.FOD_ICON_ANIMATION),
                    false, this, UserHandle.USER_ALL);
        }

        @Override
        public void onChange(boolean selfChange, Uri uri) {
            if (uri.equals(Settings.System.getUriFor(Settings.System.FOD_ICON_ANIMATION))) {
                updatePrebuiltIcons();
            }
        }

        public void update() {
            updatePrebuiltIcons();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode,
        Intent resultData) {
        if (requestCode == GET_CUSTOM_FP_ICON && resultCode == Activity.RESULT_OK) {
            Uri uri = null;
            if (resultData != null) {
                uri = resultData.getData();
                mFilePicker.setSummary(uri.toString());
                setPickerIcon(uri.toString());
                Settings.System.putString(getContentResolver(), Settings.System.OMNI_CUSTOM_FP_ICON,
                    uri.toString());
            }
        } else if (requestCode == GET_CUSTOM_FP_ICON && resultCode == Activity.RESULT_CANCELED) {
            mFilePicker.setSummary("");
            mFilePicker.setIcon(new ColorDrawable(Color.TRANSPARENT));
            Settings.System.putString(getContentResolver(), Settings.System.OMNI_CUSTOM_FP_ICON, "");
        }
    }


    private void setPickerIcon(String uri) {
        try {
                ParcelFileDescriptor parcelFileDescriptor =
                    getContext().getContentResolver().openFileDescriptor(Uri.parse(uri), "r");
                FileDescriptor fileDescriptor = parcelFileDescriptor.getFileDescriptor();
                Bitmap image = BitmapFactory.decodeFileDescriptor(fileDescriptor);
                parcelFileDescriptor.close();
                Drawable d = new BitmapDrawable(getResources(), image);
                mFilePicker.setIcon(d);
            }
            catch (Exception e) {}
    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        ContentResolver resolver = getActivity().getContentResolver();
        if (preference == mIconAnima) {
            mCustomSettingsObserver.observe();
            mCustomSettingsObserver.update();
            return true;
        }
        return false;
    }

    private void updatePrebuiltIcons() {
        ContentResolver resolver = getActivity().getContentResolver();

        boolean animeCon = Settings.System.getIntForUser(resolver,
                Settings.System.FOD_ICON_ANIMATION, 0, UserHandle.USER_CURRENT) != 0;

        if (!animeCon) {
            mFilePicker.setEnabled(true);
        } else {
            mFilePicker.setEnabled(false);
        }
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
