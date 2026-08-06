package org.tkit.onecx.human.task.domain.mappers;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Map;

import org.hibernate.type.descriptor.java.JavaType;
import org.hibernate.type.descriptor.java.ObjectJavaType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
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

    @Test
    void toString_shouldThrow_whenSerializationFails() throws JsonProcessingException {
        ObjectMapper failingObjectMapper = mock(ObjectMapper.class);
        when(failingObjectMapper.writeValueAsString(any()))
                .thenThrow(new JsonProcessingException("serialization failure") {
                    private static final long serialVersionUID = 1L;
                });

        var failingMapper = new JacksonJsonFormatMapper(failingObjectMapper);

        Assertions.assertThrows(org.hibernate.HibernateException.class,
                () -> failingMapper.toString(Map.of("key", "value"), javaType, null));
    }
}
