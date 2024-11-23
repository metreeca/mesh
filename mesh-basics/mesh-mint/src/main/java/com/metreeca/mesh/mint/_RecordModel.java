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

package com.metreeca.mesh.mint;

import com.metreeca.mesh.shapes.Property;
import com.metreeca.mesh.shapes.Shape;

import java.net.URI;
import java.util.*;
import java.util.stream.Stream;

import static com.metreeca.mesh.mint._RecordGlass.canonical;
import static com.metreeca.mesh.util.Collections.set;

import static java.lang.Boolean.TRUE;

public final class _RecordModel {

    public static final String TEXT="Entry<Locale, String>";
    public static final String TEXTS="Map<Locale, String>";
    public static final String TEXTSETS="Map<Locale, Set<String>>";

    static final Set<String> TEXTUAL=set(TEXT, TEXTS, TEXTSETS);


    public static final String DATA="Entry<URI, String>";
    public static final String DATAS="Map<URI, String>";
    public static final String DATASETS="Map<URI, Set<String>>";

    public static final String SET=_RecordGlass.simple(Set.class);
    public static final String LIST=_RecordGlass.simple(List.class);
    public static final String COLLECTION=_RecordGlass.simple(Collection.class);


    public record Info(

            URI base,

            String packageName,
            String className,

            Set<Slot> slots,

            Shape shape,

            Set<String> constraints // canonical names of constraint functions (cannot instantiate at compile time)

    ) { }

    public record Slot(

            boolean dflt,

            boolean id,
            boolean type,
            boolean internal,

            String base,
            String item,
            String name,

            Property property

    ) {

        public boolean set() {
            return base.equals(SET);
        }

        public boolean list() {
            return base.equals(LIST);
        }

        public boolean collection() {
            return base.equals(SET) || base.equals(LIST) || base.equals(COLLECTION);
        }


        public String generic() {
            return Optional.ofNullable(item)
                    .map(i -> "%s<%s>".formatted(base, i))
                    .orElse(base);
        }

    }

    public record Meta(

            String type,
            Map<String, Object> args,

            String source

    ) {

        public Meta(final Class<?> clazz, final Map<String, Object> args, final String source) {
            this(canonical(clazz), args, source);
        }


        public boolean is(final Class<?> clazz) {
            return type().equals(canonical(clazz));
        }


        boolean flag(final String name) {
            return TRUE.equals(args().get(name));
        }

        Integer integer(final String name) {
            return (Integer)args().get(name);
        }

        String string(final String name) {
            return (String)args().get(name);
        }

        List<?> list(final String name) {
            return (List<?>)args().get(name);
        }

    }


    public record Tree(String name, List<Meta> metas, List<Tree> ancestors) {

        Stream<Meta> stream() {
            return Stream.concat(metas.stream(), ancestors.stream().flatMap(Tree::stream));
        }

    }

    public record Tray<T>(T value, String source) {

        @Override
        public String toString() {
            return "<%s> @ <%s>".formatted(value, source);
        }

    }


    //̸/////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private _RecordModel() { }

}
