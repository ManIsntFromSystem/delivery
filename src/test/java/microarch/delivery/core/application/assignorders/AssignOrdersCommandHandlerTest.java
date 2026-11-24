package microarch.delivery.core.application.assignorders;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import microarch.delivery.core.domain.model.courier.Courier;
import microarch.delivery.core.domain.model.kernel.Location;
import microarch.delivery.core.domain.model.kernel.Volume;
import microarch.delivery.core.domain.model.order.Order;
import microarch.delivery.core.domain.model.order.OrderStatus;
import microarch.delivery.core.domain.services.DispatchDomainServiceImpl;
import microarch.delivery.core.ports.CourierRepository;
import microarch.delivery.core.ports.OrderRepository;

class AssignOrdersCommandHandlerTest {
    private final OrderRepository orderRepository = mock(OrderRepository.class);
    private final CourierRepository courierRepository = mock(CourierRepository.class);

    @Test
    void shouldAssignOrderAndSaveOrderAndCourier() {
        // Arrange
        var order = Order.create(UUID.randomUUID(), Location.create(5, 5).getValue(), Volume.create(5).getValue())
                .getValue();
        var courier = Courier.create("Маша", Location.create(5, 6).getValue()).getValue();
        when(orderRepository.findAnyCreated()).thenReturn(Optional.of(order));
        when(courierRepository.findAll()).thenReturn(List.of(courier));
        var handler = new AssignOrdersCommandHandlerImpl(orderRepository, courierRepository,
                new DispatchDomainServiceImpl());

        // Act
        var result = handler.handle();

        // Assert
        assertThat(result.isSuccess()).isTrue();
        assertThat(order.getStatus()).isEqualTo(OrderStatus.ASSIGNED);
        assertThat(courier.getAssignments()).hasSize(1);
        verify(orderRepository).update(order);
        verify(courierRepository).update(courier);
    }
}
