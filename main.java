import java.util.Map;
import java.util.StringJoiner;
import java.util.HashMap;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import java.net.URLConnection;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;

public class main
{
	public static void main(String[] args)throws Exception
    {
        Map<String,String> arguments = new HashMap<>();
        arguments.put("username", "root");
        arguments.put("password", "sjh76HSn!"); // This is a fake password obviously
        StringJoiner sj = new StringJoiner("&");
        for(Map.Entry<String,String> entry : arguments.entrySet())
        {
            sj.add(URLEncoder.encode(entry.getKey(), "UTF-8") + "=" 
            + URLEncoder.encode(entry.getValue(), "UTF-8"));
        }
        
        byte[] out = sj.toString().getBytes(StandardCharsets.UTF_8);
        int length = out.length;

        URL url = new URL("https://api.mch.weixin.qq.com/secapi/mch/uploadmedia");
        URLConnection con = url.openConnection();
        HttpURLConnection http = (HttpURLConnection)con;
        http.setRequestMethod("POST"); // PUT is another valid option
        http.setDoOutput(true);

        http.setFixedLengthStreamingMode(length);
        http.setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
        http.connect();
        try(OutputStream os = http.getOutputStream()) {
            os.write(out);
        }
    }
}