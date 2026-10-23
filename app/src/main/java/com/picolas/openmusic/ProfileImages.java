package com.picolas.openmusic;
import android.content.Context;
import android.graphics.*;
import android.net.Uri;
import java.io.*;
import java.util.Base64;
/** Normalize private image copies and strip EXIF before sharing. */
final class ProfileImages {
    static String copy(Context c,Uri uri,boolean banner)throws IOException{
        byte[] bytes;try(InputStream in=c.getContentResolver().openInputStream(uri)){if(in==null)throw new IOException("Imagen vacía");bytes=read(in,15*1024*1024);}
        BitmapFactory.Options o=new BitmapFactory.Options();o.inJustDecodeBounds=true;BitmapFactory.decodeByteArray(bytes,0,bytes.length,o);if(o.outWidth<=0||o.outHeight<=0)throw new IOException("Formato no compatible");int max=banner?1200:512;o.inSampleSize=1;while(o.outWidth/o.inSampleSize>max||o.outHeight/o.inSampleSize>max)o.inSampleSize*=2;o.inJustDecodeBounds=false;
        Bitmap bmp=BitmapFactory.decodeByteArray(bytes,0,bytes.length,o);if(bmp==null)throw new IOException("No se pudo leer la imagen");
        try{int orientation=1;try{android.media.ExifInterface exif=new android.media.ExifInterface(new ByteArrayInputStream(bytes));orientation=exif.getAttributeInt(android.media.ExifInterface.TAG_ORIENTATION,1);}catch(IOException ignored){}Matrix m=new Matrix();switch(orientation){case 2:m.setScale(-1,1);break;case 3:m.setRotate(180);break;case 4:m.setScale(1,-1);break;case 5:m.setRotate(90);m.postScale(-1,1);break;case 6:m.setRotate(90);break;case 7:m.setRotate(270);m.postScale(-1,1);break;case 8:m.setRotate(270);break;}
            if(!m.isIdentity()){Bitmap rotated=Bitmap.createBitmap(bmp,0,0,bmp.getWidth(),bmp.getHeight(),m,true);if(rotated!=bmp){bmp.recycle();bmp=rotated;}}
            ByteArrayOutputStream out=new ByteArrayOutputStream();int quality=88;do{out.reset();bmp.compress(Bitmap.CompressFormat.JPEG,quality,out);quality-=12;}while(out.size()>190000&&quality>=28);if(out.size()>190000)throw new IOException("Elegí una imagen más simple");
            File dir=new File(c.getFilesDir(),"profile-images");if(!dir.exists()&&!dir.mkdirs())throw new IOException("No se pudo guardar");File target=File.createTempFile(banner?"banner-":"avatar-",".jpg",dir);try(FileOutputStream file=new FileOutputStream(target)){out.writeTo(file);}return Uri.fromFile(target).toString();
        }finally{bmp.recycle();}
    }
    static String export(Context c,String uri)throws IOException{if(uri.isEmpty())return "";File f=new File(Uri.parse(uri).getPath());File dir=new File(c.getFilesDir(),"profile-images");if(!f.getCanonicalPath().startsWith(dir.getCanonicalPath()+File.separator))return "";try(InputStream in=new FileInputStream(f)){return Base64.getEncoder().encodeToString(read(in,200000));}}
    static byte[] read(InputStream in,int max)throws IOException{ByteArrayOutputStream out=new ByteArrayOutputStream();byte[] b=new byte[8192];int n;while((n=in.read(b))!=-1){if(out.size()+n>max)throw new IOException("Archivo demasiado grande");out.write(b,0,n);}return out.toByteArray();}
    static String embedded(String base64){if(base64.isEmpty())return "";try{byte[] bytes=Base64.getDecoder().decode(base64);BitmapFactory.Options o=new BitmapFactory.Options();o.inJustDecodeBounds=true;BitmapFactory.decodeByteArray(bytes,0,bytes.length,o);if(o.outWidth<=0||o.outHeight<=0||o.outWidth>2048||o.outHeight>2048)return "";return "data:image/jpeg;base64,"+base64;}catch(IllegalArgumentException e){return "";}}
    private ProfileImages(){}
}
