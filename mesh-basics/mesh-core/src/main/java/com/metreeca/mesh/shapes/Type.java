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

import com.metreeca.shim.URIs;

import java.net.URI;

import static com.metreeca.mesh.Value.isReserved;
import static com.metreeca.shim.URIs.term;

/**
 * A type definition with name, description and URI.
 *
 * <p>Represents a semantic type that can be used in data validation and schema definitions.
 * Types provide both a human-readable name and a globally unique URI identifier.
 *
 * @param name        the human-readable name of the type
 * @param description the descriptive text for the type
 * @param uri         the globally unique URI identifier for the type
 */
public final record Type(

        String name,
        String description,

        URI uri

) {

    /**
     * Creates a type with the specified name and a generated URI.
     *
     * @param name the name of the type
     *
     * @return a new type instance
     *
     * @throws NullPointerException if {@code name} is {@code null}
     */
    public static Type type(final String name) {

        if ( name == null ) {
            throw new NullPointerException("null name");
        }

        return new Type(
                name,
                "",
                term(name)
        );
    }

    /**
     * Creates a type from the specified URI with extracted name.
     *
     * @param uri the URI of the type
     *
     * @return a new type instance
     *
     * @throws NullPointerException if {@code uri} is {@code null}
     */
    public static Type type(final URI uri) {

        if ( uri == null ) {
            throw new NullPointerException("null uri");
        }

        return new Type(
                URIs.name(uri),
                "",
                uri
        );
    }

    /**
     * Creates a type with the specified name and URI.
     *
     * @param name the name of the type
     * @param uri  the URI of the type
     *
     * @return a new type instance
     *
     * @throws NullPointerException if either {@code name} or {@code uri} is {@code null}
     */
    public static Type type(final String name, final URI uri) {

        if ( name == null ) {
            throw new NullPointerException("null name");
        }

        if ( uri == null ) {
            throw new NullPointerException("null uri");
        }

        return new Type(
                name,
                "",
                uri
        );
    }


    //̸/////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public Type {

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

        if ( uri == null ) {
            throw new NullPointerException("null URI");
        }

        if ( !uri.isAbsolute() ) {
            throw new IllegalArgumentException("relative URI <%s>".formatted(uri));
        }

    }


    /**
     * Configures this type with a new name.
     *
     * @param name the new name for the type
     *
     * @return a new type instance with the specified name
     *
     * @throws NullPointerException if {@code name} is {@code null}
     */
    public Type name(final String name) {

        if ( name == null ) {
            throw new NullPointerException("null name");
        }

        return new Type(
                name,
                description,
                uri
        );
    }

    /**
     * Configures this type with a new description.
     *
     * @param description the new description for the type
     *
     * @return a new type instance with the specified description
     *
     * @throws NullPointerException if {@code description} is {@code null}
     */
    public Type description(final String description) {

        if ( description == null ) {
            throw new NullPointerException("null description");
        }

        return new Type(
                name,
                description,
                uri
        );
    }

    /**
     * Configures this type with a new URI.
     *
     * @param uri the new URI for the type
     *
     * @return a new type instance with the specified URI
     *
     * @throws NullPointerException if {@code uri} is {@code null}
     */
    public Type uri(final URI uri) {

        if ( uri == null ) {
            throw new NullPointerException("null URI");
        }

        return new Type(
                name,
                description,
                uri
        );
    }


    @Override
    public String toString() {
        return "%s=<%s>".formatted(name, uri);
    }

}
