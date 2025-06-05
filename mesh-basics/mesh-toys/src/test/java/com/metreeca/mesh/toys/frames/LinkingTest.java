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

package com.metreeca.mesh.toys.frames;

import org.junit.jupiter.api.Test;

import static com.metreeca.mesh.Value.*;
import static com.metreeca.mesh.toys.frames.LinkingFrame.toValue;
import static com.metreeca.shim.Collections.set;
import static com.metreeca.shim.URIs.base;

import static org.assertj.core.api.Assertions.assertThat;

final class LinkingTest {

    @Test void testReferenceLinkedObjectsByIdInToValue() {

        assertThat(toValue(new LinkingFrame()

        )).as("empty").isEqualTo(object(
                shape(LinkingFrame.SHAPE)
        ));

        assertThat(toValue(new LinkingFrame()
                .resource(new ResourceFrame())
        )).as("without id").isEqualTo(object(
                shape(LinkingFrame.SHAPE),
                field("resource", Nil())
        ));

        assertThat(toValue(new LinkingFrame()
                .resource(new ResourceFrame().id(base()))
        )).isEqualTo(object(
                shape(LinkingFrame.SHAPE),
                field("resource", object(
                        shape(ResourceFrame.SHAPE),
                        field(ID, uri(base()))
                ))
        ));
    }

    @Test void testReferenceLinkedObjectsInContainersByIdInToValue() {

        assertThat(toValue(new LinkingFrame()

        )).as("empty").isEqualTo(object(
                shape(LinkingFrame.SHAPE)
        ));

        assertThat(toValue(new LinkingFrame()
                .resources(set(new ResourceFrame()))
        )).as("without id").isEqualTo(object(
                shape(LinkingFrame.SHAPE),
                field("resources", array())
        ));

        assertThat(toValue(new LinkingFrame()
                .resources(set(new ResourceFrame().id(base())))
        )).isEqualTo(object(
                shape(LinkingFrame.SHAPE),
                field("resources", array(object(
                        shape(ResourceFrame.SHAPE),
                        field(ID, uri(base()))
                )))
        ));
    }

}
