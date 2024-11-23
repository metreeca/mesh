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

package com.metreeca.mesh.json;

import com.metreeca.mesh.Value;
import com.metreeca.mesh.queries.Table;
import com.metreeca.mesh.queries.Tuple;
import com.metreeca.shim.Locales;
import com.metreeca.shim.URIs;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.StringWriter;
import java.io.UncheckedIOException;
import java.net.URI;
import java.util.Locale;

import static com.metreeca.mesh.Value.Array;
import static com.metreeca.mesh.Value.Bit;
import static com.metreeca.mesh.Value.Nil;
import static com.metreeca.mesh.Value.Object;
import static com.metreeca.mesh.Value.Text;
import static com.metreeca.mesh.Value.array;
import static com.metreeca.mesh.Value.bit;
import static com.metreeca.mesh.Value.data;
import static com.metreeca.mesh.Value.decimal;
import static com.metreeca.mesh.Value.field;
import static com.metreeca.mesh.Value.floating;
import static com.metreeca.mesh.Value.id;
import static com.metreeca.mesh.Value.integer;
import static com.metreeca.mesh.Value.integral;
import static com.metreeca.mesh.Value.object;
import static com.metreeca.mesh.Value.shape;
import static com.metreeca.mesh.Value.string;
import static com.metreeca.mesh.Value.text;
import static com.metreeca.mesh.Value.type;
import static com.metreeca.mesh.Value.uri;
import static com.metreeca.mesh.Value.value;
import static com.metreeca.mesh.shapes.Property.property;
import static com.metreeca.mesh.shapes.Shape.shape;
import static com.metreeca.shim.Collections.entry;
import static com.metreeca.shim.Collections.list;

import static org.assertj.core.api.Assertions.assertThat;

final class JSONEncoderTest {

    private static String encode(final Value value) { return encode(value, true); }

    private static String encode(final Value value, final boolean prune) {
        try ( final StringWriter writer=new StringWriter() ) {

            new JSONEncoder(JSONCodec.json().prune(prune), writer).encode(value);

            return writer.toString();

        } catch ( final IOException e ) {

            throw new UncheckedIOException(e);

        }
    }


    private static String json(final String string) {
        return string
                .replace('\'', '"')
                .replace("\n", "");
    }


    @Nested
    final class ObjectTest {

        @Test void testHandleEmptyObjects() {
            assertThat(encode(Object())).isEqualTo(json(

                    "{}"

            ));
        }

        @Test void testHandleEmptyNestedObjects() {
            assertThat(encode(object(

                    field("value", Object())

            ))).isEqualTo(json(

                    "{}"

            ));
        }

        @Test void testHandleIds() {
            assertThat(encode(object(

                    id(URI.create("test:x"))

            ))).isEqualTo(json(

                    "{'@id':'test:x'}"

            ));
        }

        @Test void testHandleAliasedIds() {
            assertThat(encode(object(

                    shape(shape()
                            .id("id")
                    ),

                    id(URI.create("test:x"))

            ))).isEqualTo(json(

                    "{'id':'test:x'}"

            ));
        }

        @Test void testHandleType() {
            assertThat(encode(object(

                    type("Type")

            ))).isEqualTo(json(

                    "{'@type':'Type'}"

            ));
        }

        @Test void testHandleAliasedTypes() {
            assertThat(encode(object(

                    shape(shape()
                            .type("type")
                    ),

                    type("Type")

            ))).isEqualTo(json(

                    "{'type':'Type'}"

            ));
        }

        @Test void testHandleProperties() {
            assertThat(encode(object(

                    field("x", string("one")),
                    field("y", array(string("two")))

            ))).isEqualTo(json(

                    "{'x':'one','y':['two']}"

            ));
        }

        @Test void testIncludeAllPropertiesIfNoShapeIsProvided() {
            assertThat(encode(object(

                    field("x", string("one")),
                    field("y", string("two"))

            ))).isEqualTo(json(

                    "{'x':'one','y':'two'}"

            ));
        }

        @Test void testIgnoreUnknownPropertiesIfShapeIsProvided() {
            assertThat(encode(object(

                    shape(shape()
                            .property(property("x").forward(true))
                    ),

                    field("x", string("one")),
                    field("y", string("two"))

            ))).isEqualTo(json(

                    "{'x':'one'}"

            ));
        }

        @Test void testHandleNestedObjects() {
            assertThat(encode(object(

                    shape(shape()
                            .property(property("t").forward(true).shape(shape()
                                    .property(property("x").forward(true).shape(shape().datatype(Bit()).required()))
                                    .property(property("y").forward(true).shape(shape().datatype(Bit()).required()))
                            ))
                    ),

                    field("t", array(
                            object(entry("x", bit(true))),
                            object(entry("y", bit(false)))
                    ))

            ))).isEqualTo(json(

                    "{'t':[{'x':true},{'y':false}]}"

            ));
        }

    }

    @Nested
    final class ArrayTest {

        @Test void testHandleEmptyArrays() {
            assertThat(encode(Array())).isEqualTo(json(

                    "[]"

            ));
        }

        @Test void testHandleArray() {
            assertThat(encode(array(integral(1), integral(2)))).isEqualTo(json(

                    "[1,2]"

            ));
        }

        @Test void testHandleNestedValues() {
            assertThat(encode(array(

                    Nil(),
                    string("x"),
                    Array(),
                    array(string("x")),
                    Object(),
                    object(field("x", string("one")))

            ))).isEqualTo(json(

                    "[null,'x',[],['x'],{},{'x':'one'}]"

            ));
        }

    }

