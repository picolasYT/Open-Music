package com.picolas.openmusic;
import okhttp3.*;
import org.json.*;
import org.schabi.newpipe.extractor.*;
import org.schabi.newpipe.extractor.downloader.Downloader;
import org.schabi.newpipe.extractor.exceptions.*;
import org.schabi.newpipe.extractor.stream.*;
import java.io.*;
import java.net.URI;
import java.util.*;
import java.util.concurrent.TimeUnit;
/** Open Music's native adapter. Extractor: Team NewPipe, GPL-3.0-or-later. */
final class NewPipeBackend {
 static final String VERSION="NewPipeExtractor 0.26.5";
 static final String USER_AGENT="Mozilla/5.0 (Linux; Android 12) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/131.0.0.0 Mobile Safari/537.36";
 private static final OkHttpClient HTTP=Net.HTTP.newBuilder().callTimeout(25,TimeUnit.SECONDS).build();
 private static boolean initialized;
 static synchronized void initialize(){
  if(initialized)return;
  NewPipe.init(new Downloader(){
   @Override public org.schabi.newpipe.extractor.downloader.Response execute(org.schabi.newpipe.extractor.downloader.Request request)throws IOException,ReCaptchaException{
    Request.Builder b=new Request.Builder().url(request.url()).header("User-Agent",USER_AGENT);
    for(Map.Entry<String,List<String>> h:request.headers().entrySet()){b.removeHeader(h.getKey());for(String value:h.getValue())b.addHeader(h.getKey(),value);}
    byte[] data=request.dataToSend();RequestBody body=data==null?null:RequestBody.create(data,MediaType.parse("application/json"));
    if(body==null&&(request.httpMethod().equals("POST")||request.httpMethod().equals("PUT")))body=RequestBody.create(new byte[0],null);
    b.method(request.httpMethod(),body);
    try(Response r=HTTP.newCall(b.build()).execute()){
     if(r.code()==429)throw new ReCaptchaException("La fuente limitó las solicitudes.",request.url());
     if(r.code()==401||r.code()==403)throw new IOException("OpenMusic: La fuente requiere acceso adicional o rechazó la conexión (HTTP "+r.code()+").");
     if(!r.isSuccessful())throw new IOException("OpenMusic: La fuente respondió HTTP "+r.code()+". Intentá más tarde.");
     String text="";
     if(r.body()!=null){try(InputStream in=r.body().byteStream()){ByteArrayOutputStream out=new ByteArrayOutputStream();byte[] buffer=new byte[8192];int n;while((n=in.read(buffer))!=-1){if(out.size()+n>16*1024*1024)throw new IOException("OpenMusic: Respuesta del proveedor demasiado grande.");out.write(buffer,0,n);}text=out.toString("UTF-8");}}
     return new org.schabi.newpipe.extractor.downloader.Response(r.code(),r.message(),r.headers().toMultimap(),text,r.request().url().toString());
    }
   }
  });initialized=true;
 }
 static AudioAddress resolve(String id)throws IOException{
  if(!YouTubeProvider.validId(id))throw new IOException("OpenMusic: Identificador de YouTube no válido.");initialize();
  try{
   StreamExtractor e=ServiceList.YouTube.getStreamExtractor("https://www.youtube.com/watch?v="+id);e.fetchPage();
   ContentAvailability availability=e.getContentAvailability();
   if(e.getAgeLimit()>0||availability==ContentAvailability.PAID||availability==ContentAvailability.MEMBERSHIP||availability==ContentAvailability.UPCOMING||e.getPrivacy()==StreamExtractor.Privacy.PRIVATE||e.getPrivacy()==StreamExtractor.Privacy.INTERNAL)throw new IOException("OpenMusic: Esta pista requiere acceso adicional o no está disponible.");
   if(e.getStreamType()!=StreamType.VIDEO_STREAM&&e.getStreamType()!=StreamType.AUDIO_STREAM)throw new IOException("OpenMusic: Esta fuente es una transmisión en vivo. Elegí una canción publicada.");
   return choose(e.getAudioStreams(),System.currentTimeMillis()/1000);
  }catch(Exception e){throw sourceError(e);}
 }
 static AudioAddress choose(List<AudioStream> streams,long now)throws IOException{
  List<AudioStream> ordered=new ArrayList<>(streams);ordered.sort(Comparator.comparingInt((AudioStream a)->a.getFormat()==MediaFormat.M4A?1:0).thenComparingInt(AudioStream::getAverageBitrate).reversed());
  for(AudioStream a:ordered){
   if(!a.isUrl()||a.getDeliveryMethod()!=DeliveryMethod.PROGRESSIVE_HTTP)continue;
   String ext=a.getFormat()==MediaFormat.M4A?"m4a":a.getFormat()==MediaFormat.WEBMA_OPUS||a.getFormat()==MediaFormat.WEBMA?"webm":"";if(ext.isEmpty())continue;
   try{return AudioAddress.parse(new JSONObject().put("url",a.getContent()).put("ext",ext).put("expires",expiry(a.getContent(),now)).put("headers",new JSONObject().put("User-Agent",USER_AGENT)).toString(),now);}catch(IOException ignored){}catch(JSONException e){throw new IOException("OpenMusic: No se pudo interpretar el formato de audio.");}
  }
  throw new IOException("OpenMusic: La fuente no entregó un formato de audio compatible. Probá otra canción.");
 }
 private static long expiry(String url,long now){try{String q=new URI(url).getRawQuery();if(q!=null)for(String part:q.split("&"))if(part.startsWith("expire="))return Math.min(now+900,Long.parseLong(part.substring(7)));}catch(Exception ignored){}return now+900;}
 static List<Track> search(String query)throws Exception{
  initialize();var e=ServiceList.YouTube.getSearchExtractor(query,Collections.singletonList("videos"),"");e.fetchPage();List<Track> out=new ArrayList<>();
  for(InfoItem item:e.getInitialPage().getItems()){
   if(!(item instanceof StreamInfoItem))continue;StreamInfoItem s=(StreamInfoItem)item;if(s.getStreamType()!=StreamType.VIDEO_STREAM&&s.getStreamType()!=StreamType.AUDIO_STREAM)continue;
   String id=ServiceList.YouTube.getStreamLHFactory().getId(s.getUrl());if(!YouTubeProvider.validId(id))continue;
   out.add(YouTubeProvider.metadata(new JSONObject().put("id",id).put("title",s.getName()).put("artist",s.getUploaderName()).put("duration",Math.max(0,s.getDuration()))));if(out.size()>=30)break;
  }return out;
 }
 private static IOException sourceError(Exception error){
  for(Throwable t=error;t!=null;t=t.getCause()){
   if(t instanceof IOException&&t.getMessage()!=null&&t.getMessage().startsWith("OpenMusic:"))return new IOException(t.getMessage());
   String kind=t.getClass().getSimpleName();
   if(kind.contains("Captcha")||kind.contains("SignIn")||kind.contains("AgeRestricted")||kind.contains("Private")||kind.contains("Geographic")||kind.contains("Paid"))return new IOException("OpenMusic: YouTube solicita verificación o no permite acceder a esta pista.");
   if(t instanceof javax.net.ssl.SSLException)return new IOException("OpenMusic: No se pudo verificar la conexión segura. Revisá la fecha del teléfono y la red.");
   if(t instanceof java.net.SocketTimeoutException||t instanceof java.net.UnknownHostException)return new IOException("OpenMusic: No se pudo conectar con YouTube. Revisá tu conexión.");
  }return new IOException("OpenMusic: El extractor no pudo leer esta pista. Probá otra canción o una actualización de Open Music.");
 }
}
