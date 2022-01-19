package org.example.api;

import org.example.client.K8sClient;
import org.example.http.HttpService;

import java.io.File;

public class DynamicApi {
    private final String basePath = "apis";
    private final K8sClient client;
    private String group;
    private String version;
    private String kind;
    private String namespace;
    private HttpService httpUtils;

    public DynamicApi(K8sClient client) {
        this.client = client;
        this.httpUtils = new HttpService(this.client.getClient());
    }
    public DynamicApi Group(String group) {
        this.group = group;
        return this;
    }

    public DynamicApi Namespace(String namespace) {
        this.namespace = namespace;
        return this;
    }
    public DynamicApi Version(String version) {
        this.version = version;
        return this;
    }
    public DynamicApi Kind(String kind) {
        this.kind = kind;
        return this;
    }
    public String get() {
        var url = this.createUrl();
        System.out.println(url);
        return this.httpUtils.doRequest(url, "GET", null);
    }

    public String createUrl() {
        var url = new StringBuilder(this.client.getServer());
        url.append(File.separator.concat(basePath));
        if (this.group != null && this.group.length() > 0) {
            url.append(File.separator.concat(group));
        }
        if (this.version != null && this.version.length() > 0) {
            url.append(File.separator.concat(version));
        }
        if (this.namespace != null && this.namespace.length() > 0) {
            url.append(File.separator.concat("namespaces"));
            url.append(File.separator.concat(namespace));
        }
        if (this.kind != null && this.kind.length() > 0) {
            var plural = String.format("%s%s",kind,"s");
            url.append(File.separator.concat(plural));
        }
        return url.toString();
    }

}
