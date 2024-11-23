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

import com.metreeca.mesh.Value;
import com.metreeca.mesh.shapes.Shape;
import com.metreeca.mesh.tools.CodecException;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static com.metreeca.mesh.Value.Integer;
import static com.metreeca.mesh.Value.Nil;
import static com.metreeca.mesh.Value.integer;
import static com.metreeca.mesh.Value.object;
import static com.metreeca.mesh.Value.shape;
import static com.metreeca.mesh.Value.string;
import static com.metreeca.mesh.queries.Criterion.criterion;
import static com.metreeca.mesh.queries.Expression.expression;
import static com.metreeca.mesh.queries.Query.query;
import static com.metreeca.mesh.shapes.Property.property;
import static com.metreeca.mesh.shapes.Shape.shape;
import static com.metreeca.shim.Collections.set;

import static org.assertj.core.api.Assertions.*;

@Nested
final class QueryTest {

    @Nested
    final class ParserTest {

        private static final Shape shape=shape().property(property("x").forward(true));


        private static Query parse(final String query) {
            return parse(query, shape);
        }

        private static Query parse(final String query, final Shape shape) {
            return query(query, shape);
        }


        @Test void testDecodeEmptyQueries() {
            assertThat(parse("").model()).isEqualTo(object(shape(shape)));
            assertThat(parse("").criteria()).isEmpty();
            assertThat(parse("").offset()).isEqualTo(0);
            assertThat(parse("").limit()).isEqualTo(0);
        }

        @Test void testDecodeEmptyPaths() {
            assertThat(parse("=value", shape()).criteria().get(expression()).any())
                    .hasValue(set(string("value")));
        }

        @Test void testDecodeSingletonPaths() {
            assertThat(parse("x=value").criteria().get(expression().path("x")).any())
                    .hasValue(set(string("value")));
        }

        @Test void testDecodePaths() {
            assertThat(parse("x.y.z=value",
                    shape().property(property("x").forward(true)
                            .shape(shape().property(property("y").forward(true)
                                            .shape(shape().property(property("z").forward(true)))
                                    )
                            )
                    )
            ).criteria().get(expression().path("x", "y", "z")).any())
                    .contains(set(string("value")));
        }

        @Test void testDecodeLteConstraints() {
            assertThat(parse("x<=value").criteria().get(expression().path("x")).lte())
                    .contains(string("value"));
        }

        @Test void testDecodeGteConstraints() {
            assertThat(parse("x>=value").criteria().get(expression().path("x")).gte())
                    .contains(string("value"));
        }

        @Test void testDecodeLikeConstraints() {
            assertThat(parse("~x=value").criteria().get(expression().path("x")).like())
                    .contains("value");
        }

        @Test void testDecodeSingletonAnyConstraints() {
            assertThat(parse("x=value").criteria().get(expression().path("x")).any())
                    .contains(set(string("value")));
        }

        @Test void testDecodeMultipleAnyConstraints() {
            assertThat(parse("x=1&x=2").criteria().get(expression().path("x")).any())
                    .contains(set(new Value[]{ string("1"), string("2") }));
        }

        @Test void testDecodeNonExistentialAnyConstraints() {

            assertThat(parse("x").criteria().get(expression().path("x")).any())
                    .contains(set(Nil()));

            assertThat(parse("x=").criteria().get(expression().path("x")).any())
                    .contains(set(Nil()));
        }

        @Test void testDecodeExistentialAnyConstraints() {
            assertThat(parse("x=*").criteria().get(expression().path("x")).any())
                    .hasValue(set());
        }


        @Test void testDecodeAscendingOrder() {
            assertThat(parse("^x=1").criteria().get(expression().path("x")).order())
                    .hasValue(1);
        }

        @Test void testDecodeDescendingOrder() {
            assertThat(parse("^x=-123").criteria().get(expression().path("x")).order())
                    .hasValue(-123);
        }

        @Test void testDecodeAlternateIncreasingOrder() {
            assertThat(parse("^x=increasing").criteria().get(expression().path("x")).order())
                    .hasValue(1);
        }

        @Test void testDecodeAlternateDecreasingOrder() {
            assertThat(parse("^x=decreasing").criteria().get(expression().path("x")).order())
                    .hasValue(-1);
        }

        @Test void testReportMalformedOrder() {
            assertThatExceptionOfType(CodecException.class).isThrownBy(() -> parse("^x=1.23"));
            assertThatExceptionOfType(CodecException.class).isThrownBy(() -> parse("^x=value"));
        }


        @Test void testDecodeMergeMultipleConstraints() {
            assertThat(parse("x>=lower&x<=upper").criteria()).satisfies(criteria -> {
                assertThat(criteria.get(expression().path("x")).gte()).contains(string("lower"));
                assertThat(criteria.get(expression().path("x")).lte()).contains(string("upper"));
            });
        }


        @Test void testDecodeOffset() {
            assertThat(parse("@=123").offset())
                    .isEqualTo(123);

        }

        @Test void testReportMalformedOffset() {
            assertThatExceptionOfType(CodecException.class).isThrownBy(() -> parse("@="));
            assertThatExceptionOfType(CodecException.class).isThrownBy(() -> parse("@=value"));
        }


        @Test void testDecodeLimit() {
            assertThat(parse("#=123").limit())
                    .isEqualTo(123);

        }

        @Test void testReportMalformedLimit() {
            assertThatExceptionOfType(CodecException.class).isThrownBy(() -> parse("#=", shape()));
            assertThatExceptionOfType(CodecException.class).isThrownBy(() -> parse("#=value", shape()));
        }


        @Test void testAssignKnownDatatype() {
            assertThat(parse("x<=1",
                    shape().property(property("x").forward(true).shape(shape().datatype(Integer())))
            ).criteria().get(expression().path("x")).lte())
                    .contains(integer(1));
        }

    }


    @Nested
    final class ConstructorTest {

        @Test void testReportUnknownPropertiesInExpressions() {
            assertThatIllegalArgumentException().isThrownBy(() -> query()
                    .model(object(shape(shape().property(property("x")))))
                    .where("x.y", criterion())
            );

        }

    }

}