package com.picolas.openmusic;
import org.json.*;
import java.util.*;
/** Jamendo requires the app owner's own client id, configurable in Settings. */
public final class JamendoProvider implements MusicProvider {
    private final String client;
    public JamendoProvider(String client){this.client=client;}
    public String name(){return "Jamendo";}
    private List<Track> request(String extra)throws Exception{
        JSONObject j=Net.json("https://api.jamendo.com/v3.0/tracks/?client_id="+Net.enc(client)+"&format=json&limit=40&audioformat=mp32&include=musicinfo&"+extra);
        if(!j.getJSONObject("headers").optString("status").equals("success"))throw new Exception("Jamendo: revisá tu client_id en Ajustes");
        JSONArray a=j.getJSONArray("results");List<Track> r=new ArrayList<>();for(int i=0;i<a.length();i++) {JSONObject v=a.getJSONObject(i);Track t=new Track();t.provider="jamendo";t.id=v.getString("id");t.title=v.optString("name");t.artist=v.optString("artist_name");t.album=v.optString("album_name");t.artwork=Net.https(v.optString("image"));t.stream=Net.https(v.optString("audio"));t.duration=v.optInt("duration");t.license=Net.https(v.optString("license_ccurl"));t.web=Net.https(v.optString("shareurl"));t.downloadAllowed=v.optBoolean("audiodownload_allowed");t.download=t.downloadAllowed?Net.https(v.optString("audiodownload")):"";if(t.playable())r.add(t);}return r;
    }
    public List<Track> search(String q)throws Exception{return request("search="+Net.enc(q));}
    public List<Track> discover(String genre)throws Exception{return request("order=popularity_total"+(genre.isEmpty()?"":"&fuzzytags="+Net.enc(genre)));}
    public Track refresh(Track t)throws Exception{List<Track> r=request("id="+Net.enc(t.id));if(r.isEmpty())throw new Exception("Canción no disponible en Jamendo");return r.get(0);}
}
