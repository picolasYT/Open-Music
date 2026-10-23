package com.picolas.openmusic;
import java.util.*;
public interface MusicProvider {
    String name();
    List<Track> search(String query)throws Exception;
    List<Track> discover(String genre)throws Exception;
    Track refresh(Track track)throws Exception;
}
