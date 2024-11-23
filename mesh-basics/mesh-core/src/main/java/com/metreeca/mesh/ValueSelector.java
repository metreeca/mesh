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

package com.metreeca.mesh;

import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.metreeca.mesh.Value.Nil;
import static com.metreeca.mesh.Value.array;
import static com.metreeca.shim.Collections.list;

import static java.lang.Integer.parseInt;
import static java.lang.String.format;
import static java.util.function.Predicate.not;

final class ValueSelector {

    private static final Pattern DOT_PATTERN=Pattern.compile("(?:^|\\.)(?<dot>\\w+)");
    private static final Pattern NAME_PATTERN=Pattern.compile("\\['(?<name>(?:[^']|\\\\.)*)']");
    private static final Pattern INDEX_PATTERN=Pattern.compile("\\[(?<index>\\d+)]");
    private static final Pattern WILDCARD_PATTERN=Pattern.compile("(?:^|\\.)\\*|\\[\\*]");

    private static final Pattern STEP_PATTERN=Pattern.compile("(?:^\\$)?(?:(?:%s)|(?:%s)|(?:%s)|(?:%s))".formatted(
            DOT_PATTERN, NAME_PATTERN, INDEX_PATTERN, WILDCARD_PATTERN
    ));

    private static final Pattern ESCAPE_PATTERN=Pattern.compile("\\\\(?<escape>.)");


    private static String unescape(final String name) {
        return ESCAPE_PATTERN.matcher(name).replaceAll("${escape}");
    }


    //̸/////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    static Value select(final Value value, final String path) {
        if ( path.isEmpty() || path.equals("$") ) { return value; } else {

            Value selection=value;

            int next=0;

            final int length=path.length();

            for (
                    final Matcher matcher=STEP_PATTERN.matcher(path).useAnchoringBounds(false);
                    matcher.lookingAt();
                    matcher.region(next, length)
            ) {

                selection=matcher.group("dot") instanceof final String dot ? step(selection, unescape(dot))
                        : matcher.group("name") instanceof final String name ? step(selection, unescape(name))
                        : matcher.group("index") instanceof final String index ? step(selection, parseInt(index))
                        : selection.get();

                next=matcher.end();
            }

            if ( next < length ) {
                throw new IllegalArgumentException(format("malformed path <%s>", path));
            }

            return selection;

        }
    }


    //̸/////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private static Value step(final Value value, final String name) {
        return value.accept(new Value.Visitor<>() {

            @Override public Value visit(final Value host, final List<Value> values) {
                return array(list(values.stream().map(v -> v.get(name)).filter(not(Value::isEmpty))));
            }

            @Override public Value visit(final Value host, final Map<String, Value> fields) {
                return host.get(name);
            }

            @Override public Value visit(final Value host, final Object object) {
                return Nil();
            }

        });
    }

    private static Value step(final Value value, final int index) {
        return value.accept(new Value.Visitor<>() {

            @Override public Value visit(final Value host, final List<Value> values) {
                return host.get(index);
            }

            @Override public Value visit(final Value host, final Object object) {
                return Nil();
            }

        });
    }


    //̸/////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private ValueSelector() { }

}
