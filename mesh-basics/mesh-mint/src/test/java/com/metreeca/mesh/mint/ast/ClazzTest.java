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

import com.metreeca.mesh.meta.jsonld.*;
import com.metreeca.mesh.meta.jsonld.Class;
import com.metreeca.mesh.meta.shacl.Constraint;
import com.metreeca.mesh.meta.shacl.MinExclusive;
import com.metreeca.mesh.mint.FrameException;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.Optional;

import static com.metreeca.mesh.mint.ast.Annotation.PREFIX;
import static com.metreeca.mesh.mint.ast.Annotation.VALUE;
import static com.metreeca.mesh.mint.ast.Reference.simple;
import static com.metreeca.mesh.shapes.Type.type;
import static com.metreeca.shim.Collections.list;
import static com.metreeca.shim.URIs.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

@Nested
final class ClazzTest {

    @Nested
    final class BaseTest {

        @Test void testProvideDefaultBase() {
            assertThat(new Clazz("").base())
                    .isEqualTo(base());
        }

        @Test void testIdentifyBase() {
            assertThat(new Clazz("")
                    .annotations(list(new Annotation(Base.class).args(Map.of(VALUE, "https://example.org/"))))
                    .base()
            ).isEqualTo(
                    uri("https://example.org/")
            );
        }

        @Test void testOverrideConflictingBases() {
            assertThat(new Clazz("")
                    .annotations(list(
                            new Annotation(Base.class).args(Map.of(VALUE, "https://example.org/x"))
                    ))
                    .parents(list(
                            new Clazz("").annotations(list(
                                    new Annotation(Base.class).args(Map.of(VALUE, "https://example.org/y"))
                            )),
                            new Clazz("").annotations(list(
                                    new Annotation(Base.class).args(Map.of(VALUE, "https://example.org/z"))
                            ))
                    ))
                    .base()
            ).isEqualTo(
                    uri("https://example.org/x")
            );
        }

        @Test void testReportMalformedBases() {
            assertThatExceptionOfType(FrameException.class).isThrownBy(() -> new Clazz("")
                    .annotations(list(new Annotation(Base.class).args(Map.of(VALUE, "not a URI"))))
                    .base()
            );
        }

        @Test void testReportRelativeBases() {
            assertThatExceptionOfType(FrameException.class).isThrownBy(() -> new Clazz("")
                    .annotations(list(new Annotation(Base.class).args(Map.of(VALUE, "relative"))))
                    .base()
            );
        }

        @Test void testReportConflictingBases() {
            assertThatExceptionOfType(FrameException.class).isThrownBy(() -> new Clazz("")
                    .parents(list(
                            new Clazz("").annotations(list(
                                    new Annotation(Base.class).args(Map.of(VALUE, "https://example.org/x"))
                            )),
                            new Clazz("").annotations(list(
                                    new Annotation(Base.class).args(Map.of(VALUE, "https://example.org/y"))
                            ))
                    ))
                    .base()
            );
        }

    }

    @Nested
    final class NamespacesTest {

        @Test void testCollectNamespaces() {
            assertThat(new Clazz("")
                    .annotations(list(new Annotation(Namespace.class)
                            .args(Map.of(PREFIX, "p", VALUE, "a"))
                    ))
                    .parents(list(new Clazz("")
                            .annotations(list(new Annotation(Namespace.class)
                                    .args(Map.of(PREFIX, "q", VALUE, "b"))
                            ))
                    ))
                    .namespaces()
            ).isEqualTo(Map.of(

                    "", term(""),
                    "p", base().resolve("a"),
                    "q", base().resolve("b")

            ));
        }

        @Test void testHandleOptionalPrefixes() {
            assertThat(new Clazz("")
                    .annotations(list(new Annotation(Namespace.class)
                            .args(Map.of(PREFIX, "p", VALUE, "c"))
                    ))
                    .parents(list(
                            new Clazz("")
                                    .annotations(list(new Annotation(Namespace.class)
                                            .args(Map.of(PREFIX, "p", VALUE, "a"))
                                    )),
                            new Clazz("")
                                    .annotations(list(new Annotation(Namespace.class)
                                            .args(Map.of(PREFIX, "p", VALUE, "b"))
                                    ))
                    ))
                    .namespaces()
            ).isEqualTo(Map.of(

                    "", term(""),
                    "p", base().resolve("c")

            ));
        }

