package microarch.delivery.core.domain.model.order;

import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import libs.ddd.Aggregate;
import libs.errs.Error;
import libs.errs.GeneralErrors;
import libs.errs.Guard;
import libs.errs.Result;
import libs.errs.UnitResult;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import microarch.delivery.core.domain.model.kernel.Location;
import microarch.delivery.core.domain.model.kernel.Volume;

import java.util.UUID;

@Entity
@Table(name = "orders")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public final class Order extends Aggregate<UUID> {
    @Embedded
    private Location location;

    @Embedded
    private Volume volume;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private OrderStatus status;

    private Order(UUID id, Location location, Volume volume) {
        super(id);
        this.location = location;
        this.volume = volume;
        this.status = OrderStatus.CREATED;
    }

    public static Result<Order, Error> create(UUID id, Location location, Volume volume) {
        Error idError = Guard.againstNullOrEmpty(id, "id");
        if (idError != null)
            return Result.failure(idError);
        if (location == null)
            return Result.failure(GeneralErrors.valueIsRequired("location"));
        if (volume == null)
            return Result.failure(GeneralErrors.valueIsRequired("volume"));

        var order = new Order(id, location, volume);
        return Result.success(order);
    }

    public UnitResult<Error> assign() {
        if (this.status != OrderStatus.CREATED)
            return UnitResult.failure(Errors.orderCannotBeAssigned());

        this.status = OrderStatus.ASSIGNED;
        return UnitResult.success();
    }

    public UnitResult<Error> complete() {
        if (this.status != OrderStatus.ASSIGNED)
            return UnitResult.failure(Errors.orderCannotBeCompleted());

        this.status = OrderStatus.COMPLETED;
        return UnitResult.success();
    }

    public static class Errors {
        public static Error orderCannotBeAssigned() {
            return Error.of(Order.class.getSimpleName().toLowerCase() + ".cannot.be.assigned",
                    "Order can be assigned only in CREATED status");
        }

        public static Error orderCannotBeCompleted() {
            return Error.of(Order.class.getSimpleName().toLowerCase() + ".cannot.be.completed",
                    "Order can be completed only in ASSIGNED status");
        }
    }
}
