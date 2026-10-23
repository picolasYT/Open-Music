package com.picolas.openmusic;

import android.content.*;
import android.graphics.*;
import android.graphics.drawable.*;
import android.view.*;
import android.widget.*;
import com.bumptech.glide.Glide;

final class Ui {
    static final int BG=Color.rgb(4,5,6), SURFACE=Color.rgb(17,18,20), BORDER=Color.rgb(43,44,47), TEXT=Color.rgb(244,244,245), MUTED=Color.rgb(161,162,168), CORAL=Color.rgb(255,99,99);
    static int dp(Context c,float n){return Math.round(n*c.getResources().getDisplayMetrics().density);}
    static GradientDrawable bg(int color,int radius){GradientDrawable d=new GradientDrawable();d.setColor(color);d.setCornerRadius(radius);return d;}
    static GradientDrawable panel(Context c){GradientDrawable d=bg(SURFACE,dp(c,18));d.setStroke(dp(c,1),BORDER);return d;}
    static TextView text(Context c,String s,int size,int color,boolean bold){TextView t=new TextView(c);t.setText(s);t.setTextSize(size);t.setTextColor(color);t.setFontFeatureSettings("kern");t.setTypeface(Typeface.create("sans-serif"+(bold?"-medium":""),Typeface.NORMAL));t.setIncludeFontPadding(false);return t;}
    static LinearLayout col(Context c){LinearLayout l=new LinearLayout(c);l.setOrientation(LinearLayout.VERTICAL);return l;}
    static LinearLayout row(Context c){LinearLayout l=new LinearLayout(c);l.setGravity(Gravity.CENTER_VERTICAL);return l;}
    static void pad(View v,int x,int y){v.setPadding(dp(v.getContext(),x),dp(v.getContext(),y),dp(v.getContext(),x),dp(v.getContext(),y));}
    static TextView button(Context c,String s,Runnable action){TextView v=text(c,s,14,BG,true);v.setGravity(Gravity.CENTER);pad(v,18,13);v.setBackground(new RippleDrawable(android.content.res.ColorStateList.valueOf(0x22666666),bg(TEXT,dp(c,26)),null));v.setOnClickListener(w->action.run());v.setMinHeight(dp(c,52));v.setFocusable(true);return v;}
    static TextView secondary(Context c,String label,Runnable click){TextView v=button(c,label,click);v.setTextColor(TEXT);v.setBackground(new RippleDrawable(android.content.res.ColorStateList.valueOf(0x22ffffff),bg(SURFACE,dp(c,26)),null));return v;}
    static View actionRow(Context c,String symbol,String title,String note,Runnable click){LinearLayout row=row(c);pad(row,14,14);row.setBackground(new RippleDrawable(android.content.res.ColorStateList.valueOf(0x22ffffff),bg(SURFACE,dp(c,20)),null));ImageView image=new ImageView(c);image.setImageDrawable(new Symbol(symbol,CORAL));row.addView(image,new LinearLayout.LayoutParams(dp(c,24),dp(c,24)));LinearLayout words=col(c);pad(words,14,0);TextView name=text(c,title,15,TEXT,true);words.addView(name);TextView detail=text(c,note,12,MUTED,false);detail.setPadding(0,dp(c,6),0,0);detail.setLineSpacing(dp(c,2),1);words.addView(detail);row.addView(words,new LinearLayout.LayoutParams(0,-2,1));row.setOnClickListener(v->click.run());row.setFocusable(true);row.setMinimumHeight(dp(c,72));return row;}
    static ImageButton icon(Context c,String name,String description,Runnable click){ImageButton b=new ImageButton(c);b.setImageDrawable(new Symbol(name,TEXT));b.setContentDescription(description);b.setBackground(new RippleDrawable(android.content.res.ColorStateList.valueOf(0x33ffffff),bg(Color.TRANSPARENT,dp(c,24)),null));b.setPadding(dp(c,12),dp(c,12),dp(c,12),dp(c,12));b.setOnClickListener(v->click.run());return b;}
    static TextView chip(Context c,String label,boolean selected,Runnable click){TextView t=text(c,label,13,selected?BG:TEXT,true);pad(t,17,12);t.setGravity(Gravity.CENTER);t.setBackground(bg(selected?TEXT:SURFACE,dp(c,24)));t.setMinHeight(dp(c,44));t.setOnClickListener(v->click.run());return t;}
    static ImageView cover(Context c,Track t,int size){ImageView v=new ImageView(c);v.setScaleType(ImageView.ScaleType.CENTER_CROP);v.setBackground(bg(SURFACE,dp(c,12)));v.setClipToOutline(true);v.setContentDescription("Carátula de "+t.title);bindCover(v,t);v.setLayoutParams(new LinearLayout.LayoutParams(dp(c,size),dp(c,size)));return v;}
    static void bindCover(ImageView v,Track t){Glide.with(v).load(t.artwork.isEmpty()?null:t.artwork).placeholder(new Art(t.key())).error(new Art(t.key())).centerCrop().into(v);}
    static final class Art extends Drawable {
        final Paint p=new Paint(Paint.ANTI_ALIAS_FLAG);final int seed;
        Art(String key){seed=key.hashCode();}
        @Override public void draw(Canvas c){Rect b=getBounds();float w=b.width(),h=b.height();p.setShader(new LinearGradient(0,0,w,h,new int[]{Color.HSVToColor(new float[]{Math.floorMod(seed,360),.4f,.28f}),BG},null,Shader.TileMode.CLAMP));c.drawRect(b,p);p.setShader(null);p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(Math.max(1,w/100));p.setColor(0x55ffffff);for(int i=0;i<7;i++)c.drawCircle(w*.65f,h*.45f,w*(.12f+i*.055f),p);p.setStyle(Paint.Style.FILL);p.setColor(CORAL);c.drawCircle(w*.65f,h*.45f,w*.065f,p);}
        public void setAlpha(int a){p.setAlpha(a);}public void setColorFilter(ColorFilter f){p.setColorFilter(f);}public int getOpacity(){return PixelFormat.TRANSLUCENT;}
    }
    /** Consistent vector icons, drawn at any density instead of platform-dependent emoji. */
    static final class Symbol extends Drawable {
        final String name;final Paint p=new Paint(Paint.ANTI_ALIAS_FLAG);final int color;final Path path=new Path();
        Symbol(String name,int color){this.name=name;this.color=color;}
        @Override public void draw(Canvas c){c.save();Rect b=getBounds();c.translate(b.left,b.top);c.scale(b.width()/24f,b.height()/24f);p.setColor(color);p.setStrokeWidth(1.7f);p.setStrokeCap(Paint.Cap.ROUND);p.setStrokeJoin(Paint.Join.ROUND);p.setStyle(Paint.Style.STROKE);Path x=path;x.reset();switch(name){
            case "play":p.setStyle(Paint.Style.FILL);x.moveTo(8,5);x.lineTo(20,12);x.lineTo(8,19);x.close();c.drawPath(x,p);break;
            case "pause":p.setStyle(Paint.Style.FILL);c.drawRoundRect(6,5,10,19,1,1,p);c.drawRoundRect(14,5,18,19,1,1,p);break;
            case "next":x.moveTo(5,5);x.lineTo(15,12);x.lineTo(5,19);x.close();p.setStyle(Paint.Style.FILL);c.drawPath(x,p);c.drawRect(18,5,20,19,p);break;
            case "prev":x.moveTo(19,5);x.lineTo(9,12);x.lineTo(19,19);x.close();p.setStyle(Paint.Style.FILL);c.drawPath(x,p);c.drawRect(4,5,6,19,p);break;
            case "heart":x.moveTo(12,20);x.cubicTo(0,13,2,3,8,5);x.quadTo(11,5,12,8);x.cubicTo(17,-1,29,9,12,20);c.drawPath(x,p);break;
            case "search":c.drawCircle(10,10,6.5f,p);c.drawLine(15,15,21,21,p);break;
            case "home":x.moveTo(3,10);x.lineTo(12,3);x.lineTo(21,10);x.lineTo(21,21);x.lineTo(15,21);x.lineTo(15,14);x.lineTo(9,14);x.lineTo(9,21);x.lineTo(3,21);x.close();c.drawPath(x,p);break;
            case "library":c.drawRoundRect(3,3,8,21,1,1,p);c.drawLine(12,3,12,21,p);c.drawLine(16,4,21,20,p);break;
            case "download":c.drawLine(12,3,12,15,p);c.drawLine(7,10,12,15,p);c.drawLine(17,10,12,15,p);x.moveTo(4,16);x.lineTo(4,21);x.lineTo(20,21);x.lineTo(20,16);c.drawPath(x,p);break;
            case "back":c.drawLine(19,12,5,12,p);c.drawLine(5,12,11,6,p);c.drawLine(5,12,11,18,p);break;
            case "down":c.drawLine(5,9,12,16,p);c.drawLine(12,16,19,9,p);break;
            case "plus":c.drawLine(12,5,12,19,p);c.drawLine(5,12,19,12,p);break;
            case "close":c.drawLine(6,6,18,18,p);c.drawLine(6,18,18,6,p);break;
            case "more":p.setStyle(Paint.Style.FILL);for(int i=0;i<3;i++)c.drawCircle(12,5+i*7,1.4f,p);break;
            case "shuffle":x.moveTo(3,6);x.lineTo(7,6);x.lineTo(17,18);x.lineTo(21,18);c.drawPath(x,p);x.reset();x.moveTo(3,18);x.lineTo(7,18);x.lineTo(17,6);x.lineTo(21,6);c.drawPath(x,p);c.drawLine(18,3,21,6,p);c.drawLine(21,6,18,9,p);c.drawLine(18,15,21,18,p);c.drawLine(21,18,18,21,p);break;
            case "repeat":x.moveTo(3,10);x.lineTo(3,7);x.quadTo(3,4,6,4);x.lineTo(20,4);x.lineTo(17,1);c.drawPath(x,p);c.drawLine(20,4,17,7,p);x.reset();x.moveTo(21,14);x.lineTo(21,17);x.quadTo(21,20,18,20);x.lineTo(4,20);x.lineTo(7,23);c.drawPath(x,p);c.drawLine(4,20,7,17,p);break;
            case "queue":for(int i=0;i<3;i++)c.drawLine(4,5+i*6,20,5+i*6,p);break;
            case "settings":c.drawCircle(12,12,4,p);c.drawCircle(12,12,9,p);for(int i=0;i<8;i++){double a=i*Math.PI/4;c.drawLine(12+(float)Math.cos(a)*9,12+(float)Math.sin(a)*9,12+(float)Math.cos(a)*11,12+(float)Math.sin(a)*11,p);}break;
            case "clock":c.drawCircle(12,12,9,p);c.drawLine(12,6,12,12,p);c.drawLine(12,12,16,14,p);break;
            case "check":x.moveTo(5,12);x.lineTo(10,17);x.lineTo(20,6);c.drawPath(x,p);break;
            case "lyrics":c.drawRoundRect(3,3,21,18,3,3,p);c.drawLine(7,8,17,8,p);c.drawLine(7,12,14,12,p);c.drawLine(7,18,7,22,p);break;
            default:for(int i=0;i<5;i++){float h=i==0?5:i==1?12:i==2?18:i==3?10:4;c.drawLine(4+i*4,12-h/2,4+i*4,12+h/2,p);}break;
        }c.restore();}
        public void setAlpha(int a){p.setAlpha(a);}public void setColorFilter(ColorFilter f){p.setColorFilter(f);}public int getOpacity(){return PixelFormat.TRANSLUCENT;}
    }
    static final class Atmosphere extends View {
        final Paint p=new Paint(Paint.ANTI_ALIAS_FLAG);
        Atmosphere(Context c){super(c);setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_NO);}
        protected void onDraw(Canvas c){float w=getWidth(),h=getHeight();p.setShader(new RadialGradient(w*.7f,h*.45f,w*.7f,new int[]{0x995d292f,0x00040506},null,Shader.TileMode.CLAMP));c.drawRect(0,0,w,h,p);p.setShader(null);p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(dp(getContext(),1));for(int i=0;i<12;i++){p.setColor(Color.argb(25+i*7,255,99,99));c.drawOval(w*.34f-i*w*.021f,h*.17f-i*h*.015f,w*.93f+i*w*.021f,h*.82f+i*h*.015f,p);}p.setStyle(Paint.Style.FILL);p.setColor(CORAL);c.drawCircle(w*.68f,h*.5f,dp(getContext(),6),p);}
    }
}
