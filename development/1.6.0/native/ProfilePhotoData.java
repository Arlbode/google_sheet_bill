package cu.hogarplaya;
import org.json.*;import java.util.*;
/** Solo fotos de perfiles existentes; nunca devuelve datos de autenticación. */
public final class ProfilePhotoData {
 public static boolean validPhoto(String s){
  if(s==null||s.length()>120000||!s.matches("data:image/jpeg;base64,[A-Za-z0-9+/=]+"))return false;
  try{byte[] b=Base64.getDecoder().decode(s.substring(s.indexOf(',')+1));return b.length>=4&&b.length<=80000&&(b[0]&255)==255&&(b[1]&255)==216&&(b[b.length-2]&255)==255&&(b[b.length-1]&255)==217;}catch(Exception e){return false;}
 }
 public static JSONObject visible(JSONObject raw,JSONArray profiles)throws Exception{JSONObject result=new JSONObject();for(int i=0;i<profiles.length();i++){String id=profiles.getJSONObject(i).getString("id"),photo=raw.optString(id);if(validPhoto(photo))result.put(id,photo);}return result;}
 public static JSONObject save(JSONObject raw,JSONArray profiles,String id,String photo)throws Exception{
  boolean found=false;for(int i=0;i<profiles.length();i++)if(id.equals(profiles.getJSONObject(i).optString("id")))found=true;
  if(!found||photo!=null&&!validPhoto(photo))throw new IllegalArgumentException("No se pudo guardar la foto de este perfil.");JSONObject result=visible(raw,profiles);if(photo==null)result.remove(id);else result.put(id,photo);if(result.toString().length()>3000000)throw new IllegalArgumentException("Hay demasiadas fotos guardadas en este teléfono.");return result;
 }
 public static boolean session(String current,String expected,String profile,String day,String pinVersion){try{JSONObject s=new JSONObject(current);return !current.isEmpty()&&current.equals(expected)&&profile.equals(s.getString("profileId"))&&day.equals(s.getString("day"))&&pinVersion.equals(s.getString("pinVersion"));}catch(Exception e){return false;}}
}
