package org.tkit.onecx.human.task.domain.mappers;

import java.util.Map;

import org.hibernate.type.descriptor.java.JavaType;
import org.hibernate.type.descriptor.java.ObjectJavaType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

class JacksonJsonFormatMapperTest {

    private final JacksonJsonFormatMapper mapper = new JacksonJsonFormatMapper(new ObjectMapper());

    private final JavaType<Object> javaType = new ObjectJavaType();

    @Test
    void toString_shouldSerializeMap() {
        var json = mapper.toString(Map.of("key", "value"), javaType, null);

        Assertions.assertEquals("{\"key\":\"value\"}", json);
    }

    @Test
    void fromString_shouldDeserializeMap() {
        var result = mapper.fromString("{\"key\":\"value\"}", javaType, null);

        Assertions.assertEquals(Map.of("key", "value"), result);
    }

    @Test
    void fromString_shouldThrow_whenJsonIsInvalid() {
        Assertions.assertThrows(org.hibernate.HibernateException.class,
                () -> mapper.fromString("invalid-json", javaType, null));
    }
}
