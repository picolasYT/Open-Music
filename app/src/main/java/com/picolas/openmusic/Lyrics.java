package com.picolas.openmusic;
import org.json.*;
import java.util.*;
import java.util.regex.*;
public final class Lyrics {
    public static final class Line { public final long at;public final String text; Line(long at,String text){this.at=at;this.text=text;} }
    public static List<Line> parse(String lrc){List<Line> out=new ArrayList<>();Pattern p=Pattern.compile("\\[(\\d+):(\\d{2})(?:[.:](\\d{1,3}))?\\]");for(String line:lrc.split("\n")){Matcher m=p.matcher(line);List<Long> times=new ArrayList<>();int end=0;while(m.find()){String frac=m.group(3);long ms=frac==null?0:Long.parseLong((frac+"00").substring(0,3));times.add(Long.parseLong(m.group(1))*60000+Long.parseLong(m.group(2))*1000+ms);end=m.end();}String txt=line.substring(end).trim();for(long time:times)out.add(new Line(time,txt));}out.sort(Comparator.comparingLong(a->a.at));return out;}
    public static int active(List<Line> lines,long position){int active=-1;for(int i=0;i<lines.size();i++){if(lines.get(i).at>position)break;active=i;}return active;}
    public static JSONObject fetch(Track t)throws Exception{return Net.json("https://lrclib.net/api/get?track_name="+Net.enc(t.title)+"&artist_name="+Net.enc(t.artist)+(t.album.isEmpty()?"":"&album_name="+Net.enc(t.album))+(t.duration<=0?"":"&duration="+t.duration));}
}
