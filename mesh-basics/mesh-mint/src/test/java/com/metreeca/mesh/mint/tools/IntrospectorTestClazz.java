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

package com.metreeca.mesh.mint.tools;

import com.metreeca.mesh.mint.FrameException;
import com.metreeca.mesh.mint.ast.Clazz;

import org.junit.jupiter.api.Test;

import javax.lang.model.element.TypeElement;

import static com.metreeca.mesh.mint.tools.IntrospectorTest.with;
import static com.metreeca.shim.Collections.entry;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;


final class IntrospectorTestClazz {

    @Test void testCollectInfo() {
        with("clazz.CollectInfo", (introspector, type) -> {

            assertThat(introspector.clazz(type).pkg()).isEqualTo("com.metreeca.mesh.mint.tools.clazz");
            assertThat(introspector.clazz(type).name()).isEqualTo("CollectInfo");

            assertThat(introspector.clazz(type).lineage().flatMap(Clazz::methods))
                    .map(method -> entry(method.generic(), method.name()))
                    .contains(
                            entry("URI", "id"),
                            entry("String", "label"),
                            entry("Set<String>", "values")
                    );

        });
    }


    @Test void testReportClasses() {
        with("clazz.ReportClasses", (introspector, type) ->
                assertThatExceptionOfType(FrameException.class).isThrownBy(() -> introspector.clazz(type))
        );
    }

    @Test void testReportMissingFrameAnnotation() {
        with("clazz.ReportMissingFrameAnnotation", (introspector, type) ->
                assertThatExceptionOfType(FrameException.class).isThrownBy(() -> introspector.clazz(type))
        );
    }

    @Test void testReportNestedInterfaces() {
        with("clazz.ReportNestedInterfaces", (introspector, type) ->
                assertThatExceptionOfType(FrameException.class).isThrownBy(() ->
                        introspector.clazz((TypeElement)type.getEnclosedElements().getFirst())
                )
        );
    }

    @Test void testReportGenericInterfaces() {
        with("clazz.ReportGenericInterfaces", (introspector, type) ->
                assertThatExceptionOfType(FrameException.class).isThrownBy(() -> introspector.clazz(type))
        );
    }

    @Test void testReportInternalAbstractUtilities() {
        with("clazz.ReportInternalAbstractUtilities", (introspector, type) -> {
            assertThatExceptionOfType(FrameException.class).isThrownBy(() -> introspector.clazz(type));
        });
    }

}
