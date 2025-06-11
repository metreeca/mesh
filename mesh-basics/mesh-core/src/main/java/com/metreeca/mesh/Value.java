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

import com.metreeca.mesh.queries.Table;
import com.metreeca.mesh.queries.Tuple;
import com.metreeca.mesh.shapes.Shape;
import com.metreeca.mesh.tools.CodecException;
import com.metreeca.shim.Locales;
import com.metreeca.shim.URIs;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URI;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.*;
import java.time.format.DateTimeParseException;
import java.time.temporal.*;
import java.util.*;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static com.metreeca.shim.Collections.*;
import static com.metreeca.shim.Exceptions.error;
import static com.metreeca.shim.Exceptions.unsupported;
import static com.metreeca.shim.URIs.item;

import static java.lang.Math.max;
import static java.lang.String.format;
import static java.math.RoundingMode.UNNECESSARY;
import static java.time.Instant.EPOCH;
import static java.util.Arrays.asList;
import static java.util.Locale.ROOT;
import static java.util.Objects.requireNonNull;
import static java.util.function.Predicate.not;
import static java.util.stream.Collectors.*;

/**
 * Immutable JSON-LD value representation.
 *
 * <p>Represents JSON-LD values including literals, objects, arrays, and URIs with
 * type-safe access methods and semantic web compatibility.</p>
 */
@SuppressWarnings("MethodNamesDifferingOnlyByCase")
public abstract class Value implements Valuable {

    public static final String ID="@id";
    public static final String TYPE="@type";
    public static final String VALUE="@value";
    public static final String LANGUAGE="@language";
    public static final String CONTEXT="@context";

    public static final Value FALSE=bit(false);
    public static final Value TRUE=bit(true);


    private static final Visitor<Boolean> EMPTY_VISITOR=new Visitor<>() {

        private static final Set<String> IGNORED_FIELDS=set(CONTEXT);


        @Override public Boolean visit(final Value host, final Void nil) {
            return true;
        }

        @Override public Boolean visit(final Value host, final List<Value> values) {
            return values.isEmpty();
        }

        @Override public Boolean visit(final Value host, final Map<String, Value> fields) {
            return fields.isEmpty() || IGNORED_FIELDS.containsAll(fields.keySet());
        }

        @Override public Boolean visit(final Value host, final Object object) {
            return object instanceof Table(final List<Tuple> rows) && rows.isEmpty();
        }

    };

    private static final Visitor<Value> WILDCARD_VISITOR=new Visitor<>() {

        @Override public Value visit(final Value host, final List<Value> values) {
            return host;
        }

        @Override public Value visit(final Value host, final Map<String, Value> fields) {
            return array(fields.values());
        }

        @Override public Value visit(final Value host, final Object object) {
            return Nil();
        }

    };


    public static boolean isReserved(final String name) {
        return name.startsWith("@");
    }


    public static Entry<String, Value> id(final String id) {
        return field(ID, uri(item(id)));
    }

    public static Entry<String, Value> id(final URI id) {

        if ( id == null ) {
            throw new NullPointerException("null id");
        }

        if ( !id.equals(URIs.uri()) && !id.isAbsolute() ) {
            throw new IllegalArgumentException(format("relative id <%s>", id));
        }

        return field(ID, uri(id));
    }

    public static Entry<String, Value> type(final String type) {

        if ( type == null ) {
            throw new NullPointerException("null type");
        }

        return field(TYPE, type.isEmpty() ? Nil() : string(type));
    }

    public static Entry<String, Value> shape(final Shape shape) {

        if ( shape == null ) {
            throw new NullPointerException("null shape");
        }

        return field(CONTEXT, value(shape));
    }


    //̸// !!! Review ///////////////////////////////////////////////////////////////////////////////////////////////////

    public static boolean comparable(final Value value) {

        if ( value == null ) {
            throw new NullPointerException("null value");
        }

        return ValueComparator.comparable(value);
    }

    public static int compare(final Value x, final Value y) {

        if ( x == null ) {
            throw new NullPointerException("null x value");
        }

        if ( y == null ) {
            throw new NullPointerException("null y value");
        }

        return ValueComparator.compare(x, y);
    }

    public static Optional<Integer> compare(final Optional<Value> x, final Optional<Value> y) {
        return x.flatMap(xv -> y.map(yv -> compare(xv, yv)));
    }


    public static IllegalArgumentException unknown(final String name) {

        if ( name == null ) {
            throw new NullPointerException("null name");
        }

        return new IllegalArgumentException(format(
                "unknown property <%s>", name
        ));
    }

    public static CodecException malformed(final Value model, final String value) {

        if ( model == null ) {
            throw new NullPointerException("null model");
        }

        if ( value == null ) {
            throw new NullPointerException("null value");
        }

        return new CodecException(format(
                "malformed <%s> value <%s>", model, value
        ));
    }


