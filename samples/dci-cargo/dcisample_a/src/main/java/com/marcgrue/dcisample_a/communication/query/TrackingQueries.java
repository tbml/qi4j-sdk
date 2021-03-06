package com.marcgrue.dcisample_a.communication.query;

import com.marcgrue.dcisample_a.communication.query.dto.HandlingEventDTO;
import com.marcgrue.dcisample_a.data.entity.CargoEntity;
import com.marcgrue.dcisample_a.data.entity.HandlingEventEntity;
import com.marcgrue.dcisample_a.data.shipping.handling.HandlingEvent;
import com.marcgrue.dcisample_a.data.shipping.cargo.Cargo;
import com.marcgrue.dcisample_a.data.shipping.handling.HandlingEvent;
import com.marcgrue.dcisample_a.infrastructure.model.Queries;
import com.marcgrue.dcisample_a.infrastructure.model.QueryModel;
import org.apache.wicket.model.IModel;
import org.qi4j.api.query.Query;
import org.qi4j.api.query.QueryBuilder;
import org.qi4j.api.query.QueryExpressions;

import java.util.ArrayList;
import java.util.List;

import static org.qi4j.api.query.QueryExpressions.*;

/**
 * Tracking queries
 *
 * Used by the communication layer only. Can change according to ui needs.
 */
public class TrackingQueries extends Queries
{
    public List<String> routedCargos()
    {
        Cargo cargoEntity = templateFor( CargoEntity.class );

        QueryBuilder<CargoEntity> qb = qbf.newQueryBuilder(CargoEntity.class).where(isNotNull(cargoEntity.itinerary()));
        Query<CargoEntity> cargos = uowf.currentUnitOfWork().newQuery(qb)
              .orderBy(orderBy(cargoEntity.trackingId().get().id()));

        List<String> cargoList = new ArrayList<String>();
        for (CargoEntity cargo : cargos)
            cargoList.add( cargo.trackingId().get().id().get() );

        return cargoList;
    }

    public IModel<List<HandlingEventDTO>> events( final String trackingIdString )
    {
        return new QueryModel<HandlingEventDTO, HandlingEventEntity>( HandlingEventDTO.class )
        {
            public Query<HandlingEventEntity> getQuery()
            {
                HandlingEvent eventTemplate = templateFor( HandlingEvent.class );

                QueryBuilder<HandlingEventEntity> qb = qbf.newQueryBuilder(HandlingEventEntity.class)
                        .where(QueryExpressions.eq(eventTemplate.trackingId().get().id(), trackingIdString));
                return uowf.currentUnitOfWork().newQuery(qb)
                      .orderBy( orderBy( eventTemplate.completionTime() ) );
            }
        };
    }
}