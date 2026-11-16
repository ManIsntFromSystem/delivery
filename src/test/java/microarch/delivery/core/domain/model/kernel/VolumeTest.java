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
}
