package com.github.btbxbob;

import javax.net.ssl.SSLContext;
import java.security.KeyStore;
import java.io.*;

import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.HttpEntity;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.util.EntityUtils;

public class App 
{
    public static void wxpayMediaUpload(
        String mch_id,
        String media_hash,
        String sign,
        String media,
        String p12cert
    ) throws Exception
    {
                //载入证书
                String keyPassphrase = mch_id;

                //String text = new Scanner(AppropriateClass.class.getResourceAsStream("foo.txt"), "UTF-8").useDelimiter("\\A").next();
                
                KeyStore keyStore = KeyStore.getInstance("PKCS12");
                keyStore.load(App.class.getResourceAsStream("/P12/"+p12cert), keyPassphrase.toCharArray());
        
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
                //String filename="a6b95b8f50d400a08fae3e7ac46b5f891f025a.png";
                InputStream file = App.class.getResourceAsStream("/MEDIA/"+media);
        
                HttpEntity data = MultipartEntityBuilder
                                .create()
                                .setMode(HttpMultipartMode.BROWSER_COMPATIBLE)
                                .addBinaryBody("media", file, ContentType.DEFAULT_BINARY, media)
                                .addTextBody("mch_id", mch_id, ContentType.DEFAULT_TEXT)
                                .addTextBody("media_hash", media_hash, ContentType.DEFAULT_TEXT)
                                .addTextBody("sign", sign, ContentType.DEFAULT_TEXT)
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
