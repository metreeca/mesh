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

package com.metreeca.mesh;

import com.metreeca.mesh.shapes.Shape;
import com.metreeca.mesh.shapes.Type;

import org.junit.jupiter.api.Test;

import java.net.URI;
import java.time.LocalDate;
import java.time.Period;
import java.util.Locale;

import static com.metreeca.mesh.Value.Integer;
import static com.metreeca.mesh.Value.Nil;
import static com.metreeca.mesh.Value.Object;
import static com.metreeca.mesh.Value.array;
import static com.metreeca.mesh.Value.data;
import static com.metreeca.mesh.Value.field;
import static com.metreeca.mesh.Value.integer;
import static com.metreeca.mesh.Value.localDate;
import static com.metreeca.mesh.Value.object;
import static com.metreeca.mesh.Value.period;
import static com.metreeca.mesh.Value.shape;
import static com.metreeca.mesh.Value.string;
import static com.metreeca.mesh.Value.text;
import static com.metreeca.mesh.Value.uri;
import static com.metreeca.mesh.shapes.Property.property;
import static com.metreeca.mesh.shapes.Shape.shape;
import static com.metreeca.mesh.shapes.Type.type;
import static com.metreeca.shim.Collections.set;

import static org.assertj.core.api.Assertions.assertThat;

final class ValueValidatorTest {

    private static final Locale en=Locale.ENGLISH;
    private static final Locale it=Locale.ITALIAN;
    private static final Locale fr=Locale.FRENCH;

    private static final Type X=type("test:X");
    private static final Type Y=type("test:Y");
    private static final Type Z=type("test:Z");

    private static final Value x=string("x");
    private static final Value y=string("y");
    private static final Value z=string("z");

    private static final Value Zero=integer(0);
    private static final Value One=integer(1);
    private static final Value Ten=integer(10);


    private boolean validate(final Shape shape, final Value value) {
        return validate(shape, value, false);
    }

    private boolean validate(final Shape shape, final Value value, final boolean delta) {
        return new ValueValidator(shape, delta).validate(value, true).isEmpty();
    }


