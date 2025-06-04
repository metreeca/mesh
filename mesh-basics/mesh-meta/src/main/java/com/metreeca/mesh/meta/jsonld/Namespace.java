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

import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Defines a namespace prefix-URI mapping for a JSON-LD frame.
 *
 * <p>Establishes prefix-to-URI mappings for compact URIs (CURIEs) used within a frame interface.
 * This annotation corresponds to the {@code @context} section in JSON-LD where namespaces are defined.</p>
 *
 * <p>When applied to a {@link Frame} interface, it allows the use of prefixed property names and
 * class identifiers in other annotations like {@link Class}, {@link Forward} and {@link Reverse}. Multiple
 * {@code @Namespace} annotations can be applied to a single frame to define multiple prefix mappings.</p>
 *
 * @see Namespaces
 */
@Target(TYPE)
@Retention(RUNTIME)
@Repeatable(Namespaces.class)
public @interface Namespace {

    /**
     * Retrieves the namespace prefix.
     *
     * @return the (possibly empty) namespace prefix
     */
    String prefix() default "";

    /**
     * Retrieves the namespace URI.
     *
     * @return the namespace URI; relative URIs are resolved against the frame {@link Base @Base} uri
     */
    String value();

}
