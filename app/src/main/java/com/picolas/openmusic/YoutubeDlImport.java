package com.picolas.openmusic;
import org.json.*;
import java.util.*;
/** Imports youtube-dl --dump-single-json metadata. Never imports extracted media URLs. */
public final class YoutubeDlImport {
    public static List<Track> parse(String raw)throws JSONException{
        JSONObject root=new JSONObject(raw);JSONArray entries=root.optJSONArray("entries");if(entries==null)entries=new JSONArray().put(root);
        if(entries.length()>5000)throw new JSONException("Máximo 5000 entradas");LinkedHashMap<String,Track> out=new LinkedHashMap<>();
        for(int i=0;i<entries.length();i++){JSONObject j=entries.optJSONObject(i);if(j==null)continue;String id=j.optString("id");if(!YouTubeProvider.validId(id)||j.optString("title").isEmpty())continue;
            String extractor=j.optString("extractor_key",j.optString("ie_key",j.optString("extractor")));if(!extractor.toLowerCase(Locale.ROOT).startsWith("youtube"))throw new JSONException("Solo se admiten metadatos YouTube");
            Track t=YouTubeProvider.metadata(j);out.putIfAbsent(t.key(),t);
        }if(out.isEmpty())throw new JSONException("No hay videos válidos en este JSON de youtube-dl");return new ArrayList<>(out.values());
    }
}
