package org.qi4j.spi.property;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.qi4j.api.common.QualifiedName;
import org.qi4j.api.common.TypeName;
import org.qi4j.api.entity.EntityReference;
import org.qi4j.api.property.Property;
import org.qi4j.api.property.PropertyInfo;
import org.qi4j.api.property.StateHolder;
import org.qi4j.api.structure.Module;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.api.util.DateFunctions;
import org.qi4j.api.util.Function;
import org.qi4j.api.value.ValueBuilder;
import org.qi4j.spi.structure.ModuleSPI;
import org.qi4j.spi.util.Base64Encoder;
import org.qi4j.spi.value.ValueDescriptor;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;

/**
 * TODO
 */
public class JSONDeserializer
{
    private static Set<Class<?>> nonStringClasses = new HashSet<Class<?>>();

    /**
     * Check if the given value type should be represented as a JSON string. This can be used
     * by clients to figure out whether to add "" around a string to be deserialized.
     *
     * @param valueType
     * @return
     */
    public static boolean isString(ValueType valueType)
    {
        if (!valueType.getClass().equals( ValueType.class ))
            return false;
        else
            return !nonStringClasses.contains( valueType.type()  );
    }

    private static Map<Class<?>, Function<Object, Object>> typeFunctions = new HashMap<Class<?>, Function<Object, Object>>(  );

    public static <T> void registerDeserializer( Class<T> type, Function<Object, T> typeFunction )
    {
        typeFunctions.put( type, (Function<Object, Object>) typeFunction );
    }

    private static <T> Function<Object, T> identity()
    {
        return new Function<Object, T>()
        {
            @Override
            public T map( Object json )
            {
                return (T) json;
            }
        };
    }

    static
    {
        nonStringClasses.add( Boolean.class );
        nonStringClasses.add( Short.class );
        nonStringClasses.add( Integer.class );
        nonStringClasses.add( Long.class );
        nonStringClasses.add( Byte.class );
        nonStringClasses.add( Float.class );
        nonStringClasses.add( Double.class );

        registerDeserializer( String.class, new Function<Object, String>()
        {
            @Override
            public String map( Object o )
            {
                return o.toString();
            }
        } );

        // Primitive types
        registerDeserializer( Boolean.class, JSONDeserializer.<Boolean>identity() );
        registerDeserializer( Integer.class, new Function<Object, Integer>()
        {
            @Override
            public Integer map( Object o )
            {
                return ((Number) o).intValue();
            }
        } );

        registerDeserializer( Long.class, new Function<Object, Long>()
        {
            @Override
            public Long map( Object o )
            {
                return ((Number) o).longValue();
            }
        } );

        registerDeserializer( Short.class, new Function<Object, Short>()
        {
            @Override
            public Short map( Object o )
            {
                return ((Number) o).shortValue();
            }
        } );

        registerDeserializer( Byte.class, new Function<Object, Byte>()
        {
            @Override
            public Byte map( Object o )
            {
                return ((Number) o).byteValue();
            }
        } );

        registerDeserializer( Float.class, new Function<Object, Float>()
        {
            @Override
            public Float map( Object o )
            {
                return ((Number) o).floatValue();
            }
        } );

        registerDeserializer( Double.class, new Function<Object, Double>()
        {
            @Override
            public Double map( Object o )
            {
                return ((Number) o).doubleValue();
            }
        } );

        // Number types
        registerDeserializer( BigDecimal.class, new Function<Object, BigDecimal>()
        {
            @Override
            public BigDecimal map( Object json )
            {
                return new BigDecimal( json.toString() );
            }
        } );
        registerDeserializer( BigInteger.class, new Function<Object, BigInteger>()
        {
            @Override
            public BigInteger map( Object json )
            {
                return new BigInteger( json.toString() );
            }
        } );

        // Date types
        registerDeserializer( Date.class, new Function<Object, Date>()
        {
            @Override
            public Date map( Object json )
            {
                return DateFunctions.fromString( json.toString() );
            }
        } );
        registerDeserializer( DateTime.class, new Function<Object, DateTime>()
        {
            @Override
            public DateTime map( Object json )
            {
                return new DateTime( json, DateTimeZone.UTC );
            }
        } );
        registerDeserializer( LocalDateTime.class, new Function<Object, LocalDateTime>()
        {
            @Override
            public LocalDateTime map( Object json )
            {
                return new LocalDateTime( json );
            }
        } );
        registerDeserializer( LocalDate.class, new Function<Object, LocalDate>()
        {
            @Override
            public LocalDate map( Object json )
            {
                return new LocalDate( json );
            }
        } );

        // Other supported types
        registerDeserializer( EntityReference.class, new Function<Object, EntityReference>()
        {
            @Override
            public EntityReference map( Object json )
            {
                return EntityReference.parseEntityReference( json.toString() );
            }
        } );
    }

    private Module module;

    public JSONDeserializer( Module module )
    {
        this.module = module;
    }

