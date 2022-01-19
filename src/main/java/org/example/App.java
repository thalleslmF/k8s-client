package org.example;

import org.example.api.DynamicApi;
import org.example.client.ClientBuilder;
import org.example.config.KubeConfigUtils;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args )
    {
        var kubeConfig = KubeConfigUtils.load();
        var client =new ClientBuilder().withKubeConfig(kubeConfig).build();
        var response = new DynamicApi(client).Group("apps").Version("v1").Kind("deployment").get();
        System.out.println(response);
    }
}
