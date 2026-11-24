package microarch.delivery.adapters.out.postgres;

import microarch.delivery.core.domain.model.kernel.Location;
import microarch.delivery.core.domain.model.kernel.Volume;
import microarch.delivery.core.domain.model.order.Order;
import microarch.delivery.core.domain.model.order.OrderStatus;
import microarch.delivery.core.ports.OrderRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class OrderRepositoryIntegrationTest extends PostgresIntegrationTestBase {

    @Autowired
    OrderRepository repository;

    @Test
    void CanAddOrder() {
        // Arrange
        var order = Order.create(UUID.randomUUID(), Location.create(3, 7).getValue(), Volume.create(5).getValue())
                .getValue();

        // Act
        repository.add(order);
        var loaded = repository.findById(order.getId());

        // Assert
        assertThat(loaded).isPresent();
        assertThat(loaded.get().getLocation()).isEqualTo(order.getLocation());
        assertThat(loaded.get().getVolume()).isEqualTo(order.getVolume());
        assertThat(loaded.get().getStatus()).isEqualTo(order.getStatus());
    }

    @Test
    void CanUpdateOrder() {
        // Arrange
        var order = Order.create(UUID.randomUUID(), Location.create(3, 7).getValue(), Volume.create(5).getValue())
                .getValue();
        repository.add(order);
        order.assign();

        // Act
        repository.update(order);
        var loaded = repository.findById(order.getId());

        // Assert
        assertThat(loaded).isPresent();
        assertThat(loaded.get().getStatus()).isEqualTo(OrderStatus.ASSIGNED);
    }

    @Test
    void CanGetAnyCreatedOrder() {
        // Arrange
        var order = Order.create(UUID.randomUUID(), Location.create(3, 7).getValue(), Volume.create(5).getValue())
                .getValue();

        // Act
        repository.add(order);
        var loaded = repository.findAnyCreated();

        // Assert
        assertThat(loaded).isPresent();
        assertThat(loaded.get().getStatus()).isEqualTo(OrderStatus.CREATED);
    }

    @Test
    void CanGetAllAssignedOrders() {
        // Arrange
        var createdOrder = Order
                .create(UUID.randomUUID(), Location.create(1, 1).getValue(), Volume.create(1).getValue()).getValue();
        var assignedOrder = Order
                .create(UUID.randomUUID(), Location.create(2, 2).getValue(), Volume.create(2).getValue()).getValue();
        assignedOrder.assign();

        // Act
        repository.add(createdOrder);
        repository.add(assignedOrder);
        var loaded = repository.findAllAssigned();

        // Assert
        assertThat(loaded).extracting(Order::getId).contains(assignedOrder.getId())
                .doesNotContain(createdOrder.getId());
    }
}
