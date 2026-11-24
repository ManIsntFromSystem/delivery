package microarch.delivery.core.application.commands;

import libs.errs.Error;
import libs.errs.UnitResult;
import microarch.delivery.core.domain.model.kernel.Location;
import microarch.delivery.core.domain.model.order.Order;
import microarch.delivery.core.ports.OrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CreateOrderCommandHandlerImpl implements CreateOrderCommandHandler {
    private final OrderRepository orderRepository;

    public CreateOrderCommandHandlerImpl(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    @Override
    @Transactional
    public UnitResult<Error> handle(CreateOrderCommand command) {
        // Пока нет интеграции с сервисом Geo — Location по адресу не получить, берём случайную
        var location = Location.createRandom();

        var orderCreateResult = Order.create(command.getOrderId(), location, command.getVolume());
        if (orderCreateResult.isFailure())
            return UnitResult.failure(orderCreateResult.getError());

        orderRepository.add(orderCreateResult.getValue());
        return UnitResult.success();
    }
}
