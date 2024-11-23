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
import com.metreeca.shim.Collections;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.TupleQuery;
import org.eclipse.rdf4j.query.impl.SimpleDataset;
import org.eclipse.rdf4j.repository.RepositoryConnection;

import java.net.URI;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;
import java.util.stream.Stream;

import static com.metreeca.mesh.Value.Nil;
import static com.metreeca.mesh.rdf4j.Coder.*;
import static com.metreeca.mesh.rdf4j.SPARQL.*;
import static com.metreeca.mesh.rdf4j.SPARQLConverter.json;
import static com.metreeca.mesh.rdf4j.SPARQLConverter.rdf;
import static com.metreeca.mesh.shapes.Property.property;
import static com.metreeca.shim.URIs.term;

import static java.lang.String.format;
import static java.util.concurrent.CompletableFuture.completedFuture;
import static java.util.concurrent.CompletableFuture.runAsync;
import static java.util.function.Predicate.not;
import static java.util.stream.Collectors.*;
import static org.eclipse.rdf4j.model.util.Values.getValueFactory;
import static org.eclipse.rdf4j.model.util.Values.iri;

final class SPARQLFetcher extends _StoreLoader.Worker {

    private static final Logger LOGGER=Logger.getLogger(SPARQLFetcher.class.getName());


    private static final Value FALSE=getValueFactory().createLiteral(false);
    private static final Value TRUE=getValueFactory().createLiteral(true);

    private static final IRI SELF=iri(term("self").toString());

    private static final Property SUBJECT=property("self").forward(URI.create(SELF.stringValue()));
    private static final Property OBJECT=property("self").reverse(URI.create(SELF.stringValue()));


    //̸/////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private final URI context;

    private final Map<Key, CompletableFuture<com.metreeca.mesh.Value>> edges=new ConcurrentHashMap<>();


    SPARQLFetcher(final RDF4JStore rdf4j) {
        context=rdf4j.context();
    }


    CompletableFuture<Boolean> fetch(final URI id) {

        final CompletableFuture<com.metreeca.mesh.Value> subject=fetch(id, SUBJECT);
        final CompletableFuture<com.metreeca.mesh.Value> object=fetch(id, OBJECT);

        return subject.thenCombine(object, (s, o) ->
                Stream.concat(s.values(), o.values()).anyMatch(com.metreeca.mesh.Value.TRUE::equals)
        );
    }

    CompletableFuture<com.metreeca.mesh.Value> fetch(final URI id, final Property property) {
        return property.forward().map(uri -> new Key(rdf(id), rdf(uri), false))
                .or(() -> property.reverse().map(uri -> new Key(rdf(id), rdf(uri), true)))
                .map(key -> edges.computeIfAbsent(key, k -> new CompletableFuture<>()))
                .orElseGet(() -> completedFuture(Nil()));
    }


    //̸/////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Override CompletableFuture<Void> run(final RepositoryConnection connection) {
        return Optional

                // identify pending edges

                .of(edges.entrySet().stream()
                        .filter((entry -> !entry.getValue().isDone()))
                        .map(Entry::getKey)
                        .toList()
                )

                .filter(not(List::isEmpty))

                .map(pending -> runAsync(() -> {

                    // collect matching values

                    final TupleQuery query=connection.prepareTupleQuery(generate(pending));

                    if ( context != null ) {

                        final SimpleDataset dataset=new SimpleDataset();

                        dataset.addDefaultGraph(rdf(context));

                        query.setDataset(dataset);

                    }

                    try ( final Stream<BindingSet> tuples=query.evaluate().stream() ) {

                        tuples.collect(groupingBy(

                                tuple -> new Key(
                                        tuple.getValue("i"),
                                        (IRI)tuple.getValue("p"),
                                        tuple.getValue("r").equals(TRUE)
                                ),

                                mapping(tuple -> tuple.getValue("v"), toSet())

                        )).forEach((key, values) -> Optional.ofNullable(edges.get(key))
                                .map(future -> future.complete(json(values)))
                                .orElseThrow(() -> new AssertionError(format("missing edge <%s>", key)))
                        );

                    }

                    // provide empty value set for unmatched keys

                    pending.stream()
                            .map(edges::get)
                            .filter((values -> !values.isDone()))
                            .forEach(values -> values.complete(Nil()));

                }))

                .orElseGet(() -> completedFuture(null));
    }


    private String generate(final Collection<Key> pending) {

        final List<List<Value>> subjects=pending.stream()
                .filter(not(Key::reverse))
                .filter(key -> key.predicate().equals(SELF))
                .map(key -> Collections.list(key.resource(), key.predicate(), FALSE))
                .toList();

        final List<List<Value>> objects=pending.stream()
                .filter(Key::reverse)
                .filter(key -> key.predicate().equals(SELF))
                .map(key -> Collections.list(key.resource(), key.predicate(), TRUE))
                .toList();

        final List<List<Value>> forwards=pending.stream()
                .filter(not(Key::reverse))
                .filter(key -> !key.predicate().equals(SELF))
                .map(key -> Collections.list(key.resource(), key.predicate(), FALSE))
                .toList();

        final List<List<Value>> reverses=pending.stream()
                .filter(Key::reverse)
                .filter(key -> !key.predicate().equals(SELF))
                .map(key -> Collections.list(key.resource(), key.predicate(), TRUE))
                .toList();

        final List<Coder> vars=Collections.list(var("i"), var("p"), var("r"));

        final String sparql=sparql(items(
                select(var("i"), var("p"), var("v"), var("r")),
                where(

                        space(union(

                                subjects.isEmpty() ? nothing() : items(
                                        space(values(vars, subjects)),
                                        space(bind(exists(items(var("i"), var("x"), var("y"))), "v"))
                                ),

                                objects.isEmpty() ? nothing() : items(
                                        space(values(vars, objects)),
                                        space(bind(exists(items(var("x"), var("y"), var("i"))), "v"))
                                ),

                                forwards.isEmpty() ? nothing() : items(
                                        space(values(vars, forwards)),
                                        space(edge(var("i"), var("p"), var("v")))
                                ),

                                reverses.isEmpty() ? nothing() : items(
                                        space(values(vars, reverses)),
                                        space(edge(var("v"), var("p"), var("i")))
                                )

                        ))

                )
        ));

        LOGGER.fine(() -> "# fetch\n\n%s".formatted(sparql));

        return sparql;
    }


    //̸/////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private record Key(Value resource, IRI predicate, boolean reverse) { // !!! json values

        @Override public String toString() {
            return reverse
                    ? format("<?> <%s> <%s>", predicate.stringValue(), resource.stringValue())
                    : format("<%s> <%s> <?>", resource.stringValue(), predicate.stringValue());
        }

    }

}