    @Nested
    final class TableTest {

        @Test void testHandleEmptyTable() {
            assertThat(encode(object(

                    field("table", value(new Table(list())))

            ))).isEqualTo(json("{}"));
        }

        @Test void testHandleFullTable() {
            assertThat(encode(object(

                    field("table", value(new Table(list(
                            new Tuple(list(
                                    entry("x", integer(1)),
                                    entry("y", integer(2))
                                    )),
                            new Tuple(list(
                                    entry("x", Nil()),
                                    entry("y", Object())
                                    ))
                    ))))))).isEqualTo(json("{'table':[{'x':1,'y':2},{'x':null,'y':{}}]}"));
        }

    }

    @Nested
    final class LiteralTest {

        @Test void testEncodeNulls() {
            assertThat(encode(Nil())).isEqualTo(json("null"));
        }

        @Test void testEncodeBooleans() {
            assertThat(encode(bit(true))).isEqualTo(json("true"));
            assertThat(encode(bit(false))).isEqualTo(json("false"));
        }

        @Test void testEncodeFloatings() {
            assertThat(encode(floating(-10.234))).isEqualTo(json("-1.0234e1"));
            assertThat(encode(floating(-10.0))).isEqualTo(json("-1.0e1"));
            assertThat(encode(floating(-1.234))).isEqualTo(json("-1.234e0"));
            assertThat(encode(floating(-0.0))).isEqualTo(json("-0.0e0"));
            assertThat(encode(floating(0.0))).isEqualTo(json("0.0e0"));
            assertThat(encode(floating(+10.0))).isEqualTo(json("1.0e1"));
            assertThat(encode(floating(+1.234))).isEqualTo(json("1.234e0"));
            assertThat(encode(floating(+10.234))).isEqualTo(json("1.0234e1"));
        }

        @Test void testEncodeIntegers() {
            assertThat(encode(integer(-10))).isEqualTo(json("-10"));
            assertThat(encode(integer(0))).isEqualTo(json("0"));
            assertThat(encode(integer(+10))).isEqualTo(json("10"));
        }

        @Test void testEncodeDecimals() {
            assertThat(encode(decimal(-10.234))).isEqualTo(json("-10.234"));
            assertThat(encode(decimal(-1.234))).isEqualTo(json("-1.234"));
            assertThat(encode(decimal(-10))).isEqualTo(json("-10.0"));
            assertThat(encode(decimal(0))).isEqualTo(json("0.0"));
            assertThat(encode(decimal(+10))).isEqualTo(json("10.0"));
            assertThat(encode(decimal(+10.234))).isEqualTo(json("10.234"));
        }

        @Test void testEncodeStrings() {
            assertThat(encode(string("value"))).isEqualTo(json("'value'"));
        }

        @Test void testEncodeTexts() {
            assertThat(encode(text(Locales.locale("en"), "value")))
                    .isEqualTo(json("{'@value':'value','@language':'en'}"));
        }

        @Test void testEncodeTypeds() {
            assertThat(encode(data(URI.create("test:t"), "value")))
                    .isEqualTo(json("{'@value':'value','@type':'test:t'}"));
        }

    }

    @Nested
    final class ShorthandTest {

        @Test void testUseRootRelativeIRIs() {
            assertThat(encode(uri(URIs.base().resolve("/path/name")))).isEqualTo(json("'/path/name'"));
        }

        @Test void testCompactKnownTaggedLiterals() {
            assertThat(encode(object(

                    shape(shape().property(property("value").forward(true).shape(shape().datatype(Text())))),

                    field("value", array(
                            text(Locales.locale("en"), "one"),
                            text(Locales.locale("en"), "two"),
                            text(Locales.locale("it"), "uno"),
                            text(Locales.locale("it"), "due")
                    ))

            ))).isEqualTo(json("{'value':{'en':['one','two'],'it':['uno','due']}}"));
        }

        @Test void testCompactKnownUniqueTaggedLiterals() {
            assertThat(encode(object(

                    shape(shape().property(property("value").forward(true).shape(shape().datatype(Text()).uniqueLang(true)))),

                    field("value", array(
                            text(Locales.locale("en"), "one"),
                            text(Locales.locale("it"), "uno")
                    ))

            ))).isEqualTo(json("{'value':{'en':'one','it':'uno'}}"));
        }

        @Test void testHandleSpecialLocales() {
            assertThat(encode(object(

                    shape(shape().property(property("value").forward(true).shape(shape().datatype(Text()).uniqueLang(true)))),

                    field("value", array(
                            text(Locales.ANY, "any"),
                            text(Locale.ROOT, "root")
                    ))

            ))).isEqualTo(json("{'value':{'':'root'}}"));
        }

    }

    @Nested
    final class PruneTest {

        @Test void testIgnoreEmptyValues() {
            assertThat(encode(object(

                    field("a", Nil()),
                    field("b", Object()),
                    field("c", Array()),
                    field("d", array(Nil(), Object()))

            ), true)).isEqualTo(json(

                    "{'d':[null,{}]}"

            ));
        }

        @Test void testIncludeEmptyValues() {
            assertThat(encode(object(

                    field("a", Nil()),
                    field("b", Object()),
                    field("c", Array()),
                    field("d", array(Nil(), Object()))

            ), false)).isEqualTo(json(

                    "{'a':null,'b':{},'c':[],'d':[null,{}]}"

            ));
        }

    }

}