package cu.hogarplaya;
import java.io.*;import java.net.*;import java.nio.file.*;import java.util.*;import javax.net.ssl.*;import java.security.Principal;import java.security.cert.Certificate;
public class UpdateTransferTest {
 static final String URL="https://raw.githubusercontent.com/Arlbode/google_sheet_bill/actualizaciones-android/Hogar_finanzas_9.9.9.apk";
 static int tests;
 static void check(boolean b){if(!b)throw new AssertionError();tests++;}
 static class Fake extends HttpsURLConnection {
  InputStream stream;int code;boolean disconnected;
  Fake(InputStream s,int response)throws Exception{super(new URL(URL));stream=s;code=response;}
  public void connect(){}public void disconnect(){disconnected=true;}public boolean usingProxy(){return false;}
  public int getResponseCode(){return code;}public InputStream getInputStream(){return stream;}
  public String getCipherSuite(){return "test";}public Certificate[] getLocalCertificates(){return null;}public Certificate[] getServerCertificates(){return null;}
 }
 interface Task{void run()throws Exception;}
 static void fails(Task t,Class<?> kind)throws Exception{try{t.run();throw new AssertionError("Accepted "+kind);}catch(Exception e){check(kind.isInstance(e));}}
 public static void main(String[] args)throws Exception {
  File dir=Files.createTempDirectory("hogar-transfer-test").toFile();byte[] data=new byte[128];Arrays.fill(data,(byte)73);List<Fake> used=new ArrayList<>();
  File f=UpdateTransfer.transfer(URL,128,dir,url->{try{Fake c=new Fake(used.isEmpty()?new InputStream(){int count;public int read()throws IOException{if(count++<35)return 12;throw new SSLException("BAD_DECRYPT");}}:new ByteArrayInputStream(data),200);used.add(c);return c;}catch(Exception e){throw new IOException(e);}});
  check(used.size()==2);check(Arrays.equals(Files.readAllBytes(f.toPath()),data));check(dir.list().length==1);check(used.stream().allMatch(c->c.disconnected));check("close".equals(used.get(1).getRequestProperty("Connection")));check(!used.get(1).getInstanceFollowRedirects());check(!used.get(1).getUseCaches());f.delete();
  int[] calls={0};fails(()->UpdateTransfer.transfer(URL,128,dir,url->{calls[0]++;throw new SSLException("DECRYPTION_FAILED");}),SSLException.class);check(calls[0]==3);check(dir.list().length==0);
  calls[0]=0;fails(()->UpdateTransfer.transfer(URL,128,dir,url->{calls[0]++;throw new SSLHandshakeException("bad certificate");}),SSLHandshakeException.class);check(calls[0]==1);check(dir.list().length==0);
  calls[0]=0;fails(()->UpdateTransfer.transfer(URL,128,dir,url->{calls[0]++;try{return new Fake(new ByteArrayInputStream(new byte[129]),200);}catch(Exception e){throw new IOException(e);}}),UpdateTransfer.SizeError.class);check(calls[0]==1);check(dir.list().length==0);
  calls[0]=0;fails(()->UpdateTransfer.transfer(URL,128,dir,url->{calls[0]++;try{return new Fake(new ByteArrayInputStream(new byte[80]),200);}catch(Exception e){throw new IOException(e);}}),EOFException.class);check(calls[0]==3);check(dir.list().length==0);
  calls[0]=0;fails(()->UpdateTransfer.transfer(URL,128,dir,url->{calls[0]++;try{return new Fake(new ByteArrayInputStream(data),302);}catch(Exception e){throw new IOException(e);}}),IOException.class);check(calls[0]==1);check(dir.list().length==0);
  fails(()->UpdateTransfer.transfer(URL.replace("https:","http:"),128,dir,url->{throw new AssertionError();}),IllegalArgumentException.class);
  fails(()->UpdateTransfer.transfer(URL.replace("raw.githubusercontent.com","example.com"),128,dir,url->{throw new AssertionError();}),IllegalArgumentException.class);
  fails(()->UpdateTransfer.transfer(URL+"?redirect=1",128,dir,url->{throw new AssertionError();}),IllegalArgumentException.class);
  fails(()->UpdateTransfer.transfer(URL,51L*1024*1024,dir,url->{throw new AssertionError();}),IllegalArgumentException.class);
  try(SSLSocket socket=(SSLSocket)new UpdateTransfer.CompatibleTLS().createSocket()){
   check(Arrays.equals(socket.getEnabledProtocols(),new String[]{"TLSv1.2"}));check(Arrays.stream(socket.getEnabledCipherSuites()).allMatch(c->c.contains("ECDHE")&&(c.contains("CHACHA20")||c.contains("GCM"))));
  }
  dir.delete();System.out.println("PASS: "+tests+" checks; TLS/read retries, fresh files, cleanup, limits, no redirects, system trust and TLS1.2 AEAD configuration.");
 }
}
