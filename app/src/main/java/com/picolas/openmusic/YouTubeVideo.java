package com.picolas.openmusic;

import androidx.media3.common.*;
import androidx.media3.exoplayer.source.*;
import androidx.media3.exoplayer.drm.DrmSessionManagerProvider;
import androidx.media3.exoplayer.upstream.LoadErrorHandlingPolicy;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

/** Video and audio are resolved together, on loader threads, never persisted. */
@androidx.annotation.OptIn(markerClass=androidx.media3.common.util.UnstableApi.class)
final class YouTubeVideo {
    static final String MODE="openmusic.video";
    static final String VIDEO="openmusic-youtube-picture", AUDIO="openmusic-youtube-soundtrack";
    static final class Streams {
        final AudioAddress video,audio;
        Streams(AudioAddress video,AudioAddress audio){this.video=video;this.audio=audio;}
    }
    private static final ConcurrentHashMap<String,Streams> cache=new ConcurrentHashMap<>();
    static synchronized Streams resolve(String id)throws IOException{
        long now=System.currentTimeMillis()/1000;
        Streams old=cache.get(id);
        if(old!=null&&Math.min(old.video.expires,old.audio.expires)>now+60)return old;
        Streams value=NewPipeBackend.resolveVideo(id);
        if(cache.size()>=10)cache.clear();cache.put(id,value);
        return value;
    }
    static void invalidate(String id){cache.remove(id);}
    static boolean active(MediaItem item){return item!=null&&item.mediaMetadata.extras!=null&&item.mediaMetadata.extras.getBoolean(MODE,false);}
    static boolean available(Track track){return track!=null&&track.youtube()&&YouTubeProvider.validId(track.id);}

    /** Two progressive sources share the same clock, seek, queue and media session. */
    static final class Factory implements MediaSource.Factory {
        private final MediaSource.Factory delegate;
        Factory(MediaSource.Factory delegate){this.delegate=delegate;}
        @Override public MediaSource.Factory setDrmSessionManagerProvider(DrmSessionManagerProvider provider){delegate.setDrmSessionManagerProvider(provider);return this;}
        @Override public MediaSource.Factory setLoadErrorHandlingPolicy(LoadErrorHandlingPolicy policy){delegate.setLoadErrorHandlingPolicy(policy);return this;}
        @Override public int[] getSupportedTypes(){return delegate.getSupportedTypes();}
        @Override public MediaSource createMediaSource(MediaItem item){
            if(!active(item))return delegate.createMediaSource(item);
            String id=item.mediaId.startsWith("youtube:")?item.mediaId.substring(8):"";
            MediaItem picture=item.buildUpon().setUri(VIDEO+":///"+id).setMimeType(MimeTypes.VIDEO_MP4).build();
            MediaItem sound=item.buildUpon().setUri(AUDIO+":///"+id).setMimeType(MimeTypes.AUDIO_MP4).build();
            // Filtering also handles fallback streams with embedded audio, without doubling sound.
            return new MergingMediaSource(true,true,new FilteringMediaSource(delegate.createMediaSource(picture),C.TRACK_TYPE_VIDEO),delegate.createMediaSource(sound));
        }
    }
}