        @Test void testReportConflictingNamespaces() {

            assertThatExceptionOfType(FrameException.class).isThrownBy(() -> new Clazz("")
                    .annotations(list(
                            new Annotation(Namespace.class)
                                    .args(Map.of(PREFIX, "p", VALUE, "a")),
                            new Annotation(Namespace.class)
                                    .args(Map.of(PREFIX, "p", VALUE, "b"))
                    ))
                    .namespaces()
            );

            assertThatExceptionOfType(FrameException.class).isThrownBy(() -> new Clazz("")
                    .parents(list(
                            new Clazz("")
                                    .annotations(list(new Annotation(Namespace.class)
                                            .args(Map.of(PREFIX, "p", VALUE, "a"))
                                    )),
                            new Clazz("")
                                    .annotations(list(new Annotation(Namespace.class)
                                            .args(Map.of(PREFIX, "p", VALUE, "b"))
                                    ))
                    ))
                    .namespaces()
            );
        }


        @Test void testResolveDefaultNamespaces() {

            assertThat(new Clazz("")
                    .annotations(list(new Annotation(Namespace.class)
                            .args(Map.of(PREFIX, "", VALUE, "[x]"))
                    ))
                    .parents(list(
                            new Clazz("")
                                    .annotations(list(new Annotation(Namespace.class)
                                            .args(Map.of(PREFIX, "[x]", VALUE, "a"))
                                    )),
                            new Clazz("")
                                    .annotations(list(new Annotation(Namespace.class)
                                            .args(Map.of(PREFIX, "[y]", VALUE, "b"))
                                    ))
                    ))
                    .namespaces()
            ).isEqualTo(Map.of(

                    "", base().resolve("a"),
                    "x", base().resolve("a"),
                    "y", base().resolve("b")

            ));
        }

        @Test void testReportUndefinedDefaultNamespaces() {
            assertThatExceptionOfType(FrameException.class).isThrownBy(() -> new Clazz("")
                    .annotations(list(new Annotation(Namespace.class)
                            .args(Map.of(PREFIX, "", VALUE, "[x]"))
                    ))
                    .namespaces()
            );
        }

    }

    @Nested
    final class ResolveTest {

        private static final Clazz clazz=new Clazz("").annotations(list(
                new Annotation(Base.class).args(Map.of(VALUE, "https://example.com/")),
                new Annotation(Namespace.class).args(Map.of(PREFIX, "", VALUE, "#")),
                new Annotation(Namespace.class).args(Map.of(PREFIX, "com", VALUE, "#")),
                new Annotation(Namespace.class).args(Map.of(PREFIX, "net", VALUE, "https://example.net/"))
        ));

        private URI resolve(final String curie, final String fallback) {
            try {

                return clazz.resolve(curie, fallback);

            } catch ( final URISyntaxException e ) {

                throw new RuntimeException(e);

            }
        }


        @Test void testResolveCURIEs() {
            assertThat(resolve("com:name", "fallback")).isEqualTo(uri("https://example.com/#name"));
            assertThat(resolve("net:name", "fallback")).isEqualTo(uri("https://example.net/name"));
        }

        @Test void testResolvePrefixCURIEs() {
            assertThat(resolve("com:", "fallback")).isEqualTo(uri("https://example.com/#fallback"));
        }

        @Test void testResolveNameCURIEs() {
            assertThat(resolve(":name", "fallback")).isEqualTo(uri("https://example.com/#name"));
        }

        @Test void testResolveFallbacks() {
            assertThat(resolve("", "fallback")).isEqualTo(uri("https://example.com/#fallback"));
        }

        @Test void testReportMalformedCURIs() {
            assertThatExceptionOfType(URISyntaxException.class).isThrownBy(() ->
                    new Clazz("").resolve("not a CURIE", "fallback")
            );
        }


        @Test void testProvideFallbackNamespace() throws URISyntaxException {
            assertThat(new Clazz("").resolve("", "fallback")).isEqualTo(term("fallback"));
            assertThat(new Clazz("").resolve(":name", "fallback")).isEqualTo(term("name"));
        }

