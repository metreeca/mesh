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

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URLEncoder;
import java.util.*;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.lang.Character.isWhitespace;
import static java.lang.String.format;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Arrays.asList;
import static java.util.Objects.requireNonNull;

public final class Strings {

    private static final Pattern PLACEHOLDER_PATTERN=Pattern.compile(
            "(?<modifier>[%#])?(?<placeholder>\\{(?<name>\\w+)})"
    );


    public static Processor pipe(final Processor... processors) {

        if ( processors == null ) {
            throw new NullPointerException("null processors");
        }

        return pipe(asList(processors));
    }

    public static Processor pipe(final Collection<Processor> processors) {

        if ( processors == null ) {
            throw new NullPointerException("null processors");
        }

        return processors.stream()

                .peek(processor -> {

                    if ( processor == null ) {
                        throw new NullPointerException("null processor");
                    }

                })

                .reduce((x, y) -> w ->
                        requireNonNull(x.process(
                                requireNonNull(y.process(w), "null processed writable")
                        ), "null processed writable")
                )

                .orElse(w -> w);
    }


    public static String trim(final String string) { return trim(string, 0); }

    public static String trim(final String string, final int spacing) {

        if ( string == null ) {
            throw new NullPointerException("null string");
        }

        return pipe(writable -> trim(writable, spacing)).process(string);
    }

    public static Writable trim(final Writable writable, final int spacing) {

        if ( writable == null ) {
            throw new NullPointerException("null sink");
        }

        if ( spacing < 0 ) {
            throw new IllegalArgumentException(format("negative spacing <%d>", spacing));
        }

        return new Writable() {

            private boolean sot=true;
            private boolean sol=true;

            private int newlines;

            private final StringBuilder spaces=new StringBuilder(10);

            @Override public void write(final char c) throws IOException {

                if ( sot ) {

                    if ( !isWhitespace(c) ) {

                        sot=false;
                        sol=false;

                        writable.write(c);

                    }

                } else {

                    if ( c == '\n' ) {

                        if ( newlines <= spacing ) { ++newlines; }

                        sol=true;

                        spaces.setLength(0);

                    } else if ( isWhitespace(c) ) {

                        spaces.append(c);

                    } else {

                        for (int i=newlines; i > 0; --i) { writable.write('\n'); }

                        if ( !sol ) { writable.write(spaces); }

                        writable.write(c);

                        sol=false;

                        newlines=0;
                        spaces.setLength(0);

                    }

                }

            }
        };
    }


    public static String fold(final String string) { return fold(string, 0); }

    public static String fold(final String string, final int spacing) {

        if ( string == null ) {
            throw new NullPointerException("null string");
        }

        return pipe(writable -> fold(writable, spacing)).process(string);
    }

    public static Writable fold(final Writable writable, final int spacing) {

        if ( writable == null ) {
            throw new NullPointerException("null sink");
        }

        if ( spacing < 0 ) {
            throw new IllegalArgumentException(format("negative spacing <%d>", spacing));
        }

        return new Writable() {

            private int newlines;

            private final StringBuilder spaces=new StringBuilder(10);

            @Override public void write(final char c) throws IOException {

                if ( c == '\n' ) {

                    if ( newlines <= spacing ) { ++newlines; }

                    spaces.setLength(0);

                } else if ( isWhitespace(c) ) {

                    spaces.append(c);

                } else {

                    for (int i=newlines; i > 0; --i) { writable.write('\n'); }

                    writable.write(spaces);
                    writable.write(c);

                    newlines=0;
                    spaces.setLength(0);

                }


            }
        };
    }


    public static String prettify(final String string) {

        if ( string == null ) {
            throw new NullPointerException("null string");
        }

        return prettify(string, 4);
    }

    public static String prettify(final String string, final int indent) {

        if ( string == null ) {
            throw new NullPointerException("null string");
        }

        return pipe(writable -> prettify(writable, indent)).process(string);
    }

    public static Writable prettify(final Writable writable, final int indent) {

        if ( writable == null ) {
            throw new NullPointerException("null writable");
        }

        if ( indent < 0 ) {
            throw new IllegalArgumentException(format("negative indent <%d>", indent));
        }

        return new Writable() {

            private boolean sob;
            private boolean sol;

            private int level;
            private int leading;

            @Override public void write(final char c) throws IOException {
                if ( c == '{' || c == '[' || c == '(' || c == '‹' ) {

                    if ( sob ) { space(); }
                    if ( sol ) { indent(); }

                    level++;

                    if ( c != '‹' ) { writable.write(c); }

                    sob=true;
                    sol=false;
                    leading=0;

                } else if ( c == '}' || c == ']' || c == ')' || c == '›' ) {

                    level--;

                    if ( !sob && sol ) { indent(); }

                    if ( c != '›' ) { writable.write(c); }

                    sob=false;
                    sol=false;

                    leading=0;

                } else if ( c == '\n' ) {

                    if ( sob ) { ++leading; } else { writable.write('\n'); }

                    sol=true;

                } else if ( c == ' ' || c == '\t' ) {

                    if ( !sob && !sol ) { writable.write(c); }

                } else {

                    if ( sob ) { space(); }
                    if ( sol ) { indent(); }

                    writable.write(c);

                    sob=false;
                    sol=false;

                    leading=0;

                }
            }


            private void space() throws IOException {
                for (int i=leading; i > 0; --i) { writable.write('\n'); }
            }

            private void indent() throws IOException {
                for (int i=indent*level; i > 0; --i) { writable.write(' '); }
            }

        };
    }


