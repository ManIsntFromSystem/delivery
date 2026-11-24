package microarch.delivery.core.application.queries;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Tuple;
import libs.errs.Error;
import libs.errs.Result;
import microarch.delivery.core.application.queries.dto.LocationDto;
import microarch.delivery.core.application.queries.dto.OrderDto;
import microarch.delivery.core.domain.model.order.OrderStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class GetNotCompletedOrdersQueryHandlerImpl implements GetNotCompletedOrdersQueryHandler {
    @PersistenceContext
    private EntityManager em;

    @Override
    @Transactional(readOnly = true)
    public Result<GetNotCompletedOrdersResponse, Error> handle(GetNotCompletedOrdersQuery query) {
        String jpql = """
                SELECT o.id, o.location.x, o.location.y
                FROM Order o
                WHERE o.status IN :statuses
                """;
        List<Tuple> rows = em.createQuery(jpql, Tuple.class)
                .setParameter("statuses", List.of(OrderStatus.CREATED, OrderStatus.ASSIGNED)).getResultList();

        var orders = rows.stream().map(row -> new OrderDto(row.get(0, UUID.class),
                new LocationDto(row.get(1, Integer.class), row.get(2, Integer.class)))).toList();

        return Result.success(new GetNotCompletedOrdersResponse(orders));
    }
}
