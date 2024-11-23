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

import java.net.URI;
import java.util.Optional;
import java.util.function.Supplier;

import static com.metreeca.mesh.Value.Object;
import static com.metreeca.mesh.Value.isReserved;
import static com.metreeca.shim.URIs.term;

import static java.util.Objects.requireNonNull;
import static java.util.function.Predicate.not;

public final record Property(

        boolean hidden,
        boolean foreign,
        boolean embedded,

        String name,
        Optional<URI> forward,
        Optional<URI> reverse,

        Supplier<Shape> generator

) {

    private static final Supplier<Shape> GENERATOR=Shape::shape;


    public static Property property(final String name) {

        if ( name == null ) {
            throw new NullPointerException("null name");
        }

        return new Property(

                false,
                false,
                false,

                name,
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
        this.forward=forward;
        this.reverse=reverse;

        this.generator=generator instanceof CachingSupplier<?> ? generator : new CachingSupplier<>(generator);
    }


    public boolean embedded() {
        return embedded;
    }

    public Shape shape() {

        final Shape shape=requireNonNull(generator.get(), "null generated shape");

        return embedded ? shape.datatype(Object()) : shape;
    }


    public Property hidden(final boolean hidden) {
        return new Property(

                hidden,
                foreign,
                embedded,

                name,
                forward,
                reverse,

                generator

        );
    }

    public Property foreign(final boolean foreign) {
        return new Property(

                hidden,
                foreign,
                embedded,

                name,
                forward,
                reverse,

                generator

        );
    }

    public Property embedded(final boolean embedded) {
        return new Property(

                hidden,
                foreign,
                embedded,

                name,
                forward,
                reverse,

                generator

        );
    }


    public Property forward(final boolean forward) {
        return forward ? forward(term(name)) : this;
    }

    public Property forward(final URI forward) {

        if ( forward == null ) {
            throw new NullPointerException("null forward URI");
        }

        return new Property(

                hidden,
                foreign,
                embedded,

                name,
                Optional.of(forward),
                reverse,

                generator

        );
    }


    public Property reverse(final boolean reverse) {
        return reverse ? reverse(term(name)) : this;
    }

    public Property reverse(final URI reverse) {

        if ( reverse == null ) {
            throw new NullPointerException("null reverse URI");
        }

        return new Property(

                hidden,
                foreign,
                embedded,

                name,
                forward,
                Optional.of(reverse),

                generator

        );
    }


    public Property shape(final Shape shape) {

        if ( shape == null ) {
            throw new NullPointerException("null shape");
        }

        return new Property(

                hidden,
                foreign,
                embedded,

                name,
                forward,
                reverse,

                () -> shape

        );
    }

    public Property shape(final Supplier<Shape> generator) {

        if ( generator == null ) {
            throw new NullPointerException("null shape generator");
        }

        return new Property(

                hidden,
                foreign,
                embedded,

                name,
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
