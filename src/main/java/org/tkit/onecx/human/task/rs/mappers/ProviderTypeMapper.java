package org.tkit.onecx.human.task.rs.external.v1.mappers;

import org.mapstruct.Mapper;

import gen.org.tkit.onecx.human.task.rs.external.v1.model.ProviderTypeDTOV1;

@Mapper
public interface ProviderTypeMapper {

    default String map(ProviderTypeDTOV1 value) {
        return value == null ? null : value.toString();
    }

}
