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

import org.junit.jupiter.api.Test;

import static com.metreeca.mesh.Value.*;
import static com.metreeca.shim.URIs.base;

import static org.assertj.core.api.Assertions.assertThat;

final class ValueTestMerge {

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

        @Test void testMergeReservedFields() {
            assertThat(object().merge(object(
                    id(base()),
                    type("Type"),
                    shape(Shape.shape().virtual(true))
            ))).isEqualTo(object(
                    id(base()),
                    type("Type"),
                    shape(Shape.shape().virtual(true))
            ));
        }

}
