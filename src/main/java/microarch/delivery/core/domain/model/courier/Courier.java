package microarch.delivery.core.domain.model.courier;

import libs.ddd.Aggregate;
import libs.errs.Error;
import libs.errs.GeneralErrors;
import libs.errs.Guard;
import libs.errs.Result;
import libs.errs.UnitResult;
import lombok.Getter;
import microarch.delivery.core.domain.model.kernel.Location;
import microarch.delivery.core.domain.model.kernel.Volume;
import microarch.delivery.core.domain.model.order.Order;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
public final class Courier extends Aggregate<UUID> {
    private static final int MAX_VOLUME = 20;
    private static final int MAX_MOVE_DISTANCE = 1;

    private final List<Assignment> assignments = new ArrayList<>();
    private String name;
    private Location location;
    private Volume maxVolume;

    private Courier() {
        // для JPA
    }

    private Courier(UUID id, String name, Location location) {
        super(id);
        this.name = name;
        this.location = location;
        this.maxVolume = Volume.create(MAX_VOLUME).getValueOrThrow();
    }

    public static Result<Courier, Error> create(String name, Location location) {
        Error nameError = Guard.againstNullOrEmpty(name, "name");
        if (nameError != null)
            return Result.failure(nameError);
        if (location == null)
            return Result.failure(GeneralErrors.valueIsRequired("location"));

        var courier = new Courier(UUID.randomUUID(), name, location);
        return Result.success(courier);
    }

    public List<Assignment> getAssignments() {
        return List.copyOf(assignments);
    }

    public Result<Boolean, Error> canTakeOrder(Order order) {
        if (order == null)
            return Result.failure(GeneralErrors.valueIsRequired("order"));

        var totalVolume = calculateTotalVolumeWith(order.getVolume());
        return Result.success(totalVolume.isLessOrEqual(maxVolume));
    }

    public UnitResult<Error> takeOrder(Order order) {
        if (order == null)
            return UnitResult.failure(GeneralErrors.valueIsRequired("order"));
        if (findAssignment(order.getId()) != null)
            return UnitResult.failure(Errors.courierHasAlreadyTakenOrder());
        if (!canTakeOrder(order).getValue())
            return UnitResult.failure(Errors.courierHasNotEnoughVolume());

        var createAssignmentResult = Assignment.create(order.getId(), order.getVolume(), order.getLocation());
        if (createAssignmentResult.isFailure())
            return UnitResult.failure(createAssignmentResult.getError());

        assignments.add(createAssignmentResult.getValue());
        return UnitResult.success();
    }

    public UnitResult<Error> completeOrder(Order order) {
        if (order == null)
            return UnitResult.failure(GeneralErrors.valueIsRequired("order"));

        var assignment = findAssignment(order.getId());
        if (assignment == null)
            return UnitResult.failure(GeneralErrors.notFound("assignment", order.getId()));

        return assignment.complete(location);
    }

    public UnitResult<Error> move(Location target) {
        if (target == null)
            return UnitResult.failure(GeneralErrors.valueIsRequired("target"));
        if (location.distanceTo(target).getValue() > MAX_MOVE_DISTANCE)
            return UnitResult.failure(Errors.courierCannotMoveFurtherThanOneStep());

        this.location = target;
        return UnitResult.success();
    }

    private Assignment findAssignment(UUID orderId) {
        return assignments.stream().filter(a -> a.getOrderId().equals(orderId)).findFirst().orElse(null);
    }

    private Volume calculateTotalVolumeWith(Volume newOrderVolume) {
        // Завершённые назначения уже доставлены и не занимают объём курьера
        return assignments.stream().filter(a -> a.getStatus() == AssignmentStatus.ASSIGNED).map(Assignment::getVolume)
                .reduce(newOrderVolume, Volume::add);
    }

    public static class Errors {
        public static Error courierHasNotEnoughVolume() {
            return Error.of(Courier.class.getSimpleName().toLowerCase() + ".has.not.enough.volume",
                    "Order volume exceeds courier free volume");
        }

        public static Error courierHasAlreadyTakenOrder() {
            return Error.of(Courier.class.getSimpleName().toLowerCase() + ".has.already.taken.order",
                    "Courier has already taken this order");
        }

        public static Error courierCannotMoveFurtherThanOneStep() {
            return Error.of(Courier.class.getSimpleName().toLowerCase() + ".cannot.move.further.than.one.step",
                    "Courier can move no further than one step");
        }
    }
}
