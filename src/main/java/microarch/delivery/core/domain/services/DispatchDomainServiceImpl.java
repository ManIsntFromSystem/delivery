package microarch.delivery.core.domain.services;

import libs.errs.Error;
import libs.errs.Result;
import microarch.delivery.core.domain.model.courier.Courier;
import microarch.delivery.core.domain.model.order.Order;
import microarch.delivery.core.domain.model.order.OrderStatus;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;

@Service
public final class DispatchDomainServiceImpl implements DispatchDomainService {

    @Override
    public Result<Courier, Error> dispatch(Order order, List<Courier> couriers) {
        Objects.requireNonNull(order, "order");
        Objects.requireNonNull(couriers, "couriers");

        // Статус проверяем до изменения курьера, иначе курьер возьмёт заказ, который нельзя назначить
        if (order.getStatus() != OrderStatus.CREATED)
            return Result.failure(Order.Errors.orderCannotBeAssigned());

        var nearestCourier = couriers.stream().filter(courier -> courier.canTakeOrder(order).getValue()).min(
                Comparator.comparingInt(courier -> courier.getLocation().distanceTo(order.getLocation()).getValue()));
        if (nearestCourier.isEmpty())
            return Result.failure(Errors.noAvailableCouriers());

        var courier = nearestCourier.get();
        var takeOrderResult = courier.takeOrder(order);
        if (takeOrderResult.isFailure())
            return Result.failure(takeOrderResult.getError());

        var assignResult = order.assign();
        if (assignResult.isFailure())
            return Result.failure(assignResult.getError());

        return Result.success(courier);
    }

    public static class Errors {
        public static Error noAvailableCouriers() {
            return Error.of("dispatch.no.available.couriers", "There are no couriers available to take the order");
        }
    }
}
