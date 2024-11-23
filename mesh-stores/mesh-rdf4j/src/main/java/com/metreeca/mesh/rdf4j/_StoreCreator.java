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

final class _StoreCreator {

    private final RDF4J.Loader loader;
    private final _SPARQLUpdater updater;


    _StoreCreator(final RDF4J.Loader loader) {
        this.loader=loader;
        this.updater=loader.worker(_SPARQLUpdater::new);
    }


    boolean create(final Value value, final boolean force) {
        throw new UnsupportedOperationException(";( be implemented"); // !!!
    }

    // boolean create(final IRI id, final _Shape shape, final _Frame frame) {
    //
    //     final RepositoryConnection connection=loader.connection();
    //
    //     if ( connection.hasStatement(id, null, null, true) ) { // !!! context
    //
    //         return false;
    //
    //     } else {
    //
    //         final Stream<Statement> description=encode(shape, id, frame, connection.getValueFactory());
    //
    //         try {
    //
    //             connection.begin();
    //             connection.add(description::iterator); // !!! context // !!! use SPARQLUpdater
    //             connection.commit();
    //
    //             return true;
    //
    //         } finally {
    //
    //             if ( connection.isActive() ) {
    //                 connection.rollback();
    //             }
    //
    //         }
    //
    //     }
    // }


    //̸/////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    // private Stream<Statement> encode(final _Shape shape, final Resource id, final _Frame frame, final ValueFactory factory) {
    //
    //     return shape.predicates().keySet().stream()
    //
    //             .filter(not(ID::equals))
    //
    //             .flatMap(property -> frame.values(property)
    //
    //                     .map(value -> {
    //
    //                         if ( value instanceof _Frame ) {
    //
    //                             final _Frame _frame=(_Frame)value;
    //                             final Resource _id=_frame.id().map(Resource.class::cast).orElseGet(factory::createBNode);
    //
    //                             return link(id, property, _id, factory);
    //
    //                             // !!! cascading return Stream.concat(link(id, property, _id, factory), encode(_id, _frame, factory));
    //
    //                         } else {
    //
    //                             return link(id, property, value, factory);
    //
    //                         }
    //
    //                     })
    //
    //             );
    //
    // }

    // private Statement link(final Resource id, final IRI property, final Value value, final ValueFactory factory) {
    //     return _Frame.forward(property) ? (factory.createStatement(id, property, value))
    //             : value.isResource() ? (factory.createStatement((Resource)value, _Frame.reverse(property), id))
    //             : error("value <%s> for reverse uri <%s> is not a resource", value, property);
    // }

}
