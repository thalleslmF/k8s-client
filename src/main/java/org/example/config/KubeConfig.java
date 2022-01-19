package org.example.config;

import org.apache.commons.codec.binary.Base64;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
public class KubeConfig {
    private final List<Object> contexts;
    private final List<Object> clusters;
    private final List<Object> users;
    private final String currentContextName;
    private Map<String,Object> currentContext;
    private Map<String,Object> currentCluster;
    private Map<String, Object> currentUser;
    private byte[] caAuthority;
    private String server;
    private byte[] clientKey;
    private byte[] clientCert;

    public List<Object> getContexts() {
        return contexts;
    }

    public List<Object> getClusters() {
        return clusters;
    }

    public List<Object> getUsers() {
        return users;
    }

    public String getCurrentContextName() {
        return currentContextName;
    }

    public KubeConfig(List<Object> contexts, List<Object> clusters, List<Object> users, String currentContext) {
        this.contexts = contexts;
        this.clusters = clusters;
        this.users = users;
        this.currentContextName = currentContext;
        this.fillMissingProperties();
    }

    public void setServer(String server) {
        this.server = server;
    }


    private void fillMissingProperties() {
        this.currentContext  = find(currentContextName, this.contexts);
        this.currentCluster  = find(currentContextName, this.clusters);
        this.currentUser  = find(currentContextName, this.users);
        this.caAuthority = getCAData();
        this.server = getServer();
        this.clientKey = getClientKey();
        this.clientCert = getClientCert();
    }

    public byte[] getClientCert() {
        var user = (Map<String,Object>) this.currentUser.get("user");
        var keyPath = (String) user.get("client-certificate");
        if (keyPath == null) {
            return getRawClientCert();
        }
        try {
            return Files.readAllBytes(Path.of(keyPath));
        } catch (IOException e) {
            throw new RuntimeException("Failed to load client cert", e);
        }
    }

    private byte[] getRawClientCert() {
        var user = (Map<String,Object>) this.currentUser.get("user");
        var base64clientCert = (String) user.get("client-certificate-data");
        return Base64.decodeBase64(base64clientCert);
    }

    public byte[] getClientKey() {
        try {
            var user = (Map<String,Object>) this.currentUser.get("user");
            var keyPath = (String) user.get("client-key");
            if (keyPath == null) {
                return   getRawKeyData();
            }
            return Files.readAllBytes(Path.of(keyPath));
        }catch (Exception e) {
            throw new RuntimeException("Failed to load client key", e);
        }
    }

    private byte[] getRawKeyData() {
        var user = (Map<String,Object>) this.currentUser.get("user");
        var base64clientKey = (String) user.get("client-key-data");
        return Base64.decodeBase64(base64clientKey);
    }

    public String getServer() {
        var cluster =  (Map<String,Object>) this.currentCluster.get("cluster");
        return (String) cluster.get("server");
    }

    public void setCurrentUser(Map<String, Object> currentUser) {
        this.currentUser = currentUser;
    }

    public byte[] getCaAuthority() {
        return caAuthority;
    }

    public void setCaAuthority(byte[] caAuthority) {
        this.caAuthority = caAuthority;
    }

    private byte[] getCAData() {
        byte[] caData;
         try {

            var clusterObj = (Map<String,Object>) this.currentCluster.get("cluster");
            var caPath = (String)clusterObj.get("certificate-authority");
            if (caPath == null) {
                return getRawCABytes();
            }
            caData = Files.readAllBytes(Path.of(caPath));
        }catch (Exception exception) {
            throw new RuntimeException("Could not load ca data", exception);
        }
        return caData;
    }

    private byte[] getRawCABytes() {
        var clusterObj = (Map<String, Object>) this.currentCluster.get("cluster");
        var base64CaData = (String) clusterObj.get("certificate-authority-data");
        return Base64.decodeBase64(base64CaData);
    }
    public Map<String, Object> getCurrentUser() {
        return currentUser;
    }

    private Map<String,Object> find(String reference, List<Object> objects) {
        for( var object : objects) {
            var mapObject = (Map<String,Object>) object;
            var nameCompare = mapObject.get("name");
            if (nameCompare.equals(reference)) {
                return mapObject;
            }
        }
        return null;
    }

    public Map<String, Object> getCurrentContext() {
        return currentContext;
    }

    public void setCurrentContext(Map<String, Object> currentContext) {
        this.currentContext = currentContext;
    }

    public Map<String, Object> getCurrentCluster() {
        return currentCluster;
    }

    public void setCurrentCluster(Map<String, Object> currentCluster) {
        this.currentCluster = currentCluster;
    }
}
