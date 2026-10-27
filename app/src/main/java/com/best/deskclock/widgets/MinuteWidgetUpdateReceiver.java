// SPDX-License-Identifier: GPL-3.0-only

package com.best.deskclock.widgets;

import static com.best.deskclock.DeskClockApplication.getDefaultSharedPreferences;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;

import com.best.deskclock.data.WidgetDAO;
import com.best.deskclock.base.AppExecutors;
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

        final Context appContext = context.getApplicationContext();
        final SharedPreferences prefs = getDefaultSharedPreferences(appContext);
        final String fontPath = WidgetDAO.getDigitalWidgetFont(prefs);
        final boolean hasCustomFont = fontPath != null && ThemeUtils.loadFont(fontPath) != null;

        if (hasCustomFont) {
            DigitalAppWidgetProvider.registerDynamicReceiver(appContext);
            final PendingResult asyncResult = goAsync();
            AppExecutors.getDiskIO().execute(() -> {
                try {
                    WidgetUtils.updateWidget(appContext, DigitalAppWidgetProvider.class);
                    WidgetUtils.scheduleMinuteWidgetUpdate(appContext);
                } finally {
                    asyncResult.finish();
                }
            });
        } else {
            DigitalAppWidgetProvider.stopTicker();
            DigitalAppWidgetProvider.unregisterDynamicReceiver(appContext);
            WidgetUtils.cancelMinuteWidgetUpdate(appContext);
        }
    }
}
