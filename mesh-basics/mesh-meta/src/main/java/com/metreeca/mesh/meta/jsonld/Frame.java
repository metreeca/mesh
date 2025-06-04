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
 * Marks an interface as a JSON-LD frame.
 *
 * <p>Designates a Java interface as a frame model for JSON-LD serialization and deserialization.
 * Interfaces annotated with {@code @Frame} define the structure of JSON-LD documents and serve as data binding
 * contracts for the Mesh framework.</p>
 *
 * <p>Frame interfaces must adhere to the following requirements:</p>
 * <ul>
 *   <li>Must be top-level interfaces (not nested within other types)</li>
 *   <li>Must not be generic interfaces (no type parameters)</li>
 *   <li>Methods must not have arguments</li>
 *   <li>Methods must have non-void return types</li>
 *   <li>Static methods are ignored</li>
 * </ul>
 *
 * <p>Frame interfaces represent resources in a linked data context, where methods correspond to
 * properties of the resource.</p>
 */
@Target(TYPE)
@Retention(RUNTIME)
public @interface Frame { }
