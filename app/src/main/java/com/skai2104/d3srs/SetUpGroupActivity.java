package com.skai2104.d3srs;

import android.content.Intent;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;

public class SetUpGroupActivity extends AppCompatActivity {
    private Toolbar mToolbar;
    private Intent mIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_up_group);

        mToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Set Up Your Group");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        findViewById(R.id.fromExistingUserBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mIntent = new Intent(SetUpGroupActivity.this, AddFromExistingUsersActivity.class);
                startActivity(mIntent);
            }
        });

        findViewById(R.id.byPhoneNumberBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mIntent = new Intent(SetUpGroupActivity.this, AddByPhoneNumbersActivity.class);
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
