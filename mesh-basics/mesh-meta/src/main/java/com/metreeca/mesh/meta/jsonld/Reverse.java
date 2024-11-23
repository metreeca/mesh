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

package com.metreeca.mesh.meta.jsonld;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Specifies a reverse RDF relationship for a JSON-LD frame property.
 *
 * <p>Maps a frame method to a reverse RDF relationship using CURIE syntax. Unlike {@link Forward},
 * which defines outgoing links from the resource, this annotation represents incoming links to the
 * resource from other resources. This corresponds to the {@code @reverse} keyword in JSON-LD.</p>
 *
 * <p>When applied to a method in a {@link Frame} interface, it specifies that the property value(s)
 * represent resources that link to this resource using the specified predicate. If the value is omitted,
 * the method name is used as the predicate name, which is resolved against the default namespace.</p>
 * 
 * @see <a href="https://www.w3.org/TR/2010/NOTE-curie-20101216/">CURIE Syntax 1.0</a>
 */
@Target(METHOD)
@Retention(RUNTIME)
public @interface Reverse {

    String value() default "";

}
