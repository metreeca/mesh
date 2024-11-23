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
import com.metreeca.mesh.tools.Store;

import org.eclipse.rdf4j.common.exception.ValidationException;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.RepositoryException;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Supplier;

import static java.util.concurrent.CompletableFuture.allOf;
import static java.util.function.Predicate.not;

/**
 * RDF4J graph store.
 */
public final record RDF4J(

        Repository repository

) implements Store {

    private static final ThreadLocal<RepositoryConnection> shared=new ThreadLocal<>();


    public static RDF4J rdf4j(final Repository repository) {
        return new RDF4J(repository);
    }


    //̸/////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public RDF4J {

        if ( repository == null ) {
            throw new NullPointerException("null repository");
        }

    }


    public RDF4J context(final IRI context) {
        throw new UnsupportedOperationException(";( be implemented"); // !!!
    }


    @Override
    public Value retrieve(final Value model, final List<Locale> locales) {

        if ( model == null ) {
            throw new NullPointerException("null model");
        }

        if ( locales == null || locales.stream().anyMatch(Objects::isNull) ) {
            throw new NullPointerException("null langs");
        }

        return txn(connection -> new StoreRetriever(new Loader(connection)).retrieve(model));
    }


    @Override
    public boolean create(final Value value, final boolean force) {

        if ( value == null ) {
            throw new NullPointerException("null frame");
        }

        return txn(connection -> new _StoreCreator(new Loader(connection)).create(value, force));
    }

    @Override
    public boolean update(final Value value, final boolean force) {

        if ( value == null ) {
            throw new NullPointerException("null frame");
        }

        return txn(connection -> new _StoreUpdater(new Loader(connection)).update(value, force));
    }

    @Override
    public boolean delete(final Value value, final boolean force) {

        if ( value == null ) {
            throw new NullPointerException("null frame");
        }

        return txn(connection -> new _StoreDeleter(new Loader(connection)).delete(value, force));
    }


    @Override
    public <V> V execute(final Function<Store, V> task) {

        if ( task == null ) {
            throw new NullPointerException("null task");
        }

        return txn(connection -> task.apply(this));
    }


    //̸/////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public <V> V txn(final Function<RepositoryConnection, V> task) {

        if ( task == null ) {
            throw new NullPointerException("null task");
        }

        return connect(connection -> {
            if ( connection.isActive() ) { return task.apply(connection); } else {

                try {

                    connection.begin();

                    final V value=task.apply(connection);

                    if ( connection.isActive() ) { connection.commit(); }

                    return value;

                } catch ( final RepositoryException e ) {

                    if ( e.getCause() instanceof final ValidationException cause ) {

                        // !!! cause.validationReportAsModel() // !!! decode report

                        throw (RuntimeException)cause; // ;(rdf4j) ValidationException doesn't extend Exception…

                    } else {

                        throw e;

                    }

                } finally {

                    if ( connection.isActive() ) { connection.rollback(); }

                }

            }
        });
    }

    public <V> V connect(final Function<RepositoryConnection, V> task) {

        if ( task == null ) {
            throw new NullPointerException("null task");
        }

        final RepositoryConnection active=shared.get();

        if ( active != null && active.getRepository().equals(repository) ) { return task.apply(active); } else {

            if ( !repository.isInitialized() ) { repository.init(); }

            try ( final RepositoryConnection connection=repository.getConnection() ) { // !!! pooling

                shared.set(connection);

                return task.apply(connection);

            } finally {

                shared.set(active);

            }

        }
    }


    //̸/////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    static final class Loader {

        private final RepositoryConnection connection;

        private final Map<Supplier<? extends Worker>, Worker> workers=new ConcurrentHashMap<>(); // workers by generator


        private Loader(final RepositoryConnection connection) {
            this.connection=connection;
        }


        @SuppressWarnings("unchecked")
        <T extends Worker> T worker(final Supplier<T> factory) {
            return (T)workers.computeIfAbsent(factory, Supplier::get);
        }


        <T extends CompletableFuture<?>> T execute(final Supplier<T> task) {

            final T value=task.get();

            CompletableFuture<?>[] batch;

            do {

                batch=workers.values().stream()
                        .map(v -> v.run(connection))
                        .filter(not(CompletableFuture::isDone))
                        .toArray(CompletableFuture[]::new);

                allOf(batch).join();

            } while ( batch.length > 0 );

            return value;

        }

    }

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
