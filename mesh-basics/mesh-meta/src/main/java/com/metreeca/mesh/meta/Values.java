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

package com.metreeca.mesh.meta;

import com.metreeca.mesh.Value;
import com.metreeca.mesh.queries.Query;
import com.metreeca.mesh.queries.Table;
import com.metreeca.shim.Collections;
import com.metreeca.shim.Collections.Stash;
import com.metreeca.shim.URIs;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URI;
import java.time.*;
import java.time.temporal.Temporal;
import java.time.temporal.TemporalAmount;
import java.util.*;
import java.util.Map.Entry;
import java.util.function.Function;

import static com.metreeca.mesh.Value.Nil;
import static com.metreeca.mesh.Value.array;
import static com.metreeca.shim.URIs.relative;

import static java.util.Objects.requireNonNull;
import static java.util.function.Predicate.not;
import static java.util.stream.Collectors.*;

/**
 * Value conversion utilities for frame code generation.
 *
 * <p>This utility class provides runtime support methods for frame classes generated at compile time from
 * {@code @Frame} interfaces. The methods facilitate bidirectional conversion between Java objects and
 * {@link Value} instances, enabling seamless data binding for JSON-LD serialization and deserialization.</p>
 *
 * <p>The conversion methods are organized into several categories:</p>
 *
 * <ul>
 *     <li><strong>Collection Guards</strong> - Handle efficient conversion of collection types with special
 *         optimization for {@code Stash} instances</li>
 *     <li><strong>Object to Value Converters</strong> - Convert Java objects to {@code Value} instances,
 *         handling null values by returning {@code Nil()}</li>
 *     <li><strong>Value to Object Converters</strong> - Extract Java objects from {@code Value} instances,
 *         returning {@code null} for empty values</li>
 * </ul>
 *
 * <p>These methods are typically called by generated frame implementation classes and are not intended
 * for direct use by application code. The code generation process is driven by annotations from the
 * {@link com.metreeca.mesh.meta.jsonld} and {@link com.metreeca.mesh.meta.shacl} packages.</p>
 *
 * <h2>Type Safety</h2>
 *
 * <p>All conversion methods include null-safety checks and type validation. Generic methods use mapper
 * functions to handle custom object types, ensuring type-safe conversions while maintaining flexibility.</p>
 *
 * @see com.metreeca.mesh.Value Value class for data representation
 * @see com.metreeca.mesh.meta.jsonld.Frame Frame annotation for interface marking
 */
public final class Values {

    //̸// Collection Guards ////////////////////////////////////////////////////////////////////////////////////////////

    @SuppressWarnings("unchecked")
    public static <V> Set<V> set(final Collection<? extends V> set) {
        return set instanceof final Stash<?> stash ? (Set<V>)stash : Collections.set(Optional.ofNullable(set).stream()
                .flatMap(Collection::stream)
        );
    }

    @SuppressWarnings("unchecked")
    public static <V> List<V> list(final Collection<? extends V> list) {
        return list instanceof final Stash<?> stash ? (List<V>)stash : Collections.list(Optional.ofNullable(list).stream()
                .flatMap(Collection::stream)
        );
    }

    @SuppressWarnings("unchecked")
    public static <V> Set<? extends V> set_(final Collection<? extends V> set) {
        return set instanceof final Stash<?> stash ? (Set<V>)stash : Collections.set(Optional.ofNullable(set).stream()
                .flatMap(Collection::stream)
        );
    }

    @SuppressWarnings("unchecked")
    public static <V> List<? extends V> list_(final Collection<? extends V> list) {
        return list instanceof final Stash<?> stash ? (List<V>)stash : Collections.list(Optional.ofNullable(list).stream()
                .flatMap(Collection::stream)
        );
    }


    //̸// Object To Value Converters ///////////////////////////////////////////////////////////////////////////////////

    public static Value id(final URI id, final URI base) {

        if ( base == null ) {
            throw new NullPointerException("null base");
        }

        return Optional.ofNullable(id).map(uri -> Value.uri(URIs.absolute(base, uri))).orElseGet(Value::Nil);
    }

