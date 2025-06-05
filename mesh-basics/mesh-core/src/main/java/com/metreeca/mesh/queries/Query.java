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

package com.metreeca.mesh.queries;

import com.metreeca.mesh.Valuable;
import com.metreeca.mesh.Value;
import com.metreeca.mesh.shapes.Shape;
import com.metreeca.mesh.tools.CodecException;

import java.net.URLDecoder;
import java.util.*;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static com.metreeca.mesh.Value.*;
import static com.metreeca.mesh.queries.Expression.expression;
import static com.metreeca.shim.Collections.*;
import static com.metreeca.shim.Exceptions.error;

import static java.lang.Integer.parseInt;
import static java.lang.String.format;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Locale.ROOT;
import static java.util.Objects.requireNonNull;
import static java.util.function.Predicate.not;
import static java.util.regex.Pattern.compile;
import static java.util.stream.Collectors.toMap;

/**
 * Collection query.
 *
 * <p>A structured representation of search criteria for filtering, sorting, retrieving, and paginating collections of
 * items.</p>
 *
 * <p>Query instances are immutable and support fluent configuration through functional setters.</p>
 *
 * @param model    the value acting as the query model
 * @param criteria the map of expression/criterion pairs defining query filters and sort order
 * @param offset   the zero-based starting index for paginated results
 * @param limit    the maximum number of items to be returned (0 for unlimited)
 */
