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

import com.metreeca.shim.URIs;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URI;
import java.time.*;

import static com.metreeca.mesh.Value.*;
import static com.metreeca.shim.URIs.*;
import static com.metreeca.shim.URIs.uri;

import static java.util.Locale.ROOT;
import static org.assertj.core.api.Assertions.assertThat;

final class ValueTestCodec {

    @Test void testVoid() {
        assertThat(Nil().encode(uri())).isEqualTo("null");
        assertThat(Nil().decode("null", uri())).contains(Nil());
        assertThat(Nil().decode("not a void", uri())).isEmpty();
    }

    @Test void testBit() {
        assertThat(bit(true).encode(uri())).isEqualTo("true");
        assertThat(Bit().decode("false", uri())).contains(bit(false));
        assertThat(Bit().decode("not a boolean", uri())).isEmpty();
    }

    @Test void testNumber() {

        assertThat(number(BigInteger.valueOf(123)).encode(uri())).isEqualTo("123");
        assertThat(number(BigDecimal.valueOf(123.0)).encode(uri())).isEqualTo("123.0");
        assertThat(number(1.23e1D).encode(uri())).isEqualTo("1.23e1");

        assertThat(Number().decode("123", uri())).contains(integer(123));
        assertThat(Number().decode("123.0", uri())).contains(decimal(123.0));
        assertThat(Number().decode("1.23e1", uri())).contains(floating(1.23e1));

        assertThat(Number().decode("not a number", uri())).isEmpty();

    }

    @Test void testIntegral() {
        assertThat(integral(123L).encode(uri())).isEqualTo("123");
        assertThat(Integral().decode("123", uri())).contains(integral(123));
        assertThat(Integral().decode("not an integral", uri())).isEmpty();
    }

    @Test void testFloating() {

        assertThat(floating(123.0D).encode(uri())).isEqualTo("1.23e2");
        assertThat(Floating().decode("123", uri())).contains(floating(123.0));

        assertThat(Floating().decode("not an floating", uri())).isEmpty();

    }

    @Test void testDecimal() {
        assertThat(decimal(123).encode(uri())).isEqualTo("123.0");
        assertThat(decimal(123.456).encode(uri())).isEqualTo("123.456");
        assertThat(Decimal().decode("123.0", uri())).contains(decimal(123.0));
        assertThat(Decimal().decode("not a decimal", uri())).isEmpty();
    }

    @Test void testInteger() {
        assertThat(integer(123).encode(uri())).isEqualTo("123");
        assertThat(Integer().decode("123", uri())).contains(integer(123));
        assertThat(Integer().decode("not an integer", uri())).isEmpty();
    }

    @Test void testString() {
        assertThat(string("value").encode(uri())).isEqualTo("value");
        assertThat(String().decode("value", uri())).contains(string("value"));
    }

    @Test void testURI() {

        final URI base=URI.create("https://example.org/base/");

        assertThat(Value.uri(base.resolve("/resources/123")).encode(base)).isEqualTo("/resources/123");
        assertThat(Value.uri(URI.create("https://example.net/123")).encode(base)).isEqualTo("https://example.net/123");

        assertThat(URI().decode("", base)).contains(Value.uri(base));
        assertThat(URI().decode("/resources/123", base)).contains(Value.uri(base.resolve("/resources/123")));
        assertThat(URI().decode("https://example.net/123", base)).contains(Value.uri(URI.create("https://example.net/123")));

        assertThat(URI().decode("not a URI", base)).isEmpty();
    }

    @Test void testTemporal() {
        assertThat(Temporal().decode("2024", uri())).contains(temporal(Year.of(2024)));
        assertThat(Temporal().decode("2024-01-01", uri())).contains(temporal(LocalDate.of(2024, 1, 1)));
        assertThat(Temporal().decode("not a temporal", uri())).isEmpty();
    }

    @Test void testInstant() {

        final String encoded="2024-12-20T10:15:30.123Z";
        final Value instant=instant(Instant.parse(encoded));

        assertThat(instant.encode(uri())).isEqualTo(encoded);
        assertThat(Instant().decode(encoded, uri())).contains(instant);

        assertThat(Instant().decode("not an instant", uri())).isEmpty();
    }

    @Test void testTemporalAmount() {

        assertThat(TemporalAmount().decode("P100D", uri())).contains(temporalAmount(Period.ofDays(100)));
        assertThat(TemporalAmount().decode("PT100H", uri())).contains(temporalAmount(Duration.ofHours(100)));

        assertThat(TemporalAmount().decode("not a temporal amount", uri())).isEmpty();

    }

    @Test void testDuration() {

        final String encoded="PT1H2M3.456S";
        final Value duration=duration(Duration.parse(encoded));

        assertThat(duration.encode(uri())).isEqualTo(encoded);
        assertThat(Duration().decode(encoded, uri())).contains(duration);

        assertThat(Duration().decode("not an instant", uri())).isEmpty();
    }

    @Test void testText() {

        final String encoded="value@en";
        final Value text=text("en", "value");

        assertThat(Text().decode(encoded, uri())).contains(text);

        assertThat(text(ROOT, "value").encode(uri())).isEqualTo("value");

        assertThat(Text().decode("", uri())).contains(text(ROOT, ""));
        assertThat(Text().decode("@", uri())).contains(text(ROOT, "@"));

    }

    @Test void testData() {

        final String encoded="value^^/#datatype";
        final Value data=data(URIs.term("datatype"), "value");

        assertThat(data.encode(base())).isEqualTo(encoded);
        assertThat(Data().decode(encoded, base())).contains(data);

        assertThat(Data().decode("not a typed", uri())).isEmpty();

    }

    @Test void testObject() {

        assertThat(Object().encode(uri())).isEqualTo("");
        assertThat(object(id(item("resource"))).encode(base())).isEqualTo("/resource");

        assertThat(Object().decode("", uri())).contains(Object());
        assertThat(Object().decode("/resource", base())).contains(object(id(item("resource"))));

        assertThat(Object().decode("not an object", uri())).isEmpty();
    }

}
