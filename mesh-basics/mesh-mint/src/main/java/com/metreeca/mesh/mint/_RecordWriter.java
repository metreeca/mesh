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

package com.metreeca.mesh.mint;

import com.metreeca.mesh.Field;
import com.metreeca.mesh.Value;
import com.metreeca.mesh.meta.Record;
import com.metreeca.mesh.shapes.Property;
import com.metreeca.mesh.shapes.Shape;
import com.metreeca.mesh.shapes.Type;
import com.metreeca.mesh.util.Strings;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URI;
import java.time.*;
import java.time.temporal.Temporal;
import java.time.temporal.TemporalAmount;
import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Stream;

import static com.metreeca.mesh.mint._RecordModel.*;
import static com.metreeca.mesh.util.Collections.*;
import static com.metreeca.mesh.util.Locales.locale;
import static com.metreeca.mesh.util.Strings.*;

import static java.util.function.Predicate.not;
import static java.util.stream.Collectors.joining;

public final class _RecordWriter {

    private static final Set<String> SPECIALS=set(

            TEXT,
            TEXTS,
            TEXTSETS,

            DATA,
            DATAS,
            DATASETS

    );

    private static final Set<Class<?>> IMPORTS=set(

            Value.class,
            Field.class,
            Shape.class,
            Property.class,
            Type.class,
            Record.class,

            Locale.class,
            Map.class,
            Entry.class,
            Set.class,
            List.class,
            Collection.class,

            URI.class,
            Temporal.class,
            Year.class,
            YearMonth.class,
            LocalDate.class,
            LocalTime.class,
            OffsetTime.class,
            LocalDateTime.class,
            OffsetDateTime.class,
            ZonedDateTime.class,
            Instant.class,
            TemporalAmount.class,
            Period.class,
            Duration.class

    );


    private static final Value.Visitor<String> DATATYPE_VISITOR=new Value.Visitor<>() {

        @Override public String visit(final Value host, final Boolean bit) { return "Bit"; }

        @Override public String visit(final Value host, final Number number) { return "Number"; }

        @Override public String visit(final Value host, final Long integral) { return "Integral"; }

        @Override public String visit(final Value host, final Double floating) { return "Floating"; }

        @Override public String visit(final Value host, final BigInteger integer) { return "Integer"; }

        @Override public String visit(final Value host, final BigDecimal decimal) { return "Decimal"; }

        @Override public String visit(final Value host, final String string) { return "String"; }

        @Override public String visit(final Value host, final URI uri) { return "URI"; }

        @Override public String visit(final Value host, final Temporal temporal) { return "Temporal"; }

        @Override public String visit(final Value host, final Year year) { return "Year"; }

        @Override public String visit(final Value host, final YearMonth yearMonth) { return "YearMonth"; }

        @Override public String visit(final Value host, final LocalDate localDate) { return "LocalDate"; }

        @Override public String visit(final Value host, final LocalTime localTime) { return "LocalTime"; }

        @Override public String visit(final Value host, final OffsetTime offsetTime) { return "OffsetTime"; }

        @Override public String visit(final Value host, final LocalDateTime localDateTime) { return "LocalDateTime"; }

        @Override public String visit(final Value host, final OffsetDateTime offsetDateTime) { return "OffsetDateTime"; }

        @Override public String visit(final Value host, final ZonedDateTime zonedDateTime) { return "ZonedDateTime"; }

        @Override public String visit(final Value host, final Instant instant) { return "Instant"; }

        @Override public String visit(final Value host, final TemporalAmount amount) { return "TemporalAmount"; }

        @Override public String visit(final Value host, final Period period) { return "Period"; }

        @Override public String visit(final Value host, final Duration duration) { return "Duration"; }

        @Override public String visit(final Value host, final Locale locale, final String string) { return "Text"; }

        @Override public String visit(final Value host, final URI datatype, final String string) { return "Data"; }

        @Override public String visit(final Value host, final Map<String, Value> fields) { return "Object"; }

    };

