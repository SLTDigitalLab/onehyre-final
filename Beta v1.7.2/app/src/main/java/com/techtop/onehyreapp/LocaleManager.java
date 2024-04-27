package com.techtop.onehyreapp;

import android.content.Context;
import android.content.res.Configuration;

import java.util.Locale;

public class LocaleManager {

    public static void setLocale(Context context, String language) {

        Locale locale = new Locale(language);
        Locale.setDefault(locale);

        Configuration config = new Configuration();
        config.setLocale(locale);

        context.getResources().updateConfiguration(config, context.getResources().getDisplayMetrics());
    }
}