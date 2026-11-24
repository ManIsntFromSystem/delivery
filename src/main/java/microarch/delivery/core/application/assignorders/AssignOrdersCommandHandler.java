package microarch.delivery.core.application.assignorders;

import libs.errs.Error;
import libs.errs.UnitResult;

public interface AssignOrdersCommandHandler {
    UnitResult<Error> handle();
}
