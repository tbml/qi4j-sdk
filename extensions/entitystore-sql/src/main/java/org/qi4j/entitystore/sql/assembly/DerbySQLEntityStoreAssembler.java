/*
 * Copyright (c) 2010, Stanislav Muhametsin. All Rights Reserved.
 * Copyright (c) 2010, Paul Merlin. All Rights Reserved.
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
package org.qi4j.entitystore.sql.assembly;

import java.io.IOException;

import org.qi4j.api.common.Visibility;
import org.qi4j.entitystore.sql.internal.DerbySQLDatabaseSQLServiceMixin;
import org.qi4j.library.sql.assembly.DataSourceAssembler;

import org.sql.generation.api.vendor.DerbyVendor;
import org.sql.generation.api.vendor.SQLVendor;
import org.sql.generation.api.vendor.SQLVendorProvider;

public class DerbySQLEntityStoreAssembler
        extends AbstractSQLEntityStoreAssembler
{

    public static final String ENTITYSTORE_SERVICE_NAME = "entitystore-derby";

    public DerbySQLEntityStoreAssembler( DataSourceAssembler assembler )
    {
        super( assembler );
    }

    public DerbySQLEntityStoreAssembler( Visibility visibility, DataSourceAssembler dataSourceAssembler )
    {
        super( visibility, dataSourceAssembler );
    }

    @Override
    protected String getEntityStoreServiceName()
    {
        return ENTITYSTORE_SERVICE_NAME;
    }

    @Override
    protected Class<?> getDatabaseSQLServiceSpecializationMixin()
    {
        return DerbySQLDatabaseSQLServiceMixin.class;
    }

    @Override
    protected SQLVendor getSQLVendor()
            throws IOException
    {
        return SQLVendorProvider.createVendor( DerbyVendor.class );
    }

}
