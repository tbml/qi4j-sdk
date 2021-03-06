/*
 * Copyright (c) 2007, Rickard Öberg. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.qi4j.runtime.bootstrap;

import java.util.Arrays;
import org.qi4j.api.common.Visibility;
import org.qi4j.bootstrap.EntityDeclaration;

/**
 * Declaration of a Composite. Created by {@link org.qi4j.bootstrap.ModuleAssembly#transients(Class[])}.
 */
public final class EntityDeclarationImpl
    implements EntityDeclaration
{
    private Iterable<EntityAssemblyImpl> entities;

    public EntityDeclarationImpl( Iterable<EntityAssemblyImpl> entities )
    {
        this.entities = entities;
    }

    public EntityDeclaration setMetaInfo( Object info )
    {
        for( EntityAssemblyImpl entity : entities )
        {
            entity.metaInfo.set( info );
        }
        return this;
    }

    public EntityDeclaration visibleIn( Visibility visibility )
    {
        for( EntityAssemblyImpl entity : entities )
        {
            entity.visibility = visibility;
        }
        return this;
    }

    public EntityDeclaration withConcerns( Class<?>... concerns )
    {
        for( EntityAssemblyImpl entity : entities )
        {
            entity.concerns.addAll( Arrays.asList( concerns ) );
        }
        return this;
    }

    public EntityDeclaration withSideEffects( Class<?>... sideEffects )
    {
        for( EntityAssemblyImpl entity : entities )
        {
            entity.sideEffects.addAll( Arrays.asList( sideEffects ) );
        }
        return this;
    }

    public EntityDeclaration withMixins( Class<?>... mixins )
    {
        for( EntityAssemblyImpl entity : entities )
        {
            entity.mixins.addAll( Arrays.asList( mixins ) );
        }
        return this;
    }

    public EntityDeclaration withTypes( Class<?>... types )
    {
        for( EntityAssemblyImpl entity : entities )
        {
            entity.types.addAll( Arrays.asList( types ) );
        }
        return this;
    }
}