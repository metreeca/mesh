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

import com.metreeca.mesh.Value;
import com.metreeca.mesh.meta.jsonld.Class;
import com.metreeca.mesh.meta.jsonld.Embedded;
import com.metreeca.mesh.meta.jsonld.Property;
import com.metreeca.mesh.meta.jsonld.Virtual;
import com.metreeca.mesh.meta.shacl.*;
import com.metreeca.mesh.shapes.Shape;
import com.metreeca.mesh.shapes.Type;
import com.metreeca.mesh.util.Locales;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.*;
import java.time.temporal.Temporal;
import java.time.temporal.TemporalAmount;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.regex.PatternSyntaxException;
import java.util.stream.Stream;

import static com.metreeca.mesh.Value.*;
import static com.metreeca.mesh.mint._RecordGlass.*;
import static com.metreeca.mesh.mint._RecordModel.*;
import static com.metreeca.mesh.shapes.Shape.shape;
import static com.metreeca.mesh.util.Collections.set;
import static com.metreeca.mesh.util.Exceptions.error;

import static java.lang.String.format;
import static java.util.function.Predicate.not;

public final class _RecordShaper {

    private final Tree tree;


    public _RecordShaper(final Tree tree) {
        this.tree=tree;
    }


    //̸/////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public Shape virtual() {
        return shape().virtual(tree.stream().anyMatch(meta -> meta.is(Virtual.class)));
    }

    public Shape clazz() {

        final List<Type> explicit=clazz(tree).toList();
        final List<Type> implicit=clazzes(tree).toList();

        if ( explicit.size() > 1 ) {
            throw new AssertionError("multiple declared @Class annotations");
        }

        return explicit.isEmpty()
                ? shape().clazzes(implicit)
                : shape().clazz(explicit.getFirst(), implicit);
    }


    private Stream<Type> clazz(final Tree tree) {

        final _RecordResolver resolver=new _RecordResolver(tree);

        return tree.metas().stream()
                .filter(meta -> meta.is(Class.class))
                .map(meta -> {

                    final String value=Optional.ofNullable(meta.string(VALUE)).orElse("");

                    try {

                        return Type.type(tree.name(), resolver.resolve(value, tree.name()));

                    } catch ( final URISyntaxException e ) {

                        throw new IllegalArgumentException(format(
                                "malformed @%s URI <%s>: %s", simple(Class.class), value, e.getMessage()
                        ));

                    }
                });
    }

    private Stream<Type> clazzes(final Tree tree) {
        return Stream.concat(
                clazz(tree),
                tree.ancestors().stream().flatMap(this::clazzes)
        );
    }


    public Set<String> constraints() {
        return set(tree.stream()
                .filter(meta -> meta.is(Constraint.class))
                .map(meta -> meta.string(VALUE))
        );
    }


    //̸/////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public com.metreeca.mesh.shapes.Property property(final Tree clazz, final Tree method) {

        return properties(clazz, method)
                .reduce((x, y) -> x.value().equals(y.value()) ? x : error(new IllegalArgumentException(format(
                        "conflicting @%s declarations <%s> / <%s>", simple(Property.class), x, y
                ))))
                .map(Tray::value)
                .orElseGet(() -> {

                    final String name=method.name();
                    final _RecordResolver resolver=new _RecordResolver(clazz);

                    try {

                        return com.metreeca.mesh.shapes.Property.property(name, resolver.resolve(name, name));

                    } catch ( final URISyntaxException e ) {

                        throw new IllegalArgumentException(format(
                                "malformed URI <%s>: %s", name, e.getMessage()
                        ));

                    }

                });
    }

    private Stream<Tray<com.metreeca.mesh.shapes.Property>> properties(final Tree clazz, final Tree method) {

        final _RecordResolver resolver=new _RecordResolver(clazz);

        final Stream<Tray<com.metreeca.mesh.shapes.Property>> xxx=method.metas().stream()
                .filter(meta -> meta.is(Property.class))
                .map(meta -> {

                    try {

                        return new Tray<>(

                                com.metreeca.mesh.shapes.Property.property(method.name())
                                        .reverse(meta.flag(REVERSE))
                                        .uri(resolver.resolve(meta.string(VALUE), method.name())),

                                meta.source()

                        );

                    } catch ( final URISyntaxException e ) {

                        throw new IllegalArgumentException(format(
                                "malformed @%s URI <%s>: %s",
                                simple(Property.class), meta.string(VALUE), e.getMessage()
                        ));

                    }

                });

        final Stream<Tray<com.metreeca.mesh.shapes.Property>> yyy=method.ancestors().stream()
                .flatMap(t -> properties(clazz, t));

        return Stream.concat(xxx, yyy);
    }


    public boolean embedded() {
        return tree.stream().anyMatch(meta -> meta.is(Embedded.class));
    }

    public Shape datatype(final String type) {
        return shape().datatype(type.isEmpty() ? Nil()

                : type.equals(simple(void.class)) ? Nil()
                : type.equals(simple(boolean.class)) ? Bit()
                : type.equals(simple(byte.class)) ? Integral()
                : type.equals(simple(short.class)) ? Integral()
                : type.equals(simple(int.class)) ? Integral()
                : type.equals(simple(long.class)) ? Integral()
                : type.equals(simple(float.class)) ? Floating()
                : type.equals(simple(double.class)) ? Floating()

                : type.equals(simple(Void.class)) ? Nil()
                : type.equals(simple(Boolean.class)) ? Bit()
                : type.equals(simple(Number.class)) ? Number()
                : type.equals(simple(Byte.class)) ? Integral()
                : type.equals(simple(Short.class)) ? Integral()
                : type.equals(simple(Integer.class)) ? Integral()
                : type.equals(simple(Long.class)) ? Integral()
                : type.equals(simple(Float.class)) ? Floating()
                : type.equals(simple(Double.class)) ? Floating()
                : type.equals(simple(BigInteger.class)) ? Integer()
                : type.equals(simple(BigDecimal.class)) ? Decimal()
                : type.equals(simple(String.class)) ? String()
                : type.equals(simple(URI.class)) ? URI()
                : type.equals(simple(Temporal.class)) ? Temporal()
                : type.equals(simple(Year.class)) ? Year()
                : type.equals(simple(YearMonth.class)) ? YearMonth()
                : type.equals(simple(LocalDate.class)) ? LocalDate()
                : type.equals(simple(LocalTime.class)) ? LocalTime()
                : type.equals(simple(OffsetTime.class)) ? OffsetTime()
                : type.equals(simple(LocalDateTime.class)) ? LocalDateTime()
                : type.equals(simple(OffsetDateTime.class)) ? OffsetDateTime()
                : type.equals(simple(ZonedDateTime.class)) ? ZonedDateTime()
                : type.equals(simple(Instant.class)) ? Instant()
                : type.equals(simple(TemporalAmount.class)) ? TemporalAmount()
                : type.equals(simple(Period.class)) ? Period()
                : type.equals(simple(Duration.class)) ? Duration()

                : type.equals(TEXT) ? Text()
                : type.equals(TEXTS) ? Text()
                : type.equals(TEXTSETS) ? Text()

                : type.equals(DATA) ? Data()
                : type.equals(DATAS) ? Data()
                : type.equals(DATASETS) ? Data()

                : Object()
        );
    }

    public Shape minExclusive(final String type) {

        final Value datatype=datatype(type).datatype().orElseGet(Value::Nil);

        return tree.stream()
                .filter(meta -> meta.is(MinExclusive.class))
                .filter(meta -> !meta.string(VALUE).isEmpty())
                .map(meta -> {

                    final String value=meta.string(VALUE);

                    return new Tray<>(

                            shape().minExclusive(datatype.decode(value)
                                    .orElseThrow(() -> {

                                        return new IllegalArgumentException(format(
                                                "malformed @%s limit <%s> @ <%s>",
                                                simple(MinExclusive.class), value, meta.source()
                                        ));
                                    })
                            ),

                            meta.source()
                    );

                })
                .reduce((x, y) -> {

                    try {

                        return new Tray<>(x.value().merge(y.value()), x.source());

                    } catch ( final RuntimeException e ) {

                        throw new IllegalArgumentException(format(
                                "conflicting @%s declarations <%s> / <%s>",
                                simple(MinExclusive.class), x, y
                        ), e);

                    }

                })
                .map(Tray::value)
                .orElseGet(Shape::shape);
    }

    public Shape maxExclusive(final String type) {

        final Value datatype=datatype(type).datatype().orElseGet(Value::Nil);

        return tree.stream()
                .filter(meta -> meta.is(MaxExclusive.class))
                .filter(meta -> !meta.string(VALUE).isEmpty())
                .map(meta -> {

                    final String value=meta.string(VALUE);

                    return new Tray<>(

                            shape().maxExclusive(datatype.decode(value)
                                    .orElseThrow(() -> new IllegalArgumentException(format(
                                            "malformed @%s limit <%s> @ <%s>",
                                            simple(MaxExclusive.class), value, meta.source()
                                    )))
                            ),

                            meta.source()
                    );

                })
                .reduce((x, y) -> {
                    try {

                        return new Tray<>(x.value().merge(y.value()), x.source());

                    } catch ( final RuntimeException e ) {

                        throw new IllegalArgumentException(format(
                                "conflicting @%s declarations <%s> / <%s>",
                                simple(MaxExclusive.class), x, y
                        ), e);

                    }

                })
                .map(Tray::value)
                .orElseGet(Shape::shape);
    }

    public Shape minInclusive(final String type) {

        final Value datatype=datatype(type).datatype().orElseGet(Value::Nil);

        return tree.stream()
                .filter(meta -> meta.is(MinInclusive.class))
                .filter(meta -> !meta.string(VALUE).isEmpty())
                .map(meta -> {

                    final String value=meta.string(VALUE);

                    return new Tray<>(

                            shape().minInclusive(datatype.decode(value)
                                    .orElseThrow(() -> new IllegalArgumentException(format(
                                            "malformed @%s limit <%s> @ <%s>",
                                            simple(MinInclusive.class), value, meta.source()
                                    )))
                            ),

                            meta.source()
                    );

                })
                .reduce((x, y) -> {
                    try {

                        return new Tray<>(x.value().merge(y.value()), x.source());

                    } catch ( final RuntimeException e ) {

                        throw new IllegalArgumentException(format(
                                "conflicting @%s declarations <%s> / <%s>",
                                simple(MinInclusive.class), x, y
                        ), e);

                    }

                })
                .map(Tray::value)
                .orElseGet(Shape::shape);
    }

    public Shape maxInclusive(final String type) {

        final Value datatype=datatype(type).datatype().orElseGet(Value::Nil);

        return tree.stream()
                .filter(meta -> meta.is(MaxInclusive.class))
                .filter(meta -> !meta.string(VALUE).isEmpty())
                .map(meta -> {

                    final String value=meta.string(VALUE);

                    return new Tray<>(

                            shape().maxInclusive(datatype.decode(value)
                                    .orElseThrow(() -> new IllegalArgumentException(format(
                                            "malformed @%s limit <%s> @ <%s>",
                                            simple(MaxInclusive.class), value, meta.source()
                                    )))
                            ),

                            meta.source()
                    );

                })
                .reduce((x, y) -> {

                    try {

                        return new Tray<>(x.value().merge(y.value()), x.source());

                    } catch ( final RuntimeException e ) {

                        throw new IllegalArgumentException(format(
                                "conflicting @%s declarations <%s> / <%s>",
                                simple(MaxInclusive.class), x, y
                        ), e);

                    }

                })
                .map(Tray::value)
                .orElseGet(Shape::shape);
    }

    public Shape minLength() {
        return tree.stream()
                .filter(meta -> meta.is(MinLength.class))
                .map(meta -> new Tray<>(shape().minLength(meta.integer(VALUE)), meta.source()))
                .reduce((x, y) -> {

                    try {

                        return new Tray<>(x.value().merge(y.value()), x.source());

                    } catch ( final RuntimeException e ) {

                        throw new IllegalArgumentException(format(
                                "conflicting @%s declarations <%s> / <%s>",
                                simple(MaxLength.class), x, y
                        ), e);

                    }

                })
                .map(Tray::value)
                .orElseGet(Shape::shape);
    }

    public Shape maxLength() {
        return tree.stream()
                .filter(meta -> meta.is(MaxLength.class))
                .map(meta -> new Tray<>(shape().maxLength(meta.integer(VALUE)), meta.source()))
                .reduce((x, y) -> {

                    try {

                        return new Tray<>(x.value().merge(y.value()), x.source());

                    } catch ( final RuntimeException e ) {

                        throw new IllegalArgumentException(format(
                                "conflicting @%s declarations <%s> / <%s>",
                                simple(MaxLength.class), x, y
                        ), e);

                    }

                })
                .map(Tray::value)
                .orElseGet(Shape::shape);
    }

    public Shape pattern() {
        return tree.stream()
                .filter(meta -> meta.is(Pattern.class))
                .filter(meta -> !meta.string(VALUE).isEmpty())
                .map(meta -> {

                    final String value=meta.string("value");

                    try {

                        return new Tray<>(shape().pattern(value), meta.source());

                    } catch ( final PatternSyntaxException e ) {

                        throw new IllegalArgumentException(format(
                                "malformed @%s expression %s",
                                simple(Pattern.class), new Tray<>(value, meta.source())
                        ), e);

                    }

                })
                .reduce((x, y) -> {

                    try {

                        return new Tray<>(x.value().merge(y.value()), x.source());

                    } catch ( final RuntimeException e ) {

                        throw new IllegalArgumentException(format(
                                "conflicting @%s declarations <%s> / <%s>",
                                simple(Pattern.class), x, y
                        ), e);

                    }

                })
                .map(Tray::value)
                .orElseGet(Shape::shape);
    }

    public Shape in(final String type) {

        final Value datatype=datatype(type).datatype().orElseGet(Value::Nil);

        return tree.stream()
                .filter(meta -> meta.is(In.class))
                .filter(not(meta -> meta.list(VALUE).isEmpty()))
                .map(meta -> {

                    final List<?> value=meta.list(VALUE);

                    return new Tray<>(

                            shape().in(set(value.stream()
                                    .map(v -> datatype.decode((String)v)
                                            .orElseThrow(() -> new IllegalArgumentException(format(
                                                    "malformed @%s value <%s> @ <%s>",
                                                    simple(In.class), value, meta.source()
                                            )))
                                    )
                            )),

                            meta.source()
                    );

                })
                .reduce((x, y) -> {
                    try {

                        return new Tray<>(x.value().merge(y.value()), x.source());

                    } catch ( final RuntimeException e ) {

                        throw new IllegalArgumentException(format(
                                "conflicting @%s declarations <%s> / <%s>",
                                simple(MinExclusive.class), x, y
                        ), e);

                    }

                })
                .map(Tray::value)
                .orElseGet(Shape::shape);
    }

    public Shape languageIn(final String type) {
        return tree.stream()
                .filter(meta -> meta.is(LanguageIn.class))
                .filter(not(meta -> meta.list(VALUE).isEmpty()))
                .map(meta -> {

                    if ( !TEXTUAL.contains(type) ) {
                        throw new IllegalArgumentException(format(
                                "@%s constraint on non-textual property <%s>",
                                simple(LanguageIn.class), meta.source()
                        ));
                    }

                    final List<?> value=meta.list(VALUE);

                    return new Tray<>(

                            shape().languageIn(set(value.stream()
                                    .map(v -> {

                                        try {

                                            return Locales.locale((String)v);

                                        } catch ( final IllegalArgumentException e ) {

                                            throw new IllegalArgumentException(format(
                                                    "malformed @%s locale <%s> @ <%s>",
                                                    simple(LanguageIn.class), value, meta.source()
                                            ), e);

                                        }

                                    })
                            )),

                            meta.source()
                    );

                })
                .reduce((x, y) -> {
                    try {

                        return new Tray<>(x.value().merge(y.value()), x.source());

                    } catch ( final RuntimeException e ) {

                        throw new IllegalArgumentException(format(
                                "conflicting @%s declarations <%s> / <%s>",
                                simple(MinExclusive.class), x, y
                        ), e);

                    }

                })
                .map(Tray::value)
                .orElseGet(Shape::shape);
    }


    public Shape uniqueLang(final String type) {
        return shape().uniqueLang(type.equals(TEXTS));
    }

    public Shape minCount(final boolean multiple) {
        return tree.stream()
                .filter(meta -> meta.is(MinCount.class) || meta.is(Required.class))
                .map(meta -> {

                    final int limit=meta.is(Required.class) ? 1 : meta.integer(VALUE);

                    if ( !multiple && limit > 1 ) {
                        throw new IllegalArgumentException(format(
                                "illegal @%s limit on scalar property <%s>",
                                simple(MinCount.class), meta.source()
                        ));
                    }

                    return new Tray<>(
                            shape().minCount(limit),
                            meta.source()
                    );

                })
                .reduce((x, y) -> {

                    try {

                        return new Tray<>(x.value().merge(y.value()), x.source());

                    } catch ( final RuntimeException e ) {

                        throw new IllegalArgumentException(format(
                                "conflicting @%s declarations <%s> / <%s>",
                                simple(MinCount.class), x, y
                        ), e);

                    }

                })
                .map(Tray::value)
                .orElseGet(Shape::shape);
    }

    public Shape maxCount(final boolean multiple) {
        return tree.stream()
                .filter(meta -> meta.is(MaxCount.class))
                .map(meta -> {

                    final int limit=meta.integer(VALUE);

                    if ( !multiple && limit > 1 ) {
                        throw new IllegalArgumentException(format(
                                "illegal @%s limit on scalar property <%s>",
                                simple(MaxCount.class), meta.source()
                        ));
                    }

                    return new Tray<>(
                            shape().maxCount(limit),
                            meta.source()
                    );

                })
                .reduce((x, y) -> {

                    try {

                        return new Tray<>(x.value().merge(y.value()), x.source());

                    } catch ( final RuntimeException e ) {

                        throw new IllegalArgumentException(format(
                                "conflicting @%s declarations <%s> / <%s>",
                                simple(MaxCount.class), x, y
                        ), e);

                    }

                })
                .map(Tray::value)
                .orElseGet(() -> multiple ? shape() : shape().maxCount(1));
    }

    @SuppressWarnings("NonBooleanMethodNameMayNotStartWithQuestion") public Shape hasValue(final String type) {

        final Value datatype=datatype(type).datatype().orElseGet(Value::Nil);

        return tree.stream()
                .filter(meta -> meta.is(HasValue.class))
                .filter(not(meta -> meta.list(VALUE).isEmpty()))
                .map(meta -> {

                    final List<?> value=meta.list(VALUE);

                    return new Tray<>(

                            shape().hasValue(set(value.stream()
                                    .map(v -> datatype.decode((String)v)
                                            .orElseThrow(() -> new IllegalArgumentException(format(
                                                    "malformed @%s value <%s> @ <%s>",
                                                    simple(HasValue.class), value, meta.source()
                                            )))
                                    )
                            )),

                            meta.source()
                    );

                })
                .reduce((x, y) -> {

                    try {

                        return new Tray<>(x.value().merge(y.value()), x.source());

                    } catch ( final RuntimeException e ) {

                        throw new IllegalArgumentException(format(
                                "conflicting @%s declarations <%s> / <%s>",
                                simple(MinExclusive.class), x, y
                        ), e);

                    }

                })
                .map(Tray::value)
                .orElseGet(Shape::shape);
    }

}
