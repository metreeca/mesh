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

package com.metreeca.mesh.mint.ast;

import com.metreeca.mesh.Value;
import com.metreeca.mesh.meta.jsonld.*;
import com.metreeca.mesh.meta.shacl.*;
import com.metreeca.mesh.mint.FrameException;
import com.metreeca.shim.Locales;

import java.net.URI;
import java.util.*;
import java.util.stream.Stream;

import static com.metreeca.mesh.Value.*;
import static com.metreeca.mesh.mint.ast.Annotation.*;
import static com.metreeca.mesh.mint.ast.Annotation.VALUE;
import static com.metreeca.mesh.mint.ast.Reference.simple;
import static com.metreeca.mesh.shapes.Shape.shape;
import static com.metreeca.shim.Collections.list;
import static com.metreeca.shim.Collections.set;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;
import static java.util.function.Predicate.not;


/**
 * Represents a method in a frame interface class.
 *
 * <p>Models a method declaration within a {@link com.metreeca.mesh.meta.jsonld.Frame} annotated interface,
 * capturing return type information, name, annotations, and providing support for extracting semantic constraints from
 * annotations.</p>
 *
 * <p>Key responsibilities include:</p>
 * <ul>
 *   <li>Managing method signatures and return types including specialized collection types</li>
 *   <li>Identifying special method roles (ID, Type, Internal)</li>
 *   <li>Processing property constraints from annotations (ranges, patterns, cardinality, etc.)</li>
 *   <li>Mapping Java types to semantic data types</li>
 *   <li>Handling reverse properties and embedded objects</li>
 * </ul>
 *
 * <p>The class includes specialized support for collection types, textual properties, and data properties
 * with appropriate validation for constraints that are only applicable to specific data types.</p>
 */
@SuppressWarnings("FieldNotUsedInToString")
public final class Method {

    public static final String TEXT="Entry<Locale, String>";
    public static final String TEXTS="Map<Locale, String>";
    public static final String TEXTSETS="Map<Locale, Set<String>>";

    public static final String DATA="Entry<URI, String>";
    public static final String DATAS="Map<URI, String>";
    public static final String DATASETS="Map<URI, Set<String>>";

    public static final String SET=simple(Set.class);

    public static final String LIST=simple(List.class);
    public static final String COLLECTION=simple(Collection.class);


    private static final Set<String> TEXTUAL=set(TEXT, TEXTS, TEXTSETS);
    private static final Set<String> TYPED=set(DATA, DATAS, DATASETS);
    private static final Set<String> CONTAINER=set(SET, LIST, COLLECTION);
    private static final Set<String> MAP=set(TEXTS, TEXTSETS, DATAS, DATASETS);


    private static String boxed(final String type) {
        return switch ( type ) {

            case "boolean" -> "Boolean";
            case "byte" -> "Byte";
            case "short" -> "Short";
            case "int" -> "Integer";
            case "long" -> "Long";
            case "float" -> "Float";
            case "double" -> "Double";

            default -> null;

        };
    }


    //̸/////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private final Clazz clazz;

    private final boolean isDefault; // true for default methods
    private final boolean isEnum; // true if either the raw or the item return type is an enumeration
    private final boolean isWild; // true if the item return type is a wildcard (e.g. for List<? extends Resource>)

    private final String type; // the raw return type (e.g. String of List for List<String>)
    private final String item; // the item return type (e.g. String for List<String>) or empty for non-generic types

    private final String name;

    private final List<Annotation> annotations;


    public Method(final String name) {
        this(
                null,
                false,
                false,
                false,
                "",
                "",
                name,
                list()
        );
    }

    public Method(

            final Clazz clazz,

            final boolean isDefault,
            final boolean isEnum,
            final boolean isWild,

            final String type,
            final String item,
            final String name,

            final List<Annotation> annotations

    ) {

        this.clazz=clazz; // may be null on creation before method is assigned to a class

        this.isDefault=isDefault;
        this.isEnum=isEnum;
        this.isWild=isWild;

        this.type=requireNonNull(type, "null type");
        this.item=requireNonNull(item, "null item");
        this.name=requireNonNull(name, "null name");

        this.annotations=list(requireNonNull(annotations, "null annotations"));

    }


