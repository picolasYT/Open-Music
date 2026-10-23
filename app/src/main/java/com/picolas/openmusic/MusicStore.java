package com.picolas.openmusic;

import android.content.*;
import android.database.Cursor;
import android.database.sqlite.*;
import org.json.*;
import java.util.*;

/** Local-first library. Parameterized SQL, transactions and foreign keys protect user data. */
public final class MusicStore extends SQLiteOpenHelper {
    private static MusicStore instance;
    public static synchronized MusicStore get(Context c) { if(instance==null) instance=new MusicStore(c.getApplicationContext()); return instance; }
    private MusicStore(Context c) { super(c,"openmusic.db",null,2); }
    @Override public void onConfigure(SQLiteDatabase d) { d.setForeignKeyConstraintsEnabled(true); }
    @Override public void onCreate(SQLiteDatabase d) {
        d.execSQL("CREATE TABLE tracks (key TEXT PRIMARY KEY, data TEXT NOT NULL)");
        d.execSQL("CREATE TABLE favorites (key TEXT PRIMARY KEY REFERENCES tracks(key) ON DELETE CASCADE, added INTEGER NOT NULL)");
        d.execSQL("CREATE TABLE history (key TEXT PRIMARY KEY REFERENCES tracks(key) ON DELETE CASCADE, played INTEGER NOT NULL, count INTEGER NOT NULL)");
        d.execSQL("CREATE TABLE playlists (id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT NOT NULL, cover TEXT NOT NULL DEFAULT '', description TEXT NOT NULL DEFAULT '', pinned INTEGER NOT NULL DEFAULT 0)");
        d.execSQL("CREATE TABLE playlist_tracks (playlist INTEGER REFERENCES playlists(id) ON DELETE CASCADE, key TEXT REFERENCES tracks(key) ON DELETE CASCADE, position INTEGER, PRIMARY KEY(playlist,key))");
        d.execSQL("CREATE TABLE downloads (key TEXT PRIMARY KEY REFERENCES tracks(key), manager_id INTEGER NOT NULL, path TEXT NOT NULL)");
        d.execSQL("CREATE TABLE cache (key TEXT PRIMARY KEY, data TEXT NOT NULL, saved INTEGER NOT NULL)");
    }
    @Override public void onUpgrade(SQLiteDatabase d,int o,int n) { if(o<2){d.execSQL("ALTER TABLE playlists ADD COLUMN cover TEXT NOT NULL DEFAULT ''");d.execSQL("ALTER TABLE playlists ADD COLUMN description TEXT NOT NULL DEFAULT ''");d.execSQL("ALTER TABLE playlists ADD COLUMN pinned INTEGER NOT NULL DEFAULT 0");} }
    public synchronized void save(Track t) { Track old=track(t.key());if(old!=null&&t.local.isEmpty()&&!old.local.isEmpty())t.local=old.local;ContentValues v=new ContentValues();v.put("key",t.key());v.put("data",t.json().toString()); getWritableDatabase().insertWithOnConflict("tracks",null,v,SQLiteDatabase.CONFLICT_IGNORE);getWritableDatabase().update("tracks",v,"key=?",new String[]{t.key()}); }
    public synchronized void clearDownload(Track t){t.local="";ContentValues v=new ContentValues();v.put("data",t.json().toString());getWritableDatabase().update("tracks",v,"key=?",new String[]{t.key()});}
    public synchronized Track track(String key) {try(Cursor c=getReadableDatabase().rawQuery("SELECT data FROM tracks WHERE key=?",new String[]{key})) { if(c.moveToFirst()) return Track.from(new JSONObject(c.getString(0))); }catch(JSONException ignored){} return null;}
    private List<Track> query(String sql,String... args) {List<Track> out=new ArrayList<>();try(Cursor c=getReadableDatabase().rawQuery(sql,args)){while(c.moveToNext())try{out.add(Track.from(new JSONObject(c.getString(0))));}catch(JSONException ignored){}}return out;}
    public List<Track> favorites(){return query("SELECT t.data FROM tracks t JOIN favorites f ON t.key=f.key ORDER BY f.added DESC");}
    public List<Track> history(){return query("SELECT t.data FROM tracks t JOIN history h ON t.key=h.key ORDER BY h.played DESC LIMIT 200");}
    public List<Track> locals(){return query("SELECT data FROM tracks WHERE key LIKE 'local:%' ORDER BY rowid DESC");}
    public List<Track> offline(){ List<Track> out=new ArrayList<>();for(Track t:query("SELECT data FROM tracks ORDER BY rowid DESC")) if(!t.local.isEmpty()) out.add(t); return out; }
    public boolean favorite(Track t){try(Cursor c=getReadableDatabase().rawQuery("SELECT 1 FROM favorites WHERE key=?",new String[]{t.key()})){return c.moveToFirst();}}
    public synchronized boolean toggleFavorite(Track t){save(t);if(favorite(t)){getWritableDatabase().delete("favorites","key=?",new String[]{t.key()});return false;}ContentValues v=new ContentValues();v.put("key",t.key());v.put("added",System.currentTimeMillis());getWritableDatabase().insert("favorites",null,v);return true;}
    public synchronized void played(Track t){save(t);SQLiteDatabase d=getWritableDatabase();d.beginTransaction();try{d.execSQL("INSERT OR IGNORE INTO history(key,played,count) VALUES(?,0,0)",new Object[]{t.key()});d.execSQL("UPDATE history SET played=?,count=count+1 WHERE key=?",new Object[]{System.currentTimeMillis(),t.key()});d.setTransactionSuccessful();}finally{d.endTransaction();}}
    public synchronized long createPlaylist(String name){if(name.trim().isEmpty()||name.trim().length()>100)throw new IllegalArgumentException("Nombre de playlist inválido");ContentValues v=new ContentValues();v.put("name",name.trim());return getWritableDatabase().insertOrThrow("playlists",null,v);}
    public LinkedHashMap<Long,String> playlists(){LinkedHashMap<Long,String> out=new LinkedHashMap<>();try(Cursor c=getReadableDatabase().rawQuery("SELECT id,name FROM playlists ORDER BY pinned DESC,id DESC",null)){while(c.moveToNext())out.put(c.getLong(0),c.getString(1));}return out;}
    public String playlistField(long id,String field){if(!Arrays.asList("cover","description","pinned").contains(field))throw new IllegalArgumentException();try(Cursor c=getReadableDatabase().rawQuery("SELECT "+field+" FROM playlists WHERE id=?",new String[]{""+id})){return c.moveToFirst()?c.getString(0):"";}}
    public void playlistField(long id,String field,String value){if(!Arrays.asList("cover","description","pinned").contains(field))throw new IllegalArgumentException();ContentValues v=new ContentValues();v.put(field,value);getWritableDatabase().update("playlists",v,"id=?",new String[]{""+id});}
    public synchronized void reorder(long id,List<Track> tracks){SQLiteDatabase db=getWritableDatabase();db.beginTransaction();try{for(int i=0;i<tracks.size();i++){ContentValues v=new ContentValues();v.put("position",i);db.update("playlist_tracks",v,"playlist=? AND key=?",new String[]{""+id,tracks.get(i).key()});}db.setTransactionSuccessful();}finally{db.endTransaction();}}
    public synchronized void add(long id,Track t){save(t);getWritableDatabase().execSQL("INSERT OR IGNORE INTO playlist_tracks(playlist,key,position) VALUES(?,?,COALESCE((SELECT MAX(position)+1 FROM playlist_tracks WHERE playlist=?),0))",new Object[]{id,t.key(),id});}
    public void remove(long id,Track t){getWritableDatabase().delete("playlist_tracks","playlist=? AND key=?",new String[]{""+id,t.key()});}
    public void deletePlaylist(long id){getWritableDatabase().delete("playlists","id=?",new String[]{""+id});}
    public void renamePlaylist(long id,String name){ContentValues v=new ContentValues();v.put("name",name.trim());getWritableDatabase().update("playlists",v,"id=?",new String[]{""+id});}
    public List<Track> playlist(long id){return query("SELECT t.data FROM tracks t JOIN playlist_tracks p ON t.key=p.key WHERE p.playlist=? ORDER BY p.position",""+id);}
    public void cache(String key,String data){ContentValues v=new ContentValues();v.put("key",key);v.put("data",data);v.put("saved",System.currentTimeMillis());getWritableDatabase().insertWithOnConflict("cache",null,v,SQLiteDatabase.CONFLICT_REPLACE);}
    public String cached(String key){try(Cursor c=getReadableDatabase().rawQuery("SELECT data FROM cache WHERE key=?",new String[]{key})){return c.moveToFirst()?c.getString(0):"";}}
    public void pending(Track t,long managerId,String path){save(t);ContentValues v=new ContentValues();v.put("key",t.key());v.put("manager_id",managerId);v.put("path",path);getWritableDatabase().insertWithOnConflict("downloads",null,v,SQLiteDatabase.CONFLICT_REPLACE);}
    public boolean pending(Track t){try(Cursor c=getReadableDatabase().rawQuery("SELECT 1 FROM downloads WHERE key=?",new String[]{t.key()})){return c.moveToFirst();}}
    public void clearHistory(){getWritableDatabase().delete("history",null,null);}
    public String preferredGenre(){Map<String,Integer> score=new HashMap<>();for(Track t:history())if(!t.genre.isEmpty())score.merge(t.genre,1,Integer::sum);for(Track t:favorites())if(!t.genre.isEmpty())score.merge(t.genre,3,Integer::sum);return score.entrySet().stream().max(Map.Entry.comparingByValue()).map(Map.Entry::getKey).orElse("");}
}
