package org.tkit.onecx.human.task.rs.external.v1.mappers;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.tkit.onecx.human.task.rs.mappers.ProviderTypeMapper;
import org.tkit.onecx.human.task.rs.mappers.ProviderTypeMapperImpl;

import gen.org.tkit.onecx.human.task.rs.external.v1.model.ProviderTypeDTOV1;

class ProviderTypeMapperTest {

    ProviderTypeMapper mapper = new ProviderTypeMapperImpl();

    @Test
    void map_shouldReturnSpecValue() {
        Assertions.assertEquals("N8N", mapper.map(ProviderTypeDTOV1.N8_N));
        Assertions.assertEquals("CAMUNDA", mapper.map(ProviderTypeDTOV1.CAMUNDA));
    }

    @Test
    void map_shouldReturnNull_whenValueIsNull() {
        Assertions.assertNull(mapper.map(null));
    }

}