    private static final Value.Visitor<String> VALUE_VISITOR=new Value.Visitor<>() {

        @Override public String visit(final Value host, final Boolean bit) {
            return "Value.bit(%s)".formatted(host.encode());
        }

        @Override public String visit(final Value host, final Number number) {
            return number instanceof final BigInteger integer ? visit(host, integer)
                    : number instanceof final BigDecimal decimal ? visit(host, decimal)
                    : "Value.number(%s)".formatted(host.encode());
        }

        @Override public String visit(final Value host, final Long integral) {
            return "Value.integral(%s)".formatted(host.encode());
        }

        @Override public String visit(final Value host, final Double floating) {
            return "Value.floating(%s)".formatted(host.encode());
        }

        @Override public String visit(final Value host, final BigInteger integer) {
            return "Value.integer(new BigInteger(%s))".formatted(quote(host.encode()));
        }

        @Override public String visit(final Value host, final BigDecimal decimal) {
            return "Value.decimal(new BigDecimal(%s))".formatted(quote(host.encode()));
        }

        @Override public String visit(final Value host, final String string) {
            return quote(host.encode());
        }

        @Override public String visit(final Value host, final URI uri) {
            return "Value.uri(URI.create(%s))".formatted(quote(host.encode()));
        }

        @Override public String visit(final Value host, final Temporal temporal) {
            return "Value.Temporal().decode(%s)".formatted(quote(host.encode()));
        }

        @Override public String visit(final Value host, final Year year) {
            return "Value.Year().decode(%s)".formatted(quote(host.encode()));
        }

        @Override public String visit(final Value host, final YearMonth yearMonth) {
            return "Value.YearMonth().decode(%s)".formatted(quote(host.encode()));
        }

        @Override public String visit(final Value host, final LocalDate localDate) {
            return "Value.LocalDate().decode(%s)".formatted(quote(host.encode()));
        }

        @Override public String visit(final Value host, final LocalTime localTime) {
            return "Value.LocalTime().decode(%s)".formatted(quote(host.encode()));
        }

        @Override public String visit(final Value host, final OffsetTime offsetTime) {
            return "Value.OffsetTime().decode(%s)".formatted(quote(host.encode()));
        }

        @Override public String visit(final Value host, final LocalDateTime localDateTime) {
            return "Value.LocalDateTime().decode(%s)".formatted(quote(host.encode()));
        }

        @Override public String visit(final Value host, final OffsetDateTime offsetDateTime) {
            return "Value.OffsetDateTime().decode(%s)".formatted(quote(host.encode()));
        }

        @Override public String visit(final Value host, final ZonedDateTime zonedDateTime) {
            return "Value.ZonedDateTime().decode(%s)".formatted(quote(host.encode()));
        }

        @Override public String visit(final Value host, final Instant instant) {
            return "Value.Instant().decode(%s)".formatted(quote(host.encode()));
        }

        @Override public String visit(final Value host, final TemporalAmount amount) {
            return "Value.TemporalAmount().decode(%s)".formatted(quote(host.encode()));
        }

        @Override public String visit(final Value host, final Period period) {
            return "Value.Integer().decode(\"%s\")".formatted(host.encode());
        }

        @Override public String visit(final Value host, final Duration duration) {
            return "Value.Duration().decode(%s)".formatted(quote(host.encode()));
        }

        @Override public String visit(final Value host, final Locale locale, final String string) {
            return "Value.Text().decode(%s)".formatted(quote(host.encode()));
        }

        @Override public String visit(final Value host, final URI datatype, final String string) {
            return "Value.Data().decode(%s)".formatted(quote(host.encode()));
        }

        @Override public String visit(final Value host, final Map<String, Value> fields) {
            return Field.id(host)
                    .map("Value.object(Field.id(URI.create(\"%s\"))"::formatted)
                    .orElseGet(() -> "Value.Object()");
        }

    };


    static String record(final String name) {
        return "%sRecord".formatted(name);
    }


    //̸/////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private final Info info;


    _RecordWriter(final Info info) {
        this.info=info;
    }


    //̸// Record ///////////////////////////////////////////////////////////////////////////////////////////////////////

