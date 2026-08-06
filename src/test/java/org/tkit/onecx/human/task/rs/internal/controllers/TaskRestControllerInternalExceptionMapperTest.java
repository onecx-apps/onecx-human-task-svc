package org.tkit.onecx.human.task.rs.internal.controllers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Set;

import jakarta.validation.ConstraintViolationException;

import org.jboss.resteasy.reactive.RestResponse;
import org.junit.jupiter.api.Test;
import org.tkit.onecx.human.task.rs.internal.mappers.ExceptionMapper;

import gen.org.tkit.onecx.human.task.rs.internal.model.ProblemDetailResponseDTO;
import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
class TaskRestControllerInternalExceptionMapperTest {

    @Test
    void constraint_shouldDelegateToExceptionMapper() {
        var controller = new TaskRestControllerInternal();
        var delegate = mock(ExceptionMapper.class);
        controller.exceptionMapper = delegate;

        var exception = new ConstraintViolationException("Validation failed", Set.of());
        @SuppressWarnings("unchecked")
        RestResponse<ProblemDetailResponseDTO> expected = mock(RestResponse.class);

        when(delegate.constraint(exception)).thenReturn(expected);

        var actual = controller.constraint(exception);

        assertThat(actual).isSameAs(expected);
        verify(delegate).constraint(exception);
    }
}
