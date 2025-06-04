/*
 * Copyright © 2022-2025 Metreeca srl
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

import com.metreeca.mesh.shapes.Property;
import com.metreeca.mesh.shapes.Type;
import com.metreeca.shim.Futures;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.repository.RepositoryConnection;

import java.net.URI;
import java.util.Collection;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.logging.Logger;
import java.util.stream.Stream;

import static com.metreeca.mesh.rdf4j.SPARQLConverter.rdf;

import static java.lang.String.format;
import static java.util.concurrent.CompletableFuture.allOf;
import static java.util.concurrent.CompletableFuture.completedFuture;

final class SPARQLUpdater extends _StoreLoader.Worker {

    private static final Logger LOGGER=Logger.getLogger(SPARQLUpdater.class.getName());


    //̸/////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private final URI context;

    private final Collection<Task> inserts=new HashSet<>();
    private final Collection<Task> deletes=new HashSet<>();


    SPARQLUpdater(final RDF4JStore rdf4j) {
        context=rdf4j.context();
    }


    CompletableFuture<Void> insert(final URI id, final Set<Type> types) {
        return Futures.allOf(Stream.concat(
                Stream.of(new Task(rdf(id), RDF.TYPE, null).schedule(deletes::add)),
                types.stream().map(type -> new Task(rdf(id), RDF.TYPE, rdf(type.uri())).schedule(inserts::add))
        ));
    }


    CompletableFuture<Void> remove(final URI id) {
        return Futures.allOf(Stream.of(
                new Task(rdf(id), null, null).schedule(deletes::add),
                new Task(null, null, rdf(id)).schedule(deletes::add)
        ));
    }


    CompletableFuture<Void> insert(final URI id, final Property property, final com.metreeca.mesh.Value value) {
        return allOf(rdf(value)
                .flatMap(v -> Stream.<Optional<CompletableFuture<Void>>>of(
                        property.forward().map(f -> new Task(rdf(id), rdf(f), v).schedule(inserts::add)),
                        v.isResource()
                                ? property.reverse().map(r -> new Task((Resource)v, rdf(r), rdf(id)).schedule(inserts::add))
                                : Optional.<CompletableFuture<Void>>empty(),
                        Optional.empty()
                ))
                .flatMap(Optional::stream)
                .toArray(CompletableFuture[]::new)
        );
    }

    CompletableFuture<Void> remove(final URI id, final Property property) {
        return allOf(Stream.<Optional<CompletableFuture<Void>>>of(
                                property.forward().map(f -> new Task(rdf(id), rdf(f), null).schedule(deletes::add)),
                                property.reverse().map(r -> new Task(null, rdf(r), rdf(id)).schedule(deletes::add)),
                                Optional.empty()
                        )
                        .flatMap(Optional::stream)
                        .toArray(CompletableFuture[]::new)
        );
    }


    @Override CompletableFuture<Void> run(final RepositoryConnection connection) {
        if ( inserts.isEmpty() && deletes.isEmpty() ) { return completedFuture(null); } else {

            return CompletableFuture.runAsync(() -> {

                snapshot(deletes).forEach(delete -> {

                    final Resource resource=delete.resource;
                    final IRI predicate=delete.predicate;
                    final Value value=delete.value;

                    LOGGER.fine(() -> format("- %s %s %s (%s)", resource, predicate, value, context));

                    if ( resource == null && predicate == null && value == null ) {

                        connection.clear(context == null ? null : rdf(context));

                    } else {

                        connection.remove(resource, predicate, value, context == null ? null : rdf(context));

                    }

                    delete.complete();

                });

                snapshot(inserts).forEach(insert -> {

                    final Resource resource=insert.resource;
                    final IRI predicate=insert.predicate;
                    final Value value=insert.value;

                    LOGGER.fine(() -> format("+ %s %s %s (%s)", resource, predicate, value, context));

                    connection.add(resource, predicate, value, context == null ? null : rdf(context));

                    insert.complete();

                });

            });

        }
    }


    //̸/////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private static final class Task {

        final Resource resource;
        final IRI predicate;
        final Value value;

        private final CompletableFuture<Void> future=new CompletableFuture<>();


        private Task(final Resource resource, final IRI predicate, final Value value) {
            this.resource=resource;
            this.predicate=predicate;
            this.value=value;
        }


        private CompletableFuture<Void> schedule(final Consumer<Task> queue) {

            queue.accept(this);

            return future;
        }

        private void complete() {
            future.complete(null);
        }

    }

}
