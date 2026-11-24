package microarch.delivery.core.application.queries;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Tuple;
import libs.errs.Error;
import libs.errs.Result;
import microarch.delivery.core.application.queries.dto.CourierDto;
import microarch.delivery.core.application.queries.dto.LocationDto;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class GetAllCouriersQueryHandlerImpl implements GetAllCouriersQueryHandler {
    @PersistenceContext
    private EntityManager em;

    @Override
    @Transactional(readOnly = true)
    public Result<GetAllCouriersResponse, Error> handle(GetAllCouriersQuery query) {
        String jpql = """
                SELECT c.id, c.name, c.location.x, c.location.y
                FROM Courier c
                """;
        List<Tuple> rows = em.createQuery(jpql, Tuple.class).getResultList();

        var couriers = rows.stream().map(row -> new CourierDto(row.get(0, UUID.class), row.get(1, String.class),
                new LocationDto(row.get(2, Integer.class), row.get(3, Integer.class)))).toList();

        return Result.success(new GetAllCouriersResponse(couriers));
    }
}