    public boolean isDefault() {
        return isDefault;
    }

    public boolean isEnum() {
        return isEnum;
    }

    public boolean isWild() {
        return isWild;
    }


    public boolean isInternal() {
        return annotations()
                .filter(annotation -> annotation.is(Internal.class))
                .peek(annotation -> {

                    if ( annotations().anyMatch(not(a -> a.is(Internal.class) || a.isIgnored())) ) {
                        throw new FrameException(format(
                                "unexpected annotations on @%s method <%s>",
                                simple(Internal.class), this
                        ));
                    }

                })
                .findFirst()
                .isPresent();
    }

    public boolean isId() {
        return annotations()
                .filter(annotation -> annotation.is(Id.class))
                .peek(annotation -> {

                    if ( !type.equals(simple(URI.class)) ) {
                        throw new FrameException(format(
                                "illegal <%s> @%s method <%s>",
                                generic(), simple(Id.class), this
                        ));
                    }

                    if ( annotations().anyMatch(not(a -> a.is(Id.class) || a.isIgnored())) ) {
                        throw new FrameException(format(
                                "unexpected annotations on @%s method <%s>",
                                simple(Id.class), this
                        ));
                    }

                })
                .findFirst()
                .isPresent();
    }

    public boolean isType() {
        return annotations()
                .filter(annotation -> annotation.is(Type.class))
                .peek(annotation -> {

                    if ( !type.equals(simple(String.class)) ) {
                        throw new FrameException(format(
                                "illegal <%s> @%s method <%s>",
                                generic(), simple(Type.class), this
                        ));
                    }

                    if ( annotations().anyMatch(not(a -> a.is(Type.class) || a.isIgnored())) ) {
                        throw new FrameException(format(
                                "unexpected annotations on @%s method <%s>",
                                simple(Type.class), this
                        ));
                    }

                })
                .findFirst()
                .isPresent();
    }


    public boolean isPrimitive() {
        return boxed(type) != null;
    }

    public boolean isSpecial() {
        return TEXTUAL.contains(type) || TYPED.contains(type);
    }


    public boolean isSet() {
        return type.equals(SET);
    }

    public boolean isList() {
        return type.equals(LIST);
    }

    public boolean isCollection() {
        return type.equals(COLLECTION);
    }


    public boolean isContainer() {
        return CONTAINER.contains(type);
    }

    public boolean isMap() {
        return MAP.contains(type);
    }

    public boolean isMultiple() {
        return isContainer() || isMap();
    }


    public boolean isLiteral() {
        if ( isEnum ) {

            return true;

        } else {

            return switch ( core() ) {

                case "Boolean" -> true;
                case "Number" -> true;
                case "Byte" -> true;
                case "Short" -> true;
                case "Integer" -> true;
                case "Long" -> true;
                case "Float" -> true;
                case "Double" -> true;
                case "BigInteger" -> true;
                case "BigDecimal" -> true;
                case "String" -> true;
                case "URI" -> true;
                case "Temporal" -> true;
                case "Year" -> true;
                case "YearMonth" -> true;
                case "LocalDate" -> true;
                case "LocalTime" -> true;
                case "OffsetTime" -> true;
                case "LocalDateTime" -> true;
                case "OffsetDateTime" -> true;
                case "ZonedDateTime" -> true;
                case "Instant" -> true;
                case "TemporalAmount" -> true;
                case "Period" -> true;
                case "Duration" -> true;

                case TEXT -> true;
                case TEXTS -> true;
                case TEXTSETS -> true;

                case DATA -> true;
                case DATAS -> true;
                case DATASETS -> true;

                default -> false;

            };

        }
    }


