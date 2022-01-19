package org.example.config;

import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class KubeConfigUtils {

    public static KubeConfig load(){
        try {
            var configFile = find();
            var kubeConfigObject = parseKubeConfig(configFile);
            var contexts = (List<Object>) kubeConfigObject.get("contexts");
            var clusters = (List<Object>) kubeConfigObject.get("clusters");
            var users = (List<Object>) kubeConfigObject.get("users");
            var currentContext = (String) kubeConfigObject.get("current-context");
            return new KubeConfig(contexts, clusters, users, currentContext);
        }catch(Exception exception){
            throw new RuntimeException("Failed to load kubeconfig", exception);
        }
    }

    private static Map<String,Object> parseKubeConfig(File configFile) throws FileNotFoundException {
        Yaml yaml = new Yaml();
        InputStream inputStream = new FileInputStream(configFile);
        return yaml.load(inputStream);
    }

    public static File find() {
        var kubeConfig = findByEnv();
        if (!kubeConfig.isPresent()){
            kubeConfig = findByHomeDir();
            if (!kubeConfig.isPresent()) {
                throw new RuntimeException("Could not load kubeconfig");
            }
        }
        return kubeConfig.get();
    }

    private static Optional<File> findByHomeDir() {
        var homePath = System.getenv("HOME");
        var kubeDir = new File(homePath, new File(".kube","config").getPath());
        if (!kubeDir.exists()){
            return Optional.empty();
        }
        return Optional.of(kubeDir);
    }

    private static Optional<File> findByEnv() {
        var path = System.getenv("KUBECONFIG");
        if (path == null || path.length() == 0) {
            return Optional.empty();
        }
        return Optional.of(new File(path));
    }
}