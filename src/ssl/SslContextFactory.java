package ssl;

import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.Security;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;

public class SslContextFactory {

    /**
     * Protocol to use.
     */
    private static final String PROTOCOL = "TLS";

    private static final String KEY_MANAGER_FACTORY_ALGORITHM;

    static {
        String algorithm = Security
                .getProperty("ssl.KeyManagerFactory.algorithm");
        if (algorithm == null) {
            algorithm = KeyManagerFactory.getDefaultAlgorithm();
        }

        KEY_MANAGER_FACTORY_ALGORITHM = algorithm;
    }

    /**
     * Server certificate keystore file name.
     */
    private static String keystore = "bogus.cert";

    // NOTE: The keystore was generated using keytool:
    //   keytool -genkey -alias bogus -keysize 512 -validity 3650
    //           -keyalg RSA -dname "CN=bogus.com, OU=XXX CA,
    //               O=Bogus Inc, L=Stockholm, S=Stockholm, C=SE"
    //           -keypass boguspw -storepass boguspw -keystore bogus.cert

    /**
     * Keystore password.
     */
    private static char[] keystorePassword = { 'b', 'o', 'g', 'u', 's', 'p', 'w' };

    private static SSLContext serverInstance = null;
    
    private static SSLContext clientInstance = null;

    /**
     * Get SSLContext singleton.
     *
     * @return SSLContext
     * @throws java.security.GeneralSecurityException
     *
     */
    public static SSLContext getInstance(boolean server)
            throws GeneralSecurityException {
        SSLContext retInstance = null;
        if (server) {
            synchronized(SslContextFactory.class) {
                if (serverInstance == null) {
                    try {
                        serverInstance = createServerSslContext();
                    } catch (Exception ioe) {
                        throw new GeneralSecurityException(
                                "Can't create Server SSLContext:" + ioe);
                    }
                }
            }
            retInstance = serverInstance;
        } else {
            synchronized (SslContextFactory.class) {
                if (clientInstance == null) {
                    clientInstance = createClientSslContext();
                }
            }
            retInstance = clientInstance;
        }
        return retInstance;
    }

    private static SSLContext createServerSslContext()
            throws GeneralSecurityException, IOException {
        // Create keystore
        KeyStore ks = KeyStore.getInstance("JKS");
        InputStream in = null;
        try {
            in = SslContextFactory.class
                    .getResourceAsStream(keystore);
            ks.load(in, keystorePassword);
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException ignored) {
                }
            }
        }

        // Set up key manager factory to use our key store
        KeyManagerFactory kmf = KeyManagerFactory
                .getInstance(KEY_MANAGER_FACTORY_ALGORITHM);
        kmf.init(ks, keystorePassword);

        // Initialize the SSLContext to work with our key managers.
        SSLContext sslContext = SSLContext.getInstance(PROTOCOL);
        sslContext.init(kmf.getKeyManagers(),
                TrustManagerFactory.X509_MANAGERS, null);

        return sslContext;
    }

    private static SSLContext createClientSslContext()
            throws GeneralSecurityException {
        SSLContext context = SSLContext.getInstance(PROTOCOL);
        context.init(null, TrustManagerFactory.X509_MANAGERS, null);
        return context;
    }

	public static String getKeystore() {
		return keystore;
	}

	public static void setKeystore(String keystore) {
		SslContextFactory.keystore = keystore;
	}

	public static char[] getKeystorePassword() {
		return keystorePassword;
	}

	public static void setKeystorePassword(char[] keystorePassword) {
		SslContextFactory.keystorePassword = keystorePassword;
	}
    
    

}
