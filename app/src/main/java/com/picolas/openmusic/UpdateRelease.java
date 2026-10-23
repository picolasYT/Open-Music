package com.picolas.openmusic;
import org.json.*;
import java.io.IOException;
/** Strict stable-version comparison and official-repository asset selection. */
public final class UpdateRelease {
    public static final String API="https://api.github.com/repos/picolasYT/Open-Music/releases/latest";
    public static final String RELEASES="https://github.com/picolasYT/Open-Music/releases";
    public final String version,url,notes;public final long size;
    private UpdateRelease(String version,String url,String notes,long size){this.version=version;this.url=url;this.notes=notes;this.size=size;}
    static String normalized(String value)throws IOException{if(value==null||!value.matches("v?[0-9]{1,6}\\.[0-9]{1,6}(\\.[0-9]{1,6})?"))throw new IOException("Versión no compatible");return value.startsWith("v")?value.substring(1):value;}
    public static boolean newer(String candidate,String current)throws IOException{String[] a=normalized(candidate).split("\\."),b=normalized(current).split("\\.");for(int i=0;i<3;i++){int x=i<a.length?Integer.parseInt(a[i]):0,y=i<b.length?Integer.parseInt(b[i]):0;if(x!=y)return x>y;}return false;}
    public static UpdateRelease parse(String json,String current)throws Exception{if(json.length()>1024*1024)throw new IOException("Respuesta demasiado grande");JSONObject j=new JSONObject(json);if(j.optBoolean("draft")||j.optBoolean("prerelease"))return null;String tag=j.getString("tag_name"),version=normalized(tag);if(!newer(version,current))return null;JSONArray assets=j.optJSONArray("assets");if(assets==null)throw new IOException("La versión todavía no tiene un APK");String filename="Open-Music-"+version+".apk",expected=RELEASES+"/download/"+tag+"/"+filename;for(int i=0;i<assets.length();i++){JSONObject asset=assets.getJSONObject(i);long size=asset.optLong("size");if(filename.equals(asset.optString("name"))&&expected.equals(asset.optString("browser_download_url"))&&"uploaded".equals(asset.optString("state"))&&size>0&&size<=150L*1024*1024)return new UpdateRelease(version,expected,ProfileStyle.bounded(j.optString("body"),1800),size);}throw new IOException("No hay un APK oficial compatible en esta versión");}
}
