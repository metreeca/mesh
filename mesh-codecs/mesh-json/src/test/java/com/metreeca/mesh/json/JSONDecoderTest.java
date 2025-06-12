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

package com.metreeca.mesh.json;


import com.metreeca.mesh.Value;
import com.metreeca.mesh.pipe.CodecException;
import com.metreeca.mesh.queries.Probe;
import com.metreeca.mesh.queries.Query;
import com.metreeca.mesh.queries.Specs;
import com.metreeca.mesh.shapes.Property;
import com.metreeca.mesh.shapes.Shape;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.StringReader;
import java.io.UncheckedIOException;
import java.net.URI;
import java.time.*;
import java.util.Optional;

import static com.metreeca.mesh.Value.Array;
import static com.metreeca.mesh.Value.Duration;
import static com.metreeca.mesh.Value.ID;
import static com.metreeca.mesh.Value.Instant;
import static com.metreeca.mesh.Value.Integral;
import static com.metreeca.mesh.Value.LocalDate;
import static com.metreeca.mesh.Value.LocalDateTime;
import static com.metreeca.mesh.Value.LocalTime;
import static com.metreeca.mesh.Value.Nil;
import static com.metreeca.mesh.Value.Object;
import static com.metreeca.mesh.Value.OffsetDateTime;
import static com.metreeca.mesh.Value.OffsetTime;
import static com.metreeca.mesh.Value.Period;
import static com.metreeca.mesh.Value.String;
import static com.metreeca.mesh.Value.Text;
import static com.metreeca.mesh.Value.URI;
import static com.metreeca.mesh.Value.Year;
import static com.metreeca.mesh.Value.YearMonth;
import static com.metreeca.mesh.Value.ZonedDateTime;
import static com.metreeca.mesh.Value.array;
import static com.metreeca.mesh.Value.bit;
import static com.metreeca.mesh.Value.data;
import static com.metreeca.mesh.Value.decimal;
import static com.metreeca.mesh.Value.duration;
import static com.metreeca.mesh.Value.field;
import static com.metreeca.mesh.Value.floating;
import static com.metreeca.mesh.Value.id;
import static com.metreeca.mesh.Value.instant;
import static com.metreeca.mesh.Value.integer;
import static com.metreeca.mesh.Value.integral;
import static com.metreeca.mesh.Value.localDate;
import static com.metreeca.mesh.Value.localDateTime;
import static com.metreeca.mesh.Value.localTime;
import static com.metreeca.mesh.Value.object;
import static com.metreeca.mesh.Value.offsetDateTime;
import static com.metreeca.mesh.Value.offsetTime;
import static com.metreeca.mesh.Value.period;
import static com.metreeca.mesh.Value.shape;
import static com.metreeca.mesh.Value.string;
import static com.metreeca.mesh.Value.text;
import static com.metreeca.mesh.Value.value;
import static com.metreeca.mesh.Value.year;
import static com.metreeca.mesh.Value.yearMonth;
import static com.metreeca.mesh.Value.zonedDateTime;
import static com.metreeca.mesh.json.JSONCodec.json;
import static com.metreeca.mesh.queries.Criterion.criterion;
import static com.metreeca.mesh.queries.Expression.expression;
import static com.metreeca.mesh.shapes.Property.property;
import static com.metreeca.mesh.shapes.Shape.shape;
import static com.metreeca.shim.Collections.list;
import static com.metreeca.shim.Collections.set;
import static com.metreeca.shim.Locales.ANY;
import static com.metreeca.shim.Locales.locale;
import static com.metreeca.shim.URIs.uri;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.InstanceOfAssertFactories.type;

final class JSONDecoderTest {

    private static final URI base=URI.create("https://example.org/base/");

    private static final URI t=URI.create("test:t");

    private static final Value _1=integer(1);
    private static final Value _2=integer(2);
    private static final Value _3=integer(3);


    private static Value decode(final String json) { return decode(json, null); }

    private static Value decode(final String json, final boolean prune) { return decode(json, null, prune); }

    private static Value decode(final String json, final Shape shape) { return decode(json, shape, false); }

