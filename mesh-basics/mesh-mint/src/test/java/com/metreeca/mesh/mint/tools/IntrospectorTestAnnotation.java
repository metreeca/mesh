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

package com.metreeca.mesh.mint.tools;

import com.metreeca.mesh.meta.jsonld.Forward;
import com.metreeca.mesh.meta.jsonld.Frame;
import com.metreeca.mesh.meta.jsonld.Namespace;
import com.metreeca.mesh.meta.shacl.*;
import com.metreeca.mesh.mint.ast.Annotation;
import com.metreeca.mesh.mint.ast.Clazz;
import com.metreeca.mesh.mint.ast.Method;
import com.metreeca.mesh.mint.tools.annotation.PrecompiledAliases;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static com.metreeca.mesh.mint.ast.Annotation.PREFIX;
import static com.metreeca.mesh.mint.ast.Annotation.VALUE;
import static com.metreeca.mesh.mint.tools.IntrospectorTest.with;

import static org.assertj.core.api.Assertions.assertThat;

final class IntrospectorTestAnnotation {

    record Meta(String type, Map<String, Object> args, String source) {

        Meta(final Class<?> type, final Map<String, Object> args, final String source) {
            this(type.getCanonicalName(), args, source);
        }

        Meta(final Annotation annotation) {
            this(annotation.type(), annotation.args(), annotation.source());
        }
    }


    private static Stream<Meta> clazz(final Clazz clazz) {
        return clazz.lineage()
                .flatMap(Clazz::annotations)
                .map(Meta::new);
    }

    private static Stream<Meta> methods(final Clazz clazz) {
        return clazz.lineage()
                .flatMap(Clazz::methods)
                .flatMap(Method::annotations)
                .map(Meta::new);
    }


    //̸/////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Test void testCollectAnnotations() {
        with("annotation.CollectAnnotations", (introspector, type) -> {

            final Clazz clazz=introspector.clazz(type);

            assertThat(clazz(clazz)).containsExactly(

                    new Meta(
                            Namespace.class,
                            Map.of(PREFIX, "y", VALUE, "https://example.com/y"),
                            "CollectAnnotationsBase"
                    ),

                    new Meta(
                            Frame.class,
                            Map.of(),
                            "CollectAnnotations"
                    ),

                    new Meta(
                            Namespace.class,
                            Map.of(PREFIX, "x", VALUE, "https://example.com/x"),
                            "CollectAnnotations"
                    )
            );

            assertThat(methods(clazz)).containsExactly(

                    new Meta(
                            Forward.class,
                            Map.of(VALUE, "base"),
                            "CollectAnnotationsBase.value()"
                    ),

                    new Meta(
                            Forward.class,
                            Map.of(VALUE, "method"),
                            "CollectAnnotations.value()"
                    )

            );

        });
    }

    @Test void testCollectRepeatableAnnotations() {
        with("annotation.CollectRepeatableAnnotations", (introspector, type) -> {

            final Clazz clazz=introspector.clazz(type);

            assertThat(clazz(clazz)).containsExactly(

                    new Meta(
                            Frame.class,
                            Map.of(),
                            "CollectRepeatableAnnotations"
                    ),

                    new Meta(
                            Namespace.class,
                            Map.of(PREFIX, "x", VALUE, "https://example.com/x"),
                            "CollectRepeatableAnnotations"
                    ),

                    new Meta(
                            Namespace.class,
                            Map.of(PREFIX, "y", VALUE, "https://example.com/y"),
                            "CollectRepeatableAnnotations"
                    ),

                    new Meta(
                            Constraint.class,
                            Map.of(VALUE, "com.metreeca.mesh.mint.tools.annotation.CollectRepeatableAnnotations.C1"),
                            "CollectRepeatableAnnotations"
                    ),

                    new Meta(
                            Constraint.class,
                            Map.of(VALUE, "com.metreeca.mesh.mint.tools.annotation.CollectRepeatableAnnotations.C2"),
                            "CollectRepeatableAnnotations"
                    )

            );

        });
    }

    @Test void testCollectTypedArguments() {
        with("annotation.CollectTypedArguments", (introspector, type) -> {

            final Clazz clazz=introspector.clazz(type);

            assertThat(clazz(clazz)).containsExactly(
                    new Meta(
                            Frame.class,
                            Map.of(),
                            "CollectTypedArguments"
                    ),
                    new Meta(
                            Constraint.class,
                            Map.of(VALUE, "com.metreeca.mesh.mint.tools.annotation.CollectTypedArguments.Constraint"),
                            "CollectTypedArguments"
                    )
            );

            assertThat(methods(clazz)).containsExactly(

                    new Meta(Pattern.class,
                            Map.of(VALUE, "pattern"),
                            "CollectTypedArguments.value()"
                    ),

                    new Meta(
                            HasValue.class,
                            Map.of(VALUE, List.of("x", "y", "z")),
                            "CollectTypedArguments.value()"
                    ),

                    new Meta(
                            MinCount.class,
                            Map.of(VALUE, 10),
                            "CollectTypedArguments.value()"
                    ));

        });
    }


    @Test void testExpandSourceAliases() {
        with("annotation.ExpandSourceAliases", (introspector, type) -> {

            assertThat(introspector.unalias(new Annotation(
                    "com.metreeca.mesh.mint.tools.annotation.ExpandSourceAliases",
                    Map.of(),
                    ""
            )).map(Meta::new)).contains(
                    new Meta(MinCount.class, Map.of(VALUE, 1), ""),
                    new Meta(MaxCount.class, Map.of(VALUE, 10), ""),
                    new Meta(Constraint.class, Map.of(VALUE, "com.metreeca.mesh.mint.tools.annotation.ExpandSourceAliases.C1"), ""),
                    new Meta(Constraint.class, Map.of(VALUE, "com.metreeca.mesh.mint.tools.annotation.ExpandSourceAliases.C2"), "")
            );

        });
    }

    @Test void testExpandPrecompiledAliases() {
        with("annotation.ExpandPrecompiledAliases", (introspector, type) -> {

            assertThat(introspector.unalias(

                    new Annotation(PrecompiledAliases.class)

            ).map(Meta::new)).contains(
                    new Meta(MinCount.class, Map.of(VALUE, 1), ""),
                    new Meta(MaxCount.class, Map.of(VALUE, 10), ""),
                    new Meta(Constraint.class, Map.of(VALUE, PrecompiledAliases.C1.class.getCanonicalName()), ""),
                    new Meta(Constraint.class, Map.of(VALUE, PrecompiledAliases.C2.class.getCanonicalName()), "")
            );

        });
    }

}
