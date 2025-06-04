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
import com.metreeca.mesh.shapes.Property;
import com.metreeca.mesh.shapes.Shape;
import com.metreeca.mesh.tools.StoreException;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

import static com.metreeca.mesh.Value.*;
import static com.metreeca.shim.Collections.entry;
import static com.metreeca.shim.Collections.list;
import static com.metreeca.shim.Futures.allItemsOf;
import static com.metreeca.shim.Futures.allOf;
import static com.metreeca.shim.URIs.uri;
import static com.metreeca.shim.URIs.uuid;

import static java.lang.String.format;
import static java.util.concurrent.CompletableFuture.completedFuture;
import static java.util.function.Predicate.not;

final class _StoreWriter {

    private final _StoreLoader loader;


    _StoreWriter(final _StoreLoader loader) {
        this.loader=loader;
    }


    int create(final Value value) {

        final List<Value> resources=resources(value, false, false);

        if ( exist(resources) ) { return 0; } else {

            loader.execute(() -> create(resources)).join();

            return resources.size();

        }
    }

    int update(final Value value) {

        final List<Value> resources=resources(value, false, false);

        if ( exist(resources) ) {

            loader.execute(() -> update(resources)).join();

            return resources.size();

        } else { return 0; }
    }

    int mutate(final Value value) {

        final List<Value> resources=resources(value, false, true);

        if ( exist(resources) ) {

            loader.execute(() -> mutate(resources)).join();

            return resources.size();

        } else { return 0; }
    }

    int delete(final Value value) {

        final List<Value> resources=resources(value, true, true);

        if ( exist(resources) ) {

            loader.execute(() -> delete(resources)).join();

            return resources.size();

        } else { return 0; }
    }


    int insert(final Value value) {

        final List<Value> resources=resources(value, false, false);

        loader.execute(() -> update(resources)).join();

        return resources.size();
    }

    int remove(final Value value) {

        final List<Value> resources=resources(value, true, true);

        loader.execute(() -> delete(resources)).join();

        return resources.size();
    }

    int modify(final Value insert, final Value remove) {

        final List<Value> insertions=resources(insert, false, false);

        final List<Value> removals=list(resources(remove, true, true).stream()
                .filter(r -> insertions.stream().noneMatch(i -> id(r).equals(id(i))))
        );

        loader.execute(() -> allOf(update(insertions), delete(removals))).join();

        return insertions.size()+removals.size();
    }


