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

package com.metreeca.mesh.queries;

import com.metreeca.mesh.shapes.Shape;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static com.metreeca.mesh.Value.Decimal;
import static com.metreeca.mesh.Value.Integer;
import static com.metreeca.mesh.queries.Expression.expression;
import static com.metreeca.mesh.queries.Transform.*;
import static com.metreeca.mesh.shapes.Property.property;
import static com.metreeca.mesh.shapes.Shape.shape;
import static com.metreeca.shim.Collections.list;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

final class ExpressionTest {

    @Test void testConfigurePipe() {
        assertThat(expression().pipe()).isEmpty();
        assertThat(expression().pipe(COUNT).pipe()).isEqualTo(list(COUNT));
    }

    @Test void testConfigurePath() {
        assertThat(expression().path()).isEmpty();
        assertThat(expression().path("x").path()).isEqualTo(list("x"));
    }

    @Test void testReportReservedKeywords() {
        assertThatIllegalArgumentException().isThrownBy(() -> expression().path("@id"));
    }


    @Nested
    final class ParserTest {

        @Test void testDecodeEmptyPaths() {
            assertThat(expression(""))
                    .isEqualTo(expression());
        }

        @Test void testDecodeSingletonPaths() {
            assertThat(expression("x"))
                    .isEqualTo(expression().path("x"));
        }

        @Test void testDecodeProperPaths() {
            assertThat(expression("x.y.z"))
                    .isEqualTo(expression().path("x", "y", "z"));
        }

        @Test void testDecodeQuotedPaths() {
            assertThat(expression(".escaped\\.\\:\\\\.\\.\\:\\\\path"))
                    .isEqualTo(expression().path("escaped.:\\", ".:\\path"));
        }


        @Test void testDecodeTransforms() {
            assertThat(expression("count:x"))
                    .isEqualTo(expression().pipe(COUNT).path("x"));
        }

        @Test void testDecodeTransformPipes() {
            assertThat(expression("sum:abs:x"))
                    .isEqualTo(expression().pipe(SUM, ABS).path("x"));
        }

        @Test void testDecodeTransformPipesOnEmptyPaths() {
            assertThat(expression("sum:abs:"))
                    .isEqualTo(expression().pipe(SUM, ABS));
        }


        @Test void testReportUnknownTransforms() {
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> expression("none:x"));
        }

        @Test void testReportMalformedExpressions() {
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> expression("not an expression \\"));
        }

    }

    @Nested
    final class TraverseTest {

        @Test void testTraverseEmptyPath() {

            final Expression expression=expression();
            final Shape shape=shape().datatype(Integer());

            Assertions.assertThat(expression.apply(shape).datatype())
                    .contains(Integer());
        }

        @Test void testTraverseSingletonPath() {

            final Expression expression=expression().path("x");
            final Shape shape=shape().property(property("x").forward(true).shape(shape().datatype(Integer())));

            Assertions.assertThat(expression.apply(shape).datatype())
                    .contains(Integer());
        }

        @Test void testTraversePath() {

            final Expression expression=expression().path("x", "y");

            final Shape shape=shape().property(property("x").forward(true)
                    .shape(shape().property(property("y").forward(true).shape(shape().datatype(Integer()))))
            );

            Assertions.assertThat(expression.apply(shape).datatype())
                    .contains(Integer());
        }


        @Test void testApplySingletonTransformPipe() {

            final Expression expression=expression().pipe(MIN).path("value");
            final Shape shape=shape().property(property("value").forward(true).shape(shape().datatype(Integer())));

            Assertions.assertThat(expression.apply(shape).datatype())
                    .contains(Integer());

        }

        @Test void testApplyTransformPipe() {

            final Expression expression=expression().pipe(AVG, ROUND).path("value");
            final Shape shape=shape().property(property("value").forward(true).shape(shape().datatype(Integer())));

            Assertions.assertThat(expression.apply(shape).datatype())
                    .contains(Decimal());

        }

    }

}