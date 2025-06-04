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

import java.util.*;
import java.util.function.BinaryOperator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static com.metreeca.mesh.Value.compare;
import static com.metreeca.shim.Collections.list;
import static com.metreeca.shim.Collections.set;
import static com.metreeca.shim.Exceptions.error;

import static java.lang.String.format;
import static java.text.Normalizer.Form.NFD;
import static java.text.Normalizer.normalize;
import static java.util.Objects.requireNonNull;
import static java.util.function.Predicate.not;
import static java.util.stream.Collectors.toSet;

/**
 * Collection query criterion.
 *
 * <p>Defines a set of ordering, and filtering constraints for data queries.</p>
 *
 * <p>This class encapsulates various types of criteria that can be applied to data collections:</p>
 *
 * <ul>
 *   <li>Result ordering ({@code order}) for controlling presentation sequence</li>
 *   <li>Item ({@code focus}) for sorting prioritization</li>
 *   <li>Comparison constraints ({@code lt}, {@code gt}, {@code lte}, {@code gte}) for range filtering</li>
 *   <li>Text search capabilities ({@code like}) for keyword-based filtering</li>
 *   <li>Value enumeration constraints ({@code any}) for membership filtering</li>
 * </ul>
 *
 * <p>Criterion instances are immutable and thread-safe. Operations that would modify a criterion instead return a new
 * instance with the updated constraints. Multiple criteria can be combined using the {@link #merge(Criterion)} method,
 * which produces a new criterion representing the intersection of all constraints.</p>
 *
 * <p>Criterion objects maintain internal consistency by validating constraints during construction to ensure they
 * do not conflict (for example, incompatible range limits) and by preventing nested collections in value constraints.
 * </p>
 *
 * @param order the ordering constraint for query results; if present, determines the position of matched values in
 *              query results
 * @param focus the focus value set for item selection; if present, prioritizes the selected values in sorting
 *              operations
 * @param lt    the less-than constraint for range filtering; if present, restricts the query to items strictly less
 *              than this value
 * @param gt    the greater-than constraint for range filtering; if present, restricts the query to items strictly
 *              greater than this value
 * @param lte   the less-than-or-equal constraint for range filtering; if present, restricts the query to items less
 *              than or equal to this value
 * @param gte   the greater-than-or-equal constraint for range filtering; if present, restricts the query to items
 *              greater than or equal to this value
 * @param like  the keyword search constraint for text filtering; if present, restricts the query to items matching
 *              these keywords
 * @param any   the value enumeration constraint for membership testing; if present, restricts the query to items
 *              matching any value in this set
 *
 * @see Query A query combining a criterion with a data model
 */
