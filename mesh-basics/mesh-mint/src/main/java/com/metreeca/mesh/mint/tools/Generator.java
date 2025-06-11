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

package com.metreeca.mesh.mint.tools;

import com.metreeca.mesh.Valuable;
import com.metreeca.mesh.Value;
import com.metreeca.mesh.meta.Values;
import com.metreeca.mesh.mint.ast.Clazz;
import com.metreeca.mesh.mint.ast.Method;
import com.metreeca.mesh.shapes.Property;
import com.metreeca.mesh.shapes.Shape;
import com.metreeca.mesh.shapes.Type;
import com.metreeca.shim.Collections;
import com.metreeca.shim.Strings;
import com.metreeca.shim.URIs;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URI;
import java.time.*;
import java.time.temporal.Temporal;
import java.time.temporal.TemporalAmount;
import java.util.*;
import java.util.Map.Entry;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static com.metreeca.mesh.mint.ast.Method.*;
import static com.metreeca.shim.Collections.*;
import static com.metreeca.shim.Locales.locale;
import static com.metreeca.shim.Strings.*;
import static com.metreeca.shim.URIs.uri;

import static java.util.function.Predicate.not;
import static java.util.stream.Collectors.joining;

/**
 * Code generator for data model frames.
 *
 * <p>Generates Java implementations from interface-based data models, handling property transformations,
 * type conversion, validation constraints, and nested object models.</p>
 *
 * <p>This generator is responsible for:</p>
 * <ul>
 *   <li>Converting interface-based data models into concrete Java implementations</li>
 *   <li>Generating bidirectional mapping between domain objects and {@link Value} instances</li>
 *   <li>Implementing shape constraints from annotations as SHACL-compatible validation rules</li>
 *   <li>Creating property accessors, mutators and factory methods for data model objects</li>
 *   <li>Supporting nested object hierarchies and collections with proper type handling</li>
 *   <li>Ensuring immutability and proper null handling for all generated code</li>
 * </ul>
 *
 * <p>The implementation relies on AST representations ({@link Clazz}, {@link Method}) obtained through
 * introspection to analyze model interfaces and generate corresponding implementations.</p>
 */
public final class Generator {

    private static final Set<Class<?>> IMPORTS=set(

            Value.class,
            Valuable.class,
            Shape.class,
            Property.class,
            Type.class,
            Values.class,
            Collections.class,
            URIs.class,

            Entry.class,
            Locale.class,
            List.class,
            Map.class,
            Objects.class,
            Optional.class,
            Predicate.class,
            Set.class,
            Stream.class,

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
            return "Value.bit(%s)".formatted(host.encode(uri()));
        }

        @Override public String visit(final Value host, final Number number) {
            return number instanceof final BigInteger integer ? visit(host, integer)
                    : number instanceof final BigDecimal decimal ? visit(host, decimal)
                    : "Value.number(%s)".formatted(host.encode(uri()));
        }

        @Override public String visit(final Value host, final Long integral) {
            return "Value.integral(%s)".formatted(host.encode(uri()));
        }

        @Override public String visit(final Value host, final Double floating) {
            return "Value.floating(%s)".formatted(host.encode(uri()));
        }

        @Override public String visit(final Value host, final BigInteger integer) {
            return "Value.integer(new BigInteger(%s))".formatted(quote(host.encode(uri())));
        }

        @Override public String visit(final Value host, final BigDecimal decimal) {
            return "Value.decimal(new BigDecimal(%s))".formatted(quote(host.encode(uri())));
        }

        @Override public String visit(final Value host, final String string) {
            return "Value.string(%s)".formatted(quote(host.encode(uri())));
        }

        @Override public String visit(final Value host, final URI uri) {
            return "Value.uri(URI.create(%s))".formatted(quote(host.encode(uri())));
        }

        @Override public String visit(final Value host, final Temporal temporal) {
            return "Value.Temporal().decode(%s)".formatted(quote(host.encode(uri())));
        }

        @Override public String visit(final Value host, final Year year) {
            return "Value.Year().decode(%s)".formatted(quote(host.encode(uri())));
        }

        @Override public String visit(final Value host, final YearMonth yearMonth) {
            return "Value.YearMonth().decode(%s)".formatted(quote(host.encode(uri())));
        }

        @Override public String visit(final Value host, final LocalDate localDate) {
            return "Value.LocalDate().decode(%s)".formatted(quote(host.encode(uri())));
        }

        @Override public String visit(final Value host, final LocalTime localTime) {
            return "Value.LocalTime().decode(%s)".formatted(quote(host.encode(uri())));
        }

        @Override public String visit(final Value host, final OffsetTime offsetTime) {
            return "Value.OffsetTime().decode(%s)".formatted(quote(host.encode(uri())));
        }

        @Override public String visit(final Value host, final LocalDateTime localDateTime) {
            return "Value.LocalDateTime().decode(%s)".formatted(quote(host.encode(uri())));
        }

        @Override public String visit(final Value host, final OffsetDateTime offsetDateTime) {
            return "Value.OffsetDateTime().decode(%s)".formatted(quote(host.encode(uri())));
        }

