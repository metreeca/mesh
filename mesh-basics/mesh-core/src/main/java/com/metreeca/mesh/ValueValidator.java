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

import com.metreeca.mesh.shapes.Shape;
import com.metreeca.mesh.shapes.Type;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URI;
import java.time.*;
import java.time.temporal.Temporal;
import java.time.temporal.TemporalAmount;
import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Stream;

import static com.metreeca.mesh.Value.*;
import static com.metreeca.mesh.ValueComparator.comparable;
import static com.metreeca.mesh.ValueComparator.compare;
import static com.metreeca.shim.Collections.list;
import static com.metreeca.shim.Collections.set;
import static com.metreeca.shim.Exceptions.unsupported;
import static com.metreeca.shim.Locales.locale;

import static java.lang.String.format;
import static java.util.Map.entry;
import static java.util.function.Predicate.not;
import static java.util.regex.Pattern.compile;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toSet;

/**
 * SHACL-based value validation utilities.
 *
 * <p>Provides comprehensive validation of {@linkplain Value} instances against {@linkplain Shape} constraints
 * using SHACL (Shapes Constraint Language) semantics. Supports validation of datatypes, cardinality, value constraints,
 * property shapes, and custom constraints.</p>
 */
@SuppressWarnings("NonBooleanMethodNameMayNotStartWithQuestion")
final class ValueValidator {

    private static Optional<Value> optional(final Value value) { return optional(Optional.of(value)); }

    private static Optional<Value> optional(final Optional<Value> value) {
        return value.map(ValueValidator::prune).filter(not(Value::isEmpty));
    }


    private static Value prune(final Value value) {
        return value.accept(new Visitor<>() {

            @Override public Value visit(final Value host, final String string) {
                return string.isBlank() ? Nil() : host;
            }

            @Override public Value visit(final Value host, final Map<String, Value> fields) {

                final Value object=object(fields.entrySet().stream()
                        .map(e -> entry(e.getKey(), e.getValue().accept(this)))
                        .filter(not(e -> e.getValue().isEmpty()))
                );

                return object.isEmpty() ? Nil() : object;
            }

            @Override public Value visit(final Value host, final List<Value> values) {

                final Value array=array(values.stream()
                        .map(v -> v.accept(this))
                        .filter(not(Value::isEmpty))
                );

                return array.isEmpty() ? Nil() : array;
            }

            @Override public Value visit(final Value host, final Object object) {
                return host;
            }

        });
    }


    //̸// !!! Review ///////////////////////////////////////////////////////////////////////////////////////////////////

    private static Stream<Value> flatten(final Value value) {
        return value.accept(new Visitor<>() {

            @Override public Stream<Value> visit(final Value host, final List<Value> values) {
                return values.stream().flatMap(v -> v.accept(this));
            }

            @Override public Stream<Value> visit(final Value host, final Object object) {
                return Stream.of(host);
            }

        });
    }


    //̸/////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private final Shape shape;
    private final boolean delta;


    /**
     * Creates a new value validator.
     *
     * @param shape the shape to validate against
     * @param delta {@code true} if validation is for delta updates; {@code false} otherwise
     *
     * @throws NullPointerException if {@code shape} is {@code null}
     */
    ValueValidator(final Shape shape, final boolean delta) {
        this.shape=shape;
        this.delta=delta;
    }


    /**
     * Validates a value against this validator's shape.
     *
     * @param value      the value to validate
     * @param properties {@code true} if property validation should be performed; {@code false} otherwise
     *
     * @return validation errors if any; empty otherwise
     *
     * @throws NullPointerException if {@code value} is {@code null}
     */
    Optional<Value> validate(final Value value, final boolean properties) {
        return optional(array(Stream

                .of(

                        datatype(value),
                        clazz(value),

                        minExclusive(value),
                        maxExclusive(value),
                        minInclusive(value),
                        maxInclusive(value),

                        minLength(value),
                        maxLength(value),
                        pattern(value),

                        in(value),
                        languageIn(value),

                        uniqueLang(value),
                        minCount(value),
                        maxCount(value),
                        hasValue(value),

                        constraints(value),

                        properties ? closed(value) : Optional.<Value>empty(),
                        properties ? properties(value) : Optional.<Value>empty()

                )

                .flatMap(Optional::stream)
                .toList()

        ));
    }


