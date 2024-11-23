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


import com.metreeca.mesh.Valuable;
import com.metreeca.mesh.Value;
import com.metreeca.mesh.tools.Store;

import org.eclipse.rdf4j.common.exception.ValidationException;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.RepositoryException;

import java.net.URI;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.logging.Logger;

import static com.metreeca.shim.Loggers.time;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;

/**
 * RDF4J graph store.
 */
public final class RDF4JStore implements Store {

    private static final ThreadLocal<RepositoryConnection> shared=new ThreadLocal<>();


    public static RDF4JStore rdf4j(final Repository repository) {
        return new RDF4JStore(
                repository,
                null
        );
    }


    //̸/////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private final Repository repository;
    private final URI context;

    @SuppressWarnings("NonConstantLogger")
    private final Logger logger=Logger.getLogger(getClass().getName()); // dynamic logging from concrete subclasses


    private RDF4JStore(
            final Repository repository,
            final URI context
    ) {

        if ( repository == null ) {
            throw new NullPointerException("null repository");
        }

        if ( context != null && !context.isAbsolute() ) {
            throw new IllegalArgumentException(format("relative partition URI <%s>", context));
        }

        this.repository=repository;
        this.context=context;
    }


    public Repository repository() {
        return repository;
    }


    public URI context() {
        return context;
    }

    public RDF4JStore context(final URI context) {
        return new RDF4JStore(
                repository,
                context
        );
    }


    @Override
    public Optional<Value> retrieve(final Valuable model, final List<Locale> locales) {

        if ( model == null ) {
            throw new NullPointerException("null model");
        }

        if ( locales == null || locales.stream().anyMatch(Objects::isNull) ) {
            throw new NullPointerException("null langs");
        }

        return txn(connection -> new _StoreReader(new _StoreLoader(this, connection)).retrieve(
                        requireNonNull(model.toValue(), "null supplied model"), locales
                ).value()
        );
    }


    @Override
    public int create(final Valuable value) {

        if ( value == null ) {
            throw new NullPointerException("null frame");
        }

        return time(() -> txn(connection -> new _StoreWriter(new _StoreLoader(this, connection)).create(
                requireNonNull(value.toValue(), "null supplied value")
        ))).apply((elapsed, resources) -> logger.info(() -> format(
                "created <%,d> resources in <%,d> ms", resources, elapsed
        )));
    }

    @Override
    public int update(final Valuable value) {

        if ( value == null ) {
            throw new NullPointerException("null frame");
        }

        return time(() -> txn(connection -> new _StoreWriter(new _StoreLoader(this, connection)).update(
                requireNonNull(value.toValue(), "null supplied value")
        ))).apply((elapsed, resources) -> logger.info(() -> format(
                "updated <%,d> resources in <%,d> ms", resources, elapsed
        )));
    }

    @Override
    public int mutate(final Valuable value) {

        if ( value == null ) {
            throw new NullPointerException("null frame");
        }

        return time(() -> txn(connection -> new _StoreWriter(new _StoreLoader(this, connection)).mutate(
                requireNonNull(value.toValue(), "null supplied value")
        ))).apply((elapsed, resources) -> logger.info(() -> format(
                "mutated <%,d> resources in <%,d> ms", resources, elapsed
        )));
    }

    @Override
    public int delete(final Valuable value) {

        if ( value == null ) {
            throw new NullPointerException("null frame");
        }

        return time(() -> txn(connection -> new _StoreWriter(new _StoreLoader(this, connection)).delete(
                requireNonNull(value.toValue(), "null supplied value")
        ))).apply((elapsed, resources) -> logger.info(() -> format(
                "deleted <%,d> resources in <%,d> ms", resources, elapsed
        )));
    }


    @Override
    public int insert(final Valuable value) {

        if ( value == null ) {
            throw new NullPointerException("null value");
        }

        return time(() -> txn(connection -> new _StoreWriter(new _StoreLoader(this, connection)).insert(
                requireNonNull(value.toValue(), "null supplied insert value")
        ))).apply((elapsed, resources) -> logger.info(() -> format(
                "inserted <%,d> resources in <%,d> ms", resources, elapsed
        )));
    }

    @Override
    public int remove(final Valuable value) {

        if ( value == null ) {
            throw new NullPointerException("null value");
        }

        return time(() -> txn(connection -> new _StoreWriter(new _StoreLoader(this, connection)).remove(
                requireNonNull(value.toValue(), "null supplied remove value")
        ))).apply((elapsed, resources) -> logger.info(() -> format(
                "removed <%,d> resources in <%,d> ms", resources, elapsed
        )));
    }

    @Override
    public int modify(final Valuable insert, final Valuable remove) {

        if ( insert == null ) {
            throw new NullPointerException("null insert");
        }

        if ( remove == null ) {
            throw new NullPointerException("null remove");
        }

        return time(() -> txn(connection -> new _StoreWriter(new _StoreLoader(this, connection)).modify(
                requireNonNull(insert.toValue(), "null supplied insert value"),
                requireNonNull(remove.toValue(), "null supplied remove value")
        ))).apply((elapsed, resources) -> logger.info(() -> format(
                "modified <%,d> resources in <%,d> ms", resources, elapsed
        )));
    }


    @Override public <V> V execute(final Function<Store, V> task) {

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


}
