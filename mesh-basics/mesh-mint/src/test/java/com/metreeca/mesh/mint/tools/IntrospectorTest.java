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

import io.toolisticon.cute.Cute;

import javax.lang.model.element.TypeElement;
import java.util.function.BiConsumer;

final class IntrospectorTest {

    static void with(final String source, final BiConsumer<Introspector, TypeElement> consumer) {
        Cute.unitTest()

                .given()
                .useSourceFile("/com/metreeca/mesh/mint/tools/%s.java".formatted(source.replace('.', '/')))

                .when()
                .unitTestWithoutPassIn(environment -> consumer.accept(
                        new Introspector(environment),
                        environment.getElementUtils().getTypeElement("com.metreeca.mesh.mint.tools.%s".formatted(source))
                ))

                .thenExpectThat()
                .compilationSucceeds()

                .executeTest();
    }


    //̸/////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private IntrospectorTest() { }

}