    private static Value decode(final String json, final Shape shape, final boolean prune) {
        try ( final StringReader reader=new StringReader(json.replace('\'', '"')) ) {

            return new JSONDecoder(json().base(base).prune(prune), reader).decode(shape);

        } catch ( final IOException e ) {

            throw new UncheckedIOException(e);

        }
    }


    @Nested
    final class SyntaxText {

        @Test void testReportLocation() {
            assertThatExceptionOfType(CodecException.class)
                    .isThrownBy(() -> decode("{ "))
                    .withMessageStartingWith("(1,3)");
        }

        @Test void testReportUnexpectedEOF() {
            assertThatExceptionOfType(CodecException.class)
                    .isThrownBy(() -> decode("{"))
                    .withMessageStartingWith("(1,2)");

        }

        @Test void testReportTrailingGarbage() {
            assertThatExceptionOfType(CodecException.class)
                    .isThrownBy(() -> decode("{} {}"))
                    .withMessageStartingWith("(1,4)");
        }

        @Test void testReportUnexpectedValue() {
            assertThatExceptionOfType(CodecException.class)
                    .isThrownBy(() -> decode("{ '@': true }"))
                    .withMessageStartingWith("(1,");
        }

    }

    @Nested
    final class ObjectTest {

        @Test void testDecodeEmptyObjects() {
            assertThat(decode("{}")).isEqualTo(Object());
        }

        @Test void testDecodeSingletonObjects() {
            assertThat(decode("{'x':1}")).isEqualTo(object(
                    field("x", integer(1))
            ));
        }

        @Test void testDecodeFullObjects() {
            assertThat(decode("{'x':1,'y':2,'z':3}")).isEqualTo(object(
                    field("x", integer(1)),
                    field("y", integer(2)),
                    field("z", integer(3))
            ));
        }


        @Test void testDecodeObjectIds() {
            assertThat(decode("{'@id':'path'}")).isEqualTo(object(id(base.resolve("path"))));
            assertThat(decode("{'@id':''}")).isEqualTo(object(field(ID, Value.uri(uri())))); // empty ids
        }

        @Test void testDecodeAliasedObjectIds() {

            final Shape shape=shape().id("id");

            assertThat(decode("{'id':'path'}", shape))
                    .isEqualTo(object(
                            id(base.resolve("path")),
                            shape(shape)
                    ));
        }

        @Test void testDecodeInlinedObjectIds() {

            final Shape shape=shape().id("id");

            assertThat(decode("'path'", shape))
                    .isEqualTo(object(
                            id(base.resolve("path")),
                            shape(shape)
                    ));
        }


        @Test void testDecodeTypeProperty() {
            assertThat(decode("{'@type':'Type'}"))
                    .satisfies(object -> assertThat(object.type()).contains("Type"));
        }

        @Test void testDecodeAliasedTypeProperty() {
            assertThat(decode("{'type':'Type'}", shape().type("type")))
                    .satisfies(object -> assertThat(object.type()).contains("Type"));
        }


        @Test void testProcessUnknownLabelsIfNoShapeIsProvided() {
            assertThat(decode("{'x':1}"))
                    .isEqualTo(object(field("x", _1)));
        }

        @Test void testReportUnknownLabelsIfObjectShapeIsProvided() {
            assertThatExceptionOfType(CodecException.class).isThrownBy(() ->
                    decode("{'y':1}", shape().property(property("x").forward(true)))
            );
        }


        @Test void testReportMistypedIdField() {
            assertThatExceptionOfType(CodecException.class)
                    .isThrownBy(() -> decode("{'id':1}", shape().id("id")))
                    .satisfies(e -> assertThat(e.isSyntactic()).isFalse());
        }

        @Test void testReportMistypedTypeField() {
            assertThatExceptionOfType(CodecException.class)
                    .isThrownBy(() -> decode("{'type':1}", shape().type("type")))
                    .satisfies(e -> assertThat(e.isSyntactic()).isFalse());
        }

    }

    @Nested
    final class ArrayTest {

        @Test void testDecodeEmptyArrays() {
            assertThat(decode("[]")).isEqualTo(Array());
        }

        @Test void testDecodeSingletonArrays() {
            assertThat(decode("[1]")).isEqualTo(array(_1));
        }

