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

package com.metreeca.mesh.tools;

import com.metreeca.mesh.Value;
import com.metreeca.mesh.queries.Criterion;
import com.metreeca.mesh.queries.Query;
import com.metreeca.mesh.queries.Specs;
import com.metreeca.mesh.shapes.Property;
import com.metreeca.mesh.shapes.Shape;
import com.metreeca.shim.Collections;

import java.util.*;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static com.metreeca.mesh.Value.*;
import static com.metreeca.mesh.queries.Query.query;
import static com.metreeca.shim.Collections.entry;
import static com.metreeca.shim.Collections.list;
import static com.metreeca.shim.Locales.ANY;
import static com.metreeca.shim.URIs.uri;

import static java.lang.Math.max;
import static java.lang.Math.min;
import static java.util.function.Predicate.not;
import static java.util.stream.Collectors.*;

/**
 * Model transformation utilities for Agent operations.
 *
 * <p>Provides algorithms for expanding and populating data models used in REST operations.</p>
 */
final class AgentModel {

    /**
     * Expands a data model by filling in missing information based on shape specifications.
     *
     * <p>This method traverses a data model structure and enhances it with the following:</p>
     *
     * <ul>
     *   <li>Default shape and ID for objects when missing</li>
     *   <li>Shape-inferred properties with appropriate default values</li>
     *   <li>For object datatypes, creates nested objects with their own shape and ID</li>
     *   <li>For query values, recursively expands the query model</li>
     * </ul>
     *
     * @param model the model to be expanded
     *
     * @return the expanded model with inferred properties and structure
     *
     * @throws NullPointerException if {@code model} is {@code null}
     */
    static Value expand(final Value model) {
        return model.accept(new Visitor<>() {

            @Override public Value visit(final Value host, final List<Value> values) {
                return array(list(values.stream().map(value -> value.accept(this))));
            }

            @Override public Value visit(final Value host, final Map<String, Value> fields) {
                return object(list(Stream.concat(

                        // default shape and id

                        Stream.of(
                                Value.shape(host.shape().orElseGet(Shape::shape)),
                                id(uri())
                        ),

                        // shape-inferred properties

                        host.shape().stream()
                                .flatMap(shape -> shape.properties().stream())
                                .filter(not(Property::hidden))
                                .flatMap(property -> {

                                    final String name=property.name();
                                    final Shape shape=property.shape();

                                    return Optional.ofNullable(fields.get(name))

                                            .map(v -> shape.is(Text()) && v.equals(Array())
                                                    ? array(Text())
                                                    : v.accept(this)
                                            )

                                            .or(() -> model(shape))

                                            .filter(not(v -> v.equals(Nil())))

                                            .map(v -> field(name, v))

                                            .stream();

                                })

                )));
            }

            @Override public Value visit(final Value host, final Object object) {
                return object instanceof final Query query ? value(query.model(query.model().accept(this)))
                        : host;
            }

        });
    }


    //̸/////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Merges two data models with the second model taking precedence.
     *
     * <p>This method combines two data models using type-specific merge strategies:</p>
     * <ul>
     *   <li>For literals: Second value always replaces the first</li>
     *   <li>For arrays: Pairwise merging of elements by position</li>
     *   <li>For objects: Property-by-property merging of common properties</li>
     *   <li>For queries: Merges models, combines criteria, preserves offset from first query,
     *       and uses minimum non-zero limit</li>
     * </ul>
     *
     * <p>Special handling is provided for:</p>
     * <ul>
     *   <li>Empty values (Nil() and Array())</li>
     *   <li>Reserved fields (id, type, shape)</li>
     *   <li>Specs objects (preserved)</li>
     * </ul>
     *
     * @param x the base model
     * @param y the model to merge (takes precedence)
     *
     * @return the merged model
     *
     * @throws NullPointerException if either {@code x} or {@code y} is {@code null}
     */
    static Value populate(final Value x, final Value y) {
        return x.array().flatMap(v ->
                        y.array().map(w -> populate(v, w))
                                .or(() -> y.value(Query.class).map(w -> populate(v, w)))
                )

                .or(() -> x.object().flatMap(v ->
                        y.object().map(w -> populate(v, w))
                                .or(() -> y.value(Query.class).map(w -> populate(x, w)))
                ))

                .or(() -> x.value(Query.class).flatMap(v ->
                        y.value(Query.class).map(w -> populate(v, w))
                                .or(() -> y.array().map(w -> populate(v, w)))
                                .or(() -> y.object().map(w -> populate(v, y)))
                ))

                .orElseGet(() -> x.value(Specs.class).isPresent() ? x : y);
    }


