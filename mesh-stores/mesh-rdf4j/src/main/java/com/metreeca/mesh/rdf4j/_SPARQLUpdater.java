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

import com.metreeca.mesh.shapes.Property;
import com.metreeca.mesh.shapes.Type;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.repository.RepositoryConnection;

import java.net.URI;
import java.util.Collection;
import java.util.HashSet;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

import static com.metreeca.mesh.rdf4j.Coder.*;
import static com.metreeca.mesh.rdf4j.SPARQL.*;
import static com.metreeca.mesh.rdf4j.SPARQLConverter.rdf;

import static java.util.concurrent.CompletableFuture.allOf;
import static java.util.concurrent.CompletableFuture.completedFuture;
import static java.util.stream.Collectors.toList;

final class _SPARQLUpdater extends RDF4J.Worker {

    private final Collection<Task> inserts=new HashSet<>();
    private final Collection<Task> deletes=new HashSet<>();


    CompletableFuture<Void> insert(final URI id, final Type type) {
        return new Task(rdf(id), RDF.TYPE, rdf(type.uri())).schedule(inserts::add);
    }

    CompletableFuture<Void> insert(final URI id, final Property property, final com.metreeca.mesh.Value value) {
        return allOf(rdf(value)
                .map(v -> new Task(rdf(id), rdf(property.uri()), v).schedule(inserts::add))
                .toArray(CompletableFuture[]::new)
        );
    }

    CompletableFuture<Void> delete(final URI id) {
        return new Task(rdf(id), null, null).schedule(deletes::add);
    }


    @Override
    CompletableFuture<Void> run(final RepositoryConnection connection) {
        if ( inserts.isEmpty() && deletes.isEmpty() ) { return completedFuture(null); } else {

            return CompletableFuture.runAsync(() -> {

                final Collection<Task> ds=snapshot(deletes);
                final Collection<Task> is=snapshot(inserts);

                final String update=sparql(update(ds, is));

                connection.prepareUpdate(update).execute();

                ds.forEach(Task::complete);
                is.forEach(Task::complete);

            });

        }
    }


    //̸/////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private Coder update(final Collection<Task> deletes, final Collection<Task> inserts) {
        return items(
                comment("update"),
                delete(deletes),
                insert(inserts)
        );
    }

    private Coder delete(final Collection<Task> deletes) {
        return deletes.isEmpty() ? nothing() : space(items(text("\rdelete where "), block(items(

                deletes.stream()
                        .map(delete -> {

                            final Resource resource=delete.resource;
                            final IRI predicate=delete.predicate;
                            final Value value=delete.value;

                            return line(edge(
                                    resource == null ? var(id(new Object())) : value(resource),
                                    predicate == null ? var(id(new Object())) : value(predicate),
                                    value == null ? var(id(new Object())) : value(value)
                            ));

                        })
                        .collect(toList())

        )), text(";")));
    }

    private Coder insert(final Collection<Task> inserts) {
        return inserts.isEmpty() ? nothing() : space(items(text("\rinsert data "), block(items(

                inserts.stream()
                        .map(delete -> {

                            final Resource resource=delete.resource;
                            final IRI predicate=delete.predicate;
                            final Value value=delete.value;

                            return line(edge(value(resource), iri(predicate), value(value)));

                        })
                        .collect(toList())

        )), text(";")));
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