    //̸/////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Quotes and escapes content.
     *
     * @param string the string to be quoted
     *
     * @return a copy of {@code string} surrounded by single quotes and with control characters replaced by escape
     *         control sequences; the quoted string may be safely embedded within Java/JSON strings
     *
     * @throws NullPointerException if {@code string} is null
     */
    public static String quote(final String string) {

        if ( string == null ) {
            throw new NullPointerException("null string");
        }

        return quote(string, '"');
    }

    /**
     * Quotes and escapes content.
     *
     * @param string the string to be quoted
     * @param quote  the quote character
     *
     * @return a copy of {@code string} surrounded by single quotes and with control characters replaced by escape
     *         control sequences; the quoted string may be safely embedded within Java/JSON strings
     *
     * @throws NullPointerException if {@code string} is null
     */
    public static String quote(final String string, final char quote) { // !!! streaming / factor with JSONEncoder

        if ( string == null ) {
            throw new NullPointerException("null string");
        }

        final StringBuilder builder=new StringBuilder(string.length()+string.length()/10);

        builder.append(quote);

        for (int i=0, n=string.length(); i < n; ++i) {

            final char c=string.charAt(i);

            switch ( c ) {
                case '\\':
                    builder.append("\\\\");
                    break;
                case '\'':
                    builder.append(c == quote ? "\\'" : c);
                    break;
                case '\"':
                    builder.append(c == quote ? "\\\"" : c);
                    break;
                case '\r':
                    builder.append("\\r");
                    break;
                case '\n':
                    builder.append("\\n");
                    break;
                case '\t':
                    builder.append("\\t");
                    break;
                default:
                    builder.append(c);
                    break;
            }
        }

        builder.append(quote);

        return builder.toString();

    }


    @SafeVarargs
    public static String fill(final String template, final Map.Entry<String, String>... variables) {

        if ( template == null ) {
            throw new NullPointerException("null template");
        }

        if ( variables == null || Arrays.stream(variables).anyMatch(Objects::isNull) ) {
            throw new NullPointerException("null variables");
        }

        return fill(template, Map.ofEntries(variables)::get);
    }

    public static String fill(final String template, final Map<String, String> variables) {

        if ( template == null ) {
            throw new NullPointerException("null template");
        }

        if ( variables == null ) {
            throw new NullPointerException("null variables");
        }

        return fill(template, variables::get);
    }

    /**
     * Fills out a template.
     */
    public static String fill(final String template, final Function<String, String> resolver) {

        if ( template == null ) {
            throw new NullPointerException("null template");
        }

        if ( resolver == null ) {
            throw new NullPointerException("null resolver");
        }

        final StringBuilder builder=new StringBuilder(template.length());
        final Matcher matcher=PLACEHOLDER_PATTERN.matcher(template);

        int index=0;

        while ( matcher.find() ) {

            final String modifier=matcher.group("modifier");
            final String placeholder=matcher.group("placeholder");
            final String name=matcher.group("name");

            builder.append(template, index, matcher.start()).append(
                    "#".equals(modifier) ? placeholder : Optional.ofNullable(resolver.apply(name))
                            .map(value -> "%".equals(modifier) ? URLEncoder.encode(value, UTF_8) : value)
                            .orElseThrow(() -> new IllegalArgumentException(format("undefined variable {%s}", name)))
            );

            index=matcher.end();
        }

        builder.append(template.substring(index));

        return builder.toString();
    }


    //̸/////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private Strings() { }


    //̸/////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @FunctionalInterface
    public static interface Processor {

        Writable process(final Writable writable);

        default Writable process(final Appendable appendable) {

            if ( appendable == null ) {
                throw new NullPointerException("null appendable");
            }

            return process(new Writable() {

                @Override public void write(final char c) throws IOException {
                    appendable.append(c);
                }

                @Override public void write(final CharSequence sequence) throws IOException {
                    appendable.append(sequence);
                }

            });
        }

        default String process(final String string) {

            if ( string == null ) {
                throw new NullPointerException("null string");
            }

            try {

                final StringBuilder builder=new StringBuilder(string.length());

                final Writable process=process(builder);

                process.write(string);
                process.close();

                return builder.toString();

            } catch ( final IOException e ) {
                throw new UncheckedIOException(e);
            }
        }

    }

    @FunctionalInterface
    public static interface Writable {

        void write(final char c) throws IOException;

        default void write(final CharSequence sequence) throws IOException {

            if ( sequence == null ) {
                throw new NullPointerException("null sequence");
            }

            for (int i=0, n=sequence.length(); i < n; ++i) {
                write(sequence.charAt(i));
            }
        }

        default void close() throws IOException { }

    }

}
