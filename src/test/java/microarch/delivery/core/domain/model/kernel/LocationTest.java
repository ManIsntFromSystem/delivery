package microarch.delivery.core.domain.model.kernel;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Modifier;
import java.util.Arrays;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import libs.errs.GeneralErrors;

class LocationTest {

    @ParameterizedTest
    @CsvSource({ "1, 1", "10, 10", "1, 10", "10, 1", "4, 9" })
    void shouldBeCreatedWhenCoordinatesAreInRange(int x, int y) {
        // Act
        var result = Location.create(x, y);

        // Assert
        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getValue().getX()).isEqualTo(x);
        assertThat(result.getValue().getY()).isEqualTo(y);
    }

    @ParameterizedTest
    @CsvSource({ "0", "11", "-1" })
    void shouldReturnOutOfRangeErrorWhenXIsOutOfRange(int x) {
        // Act
        var result = Location.create(x, 5);

        // Assert
        assertThat(result.isFailure()).isTrue();
        assertThat(result.getError())
                .isEqualTo(GeneralErrors.valueIsOutOfRange("x", x, Location.MIN_COORDINATE, Location.MAX_COORDINATE));
    }

    @ParameterizedTest
    @CsvSource({ "0", "11", "-1" })
    void shouldReturnOutOfRangeErrorWhenYIsOutOfRange(int y) {
        // Act
        var result = Location.create(5, y);

        // Assert
        assertThat(result.isFailure()).isTrue();
        assertThat(result.getError())
                .isEqualTo(GeneralErrors.valueIsOutOfRange("y", y, Location.MIN_COORDINATE, Location.MAX_COORDINATE));
    }

    @Test
    void shouldBeEqualWhenCoordinatesAreEqual() {
        // Arrange
        var first = Location.create(3, 7).getValue();
        var second = Location.create(3, 7).getValue();

        // Act
        var isEqual = first.equals(second);

        // Assert
        assertThat(isEqual).isTrue();
        assertThat(first).hasSameHashCodeAs(second);
    }

    @ParameterizedTest
    @CsvSource({ "3, 8", "4, 7", "7, 3" })
    void shouldNotBeEqualWhenCoordinatesDiffer(int x, int y) {
        // Arrange
        var first = Location.create(3, 7).getValue();
        var second = Location.create(x, y).getValue();

        // Act
        var isEqual = first.equals(second);

        // Assert
        assertThat(isEqual).isFalse();
    }

    @Test
    void shouldBeImmutable() {
        // Arrange
        var fields = Arrays.stream(Location.class.getDeclaredFields()).filter(f -> !Modifier.isStatic(f.getModifiers()))
                .toList();

        // Act
        var hasNonPrivateOrNonFinalFields = fields.stream()
                .anyMatch(f -> !Modifier.isPrivate(f.getModifiers()) || !Modifier.isFinal(f.getModifiers()));
        var hasPublicConstructors = Arrays.stream(Location.class.getDeclaredConstructors())
                .anyMatch(c -> Modifier.isPublic(c.getModifiers()));
        var hasSetters = Arrays.stream(Location.class.getDeclaredMethods())
                .anyMatch(m -> m.getName().startsWith("set"));

        // Assert
        assertThat(fields).isNotEmpty();
        assertThat(hasNonPrivateOrNonFinalFields).isFalse();
        assertThat(hasPublicConstructors).isFalse();
        assertThat(hasSetters).isFalse();
    }

    @ParameterizedTest
    @CsvSource({ "4, 9, 2, 6, 5", "1, 1, 10, 10, 18", "5, 5, 5, 5, 0", "5, 5, 6, 5, 1", "2, 3, 9, 3, 7" })
    void shouldCalculateDistance(int fromX, int fromY, int toX, int toY, int expected) {
        // Arrange
        var from = Location.create(fromX, fromY).getValue();
        var to = Location.create(toX, toY).getValue();

        // Act
        var result = from.distanceTo(to);

        // Assert
        assertThat(result.getValue()).isEqualTo(expected);
        assertThat(to.distanceTo(from).getValue()).isEqualTo(expected);
    }

    @Test
    void shouldReturnValueIsRequiredErrorWhenDistanceTargetIsNull() {
        // Arrange
        var location = Location.create(1, 1).getValue();

        // Act
        var result = location.distanceTo(null);

        // Assert
        assertThat(result.isFailure()).isTrue();
        assertThat(result.getError()).isEqualTo(GeneralErrors.valueIsRequired("target"));
    }
}
