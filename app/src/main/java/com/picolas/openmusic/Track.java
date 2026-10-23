package com.picolas.openmusic;

import org.json.*;
import java.util.*;

/** Provider-neutral metadata. Streaming and download rights are separate capabilities. */
public final class Track {
    public String id="", provider="", title="", artist="", album="", artwork="", stream="", download="", local="", genre="", license="", web="";
    public int duration;
    public boolean downloadAllowed;
    public String key() { return provider+":"+id; }
    public boolean youtube() { return provider.equals("youtube") && YouTubeProvider.validId(id); }
    public boolean canPlay() { return youtube() || playable(); }
    public boolean playable() { return !local.isEmpty() || (!stream.isEmpty() && stream.startsWith("https://")); }
    public JSONObject json() {
        JSONObject j=new JSONObject();
        try { j.put("id",id).put("provider",provider).put("title",title).put("artist",artist).put("album",album).put("artwork",artwork).put("stream",stream).put("download",download).put("local",local).put("genre",genre).put("license",license).put("web",web).put("duration",duration).put("downloadAllowed",downloadAllowed); } catch(JSONException ignored) {}
        return j;
    }
    public static Track from(JSONObject j) {
        Track t=new Track(); t.id=j.optString("id"); t.provider=j.optString("provider"); t.title=j.optString("title"); t.artist=j.optString("artist"); t.album=j.optString("album"); t.artwork=j.optString("artwork"); t.stream=j.optString("stream"); t.download=j.optString("download"); t.local=j.optString("local"); t.genre=j.optString("genre"); t.license=j.optString("license"); t.web=j.optString("web"); t.duration=j.optInt("duration"); t.downloadAllowed=j.optBoolean("downloadAllowed"); return t;
    }
    public static String encode(List<Track> list) { JSONArray a=new JSONArray(); for(Track t:list) a.put(t.json()); return a.toString(); }
    public static List<Track> decode(String s) { List<Track> out=new ArrayList<>(); try {JSONArray a=new JSONArray(s); for(int i=0;i<a.length();i++) out.add(from(a.getJSONObject(i)));} catch(JSONException ignored) {} return out; }
}
