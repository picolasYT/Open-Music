package com.picolas.openmusic;

import android.app.*;
import android.graphics.*;
import android.os.Bundle;
import android.view.*;
import android.widget.*;
import static com.picolas.openmusic.Ui.*;

/** Small offline illustrations, paired with readable text and accessible navigation. */
final class QuickGuide {
    private final Activity activity;
    private final Runnable complete;
    private int page;
    private final String[][] pages={
        {"Encontrá tu próxima canción","Buscá una canción o un artista en Buscar. Tocá un resultado para escucharlo.","Buscar"},
        {"Armá una playlist","En Biblioteca, tocá + para crear una playlist. Desde ⋮ en una canción podés añadirla a tu lista.","Biblioteca  →  +"},
        {"Llevá tu música con vos","Importá archivos del teléfono o descargá cuando la fuente lo permita. Encontralos en Offline.","Offline"},
        {"Hacé tuyo el perfil","Abrí Mi perfil en Biblioteca. Personalizá colores y textos, elegí fotos o empezá con una plantilla.","Mi perfil  →  Personalizar"},
        {"Compartí lo que te gusta","Desde tu perfil, compartí un QR o escaneá otro. Para incluir fotos y todas las playlists, exportá el archivo de perfil.","Mi perfil  →  Compartir mi QR"},
        {"Un mix cada vez más tuyo","Elegí tus artistas y géneros en Mis gustos musicales. Los favoritos y las escuchas afinan las recomendaciones en este teléfono.","Mi perfil  →  Mis gustos musicales"}
    };
    QuickGuide(Activity activity,Runnable complete){this.activity=activity;this.complete=complete;}
    AlertDialog show(){LinearLayout body=col(activity);pad(body,20,12);ScrollView scroll=new ScrollView(activity);scroll.setFillViewport(true);scroll.addView(body);
        AlertDialog dialog=new AlertDialog.Builder(activity).setTitle("Guía rápida").setView(scroll).setNegativeButton("Cerrar",null).setNeutralButton("Atrás",null).setPositiveButton("Siguiente",null).create();
        Runnable render=()->{body.removeAllViews();TextView progress=text(activity,"PASO "+(page+1)+" DE "+pages.length,12,CORAL,true);body.addView(progress);Illustration illustration=new Illustration(activity,page);LinearLayout.LayoutParams art=new LinearLayout.LayoutParams(-1,dp(activity,176));art.topMargin=dp(activity,12);art.bottomMargin=dp(activity,18);body.addView(illustration,art);TextView heading=text(activity,pages[page][0],24,TEXT,true);body.addView(heading);TextView description=text(activity,pages[page][1],15,MUTED,false);description.setPadding(0,dp(activity,12),0,dp(activity,18));description.setLineSpacing(dp(activity,4),1);body.addView(description);TextView route=text(activity,pages[page][2],13,TEXT,true);pad(route,14,12);route.setBackground(panel(activity));body.addView(route,new LinearLayout.LayoutParams(-1,-2));dialog.getButton(AlertDialog.BUTTON_NEUTRAL).setVisibility(page==0?View.INVISIBLE:View.VISIBLE);dialog.getButton(AlertDialog.BUTTON_POSITIVE).setText(page==pages.length-1?"Empezar a escuchar":"Siguiente");scroll.scrollTo(0,0);progress.setAccessibilityLiveRegion(View.ACCESSIBILITY_LIVE_REGION_POLITE);};
        dialog.setOnShowListener(v->{dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(w->{if(page==pages.length-1){dialog.dismiss();complete.run();}else{page++;render.run();}});dialog.getButton(AlertDialog.BUTTON_NEUTRAL).setOnClickListener(w->{if(page>0){page--;render.run();}});render.run();});dialog.show();return dialog;
    }
    /** Illustrative UI, with no external assets or network requests. */
    static final class Illustration extends View {
        private final Paint paint=new Paint(Paint.ANTI_ALIAS_FLAG);
        private final int page;
        Illustration(android.content.Context c,int page){super(c);this.page=page;setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_NO);}
        private void rect(Canvas c,float x,float y,float w,float h,int color,float radius){paint.setColor(color);c.drawRoundRect(x,y,x+w,y+h,radius,radius,paint);}
        private void icon(Canvas c,String symbol,int color,int x,int y,int size){Symbol d=new Symbol(symbol,color);d.setBounds(x,y,x+size,y+size);d.draw(c);}
        @Override protected void onDraw(Canvas c){super.onDraw(c);int save=c.save();float scale=Math.min(getWidth()/280f,getHeight()/176f);c.translate((getWidth()-280*scale)/2,(getHeight()-176*scale)/2);c.scale(scale,scale);rect(c,0,0,280,176,0xff181c25,24);paint.setColor(0xff272b3c);c.drawCircle(252,25,62,paint);c.drawCircle(16,164,42,paint);
            if(page==0){rect(c,24,20,232,34,0xff343a49,17);icon(c,"search",TEXT,36,27,20);rect(c,67,35,110,5,0xffc4c7d3,3);for(int i=0;i<2;i++){rect(c,24,65+i*47,232,39,0xff232938,12);rect(c,32,71+i*47,27,27,i==0?CORAL:0xff9b9de8,8);icon(c,"music",BG,37,76+i*47,17);rect(c,72,77+i*47,90,5,TEXT,3);rect(c,72,89+i*47,60,4,MUTED,2);icon(c,"play",TEXT,222,75+i*47,19);}}
            else if(page==1){for(int i=0;i<3;i++){rect(c,24+i*79,28,68,83,new int[]{0xffa6baff,0xffffbd9e,0xffbdabdf}[i],16);icon(c,i==0?"heart":i==1?"music":"plus",BG,44+i*79,49,28);rect(c,26+i*79,123,57,6,TEXT,3);rect(c,26+i*79,137,37,4,MUTED,2);}}
            else if(page==2){rect(c,77,16,126,146,0xff30394e,20);icon(c,"download",0xffb9ddcc,116,34,48);rect(c,95,100,90,8,0xff535e73,4);rect(c,95,100,70,8,0xffb9ddcc,4);icon(c,"check",0xffb9ddcc,113,121,26);icon(c,"music",TEXT,36,60,25);icon(c,"music",TEXT,222,100,22);}
            else if(page==3){rect(c,45,15,190,146,0xff303548,20);rect(c,45,15,190,55,0xffa7aff0,20);paint.setColor(0xff303548);c.drawCircle(84,75,26,paint);paint.setColor(0xffeac2b7);c.drawCircle(84,75,21,paint);icon(c,"music",BG,71,62,26);rect(c,64,112,102,7,TEXT,4);rect(c,64,130,65,5,MUTED,3);for(int i=0;i<3;i++){paint.setColor(new int[]{CORAL,0xffa7aff0,0xffb9ddcc}[i]);c.drawCircle(191+i*16,89,5,paint);}}
            else if(page==4){rect(c,76,18,128,140,0xfff4f4f5,20);for(int y=0;y<7;y++)for(int x=0;x<7;x++)if(((x*7+y*3+x*y)%5)<3)rect(c,97+x*12,37+y*12,9,9,0xff24283a,2);rect(c,95,35,27,27,0xff24283a,4);rect(c,101,41,15,15,TEXT,2);rect(c,158,35,27,27,0xff24283a,4);rect(c,164,41,15,15,TEXT,2);icon(c,"plus",CORAL,225,68,24);icon(c,"heart",0xffb9ddcc,27,92,28);}
            else{for(int i=0;i<3;i++){rect(c,25+i*79,35,68,91,new int[]{0xffa7aff0,0xffeac2b7,0xffb9ddcc}[i],18);icon(c,i==0?"heart":i==1?"music":"play",BG,44+i*79,62,30);}rect(c,66,143,148,5,0xff50566b,3);rect(c,66,143,87,5,CORAL,3);}
            c.restoreToCount(save);
        }
    }
}
