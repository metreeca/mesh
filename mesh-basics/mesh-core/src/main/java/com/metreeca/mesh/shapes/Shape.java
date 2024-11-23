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

package com.metreeca.mesh.shapes;

import com.metreeca.mesh.Value;
import com.metreeca.shim.Collections;
import com.metreeca.shim.Locales;

import java.util.*;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static com.metreeca.mesh.Value.*;
import static com.metreeca.shim.Collections.set;
import static com.metreeca.shim.Exceptions.error;

import static java.lang.String.format;
import static java.util.Arrays.asList;
import static java.util.function.Function.identity;
import static java.util.function.Predicate.not;
import static java.util.stream.Collectors.toMap;

/**
 * Value validation constraints.
 *
 * @param virtual
 * @param id
 * @param type
 * @param datatype
 * @param clazzes
 * @param minExclusive
 * @param maxExclusive
 * @param minInclusive
 * @param maxInclusive
 * @param minLength
 * @param maxLength
 * @param pattern
 * @param in
 * @param languageIn
 * @param uniqueLang
 * @param minCount
 * @param maxCount
 * @param hasValue
 * @param constraints
 * @param properties
 *
 * @see <a href="https://www.w3.org/TR/shacl/#core-components">Shapes Constraint Language (SHACL)
 *         - 4. Core Constraint Components</a>
 */