    public String core() {
        return isContainer() ? boxed().item() : boxed().type();
    }


    public Clazz clazz() {
        return clazz;
    }

    public Method clazz(final Clazz clazz) {
        return new Method(
                clazz,
                isDefault,
                isEnum,
                isWild,
                type,
                item,
                name,
                annotations
        );
    }


    public String type() {
        return type;
    }

    public Method type(final String type) {
        return new Method(
                clazz,
                isDefault,
                isEnum,
                isWild,
                type,
                item,
                name,
                annotations
        );
    }


    public String item() {
        return item;
    }

    public Method item(final String item) {
        return new Method(
                clazz,
                isDefault,
                isEnum,
                isWild,
                type,
                item,
                name,
                annotations
        );
    }


    public String generic() {
        return item.isEmpty() ? type
                : isWild ? "%s<? extends %s>".formatted(type, item)
                : "%s<%s>".formatted(type, item);
    }


    public String name() {
        return name;
    }


    public Stream<Annotation> annotations() {
        return annotations.stream();
    }

    public Method annotations(final Collection<Annotation> annotations) {
        return new Method(
                clazz,
                isDefault,
                isEnum,
                isWild,
                type,
                item,
                name,
                list(annotations)
        );
    }


    public Method boxed() {
        return Optional.ofNullable(boxed(type))
                .map(boxed -> new Method(
                        clazz,
                        isDefault,
                        isEnum,
                        isWild,
                        boxed,
                        item,
                        name,
                        annotations
                ))
                .orElse(this);
    }


    //̸// Shape ////////////////////////////////////////////////////////////////////////////////////////////////////////

    public boolean hidden() {
        return annotations().anyMatch(annotation -> annotation.is(Hidden.class));
    }

    public boolean foreign() {
        return annotations().anyMatch(annotation -> annotation.is(Foreign.class));
    }

    public boolean embedded() {
        return annotations().anyMatch(annotation -> annotation.is(Embedded.class));
    }


    public Optional<String> forward() {
        return annotations()
                .filter(annotation -> annotation.is(Property.class))
                .reduce(this::reduce)
                .flatMap(annotation -> Optional.of(annotation.string(FORWARD))
                        .filter(not(String::isEmpty))
                        .or(() -> annotation.string(REVERSE).isEmpty()
                                ? Optional.of(annotation.string(IMPLIED))
                                : Optional.empty()
                        )
                )
                .filter(not(String::isEmpty));
    }

    public Optional<String> reverse() {
        return annotations()
                .filter(annotation -> annotation.is(Property.class))
                .reduce(this::reduce)
                .map(annotation -> annotation.string(REVERSE))
                .filter(not(String::isEmpty));
    }


    private Annotation reduce(final Annotation x, final Annotation y) {

        // @Forward/@Reverse annotations are allowed only on the base method, but multiple copies may be inherited

        if ( !(y.string(FORWARD).isEmpty() || y.string(FORWARD).equals(x.string(FORWARD))) ) {

            throw new FrameException(format(
                    "conflicting @%s declarations %s / %s",
                    simple(Forward.class), y.source(), x.source()
            ));

        } else if ( !(y.string(REVERSE).isEmpty() || y.string(REVERSE).equals(x.string(REVERSE))) ) {

            throw new FrameException(format(
                    "conflicting @%s declarations %s / %s",
                    simple(Reverse.class), y.source(), x.source()
            ));

        } else {

            return x;

        }
    }


