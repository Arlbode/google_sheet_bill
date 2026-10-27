package cu.hogarplaya;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.UUID;

/* loaded from: classes.dex */
public final class Money {
    private Money() {
    }

    public static long cents(String str) {
        String replace = str == null ? "" : str.trim().replace(',', '.');
        if (!replace.matches("[0-9]+(?:\\.[0-9]{1,2})?")) {
            throw new IllegalArgumentException("Escribe el importe sin separadores de miles, con hasta dos decimales.");
        }
        long longValueExact = new BigDecimal(replace).movePointRight(2).longValueExact();
        if (longValueExact <= 0 || longValueExact > 10000000000L) {
            throw new IllegalArgumentException("El importe debe ser mayor que cero.");
        }
        return longValueExact;
    }

    public static String date(String str) {
        try {
            LocalDate parse = LocalDate.parse(str);
            if (parse.getYear() < 2000 || parse.getYear() >= 2100 || parse.isAfter(LocalDate.now(ZoneId.of("America/Havana")))) {
                throw new Exception();
            }
            return parse.toString();
        } catch (Exception e) {
            throw new IllegalArgumentException("Revisa la fecha; no puede ser futura.");
        }
    }

    public static String decimal(long j) {
        return BigDecimal.valueOf(j, 2).toPlainString();
    }

    public static long lineTotal(String str, String str2) {
        try {
            BigDecimal bigDecimal = new BigDecimal(str.replace(',', '.'));
            if (bigDecimal.signum() <= 0 || bigDecimal.compareTo(new BigDecimal("100000")) > 0 || bigDecimal.scale() > 4) {
                throw new IllegalArgumentException("Revisa la cantidad.");
            }
            return BigDecimal.valueOf(cents(str2), 2).multiply(bigDecimal).setScale(2, RoundingMode.HALF_UP).movePointRight(2).longValueExact();
        } catch (Exception e) {
            throw new IllegalArgumentException("Cantidad no válida.");
        }
    }

    public static String rate(String str, boolean z) {
        String replace = str == null ? "" : str.trim().replace(',', '.');
        if (replace.isEmpty() && !z) {
            return "";
        }
        if (!replace.matches("[0-9]+(?:\\.[0-9]{1,6})?")) {
            throw new IllegalArgumentException("Introduce la tasa real en CUP por USD.");
        }
        BigDecimal bigDecimal = new BigDecimal(replace);
        if (bigDecimal.signum() <= 0 || bigDecimal.compareTo(new BigDecimal("1000000")) > 0) {
            throw new IllegalArgumentException("La tasa no es válida.");
        }
        return AppStore$$ExternalSyntheticBackportWithForwarding0.m(bigDecimal).toPlainString();
    }

    public static String today() {
        return LocalDate.now(ZoneId.of("America/Havana")).toString();
    }

    public static String uuid(String str) {
        try {
            if (UUID.fromString(str).toString().equalsIgnoreCase(str)) {
                return str;
            }
            throw new Exception();
        } catch (Exception e) {
            throw new IllegalArgumentException("Identificador no válido.");
        }
    }
}