    public static Value type(final String type) {
        return Optional.ofNullable(type).map(Value::string).orElseGet(Value::Nil);
    }


    public static Value bit(final Boolean bit) {
        return Optional.ofNullable(bit).map(Value::bit).orElse(Nil());
    }

    public static Value number(final Number number) {
        return Optional.ofNullable(number).map(Value::number).orElse(Nil());
    }

    public static Value integral(final Number number) {
        return Optional.ofNullable(number).map(Value::integral).orElse(Nil());
    }

    public static Value floating(final Double floating) {
        return Optional.ofNullable(floating).map(Value::floating).orElse(Nil());
    }

    public static Value integer(final BigInteger integer) {
        return Optional.ofNullable(integer).map(Value::integer).orElse(Nil());
    }

    public static Value decimal(final BigDecimal decimal) {
        return Optional.ofNullable(decimal).map(Value::decimal).orElse(Nil());
    }

    public static Value string(final String string) {
        return Optional.ofNullable(string).map(Value::string).orElse(Nil());
    }

    public static Value uri(final URI uRI) {
        return Optional.ofNullable(uRI).map(Value::uri).orElse(Nil());
    }

    public static Value temporal(final Temporal temporal) {
        return Optional.ofNullable(temporal).map(Value::temporal).orElse(Nil());
    }

    public static Value year(final Year year) {
        return Optional.ofNullable(year).map(Value::year).orElse(Nil());
    }

    public static Value yearMonth(final YearMonth yearMonth) {
        return Optional.ofNullable(yearMonth).map(Value::yearMonth).orElse(Nil());
    }

    public static Value localDate(final LocalDate localDate) {
        return Optional.ofNullable(localDate).map(Value::localDate).orElse(Nil());
    }

    public static Value localTime(final LocalTime localTime) {
        return Optional.ofNullable(localTime).map(Value::localTime).orElse(Nil());
    }

    public static Value offsetTime(final OffsetTime offsetTime) {
        return Optional.ofNullable(offsetTime).map(Value::offsetTime).orElse(Nil());
    }

    public static Value localDateTime(final LocalDateTime localDateTime) {
        return Optional.ofNullable(localDateTime).map(Value::localDateTime).orElse(Nil());
    }

    public static Value offsetDateTime(final OffsetDateTime offsetDateTime) {
        return Optional.ofNullable(offsetDateTime).map(Value::offsetDateTime).orElse(Nil());
    }

    public static Value zonedDateTime(final ZonedDateTime zonedDateTime) {
        return Optional.ofNullable(zonedDateTime).map(Value::zonedDateTime).orElse(Nil());
    }

    public static Value instant(final Instant instant) {
        return Optional.ofNullable(instant).map(Value::instant).orElse(Nil());
    }

    public static Value temporalAmount(final TemporalAmount temporalAmount) {
        return Optional.ofNullable(temporalAmount).map(Value::temporalAmount).orElse(Nil());
    }

    public static Value period(final Period period) {
        return Optional.ofNullable(period).map(Value::period).orElse(Nil());
    }

    public static Value duration(final Duration duration) {
        return Optional.ofNullable(duration).map(Value::duration).orElse(Nil());
    }

    public static Value text(final Entry<Locale, String> text) {
        return Optional.ofNullable(text)
                .map(Value::text)
                .orElseGet(Value::Nil);
    }

    public static Value data(final Entry<URI, String> data) {
        return Optional.ofNullable(data)
                .map(Value::data)
                .orElseGet(Value::Nil);
    }

    public static <V> Value object(final V object, final Function<V, Value> mapper) {
        return Optional.ofNullable(object)
                .map(mapper)
                .map(v -> requireNonNull(v, "null mapped value"))
                .orElseGet(Value::Nil);
    }

    public static <V extends Enum<V>> Value option(final V option) {
        return Optional.ofNullable(option).map(Enum::name).map(Value::string).orElse(Nil());
    }


