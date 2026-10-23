package com.picolas.openmusic;
import org.json.*;
import java.util.*;

/** Public Audius discovery/stream API; gated music never gets a playback URL. */
public final class AudiusProvider implements MusicProvider {
    static final String API="https://api.audius.co/v1";
    public String name(){return "Audius";}
    private JSONArray data(String path)throws Exception{return Net.json(API+path+(path.contains("?")?"&":"?")+"app_name=OpenMusic").getJSONArray("data");}
    static Track parse(JSONObject j){
        Track t=new Track();t.provider="audius";t.id=j.optString("id");t.title=j.optString("title");t.artist=j.optJSONObject("user")==null?"Artista":j.optJSONObject("user").optString("name");t.duration=j.optInt("duration");t.genre=j.optString("genre");
        JSONObject art=j.optJSONObject("artwork"); if(art!=null)t.artwork=Net.https(art.optString("480x480"));
        t.web="https://audius.co"+j.optString("permalink");t.license=j.optString("license");
        boolean allowed=j.optBoolean("is_streamable",false)&&!j.optBoolean("is_stream_gated",true)&&j.optBoolean("is_available",true)&&!j.optBoolean("is_delete");
        if(allowed)t.stream=API+"/tracks/"+t.id+"/stream?app_name=OpenMusic";
        // Only a provider-issued public download URL counts as download authorization.
        JSONObject d=j.optJSONObject("download");
        t.downloadAllowed=allowed&&j.optBoolean("is_downloadable")&&!j.optBoolean("is_download_gated",true)&&d!=null&&!Net.https(d.optString("url")).isEmpty();
        if(t.downloadAllowed)t.download=Net.https(d.optString("url"));
        return t;
    }
    private List<Track> tracks(JSONArray a)throws Exception{List<Track> r=new ArrayList<>();for(int i=0;i<a.length();i++){Track t=parse(a.getJSONObject(i));if(t.playable())r.add(t);}return r;}
    public List<Track> search(String q)throws Exception{return tracks(data("/tracks/search?query="+Net.enc(q)+"&limit=40"));}
    public List<Track> discover(String genre)throws Exception{if(genre.equals("Lo-Fi"))return search("lofi");return tracks(data("/tracks/trending?limit=30&time=week"+(genre.isEmpty()?"":"&genre="+Net.enc(genre))));}
    public Track refresh(Track t)throws Exception{JSONObject j=Net.json(API+"/tracks/"+Net.enc(t.id)+"?app_name=OpenMusic");Object d=j.get("data");return parse(d instanceof JSONArray?((JSONArray)d).getJSONObject(0):(JSONObject)d);}
    public static final class Collection { public String id,name,by,art,kind; }
    public List<Collection> collections(String q,boolean artists)throws Exception{
        JSONArray a=data((artists?"/users/search":"/playlists/search")+"?query="+Net.enc(q)+"&limit=25");List<Collection> out=new ArrayList<>();
        for(int i=0;i<a.length();i++){JSONObject j=a.getJSONObject(i);Collection c=new Collection();c.id=j.optString("id");c.kind=artists?"artist":"playlist";c.name=j.optString(artists?"name":"playlist_name");c.by=artists?j.optString("handle"):(j.optJSONObject("user")==null?"Audius":j.getJSONObject("user").optString("name"));JSONObject art=j.optJSONObject(artists?"profile_picture":"artwork");c.art=art==null?"":Net.https(art.optString("480x480"));out.add(c);}return out;
    }
    public List<Track> collection(Collection c)throws Exception{return tracks(data((c.kind.equals("artist")?"/users/":"/playlists/")+c.id+"/tracks?limit=50"));}
}