    /**
     * Merges two arrays by pairwise element merging.
     *
     * <p>Combines elements at matching positions, applying appropriate population strategies
     * based on element types. Elements with no matching counterpart are preserved.</p>
     *
     * @param x the base array
     * @param y the array to merge (takes precedence for matching positions)
     *
     * @return the merged array
     */
    private static Value populate(final List<Value> x, final List<Value> y) {
        return array(IntStream.range(0, max(x.size(), y.size()))
                .mapToObj(i -> {

                    final Value v=i < x.size() ? x.get(i) : Nil();
                    final Value w=i < y.size() ? y.get(i) : Nil();

                    return v.equals(Nil()) ? w
                            : w.equals(Nil()) ? v
                            : populate(v, w);

                })
                .toList()
        );
    }

    /**
     * Populates a query model using the first element of an array.
     *
     * @param x the source array
     * @param w the query to populate
     *
     * @return the populated query value
     */
    private static Value populate(final List<Value> x, final Query w) {
        return x.stream().findFirst()
                .map(v -> populate(v, w))
                .orElseGet(() -> value(w));
    }


    /**
     * Merges two objects by property-by-property merging.
     *
     * <p>Performs property intersection, keeping only properties present in both objects
     * and recursively populating their values.</p>
     *
     * @param x the base object
     * @param y the object to merge (takes precedence)
     *
     * @return the merged object
     */
    private static Value populate(final Map<String, Value> x, final Map<String, Value> y) {
        return object(list(x.entrySet().stream()

                .flatMap(e -> Optional.ofNullable(y.get(e.getKey()))

                        .or(() -> shape(y, e.getKey())
                                .flatMap(AgentModel::model)
                        )

                        .map(w -> field(e.getKey(), shape(y, e.getKey())
                                .filter(s -> s.is(Text()) && s.isMultiple())
                                .isPresent() ?

                                text(e.getValue(), w)

                                : shape(y, e.getKey())
                                .filter(s -> s.is(Data()) && s.isMultiple())
                                .isPresent() ?

                                data(e.getValue(), w)

                                : populate(e.getValue(), w))
                        )

                        .stream()
                )

        ));
    }


    /**
     * Merges textual values from two sources.
     *
     * @param x the base Value containing text entries
     * @param y the Value to merge (takes precedence for matching locales)
     *
     * @return a merged array of text values
     */
    private static Value text(final Value x, final Value y) {
        return text(x.array().orElseGet(Collections::list), y.array().orElseGet(Collections::list));
    }

    /**
     * Merges text values from two arrays applying locale-specific merging rules.
     *
     * <p>This method implements sophisticated text merging with locale handling:</p>
     * <ul>
     *   <li>Exact locale matches from y override corresponding entries from x</li>
     *   <li>Wildcard locale entries in y can match any locale in x</li>
     *   <li>Wildcard locale entries in x match all entries in y</li>
     * </ul>
     *
     * @param x the base list of Values containing text entries
     * @param y the list to merge (takes precedence for matching locales)
     *
     * @return a merged array of text values
     */
    private static Value text(final List<Value> x, final List<Value> y) {
        return array(list(x.stream()
                .flatMap(v -> v.text().stream())
                .flatMap(v ->

                        y.stream() // matching locale from model
                                .flatMap(w -> w.text().stream())
                                .filter(w -> w.getKey().equals(v.getKey()))
                                .map(Stream::of)
                                .findFirst()

                                .or(() -> y.stream() // matching value from model wildcard entry
                                        .flatMap(w -> w.text().stream())
                                        .filter(w -> w.getKey().equals(ANY))
                                        .map(w -> entry(v.getKey(), w.getValue()))
                                        .map(Stream::of)
                                        .findFirst()
                                )

                                .orElseGet(() -> v.getKey().equals(ANY)
                                        ? y.stream().flatMap(w -> w.text().stream()) // all model entries
                                        : Stream.of(v) // provided entry
                                )
                )
                .map(Value::text)
        ));
    }


