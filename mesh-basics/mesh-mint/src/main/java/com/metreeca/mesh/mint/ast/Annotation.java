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

package com.metreeca.mesh.mint.ast;

import com.metreeca.mesh.meta.Values;
import com.metreeca.shim.Collections;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.metreeca.mesh.mint.ast.Reference.simple;
import static com.metreeca.shim.Collections.map;

import static java.util.Objects.requireNonNull;


/**
 * Represents a Java annotation extracted from interface elements.
 *
 * <p>Provides a simplified model of Java annotations with typed accessor methods for argument values.
 * Tracks the source element (class or method) from which the annotation was extracted to support error reporting and
 * diagnostic information.</p>
 *
 * <p>The annotation model captures:</p>
 * <ul>
 *   <li>The fully qualified type name of the annotation</li>
 *   <li>The name-value pairs of annotation arguments</li>
 *   <li>The source reference for diagnostic purposes</li>
 * </ul>
 */
public final class Annotation {

    public static final String NAME="name";
    public static final String VALUE="value";
    public static final String PREFIX="prefix";
    public static final String IMPLIED="implied";
    public static final String FORWARD="forward";
    public static final String REVERSE="reverse";

    private static final String META=Values.class.getPackageName()+".";


    //̸/////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private final String type; // canonical name of annotation class
    private final Map<String, Object> args; // values are booleans, numbers, strings, class FQNs and list thereof
    private final String source; // reference to annotated element


    public Annotation(final Class<?> type) {
        this(
                requireNonNull(type).getCanonicalName(),
                map(),
                ""
        );
    }

    public Annotation(
            final Class<?> type,
            final Map<String, Object> args,
            final String source
    ) {
        this.type=requireNonNull(type, "null type").getCanonicalName();
        this.args=map(requireNonNull(args, "null args"));
        this.source=requireNonNull(source, "null source");
    }

    public Annotation(
            final String type,
            final Map<String, Object> args,
            final String source
    ) {
        this.type=requireNonNull(type);
        this.args=map(args);
        this.source=requireNonNull(source);
    }


    //̸/////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public boolean isIgnored() {
        return !type.startsWith(META) || is(Property.class); // @Property annotations are virtual (see Clazz.property())
    }

    public boolean is(final Class<?> clazz) {
        return type.equals(clazz.getCanonicalName());
    }


    public String type() { return type; }


    public Map<String, Object> args() { return args; }

    public Annotation args(final Map<String, Object> args) {
        return new Annotation(
                type,
                args,
                source
        );
    }


    public String source() { return source; }

    public Annotation source(final String source) {
        return new Annotation(
                type,
                args,
                source
        );
    }


    public int integer(final String name) {
        return Optional.ofNullable((Integer)args().get(name)).orElse(0);
    }

    public String string(final String name) {
        return Optional.ofNullable((String)args().get(name)).orElse("");
    }

    public List<?> list(final String name) {
        return Optional.ofNullable((List<?>)args().get(name)).orElseGet(Collections::list);
    }


    @Override public String toString() {
        return "@%s(%s) @ %s".formatted(simple(type), args, source);
    }

}
