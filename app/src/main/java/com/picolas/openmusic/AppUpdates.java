package com.picolas.openmusic;
import android.app.*;
import android.content.*;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.widget.*;
import okhttp3.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;
/** Checks releases on foreground entry, then hands an explicitly accepted update to Android/browser. */
final class AppUpdates {
    private final Activity a;private final SharedPreferences prefs;private final Handler ui=new Handler(Looper.getMainLooper());private final OkHttpClient client;private final String endpoint;private Call call;private boolean active,busy,closed;private AlertDialog prompt;private UpdateRelease pending;private boolean pendingManual;
    AppUpdates(Activity a){this(a,new OkHttpClient.Builder().callTimeout(15,TimeUnit.SECONDS).build(),UpdateRelease.API);}
    AppUpdates(Activity a,OkHttpClient client,String endpoint){this.a=a;this.client=client;this.endpoint=endpoint;prefs=a.getSharedPreferences("app_updates",Context.MODE_PRIVATE);}
    String current(){try{return a.getPackageManager().getPackageInfo(a.getPackageName(),0).versionName;}catch(Exception e){return "1.6.0";}}
    void resume(){active=true;if(pending!=null){UpdateRelease r=pending;pending=null;offer(r,pendingManual);}else if(new LocalProfile(a).ready()&&prefs.getBoolean("enabled",true))check(false);}
    void pause(){active=false;}
    void close(){closed=true;active=false;if(call!=null)call.cancel();if(prompt!=null)prompt.dismiss();ui.removeCallbacksAndMessages(null);}
    private void toast(String message){if(!closed)Toast.makeText(a,message,Toast.LENGTH_LONG).show();}
    void settings(){CheckBox enabled=new CheckBox(a);enabled.setText("Avisar al abrir la app si hay una versión nueva");enabled.setTextColor(Ui.TEXT);enabled.setPadding(24,20,24,20);enabled.setChecked(prefs.getBoolean("enabled",true));enabled.setOnCheckedChangeListener((v,c)->prefs.edit().putBoolean("enabled",c).apply());new AlertDialog.Builder(a).setTitle("Actualizaciones · "+current()).setMessage("Se consulta GitHub como máximo una vez al día. La descarga empieza cuando vos la aceptás y Android confirma la instalación.").setView(enabled).setPositiveButton("Buscar ahora",(q,n)->check(true)).setNeutralButton("Ver versiones",(q,n)->open(UpdateRelease.RELEASES)).setNegativeButton("Cerrar",null).show();}
    void check(boolean manual){if(busy){if(manual)toast("Ya estamos buscando una actualización…");return;}long now=System.currentTimeMillis();if(!manual&&now<prefs.getLong("nextCheck",0))return;busy=true;if(manual)toast("Buscando actualizaciones…");call=client.newCall(new Request.Builder().url(endpoint).header("Accept","application/vnd.github+json").header("X-GitHub-Api-Version","2022-11-28").header("User-Agent","Open-Music/"+current()).build());call.enqueue(new Callback(){public void onFailure(Call call,IOException e){ui.post(()->finish(null,manual,"No se pudo conectar a GitHub. Probá de nuevo más tarde.",false));}public void onResponse(Call call,Response response){try(Response r=response){if(r.code()==404){ui.post(()->finish(null,manual,"Todavía no hay versiones publicadas.",true));return;}if(!r.isSuccessful()||r.body()==null)throw new IOException("HTTP "+r.code());String json;try(InputStream in=r.body().byteStream()){json=new String(ProfileImages.read(in,1024*1024),StandardCharsets.UTF_8);}UpdateRelease update=UpdateRelease.parse(json,current());ui.post(()->finish(update,manual,null,true));}catch(Exception e){ui.post(()->finish(null,manual,"No se pudo comprobar la versión. Intentá de nuevo más tarde.",false));}}});}
    private void finish(UpdateRelease r,boolean manual,String error,boolean success){busy=false;if(closed)return;prefs.edit().putLong("nextCheck",System.currentTimeMillis()+(success?24:1)*60*60*1000L).apply();if(error!=null){if(manual)toast(error);return;}if(r==null){if(manual)toast("Ya tenés la última versión estable: "+current());return;}if(!active){pending=r;pendingManual=manual;return;}offer(r,manual);}
    void offer(UpdateRelease r,boolean manual){if(closed||a.isFinishing()||a.isDestroyed()||prompt!=null&&prompt.isShowing())return;if(!manual&&(r.version.equals(prefs.getString("skipped",""))||r.version.equals(prefs.getString("snoozed",""))&&System.currentTimeMillis()<prefs.getLong("snoozeUntil",0)))return;String size=String.format(java.util.Locale.ROOT,"%.1f",r.size/1048576.);prompt=new AlertDialog.Builder(a).setTitle("Open Music "+r.version+" disponible").setMessage("Tenés "+current()+" · Descarga de "+size+" MB\n\n"+r.notes+"\n\nSe abrirá el APK oficial en el navegador. Abrilo al terminar para instalar la actualización sin borrar tus datos.").setPositiveButton("Actualizar",(q,n)->open(r.url)).setNegativeButton("Más tarde",(q,n)->prefs.edit().putString("snoozed",r.version).putLong("snoozeUntil",System.currentTimeMillis()+24*60*60*1000L).apply()).setNeutralButton("Omitir versión",(q,n)->prefs.edit().putString("skipped",r.version).apply()).create();prompt.show();}
    private void open(String url){try{a.startActivity(new Intent(Intent.ACTION_VIEW,Uri.parse(url)));}catch(ActivityNotFoundException e){toast("No hay un navegador disponible para abrir GitHub.");}}
}
