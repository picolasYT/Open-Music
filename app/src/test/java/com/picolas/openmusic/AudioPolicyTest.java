package com.picolas.openmusic;
import org.junit.Test;
import org.json.*;
import java.io.IOException;
import static org.junit.Assert.*;
public class AudioPolicyTest {
    private JSONObject source()throws Exception{return new JSONObject().put("url","https://rr1.googlevideo.com/videoplayback?expire=9999999999").put("ext","m4a").put("expires",9999999999L).put("headers",new JSONObject().put("User-Agent","youtube-dl").put("Cookie","private"));}
    @Test public void signedAddressesAreEphemeralAndDropCredentials()throws Exception{AudioAddress a=AudioAddress.parse(source().toString(),100);assertEquals(1000,a.expires);assertEquals("youtube-dl",a.headers.get("User-Agent"));assertFalse(a.headers.containsKey("Cookie"));}
    @Test public void rejectsForeignSourcesHeaderInjectionAndExpiredLinks()throws Exception{
        for(JSONObject j:new JSONObject[]{source().put("url","https://googlevideo.com.evil.invalid/audio"),source().put("url","http://rr1.googlevideo.com/audio"),source().put("expires",101),source().put("headers",new JSONObject().put("User-Agent","bad\r\nInjected: true"))})try{AudioAddress.parse(j.toString(),100);fail("Invalid source must be rejected");}catch(IOException expected){}
    }
    @Test public void importedYoutubeIdentityCanResolveWithoutGrantingDownloads()throws Exception{Track t=YouTubeProvider.metadata(new JSONObject().put("id","aBSkvI0CkgU").put("title","A song"));assertTrue(t.canPlay());assertFalse(t.playable());assertEquals("",t.stream);assertFalse(t.downloadAllowed);t.id="../../private";assertFalse(t.canPlay());}
}
