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

package com.metreeca.mesh.test.frames;

import org.junit.jupiter.api.Test;

import static com.metreeca.shim.URIs.term;
import static com.metreeca.shim.URIs.uri;

import static org.assertj.core.api.Assertions.assertThat;

final class PropertiesTest {

    @Test void testForward() {
        assertThat(PropertiesFrame.SHAPE.property("forward")).hasValueSatisfying(property -> {
            assertThat(property.forward()).contains(term("forward"));
            assertThat(property.reverse()).isEmpty();
        });
    }

    @Test void testReverse() {
        assertThat(PropertiesFrame.SHAPE.property("reverse")).hasValueSatisfying(property -> {
            assertThat(property.forward()).isEmpty();
            assertThat(property.reverse()).contains(term("reverse"));
        });
    }

    @Test void testSymmetric() {
        assertThat(PropertiesFrame.SHAPE.property("symmetric")).hasValueSatisfying(property -> {
            assertThat(property.forward()).contains(term("symmetric"));
            assertThat(property.reverse()).contains(term("symmetric"));
        });
    }

    @Test void testCustom() {
        assertThat(PropertiesFrame.SHAPE.property("custom")).hasValueSatisfying(property -> {
            assertThat(property.forward()).contains(uri("app:/#advance"));
            assertThat(property.reverse()).contains(uri("app:/#retreat"));
        });
    }

}
