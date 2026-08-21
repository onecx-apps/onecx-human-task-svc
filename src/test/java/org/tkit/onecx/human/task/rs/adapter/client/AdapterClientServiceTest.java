package org.tkit.onecx.human.task.rs.adapter.client;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashMap;

import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
class AdapterClientServiceTest {

    @Test
    void filterValidUrls_shouldSkipNullAndBlankUrls() {
        var urls = new HashMap<String, String>();
        urls.put("VALID", "http://localhost:1");
        urls.put("NULL_URL", null);
        urls.put("BLANK_URL", "  ");
        urls.put("EMPTY_URL", "");

        var result = AdapterClientService.filterValidUrls(urls);

        assertThat(result)
                .containsEntry("VALID", "http://localhost:1")
                .doesNotContainKey("NULL_URL")
                .doesNotContainKey("BLANK_URL")
                .doesNotContainKey("EMPTY_URL");
    }

    @Test
    void filterValidUrls_shouldReturnEmptyMap_whenAllInvalid() {
        var urls = new HashMap<String, String>();
        urls.put("NULL_URL", null);
        urls.put("BLANK_URL", "  ");

        var result = AdapterClientService.filterValidUrls(urls);

        assertThat(result).isEmpty();
    }
}
