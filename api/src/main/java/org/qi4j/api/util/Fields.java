package org.qi4j.api.util;

import java.lang.reflect.Field;
import java.lang.reflect.Type;

import static org.qi4j.api.util.Iterables.iterable;

/**
 * TODO
 */
public final class Fields
{
    public static final Function2<Class<?>, String, Field> FIELD_NAMED = new Function2<Class<?>, String, Field>()
    {
        @Override
        public Field map( Class<?> aClass, String name )
        {
            return Iterables.first( Iterables.filter( Classes.memberNamed( name ), FIELDS_OF.map( aClass ) ) );
        }
    };

    public static final Function<Type, Iterable<Field>> FIELDS_OF = Classes.forClassHierarchy( new Function<Class<?>, Iterable<Field>>()
                    {
                        @Override
                        public Iterable<Field> map( Class<?> type )
                        {
                            return iterable( type.getDeclaredFields() );
                        }
                    } );

}
