package com.example.chipiquizfinal.helper;

import android.content.Context;
import android.content.res.Configuration;

import java.util.Locale;

public class LocaleHelper {
    public static Context wrap(Context context, String lang) {
        Locale locale = new Locale(lang);
        Locale.setDefault(locale);
        Configuration config = context.getResources().getConfiguration();
        config.setLocale(locale);
        return context.createConfigurationContext(config);
    }
}
