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

package com.metreeca.mesh.toys.tests;

import com.metreeca.mesh.shapes.Property;

import org.junit.jupiter.api.Test;

import static com.metreeca.mesh.Value.*;
import static com.metreeca.mesh.toys.tests.EmbeddingFrame.toValue;
import static com.metreeca.shim.Collections.set;
import static com.metreeca.shim.URIs.base;

import static org.assertj.core.api.Assertions.assertThat;

final class EmbeddingTest {

    @Test void testIdentifyEmbeddedProperties() {
        assertThat(EmbeddingFrame.SHAPE.property("reference").map(Property::embedded)).contains(true);
        assertThat(EmbeddingFrame.SHAPE.property("description").map(Property::embedded)).contains(true);
    }


    @Test void testIncludeEmbeddedObjectsInToValue() {
        assertThat(toValue(new EmbeddingFrame()
                .reference(new ReferenceFrame().id(base()).label("reference"))
                .description(new DescriptionFrame().value("description"))
                .references(set(new ReferenceFrame().id(base()).label("references")))
                .descriptions(set(new DescriptionFrame().value("descriptions")))
        )).isEqualTo(object(
                shape(EmbeddingFrame.SHAPE),
                field("reference", object(
                        shape(ReferenceFrame.SHAPE),
                        id(base()),
                        field("label", string("reference"))
                )),
                field("description", object(
                        shape(DescriptionFrame.SHAPE),
                        field("value", string("description"))
                )),
                field("references", array(object(
                        shape(ReferenceFrame.SHAPE),
                        id(base()),
                        field("label", string("references"))
                ))),
                field("descriptions", array(object(
                        shape(DescriptionFrame.SHAPE),
                        field("value", string("descriptions"))
                )))
        ));
    }

}