    //̸/////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private URI id(final Value resource) {
        return resource.id().orElseThrow(() -> new AssertionError("undefined resource id"));
    }

    private Shape shape(final Value resource) {
        return resource.shape().orElseThrow(() -> new AssertionError("undefined resource shape"));
    }

    private Map<String, Value> fields(final Value resource) {
        return resource.object().orElseThrow(() -> new AssertionError("undefined resource fields"));
    }


    private List<Value> resources(final Value value, final boolean queryable, final boolean delta) {

        value.validate(delta).ifPresent(trace -> {
            throw new StoreException(format("invalid resources value <%s>", trace));
        });

        return list(value.accept(new Visitor<Stream<Value>>() {

            @Override public Stream<Value> visit(final Value host, final List<Value> values) {
                return values.stream().flatMap(v -> v.accept(this));
            }

            @Override public Stream<Value> visit(final Value host, final Map<String, Value> fields) {

                host.id().orElseThrow(() -> new StoreException(format(
                        "undefined id in resource value <%s>", host
                )));

                host.shape().orElseThrow(() -> new StoreException(format(
                        "undefined shape in resource value <%s>", host
                )));

                return Stream.of(host);
            }

            @Override public Stream<Value> visit(final Value host, final Object object) {
                return (queryable ? host.value(Query.class) : Optional.<Query>empty())

                        .map(query -> query.model().value(Specs.class)

                                .<Stream<Value>>map(specs -> {

                                    throw new StoreException(format(
                                            "unsupported deletion query model value <%s>", host
                                    ));

                                })

                                .orElseGet(() -> loader.retrieve(value(query.model(object(
                                        Value.id(uri()),
                                        Value.shape(shape(query.model()))
                                ))), list()).values())

                        )

                        .orElseThrow(() -> new StoreException(format(
                                "unsupported resource value <%s>", host
                        )));
            }

        }));
    }


    private boolean exist(final List<Value> resources) {
        return loader.execute(() -> allItemsOf(list(resources.stream().map(v -> loader.fetch(id(v))))))
                .thenApply(vs -> vs.stream().allMatch(present -> present))
                .join();
    }


    //̸// !!! review ///////////////////////////////////////////////////////////////////////////////////////////////////

    private CompletableFuture<Void> create(final List<Value> resources) {
        return allOf(resources.stream().map(value -> {

            final URI id=id(value);
            final Shape shape=shape(value);
            final Map<String, Value> fields=fields(value);

            return _create(id, shape, fields);

        }));
    }

    private CompletableFuture<Void> update(final List<Value> resources) {
        return allOf(resources.stream().map(value -> {

            final URI id=id(value);
            final Shape shape=shape(value);
            final Map<String, Value> fields=fields(value);

            return _update(id, shape, fields);

        }));
    }

    private CompletableFuture<Void> mutate(final List<Value> resources) {
        return allOf(resources.stream().map(value -> {

            final URI id=id(value);
            final Shape shape=shape(value);
            final Map<String, Value> fields=fields(value);

            return _mutate(id, shape, fields);

        }));
    }

    private CompletableFuture<Void> delete(final List<Value> resources) {
        return allOf(resources.stream().map(value -> {

            final URI id=id(value);
            final Shape shape=shape(value);

            return _delete(id, shape);

        }));
    }


    //̸// !!! review ///////////////////////////////////////////////////////////////////////////////////////////////////

    private CompletableFuture<Void> _create(final URI id, final Shape shape, final Map<String, Value> fields) {
        return _insert(id, shape, fields);
    }

    private CompletableFuture<Void> _update(final URI id, final Shape shape, final Map<String, Value> fields) {
        return allOf(
                _remove(id, shape),
                _insert(id, shape, fields)
        );
    }

    private CompletableFuture<Void> _mutate(final URI id, final Shape shape, final Map<String, Value> fields) {
        return allOf(Stream.concat(

                shape.clazzes()
                        .map(types -> loader.insert(id, types))
                        .stream(),

                fields.entrySet().stream()

                        .filter(not(field -> isReserved(field.getKey())))

                        .map(field -> entry(
                                shape.property(field.getKey()).orElseThrow(() -> unknown(field.getKey())),
                                field.getValue()
                        ))

                        .filter(field -> !field.getKey().foreign())

                        .map(field -> allOf(
                                _remove(id, field.getKey()),
                                _insert(id, field.getKey(), field.getValue())
                        ))

        ));
    }

    private CompletableFuture<Void> _delete(final URI id, final Shape shape) {
        return allOf(Stream.concat(

                Stream.of(loader.remove(id)),

                shape.properties().stream()
                        .filter(Property::embedded)
                        .map(property -> _remove(id, property))

        ));
    }


    private CompletableFuture<Void> _remove(final URI id, final Shape shape) {
        return allOf(shape.properties().stream()
                .filter(property -> !property.foreign())
                .map(property -> _remove(id, property))
        );
    }

    private CompletableFuture<Void> _remove(final URI id, final Property property) {
        if ( property.embedded() ) {

            return allOf(loader.remove(id, property), loader.fetch(id, property).thenCompose(e ->

                    e.accept(new Visitor<CompletableFuture<Void>>() {

                        @Override public CompletableFuture<Void> visit(final Value host, final List<Value> values) {
                            return allOf(values.stream().map(v -> v.accept(this)));
                        }

                        @Override public CompletableFuture<Void> visit(final Value host, final Map<String, Value> fields) {
                            return host.id().map(i -> _delete(i, property.shape())).orElseGet(() -> completedFuture(null));
                        }

                        @Override public CompletableFuture<Void> visit(final Value host, final Object object) {
                            return completedFuture(null);
                        }

                    })

            ));

        } else {

            return loader.remove(id, property);

        }
    }


    private CompletableFuture<Void> _insert(final URI id, final Shape shape, final Map<String, Value> fields) {
        return allOf(Stream.concat(

                shape.clazzes()
                        .map(types -> loader.insert(id, types))
                        .stream(),

                shape.properties().stream()
                        .filter(property -> !property.foreign())
                        .flatMap(property -> Optional.ofNullable(fields.get(property.name()))
                                .map(v -> _insert(id, property, v))
                                .stream()
                        )

        ));
    }

    private CompletableFuture<Void> _insert(final URI id, final Property property, final Value value) {
        if ( property.embedded() ) {

            final List<Entry<URI, Map<String, Value>>> frames=list(value.accept(new Visitor<Stream<Entry<URI, Map<String, Value>>>>() {

                @Override public Stream<Entry<URI, Map<String, Value>>> visit(final Value host, final List<Value> values) {
                    return values.stream().flatMap(v -> v.accept(this));
                }

                @Override public Stream<Entry<URI, Map<String, Value>>> visit(final Value host, final Map<String, Value> fields) {

                    final URI i=host.id().orElseGet(() -> uri("urn:uuid:%s".formatted(uuid()))); // fallback identifier

                    return Stream.of(entry(i, fields));
                }

                @Override public Stream<Entry<URI, Map<String, Value>>> visit(final Value host, final Object object) {
                    return Stream.empty();
                }

            }));

            return allOf(

                    // link embedded frames as a single array (frame-by-frame linking likely to break on non-rdf stores)

                    loader.insert(id, property, array(frames.stream()
                            .map(Entry::getKey)
                            .map(i -> object(Value.id(i)))
                    )),

                    // cascade insertion to embedded frames

                    allOf(frames.stream().map(e ->
                            _insert(e.getKey(), property.shape(), e.getValue())
                    ))

            );

        } else {

            return loader.insert(id, property, value);

        }
    }

}
