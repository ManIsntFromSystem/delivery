package microarch.delivery.core.domain.model.kernel;

import libs.ddd.ValueObject;
import libs.errs.Error;
import libs.errs.GeneralErrors;
import libs.errs.Result;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Volume extends ValueObject<Volume> {

    private static final int MIN_EXCLUSIVE_VALUE = 0;

    private int value;

    private Volume() {
        // для JPA
    }

    public static Result<Volume, Error> create(int value) {
        if (value <= MIN_EXCLUSIVE_VALUE) {
            return Result.failure(GeneralErrors.valueMustBeGreaterThan("volume", value, MIN_EXCLUSIVE_VALUE));
        }

        return Result.success(new Volume(value));
    }

    @Override
    protected Iterable<Object> equalityComponents() {
        return List.of(value);
    }
}
