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

package com.metreeca.mesh.rdf4j;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.base.CoreDatatype.RDF;
import org.eclipse.rdf4j.model.base.CoreDatatype.XSD;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URI;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.temporal.Temporal;
import java.time.temporal.TemporalAmount;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Stream;

import static com.metreeca.mesh.Value.*;
import static com.metreeca.shim.Exceptions.unsupported;
import static com.metreeca.shim.Locales.locale;

import static java.util.Locale.ROOT;
import static java.util.function.Predicate.not;
import static java.util.stream.Collectors.toSet;
import static org.eclipse.rdf4j.model.util.Values.iri;
import static org.eclipse.rdf4j.model.util.Values.literal;

/**
 * Bidirectional converter between Mesh Values and RDF4J Values.
 *
 * <p>Provides conversion utilities for translating between Metreeca/Mesh
 * {@linkplain com.metreeca.mesh.Value Value} objects and Eclipse RDF4J {@linkplain Value} objects, enabling seamless
 * integration between the semantic frameworks.</p>
 *
 * <p>Supports conversion of all core datatypes including primitives, temporal
 * types, URIs, literals with language tags, and complex nested structures. Handles proper RDF datatype mapping and
 * language tag preservation.</p>
 */
final class SPARQLConverter {

    static IRI rdf(final URI uri) {
        return iri(uri.toString());
    }

    static Stream<Value> rdf(final com.metreeca.mesh.Value value) {
        return value.accept(new com.metreeca.mesh.Value.Visitor<>() {

            @Override public Stream<Value> visit(final com.metreeca.mesh.Value host, final Void nil) {
                return Stream.empty();
            }

            @Override public Stream<Value> visit(final com.metreeca.mesh.Value host, final Boolean bit) {
                return Stream.of(literal(bit));
            }

            @Override public Stream<Value> visit(final com.metreeca.mesh.Value host, final Long integral) {
                return Stream.of(literal(integral));
            }

            @Override public Stream<Value> visit(final com.metreeca.mesh.Value host, final Double floating) {
                return Stream.of(literal(floating));
            }

            @Override public Stream<Value> visit(final com.metreeca.mesh.Value host, final BigInteger integer) {
                return Stream.of(literal(integer));
            }

            @Override public Stream<Value> visit(final com.metreeca.mesh.Value host, final BigDecimal decimal) {
                return Stream.of(literal(decimal));
            }

            @Override public Stream<Value> visit(final com.metreeca.mesh.Value host, final String string) {
                return Stream.of(literal(string));
            }

            @Override public Stream<Value> visit(final com.metreeca.mesh.Value host, final URI uri) {
                return Stream.of(literal(uri.toString(), XSD.ANYURI));
            }

            @Override public Stream<Value> visit(final com.metreeca.mesh.Value host, final Temporal temporal) {
                return Stream.of(literal(temporal));
            }

            @Override public Stream<Value> visit(final com.metreeca.mesh.Value host, final ZonedDateTime zonedDateTime) {
                return visit(host, zonedDateTime.toOffsetDateTime()); // ;( zone labels not supported by ISO 8601
            }

            @Override public Stream<Value> visit(final com.metreeca.mesh.Value host, final Instant instant) {
                return Stream.of(literal(instant.atOffset(ZoneOffset.UTC)));
            }

            @Override public Stream<Value> visit(final com.metreeca.mesh.Value host, final TemporalAmount amount) {
                return Stream.of(literal(amount));
            }

            @Override public Stream<Value> visit(final com.metreeca.mesh.Value host, final Locale locale, final String string) {
                return Stream.of(locale.equals(ROOT) ? literal(string) : literal(string, locale(locale)));
            }

            @Override public Stream<Value> visit(final com.metreeca.mesh.Value host, final URI datatype, final String string) {
                return Stream.of(literal(string, rdf(datatype)));
            }

            @Override public Stream<Value> visit(final com.metreeca.mesh.Value host, final Map<String, com.metreeca.mesh.Value> fields) {
                return host.id().map(id -> (Value)rdf(id)).stream();
            }

            @Override public Stream<Value> visit(final com.metreeca.mesh.Value host, final List<com.metreeca.mesh.Value> values) {
                return values.stream().flatMap(v -> v.accept(this));
            }

            @Override public Stream<Value> visit(final com.metreeca.mesh.Value host, final Object object) {
                return unsupported(host);
            }

        });
    }


    //̸/////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    static com.metreeca.mesh.Value json(final Collection<Value> values) {
        return array(values.stream()
                .map(SPARQLConverter::json)
                .filter(not(com.metreeca.mesh.Value::isEmpty))
                .collect(toSet())
        );
    }

    static com.metreeca.mesh.Value json(final Value value) {
        return value instanceof final IRI iri ? json(iri)
                : value instanceof final Literal literal ? json(literal)
                : Nil();
    }

    private static com.metreeca.mesh.Value json(final IRI iri) {
        return object(id(URI.create(iri.stringValue())));
    }

    private static com.metreeca.mesh.Value json(final Literal literal) {
        return literal.getLanguage()

                .map(locale -> text(locale(locale), literal.stringValue()))

                .orElseGet(() -> switch ( literal.getCoreDatatype() ) {

                    case XSD.BOOLEAN -> bit(literal.booleanValue());

                    case XSD.DECIMAL -> decimal(literal.decimalValue());
                    case XSD.INTEGER -> integer(literal.integerValue());
                    case XSD.DOUBLE -> floating(literal.doubleValue());
                    case XSD.FLOAT -> floating(literal.doubleValue());
                    case XSD.LONG -> integral(literal.longValue());
                    case XSD.INT -> integral(literal.longValue());

                    case XSD.STRING -> string(literal.stringValue());
                    case XSD.ANYURI -> uri(URI.create(literal.stringValue()));

                    case XSD.GYEAR -> temporal(literal.temporalAccessorValue());
                    case XSD.GYEARMONTH -> temporal(literal.temporalAccessorValue());
                    case XSD.DATE -> temporal(literal.temporalAccessorValue());
                    case XSD.TIME -> temporal(literal.temporalAccessorValue());
                    case XSD.DATETIME -> temporal(literal.temporalAccessorValue());

                    case XSD.DURATION -> temporalAmount(literal.temporalAmountValue());

                    case RDF.LANGSTRING -> literal.getLanguage()
                            .map(locale -> text(locale(locale), literal.stringValue()))
                            .orElseGet(() -> string(literal.stringValue()));

                    default -> data(
                            URI.create(literal.getDatatype().stringValue()), literal.stringValue()
                    );

                });
    }


    //̸/////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private SPARQLConverter() { }

}
