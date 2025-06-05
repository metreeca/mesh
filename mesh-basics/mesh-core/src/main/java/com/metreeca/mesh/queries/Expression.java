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

import com.metreeca.mesh.Value;
import com.metreeca.mesh.shapes.Property;
import com.metreeca.mesh.shapes.Shape;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.metreeca.shim.Collections.list;
import static com.metreeca.shim.Exceptions.error;

import static java.lang.String.format;
import static java.lang.String.join;
import static java.util.Locale.ROOT;
import static java.util.regex.Pattern.compile;
import static java.util.stream.Collectors.joining;

/**
 * Value expression for accessing nested properties and applying transformations.
 *
 * <p>Represents a computed expression that can traverse property paths and apply transform operations to values.
 * Expressions consist of an optional pipeline of transforms followed by a property path.</p>
 *
 * @param pipe the sequence of transforms to apply to the resolved value
 * @param path the property path to navigate through the data structure
 */
public final record Expression(

        List<Transform> pipe,
        List<String> path

) {

    private static final Pattern TRANSFORM_PATTERN=compile("(?<transform>\\w+):");
    private static final Pattern STEP_PATTERN=compile("\\.?(?<step>(?:[^.:\\\\]|\\\\.)+)");
    private static final Pattern ESCAPE_PATTERN=compile("\\\\(?<escape>.)");


    private static final Expression EMPTY=new Expression(
            list(),
            list()
    );


    //̸/////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Creates an empty expression.
     *
     * @return an empty expression with no transforms or path
     */
    public static Expression expression() {
        return EMPTY;
    }

    /**
     * Parses a string expression into an Expression object.
     *
     * @param expression the string representation of the expression to parse
     *
     * @return the parsed expression
     *
     * @throws NullPointerException     if {@code expression} is {@code null}
     * @throws IllegalArgumentException if the expression string is malformed
     */
    public static Expression expression(final String expression) {

        if ( expression == null ) {
            throw new NullPointerException("null expression");
        }

        final List<Transform> pipe=new ArrayList<>();
        final List<String> path=new ArrayList<>();

        int next=0;

        final int length=expression.length();

        for (
                final Matcher matcher=TRANSFORM_PATTERN.matcher(expression).region(next, length);
                matcher.lookingAt();
                matcher.region(next, length)
        ) {

            final String transform=matcher.group("transform");

            pipe.add(Arrays.stream(Transform.values())
                    .filter(value -> value.name().equalsIgnoreCase(transform))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException(format("unknown transform <%s>", transform)))
            );

            next=matcher.end();
        }

        for (
                final Matcher matcher=STEP_PATTERN.matcher(expression).region(next, length);
                matcher.lookingAt();
                matcher.region(next, length)
        ) {

            final String step=ESCAPE_PATTERN.matcher(matcher.group("step")).replaceAll("${escape}");

            path.add(step);

            next=matcher.end();
        }

        if ( next < length ) {
            throw new IllegalArgumentException(format("malformed expression <%s>", expression));
        }

        return new Expression(pipe, path);
    }


    //̸/////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public Expression(

            final List<Transform> pipe,
            final List<String> path

    ) {

        this.pipe=list(pipe);
        this.path=list(path);

    }


    /**
     * Checks if this expression is empty.
     *
     * @return {@code true} if this expression has no transforms and no path; {@code false} otherwise
     */
    public boolean isEmpty() {
        return pipe.isEmpty() && path.isEmpty();
    }

    /**
     * Checks if this expression includes computed transformations.
     *
     * @return {@code true} if this expression has transforms; {@code false} otherwise
     */
    public boolean isComputed() {
        return !pipe.isEmpty();
    }

    /**
     * Checks if this expression includes aggregate transformations.
     *
     * @return {@code true} if this expression has aggregate transforms; {@code false} otherwise
     */
    public boolean isAggregate() {
        return pipe.stream().anyMatch(Transform::isAggregate);
    }


    /**
     * Creates a new expression with the specified transforms.
     *
     * @param transforms the transforms to include in the pipeline
     *
     * @return a new expression with the specified transforms
     *
     * @throws NullPointerException if {@code transforms} is {@code null} or contains {@code null} elements
     */
    public Expression pipe(final Transform... transforms) {

        if ( transforms == null || Arrays.stream(transforms).anyMatch(Objects::isNull) ) {
            throw new NullPointerException("null transforms");
        }

        return pipe(list(transforms));
    }

    /**
     * Creates a new expression with the specified transforms.
     *
     * @param transforms the list of transforms to include in the pipeline
     *
     * @return a new expression with the specified transforms
     *
     * @throws NullPointerException if {@code transforms} is {@code null} or contains {@code null} elements
     */
    public Expression pipe(final List<Transform> transforms) {

        if ( transforms == null || transforms.stream().anyMatch(Objects::isNull) ) {
            throw new NullPointerException("null transforms");
        }

        return new Expression(transforms, path);
    }


    /**
     * Creates a new expression with the specified property path.
     *
     * @param steps the property names that form the path
     *
     * @return a new expression with the specified path
     *
     * @throws NullPointerException     if {@code steps} is {@code null} or contains {@code null} elements
     * @throws IllegalArgumentException if any step uses a reserved property name
     */
    public Expression path(final String... steps) {

        if ( steps == null || Arrays.stream(steps).anyMatch(Objects::isNull) ) {
            throw new NullPointerException("null steps");
        }

        return path(list(steps));
    }

    /**
     * Creates a new expression with the specified property path.
     *
     * @param steps the list of property names that form the path
     *
     * @return a new expression with the specified path
     *
     * @throws NullPointerException     if {@code steps} is {@code null} or contains {@code null} elements
     * @throws IllegalArgumentException if any step uses a reserved property name
     */
    public Expression path(final List<String> steps) {

        if ( steps == null || steps.stream().anyMatch(Objects::isNull) ) {
            throw new NullPointerException("null steps");
        }

        steps.stream()
                .filter(Value::isReserved)
                .forEach(step -> error(new IllegalArgumentException(format("reserved property name <%s>", step))));

        return new Expression(pipe, steps);
    }


    /**
     * Applies this expression to a shape to determine the resulting shape.
     *
     * @param shape the input shape to apply the expression to
     *
     * @return the shape that results from applying this expression
     *
     * @throws NullPointerException   if {@code shape} is {@code null}
     * @throws NoSuchElementException if the expression references unknown properties
     */
    public Shape apply(final Shape shape) throws NoSuchElementException {

        if ( shape == null ) {
            throw new NullPointerException("null shape");
        }

        Shape mapped=shape;

        for (final String step : path) {
            mapped=mapped.property(step).map(Property::shape).orElseThrow(() ->
                    new NoSuchElementException(format("unknown property <%s>", step))
            );
        }

        for (int i=pipe.size()-1; i >= 0; --i) {
            mapped=pipe.get(i).apply(mapped);
        }

        return mapped;
    }


    @Override public String toString() {
        return pipe.stream().map(t -> "%s:".formatted(t.name().toLowerCase(ROOT))).collect(joining())
               +join(".", path);
    }

}
