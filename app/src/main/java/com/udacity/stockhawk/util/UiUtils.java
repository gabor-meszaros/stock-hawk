package com.udacity.stockhawk.util;

import android.view.View;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;

public class UiUtils {
    private static final DecimalFormat DOLLAR_FORMAT =
            (DecimalFormat) NumberFormat.getCurrencyInstance(Locale.US);
    private static final DecimalFormat DOLLAR_FORMAT_WITH_PLUS =
            (DecimalFormat) NumberFormat.getCurrencyInstance(Locale.US);
    private static final DecimalFormat PERCENTAGE_FORMAT =
            (DecimalFormat) NumberFormat.getPercentInstance(Locale.getDefault());
    private static final DecimalFormat PERCENTAGE_FORMAT_WITH_PLUS =
            (DecimalFormat) NumberFormat.getPercentInstance(Locale.getDefault());

    static {
        DOLLAR_FORMAT_WITH_PLUS.setPositivePrefix("+$");

        PERCENTAGE_FORMAT.setMaximumFractionDigits(2);
        PERCENTAGE_FORMAT.setMinimumFractionDigits(2);

        PERCENTAGE_FORMAT_WITH_PLUS.setMaximumFractionDigits(2);
        PERCENTAGE_FORMAT_WITH_PLUS.setMinimumFractionDigits(2);
        PERCENTAGE_FORMAT_WITH_PLUS.setPositivePrefix("+");
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

    public static String getChange(final double rawChange, final double percentageChange) {
        final double absoluteChange = Math.abs(percentageChange);
        return getDollar(rawChange, true) + " (" + getPercentage(absoluteChange, false) + ")";
    }
}
