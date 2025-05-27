/*
 * Copyright © 2025 Metreeca srl
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.metreeca.mesh.rdf4j;

import com.metreeca.mesh.Value;

final class _StoreDeleter {

    private final RDF4J.Loader loader;


    _StoreDeleter(final RDF4J.Loader loader) {
        this.loader=loader;
    }


    boolean delete(final Value value, boolean force) {

        // !!! inverse properties
        // !!! embedded properties

        return false;
    }


    //     boolean delete(final IRI id) {
    //         return id.id()
    //
    //                 .map(id -> {
    //
    //                     return false; // !!!
    //
    //
    //                         if ( value == null ) {
    //                             throw new NullPointerException("null value");
    //                         }
    //
    //                         return process(value, (frame, base) -> {
    //
    //                             final IRI id=iri(frame.id());
    //
    //                             try ( final RepositoryConnection connection=repository.getConnection() ) {
    //
    //                                 // !!! batch?
    //
    //                                 final boolean present=connection.hasStatement(id, null, null, true) // !!! context
    //                                         || connection.hasStatement(null, null, id, true); // !!! context
    //
    //                                 if ( present ) {
    //
    //                                     try {
    //
    //                                         connection.begin();
    //                                         connection.remove(id, null, null); // !!! context
    //                                         connection.remove((Resource)null, null, id); // !!! context
    //                                         connection.commit();
    //
    //                                         return Optional.of(frame);
    //
    //                                     } catch ( final Throwable e ) {
    //
    //                                         connection.rollback();
    //
    //                                         throw e;
    //
    //                                     }
    //
    //                                 } else {
    //
    //                                     return Optional.empty();
    //
    //                                 }
    //
    //                             }
    //          finally {
    //
    //             if (connection.isActive() ) {
    //                 connection.rollback();
    //             }
    //
    //         }
    //                     //
    //                     //     });
    //
    //
    //                 })
    //
    //                 .orElse(false);
    //     }

}