    public Value datatype() {
        if ( isEnum ) {

            return String();

        } else {

            return switch ( core() ) {

                case "Boolean" -> Bit();
                case "Number" -> Number();
                case "Byte" -> Integral();
                case "Short" -> Integral();
                case "Integer" -> Integral();
                case "Long" -> Integral();
                case "Float" -> Floating();
                case "Double" -> Floating();
                case "BigInteger" -> Integer();
                case "BigDecimal" -> Decimal();
                case "String" -> String();
                case "URI" -> URI();
                case "Temporal" -> Temporal();
                case "Year" -> Year();
                case "YearMonth" -> YearMonth();
                case "LocalDate" -> LocalDate();
                case "LocalTime" -> LocalTime();
                case "OffsetTime" -> OffsetTime();
                case "LocalDateTime" -> LocalDateTime();
                case "OffsetDateTime" -> OffsetDateTime();
                case "ZonedDateTime" -> ZonedDateTime();
                case "Instant" -> Instant();
                case "TemporalAmount" -> TemporalAmount();
                case "Period" -> Period();
                case "Duration" -> Duration();

                case TEXT -> Text();
                case TEXTS -> Text();
                case TEXTSETS -> Text();

                case DATA -> Data();
                case DATAS -> Data();
                case DATASETS -> Data();

                default -> Object();

            };

        }
    }

    public Optional<Value> minExclusive() {

        final Value datatype=datatype();

        return annotations()
                .filter(annotation -> annotation.is(MinExclusive.class))
                .filter(annotation -> !annotation.string(VALUE).isEmpty())
                .map(annotation -> {

                    final String value=annotation.string(VALUE);

                    try {

                        return new Reference<>(

                                shape().minExclusive(datatype.decode(value).orElseThrow(() ->
                                                new FrameException(format(
                                                        "malformed @%s limit <%s> @ <%s>",
                                                        simple(MinExclusive.class), value, annotation.source()
                                                ))
                                        ))
                                        .minExclusive()
                                        .orElseThrow(() -> new AssertionError(simple(MinExclusive.class))),

                                annotation.source()
                        );

                    } catch ( final IllegalArgumentException e ) {

                        throw new FrameException(format(
                                "illegal @%s limit <%s> @ <%s> : %s",
                                simple(MinExclusive.class), value, annotation.source(), e.getMessage()
                        ));

                    }

                })
                .reduce((x, y) -> {

                    try { // merge parent value into child value

                        final Value value=shape().minExclusive(y.value())
                                .merge(shape().minExclusive(x.value()))
                                .minExclusive()
                                .orElseThrow(() -> new AssertionError(simple(MinExclusive.class)));

                        return new Reference<>(value, x.source());

                    } catch ( final IllegalArgumentException e ) {

                        throw new FrameException(format(
                                "conflicting @%s declarations %s / %s",
                                simple(MinExclusive.class), y, x
                        ));

                    }

                })
                .map(Reference::value);
    }

    public Optional<Value> maxExclusive() {

        final Value datatype=datatype();

        return annotations()
                .filter(annotation -> annotation.is(MaxExclusive.class))
                .filter(annotation -> !annotation.string(VALUE).isEmpty())
                .map(annotation -> {

                    final String value=annotation.string(VALUE);

                    try {

                        return new Reference<>(

                                shape().maxExclusive(datatype.decode(value).orElseThrow(() ->
                                                new FrameException(format(
                                                        "malformed @%s limit <%s> @ <%s>",
                                                        simple(MaxExclusive.class), value, annotation.source()
                                                ))
                                        ))
                                        .maxExclusive()
                                        .orElseThrow(() -> new AssertionError(simple(MaxExclusive.class))),

                                annotation.source()
                        );

                    } catch ( final IllegalArgumentException e ) {

                        throw new FrameException(format(
                                "illegal @%s limit <%s> @ <%s> : %s",
                                simple(MaxExclusive.class), value, annotation.source(), e.getMessage()
                        ));

                    }

                })
                .reduce((x, y) -> {

                    try { // merge parent value into child value

                        final Value value=shape().maxExclusive(y.value())
                                .merge(shape().maxExclusive(x.value()))
                                .maxExclusive()
                                .orElseThrow(() -> new AssertionError(simple(MaxExclusive.class)));

                        return new Reference<>(value, x.source());

                    } catch ( final IllegalArgumentException e ) {

                        throw new FrameException(format(
                                "conflicting @%s declarations %s / %s",
                                simple(MaxExclusive.class), y, x
                        ));

                    }

                })
                .map(Reference::value);
    }

