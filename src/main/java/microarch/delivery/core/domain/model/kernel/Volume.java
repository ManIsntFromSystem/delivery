package microarch.delivery.core.domain.model.kernel;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import libs.ddd.ValueObject;
import libs.errs.Error;
import libs.errs.GeneralErrors;
import libs.errs.Result;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Embeddable
@Getter
@NoArgsConstructor(force = true, access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class Volume extends ValueObject<Volume> {

    private static final int MIN_EXCLUSIVE_VALUE = 0;

    @Column(name = "volume")
    private final int value;

    public static Result<Volume, Error> create(int value) {
        if (value <= MIN_EXCLUSIVE_VALUE) {
            return Result.failure(GeneralErrors.valueMustBeGreaterThan("volume", value, MIN_EXCLUSIVE_VALUE));
        }

        return Result.success(new Volume(value));
    }

    public Volume add(Volume other) {
        return new Volume(Math.addExact(this.value, other.value));
    }

    public boolean isLessOrEqual(Volume other) {
        return this.value <= other.value;
    }

    @Override
    protected Iterable<Object> equalityComponents() {
        return List.of(value);
    }
}