        @Test void testReportUndefinedNamespaces() throws URISyntaxException {
            assertThatExceptionOfType(URISyntaxException.class).isThrownBy(() ->
                    new Clazz("").resolve("undefined:name", "fallback")
            );
        }

    }

    @Nested
    final class FlattenedTest {

        @Test void testPreserveClassAnnotations() {
            assertThat(new Clazz("X")

                    .annotations(list(new Annotation(Virtual.class)))

                    .flattened()
                    .annotations()
                    .map(Annotation::type)

            ).containsExactly(
                    Virtual.class.getCanonicalName()
            );
        }

        @Test void testPreserveClassParents() {
            assertThat(new Clazz("X")

                    .parents(list(new Clazz("Y")
                            .annotations(list(new Annotation(Virtual.class))))
                    )

                    .flattened()
                    .parents()
                    .flatMap(Clazz::annotations)
                    .map(Annotation::type)

            ).containsExactly(
                    Virtual.class.getCanonicalName()
            );
        }

        @Test void testMigrateMethodsToVirtualClass() {

            final Clazz flattened=new Clazz("X")
                    .parents(list(new Clazz("Y")
                            .methods(list(new Method("value")
                                    .type("int")
                                    .annotations(list(new Annotation(MinExclusive.class).args(Map.of(VALUE, "100"))))
                            ))
                    ))
                    .flattened();


            assertThat(flattened.methods()).allSatisfy(method ->
                    assertThat(method.clazz()).isEqualTo(flattened)
            );

        }

        @Test void testPreserveAnnotationOrder() {
            assertThat(new Clazz("X")

                    .methods(list(new Method("value")
                            .type("int")
                            .annotations(list(new Annotation(MinExclusive.class).args(Map.of(VALUE, "10"))))
                    ))
                    .parents(list(new Clazz("Y")
                            .methods(list(new Method("value")
                                    .type("int")
                                    .annotations(list(new Annotation(MinExclusive.class).args(Map.of(VALUE, "100"))))
                            ))
                    ))

                    .flattened()
                    .methods()
                    .flatMap(Method::annotations)
                    .map(annotation -> annotation.string(VALUE))

            ).contains(
                    "100",
                    "10"
            );
        }


        @Test void testResolvePropertyURIsAgainstDeclaringClassNamespace() {
            assertThat(new Clazz("X")

                    .parents(list(new Clazz("Y")
                            .annotations(list(new Annotation(Namespace.class).args(Map.of(
                                    PREFIX, "",
                                    VALUE, "https://exmple.net/#"
                            ))))
                            .methods(list(new Method("y")
                                    .annotations(list(
                                            new Annotation(Forward.class).args(Map.of(VALUE, ":v"))
                                    ))
                            ))
                    ))

                    .flattened()
                    .methods()
                    .map(Method::forward)
                    .flatMap(Optional::stream)

            ).containsExactly(
                    "https://exmple.net/#v"
            );
        }

        @Test void testProvideDefaultPropertyURIWRTDeclaringClass() {
            assertThat(new Clazz("X")

                    .parents(list(new Clazz("Y")
                            .annotations(list(new Annotation(Namespace.class).args(Map.of(
                                    PREFIX, "",
                                    VALUE, "https://exmple.org/#"
                            ))))
                            .parents(list(new Clazz("Z")
                                    .annotations(list(new Annotation(Namespace.class).args(Map.of(
                                            PREFIX, "",
                                            VALUE, "https://exmple.net/#"
                                    ))))
                                    .methods(list(new Method("z")))
                            ))
                    ))

                    .flattened()
                    .methods()
                    .map(Method::forward)
                    .flatMap(Optional::stream)

            ).containsExactly(
                    "https://exmple.net/#z"
            );
        }

    }

    @Nested
    final class ShapeTest {

        @Test void testIdentifyVirtual() {

            assertThat(new Clazz("").virtual()).isFalse();

            assertThat(new Clazz("")
                    .annotations(list(
                            new Annotation(Virtual.class)
                    ))
                    .virtual()
            ).isTrue();

            assertThat(new Clazz("")
                    .parents(list(
                            new Clazz("").annotations(list(
                                    new Annotation(Virtual.class)
                            ))
                    ))
                    .virtual()
            ).isTrue();
        }


