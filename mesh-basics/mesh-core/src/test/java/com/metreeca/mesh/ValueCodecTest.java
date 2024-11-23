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

import com.metreeca.mesh.util.URIs;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URI;
import java.time.*;

import static com.metreeca.mesh.Field.id;
import static com.metreeca.mesh.Value.*;

import static java.util.Locale.ROOT;
import static org.assertj.core.api.Assertions.assertThat;

final class ValueCodecTest {

    @Test void testVoid() {
        assertThat(Nil().encode()).isEqualTo("null");
        assertThat(Nil().decode("null")).contains(Nil());
        assertThat(Nil().decode("not a void")).isEmpty();
    }

    @Test void testBit() {
        assertThat(bit(true).encode()).isEqualTo("true");
        assertThat(Bit().decode("false")).contains(bit(false));
        assertThat(Bit().decode("not a boolean")).isEmpty();
    }

    @Test void testNumber() {

        assertThat(number(BigInteger.valueOf(123)).encode()).isEqualTo("123");
        assertThat(number(BigDecimal.valueOf(123.0)).encode()).isEqualTo("123.0");
        assertThat(number(1.23e1D).encode()).isEqualTo("1.23e1");

        assertThat(Number().decode("123")).contains(integer(123));
        assertThat(Number().decode("123.0")).contains(decimal(123.0));
        assertThat(Number().decode("1.23e1")).contains(floating(1.23e1));

        assertThat(Number().decode("not a number")).isEmpty();

    }

    @Test void testIntegral() {
        assertThat(integral(123L).encode()).isEqualTo("123");
        assertThat(Integral().decode("123")).contains(integral(123));
        assertThat(Integral().decode("not an integral")).isEmpty();
    }

    @Test void testFloating() {

        assertThat(floating(123.0D).encode()).isEqualTo("1.23e2");
        assertThat(Floating().decode("123")).contains(floating(123.0));

        assertThat(Floating().decode("not an floating")).isEmpty();

    }

    @Test void testDecimal() {
        assertThat(decimal(123).encode()).isEqualTo("123.0");
        assertThat(decimal(123.456).encode()).isEqualTo("123.456");
        assertThat(Decimal().decode("123.0")).contains(decimal(123.0));
        assertThat(Decimal().decode("not a decimal")).isEmpty();
    }

    @Test void testInteger() {
        assertThat(integer(123).encode()).isEqualTo("123");
        assertThat(Integer().decode("123")).contains(integer(123));
        assertThat(Integer().decode("not an integer")).isEmpty();
    }

    @Test void testString() {
        assertThat(string("value").encode()).isEqualTo("value");
        assertThat(String().decode("value")).contains(string("value"));
    }

    @Test void testURI() {

        final URI base=URI.create("https://example.org/base/");

        assertThat(uri(base.resolve("/resources/123")).encode(base)).isEqualTo("/resources/123");
        assertThat(uri(URI.create("https://example.net/123")).encode(base)).isEqualTo("https://example.net/123");

        assertThat(URI().decode("", base)).contains(uri(base));
        assertThat(URI().decode("/resources/123", base)).contains(uri(base.resolve("/resources/123")));
        assertThat(URI().decode("https://example.net/123", base)).contains(uri(URI.create("https://example.net/123")));

        assertThat(URI().decode("not a URI", base)).isEmpty();
    }

    @Test void testTemporal() {
        assertThat(Temporal().decode("2024")).contains(temporal(Year.of(2024)));
        assertThat(Temporal().decode("2024-01-01")).contains(temporal(LocalDate.of(2024, 1, 1)));
        assertThat(Temporal().decode("not a temporal")).isEmpty();
    }

    @Test void testInstant() {

        final String encoded="2024-12-20T10:15:30.123Z";
        final Value instant=instant(Instant.parse(encoded));

        assertThat(instant.encode()).isEqualTo(encoded);
        assertThat(Instant().decode(encoded)).contains(instant);

        assertThat(Instant().decode("not an instant")).isEmpty();
    }

    @Test void testTemporalAmount() {

        assertThat(TemporalAmount().decode("P100D")).contains(temporalAmount(Period.ofDays(100)));
        assertThat(TemporalAmount().decode("PT100H")).contains(temporalAmount(Duration.ofHours(100)));

        assertThat(TemporalAmount().decode("not a temporal amount")).isEmpty();

    }

    @Test void testDuration() {

        final String encoded="PT1H2M3.456S";
        final Value duration=duration(Duration.parse(encoded));

        assertThat(duration.encode()).isEqualTo(encoded);
        assertThat(Duration().decode(encoded)).contains(duration);

        assertThat(Duration().decode("not an instant")).isEmpty();
    }

    @Test void testText() {

        final String encoded="value@en";
        final Value text=text("en", "value");

        assertThat(Text().decode(encoded)).contains(text);

        assertThat(text(ROOT, "value").encode()).isEqualTo("value");

        assertThat(Text().decode("")).contains(text(ROOT, ""));
        assertThat(Text().decode("@")).contains(text(ROOT, "@"));

    }

    @Test void testData() {

        final String encoded="value^^/#datatype";
        final Value data=data(URIs.term("datatype"), "value");

        assertThat(data.encode(URIs.base())).isEqualTo(encoded);
        assertThat(Data().decode(encoded, URIs.base())).contains(data);

        assertThat(Data().decode("not a typed")).isEmpty();

    }

    @Test void testObject() {

        assertThat(Object().encode()).isEqualTo("");
        assertThat(object(id(URIs.item("resource"))).encode(URIs.base())).isEqualTo("/resource");

        assertThat(Object().decode("")).contains(Object());
        assertThat(Object().decode("/resource", URIs.base())).contains(object(id(URIs.item("resource"))));

        assertThat(Object().decode("not an object")).isEmpty();
    }

}