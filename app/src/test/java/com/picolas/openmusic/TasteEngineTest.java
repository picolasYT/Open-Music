package com.picolas.openmusic;
import org.junit.Test;
import static org.junit.Assert.*;
import java.util.*;

public class TasteEngineTest {
    private Track track(String id,String artist){Track t=new Track();t.provider="youtube";t.id=id;t.artist=artist;t.title=id;return t;}
    @Test public void selectedArtistsAreRankedAndHiddenTracksAreExcluded(){Track other=track("aaaaaaaaaaa","Other"),milo=track("bbbbbbbbbbb","Milo J"),hidden=track("ccccccccccc","Milo J");List<Track> out=TasteEngine.rank(Arrays.asList(other,milo,hidden,milo),Collections.emptyList(),Collections.emptyList(),Collections.singletonList("milo j"),Collections.emptyList(),Collections.singleton(hidden.key()));assertEquals(2,out.size());assertEquals(milo.key(),out.get(0).key());assertFalse(out.contains(hidden));}
    @Test public void favoritesAndHistoryAffectUnselectedArtist(){Track a=track("aaaaaaaaaaa","Trueno"),b=track("bbbbbbbbbbb","Other"),like=track("ccccccccccc","Trueno");List<Track> out=TasteEngine.rank(Arrays.asList(b,a),Collections.singletonList(like),Collections.singletonList(like),Collections.emptyList(),Collections.emptyList(),Collections.emptySet());assertEquals(a.key(),out.get(0).key());}
    @Test public void resultsHaveArtistDiversity(){List<Track> candidates=new ArrayList<>();for(int i=0;i<6;i++)candidates.add(track("milo"+i,"Milo J"));Track other=track("trueno","Trueno");candidates.add(other);List<Track> out=TasteEngine.rank(candidates,Collections.emptyList(),Collections.emptyList(),Collections.singletonList("Milo J"),Collections.emptyList(),Collections.emptySet());assertTrue(out.indexOf(other)<4);assertEquals(7,out.size());}
}
