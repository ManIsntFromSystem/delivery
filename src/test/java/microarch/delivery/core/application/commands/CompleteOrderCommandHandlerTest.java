package microarch.delivery.core.application.commands;

import microarch.delivery.core.domain.model.courier.AssignmentStatus;
import microarch.delivery.core.domain.model.courier.Courier;
import microarch.delivery.core.domain.model.kernel.Location;
import microarch.delivery.core.domain.model.kernel.Volume;
import microarch.delivery.core.domain.model.order.Order;
import microarch.delivery.core.domain.model.order.OrderStatus;
import microarch.delivery.core.ports.CourierRepository;
import microarch.delivery.core.ports.OrderRepository;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CompleteOrderCommandHandlerTest {

    private final CourierRepository courierRepository = mock(CourierRepository.class);
    private final OrderRepository orderRepository = mock(OrderRepository.class);

    @Test
    void CompleteOrderCommandHandler_ShouldBeSuccess_WhenCourierIsNearOrder() {
        // Arrange
        var courier = Courier.create("Маша", Location.create(5, 5).getValue()).getValue();
        var order = Order.create(UUID.randomUUID(), Location.create(5, 6).getValue(), Volume.create(5).getValue())
                .getValue();
        courier.takeOrder(order);
        order.assign();
        when(courierRepository.findById(courier.getId())).thenReturn(Optional.of(courier));
        when(orderRepository.findById(order.getId())).thenReturn(Optional.of(order));
        var handler = new CompleteOrderCommandHandlerImpl(courierRepository, orderRepository);
        var command = CompleteOrderCommand.create(courier.getId(), order.getId()).getValue();

        // Act
        var result = handler.handle(command);

        // Assert
        assertThat(result.isSuccess()).isTrue();
        assertThat(order.getStatus()).isEqualTo(OrderStatus.COMPLETED);
        assertThat(courier.getAssignments().getFirst().getStatus()).isEqualTo(AssignmentStatus.COMPLETED);
        verify(courierRepository).update(courier);
        verify(orderRepository).update(order);
    }
}
