package com.marcgrue.dcisample_b.communication.query;

import com.marcgrue.dcisample_b.data.entity.CargoEntity;
import com.marcgrue.dcisample_b.data.structure.handling.HandlingEventType;
import com.marcgrue.dcisample_b.data.structure.voyage.Voyage;
import com.marcgrue.dcisample_b.infrastructure.model.Queries;
import org.qi4j.api.query.Query;
import org.qi4j.api.query.QueryBuilder;

import java.util.ArrayList;
import java.util.List;

import static org.qi4j.api.query.QueryExpressions.orderBy;
import static org.qi4j.api.query.QueryExpressions.templateFor;

/**
 * Handling queries
 *
 * Used by the communication layer only. Can change freely according to presentation needs.
 */
public class HandlingQueries extends Queries
{
    public List<String> voyages()
    {
        QueryBuilder<Voyage> qb = qbf.newQueryBuilder(Voyage.class);
        Query<Voyage> voyages = uowf.currentUnitOfWork().newQuery(qb)
                .orderBy(orderBy(templateFor(Voyage.class).voyageNumber()));

        List<String> voyageList = new ArrayList<String>();
        for (Voyage voyage : voyages)
            voyageList.add( voyage.voyageNumber().get().number().get() );
        return voyageList;
    }

    public List<String> cargoIds()
    {
        QueryBuilder<CargoEntity> qb = qbf.newQueryBuilder(CargoEntity.class);
        Query<CargoEntity> cargos = uowf.currentUnitOfWork().newQuery(qb)
                .orderBy( orderBy( templateFor( CargoEntity.class ).trackingId().get().id() ) );
        List<String> cargoList = new ArrayList<String>();
        for (CargoEntity cargo : cargos)
            cargoList.add( cargo.trackingId().get().id().get() );
        return cargoList;
    }

    public List<String> eventTypes()
    {
        List<String> eventTypes = new ArrayList<String>();
        for (HandlingEventType eventType : HandlingEventType.values())
            eventTypes.add( eventType.name() );
        return eventTypes;
    }
}