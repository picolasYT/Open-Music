package com.picolas.openmusic;

import android.app.*;
import android.content.*;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.view.*;
import android.widget.*;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.*;
import org.junit.runner.RunWith;
import java.util.*;
import java.lang.reflect.*;
import static org.junit.Assert.*;

@RunWith(AndroidJUnit4.class)
public class ProfileDeviceTest {
    @Test public void databaseMigrationRetainsExistingPlaylist(){Context c=InstrumentationRegistry.getInstrumentation().getTargetContext();try(SQLiteDatabase db=SQLiteDatabase.create(null)){db.execSQL("CREATE TABLE playlists (id INTEGER PRIMARY KEY AUTOINCREMENT,name TEXT NOT NULL)");db.execSQL("INSERT INTO playlists(name) VALUES('Anterior')");MusicStore.get(c).onUpgrade(db,1,2);try(Cursor row=db.rawQuery("SELECT name,cover,description,pinned FROM playlists",null)){assertTrue(row.moveToFirst());assertEquals("Anterior",row.getString(0));assertEquals("",row.getString(1));assertEquals(0,row.getInt(3));}}}
    @Test public void playlistArtDescriptionAndOrderPersist()throws Exception{Context c=InstrumentationRegistry.getInstrumentation().getTargetContext();MusicStore store=MusicStore.get(c);long id=store.createPlaylist("v15 test");Track a=new Track();a.provider="youtube";a.id="aaaaaaaaaaa";a.title="Primera";Track b=new Track();b.provider="youtube";b.id="bbbbbbbbbbb";b.title="Segunda";try{store.add(id,a);store.add(id,b);store.playlistField(id,"cover","mark:4");store.playlistField(id,"description","Prueba");store.playlistField(id,"pinned","1");store.reorder(id,Arrays.asList(b,a));assertEquals(b.key(),store.playlist(id).get(0).key());assertEquals("mark:4",store.playlistField(id,"cover"));assertEquals("Prueba",store.playlistField(id,"description"));assertEquals("1",store.playlistField(id,"pinned"));}finally{store.deletePlaylist(id);}}
    private View find(View root,Class<?> kind){if(kind.isInstance(root))return root;if(root instanceof ViewGroup){ViewGroup group=(ViewGroup)root;for(int i=0;i<group.getChildCount();i++){View result=find(group.getChildAt(i),kind);if(result!=null)return result;}}return null;}
    @Test public void firstLaunchTutorialCompletesAndIsRemembered()throws Exception{
        Instrumentation inst=InstrumentationRegistry.getInstrumentation();Context c=inst.getTargetContext();c.getSharedPreferences("local_profile",Context.MODE_PRIVATE).edit().clear().commit();
        MainActivity activity=(MainActivity)inst.startActivitySync(new Intent(c,MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));inst.waitForIdleSync();Field field=MainActivity.class.getDeclaredField("social");field.setAccessible(true);SocialScreens screens=(SocialScreens)field.get(activity);Field wizard=SocialScreens.class.getDeclaredField("wizard");wizard.setAccessible(true);
        inst.runOnMainSync(()->{try{AlertDialog dialog=(AlertDialog)wizard.get(screens);assertNotNull(dialog);EditText input=(EditText)find(dialog.getWindow().getDecorView(),EditText.class);input.setText("Picolas test");dialog.getButton(AlertDialog.BUTTON_POSITIVE).performClick();}catch(Exception e){throw new RuntimeException(e);}});inst.waitForIdleSync();
        inst.runOnMainSync(()->{try{AlertDialog dialog=(AlertDialog)wizard.get(screens);CheckBox first=(CheckBox)find(dialog.getWindow().getDecorView(),CheckBox.class);first.setChecked(true);dialog.getButton(AlertDialog.BUTTON_POSITIVE).performClick();}catch(Exception e){throw new RuntimeException(e);}});inst.waitForIdleSync();
        inst.runOnMainSync(()->{try{AlertDialog dialog=(AlertDialog)wizard.get(screens);dialog.getButton(AlertDialog.BUTTON_POSITIVE).performClick();}catch(Exception e){throw new RuntimeException(e);}});assertTrue(new LocalProfile(c).ready());assertEquals("Picolas test",new LocalProfile(c).name());assertTrue(new LocalProfile(c).artists().contains("Milo J"));inst.runOnMainSync(activity::finish);
    }
}
