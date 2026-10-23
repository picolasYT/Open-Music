package com.picolas.openmusic;

import android.database.sqlite.SQLiteDatabase;
import org.json.*;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.*;

/** Portable metadata only. A backup never grants access to audio or device files. */
public final class LibraryBackup {
    private static final int MAX_RECORDS=5000, MAX_PLAYLISTS=500;
    public static final class Plan {
        public final List<Track> favorites=new ArrayList<>();
        public final LinkedHashMap<String,List<Track>> playlists=new LinkedHashMap<>();
        public final Map<String,String> covers=new HashMap<>(),descriptions=new HashMap<>();
        public int records;
    }
    private static String required(JSONObject j,String key,int max)throws JSONException {
        Object raw=j.get(key);
        if(!(raw instanceof String))throw new JSONException("Campo inválido: "+key);
        String s=((String)raw).trim();
        if(s.isEmpty()||s.length()>max)throw new JSONException("Longitud inválida: "+key);
        return s;
    }
    private static String optional(JSONObject j,String key,int max)throws JSONException {
        if(!j.has(key))return "";
        Object raw=j.get(key);if(!(raw instanceof String)||((String)raw).length()>max)throw new JSONException("Campo inválido: "+key);
        return (String)raw;
    }
    private static Track safe(JSONObject j)throws JSONException {
        Track t=new Track();t.provider=required(j,"provider",30);t.id=required(j,"id",128);
        if(!Arrays.asList("audius","jamendo","spotify","spotdl","backup","youtube").contains(t.provider)||!t.id.matches("[A-Za-z0-9_-]{1,128}"))throw new JSONException("Identificador no válido");
        if(t.provider.equals("youtube")&&!YouTubeProvider.validId(t.id))throw new JSONException("ID de YouTube no válido");
        t.title=required(j,"title",500);t.artist=optional(j,"artist",500);t.album=optional(j,"album",500);t.genre=optional(j,"genre",100);
        t.duration=j.optInt("duration",0);if(t.duration<0||t.duration>86400)throw new JSONException("Duración no válida");
        // Ignore all URI, stream, artwork and rights fields, even if an input forges them.
        return t;
    }
    public static Plan parse(String text)throws JSONException {
        if(text.length()>10*1024*1024)throw new JSONException("Respaldo demasiado grande");
        JSONObject root=new JSONObject(text);if(!"open-music-backup".equals(root.getString("format"))||root.getInt("version")!=1)throw new JSONException("Formato de respaldo no compatible");
        Plan p=new Plan();readTracks(root.getJSONArray("favorites"),p.favorites,p);
        JSONArray lists=root.getJSONArray("playlists");if(lists.length()>MAX_PLAYLISTS)throw new JSONException("Demasiadas playlists");
        for(int i=0;i<lists.length();i++){JSONObject list=lists.getJSONObject(i);String name=required(list,"name",100);List<Track> tracks=p.playlists.computeIfAbsent(name,k->new ArrayList<>());readTracks(list.getJSONArray("tracks"),tracks,p);String cover=optional(list,"cover",100);if(cover.matches("mark:[0-7]"))p.covers.put(name,cover);String description=optional(list,"description",180);if(!description.isEmpty())p.descriptions.put(name,description);}
        return p;
    }
    private static void readTracks(JSONArray input,List<Track> into,Plan p)throws JSONException {
        if(input.length()>MAX_RECORDS-p.records)throw new JSONException("El límite es de 5000 entradas por respaldo");
        for(int i=0;i<input.length();i++){into.add(safe(input.getJSONObject(i)));p.records++;}
    }
    private static JSONObject metadata(Track original)throws Exception {
        Track t=Track.from(original.json());
        if(!Arrays.asList("audius","jamendo","spotify","spotdl","backup","youtube").contains(t.provider)||!t.id.matches("[A-Za-z0-9_-]{1,128}")){
            byte[] hash=MessageDigest.getInstance("SHA-256").digest(t.key().getBytes(StandardCharsets.UTF_8));StringBuilder id=new StringBuilder();for(byte b:hash)id.append(String.format(Locale.ROOT,"%02x",b));t.provider="backup";t.id=id.toString();
        }
        return new JSONObject().put("provider",t.provider).put("id",t.id).put("title",t.title).put("artist",t.artist).put("album",t.album).put("genre",t.genre).put("duration",t.duration);
    }
    private static JSONArray metadata(List<Track> tracks)throws Exception {JSONArray out=new JSONArray();for(Track t:tracks)out.put(metadata(t));return out;}
    public static String export(MusicStore store)throws Exception {
        synchronized(store){SQLiteDatabase db=store.getWritableDatabase();db.beginTransaction();try{
            JSONArray lists=new JSONArray();for(Map.Entry<Long,String> e:store.playlists().entrySet()){String cover=store.playlistField(e.getKey(),"cover");lists.put(new JSONObject().put("name",e.getValue()).put("tracks",metadata(store.playlist(e.getKey()))).put("cover",cover.matches("mark:[0-7]")?cover:"").put("description",store.playlistField(e.getKey(),"description")));}
            String text=new JSONObject().put("format","open-music-backup").put("version",1).put("favorites",metadata(store.favorites())).put("playlists",lists).toString(2);
            parse(text);db.setTransactionSuccessful();return text;
        }finally{db.endTransaction();}}
    }
    public static int restore(MusicStore store,String text)throws Exception {
        Plan p=parse(text); // Validate every entry before changing the database.
        synchronized(store){SQLiteDatabase db=store.getWritableDatabase();db.beginTransaction();try{
            for(Track t:p.favorites){Track old=store.track(t.key());if(old!=null)t=old;store.save(t);if(!store.favorite(t))store.toggleFavorite(t);}
            LinkedHashMap<Long,String> existing=store.playlists();
            for(Map.Entry<String,List<Track>> list:p.playlists.entrySet()){
                long id=-1;for(Map.Entry<Long,String> e:existing.entrySet())if(e.getValue().equals(list.getKey())){id=e.getKey();break;}
                if(id<0)id=store.createPlaylist(list.getKey());
                if(p.covers.containsKey(list.getKey())&&store.playlistField(id,"cover").isEmpty())store.playlistField(id,"cover",p.covers.get(list.getKey()));
                if(p.descriptions.containsKey(list.getKey())&&store.playlistField(id,"description").isEmpty())store.playlistField(id,"description",p.descriptions.get(list.getKey()));
                for(Track t:list.getValue()){Track old=store.track(t.key());store.add(id,old==null?t:old);}
            }
            db.setTransactionSuccessful();return p.records;
        }finally{db.endTransaction();}}
    }
    private LibraryBackup(){}
}
