package microarch.delivery.core.domain.model.courier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import libs.errs.GeneralErrors;
import microarch.delivery.core.domain.model.kernel.Location;
import microarch.delivery.core.domain.model.kernel.Volume;
import microarch.delivery.core.domain.model.order.Order;

class CourierTest {

    @Test
    void shouldBeCreatedWithMaxVolume20AndNoAssignments() {
        // Arrange
        var location = Location.create(5, 5).getValue();

        // Act
        var result = Courier.create("Маша", location);

        // Assert
        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getValue().getId()).isNotNull();
        assertThat(result.getValue().getName()).isEqualTo("Маша");
        assertThat(result.getValue().getLocation()).isEqualTo(location);
        assertThat(result.getValue().getMaxVolume()).isEqualTo(Volume.create(20).getValue());
        assertThat(result.getValue().getAssignments()).isEmpty();
    }

    @Test
    void shouldReturnValueIsRequiredErrorWhenNameIsEmpty() {
        // Arrange
        var location = Location.create(5, 5).getValue();

        // Act
        var result = Courier.create(" ", location);

        // Assert
        assertThat(result.isFailure()).isTrue();
        assertThat(result.getError()).isEqualTo(GeneralErrors.valueIsRequired("name"));
    }

    @Test
    void shouldReturnValueIsRequiredErrorWhenLocationIsNull() {
        // Act
        var result = Courier.create("Маша", null);

        // Assert
        assertThat(result.isFailure()).isTrue();
        assertThat(result.getError()).isEqualTo(GeneralErrors.valueIsRequired("location"));
    }

    @ParameterizedTest
    @CsvSource({ "20, true", "21, false" })
    void canTakeOrderWhenCourierHasNoAssignmentsOnlyIfOrderFitsMax(int orderVolume, boolean expected) {
        // Arrange
        var location = Location.create(5, 5).getValue();
        var courier = Courier.create("Маша", location).getValue();
        var order = Order.create(UUID.randomUUID(), location, Volume.create(orderVolume).getValue()).getValue();

        // Act
        var result = courier.canTakeOrder(order);

        // Assert
        assertThat(result.getValue()).isEqualTo(expected);
    }

    @ParameterizedTest
    @CsvSource({ "15, 5, true", "15, 6, false", "10, 10, true", "19, 2, false" })
    void canTakeOrderOnlyWhenTotalVolumeDoesNotExceedMax(int occupiedVolume, int orderVolume, boolean expected) {
        // Arrange
        var location = Location.create(5, 5).getValue();
        var courier = Courier.create("Маша", location).getValue();
        courier.takeOrder(
                Order.create(UUID.randomUUID(), location, Volume.create(occupiedVolume).getValue()).getValue());
        var order = Order.create(UUID.randomUUID(), location, Volume.create(orderVolume).getValue()).getValue();

        // Act
        var result = courier.canTakeOrder(order);

        // Assert
        assertThat(result.getValue()).isEqualTo(expected);
    }

    @Test
    void canTakeOrderWhenPreviousOrderIsCompleted() {
        // Arrange
        var location = Location.create(5, 5).getValue();
        var courier = Courier.create("Маша", location).getValue();
        var completedOrder = Order.create(UUID.randomUUID(), location, Volume.create(20).getValue()).getValue();
        courier.takeOrder(completedOrder);
        courier.completeOrder(completedOrder);
        var order = Order.create(UUID.randomUUID(), location, Volume.create(20).getValue()).getValue();

        // Act
        var result = courier.canTakeOrder(order);

        // Assert
        assertThat(result.getValue()).isTrue();
    }

    @Test
    void shouldCreateAssignmentWhenTakeOrder() {
        // Arrange
        var courier = Courier.create("Маша", Location.create(5, 5).getValue()).getValue();
        var order = Order.create(UUID.randomUUID(), Location.create(1, 2).getValue(), Volume.create(7).getValue())
                .getValue();

        // Act
        var result = courier.takeOrder(order);

        // Assert
        assertThat(result.isSuccess()).isTrue();
        assertThat(courier.getAssignments()).hasSize(1);
        var assignment = courier.getAssignments().getFirst();
        assertThat(assignment.getOrderId()).isEqualTo(order.getId());
        assertThat(assignment.getVolume()).isEqualTo(order.getVolume());
        assertThat(assignment.getLocation()).isEqualTo(order.getLocation());
        assertThat(assignment.getStatus()).isEqualTo(AssignmentStatus.ASSIGNED);
    }

    @Test
    void shouldReturnErrorWhenTakeOrderWithExceededVolume() {
        // Arrange
        var location = Location.create(5, 5).getValue();
        var courier = Courier.create("Маша", location).getValue();
        courier.takeOrder(Order.create(UUID.randomUUID(), location, Volume.create(15).getValue()).getValue());
        var order = Order.create(UUID.randomUUID(), location, Volume.create(6).getValue()).getValue();

        // Act
        var result = courier.takeOrder(order);

        // Assert
        assertThat(result.isFailure()).isTrue();
        assertThat(result.getError()).isEqualTo(Courier.Errors.courierHasNotEnoughVolume());
        assertThat(courier.getAssignments()).hasSize(1);
    }

    @Test
    void shouldReturnErrorWhenTakeSameOrderTwice() {
        // Arrange
        var location = Location.create(5, 5).getValue();
        var courier = Courier.create("Маша", location).getValue();
        var order = Order.create(UUID.randomUUID(), location, Volume.create(1).getValue()).getValue();
        courier.takeOrder(order);

        // Act
        var result = courier.takeOrder(order);

        // Assert
        assertThat(result.isFailure()).isTrue();
        assertThat(result.getError()).isEqualTo(Courier.Errors.courierHasAlreadyTakenOrder());
        assertThat(courier.getAssignments()).hasSize(1);
    }

    @Test
    void shouldReturnValueIsRequiredErrorWhenTakeNullOrder() {
        // Arrange
        var courier = Courier.create("Маша", Location.create(5, 5).getValue()).getValue();

        // Act
        var result = courier.takeOrder(null);

        // Assert
        assertThat(result.isFailure()).isTrue();
        assertThat(result.getError()).isEqualTo(GeneralErrors.valueIsRequired("order"));
        assertThat(courier.getAssignments()).isEmpty();
    }

    @Test
    void shouldNotAllowToChangeAssignmentsOutsideOfCourier() {
        // Arrange
        var location = Location.create(5, 5).getValue();
        var courier = Courier.create("Маша", location).getValue();
        courier.takeOrder(Order.create(UUID.randomUUID(), location, Volume.create(1).getValue()).getValue());

        // Act & Assert
        assertThatThrownBy(() -> courier.getAssignments().clear()).isInstanceOf(UnsupportedOperationException.class);
        assertThat(courier.getAssignments()).hasSize(1);
    }

    @ParameterizedTest
    @CsvSource({ "5, 5", "5, 4", "5, 6", "4, 5", "6, 5" })
    void shouldCompleteOrderWhenCourierIsOneStepFromOrderOrCloser(int orderX, int orderY) {
        // Arrange
        var courier = Courier.create("Маша", Location.create(5, 5).getValue()).getValue();
        var order = Order
                .create(UUID.randomUUID(), Location.create(orderX, orderY).getValue(), Volume.create(1).getValue())
                .getValue();
        courier.takeOrder(order);

        // Act
        var result = courier.completeOrder(order);

        // Assert
        assertThat(result.isSuccess()).isTrue();
        assertThat(courier.getAssignments().getFirst().getStatus()).isEqualTo(AssignmentStatus.COMPLETED);
    }

    @ParameterizedTest
    @CsvSource({ "6, 6", "4, 4", "5, 7", "1, 1" })
    void shouldReturnErrorWhenCompleteOrderFarFromCourier(int orderX, int orderY) {
        // Arrange
        var courier = Courier.create("Маша", Location.create(5, 5).getValue()).getValue();
        var order = Order
                .create(UUID.randomUUID(), Location.create(orderX, orderY).getValue(), Volume.create(1).getValue())
                .getValue();
        courier.takeOrder(order);

        // Act
        var result = courier.completeOrder(order);

        // Assert
        assertThat(result.isFailure()).isTrue();
        assertThat(result.getError()).isEqualTo(Assignment.Errors.courierIsTooFar());
        assertThat(courier.getAssignments().getFirst().getStatus()).isEqualTo(AssignmentStatus.ASSIGNED);
    }

    @Test
    void shouldReturnNotFoundErrorWhenCompleteNotTakenOrder() {
        // Arrange
        var location = Location.create(5, 5).getValue();
        var courier = Courier.create("Маша", location).getValue();
        var order = Order.create(UUID.randomUUID(), location, Volume.create(1).getValue()).getValue();

        // Act
        var result = courier.completeOrder(order);

        // Assert
        assertThat(result.isFailure()).isTrue();
        assertThat(result.getError()).isEqualTo(GeneralErrors.notFound("assignment", order.getId()));
    }

    @ParameterizedTest
    @CsvSource({ "5, 4", "5, 6", "4, 5", "6, 5" })
    void shouldMoveOneStep(int targetX, int targetY) {
        // Arrange
        var courier = Courier.create("Маша", Location.create(5, 5).getValue()).getValue();
        var target = Location.create(targetX, targetY).getValue();

        // Act
        var result = courier.move(target);

        // Assert
        assertThat(result.isSuccess()).isTrue();
        assertThat(courier.getLocation()).isEqualTo(target);
    }

    @ParameterizedTest
    @CsvSource({ "6, 6", "4, 4", "5, 7", "1, 1" })
    void shouldReturnErrorWhenMoveFurtherThanOneStep(int targetX, int targetY) {
        // Arrange
        var location = Location.create(5, 5).getValue();
        var courier = Courier.create("Маша", location).getValue();

        // Act
        var result = courier.move(Location.create(targetX, targetY).getValue());

        // Assert
        assertThat(result.isFailure()).isTrue();
        assertThat(result.getError()).isEqualTo(Courier.Errors.courierCannotMoveFurtherThanOneStep());
        assertThat(courier.getLocation()).isEqualTo(location);
    }

    @Test
    void shouldReturnValueIsRequiredErrorWhenMoveToNull() {
        // Arrange
        var location = Location.create(5, 5).getValue();
        var courier = Courier.create("Маша", location).getValue();

        // Act
        var result = courier.move(null);

        // Assert
        assertThat(result.isFailure()).isTrue();
        assertThat(result.getError()).isEqualTo(GeneralErrors.valueIsRequired("target"));
        assertThat(courier.getLocation()).isEqualTo(location);
    }
}
