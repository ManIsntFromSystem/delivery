package microarch.delivery.core.domain.model.kernel;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import libs.errs.GeneralErrors;

class AddressTest {

    @Test
    void shouldBeCreatedWhenAllFieldsAreFilled() {
        // Act
        var result = Address.create("Россия", "Москва", "Тверская", "1", "2");

        // Assert
        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getValue().getStreet()).isEqualTo("Тверская");
    }

    @Test
    void shouldReturnValueIsRequiredErrorWhenFieldIsEmpty() {
        // Act
        var result = Address.create("Россия", "Москва", " ", "1", "2");

        // Assert
        assertThat(result.isFailure()).isTrue();
        assertThat(result.getError()).isEqualTo(GeneralErrors.valueIsRequired("street"));
    }
}
