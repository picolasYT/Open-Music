package com.picolas.openmusic;
import org.json.*;
import org.junit.Test;
import static org.junit.Assert.*;
public class BackupPolicyTest {
    private JSONObject song()throws Exception{return new JSONObject().put("provider","audius").put("id","abc123").put("title","A song").put("artist","An artist");}
    private JSONObject backup(JSONObject t)throws Exception{return new JSONObject().put("format","open-music-backup").put("version",1).put("favorites",new JSONArray().put(t)).put("playlists",new JSONArray());}
    @Test public void importedRightsAndUrisAreNeverTrusted()throws Exception{
        Track t=LibraryBackup.parse(backup(song().put("stream","https://evil.invalid/audio").put("downloadAllowed",true).put("download","https://evil.invalid/audio").put("local","file:///data/secret").put("artwork","content://private")).toString()).favorites.get(0);
        assertFalse(t.playable());assertFalse(t.downloadAllowed);assertEquals("",t.local);assertEquals("",t.stream);assertEquals("",t.download);assertEquals("",t.artwork);
    }
    @Test public void rejectsForeignFormatAndLocalIdentifiers()throws Exception{
        for(JSONObject invalid:new JSONObject[]{backup(song()).put("version",2),backup(song().put("provider","local").put("id","content://private")),backup(song().put("title",42))}){
            try{LibraryBackup.parse(invalid.toString());fail("Must reject malformed backup");}catch(JSONException expected){}
        }
    }
    @Test public void validatesRecordLimitBeforeAllocatingTracks()throws Exception{
        JSONArray many=new JSONArray();for(int i=0;i<5001;i++)many.put(song());
        try{LibraryBackup.parse(backup(song()).put("favorites",many).toString());fail("Must bound imported entries");}catch(JSONException expected){}
    }
}