    public Optional<Value> minInclusive() {

        final Value datatype=datatype();

        return annotations()
                .filter(annotation -> annotation.is(MinInclusive.class))
                .filter(annotation -> !annotation.string(VALUE).isEmpty())
                .map(annotation -> {

                    final String value=annotation.string(VALUE);

                    try {

                        return new Reference<>(

                                shape().minInclusive(datatype.decode(value).orElseThrow(() ->
                                                new FrameException(format(
                                                        "malformed @%s limit <%s> @ <%s>",
                                                        simple(MinInclusive.class), value, annotation.source()
                                                ))
                                        ))
                                        .minInclusive()
                                        .orElseThrow(() -> new AssertionError(simple(MinInclusive.class))),

                                annotation.source()
                        );

                    } catch ( final IllegalArgumentException e ) {

                        throw new FrameException(format(
                                "illegal @%s limit <%s> @ <%s> : %s",
                                simple(MinInclusive.class), value, annotation.source(), e.getMessage()
                        ));

                    }

                })
                .reduce((x, y) -> {

                    try { // merge parent value into child value

                        final Value value=shape().minInclusive(y.value())
                                .merge(shape().minInclusive(x.value()))
                                .minInclusive()
                                .orElseThrow(() -> new AssertionError(simple(MinInclusive.class)));

                        return new Reference<>(value, x.source());

                    } catch ( final IllegalArgumentException e ) {

                        throw new FrameException(format(
                                "conflicting @%s declarations %s / %s",
                                simple(MinInclusive.class), y, x
                        ));

                    }

                })
                .map(Reference::value);
    }

    public Optional<Value> maxInclusive() {

        final Value datatype=datatype();

        return annotations()
                .filter(annotation -> annotation.is(MaxInclusive.class))
                .filter(annotation -> !annotation.string(VALUE).isEmpty())
                .map(annotation -> {

                    final String value=annotation.string(VALUE);

                    try {

                        return new Reference<>(
                                shape().maxInclusive(datatype.decode(value).orElseThrow(() ->
                                                new FrameException(format(
                                                        "malformed @%s limit <%s> @ <%s>",
                                                        simple(MaxInclusive.class), value, annotation.source()
                                                ))
                                        ))
                                        .maxInclusive()
                                        .orElseThrow(() -> new AssertionError(simple(MaxInclusive.class))),

                                annotation.source()
                        );

                    } catch ( final IllegalArgumentException e ) {

                        throw new FrameException(format(
                                "illegal @%s limit <%s> @ <%s> : %s",
                                simple(MaxInclusive.class), value, annotation.source(), e.getMessage()
                        ));

                    }

                })
                .reduce((x, y) -> {

                    try { // merge parent value into child value

                        final Value value=shape().maxInclusive(y.value())
                                .merge(shape().maxInclusive(x.value()))
                                .maxInclusive()
                                .orElseThrow(() -> new AssertionError(simple(MaxInclusive.class)));

                        return new Reference<>(value, x.source());

                    } catch ( final IllegalArgumentException e ) {

                        throw new FrameException(format(
                                "conflicting @%s declarations %s / %s",
                                simple(MaxInclusive.class), y, x
                        ));

                    }

                })
                .map(Reference::value);
    }

