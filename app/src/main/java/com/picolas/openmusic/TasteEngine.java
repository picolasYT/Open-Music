package com.picolas.openmusic;

import java.util.*;

/** On-device ranking. No listening data is sent to a recommendation service. */
public final class TasteEngine {
    private static String key(String s){return s.trim().toLowerCase(Locale.ROOT);}
    public static List<Track> rank(List<Track> candidates,List<Track> favorites,List<Track> history,List<String> artists,List<String> genres,Set<String> hidden){
        Map<String,Double> affinity=new HashMap<>();Set<String> liked=new HashSet<>(),recent=new HashSet<>();
        for(String a:artists)affinity.merge("a:"+key(a),6.0,Double::sum);
        for(String g:genres)affinity.merge("g:"+key(g),3.0,Double::sum);
        for(Track t:favorites){liked.add(t.key());affinity.merge("a:"+key(t.artist),4.0,Double::sum);if(!t.genre.isEmpty())affinity.merge("g:"+key(t.genre),2.0,Double::sum);}
        int i=0;for(Track t:history){double weight=2.0/(1+i++*.08);affinity.merge("a:"+key(t.artist),weight,Double::sum);if(!t.genre.isEmpty())affinity.merge("g:"+key(t.genre),weight/2,Double::sum);if(i<=10)recent.add(t.key());}
        LinkedHashMap<String,Track> unique=new LinkedHashMap<>();for(Track t:candidates)if(!hidden.contains(t.key()))unique.putIfAbsent(t.key(),t);
        Map<String,Double> scores=new HashMap<>();for(Track t:unique.values()){
            double score=affinity.getOrDefault("a:"+key(t.artist),0.0)+affinity.getOrDefault("g:"+key(t.genre),0.0);
            for(String a:artists)if(!a.trim().isEmpty()&&key(t.artist).contains(key(a)))score+=5;
            if(liked.contains(t.key()))score+=2;if(recent.contains(t.key()))score-=2;
            scores.put(t.key(),score);
        }
        List<Track> sorted=new ArrayList<>(unique.values());sorted.sort((a,b)->Double.compare(scores.get(b.key()),scores.get(a.key())));
        // Interleave artists: a large result from one artist must not crowd out discovery.
        List<Track> out=new ArrayList<>();Map<String,Integer> counts=new HashMap<>();List<Track> later=new ArrayList<>();
        for(Track t:sorted){String a=key(t.artist);int n=counts.getOrDefault(a,0);if(n<3){out.add(t);counts.put(a,n+1);}else later.add(t);}out.addAll(later);return out;
    }
    public static List<String> seeds(List<String> selected,List<Track> favorites,List<Track> history){
        LinkedHashSet<String> out=new LinkedHashSet<>();for(String s:selected)if(!s.trim().isEmpty())out.add(s.trim());
        for(Track t:favorites)if(!t.artist.isEmpty())out.add(t.artist);for(Track t:history)if(!t.artist.isEmpty())out.add(t.artist);
        return new ArrayList<>(out);
    }
    private TasteEngine(){}
}
