package com.picolas.openmusic;

import android.content.*;
import org.json.*;
import java.util.*;

/** One local identity per installation; no login, remote account or password. */
public final class LocalProfile {
    private final SharedPreferences prefs;
    public LocalProfile(Context c){prefs=c.getSharedPreferences("local_profile",Context.MODE_PRIVATE);}
    public String name(){return prefs.getString("name","Oyente");}
    public String bio(){return prefs.getString("bio","");}
    public int avatar(){return prefs.getInt("avatar",0);}
    public ProfileStyle style(){try{return ProfileStyle.parse(new JSONObject(prefs.getString("style","{}")));}catch(JSONException e){return new ProfileStyle();}}
    public void style(ProfileStyle s){prefs.edit().putString("style",s.json().toString()).apply();}
    public String image(boolean banner){return prefs.getString(banner?"bannerPhoto":"avatarPhoto","");}
    public void image(boolean banner,String path){prefs.edit().putString(banner?"bannerPhoto":"avatarPhoto",path).apply();}
    public JSONArray templates(){try{return new JSONArray(prefs.getString("templates","[]"));}catch(JSONException e){return new JSONArray();}}
    public boolean saveTemplate(String name,ProfileStyle s){JSONArray all=templates();if(all.length()>=20)return false;try{all.put(new JSONObject().put("name",ProfileStyle.bounded(name,40)).put("style",s.json()).put("portrait",image(false)).put("banner",image(true)));prefs.edit().putString("templates",all.toString()).apply();return true;}catch(JSONException e){return false;}}
    public void deleteTemplate(int index){JSONArray all=templates();all.remove(index);prefs.edit().putString("templates",all.toString()).apply();}
    public void applyTemplate(int index){JSONObject j=templates().optJSONObject(index);if(j==null)return;style(ProfileStyle.parse(j.optJSONObject("style")));image(false,j.optString("portrait"));image(true,j.optString("banner"));}
    public boolean ready(){return prefs.getBoolean("onboarded",false);}
    public void save(String name,String bio,int avatar){prefs.edit().putString("name",name.trim()).putString("bio",bio.trim()).putInt("avatar",Math.floorMod(avatar,8)).apply();}
    public void finish(){prefs.edit().putBoolean("onboarded",true).apply();}
    private List<String> list(String k){List<String> out=new ArrayList<>();try{JSONArray a=new JSONArray(prefs.getString(k,"[]"));for(int i=0;i<a.length();i++)out.add(a.getString(i));}catch(JSONException ignored){}return out;}
    public List<String> artists(){return list("artists");}
    public List<String> genres(){return list("genres");}
    public void tastes(List<String> artists,List<String> genres){prefs.edit().putString("artists",new JSONArray(artists).toString()).putString("genres",new JSONArray(genres).toString()).apply();}
    public Set<String> hidden(){return new HashSet<>(prefs.getStringSet("hidden",Collections.emptySet()));}
    public void hide(Track t){Set<String> h=hidden();h.add(t.key());prefs.edit().putStringSet("hidden",h).apply();}
    public void resetHidden(){prefs.edit().remove("hidden").apply();}
}
