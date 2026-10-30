import cu.hogarplaya.BackupCipher;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
public class BackupCipherTest {
    public static void main(String[] args)throws Exception {
        byte[] original="datos ficticios y movimientos de prueba".getBytes(StandardCharsets.UTF_8);
        char[] pass="solo-prueba-123456".toCharArray();byte[] encrypted=BackupCipher.encrypt(original,pass);
        if(!Arrays.equals(original,BackupCipher.decrypt(encrypted,pass)))throw new AssertionError("No recuperó datos");
        if(Arrays.equals(encrypted,BackupCipher.encrypt(original,pass)))throw new AssertionError("Sal/nonce repetidos");
        int rejected=0;
        try{BackupCipher.decrypt(encrypted,"incorrecta-123456".toCharArray());}catch(Exception expected){rejected++;}
        encrypted[encrypted.length-1]^=1;try{BackupCipher.decrypt(encrypted,pass);}catch(Exception expected){rejected++;}
        try{BackupCipher.encrypt(original,"1234".toCharArray());}catch(Exception expected){rejected++;}
        try{BackupCipher.decrypt(new byte[200],pass);}catch(Exception expected){rejected++;}
        if(rejected!=4)throw new AssertionError("No rechazó contraseña, manipulación, clave débil o formato");
        System.out.println("PASS: cifrado/recuperación, nonce aleatorio, contraseña incorrecta, manipulación, longitud y formato.");
    }
}
