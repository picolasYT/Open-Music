package com.picolas.openmusic;
import java.util.*;
import org.json.*;
/** Optional community-wrapper bridge. Metadata is never misrepresented as playable audio. */
public final class SpotifyMetadataProvider implements MusicProvider {
    private final String base;
    public SpotifyMetadataProvider(String base){if(!base.startsWith("https://"))throw new IllegalArgumentException("El puente requiere HTTPS");this.base=base.replaceAll("/+$","");}
    public String name(){return "Spotify (metadatos)";}
    public List<Track> search(String q)throws Exception{JSONObject j=Net.json(base+"/search?q="+Net.enc(q));JSONArray a=j.optJSONArray("tracks");if(a==null)throw new Exception(j.optString("error","Respuesta inválida"));List<Track> out=new ArrayList<>();for(int i=0;i<a.length();i++){Track t=Track.from(a.getJSONObject(i));t.provider="spotify";t.stream="";t.download="";t.local="";t.downloadAllowed=false;out.add(t);}return out;}
    public List<Track> discover(String genre){return Collections.emptyList();}
    public Track refresh(Track t){return t;}
}