    public static Value bits(final Collection<Boolean> bits) {
        if ( bits instanceof final Stash<?> stash ) { return Value.value(stash.payload()); } else {

            return array(Collections.list(Optional.ofNullable(bits).stream()
                    .flatMap(Collection::stream)
                    .map(Value::bit)
            ));

        }
    }

    public static Value numbers(final Collection<Number> numbers) {
        if ( numbers instanceof final Stash<?> stash ) { return Value.value(stash.payload()); } else {

            return array(Collections.list(Optional.ofNullable(numbers).stream()
                    .flatMap(Collection::stream)
                    .map(Value::number)
            ));

        }
    }

    public static Value integrals(final Collection<Long> integrals) {
        if ( integrals instanceof final Stash<?> stash ) { return Value.value(stash.payload()); } else {

            return array(Collections.list(Optional.ofNullable(integrals).stream()
                    .flatMap(Collection::stream)
                    .map(Value::integral)
            ));

        }
    }

    public static Value floatings(final Collection<Double> floatings) {
        if ( floatings instanceof final Stash<?> stash ) { return Value.value(stash.payload()); } else {

            return array(Collections.list(Optional.ofNullable(floatings).stream()
                    .flatMap(Collection::stream)
                    .map(Value::floating)
            ));

        }
    }

    public static Value integers(final Collection<BigInteger> integers) {
        if ( integers instanceof final Stash<?> stash ) { return Value.value(stash.payload()); } else {

            return array(Collections.list(Optional.ofNullable(integers).stream()
                    .flatMap(Collection::stream)
                    .map(Value::integer)
            ));

        }
    }

    public static Value decimals(final Collection<BigDecimal> decimals) {
        if ( decimals instanceof final Stash<?> stash ) { return Value.value(stash.payload()); } else {

            return array(Collections.list(Optional.ofNullable(decimals).stream()
                    .flatMap(Collection::stream)
                    .map(Value::decimal)
            ));

        }
    }

    public static Value strings(final Collection<String> strings) {
        if ( strings instanceof final Stash<?> stash ) { return Value.value(stash.payload()); } else {

            return array(Collections.list(Optional.ofNullable(strings).stream()
                    .flatMap(Collection::stream)
                    .map(Value::string)
            ));

        }
    }

    public static Value uris(final Collection<URI> uris) {
        if ( uris instanceof final Stash<?> stash ) { return Value.value(stash.payload()); } else {

            return array(Collections.list(Optional.ofNullable(uris).stream()
                    .flatMap(Collection::stream)
                    .map(Value::uri)
            ));

        }
    }

    public static Value temporals(final Collection<Temporal> temporals) {
        if ( temporals instanceof final Stash<?> stash ) { return Value.value(stash.payload()); } else {

            return array(Collections.list(Optional.ofNullable(temporals).stream()
                    .flatMap(Collection::stream)
                    .map(Value::temporal)
            ));

        }
    }

    public static Value years(final Collection<Year> years) {
        if ( years instanceof final Stash<?> stash ) { return Value.value(stash.payload()); } else {

            return array(Collections.list(Optional.ofNullable(years).stream()
                    .flatMap(Collection::stream)
                    .map(Value::year)
            ));

        }
    }

    public static Value yearMonths(final Collection<YearMonth> yearMonths) {
        if ( yearMonths instanceof final Stash<?> stash ) { return Value.value(stash.payload()); } else {

            return array(Collections.list(Optional.ofNullable(yearMonths).stream()
                    .flatMap(Collection::stream)
                    .map(Value::yearMonth)
            ));

        }
    }

    public static Value localDates(final Collection<LocalDate> localDates) {
        if ( localDates instanceof final Stash<?> stash ) { return Value.value(stash.payload()); } else {

            return array(Collections.list(Optional.ofNullable(localDates).stream()
                    .flatMap(Collection::stream)
                    .map(Value::localDate)
            ));

        }
    }

    public static Value localTimes(final Collection<LocalTime> localTimes) {
        if ( localTimes instanceof final Stash<?> stash ) { return Value.value(stash.payload()); } else {

            return array(Collections.list(Optional.ofNullable(localTimes).stream()
                    .flatMap(Collection::stream)
                    .map(Value::localTime)
            ));

        }
    }

