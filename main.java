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
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.HttpClients;

public class main {
    public static void main(String[] args) throws Exception {
        String keyPassphrase = "1900008831";

        KeyStore keyStore = KeyStore.getInstance("PKCS12");
        keyStore.load(new FileInputStream("client.p12"), keyPassphrase.toCharArray());

        SSLContext sslContext = SSLContexts.custom().loadKeyMaterial(keyStore, null).build();

        HttpClient httpClient = HttpClients.custom().setSSLContext(sslContext).build();
        //HttpResponse response = httpClient.execute(new HttpGet("https://example.com"));

        Map<String, String> arguments = new HashMap<>();
        arguments.put("username", "root");
        arguments.put("password", "sjh76HSn!"); // This is a fake password obviously
        StringJoiner sj = new StringJoiner("&");
        for (Map.Entry<String, String> entry : arguments.entrySet()) {
            sj.add(URLEncoder.encode(entry.getKey(), "UTF-8") + "=" + URLEncoder.encode(entry.getValue(), "UTF-8"));
        }

        byte[] out = sj.toString().getBytes(StandardCharsets.UTF_8);
        int length = out.length;

        URL url = new URL("https://api.mch.weixin.qq.com/secapi/mch/uploadmedia");
        URLConnection con = url.openConnection();
        HttpURLConnection http = (HttpURLConnection) con;
        http.setRequestMethod("POST"); // PUT is another valid option
        http.setDoOutput(true);

        http.setFixedLengthStreamingMode(length);
        http.setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
        http.connect();
        try (OutputStream os = http.getOutputStream()) {
            os.write(out);
        }
    }

    public static String doPost(SSLContext ctx, HostnameVerifier hostnameVerifier, String url,
            Map<String, String> params, Map<String, File> fileParams, String charset, int connectTimeout,
            int readTimeout) throws IOException {
        String boundary = System.currentTimeMillis() + "";
        HttpURLConnection conn = null;
        OutputStream out = null;
        String rsp = null;
        long start = System.currentTimeMillis();
        try {
            try {
                String ctype = "multipart/form-data;boundary=" + boundary + ";charset=" + charset;
                conn = getConnection(ctx, hostnameVerifier, new URL(url), "POST", ctype);
                conn.setConnectTimeout(connectTimeout);
                conn.setReadTimeout(readTimeout);
            } catch (IOException e) {
                throw e;
            }

            try {
                out = conn.getOutputStream();

                byte[] entryBoundaryBytes = ("\r\n--" + boundary + "\r\n").getBytes(charset);

                // 组装文本请求参数
                Set<Map.Entry<String, String>> textEntrySet = params.entrySet();
                for (Map.Entry<String, String> textEntry : textEntrySet) {
                    byte[] textBytes = getTextEntry(textEntry.getKey(), textEntry.getValue(), charset);
                    out.write(entryBoundaryBytes);
                    out.write(textBytes);
                }

                // 组装文件请求参数
                Set<Map.Entry<String, File>> fileEntrySet = fileParams.entrySet();
                for (Map.Entry<String, File> fileEntry : fileEntrySet) {
                    File fileItem = fileEntry.getValue();
                    byte[] fileBytes = getFileEntry(fileEntry.getKey(), fileItem.getName(), "image/png", charset);
                    out.write(entryBoundaryBytes);
                    out.write(fileBytes);
                    out.write(getContent(fileItem));
                }

                // 添加请求结束标志
                byte[] endBoundaryBytes = ("\r\n--" + boundary + "--\r\n").getBytes(charset);
                out.write(endBoundaryBytes);
                rsp = getResponseAsString(conn, "UTF-8");
            } catch (IOException e) {
                throw e;
            }

        } finally {
            if (out != null) {
                out.close();
            }
            if (conn != null) {
                conn.disconnect();
            }
            long end = System.currentTimeMillis();
        }

        return rsp;
    }