        @Override public String visit(final Value host, final ZonedDateTime zonedDateTime) {
            return "Value.ZonedDateTime().decode(%s)".formatted(quote(host.encode(uri())));
        }

        @Override public String visit(final Value host, final Instant instant) {
            return "Value.Instant().decode(%s)".formatted(quote(host.encode(uri())));
        }

        @Override public String visit(final Value host, final TemporalAmount amount) {
            return "Value.TemporalAmount().decode(%s)".formatted(quote(host.encode(uri())));
        }

        @Override public String visit(final Value host, final Period period) {
            return "Value.Integer().decode(\"%s\")".formatted(host.encode(uri()));
        }

        @Override public String visit(final Value host, final Duration duration) {
            return "Value.Duration().decode(%s)".formatted(quote(host.encode(uri())));
        }

        @Override public String visit(final Value host, final Locale locale, final String string) {
            return "Value.Text().decode(%s)".formatted(quote(host.encode(uri())));
        }

        @Override public String visit(final Value host, final URI datatype, final String string) {
            return "Value.Data().decode(%s)".formatted(quote(host.encode(uri())));
        }

        @Override public String visit(final Value host, final Map<String, Value> fields) {
            return host.id()
                    .map("Value.object(Value.id(URI.create(\"%s\")))"::formatted)
                    .orElse("Value.Object()");
        }

    };


    /**
     * Creates a frame name from a class name.
     *
     * @param name the base class name
     *
     * @return the frame name derived from the class name by appending "Frame"
     */
    public static String frame(final String name) {
        return "%sFrame".formatted(name);
    }


    //̸/////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private final Clazz clazz;


    /**
     * Creates a new generator for the specified class.
     *
     * @param clazz the model interface to generate a implementation for
     *
     * @throws NullPointerException if {@code clazz} is {@code null}
     */
    public Generator(final Clazz clazz) {

        if ( clazz == null ) {
            throw new NullPointerException("null clazz");
        }

        this.clazz=clazz.flattened();
    }


    /**
     * Generates a Java implementation for the model interface.
     *
     * <p>The implementation includes:</p>
     * <ul>
     *   <li>Declaration with fields corresponding to model properties</li>
     *   <li>Shape definition with validation constraints derived from annotations</li>
     *   <li>Factory methods for creating instances from different sources</li>
     *   <li>Accessor and mutator methods preserving immutability</li>
     *   <li>Conversion methods between domain objects and {@link Value} instances</li>
     * </ul>
     *
     * @return the complete Java source code for the implementation
     */
    public String generate() {
        return pipe(
                w -> prettify(w, 4),
                w -> fold(w, 3)
        ).process(source());
    }


    //̸// Frame ////////////////////////////////////////////////////////////////////////////////////////////////////////

    private String source() {
        return fill("""
                package {pkg};
                
                {imports}
                
                public final class {Frame} implements {Clazz}, Valuable {
                
                    {BASE}
                
                    {SHAPE}
                
                
                    {toFrame}
                
                    {toValue}
                
                    {toToken}
                
                
                    private final boolean $delta;
                    private final Predicate<String> $touched;
                
                    {fields}
                
                
                    {emptyConstructor}
                
                    {copyConstructor}
                
                    {valueConstructor}
                
                
                    {internalConstructor}
                
                
                    {accessors}
                
                
                    {converter}
                
                    {formatter}
                
                }""", map(

                entry("pkg", pkg()),
                entry("imports", imports()),

                entry("Frame", frame()),
                entry("Clazz", clazz()),

                entry("BASE", base()),
                entry("SHAPE", shape()),

                entry("toFrame", toFrame()),
                entry("toValue", toValue()),
                entry("toToken", toToken()),

                entry("fields", fields()),

                entry("emptyConstructor", emptyConstructor()),
                entry("copyConstructor", copyConstructor()),
                entry("valueConstructor", valueConstructor()),
                entry("internalConstructor", internalConstructor()),

                entry("accessors", accessors()),

                entry("converter", converter()),
                entry("formatter", formatter())

        ));
    }


    private String pkg() {
        return clazz.pkg();
    }

    private String imports() {
        return IMPORTS.stream()
                .map(Class::getCanonicalName)
                .sorted()
                .map("import %s;"::formatted)
                .collect(joining("\n"));
    }


    private String clazz() {
        return clazz.name();
    }

    private String frame() {
        return frame(clazz());
    }


    private String base() {
        return "public static final URI BASE=URI.create(\"%s\");".formatted(clazz.base());
    }


    //̸// Shape ////////////////////////////////////////////////////////////////////////////////////////////////////////

    private String shape() {
        return fold(fill("""
                public static final Shape SHAPE=Shape.shape()‹
                    {virtual}
                    {id}
                    {type}
                    {class}
                    {constraints}
                    {properties};›""", Map.of(

                "virtual", virtual(clazz),
                "id", id(clazz),
                "type", type(clazz),
                "class", clazz(clazz),

                "constraints", constraints(clazz),
                "properties", properties(clazz)

        )), 1);
    }


    private static String virtual(final Clazz clazz) {
        return clazz.virtual() ? ".virtual(true)" : "";
    }

    private String id(final Clazz clazz) {
        return clazz.id().map(".id(\"%s\")"::formatted).orElse("");
    }

    private String type(final Clazz clazz) {
        return clazz.type().map(".type(\"%s\")"::formatted).orElse("");
    }


    private String clazz(final Clazz clazz) {
        return explicit(clazz)
                .map(explicit -> Optional.of(implicit(clazz))
                        .filter(not(List::isEmpty))
                        .map(implicits -> clazz(explicit, implicits))
                        .orElseGet(() -> clazz(explicit))
                )
                .or(() -> Optional.of(list(clazz.clazzes()))
                        .filter(not(List::isEmpty))
                        .map(this::clazzes)
                )
                .orElse("");
    }


    private static Optional<Type> explicit(final Clazz clazz) {
        return clazz.clazz();
    }

    private List<Type> implicit(final Clazz clazz) {
        return explicit(clazz)
                .map(explicit -> list(clazz.clazzes().filter(not(explicit::equals))))
                .orElseGet(() -> list(clazz.clazzes()));
    }


    private String clazz(final Type explicit) {
        return ".clazz(%s)".formatted(type(explicit));
    }

    private String clazz(final Type explicit, final List<Type> implicits) {
        return fill("""
                .clazz({explicit},
                    {implicit}
                )""", Map.of(

                "explicit", type(explicit),
                "implicit", types(implicits)
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


    private String constraints(final Clazz clazz) {
        return Optional.of(clazz.constraints())
                .filter(not(Set::isEmpty))
                .map(constraints -> fill("""
                        .constraints(
                            {constraints}
                        )""", Map.of(

                        "constraints", constraints.stream()
                                .map(constraint -> "v -> new %s().apply(new %s(v))".formatted(constraint, frame()))
                                .collect(joining(",\n"))

                )))
                .orElse("");
    }


    private String properties(final Clazz clazz) {
        return clazz.methods()
                .filter(not(Method::isId))
                .filter(not(Method::isType))
                .filter(not(Method::isInternal))
                .map(this::property)
                .collect(joining("\n"));
    }

    private String property(final Method method) {
        return fill("""
                .property(Property.property("{name}")
                    {hidden}
                    {foreign}
                    {embedded}
                    {forward}
                    {reverse}
                    {shape}
                )""", Map.of(

                "name", method.name(),
                "hidden", hidden(method),
                "foreign", foreign(method),
                "embedded", embedded(method),
                "forward", forward(method),
                "reverse", reverse(method),
                "shape", shape(method, method.item().isEmpty() ? method.type() : method.item())
        ));
    }


    private String hidden(final Method method) {
        return ".hidden(%s)".formatted(method.hidden());
    }

    private String foreign(final Method method) {
        return ".foreign(%s)".formatted(method.foreign());
    }

    private String embedded(final Method method) {
        return ".embedded(%s)".formatted(method.embedded());
    }

    private String forward(final Method method) {
        return method.forward()
                .map(".forward(URI.create(\"%s\"))"::formatted)
                .orElse(".forward(false)");
    }

    private String reverse(final Method method) {
        return method.reverse()
                .map(".reverse(URI.create(\"%s\"))"::formatted)
                .orElse(".reverse(false)");
    }


    private String shape(final Method method, final String type) {

        final boolean object=method.datatype().equals(Value.Object()) && !method.isEnum();

        return fill("""
                .shape(() -> {shape}
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
                )""", Map.ofEntries(

                entry("shape", object ? "%s.SHAPE".formatted(frame(type)) : "Shape.shape()"),

                entry("datatype", object ? "" : datatype(method)),
                entry("minExclusive", minExclusive(method)),
                entry("maxExclusive", maxExclusive(method)),
                entry("minInclusive", minInclusive(method)),
                entry("maxInclusive", maxInclusive(method)),
                entry("minLength", minLength(method)),
                entry("maxLength", maxLength(method)),
                entry("pattern", pattern(method)),
                entry("in", in(method)),
                entry("languageIn", languageIn(method)),

                entry("uniqueLang", uniqueLang(method)),
                entry("minCount", minCount(method)),
                entry("maxCount", maxCount(method)),
                entry("hasValue", hasValue(method))

        ));
    }


    private static String datatype(final Method method) {
        return ".datatype(Value.%s())".formatted(method.datatype().accept(DATATYPE_VISITOR));
    }

    private String minExclusive(final Method method) {
        return method.minExclusive().map(v -> ".minExclusive(%s)".formatted(value(v))).orElse("");
    }

    private String maxExclusive(final Method method) {
        return method.maxExclusive().map(v -> ".maxExclusive(%s)".formatted(value(v))).orElse("");
    }

    private String minInclusive(final Method method) {
        return method.minInclusive().map(v -> ".minInclusive(%s)".formatted(value(v))).orElse("");
    }

    private String maxInclusive(final Method method) {
        return method.maxInclusive().map(v -> ".maxInclusive(%s)".formatted(value(v))).orElse("");
    }

    private String minLength(final Method method) {
        return method.minLength().map(".minLength(%d)"::formatted).orElse("");
    }

    private String maxLength(final Method method) {
        return method.maxLength().map(".maxLength(%d)"::formatted).orElse("");
    }

    private String pattern(final Method method) {
        return method.pattern()
                .map(Strings::quote)
                .map(".pattern(%s)"::formatted)
                .orElse("");
    }

    private String in(final Method method) {
        return method.in()
                .map(values -> ".in(%s)".formatted(values.stream()
                        .map(this::value)
                        .collect(joining(", "))
                ))
                .orElse("");
    }

    private String languageIn(final Method method) {
        return method.languageIn()
                .map(locales -> ".languageIn(%s)".formatted(locales.stream()
                        .map(locale -> "\"%s\"".formatted(locale(locale)))
                        .collect(joining(", "))
                )).orElse("");
    }

    private String uniqueLang(final Method method) {
        return method.uniqueLang() ? ".uniqueLang(true)" : "";
    }

    private String minCount(final Method method) {
        return method.minCount().map(".minCount(%d)"::formatted).orElse("");
    }

    private String maxCount(final Method method) {
        return method.maxCount().map(".maxCount(%d)"::formatted).orElse("");
    }

    @SuppressWarnings("NonBooleanMethodNameMayNotStartWithQuestion")
    private String hasValue(final Method method) {
        return method.hasValue()
                .map(values -> ".hasValue(%s)".formatted(values.stream()
                        .map(this::value)
                        .collect(joining(", "))
                ))
                .orElse("");
    }


    private String value(final Value value) {
        return value.accept(VALUE_VISITOR);
    }


    //̸// Utilities ////////////////////////////////////////////////////////////////////////////////////////////////////

    private String toFrame() {
        return fill("""
                public static {Frame} toFrame(final {Class} object) {
                
                    if ( object == null ) {
                        throw new NullPointerException("null object");
                    }
                
                    return object instanceof final {Frame} frame? frame : new {Frame}(object);
                }""", Map.of(

                "Class", clazz(),
                "Frame", frame()

        ));
    }

    private String toValue() {
        return fill("""
                public static Value toValue(final {Class} object) {
                
                    if ( object == null ) {
                        throw new NullPointerException("null object");
                    }
                
                    return toFrame(object).toValue();
                }""", Map.of(

                "Class", clazz()

        ));
    }

    private String toToken() {
        return fill("""
                public static Value toToken(final {Class} object) {
                
                    if ( object == null ) {
                        throw new NullPointerException("null object");
                    }
                
                    return {converter};
                
                }""", Map.of(

                "Class", clazz(),

                "converter", clazz.id()
                        .map(this::toReference)
                        .orElseGet(this::toEmbedded)

        ));
    }


    private String toReference(final String id) {
        return fill("""
                Optional.ofNullable(object.{id}())‹
                    .map({id} -> Value.object(
                            Value.shape(SHAPE),
                            Value.id(URIs.absolute(BASE, {id}))
                    ))
                    .orElseGet(Value::Nil)›""", Map.of(

                "id", id
        ));
    }

    private String toEmbedded() {
        return "toFrame(object).toValue()";
    }


    //̸// Instance /////////////////////////////////////////////////////////////////////////////////////////////////////

    private String fields() { // boxed primitive fields to support field hiding
        return clazz.methods()
                .map(Method::boxed)
                .map(method -> "private final %s %s;".formatted(
                        method.generic(),
                        method.name()
                ))
                .collect(joining("\n"));
    }


    private String emptyConstructor() {
        return fill("""
                public {Frame}() {
                    this(false);
                }
                
                public {Frame}(final boolean delta) {
                    this(
                        {values}
                    );
                }""", Map.of(

                "Frame", frame(),

                "values", Stream.concat(

                        Stream.of("delta", "$field -> false"),
                        clazz.methods().map(method -> "null")

                ).collect(joining(",\n"))

        ));
    }

    private String copyConstructor() {
        return fill("""
                public {Frame}(final {Class} object) {
                    this(
                        {values}
                    );
                }""", Map.of(

                "Frame", frame(),
                "Class", clazz(),

                "values", Stream.concat(

                        Stream.of("false", "$field -> true"),
                        clazz.methods().map(method -> "object.%s()".formatted(method.name()))

                ).collect(joining(",\n"))

        ));
    }

    private String valueConstructor() {
        return fill("""
                public {Frame}(final Value value) {
                   this(
                        {values}
                   );
                }""", Map.of(

                "Frame", frame(),

                "values", Stream.concat(

                        Stream.of(
                                "false",
                                """
                                        value.object().map(Map::keySet)‹
                                            .map($ -> (Predicate<String>)$::contains)
                                            .orElseGet(() -> $ -> false)›"""
                        ),

                        clazz.methods().map(method -> method.isInternal() ? "null"
                                : method.isId() ? "Values.id(value, BASE)"
                                : method.isType() ? "Values.type(value)"
                                : method.isContainer() ? toContainer(method)
                                : toScalar(method)
                        )

                ).collect(joining(",\n"))

        ));
    }

    private String internalConstructor() {
        return fill("""
                private {Frame}(
                    {fields}
                ) {
                    {values}
                }""", Map.of(

                "Frame", frame(),

                "fields", Stream.concat(

                        Stream.of(
                                "final boolean $delta",
                                "final Predicate<String> $touched"
                        ),

                        clazz.methods()
                                .map(Method::boxed)
                                .map(method -> "final %s %s".formatted(method.generic(), method.name()))

                ).collect(joining(",\n")),

                "values", Stream.concat(

                        Stream.of(
                                "this.$delta=$delta;",
                                "this.$touched=$touched;"
                        ),

                        clazz.methods()
                                .map(Method::boxed)
                                .map(method -> fill(method.isContainer() || method.isSpecial()
                                                ? "this.{field}=Collections.ensureImmutable({field}, \"{field}\");"
                                                : "this.{field}={field};",
                                        Map.of("field", method.name())
                                ))

                ).collect(joining("\n"))

        ));
    }


    private String toScalar(final Method method) {

        final String value="value.get(\"%s\")".formatted(method.name());

        if ( method.isEnum() ) {

            return "Values.option(%s, %s.class)".formatted(value, method.type());

        } else {

            return switch ( method.boxed().type() ) {

                case "Boolean" -> "Values.bit(%s)".formatted(value);
                case "Number" -> "Values.number(%s)".formatted(value);
                case "Byte" -> "Values._byte(%s)".formatted(value);
                case "Short" -> "Values._short(%s)".formatted(value);
                case "Integer" -> "Values._int(%s)".formatted(value);
                case "Long" -> "Values._long(%s)".formatted(value);
                case "Float" -> "Values._float(%s)".formatted(value);
                case "Double" -> "Values._double(%s)".formatted(value);
                case "BigInteger" -> "Values.integer(%s)".formatted(value);
                case "BigDecimal" -> "Values.decimal(%s)".formatted(value);
                case "String" -> "Values.string(%s)".formatted(value);
                case "URI" -> "Values.uri(%s)".formatted(value);
                case "Temporal" -> "Values.temporal(%s)".formatted(value);
                case "Year" -> "Values.year(%s)".formatted(value);
                case "YearMonth" -> "Values.yearMonth(%s)".formatted(value);
                case "LocalDate" -> "Values.localDate(%s)".formatted(value);
                case "LocalTime" -> "Values.localTime(%s)".formatted(value);
                case "OffsetTime" -> "Values.offsetTime(%s)".formatted(value);
                case "LocalDateTime" -> "Values.localDateTime(%s)".formatted(value);
                case "OffsetDateTime" -> "Values.offsetDateTime(%s)".formatted(value);
                case "ZonedDateTime" -> "Values.zonedDateTime(%s)".formatted(value);
                case "Instant" -> "Values.instant(%s)".formatted(value);
                case "TemporalAmount" -> "Values.temporalAmount(%s)".formatted(value);
                case "Period" -> "Values.period(%s)".formatted(value);
                case "Duration" -> "Values.duration(%s)".formatted(value);

                case TEXT -> "Values.text(%s)".formatted(value);
                case TEXTS -> "Values.texts(%s)".formatted(value);
                case TEXTSETS -> "Values.textsets(%s)".formatted(value);

                case DATA -> "Values.data(%s)".formatted(value);
                case DATAS -> "Values.datas(%s)".formatted(value);
                case DATASETS -> "Values.datasets(%s)".formatted(value);

                default -> "Values.<%s>object(value.get(\"%s\"), %sFrame::new)".formatted(
                        method.type(), method.name(), method.type()
                );

            };

        }
    }

    private String toContainer(final Method method) {

        final String kind=method.isSet() ? "set" : "list";
        final String value="value.get(\"%s\")".formatted(method.name());

        if ( method.isEnum() ) {

            return "Values.options(%s, %s.class)".formatted(value, method.item());

        } else {

            return switch ( method.item() ) {

                case "Boolean" -> "Collections.%s(Values.bits(%s))".formatted(kind, value);
                case "Number" -> "Collections.%s(Values.numbers(%s))".formatted(kind, value);
                case "Byte" -> "Collections.%s(Values.integrals(%s))".formatted(kind, value);
                case "Short" -> "Collections.%s(Values.integrals(%s))".formatted(kind, value);
                case "Integer" -> "Collections.%s(Values.integrals(%s))".formatted(kind, value);
                case "Long" -> "Collections.%s(Values.integrals(%s))".formatted(kind, value);
                case "Float" -> "Collections.%s(Values.floatings(%s))".formatted(kind, value);
                case "Double" -> "Collections.%s(Values.floatings(%s))".formatted(kind, value);
                case "BigInteger" -> "Collections.%s(Values.integers(%s))".formatted(kind, value);
                case "BigDecimal" -> "Collections.%s(Values.decimals(%s))".formatted(kind, value);
                case "String" -> "Collections.%s(Values.strings(%s))".formatted(kind, value);
                case "URI" -> "Collections.%s(Values.uris(%s))".formatted(kind, value);
                case "Temporal" -> "Collections.%s(Values.temporals(%s))".formatted(kind, value);
                case "Year" -> "Collections.%s(Values.years(%s))".formatted(kind, value);
                case "YearMonth" -> "Collections.%s(Values.yearMonths(%s))".formatted(kind, value);
                case "LocalDate" -> "Collections.%s(Values.localDates(%s))".formatted(kind, value);
                case "LocalTime" -> "Collections.%s(Values.localTimes(%s))".formatted(kind, value);
                case "OffsetTime" -> "Collections.%s(Values.offsetTimes(%s))".formatted(kind, value);
                case "LocalDateTime" -> "Collections.%s(Values.localDateTimes(%s))".formatted(kind, value);
                case "OffsetDateTime" -> "Collections.%s(Values.offsetDateTimes(%s))".formatted(kind, value);
                case "ZonedDateTime" -> "Collections.%s(Values.zonedDateTimes(%s))".formatted(kind, value);
                case "Instant" -> "Collections.%s(Values.instants(%s))".formatted(kind, value);
                case "TemporalAmount" -> "Collections.%s(Values.temporalAmounts(%s))".formatted(kind, value);
                case "Period" -> "Collections.%s(Values.periods(%s))".formatted(kind, value);
                case "Duration" -> "Collections.%s(Values.durations(%s))".formatted(kind, value);

                default -> "Collections.%s(Values.objects(value.get(\"%s\"), %sFrame::new))".formatted(
                        kind, method.name(), method.item()
                );

            };

        }
    }


    private String accessors() {
        return clazz.methods()
                .map(method -> "%s\n\n%s".formatted(
                        getter(method),
                        mutator(method)
                ))
                .collect(joining("\n\n\n"));
    }


    private String getter(final Method method) {
        if ( method.isDefault() && method.isInternal() ) {

            return fill("""
                    @Override
                    public {type} {name}() {
                        return {Class}.super.{name}();
                    }""", Map.of(

                    "type", method.generic(),
                    "Class", clazz(),
                    "name", method.name()

            ));

        } else if ( method.isDefault() && method.isPrimitive() ) {

            return fill("""
                    @Override
                    public {type} {name}() {
                        return $touched.test("{name}") ? {name} != null ? {name} : {initial} : {value};
                    }""", Map.of(

                    "type", method.generic(),
                    "name", method.name(),
                    "initial", toInitial(method),
                    "value", toImmutabale(method, "%s.super.%s()".formatted(clazz(), method.name()))

            ));

        } else if ( method.isDefault() ) {

            return fill("""
                    @Override
                    public {type} {name}() {
                        return $touched.test("{name}") ? {name} : {value};
                    }""", Map.of(

                    "type", method.generic(),
                    "name", method.name(),
                    "value", toImmutabale(method, "%s.super.%s()".formatted(clazz(), method.name()))

            ));

        } else if ( method.isPrimitive() ) {

            return fill("""
                    @Override
                    public {type} {name}() {
                        return {name} != null ? {name} : {initial};
                    }""", Map.of(

                    "type", method.generic(),
                    "name", method.name(),

                    "initial", toInitial(method)

            ));

        } else if ( method.isMultiple() ) {

            return fill("""
                    @Override
                    public {type} {name}() {
                        return {name} != null ? {name} : {empty};
                    }""", Map.of(

                    "type", method.generic(),
                    "name", method.name(),

                    "empty", method.isMap() ? "Collections.map()"
                            : method.isSet() ? "Collections.set()"
                            : "Collections.list()"
            ));

        } else {

            return fill("""
                    @Override
                    public {type} {name}() {
                        return {name};
                    }""", Map.of(

                    "type", method.generic(),
                    "name", method.name()

            ));

        }
    }

    private String mutator(final Method method) {
        return fill("""
                public {Frame} {name}(final {type} {name}) {
                    return new {Frame}(
                        {fields}
                    );
                }""", Map.of(

                "Frame", frame(),
                "name", method.name(),
                "type", method.isContainer() ? toParam(method) : method.boxed().generic(),

                "fields", Stream.concat(
                        Stream.of("$delta", "$touched.or(\"%s\"::equals)%s".formatted(method.name(),
                                method.isId() ? ".or(\"@id\"::equals)" : method.isType() ? ".or(\"@type\"::equals)" : ""
                        )),
                        clazz.methods().map(m -> m.equals(method) && !m.isInternal()
                                ? toImmutabale(m, m.name())
                                : m.name()
                        )
                ).collect(joining(",\n"))

        ));
    }


    private String toParam(final Method method) { // !!! refactor/review

        final String value=method.boxed().generic();

        if ( method.isEnum() ) {

            return value;

        } else {

            return switch ( method.core() ) {

                case "Boolean" -> value;
                case "Number" -> value;
                case "Byte" -> value;
                case "Short" -> value;
                case "Integer" -> value;
                case "Long" -> value;
                case "Float" -> value;
                case "Double" -> value;
                case "BigInteger" -> value;
                case "BigDecimal" -> value;
                case "String" -> value;
                case "URI" -> value;
                case "Temporal" -> value;
                case "Year" -> value;
                case "YearMonth" -> value;
                case "LocalDate" -> value;
                case "LocalTime" -> value;
                case "OffsetTime" -> value;
                case "LocalDateTime" -> value;
                case "OffsetDateTime" -> value;
                case "ZonedDateTime" -> value;
                case "Instant" -> value;
                case "TemporalAmount" -> value;
                case "Period" -> value;
                case "Duration" -> value;

                case TEXT -> value;
                case TEXTS -> value;
                case TEXTSETS -> value;

                case DATA -> value;
                case DATAS -> value;
                case DATASETS -> value;

                default -> method.isContainer() ? "%s<? extends %s>".formatted(method.type(), method.boxed().item())
                        : value;

            };

        }

    }

    private String toInitial(final Method method) {
        return switch ( method.type() ) {
            case "boolean" -> "false";
            case "byte", "short", "int" -> "0";
            case "long" -> "0L";
            case "float" -> "0.0F";
            case "double" -> "0.0D";
            default -> "null";
        };
    }

    private String toImmutabale(final Method method, final String value) {
        if ( method.isEnum() ) {

            return value;

        } else {

            return switch ( method.core() ) {

                case "Boolean" -> value;
                case "Number" -> value;
                case "Byte" -> value;
                case "Short" -> value;
                case "Integer" -> value;
                case "Long" -> value;
                case "Float" -> value;
                case "Double" -> value;
                case "BigInteger" -> value;
                case "BigDecimal" -> value;
                case "String" -> value;
                case "URI" -> value;
                case "Temporal" -> value;
                case "Year" -> value;
                case "YearMonth" -> value;
                case "LocalDate" -> value;
                case "LocalTime" -> value;
                case "OffsetTime" -> value;
                case "LocalDateTime" -> value;
                case "OffsetDateTime" -> value;
                case "ZonedDateTime" -> value;
                case "Instant" -> value;
                case "TemporalAmount" -> value;
                case "Period" -> value;
                case "Duration" -> value;

                case TEXT -> value;
                case TEXTS -> value;
                case TEXTSETS -> value;

                case DATA -> value;
                case DATAS -> value;
                case DATASETS -> value;

                default -> method.isSet() ? "Values.%s(%s)".formatted(method.isWild() ? "set_" : "set", value)
                        : method.isContainer() ? "Values.%s(%s)".formatted(method.isWild() ? "list_" : "list", value)
                        : value;

            };

        }
    }


    private String converter() {
        return fill("""
                @Override
                public Value toValue() {
                    return Value.object(Stream.concat(
                
                        Stream.of(Value.shape(SHAPE)),
                
                        $delta
                
                            ? Stream.<Entry<String, Value>>of(
                                    {delta}
                            ).filter(e -> e.getKey().equals(Value.ID) || $touched.test(e.getKey()))
                
                            : Stream.<Entry<String, Value>>of(
                                    {plain}
                            ).filter(Objects::nonNull)
                
                    ).toList());
                }
                """, Map.of(

                "delta", clazz.methods()
                        .filter(not(Method::isInternal))
                        .map(this::toDelta)
                        .collect(joining(",\n")),

                "plain", clazz.methods()
                        .filter(not(Method::isInternal))
                        .map(this::toPlain)
                        .collect(joining(",\n"))
        ));
    }

    private String formatter() {
        return """
                @Override
                public String toString() {
                    return toValue().toString();
                }""";
    }


    private String toDelta(final Method method) {
        return method.isId() ? toId("%s()".formatted(method.name())) // include default ids in generated value
                : method.isType() ? toType("%s".formatted(method.name()))
                : toField(method, "%s".formatted(method.name()));
    }

    private String toPlain(final Method method) {
        return fill("""
                Optional.ofNullable({value})‹
                    .map(v -> {field})
                    .orElseGet(() -> $touched.test("{method}") ? Value.field({property}, Value.Nil()) : null)›""", Map.of(

                "method", method.name(),

                "property", method.isId() ? "Value.ID"
                        : method.isType() ? "Value.TYPE"
                        : "\"%s\"".formatted(method.name()),

                "value", method.isDefault()

                        ? fill("$touched.test(\"{name}\") ? {name} : {cast}{name}()", Map.of(
                        "name", method.name(),
                        "cast", method.isPrimitive() ? "(%s)".formatted(method.boxed().type()) : ""
                ))

                        : "%s".formatted(method.name()),

                "field", method.isId() ? toId("v")
                        : method.isType() ? toType("v")
                        : toField(method, "v")

        ));
    }


    private String toId(final String value) {
        return "Value.field(Value.ID, Values.id(%s, BASE))".formatted(
                value
        );
    }

    private String toType(final String value) {
        return "Value.field(Value.TYPE, Values.type(%s))".formatted(
                value
        );
    }

    private String toField(final Method method, final String value) {
        return "Value.field(\"%s\", %s)".formatted(
                method.name(),
                method.isContainer() ? toValues(method, value) : toValue(method, value)
        );
    }


    private String toValue(final Method method, final String value) {
        if ( method.isEnum() ) {

            return "Values.option(%s)".formatted(value);

        } else {

            return switch ( method.boxed().type() ) {

                case "Boolean" -> "Values.bit(%s)".formatted(value);
                case "Number" -> "Values.number(%s)".formatted(value);
                case "Byte" -> "Values.integral(%s)".formatted(value);
                case "Short" -> "Values.integral(%s)".formatted(value);
                case "Integer" -> "Values.integral(%s)".formatted(value);
                case "Long" -> "Values.integral(%s)".formatted(value);
                case "Float" -> "Values.floating(%s)".formatted(value);
                case "Double" -> "Values.floating(%s)".formatted(value);
                case "BigInteger" -> "Values.integer(%s)".formatted(value);
                case "BigDecimal" -> "Values.decimal(%s)".formatted(value);
                case "String" -> "Values.string(%s)".formatted(value);
                case "URI" -> "Values.uri(%s)".formatted(value);
                case "Temporal" -> "Values.temporal(%s)".formatted(value);
                case "Year" -> "Values.year(%s)".formatted(value);
                case "YearMonth" -> "Values.yearMonth(%s)".formatted(value);
                case "LocalDate" -> "Values.localDate(%s)".formatted(value);
                case "LocalTime" -> "Values.localTime(%s)".formatted(value);
                case "OffsetTime" -> "Values.offsetTime(%s)".formatted(value);
                case "LocalDateTime" -> "Values.localDateTime(%s)".formatted(value);
                case "OffsetDateTime" -> "Values.offsetDateTime(%s)".formatted(value);
                case "ZonedDateTime" -> "Values.zonedDateTime(%s)".formatted(value);
                case "Instant" -> "Values.instant(%s)".formatted(value);
                case "TemporalAmount" -> "Values.temporalAmount(%s)".formatted(value);
                case "Period" -> "Values.period(%s)".formatted(value);
                case "Duration" -> "Values.duration(%s)".formatted(value);

                case TEXT -> "Values.text(%s)".formatted(value);
                case TEXTS -> "Values.texts(%s)".formatted(value);
                case TEXTSETS -> "Values.textsets(%s)".formatted(value);

                case DATA -> "Values.data(%s)".formatted(value);
                case DATAS -> "Values.datas(%s)".formatted(value);
                case DATASETS -> "Values.datasets(%s)".formatted(value);

                default -> "Values.object(%s, %sFrame::%s)".formatted(
                        value, method.type(), method.embedded() ? "toValue" : "toToken"
                );

            };

        }
    }

    private String toValues(final Method method, final String value) {
        if ( method.isEnum() ) {

            return "Values.options(%s)".formatted(value);

        } else {

            return switch ( method.item() ) {

                case "Boolean" -> "Values.bits(%s)".formatted(value);
                case "Number" -> "Values.numbers(%s)".formatted(value);
                case "Byte" -> "Values.integrals(%s)".formatted(value);
                case "Short" -> "Values.integrals(%s)".formatted(value);
                case "Integer" -> "Values.integrals(%s)".formatted(value);
                case "Long" -> "Values.integrals(%s)".formatted(value);
                case "Float" -> "Values.floatings(%s)".formatted(value);
                case "Double" -> "Values.floatings(%s)".formatted(value);
                case "BigInteger" -> "Values.integers(%s)".formatted(value);
                case "BigDecimal" -> "Values.decimals(%s)".formatted(value);
                case "String" -> "Values.strings(%s)".formatted(value);
                case "URI" -> "Values.uris(%s)".formatted(value);
                case "Temporal" -> "Values.temporals(%s)".formatted(value);
                case "Year" -> "Values.years(%s)".formatted(value);
                case "YearMonth" -> "Values.yearMonths(%s)".formatted(value);
                case "LocalDate" -> "Values.localDates(%s)".formatted(value);
                case "LocalTime" -> "Values.localTimes(%s)".formatted(value);
                case "OffsetTime" -> "Values.offsetTimes(%s)".formatted(value);
                case "LocalDateTime" -> "Values.localDateTimes(%s)".formatted(value);
                case "OffsetDateTime" -> "Values.offsetDateTimes(%s)".formatted(value);
                case "ZonedDateTime" -> "Values.zonedDateTimes(%s)".formatted(value);
                case "Instant" -> "Values.instants(%s)".formatted(value);
                case "TemporalAmount" -> "Values.temporalAmounts(%s)".formatted(value);
                case "Period" -> "Values.periods(%s)".formatted(value);
                case "Duration" -> "Values.durations(%s)".formatted(value);

                default -> "Values.objects(%s, %sFrame::%s)".formatted(
                        value, method.item(), method.embedded() ? "toValue" : "toToken"
                );

            };

        }
    }

}