public final record Query(

        Value model,
        Map<Expression, Criterion> criteria,

        int offset,
        int limit

) {

    private static final Pattern PAIR_PATTERN=compile("&?(?<label>[^=&]*)(?:=(?<value>[^&]*))?");

    private static final Query EMPTY=new Query(

            Object(),
            map(),

            0,
            0

    );


    //̸/////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Creates an empty query.
     *
     * @return an empty query with default parameters
     */
    public static Query query() {
        return EMPTY;
    }

    /**
     * Creates a query with the specified model.
     *
     * @param model the value to be used as query model
     *
     * @return a new query with the specified model
     *
     * @throws NullPointerException if {@code model} is {@code null}
     */
    public static Query query(final Valuable model) {

        if ( model == null ) {
            throw new NullPointerException("null model");
        }

        return query().model(model);
    }

    /**
     * Creates a query with tabular specifications.
     *
     * @param shape  the shape defining the data structure
     * @param probes the computed value definitions for the query
     *
     * @return a new query with the specified shape and probes
     *
     * @throws NullPointerException if {@code shape} or {@code probes} is {@code null} or contains {@code null}
     *                              elements
     */
    public static Query query(final Shape shape, final Probe... probes) {

        if ( shape == null ) {
            throw new NullPointerException("null shape");
        }

        if ( probes == null || Arrays.stream(probes).anyMatch(Objects::isNull) ) {
            throw new NullPointerException("null probes");
        }

        return query().model(value(new Specs(shape, list(probes))));
    }

    /**
     * Parses a query string into a structured query using the specified shape for context.
     *
     * <p>The query string format supports various operators:</p>
     * <ul>
     *   <li>{@code field=value} - equality filter</li>
     *   <li>{@code field<=value} - less than or equal filter</li>
     *   <li>{@code field>=value} - greater than or equal filter</li>
     *   <li>{@code ~field=value} - pattern matching filter</li>
     *   <li>{@code ^field=value} - sort order (increasing, decreasing, or numeric)</li>
     *   <li>{@code @=value} - result offset</li>
     *   <li>{@code #=value} - result limit</li>
     * </ul>
     *
     * @param query the URL-encoded query string to be parsed
     * @param shape the shape providing context for expression resolution and value decoding
     *
     * @return a structured query representing the parsed query string
     *
     * @throws NullPointerException if either {@code query} or {@code shape} is {@code null}
     * @throws CodecException       if the query string is malformed
     */
    public static Query query(final String query, final Shape shape) {

        if ( query == null ) {
            throw new NullPointerException("null query");
        }

        if ( shape == null ) {
            throw new NullPointerException("null shape");
        }

        final Map<Expression, Criterion> criteria=new HashMap<>();
        final Map<Expression, Set<Value>> options=new HashMap<>();

        int offset=0;
        int limit=0;

        for (
                final Matcher matcher=PAIR_PATTERN.matcher(query);
                matcher.lookingAt() && matcher.start() < query.length();
                matcher.region(matcher.end(), query.length())
        ) {

            final String label=URLDecoder.decode(matcher.group("label"), UTF_8);
            final String value=URLDecoder.decode(Optional.ofNullable(matcher.group("value")).orElse(""), UTF_8);

            try {

                if ( label.endsWith("<") ) {

                    final Expression expression=expression(label.substring(0, label.length()-1));
                    final Value datatype=expression.apply(shape).datatype().orElseGet(Value::String);

                    criteria.compute(expression, (key, criterion) -> (criterion == null
                            ? Criterion.criterion()
                            : criterion).lte(datatype.decode(value).orElseThrow(() -> malformed(datatype, value)))
                    );

                } else if ( label.endsWith(">") ) {

                    final Expression expression=expression(label.substring(0, label.length()-1));
                    final Value datatype=expression.apply(shape).datatype().orElseGet(Value::String);

                    criteria.compute(expression, (key, criterion) -> (criterion == null
                            ? Criterion.criterion()
                            : criterion).gte(datatype.decode(value).orElseThrow(() -> malformed(datatype, value)))
                    );

                } else if ( label.startsWith("~") ) {

                    final Expression expression=expression(label.substring(1));

                    criteria.compute(expression, (key, criterion) ->
                            (criterion == null ? Criterion.criterion() : criterion).like(value)
                    );

                } else if ( label.startsWith("^") ) {

                    final Expression expression=expression(label.substring(1));

                    final int order=switch ( value.toLowerCase(ROOT) ) {
                        case "increasing", "" -> +1;
                        case "decreasing" -> -1;
                        default -> parseInt(value);
                    };

                    criteria.compute(expression, (key, criterion) ->
                            (criterion == null ? Criterion.criterion() : criterion).order(order)
                    );

                } else if ( label.equals("@") ) {

                    offset=parseInt(value);

                } else if ( label.equals("#") ) {

                    limit=parseInt(value);

                } else {

                    final Expression expression=expression(label);
                    final Value datatype=expression.apply(shape).datatype().orElseGet(Value::String);

                    options.compute(expression, (key, values) -> {

                        final Set<Value> set=values == null ? new LinkedHashSet<>() : values;

                        if ( !value.equals("*") ) {
                            set.add(value.isBlank() ? Nil() :
                                    datatype.decode(value).orElseThrow(() -> malformed(datatype, value)));
                        }

                        return set;

                    });

                }

            } catch ( final NoSuchElementException e ) {

                throw new NoSuchElementException(format("%s in expression <%s>", e.getMessage(), label));

            } catch ( final NumberFormatException e ) {

                throw malformed(Number(), value);

            }

        }

        options.forEach((expression, values) -> criteria.compute(expression, (key, criterion) ->
                (criterion == null ? Criterion.criterion() : criterion).any(values)
        ));

        return new Query(object(Value.shape(shape)), criteria, offset, limit);
    }


    //̸/////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public Query(

            final Value model,
            final Map<Expression, Criterion> criteria,

            final int offset,
            final int limit

    ) {

        if ( model == null ) {
            throw new NullPointerException("null model");
        }

        if ( criteria == null ) {
            throw new NullPointerException("null criteria");
        }

        if ( offset < 0 ) {
            throw new IllegalArgumentException("negative offset");
        }

        if ( limit < 0 ) {
            throw new IllegalArgumentException("negative limit");
        }


        this.model=model;
        this.criteria=criteria.entrySet().stream()
                .filter(not(e -> e.getValue().isEmpty()))
                .collect(toMap(
                        Entry::getKey, Entry::getValue,
                        (x, y) -> error(new IllegalArgumentException(format(
                                "conflicting criteria <%s>/<%s", x, y // unexpected
                        ))),
                        LinkedHashMap::new
                ));

        final Shape shape=model.value(Specs.class)
                .map(Specs::shape)
                .or(model::shape)
                .orElseGet(Shape::shape);

        criteria.keySet().forEach(expression -> {

            try {
                expression.apply(shape);
            } catch ( final NoSuchElementException e ) {
                throw new NoSuchElementException(format("%s in expression <%s>", e.getMessage(), expression));
            }

        });

        this.offset=offset;
        this.limit=limit;

    }


    /**
     * Configures the query model.
     *
     * @param model the value to be used as query model
     *
     * @return a new query with the specified model
     *
     * @throws NullPointerException if {@code model} is {@code null}
     */
    public Query model(final Valuable model) {

        if ( model == null ) {
            throw new NullPointerException("null model");
        }

        return new Query(

                requireNonNull(model.toValue(), "null supplied model"),
                criteria,

                offset,
                limit

        );
    }


    /**
     * Adds a criterion for the specified expression.
     *
     * @param expression the string representation of the expression to be evaluated
     * @param criterion  the criterion to be used for filtering on the expression
     *
     * @return a new query with the specified criterion added
     *
     * @throws NullPointerException     if either {@code expression} or {@code criterion} is {@code null}
     * @throws IllegalArgumentException if a criterion is already defined for the expression
     */
    public Query where(final String expression, final Criterion criterion) {

        if ( expression == null ) {
            throw new NullPointerException("null expression");
        }

        if ( criterion == null ) {
            throw new NullPointerException("null criterion");
        }

        return where(expression(expression), criterion);
    }

    /**
     * Adds a criterion for the specified expression.
     *
     * @param expression the expression to be evaluated
     * @param criterion  the criterion to be used for filtering on the expression
     *
     * @return a new query with the specified criterion added
     *
     * @throws NullPointerException     if either {@code expression} or {@code criterion} is {@code null}
     * @throws IllegalArgumentException if a criterion is already defined for the expression
     */
    public Query where(final Expression expression, final Criterion criterion) {

        if ( expression == null ) {
            throw new NullPointerException("null expression");
        }

        if ( criterion == null ) {
            throw new NullPointerException("null criterion");
        }

        return new Query(

                model,
                Stream.concat(criteria.entrySet().stream(), Stream.of(entry(expression, criterion))).collect(toMap(
                        Entry::getKey, Entry::getValue,
                        (x, y) -> error(new IllegalArgumentException(format(
                                "expression constraints already defined <%s>", expression
                        ))),
                        LinkedHashMap::new
                )),

                offset,
                limit

        );
    }


    /**
     * Configures the result offset.
     *
     * @param offset the zero-based starting index for paginated results
     *
     * @return a new query with the specified offset
     */
    public Query offset(final int offset) {
        return new Query(

                model,
                criteria,

                offset,
                limit

        );
    }

    /**
     * Configures the result limit.
     *
     * @param limit the maximum number of items to be returned (0 for unlimited)
     *
     * @return a new query with the specified limit
     */
    public Query limit(final int limit) {
        return new Query(

                model,
                criteria,

                offset,
                limit

        );
    }

}
