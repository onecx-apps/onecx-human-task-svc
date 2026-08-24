package org.tkit.onecx.human.task.rs.adapter.client;

import java.util.HashMap;
import java.util.Map;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.UriBuilder;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.rest.client.RestClientBuilder;
import org.tkit.quarkus.log.cdi.LogService;

import io.quarkus.runtime.StartupEvent;

@LogService
@ApplicationScoped
public class AdapterClientService {

    @Inject
    @ConfigProperty(name = "adapter.urls")
    Map<String, String> adapterUrls;

    private final Map<String, TasksAdapterClient> clients = new HashMap<>();

    void onStart(@Observes StartupEvent ev) {
        filterValidUrls(adapterUrls).forEach((providerType, url) -> clients.put(providerType, RestClientBuilder.newBuilder()
                .baseUri(UriBuilder.fromUri(url).build())
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
