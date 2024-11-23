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

import org.junit.jupiter.api.Test;

import java.time.*;
import java.time.format.DateTimeFormatter;

import static com.metreeca.mesh.Value.*;

import static org.assertj.core.api.Assertions.assertThat;

final class ValueTestFactories {

    @Test void testConvertParsedTemporalAccessors() {

        assertThat(temporal(DateTimeFormatter.ofPattern("yyyy").parse("2025")))
                .isEqualTo(year(Year.of(2025)));

        assertThat(temporal(DateTimeFormatter.ofPattern("yyyy").parse("2025")))
                .isEqualTo(year(Year.of(2025)));

        assertThat(temporal(DateTimeFormatter.ofPattern("yyyy-MM").parse("2025-04")))
                .isEqualTo(yearMonth(YearMonth.of(2025, 4)));

        assertThat(temporal(DateTimeFormatter.ofPattern("yyyy-MM-dd").parse("2025-04-10")))
                .isEqualTo(localDate(LocalDate.of(2025, 4, 10)));

        assertThat(temporal(DateTimeFormatter.ofPattern("HH:mm:ss").parse("13:45:00")))
                .isEqualTo(localTime(LocalTime.of(13, 45, 0)));

        assertThat(temporal(DateTimeFormatter.ofPattern("HH:mm:ssXXX").parse("13:45:00+02:00")))
                .isEqualTo(offsetTime(OffsetTime.of(13, 45, 0, 0, ZoneOffset.ofHours(2))));

        assertThat(temporal(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").parse("2025-04-10 13:45:00")))
                .isEqualTo(localDateTime(LocalDateTime.of(2025, 4, 10, 13, 45, 0)));

        assertThat(temporal(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ssXXX").parse("2025-04-10 13:45:00+02:00")))
                .isEqualTo(offsetDateTime(OffsetDateTime.of(2025, 4, 10, 13, 45, 0, 0, ZoneOffset.ofHours(2))));

        assertThat(temporal(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss VV")
                .withZone(ZoneId.of("Europe/Paris")).parse("2025-04-10 13:45:00 Europe/Paris")))
                .isEqualTo(zonedDateTime(ZonedDateTime.of(2025, 4, 10, 13, 45, 0, 0, ZoneId.of("Europe/Paris"))));

        assertThat(temporal(DateTimeFormatter.ISO_INSTANT.parse("2025-04-10T11:45:00Z")))
                .isEqualTo(instant(Instant.parse("2025-04-10T11:45:00Z")));

    }

    @Test void testConvertParsedTemporalAmounts() {

        assertThat(temporalAmount(Duration.parse("PT2H30M")))
                .isEqualTo(duration(Duration.ofHours(2).plusMinutes(30)));

        assertThat(temporalAmount(Period.parse("P3Y6M4D")))
                .isEqualTo(period(Period.of(3, 6, 4)));

        assertThat(temporalAmount(Duration.parse("PT45S")))
                .isEqualTo(duration(Duration.ofSeconds(45)));

        assertThat(temporalAmount(Period.parse("P1M")))
                .isEqualTo(period(Period.ofMonths(1)));

        assertThat(temporalAmount(Duration.parse("PT0.5S")))
                .isEqualTo(duration(Duration.ofMillis(500)));

    }

}