    public Optional<Integer> minLength() {
        return annotations()
                .filter(annotation -> annotation.is(MinLength.class))
                .filter(annotation -> annotation.integer(VALUE) > 0)
                .map(annotation -> {

                    final int value=annotation.integer(VALUE);

                    try {

                        return new Reference<>(

                                shape().minLength(value)
                                        .minLength()
                                        .orElseThrow(() -> new AssertionError(simple(MinLength.class))),

                                annotation.source()

                        );

                    } catch ( final IllegalArgumentException e ) {

                        throw new FrameException(format(
                                "illegal @%s limit <%s> @ <%s> : %s",
                                simple(MinLength.class), value, annotation.source(), e.getMessage()
                        ));

                    }

                })
                .reduce((x, y) -> {

                    try { // merge parent value into child value

                        final Integer value=shape().minLength(y.value())
                                .merge(shape().minLength(x.value()))
                                .minLength()
                                .orElseThrow(() -> new AssertionError(simple(MinLength.class)));

                        return new Reference<>(value, x.source());

                    } catch ( final IllegalArgumentException e ) {

                        throw new FrameException(format(
                                "conflicting @%s declarations %s / %s",
                                simple(MaxLength.class), y, x
                        ));

                    }

                })
                .map(Reference::value);
    }

    public Optional<Integer> maxLength() {
        return annotations()
                .filter(annotation -> annotation.is(MaxLength.class))
                .filter(annotation -> annotation.integer(VALUE) > 0)
                .map(annotation -> {

                    final Integer value=annotation.integer(VALUE);

                    try {

                        return new Reference<>(

                                shape().maxLength(value)
                                        .maxLength()
                                        .orElseThrow(() -> new AssertionError(simple(MaxLength.class))),

                                annotation.source()
                        );

                    } catch ( final IllegalArgumentException e ) {

                        throw new FrameException(format(
                                "illegal @%s limit <%s> @ <%s> : %s",
                                simple(MaxLength.class), value, annotation.source(), e.getMessage()
                        ));

                    }

                }).reduce((x, y) -> {

                    try { // merge parent value into child value

                        final Integer value=shape().maxLength(y.value())
                                .merge(shape().maxLength(x.value()))
                                .maxLength()
                                .orElseThrow(() -> new AssertionError(simple(MaxLength.class)));

                        return new Reference<>(value, x.source());

                    } catch ( final IllegalArgumentException e ) {

                        throw new FrameException(format(
                                "conflicting @%s declarations %s / %s",
                                simple(MaxLength.class), y, x
                        ));

                    }

                })
                .map(Reference::value);
    }

    public Optional<String> pattern() {
        return annotations()
                .filter(annotation -> annotation.is(Pattern.class))
                .filter(annotation -> !annotation.string(VALUE).isEmpty())
                .map(annotation -> {

                    final String value=annotation.string(VALUE);

                    try {

                        return new Reference<>(

                                shape().pattern(value)
                                        .pattern()
                                        .orElseThrow(() -> new AssertionError(simple(Pattern.class))),

                                annotation.source()

                        );

                    } catch ( final IllegalArgumentException e ) {

                        throw new FrameException(format(
                                "illegal @%s expression <%s> @ <%s> : %s",
                                simple(Pattern.class), value, annotation.source(), e.getMessage()
                        ));

                    }

                })
                .reduce((x, y) -> {

                    try { // merge parent value into child value

                        final String value=shape().pattern(y.value())
                                .merge(shape().pattern(x.value()))
                                .pattern()
                                .orElseThrow(() -> new AssertionError(simple(Pattern.class)));

                        return new Reference<>(value, x.source());

                    } catch ( final IllegalArgumentException e ) {

                        throw new FrameException(format(
                                "conflicting @%s declarations %s / %s",
                                simple(Pattern.class), y, x
                        ));

                    }

                })
                .map(Reference::value);
    }

