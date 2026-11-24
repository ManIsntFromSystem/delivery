package microarch.delivery.core.domain.model.kernel;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import libs.ddd.ValueObject;
import libs.errs.Error;
import libs.errs.GeneralErrors;
import libs.errs.Guard;
import libs.errs.Result;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Embeddable
@Getter
@NoArgsConstructor(force = true, access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class Location extends ValueObject<Location> {

    public static final int MIN_COORDINATE = 1;
    public static final int MAX_COORDINATE = 10;

    @Column(name = "location_x")
    private final int x;

    @Column(name = "location_y")
    private final int y;

    public static Result<Location, Error> create(int x, int y) {
        Error error = Guard.combine(Guard.againstOutOfRange(x, MIN_COORDINATE, MAX_COORDINATE, "x"),
                Guard.againstOutOfRange(y, MIN_COORDINATE, MAX_COORDINATE, "y"));
        if (error != null) {
            return Result.failure(error);
        }

        return Result.success(new Location(x, y));
    }

    public static Location createRandom() {
        var random = ThreadLocalRandom.current();
        return create(random.nextInt(MIN_COORDINATE, MAX_COORDINATE + 1),
                random.nextInt(MIN_COORDINATE, MAX_COORDINATE + 1)).getValueOrThrow();
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