        @Test void testDecodeFullArrays() {
            assertThat(decode("[1,2]")).isEqualTo(array(_1, _2));
        }


        @Test void testReportNullArrayItemsIfShapeIsProvided() {
            assertThatExceptionOfType(CodecException.class).isThrownBy(() ->
                    decode("{'x':[null, 1]}",
                            shape().property(property("x").forward(true))
                    )
            );
        }

    }

    @Nested
    final class LiteralTest {

        @Test void testDecodeNulls() {
            assertThat(decode("null")).isEqualTo(Nil());
        }

        @Test void testDecodeBooleans() {
            assertThat(decode("false")).isEqualTo(bit(false));
            assertThat(decode("true")).isEqualTo(bit(true));
        }

        @Test void testDecodeIntegrals() {
            assertThat(decode("-10", shape().datatype(Integral()))).isEqualTo(integral(-10L));
            assertThat(decode("-0", shape().datatype(Integral()))).isEqualTo(integral(0L));
            assertThat(decode("0", shape().datatype(Integral()))).isEqualTo(integral(0L));
            assertThat(decode("+0", shape().datatype(Integral()))).isEqualTo(integral(0L));
            assertThat(decode("10", shape().datatype(Integral()))).isEqualTo(integral(10L));
            assertThat(decode("+10", shape().datatype(Integral()))).isEqualTo(integral(10L));
        }

        @Test void testDecodeFloatings() {
            assertThat(decode("-1.0234E1")).isEqualTo(floating(-10.234D));
            assertThat(decode("-1.0E1")).isEqualTo(floating(-10.0D));
            assertThat(decode("-1.234E0")).isEqualTo(floating(-1.234D));
            assertThat(decode("-0.0E0")).isEqualTo(floating(-0.0D));
            assertThat(decode("0.0E0")).isEqualTo(floating(0.0D));
            assertThat(decode("0.0e0")).isEqualTo(floating(0.0D));
            assertThat(decode("1.0E1")).isEqualTo(floating(+10.0D));
            assertThat(decode("1.234E0")).isEqualTo(floating(+1.234D));
            assertThat(decode("1.0234E1")).isEqualTo(floating(+10.234D));
            assertThat(decode("+1.0234E1")).isEqualTo(floating(+10.234D));
        }

        @Test void testDecodeIntegers() {
            assertThat(decode("-10")).isEqualTo(integer((-10)));
            assertThat(decode("-0")).isEqualTo(integer((0)));
            assertThat(decode("0")).isEqualTo(integer((0)));
            assertThat(decode("+0")).isEqualTo(integer((0)));
            assertThat(decode("10")).isEqualTo(integer((10)));
            assertThat(decode("+10")).isEqualTo(integer((10)));
        }

        @Test void testDecodeDecimals() {
            assertThat(decode("-10.234")).isEqualTo(decimal(-10.234));
            assertThat(decode("-1.234")).isEqualTo(decimal(-1.234));
            assertThat(decode("-10.0")).isEqualTo(decimal(-10));
            assertThat(decode("-0.0")).isEqualTo(decimal(0));
            assertThat(decode("0.0")).isEqualTo(decimal(0));
            assertThat(decode("+0.0")).isEqualTo(decimal(0));
            assertThat(decode("10.0")).isEqualTo(decimal(+10));
            assertThat(decode("10.234")).isEqualTo(decimal(+10.234));
            assertThat(decode("+10.234")).isEqualTo(decimal(+10.234));
        }

        @Test void testDecodeStrings() {
            assertThat(decode("''")).isEqualTo(string(""));
            assertThat(decode("'string'")).isEqualTo(string("string"));
        }

        @Test void testDecodeURIs() {
            assertThat(decode("'/'", shape().datatype(URI()))).isEqualTo(Value.uri(base.resolve("/")));
            assertThat(decode("''", shape().datatype(URI()))).isEqualTo(Value.uri(URI.create(""))); // special handling
        }

