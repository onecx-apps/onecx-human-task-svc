package org.tkit.onecx.human.task.rs.adapter.client;

import jakarta.ws.rs.client.ClientRequestContext;
import jakarta.ws.rs.client.ClientRequestFilter;
import jakarta.ws.rs.ext.Provider;

import io.quarkus.oidc.client.OidcClient;

@Provider
public class AdapterAuthFilter implements ClientRequestFilter {

    private final OidcClient oidcClient;

    public AdapterAuthFilter(OidcClient oidcClient) {
        this.oidcClient = oidcClient;
    }

    @Override
    public void filter(ClientRequestContext requestContext) {
        String token = oidcClient.getTokens().await().indefinitely().getAccessToken();
        requestContext.getHeaders().add("Authorization", "Bearer " + token);
    }
}
