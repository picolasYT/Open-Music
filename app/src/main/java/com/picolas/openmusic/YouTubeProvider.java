package com.picolas.openmusic;

import okhttp3.*;
import org.json.*;
import java.io.*;
import java.util.*;
import java.util.regex.*;
import java.util.concurrent.TimeUnit;

/** Android adaptation of talmobi/yt-search's public ytInitialData search parser (MIT).
 * Metadata fallback; native playback is resolved independently by Open Music. */
public final class YouTubeProvider implements MusicProvider {
    private static final Pattern INITIAL=Pattern.compile("(?:var\\s+)?ytInitialData\\s*[=]\\s*|window\\[\"ytInitialData\"\\]\\s*=\\s*");
    private static final OkHttpClient HTTP=Net.HTTP.newBuilder().callTimeout(18,TimeUnit.SECONDS).build();
    private final String bridge;
    public YouTubeProvider(){this("");}
    public YouTubeProvider(String bridge){this.bridge=bridge;}
    public String name(){return "YouTube";}
    public List<Track> search(String query)throws Exception{
        if(query.trim().length()<2||query.length()>160)throw new IOException("Usá entre 2 y 160 caracteres");
        if(!bridge.isEmpty()){
            try{JSONObject j=Net.json(bridge+"/youtube/search?q="+Net.enc(query));List<Track> out=new ArrayList<>();JSONArray a=j.getJSONArray("tracks");for(int i=0;i<Math.min(a.length(),30);i++)out.add(metadata(a.getJSONObject(i)));if(!out.isEmpty())return out;}catch(Exception ignored){/* Direct public search remains available. */}
        }
        try{List<Track> found=NewPipeBackend.search(query);if(!found.isEmpty())return found;}catch(Exception ignored){/* Independent HTML fallback. */}
        Request req=new Request.Builder().url("https://www.youtube.com/results?search_query="+Net.enc(query)+"&hl=es&gl=AR")
            .header("User-Agent","Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/131.0.0.0 Safari/537.36 OpenMusic/1.2")
            .header("Accept","text/html").header("Accept-Language","es-AR,es;q=0.9").build();
        try(Response r=HTTP.newCall(req).execute()){
            if(!r.isSuccessful()||r.body()==null)throw new IOException("YouTube no respondió (HTTP "+r.code()+")");
            ByteArrayOutputStream bytes=new ByteArrayOutputStream();try(InputStream in=r.body().byteStream()){byte[] b=new byte[8192];int n;while((n=in.read(b))!=-1){if(bytes.size()+n>6*1024*1024)throw new IOException("Respuesta de YouTube demasiado grande");bytes.write(b,0,n);}}
            return parseHtml(bytes.toString("UTF-8"));
        }
    }
    public List<Track> discover(String genre)throws Exception{return search(genre.isEmpty()?"música argentina":genre);}
    public Track refresh(Track t)throws Exception{if(!t.youtube())throw new IOException("Video inválido");return t;}
    public static List<Track> parseHtml(String html)throws Exception{
        Matcher m=INITIAL.matcher(html);JSONObject json=null;
        while(m.find()){int pos=m.end();try{Object value=new JSONTokener(html.substring(pos)).nextValue();if(value instanceof JSONObject){json=(JSONObject)value;break;}}catch(JSONException ignored){}}
        if(json==null)throw new IOException("YouTube cambió su respuesta o requiere verificación. Probá Abrir búsqueda en YouTube.");
        LinkedHashMap<String,Track> out=new LinkedHashMap<>();walk(json,out,0);return new ArrayList<>(out.values());
    }
    private static void walk(Object node,Map<String,Track> out,int depth)throws JSONException{
        if(depth>70||out.size()>=30)return;
        if(node instanceof JSONObject){JSONObject j=(JSONObject)node;
            // Sponsored renderers are never part of the music list.
            if(j.has("adSlotRenderer")||j.has("promotedSparklesWebRenderer")||j.has("promotedVideoRenderer"))return;
            for(String key:new String[]{"videoRenderer","compactVideoRenderer"}){JSONObject v=j.optJSONObject(key);if(v!=null){Track t=renderer(v);if(t!=null)out.putIfAbsent(t.id,t);return;}}
            Iterator<String> keys=j.keys();while(keys.hasNext())walk(j.opt(keys.next()),out,depth+1);
        }else if(node instanceof JSONArray){JSONArray a=(JSONArray)node;for(int i=0;i<a.length();i++)walk(a.opt(i),out,depth+1);}
    }
    private static Track renderer(JSONObject v)throws JSONException{
        String id=v.optString("videoId");if(!validId(id)||v.has("upcomingEventData")||v.optBoolean("isLiveNow"))return null;
        String title=text(v.optJSONObject("title"));if(title.isEmpty())return null;
        JSONObject meta=new JSONObject().put("id",id).put("title",title).put("artist",text(v.optJSONObject("ownerText")));
        if(meta.optString("artist").isEmpty())meta.put("artist",text(v.optJSONObject("longBylineText")));
        if(meta.optString("artist").isEmpty())meta.put("artist",text(v.optJSONObject("shortBylineText")));
        String stamp=text(v.optJSONObject("lengthText"));int seconds=0;try{for(String part:stamp.split(":"))seconds=seconds*60+Integer.parseInt(part);}catch(NumberFormatException ignored){}
        meta.put("duration",Math.min(86400,seconds));return metadata(meta);
    }
    private static String text(JSONObject j){if(j==null)return "";if(j.has("simpleText"))return j.optString("simpleText");JSONArray a=j.optJSONArray("runs");StringBuilder s=new StringBuilder();if(a!=null)for(int i=0;i<a.length();i++){JSONObject r=a.optJSONObject(i);if(r!=null)s.append(r.optString("text"));}return s.toString();}
    public static boolean validId(String id){return id!=null&&id.matches("[A-Za-z0-9_-]{11}");}
    public static Track metadata(JSONObject j)throws JSONException{
        String id=j.optString("id",j.optString("videoId"));if(!validId(id))throw new JSONException("ID de YouTube no válido");
        Track t=new Track();t.provider="youtube";t.id=id;t.title=j.optString("title");t.artist=j.optString("artist",j.optString("uploader","YouTube"));t.duration=Math.max(0,Math.min(86400,j.optInt("duration")));t.artwork="https://i.ytimg.com/vi/"+id+"/hqdefault.jpg";t.web="https://www.youtube.com/watch?v="+id;return t;
    }
}
