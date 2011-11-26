/*
 * Copyright (c) 2010, Stanislav Muhametsin. All Rights Reserved.
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

package org.qi4j.index.sql.support.api;

import org.qi4j.spi.entity.EntityState;

import java.sql.SQLException;

/**
 * This is the interface used by SQL-Indexing whenever
 * 
 * @author Stanislav Muhametsin
 */
public interface SQLIndexing
{
    /**
     * This method is called when states need to be indexed.
     * 
     * @param changedStates The states which changed.
     * @throws SQLException If SQL.
     */
    void indexEntities( Iterable<EntityState> changedStates )
        throws SQLException;

}