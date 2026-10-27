package cu.hogarplaya;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.HashSet;
import org.json.JSONArray;
import org.json.JSONObject;

/* loaded from: classes.dex */
public final class Planning {
    public static JSONObject evaluate(JSONObject jSONObject, JSONArray jSONArray, String str) throws Exception {
        String str2;
        String str3;
        String str4;
        String str5;
        double d;
        HashSet hashSet;
        JSONObject optJSONObject = jSONObject.optJSONObject("planning");
        String str6 = "enabled";
        String str7 = "issues";
        String str8 = "plans";
        if (optJSONObject == null) {
            return new JSONObject().put("available", false).put("enabled", true).put("plans", new JSONArray()).put("issues", new JSONArray().put("Conecta y actualiza el Sheet para recibir el plan de pagos CUP."));
        }
        JSONObject jSONObject2 = new JSONObject(optJSONObject.toString());
        jSONObject2.put("available", true);
        JSONObject optJSONObject2 = jSONObject.optJSONObject("balances");
        double optDouble = optJSONObject2 == null ? 0.0d : optJSONObject2.optDouble("usd", 0.0d);
        double optDouble2 = optJSONObject2 == null ? 0.0d : optJSONObject2.optDouble("cup", 0.0d);
        HashSet hashSet2 = new HashSet();
        JSONArray optJSONArray = jSONObject.optJSONArray("knownOperationIds");
        if (optJSONArray != null) {
            for (int i = 0; i < optJSONArray.length(); i++) {
                hashSet2.add(optJSONArray.getString(i));
            }
        }
        HashMap hashMap = new HashMap();
        int i2 = 0;
        while (true) {
            str2 = "rate";
            str3 = "id";
            if (i2 >= jSONArray.length()) {
                break;
            }
            JSONObject jSONObject3 = jSONArray.getJSONObject(i2);
            String optString = jSONObject3.optString("status");
            String str9 = str6;
            String optString2 = jSONObject3.optString("type");
            String str10 = str7;
            String optString3 = jSONObject3.optString("currency");
            if ((optString.equals("PENDING") || optString.equals("SENDING")) && !hashSet2.contains(jSONObject3.getString("id"))) {
                double d2 = jSONObject3.getDouble("amount");
                if (optString2.equals("Cambio")) {
                    optDouble -= d2;
                    hashSet = hashSet2;
                    optDouble2 += Math.round((jSONObject3.getDouble("rate") * d2) * 100.0d) / 100.0d;
                } else {
                    hashSet = hashSet2;
                    if (optString2.equals("Ingreso") || optString2.equals("Gasto")) {
                        double d3 = (optString2.equals("Ingreso") ? 1.0d : -1.0d) * d2;
                        if (optString3.equals("USD")) {
                            optDouble += d3;
                        } else {
                            optDouble2 += d3;
                        }
                    }
                }
                if (optString2.equals("Gasto") && optString3.equals("CUP") && !jSONObject3.optString("planId").isEmpty()) {
                    String string = jSONObject3.getString("planId");
                    hashMap.put(string, Double.valueOf(((Double) hashMap.getOrDefault(string, Double.valueOf(0.0d))).doubleValue() + d2));
                }
            } else {
                hashSet = hashSet2;
            }
            i2++;
            str6 = str9;
            str7 = str10;
            hashSet2 = hashSet;
        }
        String str11 = str6;
        String str12 = str7;
        String localDate = LocalDate.parse(str).plusDays(jSONObject2.optInt("horizon", 15)).toString();
        String localDate2 = LocalDate.parse(str).plusDays(jSONObject2.optInt("lead", 3)).toString();
        double max = Math.max(0.0d, optDouble2 - jSONObject2.optDouble("cupReserve", 0.0d));
        double d4 = optDouble2;
        double max2 = Math.max(0.0d, optDouble - jSONObject2.optDouble("protectedUSD", 175.0d));
        JSONArray optJSONArray2 = jSONObject2.optJSONArray("plans");
        if (optJSONArray2 == null) {
            optJSONArray2 = new JSONArray();
        }
        String str13 = "";
        double d5 = optDouble;
        int i3 = 0;
        double d6 = 0.0d;
        while (true) {
            str4 = str8;
            str5 = "paid";
            d = max2;
            if (i3 >= optJSONArray2.length()) {
                break;
            }
            JSONObject jSONObject4 = optJSONArray2.getJSONObject(i3);
            JSONArray jSONArray2 = optJSONArray2;
            double doubleValue = ((Double) hashMap.getOrDefault(jSONObject4.getString(str3), Double.valueOf(0.0d))).doubleValue();
            String str14 = str2;
            JSONObject jSONObject5 = jSONObject2;
            String str15 = str3;
            double max3 = Math.max(0.0d, jSONObject4.optDouble("remaining", 0.0d) - doubleValue);
            jSONObject4.put("remaining", max3).put("paid", jSONObject4.optDouble("paid", 0.0d) + doubleValue);
            if (jSONObject4.optBoolean("essential") && max3 > 0.0d) {
                if (jSONObject4.getString("date").compareTo(localDate) <= 0) {
                    d6 += max3;
                    if (str13.isEmpty() && d6 > 1.0E-6d + max) {
                        str13 = jSONObject4.getString("date").compareTo(str) < 0 ? str : jSONObject4.getString("date");
                    }
                }
                i3++;
                optJSONArray2 = jSONArray2;
                str2 = str14;
                str8 = str4;
                max2 = d;
                jSONObject2 = jSONObject5;
                str3 = str15;
            }
            i3++;
            optJSONArray2 = jSONArray2;
            str2 = str14;
            str8 = str4;
            max2 = d;
            jSONObject2 = jSONObject5;
            str3 = str15;
        }
        JSONArray jSONArray3 = optJSONArray2;
        String str16 = str2;
        JSONObject jSONObject6 = jSONObject2;
        String str17 = str3;
        JSONArray optJSONArray3 = jSONObject6.optJSONArray("allPlans");
        if (optJSONArray3 != null) {
            int i4 = 0;
            while (i4 < optJSONArray3.length()) {
                JSONObject jSONObject7 = optJSONArray3.getJSONObject(i4);
                double doubleValue2 = ((Double) hashMap.getOrDefault(jSONObject7.getString(str17), Double.valueOf(0.0d))).doubleValue();
                jSONObject7.put(str5, jSONObject7.optDouble(str5, 0.0d) + doubleValue2).put("remaining", Math.max(0.0d, jSONObject7.optDouble("remaining", 0.0d) - doubleValue2));
                i4++;
                str5 = str5;
                str16 = str16;
                optJSONArray3 = optJSONArray3;
                hashMap = hashMap;
                localDate = localDate;
            }
        }
        String str18 = localDate;
        String str19 = str16;
        JSONObject optJSONObject3 = jSONObject6.optJSONObject("quote");
        double optDouble3 = optJSONObject3 == null ? 0.0d : optJSONObject3.optDouble(str19, 0.0d);
        if (optJSONObject3 != null) {
            optJSONObject3.put("fresh", optJSONObject3.optString("day").equals(str));
        }
        double max4 = Math.max(0.0d, d6 - max);
        double ceil = optDouble3 > 0.0d ? Math.ceil(((max4 / optDouble3) - 1.0E-10d) * 100.0d) / 100.0d : 0.0d;
        boolean z = max4 > 0.0d && !str13.isEmpty() && str13.compareTo(localDate2) <= 0;
        JSONArray optJSONArray4 = jSONObject6.optJSONArray(str12);
        if (optJSONArray4 == null) {
            optJSONArray4 = new JSONArray();
        }
        return jSONObject6.put(str4, jSONArray3).put("today", str).put("end", str18).put("usdBalance", d5).put("cupBalance", d4).put("cupAvailable", max).put("freeUSD", d).put("upcomingCUP", d6).put("shortfall", max4).put("firstShortfall", str13).put("urgent", z).put("neededUSD", ceil).put("maxUSD", (jSONObject6.optBoolean(str11) && jSONObject6.optBoolean("controlsValid") && optJSONArray4.length() == 0 && z) ? Math.min(Math.floor((d + 1.0E-8d) * 100.0d) / 100.0d, ceil) : 0.0d).put("fullyFunded", ceil <= d).put(str12, optJSONArray4);
    }

