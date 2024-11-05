package codes.shiftmc.common.util;

import java.text.DecimalFormat;

public final class NumberFormatter {

    private static final String[] suffixes;
    private static final DecimalFormat decimal;

    static {
        suffixes = new String[]{"", "K", "M", "B", "T", "Q", "QQ", "S", "SS", "O", "N", "D", "UN", "DD"};
        decimal = new DecimalFormat("#.##");
    }

    public static String format(double number) {
        if (number < 0) throw new IllegalArgumentException("Invalid number: " + number);
        if (number == 0) return "0";

        final var size = Math.log10(number);

        if (size < 3) return String.valueOf(number);

        final int index = (int) Math.floor(size / 3);
        if (index >= suffixes.length) return String.valueOf(number); // Number way too big to format

        final double scaledNumber = number / Math.pow(10, index * 3);
        return decimal.format(scaledNumber) + suffixes[index];
    }
}