    String generate() {
        return pipe(
                w -> prettify(w, 4),
                w -> fold(w, 2)
        ).process(source());
    }


    //̸/////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private String source() {
        return fill("""
                package {pkg};
                
                {imports}
                
                public record {Record}(
                
                    {fields}
                
                ) implements {Clazz}, Record {
                
                    {shape}
                
                
                    {emptyFactory}
                
                    {objectFactory}
                
                    {valueFactory}
                
                
                    {constructor}
                
                
                    {getters}
                
                
                    {mutators}
                
                
                    {converter}
                
                    {formatter}
                
                }""", map(

                entry("pkg", pkg()),
                entry("imports", imports()),

                entry("Record", record()),
                entry("Clazz", clazz()),
                entry("fields", fields()),
                entry("shape", shape()),

                entry("emptyFactory", emptyFactory()),
                entry("objectFactory", objectFactory()),
                entry("valueFactory", valueFactory()),

                entry("constructor", constructor()),
                entry("getters", getters()),
                entry("mutators", mutators()),

                entry("converter", converter()),
                entry("formatter", formatter())

        ));
    }


    private String pkg() {
        return info.packageName();
    }

    private String imports() {
        return IMPORTS.stream()
                .map(Class::getCanonicalName)
                .sorted()
                .map("import %s;"::formatted)
                .collect(joining("\n"));
    }


    private String clazz() {
        return info.className();
    }

    private String record() {
        return record(clazz());
    }


    private String fields() {
        return info.slots().stream()
                .map(slot -> "%s %s".formatted(
                        slot.generic(),
                        slot.name()
                ))
                .collect(joining(",\n"));
    }


    //̸// Shape ////////////////////////////////////////////////////////////////////////////////////////////////////////

    private String shape() {
        return fold(fill("""
                public static final Shape {Class}=Shape.shape()‹
                    {virtual}
                    {id}
                    {type}
                    {class}
                    {constraints}
                    {properties};›""", Map.of(

                "Class", clazz(),

                "virtual", virtual(info.shape()),
                "id", id(info.shape()),
                "type", type(info.shape()),
                "class", clazz(info.shape()),

                "constraints", constraints(info.constraints()),
                "properties", properties(info.slots())

        )));
    }


    private static String virtual(final Shape shape) {
        return shape.virtual() ? ".virtual(true)" : "";
    }

    private String id(final Shape shape) {
        return shape.id().map(".id(\"%s\")"::formatted).orElse("");
    }

    private String type(final Shape shape) {
        return shape.type().map(".type(\"%s\")"::formatted).orElse("");
    }


    private String clazz(final Shape shape) {
        return shape.clazz()
                .map(explicit -> shape.clazzes()
                        .filter(not(s -> s.equals(set(explicit))))
                        .map(implicits -> clazz(explicit, implicits))
                        .orElseGet(() -> clazz(explicit))
                )
                .or(() -> shape.clazzes().map(this::clazzes))
                .orElse("");
    }

    private String clazz(final Type explicit) {
        return ".clazz(%s)".formatted(type(explicit));
    }

    private String clazz(final Type explicit, final Set<Type> implicits) {
        return fill("""
                .clazz({explicit},
                    {implicit}
                )""", Map.of(

                "explicit", type(explicit),
                "implicit", types(implicits.stream().filter(not(explicit::equals)).toList())
        ));
    }

    private String clazzes(final Collection<Type> implicits) {
        return fill("""
                .clazzes(
                    {types}
                )""", Map.of(

                "types", types(implicits)

        ));
    }


    private String types(final Collection<Type> types) {
        return types.stream().map(this::type).collect(joining(",\n"));
    }

    private String type(final Type type) {
        return "Type.type(\"%s\", URI.create(\"%s\"))".formatted(type.name(), type.uri());
    }


    private String constraints(final Set<String> constraints1) {
        return Optional.of(constraints1)
                .filter(not(Set::isEmpty))
                .map(constraints -> fill("""
                        .constraints(
                            {constraints}
                        )""", Map.of(

                        "constraints", constraints.stream()
                                .map(this::constraint)
                                .collect(joining(",\n"))

                )))
                .orElse("");
    }

    private String constraint(final String constraint) {
        return "v -> new %s().apply(Sample(v))".formatted(constraint);
    }


    private String properties(final Set<Slot> slots) {
        return slots.stream()
                .filter(not(Slot::internal))
                .map(slot -> property(slot.property(), slot.item() != null ? slot.item() : slot.base()))
                .collect(joining("\n"));
    }

    private String property(final Property property, final String type) {
        return fill("""
                .property(Property.property("{name}", URI.create("{uri}"))
                    {embedded}
                    {reverse}
                    {shape}
                )""", Map.of(

                "name", property.name(),
                "uri", property.uri().toString(),
                "embedded", embedded(property),
                "reverse", reverse(property),
                "shape", shape(property.shape(), type)
        ));
    }


    private String embedded(final Property property) {
        return property.embedded() ? ".embedded(true)" : "";
    }

    private String reverse(final Property property) {
        return property.reverse() ? ".reverse(true)" : "";
    }


    private String shape(final Shape shape, final String type) {

        final boolean object=shape.datatype().filter(dt -> dt.equals(Value.Object())).isPresent();

        return shape.equals(Shape.shape()) ? "" : fill("""
                .shape(() -> {shape}‹
                    {virtual}
                    {datatype}
                    {minExclusive}
                    {maxExclusive}
                    {minInclusive}
                    {maxInclusive}
                    {minLength}
                    {maxLength}
                    {pattern}
                    {in}
                    {languageIn}
                    {uniqueLang}
                    {minCount}
                    {maxCount}
                    {hasValue}
                ›)""", Map.ofEntries(

                entry("shape", object ? "%s.%s".formatted(record(type), simple(type)) : "Shape.shape()"),

                entry("virtual", virtual(shape)),
                entry("datatype", object ? "" : datatype(shape)),

                entry("minExclusive", minExclusive(shape)),
                entry("maxExclusive", maxExclusive(shape)),
                entry("minInclusive", minInclusive(shape)),
                entry("maxInclusive", maxInclusive(shape)),
                entry("minLength", minLength(shape)),
                entry("maxLength", maxLength(shape)),
                entry("pattern", pattern(shape)),
                entry("in", in(shape)),
                entry("languageIn", languageIn(shape)),

                entry("uniqueLang", uniqueLang(shape)),
                entry("minCount", minCount(shape)),
                entry("maxCount", maxCount(shape)),
                entry("hasValue", hasValue(shape))

        ));
    }

    private static String datatype(final Shape shape) {
        return shape.datatype()
                .map(datatype -> ".datatype(Value.%s())".formatted(datatype.accept(DATATYPE_VISITOR)))
                .orElse("");
    }

    private String minExclusive(final Shape shape) {
        return shape.minExclusive().map(v -> ".minExclusive(%s)".formatted(value(v))).orElse("");
    }

    private String maxExclusive(final Shape shape) {
        return shape.maxExclusive().map(v -> ".maxExclusive(%s)".formatted(value(v))).orElse("");
    }

    private String minInclusive(final Shape shape) {
        return shape.minInclusive().map(v -> ".minInclusive(%s)".formatted(value(v))).orElse("");
    }

    private String maxInclusive(final Shape shape) {
        return shape.maxInclusive().map(v -> ".maxInclusive(%s)".formatted(value(v))).orElse("");
    }

    private String minLength(final Shape shape) {
        return shape.minLength().map(".minLength(%d)"::formatted).orElse("");
    }

    private String maxLength(final Shape shape) {
        return shape.maxLength().map(".maxLength(%d)"::formatted).orElse("");
    }

    private String pattern(final Shape shape) {
        return shape.pattern()
                .map(Strings::quote)
                .map(".pattern(%s)"::formatted)
                .orElse("");
    }

    private String in(final Shape shape) {
        return shape.in()
                .map(values -> ".in(%s)".formatted(values.stream()
                        .map(this::value)
                        .collect(joining(", "))
                ))
                .orElse("");
    }

    private String languageIn(final Shape shape) {
        return shape.languageIn()
                .map(locales -> ".languageIn(%s)".formatted(locales.stream()
                        .map(locale -> "\"%s\"".formatted(locale(locale)))
                        .collect(joining(", "))
                )).orElse("");
    }

    private String uniqueLang(final Shape shape) {
        return shape.uniqueLang() ? ".uniqueLang(true)" : "";
    }

    private String minCount(final Shape shape) {
        return shape.minCount().map(".minCount(%d)"::formatted).orElse("");
    }

    private String maxCount(final Shape shape) {
        return shape.maxCount().map(".maxCount(%d)"::formatted).orElse("");
    }

    @SuppressWarnings("NonBooleanMethodNameMayNotStartWithQuestion")
    private String hasValue(final Shape shape) {
        return shape.in()
                .map(values -> ".hasValue(%s)".formatted(values.stream()
                        .map(this::value)
                        .collect(joining(", "))
                ))
                .orElse("");
    }


    private String value(final Value value) {
        return value.accept(VALUE_VISITOR);
    }


    //̸// Factories ////////////////////////////////////////////////////////////////////////////////////////////////////

    private String emptyFactory() {
        return fill("""
                public static {Record} {Class}() {
                    return new {Record}(
                        {fields}
                    );
                }""", Map.of(

                "Record", record(),
                "Class", clazz(),

                "fields", info.slots().stream()
                        .map(slot -> initial(slot.generic()))
                        .collect(joining(",\n"))

        ));
    }

    private String objectFactory() {
        return fill("""
                public static {Record} {Class}(final {Class} object) {
                
                    if ( object == null ) {
                        throw new NullPointerException("null object");
                    }
                
                    return object instanceof final {Record} frame ? frame : new {Record}(
                        {fields}
                    );
                }""", Map.of(

                "Record", record(),
                "Class", clazz(),

                "fields", info.slots().stream()
                        .map(slot -> "object.%s()".formatted(slot.name()))
                        .collect(joining(",\n"))

        ));
    }

    private String valueFactory() {
        return fill("""
                public static {Record} {Class}(final Value value) {
                
                    if ( value == null ) {
                        throw new NullPointerException("null value");
                    }
                
                    return new {Record}(
                        {fields}
                    );
                }""", Map.of(

                "Record", record(),
                "Class", clazz(),

                "fields", info.slots().stream()
                        .map(slot -> slot.internal() ? null
                                : slot.id() ? "Record.id(value, URI.create(\"%s\"))".formatted(info.base())
                                : slot.type() ? "Record.type(value)"
                                : slot.collection() ? toCollection(slot)
                                : toScalar(slot)
                        )
                        .collect(joining(",\n"))

        ));
    }


    private String initial(final String type) {
        return switch ( type ) {
            case "boolean" -> "false";
            case "byte", "short", "int" -> "0";
            case "long" -> "0L";
            case "float" -> "0.0F";
            case "double" -> "0.0D";
            default -> "null";
        };
    }


    //̸// Instance Methods /////////////////////////////////////////////////////////////////////////////////////////////

    private String constructor() {
        return fill("""
                public {Record}(
                    {fields}
                ) {
                    {initializers}
                }""", Map.of(

                "Record", record(),

                "fields", info.slots().stream()
                        .map(slot -> "final %s %s".formatted(slot.generic(), slot.name()))
                        .collect(joining(",\n")),

                "initializers", info.slots().stream()
                        .map(slot -> slot.collection() || SPECIALS.contains(slot.base())
                                ? "this.%s=Record.ensureImmutable(%s, \"%s\");".formatted(slot.name(), slot.name(), slot.name())
                                : "this.%s=%s;".formatted(slot.name(), slot.name())
                        )
                        .collect(joining("\n"))

        ));
    }

    private String getters() { // special getters for default methods
        return info.slots().stream()
                .filter(Slot::dflt)
                .map(slot -> fill("""
                        public {type} {name}() {
                            return {name} != null ? {name} : {Class}.super.{name}();
                        }""", Map.of(

                        "type", slot.generic(),
                        "name", slot.name(),
                        "Class", clazz()

                )))
                .collect(joining("\n\n"));
    }

    private String mutators() {
        return info.slots().stream()
                .map(slot -> fill("""
                        public {Record} {name}(final {type} {name}) {
                            return new {Record}(
                                {fields}
                            );
                        }""", Map.of(

                        "Record", record(),
                        "name", slot.name(),
                        "type", slot.generic(),

                        "fields", info.slots().stream()
                                .map(Slot::name)
                                .collect(joining(",\n"))

                )))
                .collect(joining("\n\n"));
    }


    private String converter() {
        return fill("""
                @Override
                public Value toValue(final boolean model) {
                    return Value.object(
                        {fields}
                    );
                }""", Map.of(

                "Class", clazz(),

                        "fields", Stream.concat(

                        Stream.of("Field.shape(%s)".formatted(clazz())),

                        info.slots().stream()
                                .filter(not(Slot::internal))
                                .map(slot -> slot.id() ? toID(slot)
                                        : slot.type() ? toType(slot)
                                        : toField(slot)
                                )

                        ).collect(joining(",\n"))

                )
        );
    }

    private String formatter() {
        return fill("""
                @Override
                public String toString() {
                    return toValue(false).toString();
                }"""
        );
    }


    //̸// Value To Native Conversion ///////////////////////////////////////////////////////////////////////////////////

    private static String toScalar(final Slot slot) {

        final String value="value.get(\"%s\")".formatted(slot.name());

        return switch ( slot.base() ) {

            case "boolean" -> "Record._boolean(%s)".formatted(value);
            case "byte" -> "Record._byte(%s)".formatted(value);
            case "short" -> "Record._short(%s)".formatted(value);
            case "int" -> "Record._int(%s)".formatted(value);
            case "long" -> "Record._long(%s)".formatted(value);
            case "float" -> "Record._float(%s)".formatted(value);
            case "double" -> "Record._double(%s)".formatted(value);

            case "Boolean" -> "Record.bit(%s)".formatted(value);
            case "Number" -> "Record.number(%s)".formatted(value);
            case "Long" -> "Record.integral(%s)".formatted(value);
            case "Double" -> "Record.floating(%s)".formatted(value);
            case "BigInteger" -> "Record.integer(%s)".formatted(value);
            case "BigDecimal" -> "Record.decimal(%s)".formatted(value);
            case "String" -> "Record.string(%s)".formatted(value);
            case "URI" -> "Record.uri(%s)".formatted(value);
            case "Temporal" -> "Record.temporal(%s)".formatted(value);
            case "Year" -> "Record.year(%s)".formatted(value);
            case "YearMonth" -> "Record.yearMonth(%s)".formatted(value);
            case "LocalDate" -> "Record.localDate(%s)".formatted(value);
            case "LocalTime" -> "Record.localTime(%s)".formatted(value);
            case "OffsetTime" -> "Record.offsetTime(%s)".formatted(value);
            case "LocalDateTime" -> "Record.localDateTime(%s)".formatted(value);
            case "OffsetDateTime" -> "Record.offsetDateTime(%s)".formatted(value);
            case "ZonedDateTime" -> "Record.zonedDateTime(%s)".formatted(value);
            case "Instant" -> "Record.instant(%s)".formatted(value);
            case "TemporalAmount" -> "Record.temporalAmount(%s)".formatted(value);
            case "Period" -> "Record.period(%s)".formatted(value);
            case "Duration" -> "Record.duration(%s)".formatted(value);

            case TEXT -> "Record.text(%s)".formatted(value);
            case TEXTS -> "Record.texts(%s)".formatted(value);
            case TEXTSETS -> "Record.textsets(%s)".formatted(value);

            case DATA -> "Record.data(%s)".formatted(value);
            case DATAS -> "Record.datas(%s)".formatted(value);
            case DATASETS -> "Record.datasets(%s)".formatted(value);

            default -> "Record.<%s>object(value.get(\"%s\"), %sRecord::%s)".formatted(
                    slot.base(), slot.name(), slot.base(), simple(slot.base())
            );

        };
    }

    private static String toCollection(final Slot slot) {

        final String kind=slot.set() ? "set" : "list";
        final String value="value.get(\"%s\")".formatted(slot.name());

        return switch ( slot.item() ) {

            case "Boolean" -> "Record.%s(Record.bits(%s))".formatted(kind, value);
            case "Number" -> "Record.%s(Record.numbers(%s))".formatted(kind, value);
            case "Long" -> "Record.%s(Record.integrals(%s))".formatted(kind, value);
            case "Double" -> "Record.%s(Record.floatings(%s))".formatted(kind, value);
            case "BigInteger" -> "Record.%s(Record.integers(%s))".formatted(kind, value);
            case "BigDecimal" -> "Record.%s(Record.decimals(%s))".formatted(kind, value);
            case "String" -> "Record.%s(Record.strings(%s))".formatted(kind, value);
            case "URI" -> "Record.%s(Record.uris(%s))".formatted(kind, value);
            case "Temporal" -> "Record.%s(Record.temporals(%s))".formatted(kind, value);
            case "Year" -> "Record.%s(Record.years(%s))".formatted(kind, value);
            case "YearMonth" -> "Record.%s(Record.yearMonths(%s))".formatted(kind, value);
            case "LocalDate" -> "Record.%s(Record.localDates(%s))".formatted(kind, value);
            case "LocalTime" -> "Record.%s(Record.localTimes(%s))".formatted(kind, value);
            case "OffsetTime" -> "Record.%s(Record.offsetTimes(%s))".formatted(kind, value);
            case "LocalDateTime" -> "Record.%s(Record.localDateTimes(%s))".formatted(kind, value);
            case "OffsetDateTime" -> "Record.%s(Record.offsetDateTimes(%s))".formatted(kind, value);
            case "ZonedDateTime" -> "Record.%s(Record.zonedDateTimes(%s))".formatted(kind, value);
            case "Instant" -> "Record.%s(Record.instants(%s))".formatted(kind, value);
            case "TemporalAmount" -> "Record.%s(Record.temporalAmounts(%s))".formatted(kind, value);
            case "Period" -> "Record.%s(Record.periods(%s))".formatted(kind, value);
            case "Duration" -> "Record.%s(Record.durations(%s))".formatted(kind, value);

            default -> "Record.%s(Record.objects(value.get(\"%s\"), %sRecord::%s))".formatted(
                    kind, slot.name(), slot.item(), simple(slot.item())
            );

        };
    }


    //̸// Native To Value Conversion ///////////////////////////////////////////////////////////////////////////////////

    private String toID(final Slot slot) {
        return "Field.id(Record.id(%s(), URI.create(\"%s\")))".formatted(slot.name(), info.base());
    }

    private String toType(final Slot slot) {
        return "Field.type(Record.type(%s()))".formatted(slot.name());
    }

    private String toField(final Slot slot) {
        return "Field.field(\"%s\", %s)".formatted(slot.name(), slot.collection() ? toValues(slot) : toValue(slot));
    }

    private String toValue(final Slot slot) {

        final String value=toModel(slot);

        return switch ( slot.base() ) {

            case "boolean" -> "Record._boolean(%s)".formatted(value);
            case "byte" -> "Record._byte(%s)".formatted(value);
            case "short" -> "Record._short(%s)".formatted(value);
            case "int" -> "Record._int(%s)".formatted(value);
            case "long" -> "Record._long(%s)".formatted(value);
            case "float" -> "Record._float(%s)".formatted(value);
            case "double" -> "Record._double(%s)".formatted(value);

            case "Boolean" -> "Record.bit(%s)".formatted(value);
            case "Number" -> "Record.number(%s)".formatted(value);
            case "Long" -> "Record.integral(%s)".formatted(value);
            case "Double" -> "Record.floating(%s)".formatted(value);
            case "BigInteger" -> "Record.integer(%s)".formatted(value);
            case "BigDecimal" -> "Record.decimal(%s)".formatted(value);
            case "String" -> "Record.string(%s)".formatted(value);
            case "URI" -> "Record.uri(%s)".formatted(value);
            case "Temporal" -> "Record.temporal(%s)".formatted(value);
            case "Year" -> "Record.year(%s)".formatted(value);
            case "YearMonth" -> "Record.yearMonth(%s)".formatted(value);
            case "LocalDate" -> "Record.localDate(%s)".formatted(value);
            case "LocalTime" -> "Record.localTime(%s)".formatted(value);
            case "OffsetTime" -> "Record.offsetTime(%s)".formatted(value);
            case "LocalDateTime" -> "Record.localDateTime(%s)".formatted(value);
            case "OffsetDateTime" -> "Record.offsetDateTime(%s)".formatted(value);
            case "ZonedDateTime" -> "Record.zonedDateTime(%s)".formatted(value);
            case "Instant" -> "Record.instant(%s)".formatted(value);
            case "TemporalAmount" -> "Record.temporalAmount(%s)".formatted(value);
            case "Period" -> "Record.period(%s)".formatted(value);
            case "Duration" -> "Record.duration(%s)".formatted(value);

            case TEXT -> "Record.text(%s)".formatted(value);
            case TEXTS -> "Record.texts(%s)".formatted(value);
            case TEXTSETS -> "Record.textsets(%s)".formatted(value);

            case DATA -> "Record.data(%s)".formatted(value);
            case DATAS -> "Record.datas(%s)".formatted(value);
            case DATASETS -> "Record.datasets(%s)".formatted(value);

            default -> "Record.object(%s, v -> %sRecord.%s(v).toValue(model))".formatted(
                    value, slot.base(), simple(slot.base())
            );

        };
    }

    private String toValues(final Slot slot) {

        final String value=toModel(slot);

        return switch ( slot.item() ) {

            case "Boolean" -> "Record.bits(%s)".formatted(value);
            case "Number" -> "Record.numbers(%s)".formatted(value);
            case "Long" -> "Record.integrals(%s)".formatted(value);
            case "Double" -> "Record.floatings(%s)".formatted(value);
            case "BigInteger" -> "Record.integers(%s)".formatted(value);
            case "BigDecimal" -> "Record.decimals(%s)".formatted(value);
            case "String" -> "Record.strings(%s)".formatted(value);
            case "URI" -> "Record.uris(%s)".formatted(value);
            case "Temporal" -> "Record.temporals(%s)".formatted(value);
            case "Year" -> "Record.years(%s)".formatted(value);
            case "YearMonth" -> "Record.yearMonths(%s)".formatted(value);
            case "LocalDate" -> "Record.localDates(%s)".formatted(value);
            case "LocalTime" -> "Record.localTimes(%s)".formatted(value);
            case "OffsetTime" -> "Record.offsetTimes(%s)".formatted(value);
            case "LocalDateTime" -> "Record.localDateTimes(%s)".formatted(value);
            case "OffsetDateTime" -> "Record.offsetDateTimes(%s)".formatted(value);
            case "ZonedDateTime" -> "Record.zonedDateTimes(%s)".formatted(value);
            case "Instant" -> "Record.instants(%s)".formatted(value);
            case "TemporalAmount" -> "Record.temporalAmounts(%s)".formatted(value);
            case "Period" -> "Record.periods(%s)".formatted(value);
            case "Duration" -> "Record.durations(%s)".formatted(value);

            default -> "Record.objects(%s, v -> %sRecord.%s(v).toValue(model))".formatted(
                    value, slot.item(), simple(slot.item())
            );

        };
    }

    private String toModel(final Slot slot) {
        return slot.dflt()

                ? fill("model ? (this.{name} == null ? null : {Class}.super.{name}()) : {name}()", Map.of(
                "Class", clazz(),
                "name", slot.name()
        ))

                : "%s()".formatted(slot.name());
    }


    //̸/////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private static String simple(final String type) {
        return type.substring(type.lastIndexOf('.')+1);
    }

}
