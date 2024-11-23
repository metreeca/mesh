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

import java.net.URI;
import java.time.*;
import java.util.List;

import static com.metreeca.mesh.Value.*;
import static com.metreeca.mesh.ValueComparator.compare;
import static com.metreeca.shim.Collections.list;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

final class ValueComparatorTest {

    private int normalize(final int v) {
        return Integer.compare(v, 0);
    }


    @Test void testCompareBits() {

        final List<Value> values=list(bit(false), bit(true));

        for (int i=0; i < values.size(); i++) {
            for (int j=0; j < values.size(); j++) {

                assertThat(normalize(compare(values.get(i), values.get(j))))
                        .as("%s @%d / %s @%d".formatted(values.get(i), i, values.get(j), j))
                        .isEqualTo(normalize(Integer.compare(i, j)));

            }
        }

    }

    @Test void testCompareNumbers() {

        final List<Value> one=list(integral(1), floating(1.0), integer(1), decimal(1));
        final List<Value> two=list(integral(2), floating(2.0), integer(2), decimal(2));

        for (int i=0; i < one.size(); i++) {
            for (int j=0; j < one.size(); j++) {

                assertThat(normalize(compare(one.get(i), one.get(j))))
                        .as("%s @%d / %s @%d".formatted(one.get(i), i, one.get(j), j))
                        .isEqualTo(0);

            }
        }

        for (int i=0; i < one.size(); i++) {
            for (int j=0; j < two.size(); j++) {

                assertThat(normalize(compare(one.get(i), two.get(j))))
                        .as("%s @%d / %s @%d".formatted(one.get(i), i, two.get(j), j))
                        .isEqualTo(-1);

            }
        }

        for (int i=0; i < two.size(); i++) {
            for (int j=0; j < one.size(); j++) {

                assertThat(normalize(compare(two.get(i), one.get(j))))
                        .as("%s @%d / %s @%d".formatted(two.get(i), i, one.get(j), j))
                        .isEqualTo(1);

            }
        }

    }

    @Test void testCompareStrings() {

        final List<Value> values=list(string("due"), string("tre"), string("uno"));

        for (int i=0; i < values.size(); i++) {
            for (int j=0; j < values.size(); j++) {

                assertThat(normalize(compare(values.get(i), values.get(j))))
                        .as("%s @%d / %s @%d".formatted(values.get(i), i, values.get(j), j))
                        .isEqualTo(normalize(Integer.compare(i, j)));

            }
        }

    }

    @Test void testCompareURI() {

        final List<Value> values=list(uri(URI.create("due")), uri(URI.create("tre")), uri(URI.create("uno")));

        for (int i=0; i < values.size(); i++) {
            for (int j=0; j < values.size(); j++) {

                assertThat(normalize(compare(values.get(i), values.get(j))))
                        .as("%s @%d / %s @%d".formatted(values.get(i), i, values.get(j), j))
                        .isEqualTo(normalize(Integer.compare(i, j)));

            }
        }

    }

    @Test void testCompareYear() {

        final List<Value> values=list(
                year(Year.parse("2024")),
                year(Year.parse("2025")),
                year(Year.parse("2026"))
        );

        for (int i=0; i < values.size(); i++) {
            for (int j=0; j < values.size(); j++) {

                assertThat(normalize(compare(values.get(i), values.get(j))))
                        .as("%s @%d / %s @%d".formatted(values.get(i), i, values.get(j), j))
                        .isEqualTo(normalize(Integer.compare(i, j)));

            }
        }

    }

    @Test void testCompareYearMonth() {

        final List<Value> values=list(
                yearMonth(YearMonth.parse("2024-01")),
                yearMonth(YearMonth.parse("2024-02")),
                yearMonth(YearMonth.parse("2025-01"))
        );

        for (int i=0; i < values.size(); i++) {
            for (int j=0; j < values.size(); j++) {

                assertThat(normalize(compare(values.get(i), values.get(j))))
                        .as("%s @%d / %s @%d".formatted(values.get(i), i, values.get(j), j))
                        .isEqualTo(normalize(Integer.compare(i, j)));

            }
        }

    }

    @Test void testCompareLocalDate() {

        final List<Value> values=list(
                localDate(LocalDate.parse("2024-12-01")),
                localDate(LocalDate.parse("2024-12-02")),
                localDate(LocalDate.parse("2024-12-03"))
        );

        for (int i=0; i < values.size(); i++) {
            for (int j=0; j < values.size(); j++) {

                assertThat(normalize(compare(values.get(i), values.get(j))))
                        .as("%s @%d / %s @%d".formatted(values.get(i), i, values.get(j), j))
                        .isEqualTo(normalize(Integer.compare(i, j)));

            }
        }

    }

    @Test void testCompareLocalTime() {

        final List<Value> values=list(
                localTime(LocalTime.parse("20:00")),
                localTime(LocalTime.parse("20:01")),
                localTime(LocalTime.parse("20:02"))
        );

        for (int i=0; i < values.size(); i++) {
            for (int j=0; j < values.size(); j++) {

                assertThat(normalize(compare(values.get(i), values.get(j))))
                        .as("%s @%d / %s @%d".formatted(values.get(i), i, values.get(j), j))
                        .isEqualTo(normalize(Integer.compare(i, j)));

            }
        }

    }

