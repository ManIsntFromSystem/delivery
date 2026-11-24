package microarch.delivery.core.domain.model.courier;

import libs.ddd.BaseEntity;
import libs.errs.Error;
import libs.errs.GeneralErrors;
import libs.errs.Guard;
import libs.errs.Result;
import libs.errs.UnitResult;
import lombok.Getter;
import microarch.delivery.core.domain.model.kernel.Location;
import microarch.delivery.core.domain.model.kernel.Volume;

import java.util.UUID;

@Getter
public class Assignment extends BaseEntity<UUID> {

    private static final int MAX_COMPLETION_DISTANCE = 1;

    private UUID orderId;
    private Volume volume;
    private Location location;
    private AssignmentStatus status;

    private Assignment() {
        // для JPA
    }

    private Assignment(UUID orderId, Volume volume, Location location) {
        super(UUID.randomUUID());
        this.orderId = orderId;
        this.volume = volume;
        this.location = location;
        this.status = AssignmentStatus.ASSIGNED;
    }

    public static Result<Assignment, Error> create(UUID orderId, Volume volume, Location location) {
        Error orderIdError = Guard.againstNullOrEmpty(orderId, "orderId");
        if (orderIdError != null) {
            return Result.failure(orderIdError);
        }
        if (volume == null) {
            return Result.failure(GeneralErrors.valueIsRequired("volume"));
        }
        if (location == null) {
            return Result.failure(GeneralErrors.valueIsRequired("location"));
        }

        return Result.success(new Assignment(orderId, volume, location));
    }

    public UnitResult<Error> complete(Location courierLocation) {
        if (courierLocation == null) {
            return UnitResult.failure(GeneralErrors.valueIsRequired("courierLocation"));
        }
        if (status == AssignmentStatus.COMPLETED) {
            return UnitResult.failure(Errors.alreadyCompleted());
        }
        if (location.distanceTo(courierLocation).getValue() > MAX_COMPLETION_DISTANCE) {
            return UnitResult.failure(Errors.courierIsTooFar());
        }

        status = AssignmentStatus.COMPLETED;
        return UnitResult.success();
    }

    public static final class Errors {

        private Errors() {
        }

        public static Error alreadyCompleted() {
            return Error.of("assignment.is.already.completed", "Assignment is already completed");
        }

        public static Error courierIsTooFar() {
            return Error.of("courier.is.too.far",
                    "Courier must be in the order location cell or one step away from it");
        }
    }
}
