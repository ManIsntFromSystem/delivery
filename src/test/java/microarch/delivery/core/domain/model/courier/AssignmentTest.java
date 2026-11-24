package microarch.delivery.core.domain.model.courier;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import libs.errs.GeneralErrors;
import microarch.delivery.core.domain.model.kernel.Location;
import microarch.delivery.core.domain.model.kernel.Volume;

class AssignmentTest {

    @Test
    void shouldBeCreatedWithAssignedStatus() {
        // Arrange
        var orderId = UUID.randomUUID();
        var volume = Volume.create(5).getValue();
        var location = Location.create(5, 5).getValue();

        // Act
        var result = Assignment.create(orderId, volume, location);

        // Assert
        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getValue().getId()).isNotNull();
        assertThat(result.getValue().getOrderId()).isEqualTo(orderId);
        assertThat(result.getValue().getVolume()).isEqualTo(volume);
        assertThat(result.getValue().getLocation()).isEqualTo(location);
        assertThat(result.getValue().getStatus()).isEqualTo(AssignmentStatus.ASSIGNED);
    }

    @Test
    void shouldReturnValueIsRequiredErrorWhenOrderIdIsNull() {
        // Act
        var result = Assignment.create(null, Volume.create(5).getValue(), Location.create(5, 5).getValue());

        // Assert
        assertThat(result.isFailure()).isTrue();
        assertThat(result.getError()).isEqualTo(GeneralErrors.valueIsRequired("orderId"));
    }

    @Test
    void shouldReturnValueIsRequiredErrorWhenOrderIdIsEmpty() {
        // Act
        var result = Assignment.create(new UUID(0L, 0L), Volume.create(5).getValue(), Location.create(5, 5).getValue());

        // Assert
        assertThat(result.isFailure()).isTrue();
        assertThat(result.getError()).isEqualTo(GeneralErrors.valueIsRequired("orderId"));
    }

    @Test
    void shouldReturnValueIsRequiredErrorWhenVolumeIsNull() {
        // Act
        var result = Assignment.create(UUID.randomUUID(), null, Location.create(5, 5).getValue());

        // Assert
        assertThat(result.isFailure()).isTrue();
        assertThat(result.getError()).isEqualTo(GeneralErrors.valueIsRequired("volume"));
    }

    @Test
    void shouldReturnValueIsRequiredErrorWhenLocationIsNull() {
        // Act
        var result = Assignment.create(UUID.randomUUID(), Volume.create(5).getValue(), null);

        // Assert
        assertThat(result.isFailure()).isTrue();
        assertThat(result.getError()).isEqualTo(GeneralErrors.valueIsRequired("location"));
    }

    @Test
    void shouldNotBeEqualWhenIdsDifferEvenIfDataIsSame() {
        // Arrange
        var orderId = UUID.randomUUID();
        var volume = Volume.create(5).getValue();
        var location = Location.create(5, 5).getValue();
        var first = Assignment.create(orderId, volume, location).getValue();
        var second = Assignment.create(orderId, volume, location).getValue();

        // Act
        var isEqual = first.equals(second);

        // Assert
        assertThat(isEqual).isFalse();
        assertThat(first).isEqualTo(first);
    }

    @ParameterizedTest
    @CsvSource({ "5, 5", "5, 4", "5, 6", "4, 5", "6, 5" })
    void shouldBeCompletedWhenCourierIsOneStepFromOrderOrCloser(int courierX, int courierY) {
        // Arrange
        var assignment = Assignment
                .create(UUID.randomUUID(), Volume.create(5).getValue(), Location.create(5, 5).getValue()).getValue();

        // Act
        var result = assignment.complete(Location.create(courierX, courierY).getValue());

        // Assert
        assertThat(result.isSuccess()).isTrue();
        assertThat(assignment.getStatus()).isEqualTo(AssignmentStatus.COMPLETED);
    }

    @ParameterizedTest
    @CsvSource({ "6, 6", "4, 4", "5, 7", "1, 1" })
    void shouldReturnErrorWhenCourierIsFarFromOrder(int courierX, int courierY) {
        // Arrange
        var assignment = Assignment
                .create(UUID.randomUUID(), Volume.create(5).getValue(), Location.create(5, 5).getValue()).getValue();

        // Act
        var result = assignment.complete(Location.create(courierX, courierY).getValue());

        // Assert
        assertThat(result.isFailure()).isTrue();
        assertThat(result.getError()).isEqualTo(Assignment.Errors.courierIsTooFar());
        assertThat(assignment.getStatus()).isEqualTo(AssignmentStatus.ASSIGNED);
    }

    @Test
    void shouldReturnErrorWhenCompleteAlreadyCompletedAssignment() {
        // Arrange
        var location = Location.create(5, 5).getValue();
        var assignment = Assignment.create(UUID.randomUUID(), Volume.create(5).getValue(), location).getValue();
        assignment.complete(location);

        // Act
        var result = assignment.complete(location);

        // Assert
        assertThat(result.isFailure()).isTrue();
        assertThat(result.getError()).isEqualTo(Assignment.Errors.alreadyCompleted());
        assertThat(assignment.getStatus()).isEqualTo(AssignmentStatus.COMPLETED);
    }

    @Test
    void shouldReturnValueIsRequiredErrorWhenCourierLocationIsNull() {
        // Arrange
        var assignment = Assignment
                .create(UUID.randomUUID(), Volume.create(5).getValue(), Location.create(5, 5).getValue()).getValue();

        // Act
        var result = assignment.complete(null);

        // Assert
        assertThat(result.isFailure()).isTrue();
        assertThat(result.getError()).isEqualTo(GeneralErrors.valueIsRequired("courierLocation"));
        assertThat(assignment.getStatus()).isEqualTo(AssignmentStatus.ASSIGNED);
    }
}
