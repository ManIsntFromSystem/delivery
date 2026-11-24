package microarch.delivery.core.application.commands;

import libs.errs.Error;
import libs.errs.Guard;
import libs.errs.Result;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import microarch.delivery.core.domain.model.kernel.Location;

import java.util.UUID;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class MoveCourierCommand {
    private final UUID courierId;
    private final Location location;

    public static Result<MoveCourierCommand, Error> create(UUID courierId, int x, int y) {
        var err = Guard.againstNullOrEmpty(courierId, "courierId");
        if (err != null)
            return Result.failure(err);

        var locationResult = Location.create(x, y);
        if (locationResult.isFailure())
            return Result.failure(locationResult.getError());

        return Result.success(new MoveCourierCommand(courierId, locationResult.getValue()));
    }
}