    private static HttpURLConnection getConnection(SSLContext sslContext, HostnameVerifier hostnameVerifier, URL url,
            String method, String ctype) throws IOException {
        HttpURLConnection conn = null;
        if ("https".equals(url.getProtocol())) {
            HttpsURLConnection connHttps = (HttpsURLConnection) url.openConnection();
            if (sslContext != null) {
                connHttps.setSSLSocketFactory(sslContext.getSocketFactory());
            }
            if (hostnameVerifier != null) {
                connHttps.setHostnameVerifier(hostnameVerifier);
            }
            conn = connHttps;
        } else {
            conn = (HttpURLConnection) url.openConnection();
        }

        conn.setRequestMethod(method);
        conn.setDoInput(true);
        conn.setDoOutput(true);
        conn.setRequestProperty("Accept", "text/xml,text/javascript,text/html,*/*");
        conn.setRequestProperty("User-Agent", "aop-sdk-java");
        conn.setRequestProperty("Content-Type", ctype);
        return conn;
    }

    private static byte[] getTextEntry(String fieldName, String fieldValue, String charset) throws IOException {
        StringBuilder entry = new StringBuilder();
        entry.append("Content-Disposition:form-data;name=\"");
        entry.append(fieldName);
        entry.append("\"\r\n\r\n");
        //        entry.append("\"\r\nContent-Type:text/plain\r\n\r\n");
        entry.append(fieldValue);
        return entry.toString().getBytes(charset);
    }

    private static byte[] getFileEntry(String fieldName, String fileName, String mimeType, String charset)
            throws IOException {
        StringBuilder entry = new StringBuilder();
        entry.append("Content-Disposition:form-data;name=\"");
        entry.append(fieldName);
        entry.append("\";filename=\"");
        entry.append(fileName);
        entry.append("\"\r\nContent-Type:");
        entry.append(mimeType);
        entry.append("\r\n\r\n");
        return entry.toString().getBytes(charset);
    }

    protected static String getResponseAsString(HttpURLConnection conn, String responseCharset) throws IOException {
        String charset = getResponseCharset(conn.getContentType(),responseCharset);
        InputStream es = conn.getErrorStream();
        if (es == null) {
            return getStreamAsString(conn.getInputStream(), charset);
        } else {
            String msg = getStreamAsString(es, charset);
            if (isEmpty(msg)) {
                throw new IOException(conn.getResponseCode() + ":" + conn.getResponseMessage());
            } else {
                throw new IOException(msg);
            }
        }
    }

    private static String getResponseCharset(String ctype, String defaultCharset) {
        String charset = defaultCharset;
        if(charset == null){
            charset = "UTF-8";
        }
        if (!isEmpty(ctype)) {
            String[] params = ctype.split(";");
            for (String param : params) {
                param = param.trim();
                if (param.startsWith("charset")) {
                    String[] pair = param.split("=", 2);
                    if (pair.length == 2) {
                        if (!isEmpty(pair[1])) {
                            charset = pair[1].trim();
                        }
                    }
                    break;
                }
            }
        }

        return charset;
    }

    private static String getStreamAsString(InputStream stream, String charset) throws IOException {
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(stream, charset));
            StringWriter writer = new StringWriter();

            char[] chars = new char[256];
            int count = 0;
            while ((count = reader.read(chars)) > 0) {
                writer.write(chars, 0, count);
            }

            return writer.toString();
        } finally {
            if (stream != null) {
                stream.close();
            }
        }
    }

    private static boolean isEmpty(String value) {
        int strLen;
        if (value == null || (strLen = value.length()) == 0) {
            return true;
        }
        for (int i = 0; i < strLen; i++) {
            if ((Character.isWhitespace(value.charAt(i)) == false)) {
                return false;
            }
        }
        return true;
    }

    // private static Map hashMap(Object... objects) {
    //     Map result = new LinkedHashMap();

    //     for(int i = 0; i < objects.length / 2; ++i) {
    //         result.put(objects[2 * i], objects[2 * i + 1]);
    //     }

    //     return result;
    // }

    public static byte[] getContent(File file) throws IOException {
        byte[] content = null;
        if (file != null && file.exists()) {
            InputStream in = null;
            ByteArrayOutputStream out = null;

            try {
                in = new FileInputStream(file);
                out = new ByteArrayOutputStream();
                int ch;
                while ((ch = in.read()) != -1) {
                    out.write(ch);
                }
                content = out.toByteArray();
            } finally {
                if (out != null) {
                    out.close();
                }
                if (in != null) {
                    in.close();
                }
            }
        }
        return content;
    }
}