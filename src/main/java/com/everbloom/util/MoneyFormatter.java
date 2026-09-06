package com.everbloom.util;

import java.math.BigDecimal;

public final class MoneyFormatter {

    private MoneyFormatter() {
    }

    public static String format(long minorUnits) {
        return "BDT " + formatAmount(minorUnits);
    }

    public static String formatAmount(long minorUnits) {
        return BigDecimal.valueOf(minorUnits, 2).setScale(2).toPlainString();
    }
}
