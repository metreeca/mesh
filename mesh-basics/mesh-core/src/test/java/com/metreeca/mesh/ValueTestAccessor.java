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

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URI;
import java.time.*;

import static com.metreeca.mesh.Value.*;

import static org.assertj.core.api.Assertions.assertThat;

@Nested
final class ValueTestAccessor {

    @Nested
    final class GetTest {

        private static final Value array=array(string("x"), string("y"));
        private static final Value object=object(field("x", string("1")), field("y", string("2")));
        private static final Value literal=string("x");


        @Test void testWildcard() {

            assertThat(literal.get()).isEqualTo(Nil());

            assertThat(object.get()).isEqualTo(array(string("1"), string("2")));

            assertThat(array.get()).isEqualTo(array);
        }

        @Test void testIndex() {

            assertThat(literal.get(0)).isEqualTo(Nil());

            assertThat(object.get(0)).isEqualTo(Nil());

            assertThat(array.get(0)).isEqualTo(literal);
            assertThat(array.get(-1)).isEqualTo(string("y"));
            assertThat(array.get(10)).isEqualTo(Nil());
            assertThat(array.get(-10)).isEqualTo(Nil());

        }

        @Test void testName() {

            assertThat(literal.get("x")).isEqualTo(Nil());

            assertThat(object.get("x")).isEqualTo(string("1"));
            assertThat(object.get("z")).isEqualTo(Nil());

            assertThat(array.get("x")).isEqualTo(Nil());

        }

    }


    @Nested
    final class NumberTest {

        @Test void testCastToIntegral() {
            assertThat(integral(1).integral()).contains(1L);
            assertThat(floating(1).integral()).contains(1L);
            assertThat(integer(1).integral()).contains(1L);
            assertThat(decimal(1).integral()).contains(1L);
        }

        @Test void testCastToFloating() {
            assertThat(integral(1).floating()).contains(1.0);
            assertThat(floating(1).floating()).contains(1.0);
            assertThat(integer(1).floating()).contains(1.0);
            assertThat(decimal(1).floating()).contains(1.0);
        }

        @Test void testCastToInteger() {
            assertThat(integral(1).integer()).contains(BigInteger.ONE);
            assertThat(floating(1).integer()).contains(BigInteger.ONE);
            assertThat(integer(1).integer()).contains(BigInteger.ONE);
            assertThat(decimal(1).integer()).contains(BigInteger.ONE);
        }

        @Test void testCastToDecimal() {
            assertThat(integral(1).decimal()).hasValueSatisfying(v -> assertThat(v).isEqualByComparingTo(BigDecimal.ONE));
            assertThat(floating(1).decimal()).hasValueSatisfying(v -> assertThat(v).isEqualByComparingTo(BigDecimal.ONE));
            assertThat(integer(1).decimal()).hasValueSatisfying(v -> assertThat(v).isEqualByComparingTo(BigDecimal.ONE));
            assertThat(decimal(1).decimal()).hasValueSatisfying(v -> assertThat(v).isEqualByComparingTo(BigDecimal.ONE));
        }

    }

    @Nested
    final class StringTest {

        @Test void testCastToURI() {
            assertThat(string("http://example.org").uri()).contains(URI.create("http://example.org"));
        }

        @Test void testCastToTemporal() {
            assertThat(string("2023").temporal()).contains(Year.parse("2023"));
            assertThat(string("2023-06").temporal()).contains(YearMonth.parse("2023-06"));
            assertThat(string("2023-06-15").temporal()).contains(LocalDate.parse("2023-06-15"));
            assertThat(string("12:30:45").temporal()).contains(LocalTime.parse("12:30:45"));
            assertThat(string("12:30:45+01:00").temporal()).contains(OffsetTime.parse("12:30:45+01:00"));
            assertThat(string("2023-06-15T12:30:45").temporal()).contains(LocalDateTime.parse("2023-06-15T12:30:45"));
            assertThat(string("2023-06-15T12:30:45+01:00").temporal()).contains(OffsetDateTime.parse("2023-06-15T12:30:45+01:00"));
            assertThat(string("2023-06-15T12:30:45+01:00[Europe/Paris]").temporal()).contains(ZonedDateTime.parse("2023-06-15T12:30:45+01:00[Europe/Paris]"));
            // ;( Instant not supported (parsed as OffsetDateTime)
        }

        @Test void testCastToYear() {
            assertThat(string("2023").year()).contains(Year.parse("2023"));
        }

