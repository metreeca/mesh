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

package com.metreeca.mesh.shapes;

import java.net.URI;
import java.util.Optional;
import java.util.function.Supplier;

import static com.metreeca.mesh.Value.Object;
import static com.metreeca.mesh.Value.isReserved;
import static com.metreeca.shim.URIs.term;

import static java.util.Objects.requireNonNull;
import static java.util.function.Predicate.not;

/**
 * A property definition with name, direction predicates and shape constraints.
 *
 * <p>Represents a property that can connect resources through forward or reverse predicates, with optional shape
 * constraints for validation. Properties can be marked as hidden, foreign, or embedded to control their processing
 * and serialization behavior.
 *
 * @param hidden      whether this property should be hidden from serialization
 * @param foreign     whether this property references external resources
 * @param embedded    whether this property's values should be embedded inline
 * @param name        the name of the property
 * @param description the descriptive text for the property
 * @param forward     the optional forward predicate URI
 * @param reverse     the optional reverse predicate URI
 * @param generator   the shape generator for value constraints
 */
public final record Property(

        boolean hidden,
        boolean foreign,
        boolean embedded,

        String name,

        String description,

        Optional<URI> forward,
        Optional<URI> reverse,

        Supplier<Shape> generator

) {

    private static final Supplier<Shape> GENERATOR=Shape::shape;


    /**
     * Creates a property with the specified name and default settings.
     *
     * @param name the name of the property
     *
     * @return a new property instance with default settings
     *
     * @throws NullPointerException if {@code name} is {@code null}
     */
    public static Property property(final String name) {

        if ( name == null ) {
            throw new NullPointerException("null name");
        }

        return new Property(

                false,
                false,
                false,

                name,
                "",

                Optional.empty(),
                Optional.empty(),

                GENERATOR

        );
    }


    //̸/////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public Property(

            final boolean hidden,
            final boolean foreign,
            final boolean embedded,

            final String name,
            String description,

            final Optional<URI> forward,
            final Optional<URI> reverse,

            final Supplier<Shape> generator

    ) {

        if ( foreign && embedded ) {
            throw new IllegalArgumentException("conflicting foreign and embedded flags");
        }

        if ( name == null ) {
            throw new NullPointerException("null name");
        }

        if ( name.isBlank() ) {
            throw new IllegalArgumentException("blank name");
        }

        if ( isReserved(name) ) {
            throw new IllegalArgumentException("reserved property name <%s>".formatted(name));
        }

        if ( description == null ) {
            throw new NullPointerException("null description");
        }

        if ( forward == null ) {
            throw new NullPointerException("null forward URI");
        }

        if ( forward.filter(not(URI::isAbsolute)).isPresent() ) {
            throw new IllegalArgumentException("relative forward URI <%s>".formatted(forward));
        }

        if ( reverse == null ) {
            throw new NullPointerException("null reverse URI");
        }

        if ( reverse.filter(not(URI::isAbsolute)).isPresent() ) {
            throw new IllegalArgumentException("relative reverse URI <%s>".formatted(reverse));
        }

        if ( generator == null ) {
            throw new NullPointerException("null shape generator");
        }


        this.hidden=hidden;
        this.foreign=foreign;
        this.embedded=embedded;

        this.name=name;
        this.description=description;

        this.forward=forward;
        this.reverse=reverse;

        this.generator=generator instanceof CachingSupplier<?> ? generator : new CachingSupplier<>(generator);
    }


    /**
     * Checks if this property's values should be embedded inline.
     *
     * @return {@code true} if values should be embedded inline
     */
    public boolean embedded() {
        return embedded;
    }

    /**
     * Retrieves the shape constraints for this property's values.
     *
     * <p>If the property is embedded, the shape is automatically configured
     * with an Object datatype constraint.
     *
     * @return the shape constraints for this property's values
     */
    public Shape shape() {

        final Shape shape=requireNonNull(generator.get(), "null generated shape");

        return embedded ? shape.datatype(Object()) : shape;
    }


    /**
     * Configures this property's hidden flag.
     *
     * @param hidden whether the property should be hidden from serialization
     *
     * @return a new property instance with the specified hidden flag
     */
    public Property hidden(final boolean hidden) {
        return new Property(

                hidden,
                foreign,
                embedded,

                name,
                description,

                forward,
                reverse,

                generator

        );
    }

    /**
     * Configures this property's foreign flag.
     *
     * @param foreign whether the property references external resources
     *
     * @return a new property instance with the specified foreign flag
     */
    public Property foreign(final boolean foreign) {
        return new Property(

                hidden,
                foreign,
                embedded,

                name,
                description,

                forward,
                reverse,

                generator

        );
    }

    /**
     * Configures this property's embedded flag.
     *
     * @param embedded whether the property's values should be embedded inline
     *
     * @return a new property instance with the specified embedded flag
     */
    public Property embedded(final boolean embedded) {
        return new Property(

                hidden,
                foreign,
                embedded,

                name,
                description,

                forward,
                reverse,

                generator

        );
    }


    /**
     * Configures this property with a new name.
     *
     * @param name the new name for the property
     *
     * @return a new property instance with the specified name
     *
     * @throws NullPointerException if {@code name} is {@code null}
     */
    public Property name(final String name) {

        if ( name == null ) {
            throw new NullPointerException("null name");
        }

        return new Property(

                hidden,
                foreign,
                embedded,

                name,
                description,

                forward,
                reverse,

                generator

        );
    }

    /**
     * Configures this property with a new description.
     *
     * @param description the new description for the property
     *
     * @return a new property instance with the specified description
     *
     * @throws NullPointerException if {@code description} is {@code null}
     */
    public Property description(final String description) {

        if ( description == null ) {
            throw new NullPointerException("null description");
        }

        return new Property(

                hidden,
                foreign,
                embedded,

                name,
                description,

                forward,
                reverse,

                generator

        );
    }


    /**
     * Configures this property with a forward predicate URI derived from the property name.
     *
     * @param forward whether to configure a forward predicate
     *
     * @return a new property instance with the forward predicate if true, otherwise this instance
     */
    public Property forward(final boolean forward) {
        return forward ? forward(term(name)) : this;
    }

    /**
     * Configures this property with the specified forward predicate URI.
     *
     * @param forward the forward predicate URI
     *
     * @return a new property instance with the specified forward predicate
     *
     * @throws NullPointerException if {@code forward} is {@code null}
     */
    public Property forward(final URI forward) {

        if ( forward == null ) {
            throw new NullPointerException("null forward URI");
        }

        return new Property(

                hidden,
                foreign,
                embedded,

                name,
                description,

                Optional.of(forward),
                reverse,

                generator

        );
    }


    /**
     * Configures this property with a reverse predicate URI derived from the property name.
     *
     * @param reverse whether to configure a reverse predicate
     *
     * @return a new property instance with the reverse predicate if true, otherwise this instance
     */
    public Property reverse(final boolean reverse) {
        return reverse ? reverse(term(name)) : this;
    }

    /**
     * Configures this property with the specified reverse predicate URI.
     *
     * @param reverse the reverse predicate URI
     *
     * @return a new property instance with the specified reverse predicate
     *
     * @throws NullPointerException if {@code reverse} is {@code null}
     */
    public Property reverse(final URI reverse) {

        if ( reverse == null ) {
            throw new NullPointerException("null reverse URI");
        }

        return new Property(

                hidden,
                foreign,
                embedded,

                name,
                description,

                forward,
                Optional.of(reverse),

                generator

        );
    }


    /**
     * Configures this property with the specified shape constraints.
     *
     * @param shape the shape constraints for the property's values
     *
     * @return a new property instance with the specified shape
     *
     * @throws NullPointerException if {@code shape} is {@code null}
     */
    public Property shape(final Shape shape) {

        if ( shape == null ) {
            throw new NullPointerException("null shape");
        }

        return new Property(

                hidden,
                foreign,
                embedded,

                name,
                description,

                forward,
                reverse,

                () -> shape

        );
    }

    /**
     * Configures this property with the specified shape generator.
     *
     * @param generator the shape generator for the property's value constraints
     *
     * @return a new property instance with the specified shape generator
     *
     * @throws NullPointerException if {@code generator} is {@code null}
     */
    public Property shape(final Supplier<Shape> generator) {

        if ( generator == null ) {
            throw new NullPointerException("null shape generator");
        }

        return new Property(

                hidden,
                foreign,
                embedded,

                name,
                description,

                forward,
                reverse,

                generator

        );
    }


    //̸/////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private static final class CachingSupplier<T> implements Supplier<T> {

        private final Supplier<? extends T> supplier;
        private T value;

        private CachingSupplier(final Supplier<? extends T> supplier) { this.supplier=supplier; }

        @Override public T get() {
            return value != null ? value : (value=supplier.get());
        }

        @Override public boolean equals(final Object object) { // ;( support partial shape equality (see Shape#extend)

            return this == object || object instanceof final CachingSupplier<?> caching
                                     && supplier.equals(caching.supplier);
        }

        @Override public int hashCode() {
            return supplier.hashCode();
        }

    }

}
