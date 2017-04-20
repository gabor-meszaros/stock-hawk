package com.udacity.stockhawk.ui;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import com.udacity.stockhawk.R;
import com.udacity.stockhawk.data.Contract;

import butterknife.BindView;
import butterknife.ButterKnife;

public class StockDetailsActivity extends AppCompatActivity {

    @BindView(R.id.stock_details_symbol)
    TextView mSymbol;

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
                cursor.close();
            }
        }
    }
}
