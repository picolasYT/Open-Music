package com.picolas.openmusic;
import org.json.*;
import java.util.*;
/** Portable, bounded appearance values; no markup or external resources. */
public final class ProfileStyle {
    public String background="#19191F",accent="#F28779",banner="#663D4B",layout="left",shape="circle",font="sans",pattern="stars",badge="♫",status="",pronouns="",about="";
    public int bannerHeight=170;public boolean showStats=true,showPlaylists=true;
    public static final String[] NAMES={"Graphite","Noche estrellada","Sakura","Café","Crimson","Bosque","Océano","Lavanda"};
    public static ProfileStyle preset(int index){ProfileStyle s=new ProfileStyle();String[][] colors={{"#19191F","#F28779","#663D4B"},{"#11182D","#ADC3FF","#324B7D"},{"#F6E5EA","#9C365B","#C887A1"},{"#E8DFD0","#77573C","#A28A6E"},{"#211519","#FF899D","#793C50"},{"#12231C","#A4D4A5","#38654D"},{"#101F2A","#86DCE5","#316779"},{"#211C30","#D9B3FF","#5C437E"}};int n=Math.floorMod(index,8);s.background=colors[n][0];s.accent=colors[n][1];s.banner=colors[n][2];s.pattern=n==0?"grid":n==3?"waves":"stars";s.layout=n==2||n==7?"center":"left";s.font=n==3?"serif":"sans";s.badge=new String[]{"♫","☾","♡","✦","⚡","❀","≈","◇"}[n];return s;}
    static String choice(String value,String fallback,String... options){for(String option:options)if(option.equals(value))return value;return fallback;}
    public static String color(String value,String fallback){return value!=null&&value.matches("#[0-9a-fA-F]{6}")?value.toUpperCase(Locale.ROOT):fallback;}
    static String bounded(String value,int max){if(max<=0)return "";String s=value==null?"":value.trim();if(s.length()<=max)return s;int end=max;if(Character.isHighSurrogate(s.charAt(end-1)))end--;return s.substring(0,end);}
    public static ProfileStyle parse(JSONObject j){ProfileStyle s=new ProfileStyle();if(j==null)return s;s.background=color(j.optString("background"),s.background);s.accent=color(j.optString("accent"),s.accent);s.banner=color(j.optString("banner"),s.banner);s.layout=choice(j.optString("layout"),"left","left","center");s.shape=choice(j.optString("shape"),"circle","circle","rounded","square");s.font=choice(j.optString("font"),"sans","sans","serif","mono");s.pattern=choice(j.optString("pattern"),"stars","none","stars","waves","grid");s.badge=bounded(j.optString("badge","♫"),8);s.status=bounded(j.optString("status"),80);s.pronouns=bounded(j.optString("pronouns"),40);s.about=bounded(j.optString("about"),500);s.bannerHeight=Math.max(120,Math.min(240,j.optInt("bannerHeight",170)));s.showStats=j.optBoolean("showStats",true);s.showPlaylists=j.optBoolean("showPlaylists",true);return s;}
    public JSONObject json(){JSONObject j=new JSONObject();try{j.put("background",background).put("accent",accent).put("banner",banner).put("layout",layout).put("shape",shape).put("font",font).put("pattern",pattern).put("badge",badge).put("status",status).put("pronouns",pronouns).put("about",about).put("bannerHeight",bannerHeight).put("showStats",showStats).put("showPlaylists",showPlaylists);}catch(JSONException e){throw new IllegalStateException(e);}return j;}
    public ProfileStyle copy(){return parse(json());}
}
