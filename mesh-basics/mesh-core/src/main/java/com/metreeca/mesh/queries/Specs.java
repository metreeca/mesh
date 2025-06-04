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

package com.metreeca.mesh.queries;

import com.metreeca.mesh.shapes.Shape;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static com.metreeca.shim.Collections.list;

import static java.lang.String.format;

/**
 * Tabular specs.
 */
public final record Specs(

        Shape shape,
        List<Probe> columns

) {

    public Specs(

            final Shape shape,
            final List<Probe> columns

    ) {

        if ( shape == null ) {
            throw new NullPointerException("null shape");
        }

        if ( columns == null || columns.stream().anyMatch(Objects::isNull) ) {
            throw new NullPointerException("null columns");
        }

        columns.forEach(x -> columns.forEach(y -> {

            if ( x != y && x.name().equals(y.name()) ) {
                throw new IllegalArgumentException(format("duplicate probe name <%s>", x.name()));
            }

        }));

        this.shape=shape;
        this.columns=list(columns);
    }


    public Optional<Probe> column(final String name) {

        if ( name == null ) {
            throw new NullPointerException("null name");
        }

        return columns().stream()
                .filter(probe -> probe.name().equals(name))
                .findFirst();
    }

}
