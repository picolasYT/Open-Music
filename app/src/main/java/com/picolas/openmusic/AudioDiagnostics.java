package com.picolas.openmusic;
import android.os.Build;
import java.text.DateFormat;
import java.util.Date;
/** Local diagnostics never contain signed URLs, headers or account data. */
final class AudioDiagnostics {
 private static volatile String event="Todavía no se intentó reproducir una canción en esta sesión.";
 static void record(String message){event=DateFormat.getDateTimeInstance().format(new Date())+"\n"+message;}
 static String report(){return "Open Music 1.4.0\nAndroid "+Build.VERSION.RELEASE+" (API "+Build.VERSION.SDK_INT+")\nMotor: "+NewPipeBackend.VERSION+"\nReproductor: AndroidX Media3 1.8.0\n\n"+event;}
}