        @Test void testDecodeTemporals() {
            assertThat(decode("'2018'", shape().datatype(Year()))).isEqualTo(year(Year.parse("2018")));
            assertThat(decode("'2018-01'", shape().datatype(YearMonth()))).isEqualTo(yearMonth(YearMonth.parse("2018-01")));
            assertThat(decode("'2018-01-01'", shape().datatype(LocalDate()))).isEqualTo(localDate(LocalDate.parse("2018-01-01")));
            assertThat(decode("'01:02:03'", shape().datatype(LocalTime()))).isEqualTo(localTime(LocalTime.parse("01:02:03")));
            assertThat(decode("'01:02:03+01:00'", shape().datatype(OffsetTime()))).isEqualTo(offsetTime(OffsetTime.parse("01:02:03+01:00")));
            assertThat(decode("'2018-01-01T01:02:03'", shape().datatype(LocalDateTime()))).isEqualTo(localDateTime(LocalDateTime.parse("2018-01-01T01:02:03")));
            assertThat(decode("'2018-01-01T01:02:03+01:00'", shape().datatype(OffsetDateTime()))).isEqualTo(offsetDateTime(OffsetDateTime.parse("2018-01-01T01:02:03+01:00")));
            assertThat(decode("'2018-01-01T01:02:03Z'", shape().datatype(ZonedDateTime()))).isEqualTo(zonedDateTime(ZonedDateTime.parse("2018-01-01T01:02:03Z")));
            assertThat(decode("'2018-01-01T01:02:03.004Z'", shape().datatype(Instant()))).isEqualTo(instant(Instant.parse("2018-01-01T01:02:03.004Z")));
        }

        @Test void testDecodeTemporalAmounts() {
            assertThat(decode("'P1Y2M3D'", shape().datatype(Period()))).isEqualTo(period(Period.parse("P1Y2M3D")));
            assertThat(decode("'PT1H2M3S'", shape().datatype(Duration()))).isEqualTo(duration(Duration.parse("PT1H2M3S")));
        }


        @Test void testDecodeTexts() {
            assertThat(decode("{'@value':'value','@language':'en'}"))
                    .isEqualTo(text(locale("en"), "value"));
        }

        @Test void testDecodeTypeds() {
            assertThat(decode("{'@value':'value','@type':'test:t'}"))
                    .isEqualTo(data(t, "value"));
        }


        @Test void testReportMalformedValues() {
            assertThatExceptionOfType(CodecException.class).isThrownBy(() -> decode("{'@value':'value'}"));
            assertThatExceptionOfType(CodecException.class).isThrownBy(() -> decode("{'@language':'en'}"));
        }

        @Test void testReportUnexpectedFields() {
            assertThatExceptionOfType(CodecException.class).isThrownBy(() ->
                    decode("{'@value':'value','@type':'test:t','x':1}", shape().property(property("x").forward(true)))
            );
        }

        @Test void testReportDuplicatedSpecialFields() {
            assertThatExceptionOfType(CodecException.class).isThrownBy(() ->
                    decode("{'@value':'value','@type':'test:t','@value':''}")
            );
        }

        @Test void testReportMalformedSpecialFields() {
            assertThatExceptionOfType(CodecException.class).isThrownBy(() ->
                    decode("{'@value':1,'@type':'test:t'}")
            );
        }

        @Test void testReportUnexpectedSpecialFields() {
            assertThatExceptionOfType(CodecException.class).isThrownBy(() ->
                    decode("{'@value':'value','@type':'test:t','@id':1}")
            );
        }

        @Test void testReportMalformedLanguageTags() {
            assertThatExceptionOfType(CodecException.class).isThrownBy(() ->
                    decode("{'@value':'value','@language':'123'}")
            );
        }

    }

    @Nested
    final class ShorthandTest {

        @Test void testResolveRelativeURIs() {
            assertThat(decode("'path'", shape().datatype(URI()))).isEqualTo(Value.uri(base.resolve("path")));
        }

        @Test void testDecodeEmptyLocals() {
            assertThat(decode("{}", shape().datatype(Text())).array())
                    .hasValueSatisfying(values -> assertThat(values).isEmpty());
        }

        @Test void testDecodeUniqueLocals() {
            assertThat(decode("{'en':'one','it':'uno'}", shape().datatype(Text())).array())
                    .hasValueSatisfying(values -> assertThat(values).containsExactlyInAnyOrder(
                            text("en", "one"),
                            text("it", "uno")
                    ));
        }

