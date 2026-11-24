package microarch.delivery.adapters.out.postgres;

import microarch.delivery.core.domain.model.courier.AssignmentStatus;
import microarch.delivery.core.domain.model.courier.Courier;
import microarch.delivery.core.domain.model.kernel.Location;
import microarch.delivery.core.domain.model.kernel.Volume;
import microarch.delivery.core.domain.model.order.Order;
import microarch.delivery.core.ports.CourierRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class CourierRepositoryIntegrationTest extends PostgresIntegrationTestBase {

    @Autowired
    CourierRepository repository;

    @Test
    void CanAddCourier() {
        // Arrange
        var courier = Courier.create("Маша", Location.create(5, 5).getValue()).getValue();
        var order = Order.create(UUID.randomUUID(), Location.create(5, 6).getValue(), Volume.create(7).getValue())
                .getValue();
        courier.takeOrder(order);

        // Act
        repository.add(courier);
        var loaded = repository.findById(courier.getId());

        // Assert
        assertThat(loaded).isPresent();
        assertThat(loaded.get().getName()).isEqualTo(courier.getName());
        assertThat(loaded.get().getLocation()).isEqualTo(courier.getLocation());
        assertThat(loaded.get().getMaxVolume()).isEqualTo(courier.getMaxVolume());
        assertThat(loaded.get().getAssignments()).hasSize(1);
        assertThat(loaded.get().getAssignments().getFirst().getOrderId()).isEqualTo(order.getId());
        assertThat(loaded.get().getAssignments().getFirst().getVolume()).isEqualTo(order.getVolume());
        assertThat(loaded.get().getAssignments().getFirst().getLocation()).isEqualTo(order.getLocation());
        assertThat(loaded.get().getAssignments().getFirst().getStatus()).isEqualTo(AssignmentStatus.ASSIGNED);
    }

    @Test
    void CanUpdateCourier() {
        // Arrange
        var courier = Courier.create("Маша", Location.create(5, 5).getValue()).getValue();
        var order = Order.create(UUID.randomUUID(), Location.create(5, 6).getValue(), Volume.create(7).getValue())
                .getValue();
        courier.takeOrder(order);
        repository.add(courier);
        courier.move(Location.create(5, 6).getValue());
        courier.completeOrder(order);

        // Act
        repository.update(courier);
        var loaded = repository.findById(courier.getId());

        // Assert
        assertThat(loaded).isPresent();
        assertThat(loaded.get().getLocation()).isEqualTo(Location.create(5, 6).getValue());
        assertThat(loaded.get().getAssignments().getFirst().getStatus()).isEqualTo(AssignmentStatus.COMPLETED);
    }

    @Test
    void CanGetAllCouriers() {
        // Arrange
        var first = Courier.create("Маша", Location.create(1, 1).getValue()).getValue();
        var second = Courier.create("Петя", Location.create(2, 2).getValue()).getValue();

        // Act
        repository.add(first);
        repository.add(second);
        var loaded = repository.findAll();

        // Assert
        assertThat(loaded).extracting(Courier::getId).contains(first.getId(), second.getId());
    }
}
