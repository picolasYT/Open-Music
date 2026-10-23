package com.picolas.openmusic;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.media3.datasource.*;
import android.net.Uri;
import okhttp3.*;
import okhttp3.mockwebserver.*;
import okhttp3.tls.*;
import org.junit.Test;
import org.junit.runner.RunWith;
import java.util.*;
import java.util.concurrent.TimeUnit;
import static org.junit.Assert.*;
/** Real local HTTPS, with a test-only CA; production trust settings are unchanged. */
@RunWith(AndroidJUnit4.class)
public class AudioHttpTest {
 @Test public void oneUserAgentAndRejectsHtml206OverRealHttps()throws Exception{
  HeldCertificate cert=new HeldCertificate.Builder().commonName("localhost").addSubjectAlternativeName("localhost").build();
  HandshakeCertificates serverTls=new HandshakeCertificates.Builder().heldCertificate(cert).build();
  HandshakeCertificates clientTls=new HandshakeCertificates.Builder().addTrustedCertificate(cert.certificate()).build();
  try(MockWebServer server=new MockWebServer()){
   server.useHttps(serverTls.sslSocketFactory(),false);server.start();
   // Software-only emulators can take tens of seconds to initialize Conscrypt.
   OkHttpClient client=new OkHttpClient.Builder().sslSocketFactory(clientTls.sslSocketFactory(),clientTls.trustManager()).callTimeout(60,TimeUnit.SECONDS).connectTimeout(60,TimeUnit.SECONDS).readTimeout(60,TimeUnit.SECONDS).build();
   server.enqueue(new MockResponse().setHeader("Content-Type","audio/mp4").setBody("test-audio-bytes"));
   DataSource source=PlaybackService.httpFactory(client).createDataSource();
   DataSpec spec=new DataSpec.Builder().setUri(Uri.parse(server.url("/audio").toString())).setHttpRequestHeaders(Collections.singletonMap("User-Agent",NewPipeBackend.USER_AGENT)).build();
   try{source.open(spec);byte[] buffer=new byte[32];assertEquals(16,source.read(buffer,0,buffer.length));}finally{source.close();}
   RecordedRequest request=server.takeRequest(5,TimeUnit.SECONDS);assertNotNull(request);assertEquals(Collections.singletonList(NewPipeBackend.USER_AGENT),request.getHeaders().values("User-Agent"));
   server.enqueue(new MockResponse().setResponseCode(206).setHeader("Content-Type","text/html").setBody("<html>Site Unavailable</html>"));
   try{source.open(spec);fail("An HTTP 206 HTML page must not reach the decoder");}catch(HttpDataSource.InvalidContentTypeException expected){assertTrue(YouTubeAudio.userMessage(expected).contains("página de error"));}finally{source.close();client.connectionPool().evictAll();client.dispatcher().executorService().shutdown();}
  }
 }
}