    public Optional<Set<Value>> in() {

        final Value datatype=datatype();

        return annotations()
                .filter(annotation -> annotation.is(In.class))
                .filter(not(annotation -> annotation.list(VALUE).isEmpty()))
                .map(annotation -> {

                    final List<?> value=annotation.list(VALUE);

                    try {

                        return new Reference<>(

                                shape().in(set(value.stream().map(v -> datatype.decode((String)v)
                                        .orElseThrow(() -> new FrameException(format(
                                                "malformed @%s value <%s> @ <%s>",
                                                simple(In.class), value, annotation.source()
                                        )))
                                ))).in().orElseThrow(() -> new AssertionError(simple(In.class))),

                                annotation.source()
                        );

                    } catch ( final IllegalArgumentException e ) {

                        throw new FrameException(format(
                                "illegal @%s values <%s> @ <%s> : %s",
                                simple(In.class), value, annotation.source(), e.getMessage()
                        ));

                    }

                })
                .reduce((x, y) -> {

                    try { // merge parent value into child value

                        final Set<Value> value=shape().in(y.value())
                                .merge(shape().in(x.value()))
                                .in()
                                .orElseThrow(() -> new AssertionError(simple(In.class)));

                        return new Reference<>(value, x.source());

                    } catch ( final IllegalArgumentException e ) {

                        throw new FrameException(format(
                                "conflicting @%s declarations %s / %s",
                                simple(MinExclusive.class), y, x
                        ));

                    }

                })
                .map(Reference::value);
    }

    public Optional<Set<Locale>> languageIn() {
        return annotations()
                .filter(annotation -> annotation.is(LanguageIn.class))
                .filter(not(annotation -> annotation.list(VALUE).isEmpty()))
                .map(annotation -> {

                    if ( !TEXTUAL.contains(type) ) {
                        throw new FrameException(format(
                                "@%s constraint on non-textual property <%s>",
                                simple(LanguageIn.class), annotation.source()
                        ));
                    }

                    final List<?> value=annotation.list(VALUE);

                    try {

                        return new Reference<>(

                                shape().languageIn(set(value.stream().map(v -> {

                                    try {

                                        return Locales.locale((String)v);

                                    } catch ( final IllegalArgumentException e ) {

                                        throw new FrameException(format(
                                                "malformed @%s locale <%s> @ <%s>",
                                                simple(LanguageIn.class), value, annotation.source()
                                        ));

                                    }

                                }))).languageIn().orElseThrow(() -> new AssertionError(simple(LanguageIn.class))),

                                annotation.source()
                        );

                    } catch ( final IllegalArgumentException e ) {

                        throw new FrameException(format(
                                "illegal @%s values <%s> @ <%s> : %s",
                                simple(LanguageIn.class), value, annotation.source(), e.getMessage()
                        ));

                    }

                })
                .reduce((x, y) -> {

                    try { // merge parent value into child value

                        final Set<Locale> value=shape().languageIn(y.value())
                                .merge(shape().languageIn(x.value()))
                                .languageIn()
                                .orElseThrow(() -> new AssertionError(simple(LanguageIn.class)));

                        return new Reference<>(value, x.source());

                    } catch ( final IllegalArgumentException e ) {

                        throw new FrameException(format(
                                "conflicting @%s declarations %s / %s",
                                simple(MinExclusive.class), y, x
                        ));

                    }

                })
                .map(Reference::value);
    }


    public boolean uniqueLang() {
        return type.equals(TEXTS);
    }

    public Optional<Integer> minCount() {
        return annotations()
                .filter(annotation -> annotation.is(MinCount.class) || annotation.is(Required.class))
                .filter(annotation -> annotation.integer(VALUE) > 0 || annotation.is(Required.class))
                .map(annotation -> {

                    final int limit=annotation.is(Required.class) ? 1 : annotation.integer(VALUE);

                    if ( !isMultiple() && limit > 1 ) {
                        throw new FrameException(format(
                                "illegal @%s limit on scalar property <%s>",
                                simple(MinCount.class), annotation.source()
                        ));
                    }

                    try {

                        return new Reference<>(

                                shape().minCount(limit)
                                        .minCount()
                                        .orElseThrow(() -> new AssertionError(simple(MinCount.class))),

                                annotation.source()

                        );

                    } catch ( final IllegalArgumentException e ) {

                        throw new FrameException(format(
                                "illegal @%s limit <%s> @ <%s> : %s",
                                simple(MinCount.class), limit, annotation.source(), e.getMessage()
                        ));

                    }

                })
                .reduce((x, y) -> {

                    try { // merge parent value into child value

                        final Integer value=shape().minCount(y.value())
                                .merge(shape().minCount(x.value()))
                                .minCount()
                                .orElseThrow(() -> new AssertionError(simple(MinCount.class)));

                        return new Reference<>(value, x.source());

                    } catch ( final IllegalArgumentException e ) {

                        throw new FrameException(format(
                                "conflicting @%s declarations %s / %s",
                                simple(MinCount.class), y, x
                        ));

                    }

                })
                .map(Reference::value);
    }