    public static Value offsetTimes(final Collection<OffsetTime> offsetTimes) {
        if ( offsetTimes instanceof final Stash<?> stash ) { return Value.value(stash.payload()); } else {

            return array(Collections.list(Optional.ofNullable(offsetTimes).stream()
                    .flatMap(Collection::stream)
                    .map(Value::offsetTime)
            ));

        }
    }

    public static Value localDateTimes(final Collection<LocalDateTime> localDateTimes) {
        if ( localDateTimes instanceof final Stash<?> stash ) { return Value.value(stash.payload()); } else {

            return array(Collections.list(Optional.ofNullable(localDateTimes).stream()
                    .flatMap(Collection::stream)
                    .map(Value::localDateTime)
            ));

        }
    }

    public static Value offsetDateTimes(final Collection<OffsetDateTime> offsetDateTimes) {
        if ( offsetDateTimes instanceof final Stash<?> stash ) { return Value.value(stash.payload()); } else {

            return array(Collections.list(Optional.ofNullable(offsetDateTimes).stream()
                    .flatMap(Collection::stream)
                    .map(Value::offsetDateTime)
            ));

        }
    }

    public static Value zonedDateTimes(final Collection<ZonedDateTime> zonedDateTimes) {
        if ( zonedDateTimes instanceof final Stash<?> stash ) { return Value.value(stash.payload()); } else {

            return array(Collections.list(Optional.ofNullable(zonedDateTimes).stream()
                    .flatMap(Collection::stream)
                    .map(Value::zonedDateTime)
            ));

        }
    }

    public static Value instants(final Collection<Instant> instants) {
        if ( instants instanceof final Stash<?> stash ) { return Value.value(stash.payload()); } else {

            return array(Collections.list(Optional.ofNullable(instants).stream()
                    .flatMap(Collection::stream)
                    .map(Value::instant)
            ));

        }
    }

    public static Value temporalAmounts(final Collection<TemporalAmount> temporalAmounts) {
        if ( temporalAmounts instanceof final Stash<?> stash ) { return Value.value(stash.payload()); } else {

            return array(Collections.list(Optional.ofNullable(temporalAmounts).stream()
                    .flatMap(Collection::stream)
                    .map(Value::temporalAmount)
            ));

        }
    }

    public static Value periods(final Collection<Period> periods) {
        if ( periods instanceof final Stash<?> stash ) { return Value.value(stash.payload()); } else {

            return array(Collections.list(Optional.ofNullable(periods).stream()
                    .flatMap(Collection::stream)
                    .map(Value::period)
            ));

        }
    }

    public static Value durations(final Collection<Duration> durations) {
        if ( durations instanceof final Stash<?> stash ) { return Value.value(stash.payload()); } else {

            return array(Collections.list(Optional.ofNullable(durations).stream()
                    .flatMap(Collection::stream)
                    .map(Value::duration)
            ));

        }
    }

    public static Value texts(final Map<Locale, String> texts) {
        return array(Collections.list(Optional.ofNullable(texts).stream()
                .map(Map::entrySet)
                .flatMap(Collection::stream)
                .map(Value::text)
        ));
    }

    public static Value textsets(final Map<Locale, Set<String>> textsets) {
        return array(Collections.list(Optional.ofNullable(textsets).map(Map::entrySet).stream()
                .flatMap(Collection::stream)
                .flatMap(e -> e.getValue().stream()
                        .map(v -> Value.text(e.getKey(), v))
                )
        ));
    }

    public static Value datas(final Map<URI, String> datas) {
        return array(Collections.list(Optional.ofNullable(datas).stream()
                .map(Map::entrySet)
                .flatMap(Collection::stream)
                .map(Value::data)
        ));
    }

    public static Value datasets(final Map<URI, Set<String>> datasets) {
        return array(Collections.list(Optional.ofNullable(datasets).map(Map::entrySet).stream()
                .flatMap(Collection::stream)
                .flatMap(e -> e.getValue().stream()
                        .map(v -> Value.data(e.getKey(), v))
                )
        ));
    }

