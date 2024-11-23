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

package com.metreeca.mesh.test.frames;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static com.metreeca.mesh.Value.*;

import static org.assertj.core.api.Assertions.assertThat;

@Nested
final class ResourceTest {

    @Nested
    final class ValueConstructorTest {

        @Test void testIncludeNestedObjects() {
            assertThat(new ResourceFrame(object(
                            field("value", string("outer")),
                            field("resource", object(
                                    field("value", string("inner"))
                            ))
                    ))
                            .resource()
                            .value()
            ).isEqualTo("inner");
        }

    }

    @Nested
    final class AccessorsTest {

        @Test void testProvideVirtualEmptySets() {
            assertThat(new ResourceFrame().resources()).isNotNull().isEmpty(); // return an empty set if undefined
            assertThat(new ResourceFrame().toValue().get("frames").isEmpty()).isTrue(); // use actual data in value
        }

        @Test void testProvideVirtualEmptyMaps() {
            assertThat(new ResourceFrame().texts()).isNotNull().isEmpty();// return an empty map if undefined
            assertThat(new ResourceFrame().toValue().get("texts").isEmpty()).isTrue(); // use actual data in value
        }

    }

}