        @Test void testDecodeMultipleLocals() {
            assertThat(decode("{'en':['one','two'],'it':['uno','due']}", shape().datatype(Text())).array())
                    .hasValueSatisfying(values -> assertThat(values).containsExactlyInAnyOrder(
                            text("en", "one"),
                            text("en", "two"),
                            text("it", "uno"),
                            text("it", "due")
                    ));
        }

        @Test void testDecodeSpecialLocals() {
            assertThat(decode("{'':'value','*':'value'}", shape().datatype(Text())).array())
                    .hasValueSatisfying(values -> assertThat(values).containsExactlyInAnyOrder(
                            text("value"),
                            text(ANY, "value")
                    ));
        }

        @Test void testDecodeInlinedLocals() {
            assertThat(decode("'value'", shape().datatype(Text())))
                    .isEqualTo(text("value"));
        }

        @Test void testReportMalformedLocals() {

            assertThatExceptionOfType(CodecException.class)
                    .isThrownBy(() -> decode("{'en':0}", shape().datatype(Text())));

            assertThatExceptionOfType(CodecException.class)
                    .isThrownBy(() -> decode("{'a tag':''}", shape().datatype(Text())));

        }

    }

    @Nested
    final class QueryTest {

        private Query query(final String query) { return query(query, shape()); }

        private Query query(final String query, final Shape shape) {
            return Optional.of(decode(query, x(shape)))
                    .flatMap(v -> v.value(Query.class))
                    .orElseThrow(() -> new IllegalStateException("undefined query"));
        }

        private static Shape x(final Shape shape) {
            return shape().property(property("x").forward(true).shape(shape));
        }


        @Test void testDecodeQueries() {

            final Shape label=shape().property(property("label").forward(true));
            final Shape members=shape().property(property("members").forward(true).shape(label));

            assertThat(decode("{\"members\": [{\"^label\": \"increasing\"}]}", members)).isEqualTo(object(
                    shape(members),
                    field("members", value(Query.query()
                            .model(object(shape(label)))
                            .where("label", criterion().order(+1))
                    ))

            ));
        }

        @Test void testReportQueryOutsideArray() {
            assertThatExceptionOfType(CodecException.class).isThrownBy(() ->
                    query("{'x':{ '@': 0 }}")
            );
        }

        @Test void testReportMultipleQueries() {
            assertThatExceptionOfType(CodecException.class).isThrownBy(() ->
                    decode("[{ '@': 0 }, { 'x': 0 }]")
            );
        }

        @Test void testReportQueryInProbeModel() {
            assertThatExceptionOfType(CodecException.class).isThrownBy(() ->
                    decode("{\"v=p\":[{\"@\":1}]}", shape().property(property("p").forward(true)))
            );
        }

        @Test void testReportQueryInTableModel() {
            assertThatExceptionOfType(CodecException.class).isThrownBy(() ->
                    decode("{\"v=p\":1,\"p\":[{\"@\":1}]}", shape().property(property("p").forward(true)))
            );
        }


        @Test void testDecodePlainFields() {
            assertThat(query("[{ 'x': 'value', '~x': 'keywords' }]").model().object())
                    .hasValueSatisfying(fields -> assertThat(fields).contains(
                            field("x", string("value"))
                    ));
        }

        @Test void testDecodeTableFields() {
            assertThat(query("[{ 'field=x': 'value', '~x': 'keywords' }]"))

                    .asInstanceOf(type(Query.class))
                    .satisfies(query -> assertThat(query.model().value(Specs.class)).isPresent())

                    .extracting(query -> query.model()
                            .value(Specs.class)
                            .map(Specs::columns)
                            .orElseThrow()
                    )
                    .isEqualTo(list( // ;( compare ignoreing shape
                            new Probe("field", expression("x"), string("value"))
                    ));
        }

        @Test void testReportUnknownProperties() {
            assertThatExceptionOfType(CodecException.class).isThrownBy(() ->
                    decode("{\"z=q\":1}\n", shape().property(property("p").forward(true)))
            );
        }

