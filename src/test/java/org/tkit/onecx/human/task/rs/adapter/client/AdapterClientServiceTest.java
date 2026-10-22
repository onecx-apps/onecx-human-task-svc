package org.tkit.onecx.human.task.rs.adapter.client;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashMap;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
class AdapterClientServiceTest {

    @Test
    void filterValidUrls_shouldSkipNullAndBlankUrls() {
        var optionalUrls = new HashMap<String, Optional<String>>();
        optionalUrls.put("VALID", Optional.of("http://localhost:1"));
        optionalUrls.put("NULL_URL", Optional.empty());
        optionalUrls.put("BLANK_URL", Optional.of("  "));
        optionalUrls.put("EMPTY_URL", Optional.of(""));

        var result = AdapterClientService.filterValidUrls(optionalUrls);

        assertThat(result)
                .containsEntry("VALID", "http://localhost:1")
                .doesNotContainKey("NULL_URL")
                .doesNotContainKey("BLANK_URL")
                .doesNotContainKey("EMPTY_URL");
    }

    @Test
    void filterValidUrls_shouldReturnEmptyMap_whenAllInvalid() {
        var urls = new HashMap<String, Optional<String>>();
        urls.put("NULL_URL", Optional.empty());
        urls.put("BLANK_URL", Optional.of("  "));

        var result = AdapterClientService.filterValidUrls(urls);

        assertThat(result).isEmpty();
    }
}