    public static <V> Value objects(final Collection<V> objects, final Function<V, Value> mapper) {
        if ( objects instanceof final Stash<?> stash ) { return Value.value(stash.payload()); } else {

            return array(Collections.list(Optional.ofNullable(objects).stream()
                    .flatMap(Collection::stream)
                    .map(mapper)
                    .map(v -> requireNonNull(v, "null mapped value"))
                    .filter(not(Value::isEmpty))
            ));

        }
    }

    public static <V extends Enum<V>> Value options(final Collection<V> options) {
        if ( options instanceof final Stash<?> stash ) { return Value.value(stash.payload()); } else {

            return array(Collections.list(Optional.ofNullable(options).stream()
                    .flatMap(Collection::stream)
                    .map(Enum::name)
                    .map(Value::string)
            ));

        }
    }


    //̸// Value To Object Converters ///////////////////////////////////////////////////////////////////////////////////

    public static URI id(final Value value, final URI base) {

        if ( value == null ) {
            throw new NullPointerException("null value");
        }

        if ( base == null ) {
            throw new NullPointerException("null base");
        }

        return value.id().map(id -> relative(base, id)).orElse(null);
    }

    public static String type(final Value value) {

        if ( value == null ) {
            throw new NullPointerException("null value");
        }

        return value.type().orElse(null);
    }


    public static Boolean bit(final Value value) {

        if ( value == null ) {
            throw new NullPointerException("null value");
        }

        return value.bit().orElse(null);
    }

    public static Number number(final Value value) {

        if ( value == null ) {
            throw new NullPointerException("null value");
        }

        return value.number().orElse(null);
    }

    public static Byte _byte(final Value value) {

        if ( value == null ) {
            throw new NullPointerException("null value");
        }

        return value.integral().map(Long::byteValue).orElse(null);
    }

    public static Short _short(final Value value) {

        if ( value == null ) {
            throw new NullPointerException("null value");
        }

        return value.integral().map(Long::shortValue).orElse(null);
    }

    public static Integer _int(final Value value) {

        if ( value == null ) {
            throw new NullPointerException("null value");
        }

        return value.integral().map(Long::intValue).orElse(null);
    }

    public static Long _long(final Value value) {

        if ( value == null ) {
            throw new NullPointerException("null value");
        }

        return value.integral().map(Long::longValue).orElse(null);
    }

    public static Float _float(final Value value) {

        if ( value == null ) {
            throw new NullPointerException("null value");
        }

        return value.floating().map(Double::floatValue).orElse(null);
    }

    public static Double _double(final Value value) {

        if ( value == null ) {
            throw new NullPointerException("null value");
        }

        return value.floating().map(Double::doubleValue).orElse(null);
    }

    public static BigInteger integer(final Value value) {

        if ( value == null ) {
            throw new NullPointerException("null value");
        }

        return value.integer().orElse(null);
    }

    public static BigDecimal decimal(final Value value) {

        if ( value == null ) {
            throw new NullPointerException("null value");
        }

        return value.decimal().orElse(null);
    }

    public static String string(final Value value) {

        if ( value == null ) {
            throw new NullPointerException("null value");
        }

        return value.string().orElse(null);
    }

    public static URI uri(final Value value) {

        if ( value == null ) {
            throw new NullPointerException("null value");
        }

        return value.uri().orElse(null);
    }

    public static Temporal temporal(final Value value) {

        if ( value == null ) {
            throw new NullPointerException("null value");
        }

        return value.temporal().orElse(null);
    }

    public static Year year(final Value value) {

        if ( value == null ) {
            throw new NullPointerException("null value");
        }

        return value.year().orElse(null);
    }

    public static YearMonth yearMonth(final Value value) {

        if ( value == null ) {
            throw new NullPointerException("null value");
        }

        return value.yearMonth().orElse(null);
    }

