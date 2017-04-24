package com.udacity.stockhawk.widget;

import android.annotation.TargetApi;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.TaskStackBuilder;
import android.widget.RemoteViews;

import com.udacity.stockhawk.R;
import com.udacity.stockhawk.data.StockProvider;
import com.udacity.stockhawk.ui.MainActivity;
import com.udacity.stockhawk.ui.StockDetailsActivity;

public class StockListWidgetProvider extends AppWidgetProvider {

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // Perform this loop procedure for each App Widget that belongs to this provider
        for (int appWidgetId : appWidgetIds) {
            final RemoteViews views =
                    new RemoteViews(context.getPackageName(), R.layout.widget_stock_list);

            // Create an Intent to launch MainActivity
            final Intent intent = new Intent(context, MainActivity.class);
            final PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);
            views.setOnClickPendingIntent(R.id.widget_stock_list, pendingIntent);

            // Set up the collection
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                setRemoteAdapter(context, views);
            } else {
                setRemoteAdapterV11(context, views);
            }
            final boolean useDetailActivity = context.getResources()
                    .getBoolean(R.bool.use_stock_detail_activity);
            final Intent clickIntentTemplate = useDetailActivity
                    ? new Intent(context, StockDetailsActivity.class)
                    : new Intent(context, MainActivity.class);
            final PendingIntent clickPendingIntentTemplate = TaskStackBuilder.create(context)
                    .addNextIntentWithParentStack(clickIntentTemplate)
                    .getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
            views.setPendingIntentTemplate(R.id.widget_stock_list_list, clickPendingIntentTemplate);
            views.setEmptyView(R.id.widget_stock_list_list, R.id.widget_stock_list_empty);

            // Tell the AppWidgetManager to perform an update on the current app widget
            appWidgetManager.updateAppWidget(appWidgetId, views);
        }
    }

    @Override
    public void onReceive(@NonNull final Context context, @NonNull final Intent intent) {
        super.onReceive(context, intent);
        if (StockProvider.ACTION_DATA_UPDATED.equals(intent.getAction())) {
            final AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
            final int[] appWidgetIds = appWidgetManager.getAppWidgetIds(
                    new ComponentName(context, getClass()));
            appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetIds,
                    R.id.widget_stock_list_list);
        }
    }

    /**
     * Sets the remote adapter used to fill in the list items
     *
     * @param views RemoteViews to set the RemoteAdapter
     */
    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    private void setRemoteAdapter(final Context context, @NonNull final RemoteViews views) {
        views.setRemoteAdapter(R.id.widget_stock_list_list,
                new Intent(context, StockListWidgetRemoteViewsService.class));
    }

    /**
     * Sets the remote adapter used to fill in the list items
     *
     * @param views RemoteViews to set the RemoteAdapter
     */
    @SuppressWarnings("deprecation")
    private void setRemoteAdapterV11(final Context context, @NonNull final RemoteViews views) {
        views.setRemoteAdapter(0, R.id.widget_stock_list_list,
                new Intent(context, StockListWidgetRemoteViewsService.class));
    }
}
