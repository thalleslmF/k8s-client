package org.example.client;

import io.kubernetes.client.util.SSLUtils;
import okhttp3.OkHttpClient;
import okhttp3.internal.tls.OkHostnameVerifier;
import org.example.config.KubeConfig;

import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.util.Collection;

public class ClientBuilder {

    private String server = "localhost";
    private byte[] clientKey;
    private byte[] clientCert;
    private byte[] caCert;
    private OkHttpClient client;
    private KeyManager[] keyManagers;

    public ClientBuilder withKubeConfig(KubeConfig kubeConfig) {
        this.server = kubeConfig.getServer();
        this.clientKey = kubeConfig.getClientKey();
        this.clientCert = kubeConfig.getClientCert();
        this.caCert = kubeConfig.getCaAuthority();
        return this;
    }

    public K8sClient build() {
        SSLContext context = null;
        try {
            if (this.clientCert != null && this.clientKey != null) {
               applySSLSettings();
            } else {
                this.client = new OkHttpClient().newBuilder().build();
            }
        }catch (Exception exception) {
            throw new RuntimeException("Failed to apply tls settings", exception);
        }
        return new K8sClient(this.server, this.client);
    }

    private void applySSLSettings() throws GeneralSecurityException, IOException {

        keyManagers = SSLUtils.keyManagers(this.clientCert, this.clientKey, "RSA", "" , null, null);
        TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        if (this.caCert == null ) {
            trustManagerFactory.init((KeyStore)null);
        }else {
            CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
            Collection<? extends java.security.cert.Certificate> certificates =
                    certificateFactory.generateCertificates(new ByteArrayInputStream(this.caCert));
            if (certificates.isEmpty()) {
                throw new IllegalArgumentException("expected non-empty set of trusted certificates");
            }
            KeyStore caKeyStore = newEmptyKeyStore();
            int index = 0;
            for (Certificate certificate : certificates) {
                String certificateAlias = "ca" + Integer.toString(index++);
                caKeyStore.setCertificateEntry(certificateAlias, certificate);
            }
            trustManagerFactory.init(caKeyStore);
        }
        var trustManagers = trustManagerFactory.getTrustManagers();
        var hostnameVerifier = OkHostnameVerifier.INSTANCE;

        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(this.keyManagers, trustManagers, new SecureRandom());
        this.client = new OkHttpClient()
                .newBuilder()
                .sslSocketFactory(sslContext.getSocketFactory(), (X509TrustManager) trustManagers[0])
                .hostnameVerifier(hostnameVerifier)
                .build();
    }

    private void createSSLContext(TrustManagerFactory trustManagerFactory) {

    }


    private KeyStore newEmptyKeyStore() throws GeneralSecurityException {
        try {
            KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
            keyStore.load(null, null);
            return keyStore;
        } catch (IOException e) {
            throw new AssertionError(e);
        }
    }
}
