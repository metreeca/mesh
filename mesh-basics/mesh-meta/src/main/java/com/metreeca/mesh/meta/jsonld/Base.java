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

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Specifies the base URI for a JSON-LD frame.
 *
 * <p>Defines the base URI used to resolve relative URIs within a frame. This annotation corresponds
 * to the {@code @base} keyword in the JSON-LD context and establishes the base against which
 * relative URIs are resolved.</p>
 *
 * <p>When applied to a {@link Frame} interface, it serves two main purposes:</p>
 * <ul>
 *   <li>Resolves relative URIs returned by {@link Id} methods against this base URI</li>
 *   <li>Resolves relative URIs in {@link Namespace} annotations against this base URI</li>
 * </ul>
 */
@Target(TYPE)
@Retention(RUNTIME)
public @interface Base {

    String value();

}
