package microarch.delivery.core.application.queries;

import microarch.delivery.adapters.out.postgres.PostgresIntegrationTestBase;
import microarch.delivery.core.application.queries.dto.LocationDto;
import microarch.delivery.core.application.queries.dto.OrderDto;
import microarch.delivery.core.domain.model.kernel.Location;
import microarch.delivery.core.domain.model.kernel.Volume;
import microarch.delivery.core.domain.model.order.Order;
import microarch.delivery.core.ports.OrderRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class GetNotCompletedOrdersQueryHandlerIntegrationTest extends PostgresIntegrationTestBase {

    @Autowired
    OrderRepository orderRepository;

    @Autowired
    GetNotCompletedOrdersQueryHandler handler;

    @Test
    void testGetNotCompletedOrders() {
        // Arrange
        var createdOrder = Order
                .create(UUID.randomUUID(), Location.create(1, 2).getValue(), Volume.create(1).getValue()).getValue();
        var assignedOrder = Order
                .create(UUID.randomUUID(), Location.create(3, 4).getValue(), Volume.create(1).getValue()).getValue();
        assignedOrder.assign();
        var completedOrder = Order
                .create(UUID.randomUUID(), Location.create(5, 6).getValue(), Volume.create(1).getValue()).getValue();
        completedOrder.assign();
        completedOrder.complete();
        orderRepository.add(createdOrder);
        orderRepository.add(assignedOrder);
        orderRepository.add(completedOrder);

        // Act
        var result = handler.handle(new GetNotCompletedOrdersQuery());

        // Assert
        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getValue().orders()).contains(new OrderDto(createdOrder.getId(), new LocationDto(1, 2)),
                new OrderDto(assignedOrder.getId(), new LocationDto(3, 4)));
        assertThat(result.getValue().orders()).extracting(OrderDto::id).doesNotContain(completedOrder.getId());
    }
}
