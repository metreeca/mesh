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

import com.metreeca.mesh.mint.ast.Clazz;
import com.metreeca.mesh.mint.ast.Method;

import org.junit.jupiter.api.Test;

import static com.metreeca.mesh.mint.tools.IntrospectorTest.with;

import static org.assertj.core.api.Assertions.assertThat;

final class IntrospectorTestGenerics {

    @Test void testSimpleLevel0() {
        with("generics.SimpleLevel0", (introspector, type) ->
                assertThat(introspector.clazz(type).lineage().flatMap(Clazz::methods)
                        .filter(method -> method.name().equals("items"))
                        .map(Method::generic)
                        .findFirst()
                ).contains("List<String>")
        );
    }

    @Test void testSimpleLevel1() {
        with("generics.SimpleLevel1", (introspector, type) ->
                assertThat(introspector.clazz(type).lineage().flatMap(Clazz::methods)
                        .filter(method -> method.name().equals("items"))
                        .map(Method::generic)
                        .findFirst()
                ).contains("List<String>")
        );
    }

    @Test void testSimpleLevel2() {
        with("generics.SimpleLevel2", (introspector, type) ->
                assertThat(introspector.clazz(type).lineage().flatMap(Clazz::methods)
                        .filter(method -> method.name().equals("items"))
                        .map(Method::generic)
                        .findFirst()
                ).contains("List<String>")
        );
    }

}
