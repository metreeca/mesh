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

import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URI;
import java.time.*;
import java.time.temporal.Temporal;
import java.time.temporal.TemporalAmount;
import java.util.Optional;

import static java.lang.String.format;

/**
 * Value comparison utilities.
 *
 * <p>Provides methods to check comparability and perform comparisons between {@linkplain Value} instances.
 * Supports comparison of scalar types including numbers, strings, URIs, temporal values, and temporal amounts.</p>
 */
final class ValueComparator {

    /**
     * Checks if a value supports comparison operations.
     *
     * @param value the value to check
     *
     * @return {@code true} if the value is comparable; {@code false} otherwise
     *
     * @throws NullPointerException if {@code value} is {@code null}
     */
    static Boolean comparable(final Value value) {
        return value.accept(new Value.Visitor<>() {

            @Override public Boolean visit(final Value host, final Object object) { return false; }

            @Override public Boolean visit(final Value host, final Void nil) { return true; }

            @Override public Boolean visit(final Value host, final Boolean bit) { return true; }

            @Override public Boolean visit(final Value host, final Number number) { return true; }

            @Override public Boolean visit(final Value host, final String string) { return true; }

            @Override public Boolean visit(final Value host, final URI uri) { return true; }

            @Override public Boolean visit(final Value host, final Temporal temporal) { return true; }

            @Override public Boolean visit(final Value host, final TemporalAmount amount) { return true; }

        });
    }

    /**
     * Checks if two values are comparable to each other.
     *
     * @param x the first value
     * @param y the second value
     *
     * @return {@code true} if the values are comparable; {@code false} otherwise
     *
     * @throws NullPointerException if either {@code x} or {@code y} is {@code null}
     */
    static boolean comparable(final Value x, final Value y) {
        return cmp(x, y).isPresent();
    }

    /**
     * Compares two values.
     *
     * @param x the first value
     * @param y the second value
     *
     * @return a negative integer if {@code x} is less than {@code y}, zero if they are equal, or a positive integer
     *         if {@code x} is greater than {@code y}
     *
     * @throws NullPointerException     if either {@code x} or {@code y} is {@code null}
     * @throws IllegalArgumentException if the values are not comparable
     */
    static int compare(final Value x, final Value y) {
        return cmp(x, y).orElseThrow(() -> new IllegalArgumentException(format(
                "incomparable values <%s> / <%s>", x, y
        )));
    }

    private static Optional<Integer> cmp(final Value x, final Value y) { // !!! review
        return Optional.ofNullable(x.accept(new Value.Visitor<>() {

            @Override public Integer visit(final Value host, final Void nil) {
                return compare(nil, y);
            }

            @Override public Integer visit(final Value host, final Boolean bit) {
                return compare(bit, y);
            }

            @Override public Integer visit(final Value host, final Long integral) {
                return compare(integral, y);
            }

            @Override public Integer visit(final Value host, final Double floating) {
                return compare(floating, y);
            }

            @Override public Integer visit(final Value host, final BigInteger integer) {
                return compare(integer, y);
            }

            @Override public Integer visit(final Value host, final BigDecimal decimal) {
                return compare(decimal, y);
            }

            @Override public Integer visit(final Value host, final String string) {
                return compare(string, y);
            }

            @Override public Integer visit(final Value host, final URI uri) {
                return compare(uri, y);
            }

            @Override public Integer visit(final Value host, final Year year) {
                return compare(year, y);
            }

            @Override public Integer visit(final Value host, final YearMonth yearMonth) {
                return compare(yearMonth, y);
            }

            @Override public Integer visit(final Value host, final LocalDate localDate) {
                return compare(localDate, y);
            }

            @Override public Integer visit(final Value host, final LocalTime localTime) {
                return compare(localTime, y);
            }

            @Override public Integer visit(final Value host, final OffsetTime offsetTime) {
                return compare(offsetTime, y);
            }

            @Override public Integer visit(final Value host, final LocalDateTime localDateTime) {
                return compare(localDateTime, y);
            }

            @Override public Integer visit(final Value host, final OffsetDateTime offsetDateTime) {
                return compare(offsetDateTime, y);
            }

            @Override public Integer visit(final Value host, final ZonedDateTime zonedDateTime) {
                return compare(zonedDateTime, y);
            }

            @Override public Integer visit(final Value host, final Instant instant) {
                return compare(instant, y);
            }

            @Override public Integer visit(final Value host, final Period period) {
                return compare(period, y);
            }

            @Override public Integer visit(final Value host, final Duration duration) {
                return compare(duration, y);
            }

        }));
    }


