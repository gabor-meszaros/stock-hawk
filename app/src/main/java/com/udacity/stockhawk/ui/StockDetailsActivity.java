package com.udacity.stockhawk.ui;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.XAxis;
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

    @BindView(R.id.stock_details_symbol)
    TextView mSymbol;

    @BindView(R.id.stock_details_price)
    TextView mPrice;

    @BindView(R.id.stock_details_change)
    TextView mChange;

    @BindView(R.id.stock_details_history)
    LineChart mHistory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stock_details);
        ButterKnife.bind(this);

        final Intent intent = getIntent();
        if (null != intent && null != intent.getData()) {
            final Uri stockUri = intent.getData();
            final Cursor cursor = getContentResolver().query(stockUri, null, null, null, null);
            if (null != cursor && cursor.moveToFirst()) {
                final int symbolColumnIndex = cursor.getColumnIndex(Contract.Quote.COLUMN_SYMBOL);
                mSymbol.setText(cursor.getString(symbolColumnIndex));

                final float price =
                        cursor.getFloat(cursor.getColumnIndex(Contract.Quote.COLUMN_PRICE));
                mPrice.setText(UiUtils.getDollar(price, false));

                final float rawAbsoluteChange =
                        cursor.getFloat(cursor.getColumnIndex(Contract.Quote.COLUMN_ABSOLUTE_CHANGE));
                final float percentageChange =
                        cursor.getFloat(cursor.getColumnIndex(Contract.Quote.COLUMN_PERCENTAGE_CHANGE));
                mChange.setText(UiUtils.getChange(rawAbsoluteChange, percentageChange / 100));

                if (rawAbsoluteChange > 0) {
                    mChange.setBackgroundResource(R.drawable.percent_change_pill_green);
                } else {
                    mChange.setBackgroundResource(R.drawable.percent_change_pill_red);
                }

                final int historyColumnIndex = cursor.getColumnIndex(Contract.Quote.COLUMN_HISTORY);
                final String history = cursor.getString(historyColumnIndex);
                final List<String> historyPoints = Arrays.asList(history.split("\\r?\\n"));
                Collections.reverse(historyPoints);

                final List<Long> xAxisValues = new ArrayList<>();
                int xAxisPosition = 0;
                final List<Entry> entries = new ArrayList<Entry>();
                for (String historyPoint : historyPoints) {
                    final String xyPair[] = historyPoint.split(", ");
                    xAxisValues.add(Long.parseLong(xyPair[0]));
                    final float y = Float.parseFloat(xyPair[1]);
                    entries.add(new Entry(xAxisPosition, y));
                    xAxisPosition++;
                }

                final LineDataSet dataSet = new LineDataSet(entries, "History");
                dataSet.setColor(R.color.colorAccent);
                final LineData lineData = new LineData(dataSet);
                mHistory.setData(lineData);
                mHistory.invalidate();
                final XAxis xAxis = mHistory.getXAxis();
                xAxis.setLabelRotationAngle(-45);
                xAxis.setValueFormatter(new IAxisValueFormatter() {
                    @Override
                    public String getFormattedValue(float value, AxisBase axis) {
                        final Date date = new Date(xAxisValues.get((int) value));
                        return new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH).format(date);
                    }
                });


                cursor.close();
            }
        }
    }
}
