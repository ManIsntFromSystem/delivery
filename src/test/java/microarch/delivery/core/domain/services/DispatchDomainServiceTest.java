package microarch.delivery.core.domain.services;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import microarch.delivery.core.domain.model.courier.Courier;
import microarch.delivery.core.domain.model.kernel.Location;
import microarch.delivery.core.domain.model.kernel.Volume;
import microarch.delivery.core.domain.model.order.Order;
import microarch.delivery.core.domain.model.order.OrderStatus;

class DispatchDomainServiceTest {

    @Test
    void shouldDispatchOrderToNearestCourier() {
        // Arrange
        var order = Order.create(UUID.randomUUID(), Location.create(5, 5).getValue(), Volume.create(5).getValue())
                .getValue();
        var farCourier = Courier.create("Петя", Location.create(1, 1).getValue()).getValue();
        var nearestCourier = Courier.create("Маша", Location.create(5, 6).getValue()).getValue();
        var otherCourier = Courier.create("Вера", Location.create(10, 10).getValue()).getValue();
        var dispatchService = new DispatchDomainServiceImpl();

        // Act
        var result = dispatchService.dispatch(order, List.of(farCourier, nearestCourier, otherCourier));

        // Assert
        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getValue()).isEqualTo(nearestCourier);
        assertThat(order.getStatus()).isEqualTo(OrderStatus.ASSIGNED);
        assertThat(nearestCourier.getAssignments()).hasSize(1);
        assertThat(nearestCourier.getAssignments().getFirst().getOrderId()).isEqualTo(order.getId());
        assertThat(farCourier.getAssignments()).isEmpty();
        assertThat(otherCourier.getAssignments()).isEmpty();
    }

    @Test
    void shouldSkipCourierWithoutEnoughVolume() {
        // Arrange
        var order = Order.create(UUID.randomUUID(), Location.create(5, 5).getValue(), Volume.create(5).getValue())
                .getValue();
        var fullCourier = Courier.create("Маша", Location.create(5, 5).getValue()).getValue();
        fullCourier.takeOrder(Order
                .create(UUID.randomUUID(), Location.create(1, 1).getValue(), Volume.create(20).getValue()).getValue());
        var freeCourier = Courier.create("Петя", Location.create(9, 9).getValue()).getValue();
        var dispatchService = new DispatchDomainServiceImpl();

        // Act
        var result = dispatchService.dispatch(order, List.of(fullCourier, freeCourier));

        // Assert
        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getValue()).isEqualTo(freeCourier);
        assertThat(fullCourier.getAssignments()).hasSize(1);
    }

    @Test
    void shouldReturnErrorWhenAllCouriersAreFull() {
        // Arrange
        var order = Order.create(UUID.randomUUID(), Location.create(5, 5).getValue(), Volume.create(5).getValue())
                .getValue();
        var courier = Courier.create("Маша", Location.create(5, 5).getValue()).getValue();
        courier.takeOrder(Order
                .create(UUID.randomUUID(), Location.create(1, 1).getValue(), Volume.create(16).getValue()).getValue());
        var dispatchService = new DispatchDomainServiceImpl();

        // Act
        var result = dispatchService.dispatch(order, List.of(courier));

        // Assert
        assertThat(result.isFailure()).isTrue();
        assertThat(result.getError()).isEqualTo(DispatchDomainServiceImpl.Errors.noAvailableCouriers());
        assertThat(order.getStatus()).isEqualTo(OrderStatus.CREATED);
        assertThat(courier.getAssignments()).hasSize(1);
    }

    @Test
    void shouldReturnErrorWhenOrderIsNotCreated() {
        // Arrange
        var order = Order.create(UUID.randomUUID(), Location.create(5, 5).getValue(), Volume.create(5).getValue())
                .getValue();
        order.assign();
        var courier = Courier.create("Маша", Location.create(5, 5).getValue()).getValue();
        var dispatchService = new DispatchDomainServiceImpl();

        // Act
        var result = dispatchService.dispatch(order, List.of(courier));

        // Assert
        assertThat(result.isFailure()).isTrue();
        assertThat(result.getError()).isEqualTo(Order.Errors.orderCannotBeAssigned());
        assertThat(courier.getAssignments()).isEmpty();
    }
}
