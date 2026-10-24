package com.picolas.openmusic;

import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.*;
import androidx.annotation.Nullable;
import androidx.media3.common.*;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory;
import androidx.media3.datasource.DefaultDataSource;
import androidx.media3.datasource.okhttp.OkHttpDataSource;
import androidx.media3.session.*;
import java.util.*;

/** A real foreground media session, independent of the Activity's lifecycle. */
@androidx.annotation.OptIn(markerClass=androidx.media3.common.util.UnstableApi.class)
public final class PlaybackService extends MediaSessionService {
    private MediaSession session;
    private ExoPlayer player;
    private final Handler handler=new Handler(Looper.getMainLooper());
    private String recorded="";
    public static MediaItem item(Track t){
        return item(t,false);
    }
    static MediaItem item(Track t,boolean video){
        MediaMetadata.Builder m=new MediaMetadata.Builder().setTitle(t.title).setArtist(t.artist).setAlbumTitle(t.album);
        Bundle extras=new Bundle();extras.putBoolean(YouTubeVideo.MODE,video&&YouTubeVideo.available(t));m.setExtras(extras);
        if(!t.artwork.isEmpty())m.setArtworkUri(Uri.parse(t.artwork));
        String source=!t.local.isEmpty()?t.local:t.youtube()?"openmusic-youtube:///"+t.id:t.stream;
        return new MediaItem.Builder().setMediaId(t.key()).setUri(source).setMediaMetadata(m.build()).build();
    }
    static OkHttpDataSource.Factory httpFactory(okhttp3.OkHttpClient client){return new OkHttpDataSource.Factory(client).setDefaultRequestProperties(Collections.singletonMap("User-Agent","OpenMusic/1.5 (Android; Picolas)")).setContentTypePredicate(AudioContent::accepted);}
    @Override public void onCreate(){
        super.onCreate();
        DefaultDataSource.Factory source=new DefaultDataSource.Factory(this,httpFactory(Net.HTTP));
        player=new ExoPlayer.Builder(this).setMediaSourceFactory(new YouTubeVideo.Factory(new DefaultMediaSourceFactory(YouTubeAudio.factory(this,source)))).build();
        player.setAudioAttributes(new AudioAttributes.Builder().setContentType(C.AUDIO_CONTENT_TYPE_MUSIC).setUsage(C.USAGE_MEDIA).build(),true);
        player.setHandleAudioBecomingNoisy(true);player.setWakeMode(C.WAKE_MODE_NETWORK);
        PendingIntent pi=PendingIntent.getActivity(this,0,new Intent(this,MainActivity.class),PendingIntent.FLAG_IMMUTABLE|PendingIntent.FLAG_UPDATE_CURRENT);
        session=new MediaSession.Builder(this,player).setSessionActivity(pi).build();
        SharedPreferences prefs=getSharedPreferences("player",MODE_PRIVATE);
        List<Track> tracks=Track.decode(prefs.getString("queue","[]"));
        if(!tracks.isEmpty()){List<MediaItem> items=new ArrayList<>();for(Track t:tracks){Track saved=MusicStore.get(this).track(t.key());items.add(item(saved==null?t:saved));}player.setMediaItems(items,Math.min(prefs.getInt("index",0),items.size()-1),prefs.getLong("position",0));player.setRepeatMode(prefs.getInt("repeat",Player.REPEAT_MODE_OFF));player.setShuffleModeEnabled(prefs.getBoolean("shuffle",false));player.prepare();}
        player.addListener(new Player.Listener(){
            @Override public void onPlayerError(PlaybackException error){AudioDiagnostics.record(YouTubeAudio.userMessage(error));MediaItem item=player.getCurrentMediaItem();if(item!=null&&item.mediaId.startsWith("youtube:")){YouTubeAudio.invalidate(item.mediaId.substring(8));YouTubeVideo.invalidate(item.mediaId.substring(8));}}
            @Override public void onMediaItemTransition(@Nullable MediaItem m,int reason){recorded="";persist();}
            @Override public void onIsPlayingChanged(boolean playing){if(playing)AudioDiagnostics.record("Reproducción nativa iniciada.");persist();}
            @Override public void onEvents(Player p,Player.Events events){if(p.isPlaying()&&p.getCurrentMediaItem()!=null){String key=p.getCurrentMediaItem().mediaId;if(!key.equals(recorded)){Track t=MusicStore.get(PlaybackService.this).track(key);if(t!=null)MusicStore.get(PlaybackService.this).played(t);recorded=key;}}}
        });
        handler.post(tick);
    }
    private final Runnable tick=new Runnable(){public void run(){persist();SharedPreferences p=getSharedPreferences("settings",MODE_PRIVATE);long deadline=p.getLong("sleepUntil",0);if(deadline>0&&System.currentTimeMillis()>=deadline){player.pause();p.edit().remove("sleepUntil").apply();}handler.postDelayed(this,2000);}};
    private void persist(){if(player==null)return;List<Track> tracks=new ArrayList<>();for(int i=0;i<player.getMediaItemCount();i++){Track t=MusicStore.get(this).track(player.getMediaItemAt(i).mediaId);if(t!=null)tracks.add(t);}getSharedPreferences("player",MODE_PRIVATE).edit().putString("queue",Track.encode(tracks)).putInt("index",Math.max(0,player.getCurrentMediaItemIndex())).putLong("position",Math.max(0,player.getCurrentPosition())).putInt("repeat",player.getRepeatMode()).putBoolean("shuffle",player.getShuffleModeEnabled()).apply();}
    @Nullable @Override public MediaSession onGetSession(MediaSession.ControllerInfo info){return session;}
    @Override public void onDestroy(){handler.removeCallbacksAndMessages(null);persist();if(session!=null)session.release();if(player!=null)player.release();super.onDestroy();}
}