    public static void validateChange(AppStore appStore, JSONObject jSONObject) throws Exception {
        JSONObject evaluate = evaluate(PlanStore.snapshot(appStore), appStore.operations(), Money.today());
        if (!evaluate.optBoolean("available")) {
            throw new IllegalArgumentException("Conecta el Sheet y completa Plan CUP antes de cambiar USD.");
        }
        if (evaluate.optBoolean("enabled")) {
            if (evaluate.getJSONArray("issues").length() > 0) {
                throw new IllegalArgumentException(evaluate.getJSONArray("issues").getString(0));
            }
            if (!evaluate.optBoolean("urgent")) {
                throw new IllegalArgumentException("Tus CUP cubren los pagos próximos o todavía no llegó la fecha del aviso. No necesitas cambiar USD ahora.");
            }
            double min = (Math.min(evaluate.getDouble("freeUSD"), Math.ceil(((evaluate.getDouble("shortfall") / Double.parseDouble(Money.rate(jSONObject.optString("rate"), true))) - 1.0E-10d) * 100.0d) / 100.0d) + 1.0E-8d) * 100.0d;
            if (Money.cents(jSONObject.getString("amount")) > Math.floor(min)) {
                throw new IllegalArgumentException("El plan permite hasta " + Money.decimal((long) Math.floor(min)) + " USD con esta tasa; conserva la reserva protegida.");
            }
        }
    }
}
