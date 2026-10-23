package com.picolas.openmusic;

import android.app.*;
import android.content.*;
import android.graphics.*;
import android.net.Uri;
import android.text.InputFilter;
import android.text.TextUtils;
import android.view.*;
import android.widget.*;
import com.google.zxing.*;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import com.google.zxing.integration.android.IntentIntegrator;
import java.util.*;
import java.util.concurrent.Executor;
import static com.picolas.openmusic.Ui.*;

/** Real local profile, onboarding, playlist editing and portable offline sharing. */
final class SocialScreens {
    interface Host {
        void navigate(String page);void saved(String name,List<Track> tracks,long playlist);void search(String query);
        void pickCover(long playlist);void exportProfile();void importProfile();void toast(String message);
    }
    private final Activity a;private final MusicStore store;private final Host host;private final Executor io;
    final LocalProfile profile;
    private static final String[] GENRES={"Hip-Hop/Rap","Trap","Pop","Rock","Reggaetón","Electronic","Lo-Fi","Jazz","R&B","Indie"};
    private static final String[] ARTISTS={"Milo J","Trueno","Wos","Duki","Nicki Nicole","Babasónicos","Tini","María Becerra"};
    private static final String[] MARKS={"♫","✦","◎","♥","☾","⚡","◇","☀"};
    private Dialog wizard;
    SocialScreens(Activity a,MusicStore store,Host host,Executor io){this.a=a;this.store=store;this.host=host;this.io=io;profile=new LocalProfile(a);}
    void close(){if(wizard!=null)wizard.dismiss();}
    private int d(float n){return dp(a,n);}
    private void add(LinearLayout b,View v){b.addView(v,new LinearLayout.LayoutParams(-1,-2));}
    private void gap(LinearLayout b,int n){b.addView(new View(a),new LinearLayout.LayoutParams(1,d(n)));}
    private LinearLayout body(){LinearLayout b=col(a);pad(b,20,16);return b;}
    private TextView heading(String s,int n){return text(a,s,n,TEXT,true);}
    private EditText input(String hint,String value,int max){EditText t=new EditText(a);t.setTextColor(TEXT);t.setHintTextColor(MUTED);t.setHint(hint);t.setText(value);t.setTextSize(15);t.setSingleLine();t.setFilters(new InputFilter[]{new InputFilter.LengthFilter(max)});t.setBackground(panel(a));pad(t,14,14);return t;}
    private void title(LinearLayout b,String title,String note){add(b,heading(title,28));gap(b,10);add(b,text(a,note,13,MUTED,false));gap(b,22);}
    private void action(LinearLayout b,String name,Runnable click){gap(b,10);add(b,button(a,name,click));}
    private ScrollView scroller(View child){ScrollView sc=new ScrollView(a);sc.setFillViewport(true);sc.addView(child);return sc;}
    View avatar(int size){TextView v=heading(profile.name().substring(0,1).toUpperCase(Locale.ROOT),size/2);v.setGravity(Gravity.CENTER);v.setBackground(bg(avatarColor(profile.avatar()),d(size)));v.setLayoutParams(new LinearLayout.LayoutParams(d(size),d(size)));v.setContentDescription("Perfil de "+profile.name());return v;}
    private int avatarColor(int index){return Color.HSVToColor(new float[]{Math.floorMod(index*43+347,360),.55f,.55f});}
    View playlistCover(long id,String name,int size){String custom=store.playlistField(id,"cover");
        if(custom.startsWith("file:")){Track t=new Track();t.id="playlist:"+id;t.title=name;t.artwork=custom;return cover(a,t,size);}
        if(custom.isEmpty()){List<Track> tracks=store.playlist(id);if(!tracks.isEmpty()&&!tracks.get(0).artwork.isEmpty())return cover(a,tracks.get(0),size);}
        int index=0;try{if(custom.startsWith("mark:"))index=Integer.parseInt(custom.substring(5));}catch(Exception ignored){}
        TextView v=text(a,MARKS[Math.floorMod(index,MARKS.length)],Math.max(22,size/2),TEXT,true);v.setGravity(Gravity.CENTER);
        android.graphics.drawable.GradientDrawable bg=new android.graphics.drawable.GradientDrawable(android.graphics.drawable.GradientDrawable.Orientation.TL_BR,new int[]{avatarColor(index),SURFACE});bg.setCornerRadius(d(10));v.setBackground(bg);v.setLayoutParams(new LinearLayout.LayoutParams(d(size),d(size)));v.setContentDescription("Portada de "+name);return v;
    }
    void playlistRow(LinearLayout parent,long id,String name){LinearLayout r=row(a);pad(r,0,9);r.addView(playlistCover(id,name,62));LinearLayout words=col(a);pad(words,14,0);TextView title=heading(name,16);title.setMaxLines(2);title.setEllipsize(TextUtils.TruncateAt.END);add(words,title);gap(words,7);add(words,text(a,store.playlist(id).size()+" canciones"+(store.playlistField(id,"pinned").equals("1")?" · Fijada":""),12,MUTED,false));r.addView(words,new LinearLayout.LayoutParams(0,-2,1));r.addView(icon(a,"more","Editar "+name,()->editPlaylist(id)),new LinearLayout.LayoutParams(d(48),d(48)));r.setOnClickListener(v->host.saved(name,store.playlist(id),id));add(parent,r);}
    void createPlaylist(Track track){editForm(-1,track);}
    private void editForm(long id,Track track){LinearLayout b=body();String old=store.playlists().getOrDefault(id,"");EditText name=input("Nombre de la playlist",old,100),desc=input("Descripción (opcional)",id<0?"":store.playlistField(id,"description"),180);add(b,name);gap(b,14);add(b,desc);gap(b,12);add(b,text(a,"Después podés elegir una imagen o un logo y agregar canciones desde Buscar.",13,MUTED,false));
        AlertDialog dialog=new AlertDialog.Builder(a).setTitle(id<0?"Crear playlist":"Editar playlist").setView(scroller(b)).setNegativeButton("Cancelar",null).setPositiveButton("Guardar",null).create();dialog.setOnShowListener(x->dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v->{String s=name.getText().toString().trim();if(s.isEmpty()){name.setError("Escribí un nombre");return;}long target=id<0?store.createPlaylist(s):id;store.renamePlaylist(target,s);store.playlistField(target,"description",desc.getText().toString().trim());if(track!=null)store.add(target,track);dialog.dismiss();host.saved(s,store.playlist(target),target);host.toast("Playlist guardada");}));dialog.show();
    }
    void editPlaylist(long id){String name=store.playlists().get(id);if(name==null)return;
        new AlertDialog.Builder(a).setTitle(name).setItems(new String[]{"Editar nombre y descripción","Elegir foto de portada","Elegir un logo","Usar carátula automática",store.playlistField(id,"pinned").equals("1")?"Desfijar del inicio":"Fijar en el inicio","Ordenar canciones","Agregar canciones","Duplicar playlist","Compartir playlist por QR","Eliminar playlist"},(dialog,i)->{switch(i){
            case 0:editForm(id,null);break;case 1:host.pickCover(id);break;
            case 2:new AlertDialog.Builder(a).setTitle("Logo de la playlist").setItems(MARKS,(q,n)->{store.playlistField(id,"cover","mark:"+n);host.saved(name,store.playlist(id),id);}).show();break;
            case 3:store.playlistField(id,"cover","");host.saved(name,store.playlist(id),id);break;
            case 4:store.playlistField(id,"pinned",store.playlistField(id,"pinned").equals("1")?"0":"1");host.navigate("library");break;
            case 5:sortPlaylist(id,name);break;case 6:host.search("");host.toast("Abrí ⋮ en una canción y elegí Añadir a una playlist");break;
            case 7:long copy=store.createPlaylist((name.length()>90?name.substring(0,90):name)+" · copia");for(Track t:store.playlist(id))store.add(copy,t);store.playlistField(copy,"cover",store.playlistField(id,"cover"));store.playlistField(copy,"description",store.playlistField(id,"description"));host.navigate("library");break;
            case 8:share(id);break;
            case 9:new AlertDialog.Builder(a).setTitle("¿Eliminar “"+name+"”?").setMessage("Las canciones y favoritos se conservan.").setNegativeButton("Cancelar",null).setPositiveButton("Eliminar",(x,y)->{store.deletePlaylist(id);host.navigate("library");}).show();break;
        }}).show();
    }
    private void sortPlaylist(long id,String name){new AlertDialog.Builder(a).setTitle("Ordenar canciones").setItems(new String[]{"Título A–Z","Artista A–Z","Duración","Invertir orden","Mover una canción"},(dialog,i)->{List<Track> tracks=store.playlist(id);if(i==4){moveTrack(id,name,tracks);return;}if(i==0)tracks.sort(Comparator.comparing(t->t.title.toLowerCase(Locale.ROOT)));if(i==1)tracks.sort(Comparator.comparing(t->t.artist.toLowerCase(Locale.ROOT)));if(i==2)tracks.sort(Comparator.comparingInt(t->t.duration));if(i==3)Collections.reverse(tracks);store.reorder(id,tracks);host.saved(name,tracks,id);}).show();}
    private void moveTrack(long id,String name,List<Track> tracks){String[] titles=new String[tracks.size()];for(int i=0;i<tracks.size();i++)titles[i]=tracks.get(i).title;new AlertDialog.Builder(a).setTitle("Mover canción").setItems(titles,(d,index)->new AlertDialog.Builder(a).setTitle(titles[index]).setItems(new String[]{"Al principio","Subir un lugar","Bajar un lugar","Al final"},(q,n)->{int to=n==0?0:n==1?Math.max(0,index-1):n==2?Math.min(tracks.size()-1,index+1):tracks.size()-1;Track t=tracks.remove(index);tracks.add(to,t);store.reorder(id,tracks);host.saved(name,tracks,id);}).show()).show();}
    View profileView(){LinearLayout b=body();gap(b,8);b.addView(avatar(84));gap(b,18);title(b,profile.name(),profile.bio().isEmpty()?"Tu perfil local · sin registro online":profile.bio());
        add(b,text(a,store.playlists().size()+" playlists   ·   "+store.favorites().size()+" favoritos",14,TEXT,true));gap(b,14);
        action(b,"Editar perfil",this::editProfile);action(b,"Mis gustos musicales",()->tastes(false));action(b,"Compartir mi QR",()->share(-1));action(b,"Escanear perfil o playlist",this::scan);action(b,"Exportar perfil completo",host::exportProfile);action(b,"Abrir archivo de perfil",host::importProfile);
        gap(b,18);add(b,text(a,"Tu perfil se guarda en este teléfono. El QR contiene una copia, no un enlace a una cuenta online. Otra persona necesita Open Music para abrirlo. No se comparte tu historial, audio ni acceso a archivos.",13,MUTED,false));gap(b,24);add(b,heading("Tus playlists",22));for(Map.Entry<Long,String> e:store.playlists().entrySet())playlistRow(b,e.getKey(),e.getValue());return scroller(b);
    }
    void editProfile(){LinearLayout b=body();EditText name=input("Tu nombre",profile.name(),40),bio=input("Sobre vos",profile.bio(),180);add(b,name);gap(b,14);add(b,bio);gap(b,14);final int[] chosen={profile.avatar()};LinearLayout colors=row(a);for(int i=0;i<8;i++){final int n=i;TextView mark=text(a,chosen[0]==i?"✓":"●",19,TEXT,true);mark.setGravity(Gravity.CENTER);mark.setBackground(bg(avatarColor(i),d(20)));LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(0,d(40),1);p.rightMargin=d(3);colors.addView(mark,p);mark.setOnClickListener(v->{chosen[0]=n;for(int x=0;x<colors.getChildCount();x++)((TextView)colors.getChildAt(x)).setText(x==n?"✓":"●");});}add(b,colors);
        AlertDialog dialog=new AlertDialog.Builder(a).setTitle("Editar perfil local").setView(scroller(b)).setNegativeButton("Cancelar",null).setPositiveButton("Guardar",null).create();dialog.setOnShowListener(x->dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v->{if(name.getText().toString().trim().isEmpty()){name.setError("Escribí un nombre");return;}profile.save(name.getText().toString(),bio.getText().toString(),chosen[0]);dialog.dismiss();host.navigate("profile");}));dialog.show();
    }
    void onboard(){if(!profile.ready())welcome();}
    void welcome(){LinearLayout b=body();title(b,"Tu música.\nTu espacio.","1 de 3 · Un perfil local, sin correo ni contraseña.");EditText name=input("¿Cómo te llamás?",profile.name().equals("Oyente")?"":profile.name(),40);add(b,name);gap(b,18);add(b,text(a,"Elegí tus artistas, armá playlists y llevá tus gustos con vos. Tus datos permanecen en este teléfono.",15,MUTED,false));
        AlertDialog dialog=new AlertDialog.Builder(a).setView(scroller(b)).setNegativeButton("Ahora no",(q,n)->{profile.finish();host.navigate("home");}).setPositiveButton("Continuar",null).create();wizard=dialog;dialog.setOnShowListener(x->dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v->{String s=name.getText().toString().trim();if(s.isEmpty()){name.setError("Escribí un nombre");return;}profile.save(s,profile.bio(),profile.avatar());dialog.dismiss();tastes(true);}));dialog.show();
    }
    void tastes(boolean onboarding){LinearLayout b=body();title(b,onboarding?"¿Qué te mueve?":"Tus gustos musicales",onboarding?"2 de 3 · Elegí artistas y géneros; después podés cambiarlos.":"Todo se calcula en este teléfono. Los favoritos y las escuchas afinan tu mix.");
        LinkedHashSet<String> selected=new LinkedHashSet<>(profile.artists()),genres=new LinkedHashSet<>(profile.genres());add(b,heading("Artistas",18));gap(b,8);for(String artist:ARTISTS){CheckBox c=new CheckBox(a);c.setText(artist);c.setTextColor(TEXT);c.setChecked(selected.contains(artist));c.setOnCheckedChangeListener((v,checked)->{if(checked)selected.add(artist);else selected.remove(artist);});add(b,c);}
        List<String> custom=new ArrayList<>(profile.artists());custom.removeAll(Arrays.asList(ARTISTS));EditText extra=input("Otros artistas, separados por coma",String.join(", ",custom),180);gap(b,10);add(b,extra);gap(b,22);add(b,heading("Géneros",18));for(String genre:GENRES){CheckBox c=new CheckBox(a);c.setText(genre);c.setTextColor(TEXT);c.setChecked(genres.contains(genre));c.setOnCheckedChangeListener((v,checked)->{if(checked)genres.add(genre);else genres.remove(genre);});add(b,c);}
        AlertDialog dialog=new AlertDialog.Builder(a).setView(scroller(b)).setNegativeButton("Cancelar",null).setPositiveButton(onboarding?"Continuar":"Guardar",(q,n)->{for(String old:custom)selected.remove(old);for(String s:extra.getText().toString().split(","))if(!s.trim().isEmpty())selected.add(s.trim());profile.tastes(new ArrayList<>(selected),new ArrayList<>(genres));if(onboarding)tutorial();else host.navigate("home");}).create();wizard=dialog;dialog.show();
    }
    void tutorial(){LinearLayout b=body();title(b,"Dale play a Open Music","3 de 3 · Una guía rápida para empezar.");
        String[][] steps={{"01 · Buscar","Escribí una canción o artista. Tocá el resultado para escuchar con el reproductor nativo."},{"02 · Guardar y crear","En ⋮ podés guardar favoritos, crear una playlist y sumar canciones. En Biblioteca cambiás su foto, logo y orden."},{"03 · Escuchar offline","Importá archivos del teléfono o descargá cuando la fuente lo permita. Offline muestra progreso y cancelación."},{"04 · Tu mix","Tus artistas, favoritos y escuchas ordenan las recomendaciones. En ⋮ podés elegir No recomendar."},{"05 · Compartir","Abrí tu perfil para generar un QR o un archivo completo. Quien lo abre puede ver e importar tus playlists; los cambios futuros requieren compartir de nuevo."}};
        for(String[] step:steps){add(b,heading(step[0],17));gap(b,8);add(b,text(a,step[1],14,MUTED,false));gap(b,24);}
        AlertDialog dialog=new AlertDialog.Builder(a).setView(scroller(b)).setPositiveButton("Empezar a escuchar",(q,n)->{profile.finish();host.navigate("home");}).create();wizard=dialog;dialog.show();
    }
    void share(long playlist){host.toast("Preparando QR…");io.execute(()->{try{ProfileShare.Snapshot s=ProfileShare.snapshot(profile,store);if(playlist>=0)s=ProfileShare.playlist(s,new ArrayList<>(store.playlists().keySet()).indexOf(playlist));final ProfileShare.Snapshot shared=s;String uri=ProfileShare.qr(s);ProfileShare.Snapshot preview=ProfileShare.decode(uri);
            Map<EncodeHintType,Object> hints=new EnumMap<>(EncodeHintType.class);hints.put(EncodeHintType.ERROR_CORRECTION,ErrorCorrectionLevel.M);hints.put(EncodeHintType.MARGIN,4);BitMatrix matrix=new QRCodeWriter().encode(uri,BarcodeFormat.QR_CODE,900,900,hints);Bitmap bitmap=Bitmap.createBitmap(900,900,Bitmap.Config.ARGB_8888);int[] pixels=new int[900*900];for(int y=0;y<900;y++)for(int x=0;x<900;x++)pixels[y*900+x]=matrix.get(x,y)?Color.BLACK:Color.WHITE;bitmap.setPixels(pixels,0,900,0,0,900,900);
            a.runOnUiThread(()->{if(a.isDestroyed())return;LinearLayout b=body();title(b,profile.name(),"Escaneá desde Perfil → Escanear, en Open Music.");ImageView image=new ImageView(a);image.setImageBitmap(bitmap);image.setScaleType(ImageView.ScaleType.FIT_CENTER);image.setContentDescription("QR del perfil de "+profile.name());b.addView(image,new LinearLayout.LayoutParams(-1,d(300)));gap(b,14);add(b,text(a,preview.playlists.size()+" de "+shared.totalPlaylists+" playlists · "+preview.included()+" de "+shared.totalTracks+" canciones. Es una copia de este momento. Para incluir todo, exportá el perfil completo.",13,MUTED,false));action(b,"Copiar código",()->{android.content.ClipboardManager cb=(android.content.ClipboardManager)a.getSystemService(Context.CLIPBOARD_SERVICE);cb.setPrimaryClip(ClipData.newPlainText("Perfil Open Music",uri));host.toast("Código copiado");});action(b,"Compartir código",()->a.startActivity(Intent.createChooser(new Intent(Intent.ACTION_SEND).setType("text/plain").putExtra(Intent.EXTRA_TEXT,uri),"Compartir perfil")));new AlertDialog.Builder(a).setView(scroller(b)).setPositiveButton("Cerrar",null).show();});
        }catch(Exception e){a.runOnUiThread(()->host.toast("No se pudo crear el QR: "+e.getMessage()));}});}
    void scan(){new AlertDialog.Builder(a).setTitle("Abrir un perfil").setItems(new String[]{"Escanear con la cámara","Pegar un código","Abrir archivo completo"},(q,n)->{if(n==2){host.importProfile();return;}if(n==1){LinearLayout b=body();EditText t=input("openmusic://profile…","",6000);add(b,t);new AlertDialog.Builder(a).setTitle("Código de perfil").setView(b).setNegativeButton("Cancelar",null).setPositiveButton("Abrir",(x,y)->open(t.getText().toString().trim())).show();return;}
            new IntentIntegrator(a).setDesiredBarcodeFormats(IntentIntegrator.QR_CODE).setPrompt("Escaneá un perfil de Open Music").setBeepEnabled(false).setOrientationLocked(false).initiateScan();}).show();}
    void open(String code){io.execute(()->{try{ProfileShare.Snapshot s=ProfileShare.decode(code);a.runOnUiThread(()->preview(s));}catch(Exception e){a.runOnUiThread(()->host.toast("No se pudo abrir el perfil: "+e.getMessage()));}});}
    private void preview(ProfileShare.Snapshot s){if(a.isDestroyed())return;final AlertDialog[] dialogHolder={null};LinearLayout b=body();title(b,s.name,s.bio.isEmpty()?"Perfil compartido · copia local":s.bio);add(b,text(a,s.playlists.size()+" de "+s.totalPlaylists+" playlists · "+s.included()+" de "+s.totalTracks+" canciones incluidas\nEste código no verifica la identidad de quien lo comparte.",13,MUTED,false));gap(b,20);
        for(Map.Entry<String,List<Track>> e:s.playlists.entrySet()){String mark=s.covers.getOrDefault(e.getKey(),"mark:0");int symbol=0;try{symbol=Integer.parseInt(mark.substring(5));}catch(Exception ignored){}add(b,heading(MARKS[Math.floorMod(symbol,8)]+"  "+e.getKey(),19));if(!s.descriptions.getOrDefault(e.getKey(),"").isEmpty()){gap(b,6);add(b,text(a,s.descriptions.get(e.getKey()),13,MUTED,false));}gap(b,8);for(Track t:e.getValue()){TextView song=text(a,t.title+"\n"+t.artist,14,TEXT,false);pad(song,0,10);song.setOnClickListener(v->{if(dialogHolder[0]!=null)dialogHolder[0].dismiss();Track existing=store.track(t.key());host.saved(e.getKey(),Collections.singletonList(existing==null?t:existing),-1);});add(b,song);}action(b,"Guardar “"+e.getKey()+"”",()->{long id=store.createPlaylist(e.getKey());store.playlistField(id,"cover",s.covers.getOrDefault(e.getKey(),""));store.playlistField(id,"description",s.descriptions.getOrDefault(e.getKey(),""));for(Track t:e.getValue()){Track existing=store.track(t.key());store.add(id,existing==null?t:existing);}host.toast("Playlist importada · "+e.getValue().size()+" canciones");});gap(b,24);}
        if(s.playlists.isEmpty())add(b,text(a,"Este perfil todavía no tiene playlists compartidas.",14,MUTED,false));dialogHolder[0]=new AlertDialog.Builder(a).setTitle("Perfil compartido").setView(scroller(b)).setPositiveButton("Cerrar",null).show();
    }
}