    /**
     * Merges binary data values from two sources.
     *
     * @param x the base Value containing data entries
     * @param y the Value to merge (takes precedence for matching keys)
     *
     * @return a merged array of data values
     */
    private static Value data(final Value x, final Value y) {
        return data(x.array().orElseGet(Collections::list), y.array().orElseGet(Collections::list));
    }

    /**
     * Merges binary data values from two arrays by key.
     *
     * <p>This method merges data entries where matching entries from y override
     * corresponding entries from x by key.</p>
     *
     * @param x the base list of Values containing data entries
     * @param y the list to merge (takes precedence for matching keys)
     *
     * @return a merged array of data values
     */
    private static Value data(final List<Value> x, final List<Value> y) {
        return array(list(x.stream()
                .flatMap(v -> v.data().stream())
                .map(v ->

                        y.stream() // matching locale from model
                                .flatMap(w -> w.data().stream())
                                .filter(w -> w.getKey().equals(v.getKey()))
                                .findFirst()

                                .orElse(v) // provided value
                )
                .map(Value::data)
        ));
    }


    /**
     * Merges two queries with specialized handling for query structures.
     *
     * <p>This method:</p>
     * <ul>
     *   <li>Populates the query models</li>
     *   <li>Merges criteria using the Criterion.merge operation</li>
     *   <li>Preserves offset from the first query</li>
     *   <li>Takes minimum limit between both queries (unless one is zero)</li>
     * </ul>
     *
     * @param x the base query
     * @param y the query to merge (takes precedence)
     *
     * @return the merged query value
     */
    private static Value populate(final Query x, final Query y) {
        return value(new Query(

                populate(x.model(), y.model()),

                Stream.of(x.criteria().entrySet(), y.criteria().entrySet())
                        .flatMap(Collection::stream)
                        .collect(groupingBy(Map.Entry::getKey, LinkedHashMap::new, mapping(Map.Entry::getValue,
                                reducing(Criterion.criterion(), Criterion::merge)
                        ))),

                x.offset(),
                x.limit() == 0 ? y.limit() : y.limit() == 0 ? x.limit() : min(x.limit(), y.limit())

        ));
    }

    /**
     * Populates a query using the first element of an array.
     *
     * @param x the query to populate
     * @param y the source array
     *
     * @return the populated query value
     */
    private static Value populate(final Query x, final List<Value> y) {
        return y.stream().findFirst()
                .map(w -> populate(x, w))
                .orElseGet(() -> value(x));
    }

    /**
     * Populates a query with a generic value by converting the value to a query.
     *
     * @param x the query to populate
     * @param y the value to merge
     *
     * @return the populated query value
     */
    private static Value populate(final Query x, final Value y) {
        return populate(x, query(y));
    }

    /**
     * Populates a query with a generic value by converting the value to a query.
     *
     * @param x the value to merge
     * @param y the query to populate
     *
     * @return the populated query value
     */
    private static Value populate(final Value x, final Query y) {
        return populate(query(x), y);
    }


    //̸/////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Retrieves the shape for a property in a field map.
     *
     * @param fields the field map containing the context with shape information
     * @param name   the name of the property to retrieve the shape for
     *
     * @return an Optional containing the shape of the named property, or empty if not found
     */
    private static Optional<Shape> shape(final Map<String, Value> fields, final String name) {
        return Optional.ofNullable(fields.get(CONTEXT))
                .flatMap(v -> v.value(Shape.class))
                .flatMap(s -> s.property(name))
                .map(Property::shape);
    }

    /**
     * Creates a model value from a shape definition.
     *
     * @param shape the shape to create a model value for
     *
     * @return an Optional containing the generated model value, or empty if the shape has no datatype
     */
    private static Optional<Value> model(final Shape shape) {
        return shape.datatype()

                .map(datatype -> datatype.equals(Object())
                        ? object(Value.shape(shape), id(uri()))
                        : datatype
                )

                .map(value -> shape.isMultiple()
                        ? array(value)
                        : value
                );
    }


    //̸/////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private AgentModel() { }

}
