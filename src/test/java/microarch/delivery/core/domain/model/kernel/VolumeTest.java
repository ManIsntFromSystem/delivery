package microarch.delivery.core.domain.model.kernel;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import libs.errs.GeneralErrors;

class VolumeTest {

    @ParameterizedTest
    @CsvSource({ "1", "5", "20" })
    void shouldBeCreatedWhenValueIsPositive(int value) {
        // Act
        var result = Volume.create(value);

        // Assert
        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getValue().getValue()).isEqualTo(value);
    }

    @ParameterizedTest
    @CsvSource({ "0", "-1" })
    void shouldReturnErrorWhenValueIsNotPositive(int value) {
        // Act
        var result = Volume.create(value);

        // Assert
        assertThat(result.isFailure()).isTrue();
        assertThat(result.getError()).isEqualTo(GeneralErrors.valueMustBeGreaterThan("volume", value, 0));
    }

    @Test
    void shouldBeEqualWhenValuesAreEqual() {
        // Arrange
        var first = Volume.create(7).getValue();
        var second = Volume.create(7).getValue();

        // Act
        var isEqual = first.equals(second);

        // Assert
        assertThat(isEqual).isTrue();
        assertThat(first).hasSameHashCodeAs(second);
    }

    @Test
    void shouldNotBeEqualWhenValuesDiffer() {
        // Arrange
        var first = Volume.create(7).getValue();
        var second = Volume.create(8).getValue();

        // Act
        var isEqual = first.equals(second);

        // Assert
        assertThat(isEqual).isFalse();
    }

    @Test
    void shouldAddVolumes() {
        // Arrange
        var first = Volume.create(7).getValue();
        var second = Volume.create(8).getValue();

        // Act
        var result = first.add(second);

        // Assert
        assertThat(result).isEqualTo(Volume.create(15).getValue());
        assertThat(first).isEqualTo(Volume.create(7).getValue());
        assertThat(second).isEqualTo(Volume.create(8).getValue());
    }

    @ParameterizedTest
    @CsvSource({ "7, 8, true", "8, 8, true", "9, 8, false" })
    void shouldCompareVolumes(int first, int second, boolean expected) {
        // Arrange
        var firstVolume = Volume.create(first).getValue();
        var secondVolume = Volume.create(second).getValue();

        // Act
        var result = firstVolume.isLessOrEqual(secondVolume);

        // Assert
        assertThat(result).isEqualTo(expected);
    }
}
