package com.udacity.stockhawk.util;

import android.content.Context;
import android.os.Build;
import android.support.annotation.NonNull;

import com.udacity.stockhawk.R;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;

public class UiUtils {
    private static final DecimalFormat DOLLAR_FORMAT =
            (DecimalFormat) NumberFormat.getCurrencyInstance(Locale.US);

    private static final String POSITIVE_PREFIX = "+";
    private static final String POSITIVE_DOLLAR_PREFIX = POSITIVE_PREFIX + "$";
    private static final DecimalFormat DOLLAR_FORMAT_WITH_PLUS =
            (DecimalFormat) NumberFormat.getCurrencyInstance(Locale.US);

    private static final DecimalFormat PERCENTAGE_FORMAT =
            (DecimalFormat) NumberFormat.getPercentInstance(Locale.getDefault());

    private static final DecimalFormat PERCENTAGE_FORMAT_WITH_PLUS =
            (DecimalFormat) NumberFormat.getPercentInstance(Locale.getDefault());

    static {
        DOLLAR_FORMAT_WITH_PLUS.setPositivePrefix(POSITIVE_DOLLAR_PREFIX);

        PERCENTAGE_FORMAT.setMaximumFractionDigits(2);
        PERCENTAGE_FORMAT.setMinimumFractionDigits(2);

        PERCENTAGE_FORMAT_WITH_PLUS.setMaximumFractionDigits(2);
        PERCENTAGE_FORMAT_WITH_PLUS.setMinimumFractionDigits(2);
        PERCENTAGE_FORMAT_WITH_PLUS.setPositivePrefix(POSITIVE_PREFIX);
    }

    public static String getDollar(final double amount, final boolean showPositiveSign) {
        if (showPositiveSign) {
            return DOLLAR_FORMAT_WITH_PLUS.format(amount);
        } else {
            return DOLLAR_FORMAT.format(amount);
        }
    }

    public static String getPercentage(final double amount, final boolean showPositiveSign) {
        if (showPositiveSign) {
            return PERCENTAGE_FORMAT_WITH_PLUS.format(amount);
        } else {
            return PERCENTAGE_FORMAT.format(amount);
        }
    }

    public static String getChange(@NonNull final Context context, final double rawChange,
                                   final double percentageChange) {
        final double absoluteChange = Math.abs(percentageChange);
        final String formattedDollarChange= getDollar(rawChange, true);
        final String formattedPercentageChange = getPercentage(absoluteChange, false);
        return context.getString(R.string.stock_price_change_combined_format,formattedDollarChange,
                formattedPercentageChange);
    }

    public static int getColor(@NonNull final Context context, final int resourceColorId) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return context.getColor(resourceColorId);
        } else {
            return context.getResources().getColor(resourceColorId);
        }
    }
}