public final record Criterion(

        Optional<Integer> order, Optional<Set<Value>> focus,

        Optional<Value> lt, Optional<Value> gt, Optional<Value> lte, Optional<Value> gte,

        Optional<String> like, Optional<Set<Value>> any

) {

    private static final Pattern WORD_PATTERN=Pattern.compile("\\w+");
    private static final Pattern MARK_PATTERN=Pattern.compile("\\p{M}");

    private static final Criterion EMPTY=new Criterion(

            Optional.empty(), Optional.empty(),

            Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(),

            Optional.empty(), Optional.empty()

    );


    /**
     * Creates an empty criterion.
     *
     * @return a new empty criterion with no constraints
     */
    public static Criterion criterion() {
        return EMPTY;
    }

    /**
     * Creates a regular expression pattern for matching keywords.
     *
     * <p>Generates a case-insensitive regular expression pattern that matches text containing all the words in the
     * given keywords string. The pattern normalizes Unicode characters by removing diacritical marks and supports
     * optional word stemming.</p>
     *
     * @param keywords the keywords string to be converted into a pattern
     * @param stemming if {@code true}, enables word stemming for broader matches; if {@code false}, requires exact word
     *                 matches
     *
     * @return a regular expression pattern for matching the given keywords
     *
     * @throws NullPointerException if {@code keywords} is {@code null}
     */
    public static String pattern(final CharSequence keywords, final boolean stemming) {

        if ( keywords == null ) {
            throw new NullPointerException("null keywords");
        }

        final StringBuilder builder=new StringBuilder(keywords.length()).append("(?i:.*");

        final String normalized=MARK_PATTERN.matcher(normalize(keywords, NFD)).replaceAll("");
        final Matcher matcher=WORD_PATTERN.matcher(normalized);

        while ( matcher.find() ) {
            builder.append("\\b").append(matcher.group()).append(stemming ? "" : "\\b").append(".*");
        }

        return builder.append(")").toString();
    }


    //̸/////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public Criterion(

            final Optional<Integer> order,
            final Optional<Set<Value>> focus,

            final Optional<Value> lt,
            final Optional<Value> gt,
            final Optional<Value> lte,
            final Optional<Value> gte,

            final Optional<String> like,
            final Optional<Set<Value>> any

    ) {

        if ( focus.map(values -> values.stream().anyMatch(v -> v.array().isPresent())).orElse(false) ) {
            throw new IllegalArgumentException(format("nested <focus> values <%s>", focus.get()));
        }

        if ( !lt.map(Value::comparable).orElse(true) ) {
            throw new IllegalArgumentException(format("incomparable <lt> value <%s>", lt));
        }

        if ( !gt.map(Value::comparable).orElse(true) ) {
            throw new IllegalArgumentException(format("incomparable <gt> value <%s>", gt));
        }

        if ( !lte.map(Value::comparable).orElse(true) ) {
            throw new IllegalArgumentException(format("incomparable <lte> value <%s>", lte));
        }

        if ( !gte.map(Value::comparable).orElse(true) ) {
            throw new IllegalArgumentException(format("incomparable <gte> value <%s>", gte));
        }

        if ( lt.isPresent() && lte.isPresent() ) {
            throw new IllegalArgumentException(format("conflicting <lt/lte> criteria <%s> / <%s>", lt, lte));
        }

        if ( gt.isPresent() && gte.isPresent() ) {
            throw new IllegalArgumentException(format("conflicting <gt/gte> criteria <%s> / <%s>", gt, gte));
        }

        if ( !compare(gt, lt).map(v -> v < 0).orElse(true) ) {
            throw new IllegalArgumentException(format("conflicting <gt/lt> criteria <%s> / <%s>", gt, lt));
        }

        if ( !compare(gt, lte).map(v -> v <= 0).orElse(true) ) {
            throw new IllegalArgumentException(format("conflicting <gt/lte> criteria <%s> / <%s>", gt, lte));
        }

        if ( !compare(gte, lt).map(v -> v <= 0).orElse(true) ) {
            throw new IllegalArgumentException(format("conflicting <gte/lt> criteria <%s> / <%s>", gte, lt));
        }

        if ( !compare(gte, lte).map(v -> v <= 0).orElse(true) ) {
            throw new IllegalArgumentException(format("conflicting <gte/lte> criteria <%s> / <%s>", gte, lte));
        }

        if ( any.map(values -> values.stream().anyMatch(v -> v.array().isPresent())).orElse(false) ) {
            throw new IllegalArgumentException(format("nested <any> values <%s>", any.get()));
        }

        this.order=order;
        this.focus=focus.filter(not(Set::isEmpty));

        this.lt=lt;
        this.gt=gt;
        this.lte=lte;
        this.gte=gte;

        this.like=like.filter(not(String::isBlank));
        this.any=any; // accept empty value sets

    }


    /**
     * Checks if this criterion has no constraints.
     *
     * @return {@code true} if this criterion has no constraints; {@code false} otherwise
     */
    public boolean isEmpty() {
        return order.isEmpty() && focus.isEmpty()

               && lt().isEmpty() && gt().isEmpty() && lte().isEmpty() && gte().isEmpty()

               && like().isEmpty() && any().isEmpty();
    }

    /**
     * Checks if this criterion contains filtering constraints.
     *
     * <p>Returns true if this criterion includes at least one filtering constraint (lt, gt, lte, gte, like, or
     * any).</p>
     *
     * @return {@code true} if this criterion has filtering constraints; {@code false} otherwise
     */
    public boolean isFilter() {
        return lt().isPresent() || gt().isPresent() || lte().isPresent() || gte().isPresent() || like().isPresent() || any().isPresent();
    }


    /**
     * Creates a new criterion with the specified ordering constraint.
     *
     * <p>Returns a new criterion identical to this one but with the ordering constraint set to the specified
     * value.</p>
     *
     * @param ordered the ordering value to be set
     *
     * @return a new criterion with the specified ordering constraint
     */
    public Criterion order(final int ordered) {
        return new Criterion(

                Optional.of(ordered), focus,

                lt, gt, lte, gte,

                like, any

        );
    }


    /**
     * Creates a new criterion with the specified focus values.
     *
     * <p>Returns a new criterion identical to this one but with the focus constraint set to include the specified
     * values. Focus values are prioritized in sorting operations.</p>
     *
     * @param values the focus values to be included in the new criterion
     *
     * @return a new criterion with the specified focus values
     *
     * @throws NullPointerException if {@code values} is {@code null} or contains {@code null} elements
     */
    public final Criterion focus(final Valuable... values) {

        if ( values == null || Arrays.stream(values).anyMatch(Objects::isNull) ) {
            throw new NullPointerException("null values");
        }

        return focus(list(values));
    }

    /**
     * Creates a new criterion with the specified focus values.
     *
     * <p>Returns a new criterion identical to this one but with the focus constraint set to include the values from
     * the provided collection. Focus values are prioritized in sorting operations.</p>
     *
     * @param values the collection of focus values to be included in the new criterion
     *
     * @return a new criterion with the specified focus values
     *
     * @throws NullPointerException if {@code values} is {@code null}
     */
    public Criterion focus(final Collection<? extends Valuable> values) {

        if ( values == null ) { // accept null elements
            throw new NullPointerException("null values");
        }

        return new Criterion(

                order, Optional.of(set(values.stream().map(v -> requireNonNull(v.toValue(), "null supplied value")))).filter(not(Set::isEmpty)),

                lt, gt, lte, gte,

                like, any

        );
    }


    /**
     * Creates a new criterion with a less-than constraint.
     *
     * <p>Returns a new criterion identical to this one but with the less-than constraint set to the specified value.
     * This restricts a query to match only values strictly less than the limit.</p>
     *
     * @param limit the value to use as the upper bound (exclusive)
     *
     * @return a new criterion with the specified less-than constraint
     *
     * @throws NullPointerException     if {@code limit} is {@code null}
     * @throws IllegalArgumentException if the constraint would conflict with existing constraints or if the value is
     *                                  not comparable
     */
    public Criterion lt(final Valuable limit) {

        if ( limit == null ) {
            throw new NullPointerException("null limit");
        }

        return new Criterion(

                order, focus,

                Optional.of(limit).map(v -> requireNonNull(v.toValue(), "null supplied limit")), gt, lte, gte,

                like, any

        );
    }

    /**
     * Creates a new criterion with a greater-than constraint.
     *
     * <p>Returns a new criterion identical to this one but with the greater-than constraint set to the specified
     * value. This restricts a query to match only values strictly greater than the limit.</p>
     *
     * @param limit the value to use as the lower bound (exclusive)
     *
     * @return a new criterion with the specified greater-than constraint
     *
     * @throws NullPointerException     if {@code limit} is {@code null}
     * @throws IllegalArgumentException if the constraint would conflict with existing constraints or if the value is
     *                                  not comparable
     */
    public Criterion gt(final Valuable limit) {

        if ( limit == null ) {
            throw new NullPointerException("null limit");
        }

        return new Criterion(

                order, focus,

                lt, Optional.of(limit).map(v -> requireNonNull(v.toValue(), "null supplied limit")), lte, gte,

                like, any

        );
    }

    /**
     * Creates a new criterion with a less-than-or-equal constraint.
     *
     * <p>Returns a new criterion identical to this one but with the less-than-or-equal constraint set to the specified
     * value. This restricts a query to match only values less than or equal to the limit.</p>
     *
     * @param limit the value to use as the upper bound (inclusive)
     *
     * @return a new criterion with the specified less-than-or-equal constraint
     *
     * @throws NullPointerException     if {@code limit} is {@code null}
     * @throws IllegalArgumentException if the constraint would conflict with existing constraints or if the value is
     *                                  not comparable
     */
    public Criterion lte(final Valuable limit) {

        if ( limit == null ) {
            throw new NullPointerException("null limit");
        }
        return new Criterion(

                order, focus,

                lt, gt, Optional.of(limit).map(v -> requireNonNull(v.toValue(), "null supplied limit")), gte,

                like, any

        );
    }

    /**
     * Creates a new criterion with a greater-than-or-equal constraint.
     *
     * <p>Returns a new criterion identical to this one but with the greater-than-or-equal constraint set to the
     * specified value. This restricts a query to match only values greater than or equal to the limit.</p>
     *
     * @param limit the value to use as the lower bound (inclusive)
     *
     * @return a new criterion with the specified greater-than-or-equal constraint
     *
     * @throws NullPointerException     if {@code limit} is {@code null}
     * @throws IllegalArgumentException if the constraint would conflict with existing constraints or if the value is
     *                                  not comparable
     */
    public Criterion gte(final Valuable limit) {

        if ( limit == null ) {
            throw new NullPointerException("null limit");
        }

        return new Criterion(

                order, focus,

                lt, gt, lte, Optional.of(limit).map(v -> requireNonNull(v.toValue(), "null supplied limit")),

                like, any

        );
    }


    /**
     * Creates a new criterion with a keyword search constraint.
     *
     * <p>Returns a new criterion identical to this one but with the keyword search constraint set to the specified
     * string. This restricts a query to match only values containing the specified keywords. Blank keyword strings are
     * ignored.</p>
     *
     * @param keywords the string of keywords to search for
     *
     * @return a new criterion with the specified keyword search constraint
     *
     * @throws NullPointerException if {@code keywords} is {@code null}
     */
    public Criterion like(final String keywords) {

        if ( keywords == null ) {
            throw new NullPointerException("null keywords");
        }

        return new Criterion(

                order, focus,

                lt, gt, lte, gte,

                Optional.of(keywords).filter(not(String::isBlank)), any

        );
    }


    /**
     * Creates a new criterion with a value enumeration constraint.
     *
     * <p>Returns a new criterion identical to this one but with the value enumeration constraint set to include the
     * specified values. This restricts a query to match only values that equal any of the provided values.</p>
     *
     * @param values the values to include in the enumeration set
     *
     * @return a new criterion with the specified value enumeration constraint
     *
     * @throws NullPointerException if {@code values} is {@code null} or contains {@code null} elements
     */
    public final Criterion any(final Valuable... values) {

        if ( values == null || Arrays.stream(values).anyMatch(Objects::isNull) ) {
            throw new NullPointerException("null values");
        }

        return any(list(values));
    }

    /**
     * Creates a new criterion with a value enumeration constraint.
     *
     * <p>Returns a new criterion identical to this one but with the value enumeration constraint set to include the
     * values from the provided collection. This restricts a query to match only values that equal any of the provided
     * values. Empty value sets are accepted.</p>
     *
     * @param values the collection of values to include in the enumeration set
     *
     * @return a new criterion with the specified value enumeration constraint
     *
     * @throws NullPointerException if {@code values} is {@code null}
     */
    public Criterion any(final Collection<? extends Valuable> values) {

        if ( values == null ) { // accept null elements
            throw new NullPointerException("null values");
        }

        return new Criterion(

                order, focus,

                lt, gt, lte, gte,

                like, Optional.of(set(values.stream().map(v -> requireNonNull(v.toValue(), "null supplied value")))) // accept empty sets

        );
    }


    /**
     * Merges this criterion with another.
     *
     * <p>Returns a new criterion that combines the constraints from both this criterion and the specified one.
     * The merge operation produces a criterion that represents the logical intersection of both sets of constraints,
     * applying the most restrictive version of each constraint type.</p>
     *
     * <p>Constraints are merged according to these rules:</p>
     *
     * <ul>
     *   <li>Order constraints must be identical or an exception is thrown</li>
     *   <li>Focus values are combined through a union operation</li>
     *   <li>Range constraints (lt, gt, lte, gte) are narrowed to the most restrictive range</li>
     *   <li>Like constraints must be identical or an exception is thrown</li>
     *   <li>Any constraints must form a subset relationship or an exception is thrown</li>
     * </ul>
     *
     * @param criterion the criterion to merge with this one
     *
     * @return a new criterion representing the merged constraints
     *
     * @throws NullPointerException     if {@code criterion} is {@code null}
     * @throws IllegalArgumentException if the two criteria contain incompatible constraints that cannot be merged
     */
    public Criterion merge(final Criterion criterion) {

        if ( criterion == null ) {
            throw new NullPointerException("null criterion");
        }

        final Optional<Set<Value>> xxx=merge(any, criterion.any, (x, y) -> x.containsAll(y) ? y : y.containsAll(x) ? x : error(new IllegalArgumentException(format("inconsistent any constraints <%s> / <%s>", x, y))));

        return new Criterion(

                merge(order, criterion.order, (x, y) -> x.equals(y) ? x : error(new IllegalArgumentException(format("inconsistent order constraints <%s> / <%s>", x, y)))),

                Optional.of(Stream.of(focus, criterion.focus).flatMap(Optional::stream).flatMap(Collection::stream).collect(toSet())),

                merge(lt, criterion.lt, (x, y) -> compare(x, y) <= 0 ? x : y), // !!! report constratint type if not comparable
                merge(gt, criterion.gt, (x, y) -> compare(x, y) >= 0 ? x : y), merge(lte, criterion.lte, (x, y) -> compare(x, y) <= 0 ? x : y), merge(gte, criterion.gte, (x, y) -> compare(x, y) >= 0 ? x : y),

                merge(like, criterion.like, (x, y) -> x.equals(y) ? x : error(new IllegalArgumentException(format("inconsistent like constraints <%s> / <%s>", x, y)))),

                xxx

        );
    }


    private <T> Optional<T> merge(final Optional<T> x, final Optional<T> y, final BinaryOperator<T> merge) {
        return x.map(xv -> y.map(yv -> merge.apply(xv, yv)).orElse(xv)).or(() -> y);
    }

}
