package com.picolas.openmusic;
import android.app.DownloadManager;
import android.content.*;
import android.database.Cursor;
import android.net.Uri;
import java.io.*;
import java.util.*;
/** Android DownloadManager persists work across process death and network interruptions. */
public final class Downloads {
    public static synchronized void start(Context c,Track t)throws Exception {
        if(!t.downloadAllowed||!t.download.startsWith("https://"))throw new Exception("El artista no habilitó una descarga pública para esta canción.");
        MusicStore s=MusicStore.get(c);if(s.pending(t))throw new Exception("La descarga ya está en curso.");
        String name=Integer.toHexString(t.key().hashCode())+"-"+System.currentTimeMillis()+".audio";
        DownloadManager dm=(DownloadManager)c.getSystemService(Context.DOWNLOAD_SERVICE);
        DownloadManager.Request r=new DownloadManager.Request(Uri.parse(t.download)).setTitle(t.title).setDescription(t.artist+" · Open Music").setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED).setDestinationInExternalFilesDir(c,"music",name).setAllowedOverMetered(true).setAllowedOverRoaming(false);
        long id=dm.enqueue(r);try{s.pending(t,id,new File(c.getExternalFilesDir("music"),name).getAbsolutePath());}catch(RuntimeException e){dm.remove(id);throw e;}
    }
    public static final class Job {
        public final long id,bytes,total;public final String key,title,status;
        Job(long id,String key,String title,int state,long bytes,long total){this.id=id;this.key=key;this.title=title;this.bytes=bytes;this.total=total;status=state==DownloadManager.STATUS_PAUSED?"En espera de red o reintento":state==DownloadManager.STATUS_PENDING?"En cola":state==DownloadManager.STATUS_FAILED?"Descarga fallida":state==DownloadManager.STATUS_SUCCESSFUL?"Completada":"Descargando";}
        public int percent(){return total<=0?-1:(int)Math.min(100,Math.max(0,bytes)*100.0/total);}
    }
    public static synchronized List<Job> jobs(Context c){
        List<Job> jobs=new ArrayList<>();MusicStore s=MusicStore.get(c);DownloadManager dm=(DownloadManager)c.getSystemService(Context.DOWNLOAD_SERVICE);
        try(Cursor rows=s.getReadableDatabase().rawQuery("SELECT key,manager_id FROM downloads ORDER BY manager_id",null)){while(rows.moveToNext()){
            String key=rows.getString(0);long id=rows.getLong(1);Track t=s.track(key);
            try(Cursor d=dm.query(new DownloadManager.Query().setFilterById(id))){if(d!=null&&d.moveToFirst())jobs.add(new Job(id,key,t==null?"Canción":t.title,d.getInt(d.getColumnIndexOrThrow(DownloadManager.COLUMN_STATUS)),d.getLong(d.getColumnIndexOrThrow(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR)),d.getLong(d.getColumnIndexOrThrow(DownloadManager.COLUMN_TOTAL_SIZE_BYTES))));}
        }}return jobs;
    }
    public static synchronized boolean cancel(Context c,long id){
        MusicStore s=MusicStore.get(c);String key,path;
        try(Cursor row=s.getReadableDatabase().rawQuery("SELECT key,path FROM downloads WHERE manager_id=?",new String[]{Long.toString(id)})){if(!row.moveToFirst())return false;key=row.getString(0);path=row.getString(1);}
        ((DownloadManager)c.getSystemService(Context.DOWNLOAD_SERVICE)).remove(id);
        new File(path).delete();s.getWritableDatabase().delete("downloads","key=? AND manager_id=?",new String[]{key,Long.toString(id)});return true;
    }
    public static synchronized List<String> reconcile(Context c){
        List<String> errors=new ArrayList<>();MusicStore s=MusicStore.get(c);DownloadManager dm=(DownloadManager)c.getSystemService(Context.DOWNLOAD_SERVICE);
        try(Cursor rows=s.getReadableDatabase().rawQuery("SELECT key,manager_id,path FROM downloads",null)){while(rows.moveToNext()){
            String key=rows.getString(0);long id=rows.getLong(1);String path=rows.getString(2);
            try(Cursor d=dm.query(new DownloadManager.Query().setFilterById(id))){
                if(d==null||!d.moveToFirst()){s.getWritableDatabase().delete("downloads","key=?",new String[]{key});continue;}
                int status=d.getInt(d.getColumnIndexOrThrow(DownloadManager.COLUMN_STATUS));
                if(status==DownloadManager.STATUS_SUCCESSFUL){Track t=s.track(key);if(t!=null&&new File(path).isFile()&&new File(path).length()>0){t.local=Uri.fromFile(new File(path)).toString();s.save(t);}s.getWritableDatabase().delete("downloads","key=?",new String[]{key});}
                else if(status==DownloadManager.STATUS_FAILED){Track t=s.track(key);errors.add((t==null?"Canción":t.title)+": descarga fallida; podés reintentar.");new File(path).delete();s.getWritableDatabase().delete("downloads","key=?",new String[]{key});}
            }
        }}return errors;
    }
    public static void remove(Context c,Track t){if(t.provider.equals("local"))return;if(t.local.startsWith("file:")){new File(Uri.parse(t.local).getPath()).delete();MusicStore.get(c).clearDownload(t);}}
}
