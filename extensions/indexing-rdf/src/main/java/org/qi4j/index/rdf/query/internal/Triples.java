/*
 * Copyright 2008 Michael Hunger.
 *
 * Licensed  under the  Apache License,  Version 2.0  (the "License");
 * you may not use  this file  except in  compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed  under the  License is distributed on an "AS IS" BASIS,
 * WITHOUT  WARRANTIES OR CONDITIONS  OF ANY KIND, either  express  or
 * implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.qi4j.index.rdf.query.internal;

import org.qi4j.api.common.QualifiedName;
import org.qi4j.api.entity.Identity;
import org.qi4j.api.query.grammar.AssociationFunction;
import org.qi4j.api.query.grammar.ManyAssociationFunction;
import org.qi4j.api.query.grammar.PropertyFunction;
import org.qi4j.api.util.Classes;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static java.lang.String.format;

public class Triples
    implements Iterable<Triples.Triple>
{
    private int valueCounter = 0;
    private final List<Triple> triples = new ArrayList<Triple>();
    private final Namespaces namespaces;

    public Triples( Namespaces namespaces )
    {
        this.namespaces = namespaces;
    }

    public void addDefaultTriples( String resultType )
    {
        triples.add(
            new Triple(
                "?entityType",
                "rdfs:subClassOf",
                "<" + Classes.toURI( resultType ) + ">",
                false )
        );
        triples.add(
            new Triple(
                "?entity",
                "rdf:type",
                "?entityType",
                false )
        );
        triples.add(
            new Triple(
                "?entity",
                addNamespace( QualifiedName.fromClass( Identity.class, "identity" ).toNamespace() ) + ":identity",
                "?identity",
                false
            )
        );
    }

    private String addNamespace( String namespace )
    {
        return namespaces.addNamespace( namespace );
    }

    public Triple addTriple( final PropertyFunction propertyFunction, boolean optional )
    {
        String subject = "?entity";
        if( propertyFunction.getTraversedAssociation() != null )
        {
            subject = addTripleAssociation( propertyFunction.getTraversedAssociation(), false ).value;
        }
        else if( propertyFunction.getTraversedProperty() != null )
        {
            subject = addTriple( propertyFunction.getTraversedProperty(), false ).value;
        }
        QualifiedName qualifiedName = QualifiedName.fromAccessor( propertyFunction.getAccessor() );
        String prefix = addNamespace( qualifiedName.toNamespace() );
        return addTriple( subject, prefix + ":" + qualifiedName.name(), optional );
    }

    public Triple addTripleAssociation( AssociationFunction associationReference, boolean optional )
    {
        String subject = "?entity";
        if( associationReference.getTraversedAssociation() != null )
        {
            subject = addTripleAssociation( associationReference.getTraversedAssociation(), false ).value;
        }
        QualifiedName qualifiedName = QualifiedName.fromAccessor( associationReference.getAccessor() );
        String prefix = addNamespace( qualifiedName.toNamespace() );
        return addTriple( subject, prefix + ":" + qualifiedName.name(), optional );
    }

    public Triple addTripleManyAssociation( final ManyAssociationFunction manyAssociationReference,
                                             final boolean optional
    )
    {
        AssociationFunction traversedAssociation = manyAssociationReference.getTraversedAssociation();
        String subject = "?entity";
        if( traversedAssociation != null )
        {
            subject = addTripleAssociation( traversedAssociation, false ).value;
        }
        QualifiedName qualifiedName = QualifiedName.fromAccessor( manyAssociationReference.getAccessor() );
        String predicatePrefix = addNamespace( qualifiedName.toNamespace() );
        String predicate = predicatePrefix + ":" + qualifiedName.name();
        Triple collectionTriple = addTriple( subject, predicate, optional );

        String liSubject = collectionTriple.value;
        return addTriple( liSubject, "rdf:li", false );
    }

    private Triple addTriple( final String subject,
                              final String predicate,
                              final boolean optional
    )
    {
        Triple triple = getTriple( subject, predicate );
        if( triple == null )
        {
            final String value = "?v" + valueCounter++;
            triple = new Triple( subject, predicate, value, optional );
            triples.add( triple );
        }
        if( !optional && triple.optional )
        {
            triple.optional = false;
        }
        return triple;
    }

    private Triple getTriple( final String subject,
                              final String predicate
    )
    {
        for( Triple triple : triples )
        {
            if( triple.subject.equals( subject )
                && triple.predicate.equals( predicate ) )
            {
                return triple;
            }
        }
        return null;
    }

    public boolean hasTriples()
    {
        return !triples.isEmpty();
    }

    public Iterator<Triple> iterator()
    {
        return triples.iterator();
    }

    public String toSparql()
    {
        StringBuilder sparql = new StringBuilder();
        for( Triple triple : triples )
        {
            sparql.append( triple.toSparql() );
        }
        return sparql.toString();
    }

    public static class Triple
    {
        private final String subject;
        private final String predicate;
        private String value;
        private boolean optional;

        private Triple( final String subject,
                        final String predicate,
                        final String value,
                        final boolean optional
        )
        {
            this.subject = subject;
            this.predicate = predicate;
            this.value = value;
            this.optional = optional;
        }

        @Override
        public boolean equals( Object otherObject )
        {
            if( this == otherObject )
            {
                return true;
            }
            if( otherObject == null || getClass() != otherObject.getClass() )
            {
                return false;
            }

            Triple other = (Triple) otherObject;

            if( predicate != null ? !predicate.equals( other.predicate ) : other.predicate != null )
            {
                return false;
            }
            if( subject != null ? !subject.equals( other.subject ) : other.subject != null )
            {
                return false;
            }
            if( value != null )
            {
                return value.equals( other.value );
            }
            else
            {
                return other.value == null;
            }
        }

        @Override
        public int hashCode()
        {
            int result;
            result = ( subject != null ? subject.hashCode() : 0 );
            result = 31 * result + ( predicate != null ? predicate.hashCode() : 0 );
            result = 31 * result + ( value != null ? value.hashCode() : 0 );
            return result;
        }

        @Override
        public String toString()
        {
            return toSparql();
        }

        public String toSparql()
        {

            if( optional )
            {
                return format( "OPTIONAL {%s %s %s}.", subject, predicate, value );
            }
            return format( "%s %s %s.", subject, predicate, value );
        }

        public String getSubject()
        {
            return subject;
        }

        public String getPredicate()
        {
            return predicate;
        }

        public String getValue()
        {
            return value;
        }

        public void setValue( String value )
        {
            this.value = value;
        }

        public boolean isOptional()
        {
            return optional;
        }
    }

    @Override
    public String toString()
    {
        return triples.toString();
    }
}
