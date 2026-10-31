package cu.hogarplaya;
import java.io.*;
import java.net.*;
import java.security.cert.CertificateException;
import java.util.*;
import javax.net.ssl.*;

/** Descarga acotada: conexión nueva por intento, TLS 1.2 y confianza del sistema. */
public final class UpdateTransfer {
    interface Connector { HttpsURLConnection open(URL url)throws IOException; }
    static final long MAX=50L*1024*1024;
    static final class SizeError extends IOException {SizeError(String m){super(m);}}
    public static final class CompatibleTLS extends SSLSocketFactory {
        final SSLSocketFactory delegate;
        public CompatibleTLS()throws Exception {SSLContext c=SSLContext.getInstance("TLSv1.2");c.init(null,null,null);delegate=c.getSocketFactory();}
        Socket configure(Socket socket)throws IOException {
            if(!(socket instanceof SSLSocket))throw new SSLException("TLS no disponible.");
            SSLSocket s=(SSLSocket)socket;s.setEnabledProtocols(new String[]{"TLSv1.2"});
            List<String> enabled=Arrays.asList(s.getEnabledCipherSuites());List<String> chosen=new ArrayList<>();
            for(String cipher:enabled)if(cipher.contains("CHACHA20")&&cipher.contains("ECDHE"))chosen.add(cipher);
            for(String cipher:enabled)if(cipher.contains("GCM")&&cipher.contains("ECDHE"))chosen.add(cipher);
            if(chosen.isEmpty())throw new SSLException("No hay cifrado TLS compatible.");
            s.setEnabledCipherSuites(chosen.toArray(new String[0]));return s;
        }
        public String[] getDefaultCipherSuites(){return delegate.getDefaultCipherSuites();}
        public String[] getSupportedCipherSuites(){return delegate.getSupportedCipherSuites();}
        public Socket createSocket()throws IOException{return configure(delegate.createSocket());}
        public Socket createSocket(Socket s,String h,int p,boolean close)throws IOException{return configure(delegate.createSocket(s,h,p,close));}
        public Socket createSocket(String h,int p)throws IOException{return configure(delegate.createSocket(h,p));}
        public Socket createSocket(String h,int p,InetAddress l,int lp)throws IOException{return configure(delegate.createSocket(h,p,l,lp));}
        public Socket createSocket(InetAddress h,int p)throws IOException{return configure(delegate.createSocket(h,p));}
        public Socket createSocket(InetAddress h,int p,InetAddress l,int lp)throws IOException{return configure(delegate.createSocket(h,p,l,lp));}
    }
    static boolean retryable(IOException e){
        if(e instanceof SizeError||e instanceof SSLHandshakeException||e instanceof SSLPeerUnverifiedException)return false;
        for(Throwable t=e;t!=null;t=t.getCause())if(t instanceof CertificateException)return false;
        return e instanceof SSLException||e instanceof EOFException||e instanceof SocketException||e instanceof java.net.SocketTimeoutException;
    }
    public static File download(String address,long expected,File directory)throws Exception {
        return transfer(address,expected,directory,url->{
            HttpsURLConnection c=(HttpsURLConnection)url.openConnection();
            try{c.setSSLSocketFactory(new CompatibleTLS());}catch(Exception e){c.disconnect();throw new IOException("No se pudo preparar TLS.",e);}return c;
        });
    }
    static File transfer(String address,long expected,File directory,Connector connector)throws Exception {
        URL url=new URL(address);
        if(!"https".equals(url.getProtocol())||!"raw.githubusercontent.com".equals(url.getHost())||url.getPort()!=-1||url.getUserInfo()!=null||url.getQuery()!=null||url.getRef()!=null||!url.getPath().matches("/Arlbode/google_sheet_bill/actualizaciones-android/Hogar_finanzas_[0-9]+\\.[0-9]+\\.[0-9]+\\.apk"))throw new IllegalArgumentException("Ruta de actualización no autorizada.");
        if(expected<100||expected>MAX)throw new IllegalArgumentException("Tamaño de actualización inválido.");
        for(int attempt=0;attempt<3;attempt++){
            File temp=File.createTempFile("download-compatible-",".apk",directory);HttpsURLConnection c=null;boolean complete=false;
            try {
                c=connector.open(url);c.setInstanceFollowRedirects(false);c.setUseCaches(false);c.setConnectTimeout(15000);c.setReadTimeout(25000);
                c.setRequestProperty("Accept","application/octet-stream");c.setRequestProperty("Accept-Encoding","identity");c.setRequestProperty("Connection","close");c.setRequestProperty("Cache-Control","no-cache");
                if(c.getResponseCode()!=200)throw new IOException("El servidor no entregó la actualización (HTTP "+c.getResponseCode()+").");
                long count=0;
                try(InputStream in=c.getInputStream();FileOutputStream out=new FileOutputStream(temp)){
                    byte[] buffer=new byte[32768];int n;
                    while((n=in.read(buffer))!=-1){count+=n;if(count>expected||count>MAX)throw new SizeError("La descarga supera el tamaño publicado.");out.write(buffer,0,n);}
                    if(count!=expected)throw new EOFException("Descarga incompleta.");out.getFD().sync();
                }
                complete=true;return temp;
            }catch(IOException e){if(attempt==2||!retryable(e))throw e;}
            finally{if(c!=null)c.disconnect();if(!complete)temp.delete();}
        }
        throw new IOException("No se pudo descargar la actualización.");
    }
}
