package com.picolas.openmusic;

import org.json.*;
import java.util.*;
import java.util.zip.*;
import java.io.*;
import java.nio.charset.StandardCharsets;

/** Self-contained QR snapshots. No server, signed media URL, file URI or download grant. */
public final class ProfileShare {
    public static final String PREFIX="openmusic://profile?v=1&data=";
    public static final int QR_LIMIT=1800;
    public static final class Snapshot {
        public String name="",bio="",portrait="",banner="";public ProfileStyle style=new ProfileStyle();public int avatar;public long created;
        public final LinkedHashMap<String,List<Track>> playlists=new LinkedHashMap<>();
        public final Map<String,String> covers=new HashMap<>(),descriptions=new HashMap<>();
        public int totalTracks,totalPlaylists;
        public int included(){int n=0;for(List<Track> l:playlists.values())n+=l.size();return n;}
    }
    public static Snapshot snapshot(LocalProfile p,MusicStore store)throws Exception{return snapshot(p,store,false);}
    public static Snapshot snapshot(LocalProfile p,MusicStore store,boolean explicitPlaylist)throws Exception{
        Snapshot s=new Snapshot();s.name=p.name();s.bio=p.bio();s.avatar=p.avatar();s.created=System.currentTimeMillis();s.style=p.style().copy();if(!s.style.showPlaylists&&!explicitPlaylist)return s;
        JSONObject backup=new JSONObject(LibraryBackup.export(store));JSONArray lists=backup.getJSONArray("playlists");s.totalPlaylists=lists.length();
        for(int i=0;i<lists.length();i++){JSONObject j=lists.getJSONObject(i);List<Track> tracks=new ArrayList<>();JSONArray a=j.getJSONArray("tracks");for(int x=0;x<a.length();x++)tracks.add(Track.from(a.getJSONObject(x)));s.totalTracks+=tracks.size();String base=j.getString("name"),name=base;int duplicate=1;while(s.playlists.containsKey(name))name=base.substring(0,Math.min(90,base.length()))+" · "+(++duplicate);s.playlists.put(name,tracks);String style=j.optString("cover");if(style.matches("mark:[0-7]"))s.covers.put(name,style);s.descriptions.put(name,j.optString("description"));}
        return s;
    }
    public static Snapshot playlist(Snapshot full,int index)throws IOException{
        if(index<0||index>=full.playlists.size())throw new IOException("La playlist ya no está disponible");
        String name=new ArrayList<>(full.playlists.keySet()).get(index);Snapshot s=new Snapshot();s.name=full.name;s.bio=full.bio;s.avatar=full.avatar;s.created=full.created;s.style=full.style.copy();s.style.showPlaylists=true;s.playlists.put(name,new ArrayList<>(full.playlists.get(name)));s.covers.put(name,full.covers.getOrDefault(name,""));s.descriptions.put(name,full.descriptions.getOrDefault(name,""));s.totalPlaylists=1;s.totalTracks=s.included();return s;
    }
    public static String json(Snapshot s)throws JSONException{
        JSONArray lists=new JSONArray();for(Map.Entry<String,List<Track>> e:s.playlists.entrySet()){
            JSONArray tracks=new JSONArray();for(Track t:e.getValue())tracks.put(new JSONObject().put("provider",t.provider).put("id",t.id).put("title",t.title).put("artist",t.artist).put("genre",t.genre).put("duration",t.duration));
            lists.put(new JSONObject().put("name",e.getKey()).put("tracks",tracks).put("cover",s.covers.getOrDefault(e.getKey(),"")).put("description",s.descriptions.getOrDefault(e.getKey(),"")));
        }
        return new JSONObject().put("format","open-music-profile").put("version",1).put("name",s.name).put("bio",s.bio).put("avatar",s.avatar).put("style",s.style.json()).put("portrait",s.portrait).put("banner",s.banner).put("created",s.created).put("totalTracks",s.totalTracks).put("totalPlaylists",s.totalPlaylists).put("playlists",lists).toString();
    }
    private static String compress(Snapshot s)throws Exception{
        ByteArrayOutputStream out=new ByteArrayOutputStream();try(DeflaterOutputStream z=new DeflaterOutputStream(out)){z.write(json(s).getBytes(StandardCharsets.UTF_8));}
        return PREFIX+Base64.getUrlEncoder().withoutPadding().encodeToString(out.toByteArray());
    }
    public static String qr(Snapshot full)throws Exception{
        Snapshot s=parse(json(full));s.portrait="";s.banner="";int count=0;for(String name:new ArrayList<>(s.playlists.keySet())){if(++count>8){s.playlists.remove(name);continue;}List<Track> list=s.playlists.get(name);if(list.size()>6)list.subList(6,list.size()).clear();}String uri=compress(s);
        // QR capacity is finite. Remove the tail evenly and disclose exact included counts.
        while(uri.length()>QR_LIMIT){boolean trimmed=false;List<String> names=new ArrayList<>(s.playlists.keySet());Collections.reverse(names);for(String name:names){List<Track> list=s.playlists.get(name);if(!list.isEmpty()){list.remove(list.size()-1);trimmed=true;break;}}
            if(!trimmed){if(s.playlists.isEmpty()){if(!s.style.about.isEmpty())s.style.about=ProfileStyle.bounded(s.style.about,s.style.about.length()/2);else if(!s.bio.isEmpty())s.bio=ProfileStyle.bounded(s.bio,s.bio.length()/2);else throw new IOException("El perfil es demasiado grande para un QR");}else s.playlists.remove(names.get(0));}uri=compress(s);
        }return uri;
    }
    public static Snapshot decode(String input)throws Exception{
        if(input==null||input.length()>2*1024*1024)throw new IOException("Perfil demasiado grande");
        if(!input.startsWith(PREFIX))return parse(input);
        String encoded=input.substring(PREFIX.length());if(encoded.length()>QR_LIMIT)throw new IOException("QR demasiado grande");
        byte[] bytes;try{bytes=Base64.getUrlDecoder().decode(encoded);}catch(IllegalArgumentException e){throw new IOException("QR inválido",e);}
        ByteArrayOutputStream out=new ByteArrayOutputStream();try(InflaterInputStream z=new InflaterInputStream(new ByteArrayInputStream(bytes))){byte[] b=new byte[2048];int n;while((n=z.read(b))!=-1){if(out.size()+n>512*1024)throw new IOException("Perfil descomprimido demasiado grande");out.write(b,0,n);}}
        return parse(new String(out.toByteArray(),StandardCharsets.UTF_8));
    }
    public static Snapshot parse(String input)throws Exception{
        if(input.length()>2*1024*1024)throw new IOException("Perfil demasiado grande");JSONObject j=new JSONObject(input);
        if(!j.optString("format").equals("open-music-profile")||j.optInt("version")!=1)throw new IOException("No es un perfil de Open Music compatible");
        Snapshot s=new Snapshot();s.name=j.getString("name").trim();s.bio=j.optString("bio");if(s.name.isEmpty()||s.name.length()>40||s.bio.length()>180)throw new IOException("Perfil inválido");s.style=ProfileStyle.parse(j.optJSONObject("style"));s.portrait=photo(j.optString("portrait"));s.banner=photo(j.optString("banner"));s.avatar=Math.floorMod(j.optInt("avatar"),8);s.created=Math.max(0,j.optLong("created"));
        JSONObject backup=new JSONObject().put("format","open-music-backup").put("version",1).put("favorites",new JSONArray()).put("playlists",j.getJSONArray("playlists"));
        LibraryBackup.Plan safe=LibraryBackup.parse(backup.toString());s.playlists.putAll(safe.playlists);s.covers.putAll(safe.covers);s.descriptions.putAll(safe.descriptions);s.totalTracks=Math.max(s.included(),Math.min(5000,j.optInt("totalTracks",s.included())));s.totalPlaylists=Math.max(s.playlists.size(),Math.min(500,j.optInt("totalPlaylists",s.playlists.size())));return s;
    }
    private static String photo(String value)throws IOException{if(value.isEmpty())return "";if(value.length()>270000)throw new IOException("Foto demasiado grande");try{byte[] bytes=Base64.getDecoder().decode(value);if(bytes.length>200000||bytes.length<3||(bytes[0]&255)!=255||(bytes[1]&255)!=216||(bytes[2]&255)!=255)throw new IOException("Foto de perfil no compatible");return value;}catch(IllegalArgumentException e){throw new IOException("Foto inválida",e);}}
    private ProfileShare(){}
}
