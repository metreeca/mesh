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

import com.metreeca.mesh.Field;
import com.metreeca.mesh.Value;
import com.metreeca.mesh.queries.Query;
import com.metreeca.mesh.queries.Specs;
import com.metreeca.mesh.queries.Table;
import com.metreeca.mesh.queries.Tuple;
import com.metreeca.mesh.rdf4j.RDF4J.Loader;
import com.metreeca.mesh.shapes.Property;
import com.metreeca.mesh.shapes.Shape;

import java.net.URI;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

import static com.metreeca.mesh.Field.*;
import static com.metreeca.mesh.Value.*;

import static java.lang.String.format;
import static java.util.concurrent.CompletableFuture.allOf;
import static java.util.concurrent.CompletableFuture.completedFuture;
import static java.util.function.Predicate.not;

final class StoreRetriever {

    private static <T> CompletableFuture<List<T>> merge(final Collection<CompletableFuture<T>> futures) {
        return allOf(futures.toArray(CompletableFuture[]::new)).thenApply(v -> futures.stream()
                .map(CompletableFuture::join)
                .toList()
        );
    }


    //̸/////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private final Loader loader;

    private final SPARQLFetcher fetcher;
    private final SPARQLSelector selector;


    StoreRetriever(final Loader loader) {

        this.loader=loader;

        this.fetcher=loader.worker(SPARQLFetcher::new);
        this.selector=loader.worker(SPARQLSelector::new);
    }


    Value retrieve(final Value model) {
        return loader.execute(() -> model

                .accept(new Visitor<CompletableFuture<Value>>() {

                    @Override
                    public CompletableFuture<Value> visit(final Value host, final List<Value> values) {
                        return merge(values.stream().map(v -> v.accept(this)).toList()).thenApply(Value::array);
                    }

                    @Override
                    public CompletableFuture<Value> visit(final Value host, final Map<String, Value> fields) {
                        return id(host).flatMap(id -> shape(host).map(shape -> retrieve(id, shape, fields)))
                                .orElseGet(() -> completedFuture(Nil()));
                    }

                    @Override
                    public CompletableFuture<Value> visit(final Value host, final Object object) {
                        return completedFuture(Nil());
                    }

                })

        ).join();
    }


    //̸/////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private CompletableFuture<Value> retrieve(final URI id, final Shape shape, final Map<String, Value> fields) {

        // generate field futures before checking for resource existence, so that a single query is generated

        final Collection<CompletableFuture<Field>> futures=fields.entrySet().stream().flatMap(entry -> {

            final String name=entry.getKey();
            final Value value=entry.getValue();

            if ( name.equals(ID) ) {

                return Stream.of(completedFuture(id(id)));

            } else if ( isReserved(name) ) {

                return Stream.empty();

            } else {

                final Property property=shape.property(name).orElseThrow(() ->
                        new IllegalArgumentException(format("unknown property <%s>", name))
                );

                return value.accept(new Visitor<Stream<CompletableFuture<Field>>>() {

                    @Override
                    public Stream<CompletableFuture<Field>> visit(final Value host, final List<Value> values) {
                        return values.stream().map(value -> retrieve(id, shape, property, value));
                    }

                    @Override
                    public Stream<CompletableFuture<Field>> visit(final Value host, final Object object) {
                        return Stream.of(
                                object instanceof final Query query ? retrieve(shape.virtual(), id, property, query)
                                        : shape.virtual() ? completedFuture(field(name, host))
                                        : retrieve(id, shape, property, host)
                        );
                    }

                });

            }

        }).toList();

