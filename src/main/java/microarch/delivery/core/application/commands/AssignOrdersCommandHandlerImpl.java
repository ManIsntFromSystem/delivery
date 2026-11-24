package microarch.delivery.core.application.commands;

import libs.errs.Error;
import libs.errs.UnitResult;
import microarch.delivery.core.domain.services.DispatchDomainService;
import microarch.delivery.core.ports.CourierRepository;
import microarch.delivery.core.ports.OrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AssignOrdersCommandHandlerImpl implements AssignOrdersCommandHandler {
    private final OrderRepository orderRepository;
    private final CourierRepository courierRepository;
    private final DispatchDomainService dispatchDomainService;

    public AssignOrdersCommandHandlerImpl(OrderRepository orderRepository, CourierRepository courierRepository,
            DispatchDomainService dispatchDomainService) {
        this.orderRepository = orderRepository;
        this.courierRepository = courierRepository;
        this.dispatchDomainService = dispatchDomainService;
    }

    @Override
    @Transactional
    public UnitResult<Error> handle(AssignOrdersCommand command) {
        // Нет нераспределённых заказов — назначать нечего
        var orderOpt = orderRepository.findAnyCreated();
        if (orderOpt.isEmpty())
            return UnitResult.success();
        var order = orderOpt.get();

        var dispatchResult = dispatchDomainService.dispatch(order, courierRepository.findAll());
        if (dispatchResult.isFailure())
            return UnitResult.failure(dispatchResult.getError());
        var courier = dispatchResult.getValue();

        orderRepository.update(order);
        courierRepository.update(courier);
        return UnitResult.success();
    }
}
