package org.tkit.onecx.human.task.domain.mappers;

import java.io.IOException;

import jakarta.inject.Inject;

import org.hibernate.HibernateException;
import org.hibernate.type.descriptor.WrapperOptions;
import org.hibernate.type.descriptor.java.JavaType;
import org.hibernate.type.format.FormatMapper;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.quarkus.hibernate.orm.JsonFormat;
import io.quarkus.hibernate.orm.PersistenceUnitExtension;

@JsonFormat
@PersistenceUnitExtension
public class JacksonJsonFormatMapper implements FormatMapper {

    @Inject
    ObjectMapper objectMapper;

    public JacksonJsonFormatMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public <T> T fromString(CharSequence charSequence, JavaType<T> javaType, WrapperOptions wrapperOptions) {
        try {
            return objectMapper.readValue(charSequence.toString(), objectMapper.constructType(javaType.getJavaType()));
        } catch (IOException e) {
            throw new HibernateException("Could not deserialize JSON value to type " + javaType.getJavaType(), e);
        }
    }

    @Override
    public <T> String toString(T value, JavaType<T> javaType, WrapperOptions wrapperOptions) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (IOException e) {
            throw new HibernateException("Could not serialize value of type " + javaType.getJavaType(), e);
        }
    }
}
