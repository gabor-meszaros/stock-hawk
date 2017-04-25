package com.udacity.stockhawk.widget;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Binder;
import android.widget.AdapterView;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.udacity.stockhawk.R;
import com.udacity.stockhawk.data.Contract;
import com.udacity.stockhawk.util.UiUtils;

public class StockListWidgetRemoteViewsService extends RemoteViewsService {

    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new RemoteViewsFactory() {
            private Cursor mData = null;

            @Override
            public void onCreate() {
                // Nothing to do
            }

            @Override
            public void onDataSetChanged() {
                if (mData != null) {
                    mData.close();
                }
                // This method is called by the app hosting the widget (e.g., the launcher)
                // However, our ContentProvider is not exported so it doesn't have access to the
                // mData. Therefore we need to clear (and finally restore) the calling identity so
                // that calls use our process and permission
                final long identityToken = Binder.clearCallingIdentity();
                final Uri stocksUri = Contract.Quote.URI;
                mData = getContentResolver().query(stocksUri,
                        null,
                        Contract.Quote.COLUMN_HISTORY + " IS NOT NULL",
                        null,
                        Contract.Quote.COLUMN_SYMBOL);
                Binder.restoreCallingIdentity(identityToken);
            }

            @Override
            public void onDestroy() {
                if (mData != null) {
                    mData.close();
                    mData = null;
                }
            }

            @Override
            public int getCount() {
                return mData == null ? 0 : mData.getCount();
            }

            @Override
            public RemoteViews getViewAt(int position) {
                if (position == AdapterView.INVALID_POSITION ||
                        mData == null || !mData.moveToPosition(position)) {
                    return null;
                }
                RemoteViews views = new RemoteViews(getPackageName(),
                        R.layout.list_item_widget_stock_list);

                final String symbol =
                        mData.getString(mData.getColumnIndex(Contract.Quote.COLUMN_SYMBOL));
                views.setTextViewText(R.id.widget_stock_list_item_symbol, symbol);

                final float price =
                        mData.getFloat(mData.getColumnIndex(Contract.Quote.COLUMN_PRICE));
                views.setTextViewText(R.id.widget_stock_list_item_price,
                        UiUtils.getDollar(price, false));

                final float rawAbsoluteChange =
                        mData.getFloat(mData.getColumnIndex(Contract.Quote.COLUMN_ABSOLUTE_CHANGE));
                final float percentageChange =
                        mData.getFloat(mData.getColumnIndex(Contract.Quote.COLUMN_PERCENTAGE_CHANGE));
                final Context context = getApplicationContext();
                views.setTextViewText(R.id.widget_stock_list_item_change,
                        UiUtils.getChange(context, rawAbsoluteChange, percentageChange / 100));

                if (rawAbsoluteChange > 0) {
                    views.setInt(R.id.widget_stock_list_item_change, "setBackgroundResource",
                            R.drawable.percent_change_pill_green);
                } else {
                    views.setInt(R.id.widget_stock_list_item_change, "setBackgroundResource",
                            R.drawable.percent_change_pill_red);
                }

                final Intent fillInIntent = new Intent();
                final Uri stockUri = Contract.Quote.makeUriForStock(symbol);
                fillInIntent.setData(stockUri);
                views.setOnClickFillInIntent(R.id.widget_stock_list_item, fillInIntent);

                return views;
            }

            @Override
            public RemoteViews getLoadingView() {
                return new RemoteViews(getPackageName(), R.layout.list_item_widget_stock_list);
            }

            @Override
            public int getViewTypeCount() {
                return 1;
            }

            @Override
            public long getItemId(int position) {
                if (mData.moveToPosition(position))
                    return mData.getLong(mData.getColumnIndex(Contract.Quote._ID));
                return position;
            }

            @Override
            public boolean hasStableIds() {
                return true;
            }
        };
    }
}
