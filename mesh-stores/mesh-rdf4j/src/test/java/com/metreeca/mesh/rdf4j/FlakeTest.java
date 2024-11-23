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
import com.metreeca.mesh.shapes.Shape;

import org.junit.jupiter.api.Test;

import java.util.Optional;

import static com.metreeca.mesh.queries.Criterion.criterion;
import static com.metreeca.mesh.queries.Expression.expression;
import static com.metreeca.mesh.queries.Transform.MAX;
import static com.metreeca.mesh.queries.Transform.MIN;
import static com.metreeca.mesh.rdf4j.Flake.flake;
import static com.metreeca.mesh.shapes.Property.property;
import static com.metreeca.mesh.shapes.Shape.shape;
import static com.metreeca.shim.Collections.*;

import static org.assertj.core.api.Assertions.assertThat;

final class FlakeTest {

    private static final Shape shape=shape().property(property("x").forward(true).shape(shape().property(property("y").forward(true))));

    private static final Property x=shape.property("x").orElseThrow();
    private static final Property y=x.shape().property("y").orElseThrow();


    @Test void testMergePaths() {
        assertThat(flake(shape, map(

                entry(expression().path("x"), criterion().like("x")),
                entry(expression().path("x", "y"), criterion().like("y"))

        ))).satisfies(flake -> {

            assertThat(flake.flakes().keySet()).containsExactlyInAnyOrder(x);
            assertThat(flake.flakes().get(x).flakes().keySet()).containsExactlyInAnyOrder(y);

        });
    }

    @Test void testMergeTransforms() {
        assertThat(flake(shape, map(

                entry(expression().path("x"), criterion().like(":")),
                entry(expression().pipe(MIN).path("x"), criterion().like("min:")),
                entry(expression().pipe(MAX).path("x"), criterion().like("max:"))

        ))).satisfies(flake -> {

            assertThat(flake.flakes().get(x)
                    .criteria().get(list())
                    .like()
            ).isEqualTo(Optional.of(":"));

            assertThat(flake.flakes().get(x)
                    .criteria().get(list(MIN))
                    .like()
            ).isEqualTo(Optional.of("min:"));

            assertThat(flake.flakes().get(x)
                    .criteria().get(list(MAX))
                    .like()
            ).isEqualTo(Optional.of("max:"));

        });
    }

    @Test void testMergeRootTransforms() {
        assertThat(flake(shape(), map(

                entry(expression(), criterion().like(":")),
                entry(expression().pipe(MIN), criterion().like("min:")),
                entry(expression().pipe(MAX), criterion().like("max:"))

        ))).satisfies(flake -> {

            assertThat(flake
                    .criteria().get(list())
                    .like()
            ).isEqualTo(Optional.of(":"));

            assertThat(flake
                    .criteria().get(list(MIN))
                    .like()
            ).isEqualTo(Optional.of("min:"));

            assertThat(flake
                    .criteria().get(list(MAX))
                    .like()
            ).isEqualTo(Optional.of("max:"));

        });
    }

    @Test void testRetainConstraints() {
        assertThat(flake(shape, map(

                entry(expression().path("x"), criterion().like("x")),
                entry(expression().path("x", "y"), criterion().like("y"))

        ))).satisfies(flake -> {

            assertThat(flake.flakes().get(x)
                    .criteria().get(list())
                    .like()
            ).isEqualTo(Optional.of("x"));

            assertThat(flake.flakes().get(x)
                    .flakes().get(y)
                    .criteria().get(list())
                    .like()
            ).isEqualTo(Optional.of("y"));

        });
    }

}