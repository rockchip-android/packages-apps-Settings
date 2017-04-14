package com.android.settings;

import com.android.internal.logging.MetricsLogger;
import com.android.internal.logging.MetricsProto.MetricsEvent;
import android.util.Log;
import static android.provider.Settings.System.SCREEN_OFF_TIMEOUT;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import javax.xml.transform.Result;
import com.android.settings.widget.SwitchBar;
import android.os.AsyncTask;
import android.os.SystemProperties;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Switch;
import android.widget.Toast;
import android.content.ContentResolver;
import android.os.Handler;
import android.database.ContentObserver;
import android.os.RemoteException;
import android.os.DisplayOutputManager;
import android.support.v7.preference.CheckBoxPreference;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceScreen;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import android.os.SystemProperties;
import com.android.settings.display.*;
import android.app.DialogFragment;
import com.android.settings.data.ConstData;
import android.hardware.display.DisplayManager;
import android.view.Display;

public class HdmiSettings extends SettingsPreferenceFragment
       implements OnPreferenceChangeListener,SwitchBar.OnSwitchChangeListener,Preference.OnPreferenceClickListener{
   /** Called when the activity is first created. */
   private static final String TAG = "HdmiControllerActivity";
   private static final String KEY_HDMI_RESOLUTION = "hdmi_resolution";
   private static final String KEY_HDMI_SCALE="hdmi_screen_zoom";
   private static final String KEY_HDMI_LCD = "hdmi_lcd_timeout";
   // for identify the HdmiFile state
   private boolean IsHdmiConnect = false;
   // for identify the Hdmi connection state
   private boolean IsHdmiPlug = false;
   private boolean IsHdmiDisplayOn = false;

   private ListPreference mHdmiResolution;
   private ListPreference mHdmiLcd;
   private Preference mHdmiScale;
   private DisplayOutputManager mDisplayManagement = null;
   private Context context;
   private static final int DEF_HDMI_LCD_TIMEOUT_VALUE = 10;
   private SharedPreferences sharedPreferences;
   private SharedPreferences.Editor editor;
   private SwitchBar mSwitchBar;
   private int mDisplay;
   private String mOldResolution;
   protected String mStrPlatform;
   protected DisplayInfo mDisplayInfo;
   private DisplayManager mDisplayManager;
   private DisplayListener mDisplayListener;

   @Override
   protected int getMetricsCategory() {
       return MetricsEvent.DISPLAY;
   }

   @Override
   public void onCreate(Bundle savedInstanceState) {
       super.onCreate(savedInstanceState);
       context = getActivity();
       mDisplayManager = (DisplayManager)context.getSystemService(Context.DISPLAY_SERVICE);
       mDisplayListener = new DisplayListener();
       mStrPlatform = SystemProperties.get("ro.board.platform");
       mDisplayInfo = getDisplayInfo();
       addPreferencesFromResource(R.xml.hdmi_settings_timeout);
       mHdmiResolution = (ListPreference) findPreference(KEY_HDMI_RESOLUTION);
       mHdmiResolution.setOnPreferenceChangeListener(this);
       mHdmiResolution.setOnPreferenceClickListener(this);
       mHdmiResolution.setEntries(DrmDisplaySetting.getDisplayModes(mDisplayInfo).toArray(new String[0]));
       mHdmiResolution.setEntryValues(DrmDisplaySetting.getDisplayModes(mDisplayInfo).toArray(new String[0]));
       mHdmiScale = findPreference(KEY_HDMI_SCALE);
       mHdmiScale.setOnPreferenceClickListener(this);
       mHdmiLcd = (ListPreference) findPreference(KEY_HDMI_LCD);
       initHdmiLCD();
       Log.d(TAG,"onCreate---------------------");
   }

   
   @Override
   public View onCreateView(LayoutInflater inflater, ViewGroup container,
           Bundle savedInstanceState) {
       Log.d(TAG,"onCreateView----------------------------------------");
       return super.onCreateView(inflater, container, savedInstanceState);
   }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
       // TODO Auto-generated method stub
       super.onActivityCreated(savedInstanceState);
       final SettingsActivity activity = (SettingsActivity) getActivity();
       mSwitchBar = activity.getSwitchBar();
       mSwitchBar.show();
       //mSwitchBar.addOnSwitchChangeListener(this);
       //mSwitchBar.setChecked(sharedPreferences.getString("enable", "1").equals("1"));
       //String resolutionValue=sharedPreferences.getString("resolution", "1280x720p-60");
       //Log.d(TAG,"onActivityCreated resolutionValue="+resolutionValue);
      // context.registerReceiver(hdmiReceiver, new IntentFilter("android.intent.action.HDMI_PLUG"));
   }

    @Override
    public void onResume() {
        super.onResume();
        updateHDMIState();
        mDisplayManager.registerDisplayListener(mDisplayListener, null);
        getContentResolver().registerContentObserver(
        Settings.System.getUriFor(Settings.System.HDMI_LCD_TIMEOUT), true, mHdmiTimeoutSettingObserver);
    }

   public void onPause() {
       super.onPause();
       Log.d(TAG,"onPause----------------");
       mDisplayManager.unregisterDisplayListener(mDisplayListener);
       getContentResolver().unregisterContentObserver(mHdmiTimeoutSettingObserver);
   }

   public void onDestroy() {
       super.onDestroy();
   }

    private void initHdmiLCD(){
        mHdmiLcd.setOnPreferenceChangeListener(this);
        ContentResolver resolver = context.getContentResolver();
        long lcdTimeout = -1;
        if ((lcdTimeout = Settings.System.getLong(resolver, Settings.System.HDMI_LCD_TIMEOUT,
                   DEF_HDMI_LCD_TIMEOUT_VALUE)) > 0) {
               lcdTimeout /= 10;
        }
        mHdmiLcd.setValue(String.valueOf(lcdTimeout));
    }
    protected DisplayInfo getDisplayInfo() {
        return DrmDisplaySetting.getHdmiDisplayInfo();
    }

       /**
     * 还原分辨率值
     */
    public void updateResolutionValue(){
        String resolutionValue = null;
        resolutionValue = DrmDisplaySetting.getCurDisplayMode(mDisplayInfo);
        Log.i(TAG, "resolutionValue:" + resolutionValue);
        mOldResolution = resolutionValue;
        if(resolutionValue != null)
            mHdmiResolution.setValue(resolutionValue);
    }
    
    private void updateHDMIState(){
        Display[] allDisplays = mDisplayManager.getDisplays();
        if(allDisplays == null || allDisplays.length < 2){
            mHdmiResolution.setEnabled(false);
            mHdmiScale.setEnabled(false);
        }else{
            mHdmiResolution.setEnabled(true);
            mHdmiScale.setEnabled(true);
            updateResolutionValue();
        }
    }
    
    protected void showConfirmSetModeDialog() {
        DialogFragment df = ConfirmSetModeDialogFragment.newInstance(mDisplayInfo, new ConfirmSetModeDialogFragment.OnDialogDismissListener() {
            @Override
            public void onDismiss(boolean isok) {
                Log.i(TAG, "showConfirmSetModeDialog->onDismiss->isok:" + isok);
                Log.i(TAG, "showConfirmSetModeDialog->onDismiss->mOldResolution:" + mOldResolution);
                updateResolutionValue();
            }
        });
        df.show(getFragmentManager(), "ConfirmDialog");
    }
   
    @Override
    public boolean onPreferenceTreeClick(Preference preference) {
        // TODO Auto-generated method stub
       return true;
    }
    @Override
    public boolean onPreferenceClick(Preference preference) {
        if(preference == mHdmiScale) {
            Intent screenScaleIntent = new Intent(getActivity(), ScreenScaleActivity.class);
            screenScaleIntent.putExtra(ConstData.IntentKey.PLATFORM, mStrPlatform);
            screenScaleIntent.putExtra(ConstData.IntentKey.DISPLAY_INFO, mDisplayInfo);
            startActivity(screenScaleIntent);
        }else if(preference == mHdmiResolution){
            updateHDMIState();
        }
        return true;
    }

   @Override
   public boolean onPreferenceChange(Preference preference, Object obj) {
       Log.i(TAG, "onPreferenceChange:" + obj);
       String key = preference.getKey();
       Log.d(TAG, key);
       if (KEY_HDMI_RESOLUTION.equals(key)) {
            if(obj.equals(mOldResolution))
                return true;
            int index = mHdmiResolution.findIndexOfValue((String)obj);
            boolean isSuccess = DrmDisplaySetting.setDisplayModeTemp(mDisplayInfo, index);
            if(isSuccess)
                showConfirmSetModeDialog();
       }
       return true;
   }



   @Override
   public void onSwitchChanged(Switch switchView, boolean isChecked) {
       //setHdmiConfig(isChecked);
   }
    private ContentObserver mHdmiTimeoutSettingObserver = new ContentObserver(new Handler()) {
       @Override
       public void onChange(boolean selfChange) {

           ContentResolver resolver = getActivity().getContentResolver();
           final long currentTimeout = Settings.System.getLong(resolver,
                   Settings.System.HDMI_LCD_TIMEOUT, -1);
           long lcdTimeout = -1;
           if ((lcdTimeout = Settings.System.getLong(resolver,
                   Settings.System.HDMI_LCD_TIMEOUT,
                   DEF_HDMI_LCD_TIMEOUT_VALUE)) > 0) {
               lcdTimeout /= 10;
           }
           mHdmiLcd.setValue(String.valueOf(lcdTimeout));
       }
   };
 


    class DisplayListener implements DisplayManager.DisplayListener{
        @Override
        public void onDisplayAdded(int displayId){
            updateHDMIState();
        }

        @Override
        public void onDisplayChanged(int displayId){
            updateHDMIState();
        }

        @Override
        public void onDisplayRemoved(int displayId){
            updateHDMIState();
        }
    }
}
