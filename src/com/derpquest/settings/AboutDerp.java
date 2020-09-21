package com.derpquest.settings;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import com.android.internal.logging.nano.MetricsProto.MetricsEvent;
import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;

public class AboutDerp extends SettingsPreferenceFragment {

    @Override
    public int getMetricsCategory() {
        return MetricsEvent.DERP;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.about);
    }

    @Override
    public void setDivider(Drawable divider) {
        super.setDivider(new ColorDrawable(Color.TRANSPARENT));
    }

    @Override
    public void setDividerHeight(int height) {
        super.setDividerHeight(0);
    }
}
