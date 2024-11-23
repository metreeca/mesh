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

import com.metreeca.mesh.Field;
import com.metreeca.mesh.Value;
import com.metreeca.mesh.queries.Query;
import com.metreeca.mesh.queries.Table;
import com.metreeca.mesh.util.Collections;
import com.metreeca.mesh.util.URIs;

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
import static com.metreeca.mesh.util.Exceptions.error;
import static com.metreeca.mesh.util.URIs.relative;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.*;

/**
 * Frame record.
 */
@FunctionalInterface
public interface Record {

    Value toValue(final boolean model);


    //̸/////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    static Value value(final Object object) {

        if ( object == null ) {
            throw new NullPointerException("null object");
        }

        return object instanceof final Record record
                ? record.toValue(false)
                : error(new IllegalArgumentException(format("not a frame record <%s>", object)));
    }

    static Value model(final Object object) {

        if ( object == null ) {
            throw new NullPointerException("null object");
        }

        return object instanceof final Record record
                ? record.toValue(true)
                : error(new IllegalArgumentException(format("not a frame record <%s>", object)));
    }


    //̸/////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    static URI id(final Value value, final URI base) {

        if ( value == null ) {
            throw new NullPointerException("null value");
        }

        if ( base == null ) {
            throw new NullPointerException("null base");
        }

        return Field.id(value).map(id -> relative(base, id)).orElse(null);
    }

    static String type(final Value value) {

        if ( value == null ) {
            throw new NullPointerException("null value");
        }

        return Field.type(value).orElse(null);
    }


    static boolean _boolean(final Value value) {

        if ( value == null ) {
            throw new NullPointerException("null value");
        }

        return value.bit().orElse(false);
    }

    static byte _byte(final Value value) {

        if ( value == null ) {
            throw new NullPointerException("null value");
        }

        return value.integral().orElse(0L).byteValue();
    }

    static short _short(final Value value) {

        if ( value == null ) {
            throw new NullPointerException("null value");
        }

        return value.integral().orElse(0L).shortValue();
    }

    static int _int(final Value value) {

        if ( value == null ) {
            throw new NullPointerException("null value");
        }

        return value.integral().orElse(0L).intValue();
    }

    static long _long(final Value value) {

        if ( value == null ) {
            throw new NullPointerException("null value");
        }

        return value.integral().orElse(0L);
    }

    static float _float(final Value value) {

        if ( value == null ) {
            throw new NullPointerException("null value");
        }

        return value.floating().orElse(0.0D).floatValue();
    }

    static double _double(final Value value) {

        if ( value == null ) {
            throw new NullPointerException("null value");
        }

        return value.floating().orElse(0.0D);
    }


    static Boolean bit(final Value value) {

        if ( value == null ) {
            throw new NullPointerException("null value");
        }

        return value.bit().orElse(null);
    }

    static Number number(final Value value) {

        if ( value == null ) {
            throw new NullPointerException("null value");
        }

        return value.number().orElse(null);
    }

    static Long integral(final Value value) {

        if ( value == null ) {
            throw new NullPointerException("null value");
        }

        return value.integral().orElse(null);
    }

    static Double floating(final Value value) {

        if ( value == null ) {
            throw new NullPointerException("null value");
        }

        return value.floating().orElse(null);
    }

    static BigInteger integer(final Value value) {

        if ( value == null ) {
            throw new NullPointerException("null value");
        }

        return value.integer().orElse(null);
    }

    static BigDecimal decimal(final Value value) {

        if ( value == null ) {
            throw new NullPointerException("null value");
        }

        return value.decimal().orElse(null);
    }

    static String string(final Value value) {

        if ( value == null ) {
            throw new NullPointerException("null value");
        }

        return value.string().orElse(null);
    }

    static URI uri(final Value value) {

        if ( value == null ) {
            throw new NullPointerException("null value");
        }

        return value.uri().orElse(null);
    }

    static Temporal temporal(final Value value) {

        if ( value == null ) {
            throw new NullPointerException("null value");
        }

        return value.temporal().orElse(null);
    }

    static Year year(final Value value) {

        if ( value == null ) {
            throw new NullPointerException("null value");
        }

        return value.year().orElse(null);
    }

    static YearMonth yearMonth(final Value value) {

        if ( value == null ) {
            throw new NullPointerException("null value");
        }

        return value.yearMonth().orElse(null);
    }

    static LocalDate localDate(final Value value) {

        if ( value == null ) {
            throw new NullPointerException("null value");
        }

        return value.localDate().orElse(null);
    }

    static LocalTime localTime(final Value value) {

        if ( value == null ) {
            throw new NullPointerException("null value");
        }

        return value.localTime().orElse(null);
    }

    static OffsetTime offsetTime(final Value value) {

        if ( value == null ) {
            throw new NullPointerException("null value");
        }

        return value.offsetTime().orElse(null);
    }

    static LocalDateTime localDateTime(final Value value) {

        if ( value == null ) {
            throw new NullPointerException("null value");
        }

        return value.localDateTime().orElse(null);
    }

    static OffsetDateTime offsetDateTime(final Value value) {

        if ( value == null ) {
            throw new NullPointerException("null value");
        }

        return value.offsetDateTime().orElse(null);
    }

    static ZonedDateTime zonedDateTime(final Value value) {

        if ( value == null ) {
            throw new NullPointerException("null value");
        }

        return value.zonedDateTime().orElse(null);
    }

    static Instant instant(final Value value) {

        if ( value == null ) {
            throw new NullPointerException("null value");
        }

        return value.instant().orElse(null);
    }

    static TemporalAmount temporalAmount(final Value value) {

        if ( value == null ) {
            throw new NullPointerException("null value");
        }

        return value.temporalAmount().orElse(null);
    }

    static Period period(final Value value) {

        if ( value == null ) {
            throw new NullPointerException("null value");
        }

        return value.period().orElse(null);
    }

    static Duration duration(final Value value) {

        if ( value == null ) {
            throw new NullPointerException("null value");
        }

        return value.duration().orElse(null);
    }

    static Entry<Locale, String> text(final Value value) {

        if ( value == null ) {
            throw new NullPointerException("null value");
        }

        return value.text().orElse(null);
    }

    static Entry<URI, String> data(final Value value) {

        if ( value == null ) {
            throw new NullPointerException("null value");
        }

        return value.data().orElse(null);
    }

    static <V> V object(final Value value, final Function<Value, V> mapper) {

        if ( value == null ) {
            throw new NullPointerException("null value");
        }

        if ( mapper == null ) {
            throw new NullPointerException("null mapper");
        }

        return value.value()
                .map(mapper)
                .map(v -> requireNonNull(v, "null mapped value"))
                .orElse(null);
    }


    static List<Boolean> bits(final Value value) {

        if ( value == null ) {
            throw new NullPointerException("null value");
        }

        return value.bits().toList();
    }

    static List<Number> numbers(final Value value) {

        if ( value == null ) {
            throw new NullPointerException("null value");
        }

        return value.numbers().toList();
    }

    static List<Long> integrals(final Value value) {

        if ( value == null ) {
            throw new NullPointerException("null value");
        }

        return value.integrals().toList();
    }

    static List<Double> floatings(final Value value) {

        if ( value == null ) {
            throw new NullPointerException("null value");
        }

        return value.floatings().toList();
    }

    static List<BigInteger> integers(final Value value) {

        if ( value == null ) {
            throw new NullPointerException("null value");
        }

        return value.integers().toList();
    }

    static List<BigDecimal> decimals(final Value value) {

        if ( value == null ) {
            throw new NullPointerException("null value");
        }

        return value.decimals().toList();
    }

    static List<String> strings(final Value value) {

        if ( value == null ) {
            throw new NullPointerException("null value");
        }

        return value.strings().toList();
    }

    static List<URI> uris(final Value value) {

        if ( value == null ) {
            throw new NullPointerException("null value");
        }

        return value.uris().toList();
    }

    static List<Temporal> temporals(final Value value) {

        if ( value == null ) {
            throw new NullPointerException("null value");
        }

        return value.temporals().toList();
    }

    static List<Year> years(final Value value) {

        if ( value == null ) {
            throw new NullPointerException("null value");
        }

        return value.years().toList();
    }

    static List<YearMonth> yearMonths(final Value value) {

        if ( value == null ) {
            throw new NullPointerException("null value");
        }

        return value.yearMonths().toList();
    }

    static List<LocalDate> localDates(final Value value) {

        if ( value == null ) {
            throw new NullPointerException("null value");
        }

        return value.localDates().toList();
    }

    static List<LocalTime> localTimes(final Value value) {

        if ( value == null ) {
            throw new NullPointerException("null value");
        }

        return value.localTimes().toList();
    }

    static List<OffsetTime> offsetTimes(final Value value) {

        if ( value == null ) {
            throw new NullPointerException("null value");
        }

        return value.offsetTimes().toList();
    }

    static List<LocalDateTime> localDateTimes(final Value value) {

        if ( value == null ) {
            throw new NullPointerException("null value");
        }

        return value.localDateTimes().toList();
    }

    static List<OffsetDateTime> offsetDateTimes(final Value value) {

        if ( value == null ) {
            throw new NullPointerException("null value");
        }

        return value.offsetDateTimes().toList();
    }

    static List<ZonedDateTime> zonedDateTimes(final Value value) {

        if ( value == null ) {
            throw new NullPointerException("null value");
        }

        return value.zonedDateTimes().toList();
    }

    static List<Instant> instants(final Value value) {

        if ( value == null ) {
            throw new NullPointerException("null value");
        }

        return value.instants().toList();
    }

    static List<TemporalAmount> temporalAmounts(final Value value) {

        if ( value == null ) {
            throw new NullPointerException("null value");
        }

        return value.temporalAmounts().toList();
    }

    static List<Period> periods(final Value value) {

        if ( value == null ) {
            throw new NullPointerException("null value");
        }

        return value.periods().toList();
    }

    static List<Duration> durations(final Value value) {

        if ( value == null ) {
            throw new NullPointerException("null value");
        }

        return value.durations().toList();
    }

    static Map<Locale, String> texts(final Value value) {

        if ( value == null ) {
            throw new NullPointerException("null value");
        }

        return value.texts().collect(toMap(Entry::getKey, Entry::getValue));
    }

    static Map<Locale, Set<String>> textsets(final Value value) {

        if ( value == null ) {
            throw new NullPointerException("null value");
        }

        return value.texts().collect(groupingBy(Entry::getKey, mapping(Entry::getValue, toSet())));
    }

    static Map<URI, String> datas(final Value value) {

        if ( value == null ) {
            throw new NullPointerException("null value");
        }

        return value.datas().collect(toMap(Entry::getKey, Entry::getValue));
    }

    static Map<URI, Set<String>> datasets(final Value value) {

        if ( value == null ) {
            throw new NullPointerException("null value");
        }

        return value.datas().collect(groupingBy(Entry::getKey, mapping(Entry::getValue, toSet())));
    }

    static <V> List<V> objects(final Value value, final Function<Value, V> mapper) {

        if ( value == null ) {
            throw new NullPointerException("null value");
        }

        if ( mapper == null ) {
            throw new NullPointerException("null mapper");
        }

        return value.value(Query.class).map(Record::<V>list)
                .or(() -> value.value(Table.class).map(Record::<V>list))
                .orElseGet(() -> value.values()
                        .map(mapper)
                        .map(v -> requireNonNull(v, "null mapped value"))
                        .toList()
                );
    }


    //̸/////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    static URI id(final URI id, final URI base) {

        if ( base == null ) {
            throw new NullPointerException("null base");
        }

        return Optional.ofNullable(id).map(uri -> URIs.absolute(base, uri)).orElse(URIs.uri());
    }

    static String type(final String type) {
        return Optional.ofNullable(type).orElse("");
    }


    static Value _boolean(final boolean bit) {
        return Optional.of(bit).map(Value::bit).orElse(Nil());
    }

    static Value _byte(final byte number) {
        return Optional.of(number).map(Value::integral).orElse(Nil());
    }

    static Value _short(final short number) {
        return Optional.of(number).map(Value::integral).orElse(Nil());
    }

    static Value _int(final int number) {
        return Optional.of(number).map(Value::integral).orElse(Nil());
    }

    static Value _long(final long number) {
        return Optional.of(number).map(Value::integral).orElse(Nil());
    }

    static Value _float(final float number) {
        return Optional.of(number).map(Value::floating).orElse(Nil());
    }

    static Value _double(final double number) {
        return Optional.of(number).map(Value::floating).orElse(Nil());
    }


    static Value bit(final Boolean bit) {
        return Optional.ofNullable(bit).map(Value::bit).orElse(Nil());
    }

    static Value number(final Number number) {
        return Optional.ofNullable(number).map(Value::number).orElse(Nil());
    }

    static Value integral(final Number number) {
        return Optional.ofNullable(number).map(Value::integral).orElse(Nil());
    }

    static Value floating(final Double floating) {
        return Optional.ofNullable(floating).map(Value::floating).orElse(Nil());
    }

    static Value integer(final BigInteger integer) {
        return Optional.ofNullable(integer).map(Value::integer).orElse(Nil());
    }

    static Value decimal(final BigDecimal decimal) {
        return Optional.ofNullable(decimal).map(Value::decimal).orElse(Nil());
    }

    static Value string(final String string) {
        return Optional.ofNullable(string).map(Value::string).orElse(Nil());
    }

    static Value uri(final URI uRI) {
        return Optional.ofNullable(uRI).map(Value::uri).orElse(Nil());
    }

    static Value temporal(final Temporal temporal) {
        return Optional.ofNullable(temporal).map(Value::temporal).orElse(Nil());
    }

    static Value year(final Year year) {
        return Optional.ofNullable(year).map(Value::year).orElse(Nil());
    }

    static Value yearMonth(final YearMonth yearMonth) {
        return Optional.ofNullable(yearMonth).map(Value::yearMonth).orElse(Nil());
    }

    static Value localDate(final LocalDate localDate) {
        return Optional.ofNullable(localDate).map(Value::localDate).orElse(Nil());
    }

    static Value localTime(final LocalTime localTime) {
        return Optional.ofNullable(localTime).map(Value::localTime).orElse(Nil());
    }

    static Value offsetTime(final OffsetTime offsetTime) {
        return Optional.ofNullable(offsetTime).map(Value::offsetTime).orElse(Nil());
    }

    static Value localDateTime(final LocalDateTime localDateTime) {
        return Optional.ofNullable(localDateTime).map(Value::localDateTime).orElse(Nil());
    }

    static Value offsetDateTime(final OffsetDateTime offsetDateTime) {
        return Optional.ofNullable(offsetDateTime).map(Value::offsetDateTime).orElse(Nil());
    }

    static Value zonedDateTime(final ZonedDateTime zonedDateTime) {
        return Optional.ofNullable(zonedDateTime).map(Value::zonedDateTime).orElse(Nil());
    }

    static Value instant(final Instant instant) {
        return Optional.ofNullable(instant).map(Value::instant).orElse(Nil());
    }

    static Value temporalAmount(final TemporalAmount temporalAmount) {
        return Optional.ofNullable(temporalAmount).map(Value::temporalAmount).orElse(Nil());
    }

    static Value period(final Period period) {
        return Optional.ofNullable(period).map(Value::period).orElse(Nil());
    }

    static Value duration(final Duration duration) {
        return Optional.ofNullable(duration).map(Value::duration).orElse(Nil());
    }

    static Value text(final Entry<Locale, String> text) {
        return Optional.ofNullable(text)
                .map(Value::text)
                .orElseGet(Value::Nil);
    }

    static Value data(final Entry<URI, String> data) {
        return Optional.ofNullable(data)
                .map(Value::data)
                .orElseGet(Value::Nil);
    }

    static <V> Value object(final V object, final Function<V, Value> mapper) {
        return Optional.ofNullable(object)
                .map(mapper)
                .map(v -> requireNonNull(v, "null mapped value"))
                .orElseGet(Value::Nil);
    }


    static Value bits(final Collection<Boolean> bits) {
        return array(Optional.ofNullable(bits).stream()
                .flatMap(Collection::stream)
                .map(Value::bit)
                .toList()
        );
    }

    static Value numbers(final Collection<Number> numbers) {
        return array(Optional.ofNullable(numbers).stream()
                .flatMap(Collection::stream)
                .map(Value::number)
                .toList()
        );
    }

    static Value integrals(final Collection<Long> integrals) {
        return array(Optional.ofNullable(integrals).stream()
                .flatMap(Collection::stream)
                .map(Value::integral)
                .toList()
        );
    }

    static Value floatings(final Collection<Double> floatings) {
        return array(Optional.ofNullable(floatings).stream()
                .flatMap(Collection::stream)
                .map(Value::floating)
                .toList()
        );
    }

    static Value integers(final Collection<BigInteger> integers) {
        return array(Optional.ofNullable(integers).stream()
                .flatMap(Collection::stream)
                .map(Value::integer)
                .toList()
        );
    }

    static Value decimals(final Collection<BigDecimal> decimals) {
        return array(Optional.ofNullable(decimals).stream()
                .flatMap(Collection::stream)
                .map(Value::decimal)
                .toList()
        );
    }

    static Value strings(final Collection<String> strings) {
        return array(Optional.ofNullable(strings).stream()
                .flatMap(Collection::stream)
                .map(Value::string)
                .toList()
        );
    }

    static Value uris(final Collection<URI> uRIs) {
        return array(Optional.ofNullable(uRIs).stream()
                .flatMap(Collection::stream)
                .map(Value::uri)
                .toList()
        );
    }

    static Value temporals(final Collection<Temporal> temporals) {
        return array(Optional.ofNullable(temporals).stream()
                .flatMap(Collection::stream)
                .map(Value::temporal)
                .toList()
        );
    }

    static Value years(final Collection<Year> years) {
        return array(Optional.ofNullable(years).stream()
                .flatMap(Collection::stream)
                .map(Value::year)
                .toList()
        );
    }

    static Value yearMonths(final Collection<YearMonth> yearMonths) {
        return array(Optional.ofNullable(yearMonths).stream()
                .flatMap(Collection::stream)
                .map(Value::yearMonth)
                .toList()
        );
    }

    static Value localDates(final Collection<LocalDate> localDates) {
        return array(Optional.ofNullable(localDates).stream()
                .flatMap(Collection::stream)
                .map(Value::localDate)
                .toList()
        );
    }

    static Value localTimes(final Collection<LocalTime> localTimes) {
        return array(Optional.ofNullable(localTimes).stream()
                .flatMap(Collection::stream)
                .map(Value::localTime)
                .toList()
        );
    }

    static Value offsetTimes(final Collection<OffsetTime> offsetTimes) {
        return array(Optional.ofNullable(offsetTimes).stream()
                .flatMap(Collection::stream)
                .map(Value::offsetTime)
                .toList()
        );
    }

    static Value localDateTimes(final Collection<LocalDateTime> localDateTimes) {
        return array(Optional.ofNullable(localDateTimes).stream()
                .flatMap(Collection::stream)
                .map(Value::localDateTime)
                .toList()
        );
    }

    static Value offsetDateTimes(final Collection<OffsetDateTime> offsetDateTimes) {
        return array(Optional.ofNullable(offsetDateTimes).stream()
                .flatMap(Collection::stream)
                .map(Value::offsetDateTime)
                .toList()
        );
    }

    static Value zonedDateTimes(final Collection<ZonedDateTime> zonedDateTimes) {
        return array(Optional.ofNullable(zonedDateTimes).stream()
                .flatMap(Collection::stream)
                .map(Value::zonedDateTime)
                .toList()
        );
    }

    static Value instants(final Collection<Instant> instants) {
        return array(Optional.ofNullable(instants).stream()
                .flatMap(Collection::stream)
                .map(Value::instant)
                .toList()
        );
    }

    static Value temporalAmounts(final Collection<TemporalAmount> temporalAmounts) {
        return array(Optional.ofNullable(temporalAmounts).stream()
                .flatMap(Collection::stream)
                .map(Value::temporalAmount)
                .toList()
        );
    }

    static Value periods(final Collection<Period> periods) {
        return array(Optional.ofNullable(periods).stream()
                .flatMap(Collection::stream)
                .map(Value::period)
                .toList()
        );
    }

    static Value durations(final Collection<Duration> durations) {
        return array(Optional.ofNullable(durations).stream()
                .flatMap(Collection::stream)
                .map(Value::duration)
                .toList()
        );
    }

    static Value texts(final Map<Locale, String> texts) {
        return array(Optional.ofNullable(texts).stream()
                .map(Map::entrySet)
                .flatMap(Collection::stream)
                .map(Value::text)
                .toList()
        );
    }

    static Value textsets(final Map<Locale, Set<String>> textsets) {
        return array(Optional.ofNullable(textsets).map(Map::entrySet).stream()
                .flatMap(Collection::stream)
                .flatMap(e -> e.getValue().stream()
                        .map(v -> Value.text(e.getKey(), v))
                )
                .toList()
        );
    }

    static Value datas(final Map<URI, String> datas) {
        return array(Optional.ofNullable(datas).stream()
                .map(Map::entrySet)
                .flatMap(Collection::stream)
                .map(Value::data)
                .toList()
        );
    }

    static Value datasets(final Map<URI, Set<String>> datasets) {
        return array(Optional.ofNullable(datasets).map(Map::entrySet).stream()
                .flatMap(Collection::stream)
                .flatMap(e -> e.getValue().stream()
                        .map(v -> Value.data(e.getKey(), v))
                )
                .toList()
        );
    }

    static <V> Value objects(final Collection<V> objects, final Function<V, Value> mapper) {
        if ( objects instanceof Stash<?>(final Object payload) ) { return Value.value(payload); } else {

            return array(Optional.ofNullable(objects).stream()
                    .flatMap(Collection::stream)
                    .map(mapper)
                    .map(v -> requireNonNull(v, "null mapped value"))
                    .toList()
            );

        }
    }


    //̸/////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    static <V> Set<V> set(final Collection<V> items) {

        if ( items == null ) {
            throw new NullPointerException("null items");
        }

        return items instanceof final Stash<V> stash ? stash : Collections.set(items);
    }

    static <V> Set<V> set(final Query query) {

        if ( query == null ) {
            throw new NullPointerException("null query");
        }

        return new Stash<>(query);
    }

    static <V> Set<V> set(final Table table) {

        if ( table == null ) {
            throw new NullPointerException("null table");
        }

        return new Stash<>(table);
    }


    static <V> List<V> list(final Collection<V> items) {

        if ( items == null ) {
            throw new NullPointerException("null items");
        }

        return items instanceof final Stash<V> stash ? stash : Collections.list(items);
    }

    static <V> List<V> list(final Query query) {

        if ( query == null ) {
            throw new NullPointerException("null query");
        }

        return new Stash<>(query);
    }

    static <V> List<V> list(final Table table) {

        if ( table == null ) {
            throw new NullPointerException("null table");
        }

        return new Stash<>(table);
    }


    //̸/////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    static <V> Set<V> ensureImmutable(final Set<V> set, final String name) {
        try { return set == null ? null : Collections.set(set); } catch ( final NullPointerException e ) {

            throw new NullPointerException("nulls in %s".formatted(
                    requireNonNull(name, "null set name")
            ));

        }
    }

    static <V> List<V> ensureImmutable(final List<V> list, final String name) {
        try { return list == null ? null : Collections.list(list); } catch ( final NullPointerException e ) {

            throw new NullPointerException("nulls in %s".formatted(
                    requireNonNull(name, "null list name")
            ));

        }
    }

    static <V> Collection<V> ensureImmutable(final Collection<V> collection, final String name) {
        try { return collection == null ? null : Collections.list(collection); } catch ( final
        NullPointerException e ) {

            throw new NullPointerException("nulls in %s".formatted(
                    requireNonNull(name, "null collection name")
            ));

        }
    }

    static <K, V> Map<K, V> ensureImmutable(final Map<K, V> map, final String name) {
        try { return map == null ? null : Collections.map(map); } catch ( final NullPointerException e ) {

            throw new NullPointerException("nulls in %s".formatted(
                    requireNonNull(name, "null map name")
            ));

        }
    }

    static <K, V> Entry<K, V> ensureImmutable(final Entry<K, V> entry, final String name) {
        try { return entry == null ? null : Collections.entry(entry); } catch ( final NullPointerException e ) {

            throw new NullPointerException("nulls in %s".formatted(
                    requireNonNull(name, "null map name")
            ));

        }
    }

}