        @Test void testReportReservedNamesInTable() {

            assertThatExceptionOfType(CodecException.class).isThrownBy(() -> query("[{ '@id' : '', 'v=x' : '' }]"));
            assertThatExceptionOfType(CodecException.class).isThrownBy(() -> query("[{ '@type' : '', 'v=x' : '' }]"));
            assertThatExceptionOfType(CodecException.class).isThrownBy(() -> query("[{ '@' : '', 'v=x' : '' }]"));

            assertThatExceptionOfType(CodecException.class).isThrownBy(() -> query("[{ 'v=x' : '', '@id' : '' }]"));
            assertThatExceptionOfType(CodecException.class).isThrownBy(() -> query("[{ 'v=x' : '', '@type' : '' }]"));
            assertThatExceptionOfType(CodecException.class).isThrownBy(() -> query("[{ 'v=x' : '', '@' : '' }]"));

        }


        @Test void testDecodeLtConstraint() {
            assertThat(query("[{ '<x': 'value' }]").criteria().get(expression("x")).lt())
                    .contains(string("value"));
        }

        @Test void testDecodeGtConstraint() {
            assertThat(query("[{ '>x': 'value' }]").criteria().get(expression("x")).gt())
                    .contains(string("value"));
        }

        @Test void testDecodeLteConstraint() {
            assertThat(query("[{ '<=x': 'value' }]").criteria().get(expression("x")).lte())
                    .contains(string("value"));
        }

        @Test void testDecodeGteConstraint() {
            assertThat(query("[{ '>=x': 'value' }]").criteria().get(expression("x")).gte())
                    .contains(string("value"));
        }


        @Test void testDecodeLikeConstraint() {
            assertThat(query("[{ '~x': 'value' }]").criteria().get(expression("x")).like())
                    .contains("value");
        }

        @Test void testReportMalformedLikeConstraint() {
            assertThatExceptionOfType(CodecException.class)
                    .isThrownBy(() -> query("[{ '~x': 0 }]"));
        }


        @Test void testDecodeAnyConstraint() {

            assertThat(query("[{ '?x': [] }]").criteria().get(expression("x")).any())
                    .contains(set());

            assertThat(query("[{ '?x': [null, true, 1, 'value'] }]").criteria().get(expression("x")).any())
                    .contains(set(Nil(), bit(true), integer(1), string("value")));

        }

        @Test void testDecodeLocalizedAnyConstraint() {
            assertThat(query("[{'?x': [{'en': 'value'}]}]", shape().datatype(Text()))
                    .criteria()
                    .get(expression("x"))
                    .any()
            )
                    .contains(set(text("en", "value")));
        }

        @Test void testDecodeReferenceAnyConstraint() {

            final Shape shape=shape().id("id").datatype(Object());

            assertThat(query("[{'?x': [{'id': 'x'}]}]", shape)
                    .criteria()
                    .get(expression("x"))
                    .any()
            )
                    .contains(set(object(
                            shape(shape),
                            id(base.resolve("x"))
                    )));
        }

        @Test void testDecodeInlinedReferenceAnyConstraint() {
            assertThat(query("[{'?x': ['x']}]", shape().datatype(Object()))
                    .criteria()
                    .get(expression("x"))
                    .any()
            )
                    .contains(set(object(
                            shape(shape().datatype(Object())),
                            id(base.resolve("x"))
                    )));
        }

        @Test void testIgnoreAdditionalFieldsInReferenceAnyConstraint() {
            assertThat(query("[{'?x': [{'@id': 'x', 'label': 'value'}]}]", shape().property(property("label").forward(true)))
                    .criteria()
                    .get(expression("x"))
                    .any()
            )
                    .contains(set(object(
                            shape(shape().property(property("label").forward(true))),
                            id(base.resolve("x"))
                    )));
        }

        @Test void testReportMalformedReferenceAnyConstraint() {
            assertThatExceptionOfType(CodecException.class).as("missing @id").isThrownBy(() ->
                    query("[{'?x': [{}]}]", shape().datatype(Object()))
            );
        }


        @Test void testDecodeOrder() {
            assertThat(query("[{ '^x': 1 }]").criteria().get(expression("x")).order())
                    .contains(1);
        }

