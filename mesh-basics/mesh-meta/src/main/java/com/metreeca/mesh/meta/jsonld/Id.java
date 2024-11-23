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
 * Marks a method as providing the identifier for a JSON-LD frame.
 *
 * <p>Designates a method that returns the URI or IRI identifier of the resource represented by a frame.
 * This annotation corresponds to the {@code @id} keyword in JSON-LD.</p>
 *
 * <p>Methods annotated with {@code @Id} must return a {@link java.net.URI} value
 * representing the unique identifier of the resource. If a relative URI is returned, it will be resolved against the
 * frame's {@link Base} URI, if specified, or against a default base URI, otherwise.</p>
 */
@Target(METHOD)
@Retention(RUNTIME)
public @interface Id { }
