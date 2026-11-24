package microarch.delivery.core.application.commands;

import libs.errs.Error;
import libs.errs.Guard;
import libs.errs.Result;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class CreateCourierCommand {
    private final String name;

    public static Result<CreateCourierCommand, Error> create(String name) {
        var err = Guard.againstNullOrEmpty(name, "name");
        if (err != null)
            return Result.failure(err);

        return Result.success(new CreateCourierCommand(name));
    }
}