        @Test void testIdentifyIdMethods() {

            assertThat(new Clazz("X").id()).isEmpty();

            assertThat(new Clazz("X")
                    .methods(list(new Method("x")
                            .annotations(list(new Annotation(Id.class)))
                            .type(simple(URI.class))
                    ))
                    .id()
            ).contains(
                    "x"
            );

            assertThat(new Clazz("X")
                    .parents(list(new Clazz("Y")
                            .methods(list(new Method("y")
                                    .annotations(list(new Annotation(Id.class)))
                                    .type(simple(URI.class))
                            ))
                    ))
                    .id()
            ).contains(
                    "y"
            );

        }

        @Test void testReportMultipleIdMethods() {
            assertThatExceptionOfType(FrameException.class).isThrownBy(() -> new Clazz("X")
                    .parents(list(
                            new Clazz("Y")
                                    .methods(list(new Method("y")
                                            .annotations(list(new Annotation(Id.class)))
                                    )),
                            new Clazz("Z")
                                    .methods(list(new Method("z")
                                            .annotations(list(new Annotation(Id.class)))
                                    ))
                    ))
                    .id()
            );
        }


        @Test void testIdentifyTypeMethods() {

            assertThat(new Clazz("X").type()).isEmpty();

            assertThat(new Clazz("X")
                    .methods(list(new Method("x")
                            .annotations(list(new Annotation(Type.class)))
                            .type(simple(String.class))
                    ))
                    .type()
            ).contains(
                    "x"
            );

            assertThat(new Clazz("X")
                    .parents(list(new Clazz("Y")
                            .methods(list(new Method("y")
                                    .annotations(list(new Annotation(Type.class)))
                                    .type(simple(String.class))
                            ))
                    ))
                    .type()
            ).contains(
                    "y"
            );

        }

        @Test void testReportMultipleTypeMethods() {
            assertThatExceptionOfType(FrameException.class).isThrownBy(() -> new Clazz("X")
                    .parents(list(
                            new Clazz("Y")
                                    .methods(list(new Method("y")
                                            .annotations(list(new Annotation(Type.class)))
                                    )),
                            new Clazz("Z")
                                    .methods(list(new Method("z")
                                            .annotations(list(new Annotation(Type.class)))
                                    ))
                    ))
                    .type()
            );
        }


        @Test void testIdentifyClass() {

            assertThat(new Clazz("Name").clazz())
                    .isEmpty();

            assertThat(new Clazz("Name")
                    .annotations(list(new Annotation(Class.class).args(Map.of(VALUE, "explicit"))))
                    .clazz()
            ).contains(
                    type("Name", term("explicit"))
            );

        }

        @Test void testIdentifyClasses() {
            assertThat(new Clazz("X")
                    .annotations(list(
                            new Annotation(Class.class).args(Map.of(VALUE, "x"))
                    ))
                    .parents(list(
                            new Clazz("Y")
                                    .annotations(list(new Annotation(Class.class).args(Map.of(VALUE, "y")))),
                            new Clazz("Z")
                                    .annotations(list(new Annotation(Class.class).args(Map.of(VALUE, "z"))))
                    ))
                    .clazzes()
            ).isEqualTo(list(

                    type("X", term("x")),
                    type("Y", term("y")),
                    type("Z", term("z"))

            ));
        }

        @Test void testReportMultipleExplicitTypes() {
            assertThatExceptionOfType(FrameException.class).isThrownBy(() -> new Clazz("Name")
                    .annotations(list(
                            new Annotation(Class.class).args(Map.of(VALUE, "x")),
                            new Annotation(Class.class).args(Map.of(VALUE, "y"))
                    ))
                    .clazz());
        }


        @Test void testIdentifyConstraints() {

            assertThat(new Clazz("Name").constraints()).isEmpty();

            assertThat(new Clazz("Name")
                    .annotations(list(
                            new Annotation(Constraint.class).args(Map.of(VALUE, "com.example.C1")),
                            new Annotation(Constraint.class).args(Map.of(VALUE, "com.example.C2"))
                    ))
                    .constraints()
            ).containsExactly(
                    "com.example.C1",
                    "com.example.C2"
            );

        }

    }

}