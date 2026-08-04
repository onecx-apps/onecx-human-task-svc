package org.tkit.onecx.human.task.rs.internal.mappers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.Set;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Path;

import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import org.mockito.Mockito;

class ExceptionMapperTest {

    private final ExceptionMapper mapper = Mappers.getMapper(ExceptionMapper.class);

    @SuppressWarnings("unchecked")
    @Test
    void constraint_shouldMapValidationDetails() {
        Path path = Mockito.mock(Path.class);
        Mockito.when(path.toString()).thenReturn("modificationCount");

        ConstraintViolation<Object> violation = Mockito.mock(ConstraintViolation.class);
        Mockito.when(violation.getPropertyPath()).thenReturn(path);
        Mockito.when(violation.getMessage()).thenReturn("must not be null");

        var ex = new ConstraintViolationException("Validation failed", Set.of((ConstraintViolation<?>) violation));

        var response = mapper.constraint(ex);

        assertNotNull(response);
        assertEquals(400, response.getStatus());
        assertNotNull(response.getEntity());
        assertEquals(ExceptionMapper.ErrorKeys.CONSTRAINT_VIOLATIONS.name(), response.getEntity().getErrorCode());
        assertNotNull(response.getEntity().getInvalidParams());
        assertEquals(1, response.getEntity().getInvalidParams().size());
        assertEquals("modificationCount", response.getEntity().getInvalidParams().getFirst().getName());
        assertEquals("must not be null", response.getEntity().getInvalidParams().getFirst().getMessage());
    }
}
