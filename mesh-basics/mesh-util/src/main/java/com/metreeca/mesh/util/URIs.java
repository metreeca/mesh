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

package com.metreeca.mesh.util;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.util.Objects;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.UUID.nameUUIDFromBytes;
import static java.util.UUID.randomUUID;

public final class URIs {

    private static final URI EMPTY=uri("");
    private static final URI BASE=uri("app:/");
    private static final URI TERM=uri("app:/#");


    public static URI base() {
        return BASE;
    }

    public static URI item(final String path) {

        if ( path == null ) {
            throw new NullPointerException("null path");
        }

        return BASE.resolve(path);
    }

    public static URI term(final String name) {

        if ( name == null ) {
            throw new NullPointerException("null name");
        }

        return uri(TERM+URLEncoder.encode(name, UTF_8));
    }


    public static URI uri() {
        return EMPTY;
    }

    public static URI uri(final String uri) {

        if ( uri == null ) {
            throw new NullPointerException("null uri");
        }

        return URI.create(uri);
    }


    public static URI absolute(final URI base, final URI uri) {

        if ( uri == null ) {
            throw new NullPointerException("null uri");
        }

        if ( base == null ) {
            throw new NullPointerException("null base");
        }

        return base.resolve(uri);
    }

    public static URI relative(final URI base, final URI uri) {

        if ( uri == null ) {
            throw new NullPointerException("null uri");
        }

        if ( base == null ) {
            throw new NullPointerException("null base");
        }

        if ( Objects.equals(uri.getScheme(), base.getScheme())
             && Objects.equals(uri.getRawAuthority(), base.getRawAuthority())
        ) {

            try {

                final String authority=uri.getRawAuthority();
                final String ssp=uri.getRawSchemeSpecificPart();
                final String fragment=uri.getRawFragment();

                return new URI(
                        null,
                        authority == null ? ssp : ssp.substring(authority.length()+2),
                        fragment
                );

            } catch ( final URISyntaxException e ) {
                throw new IllegalArgumentException("malformed URI <%s>".formatted(uri), e);
            }

        } else {

            return uri;

        }
    }


    public static String name(final URI uri) {

        if ( uri == null ) {
            throw new NullPointerException("null uri");
        }

        final String string=uri.toString();

        final int hash=string.indexOf('#');

        if ( hash >= 0 ) {

            return string.substring(hash+1);

        } else {

            final int slash=string.lastIndexOf('/');

            if ( slash >= 0 ) {

                return string.substring(slash+1);

            } else {

                final int colon=string.indexOf(':');

                if ( colon >= 0 ) {

                    return string.substring(colon+1);

                } else {

                    return string;

                }

            }

        }

    }


    /**
     * Generates a random UUID.
     *
     * @return a random
     *         <a href="https://en.wikipedia.org/wiki/Universally_unique_identifier#Version_4_(random)">Version 4
     *         UUID</a> generated using a cryptographically strong pseudo random number generator
     */
    public static String uuid() {
        return randomUUID().toString();
    }

    /**
     * Generates a name-based UUID.
     *
     * @param text the content to be hashed in the generated UUID
     *
     * @return a name-based <a href=
     *         "https://en.wikipedia.org/wiki/Universally_unique_identifier#Versions_3_and_5_(namespace_name-based)"
     *         >Version 3 UUID</a> based on {@code text}
     *
     * @throws NullPointerException if {@code text} is null
     */
    public static String uuid(final String text) {

        if ( text == null ) {
            throw new NullPointerException("null text");
        }

        return uuid(text.getBytes(UTF_8));
    }

    /**
     * Generates a name-based UUID.
     *
     * @param data the content to be hashed in the generated UUID
     *
     * @return a name-based <a
     *         href="https://en.wikipedia.org/wiki/Universally_unique_identifier#Versions_3_and_5_(namespace_name-based)"
     *         >Version 3 UUID</a> based on {@code data}
     *
     * @throws NullPointerException if {@code data} is null
     */
    public static String uuid(final byte[] data) {

        if ( data == null ) {
            throw new NullPointerException("null data");
        }

        return nameUUIDFromBytes(data).toString();
    }


    //̸/////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private URIs() { }

}
