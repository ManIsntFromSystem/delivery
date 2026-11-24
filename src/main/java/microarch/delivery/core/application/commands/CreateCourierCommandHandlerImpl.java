package microarch.delivery.core.application.commands;

import libs.errs.Error;
import libs.errs.Result;
import microarch.delivery.core.domain.model.courier.Courier;
import microarch.delivery.core.domain.model.kernel.Location;
import microarch.delivery.core.ports.CourierRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class CreateCourierCommandHandlerImpl implements CreateCourierCommandHandler {
    private final CourierRepository courierRepository;

    public CreateCourierCommandHandlerImpl(CourierRepository courierRepository) {
        this.courierRepository = courierRepository;
    }

    @Override
    @Transactional
    public Result<UUID, Error> handle(CreateCourierCommand command) {
        var courierCreateResult = Courier.create(command.getName(), Location.createRandom());
        if (courierCreateResult.isFailure())
            return Result.failure(courierCreateResult.getError());
        var courier = courierCreateResult.getValue();

        courierRepository.add(courier);
        return Result.success(courier.getId());
    }
}