    public static LocalDate localDate(final Value value) {

        if ( value == null ) {
            throw new NullPointerException("null value");
        }

        return value.localDate().orElse(null);
    }

    public static LocalTime localTime(final Value value) {

        if ( value == null ) {
            throw new NullPointerException("null value");
        }

        return value.localTime().orElse(null);
    }

    public static OffsetTime offsetTime(final Value value) {

        if ( value == null ) {
            throw new NullPointerException("null value");
        }

        return value.offsetTime().orElse(null);
    }

    public static LocalDateTime localDateTime(final Value value) {

        if ( value == null ) {
            throw new NullPointerException("null value");
        }

        return value.localDateTime().orElse(null);
    }

    public static OffsetDateTime offsetDateTime(final Value value) {

        if ( value == null ) {
            throw new NullPointerException("null value");
        }

        return value.offsetDateTime().orElse(null);
    }

    public static ZonedDateTime zonedDateTime(final Value value) {

        if ( value == null ) {
            throw new NullPointerException("null value");
        }

        return value.zonedDateTime().orElse(null);
    }

    public static Instant instant(final Value value) {

        if ( value == null ) {
            throw new NullPointerException("null value");
        }

        return value.instant().orElse(null);
    }

    public static TemporalAmount temporalAmount(final Value value) {

        if ( value == null ) {
            throw new NullPointerException("null value");
        }

        return value.temporalAmount().orElse(null);
    }

    public static Period period(final Value value) {

        if ( value == null ) {
            throw new NullPointerException("null value");
        }

        return value.period().orElse(null);
    }

    public static Duration duration(final Value value) {

        if ( value == null ) {
            throw new NullPointerException("null value");
        }

        return value.duration().orElse(null);
    }

    public static Entry<Locale, String> text(final Value value) {

        if ( value == null ) {
            throw new NullPointerException("null value");
        }

        return value.text().orElse(null);
    }

    public static Entry<URI, String> data(final Value value) {

        if ( value == null ) {
            throw new NullPointerException("null value");
        }

        return value.data().orElse(null);
    }

    public static <V> V object(final Value value, final Function<Value, V> mapper) {

        if ( value == null ) {
            throw new NullPointerException("null value");
        }

        if ( mapper == null ) {
            throw new NullPointerException("null mapper");
        }

        return value.value()
                .filter(not(Value::isEmpty))
                .map(mapper)
                .map(v -> requireNonNull(v, "null mapped value"))
                .orElse(null);
    }

    public static <T extends Enum<T>> T option(final Value value, final Class<T> clazz) {

        if ( value == null ) {
            throw new NullPointerException("null value");
        }

        return value.string()
                .map(s -> Enum.valueOf(clazz, s))
                .orElse(null);
    }


    public static List<Boolean> bits(final Value value) {

        if ( value == null ) {
            throw new NullPointerException("null value");
        }

        return value.bits().toList();
    }

    public static List<Number> numbers(final Value value) {

        if ( value == null ) {
            throw new NullPointerException("null value");
        }

        return value.numbers().toList();
    }

    public static List<Long> integrals(final Value value) {

        if ( value == null ) {
            throw new NullPointerException("null value");
        }

        return value.integrals().toList();
    }

    public static List<Double> floatings(final Value value) {

        if ( value == null ) {
            throw new NullPointerException("null value");
        }

        return value.floatings().toList();
    }

    public static List<BigInteger> integers(final Value value) {

        if ( value == null ) {
            throw new NullPointerException("null value");
        }

        return value.integers().toList();
    }

    public static List<BigDecimal> decimals(final Value value) {

        if ( value == null ) {
            throw new NullPointerException("null value");
        }

        return value.decimals().toList();
    }

    public static List<String> strings(final Value value) {

        if ( value == null ) {
            throw new NullPointerException("null value");
        }

        return value.strings().toList();
    }

    public static List<URI> uris(final Value value) {

        if ( value == null ) {
            throw new NullPointerException("null value");
        }

        return value.uris().toList();
    }

    public static List<Temporal> temporals(final Value value) {

        if ( value == null ) {
            throw new NullPointerException("null value");
        }

        return value.temporals().toList();
    }

