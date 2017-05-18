package demo.client;

import java.io.IOException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.cli.*;

/**
 * vm:
 * -Djavax.net.ssl.trustStore=hry-openapi-client-demo/certs/jssecacerts -Djavax.net.ssl.trustStorePassword=changeit
 * args:
 * -host zopenapi.hairongyi.com:443 -key abc1 -secret 123 -keystore hry-openapi-client-demo/certs/client1.p12 -keypass 123456 -keytype PKCS12
 */
public class MainApp {

    public static void main(String[] args) throws KeyStoreException, CertificateException, NoSuchAlgorithmException, IOException {

        Options options = new Options();
        options.addOption("host", true, "hry openapi host name");
        options.addOption("key", true, "api key");
        options.addOption("secret", true, "api secret");
        options.addOption("keytype",true,"keystore type, use -Djavax.net.ssl.keyStoreType if absent, falls back to 'jks'");
        options.addOption("keystore", true, "keystor path, use -Djavax.net.ssl.keyStore if absent");
        options.addOption("keypass", true, "keystore password, use -Djavax.net.ssl.keyStorePassword if absent");
        options.addOption("h", "help", false, "show usage");

        CommandLineParser commandLineParser = new DefaultParser();
        CommandLine commandLine = null;
        try {
            commandLine = commandLineParser.parse(options, args);
        } catch (ParseException e) {
            showUsage(options);
            System.exit(-1);
        }

        if (commandLine.hasOption("h")) {
            showUsage(options);
            System.exit(0);
        }

        Map<String, String> params = new HashMap<>();
        String[] paramNames = new String[]{"host", "key", "secret"};
        for (String paramName: paramNames) {
            if (commandLine.hasOption(paramName)) {
                params.put(paramName, commandLine.getOptionValue(paramName));
            } else {
                System.err.println("缺少必要参数" + paramName);
                showUsage(options);
                System.exit(-1);
            }
        }

        // optional params
        params.put("keytype", commandLine.getOptionValue("keytype", System.getProperty("javax.net.ssl.keyStoreType","jks")));
        params.put("keystore", commandLine.getOptionValue("keystore", System.getProperty("javax.net.ssl.keyStore")));
        params.put("keypass", commandLine.getOptionValue("keypass", System.getProperty("javax.net.ssl.keyStorePassword")) );
        
        HryOpenApiClient ac = new HryOpenApiClient(
                params.get("host"),
                params.get("key"),
                params.get("secret"),
                params.get("keytype"),
                params.get("keystore"),
                params.get("keypass")
                );
        
        // invoke the api

        System.out.println(ac.getDailyTaskList("10007756","123456"));
    }

    private static void showUsage(Options options) {
        HelpFormatter helpFormatter = new HelpFormatter();
        helpFormatter.printHelp("openapi-client",  options);
    }
}
