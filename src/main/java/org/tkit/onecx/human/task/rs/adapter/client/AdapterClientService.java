package org.tkit.onecx.human.task.rs.adapter.client;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import org.tkit.quarkus.log.cdi.LogService;

import io.quarkus.oidc.client.reactive.filter.OidcClientRequestReactiveFilter;
import io.quarkus.rest.client.reactive.QuarkusRestClientBuilder;
import io.quarkus.runtime.Startup;

@Startup
@LogService
@ApplicationScoped
public class AdapterClientService {

    @Inject
    AdapterConfig config;

    private final Map<String, TasksAdapterClient> clients = new HashMap<>();

    @PostConstruct
    void init() {
        filterValidUrls(config.urls()).forEach((providerType, url) -> clients.put(providerType,
                QuarkusRestClientBuilder.newBuilder()
                        .baseUri(URI.create(url))
                        .register(OidcClientRequestReactiveFilter.class)
                        .build(TasksAdapterClient.class)));
    }

    static Map<String, String> filterValidUrls(Map<String, String> urls) {
        var result = new HashMap<String, String>();
        urls.forEach((providerType, url) -> {
            if (url != null && !url.isBlank()) {
                result.put(providerType.toUpperCase(), url);
            }
        });
        return result;
    }

    public TasksAdapterClient getClient(String providerType) {
        return clients.get(providerType);
    }
}