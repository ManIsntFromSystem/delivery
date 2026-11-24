package microarch.delivery.core.application.commands;

import libs.errs.Error;
import libs.errs.GeneralErrors;
import libs.errs.UnitResult;
import microarch.delivery.core.ports.CourierRepository;
import microarch.delivery.core.ports.OrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CompleteOrderCommandHandlerImpl implements CompleteOrderCommandHandler {
    private final CourierRepository courierRepository;
    private final OrderRepository orderRepository;

    public CompleteOrderCommandHandlerImpl(CourierRepository courierRepository, OrderRepository orderRepository) {
        this.courierRepository = courierRepository;
        this.orderRepository = orderRepository;
    }

    @Override
    @Transactional
    public UnitResult<Error> handle(CompleteOrderCommand command) {
        var courierOpt = courierRepository.findById(command.getCourierId());
        if (courierOpt.isEmpty())
            return UnitResult.failure(GeneralErrors.notFound("courier", command.getCourierId()));
        var courier = courierOpt.get();

        var orderOpt = orderRepository.findById(command.getOrderId());
        if (orderOpt.isEmpty())
            return UnitResult.failure(GeneralErrors.notFound("order", command.getOrderId()));
        var order = orderOpt.get();

        // Сначала курьер: если он далеко от заказа, заказ не должен стать завершённым
        var completeAssignmentResult = courier.completeOrder(order);
        if (completeAssignmentResult.isFailure())
            return completeAssignmentResult;

        var completeOrderResult = order.complete();
        if (completeOrderResult.isFailure())
            return completeOrderResult;

        courierRepository.update(courier);
        orderRepository.update(order);
        return UnitResult.success();
    }
}