        return (shape.virtual()

                ? completedFuture(true)
                : fetcher.fetch(id)

        ).thenCompose(exists -> exists

                ? merge(futures).thenApply(Value::object)
                : completedFuture(Nil())

        );
    }


    private CompletableFuture<Field> retrieve(
            final boolean virtual, final URI id, final Property property, final Query query
    ) {

        return query.model().value(Specs.class)

                .map(specs -> selector.tuples(virtual, id, property, query)

                        .thenCompose(tuples -> merge(tuples.stream()

                                .map(tuple -> merge(tuple.fields().stream()

                                        .map(field -> {

                                            final String name=field.name();
                                            final Value value=field.value();

                                            return specs.column(name)
                                                    .map(probe -> retrieve(value,
                                                            probe.expression().apply(property.shape()),
                                                            probe.model()
                                                    ))
                                                    .orElseGet(() -> completedFuture(value))
                                                    .thenApply(v -> field(name, v));

                                        })

                                        .toList()

                                ).thenApply(Tuple::new))

                                .toList()

                        ))

                        .thenApply(values ->
                                field(property.name(), Value.value(new Table(values)))
                        ))

                .orElseGet(() -> selector.values(virtual, id, property, query)

                        .thenCompose(values -> merge(values.stream()
                                .map(value -> retrieve(value, property.shape(), query.model()))
                                .toList()
                        ))

                        .thenApply(values ->
                                field(property.name(), array(values))
                        )

                );

    }

    private CompletableFuture<Field> retrieve(
            final URI id, final Shape shape, final Property property, final Value model
    ) {

        final Boolean isCollection=property.shape().maxCount().map(limit -> limit > 1).orElse(true);

        return fetcher.fetch(id, property).thenCompose(value -> {

            final Value effective=shape.virtual() && value.isEmpty() ? prune(model) : value;

            if ( effective.isEmpty() ) {

                return completedFuture(Nil());

            } else if ( isCollection ) {

                return merge(values(effective)
                        .map(v -> retrieve(v, property.shape(), model))
                        .toList()
                ).thenApply(Value::array);

            } else {

                return retrieve(value(effective), property.shape(), model);

            }

        }).thenApply(v -> field(property.name(), v));
    }

    private CompletableFuture<Value> retrieve(final Value value, final Shape shape, final Value model) {
        return id(value)
                .flatMap(id -> model.object().map(fields -> retrieve(id, shape, fields)))
                .orElseGet(() -> completedFuture(value));
    }


    //̸/////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /*
     * Recursively removes default values from models.
     */
    private Value prune(final Value model) {
        return model.accept(new Visitor<>() {

            @Override public Value visit(final Value host, final Boolean bit) {
                return bit ? host : Nil();
            }

            @Override public Value visit(final Value host, final Number number) {
                return number.longValue() == 0 ? Nil() : host;
            }

            @Override public Value visit(final Value host, final String string) {
                return string.isEmpty() ? Nil() : host;
            }

            @Override public Value visit(final Value host, final URI uri) {
                return uri.toString().isEmpty() ? Nil() : host;
            }

            @Override public Value visit(final Value host, final Map<String, Value> fields) {
                throw new UnsupportedOperationException(";( be implemented"); // !!!
            }

            @Override public Value visit(final Value host, final List<Value> values) {

                final List<Value> pruned=values.stream()
                        .map(v -> v.accept(this))
                        .filter(not(Value::isEmpty))
                        .toList();

                return pruned.isEmpty() ? Nil() : array(pruned);
            }

            @Override public Value visit(final Value host, final Object object) {
                return host;
            }

        });
    }


    private Value value(final Value value) {
        return value.accept(new Visitor<>() {

            @Override public Value visit(final Value host, final List<Value> values) {
                return values.stream().findFirst().orElse(Nil());
            }

            @Override public Value visit(final Value host, final Object object) {
                return host;
            }

        });
    }

    private Stream<Value> values(final Value value) {
        return value.accept(new Visitor<>() {

            @Override public Stream<Value> visit(final Value host, final List<Value> values) {
                return values.stream();
            }

            @Override public Stream<Value> visit(final Value host, final Object object) {
                return Stream.of(host);
            }

        });
    }

}
