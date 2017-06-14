/*
 * Copyright (C) 2010 The Android Open Source Project
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
package com.android.settings;

import java.net.Inet4Address;
import java.net.InetAddress;

import android.net.NetworkUtils;

import com.android.settings.R;

import java.util.regex.Pattern;

import android.content.Context;
import android.preference.EditTextPreference;
import android.provider.Settings;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.SystemProperties;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Button;
import android.net.EthernetManager;

class pppoe_dialog extends AlertDialog implements TextWatcher {
	
    public getStaticIpInfo mGetStaticInfo;
	private TextView mPppoeUsernameView;
	private TextView mPppoePasswordView;

	public EditText pppoe_username;
	public EditText pppoe_password;

	static final int BUTTON_SUBMIT = DialogInterface.BUTTON_POSITIVE;
    static final int BUTTON_FORGET = DialogInterface.BUTTON_NEUTRAL;

	// private final boolean mEdit;
	private final DialogInterface.OnClickListener mListener;

	private View mView;
	Context mcontext;
    EthernetManager mEthManager;
	// private boolean mHideSubmitButton;

	public pppoe_dialog(Context context, boolean cancelable,
            DialogInterface.OnClickListener listener, getStaticIpInfo GetgetStaticIpInfo) {
		super(context);
		mcontext = context;
		mListener = listener;
        mGetStaticInfo=GetgetStaticIpInfo;
		// TODO Auto-generated constructor stub
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		mView = getLayoutInflater().inflate(R.layout.pppoe_dialog, null);
		setView(mView);
		setInverseBackgroundForced(true);

		mPppoeUsernameView = (TextView) mView.findViewById(R.id.username);
		mPppoePasswordView = (TextView) mView.findViewById(R.id.password);

		mPppoeUsernameView.addTextChangedListener(this);
		mPppoePasswordView.addTextChangedListener(this);

		setButton(BUTTON_NEGATIVE,mcontext.getString(R.string.pppoe_cancel), mListener);
		setButton(BUTTON_SUBMIT, mcontext.getString(R.string.pppoe_connect), mListener);

		setTitle(mcontext.getString(R.string.pppoe_settings));
        
        mEthManager = (EthernetManager) mcontext.getSystemService(Context.ETHERNET_SERVICE);

		super.onCreate(savedInstanceState);
	}

	@Override
	public void onStart() {
		super.onStart();
		updatePppoeInfo();
	}
    
	private void updatePppoeInfo() {
		Log.d("fzk", "updatePppoeInfo");
        
        String uname = Settings.Secure.getString(mcontext.getContentResolver(),
				Settings.Secure.PPPOE_USERNAME);
		String pwd = Settings.Secure.getString(mcontext.getContentResolver(),
				Settings.Secure.PPPOE_PSWD);
                
		if (!TextUtils.isEmpty(uname))
			mPppoeUsernameView.setText(uname);
        
		if (!TextUtils.isEmpty(pwd))
			mPppoePasswordView.setText(pwd);
	}

	public void savePppoeInfo() {
        
        String mPppoeuname = mPppoeUsernameView.getText().toString();
		String mPppoepwd = mPppoePasswordView.getText().toString();
        
        Settings.Secure.putString(mcontext.getContentResolver(),
				Settings.Secure.PPPOE_USERNAME, mPppoeuname);
		Settings.Secure.putString(mcontext.getContentResolver(),
				Settings.Secure.PPPOE_PSWD, mPppoepwd);
                
		mGetStaticInfo.getPppoeUsername(mPppoeuname);
		mGetStaticInfo.getPppoePassword(mPppoepwd);
	}
    
	@Override
	public void afterTextChanged(Editable s) {
	//	checkIPValue();
	//	Log.d("blb", "afterTextChanged");
	}
	@Override
	public void beforeTextChanged(CharSequence s, int start, int count,
			int after) {
		// work done in afterTextChanged
	}

	@Override
	public void onTextChanged(CharSequence s, int start, int before, int count) {
		// work done in afterTextChanged
	}

}
