package org.tkit.onecx.human.task.rs.internal.mappers;

import org.mapstruct.Mapper;

import gen.org.tkit.onecx.human.task.rs.internal.model.ProviderTypeDTO;

@Mapper
public interface ProviderTypeMapper {

    default String map(ProviderTypeDTO value) {
        return value == null ? null : value.toString();
    }

    default ProviderTypeDTO fromString(String value) {
        return value == null ? null : ProviderTypeDTO.fromValue(value);
    }

}