    @Test void testCompareOffsetTime() {

        final List<Value> values=list(
                offsetTime(OffsetTime.parse("20:00+01:00")),
                offsetTime(OffsetTime.parse("20:01+01:00")),
                offsetTime(OffsetTime.parse("20:02+01:00"))
        );

        for (int i=0; i < values.size(); i++) {
            for (int j=0; j < values.size(); j++) {

                assertThat(normalize(compare(values.get(i), values.get(j))))
                        .as("%s @%d / %s @%d".formatted(values.get(i), i, values.get(j), j))
                        .isEqualTo(normalize(Integer.compare(i, j)));

            }
        }

    }

    @Test void testCompareLocalDateTime() {

        final List<Value> values=list(
                localDateTime(LocalDateTime.parse("2024-12-01T20:00")),
                localDateTime(LocalDateTime.parse("2024-12-01T20:01")),
                localDateTime(LocalDateTime.parse("2024-12-01T20:02"))
        );

        for (int i=0; i < values.size(); i++) {
            for (int j=0; j < values.size(); j++) {

                assertThat(normalize(compare(values.get(i), values.get(j))))
                        .as("%s @%d / %s @%d".formatted(values.get(i), i, values.get(j), j))
                        .isEqualTo(normalize(Integer.compare(i, j)));

            }
        }

    }

    @Test void testCompareOffsetDateTime() {

        final List<Value> values=list(
                offsetDateTime(OffsetDateTime.parse("2024-12-01T20:00+01:00")),
                offsetDateTime(OffsetDateTime.parse("2024-12-01T20:01+01:00")),
                offsetDateTime(OffsetDateTime.parse("2024-12-01T20:02+01:00"))
        );

        for (int i=0; i < values.size(); i++) {
            for (int j=0; j < values.size(); j++) {

                assertThat(normalize(compare(values.get(i), values.get(j))))
                        .as("%s @%d / %s @%d".formatted(values.get(i), i, values.get(j), j))
                        .isEqualTo(normalize(Integer.compare(i, j)));

            }
        }

    }

    @Test void testCompareZonedDateTime() {

        final List<Value> values=list(
                zonedDateTime(ZonedDateTime.parse("2024-12-12T17:34:36.337601Z")),
                zonedDateTime(ZonedDateTime.parse("2024-12-12T18:35:38.526784+01:00[Europe/Rome]")),
                zonedDateTime(ZonedDateTime.parse("2024-12-12T18:36:19.586667+01:00[CET]"))
        );

        for (int i=0; i < values.size(); i++) {
            for (int j=0; j < values.size(); j++) {

                assertThat(normalize(compare(values.get(i), values.get(j))))
                        .as("%s @%d / %s @%d".formatted(values.get(i), i, values.get(j), j))
                        .isEqualTo(normalize(Integer.compare(i, j)));

            }
        }

    }

    @Test void testCompareInstant() {

        final List<Value> values=list(
                instant(Instant.parse("2024-12-01T20:00:00.000Z")),
                instant(Instant.parse("2024-12-01T20:01:00.000Z")),
                instant(Instant.parse("2024-12-01T20:02:00.000Z"))
        );

        for (int i=0; i < values.size(); i++) {
            for (int j=0; j < values.size(); j++) {

                assertThat(normalize(compare(values.get(i), values.get(j))))
                        .as("%s @%d / %s @%d".formatted(values.get(i), i, values.get(j), j))
                        .isEqualTo(normalize(Integer.compare(i, j)));

            }
        }

    }

    @Test void testComparePeriod() {

        final List<Value> values=list(
                period(Period.parse("P1D")),
                period(Period.parse("P2D")),
                period(Period.parse("P3D"))
        );

        for (int i=0; i < values.size(); i++) {
            for (int j=0; j < values.size(); j++) {

                assertThat(normalize(compare(values.get(i), values.get(j))))
                        .as("%s @%d / %s @%d".formatted(values.get(i), i, values.get(j), j))
                        .isEqualTo(normalize(Integer.compare(i, j)));

            }
        }

    }

    @Test void testCompareDuration() {

        final List<Value> values=list(
                duration(Duration.parse("PT1S")),
                duration(Duration.parse("PT2S")),
                duration(Duration.parse("PT3S"))
        );

        for (int i=0; i < values.size(); i++) {
            for (int j=0; j < values.size(); j++) {

                assertThat(normalize(compare(values.get(i), values.get(j))))
                        .as("%s @%d / %s @%d".formatted(values.get(i), i, values.get(j), j))
                        .isEqualTo(normalize(Integer.compare(i, j)));

            }
        }

    }


    @Test void testReportIncompatibleValues() {
        assertThatIllegalArgumentException().isThrownBy(() ->
                compare(integer(0), string(""))
        );
    }

    @Test void testReportIncomparableValues() {
        assertThatIllegalArgumentException().isThrownBy(() ->
                compare(Array(), Array())
        );
    }

}