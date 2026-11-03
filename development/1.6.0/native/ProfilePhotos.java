package cu.hogarplaya;
import android.database.Cursor;import android.graphics.*;import android.media.ExifInterface;import java.io.*;import java.nio.charset.StandardCharsets;import java.security.MessageDigest;import java.time.*;import java.util.*;import org.json.*;
/** Fotos locales en cache SQLite, incluidas en el respaldo cifrado existente. */
public final class ProfilePhotos {
 public static final String KEY="profilePhotos170";
 public static JSONObject read(AppStore s)throws Exception{return ProfilePhotoData.visible(new JSONObject(s.cache(KEY,"{}")),s.profiles());}
 public static void remove(AppStore s,String id)throws Exception{s.cache(KEY,ProfilePhotoData.save(new JSONObject(s.cache(KEY,"{}")),s.profiles(),id,null));}
 static String pinVersion(AppStore s,String id)throws Exception{try(Cursor c=s.getReadableDatabase().rawQuery("SELECT pin_hash,salt FROM profiles WHERE id=?",new String[]{id})){if(!c.moveToFirst())throw new IllegalArgumentException("El perfil ya no existe.");byte[] hash=MessageDigest.getInstance("SHA-256").digest((c.getString(0)+":"+c.getString(1)).getBytes(StandardCharsets.UTF_8));StringBuilder text=new StringBuilder();for(byte b:hash)text.append(String.format(Locale.ROOT,"%02x",b&255));return text.toString();}}
 static void requireSession(AppStore s,String id,String expected)throws Exception{String today=LocalDate.now(ZoneId.of("America/Havana")).toString();if(!ProfilePhotoData.session(s.prefs.getString("authenticatedDay",""),expected,id,today,pinVersion(s,id)))throw new IllegalArgumentException("Vuelve a entrar en tu perfil y elige la foto otra vez.");}
 public static void save(AppStore s,String id,String expected,byte[] raw)throws Exception {
  requireSession(s,id,expected);String encoded;
  try{
   BitmapFactory.Options o=new BitmapFactory.Options();o.inJustDecodeBounds=true;BitmapFactory.decodeByteArray(raw,0,raw.length,o);if(o.outWidth<1||o.outHeight<1||o.outWidth>50000||o.outHeight>50000)throw new IOException("Elige una imagen válida de la galería.");o.inSampleSize=1;while(o.outWidth/o.inSampleSize>1024||o.outHeight/o.inSampleSize>1024)o.inSampleSize*=2;o.inJustDecodeBounds=false;
   Bitmap source=BitmapFactory.decodeByteArray(raw,0,raw.length,o);if(source==null)throw new IOException("No se pudo abrir esa imagen.");
   try{
    int orientation=1;try{orientation=new ExifInterface(new ByteArrayInputStream(raw)).getAttributeInt(ExifInterface.TAG_ORIENTATION,1);}catch(IOException ignored){}
    Matrix m=new Matrix();switch(orientation){case 2:m.setScale(-1,1);break;case 3:m.setRotate(180);break;case 4:m.setScale(1,-1);break;case 5:m.setRotate(90);m.postScale(-1,1);break;case 6:m.setRotate(90);break;case 7:m.setRotate(-90);m.postScale(-1,1);break;case 8:m.setRotate(-90);break;}
    if(!m.isIdentity()){Bitmap rotated=Bitmap.createBitmap(source,0,0,source.getWidth(),source.getHeight(),m,true);if(rotated!=source){source.recycle();source=rotated;}}
    Bitmap avatar=Bitmap.createBitmap(256,256,Bitmap.Config.ARGB_8888);try{Canvas canvas=new Canvas(avatar);canvas.drawColor(Color.WHITE);int size=Math.min(source.getWidth(),source.getHeight()),left=(source.getWidth()-size)/2,top=(source.getHeight()-size)/2;canvas.drawBitmap(source,new Rect(left,top,left+size,top+size),new Rect(0,0,256,256),new Paint(Paint.FILTER_BITMAP_FLAG));ByteArrayOutputStream out=new ByteArrayOutputStream();if(!avatar.compress(Bitmap.CompressFormat.JPEG,82,out)||out.size()>80000)throw new IOException("Elige una foto más pequeña.");encoded="data:image/jpeg;base64,"+android.util.Base64.encodeToString(out.toByteArray(),2);}finally{avatar.recycle();}
   }finally{source.recycle();}
  }catch(OutOfMemoryError e){throw new IOException("La imagen es demasiado grande. Elige otra foto.");}
  requireSession(s,id,expected);s.cache(KEY,ProfilePhotoData.save(new JSONObject(s.cache(KEY,"{}")),s.profiles(),id,encoded));SyncEngine.changed();
 }
}
