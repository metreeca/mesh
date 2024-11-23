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

import org.eclipse.rdf4j.model.vocabulary.XSD;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.*;
import java.util.Set;

import static com.metreeca.mesh.Value.*;
import static com.metreeca.mesh.rdf4j.SPARQLConverter.json;
import static com.metreeca.mesh.rdf4j.SPARQLConverter.rdf;
import static com.metreeca.shim.Collections.set;
import static com.metreeca.shim.URIs.base;

import static java.time.Instant.EPOCH;
import static java.time.ZoneOffset.UTC;
import static org.assertj.core.api.Assertions.assertThat;
import static org.eclipse.rdf4j.model.util.Values.iri;
import static org.eclipse.rdf4j.model.util.Values.literal;

final class SPARQLConverterTest {

    private static final ZonedDateTime epoch=EPOCH.atZone(ZoneId.of("UTC"));


    @Nested
    final class JSONToRDFTest {

        @Test void testConvertNil() {
            assertThat(rdf(Nil())).isEmpty();
        }

        @Test void testConvertBit() {
            assertThat(rdf(bit(true))).containsExactly(literal(true));
        }

        @Test void testConvertNumber() {
            assertThat(rdf(decimal(BigDecimal.ZERO))).containsExactly(literal(BigDecimal.ZERO));
            assertThat(rdf(integer(BigInteger.ZERO))).containsExactly(literal(BigInteger.ZERO));
            assertThat(rdf(floating(0))).containsExactly(literal(0.0D));
            assertThat(rdf(integral(0))).containsExactly(literal(0L));
        }

        @Test void testConvertString() {
            assertThat(rdf(string(""))).containsExactly(literal(""));
        }

        @Test void testConvertURI() {
            assertThat(rdf(uri(base()))).containsExactly(literal(base().toString(), XSD.ANYURI));
        }

        @Test void testConvertTemporal() {
            assertThat(rdf(Year())).containsExactly(literal(Year.from(epoch)));
            assertThat(rdf(YearMonth())).containsExactly(literal(YearMonth.from(epoch)));
            assertThat(rdf(LocalDate())).containsExactly(literal(LocalDate.from(epoch)));
            assertThat(rdf(LocalTime())).containsExactly(literal(LocalTime.from(epoch)));
            assertThat(rdf(OffsetTime())).containsExactly(literal(OffsetTime.from(epoch)));
            assertThat(rdf(LocalDateTime())).containsExactly(literal(LocalDateTime.from(epoch)));
            assertThat(rdf(OffsetDateTime())).containsExactly(literal(OffsetDateTime.from(epoch)));
            assertThat(rdf(ZonedDateTime())).containsExactly(literal(ZonedDateTime.from(epoch)));
            assertThat(rdf(Instant())).containsExactly(literal(Instant.from(epoch).atOffset(UTC)));
        }

        @Test void testConvertTemporalAmount() {
            assertThat(rdf(Period())).containsExactly(literal(Period.ZERO));
            assertThat(rdf(Duration())).containsExactly(literal(Duration.ZERO));
        }

        @Test void testConvertText() {
            assertThat(rdf(text("en", ""))).containsExactly(literal("", "en"));
        }

        @Test void testConvertData() {
            assertThat(rdf(data(base(), ""))).containsExactly(literal("", iri(base().toString())));
        }

        @Test void testConvertObject() {
            assertThat(rdf(object())).isEmpty();
            assertThat(rdf(object(id(base())))).containsExactly(iri(base().toString()));
        }

        @Test void testConvertArray() {
            assertThat(rdf(array())).isEmpty();
            assertThat(rdf(array(string("x"), integral(0)))).containsExactly(literal("x"), literal(0L));
        }

    }

    @Nested
    final class RDFTOJSONTest {

        @Test void testConvertBit() {
            assertThat(json(set(literal(true)))).isEqualTo(array(bit(true)));
        }

        @Test void testConvertNumber() {
            assertThat(json(set(literal(BigDecimal.ZERO)))).isEqualTo(array(decimal(BigDecimal.ZERO)));
            assertThat(json(set(literal(BigInteger.ZERO)))).isEqualTo(array(integer(BigInteger.ZERO)));
            assertThat(json(set(literal(0.0D)))).isEqualTo(array(floating(0)));
            assertThat(json(set(literal(0L)))).isEqualTo(array(integral(0)));
        }

        @Test void testConvertString() {
            assertThat(json(set(literal("")))).isEqualTo(array(string("")));
        }

        @Test void testConvertURI() {
            assertThat(json(set(literal(base().toString(), XSD.ANYURI)))).isEqualTo(array(uri(base())));
        }

        @Test void testConvertTemporal() {
            assertThat(json(set(literal(Year.from(epoch))))).isEqualTo(array(Year()));
            assertThat(json(set(literal(YearMonth.from(epoch))))).isEqualTo(array(YearMonth()));
            assertThat(json(set(literal(LocalDate.from(epoch))))).isEqualTo(array(LocalDate()));
            assertThat(json(set(literal(LocalTime.from(epoch))))).isEqualTo(array(LocalTime()));
            assertThat(json(set(literal(OffsetTime.from(epoch))))).isEqualTo(array(OffsetTime()));
            assertThat(json(set(literal(LocalDateTime.from(epoch))))).isEqualTo(array(LocalDateTime()));
            assertThat(json(set(literal(OffsetDateTime.from(epoch))))).isEqualTo(array(OffsetDateTime()));
            // ;( ZonedDateTime not supported
            // ;( Instant not supported
        }

        @Test void testConvertTemporalAmount() {
            assertThat(json(set(literal(Period.ZERO)))).isEqualTo(array(Period()));
            assertThat(json(set(literal(Duration.ZERO)))).isEqualTo(array(Duration()));
        }

        @Test void testConvertText() {
            assertThat(json(set(literal("", "en")))).isEqualTo(array(text("en", "")));
        }

        @Test void testConvertData() {
            assertThat(json(set(literal("", iri(base().toString()))))).isEqualTo(array(data(base(), "")));
        }

        @Test void testConvertObject() {
            assertThat(json(set(iri(base().toString())))).isEqualTo(array(object(id(base()))));
        }

        @Test void testConvertArray() {
            assertThat(json(set())).isEqualTo(array());
            assertThat(json(set(literal("x"), literal(0L))).array().map(Set::copyOf))
                    .hasValue(set(string("x"), integral(0)));
        }

    }

}