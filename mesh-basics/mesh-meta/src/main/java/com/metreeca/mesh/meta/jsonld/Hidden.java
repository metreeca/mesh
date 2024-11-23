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
 * Marks a method as providing a property that should be hidden in JSON-LD serialization.
 *
 * <p>Designates a method in a {@link Frame} interface whose value should not be included by default in the
 * JSON-LD serialization output.</p>
 *
 * <p>When applied to a method in a {@link Frame} interface, the property value will be processed
 * normally during deserialization (the property can be read from input), but will be omitted during serialization (the
 * property won't appear in output), unless explicitly requested by the client.</p>
 */
@Target(METHOD)
@Retention(RUNTIME)
public @interface Hidden { }
