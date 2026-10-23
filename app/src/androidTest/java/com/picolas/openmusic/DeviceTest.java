package com.picolas.openmusic;

import android.app.Activity;
import android.content.*;
import android.net.Uri;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.view.View;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.media3.common.*;
import androidx.media3.session.MediaController;
import org.junit.Test;
import org.junit.runner.RunWith;
import java.io.*;
import java.lang.reflect.*;
import java.nio.*;
import java.util.*;
import java.util.concurrent.atomic.*;
import static org.junit.Assert.*;

@RunWith(AndroidJUnit4.class)
public class DeviceTest {
    @org.junit.Before public void skipOnboarding(){new LocalProfile(InstrumentationRegistry.getInstrumentation().getTargetContext()).finish();}
    interface Check { boolean get()throws Exception; }
    private void waitFor(Check check,long timeout)throws Exception{long end=System.currentTimeMillis()+timeout;while(System.currentTimeMillis()<end){if(check.get())return;Thread.sleep(150);}fail("Timed out waiting for Android state");}
    private void main(Runnable r){InstrumentationRegistry.getInstrumentation().runOnMainSync(r);}
    private void capture(Activity activity,String name){main(()->{View root=activity.findViewById(android.R.id.content);if(root.getWidth()<=0||root.getHeight()<=0)throw new AssertionError("Activity must have a measured layout");root.measure(View.MeasureSpec.makeMeasureSpec(root.getWidth(),View.MeasureSpec.EXACTLY),View.MeasureSpec.makeMeasureSpec(root.getHeight(),View.MeasureSpec.EXACTLY));root.layout(root.getLeft(),root.getTop(),root.getLeft()+root.getMeasuredWidth(),root.getTop()+root.getMeasuredHeight());checkListWidths(root);Bitmap bitmap=Bitmap.createBitmap(root.getWidth(),root.getHeight(),Bitmap.Config.ARGB_8888);root.draw(new Canvas(bitmap));File f=new File(activity.getExternalFilesDir(null),"qa-"+name+".png");try(FileOutputStream out=new FileOutputStream(f)){bitmap.compress(Bitmap.CompressFormat.PNG,100,out);}catch(IOException e){throw new RuntimeException(e);}finally{bitmap.recycle();}});}
    private void checkListWidths(View view){if(view instanceof androidx.recyclerview.widget.RecyclerView){androidx.recyclerview.widget.RecyclerView list=(androidx.recyclerview.widget.RecyclerView)view;for(int i=0;i<list.getChildCount();i++)assertTrue("List content must fill available width",list.getChildAt(i).getWidth()>=list.getWidth()-list.getPaddingLeft()-list.getPaddingRight()-1);}if(view instanceof android.view.ViewGroup){android.view.ViewGroup group=(android.view.ViewGroup)view;for(int i=0;i<group.getChildCount();i++)checkListWidths(group.getChildAt(i));}}
    @Test public void libraryPersistsAndPlaylistDeletionPreservesFavorites()throws Exception{
        Context c=InstrumentationRegistry.getInstrumentation().getTargetContext();MusicStore s=MusicStore.get(c);Track t=new Track();t.provider="local";t.id="database-test";t.title="Test";t.local="file:///not-used-by-test.wav";s.save(t);if(!s.favorite(t))s.toggleFavorite(t);long id=s.createPlaylist("Persistence test");s.add(id,t);s.add(id,t);assertEquals(1,s.playlist(id).size());assertTrue(s.favorite(t));s.deletePlaylist(id);assertTrue(s.favorite(t));assertEquals(t.local,s.track(t.key()).local);s.toggleFavorite(t);s.getWritableDatabase().delete("tracks","key=?",new String[]{t.key()});
    }
    @Test public void localAudioPlaysSeeksAndSurvivesBackground()throws Exception{
        var inst=InstrumentationRegistry.getInstrumentation();if(android.os.Build.FINGERPRINT.contains("generic")||android.os.Build.MODEL.contains("Android SDK")){try(android.os.ParcelFileDescriptor fd=inst.getUiAutomation().executeShellCommand("wm density 160");FileInputStream densityOutput=new FileInputStream(fd.getFileDescriptor())){while(densityOutput.read()!=-1){}}}Context context=inst.getTargetContext();MusicStore s=MusicStore.get(context);
        // Generated sine fixture: test native playback without benchmarking Math.sin on TCG.
        File wav=new File(context.getFilesDir(),"instrumentation-audio.wav");try(InputStream in=inst.getContext().getAssets().open("local-audio-fixture.wav");FileOutputStream out=new FileOutputStream(wav)){byte[] buffer=new byte[8192];int n;while((n=in.read(buffer))!=-1)out.write(buffer,0,n);}
        Track t=new Track();t.provider="local";t.id="device-playback-test";t.title="Prueba de audio local";t.artist="Open Music";t.local=Uri.fromFile(wav).toString();t.duration=120;s.save(t);
        MainActivity a=(MainActivity)inst.startActivitySync(new Intent(context,MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
        Field f=MainActivity.class.getDeclaredField("player");f.setAccessible(true);AtomicReference<MediaController> ref=new AtomicReference<>();
        waitFor(()->{main(()->{try{ref.set((MediaController)f.get(a));}catch(Exception e){throw new RuntimeException(e);}});return ref.get()!=null;},30000);
        MediaController p=ref.get();main(()->{p.setMediaItems(List.of(PlaybackService.item(t)));p.prepare();p.play();});AtomicBoolean playing=new AtomicBoolean();waitFor(()->{main(()->playing.set(p.isPlaying()));return playing.get();},30000);
        AtomicLong position=new AtomicLong();waitFor(()->{main(()->position.set(p.getCurrentPosition()));return position.get()>500;},10000);
        main(()->p.seekTo(7000));waitFor(()->{main(()->position.set(p.getCurrentPosition()));return position.get()>=7000;},10000);
        Method nav=MainActivity.class.getDeclaredMethod("navigate",String.class);nav.setAccessible(true);for(String page:new String[]{"player","library","offline","settings","search","home","profile"}){main(()->{try{nav.invoke(a,page);}catch(Exception e){throw new RuntimeException(e);}});inst.waitForIdleSync();capture(a,page);}
        main(()->a.moveTaskToBack(true));Thread.sleep(1100);main(()->{playing.set(p.isPlaying());position.set(p.getCurrentPosition());});assertTrue("Foreground media service must continue in background",playing.get());assertTrue(position.get()>7000);assertTrue(s.history().stream().anyMatch(x->x.key().equals(t.key())));
        main(()->{p.stop();p.clearMediaItems();a.finish();});s.getWritableDatabase().delete("tracks","key=?",new String[]{t.key()});wav.delete();
    }
    @Test public void backupMergesWithoutLosingTrustedAudioAndRejectsPartialImports()throws Exception{
        Context c=InstrumentationRegistry.getInstrumentation().getTargetContext();MusicStore s=MusicStore.get(c);
        Track t=new Track();t.provider="audius";t.id="backup-test";t.title="Backup test";t.local="file:///private/download.wav";t.stream="https://example.invalid/authorized";s.save(t);s.toggleFavorite(t);long id=s.createPlaylist("Backup merge test");s.add(id,t);
        try{
            String backup=LibraryBackup.export(s);assertFalse(backup.contains("file:///"));assertFalse(backup.contains("https://"));
            s.toggleFavorite(t);LibraryBackup.restore(s,backup);LibraryBackup.restore(s,backup);
            assertTrue(s.favorite(t));assertEquals(1,s.playlist(id).size());assertEquals(t.local,s.track(t.key()).local);assertEquals(t.stream,s.track(t.key()).stream);
            org.json.JSONObject broken=new org.json.JSONObject(backup);broken.getJSONArray("playlists").put(new org.json.JSONObject().put("name","Should never exist").put("tracks",new org.json.JSONArray().put(new org.json.JSONObject().put("provider","local"))));
            int count=s.playlists().size();s.toggleFavorite(t);
            try{LibraryBackup.restore(s,broken.toString());fail("Invalid backup must fail before mutations");}catch(org.json.JSONException expected){}
            assertFalse(s.favorite(t));assertEquals(count,s.playlists().size());
            Track local=new Track();local.provider="local";local.id="content://private/song";local.local=local.id;local.title="Private local song";s.save(local);s.add(id,local);
            assertFalse(LibraryBackup.export(s).contains("content://"));s.getWritableDatabase().delete("tracks","key=?",new String[]{local.key()});
        }finally{s.deletePlaylist(id);s.getWritableDatabase().delete("tracks","key=?",new String[]{t.key()});}
    }
    private View findDescription(View v,String description){if(description.contentEquals(v.getContentDescription()==null?"":v.getContentDescription()))return v;if(v instanceof android.view.ViewGroup){android.view.ViewGroup g=(android.view.ViewGroup)v;for(int i=0;i<g.getChildCount();i++){View match=findDescription(g.getChildAt(i),description);if(match!=null)return match;}}return null;}
    @Test public void downloadCancellationUsesAndroidManagerAndPreservesFavorites()throws Exception{
        var inst=InstrumentationRegistry.getInstrumentation();Context c=inst.getTargetContext();MusicStore s=MusicStore.get(c);android.app.DownloadManager dm=(android.app.DownloadManager)c.getSystemService(Context.DOWNLOAD_SERVICE);
        Track t=new Track();t.provider="audius";t.id="download-test";t.title="Prueba de descarga";t.artist="Open Music";s.save(t);if(!s.favorite(t))s.toggleFavorite(t);
        File path=new File(c.getExternalFilesDir("music"),"test-never-download.audio");
        // Device-idle constraint keeps this controlled request pending: no music is fetched.
        long id=dm.enqueue(new android.app.DownloadManager.Request(Uri.parse("https://example.invalid/openmusic-test.audio")).setRequiresDeviceIdle(true).setDestinationUri(Uri.fromFile(path)).setTitle(t.title));s.pending(t,id,path.getAbsolutePath());
        MainActivity a=null;
        try{assertFalse(Downloads.cancel(c,-12345));assertTrue(s.pending(t));assertTrue(Downloads.jobs(c).stream().anyMatch(j->j.id==id));
            a=(MainActivity)inst.startActivitySync(new Intent(c,MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));MainActivity activity=a;Method nav=MainActivity.class.getDeclaredMethod("navigate",String.class);nav.setAccessible(true);main(()->{try{nav.invoke(activity,"offline");}catch(Exception e){throw new RuntimeException(e);}});
            AtomicReference<View> cancel=new AtomicReference<>();waitFor(()->{main(()->cancel.set(findDescription(activity.findViewById(android.R.id.content),"Cancelar descarga de "+t.title)));return cancel.get()!=null;},15000);capture(activity,"downloads");main(()->cancel.get().performClick());waitFor(()->!s.pending(t),10000);
            try(android.database.Cursor cursor=dm.query(new android.app.DownloadManager.Query().setFilterById(id))){assertTrue(cursor==null||!cursor.moveToFirst());}assertTrue(s.favorite(t));assertNotNull(s.track(t.key()));assertFalse(path.exists());
        }finally{dm.remove(id);s.getWritableDatabase().delete("downloads","key=?",new String[]{t.key()});s.getWritableDatabase().delete("tracks","key=?",new String[]{t.key()});if(a!=null){MainActivity activity=a;main(activity::finish);}}
    }
    @Test public void nativeExtractorFeedsPlayerQueueAndControls()throws Exception{
        var inst=InstrumentationRegistry.getInstrumentation();Context c=inst.getTargetContext();MusicStore s=MusicStore.get(c);
        NewPipeBackend.initialize();assertNotNull(org.schabi.newpipe.extractor.NewPipe.getDownloader());
        org.mozilla.javascript.Context js=org.mozilla.javascript.Context.enter();try{js.setOptimizationLevel(-1);assertEquals(2.0,((Number)js.evaluateString(js.initStandardObjects(),"1+1","native-test",1,null)).doubleValue(),0.0);}finally{org.mozilla.javascript.Context.exit();}
        assertEquals("test",java.nio.file.Paths.get("test").getFileName().toString());
        long now=System.currentTimeMillis()/1000;
        org.schabi.newpipe.extractor.stream.AudioStream format=new org.schabi.newpipe.extractor.stream.AudioStream.Builder().setId("test-aac").setContent("https://fixture.googlevideo.com/videoplayback?expire="+(now+3600),true).setMediaFormat(org.schabi.newpipe.extractor.MediaFormat.M4A).setAverageBitrate(96).build();
        AudioAddress address=NewPipeBackend.choose(List.of(format),now);
        byte[] audio;try(InputStream in=inst.getContext().getAssets().open("native-audio-fixture.m4a")){ByteArrayOutputStream bytes=new ByteArrayOutputStream();byte[] buffer=new byte[8192];int n;while((n=in.read(buffer))!=-1)bytes.write(buffer,0,n);audio=bytes.toByteArray();}
        java.util.concurrent.atomic.AtomicInteger resolves=new java.util.concurrent.atomic.AtomicInteger();
        AtomicReference<androidx.media3.exoplayer.ExoPlayer> exo=new AtomicReference<>();AtomicReference<androidx.media3.session.MediaSession> session=new AtomicReference<>();
        main(()->{androidx.media3.datasource.DataSource.Factory upstream=()->new androidx.media3.datasource.ByteArrayDataSource(audio);androidx.media3.datasource.DataSource.Factory source=YouTubeAudio.factory(upstream,id->{assertTrue(YouTubeProvider.validId(id));resolves.incrementAndGet();return address;});exo.set(new androidx.media3.exoplayer.ExoPlayer.Builder(c).setMediaSourceFactory(new androidx.media3.exoplayer.source.DefaultMediaSourceFactory(source)).build());session.set(new androidx.media3.session.MediaSession.Builder(c,exo.get()).setId("native-pipeline-test").build());});
        com.google.common.util.concurrent.ListenableFuture<MediaController> future=new MediaController.Builder(c,session.get().getToken()).buildAsync();MediaController controller=future.get(30,java.util.concurrent.TimeUnit.SECONDS);
        Track t=YouTubeProvider.metadata(new org.json.JSONObject().put("id","aBSkvI0CkgU").put("title","Prueba de audio nativo").put("artist","Open Music"));Track next=YouTubeProvider.metadata(new org.json.JSONObject().put("id","JWRlTezTF2k").put("title","Segunda pista de prueba").put("artist","Open Music"));s.save(t);s.save(next);
        MainActivity a=(MainActivity)inst.startActivitySync(new Intent(c,MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));Field playerField=MainActivity.class.getDeclaredField("player");playerField.setAccessible(true);AtomicBoolean ready=new AtomicBoolean();waitFor(()->{main(()->{try{ready.set(playerField.get(a)!=null);}catch(Exception e){throw new RuntimeException(e);}});return ready.get();},30000);
        try{
            Method render=MainActivity.class.getDeclaredMethod("renderTracks",View.class,List.class,String.class,long.class);render.setAccessible(true);Field gen=MainActivity.class.getDeclaredField("generation");gen.setAccessible(true);
            main(()->{try{gen.setInt(a,gen.getInt(a)+1);playerField.set(a,controller);android.widget.TextView title=new android.widget.TextView(a);title.setText("Audio nativo · prueba controlada");title.setTextColor(android.graphics.Color.WHITE);render.invoke(a,title,List.of(t,next),"",-1L);}catch(Exception e){throw new RuntimeException(e);}});inst.waitForIdleSync();
            main(()->{View result=findDescription(a.findViewById(android.R.id.content),t.title+", "+t.artist+", reproducir");assertNotNull(result);result.performClick();});
            // The first AAC codec startup is slow on software-only Android emulators.
            AtomicBoolean playing=new AtomicBoolean();waitFor(()->{main(()->playing.set(controller.isPlaying()));return playing.get();},60000);
            main(()->{assertEquals(2,controller.getMediaItemCount());assertEquals("openmusic-youtube",exo.get().getCurrentMediaItem().localConfiguration.uri.getScheme());controller.seekTo(3000);});
            AtomicLong position=new AtomicLong();waitFor(()->{main(()->position.set(controller.getCurrentPosition()));return position.get()>=3000;},10000);
            Method nav=MainActivity.class.getDeclaredMethod("navigate",String.class);nav.setAccessible(true);main(()->{try{nav.invoke(a,"player");}catch(Exception e){throw new RuntimeException(e);}});inst.waitForIdleSync();capture(a,"native-youtube-player");
            main(()->{View pause=findDescription(a.findViewById(android.R.id.content),"Reproducir o pausar");assertNotNull(pause);pause.performClick();});waitFor(()->{main(()->playing.set(controller.isPlaying()));return !playing.get();},5000);
            main(()->{controller.seekToNextMediaItem();controller.play();});waitFor(()->{main(()->playing.set(controller.isPlaying()&&controller.getCurrentMediaItemIndex()==1));return playing.get();},10000);assertTrue(resolves.get()>=2);assertEquals("",s.track(t.key()).stream);assertFalse(s.track(t.key()).downloadAllowed);
        }finally{main(()->{controller.stop();controller.clearMediaItems();a.finish();controller.release();session.get().release();exo.get().release();});s.getWritableDatabase().delete("tracks","key IN (?,?)",new String[]{t.key(),next.key()});}
    }
    @Test public void playerErrorSurvivesUiRefresh()throws Exception{
        var inst=InstrumentationRegistry.getInstrumentation();Context c=inst.getTargetContext();MusicStore store=MusicStore.get(c);Track t=new Track();t.provider="local";t.id="missing-audio-ui-test";t.title="Prueba de error visible";t.local="file:///openmusic-test-does-not-exist.wav";store.save(t);
        MainActivity a=(MainActivity)inst.startActivitySync(new Intent(c,MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));Field field=MainActivity.class.getDeclaredField("player");field.setAccessible(true);AtomicReference<MediaController> ref=new AtomicReference<>();waitFor(()->{main(()->{try{ref.set((MediaController)field.get(a));}catch(Exception e){throw new RuntimeException(e);}});return ref.get()!=null;},30000);MediaController controller=ref.get();
        try{
            main(()->{controller.setMediaItems(List.of(PlaybackService.item(t)));controller.prepare();controller.play();});AtomicBoolean failed=new AtomicBoolean();waitFor(()->{main(()->failed.set(controller.getPlayerError()!=null));return failed.get();},20000);
            Method nav=MainActivity.class.getDeclaredMethod("navigate",String.class);nav.setAccessible(true);Method update=MainActivity.class.getDeclaredMethod("updatePlayback");update.setAccessible(true);Field status=MainActivity.class.getDeclaredField("playerStatus");status.setAccessible(true);
            main(()->{try{nav.invoke(a,"player");update.invoke(a);update.invoke(a);String message=((android.widget.TextView)status.get(a)).getText().toString();assertTrue(message.contains("reintentar"));assertFalse(message.contains("Online"));}catch(Exception e){throw new RuntimeException(e);}});capture(a,"persistent-error");
        }finally{main(()->{controller.stop();controller.clearMediaItems();a.finish();});store.getWritableDatabase().delete("tracks","key=?",new String[]{t.key()});}
    }

}
