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

package com.metreeca.mesh.meta.jsonld;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Specifies the RDF class for a JSON-LD frame.
 *
 * <p>Associates a frame interface with an RDF class identifier using CURIE syntax. This annotation
 * corresponds to the {@code @type} keyword in JSON-LD and defines the semantic type of resources
 * represented by the frame.</p>
 *
 * <p>When applied to a {@link Frame} interface, it specifies the default RDF class for instances
 * of the frame. If the value is omitted, the simple name of the interface is used as the type name,
 * which is resolved against the default namespace.</p>
 * 
 * @see <a href="https://www.w3.org/TR/2010/NOTE-curie-20101216/">CURIE Syntax 1.0</a>
 */
@Target(TYPE)
@Retention(RUNTIME)
public @interface Class {

    /**
     * Retrieves the RDF class IRI.
     *
     * @return the IRI of the RDF class this frame represents; defaults to empty string
     */
    String value() default "";

}
