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
 * Marks a method as an internal utility in a JSON-LD frame.
 *
 * <p>Designates a method in a {@link Frame} interface that should be ignored for JSON-LD
 * serialization and deserialization purposes. This annotation allows frames to contain utility methods that are used
 * internally by the application but have no corresponding RDF property.</p>
 *
 * <p>Methods annotated with {@code @Internal} are completely excluded from the frame processing
 * pipeline. They are not considered during serialization or deserialization, and no validation constraints are applied
 * to them. Typically, such methods have default implementations and may accept parameters.</p>
 */
@Target(METHOD)
@Retention(RUNTIME)
public @interface Internal { }
