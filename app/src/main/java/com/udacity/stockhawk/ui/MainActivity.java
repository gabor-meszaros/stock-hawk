package com.udacity.stockhawk.ui;

import android.content.AsyncQueryHandler;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.udacity.stockhawk.R;
import com.udacity.stockhawk.data.Contract;
import com.udacity.stockhawk.data.PrefUtils;
import com.udacity.stockhawk.sync.QuoteSyncJob;

import java.util.Set;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>,
        SwipeRefreshLayout.OnRefreshListener,
        StockAdapter.StockAdapterOnClickHandler {

    private static final int STOCK_LOADER = 0;

    @SuppressWarnings("WeakerAccess")
    @BindView(R.id.activity_main_layout)
    LinearLayout mActivityMainLayout;

    @SuppressWarnings("WeakerAccess")
    @BindView(R.id.activity_main_stocks)
    RecyclerView mStockRecyclerView;

    @SuppressWarnings("WeakerAccess")
    @BindView(R.id.activity_main_swipe_refresh)
    SwipeRefreshLayout mSwipeRefreshLayout;

    @SuppressWarnings("WeakerAccess")
    @BindView(R.id.activity_main_error_display)
    TextView mErrorDisplayTextView;

    private StockAdapter mAdapter;

    @Override
    public void onClick(String symbol) {
        final Intent showStockDetailsIntent = new Intent(this, StockDetailsActivity.class);
        showStockDetailsIntent.setData(Contract.Quote.makeUriForStock(symbol));
        startActivity(showStockDetailsIntent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ButterKnife.bind(this);

        mAdapter = new StockAdapter(this, this);
        mStockRecyclerView.setAdapter(mAdapter);
        mStockRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        mSwipeRefreshLayout.setOnRefreshListener(this);
        mSwipeRefreshLayout.setRefreshing(true);
        onRefresh();

        QuoteSyncJob.initialize(this);
        getSupportLoaderManager().initLoader(STOCK_LOADER, null, this);

        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(RecyclerView recyclerView,
                                  RecyclerView.ViewHolder viewHolder,
                                  RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(final RecyclerView.ViewHolder viewHolder, int direction) {
                final String symbol = mAdapter.getSymbolAtPosition(viewHolder.getAdapterPosition());
                final AsyncQueryHandler asyncDeleteHandler =
                        new AsyncQueryHandler(getContentResolver()) {
                            @Override
                            protected void onDeleteComplete(int token, Object cookie, int result) {
                                PrefUtils.removeStock(MainActivity.this, symbol);
                                final Set<String> stocks = PrefUtils.getStocks(MainActivity.this);
                                if (stocks.isEmpty()) {
                                    onRefresh();
                                }
                            }
                        };
                final int anyId = 42; // We will not use it in the result handler function
                asyncDeleteHandler.startDelete(anyId, null, Contract.Quote.makeUriForStock(symbol),
                        null, null);
            }
        }).attachToRecyclerView(mStockRecyclerView);


    }

    private boolean networkUp() {
        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnectedOrConnecting();
    }

    @Override
    public void onRefresh() {

        QuoteSyncJob.syncImmediately(this);

        if (!networkUp() && mAdapter.getItemCount() == 0) {
            mSwipeRefreshLayout.setRefreshing(false);
            mErrorDisplayTextView.setText(getString(R.string.activity_main_error_no_network));
            mErrorDisplayTextView.setVisibility(View.VISIBLE);
        } else if (!networkUp()) {
            mSwipeRefreshLayout.setRefreshing(false);
            if (PrefUtils.areStockValuesExpired(this)) {
                Snackbar.make(mActivityMainLayout, R.string.error_stocks_are_out_of_date, Snackbar.LENGTH_LONG).show();
            } else {
                Snackbar.make(mActivityMainLayout, R.string.activity_main_toast_no_connectivity, Snackbar.LENGTH_LONG).show();
            }
        } else if (PrefUtils.getStocks(this).size() == 0) {
            mSwipeRefreshLayout.setRefreshing(false);
            mErrorDisplayTextView.setText(getString(R.string.activity_main_error_no_stocks));
            mErrorDisplayTextView.setVisibility(View.VISIBLE);
        } else {
            mErrorDisplayTextView.setVisibility(View.GONE);
        }
    }

    public void addStockButtonOnClick(@SuppressWarnings("UnusedParameters") View view) {
        new AddStockDialog().show(getFragmentManager(), "StockDialogFragment");
    }

    void addStock(String symbol) {
        if (symbol != null && !symbol.isEmpty()) {

            if (networkUp()) {
                mSwipeRefreshLayout.setRefreshing(true);
            } else {
                String message = getString(R.string.activity_main_toast_stock_added_no_connectivity, symbol);
                Snackbar.make(mActivityMainLayout, message, Snackbar.LENGTH_LONG).show();
            }

            PrefUtils.addStock(this, symbol);
            QuoteSyncJob.syncImmediately(this);
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(this,
                Contract.Quote.URI,
                Contract.Quote.QUOTE_COLUMNS.toArray(new String[]{}),
                null, null, Contract.Quote.COLUMN_SYMBOL);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mSwipeRefreshLayout.setRefreshing(false);

        if (data.getCount() != 0) {
            if (PrefUtils.areStockValuesExpired(this)) {
                Snackbar.make(mActivityMainLayout, R.string.error_stocks_are_out_of_date, Snackbar.LENGTH_LONG).show();
            }
            mErrorDisplayTextView.setVisibility(View.GONE);
        }

        mAdapter.setCursor(data);
    }


    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mSwipeRefreshLayout.setRefreshing(false);
        mAdapter.setCursor(null);
    }


    private void setDisplayModeMenuItemIcon(MenuItem item) {
        if (PrefUtils.getDisplayMode(this)
                .equals(getString(R.string.pref_display_mode_absolute_key))) {
            item.setIcon(R.drawable.ic_percentage);
            item.setTitle(getString(R.string.activity_main_menu_switch_percentage_description));
        } else {
            item.setIcon(R.drawable.ic_dollar);
            item.setTitle(getString(R.string.activity_main_menu_switch_dollar_description));
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_activity_settings, menu);
        MenuItem item = menu.findItem(R.id.action_change_units);
        setDisplayModeMenuItemIcon(item);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_change_units) {
            PrefUtils.toggleDisplayMode(this);
            setDisplayModeMenuItemIcon(item);
            mAdapter.notifyDataSetChanged();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
