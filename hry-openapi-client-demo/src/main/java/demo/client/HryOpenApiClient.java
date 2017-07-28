package demo.client;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.*;
import java.security.cert.CertificateException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.net.ssl.SSLContext;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContexts;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

import demo.encrypt.Base64;
import demo.util.JsonUtils;

/**
 * @author jiankuan
 *         21/10/2015.
 */
@SuppressWarnings("unused")
public class HryOpenApiClient {

    private static final String HMAC_SHA1_ALGORITHM = "HmacSHA1";

    private String host;

    private String apiKey;

    private String apiSecret;

    private HttpClient httpClient;
    
    final private String keyStoreType;
    final private String keyStorePath;
    final private String keyStorePassword;

    private static final String API_PATH = "/service";

//    private String trustStorePath;
//    private String trustStorePassword;

    private Logger logger = LoggerFactory.getLogger(HryOpenApiClient.class);

    private ObjectMapper objectMapper = new ObjectMapper();

    // a handy simplified version
    public HryOpenApiClient(String host, String apiKey, String apiSecret ) {
    	this(
    			host,
    			apiKey,
    			apiSecret,
    			System.getProperty("javax.net.ssl.keyStoreType", "jks"),
    			System.getProperty("javax.net.ssl.keyStore"),
    			System.getProperty("javax.net.ssl.keyStorePassword")
    	);
    }

	public HryOpenApiClient(String host, String apiKey, String apiSecret, String keyStoreType, String keyStorePath, String keyStorePassword ) {
        this.host = host;
        this.apiKey = apiKey;
        this.apiSecret = apiSecret;
        this.keyStoreType = keyStoreType;
        this.keyStorePath = keyStorePath;
        this.keyStorePassword = keyStorePassword;

        try {

            // load client certificate
        	KeyStore ks = KeyStore.getInstance(this.keyStoreType);
        	ks.load( new FileInputStream( new File( this.keyStorePath)), this.keyStorePassword.toCharArray());

        	// Not calling loadTrustStore() in order to use the settings in $JAVA_OPTS implicitly
        	// -Djavax.net.ssl.trustStore=/path/to/app-trust-store
        	// -Djavax.net.ssl.trustStorePassword=password
        	
        	SSLContext context = SSLContexts.custom()
        			.loadKeyMaterial(ks, this.keyStorePassword.toCharArray())
        			.build();
        
            SSLConnectionSocketFactory sf = new SSLConnectionSocketFactory(
                    context,
                    new String[] {"TLSv1.2"},
                    null,
                    SSLConnectionSocketFactory.getDefaultHostnameVerifier());

            httpClient = HttpClients.custom().setSSLSocketFactory(sf).build();

        } catch (
        		KeyStoreException |
                        NoSuchAlgorithmException |
                        CertificateException |
                        IOException |
                        UnrecoverableKeyException |
                        KeyManagementException
        		e) {
            e.printStackTrace();
        }
    }

    public String getDailyTaskList(String hryId , String token) {
        Map<String, Object> params = new HashMap<>();

        try{
            params.put("hryId", hryId);
            params.put("token", token);
            params.put("serviceName", "getDailyTaskList");
        } catch (Exception e){
            e.printStackTrace();
        }

        return post(params);
    }

    String post(Map<String, Object> params) {
        String basePath = "/";
        URIBuilder builder = new URIBuilder().setScheme("https")
                .setHost(host)
                .setPath(basePath + API_PATH);
        // clear the params with empty value
        Map<String, Object> trimmedParams = new HashMap<>();
        for (String key: params.keySet()) {
            if (params.get(key) != null) {
                trimmedParams.put(key, params.get(key));
            }
        }
        addRequiredParams("POST", API_PATH, trimmedParams, apiKey, apiSecret);

        try {
            URI uri = builder.build();
            RequestBuilder requestBuilder = RequestBuilder.post();
            requestBuilder.setUri(uri);
            StringEntity se = new StringEntity(JsonUtils.obj2JsonString(trimmedParams),"UTF-8");
            se.setContentType("application/json");
            requestBuilder.setEntity(se);
            HttpUriRequest request = requestBuilder.build();

            System.out.println("request content: " + EntityUtils.toString(se, "UTF-8"));

            request.setHeader("content-type","application/json");
            HttpResponse resp = httpClient.execute(request);
            if (resp.getStatusLine().getStatusCode() >= 300) {
                System.err.println("Something wrong: " + resp.getStatusLine().toString());
            }
            BufferedReader input = new BufferedReader(new InputStreamReader(resp.getEntity().getContent(), "UTF-8"));
            StringBuilder sb = new StringBuilder();
            char[] buf = new char[1000];
            int count;
            while ((count = input.read(buf)) > 0) {
                sb.append(buf, 0, count);
            }
            return sb.toString();
        } catch (IOException | URISyntaxException e) {
           throw new RuntimeException(e);
        }
    }

    
    void addRequiredParams(String method, String path, Map<String, Object> params, String apiKey, String apiSecret) {
        params.put("key", apiKey);
        String ts = String.valueOf(System.currentTimeMillis());
        params.put("ts", ts);

        // 计算签名时把非string对象转成json 字符串
        /**
         * 为了保证签名与服务端一致
         * 1.使用 jackson
         * 2.配置以下两项,保证解析顺序
         * objectMapper.configure(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY, true);
         * objectMapper.configure(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS, true);
         */
        Map<String, String> sigMap = new HashMap<>();
        for (String key : params.keySet()) {
            if(params.get(key) instanceof  String){
                sigMap.put(key, params.get(key).toString());
            } else
                sigMap.put(key, JsonUtils.obj2JsonString(params.get(key)));
        }
        
        
        String sig = getSig(method, path, apiSecret, sigMap);
        params.put("sig", sig);
    }
    
    String getSig(String method, String path, String apiSecret, Map<String, String> params) {
        StringBuilder sb = new StringBuilder();
        Set<String> keySet = new TreeSet<>(params.keySet());
        for (String key: keySet) {
            String value = params.get(key);
            if (value == null) {
                continue;
            }
            sb.append(key);
            sb.append("=");
            sb.append(params.get(key));
            sb.append("&");
        }
        sb.setLength(sb.length() - 1); // trim the last "&"
        String unifiedString = method.toUpperCase() + ":" + path + ":" + sb.toString();
        logger.debug("unified string: " + unifiedString);

        // calc hmac sha1
        try {
            SecretKeySpec secret = new SecretKeySpec(apiSecret.getBytes(), HMAC_SHA1_ALGORITHM);
            Mac mac = Mac.getInstance(HMAC_SHA1_ALGORITHM);
            mac.init(secret);
            byte[] hmac = mac.doFinal(unifiedString.getBytes()); // UTF8

            // base64 encode the hmac
            String sig = Base64.getEncoder().encodeToString(hmac);
            logger.debug("signature: " + sig);
            return sig;
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new RuntimeException(e);
        }
    }
}
