package com.xabber.android.ui.fragment;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.xabber.android.R;
import com.xabber.android.data.Application;
import com.xabber.android.data.NetworkException;
import com.xabber.android.data.account.AccountManager;
import com.xabber.android.data.entity.AccountJid;
import com.xabber.android.data.xaccount.XabberAccountManager;
import com.xabber.android.ui.activity.AccountActivity;
import com.xabber.android.ui.dialog.OrbotInstallerDialog;
import com.xabber.android.ui.helper.OrbotHelper;

public class XAccountLoginFragment extends Fragment implements View.OnClickListener {

    private CheckBox storePasswordView;
    private CheckBox chkSync;
    private CheckBox chkRequireTLS;
    private CheckBox chkUseTOR;
    private EditText edtUsername;
    private EditText edtPassword;
    private Button btnLogin;
    private Button btnOptions;
    private View optionsView;
    private View socialView;

    private Listener listener;

    public interface Listener {
        void onGoogleClick();
        void onFacebookClick();
        void onTwitterClick();
    }

    public static XAccountLoginFragment newInstance(Listener listener) {
        XAccountLoginFragment fragment = new XAccountLoginFragment();
        fragment.listener = listener;
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_xaccount_login, container, false);

        storePasswordView = (CheckBox) view.findViewById(R.id.store_password);
        chkSync = (CheckBox) view.findViewById(R.id.chkSync);
        if (XabberAccountManager.getInstance().getAccount() == null) {
            chkSync.setVisibility(View.GONE);
            chkSync.setChecked(false);
        }

        chkRequireTLS = view.findViewById(R.id.chkRequireTLS);
        chkUseTOR = view.findViewById(R.id.chkUseTOR);

        optionsView = view.findViewById(R.id.optionsView);
        socialView = view.findViewById(R.id.socialView);
        edtUsername = (EditText) view.findViewById(R.id.edtUsername);
        edtPassword = (EditText) view.findViewById(R.id.edtPass);
        btnLogin = view.findViewById(R.id.btnLogin);
        btnLogin.setOnClickListener(this);
        btnOptions = view.findViewById(R.id.btnOptions);
        btnOptions.setOnClickListener(this);

        ((TextView) view.findViewById(R.id.account_help))
                .setMovementMethod(LinkMovementMethod.getInstance());

        ImageView ivFacebook = view.findViewById(R.id.ivFacebook);
        ImageView ivGoogle = view.findViewById(R.id.ivGoogle);
        ImageView ivTwitter = view.findViewById(R.id.ivTwitter);

        ivFacebook.setOnClickListener(this);
        ivGoogle.setOnClickListener(this);
        ivTwitter.setOnClickListener(this);

        return view;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnLogin:
                addAccount();
                break;
            case R.id.ivFacebook:
                listener.onFacebookClick();
                break;
            case R.id.ivGoogle:
                listener.onGoogleClick();
                break;
            case R.id.ivTwitter:
                listener.onTwitterClick();
                break;
            case R.id.btnOptions:
                showOptions();
                break;
        }
    }

    private void addAccount() {
        if (chkUseTOR.isChecked() && !OrbotHelper.isOrbotInstalled()) {
            OrbotInstallerDialog.newInstance().show(getFragmentManager(), OrbotInstallerDialog.class.getName());
            return;
        }

        AccountJid account;
        try {
            account = AccountManager.getInstance().addAccount(
                    edtUsername.getText().toString().trim(),
                    edtPassword.getText().toString(),
                    "",
                    false,
                    storePasswordView.isChecked(),
                    chkSync.isChecked(),
                    chkUseTOR.isChecked(),
                    false, true,
                    chkRequireTLS.isChecked());
        } catch (NetworkException e) {
            Application.getInstance().onError(e);
            return;
        }

        // update remote settings
        if (chkSync.isChecked()) XabberAccountManager.getInstance().updateSettingsWithSaveLastAccount(account);

        startActivity(AccountActivity.createIntent(getActivity(), account));
        getActivity().finish();
    }

    private void showOptions() {
        if (optionsView.getVisibility() == View.VISIBLE) {
            optionsView.setVisibility(View.GONE);
            socialView.setVisibility(View.VISIBLE);
            btnOptions.setText(R.string.button_advanced_options);
        } else {
            optionsView.setVisibility(View.VISIBLE);
            socialView.setVisibility(View.GONE);
            btnOptions.setText(R.string.button_hide_options);
        }
    }
}