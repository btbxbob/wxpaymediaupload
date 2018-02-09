import java.util.Map;
import java.util.StringJoiner;
import java.util.HashMap;
import java.util.Set;
import java.util.LinkedHashMap;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import java.security.KeyStore;
import java.net.URLConnection;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;

import org.apache.http.ssl.SSLContexts;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.HttpEntity;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;

public class main {
    public static void main(String[] args) throws Exception {
        //载入证书
        String keyPassphrase = "1900008831";

        KeyStore keyStore = KeyStore.getInstance("PKCS12");
        keyStore.load(new FileInputStream("client.p12"), keyPassphrase.toCharArray());

        SSLContext sslContext =  SSLContextBuilder
                                .create()
                                .loadKeyMaterial(keyStore, keyPassphrase.toCharArray())
                                .setProtocol("TLSv1.2")
                                .build();

        //证书塞进httpClient里
        HttpClient httpClient = HttpClientBuilder
                                .create()
                                .setSSLContext(sslContext)
                                .build();

        //组装参数
        //读入文件
        File file = new File(main.class.getResource("a6b95b8f50d400a08fae3e7ac46b5f891f025a.png").getFile());
        Map<String, String> arguments = new HashMap<>();

        HttpEntity data = MultipartEntityBuilder
                        .create()
                        .setMode(HttpMultipartMode.BROWSER_COMPATIBLE)
                        .addBinaryBody("media", file, ContentType.DEFAULT_BINARY, file.getName())
                        .addTextBody("mch_id", "1900008831", ContentType.DEFAULT_TEXT)
                        .addTextBody("media_hash", "f31edc8377e4d244e3a3cbde4b19254e", ContentType.DEFAULT_TEXT)
                        .addTextBody("sign", "D24C62D7B8AE0D470191BE0F161B8A36", ContentType.DEFAULT_TEXT)
                        .build();

        //构造请求
        HttpUriRequest request = RequestBuilder
                                .post("https://api.mch.weixin.qq.com/secapi/mch/uploadmedia")
                                .setEntity(data)
                                .build();
        
        //异常处理
        ResponseHandler<String> responseHandler = response -> {
            int status = response.getStatusLine().getStatusCode();
            if (status >= 200 && status < 300) {
                HttpEntity entity = response.getEntity();
                return entity != null ? EntityUtils.toString(entity) : null;
            } else {
                throw new ClientProtocolException("Unexpected response status: " + status);
            }
        };
        
        //请求，打印
        String responseBody = httpClient.execute(request, responseHandler);
        System.out.println("----------------------------------------");
        System.out.println(responseBody);

    }
}