    //̸/////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Test void testDatatype() {
        assertThat(validate(shape().datatype(Integer()), Nil())).as("ignored").isTrue();
        assertThat(validate(shape().datatype(Integer()), integer(0))).as("satisfied").isTrue();
        assertThat(validate(shape().datatype(Integer()), string(""))).as("failed").isFalse();
    }

    @Test void testClass() {
        assertThat(validate(shape().clazz(X, Y), Nil())).as("ignored").isTrue();
        assertThat(validate(shape().clazz(X, Y), object(shape(shape().clazz(X, Y))))).as("satisfied").isTrue();
        assertThat(validate(shape().clazz(X, Y), object(shape(shape().clazz(X, Z))))).as("failed").isFalse();
        assertThat(validate(shape().clazz(X, Y), Object())).as("undefined type").isFalse();
        assertThat(validate(shape().clazz(X, Y), integer(0))).as("not a frame").isFalse();
    }


    @Test void testMinExclusive() {
        assertThat(validate(shape().minExclusive(Zero), Nil())).as("ignored").isTrue();
        assertThat(validate(shape().minExclusive(Zero), array(One, Ten))).as("satisfied").isTrue();
        assertThat(validate(shape().minExclusive(Zero), array(Zero, One))).as("failed").isFalse();
        assertThat(validate(shape().minExclusive(Zero), string(""))).as("incomparable").isFalse();
    }

    @Test void testMaxExclusive() {
        assertThat(validate(shape().maxExclusive(Zero), Nil())).as("ignored").isTrue();
        assertThat(validate(shape().maxExclusive(Ten), array(Zero, One))).as("satisfied").isTrue();
        assertThat(validate(shape().maxExclusive(Ten), array(One, Ten))).as("failed").isFalse();
        assertThat(validate(shape().maxExclusive(Zero), string(""))).as("incomparable").isFalse();
    }

    @Test void testMinInclusive() {
        assertThat(validate(shape().minInclusive(Zero), Nil())).as("ignored").isTrue();
        assertThat(validate(shape().minInclusive(One), array(One, Ten))).as("satisfied").isTrue();
        assertThat(validate(shape().minInclusive(One), array(Zero, One))).as("failed").isFalse();
        assertThat(validate(shape().minInclusive(Zero), string(""))).as("incomparable").isFalse();
    }

    @Test void testMaxInclusive() {
        assertThat(validate(shape().maxInclusive(Zero), Nil())).as("ignored").isTrue();
        assertThat(validate(shape().maxInclusive(One), array(Zero, One))).as("satisfied").isTrue();
        assertThat(validate(shape().maxInclusive(One), array(One, Ten))).as("failed").isFalse();
        assertThat(validate(shape().maxInclusive(Zero), string(""))).as("incomparable").isFalse();
    }

    @Test void testMinLength() {
        assertThat(validate(shape().minLength(3), Nil())).as("ignored").isTrue();
        assertThat(validate(shape().minLength(0), string(""))).as("ignored").isTrue();
        assertThat(validate(shape().minLength(3), string("abc"))).as("satisfied").isTrue();
        assertThat(validate(shape().minLength(3), string("ab"))).as("failed").isFalse();
        assertThat(validate(shape().minLength(8), uri(URI.create("test:abc")))).as("satisfied").isTrue();
        assertThat(validate(shape().minLength(8), uri(URI.create("test:ab")))).as("failed").isFalse();
        assertThat(validate(shape().minLength(3), text("en", "abc"))).as("satisfied").isTrue();
        assertThat(validate(shape().minLength(3), text("en", "ab"))).as("failed").isFalse();
        assertThat(validate(shape().minLength(3), data(URI.create("test:t"), "abc"))).as("satisfied").isTrue();
        assertThat(validate(shape().minLength(3), data(URI.create("test:t"), "ab"))).as("failed").isFalse();
    }

    @Test void testMaxLength() {
        assertThat(validate(shape().maxLength(3), Nil())).as("ignored").isTrue();
        assertThat(validate(shape().maxLength(0), string("abc"))).as("ignored").isTrue();
        assertThat(validate(shape().maxLength(3), string("ab"))).as("satisfied").isTrue();
        assertThat(validate(shape().maxLength(3), string("abcd"))).as("failed").isFalse();
        assertThat(validate(shape().maxLength(8), uri(URI.create("test:abc")))).as("satisfied").isTrue();
        assertThat(validate(shape().maxLength(8), uri(URI.create("test:abcd")))).as("failed").isFalse();
        assertThat(validate(shape().maxLength(2), text("en", "ab"))).as("satisfied").isTrue();
        assertThat(validate(shape().maxLength(2), text("en", "abc"))).as("failed").isFalse();
        assertThat(validate(shape().maxLength(2), data(URI.create("test:t"), "ab"))).as("satisfied").isTrue();
        assertThat(validate(shape().maxLength(2), data(URI.create("test:t"), "abc"))).as("failed").isFalse();
    }

    @Test void testPattern() {
        assertThat(validate(shape().pattern("^1"), Nil())).as("ignored").isTrue();
        assertThat(validate(shape().pattern(""), string("abc"))).as("ignored").isTrue();
        assertThat(validate(shape().pattern("^1"), integer(100))).as("satisfied").isTrue();
        assertThat(validate(shape().pattern("^1"), integer(300))).as("failed").isFalse();
        assertThat(validate(shape().pattern("^a"), string("abc"))).as("satisfied").isTrue();
        assertThat(validate(shape().pattern("^a"), string("xyz"))).as("failed").isFalse();
        assertThat(validate(shape().pattern("^test:a"), uri(URI.create("test:abc")))).as("satisfied").isTrue();
        assertThat(validate(shape().pattern("^test:a"), uri(URI.create("test:xyz")))).as("failed").isFalse();
        assertThat(validate(shape().pattern("^2025"), localDate(LocalDate.of(2025, 2, 24)))).as("satisfied").isTrue();
        assertThat(validate(shape().pattern("^2024"), localDate(LocalDate.of(2025, 2, 24)))).as("failed").isFalse();
        assertThat(validate(shape().pattern("^P1"), period(Period.ofDays(100)))).as("satisfied").isTrue();
        assertThat(validate(shape().pattern("^P1"), period(Period.ofDays(300)))).as("failed").isFalse();
        assertThat(validate(shape().pattern("^a"), text("en", "abc"))).as("satisfied").isTrue();
        assertThat(validate(shape().pattern("^a"), text("en", "xyz"))).as("failed").isFalse();
        assertThat(validate(shape().pattern("^a"), data(URI.create("test:t"), "abc"))).as("satisfied").isTrue();
        assertThat(validate(shape().pattern("^a"), data(URI.create("test:t"), "xyz"))).as("failed").isFalse();
    }


    @Test void testIn() {
        assertThat(validate(shape().in(x, y), Nil())).as("ignored").isTrue();
        assertThat(validate(shape().in(set()), x)).as("ignored").isTrue();
        assertThat(validate(shape().in(x, y), x)).as("satisfied").isTrue();
        assertThat(validate(shape().in(x, y), array(y, z))).as("failed").isFalse();
    }

    @Test void testLanguageIn() {
        assertThat(validate(shape().languageIn(en, it), Nil())).as("ignored").isTrue();
        assertThat(validate(shape().languageIn(set()), string("abc"))).as("ignored").isTrue();
        assertThat(validate(shape().languageIn(en, it), text(en, "abc"))).as("satisfied").isTrue();
        assertThat(validate(shape().languageIn(en, it), text(fr, "abc"))).as("failed").isFalse();
        assertThat(validate(shape().languageIn(en, it), integer(0))).as("not a localized string").isFalse();
    }


    @Test void testUniqueLang() {

        assertThat(validate(shape().uniqueLang(true), Nil())).isTrue();

        assertThat(validate(shape().uniqueLang(true), array(
                text(en, "a"),
                text(it, "a")
        ))).isTrue();

        assertThat(validate(shape().uniqueLang(true), array(
                text(en, "b"),
                text(it, "a"),
                text(it, "b")
        ))).isFalse();

    }

    @Test void testMinCount() {
        assertThat(validate(shape().minCount(0), Nil())).as("satisfied").isTrue();
        assertThat(validate(shape().minCount(1), Nil())).as("failed").isFalse();
        assertThat(validate(shape().minCount(1), x)).as("satisfied").isTrue();
        assertThat(validate(shape().minCount(2), x)).as("failed").isFalse();
        assertThat(validate(shape().minCount(2), array(x, y))).as("satisfied").isTrue();
        assertThat(validate(shape().minCount(3), array(x, y))).as("failed").isFalse();
    }

    @Test void testMaxCount() {
        assertThat(validate(shape().maxCount(0), x)).as("ignored").isTrue();
        assertThat(validate(shape().maxCount(2), Nil())).as("satisfied").isTrue();
        assertThat(validate(shape().maxCount(2), array(x, y))).as("satisfied").isTrue();
        assertThat(validate(shape().maxCount(2), array(x, y, z))).as("failed").isFalse();
    }

    @Test void testHasValue() {
        assertThat(validate(shape().hasValue(set()), x)).as("ignored").isTrue();
        assertThat(validate(shape().hasValue(x), Nil())).as("failed").isFalse();
        assertThat(validate(shape().hasValue(x), x)).as("satisfied").isTrue();
        assertThat(validate(shape().hasValue(x), y)).as("failed").isFalse();
        assertThat(validate(shape().hasValue(x), array(x, y))).as("satisfied").isTrue();
        assertThat(validate(shape().hasValue(x, y), array(y, z))).as("failed").isFalse();
    }


    @Test void testConstraint() {

        final Shape pass=shape().constraints(frame -> Nil());
        final Shape fail=shape().constraints(frame -> string("fail"));

        assertThat(validate(pass, Nil())).isTrue();
        assertThat(validate(pass, Object())).isTrue();
        assertThat(validate(fail, Object())).isFalse();
    }

    @Test void testProperties() {

        final Shape shape=shape().property(property("value").forward(true)
                .shape(shape().minCount(2))
        );

        assertThat(validate(shape, Nil())).as("ignored").isTrue();
        assertThat(validate(shape, object(field("value", array(integer(1), integer(2)))))).as("satisfied").isTrue();
        assertThat(validate(shape, object())).as("missing").isFalse();
        assertThat(validate(shape, object(field("value", integer(1))))).as("under size").isFalse();

    }

    @Test void testClosed() {
        assertThat(validate(shape(), object(field("unexpected", Nil())))).isFalse();
    }


    //̸/////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Test void testArray() {

        assertThat(validate(

                shape()
                        .property(property("x").forward(true).shape(shape().minInclusive(integer(10)))),

                object(
                        field("x", array(integer(1)))
                ),

                true

        )).as("invalid array value").isFalse();

    }

    @Test void testEmbedded() {

        assertThat(validate(
                shape().property(property("x").forward(true)),
                object(
                        field("x", object(
                                shape(shape().property(property("y").forward(true).shape(shape().minCount(2)))),
                                field("y", integer(1))

                        ))
                )

        )).as("not traversed").isTrue();

        assertThat(validate(
                shape().property(property("x").forward(true).embedded(true)),
                object(
                        field("x", object(
                                shape(shape().property(property("y").forward(true).shape(shape().minCount(2)))),
                                field("y", integer(1))

                        ))
                )
        )).as("traversed").isFalse();

    }

    @Test void testPartial() {

        assertThat(validate(

                shape()
                        .property(property("x").forward(true).shape(shape().required().datatype(Integer())))
                        .property(property("y").forward(true).shape(shape().minCount(2).datatype(Integer()))),

                object(
                        field("x", integer(1))
                ),

                true

        )).as("undefined field").isTrue();

        assertThat(validate(

                shape()
                        .property(property("x").forward(true).shape(shape().required().datatype(Integer())))
                        .property(property("y").forward(true).shape(shape().minCount(2).datatype(Integer()))),

                object(
                        field("x", integer(1)),
                        field("y", array(integer(1)))
                ),

                true

        )).as("defined invalid field").isFalse();
    }

}