@SuppressWarnings("NonBooleanMethodNameMayNotStartWithQuestion")
public final record Shape(

        boolean virtual,
        Optional<String> id,
        Optional<String> type,

        Optional<Value> datatype,
        Optional<Type> clazz,
        Optional<Set<Type>> clazzes,
        Optional<Value> minExclusive,
        Optional<Value> maxExclusive,
        Optional<Value> minInclusive,
        Optional<Value> maxInclusive,
        Optional<Integer> minLength,
        Optional<Integer> maxLength,
        Optional<String> pattern, // ;( as String to support Shape equality: Patterns don't implement .equals()…
        Optional<Set<Value>> in,
        Optional<Set<Locale>> languageIn,

        boolean uniqueLang,
        Optional<Integer> minCount,
        Optional<Integer> maxCount,
        Optional<Set<Value>> hasValue,

        Optional<Set<Function<Value, Value>>> constraints,
        Collection<Property> properties

) {

    private static final Shape EMPTY=new Shape(

            false,
            Optional.empty(),
            Optional.empty(),

            Optional.empty(),
            Optional.empty(),
            Optional.empty(),
            Optional.empty(),
            Optional.empty(),
            Optional.empty(),
            Optional.empty(),
            Optional.empty(),
            Optional.empty(),
            Optional.empty(),
            Optional.empty(),
            Optional.empty(),

            false,
            Optional.empty(),
            Optional.empty(),
            Optional.empty(),

            Optional.empty(),
            set()

    );


    public static Shape shape() {
        return EMPTY;
    }


    //̸/////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public Shape(

            final boolean virtual,
            final Optional<String> id,
            final Optional<String> type,

            final Optional<Value> datatype,
            final Optional<Type> clazz,
            final Optional<Set<Type>> clazzes,
            final Optional<Value> minExclusive,
            final Optional<Value> maxExclusive,
            final Optional<Value> minInclusive,
            final Optional<Value> maxInclusive,
            final Optional<Integer> minLength,
            final Optional<Integer> maxLength,
            final Optional<String> pattern,
            final Optional<Set<Value>> in,
            final Optional<Set<Locale>> languageIn,
            final boolean uniqueLang,
            final Optional<Integer> minCount,
            final Optional<Integer> maxCount,
            final Optional<Set<Value>> hasValue,

            final Optional<Set<Function<Value, Value>>> constraints,

            final Collection<Property> properties

    ) {

        if ( id == null ) {
            throw new NullPointerException("null id property name");
        }

        if ( type == null ) {
            throw new NullPointerException("null type property name");
        }

        if ( datatype == null ) {
            throw new NullPointerException("null datatype");
        }

        if ( clazz == null ) {
            throw new NullPointerException("null clazz");
        }

        if ( clazzes == null ) {
            throw new NullPointerException("null clazz");
        }

        if ( clazzes.map(types -> types.stream().anyMatch(Objects::isNull)).orElse(false) ) {
            throw new NullPointerException("null clazz types");
        }

        if ( minExclusive == null ) {
            throw new NullPointerException("null minExclusive");
        }

        if ( maxExclusive == null ) {
            throw new NullPointerException("null maxExclusive");
        }

        if ( minInclusive == null ) {
            throw new NullPointerException("null minInclusive");
        }

        if ( maxInclusive == null ) {
            throw new NullPointerException("null maxInclusive");
        }

        if ( minLength == null ) {
            throw new NullPointerException("null minLength");
        }

        if ( maxLength == null ) {
            throw new NullPointerException("null maxLength");
        }

        if ( pattern == null ) {
            throw new NullPointerException("null pattern");
        }

        if ( in == null ) {
            throw new NullPointerException("null in");
        }

        if ( languageIn == null ) {
            throw new NullPointerException("null languageIn");
        }

        if ( languageIn.map(locales -> locales.stream().anyMatch(Objects::isNull)).orElse(false) ) {
            throw new NullPointerException("null languageIn locales");
        }

        if ( minCount == null ) {
            throw new NullPointerException("null minCount");
        }

        if ( maxCount == null ) {
            throw new NullPointerException("null maxCount");
        }

        if ( hasValue == null ) {
            throw new NullPointerException("null hasValue");
        }

        if ( constraints == null ) {
            throw new NullPointerException("null constraints");
        }

        if ( constraints.map(functions -> functions.stream().anyMatch(Objects::isNull)).orElse(false) ) {
            throw new NullPointerException("null constraints functions");
        }

        if ( properties == null || properties.stream().anyMatch(Objects::isNull) ) {
            throw new NullPointerException("null properties");
        }


        id.filter(Value::isReserved)
                .ifPresent(v -> error(new IllegalArgumentException(format(
                        "reserved id property name <%s>", v
                ))));

        type.filter(Value::isReserved)
                .ifPresent(v -> error(new IllegalArgumentException(format(
                        "reserved type property name <%s>", v
                ))));


        final boolean object=virtual
                             || id.isPresent()
                             || type.isPresent()
                             || datatype.map(Object()::equals).orElse(false)
                             || clazz.isPresent()
                             || clazzes.map(t -> !t.isEmpty()).orElse(false)
                             || constraints.map(s -> !s.isEmpty()).orElse(false)
                             || properties.stream().anyMatch(p -> p.forward().isPresent());

        final boolean text=uniqueLang
                           || languageIn.map(s -> !s.isEmpty()).orElse(false);

        if ( datatype.flatMap(t -> t.array()).isPresent() ) {
            throw new IllegalArgumentException(format("array datatype <%s>", datatype.get()));
        }

        datatype.stream()
                .filter(t -> object)
                .filter(not(Object()::equals))
                .forEach(t -> error(new IllegalArgumentException(format(
                        "incompatible datatype(<%s>) on object shape", t
                ))));

        datatype.stream()
                .filter(t -> text)
                .filter(not(Text()::equals))
                .forEach(t -> error(new IllegalArgumentException(format(
                        "incompatible datatype(<%s>) on text shape", t
                ))));


        final Set<Type> implicit=set(Stream.concat(
                clazz.stream(),
                clazzes.stream().flatMap(Collection::stream)
        ));

        implicit.forEach(x -> implicit.forEach(y -> {

            if ( !x.equals(y) && (x.name().equals(y.name()) || x.uri().equals(y.uri())) ) {

                throw new IllegalArgumentException(format(
                        "conflicting clazz(<%s>) / clazz(<%s>) definitions", x, y
                ));

            }

        }));


        if ( !minInclusive.map(Value::comparable).orElse(true) ) {
            throw new IllegalArgumentException(format(
                    "incomparable minInclusive(<%s>) limit", minInclusive.get()
            ));
        }

        if ( !maxInclusive.map(Value::comparable).orElse(true) ) {
            throw new IllegalArgumentException(format(
                    "incomparable maxInclusive(<%s>) limit", maxInclusive.get()
            ));
        }

        if ( !minExclusive.map(Value::comparable).orElse(true) ) {
            throw new IllegalArgumentException(format(
                    "incomparable minExclusive(<%s>) limit", minExclusive.get()
            ));
        }

        if ( !maxExclusive.map(Value::comparable).orElse(true) ) {
            throw new IllegalArgumentException(format(
                    "incomparable maxExclusive(<%s>) limit", maxExclusive.get()
            ));
        }


        if ( minInclusive.isPresent() && minExclusive.isPresent() ) {
            throw new IllegalArgumentException(format(
                    "incompatible minInclusive(<%s>) / minExclusive(%s) limits",
                    minInclusive.get(), minExclusive.get()
            ));
        }

        if ( maxInclusive.isPresent() && maxExclusive.isPresent() ) {
            throw new IllegalArgumentException(format(
                    "incompatible maxInclusive(<%s>) / maxExclusive(%s) limits",
                    maxInclusive.get(), maxExclusive.get()
            ));
        }


        if ( !compare(minExclusive, maxInclusive).map(v -> v < 0).orElse(true) ) {
            throw new IllegalArgumentException(format(
                    "conflicting minExclusive(<%s>) / maxInclusive(<%s>) limits",
                    minExclusive.get(), maxInclusive.get()
            ));
        }

        if ( !compare(minExclusive, maxExclusive).map(v -> v < 0).orElse(true) ) {
            throw new IllegalArgumentException(format(
                    "conflicting minExclusive(<%s>) / maxExclusive(<%s>) limits",
                    minExclusive.get(), maxExclusive.get()
            ));
        }

        if ( !compare(minInclusive, maxInclusive).map(v -> v <= 0).orElse(true) ) {
            throw new IllegalArgumentException(format(
                    "conflicting minInclusive(<%s>) / maxInclusive(<%s>) limits",
                    maxInclusive.get(), maxInclusive.get()
            ));
        }

        if ( !compare(minInclusive, maxExclusive).map(v -> v < 0).orElse(true) ) {
            throw new IllegalArgumentException(format(
                    "conflicting minInclusive(<%s>) / maxExclusive(<%s>) limits",
                    minInclusive.get(), maxExclusive.get()
            ));
        }


        if ( !minLength.flatMap(xv -> maxLength.map(yv -> xv <= yv)).orElse(true) ) {
            throw new IllegalArgumentException(format(
                    "conflicting minLength(<%s>) / maxLength(<%s>) limits",
                    minLength.get(), maxLength.get()
            ));
        }

        if ( !minCount.flatMap(xv -> maxCount.map(yv -> xv <= yv)).orElse(true) ) {
            throw new IllegalArgumentException(format(
                    "conflicting minCount(<%s>) / maxCount(<%s>) limits",
                    minCount.get(), maxCount.get()
            ));
        }


        if ( in.map(values -> values.stream().anyMatch(v -> v.array().isPresent())).orElse(false) ) {
            throw new IllegalArgumentException(format("nested <focus> values <%s>", in.get()));
        }

        if ( hasValue.map(values -> values.stream().anyMatch(v -> v.array().isPresent())).orElse(false) ) {
            throw new IllegalArgumentException(format("nested <focus> values <%s>", hasValue.get()));
        }


        properties.forEach(p -> {

            if ( p.forward().isEmpty() && p.reverse().isEmpty() ) {
                throw new IllegalArgumentException(format(
                        "undefined forward/reverse predicates for property(%s)", p.name()
                ));
            }

        });

        properties.forEach(x -> properties.forEach(y -> {

            if ( x != y && x.name().equals(y.name()) ) {

                throw new IllegalArgumentException(format(
                        "multiple property(%s) definitions", x.name()
                ));

            }

            if ( x != y && x.forward().isPresent() && x.forward().equals(y.forward()) ) {

                throw new IllegalArgumentException(format(
                        "multiple property.name(%s).forward(%s) definitions", x.name(), x.forward().get()
                ));

            }

            if ( x != y && x.reverse().isPresent() && x.reverse().equals(y.reverse()) ) {

                throw new IllegalArgumentException(format(
                        "multiple property(%s).reverse(%s) definitions", x.name(), x.reverse().get()
                ));

            }

        }));


        this.virtual=virtual;
        this.id=id.filter(not(String::isEmpty));
        this.type=type.filter(not(String::isEmpty));

        this.datatype=object ? Optional.of(Object()) : text ? Optional.of(Text()) : datatype;
        this.clazz=clazz;
        this.clazzes=Optional.of(implicit).filter(not(Set::isEmpty));
        this.minExclusive=minExclusive;
        this.maxExclusive=maxExclusive;
        this.minInclusive=minInclusive;
        this.maxInclusive=maxInclusive;
        this.minLength=minLength.filter(v -> v > 0);
        this.maxLength=maxLength.filter(v -> v > 0);
        this.pattern=pattern.filter(p -> !p.isEmpty());
        this.in=in.filter(not(Set::isEmpty)).map(Collections::set);
        this.languageIn=languageIn.filter(not(Set::isEmpty)).map(Collections::set);

        this.uniqueLang=uniqueLang;
        this.minCount=minCount.filter(v -> v > 0);
        this.maxCount=maxCount.filter(v -> v > 0);
        this.hasValue=hasValue.filter(not(Set::isEmpty)).map(Collections::set);

        this.constraints=constraints.filter(not(Set::isEmpty)).map(Collections::set);
        this.properties=set(properties);

    }


    public boolean is(final Value datatype) {

        if ( datatype == null ) {
            throw new NullPointerException("null datatype");
        }

        return datatype().map(datatype::equals).orElse(false);
    }

    public boolean isMultiple() {
        return maxCount().map(limit -> limit > 1).orElse(true);
    }


    public Optional<Property> property(final String name) {

        if ( name == null ) {
            throw new NullPointerException("null name");
        }

        return properties.stream()
                .filter(p -> p.name().equals(name))
                .findFirst();
    }


    //̸/////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public Shape virtual(final boolean virtual) {
        return new Shape(

                virtual,
                id,
                type,

                datatype,
                clazz,
                clazzes,
                minExclusive,
                maxExclusive,
                minInclusive,
                maxInclusive,
                minLength,
                maxLength,
                pattern,
                in,

                languageIn,
                uniqueLang,
                minCount,
                maxCount,
                hasValue,

                constraints,
                properties

        );
    }

    public Shape id(final String id) {

        if ( id == null ) {
            throw new NullPointerException("null id");
        }

        return new Shape(

                virtual,
                Optional.of(id),
                type,

                Optional.of(Object()),
                clazz,
                clazzes,
                minExclusive,
                maxExclusive,
                minInclusive,
                maxInclusive,
                minLength,
                maxLength,
                pattern,
                in,
                languageIn,

                uniqueLang,
                minCount,
                maxCount,
                hasValue,

                constraints,
                properties

        );
    }

    public Shape type(final String type) {

        if ( type == null ) {
            throw new NullPointerException("null type");
        }

        return new Shape(

                virtual,
                id,
                Optional.of(type),

                Optional.of(Object()),
                clazz,
                clazzes,
                minExclusive,
                maxExclusive,
                minInclusive,
                maxInclusive,
                minLength,
                maxLength,
                pattern,
                in,
                languageIn,

                uniqueLang,
                minCount,
                maxCount,
                hasValue,

                constraints,
                properties

        );
    }


    public Shape datatype(final Value datatype) {

        if ( datatype == null ) {
            throw new NullPointerException("null datatype");
        }

        return new Shape(

                virtual,
                id,
                type,

                Optional.of(datatype),
                clazz,
                clazzes,
                minExclusive,
                maxExclusive,
                minInclusive,
                maxInclusive,
                minLength,
                maxLength,
                pattern,
                in,
                languageIn,

                uniqueLang,
                minCount,
                maxCount,
                hasValue,

                constraints,
                properties

        );
    }


    public Shape clazz(final Type explicit, final Type... implicit) {

        if ( explicit == null ) {
            throw new NullPointerException("null explicit type");
        }

        if ( implicit == null || Arrays.stream(implicit).anyMatch(Objects::isNull) ) {
            throw new NullPointerException("null implicit types");
        }

        return clazz(explicit, asList(implicit));
    }

    public Shape clazz(final Type explicit, final Collection<Type> implicit) {

        if ( explicit == null ) {
            throw new NullPointerException("null explicit type");
        }

        if ( implicit == null || implicit.stream().anyMatch(Objects::isNull) ) {
            throw new NullPointerException("null implicit types");
        }

        return new Shape(

                virtual,
                id,
                type,

                datatype,
                Optional.of(explicit),
                Optional.of(set(implicit)),
                minExclusive,
                maxExclusive,
                minInclusive,
                maxInclusive,
                minLength,
                maxLength,
                pattern,
                in,

                languageIn,
                uniqueLang,
                minCount,
                maxCount,
                hasValue,

                constraints,
                properties

        );
    }

    public Shape clazzes(final Type... implicit) {

        if ( implicit == null || Arrays.stream(implicit).anyMatch(Objects::isNull) ) {
            throw new NullPointerException("null implicit types");
        }

        return clazzes(asList(implicit));
    }

    public Shape clazzes(final Collection<Type> implicit) {

        if ( implicit == null || implicit.stream().anyMatch(Objects::isNull) ) {
            throw new NullPointerException("null implicit types");
        }

        return new Shape(

                virtual,
                id,
                type,

                datatype,
                Optional.empty(),
                Optional.of(set(implicit)),
                minExclusive,
                maxExclusive,
                minInclusive,
                maxInclusive,
                minLength,
                maxLength,
                pattern,
                in,

                languageIn,
                uniqueLang,
                minCount,
                maxCount,
                hasValue,

                constraints,
                properties

        );
    }


    public Shape minExclusive(final Value limit) {

        if ( limit == null ) {
            throw new NullPointerException("null limit");
        }

        // !!! only scalars

        return new Shape(

                virtual,
                id,
                type,

                datatype,
                clazz,
                clazzes,
                Optional.of(limit),
                maxExclusive,
                minInclusive,
                maxInclusive,
                minLength,
                maxLength,
                pattern,
                in,
                languageIn,

                uniqueLang,
                minCount,
                maxCount,
                hasValue,

                constraints,
                properties

        );
    }

    public Shape maxExclusive(final Value limit) {

        if ( limit == null ) {
            throw new NullPointerException("null limit");
        }

        // !!! only scalars

        return new Shape(

                virtual,
                id,
                type,

                datatype,
                clazz,
                clazzes,
                minExclusive,
                Optional.of(limit),
                minInclusive,
                maxInclusive,
                minLength,
                maxLength,
                pattern,
                in,

                languageIn,
                uniqueLang,
                minCount,
                maxCount,
                hasValue,

                constraints,
                properties

        );

    }

    public Shape minInclusive(final Value limit) {

        if ( limit == null ) {
            throw new NullPointerException("null limit");
        }

        return new Shape(

                virtual,
                id,
                type,

                datatype,
                clazz,
                clazzes,
                minExclusive,
                maxExclusive,
                Optional.of(limit),
                maxInclusive,
                minLength,
                maxLength,
                pattern,
                in,
                languageIn,

                uniqueLang,
                minCount,
                maxCount,
                hasValue,

                constraints,
                properties

        );

    }

    public Shape maxInclusive(final Value limit) {

        if ( limit == null ) {
            throw new NullPointerException("null limit");
        }

        return new Shape(

                virtual,
                id,
                type,

                datatype,
                clazz,
                clazzes,
                minExclusive,
                maxExclusive,
                minInclusive,
                Optional.of(limit),
                minLength,
                maxLength,
                pattern,
                in,
                languageIn,

                uniqueLang,
                minCount,
                maxCount,
                hasValue,

                constraints,
                properties

        );

    }


    public Shape minLength(final int limit) {

        if ( limit < 0 ) {
            throw new IllegalArgumentException("negative limit");
        }

        return new Shape(

                virtual,
                id,
                type,

                datatype,
                clazz,
                clazzes,
                minExclusive,
                maxExclusive,
                minInclusive,
                maxInclusive,
                Optional.of(limit),
                maxLength, pattern,
                in,
                languageIn,

                uniqueLang,
                minCount,
                maxCount,
                hasValue,

                constraints,
                properties

        );

    }

    public Shape maxLength(final int limit) {

        if ( limit < 0 ) {
            throw new IllegalArgumentException("negative limit");
        }

        return new Shape(

                virtual,
                id,
                type,

                datatype,
                clazz,
                clazzes,
                minExclusive,
                maxExclusive,
                minInclusive,
                maxInclusive,
                minLength,
                Optional.of(limit),
                pattern,
                in,
                languageIn,

                uniqueLang,
                minCount,
                maxCount,
                hasValue,

                constraints,
                properties

        );

    }

    public Shape pattern(final String pattern) {

        if ( pattern == null ) {
            throw new NullPointerException("null pattern");
        }

        return new Shape(

                virtual,
                id,
                type,
                datatype,
                clazz,
                clazzes,
                minExclusive,
                maxExclusive,
                minInclusive,
                maxInclusive,
                minLength,
                maxLength,
                Optional.of(Pattern.compile(pattern).pattern()), // ;( force syntax checks
                in,
                languageIn,
                uniqueLang,
                minCount,
                maxCount,
                hasValue,

                constraints,
                properties

        );

    }


    public Shape in(final Value... values) {

        if ( values == null || Arrays.stream(values).anyMatch(Objects::isNull) ) {
            throw new NullPointerException("null values");
        }

        return in(set(values));
    }

    public Shape in(final Collection<Value> values) {

        if ( values == null || values.stream().anyMatch(Objects::isNull) ) {
            throw new NullPointerException("null values");
        }

        if ( values.stream().anyMatch(v -> v.array().isPresent()) ) {
            throw new IllegalArgumentException("array values");
        }

        return new Shape(

                virtual,
                id,
                type,
                datatype,
                clazz,
                clazzes,
                minExclusive,
                maxExclusive,
                minInclusive,
                maxInclusive,
                minLength,
                maxLength,
                pattern,
                Optional.of(set(values)),
                languageIn,
                uniqueLang,
                minCount,
                maxCount,
                hasValue,

                constraints,
                properties

        );

    }


    public Shape languageIn(final String... locales) { // support auto-generated records

        if ( locales == null || Arrays.stream(locales).anyMatch(Objects::isNull) ) {
            throw new NullPointerException("null locales");
        }

        return languageIn(set(Arrays.stream(locales).map(Locales::locale)));
    }

    public Shape languageIn(final Locale... locales) {

        if ( locales == null || Arrays.stream(locales).anyMatch(Objects::isNull) ) {
            throw new NullPointerException("null locales");
        }

        return languageIn(set(locales));
    }

    public Shape languageIn(final Collection<Locale> locales) {

        if ( locales == null || locales.stream().anyMatch(Objects::isNull) ) {
            throw new NullPointerException("null locales");
        }

        return new Shape(

                virtual,
                id,
                type,

                datatype,
                clazz,
                clazzes,
                minExclusive,
                maxExclusive,
                minInclusive,
                maxInclusive,
                minLength,
                maxLength,
                pattern,
                in,
                Optional.of(set(locales)),

                uniqueLang,
                minCount,
                maxCount,
                hasValue,

                constraints,
                properties

        );

    }


    public Shape uniqueLang(final boolean uniqueLang) {
        return new Shape(

                virtual,
                id,
                type,
                datatype,
                clazz,
                clazzes,
                minExclusive,
                maxExclusive,
                minInclusive,
                maxInclusive,
                minLength,
                maxLength,
                pattern,
                in,
                languageIn,

                uniqueLang,
                minCount,
                maxCount,
                hasValue,

                constraints,
                properties

        );
    }

    public Shape minCount(final int limit) {

        if ( limit < 0 ) {
            throw new IllegalArgumentException("negative limit");
        }

        return new Shape(

                virtual,
                id,
                type,
                datatype,
                clazz,
                clazzes,
                minExclusive,
                maxExclusive,
                minInclusive,
                maxInclusive,
                minLength,
                maxLength,
                pattern,
                in,
                languageIn,
                uniqueLang,
                Optional.of(limit),
                maxCount,
                hasValue,

                constraints,
                properties

        );

    }

    public Shape maxCount(final int limit) {

        if ( limit < 0 ) {
            throw new IllegalArgumentException("negative limit");
        }

        return new Shape(

                virtual,
                id,
                type,
                datatype,
                clazz,
                clazzes,
                minExclusive,
                maxExclusive,
                minInclusive,
                maxInclusive,
                minLength,
                maxLength,
                pattern,
                in,
                languageIn,
                uniqueLang,
                minCount,
                Optional.of(limit),
                hasValue,

                constraints,
                properties

        );

    }


    public Shape multiple() {
        return this;
    }

    public Shape repeatable() {
        return minCount(1);
    }

    public Shape optional() {
        return maxCount(1);
    }

    public Shape required() {
        return exactly(1);
    }


    public Shape exactly(final int limit) {

        if ( limit < 0 ) {
            throw new IllegalArgumentException(format("negative limit <%d>", limit));
        }

        return minCount(limit).maxCount(limit);
    }


    public Shape hasValue(final Value... values) {

        if ( values == null || Arrays.stream(values).anyMatch(Objects::isNull) ) {
            throw new NullPointerException("null values");
        }

        return hasValue(set(values));
    }

    public Shape hasValue(final Collection<Value> values) {

        if ( values == null || values.stream().anyMatch(Objects::isNull) ) {
            throw new NullPointerException("null values");
        }

        if ( values.stream().anyMatch(v -> v.array().isPresent()) ) {
            throw new IllegalArgumentException("array values");
        }

        return new Shape(

                virtual,
                id,
                type,

                datatype,
                clazz,
                clazzes,
                minExclusive,
                maxExclusive,
                minInclusive,
                maxInclusive,
                minLength,
                maxLength,
                pattern,
                in,

                languageIn,
                uniqueLang,
                minCount,
                maxCount,
                Optional.of(set(values)),

                constraints,
                properties

        );

    }


    @SafeVarargs
    public final Shape constraints(final Function<Value, Value>... constraints) {

        if ( constraints == null || Arrays.stream(constraints).anyMatch(Objects::isNull) ) {
            throw new NullPointerException("null constraints");
        }

        return constraints(set(constraints));
    }

    public Shape constraints(final Collection<Function<Value, Value>> constraints) {

        if ( constraints == null || constraints.stream().anyMatch(Objects::isNull) ) {
            throw new NullPointerException("null constraints");
        }

        return new Shape(

                virtual,
                id,
                type,
                datatype,
                clazz,
                clazzes,
                minExclusive,
                maxExclusive,
                minInclusive,
                maxInclusive,
                minLength,
                maxLength,
                pattern,
                in,
                languageIn,
                uniqueLang,
                minCount,
                maxCount,
                hasValue,

                Optional.of(set(constraints)),
                properties

        );
    }


    public Shape property(final Property property) {

        if ( property == null ) {
            throw new NullPointerException("null property");
        }

        return properties(Stream.concat(properties.stream(), Stream.of(property)).toList());
    }

    public Shape properties(final Property... properties) {

        if ( properties == null || Arrays.stream(properties).anyMatch(Objects::isNull) ) {
            throw new NullPointerException("null properties");
        }

        return properties(asList(properties));

    }

    public Shape properties(final Collection<Property> properties) {

        if ( properties == null || properties.stream().anyMatch(Objects::isNull) ) {
            throw new NullPointerException("null properties");
        }

        return new Shape(

                virtual,
                id,
                type,

                datatype,
                clazz,
                clazzes,
                minExclusive,
                maxExclusive,
                minInclusive,
                maxInclusive,
                minLength,
                maxLength,
                pattern,
                in,

                languageIn,
                uniqueLang,
                minCount,
                maxCount,
                hasValue,

                constraints,
                properties

        );

    }


    //̸/////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public Shape extend(final Shape shape) {

        if ( shape == null ) {
            throw new NullPointerException("null shape");
        }

        return new Shape(

                virtual(virtual, shape.virtual),
                id(id, shape.id),
                type(type, shape.type),

                datatype(datatype, shape.datatype),
                clazz,
                clazzes(clazzes, shape.clazzes),
                minExclusive(minExclusive, shape.minExclusive),
                maxExclusive(maxExclusive, shape.maxExclusive),
                minInclusive(minInclusive, shape.minInclusive),
                maxInclusive(maxInclusive, shape.maxInclusive),
                minLength(minLength, shape.minLength),
                maxLength(maxLength, shape.maxLength),
                pattern(pattern, shape.pattern),
                in(in, shape.in),
                languageIn(languageIn, shape.languageIn),

                uniqueLang(uniqueLang, shape.uniqueLang),
                minCount(minCount, shape.minCount),
                maxCount(maxCount, shape.maxCount),
                hasValue(hasValue, shape.hasValue),

                constraints(constraints, shape.constraints),
                properties(properties, shape.properties)

        );

    }

    public Shape merge(final Shape shape) {

        if ( shape == null ) {
            throw new NullPointerException("null shape");
        }

        return new Shape(

                virtual(virtual, shape.virtual),
                id(id, shape.id),
                type(type, shape.type),

                datatype(datatype, shape.datatype),
                clazz(clazz, shape.clazz),
                clazzes(clazzes, shape.clazzes),
                minExclusive(minExclusive, shape.minExclusive),
                maxExclusive(maxExclusive, shape.maxExclusive),
                minInclusive(minInclusive, shape.minInclusive),
                maxInclusive(maxInclusive, shape.maxInclusive),
                minLength(minLength, shape.minLength),
                maxLength(maxLength, shape.maxLength),
                pattern(pattern, shape.pattern),
                in(in, shape.in),
                languageIn(languageIn, shape.languageIn),

                uniqueLang(uniqueLang, shape.uniqueLang),
                minCount(minCount, shape.minCount),
                maxCount(maxCount, shape.maxCount),
                hasValue(hasValue, shape.hasValue),

                constraints(constraints, shape.constraints),
                properties(properties, shape.properties)

        );
    }


    private boolean virtual(final boolean x, final boolean y) {
        return x || y;
    }

    private Optional<String> id(final Optional<String> x, final Optional<String> y) {
        return merge(x, y, (v, w) -> v.equals(w) ? v : error(
                new IllegalArgumentException(format("conflicting id property names <%s> / <%s>", v, w))
        ));
    }

    private Optional<String> type(final Optional<String> x, final Optional<String> y) {
        return merge(x, y, (v, w) -> v.equals(w) ? v : error(
                new IllegalArgumentException(format("conflicting type property names <%s> / <%s>", v, w))
        ));
    }


    private Optional<Value> datatype(final Optional<Value> x, final Optional<Value> y) {
        return merge(x, y, (value, w) -> value.equals(w) ? value : error(
                new IllegalArgumentException(format("conflicting datatypes <%s> / <%s>", value, w))
        ));
    }

    private Optional<Type> clazz(final Optional<Type> x, final Optional<Type> y) {
        return merge(x, y, (v, w) -> v.equals(w) ? v : error(new IllegalArgumentException(format(
                "conflicting explicit classes <%s> / <%s>", x, y
        ))));
    }

    private Optional<Set<Type>> clazzes(final Optional<Set<Type>> x, final Optional<Set<Type>> y) {
        return merge(x, y, (v, w) -> set(Stream.concat(v.stream(), w.stream())));
    }

    private Optional<Value> minExclusive(final Optional<Value> x, final Optional<Value> y) {
        return merge(x, y, (v, w) -> compare(v, w) >= 0 ? v : error(
                new IllegalArgumentException(format("conflicting minExclusive limits <%s> / <%s>", v, w))
        ));
    }

    private Optional<Value> maxExclusive(final Optional<Value> x, final Optional<Value> y) {
        return merge(x, y, (v, w) -> compare(v, w) <= 0 ? v : error(
                new IllegalArgumentException(format("conflicting maxExclusive limits <%s> / <%s>", v, w))
        ));
    }

    private Optional<Value> minInclusive(final Optional<Value> x, final Optional<Value> y) {
        return merge(x, y, (v, w) -> compare(v, w) >= 0 ? v : error(
                new IllegalArgumentException(format("conflicting minInclusive limits <%s> / <%s>", v, w))
        ));
    }

    private Optional<Value> maxInclusive(final Optional<Value> x, final Optional<Value> y) {
        return merge(x, y, (v, w) -> compare(v, w) <= 0 ? v : error(
                new IllegalArgumentException(format("conflicting maxInclusive limits <%s> / <%s>", v, w))
        ));
    }

    private Optional<Integer> minLength(final Optional<Integer> x, final Optional<Integer> y) {
        return merge(x, y, (v, w) -> v.compareTo(w) >= 0 ? v : error(
                new IllegalArgumentException(format("conflicting minLength limits <%s> / <%s>", v, w))
        ));
    }

    private Optional<Integer> maxLength(final Optional<Integer> x, final Optional<Integer> y) {
        return merge(x, y, (v, w) -> v.compareTo(w) <= 0 ? v : error(
                new IllegalArgumentException(format("conflicting maxLength limits <%s> / <%s>", v, w))
        ));
    }

    private Optional<String> pattern(final Optional<String> x, final Optional<String> y) {
        return merge(x, y, (v, w) -> v.equals(w) ? v : error(
                new IllegalArgumentException(format("conflicting patterns <%s> / <%s>", v, w))
        ));
    }

    private Optional<Set<Value>> in(final Optional<Set<Value>> x, final Optional<Set<Value>> y) {
        return merge(x, y, (v, w) -> set(Stream.of(v, w).flatMap(Collection::stream)));
    }

    private Optional<Set<Locale>> languageIn(final Optional<Set<Locale>> x, final Optional<Set<Locale>> y) {
        return merge(x, y, (v, w) -> set(Stream.of(v, w).flatMap(Collection::stream)));
    }

    private boolean uniqueLang(final boolean x, final boolean y) {
        return x || y;
    }

    private Optional<Integer> minCount(final Optional<Integer> x, final Optional<Integer> y) {
        return merge(x, y, (v, w) -> v.compareTo(w) >= 0 ? v : error(
                new IllegalArgumentException(format("conflicting minCount limits <%s> / <%s>", v, w))
        ));
    }

    private Optional<Integer> maxCount(final Optional<Integer> x, final Optional<Integer> y) {
        return merge(x, y, (v, w) -> v.compareTo(w) <= 0 ? v : error(
                new IllegalArgumentException(format("conflicting maxCount limits <%s> / <%s>", v, w))
        ));
    }

    private Optional<Set<Value>> hasValue(final Optional<Set<Value>> x, final Optional<Set<Value>> y) {
        return merge(x, y, (v, w) -> set(Stream.of(v, w).flatMap(Collection::stream)));
    }

    private Optional<Set<Function<Value, Value>>> constraints(
            final Optional<Set<Function<Value, Value>>> x, final Optional<Set<Function<Value, Value>>> y
    ) {
        return merge(x, y, (v, w) -> set(Stream.of(v, w).flatMap(Collection::stream)));
    }

    private Collection<Property> properties(final Collection<Property> x, final Collection<Property> y) {
        return merge(x, y, (v, w) -> Stream.of(v, w)
                .flatMap(Collection::stream)
                .collect(toMap(
                        p -> p.shape(Shape::shape), // group ignoring shape generator
                        identity(),
                        (p, q) -> p.shape(() -> // lazy extend to handle recursive shapes
                                p.shape().extend(q.shape())
                        )
                ))
                .values()
        );
    }


    private <T extends Collection<?>> T merge(final T x, final T y, final BinaryOperator<T> merge) {
        return merge.apply(x, y);
    }

    private <T> Optional<T> merge(final Optional<T> x, final Optional<T> y, final BinaryOperator<T> merge) {
        return x.map(v -> y.map(w -> merge.apply(v, w)).orElse(v)).or(() -> y);
    }

}
