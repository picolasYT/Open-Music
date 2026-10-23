package com.picolas.openmusic;
import org.json.*;
import org.junit.Test;
import static org.junit.Assert.*;
public class UpdateReleaseTest {
    private JSONObject release(String tag)throws Exception{String v=tag.substring(1),name="Open-Music-"+v+".apk";return new JSONObject().put("tag_name",tag).put("assets",new JSONArray().put(new JSONObject().put("name",name).put("state","uploaded").put("size",8500000).put("browser_download_url",UpdateRelease.RELEASES+"/download/"+tag+"/"+name)));}
    @Test public void comparesNumbersNotStrings()throws Exception{assertTrue(UpdateRelease.newer("v1.10.0","1.9.9"));assertTrue(UpdateRelease.newer("2.0","1.99.0"));assertFalse(UpdateRelease.newer("1.6","1.6.0"));assertFalse(UpdateRelease.newer("1.5.1","1.6.0"));}
    @Test public void selectsOfficialApk()throws Exception{UpdateRelease r=UpdateRelease.parse(release("v1.7.0").toString(),"1.6.0");assertEquals("1.7.0",r.version);assertTrue(r.url.endsWith("/v1.7.0/Open-Music-1.7.0.apk"));}
    @Test public void ignoresDraftsPrereleasesAndDowngrades()throws Exception{assertNull(UpdateRelease.parse(release("v1.7.0").put("draft",true).toString(),"1.6.0"));assertNull(UpdateRelease.parse(release("v1.7.0").put("prerelease",true).toString(),"1.6.0"));assertNull(UpdateRelease.parse(release("v1.5.1").toString(),"1.6.0"));}
    @Test public void rejectsForeignDownloadAndMissingApk()throws Exception{JSONObject j=release("v1.7.0");j.getJSONArray("assets").getJSONObject(0).put("browser_download_url","https://evil.invalid/Open-Music-1.7.0.apk");try{UpdateRelease.parse(j.toString(),"1.6.0");fail();}catch(java.io.IOException expected){}try{UpdateRelease.parse(release("v1.7.0").put("assets",new JSONArray()).toString(),"1.6.0");fail();}catch(java.io.IOException expected){}}
    @Test public void rejectsMalformedAndUnstableTags()throws Exception{for(String s:new String[]{"1.7.0-beta","v99/../../evil","999999999999.0.0","garbage"})try{UpdateRelease.newer(s,"1.6.0");fail(s);}catch(java.io.IOException expected){}}
}
