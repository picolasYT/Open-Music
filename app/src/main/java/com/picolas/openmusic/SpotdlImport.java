package com.picolas.openmusic;
import org.json.*;
import java.util.*;
/** Consumes spotDL's save output, keeping metadata separate from audio availability. */
public final class SpotdlImport {
    public static List<Track> parse(String raw)throws JSONException{
        Object root=new JSONTokener(raw).nextValue();JSONArray a;
        if(root instanceof JSONArray)a=(JSONArray)root;else if(root instanceof JSONObject&&((JSONObject)root).has("songs"))a=((JSONObject)root).getJSONArray("songs");else throw new JSONException("Se esperaba un archivo .spotdl generado con spotdl save");
        if(a.length()>5000)throw new JSONException("Máximo 5000 canciones por importación");List<Track> out=new ArrayList<>();
        for(int i=0;i<a.length();i++){JSONObject j=a.getJSONObject(i);Track t=new Track();t.provider="spotdl";t.title=j.optString("name");JSONArray artists=j.optJSONArray("artists");if(artists!=null){List<String> names=new ArrayList<>();for(int n=0;n<artists.length();n++)names.add(artists.optString(n));t.artist=String.join(", ",names);}t.album=j.optString("album_name");t.duration=j.optInt("duration");t.artwork=Net.https(j.optString("cover_url"));t.web=Net.https(j.optString("url"));t.id=j.optString("song_id",t.web);if(t.id.isEmpty())t.id=Integer.toHexString((t.title+"|"+t.artist).hashCode());if(!t.title.isEmpty())out.add(t);}return out;
    }
}
