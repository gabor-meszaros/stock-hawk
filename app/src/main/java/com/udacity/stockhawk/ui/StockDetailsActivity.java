package com.udacity.stockhawk.ui;

import android.content.AsyncQueryHandler;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.udacity.stockhawk.R;
import com.udacity.stockhawk.data.Contract;
import com.udacity.stockhawk.util.UiUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;

public class StockDetailsActivity extends AppCompatActivity {

    private static final String END_LINE_SEPARATOR = "\\r?\\n";
    private static final String HISTORY_POINT_VALUE_SEPARATOR = ", ";
    private static final SimpleDateFormat X_AXIS_DATE_LABEL_FORMAT =
            new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);

    @BindView(R.id.activity_stock_details_current_stock_info)
    View mCurrentStockInfo;

    @BindView(R.id.activity_stock_details_symbol)
    TextView mSymbol;

    @BindView(R.id.activity_stock_details_price)
    TextView mPrice;

    @BindView(R.id.activity_stock_details_change)
    TextView mChange;

    @BindView(R.id.activity_stock_details_history)
    LineChart mHistory;

    @BindView(R.id.activity_stock_details_progress_bar)
    ProgressBar mProgressBar;

    @BindView(R.id.activity_stock_details_error_display)
    TextView mErrorDisplay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stock_details);

        ButterKnife.bind(this);

        showProgressBar();

        final Intent intent = getIntent();
        if (null != intent && null != intent.getData()) {
            final Uri stockUri = intent.getData();
            final AsyncQueryHandler asyncQueryHandler =
                    new AsyncQueryHandler(getContentResolver()) {
                        @Override
                        protected void onQueryComplete(int token, Object cookie, Cursor cursor) {
                            if (null != cursor && cursor.moveToFirst()) {
                                setSymbol(cursor);
                                setPrice(cursor);
                                setPriceChange(cursor);
                                setHistory(cursor);

                                showStockData();
                            } else {
                                showErrorMessage();
                            }
                        }
                    };
            final int anyId = 42; // We will not use it in the result handler function
            asyncQueryHandler.startQuery(anyId, null, stockUri, null, null, null, null);
        }
    }

    private void setSymbol(@NonNull final Cursor cursor) {
        final int symbolColumnIndex = cursor.getColumnIndex(Contract.Quote.COLUMN_SYMBOL);
        final String symbol = cursor.getString(symbolColumnIndex);
        mSymbol.setText(symbol);
    }

    private void setPrice(@NonNull final Cursor cursor) {
        final int priceColumnIndex = cursor.getColumnIndex(Contract.Quote.COLUMN_PRICE);
        final float price = cursor.getFloat(priceColumnIndex);
        mPrice.setText(UiUtils.getDollar(price, false));
    }

    private void setPriceChange(@NonNull final Cursor cursor) {
        final int absolutePriceChangeColumnIndex =
                cursor.getColumnIndex(Contract.Quote.COLUMN_ABSOLUTE_CHANGE);
        final float rawAbsoluteChange = cursor.getFloat(absolutePriceChangeColumnIndex);

        final int percentagePriceChangeColumnIndex =
                cursor.getColumnIndex(Contract.Quote.COLUMN_PERCENTAGE_CHANGE);
        final float percentageChange = cursor.getFloat(percentagePriceChangeColumnIndex);

        mChange.setText(UiUtils.getChange(rawAbsoluteChange, percentageChange / 100));

        setPriceChangeBackgroundColor(rawAbsoluteChange);
    }

    private void setPriceChangeBackgroundColor(final float priceChange) {
        if (priceChange > 0) {
            mChange.setBackgroundResource(R.drawable.percent_change_pill_green);
        } else {
            mChange.setBackgroundResource(R.drawable.percent_change_pill_red);
        }
    }

    private void setHistory(@NonNull final Cursor cursor) {
        final List<String> historyPointStrings = getHistoryPointStrings(cursor);

        // We need chronological order, but we store them in a reversed order in the DB.
        Collections.reverse(historyPointStrings);

        final List<Entry> yAxisIdPriceEntries = getYAxisIdPriceEntries(historyPointStrings);
        final LineData lineData = getLineData(yAxisIdPriceEntries);
        final List<String> xAxisDateValues = getXAxisDateValues(historyPointStrings);

        setHistoryData(lineData, xAxisDateValues);

        setHistoryLookAndFeel();

        redrawnHistory();
    }

    private List<String> getHistoryPointStrings(@NonNull final Cursor cursor) {
        final int historyColumnIndex = cursor.getColumnIndex(Contract.Quote.COLUMN_HISTORY);
        final String history = cursor.getString(historyColumnIndex);
        return Arrays.asList(history.split(END_LINE_SEPARATOR));
    }

    private List<Entry> getYAxisIdPriceEntries(@NonNull final List<String> historyPointStrings) {
        int xAxisPosition = 0;
        final List<Entry> entries = new ArrayList<>();
        for (final String historyPointString : historyPointStrings) {
            final float stockPrice = getStockPrice(historyPointString);
            entries.add(new Entry(xAxisPosition, stockPrice));
            xAxisPosition++;
        }
        return entries;
    }

    private float getStockPrice(@NonNull final String historyPointString) {
        final String[] datePricePair = getPriceDatePair(historyPointString);
        final int priceIndex = 1;
        return Float.parseFloat(datePricePair[priceIndex]);
    }

    private String[] getPriceDatePair(@NonNull final String historyPointString) {
        return historyPointString.split(HISTORY_POINT_VALUE_SEPARATOR);
    }

    private List<String> getXAxisDateValues(@NonNull final List<String> historyPointStrings) {
        final List<String> xAxisDateValues = new ArrayList<>();
        for (final String historyPointString : historyPointStrings) {
            final long stockDateInMilliseconds = getStockDate(historyPointString);
            xAxisDateValues.add(getDateString(stockDateInMilliseconds));
        }
        return xAxisDateValues;
    }

    private long getStockDate(@NonNull final String historyPointString) {
        final String[] datePricePair = getPriceDatePair(historyPointString);
        final int dateIndex = 0;
        return Long.parseLong(datePricePair[dateIndex]);
    }

    private String getDateString(final long dateInMilliseconds) {
        final Date date = new Date(dateInMilliseconds);
        return X_AXIS_DATE_LABEL_FORMAT.format(date);
    }

    private LineData getLineData(@NonNull final List<Entry> dataEntries) {
        final String descriptionIsNotNeeded = null;
        final LineDataSet dataSet = new LineDataSet(dataEntries, descriptionIsNotNeeded);
        dataSet.setColor(UiUtils.getColor(this, R.color.colorPrimary));
        return new LineData(dataSet);
    }

    private void setHistoryData(@NonNull final LineData lineData,
                                @NonNull final List<String> xAxisValues) {
        mHistory.setData(lineData);
        mHistory.invalidate();
        final XAxis xAxis = mHistory.getXAxis();
        xAxis.setValueFormatter(new IAxisValueFormatter() {
            @Override
            public String getFormattedValue(float value, AxisBase axis) {
                return xAxisValues.get((int) value);
            }
        });
    }

    private void setHistoryLookAndFeel() {
        // The labels are overlapping otherwise
        final XAxis xAxis = mHistory.getXAxis();
        xAxis.setLabelRotationAngle(-45);

        // Good for financial data
        // See: https://github.com/PhilJay/MPAndroidChart/wiki/Specific-Chart-Settings-&-Styling
        mHistory.setAutoScaleMinMaxEnabled(true);

        // Other views on the screen has already told the content of the legend
        mHistory.getLegend().setEnabled(false);

        // Show the source of the data
        final String symbol = mSymbol.getText().toString();
        mHistory.getDescription().setText(symbol + "@YahooFinance");

        // Borders
        mHistory.setDrawBorders(true);
        mHistory.setBorderColor(UiUtils.getColor(this, R.color.white));
        mHistory.setBorderWidth(2);

        // Highlight this part of the activity as this is the most important part of stock details
        mHistory.setDrawGridBackground(true);
        mHistory.setDrawingCacheBackgroundColor(UiUtils.getColor(this, R.color.white));

        // Make the text visible on dark background
        xAxis.setTextColor(UiUtils.getColor(this, R.color.white));
        final YAxis axisLeft = mHistory.getAxisLeft();
        axisLeft.setTextColor(UiUtils.getColor(this, R.color.white));
        final YAxis axisRight = mHistory.getAxisRight();
        axisRight.setTextColor(UiUtils.getColor(this, R.color.white));
    }

    private void showStockData() {
        mCurrentStockInfo.setVisibility(View.VISIBLE);
        mHistory.setVisibility(View.VISIBLE);
        mProgressBar.setVisibility(View.GONE);
        mErrorDisplay.setVisibility(View.GONE);
    }

    private void showProgressBar() {
        mCurrentStockInfo.setVisibility(View.GONE);
        mHistory.setVisibility(View.GONE);
        mProgressBar.setVisibility(View.VISIBLE);
        mErrorDisplay.setVisibility(View.GONE);
    }

    private void showErrorMessage() {
        mCurrentStockInfo.setVisibility(View.GONE);
        mHistory.setVisibility(View.GONE);
        mProgressBar.setVisibility(View.GONE);
        mErrorDisplay.setVisibility(View.VISIBLE);
    }

    private void redrawnHistory() {
        mHistory.invalidate();
    }
}
