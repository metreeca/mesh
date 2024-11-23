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
 * Marks a method as providing the RDF type for a JSON-LD frame.
 *
 * <p>Designates a method that returns the RDF type or types of the resource represented by a frame.
 * This annotation corresponds to the {@code @type} keyword in JSON-LD and allows for inclusion of type information in
 * JSON-LD serialization.</p>
 *
 * <p>Methods annotated with {@code @Type} must return a {@link String} value representing the class
 * for the resource. This is usually the simple name of the most pecific class of the resource.</p>
 */
@Target(METHOD)
@Retention(RUNTIME)
public @interface Type { }