    public static List<Year> years(final Value value) {

        if ( value == null ) {
            throw new NullPointerException("null value");
        }

        return value.years().toList();
    }

    public static List<YearMonth> yearMonths(final Value value) {

        if ( value == null ) {
            throw new NullPointerException("null value");
        }

        return value.yearMonths().toList();
    }

    public static List<LocalDate> localDates(final Value value) {

        if ( value == null ) {
            throw new NullPointerException("null value");
        }

        return value.localDates().toList();
    }

    public static List<LocalTime> localTimes(final Value value) {

        if ( value == null ) {
            throw new NullPointerException("null value");
        }

        return value.localTimes().toList();
    }

    public static List<OffsetTime> offsetTimes(final Value value) {

        if ( value == null ) {
            throw new NullPointerException("null value");
        }

        return value.offsetTimes().toList();
    }

    public static List<LocalDateTime> localDateTimes(final Value value) {

        if ( value == null ) {
            throw new NullPointerException("null value");
        }

        return value.localDateTimes().toList();
    }

    public static List<OffsetDateTime> offsetDateTimes(final Value value) {

        if ( value == null ) {
            throw new NullPointerException("null value");
        }

        return value.offsetDateTimes().toList();
    }

    public static List<ZonedDateTime> zonedDateTimes(final Value value) {

        if ( value == null ) {
            throw new NullPointerException("null value");
        }

        return value.zonedDateTimes().toList();
    }

    public static List<Instant> instants(final Value value) {

        if ( value == null ) {
            throw new NullPointerException("null value");
        }

        return value.instants().toList();
    }

    public static List<TemporalAmount> temporalAmounts(final Value value) {

        if ( value == null ) {
            throw new NullPointerException("null value");
        }

        return value.temporalAmounts().toList();
    }

    public static List<Period> periods(final Value value) {

        if ( value == null ) {
            throw new NullPointerException("null value");
        }

        return value.periods().toList();
    }

    public static List<Duration> durations(final Value value) {

        if ( value == null ) {
            throw new NullPointerException("null value");
        }

        return value.durations().toList();
    }

    public static Map<Locale, String> texts(final Value value) {

        if ( value == null ) {
            throw new NullPointerException("null value");
        }

        return value.texts().collect(toMap(Entry::getKey, Entry::getValue));
    }

    public static Map<Locale, Set<String>> textsets(final Value value) {

        if ( value == null ) {
            throw new NullPointerException("null value");
        }

        return value.texts().collect(groupingBy(Entry::getKey, mapping(Entry::getValue, toSet())));
    }

    public static Map<URI, String> datas(final Value value) {

        if ( value == null ) {
            throw new NullPointerException("null value");
        }

        return value.datas().collect(toMap(Entry::getKey, Entry::getValue));
    }

    public static Map<URI, Set<String>> datasets(final Value value) {

        if ( value == null ) {
            throw new NullPointerException("null value");
        }

        return value.datas().collect(groupingBy(Entry::getKey, mapping(Entry::getValue, toSet())));
    }

    public static <T> List<T> objects(final Value value, final Function<Value, T> mapper) {

        if ( value == null ) {
            throw new NullPointerException("null value");
        }

        if ( mapper == null ) {
            throw new NullPointerException("null mapper");
        }

        return value.value(Query.class).map(query -> (List<T>)Collections.<T>stash(query))
                .or(() -> value.value(Table.class).map(table -> (List<T>)Collections.<T>stash(table)))
                .orElseGet(() -> value.values()
                        .filter(not(Value::isEmpty))
                        .map(mapper)
                        .map(t -> requireNonNull(t, "null mapped value"))
                        .toList()
                );
    }

    public static <T extends Enum<T>> Set<T> options(final Value value, final Class<T> clazz) {

        if ( value == null ) {
            throw new NullPointerException("null value");
        }

        return Collections.set(value.strings()
                .map(s -> Enum.valueOf(clazz, s))
        );
    }


    //̸/////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private Values() { }

}