        @Test void testCastToYearMonth() {
            assertThat(string("2023-06").yearMonth()).contains(YearMonth.parse("2023-06"));
        }

        @Test void testCastToLocalDate() {
            assertThat(string("2023-06-15").localDate()).contains(LocalDate.parse("2023-06-15"));
        }

        @Test void testCastToLocalTime() {
            assertThat(string("12:30:45").localTime()).contains(LocalTime.parse("12:30:45"));
        }

        @Test void testCastToOffsetTime() {
            assertThat(string("12:30:45+01:00").offsetTime()).contains(
                    OffsetTime.parse("12:30:45+01:00")
            );
        }

        @Test void testCastToLocalDateTime() {
            assertThat(string("2023-06-15T12:30:45").localDateTime()).contains(
                    LocalDateTime.parse("2023-06-15T12:30:45")
            );
        }

        @Test void testCastToOffsetDateTime() {
            assertThat(string("2023-06-15T12:30:45+01:00").offsetDateTime()).contains(
                    OffsetDateTime.parse("2023-06-15T12:30:45+01:00")
            );
        }

        @Test void testCastToZonedDateTime() {
            assertThat(string("2023-06-15T12:30:45+01:00[Europe/Paris]").zonedDateTime()).contains(
                    ZonedDateTime.parse("2023-06-15T12:30:45+01:00[Europe/Paris]")
            );
        }

        @Test void testCastToInstant() {
            assertThat(string("2023-06-15T12:30:45Z").instant()).contains(
                    Instant.parse("2023-06-15T12:30:45Z")
            );
        }

        @Test void testCastToTemporalAmount() {
            assertThat(string("P1Y2M3D").temporalAmount()).contains(Period.parse("P1Y2M3D"));
            assertThat(string("PT1H2M3S").temporalAmount()).contains(Duration.parse("PT1H2M3S"));
        }

        @Test void testCastToPeriod() {
            assertThat(string("P1Y2M3D").period()).contains(Period.parse("P1Y2M3D"));
        }

        @Test void testCastToDuration() {
            assertThat(string("PT1H2M3S").duration()).contains(Duration.parse("PT1H2M3S"));
        }

    }

    @Nested
    final class LiteralTest {

        @Test void testCastURIToString() {
            assertThat(uri(URI.create("http://example.org")).string()).contains("http://example.org");
        }

        @Test void testCastYearToString() {
            assertThat(year(Year.parse("2023")).string()).contains("2023");
        }

        @Test void testCastYearMonthToString() {
            assertThat(yearMonth(YearMonth.parse("2023-06")).string()).contains("2023-06");
        }

        @Test void testCastLocalDateToString() {
            assertThat(localDate(LocalDate.parse("2023-06-15")).string()).contains("2023-06-15");
        }

        @Test void testCastLocalTimeToString() {
            assertThat(localTime(LocalTime.parse("12:30:45")).string()).contains("12:30:45");
        }

        @Test void testCastOffsetTimeToString() {
            assertThat(offsetTime(OffsetTime.parse("12:30:45+01:00")).string())
                    .contains("12:30:45+01:00");
        }

        @Test void testCastLocalDateTimeToString() {
            assertThat(localDateTime(LocalDateTime.parse("2023-06-15T12:30:45")).string())
                    .contains("2023-06-15T12:30:45");
        }

        @Test void testCastOffsetDateTimeToString() {
            assertThat(offsetDateTime(OffsetDateTime.parse("2023-06-15T12:30:45+01:00")).string())
                    .contains("2023-06-15T12:30:45+01:00");
        }

        @Test void testCastZonedDateTimeToString() {
            assertThat(zonedDateTime(ZonedDateTime.parse("2023-06-15T12:30:45+01:00[Europe/Paris]")).string())
                    .hasValueSatisfying(v -> assertThat(v)
                            .startsWith("2023-06-15T") // partial check due to variable timezone offset
                    );
        }

        @Test void testCastInstantToString() {
            assertThat(instant(Instant.parse("2023-06-15T12:30:45Z")).string())
                    .contains("2023-06-15T12:30:45Z");
        }

        @Test void testCastPeriodToString() {
            assertThat(period(Period.parse("P1Y2M3D")).string()).contains("P1Y2M3D");
        }

        @Test void testCastDurationToString() {
            assertThat(duration(Duration.parse("PT1H2M3S")).string())
                    .contains("PT1H2M3S");
        }

    }

}
