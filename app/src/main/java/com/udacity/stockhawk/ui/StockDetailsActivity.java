package com.udacity.stockhawk.ui;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import com.udacity.stockhawk.R;

import butterknife.BindView;
import butterknife.ButterKnife;

public class StockDetailsActivity extends AppCompatActivity {

    public static final String EXTRA_STOCK_SYMBOL = "extra-stock-name";

    @BindView(R.id.stock_details_symbol)
    TextView mSymbol;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stock_details);
        ButterKnife.bind(this);

        final Intent intent = getIntent();
        if (null != intent && intent.hasExtra(EXTRA_STOCK_SYMBOL)) {
            final String symbol= intent.getStringExtra(EXTRA_STOCK_SYMBOL);
            mSymbol.setText(symbol);
        }
    }
}
