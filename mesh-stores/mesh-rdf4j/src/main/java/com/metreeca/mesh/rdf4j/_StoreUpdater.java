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
import com.metreeca.mesh.shapes.Property;

import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

import static com.metreeca.mesh.Field.id;
import static com.metreeca.mesh.Field.isReserved;
import static com.metreeca.mesh.Value.flatten;

import static java.util.concurrent.CompletableFuture.allOf;
import static java.util.function.Function.identity;
import static java.util.function.Predicate.not;

final class _StoreUpdater {

    private final RDF4J.Loader loader;
    private final _SPARQLUpdater updater;


    _StoreUpdater(final RDF4J.Loader loader) {
        this.loader=loader;
        this.updater=loader.worker(_SPARQLUpdater::new);
    }


    boolean update(final Value value, final boolean force) { // !!! refactor

        // !!! inverse properties
        // !!! embedded properties

        if ( force ) {

            loader.execute(() -> allOf(flatten(value)
                    .flatMap(_value -> id(_value).stream()
                            .flatMap(id -> Field.shape(_value).stream()
                                    .flatMap(shape -> _value.object().stream()
                                            .flatMap(fields -> Stream

                                                    .of(

                                                            Stream.of(updater.delete(id)),

                                                            shape.clazzes().stream()
                                                                    .flatMap(Collection::stream)
                                                                    .map(t -> updater.insert(id, t)),

                                                            fields.entrySet().stream()
                                                                    .filter(not(field -> isReserved(field.getKey())))
                                                                    .map(field -> {

                                                                        final Property property=shape.property(field.getKey())
                                                                                .orElseThrow(() -> new IllegalArgumentException(field.getKey()));

                                                                        return updater.insert(id, property, field.getValue());
                                                                    })

                                                    )

                                                    .flatMap(identity()))
                                    )
                            )
                    )
                    .toArray(CompletableFuture[]::new)
            )).join();

            return true;

        } else {

            throw new UnsupportedOperationException(";( be implemented"); // !!!

        }
    }
}
