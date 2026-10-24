package com.picolas.openmusic;

import android.app.Instrumentation;
import android.content.*;
import android.net.Uri;
import android.view.*;
import android.widget.*;
import androidx.media3.common.*;
import androidx.media3.datasource.*;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory;
import androidx.media3.ui.PlayerView;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import org.junit.*;
import org.junit.runner.RunWith;
import java.io.*;
import java.lang.reflect.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;
import static org.junit.Assert.*;

@RunWith(AndroidJUnit4.class)
@androidx.annotation.OptIn(markerClass=androidx.media3.common.util.UnstableApi.class)
public class VideoDeviceTest {
 private final Instrumentation inst=InstrumentationRegistry.getInstrumentation();
 private final Context context=inst.getTargetContext();
 private MainActivity activity;
 @Before public void launch(){new LocalProfile(context).finish();activity=(MainActivity)inst.startActivitySync(new Intent(context,MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));}
 @After public void close(){if(activity!=null)inst.runOnMainSync(activity::finish);}
 @Test public void mergedVideoRendersWithAudioAndRetainsSeekAndPause()throws Exception{
  File fixture=new File(context.getCacheDir(),"video-fixture.mp4");
  try(InputStream input=inst.getContext().getAssets().open("native-video-fixture.mp4");OutputStream out=new FileOutputStream(fixture)){byte[] buf=new byte[8192];int n;while((n=input.read(buf))!=-1)out.write(buf,0,n);}
  AtomicReference<ExoPlayer> ref=new AtomicReference<>();AtomicReference<PlayerView> view=new AtomicReference<>();AtomicReference<PlaybackException> error=new AtomicReference<>();
  CountDownLatch rendered=new CountDownLatch(1),ready=new CountDownLatch(1);
  try{
   inst.runOnMainSync(()->{
    DataSource.Factory local=new ResolvingDataSource.Factory(new DefaultDataSource.Factory(context),spec->spec.withUri(Uri.fromFile(fixture)));
    ExoPlayer p=new ExoPlayer.Builder(context).setMediaSourceFactory(new YouTubeVideo.Factory(new DefaultMediaSourceFactory(local))).build();ref.set(p);
    PlayerView surface=new PlayerView(activity);view.set(surface);surface.setPlayer(p);activity.setContentView(surface);
    p.addListener(new Player.Listener(){public void onRenderedFirstFrame(){rendered.countDown();}public void onPlaybackStateChanged(int state){if(state==Player.STATE_READY)ready.countDown();}public void onPlayerError(PlaybackException e){error.set(e);ready.countDown();rendered.countDown();}});
    Track track=new Track();track.id="aaaaaaaaaaa";track.provider="youtube";track.title="Video fixture";
    p.setMediaItems(java.util.List.of(PlaybackService.item(track,true),PlaybackService.item(track)),0,2000);p.prepare();p.play();
   });
   assertTrue("Video must become ready",ready.await(60,TimeUnit.SECONDS));assertNull(error.get());assertTrue("A real frame must render",rendered.await(60,TimeUnit.SECONDS));assertNull(error.get());
   inst.runOnMainSync(()->{ExoPlayer p=ref.get();assertTrue(p.getCurrentTracks().isTypeSelected(C.TRACK_TYPE_VIDEO));assertTrue(p.getCurrentTracks().isTypeSelected(C.TRACK_TYPE_AUDIO));assertEquals(2,p.getMediaItemCount());assertTrue(p.getCurrentPosition()>=1900);p.pause();p.seekTo(5000);});
   inst.waitForIdleSync();inst.runOnMainSync(()->{ExoPlayer p=ref.get();assertFalse(p.getPlayWhenReady());assertTrue(Math.abs(p.getCurrentPosition()-5000)<500);assertTrue(YouTubeVideo.active(p.getCurrentMediaItem()));assertFalse(YouTubeVideo.active(p.getMediaItemAt(1)));});
   inst.getUiAutomation().takeScreenshot().compress(android.graphics.Bitmap.CompressFormat.PNG,100,new FileOutputStream(new File(context.getExternalFilesDir(null),"native-video.png")));
  }finally{inst.runOnMainSync(()->{if(view.get()!=null)view.get().setPlayer(null);if(ref.get()!=null)ref.get().release();});fixture.delete();}
 }
 @Test public void playerShowsModeSelectorAndDisablesVideoForAudioOnlySource()throws Exception{
  Field playerField=MainActivity.class.getDeclaredField("player");playerField.setAccessible(true);long end=System.currentTimeMillis()+30000;AtomicBoolean connected=new AtomicBoolean();
  while(!connected.get()&&System.currentTimeMillis()<end){inst.runOnMainSync(()->{try{connected.set(playerField.get(activity)!=null);}catch(Exception e){throw new RuntimeException(e);}});Thread.sleep(100);}assertTrue(connected.get());
  Track t=new Track();t.provider="local";t.id="video-ui";t.title="Audio local";t.local="file:///does-not-exist.m4a";MusicStore.get(context).save(t);
  Method navigate=MainActivity.class.getDeclaredMethod("navigate",String.class);navigate.setAccessible(true);
  inst.runOnMainSync(()->{try{androidx.media3.session.MediaController p=(androidx.media3.session.MediaController)playerField.get(activity);p.setMediaItem(PlaybackService.item(t));navigate.invoke(activity,"player");View root=activity.findViewById(android.R.id.content);TextView music=find(root,"Música"),video=find(root,"Video");assertNotNull(music);assertNotNull(video);assertFalse(video.isEnabled());}catch(ReflectiveOperationException e){throw new RuntimeException(e);}});
 }
 private TextView find(View view,String label){if(view instanceof TextView&&label.contentEquals(((TextView)view).getText()))return (TextView)view;if(view instanceof ViewGroup){ViewGroup group=(ViewGroup)view;for(int i=0;i<group.getChildCount();i++){TextView found=find(group.getChildAt(i),label);if(found!=null)return found;}}return null;}
}
