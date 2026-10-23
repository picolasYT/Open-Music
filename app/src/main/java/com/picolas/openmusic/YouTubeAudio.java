package com.picolas.openmusic;
import android.content.Context;
import android.net.Uri;
import androidx.media3.datasource.*;
import java.io.*;
import java.util.*;
/** Open Music resolves stable IDs on Media3's loader thread. */
@androidx.annotation.OptIn(markerClass=androidx.media3.common.util.UnstableApi.class)
final class YouTubeAudio {
 private static final LinkedHashMap<String,AudioAddress> cache=new LinkedHashMap<>(16,.75f,true);
 interface Lookup {AudioAddress resolve(String id)throws IOException;}
 static DataSource.Factory factory(Context context,DataSource.Factory upstream){return factory(upstream,YouTubeAudio::resolve);}
 static DataSource.Factory factory(DataSource.Factory upstream,Lookup lookup){return new ResolvingDataSource.Factory(upstream,spec->{
  if(!"openmusic-youtube".equals(spec.uri.getScheme()))return spec;
  String id=spec.uri.getLastPathSegment();if(!YouTubeProvider.validId(id))throw new IOException("OpenMusic: Identificador de YouTube no válido.");
  if(Thread.currentThread().isInterrupted())throw new InterruptedIOException("Cancelado");
  AudioAddress address=lookup.resolve(id);Map<String,String> headers=new HashMap<>(spec.httpRequestHeaders);headers.putAll(address.headers);
  if(Thread.currentThread().isInterrupted())throw new InterruptedIOException("Cancelado");
  return spec.withUri(Uri.parse(address.url)).withRequestHeaders(headers);
 });}
 static AudioAddress resolve(String id)throws IOException{
  long now=System.currentTimeMillis()/1000;synchronized(cache){AudioAddress old=cache.get(id);if(old!=null&&old.expires>now+60)return old;}
  AudioAddress value=NewPipeBackend.resolve(id);synchronized(cache){cache.put(id,value);if(cache.size()>100)cache.remove(cache.keySet().iterator().next());}return value;
 }
 static void invalidate(String id){synchronized(cache){cache.remove(id);}}
 static String userMessage(Throwable error){
  for(Throwable cause=error;cause!=null;cause=cause.getCause()){
   String message=cause.getMessage();if(message!=null&&message.startsWith("OpenMusic:"))return message.substring(10).trim();
   if(cause instanceof HttpDataSource.InvalidContentTypeException)return "El servidor devolvió una página de error en lugar de audio. Revisá la red o probá otra fuente.";
   if(cause instanceof HttpDataSource.InvalidResponseCodeException){int code=((HttpDataSource.InvalidResponseCodeException)cause).responseCode;return code==401||code==403?"La fuente rechazó el enlace de audio (HTTP "+code+"). Tocá Play para obtener uno nuevo.":"El servidor de audio respondió HTTP "+code+". Intentá más tarde.";}
   if(cause instanceof javax.net.ssl.SSLException)return "No se pudo verificar la conexión segura. Revisá la fecha del teléfono y la red.";
   if(cause instanceof java.net.UnknownHostException||cause instanceof java.net.SocketTimeoutException)return "No se pudo conectar con el servidor de audio. Revisá tu conexión.";
  }return "No se pudo reproducir. Revisá la conexión o probá otra canción.";
 }
}
