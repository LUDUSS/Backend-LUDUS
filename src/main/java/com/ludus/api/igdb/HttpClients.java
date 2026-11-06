package com.ludus.api.igdb;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;
public final class HttpClients {
    private HttpClients() {}
    public static RestClient create(String url) {
        var factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(3000); factory.setReadTimeout(5000);
        return RestClient.builder().baseUrl(url).requestFactory(factory).build();
    }
}
