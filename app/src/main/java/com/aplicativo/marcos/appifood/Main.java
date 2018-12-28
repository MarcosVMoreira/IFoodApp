package com.aplicativo.marcos.appifood;

import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;

public class Main extends AppCompatActivity {

    LoginButton loginButton;
    TextView textView;
    CallbackManager callbackManager;
    private Button button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(getApplicationContext());

        setContentView(R.layout.activity_main);
        loginButton = (LoginButton) findViewById(R.id.fb_login_bn);
        textView = (TextView) findViewById(R.id.fb_login_bn);
        callbackManager = CallbackManager.Factory.create();
        loginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                textView.setText("Login efetuado com sucesso.\n"+loginResult.getAccessToken().getUserId()+"\n"
                        +loginResult.getAccessToken().getToken());
            }

            @Override
            public void onCancel() {
                textView.setText("Login cancelado.");
            }

            @Override
            public void onError(FacebookException error) {

            }
        });

        button = (Button) findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openActivity2();
            }


        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }

    private void openActivity2() {
        Intent intent = new Intent(this, SecondActivity.class);
        startActivity(intent);
    }
}
