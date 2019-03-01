package com.skai2104.d3srs;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;

public class AccountSettingsActivity extends AppCompatActivity {
    private Toolbar mToolbar;

    private Intent mIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_settings);

        mToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Account Settings");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        findViewById(R.id.editNameBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mIntent = new Intent(AccountSettingsActivity.this, EditNameActivity.class);
                startActivity(mIntent);
            }
        });

        findViewById(R.id.changeEmailBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mIntent = new Intent(AccountSettingsActivity.this, ChangeEmailActivity.class);
                startActivity(mIntent);
            }
        });

        findViewById(R.id.changePasswordBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mIntent = new Intent(AccountSettingsActivity.this, ChangePasswordActivity.class);
                startActivity(mIntent);
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
