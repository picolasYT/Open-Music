package com.picolas.openmusic;
import org.junit.Test;
import org.json.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import static org.junit.Assert.*;
public class YouTubeSearchTest {
    @Test public void parsesRealMiloAndTruenoSearchMetadata()throws Exception{
        String fixture;try(var in=getClass().getResourceAsStream("/youtube-music.json")){assertNotNull(in);fixture=new String(in.readAllBytes(),StandardCharsets.UTF_8);}
        List<Track> tracks=YouTubeProvider.parseHtml("<script>var ytInitialData = "+fixture+"; unrelated()</script>");
        assertTrue(tracks.size()>=6);assertTrue(tracks.stream().anyMatch(t->t.title.toLowerCase(Locale.ROOT).contains("milo")));assertTrue(tracks.stream().anyMatch(t->t.title.toLowerCase(Locale.ROOT).contains("trueno")));
        for(Track t:tracks){assertTrue(t.youtube());assertFalse(t.playable());assertFalse(t.downloadAllowed);assertTrue(t.web.startsWith("https://www.youtube.com/watch?v="));}
    }
    @Test public void deduplicatesVideosAndIgnoresSponsoredOrMalformedEntries()throws Exception{
        JSONObject renderer=new JSONObject().put("videoId","aBSkvI0CkgU").put("title",new JSONObject().put("simpleText","Milo } \"quoted\""));
        JSONArray a=new JSONArray().put(new JSONObject().put("videoRenderer",renderer)).put(new JSONObject().put("compactVideoRenderer",renderer)).put(new JSONObject().put("adSlotRenderer",new JSONObject().put("videoRenderer",renderer))).put(new JSONObject().put("videoRenderer",new JSONObject().put("videoId","<script>").put("title",new JSONObject().put("simpleText","bad"))));
        List<Track> tracks=YouTubeProvider.parseHtml("window[\"ytInitialData\"] = "+new JSONObject().put("contents",a)+";</script>");assertEquals(1,tracks.size());assertEquals("Milo } \"quoted\"",tracks.get(0).title);
    }
    @Test public void verificationPageRaisesAnActionableError()throws Exception{
        try{YouTubeProvider.parseHtml("<html>Confirm you're not a bot</html>");fail();}catch(java.io.IOException expected){assertTrue(expected.getMessage().contains("Abrir búsqueda"));}
    }
    @Test public void youtubeDlImportDiscardsAllExtractedCapabilities()throws Exception{
        JSONObject input=new JSONObject().put("extractor_key","Youtube").put("id","aBSkvI0CkgU").put("title","Rara vez").put("url","https://untrusted.invalid/audio").put("local","file:///private/song").put("downloadAllowed",true).put("formats",new JSONArray().put(new JSONObject().put("url","https://untrusted.invalid/audio")));
        Track t=YoutubeDlImport.parse(input.toString()).get(0);assertTrue(t.youtube());assertEquals("",t.stream);assertEquals("",t.download);assertEquals("",t.local);assertFalse(t.downloadAllowed);
        input.put("extractor_key","Generic");try{YoutubeDlImport.parse(input.toString());fail();}catch(JSONException expected){}
    }
    @Test public void backupRetainsYoutubeIdentityWithoutGrantingAudioAccess()throws Exception{
        JSONObject t=new JSONObject().put("provider","youtube").put("id","aBSkvI0CkgU").put("title","Rara vez").put("stream","https://untrusted.invalid/audio");
        String backup=new JSONObject().put("format","open-music-backup").put("version",1).put("favorites",new JSONArray().put(t)).put("playlists",new JSONArray()).toString();Track restored=LibraryBackup.parse(backup).favorites.get(0);assertTrue(restored.youtube());assertFalse(restored.playable());assertFalse(restored.downloadAllowed);
    }
}
