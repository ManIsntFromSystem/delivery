package microarch.delivery.core.domain.model.order;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;

import org.junit.jupiter.api.Test;

import libs.errs.GeneralErrors;
import microarch.delivery.core.domain.model.kernel.Location;
import microarch.delivery.core.domain.model.kernel.Volume;

class OrderTest {

    @Test
    void shouldBeCreatedWithCreatedStatus() {
        // Arrange
        var id = UUID.randomUUID();
        var location = Location.create(3, 7).getValue();
        var volume = Volume.create(5).getValue();

        // Act
        var result = Order.create(id, location, volume);

        // Assert
        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getValue().getId()).isEqualTo(id);
        assertThat(result.getValue().getLocation()).isEqualTo(location);
        assertThat(result.getValue().getVolume()).isEqualTo(volume);
        assertThat(result.getValue().getStatus()).isEqualTo(OrderStatus.CREATED);
    }

    @Test
    void shouldReturnValueIsRequiredErrorWhenIdIsNull() {
        // Arrange
        var location = Location.create(3, 7).getValue();
        var volume = Volume.create(5).getValue();

        // Act
        var result = Order.create(null, location, volume);

        // Assert
        assertThat(result.isFailure()).isTrue();
        assertThat(result.getError()).isEqualTo(GeneralErrors.valueIsRequired("id"));
    }

    @Test
    void shouldReturnValueIsRequiredErrorWhenIdIsEmpty() {
        // Arrange
        var location = Location.create(3, 7).getValue();
        var volume = Volume.create(5).getValue();

        // Act
        var result = Order.create(new UUID(0L, 0L), location, volume);

        // Assert
        assertThat(result.isFailure()).isTrue();
        assertThat(result.getError()).isEqualTo(GeneralErrors.valueIsRequired("id"));
    }

    @Test
    void shouldReturnValueIsRequiredErrorWhenLocationIsNull() {
        // Arrange
        var volume = Volume.create(5).getValue();

        // Act
        var result = Order.create(UUID.randomUUID(), null, volume);

        // Assert
        assertThat(result.isFailure()).isTrue();
        assertThat(result.getError()).isEqualTo(GeneralErrors.valueIsRequired("location"));
    }

    @Test
    void shouldReturnValueIsRequiredErrorWhenVolumeIsNull() {
        // Arrange
        var location = Location.create(3, 7).getValue();

        // Act
        var result = Order.create(UUID.randomUUID(), location, null);

        // Assert
        assertThat(result.isFailure()).isTrue();
        assertThat(result.getError()).isEqualTo(GeneralErrors.valueIsRequired("volume"));
    }

    @Test
    void shouldBeAssignedWhenStatusIsCreated() {
        // Arrange
        var order = Order.create(UUID.randomUUID(), Location.create(3, 7).getValue(), Volume.create(5).getValue())
                .getValue();

        // Act
        var result = order.assign();

        // Assert
        assertThat(result.isSuccess()).isTrue();
        assertThat(order.getStatus()).isEqualTo(OrderStatus.ASSIGNED);
    }

    @Test
    void shouldReturnErrorWhenAssignAlreadyAssignedOrder() {
        // Arrange
        var order = Order.create(UUID.randomUUID(), Location.create(3, 7).getValue(), Volume.create(5).getValue())
                .getValue();
        order.assign();

        // Act
        var result = order.assign();

        // Assert
        assertThat(result.isFailure()).isTrue();
        assertThat(result.getError()).isEqualTo(Order.Errors.orderCannotBeAssigned());
        assertThat(order.getStatus()).isEqualTo(OrderStatus.ASSIGNED);
    }

    @Test
    void shouldBeCompletedWhenStatusIsAssigned() {
        // Arrange
        var order = Order.create(UUID.randomUUID(), Location.create(3, 7).getValue(), Volume.create(5).getValue())
                .getValue();
        order.assign();

        // Act
        var result = order.complete();

        // Assert
        assertThat(result.isSuccess()).isTrue();
        assertThat(order.getStatus()).isEqualTo(OrderStatus.COMPLETED);
    }

    @Test
    void shouldReturnErrorWhenCompleteNotAssignedOrder() {
        // Arrange
        var order = Order.create(UUID.randomUUID(), Location.create(3, 7).getValue(), Volume.create(5).getValue())
                .getValue();

        // Act
        var result = order.complete();

        // Assert
        assertThat(result.isFailure()).isTrue();
        assertThat(result.getError()).isEqualTo(Order.Errors.orderCannotBeCompleted());
        assertThat(order.getStatus()).isEqualTo(OrderStatus.CREATED);
    }
}
