package com.techtop.onehyreapp;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.text.Html;
import android.widget.TextView;

public class SettingsTermsAndConditions extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings_terms_and_conditions);
        getSupportActionBar().setTitle("Terms And Conditions");

        @SuppressLint({"MissingInflatedId", "LocalSuppress"}) TextView textView = findViewById(R.id.textTermsAndConditions);

        String htmlContent = getString(R.string.terms_and_conditions_agreement);

        CharSequence spannedText = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            spannedText = Html.fromHtml(htmlContent, Html.FROM_HTML_MODE_LEGACY);
        }
        textView.setText(spannedText);
    }
}