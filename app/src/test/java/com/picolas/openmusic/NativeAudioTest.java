package com.picolas.openmusic;
import org.junit.Test;
import org.schabi.newpipe.extractor.MediaFormat;
import org.schabi.newpipe.extractor.stream.*;
import java.io.IOException;
import java.util.*;
import static org.junit.Assert.*;
public class NativeAudioTest {
 private AudioStream stream(String url,MediaFormat f,int bitrate){return new AudioStream.Builder().setId("test").setContent(url,true).setMediaFormat(f).setAverageBitrate(bitrate).build();}
 @Test public void prefersAacAndClampsExpiry()throws Exception{
  AudioAddress a=NewPipeBackend.choose(List.of(stream("https://r.googlevideo.com/opus?expire=9999999",MediaFormat.WEBMA_OPUS,160),stream("https://r.googlevideo.com/low?expire=9999999",MediaFormat.M4A,48),stream("https://r.googlevideo.com/aac?expire=9999999",MediaFormat.M4A,128)),100);assertTrue(a.url.contains("/aac?"));assertEquals(1000,a.expires);assertFalse(a.headers.containsKey("Cookie"));
 }
 @Test public void invalidCandidateCannotConcealFallback()throws Exception{AudioAddress a=NewPipeBackend.choose(List.of(stream("https://foreign.invalid/audio",MediaFormat.M4A,128),stream("https://r.googlevideo.com/valid",MediaFormat.WEBMA_OPUS,160)),100);assertTrue(a.url.endsWith("/valid"));}
 @Test public void rejectsExpiredAndManifestCandidates()throws Exception{
  AudioStream manifest=new AudioStream.Builder().setId("dash").setContent("<MPD/>",false).setMediaFormat(MediaFormat.M4A).setDeliveryMethod(DeliveryMethod.DASH).build();
  try{NewPipeBackend.choose(List.of(manifest,stream("https://r.googlevideo.com/expired?expire=101",MediaFormat.M4A,128)),100);fail("Cannot fabricate audio");}catch(IOException expected){assertTrue(expected.getMessage().startsWith("OpenMusic:"));}
 }
 @Test public void rejectsErrorPagesWithoutBlockingAudioOrHls(){for(String t:List.of("text/html","Text/HTML; charset=UTF-8","application/json","application/xhtml+xml"))assertFalse(AudioContent.accepted(t));for(String t:List.of("audio/mp4","audio/webm","application/octet-stream","application/vnd.apple.mpegurl"))assertTrue(AudioContent.accepted(t));assertTrue(AudioContent.accepted(null));}
}