    //̸/////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private static Integer compare(final Void x, final Value y) {
        return y.accept(new Value.Visitor<>() {

            @Override public Integer visit(final Value host, final Void nil) { return 0; }

        });
    }

    private static Integer compare(final Boolean x, final Value y) {
        return y.accept(new Value.Visitor<>() {

            @Override public Integer visit(final Value host, final Boolean bit) { return Boolean.compare(x, bit); }

        });
    }

    private static Integer compare(final Long x, final Value y) {
        return y.accept(new Value.Visitor<>() {

            @Override public Integer visit(final Value host, final Long integral) {
                return Long.compare(x, integral);
            }

            @Override public Integer visit(final Value host, final Double floating) {
                return Double.compare(x, floating);
            }

            @Override public Integer visit(final Value host, final BigInteger integer) {
                return BigInteger.valueOf(x).compareTo(integer);
            }

            @Override public Integer visit(final Value host, final BigDecimal decimal) {
                return BigDecimal.valueOf(x).compareTo(decimal);
            }

        });
    }

    private static Integer compare(final Double x, final Value y) {
        return y.accept(new Value.Visitor<>() {

            @Override public Integer visit(final Value host, final Long integral) {
                return Double.compare(x, integral);
            }

            @Override public Integer visit(final Value host, final Double floating) {
                return Double.compare(x, floating);
            }

            @Override public Integer visit(final Value host, final BigInteger integer) {
                return BigDecimal.valueOf(x).compareTo(new BigDecimal(integer));
            }

            @Override public Integer visit(final Value host, final BigDecimal decimal) {
                return BigDecimal.valueOf(x).compareTo(decimal);
            }

        });
    }

    private static Integer compare(final BigInteger x, final Value y) {
        return y.accept(new Value.Visitor<>() {

            @Override public Integer visit(final Value host, final Long integral) {
                return x.compareTo(BigInteger.valueOf(integral));
            }

            @Override public Integer visit(final Value host, final Double floating) {
                return new BigDecimal(x).compareTo(BigDecimal.valueOf(floating));
            }

            @Override public Integer visit(final Value host, final BigInteger integer) {
                return x.compareTo(integer);
            }

            @Override public Integer visit(final Value host, final BigDecimal decimal) {
                return new BigDecimal(x).compareTo(decimal);
            }

        });
    }

    private static Integer compare(final BigDecimal x, final Value y) {
        return y.accept(new Value.Visitor<>() {

            @Override public Integer visit(final Value host, final Long integral) {
                return x.compareTo(BigDecimal.valueOf(integral));
            }

            @Override public Integer visit(final Value host, final Double floating) {
                return x.compareTo(BigDecimal.valueOf(floating));
            }

            @Override public Integer visit(final Value host, final BigInteger integer) {
                return x.compareTo(new BigDecimal(integer));
            }

            @Override public Integer visit(final Value host, final BigDecimal decimal) {
                return x.compareTo(decimal);
            }

        });
    }

    private static Integer compare(final String x, final Value y) {
        return y.accept(new Value.Visitor<>() {

            @Override public Integer visit(final Value host, final String string) {
                return x.compareTo(string);
            }

        });
    }

    private static Integer compare(final URI x, final Value y) {
        return y.accept(new Value.Visitor<>() {

            @Override public Integer visit(final Value host, final URI uri) {
                return x.compareTo(uri);
            }

        });
    }

    private static Integer compare(final Year x, final Value y) {
        return y.accept(new Value.Visitor<>() {

            @Override public Integer visit(final Value host, final Year year) {
                return x.compareTo(year);
            }

        });
    }

    private static Integer compare(final YearMonth x, final Value y) {
        return y.accept(new Value.Visitor<>() {

            @Override public Integer visit(final Value host, final YearMonth yearMonth) {
                return x.compareTo(yearMonth);
            }

        });
    }

    private static Integer compare(final LocalDate x, final Value y) {
        return y.accept(new Value.Visitor<>() {

            @Override public Integer visit(final Value host, final LocalDate localDate) {
                return x.compareTo(localDate);
            }

        });
    }

    private static Integer compare(final LocalTime x, final Value y) {
        return y.accept(new Value.Visitor<>() {

            @Override public Integer visit(final Value host, final LocalTime localTime) {
                return x.compareTo(localTime);
            }

        });
    }

    private static Integer compare(final OffsetTime x, final Value y) {
        return y.accept(new Value.Visitor<>() {

            @Override public Integer visit(final Value host, final OffsetTime offsetTime) {
                return x.compareTo(offsetTime);
            }

        });
    }

    private static Integer compare(final LocalDateTime x, final Value y) {
        return y.accept(new Value.Visitor<>() {

            @Override public Integer visit(final Value host, final LocalDateTime localDateTime) {
                return x.compareTo(localDateTime);
            }

        });
    }

    private static Integer compare(final OffsetDateTime x, final Value y) {
        return y.accept(new Value.Visitor<>() {

            @Override public Integer visit(final Value host, final OffsetDateTime offsetDateTime) {
                return x.compareTo(offsetDateTime);
            }

        });
    }

    private static Integer compare(final ZonedDateTime x, final Value y) {
        return y.accept(new Value.Visitor<>() {

            @Override public Integer visit(final Value host, final ZonedDateTime zonedDateTime) {
                return x.compareTo(zonedDateTime);
            }

        });
    }

    private static Integer compare(final Instant x, final Value y) {
        return y.accept(new Value.Visitor<>() {

            @Override public Integer visit(final Value host, final Instant instant) {
                return x.compareTo(instant);
            }

        });
    }

    private static Integer compare(final Period x, final Value y) {
        return y.accept(new Value.Visitor<>() {

            @Override public Integer visit(final Value host, final Period period) {
                return LocalDate.ofEpochDay(0).plus(x).compareTo(LocalDate.ofEpochDay(0).plus(period));
            }

        });
    }

    private static Integer compare(final Duration x, final Value y) {
        return y.accept(new Value.Visitor<>() {

            @Override public Integer visit(final Value host, final Duration duration) {
                return x.compareTo(duration);
            }

        });
    }


    //̸/////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private ValueComparator() { }

}