    public Optional<Integer> maxCount() {
        return annotations()
                .filter(annotation -> annotation.is(MaxCount.class))
                .filter(annotation -> annotation.integer(VALUE) > 0)
                .map(annotation -> {

                    final int limit=annotation.integer(VALUE);

                    if ( !isMultiple() && limit > 1 ) {
                        throw new FrameException(format(
                                "illegal @%s limit on scalar property <%s>",
                                simple(MaxCount.class), annotation.source()
                        ));
                    }

                    try {

                        return new Reference<>(

                                shape().maxCount(limit)
                                        .maxCount()
                                        .orElseThrow(() -> new AssertionError(simple(MaxCount.class))),

                                annotation.source()

                        );

                    } catch ( final IllegalArgumentException e ) {

                        throw new FrameException(format(
                                "illegal @%s limit <%s> @ <%s> : %s",
                                simple(MaxCount.class), limit, annotation.source(), e.getMessage()
                        ));

                    }

                })
                .reduce((x, y) -> {

                    try { // merge parent value into child value

                        final Integer value=shape().maxCount(y.value())
                                .merge(shape().maxCount(x.value()))
                                .maxCount()
                                .orElseThrow(() -> new AssertionError(simple(MaxCount.class)));

                        return new Reference<>(value, x.source());

                    } catch ( final IllegalArgumentException e ) {

                        throw new FrameException(format(
                                "conflicting @%s declarations %s / %s",
                                simple(MaxCount.class), y, x
                        ));

                    }

                })
                .map(Reference::value)
                .or(() -> isMultiple() ? Optional.empty() : Optional.of(1));
    }

    @SuppressWarnings("NonBooleanMethodNameMayNotStartWithQuestion")
    public Optional<Set<Value>> hasValue() {

        final Value datatype=datatype();

        return annotations()
                .filter(annotation -> annotation.is(HasValue.class))
                .filter(not(annotation -> annotation.list(VALUE).isEmpty()))
                .map(annotation -> {

                    final List<?> value=annotation.list(VALUE);

                    try {

                        return new Reference<>(

                                shape().hasValue(set(value.stream().map(v -> datatype.decode((String)v)
                                        .orElseThrow(() -> new FrameException(format(
                                                "malformed @%s value <%s> @ <%s>",
                                                simple(HasValue.class), value, annotation.source()
                                        )))
                                ))).hasValue().orElseThrow(() -> new AssertionError(simple(HasValue.class))),

                                annotation.source()
                        );

                    } catch ( final IllegalArgumentException e ) {

                        throw new FrameException(format(
                                "illegal @%s values <%s> @ <%s> : %s",
                                simple(HasValue.class), value, annotation.source(), e.getMessage()
                        ));

                    }

                })
                .reduce((x, y) -> {

                    try { // merge parent value into child value

                        final Set<Value> value=shape().hasValue(y.value())
                                .merge(shape().hasValue(x.value()))
                                .hasValue()
                                .orElseThrow(() -> new AssertionError(simple(HasValue.class)));

                        return new Reference<>(value, x.source());

                    } catch ( final IllegalArgumentException e ) {

                        throw new FrameException(format(
                                "conflicting @%s declarations %s / %s",
                                simple(MinExclusive.class), y, x
                        ));

                    }

                })
                .map(Reference::value);
    }


    //̸/////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Override public String toString() {
        return "%s.%s()".formatted(clazz.name(), name);
    }

}
