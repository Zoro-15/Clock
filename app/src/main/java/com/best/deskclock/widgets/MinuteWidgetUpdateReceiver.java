// SPDX-License-Identifier: GPL-3.0-only

package com.best.deskclock.widgets;

import static androidx.preference.PreferenceManager.getDefaultSharedPreferences;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;

import com.best.deskclock.data.WidgetDAO;
import com.best.deskclock.utils.LogUtils;
import com.best.deskclock.utils.ThemeUtils;
import com.best.deskclock.utils.WidgetUtils;

/**
 * BroadcastReceiver responsible for updating the digital clock widget every minute
 * when a custom font is active.
 */
public class MinuteWidgetUpdateReceiver extends BroadcastReceiver {

    private static final LogUtils.Logger LOGGER = new LogUtils.Logger("MinuteWidgetUpdateReceiver");

    @Override
    public void onReceive(@NonNull Context context, @NonNull Intent intent) {
        LOGGER.i("onReceive: " + intent.getAction());

        Context appContext = context.getApplicationContext();
        SharedPreferences prefs = getDefaultSharedPreferences(appContext);
        String fontPath = WidgetDAO.getDigitalWidgetFont(prefs);
        boolean hasCustomFont = fontPath != null && ThemeUtils.loadFont(fontPath) != null;

        if (hasCustomFont) {
            DigitalAppWidgetProvider.registerReceivers(appContext, new DigitalAppWidgetProvider());
            DigitalAppWidgetProvider.updateClockOnly(appContext);
            DigitalAppWidgetProvider.startTickerIfNeeded(appContext);
            WidgetUtils.scheduleMinuteWidgetUpdate(appContext);
        } else {
            DigitalAppWidgetProvider.stopTicker();
            WidgetUtils.cancelMinuteWidgetUpdate(appContext);
        }
    }
}