    //̸/////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private Optional<Value> datatype(final Value value) {
        return optional(shape.datatype().map(datatype -> value.accept(new ScalarVisitor() {

            @Override public Value visit(final Value host, final Void nil) {
                return null; // ignore null values
            }

            @Override public Value visit(final Value host, final Boolean bit) {
                return expect(Bit());
            }

            @Override public Value visit(final Value host, final Number number) {
                return expect(Number());
            }

            @Override public Value visit(final Value host, final Long integral) {
                return expect(Integral());
            }

            @Override public Value visit(final Value host, final Double floating) {
                return expect(Floating());
            }

            @Override public Value visit(final Value host, final BigInteger integer) {
                return expect(Integer());
            }

            @Override public Value visit(final Value host, final BigDecimal decimal) {
                return expect(Decimal());
            }

            @Override public Value visit(final Value host, final String string) {
                return expect(String());
            }

            @Override public Value visit(final Value host, final URI uri) {
                return expect(URI());
            }

            @Override public Value visit(final Value host, final Temporal temporal) {
                return expect(Temporal());
            }

            @Override public Value visit(final Value host, final Year year) {
                return expect(Year());
            }

            @Override public Value visit(final Value host, final YearMonth yearMonth) {
                return expect(YearMonth());
            }

            @Override public Value visit(final Value host, final LocalDate localDate) {
                return expect(LocalDate());
            }

            @Override public Value visit(final Value host, final LocalTime localTime) {
                return expect(LocalTime());
            }

            @Override public Value visit(final Value host, final OffsetTime offsetTime) {
                return expect(OffsetTime());
            }

            @Override public Value visit(final Value host, final LocalDateTime localDateTime) {
                return expect(LocalDateTime());
            }

            @Override public Value visit(final Value host, final OffsetDateTime offsetDateTime) {
                return expect(OffsetDateTime());
            }

            @Override public Value visit(final Value host, final ZonedDateTime zonedDateTime) {
                return expect(ZonedDateTime());
            }

            @Override public Value visit(final Value host, final Instant instant) {
                return expect(Instant());
            }

            @Override public Value visit(final Value host, final TemporalAmount amount) {
                return expect(TemporalAmount());
            }

            @Override public Value visit(final Value host, final Period period) {
                return expect(Period());
            }

            @Override public Value visit(final Value host, final Duration duration) {
                return expect(Duration());
            }

            @Override public Value visit(final Value host, final Locale locale, final String string) {
                return expect(Text());
            }

            @Override public Value visit(final Value host, final URI datatype, final String string) {
                return expect(Data());
            }

            @Override public Value visit(final Value host, final Map<String, Value> fields) {
                return expect(Object());
            }

            @Override public Value visit(final Value host, final Object object) {
                return unsupported(object);
            }


            private Value expect(final Value expected) {
                return datatype.equals(expected) ? Nil() : string(format(
                        "datatype(%s) / unexpected datatype <%s> / <%s>",
                        datatype,
                        expected,
                        value
                ));
            }

        })));
    }

    private Optional<Value> clazz(final Value value) {
        return optional(shape.clazzes().map(types -> value.accept(new ScalarVisitor() {

            @Override public Value visit(final Value host, final Map<String, Value> fields) {

                final Set<URI> expected=types.stream()
                        .map(Type::uri)
                        .collect(toSet());

                final Set<URI> actual=host.shape()
                        .flatMap(Shape::clazzes)
                        .stream()
                        .flatMap(Collection::stream)
                        .map(Type::uri)
                        .collect(toSet());

                return actual.containsAll(expected) ? Nil() : string(format(
                        "clazz(%s) / missing types <%s> / <%s>",
                        types,
                        types.stream().filter(not(t -> actual.contains(t.uri()))).toList(),
                        value
                ));

            }

        })));
    }


    private Optional<Value> minExclusive(final Value value) {
        return optional(shape.minExclusive().map(limit -> value.accept(new ScalarVisitor() {

            @Override public Value visit(final Value host, final Void nil) {
                return Nil();
            }

            @Override public Value visit(final Value host, final Object object) {
                if ( !comparable(host, limit) ) {

                    return string(format(
                            "minExclusive(%s) / incomparable value / <%s>",
                            limit,
                            host
                    ));

                } else if ( !(compare(host, limit) > 0) ) {

                    return string(format(
                            "minExclusive(%s) / less than or equal to limit / <%s>",
                            limit,
                            host
                    ));

                } else {

                    return Nil();

                }
            }

        })));
    }

    private Optional<Value> maxExclusive(final Value value) {
        return optional(shape.maxExclusive().map(limit -> value.accept(new ScalarVisitor() {

            @Override public Value visit(final Value host, final Void nil) {
                return Nil();
            }

            @Override public Value visit(final Value host, final Object object) {
                if ( !comparable(host, limit) ) {

                    return string(format(
                            "maxExclusive(%s) / incomparable value / <%s>",
                            limit,
                            host
                    ));

                } else if ( !(compare(host, limit) < 0) ) {

                    return string(format(
                            "maxExclusive(%s) / greater than or equal to limit / <%s>",
                            limit,
                            host
                    ));

                } else {

                    return Nil();

                }
            }

        })));
    }

    private Optional<Value> minInclusive(final Value value) {
        return optional(shape.minInclusive().map(limit -> value.accept(new ScalarVisitor() {

            @Override public Value visit(final Value host, final Void nil) {
                return Nil();
            }

            @Override public Value visit(final Value host, final Object object) {
                if ( !comparable(host, limit) ) {

                    return string(format(
                            "minInclusive(%s) / incomparable value / <%s>",
                            limit,
                            host
                    ));

                } else if ( !(compare(host, limit) >= 0) ) {

                    return string(format(
                            "minInclusive(%s) / less than limit / <%s>",
                            limit,
                            host
                    ));

                } else {

                    return Nil();

                }
            }

        })));
    }

    private Optional<Value> maxInclusive(final Value value) {
        return optional(shape.maxInclusive().map(limit -> value.accept(new ScalarVisitor() {

            @Override public Value visit(final Value host, final Void nil) {
                return Nil();
            }

            @Override public Value visit(final Value host, final Object object) {
                if ( !comparable(host, limit) ) {

                    return string(format(
                            "maxInclusive(%s) / incomparable value / <%s>",
                            limit,
                            host
                    ));

                } else if ( !(compare(host, limit) <= 0) ) {

                    return string(format(
                            "maxInclusive(%s) / greater than limit / <%s>",
                            limit,
                            host
                    ));

                } else {

                    return Nil();

                }
            }

        })));
    }


    private Optional<Value> minLength(final Value value) {
        return optional(shape.minLength().map(limit -> value.accept(new ScalarVisitor() {

            @Override public Value visit(final Value host, final String string) {
                return string.length() >= limit ? Nil() : string(format(
                        "minLength(%d) / length less than limit / <%s>",
                        limit,
                        host
                ));
            }

            @Override public Value visit(final Value host, final URI uri) {
                return visit(host, host.encode());
            }

            @Override public Value visit(final Value host, final Temporal temporal) {
                return visit(host, host.encode());
            }

            @Override public Value visit(final Value host, final TemporalAmount amount) {
                return visit(host, host.encode());
            }

            @Override public Value visit(final Value host, final Locale locale, final String string) {
                return visit(host, string);
            }

            @Override public Value visit(final Value host, final URI datatype, final String string) {
                return visit(host, string);
            }

        })));
    }

    private Optional<Value> maxLength(final Value value) {
        return optional(shape.maxLength().map(limit -> value.accept(new ScalarVisitor() {

            @Override public Value visit(final Value host, final String string) {
                return string.length() <= limit ? Nil() : string(format(
                        "maxLength(%d) / length greater than limit / <%s>",
                        limit,
                        host
                ));
            }

            @Override public Value visit(final Value host, final URI uri) {
                return visit(host, host.encode());
            }

            @Override public Value visit(final Value host, final Temporal temporal) {
                return visit(host, host.encode());
            }

            @Override public Value visit(final Value host, final TemporalAmount amount) {
                return visit(host, host.encode());
            }

            @Override public Value visit(final Value host, final Locale locale, final String string) {
                return visit(host, string);
            }

            @Override public Value visit(final Value host, final URI datatype, final String string) {
                return visit(host, string);
            }

        })));
    }

    private Optional<Value> pattern(final Value value) {
        return optional(shape.pattern().map(pattern -> value.accept(new ScalarVisitor() {

            @Override public Value visit(final Value host, final Number number) {
                return visit(host, host.encode());
            }

            @Override public Value visit(final Value host, final String string) {
                return compile(pattern).matcher(string).find() ? Nil() : string(format(
                        "pattern(%s) / not matched / <%s>",
                        pattern,
                        host
                ));
            }

            @Override public Value visit(final Value host, final URI uri) {
                return visit(host, host.encode());
            }

            @Override public Value visit(final Value host, final Temporal temporal) {
                return visit(host, host.encode());
            }

            @Override public Value visit(final Value host, final TemporalAmount amount) {
                return visit(host, host.encode());
            }

            @Override public Value visit(final Value host, final Locale locale, final String string) {
                return visit(host, string);
            }

            @Override public Value visit(final Value host, final URI datatype, final String string) {
                return visit(host, string);
            }

        })));
    }


    private Optional<Value> in(final Value value) {
        return optional(shape.in().map(options -> value.accept(new ScalarVisitor() {

            @Override public Value visit(final Value host, final Void nil) {
                return Nil();
            }

            @Override public Value visit(final Value host, final Object object) {
                return options.contains(value) ? Nil() : string(format(
                        "in(%s) / unexpected value / <%s>",
                        options,
                        host
                ));
            }

        })));
    }

    private Optional<Value> languageIn(final Value value) {
        return optional(shape.languageIn().map(locales -> value.accept(new ScalarVisitor() {

            @Override public Value visit(final Value host, final Locale locale, final String string) {
                return locales.contains(locale) ? Nil() : string(format(
                        "languageIn(%s) / unexpected locale <%s> / <%s>",
                        locales,
                        locale,
                        host
                ));
            }

        })));
    }


    private Optional<Value> uniqueLang(final Value value) {
        return optional(Optional.of(shape.uniqueLang())
                .filter(v -> v)
                .map(uniqueLang -> value.accept(new CollectionVisitor() {

                    @Override public Value visit(final Value host, final List<Value> values) {
                        return array(values.stream()
                                .flatMap(v -> v.text().stream())
                                .collect(groupingBy(Entry::getKey))
                                .entrySet()
                                .stream()
                                .filter(group -> group.getValue().size() > 1)
                                .map(group -> string(format(
                                        "uniqueLang() / more than one value for locale <%s> / <%s>",
                                        locale(group.getKey()),
                                        group.getValue().size()
                                )))
                                .toList()
                        );
                    }

                }))
        );
    }

    private Optional<Value> minCount(final Value value) {
        return optional(shape.minCount().map(limit -> value.accept(new CollectionVisitor() {

            @Override public Value visit(final Value host, final List<Value> values) {
                return delta && values.isEmpty() || values.size() >= limit ? Nil() : string(format(
                        "minCount(%d) / value set size less than limit / <{%d}>",
                        limit,
                        values.size()
                ));
            }

        })));
    }

    private Optional<Value> maxCount(final Value value) {
        return optional(shape.maxCount().map(limit -> value.accept(new CollectionVisitor() {

            @Override public Value visit(final Value host, final List<Value> values) {
                return values.size() <= limit ? Nil() : string(format(
                        "maxCount(%d) / value set size greater than limit / <{%d}>",
                        limit,
                        values.size()
                ));
            }

        })));
    }

    private Optional<Value> hasValue(final Value value) {
        return optional(shape.hasValue().map(set -> value.accept(new CollectionVisitor() {
            @Override public Value visit(final Value host, final List<Value> values) {
                return set(values).containsAll(set) ? Nil() : string(format(
                        "hasValue(%s) / missing values <%s>",
                        set,
                        value
                ));
            }
        })));
    }


    private Optional<Value> constraints(final Value value) {
        return optional(shape.constraints().map(constraints -> array(constraints.stream()
                .map(constraint -> value.accept(new ScalarVisitor() {

                    @Override public Value visit(final Value host, final Object object) {
                        return constraint.apply(host);
                    }

                }))
                .filter(not(Value::isEmpty))
                .toList()
        )));
    }


    private Optional<Value> closed(final Value value) {
        return optional(value.accept(new ScalarVisitor() {

            @Override public Value visit(final Value host, final Map<String, Value> fields) {

                return object(fields.keySet().stream()
                        .filter(not(Value::isReserved))
                        .filter(name -> shape.property(name).isEmpty())
                        .map(name -> entry(name, string("unexpected property")))
                        .toList()
                );

            }

        }));
    }

    private Optional<Value> properties(final Value value) {
        return optional(value.accept(new ScalarVisitor() {

            @Override public Value visit(final Value host, final Map<String, Value> fields) {

                final List<Entry<String, Value>> traces=shape.properties().stream()
                        .map(property -> {

                            final String name=property.name();
                            final ValueValidator validator=new ValueValidator(property.shape(), delta);

                            return Optional.of(fields.getOrDefault(name, Nil()))
                                    .map(v -> array(validator.validate(v, property.embedded()).stream()
                                            .flatMap(ValueValidator::flatten)
                                    ))
                                    .filter(not(Value::isEmpty))
                                    .map(trace -> entry(name, trace));

                        })
                        .flatMap(Optional::stream)
                        .toList();

                return traces.isEmpty() ? Nil() : object(Stream.concat(
                        host.id().map(Value::id).stream(),
                        traces.stream()
                ).toList());
            }

        }));
    }


    //̸/////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private abstract static class ScalarVisitor extends Visitor<Value> {

        @Override public Value visit(final Value host, final List<Value> values) {
            return array(values.stream()
                    .flatMap(v -> optional(v.accept(this)).stream())
                    .toList()
            );
        }

        @Override public Value visit(final Value host, final Object object) {
            return Nil();
        }

    }

    private abstract static class CollectionVisitor extends Visitor<Value> {

        @Override public Value visit(final Value host, final Void nil) {
            return visit(host, list());
        }

        @Override public Value visit(final Value host, final Object object) {
            return visit(host, list(host));
        }

    }

}
