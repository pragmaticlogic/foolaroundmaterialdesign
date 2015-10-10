package com.codeprototype.kevin.foolaroundmaterialdesign.activity;

import android.content.Intent;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;

import com.afollestad.materialdialogs.MaterialDialog;
import com.codeprototype.kevin.foolaroundmaterialdesign.R;
import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.SignUpCallback;

public class SignupActivity extends AppCompatActivity {

    protected Button _loginButton;
    protected Button _signupButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        final TextInputLayout usernameWrapper = (TextInputLayout) findViewById(R.id.usernameWrapper);
        final TextInputLayout passwordWrapper = (TextInputLayout) findViewById(R.id.passwordWrapper);
        final TextInputLayout emailWrapper = (TextInputLayout) findViewById(R.id.emailWrapper);

        Toolbar toolbar = (Toolbar) findViewById(R.id.my_toolbar);
        if(toolbar != null) {
            setSupportActionBar(toolbar);
            getSupportActionBar().setTitle(R.string.welcome_label);
            getSupportActionBar().setHomeButtonEnabled(true);
            //getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        _signupButton = (Button) findViewById(R.id.signupButton);
        _signupButton.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                String userName = usernameWrapper.getEditText().getText().toString().trim();
                String password = passwordWrapper.getEditText().getText().toString().trim();
                String email = emailWrapper.getEditText().getText().toString().trim();

                if (userName.isEmpty() || password.isEmpty() || email.isEmpty()) {
                    new MaterialDialog.Builder(SignupActivity.this)
                            .title(R.string.signup_error_title)
                            .content(R.string.signup_error_message)
                            .positiveText(android.R.string.ok)
                            .negativeText(android.R.string.cancel)
                            .show();
                } else {
                    ParseUser user = new ParseUser();
                    user.setUsername(userName);
                    user.setPassword(password);
                    user.setEmail(email);

                    setProgressBarIndeterminateVisibility(true);
                    user.signUpInBackground(new SignUpCallback() {
                        @Override
                        public void done(ParseException e) {
                            setProgressBarIndeterminateVisibility(false);
                            if (e == null) {
                                Intent intent = new Intent(SignupActivity.this, MainActivity.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(intent);
                            } else {
                                new MaterialDialog.Builder(SignupActivity.this)
                                        .title(R.string.signup_error_title)
                                        .content(e.getMessage())
                                        .positiveText(android.R.string.ok)
                                        .negativeText(android.R.string.cancel)
                                        .show();
                            }
                        }
                    });
                }
            }
        });

        _loginButton = (Button) findViewById(R.id.loginButton);
        _loginButton.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SignupActivity.this, LoginActivity.class);
                startActivity(intent);
            }
        });
    }
}