    //̸/////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public static Value Nil() {
        return NilValue.MODEL;
    }

    public static Value Bit() { return BitValue.MODEL; }

    public static Value Number() {
        return NumberValue.MODEL;
    }

    public static Value Integral() { return IntegralValue.MODEL; }

    public static Value Floating() { return FloatingValue.MODEL; }

    public static Value Integer() { return IntegerValue.MODEL; }

    public static Value Decimal() { return DecimalValue.MODEL; }

    public static Value String() { return StringValue.MODEL; }

    public static Value URI() { return URIValue.MODEL; }

    public static Value Temporal() {
        return TemporalValue.MODEL;
    }

    public static Value Year() { return YearValue.MODEL; }

    public static Value YearMonth() { return YearMonthValue.MODEL; }

    public static Value LocalDate() { return LocalDateValue.MODEL; }

    public static Value LocalTime() { return LocalTimeValue.MODEL; }

    public static Value OffsetTime() { return OffsetTimeValue.MODEL; }

    public static Value LocalDateTime() { return LocalDateTimeValue.MODEL; }

    public static Value OffsetDateTime() { return OffsetDateTimeValue.MODEL; }

    public static Value ZonedDateTime() { return ZonedDateTimeValue.MODEL; }

    public static Value Instant() { return InstantValue.MODEL; }

    public static Value TemporalAmount() {
        return TemporalAmountValue.MODEL;
    }

    public static Value Period() { return PeriodValue.MODEL; }

    public static Value Duration() { return DurationValue.MODEL; }

    public static Value Text() { return TextValue.MODEL; }

    public static Value Data() { return DataValue.MODEL; }

    public static Value Object() { return ObjectValue.MODEL; }

    public static Value Array() { return ArrayValue.MODEL; }


    public static Value nil(final Void ignored) {
        return new NilValue();
    }

    public static Value bit(final boolean bit) {
        return new BitValue(bit);
    }

    public static Value number(final Number number) {

        if ( number == null ) {
            throw new NullPointerException("null number");
        }

        return number instanceof final BigDecimal decimal ? decimal(decimal)
                : number instanceof final BigInteger integer ? integer(integer)
                : number instanceof final Double floating ? floating(floating)
                : number instanceof final Float floating ? floating(floating.doubleValue())
                : integral(number.longValue());
    }

    public static Value integral(final long integral) {
        return new IntegralValue(integral);
    }

    public static Value integral(final Number number) {

        if ( number == null ) {
            throw new NullPointerException("null number");
        }

        return new IntegralValue(number.longValue());
    }

    public static Value floating(final double floating) {

        if ( !Double.isFinite(floating) ) {
            throw new IllegalArgumentException(format("non-finite floating value <%f>", floating));
        }

        return new FloatingValue(floating);
    }

    public static Value floating(final Number number) {

        if ( number == null ) {
            throw new NullPointerException("null number");
        }

        if ( !Double.isFinite(number.doubleValue()) ) {
            throw new IllegalArgumentException(format("non-finite floating value <%f>", number.doubleValue()));
        }

        return new FloatingValue(number.doubleValue());
    }

    public static Value integer(final long integer) {
        return integer(java.math.BigInteger.valueOf(integer));
    }

    public static Value integer(final Number number) {

        if ( number == null ) {
            throw new NullPointerException("null number");
        }

        return new IntegerValue(number instanceof final BigInteger integer ? integer
                : number instanceof final BigDecimal decimal ? decimal.toBigInteger()
                : BigInteger.valueOf(number.longValue())
        );
    }

    public static Value decimal(final double decimal) {
        return decimal(java.math.BigDecimal.valueOf(decimal));
    }

    public static Value decimal(final Number number) {

        if ( number == null ) {
            throw new NullPointerException("null number");
        }

        return new DecimalValue(number instanceof final BigInteger integer ? new BigDecimal(integer)
                : number instanceof final BigDecimal decimal ? decimal
                : number instanceof final Long integral ? BigDecimal.valueOf(integral.longValue())
                : BigDecimal.valueOf(number.longValue())
        );
    }

    public static Value string(final String string) {

        if ( string == null ) {
            throw new NullPointerException("null string");
        }

        return new StringValue(string);
    }

    public static Value uri(final URI uri) {

        if ( uri == null ) {
            throw new NullPointerException("null uri");
        }

        return new URIValue(uri);
    }

    public static Value temporal(final TemporalAccessor accessor) {

        if ( accessor == null ) {
            throw new NullPointerException("null accessor");
        }

        return TemporalValue.create(accessor);
    }

    public static Value year(final Year year) {

        if ( year == null ) {
            throw new NullPointerException("null year");
        }

        return new YearValue(year);
    }

    public static Value yearMonth(final YearMonth yearMonth) {

        if ( yearMonth == null ) {
            throw new NullPointerException("null yearMonth");
        }

        return new YearMonthValue(yearMonth);
    }

    public static Value localDate(final LocalDate localDate) {

        if ( localDate == null ) {
            throw new NullPointerException("null localDate");
        }

        return new LocalDateValue(localDate);
    }

    public static Value localTime(final LocalTime localTime) {

        if ( localTime == null ) {
            throw new NullPointerException("null localTime");
        }

        return new LocalTimeValue(localTime);
    }

    public static Value offsetTime(final OffsetTime offsetTime) {

        if ( offsetTime == null ) {
            throw new NullPointerException("null offsetTime");
        }

        return new OffsetTimeValue(offsetTime);
    }

    public static Value localDateTime(final LocalDateTime localDateTime) {

        if ( localDateTime == null ) {
            throw new NullPointerException("null localDateTime");
        }

        return new LocalDateTimeValue(localDateTime);
    }

    public static Value offsetDateTime(final OffsetDateTime offsetDateTime) {

        if ( offsetDateTime == null ) {
            throw new NullPointerException("null offsetDateTime");
        }

        return new OffsetDateTimeValue(offsetDateTime);
    }

    public static Value zonedDateTime(final ZonedDateTime zonedDateTime) {

        if ( zonedDateTime == null ) {
            throw new NullPointerException("null zonedDateTime");
        }

        return new ZonedDateTimeValue(zonedDateTime);
    }

    public static Value instant(final Instant instant) {

        if ( instant == null ) {
            throw new NullPointerException("null instant");
        }

        return new InstantValue(instant);
    }

    public static Value temporalAmount(final TemporalAmount amount) {

        if ( amount == null ) {
            throw new NullPointerException("null amount");
        }

        return TemporalAmountValue.create(amount);
    }

    public static Value period(final Period period) {

        if ( period == null ) {
            throw new NullPointerException("null period");
        }

        return new PeriodValue(period);
    }

    public static Value duration(final Duration duration) {

        if ( duration == null ) {
            throw new NullPointerException("null duration");
        }

        return new DurationValue(duration);
    }

    public static Value text(final String string) {

        if ( string == null ) {
            throw new NullPointerException("null text");
        }

        return text(ROOT, string);
    }

    public static Value text(final Entry<Locale, String> text) {

        if ( text == null ) {
            throw new NullPointerException("null text");
        }

        return text(text.getKey(), text.getValue());
    }

    public static Value text(final String locale, final String string) {

        if ( string == null ) {
            throw new NullPointerException("null string");
        }

        if ( locale == null ) {
            throw new NullPointerException("null locale");
        }

        return text(Locales.locale(locale), string);
    }

    public static Value text(final Locale locale, final String string) {

        if ( string == null ) {
            throw new NullPointerException("null string");
        }

        if ( locale == null ) {
            throw new NullPointerException("null locale");
        }

        return new TextValue(string, locale);
    }

    public static Value data(final Entry<URI, String> data) {

        if ( data == null ) {
            throw new NullPointerException("null data");
        }

        return data(data.getKey(), data.getValue());
    }

    public static Value data(final String datatype, final String string) {

        if ( string == null ) {
            throw new NullPointerException("null string");
        }

        if ( datatype == null ) {
            throw new NullPointerException("null datatype");
        }

        return data(URI.create(datatype), string);
    }

    public static Value data(final URI datatype, final String string) {

        if ( string == null ) {
            throw new NullPointerException("null string");
        }

        if ( datatype == null ) {
            throw new NullPointerException("null datatype");
        }

        if ( !datatype.isAbsolute() ) {
            throw new IllegalArgumentException(format("relative datatype <%s>", datatype));
        }

        return new DataValue(string, datatype);
    }

    public static Value object(final Map<String, Value> fields) {

        if ( fields == null ) {
            throw new NullPointerException("null fields");
        }

        return object(fields.entrySet());
    }

    @SafeVarargs
    public static Value object(final Entry<String, Value>... fields) {

        if ( fields == null ) {
            throw new NullPointerException("null fields");
        }

        return object(asList(fields));
    }

    public static Value object(final Stream<Entry<String, Value>> fields) {

        if ( fields == null ) {
            throw new NullPointerException("null fields");
        }

        return object(fields.toList());
    }

    public static Value object(final Collection<Entry<String, Value>> fields) {

        if ( fields == null || fields.stream().anyMatch(Objects::isNull) ) {
            throw new NullPointerException("null fields");
        }

        return new ObjectValue(fields.stream().collect(groupingBy(

                Entry::getKey,
                LinkedHashMap::new,

                collectingAndThen(
                        reducing(null, (x, y) ->
                                x == null ? y : error(new IllegalArgumentException(format(
                                        "conflicting <%s> field definitions", x.getKey()
                                )))
                        ),
                        Entry::getValue
                )

        )));
    }

    public static Value array(final Valuable... values) {

        if ( values == null ) {
            throw new NullPointerException("null values");
        }

        return array(asList(values));
    }

    public static Value array(final Stream<? extends Valuable> values) {

        if ( values == null ) {
            throw new NullPointerException("null values");
        }

        return array(values.toList());
    }

    public static Value array(final Collection<? extends Valuable> values) {

        if ( values == null || values.stream().anyMatch(Objects::isNull) ) {
            throw new NullPointerException("null values");
        }

        return new ArrayValue(list(values.stream().map(Valuable::toValue)));
    }

    public static Value value(final Object object) {

        if ( object == null ) {
            throw new NullPointerException("null object");
        }

        return new GenericValue(object);
    }


    public static Entry<String, Value> field(final String name, final Valuable value) {

        if ( name == null ) {
            throw new NullPointerException("null name");
        }

        if ( value == null ) {
            throw new NullPointerException("null value");
        }

        return entry(name, requireNonNull(value.toValue(), "null supplied value"));
    }


    //̸/////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private Value() { }


    public boolean isEmpty() {
        return accept(EMPTY_VISITOR);
    }


    public Optional<URI> id() {
        return object()
                .map(fields -> fields.get(ID))
                .flatMap(Value::uri)
                .filter(URI::isAbsolute);
    }

    public Optional<String> type() {
        return object()
                .map(fields -> fields.get(TYPE))
                .flatMap(Value::string);
    }

    public Optional<Shape> shape() {
        return object()
                .map(fields -> fields.get(CONTEXT))
                .flatMap(v -> v.value(Shape.class));
    }


    /**
     * Retrieves nested values from object and array values.
     *
     * @return if this value is an array, this value; if this value is an object, an array containing the field values;
     *         otherwise, the {@code Nil()} value
     */
    public Value get() {
        return accept(WILDCARD_VISITOR);
    }

    /**
     * Retrieves the value at the specified index from array values.
     *
     * @param index the index of the array element to retrieve; negative indexes count from the end of the array (-1
     *              refers to the last element, -2 to the second-to-last, and so on)
     *
     * @return the value at the specified index if this value is an array and the index is valid (either 0 &lt;= index
     *         &lt; array.size(), or -array.size() &lt;= index &lt; 0); otherwise, the {@code Nil()} value
     */
    public Value get(final int index) {
        return array().map(values -> {

            final int size=values.size();
            final int normalizedIndex=index < 0 ? size+index : index;

            return (normalizedIndex >= 0 && normalizedIndex < size) ? values.get(normalizedIndex) : null;

        }).orElse(NilValue.MODEL);
    }

    /**
     * Retrieves the value associated with the specified field name from object values.
     *
     * @param name the name of the field to retrieve
     *
     * @return the value associated with the specified field name, if this value is an object and the field exists;
     *         otherwise, the {@code Nil()} value
     *
     * @throws NullPointerException if {@code name} is {@code null}
     */
    public Value get(final String name) {

        if ( name == null ) {
            throw new NullPointerException("null name");
        }

        if ( isReserved(name) ) {
            throw new IllegalArgumentException(format("reserved field name <%s>", name));
        }

        return object().map(fields -> fields.get(name)).orElse(NilValue.MODEL);
    }


    public Value select(final String path) {

        if ( path == null ) {
            throw new NullPointerException("null path");
        }

        return ValueSelector.select(this, path);
    }


    public Optional<Value> value() {
        return Optional.of(this).filter(not(Value::isEmpty));
    }

    public Optional<Boolean> bit() {
        return Optional.ofNullable(accept(BitValue.VISITOR));
    }

    public Optional<Number> number() {
        return Optional.ofNullable(accept(NumberValue.VISITOR));
    }

    public Optional<Long> integral() {
        return Optional.ofNullable(accept(IntegralValue.VISITOR));
    }

    public Optional<Double> floating() {
        return Optional.ofNullable(accept(FloatingValue.VISITOR));
    }

    public Optional<BigInteger> integer() {
        return Optional.ofNullable(accept(IntegerValue.VISITOR));
    }

    public Optional<BigDecimal> decimal() {
        return Optional.ofNullable(accept(DecimalValue.VISITOR));
    }

    public Optional<String> string() {
        return Optional.ofNullable(accept(StringValue.VISITOR));
    }

    public Optional<URI> uri() {
        return Optional.ofNullable(accept(URIValue.VISITOR));
    }

    public Optional<Temporal> temporal() {
        return Optional.ofNullable(accept(TemporalValue.VISITOR));
    }

    public Optional<Year> year() {
        return Optional.ofNullable(accept(YearValue.VISITOR));
    }

    public Optional<YearMonth> yearMonth() {
        return Optional.ofNullable(accept(YearMonthValue.VISITOR));
    }

    public Optional<LocalDate> localDate() {
        return Optional.ofNullable(accept(LocalDateValue.VISITOR));
    }

    public Optional<LocalTime> localTime() {
        return Optional.ofNullable(accept(LocalTimeValue.VISITOR));
    }

    public Optional<OffsetTime> offsetTime() {
        return Optional.ofNullable(accept(OffsetTimeValue.VISITOR));
    }

    public Optional<LocalDateTime> localDateTime() {
        return Optional.ofNullable(accept(LocalDateTimeValue.VISITOR));
    }

    public Optional<OffsetDateTime> offsetDateTime() {
        return Optional.ofNullable(accept(OffsetDateTimeValue.VISITOR));
    }

    public Optional<ZonedDateTime> zonedDateTime() {
        return Optional.ofNullable(accept(ZonedDateTimeValue.VISITOR));
    }

    public Optional<Instant> instant() {
        return Optional.ofNullable(accept(InstantValue.VISITOR));
    }

    public Optional<TemporalAmount> temporalAmount() {
        return Optional.ofNullable(accept(TemporalAmountValue.VISITOR));
    }

    public Optional<Period> period() {
        return Optional.ofNullable(accept(PeriodValue.VISITOR));
    }

    public Optional<Duration> duration() {
        return Optional.ofNullable(accept(DurationValue.VISITOR));
    }

    public Optional<Entry<Locale, String>> text() {
        return Optional.ofNullable(accept(TextValue.VISITOR));
    }

    public Optional<Entry<URI, String>> data() {
        return Optional.ofNullable(accept(DataValue.VISITOR));
    }

    public Optional<Map<String, Value>> object() {
        return Optional.ofNullable(accept(ObjectValue.VISITOR));
    }

    public Optional<List<Value>> array() {
        return Optional.ofNullable(accept(ArrayValue.VISITOR));
    }

    public <T> Optional<T> value(final Class<T> clazz) {

        if ( clazz == null ) {
            throw new NullPointerException("null clazz");
        }

        return Optional.ofNullable(accept(GenericValue.VISITOR))
                .filter(clazz::isInstance)
                .map(clazz::cast);
    }


    public Stream<Value> values() {
        return Stream.ofNullable(accept(ArrayValue.VISITOR))
                .flatMap(Collection::stream);
    }

    public Stream<Boolean> bits() {
        return Stream.ofNullable(accept(ArrayValue.VISITOR))
                .flatMap(Collection::stream)
                .map(v -> v.accept(BitValue.VISITOR))
                .filter(Objects::nonNull);
    }

    public Stream<Number> numbers() {
        return Stream.ofNullable(accept(ArrayValue.VISITOR))
                .flatMap(Collection::stream)
                .map(v -> v.accept(NumberValue.VISITOR))
                .filter(Objects::nonNull);
    }

    public Stream<Long> integrals() {
        return Stream.ofNullable(accept(ArrayValue.VISITOR))
                .flatMap(Collection::stream)
                .map(v -> v.accept(IntegralValue.VISITOR))
                .filter(Objects::nonNull);
    }

    public Stream<Double> floatings() {
        return Stream.ofNullable(accept(ArrayValue.VISITOR))
                .flatMap(Collection::stream)
                .map(v -> v.accept(FloatingValue.VISITOR))
                .filter(Objects::nonNull);
    }

    public Stream<BigInteger> integers() {
        return Stream.ofNullable(accept(ArrayValue.VISITOR))
                .flatMap(Collection::stream)
                .map(v -> v.accept(IntegerValue.VISITOR))
                .filter(Objects::nonNull);
    }

    public Stream<BigDecimal> decimals() {
        return Stream.ofNullable(accept(ArrayValue.VISITOR))
                .flatMap(Collection::stream)
                .map(v -> v.accept(DecimalValue.VISITOR))
                .filter(Objects::nonNull);
    }

    public Stream<String> strings() {
        return Stream.ofNullable(accept(ArrayValue.VISITOR))
                .flatMap(Collection::stream)
                .map(v -> v.accept(StringValue.VISITOR))
                .filter(Objects::nonNull);
    }

    public Stream<URI> uris() {
        return Stream.ofNullable(accept(ArrayValue.VISITOR))
                .flatMap(Collection::stream)
                .map(v -> v.accept(URIValue.VISITOR))
                .filter(Objects::nonNull);
    }

    public Stream<Temporal> temporals() {
        return Stream.ofNullable(accept(ArrayValue.VISITOR))
                .flatMap(Collection::stream)
                .map(v -> v.accept(TemporalValue.VISITOR))
                .filter(Objects::nonNull);
    }

    public Stream<Year> years() {
        return Stream.ofNullable(accept(ArrayValue.VISITOR))
                .flatMap(Collection::stream)
                .map(v -> v.accept(YearValue.VISITOR))
                .filter(Objects::nonNull);
    }

    public Stream<YearMonth> yearMonths() {
        return Stream.ofNullable(accept(ArrayValue.VISITOR))
                .flatMap(Collection::stream)
                .map(v -> v.accept(YearMonthValue.VISITOR))
                .filter(Objects::nonNull);
    }

    public Stream<LocalDate> localDates() {
        return Stream.ofNullable(accept(ArrayValue.VISITOR))
                .flatMap(Collection::stream)
                .map(v -> v.accept(LocalDateValue.VISITOR))
                .filter(Objects::nonNull);
    }

    public Stream<LocalTime> localTimes() {
        return Stream.ofNullable(accept(ArrayValue.VISITOR))
                .flatMap(Collection::stream)
                .map(v -> v.accept(LocalTimeValue.VISITOR))
                .filter(Objects::nonNull);
    }

    public Stream<OffsetTime> offsetTimes() {
        return Stream.ofNullable(accept(ArrayValue.VISITOR))
                .flatMap(Collection::stream)
                .map(v -> v.accept(OffsetTimeValue.VISITOR))
                .filter(Objects::nonNull);
    }

    public Stream<LocalDateTime> localDateTimes() {
        return Stream.ofNullable(accept(ArrayValue.VISITOR))
                .flatMap(Collection::stream)
                .map(v -> v.accept(LocalDateTimeValue.VISITOR))
                .filter(Objects::nonNull);
    }

    public Stream<OffsetDateTime> offsetDateTimes() {
        return Stream.ofNullable(accept(ArrayValue.VISITOR))
                .flatMap(Collection::stream)
                .map(v -> v.accept(OffsetDateTimeValue.VISITOR))
                .filter(Objects::nonNull);
    }

    public Stream<ZonedDateTime> zonedDateTimes() {
        return Stream.ofNullable(accept(ArrayValue.VISITOR))
                .flatMap(Collection::stream)
                .map(v -> v.accept(ZonedDateTimeValue.VISITOR))
                .filter(Objects::nonNull);
    }

    public Stream<Instant> instants() {
        return Stream.ofNullable(accept(ArrayValue.VISITOR))
                .flatMap(Collection::stream)
                .map(v -> v.accept(InstantValue.VISITOR))
                .filter(Objects::nonNull);
    }

    public Stream<TemporalAmount> temporalAmounts() {
        return Stream.ofNullable(accept(ArrayValue.VISITOR))
                .flatMap(Collection::stream)
                .map(v -> v.accept(TemporalAmountValue.VISITOR))
                .filter(Objects::nonNull);
    }

    public Stream<Period> periods() {
        return Stream.ofNullable(accept(ArrayValue.VISITOR))
                .flatMap(Collection::stream)
                .map(v -> v.accept(PeriodValue.VISITOR))
                .filter(Objects::nonNull);
    }

    public Stream<Duration> durations() {
        return Stream.ofNullable(accept(ArrayValue.VISITOR))
                .flatMap(Collection::stream)
                .map(v -> v.accept(DurationValue.VISITOR))
                .filter(Objects::nonNull);
    }

    public Stream<Entry<Locale, String>> texts() {
        return Stream.ofNullable(accept(ArrayValue.VISITOR))
                .flatMap(Collection::stream)
                .map(v -> v.accept(TextValue.VISITOR))
                .filter(Objects::nonNull);
    }

    public Stream<Entry<URI, String>> datas() {
        return Stream.ofNullable(accept(ArrayValue.VISITOR))
                .flatMap(Collection::stream)
                .map(v -> v.accept(DataValue.VISITOR))
                .filter(Objects::nonNull);
    }

    public Stream<Map<String, Value>> objects() {
        return Stream.ofNullable(accept(ArrayValue.VISITOR))
                .flatMap(Collection::stream)
                .map(v -> v.accept(ObjectValue.VISITOR))
                .filter(Objects::nonNull);
    }

    public Stream<List<Value>> arrays() {
        return Stream.ofNullable(accept(ArrayValue.VISITOR))
                .flatMap(Collection::stream)
                .map(v -> v.accept(ArrayValue.VISITOR))
                .filter(Objects::nonNull);
    }

    public <T> Stream<T> values(final Class<T> clazz) {

        if ( clazz == null ) {
            throw new NullPointerException("null clazz");
        }

        return Stream.ofNullable(accept(ArrayValue.VISITOR))
                .flatMap(Collection::stream)
                .map(v -> v.accept(GenericValue.VISITOR))
                .filter(clazz::isInstance)
                .map(clazz::cast);
    }


    public Value merge(final Value value) {

        if ( value == null ) {
            throw new NullPointerException("null value");
        }

        return merge(this, value);
    }


    private Value merge(final Value x, final Value y) {
        return x.object().flatMap(v ->
                        y.object().map(w -> merge(v, w))
                )

                .or(() -> x.array().flatMap(v ->
                        y.array().map(w -> merge(v, w))
                ))

                .orElse(x);
    }

    private Value merge(final Map<String, Value> x, final Map<String, Value> y) {
        return new ObjectValue(Stream.of(x.entrySet(), y.entrySet())

                .flatMap(Collection::stream)

                .collect(groupingBy(Entry::getKey, LinkedHashMap::new, mapping(Entry::getValue,
                        reducing(Nil(), (v, w) -> w)
                )))

        );
    }

    private Value merge(final List<Value> x, final List<Value> y) {
        return array(Stream.of(x, y)
                .flatMap(Collection::stream)
                .toList()
        );
    }


    public Optional<Value> validate() { return validate(false); }

    public Optional<Value> validate(final boolean delta) {
        return accept(new Visitor<>() {

            @Override public Optional<Value> visit(final Value host, final List<Value> values) {
                return Optional

                        .of(array(list(values.stream()
                                .flatMap(v -> v.validate(delta).stream())
                                .filter(not(Value::isEmpty))
                        )))

                        .filter(not(Value::isEmpty));
            }

            @Override public Optional<Value> visit(final Value host, final Map<String, Value> fields) {
                return host.shape().flatMap(shape -> new ValueValidator(shape, delta).validate(host, true));
            }

            @Override public Optional<Value> visit(final Value host, final Object object) {
                return Optional.empty();
            }

        });
    }


    public String encode(final URI base) throws UnsupportedOperationException {
        return error(new UnsupportedOperationException(format("<%s> encoding", this)));
    }

    public Optional<Value> decode(final String value, final URI base) throws UnsupportedOperationException {
        return error(new UnsupportedOperationException(format("<%s> decoding", this)));
    }


    public abstract <R, E extends Exception> R accept(final ThrowingVisitor<R, E> visitor) throws E;


    @Override
    public Value toValue() {
        return this;
    }


    //̸/////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public abstract static class Visitor<R> extends ThrowingVisitor<R, RuntimeException> { }

    public abstract static class ThrowingVisitor<R, E extends Exception> {

        public R visit(final Value host, final Object object) throws E {
            return null;
        }


        public R visit(final Value host, final Void nil) throws E {
            return visit(host, (Object)nil);
        }

        public R visit(final Value host, final Boolean bit) throws E {
            return visit(host, (Object)bit);
        }

        public R visit(final Value host, final Number number) throws E {
            return visit(host, (Object)number);
        }

        public R visit(final Value host, final Long integral) throws E {
            return visit(host, (Number)integral);
        }

        public R visit(final Value host, final Double floating) throws E {
            return visit(host, (Number)floating);
        }

        public R visit(final Value host, final BigInteger integer) throws E {
            return visit(host, (Number)integer);
        }

        public R visit(final Value host, final BigDecimal decimal) throws E {
            return visit(host, (Number)decimal);
        }

        public R visit(final Value host, final String string) throws E {
            return visit(host, (Object)string);
        }


        public R visit(final Value host, final URI uri) throws E {
            return visit(host, (Object)uri);
        }

        public R visit(final Value host, final Temporal temporal) throws E {
            return visit(host, (Object)temporal);
        }

        public R visit(final Value host, final Year year) throws E {
            return visit(host, (Temporal)year);
        }

        public R visit(final Value host, final YearMonth yearMonth) throws E {
            return visit(host, (Temporal)yearMonth);
        }

        public R visit(final Value host, final LocalDate localDate) throws E {
            return visit(host, (Temporal)localDate);
        }

        public R visit(final Value host, final LocalTime localTime) throws E {
            return visit(host, (Temporal)localTime);
        }

        public R visit(final Value host, final OffsetTime offsetTime) throws E {
            return visit(host, (Temporal)offsetTime);
        }

        public R visit(final Value host, final LocalDateTime localDateTime) throws E {
            return visit(host, (Temporal)localDateTime);
        }

        public R visit(final Value host, final OffsetDateTime offsetDateTime) throws E {
            return visit(host, (Temporal)offsetDateTime);
        }

        public R visit(final Value host, final ZonedDateTime zonedDateTime) throws E {
            return visit(host, (Temporal)zonedDateTime);
        }

        public R visit(final Value host, final Instant instant) throws E {
            return visit(host, (Temporal)instant);
        }

        public R visit(final Value host, final TemporalAmount amount) throws E {
            return visit(host, (Object)amount);
        }

        public R visit(final Value host, final Period period) throws E {
            return visit(host, (TemporalAmount)period);
        }

        public R visit(final Value host, final Duration duration) throws E {
            return visit(host, (TemporalAmount)duration);
        }


        public R visit(final Value host, final Locale locale, final String string) throws E {
            return visit(host, entry(string, locale));
        }

        public R visit(final Value host, final URI datatype, final String string) throws E {
            return visit(host, entry(string, datatype));
        }

        public R visit(final Value host, final Map<String, Value> fields) throws E {
            return visit(host, (Object)fields);
        }

        public R visit(final Value host, final List<Value> values) throws E {
            return visit(host, (Object)values);
        }

    }


    //̸/////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private static final class NilValue extends Value {

        private static final Value MODEL=new NilValue();

        @Override public String encode(final URI base) {
            return "null";
        }

        @Override public Optional<Value> decode(final String value, final URI base) {
            return value.equals("null") ? Optional.of(MODEL) : Optional.empty();
        }

        @Override public <V, E extends Exception> V accept(final ThrowingVisitor<V, E> visitor) throws E {
            return visitor.visit(this, (Void)null);
        }

        @Override public boolean equals(final Object object) {
            return object instanceof NilValue;
        }

        @Override public int hashCode() {
            return Objects.hashCode(null);
        }

        @Override public String toString() {
            return "Nil()";
        }

    }

    private static final class BitValue extends Value {

        private static final Value MODEL=new BitValue(false);

        private static final Visitor<Boolean> VISITOR=new Visitor<>() {

            @Override public Boolean visit(final Value host, final Boolean bit) {
                return bit;
            }

        };

        private final boolean bit;

        private BitValue(final boolean bit) { this.bit=bit; }

        @Override public String encode(final URI base) {
            return Boolean.toString(bit);
        }

        @Override public Optional<Value> decode(final String value, final URI base) {
            return value.equalsIgnoreCase("true") ? Optional.of(TRUE)
                    : value.equalsIgnoreCase("false") ? Optional.of(FALSE)
                    : Optional.empty();
        }

        @Override public <V, E extends Exception> V accept(final ThrowingVisitor<V, E> visitor) throws E {
            return visitor.visit(this, bit);
        }

        @Override public boolean equals(final Object object) {
            return this == object || object instanceof final BitValue value
                                     && bit == value.bit;
        }

        @Override public int hashCode() {
            return Boolean.hashCode(bit);
        }

        @Override public String toString() {
            return format("Bit(%s)", bit);
        }

    }

    private static final class NumberValue extends Value {

        private static final Value MODEL=new NumberValue();

        private static final Visitor<Number> VISITOR=new Visitor<>() {

            @Override public Number visit(final Value host, final Number number) {
                return number;
            }

        };

        @Override public Optional<Value> decode(final String value, final URI base) {
            return value.indexOf('e') >= 0 ? FloatingValue.MODEL.decode(value, base)
                    : value.indexOf('E') >= 0 ? FloatingValue.MODEL.decode(value, base)
                    : value.indexOf('.') >= 0 ? DecimalValue.MODEL.decode(value, base)
                    : IntegerValue.MODEL.decode(value, base);
        }

        @Override public <R, E extends Exception> R accept(final ThrowingVisitor<R, E> visitor) throws E {
            return visitor.visit(this, 0);
        }

    }

    private static final class IntegralValue extends Value {

        private static final Value MODEL=new IntegralValue(0);

        private static final Visitor<Long> VISITOR=new Visitor<>() {

            @Override public Long visit(final Value host, final Long integral) {
                return integral;
            }

            @Override public Long visit(final Value host, final Number number) {
                return number.longValue();
            }

        };

        private final long integral;

        private IntegralValue(final long integral) { this.integral=integral; }

        @Override public String encode(final URI base) {
            return Long.toString(integral);
        }

        @Override public Optional<Value> decode(final String value, final URI base) {
            try {

                return Optional.of(integral(Long.parseLong(value)));

            } catch ( final NumberFormatException e ) {
                return Optional.empty();
            }
        }

        @Override public <V, E extends Exception> V accept(final ThrowingVisitor<V, E> visitor) throws E {
            return visitor.visit(this, integral);
        }

        @Override public boolean equals(final Object object) {
            return this == object || object instanceof final IntegralValue value
                                     && integral == value.integral;
        }

        @Override public int hashCode() {
            return Long.hashCode(integral);
        }

        @Override public String toString() {
            return format("Integral(%d)", integral);
        }

    }

    private static final class FloatingValue extends Value {

        private static final Value MODEL=new FloatingValue(0);

        private static final Visitor<Double> VISITOR=new Visitor<>() {

            @Override public Double visit(final Value host, final Double floating) {
                return floating;
            }

            @Override public Double visit(final Value host, final Number number) {
                return number.doubleValue();
            }

        };

        private static final ThreadLocal<DecimalFormat> FloatingFormat=ThreadLocal.withInitial(() -> { // ;( thread-safe

            final DecimalFormatSymbols symbols=DecimalFormatSymbols.getInstance(ROOT);

            symbols.setExponentSeparator("e");

            return new DecimalFormat("0.0%sE0".formatted("#".repeat(15)), symbols);

        });

        private final double floating;

        private FloatingValue(final double floating) { this.floating=floating; }

        @Override public String encode(final URI base) {
            return FloatingFormat.get().format(floating);
        }

        @Override public Optional<Value> decode(final String value, final URI base) {
            try {

                return Optional.of(floating(Double.parseDouble(value)));

            } catch ( final NumberFormatException e ) {
                return Optional.empty();
            }
        }

        @Override public <V, E extends Exception> V accept(final ThrowingVisitor<V, E> visitor) throws E {
            return visitor.visit(this, floating);
        }

        @Override public boolean equals(final Object object) {
            return this == object || object instanceof final FloatingValue value
                                     && Double.compare(floating, value.floating) == 0;
        }

        @Override public int hashCode() {
            return Double.hashCode(floating);
        }

        @Override public String toString() {
            return format("Floating(%f)", floating);
        }

    }

    private static final class IntegerValue extends Value {

        private static final Value MODEL=new IntegerValue(BigInteger.valueOf(0));

        private static final Visitor<BigInteger> VISITOR=new Visitor<>() {

            @Override public BigInteger visit(final Value host, final BigInteger integer) {
                return integer;
            }

            @Override public BigInteger visit(final Value host, final BigDecimal decimal) {
                return decimal.toBigInteger();
            }

            @Override public BigInteger visit(final Value host, final Number number) {
                return BigInteger.valueOf(number.longValue());
            }

        };

        private final BigInteger integer;

        private IntegerValue(final BigInteger integer) { this.integer=integer; }

        @Override public String encode(final URI base) {
            return integer.toString();
        }

        @Override public Optional<Value> decode(final String value, final URI base) {
            try {

                return Optional.of(integer(new BigInteger(value)));

            } catch ( final NumberFormatException e ) {
                return Optional.empty();
            }
        }

        @Override public <V, E extends Exception> V accept(final ThrowingVisitor<V, E> visitor) throws E {
            return visitor.visit(this, integer);
        }

        @Override public boolean equals(final Object object) {
            return this == object || object instanceof final IntegerValue value
                                     && integer.equals(value.integer);
        }

        @Override public int hashCode() {
            return integer.hashCode();
        }

        @Override public String toString() {
            return format("Integer(%d)", integer);
        }

    }

    private static final class DecimalValue extends Value {

        private static final Value MODEL=new DecimalValue(BigDecimal.valueOf(0));

        private static final Visitor<BigDecimal> VISITOR=new Visitor<>() {

            @Override public BigDecimal visit(final Value host, final BigDecimal decimal) {
                return decimal;
            }

            @Override public BigDecimal visit(final Value host, final BigInteger integer) {
                return new BigDecimal(integer);
            }

            @Override public BigDecimal visit(final Value host, final Number number) {
                return BigDecimal.valueOf(number.doubleValue());
            }

        };

        private final BigDecimal decimal;

        private DecimalValue(final BigDecimal decimal) { this.decimal=decimal; }

        @Override public String encode(final URI base) {
            return decimal.setScale(max(1, decimal.scale()), UNNECESSARY).toString();
        }

        @Override public Optional<Value> decode(final String value, final URI base) {
            try {

                return Optional.of(decimal(new BigDecimal(value)));

            } catch ( final NumberFormatException e ) {
                return Optional.empty();
            }
        }

        @Override public <V, E extends Exception> V accept(final ThrowingVisitor<V, E> visitor) throws E {
            return visitor.visit(this, decimal);
        }

        @Override public boolean equals(final Object object) {
            return this == object || object instanceof final DecimalValue value
                                     && decimal.compareTo(value.decimal) == 0;
        }

        @Override public int hashCode() {
            return decimal.hashCode();
        }

        @Override public String toString() {
            return format("Decimal(%f)", decimal);
        }

    }

    private static final class StringValue extends Value {

        private static final Value MODEL=new StringValue("");

        private static final Visitor<String> VISITOR=new Visitor<>() {

            @Override public String visit(final Value host, final String string) {
                return string;
            }

            @Override public String visit(final Value host, final URI uri) {
                return URIValue.string(uri, URIs.uri());
            }

            @Override public String visit(final Value host, final Year year) {
                return YearValue.string(year);
            }

            @Override public String visit(final Value host, final YearMonth yearMonth) {
                return YearMonthValue.string(yearMonth);
            }

            @Override public String visit(final Value host, final LocalDate localDate) {
                return LocalDateValue.string(localDate);
            }

            @Override public String visit(final Value host, final LocalTime localTime) {
                return LocalTimeValue.string(localTime);
            }

            @Override public String visit(final Value host, final OffsetTime offsetTime) {
                return OffsetTimeValue.string(offsetTime);
            }

            @Override public String visit(final Value host, final LocalDateTime localDateTime) {
                return LocalDateTimeValue.string(localDateTime);
            }

            @Override public String visit(final Value host, final OffsetDateTime offsetDateTime) {
                return OffsetDateTimeValue.string(offsetDateTime);
            }

            @Override public String visit(final Value host, final ZonedDateTime zonedDateTime) {
                return ZonedDateTimeValue.string(zonedDateTime);
            }

            @Override public String visit(final Value host, final Instant instant) {
                return InstantValue.string(instant);
            }

            @Override public String visit(final Value host, final Period period) {
                return PeriodValue.string(period);
            }

            @Override public String visit(final Value host, final Duration duration) {
                return DurationValue.string(duration);
            }

        };

        private final String string;

        private StringValue(final String string) { this.string=string; }

        @Override public String encode(final URI base) {
            return string;
        }

        @Override public Optional<Value> decode(final String value, final URI base) {
            return Optional.of(string(value));
        }

        @Override public <V, E extends Exception> V accept(final ThrowingVisitor<V, E> visitor) throws E {
            return visitor.visit(this, string);
        }

        @Override public boolean equals(final Object object) {
            return this == object || object instanceof final StringValue value
                                     && string.equals(value.string);
        }

        @Override public int hashCode() {
            return string.hashCode();
        }

        @Override public String toString() {
            return format("String(%s)", string);
        }

    }


    private static final class URIValue extends Value {

        private static final Value MODEL=new URIValue(URIs.uri());

        private static final Visitor<URI> VISITOR=new Visitor<>() {

            @Override public URI visit(final Value host, final String string) {
                return value(string, URIs.uri());
            }

            @Override public URI visit(final Value host, final URI uri) {
                return uri;
            }

        };

        private static String string(final URI uri, final URI base) {
            return URIs.relative(base, uri).toString();
        }

        private static URI value(final String value, final URI base) {
            try {

                return URIs.absolute(base, URI.create(value));

            } catch ( final IllegalArgumentException e ) {

                return null;

            }
        }

        private final URI uri;

        private URIValue(final URI uri) { this.uri=uri; }

        @Override public String encode(final URI base) {
            return string(uri, base);
        }

        @Override public Optional<Value> decode(final String value, final URI base) {
            return Optional.ofNullable(value(value, base)).map(Value::uri);
        }

        @Override public <V, E extends Exception> V accept(final ThrowingVisitor<V, E> visitor) throws E {
            return visitor.visit(this, uri);
        }

        @Override public boolean equals(final Object object) {
            return this == object || object instanceof final URIValue value
                                     && uri.equals(value.uri);
        }

        @Override public int hashCode() {
            return uri.hashCode();
        }

        @Override public String toString() {
            return format("URI(%s)", uri);
        }

    }

    private static final class TemporalValue extends Value {

        private static final Value MODEL=new TemporalValue();

        private static final Visitor<Temporal> VISITOR=new Visitor<>() {

            @Override public Temporal visit(final Value host, final String string) {
                return value(string);
            }

            @Override public Temporal visit(final Value host, final Temporal temporal) {
                return temporal;
            }

        };

        private static final Pattern YearPattern=Pattern.compile("\\d{4}");
        private static final Pattern YearMonthPattern=Pattern.compile("\\d{4}-\\d{2}");
        private static final Pattern LocalDatePattern=Pattern.compile("\\d{4}-\\d{2}-\\d{2}");
        private static final Pattern LocalTimePattern=Pattern.compile("\\d{2}:\\d{2}(:\\d{2}(\\.\\d+)?)?");
        private static final Pattern OffsetTimePattern=Pattern.compile("\\d{2}:\\d{2}(:\\d{2}(\\.\\d+)?)?([+-]\\d{2}:\\d{2})");
        private static final Pattern LocalDateTimePattern=Pattern.compile("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}(:\\d{2}(\\.\\d+)?)?");
        private static final Pattern OffsetDateTimePattern=Pattern.compile("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}(:\\d{2}(\\.\\d+)?)?([+-]\\d{2}:\\d{2}|Z)");
        private static final Pattern ZonedDateTimePattern=Pattern.compile("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}(:\\d{2}(\\.\\d+)?)?([+-]\\d{2}:\\d{2}|Z)\\[.+]");
        private static final Pattern InstantPattern=Pattern.compile("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}(:\\d{2}(\\.\\d+)?)?Z");

        private static Temporal value(final String value) {
            return YearPattern.matcher(value).matches() ? YearValue.value(value)
                    : YearMonthPattern.matcher(value).matches() ? YearMonthValue.value(value)
                    : LocalDatePattern.matcher(value).matches() ? LocalDateValue.value(value)
                    : LocalTimePattern.matcher(value).matches() ? LocalTimeValue.value(value)
                    : OffsetTimePattern.matcher(value).matches() ? OffsetTimeValue.value(value)
                    : LocalDateTimePattern.matcher(value).matches() ? LocalDateTimeValue.value(value)
                    : OffsetDateTimePattern.matcher(value).matches() ? OffsetDateTimeValue.value(value)
                    : ZonedDateTimePattern.matcher(value).matches() ? ZonedDateTimeValue.value(value)
                    : InstantPattern.matcher(value).matches() ? InstantValue.value(value)
                    : null;
        }

        private static Value create(final TemporalAccessor accessor) {
            return accessor instanceof final Year year ? year(year)
                    : accessor instanceof final YearMonth yearMonth ? yearMonth(yearMonth)
                    : accessor instanceof final LocalDate localDate ? localDate(localDate)
                    : accessor instanceof final LocalTime localTime ? localTime(localTime)
                    : accessor instanceof final OffsetTime offsetTime ? offsetTime(offsetTime)
                    : accessor instanceof final LocalDateTime localDateTime ? localDateTime(localDateTime)
                    : accessor instanceof final OffsetDateTime offsetDateTime ? offsetDateTime(offsetDateTime)
                    : accessor instanceof final ZonedDateTime zonedDateTime ? zonedDateTime(zonedDateTime)
                    : accessor instanceof final Instant instant ? instant(instant)

                    : guess(accessor);
        }

        @Override public Optional<Value> decode(final String value, final URI base) {
            return Optional.ofNullable(value(value)).map(TemporalValue::create);
        }

        @Override public <R, E extends Exception> R accept(final ThrowingVisitor<R, E> visitor) throws E {
            return visitor.visit(this, EPOCH);
        }


        private static Value guess(final TemporalAccessor accessor) {

            final ZoneId zone=accessor.query(TemporalQueries.zone());

            final boolean hasNamedZone=zone != null && !(zone instanceof ZoneOffset);
            final boolean hasOffset=accessor.isSupported(ChronoField.OFFSET_SECONDS);
            final boolean hasYear=accessor.isSupported(ChronoField.YEAR);
            final boolean hasMonth=accessor.isSupported(ChronoField.MONTH_OF_YEAR);
            final boolean hasDay=accessor.isSupported(ChronoField.DAY_OF_MONTH);
            final boolean hasTime=accessor.isSupported(ChronoField.HOUR_OF_DAY);
            final boolean hasSeconds=accessor.isSupported(ChronoField.INSTANT_SECONDS);

            if ( hasNamedZone && hasYear && hasMonth && hasDay && hasTime ) {

                return zonedDateTime(ZonedDateTime.from(accessor));

            } else if ( hasOffset ) {

                return hasYear && hasMonth && hasDay && hasTime ? offsetDateTime(OffsetDateTime.from(accessor))
                        : hasTime ? offsetTime(OffsetTime.from(accessor))
                        : error(unsupported(accessor));

            } else if ( hasYear ) {

                return hasMonth ? hasDay ? hasTime ? localDateTime(LocalDateTime.from(accessor))
                        : localDate(LocalDate.from(accessor))
                        : yearMonth(YearMonth.from(accessor))
                        : year(Year.from(accessor));

            } else if ( hasTime ) {

                return localTime(LocalTime.from(accessor));

            } else if ( hasSeconds ) {

                return instant(Instant.from(accessor));

            } else {

                return error(unsupported(accessor));

            }

        }

    }

    private static final class YearValue extends Value {

        private static final Value MODEL=new YearValue(Year.from(EPOCH.atZone(ZoneOffset.UTC)));

        private static final Visitor<Year> VISITOR=new Visitor<>() {

            @Override public Year visit(final Value host, final String string) {
                return value(string);
            }

            @Override public Year visit(final Value host, final Year year) {
                return year;
            }

        };

        private static String string(final Year year) {
            return year.toString();
        }

        private static Year value(final String value) {
            try {

                return Year.parse(value);

            } catch ( final DateTimeParseException e ) {

                return null;

            }
        }

        private final Year year;

        private YearValue(final Year year) { this.year=year; }

        @Override public String encode(final URI base) {
            return string(year);
        }

        @Override public Optional<Value> decode(final String value, final URI base) {
            return Optional.ofNullable(value(value)).map(Value::year);
        }

        @Override public <V, E extends Exception> V accept(final ThrowingVisitor<V, E> visitor) throws E {
            return visitor.visit(this, year);
        }

        @Override public boolean equals(final Object object) {
            return this == object || object instanceof final YearValue value
                                     && year.equals(value.year);
        }

        @Override public int hashCode() {
            return year.hashCode();
        }

        @Override public String toString() {
            return format("Year(%s)", year);
        }

    }

    private static final class YearMonthValue extends Value {

        private static final Value MODEL=new YearMonthValue(YearMonth.from(EPOCH.atZone(ZoneOffset.UTC)));

        private static final Visitor<YearMonth> VISITOR=new Visitor<>() {

            @Override public YearMonth visit(final Value host, final String string) {
                return value(string);
            }

            @Override public YearMonth visit(final Value host, final YearMonth yearMonth) {
                return yearMonth;
            }

        };

        private static String string(final YearMonth yearMonth) {
            return yearMonth.toString();
        }

        private static YearMonth value(final String value) {
            try {

                return YearMonth.parse(value);

            } catch ( final DateTimeParseException e ) {

                return null;

            }
        }

        private final YearMonth yearMonth;

        private YearMonthValue(final YearMonth yearMonth) { this.yearMonth=yearMonth; }

        @Override public String encode(final URI base) {
            return string(yearMonth);
        }

        @Override public Optional<Value> decode(final String value, final URI base) {
            return Optional.ofNullable(value(value)).map(Value::yearMonth);
        }

        @Override public <V, E extends Exception> V accept(final ThrowingVisitor<V, E> visitor) throws E {
            return visitor.visit(this, yearMonth);
        }

        @Override public boolean equals(final Object object) {
            return this == object || object instanceof final YearMonthValue value
                                     && yearMonth.equals(value.yearMonth);
        }

        @Override public int hashCode() {
            return yearMonth.hashCode();
        }

        @Override public String toString() {
            return format("YearMonth(%s)", yearMonth);
        }

    }

    private static final class LocalDateValue extends Value {

        private static final Value MODEL=new LocalDateValue(LocalDate.from(EPOCH.atZone(ZoneOffset.UTC)));

        private static final Visitor<LocalDate> VISITOR=new Visitor<>() {

            @Override public LocalDate visit(final Value host, final String string) {
                return value(string);
            }

            @Override public LocalDate visit(final Value host, final LocalDate localDate) {
                return localDate;
            }

        };

        private static String string(final LocalDate localDate) {
            return localDate.toString();
        }

        private static LocalDate value(final String value) {
            try {

                return LocalDate.parse(value);

            } catch ( final DateTimeParseException e ) {

                return null;

            }
        }

        private final LocalDate localDate;

        private LocalDateValue(final LocalDate localDate) { this.localDate=localDate; }

        @Override public String encode(final URI base) {
            return string(localDate);
        }

        @Override public Optional<Value> decode(final String value, final URI base) {
            return Optional.ofNullable(value(value)).map(Value::localDate);
        }

        @Override public <V, E extends Exception> V accept(final ThrowingVisitor<V, E> visitor) throws E {
            return visitor.visit(this, localDate);
        }

        @Override public boolean equals(final Object object) {
            return this == object || object instanceof final LocalDateValue value
                                     && localDate.equals(value.localDate);
        }

        @Override public int hashCode() {
            return localDate.hashCode();
        }

        @Override public String toString() {
            return format("LocalDate(%s)", localDate);
        }

    }

    private static final class LocalTimeValue extends Value {

        private static final Value MODEL=new LocalTimeValue(LocalTime.from(EPOCH.atZone(ZoneOffset.UTC)));

        private static final Visitor<LocalTime> VISITOR=new Visitor<>() {

            @Override public LocalTime visit(final Value host, final String string) {
                return value(string);
            }

            @Override public LocalTime visit(final Value host, final LocalTime localTime) {
                return localTime;
            }

        };

        private static String string(final LocalTime localTime) {
            return localTime.toString();
        }

        private static LocalTime value(final String value) {
            try {

                return LocalTime.parse(value);

            } catch ( final DateTimeParseException e ) {

                return null;

            }
        }

        private final LocalTime localTime;

        private LocalTimeValue(final LocalTime localTime) { this.localTime=localTime; }

        @Override public String encode(final URI base) {
            return string(localTime);
        }

        @Override public Optional<Value> decode(final String value, final URI base) {
            return Optional.ofNullable(value(value)).map(Value::localTime);
        }

        @Override public <V, E extends Exception> V accept(final ThrowingVisitor<V, E> visitor) throws E {
            return visitor.visit(this, localTime);
        }

        @Override public boolean equals(final Object object) {
            return this == object || object instanceof final LocalTimeValue value
                                     && localTime.equals(value.localTime);
        }

        @Override public int hashCode() {
            return localTime.hashCode();
        }

        @Override public String toString() {
            return format("LocalTime(%s)", localTime);
        }

    }

    private static final class OffsetTimeValue extends Value {

        private static final Value MODEL=new OffsetTimeValue(OffsetTime.from(EPOCH.atZone(ZoneOffset.UTC)));

        private static final Visitor<OffsetTime> VISITOR=new Visitor<>() {

            @Override public OffsetTime visit(final Value host, final String string) {
                return value(string);
            }

            @Override public OffsetTime visit(final Value host, final OffsetTime offsetTime) {
                return offsetTime;
            }

        };

        private static String string(final OffsetTime offsetTime) {
            return offsetTime.toString();
        }

        private static OffsetTime value(final String value) {
            try {

                return OffsetTime.parse(value);

            } catch ( final DateTimeParseException e ) {

                return null;

            }
        }

        private final OffsetTime offsetTime;

        private OffsetTimeValue(final OffsetTime offsetTime) { this.offsetTime=offsetTime; }

        @Override public String encode(final URI base) {
            return string(offsetTime);
        }

        @Override public Optional<Value> decode(final String value, final URI base) {
            return Optional.ofNullable(value(value)).map(Value::offsetTime);
        }

        @Override public <V, E extends Exception> V accept(final ThrowingVisitor<V, E> visitor) throws E {
            return visitor.visit(this, offsetTime);
        }

        @Override public boolean equals(final Object object) {
            return this == object || object instanceof final OffsetTimeValue value
                                     && offsetTime.equals(value.offsetTime);
        }

        @Override public int hashCode() {
            return offsetTime.hashCode();
        }

        @Override public String toString() {
            return format("OffsetTime(%s)", offsetTime);
        }

    }

    private static final class LocalDateTimeValue extends Value {

        private static final Value MODEL=new LocalDateTimeValue(LocalDateTime.from(EPOCH.atZone(ZoneOffset.UTC)));

        private static final Visitor<LocalDateTime> VISITOR=new Visitor<>() {

            @Override public LocalDateTime visit(final Value host, final String string) {
                return value(string);
            }

            @Override public LocalDateTime visit(final Value host, final LocalDateTime localDateTime) {
                return localDateTime;
            }

        };

        private static String string(final LocalDateTime localDateTime) {
            return localDateTime.toString();
        }

        private static LocalDateTime value(final String value) {
            try {

                return LocalDateTime.parse(value);

            } catch ( final DateTimeParseException e ) {

                return null;

            }
        }

        private final LocalDateTime localDateTime;

        private LocalDateTimeValue(final LocalDateTime localDateTime) { this.localDateTime=localDateTime; }

        @Override public String encode(final URI base) {
            return string(localDateTime);
        }

        @Override public Optional<Value> decode(final String value, final URI base) {
            return Optional.ofNullable(value(value)).map(Value::localDateTime);
        }

        @Override public <V, E extends Exception> V accept(final ThrowingVisitor<V, E> visitor) throws E {
            return visitor.visit(this, localDateTime);
        }

        @Override public boolean equals(final Object object) {
            return this == object || object instanceof final LocalDateTimeValue value
                                     && localDateTime.equals(value.localDateTime);
        }

        @Override public int hashCode() {
            return localDateTime.hashCode();
        }

        @Override public String toString() {
            return format("LocalDateTime(%s)", localDateTime);
        }

    }

    private static final class OffsetDateTimeValue extends Value {

        private static final Value MODEL=new OffsetDateTimeValue(OffsetDateTime.from(EPOCH.atZone(ZoneOffset.UTC)));

        private static final Visitor<OffsetDateTime> VISITOR=new Visitor<>() {

            @Override public OffsetDateTime visit(final Value host, final String string) {
                return value(string);
            }

            @Override public OffsetDateTime visit(final Value host, final OffsetDateTime offsetDateTime) {
                return offsetDateTime;
            }

        };

        private static String string(final OffsetDateTime offsetDateTime) {
            return offsetDateTime.toString();
        }

        private static OffsetDateTime value(final String value) {
            try {

                return OffsetDateTime.parse(value);

            } catch ( final DateTimeParseException e ) {

                return null;

            }
        }

        private final OffsetDateTime offsetDateTime;

        private OffsetDateTimeValue(final OffsetDateTime offsetDateTime) { this.offsetDateTime=offsetDateTime; }

        @Override public String encode(final URI base) {
            return string(offsetDateTime);
        }

        @Override public Optional<Value> decode(final String value, final URI base) {
            return Optional.ofNullable(value(value)).map(Value::offsetDateTime);
        }

        @Override public <V, E extends Exception> V accept(final ThrowingVisitor<V, E> visitor) throws E {
            return visitor.visit(this, offsetDateTime);
        }

        @Override public boolean equals(final Object object) {
            return this == object || object instanceof final OffsetDateTimeValue value
                                     && offsetDateTime.equals(value.offsetDateTime);
        }

        @Override public int hashCode() {
            return offsetDateTime.hashCode();
        }

        @Override public String toString() {
            return format("OffsetDateTime(%s)", offsetDateTime);
        }

    }

    private static final class ZonedDateTimeValue extends Value {

        private static final Value MODEL=new ZonedDateTimeValue(EPOCH.atZone(ZoneOffset.UTC));

        private static final Visitor<ZonedDateTime> VISITOR=new Visitor<>() {

            @Override public ZonedDateTime visit(final Value host, final String string) {
                return value(string);
            }

            @Override public ZonedDateTime visit(final Value host, final ZonedDateTime zonedDateTime) {
                return zonedDateTime;
            }

        };

        private static String string(final ZonedDateTime zonedDateTime) {
            return zonedDateTime.toString();
        }

        private static ZonedDateTime value(final String value) {
            try {

                return ZonedDateTime.parse(value);

            } catch ( final DateTimeParseException e ) {

                return null;

            }
        }

        private final ZonedDateTime zonedDateTime;

        private ZonedDateTimeValue(final ZonedDateTime zonedDateTime) { this.zonedDateTime=zonedDateTime; }

        @Override public String encode(final URI base) {
            return string(zonedDateTime);
        }

        @Override public Optional<Value> decode(final String value, final URI base) {
            return Optional.ofNullable(value(value)).map(Value::zonedDateTime);
        }

        @Override public <V, E extends Exception> V accept(final ThrowingVisitor<V, E> visitor) throws E {
            return visitor.visit(this, zonedDateTime);
        }

        @Override public boolean equals(final Object object) {
            return this == object || object instanceof final ZonedDateTimeValue value
                                     && zonedDateTime.equals(value.zonedDateTime);
        }

        @Override public int hashCode() {
            return zonedDateTime.hashCode();
        }

        @Override public String toString() {
            return format("ZonedDateTime(%s)", zonedDateTime);
        }

    }

    private static final class InstantValue extends Value {

        private static final Value MODEL=new InstantValue(Instant.from(EPOCH.atZone(ZoneOffset.UTC)));

        private static final Visitor<Instant> VISITOR=new Visitor<>() {

            @Override public Instant visit(final Value host, final String string) {
                return value(string);
            }

            @Override public Instant visit(final Value host, final Instant instant) {
                return instant;
            }

        };

        private static String string(final Instant instant) {
            return instant.toString();
        }

        private static Instant value(final String value) {
            try {

                return Instant.parse(value);

            } catch ( final DateTimeParseException e ) {

                return null;

            }
        }

        private final Instant instant;

        private InstantValue(final Instant instant) { this.instant=instant; }

        @Override public String encode(final URI base) {
            return string(instant);
        }

        @Override public Optional<Value> decode(final String value, final URI base) {
            return Optional.ofNullable(value(value)).map(Value::instant);
        }

        @Override public <V, E extends Exception> V accept(final ThrowingVisitor<V, E> visitor) throws E {
            return visitor.visit(this, instant);
        }

        @Override public boolean equals(final Object object) {
            return this == object || object instanceof final InstantValue value
                                     && instant.equals(value.instant);
        }

        @Override public int hashCode() {
            return instant.hashCode();
        }

        @Override public String toString() {
            return format("Instant(%s)", instant);
        }

    }

    private static final class TemporalAmountValue extends Value {

        private static final Value MODEL=new TemporalAmountValue();

        private static final Visitor<TemporalAmount> VISITOR=new Visitor<>() {

            @Override public TemporalAmount visit(final Value host, final String string) {
                return string.startsWith("PT") ? DurationValue.value(string) : PeriodValue.value(string);
            }

            @Override public TemporalAmount visit(final Value host, final TemporalAmount amount) {
                return amount;
            }

        };

        private static TemporalAmount value(final String value) {
            return value.startsWith("PT") ? DurationValue.value(value)
                    : value.startsWith("P") ? PeriodValue.value(value)
                    : null;
        }

        private static Value create(final TemporalAmount amount) {
            return amount instanceof final Period period ? period(period)
                    : amount instanceof final Duration duration ? duration(duration)
                    : guess(amount);
        }

        @Override public Optional<Value> decode(final String value, final URI base) {
            return Optional.ofNullable(value(value)).map(TemporalAmountValue::create);
        }

        @Override public <R, E extends Exception> R accept(final ThrowingVisitor<R, E> visitor) throws E {
            return visitor.visit(this, Duration.ZERO);
        }


        private static Value guess(final TemporalAmount amount) {

            final Set<TemporalUnit> units=new HashSet<>(amount.getUnits());

            final boolean hasDateUnits=units.contains(ChronoUnit.YEARS)
                                       || units.contains(ChronoUnit.MONTHS)
                                       || units.contains(ChronoUnit.DAYS);

            final boolean hasTimeUnits=units.contains(ChronoUnit.HOURS)
                                       || units.contains(ChronoUnit.MINUTES)
                                       || units.contains(ChronoUnit.SECONDS)
                                       || units.contains(ChronoUnit.MILLIS)
                                       || units.contains(ChronoUnit.MICROS)
                                       || units.contains(ChronoUnit.NANOS);

            return hasDateUnits && !hasTimeUnits ? period(Period.from(amount))
                    : hasTimeUnits && !hasDateUnits ? duration(Duration.from(amount))
                    : error(unsupported(amount));
        }

    }

    private static final class PeriodValue extends Value {

        private static final Value MODEL=new PeriodValue(Period.ZERO);

        private static final Visitor<Period> VISITOR=new Visitor<>() {

            @Override public Period visit(final Value host, final String string) {
                return value(string);
            }

            @Override public Period visit(final Value host, final Period period) {
                return period;
            }

        };

        private static String string(final Period period) {
            return period.toString();
        }

        private static Period value(final String value) {
            try {

                return Period.parse(value);

            } catch ( final DateTimeParseException e ) {

                return null;

            }
        }

        private final Period period;

        private PeriodValue(final Period period) { this.period=period; }

        @Override public String encode(final URI base) {
            return string(period);
        }

        @Override public Optional<Value> decode(final String value, final URI base) {
            return Optional.ofNullable(value(value)).map(Value::period);
        }

        @Override public <V, E extends Exception> V accept(final ThrowingVisitor<V, E> visitor) throws E {
            return visitor.visit(this, period);
        }

        @Override public boolean equals(final Object object) {
            return this == object || object instanceof final PeriodValue value
                                     && period.equals(value.period);
        }

        @Override public int hashCode() {
            return period.hashCode();
        }

        @Override public String toString() {
            return format("Period(%s)", period);
        }

    }

    private static final class DurationValue extends Value {

        private static final Value MODEL=new DurationValue(Duration.ZERO);

        private static final Visitor<Duration> VISITOR=new Visitor<>() {

            @Override public Duration visit(final Value host, final String string) {
                return value(string);
            }

            @Override public Duration visit(final Value host, final Duration duration) {
                return duration;
            }

        };

        private static String string(final Duration duration) {
            return duration.toString();
        }

        private static Duration value(final String value) {
            try {

                return Duration.parse(value);

            } catch ( final DateTimeParseException e ) {

                return null;

            }
        }

        private final Duration duration;

        private DurationValue(final Duration duration) { this.duration=duration; }

        @Override public String encode(final URI base) {
            return string(duration);
        }

        @Override public Optional<Value> decode(final String value, final URI base) {
            return Optional.ofNullable(value(value)).map(Value::duration);
        }

        @Override public <V, E extends Exception> V accept(final ThrowingVisitor<V, E> visitor) throws E {
            return visitor.visit(this, duration);
        }

        @Override public boolean equals(final Object object) {
            return this == object || object instanceof final DurationValue value
                                     && duration.equals(value.duration);
        }

        @Override public int hashCode() {
            return duration.hashCode();
        }

        @Override public String toString() {
            return format("Duration(%s)", duration);
        }

    }


    private static final class TextValue extends Value {

        private static final Value MODEL=new TextValue("", ROOT);

        private static final Visitor<Entry<Locale, String>> VISITOR=new Visitor<>() {

            @Override public Entry<Locale, String> visit(final Value host, final Locale locale, final String string) {
                return entry(locale, string);
            }

        };

        private static final Pattern TextPattern=Pattern.compile("(?<string>.*?)(?:@(?<locale>\\w+))?");

        private final String string;
        private final Locale locale;

        private TextValue(final String string, final Locale locale) {
            this.string=string;
            this.locale=locale;
        }

        @Override public String encode(final URI base) {
            return locale.equals(ROOT) ? string : "%s@%s".formatted(string, locale.getLanguage());
        }

        @Override public Optional<Value> decode(final String value, final URI base) {
            try {

                return Optional.of(value)
                        .map(TextPattern::matcher)
                        .filter(Matcher::matches)
                        .map(matcher -> text(
                                Optional.ofNullable(matcher.group("locale"))
                                        .map(Locales::locale)
                                        .orElse(ROOT), matcher.group("string")
                        ));

            } catch ( final IllegalArgumentException e ) {
                return Optional.empty();
            }
        }

        @Override public <V, E extends Exception> V accept(final ThrowingVisitor<V, E> visitor) throws E {
            return visitor.visit(this, locale, string);
        }

        @Override public boolean equals(final Object object) {
            return this == object || object instanceof final TextValue value
                                     && string.equals(value.string)
                                     && locale.equals(value.locale);
        }

        @Override public int hashCode() {
            return string.hashCode()^locale.hashCode();
        }

        @Override public String toString() {
            return format("Text(%s@%s)", string, Locales.locale(locale));
        }

    }

    private static final class DataValue extends Value {

        private static final Value MODEL=new DataValue("", URIs.base());

        private static final Visitor<Entry<URI, String>> VISITOR=new Visitor<>() {

            @Override public Entry<URI, String> visit(final Value host, final URI datatype, final String string) {
                return entry(datatype, string);
            }

        };

        private static final Pattern DataPattern=Pattern.compile("(?<string>.*?)\\^\\^(?<datatype>\\S+)");

        private final String string;
        private final URI datatype;

        private DataValue(final String string, final URI datatype) {
            this.string=string;
            this.datatype=datatype;
        }

        @Override public String encode(final URI base) {
            return "%s^^%s".formatted(
                    string, URIs.relative(base, datatype)
            );
        }

        @Override public Optional<Value> decode(final String value, final URI base) {
            try {

                return Optional.of(value)
                        .map(DataPattern::matcher)
                        .filter(Matcher::matches)
                        .map(matcher -> data(
                                URIs.absolute(base, URI.create(matcher.group("datatype"))), matcher.group("string")
                        ));

            } catch ( final IllegalArgumentException e ) {
                return Optional.empty();
            }
        }

        @Override public <V, E extends Exception> V accept(final ThrowingVisitor<V, E> visitor) throws E {
            return visitor.visit(this, datatype, string);
        }

        @Override public boolean equals(final Object object) {
            return this == object || object instanceof final DataValue value
                                     && string.equals(value.string)
                                     && datatype.equals(value.datatype);
        }

        @Override public int hashCode() {
            return string.hashCode()^datatype.hashCode();
        }

        @Override public String toString() {
            return format("Data(%s^^<%s>)", string, datatype);
        }

    }

    private static final class ObjectValue extends Value {

        private static final Value MODEL=new ObjectValue(map());

        private static final Visitor<Map<String, Value>> VISITOR=new Visitor<>() {

            @Override public Map<String, Value> visit(final Value host, final Map<String, Value> fields) {
                return fields;
            }

        };

        private final Map<String, Value> fields;

        private ObjectValue(final Map<String, Value> fields) { this.fields=fields; }

        @Override public String encode(final URI base) {
            return id().map(id -> URIs.relative(base, id).toString()).orElse("");
        }

        @Override public Optional<Value> decode(final String value, final URI base) {
            try {

                return Optional.of(value.isEmpty() ? MODEL : object(id(URIs.absolute(base, URI.create(value)))));

            } catch ( final IllegalArgumentException e ) {
                return Optional.empty();
            }
        }

        @Override public <V, E extends Exception> V accept(final ThrowingVisitor<V, E> visitor) throws E {
            return visitor.visit(this, fields);
        }

        @Override public boolean equals(final Object object) {
            return this == object || object instanceof final ObjectValue value
                                     && fields.equals(value.fields);
        }

        @Override public int hashCode() {
            return fields.hashCode();
        }

        @Override public String toString() {
            return format("Object({%s})", fields.isEmpty() ? "" : fields.entrySet().stream()
                    .map(field -> format("%s: %s",
                            field.getKey(), field.getKey().equals(CONTEXT) ? "Shape(…)"
                                    : field.getValue().toString().replace("\n", "\n\t")
                    ))
                    .collect(joining(",\n\t", "\n\t", "\n"))
            );
        }

    }

    private static final class ArrayValue extends Value {

        private static final Value MODEL=new ArrayValue(list());

        private static final Visitor<List<Value>> VISITOR=new Visitor<>() {

            @Override public List<Value> visit(final Value host, final List<Value> values) {
                return values;
            }

        };

        private final List<Value> values;

        private ArrayValue(final List<Value> values) {
            this.values=values;
        }

        @Override public <V, E extends Exception> V accept(final ThrowingVisitor<V, E> visitor) throws E {
            return visitor.visit(this, values);
        }

        @Override public boolean equals(final Object object) {
            return this == object || object instanceof final ArrayValue value
                                     && values.equals(value.values);
        }

        @Override public int hashCode() {
            return values.hashCode();
        }

        @Override public String toString() {
            return format("Array([%s])", values.isEmpty() ? "" : values.stream()
                    .map(value -> value.toString().replace("\n", "\n\t"))
                    .collect(joining(",\n\t", "\n\t", "\n"))
            );
        }

    }


    private static final class GenericValue extends Value {

        private static final Visitor<Object> VISITOR=new Visitor<>() {

            @Override public Object visit(final Value host, final Object object) {
                return object;
            }

        };

        private final Object object;

        private GenericValue(final Object object) { this.object=object; }

        @Override public <R, E extends Exception> R accept(final ThrowingVisitor<R, E> visitor) throws E {
            return visitor.visit(this, object);
        }

        @Override public boolean equals(final Object object) {
            return this == object || object instanceof final GenericValue value
                                     && Objects.equals(this.object, value.object);
        }

        @Override public int hashCode() {
            return Objects.hashCode(object);
        }

        @Override public String toString() {
            return format("Generic([%s])", object);
        }

    }

}
