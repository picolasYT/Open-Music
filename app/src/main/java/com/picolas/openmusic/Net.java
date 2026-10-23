package com.picolas.openmusic;
import okhttp3.*;
import org.json.*;
import java.io.*;
import java.util.concurrent.TimeUnit;
public final class Net {
    public static final OkHttpClient HTTP=new OkHttpClient.Builder().connectTimeout(12,TimeUnit.SECONDS).readTimeout(20,TimeUnit.SECONDS).callTimeout(35,TimeUnit.SECONDS).build();
    public static String get(String url) throws IOException {
        Request r=new Request.Builder().url(url).header("User-Agent","OpenMusic/1.4 (Android; Picolas)").header("Accept","application/json").build();
        try(Response response=HTTP.newCall(r).execute()) {if(!response.isSuccessful())throw new IOException("El proveedor respondió HTTP "+response.code()); if(response.body()==null)throw new IOException("Respuesta vacía");return response.body().string();}
    }
    public static JSONObject json(String url)throws Exception{return new JSONObject(get(url));}
    public static String enc(String s){try{return java.net.URLEncoder.encode(s,"UTF-8");}catch(java.io.UnsupportedEncodingException e){throw new AssertionError(e);}}
    public static String https(String s){if(s==null||s.equals("null"))return "";return s.startsWith("http://")?s.replaceFirst("http://","https://"):s.startsWith("https://")?s:"";}
}
