package com.picolas.openmusic;
import org.json.*;
import java.io.*;
import java.net.URI;
import java.util.*;

/** Short-lived source capabilities stay in memory, outside tracks, backups and history. */
final class AudioAddress {
    final String url;final Map<String,String> headers;final long expires;
    private AudioAddress(String url,Map<String,String> headers,long expires){this.url=url;this.headers=Collections.unmodifiableMap(headers);this.expires=expires;}
    static AudioAddress parse(String text,long now)throws IOException{
        try{JSONObject j=new JSONObject(text);String url=j.getString("url");URI u=new URI(url);String host=u.getHost();
            if(!"https".equals(u.getScheme())||host==null||!host.toLowerCase(Locale.ROOT).endsWith(".googlevideo.com")||u.getUserInfo()!=null||(u.getPort()!=-1&&u.getPort()!=443)||u.getFragment()!=null)throw new IOException("OpenMusic: La fuente devolvió una dirección de audio no válida.");
            if(!Arrays.asList("m4a","webm","mp4").contains(j.optString("ext")))throw new IOException("OpenMusic: Formato multimedia no compatible.");
            long expires=Math.min(j.getLong("expires"),now+900);if(expires<=now+30)throw new IOException("OpenMusic: El enlace de audio expiró. Volvé a tocar Play.");
            Map<String,String> headers=new HashMap<>();JSONObject h=j.optJSONObject("headers");if(h!=null){Iterator<String> keys=h.keys();while(keys.hasNext()){String key=keys.next();String value=h.getString(key);String lower=key.toLowerCase(Locale.ROOT);if(!Arrays.asList("user-agent","accept","accept-language","referer","origin").contains(lower))continue;if(value.length()>2048||value.contains("\r")||value.contains("\n"))throw new IOException("OpenMusic: La fuente devolvió cabeceras no válidas.");headers.put(key,value);}}
            return new AudioAddress(url,headers,expires);
        }catch(IOException e){throw e;}catch(Exception e){throw new IOException("OpenMusic: No se pudo interpretar el enlace de audio.",e);}
    }
}
