package cu.hogarplaya;

import android.content.Context;
import android.content.SharedPreferences;
import android.security.keystore.KeyGenParameterSpec;
import android.util.Base64;
import java.nio.charset.StandardCharsets;
import java.security.KeyStore;
import java.security.MessageDigest;
import java.security.SecureRandom;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.PBEKeySpec;

/* loaded from: classes.dex */
public final class Vault {
    private static final String ALIAS = "hogar-playa-device-v1";
    private final SharedPreferences prefs;

    public Vault(Context context) {
        this.prefs = context.getSharedPreferences("vault", 0);
    }

    private SecretKey key() throws Exception {
        KeyStore keyStore = KeyStore.getInstance("AndroidKeyStore");
        keyStore.load(null);
        if (!keyStore.containsAlias(ALIAS)) {
            KeyGenerator keyGenerator = KeyGenerator.getInstance("AES", "AndroidKeyStore");
            keyGenerator.init(new KeyGenParameterSpec.Builder(ALIAS, 3).setBlockModes("GCM").setEncryptionPaddings("NoPadding").build());
            keyGenerator.generateKey();
        }
        return ((KeyStore.SecretKeyEntry) keyStore.getEntry(ALIAS, null)).getSecretKey();
    }

    public static String pinHash(String str, String str2) throws Exception {
        if (str == null || !str.matches("[0-9]{4,6}")) {
            throw new IllegalArgumentException("Usa un PIN de 4 a 6 números.");
        }
        PBEKeySpec pBEKeySpec = new PBEKeySpec(str.toCharArray(), Base64.decode(str2, 2), 120000, 256);
        try {
            return Base64.encodeToString(SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(pBEKeySpec).getEncoded(), 2);
        } finally {
            pBEKeySpec.clearPassword();
        }
    }

    public static String salt() {
        byte[] bArr = new byte[16];
        new SecureRandom().nextBytes(bArr);
        return Base64.encodeToString(bArr, 2);
    }

    public static boolean same(String str, String str2) {
        return MessageDigest.isEqual(str.getBytes(StandardCharsets.UTF_8), str2.getBytes(StandardCharsets.UTF_8));
    }

    public void putToken(String str) throws Exception {
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        cipher.init(1, key());
        this.prefs.edit().putString("token", Base64.encodeToString(cipher.doFinal(str.getBytes(StandardCharsets.UTF_8)), 2)).putString("iv", Base64.encodeToString(cipher.getIV(), 2)).commit();
    }

    public String token() throws Exception {
        String string = this.prefs.getString("token", "");
        if (string.isEmpty()) {
            return "";
        }
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        cipher.init(2, key(), new GCMParameterSpec(128, Base64.decode(this.prefs.getString("iv", ""), 2)));
        return new String(cipher.doFinal(Base64.decode(string, 2)), StandardCharsets.UTF_8);
    }
}
