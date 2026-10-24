package com.picolas.openmusic;
import org.junit.Test;
import org.schabi.newpipe.extractor.MediaFormat;
import org.schabi.newpipe.extractor.stream.*;
import java.io.IOException;
import java.util.*;
import static org.junit.Assert.*;
public class NativeVideoTest {
 private VideoStream stream(String url,String resolution,boolean only){return new VideoStream.Builder().setId("test").setContent(url,true).setMediaFormat(MediaFormat.MPEG_4).setResolution(resolution).setIsVideoOnly(only).build();}
 @Test public void selects720pAndSupportsSeparateAndEmbeddedAudio()throws Exception{
  for(boolean only:List.of(true,false)){
   AudioAddress result=NewPipeBackend.chooseVideo(List.of(stream("https://r.googlevideo.com/1080", "1080p",only),stream("https://r.googlevideo.com/360", "360p",only),stream("https://r.googlevideo.com/720?expire=9999999", "720p",only)),100);
   assertTrue(result.url.contains("/720?"));assertEquals(1000,result.expires);assertEquals(NewPipeBackend.USER_AGENT,result.headers.get("User-Agent"));
  }
 }
 @Test public void invalidOrExpiredCandidateDoesNotHideLowerQuality()throws Exception{
  AudioAddress result=NewPipeBackend.chooseVideo(List.of(stream("https://foreign.invalid/video","720p",true),stream("https://r.googlevideo.com/expired?expire=101","480p",true),stream("https://r.googlevideo.com/valid","360p",false)),100);
  assertTrue(result.url.endsWith("/valid"));
 }
 @Test public void failsClearlyWithoutCompatibleVideo()throws Exception{
  VideoStream manifest=new VideoStream.Builder().setId("dash").setContent("<MPD/>",false).setMediaFormat(MediaFormat.MPEG_4).setResolution("720p").setIsVideoOnly(true).setDeliveryMethod(DeliveryMethod.DASH).build();
  try{NewPipeBackend.chooseVideo(List.of(manifest),100);fail("Must not invent video");}catch(IOException e){assertTrue(e.getMessage().contains("modo Música"));}
 }
 @Test public void videoOnlyForValidYouTubeTracks(){
  Track t=new Track();t.provider="youtube";t.id="aaaaaaaaaaa";assertTrue(YouTubeVideo.available(t));t.provider="audius";assertFalse(YouTubeVideo.available(t));t.provider="youtube";t.id="invalid";assertFalse(YouTubeVideo.available(t));assertFalse(YouTubeVideo.available(null));
 }
}