        @Test void testDecodeAlternateOrder() {

            assertThat(query("[{ '^x': 'increasing' }]").criteria().get(expression("x")).order())
                    .contains(1);

            assertThat(query("[{ '^x': 'decreasing' }]").criteria().get(expression("x")).order())
                    .contains(-1);

        }

        @Test void testReportMalformedOrder() {

            assertThatExceptionOfType(CodecException.class)
                    .isThrownBy(() -> query("{ '^x': 'x' }"));

            assertThatExceptionOfType(CodecException.class)
                    .isThrownBy(() -> query("{ '^x': 1.2 }"));

        }


        @Test void testDecodeFocus() {
            assertThat(query("[{ '$x': [null] }]").criteria().get(expression("x")).focus())
                    .contains(set(Nil()));
        }


        @Test void testDecodeOffset() {
            assertThat(query("[{ '@': 100  }]").offset())
                    .isEqualTo(100);
        }

        @Test void testReportMalformedOffset() {
            assertThatExceptionOfType(CodecException.class).isThrownBy(() ->
                    query("[{ '@': true  }]")
            );
        }


        @Test void testDecodeLimit() {
            assertThat(query("[{ '#': 100  }]").limit())
                    .isEqualTo(100);
        }

        @Test void testReportMalformedLimit() {
            assertThatExceptionOfType(CodecException.class).isThrownBy(() ->
                    query("[{ '#': true  }]")
            );
        }


        @Test void testHandleMultipleCriteriaOnSamePath() {
            assertThat(query("[{ '>=x': 'lower', '<=x': 'upper' }]").criteria().get(expression("x")))
                    .satisfies(criterion -> {
                        assertThat(criterion.gte()).contains(string("lower"));
                        assertThat(criterion.lte()).contains(string("upper"));
                    });

        }

        @Test void testReportRepeatedCriteriaOnSamePath() {
            assertThatExceptionOfType(CodecException.class).isThrownBy(() ->
                    query("[{ '>=x': 'a', '>=x': 'b' }]")
            );
        }

        @Test void testReportUnknownConstraints() {
            assertThatExceptionOfType(CodecException.class).isThrownBy(() ->
                    query("[{ '±field': 0 }]")
            );
        }

    }

    @Nested
    final class SpecsTest {

        @Test void testDecodeSpecs() {
            assertThat(decode("[{'count=count:':0}]")
                    .value(Query.class)
            )
                    .contains(Query.query(value(new Specs(shape(), list(
                                    Probe.probe("count", expression("count:"), integral(0))
                            ))))
                    );
        }

        @Test void testPreservePlainFieldModels() {

            final Shape shape=shape().property(property("dataset").forward(true).shape(shape()
                    .property(property("label").forward(true).shape(shape().datatype(String())))
            ));

            assertThat(decode("[{'dataset':{'label':''}, 'resources=count:':0}]", shape)
                    .value(Query.class)
                    .flatMap(query -> query.model().value(Specs.class))
                    .flatMap(specs -> specs.column("dataset"))
                    .map(Probe::model)
            ).contains(
                    object(
                            shape(shape.property("dataset").map(Property::shape).orElseGet(Shape::shape)),
                            field("label", String())
                    )
            );

        }

        @Test void testReportSpecsOutsideArray() {
            assertThatExceptionOfType(CodecException.class).isThrownBy(() ->
                    decode("{'x':{'count=count:':0}}")
            );
        }

        @Test void testReportMultipleSpecs() {
            assertThatExceptionOfType(CodecException.class).isThrownBy(() ->
                    decode("[{'count=count:':0}, {'count=count:':0}]")
            );
        }

    }

    @Nested
    final class PruneTest {

        @Test void testIgnoreEmptyValues() {
            assertThat(decode("{'a':null,'b':{},'c':[],'d':[null,{}]}", true)).isEqualTo(object(
                    field("d", array(Nil(), Object()))
            ));
        }

        @Test void testIncludeEmptyValues() {
            assertThat(decode("{'a':null,'b':{},'c':[],'d':[null,{}]}", false)).isEqualTo(object(

                    field("a", Nil()),
                    field("b", Object()),
                    field("c", Array()),
                    field("d", array(Nil(), Object()))

            ));
        }

    }

}
