package com.picolas.openmusic;
import java.util.Locale;
/** Reject successful HTTP error pages before they reach the decoder. */
final class AudioContent {
 static boolean accepted(String type){if(type==null)return true;String t=type.toLowerCase(Locale.ROOT).split(";",2)[0].trim();return !t.equals("text/html")&&!t.equals("application/xhtml+xml")&&!t.equals("application/json")&&!t.equals("text/json");}
}
