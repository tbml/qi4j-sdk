package org.qi4j.index.solr;

import org.qi4j.api.query.grammar2.QuerySpecification;

/**
 * TODO
 */
public class SolrExpressions
{
    public static QuerySpecification search(String queryString)
    {
        return new QuerySpecification( "solr", queryString );
    }
}
