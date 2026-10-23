package com.picolas.openmusic;
import org.json.*;
import org.junit.Test;
import static org.junit.Assert.*;
import java.util.Base64;
public class CustomizationTest {
    @Test public void trimmingSupportsEmptyAndSurrogateBoundaries(){assertEquals("",ProfileStyle.bounded("x",0));assertEquals("",ProfileStyle.bounded("🎵",1));assertEquals("🎵",ProfileStyle.bounded("🎵x",2));}
    @Test public void importedStylesAreBoundedAndIgnorePaths()throws Exception{ProfileStyle s=ProfileStyle.parse(new JSONObject().put("background","file:///private").put("accent","#aabbcc").put("font","<script>").put("layout","evil").put("bannerHeight",99999).put("about",new String(new char[900]).replace('\0','x')).put("photo","content://private"));assertEquals("#19191F",s.background);assertEquals("#AABBCC",s.accent);assertEquals("sans",s.font);assertEquals("left",s.layout);assertEquals(240,s.bannerHeight);assertEquals(500,s.about.length());assertFalse(s.json().toString().contains("private"));}
    @Test public void shareKeepsDesignAndQrOmitsPhotos()throws Exception{ProfileShare.Snapshot s=new ProfileShare.Snapshot();s.name="Picolas";s.style=ProfileStyle.preset(2);s.style.status="Escuchando mis temas";s.portrait=Base64.getEncoder().encodeToString(new byte[]{(byte)255,(byte)216,(byte)255,0});s.banner=s.portrait;ProfileShare.Snapshot full=ProfileShare.parse(ProfileShare.json(s));assertEquals(s.portrait,full.portrait);ProfileShare.Snapshot qr=ProfileShare.decode(ProfileShare.qr(s));assertEquals(s.style.background,qr.style.background);assertEquals(s.style.status,qr.style.status);assertTrue(qr.portrait.isEmpty());assertTrue(qr.banner.isEmpty());}
    @Test public void arbitraryPhotoUrisAreRejected()throws Exception{ProfileShare.Snapshot s=new ProfileShare.Snapshot();s.name="Test";s.portrait="file:///data/private";try{ProfileShare.parse(ProfileShare.json(s));fail();}catch(java.io.IOException expected){}}
    @Test public void oldProfilesStillLoadWithDefaultStyle()throws Exception{JSONObject j=new JSONObject().put("format","open-music-profile").put("version",1).put("name","Anterior").put("playlists",new JSONArray());ProfileShare.Snapshot s=ProfileShare.parse(j.toString());assertEquals("#19191F",s.style.background);assertTrue(s.portrait.isEmpty());}
    @Test public void eachPresetRoundTripsWithoutSharingMutableState(){for(int i=0;i<8;i++){ProfileStyle original=ProfileStyle.preset(i),copy=original.copy();assertEquals(original.json().toString(),copy.json().toString());copy.background="#FFFFFF";assertNotEquals(original.background,copy.background);}}
}
