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

package com.metreeca.mesh.json;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import static com.metreeca.shim.Collections.list;

import static java.lang.String.format;

/**
 * JSON streaming writer for low-level JSON output generation.
 *
 * <p>Provides push-based JSON writing with support for pretty-printing
 * and circular reference detection. Maintains proper indentation and
 * structural validation during output generation.</p>
 */
final class JSONWriter {

    private final int indent;

    private final Appendable target;

    private final Collection<Object> visited=new HashSet<>();
    private final List<Boolean> stack=new ArrayList<>(list(true));


    JSONWriter(final JSONCodec codec, final Appendable target) {
        this.indent=codec.indent();
        this.target=target;
    }


    /**
     * Checks if the given object has been visited to detect cycles.
     *
     * @param value the object to check
     *
     * @return {@code true} if this is the first visit to the object
     *
     * @throws NullPointerException if {@code value} is {@code null}
     */
    boolean visit(final Object value) {

        if ( value == null ) {
            throw new NullPointerException("null value");
        }

        return visited.add(value);
    }


    /**
     * Writes object opening or closing brace.
     *
     * @param open if {@code true}, write opening brace; if {@code false}, write closing brace
     *
     * @return this writer instance for method chaining
     *
     * @throws IOException if an I/O error occurs during writing
     */
    JSONWriter object(final boolean open) throws IOException {

        if ( open ) { open('{'); } else { close('}'); }

        return this;
    }

    /**
     * Writes array opening or closing bracket.
     *
     * @param open if {@code true}, write opening bracket; if {@code false}, write closing bracket
     *
     * @return this writer instance for method chaining
     *
     * @throws IOException if an I/O error occurs during writing
     */
    JSONWriter array(final boolean open) throws IOException {

        if ( open ) { open('['); } else { close(']'); }

        return this;
    }


    /**
     * Writes a colon separator with appropriate spacing.
     *
     * @return this writer instance for method chaining
     *
     * @throws IOException if an I/O error occurs during writing
     */
    JSONWriter colon() throws IOException {

        if ( indent > 0 ) {

            write(": ");

        } else {

            write(':');

        }

        return this;
    }

    /**
     * Writes a comma separator with proper indentation.
     *
     * @return this writer instance for method chaining
     *
     * @throws IOException if an I/O error occurs during writing
     */
    JSONWriter comma() throws IOException {

        if ( stack.getFirst() ) {
            write(',');
            indent();
        }

        return this;
    }


    /**
     * Writes a literal value without quotes.
     *
     * @param literal the literal value to write
     *
     * @return this writer instance for method chaining
     *
     * @throws IOException if an I/O error occurs during writing
     */
    JSONWriter literal(final String literal) throws IOException {

        if ( !stack.set(0, true) ) { indent(); }

        write(literal);

        return this;

    }

    /**
     * Writes a quoted string with proper escaping.
     *
     * @param string the string value to write
     *
     * @return this writer instance for method chaining
     *
     * @throws IOException if an I/O error occurs during writing
     */
    JSONWriter string(final String string) throws IOException {

        if ( !stack.set(0, true) ) { indent(); }

        write('"');

        for (int i=0, n=string.length(); i < n; ++i) {

            final char c=string.charAt(i);

            switch ( c ) {

                case '\u0000':
                case '\u0001':
                case '\u0002':
                case '\u0003':
                case '\u0004':
                case '\u0005':
                case '\u0006':
                case '\u0007':

                case '\u000B':

                case '\u000E':
                case '\u000F':
                case '\u0010':
                case '\u0011':
                case '\u0012':
                case '\u0013':
                case '\u0014':
                case '\u0015':
                case '\u0016':
                case '\u0017':
                case '\u0018':
                case '\u0019':
                case '\u001A':
                case '\u001B':
                case '\u001C':
                case '\u001D':
                case '\u001E':
                case '\u001F':

                    write(format("\\u%04X", (int)c));

                    break;

                case '\b':

                    write("\\b");

                    break;

                case '\f':

                    write("\\f");

                    break;

                case '\n':

                    write("\\n");

                    break;

                case '\r':

                    write("\\r");

                    break;

                case '\t':

                    write("\\t");

                    break;

                case '"':

                    write("\\\"");

                    break;

                case '\\':

                    write("\\\\");

                    break;

                default:

                    write(c);

                    break;

            }
        }

        write('"');

        return this;
    }


    @SuppressWarnings("unchecked")
    JSONWriter value(final Object object) throws IOException {

        throw new UnsupportedOperationException(";( be implemented"); // !!!

        // return this;
    }


    //̸/////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private void open(final char c) throws IOException {

        if ( !stack.set(0, true) ) { indent(); }

        stack.add(0, false);

        write(c);
    }

    private void close(final char c) throws IOException {

        if ( stack.remove(0) ) {
            indent();
        }

        write(c);
    }


    private void indent() throws IOException {
        if ( indent > 0 ) {

            write('\n');

            for (int spaces=indent*(stack.size()-1); spaces > 0; --spaces) {
                write(' ');
            }

        }
    }


    private void write(final char c) throws IOException {
        target.append(c);
    }

    private void write(final String s) throws IOException {
        target.append(s);
    }

}
