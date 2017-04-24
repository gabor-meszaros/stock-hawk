package com.udacity.stockhawk.ui;

import android.content.Context;
import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.udacity.stockhawk.R;
import com.udacity.stockhawk.data.Contract;
import com.udacity.stockhawk.data.PrefUtils;
import com.udacity.stockhawk.util.UiUtils;

import butterknife.BindView;
import butterknife.ButterKnife;

class StockAdapter extends RecyclerView.Adapter<StockAdapter.StockViewHolder> {

    private final Context context;
    private Cursor cursor;
    private final StockAdapterOnClickHandler clickHandler;

    StockAdapter(Context context, StockAdapterOnClickHandler clickHandler) {
        this.context = context;
        this.clickHandler = clickHandler;
    }

    void setCursor(Cursor cursor) {
        this.cursor = cursor;
        notifyDataSetChanged();
    }

    String getSymbolAtPosition(int position) {
        cursor.moveToPosition(position);
        return cursor.getString(Contract.Quote.POSITION_SYMBOL);
    }

    @Override
    public StockViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View item = LayoutInflater.from(context).inflate(R.layout.list_item_quote, parent, false);
        return new StockViewHolder(item);
    }

    @Override
    public void onBindViewHolder(StockViewHolder holder, int position) {

        cursor.moveToPosition(position);

        holder.symbol.setText(cursor.getString(Contract.Quote.POSITION_SYMBOL));

        final String history = cursor.getString(Contract.Quote.POSITION_HISTORY);
        if (null != history && !history.isEmpty()) {
            showPriceFields(holder);

            holder.price.setText(
                    UiUtils.getDollar(cursor.getFloat(Contract.Quote.POSITION_PRICE), false));

            float rawAbsoluteChange = cursor.getFloat(Contract.Quote.POSITION_ABSOLUTE_CHANGE);
            float percentageChange = cursor.getFloat(Contract.Quote.POSITION_PERCENTAGE_CHANGE);

            if (rawAbsoluteChange > 0) {
                holder.change.setBackgroundResource(R.drawable.percent_change_pill_green);
            } else {
                holder.change.setBackgroundResource(R.drawable.percent_change_pill_red);
            }

            String change = UiUtils.getDollar(rawAbsoluteChange, true);
            String percentage = UiUtils.getPercentage(percentageChange / 100, true);

            if (PrefUtils.getDisplayMode(context)
                    .equals(context.getString(R.string.pref_display_mode_absolute_key))) {
                holder.change.setText(change);
            } else {
                holder.change.setText(percentage);
            }
            holder.enableOnClick();
        } else {
            showErrorDisplay(holder);
            holder.disableOnClick();
        }
    }

    private void showErrorDisplay(final StockViewHolder holder) {
        holder.error.setVisibility(View.VISIBLE);
        holder.price.setVisibility(View.GONE);
        holder.change.setVisibility(View.GONE);
    }

    private void showPriceFields(final StockViewHolder holder) {
        holder.error.setVisibility(View.GONE);
        holder.price.setVisibility(View.VISIBLE);
        holder.change.setVisibility(View.VISIBLE);
    }

    @Override
    public int getItemCount() {
        int count = 0;
        if (cursor != null) {
            count = cursor.getCount();
        }
        return count;
    }


    interface StockAdapterOnClickHandler {
        void onClick(String symbol);
    }

    class StockViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        @BindView(R.id.activity_main_list_item_symbol)
        TextView symbol;

        @BindView(R.id.activity_main_list_item_error_display)
        TextView error;

        @BindView(R.id.activity_main_list_item_price)
        TextView price;

        @BindView(R.id.activity_main_list_item_change)
        TextView change;

        StockViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        @Override
        public void onClick(View v) {
            int adapterPosition = getAdapterPosition();
            cursor.moveToPosition(adapterPosition);
            int symbolColumn = cursor.getColumnIndex(Contract.Quote.COLUMN_SYMBOL);
            clickHandler.onClick(cursor.getString(symbolColumn));
        }

        public void enableOnClick() {
            itemView.setOnClickListener(this);
        }

        public void disableOnClick() {
            itemView.setOnClickListener(null);
        }
    }
}
