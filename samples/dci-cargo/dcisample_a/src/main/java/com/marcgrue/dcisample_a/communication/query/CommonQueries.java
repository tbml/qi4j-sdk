package com.marcgrue.dcisample_a.communication.query;

import com.marcgrue.dcisample_a.communication.query.dto.CargoDTO;
import com.marcgrue.dcisample_a.data.entity.CargoEntity;
import com.marcgrue.dcisample_a.data.shipping.location.Location;
import com.marcgrue.dcisample_a.infrastructure.model.EntityModel;
import com.marcgrue.dcisample_a.infrastructure.model.Queries;
import com.marcgrue.dcisample_a.infrastructure.model.QueryModel;
import org.apache.wicket.model.IModel;
import org.qi4j.api.query.Query;
import org.qi4j.api.query.QueryBuilder;

import java.util.ArrayList;
import java.util.List;

import static org.qi4j.api.query.QueryExpressions.orderBy;
import static org.qi4j.api.query.QueryExpressions.templateFor;

/**
 * Common queries
 *
 * Queries shared across packages.
 *
 * Used by the communication layer only. Can change according to ui needs.
 */
public class CommonQueries extends Queries
{
    public IModel<CargoDTO> cargo( String trackingId )
    {
        return EntityModel.of( CargoEntity.class, trackingId, CargoDTO.class );
    }

    public IModel<List<CargoDTO>> cargoList()
    {
        return new QueryModel<CargoDTO, CargoEntity>( CargoDTO.class )
        {
            public Query<CargoEntity> getQuery()
            {
                QueryBuilder<CargoEntity> qb = qbf.newQueryBuilder(CargoEntity.class);
                return uowf.currentUnitOfWork().newQuery( qb)
                      .orderBy(orderBy(templateFor(CargoEntity.class).trackingId().get().id()));
            }
        };
    }

    public List<String> unLocodes()
    {
        QueryBuilder<Location> qb = qbf.newQueryBuilder(Location.class);
        Query<Location> locations = uowf.currentUnitOfWork().newQuery(qb)
              .orderBy( orderBy( templateFor( Location.class ).unLocode().get().code() ) );
        List<String> unLocodeList = new ArrayList<String>();
        for (Location location : locations)
            unLocodeList.add( location.getCode() );

        return unLocodeList;
    }
}