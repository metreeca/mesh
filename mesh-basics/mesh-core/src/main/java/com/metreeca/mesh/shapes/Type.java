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

public final record Type(

        String name,
        String description,

        URI uri

) {

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

    public Type description(final String description) {

        if ( name == null ) {
            throw new NullPointerException("null description");
        }

        return new Type(
                name,
                description,
                uri
        );
    }

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
