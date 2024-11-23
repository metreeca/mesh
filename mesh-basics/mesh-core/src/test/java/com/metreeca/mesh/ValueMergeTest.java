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

package com.metreeca.mesh;


import com.metreeca.mesh.shapes.Shape;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static com.metreeca.mesh.Field.*;
import static com.metreeca.mesh.Value.*;
import static com.metreeca.mesh.queries.Criterion.criterion;
import static com.metreeca.mesh.queries.Query.query;

import static org.assertj.core.api.Assertions.assertThat;

final class ValueMergeTest {

    @Nested
    final class MergeTest {

        @Test void testMergeObjects() {
            assertThat(object(
                    field("x", integer(1)),
                    field("y", integer(2))
            ).merge(object(
                    field("y", integer(3)),
                    field("z", integer(4))

            ))).isEqualTo(object(
                    field("x", integer(1)),
                    field("y", integer(3)),
                    field("z", integer(4))
            ));
        }

        @Test void testMergeArrays() {
            assertThat(array(integer(1)).merge(array(integer(2))))
                    .isEqualTo(array(integer(1), integer(2)));
        }

        @Test void testMergeLiterals() {
            assertThat(string("x").merge(integer(1))).isEqualTo(string("x"));
        }

    }

    @Nested
    final class ExtendTest {


        @Test void testMergeLiteralWithLiteral() {
            assertThat(string("x").extend(string("y"))).isEqualTo(string("y"));
        }

        @Test void testMergeLiteralWithObject() {
            assertThat(String().extend(Object())).isEqualTo(Object());
        }

        @Test void testMergeLiteralWithArray() {
            assertThat(String().extend(Array())).isEqualTo(Array());
        }

        @Test void testMergeLiteralWithQuery() {
            final Value y=value(query());
            assertThat(String().extend(y)).isEqualTo(value(query()));
        }


        @Test void testMergeObjectWithString() {
            assertThat(Object().extend(String())).isEqualTo(String());
        }

        @Test void testMergeObjectWithObject() {
            assertThat(object(
                    field("x", Nil()),
                    field("y", object(field("a", Nil())))
            ).extend(object(
                    field("y", object(field("b", Nil()))),
                    field("z", Nil())
            ))).isEqualTo(object(
                    field("x", Nil()),
                    field("y", object(
                            field("a", Nil()),
                            field("b", Nil())
                    )),
                    field("z", Nil())
            ));
        }

        @Test void testMergeObjectWithArray() {
            assertThat(Object().extend(Array())).isEqualTo(Array());
        }

        @Test void testMergeObjectWithQuery() {
            assertThat(object(
                    field("x", Nil())
            ).extend(value(query()
                    .model(object(
                            field("y", Nil())
                    ))
            ))).isEqualTo(value(query() // merge object into query model
                    .model(object(
                            field("x", Nil()),
                            field("y", Nil())
                    ))
            ));
        }

        @Test void testMergeObjectReservedFields() {

            assertThat(object(
                    id("x")
            ).extend(object(
                    id("y")
            ))).isEqualTo(object(
                    id("y")
            ));

            assertThat(object(
                    type("x")
            ).extend(object(
                    type("y")
            ))).isEqualTo(object(
                    type("y")
            ));

            assertThat(object(
                    shape(Shape.shape().minInclusive(integer(1)))
            ).extend(object(
                    shape(Shape.shape().maxInclusive(integer(10)))
            ))).isEqualTo(object( // merged shapes
                    shape(Shape.shape().minInclusive(integer(1)).maxInclusive(integer(10)))
            ));

        }


        @Test void testMergeArrayWithString() {
            assertThat(Array().extend(string("y"))).isEqualTo(string("y"));
        }

        @Test void testMergeArrayWithObject() {
            assertThat(Array().extend(Object())).isEqualTo(Object());
        }

        @Test void testMergeArrayWithArray() {

            assertThat(array(
                    object(
                            field("x", Nil())
                    ),
                    object(
                            field("y", Nil())
                    )
            ).extend(array(
                    object(
                            field("z", Nil())
                    )
            ))).isEqualTo(array( // pairwise merging
                    object(
                            field("x", Nil()),
                            field("z", Nil())
                    ),
                    object(
                            field("y", Nil())
                    )
            ));

            assertThat(array(
                    object(
                            field("z", Nil())
                    )
            ).extend(array(
                    object(
                            field("x", Nil())
                    ),
                    object(
                            field("y", Nil())
                    )
            ))).isEqualTo(array( // pairwise merging
                    object(
                            field("x", Nil()),
                            field("z", Nil())
                    ),
                    object(
                            field("y", Nil())
                    )
            ));
        }

        @Test void testMergeArrayWithQuery() {
            assertThat(Array().extend(value(query()))).isEqualTo(value(query()));
        }


        @Test void testMergeQueryWithString() {
            assertThat(value(query()).extend(string("y"))).isEqualTo(string("y"));
        }

        @Test void testMergeQueryWithObject() {
            assertThat(value(query()
                    .model(object(
                            field("y", Nil())
                    ))
            ).extend(object(
                    field("x", Nil())
            ))).isEqualTo(value(query() // merge object into query model
                    .model(object(
                            field("x", Nil()),
                            field("y", Nil())
                    ))
            ));
        }


        @Test void testMergeQueryWithArray() {
            assertThat(value(query()).extend(Array())).isEqualTo(Array());
        }

        @Test void testMergeQueryWithQuery() {
            assertThat(value(query()
                    .model(object(
                            field("x", Nil())
                    ))
                    .criterion("", criterion().gt(integer(0)))
            ).extend(value(query()
                    .model(object(
                            field("y", Nil())
                    ))
                    .criterion("", criterion().lt(integer(10)))
            ))).isEqualTo(value(query() // merge models
                    .model(object(
                            field("x", Nil()),
                            field("y", Nil())
                    ))
                    .criterion("", criterion() // merge criteria
                            .gt(integer(0))
                            .lt(integer(10))
                    )
            ));
        }

    }

}
