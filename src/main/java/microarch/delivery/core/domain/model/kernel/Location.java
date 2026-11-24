package microarch.delivery.core.domain.model.kernel;

import libs.ddd.ValueObject;
import libs.errs.Error;
import libs.errs.GeneralErrors;
import libs.errs.Guard;
import libs.errs.Result;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class Location extends ValueObject<Location> {

    public static final int MIN_COORDINATE = 1;
    public static final int MAX_COORDINATE = 10;

    private final int x;
    private final int y;

    private Location() {
        // для JPA
        this.x = 0;
        this.y = 0;
    }

    public static Result<Location, Error> create(int x, int y) {
        Error error = Guard.combine(Guard.againstOutOfRange(x, MIN_COORDINATE, MAX_COORDINATE, "x"),
                Guard.againstOutOfRange(y, MIN_COORDINATE, MAX_COORDINATE, "y"));
        if (error != null) {
            return Result.failure(error);
        }

        return Result.success(new Location(x, y));
    }

    /**
     * Расстояние — совокупное количество шагов по X и Y до целевой точки (двигаться можно только по вертикали и
     * горизонтали).
     */
    public Result<Integer, Error> distanceTo(Location target) {
        if (target == null) {
            return Result.failure(GeneralErrors.valueIsRequired("target"));
        }

        return Result.success(Math.abs(x - target.x) + Math.abs(y - target.y));
    }

    @Override
    protected Iterable<Object> equalityComponents() {
        return List.of(x, y);
    }
}