    public Object deserialize( Object json, ValueType valueType )
            throws JSONException
    {
        if (json == JSONObject.NULL)
            return null;

        Function<Object, ? extends Object> typeFunction = typeFunctions.get( valueType.type() );

        if (typeFunction != null)
            return typeFunction.map( json );

        if( valueType instanceof CollectionType )
        {
            CollectionType collectionType = (CollectionType) valueType;

            JSONArray array = (JSONArray) json;

            Collection<Object> coll;
            if( valueType.type().equals( Set.class ) )
            {
                coll = new LinkedHashSet<Object>();
            } else
            {
                coll = new ArrayList<Object>();
            }

            for( int i = 0; i < array.length(); i++ )
            {
                Object value = array.get( i );
                coll.add( deserialize( value, collectionType.collectedType() ));
            }

            return coll;
        } else if( valueType instanceof EnumType )
        {
            try
            {
                Class enumType = valueType.type();

                // Get enum value
                return Enum.valueOf( enumType, (String) json );
            } catch( Exception e )
            {
                throw new IllegalArgumentException( e );
            }
        } else if( valueType instanceof MapType )
        {
            if( json instanceof String )
            {
                try
                {
                    // Legacy handling of serialized maps
                    String serializedString = (String) json;
                    byte[] bytes = serializedString.getBytes( "UTF-8" );
                    bytes = Base64Encoder.decode( bytes );
                    ByteArrayInputStream bin = new ByteArrayInputStream( bytes );
                    ObjectInputStream oin = new ObjectInputStream( bin );
                    Object result = oin.readObject();
                    oin.close();

                    return result;
                } catch( IOException e )
                {
                    throw new IllegalStateException( "Could not deserialize value", e );
                } catch( ClassNotFoundException e )
                {
                    throw new IllegalStateException( "Could not find class for serialized value", e );
                }
            } else
            {
                MapType mapType = (MapType) valueType;

                // New array-based handling
                JSONArray array = (JSONArray) json;

                Map<Object, Object> map = (Map<Object, Object>) DefaultValues.getDefaultValue( Map.class );

                for( int i = 0; i < array.length(); i++ )
                {
                    JSONObject entry = array.getJSONObject( i );
                    Object key = deserialize( entry.get( "key" ), mapType.getKeyType() );
                    Object value = deserialize(entry.get( "value" ), mapType.getValueType());
                    map.put( key, value );
                }

                return map;
            }
        } else if( valueType instanceof ValueCompositeType )
        {
            JSONObject jsonObject = (JSONObject) json;

            ValueCompositeType actualValueType = (ValueCompositeType) valueType;
            Iterable<PersistentPropertyDescriptor> actualTypes = actualValueType.types();
            String actualType = jsonObject.optString( "_type" );
            if( !actualType.equals( "" ) )
            {
                ValueDescriptor descriptor = ((ModuleSPI) module).valueDescriptor( actualType );

                if( descriptor == null )
                {
                    throw new IllegalArgumentException( "Could not find any value of type '" + actualType + "' in module" + module );
                }

                actualValueType = (ValueCompositeType) descriptor.valueType();
                actualTypes = actualValueType.types();
            }

            final Map<QualifiedName, Object> values = new HashMap<QualifiedName, Object>();
            for( PersistentPropertyDescriptor persistentProperty : actualTypes )
            {
                Object valueJson = null;
                try
                {
                    valueJson = jsonObject.opt( persistentProperty.qualifiedName().name() );

                    Object value = null;
                    if( valueJson != null && !valueJson.equals( JSONObject.NULL ) )
                    {
                        value = deserialize( valueJson, persistentProperty.valueType() );
                    }

                    values.put( persistentProperty.qualifiedName(), value );
                } catch( JSONException e )
                {
                    // Not found in JSON or wrong format - try defaulting it
                    try
                    {
                        Object defaultValue = DefaultValues.getDefaultValue( persistentProperty.valueType().type());
                        values.put( persistentProperty.qualifiedName(), defaultValue );
                    } catch( RuntimeException e1 )
                    {
                        // Didn't work, throw the exception
                        throw e;
                    }
                }
            }

            ValueBuilder valueBuilder = module.valueBuilderFactory()
                    .newValueBuilder( actualValueType.type() );
            valueBuilder.withState( new Function<PropertyInfo, Object>()
            {
                @Override
                public Object map( PropertyInfo propertyInfo )
                {
                    return values.get( propertyInfo.qualifiedName() );
                }
            });

            return valueBuilder.newInstance();
        } else
        {
            try
            {
                if( json instanceof JSONObject )
                {
                    // ValueComposite deserialization
                    JSONObject jsonObject = (JSONObject) json;
                    String type = jsonObject.getString( "_type" );

                    ValueDescriptor valueDescriptor = ((ModuleSPI) module).valueDescriptor( type );
                    return deserialize( json, valueDescriptor.valueType() );
                } else
                {
                    String serializedString = (String) json;
                    byte[] bytes = serializedString.getBytes( "UTF-8" );
                    bytes = Base64Encoder.decode( bytes );
                    ByteArrayInputStream bin = new ByteArrayInputStream( bytes );
                    ObjectInputStream oin = new ObjectInputStream( bin );
                    Object result = oin.readObject();
                    oin.close();

                    if( result instanceof EntityReference )
                    {
                        EntityReference ref = (EntityReference) result;
                        if( !valueType.type().equals( EntityReference.class ) )
                        {
                            Class mixinType = valueType.type();
                            UnitOfWork unitOfWork = module.unitOfWorkFactory().currentUnitOfWork();
                            if( unitOfWork != null )
                            {
                                result = unitOfWork.get( mixinType, ref.identity() );
                            }
                        }
                    }

                    return result;
                }
            } catch( IOException e )
            {
                throw new IllegalStateException( "Could not deserialize value", e );
            } catch( ClassNotFoundException e )
            {
                throw new IllegalStateException( "Could not find class for serialized value", e );
            }
        }
    }
}
