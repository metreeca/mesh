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

import com.metreeca.mesh.Value;
import com.metreeca.mesh.queries.Query;
import com.metreeca.mesh.queries.Specs;
import com.metreeca.mesh.queries.Table;
import com.metreeca.mesh.queries.Tuple;
import com.metreeca.mesh.shapes.Property;
import com.metreeca.mesh.shapes.Shape;
import com.metreeca.mesh.tools.StoreException;

import java.net.URI;
import java.time.*;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

import static com.metreeca.mesh.Value.*;
import static com.metreeca.shim.Collections.list;
import static com.metreeca.shim.Futures.allItemsOf;
import static com.metreeca.shim.Locales.ANY;

import static java.lang.String.format;
import static java.util.concurrent.CompletableFuture.completedFuture;
import static java.util.function.Predicate.not;

final class _StoreReader {

    private final _StoreLoader loader;


    _StoreReader(final _StoreLoader loader) {
        this.loader=loader;
    }


    Value retrieve(final Value model, final List<Locale> locales) {
        return loader.execute(() -> retrieve(model)).join();
    }


    //̸/////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private CompletableFuture<Value> retrieve(final Value model) {
        return model.accept(new Visitor<>() {

            @Override public CompletableFuture<Value> visit(final Value host, final List<Value> values) {
                return allItemsOf(values.stream().map(v -> v.accept(this)).toList()).thenApply(Value::array);
            }

            @Override public CompletableFuture<Value> visit(final Value host, final Map<String, Value> fields) {

                final URI id=host.id().orElseThrow(() -> new StoreException(format(
                        "undefined id in model value <%s>", host
                )));

                final Shape shape=host.shape().orElseThrow(() -> new StoreException(format(
                        "undefined shape in model value <%s>", host
                )));

                return retrieve(id, shape, fields);
            }

            @Override public CompletableFuture<Value> visit(final Value host, final Object object) {
                return host.value(Query.class)

                        .map(query -> query.model().value(Specs.class)

                                .map(specs -> {

                                    final Shape shape=specs.shape();

                                    return loader.tuples(query, shape)
                                            .thenCompose(tuples -> retrieve(tuples, shape, specs));

                                })


                                .orElseGet(() -> {

                                    final Shape shape=query.model().shape().orElseGet(Shape::shape);

                                    return loader.values(query, shape)
                                            .thenCompose(values -> retrieve(values, shape, query.model()));

                                }))

                        .orElseThrow(() -> new StoreException(format(
                                "unsupported model value <%s>", host
                        )));
            }

        });
    }


    //̸/////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private CompletableFuture<Value> retrieve(final URI id, final Shape shape, final Map<String, Value> fields) {

        // generate field futures before checking for resource existence, so that a single query is generated

        final Collection<CompletableFuture<Entry<String, Value>>> futures=list(Stream.concat(

                Stream.of(completedFuture(shape(shape))), // associate the shape to the retrieved object

                fields.entrySet().stream().flatMap(entry -> {

                    final String name=entry.getKey();
                    final Value value=entry.getValue();

                    if ( name.equals(ID) ) {

                        return Stream.of(completedFuture(id(id)));

                    } else if ( isReserved(name) ) {

                        return Stream.empty();

                    } else {

                        final Property property=shape.property(name).orElseThrow(() -> unknown(name));

                        return value.accept(new Visitor<Stream<CompletableFuture<Entry<String, Value>>>>() {

                            @Override public Stream<CompletableFuture<Entry<String, Value>>> visit(final Value host, final Void nil) {
                                return Stream.empty();
                            }

                            @Override public Stream<CompletableFuture<Entry<String, Value>>> visit(final Value host, final List<Value> values) {
                                return values.stream().map(value -> retrieve(id, shape, property, value));
                            }

                            @Override public Stream<CompletableFuture<Entry<String, Value>>> visit(final Value host, final Object object) {
                                return Stream.of(object instanceof final Query query
                                        ? retrieve(shape.virtual(), id, property, query)
                                        : retrieve(id, shape, property, host)
                                );
                            }

                        });

                    }

                })

        ));

        return (shape.virtual()

                ? completedFuture(true)
                : loader.fetch(id)

        ).thenCompose(exists -> exists

                ? allItemsOf(futures).thenApply(Value::object)
                : completedFuture(Nil())

        );
    }


    private CompletableFuture<Entry<String, Value>> retrieve(
            final boolean virtual, final URI id, final Property property, final Query query
    ) {

        return query.model().value(Specs.class)

                .map(specs -> loader.tuples(virtual, id, property, query)
                        .thenCompose(tuples -> retrieve(tuples, property.shape(), specs))
                        .thenApply(value -> field(property.name(), value)))

                .orElseGet(() -> loader.values(virtual, id, property, query)
                        .thenCompose(values -> retrieve(values, property.shape(), query.model()))
                        .thenApply(value -> field(property.name(), value))
                );

    }

    private CompletableFuture<Entry<String, Value>> retrieve(
            final URI id, final Shape shape, final Property property, final Value model
    ) {

        final Boolean isCollection=property.shape().maxCount().map(limit -> limit > 1).orElse(true);

        return loader.fetch(id, property).thenCompose(value -> {

            final Value effective=shape.virtual() && value.isEmpty() ? prune(model) : value;

            if ( effective.isEmpty() ) {

                return completedFuture(Nil());

            } else if ( isCollection ) {

                return allItemsOf(values(effective)
                        .map(v -> retrieve(v, property.shape(), model))
                        .toList()
                ).thenApply(Value::array);

            } else {

                return retrieve(value(effective), property.shape(), model);

            }

        }).thenApply(v -> field(property.name(), v));
    }

    private CompletableFuture<Value> retrieve(final Value value, final Shape shape, final Value model) {
        return value.id()
                .flatMap(id -> model.object().map(fields -> retrieve(id, shape, fields)))
                .orElseGet(() -> completedFuture(value));
    }


    private CompletableFuture<Value> retrieve(final List<Value> values, final Shape shape, final Value model) {
        return allItemsOf(values.stream()
                .map(value -> retrieve(value, shape, model))
                .toList()
        ).thenApply(Value::array);
    }

    private CompletableFuture<Value> retrieve(final List<Tuple> tuples, final Shape shape, final Specs specs) {
        return allItemsOf(tuples.stream()

                .map(tuple -> allItemsOf(tuple.fields().stream()

                        .map(field -> {

                            final String name=field.getKey();
                            final Value value=field.getValue();

                            return specs.column(name)
                                    .map(probe -> retrieve(value,
                                            probe.expression().apply(shape),
                                            probe.model()
                                    ))
                                    .orElseGet(() -> completedFuture(value))
                                    .thenApply(v -> field(name, v));

                        })

                        .toList()

                ).thenApply(Tuple::new))

                .toList()

        ).thenApply(ts -> Value.value(new Table(ts)));
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

            @Override public Value visit(final Value host, final Year year) {
                return host.equals(Year()) ? Nil() : host;
            }

            @Override public Value visit(final Value host, final YearMonth yearMonth) {
                return host.equals(YearMonth()) ? Nil() : host;
            }

            @Override public Value visit(final Value host, final LocalDate localDate) {
                return host.equals(LocalDate()) ? Nil() : host;
            }

            @Override public Value visit(final Value host, final LocalTime localTime) {
                return host.equals(LocalTime()) ? Nil() : host;
            }

            @Override public Value visit(final Value host, final OffsetTime offsetTime) {
                return host.equals(OffsetTime()) ? Nil() : host;
            }

            @Override public Value visit(final Value host, final LocalDateTime localDateTime) {
                return host.equals(LocalDateTime()) ? Nil() : host;
            }

            @Override public Value visit(final Value host, final OffsetDateTime offsetDateTime) {
                return host.equals(OffsetDateTime()) ? Nil() : host;
            }

            @Override public Value visit(final Value host, final ZonedDateTime zonedDateTime) {
                return host.equals(ZonedDateTime()) ? Nil() : host;
            }

            @Override public Value visit(final Value host, final Instant instant) {
                return host.equals(Instant()) ? Nil() : host;
            }

            @Override public Value visit(final Value host, final Period period) {
                return host.equals(Period()) ? Nil() : host;
            }

            @Override public Value visit(final Value host, final Duration duration) {
                return host.equals(Duration()) ? Nil() : host;
            }

            @Override public Value visit(final Value host, final Locale locale, final String string) {
                return host.equals(Text()) || locale.equals(ANY) ? Nil() : host;
            }

            @Override public Value visit(final Value host, final URI datatype, final String string) {
                return host.equals(Data()) ? Nil() : host;
            }

            @Override public Value visit(final Value host, final Map<String, Value> fields) {

                final List<Entry<String, Value>> pruned=fields.entrySet().stream()
                        .map(e -> field(e.getKey(), prune(e.getValue())))
                        .filter(not(e -> e.getValue().isEmpty()))
                        .toList();

                return pruned.isEmpty() ? Nil() : object(pruned);
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
