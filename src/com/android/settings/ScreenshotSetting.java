package com.android.settings;

import java.util.Timer;
import java.util.TimerTask;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.preference.CheckBoxPreference;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceScreen;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import android.provider.Settings;
import android.util.Log;
import android.view.Gravity;
import android.view.WindowManager;
import android.view.ViewGroup.LayoutParams;
import android.widget.TextView;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.os.SystemProperties;
import android.content.res.Resources;

public class ScreenshotSetting extends SettingsPreferenceFragment implements OnPreferenceChangeListener {
    /**
     * Called when the activity is first created.
     */
    private static final String KEY_SCREENSHOT_DELAY = "screenshot_delay";
    private static final String KEY_SCREENSHOT_STORAGE_LOCATION = "screenshot_storage";
    private static final String KEY_SCREENSHOT_SHOW = "screenshot_show";
    private static final String KEY_SCREENSHOT_VERSION = "screenshot_version";

    private ListPreference mDelay;
    private ListPreference mStorage;
    private CheckBoxPreference mShow;
    private Preference mVersion;

    private SharedPreferences mSharedPreference;
    private SharedPreferences.Editor mEdit;
    private SettingsApplication mScreenshot;

    private Context mContext;
    private Dialog dialog;
    private static final String INTERNAL_STORAGE = "internal_storage";
    private static final String EXTERNAL_SD_STORAGE = "external_sd_storage";
    private static final String EXTERNAL_USB_STORAGE = "internal_usb_storage";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.screenshot);

        mContext = getActivity();
        mDelay = (ListPreference) findPreference(KEY_SCREENSHOT_DELAY);
        mStorage = (ListPreference) findPreference(KEY_SCREENSHOT_STORAGE_LOCATION);
        mShow = (CheckBoxPreference) findPreference(KEY_SCREENSHOT_SHOW);
        mVersion = (Preference) findPreference(KEY_SCREENSHOT_VERSION);

        mShow.setOnPreferenceChangeListener(this);
        mDelay.setOnPreferenceChangeListener(this);
        mStorage.setOnPreferenceChangeListener(this);

        mSharedPreference = this.getPreferenceScreen().getSharedPreferences();
        mEdit = mSharedPreference.edit();

        String summary_delay = mDelay.getSharedPreferences().getString("screenshot_delay", "15");
        mDelay.setSummary(summary_delay + getString(R.string.later));
        mDelay.setValue(summary_delay);
        String summary_storage = mStorage.getSharedPreferences().getString("screenshot_storage", "flash");
        mStorage.setValue(summary_storage);
        mStorage.setSummary(mStorage.getEntry());
        boolean isShow = Settings.System.getInt(mContext.getContentResolver(), Settings.System.SCREENSHOT_BUTTON_SHOW, 1) == 1;
        mShow.setChecked(isShow);
        Resources res = mContext.getResources();
        boolean mHasNavigationBar = res.getBoolean(com.android.internal.R.bool.config_showNavigationBar);
        if (!mHasNavigationBar) {
            getPreferenceScreen().removePreference(mShow);
        }
        getPreferenceScreen().removePreference(mVersion);

        mScreenshot = (SettingsApplication) getActivity().getApplication();
    }


    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        // TODO Auto-generated method stub
        if (preference == mDelay) {
            int value = Integer.parseInt((String) newValue);
            mDelay.setSummary((String) newValue + getString(R.string.later));
            mScreenshot.startScreenshot(value);
        } else if (preference == mStorage) {
            String value = (String) newValue;
            //mEdit.putString("storageLocation",value);
            if (value.equals("flash")) {
                Settings.System.putString(getContentResolver(), Settings.System.SCREENSHOT_LOCATION, INTERNAL_STORAGE);
                mStorage.setSummary(mStorage.getEntries()[0]);
            } else if (value.equals("sdcard")) {
                Settings.System.putString(getContentResolver(), Settings.System.SCREENSHOT_LOCATION, EXTERNAL_SD_STORAGE);
                mStorage.setSummary(mStorage.getEntries()[1]);
            } else if (value.equals("usb")) {
                Settings.System.putString(getContentResolver(), Settings.System.SCREENSHOT_LOCATION, EXTERNAL_USB_STORAGE);
                mStorage.setSummary(mStorage.getEntries()[2]);
            }

        }
        return true;
    }

    @Override
    public boolean onPreferenceTreeClick(Preference preference) {
        // TODO Auto-generated method stub
        if (preference == mShow) {
            boolean show = mShow.isChecked();
            Settings.System.putInt(getContentResolver(), Settings.System.SCREENSHOT_BUTTON_SHOW, show ? 1 : 0);
        }
        return super.onPreferenceTreeClick(preference);
    }

    @Override
    protected int getMetricsCategory() {
        // TODO Auto-generated method stub
        return InstrumentedFragment.SCREENSHOT;
    }

}
