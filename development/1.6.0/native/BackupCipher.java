package cu.hogarplaya;
import java.security.SecureRandom;
import java.util.Arrays;
import javax.crypto.*;
import javax.crypto.spec.*;
/** Formato binario fijo: HGF2, sal de 16 bytes, nonce de 12, AES-256-GCM. */
public final class BackupCipher {
    private static final byte[] MAGIC={'H','G','F','2'};
    private static byte[] key(char[] password,byte[] salt)throws Exception {
        PBEKeySpec spec=new PBEKeySpec(password,salt,210000,256);
        try{return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).getEncoded();}finally{spec.clearPassword();}
    }
    public static byte[] encrypt(byte[] plain,char[] password)throws Exception {
        if(password.length<10)throw new IllegalArgumentException("Usa una contraseña de al menos 10 caracteres.");
        byte[] salt=new byte[16],iv=new byte[12];SecureRandom random=new SecureRandom();random.nextBytes(salt);random.nextBytes(iv);
        byte[] k=key(password,salt);
        try{Cipher cipher=Cipher.getInstance("AES/GCM/NoPadding");cipher.init(Cipher.ENCRYPT_MODE,new SecretKeySpec(k,"AES"),new GCMParameterSpec(128,iv));cipher.updateAAD(MAGIC);byte[] encrypted=cipher.doFinal(plain);byte[] result=new byte[32+encrypted.length];System.arraycopy(MAGIC,0,result,0,4);System.arraycopy(salt,0,result,4,16);System.arraycopy(iv,0,result,20,12);System.arraycopy(encrypted,0,result,32,encrypted.length);return result;}finally{Arrays.fill(k,(byte)0);}
    }
    public static byte[] decrypt(byte[] encrypted,char[] password)throws Exception {
        if(encrypted.length<48||encrypted.length>24000000||!Arrays.equals(MAGIC,Arrays.copyOf(encrypted,4)))throw new IllegalArgumentException("Formato de respaldo no admitido.");
        byte[] k=key(password,Arrays.copyOfRange(encrypted,4,20));
        try{Cipher cipher=Cipher.getInstance("AES/GCM/NoPadding");cipher.init(Cipher.DECRYPT_MODE,new SecretKeySpec(k,"AES"),new GCMParameterSpec(128,Arrays.copyOfRange(encrypted,20,32)));cipher.updateAAD(MAGIC);return cipher.doFinal(encrypted,32,encrypted.length-32);}finally{Arrays.fill(k,(byte)0);}
    }
}
