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
import com.metreeca.mesh.queries.Query;
import com.metreeca.mesh.queries.Tuple;
import com.metreeca.mesh.shapes.Property;
import com.metreeca.mesh.shapes.Shape;
import com.metreeca.mesh.shapes.Type;

import org.eclipse.rdf4j.repository.RepositoryConnection;

import java.net.URI;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static com.metreeca.mesh.shapes.Property.property;
import static com.metreeca.shim.URIs.base;

import static java.util.concurrent.CompletableFuture.allOf;
import static java.util.function.Predicate.not;

final class _StoreLoader {

    private final RepositoryConnection connection;

    private final SPARQLSelector selector;
    private final SPARQLFetcher fetcher;
    private final SPARQLUpdater updater;


    _StoreLoader(final RDF4JStore rdf4j, final RepositoryConnection connection) {

        this.connection=connection;

        this.selector=new SPARQLSelector(rdf4j);
        this.fetcher=new SPARQLFetcher(rdf4j);
        this.updater=new SPARQLUpdater(rdf4j);
    }


    <T extends CompletableFuture<?>> T execute(final Supplier<T> task) {

        final T value=task.get();

        CompletableFuture<?>[] reading;
        CompletableFuture<?>[] writing;

        do {

            // read current state before modifying it to support handling of embedded values

            reading=Stream.of(selector, fetcher)
                    .map(v -> v.run(connection))
                    .filter(not(CompletableFuture::isDone))
                    .toArray(CompletableFuture[]::new);

            allOf(reading).join();

            writing=Stream.of(updater)
                    .map(v -> v.run(connection))
                    .filter(not(CompletableFuture::isDone))
                    .toArray(CompletableFuture[]::new);

            allOf(writing).join();

        } while ( reading.length+writing.length > 0 );

        return value;

    }


    //̸/////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    Value retrieve(final Value value, final List<Locale> locales) { // !!! review usage
        return new _StoreReader(this).retrieve(value, locales);
    }


    //̸/////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    CompletableFuture<List<Value>> values(final Query query, final Shape shape) {
        return selector.values(true, base(), property("virtual").shape(shape), query);
    }

    CompletableFuture<List<Value>> values(final boolean virtual, final URI id, final Property property, final Query query) {
        return selector.values(virtual, id, property, query);
    }


    CompletableFuture<List<Tuple>> tuples(final Query query, final Shape shape) {
        return selector.tuples(true, base(), property("virtual").shape(shape), query);
    }

    CompletableFuture<List<Tuple>> tuples(final boolean virtual, final URI id, final Property property, final Query query) {
        return selector.tuples(virtual, id, property, query);
    }


    CompletableFuture<Boolean> fetch(final URI id) {
        return fetcher.fetch(id);
    }

    CompletableFuture<Value> fetch(final URI id, final Property property) {
        return fetcher.fetch(id, property);
    }


    CompletableFuture<Void> insert(final URI id, final Set<Type> types) {
        return updater.insert(id, types);
    }

    CompletableFuture<Void> insert(final URI id, final Property property, final Value value) {
        return updater.insert(id, property, value);
    }


    CompletableFuture<Void> remove(final URI id) {
        return updater.remove(id);
    }

    CompletableFuture<Void> remove(final URI id, final Property property) {
        return updater.remove(id, property);
    }


    //̸/////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    abstract static class Worker {

        private final Map<Object, String> scope=new ConcurrentHashMap<>();


        String id(final Object object) {
            return scope.computeIfAbsent(object, o -> String.valueOf(scope.size()));
        }

        <T> Collection<T> snapshot(final Collection<T> collection) {
            synchronized ( collection ) {
                try {

                    return new ArrayList<>(collection);

                } finally {

                    collection.clear();

                }
            }
        }


        abstract CompletableFuture<Void> run(final RepositoryConnection connection);

    }

}
