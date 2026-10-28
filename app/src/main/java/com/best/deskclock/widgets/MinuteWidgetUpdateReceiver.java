// SPDX-License-Identifier: GPL-3.0-only

package com.best.deskclock.widgets;

import static com.best.deskclock.DeskClockApplication.getDefaultSharedPreferences;

import android.appwidget.AppWidgetManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.PowerManager;

import androidx.annotation.NonNull;

import com.best.deskclock.base.AppExecutors;
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

        final Context appContext = context.getApplicationContext();
        final SharedPreferences prefs = getDefaultSharedPreferences(appContext);
        final String fontPath = WidgetDAO.getDigitalWidgetFont(prefs);
        final boolean hasCustomFont = fontPath != null && ThemeUtils.loadFont(fontPath) != null;

        final AppWidgetManager wm = AppWidgetManager.getInstance(appContext);
        final ComponentName provider = new ComponentName(appContext, DigitalAppWidgetProvider.class);
        final int[] widgetIds = wm != null ? wm.getAppWidgetIds(provider) : null;
        final boolean hasWidgets = widgetIds != null && widgetIds.length > 0;

        final PowerManager pm = (PowerManager) appContext.getSystemService(Context.POWER_SERVICE);
        final boolean isInteractive = pm == null || pm.isInteractive();

        if (hasCustomFont && hasWidgets && isInteractive) {
            DigitalAppWidgetProvider.registerDynamicReceiver(appContext);
            final PendingResult asyncResult = goAsync();
            AppExecutors.getDiskIO().execute(() -> {
                try {
                    WidgetUtils.updateWidgetSync(appContext, DigitalAppWidgetProvider.class);
                    WidgetUtils.scheduleMinuteWidgetUpdate(appContext);
                } finally {
                    asyncResult.finish();
                }
            });
        } else {
            DigitalAppWidgetProvider.unregisterDynamicReceiver(appContext);
            WidgetUtils.cancelMinuteWidgetUpdate(appContext);
        }
    }
}
