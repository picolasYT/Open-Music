package com.picolas.openmusic;
import org.junit.Test;import java.util.*;import static org.junit.Assert.*;
public class LyricsTest {
    @Test public void multipleTagsAndFractionalTimestampsAreSorted(){List<Lyrics.Line> lines=Lyrics.parse("[ar:Artist]\n[01:02.5][00:03.125] Repeated\n[00:01.02] Start");assertEquals(3,lines.size());assertEquals(1020,lines.get(0).at);assertEquals(3125,lines.get(1).at);assertEquals(62500,lines.get(2).at);assertEquals("Repeated",lines.get(2).text);}
    @Test public void currentLineChangesOnlyAfterItsTimestamp(){List<Lyrics.Line> lines=Lyrics.parse("[00:05.00] First\n[00:10.00] Second");assertEquals(-1,Lyrics.active(lines,4999));assertEquals(0,Lyrics.active(lines,5000));assertEquals(0,Lyrics.active(lines,9999));assertEquals(1,Lyrics.active(lines,10000));}
}
