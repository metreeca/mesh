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

package com.metreeca.mesh.meta.shacl;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Required property annotation.
 *
 * <p>Method-level annotation that marks a property as required, meaning it must have at least one value.
 * This is a convenience shorthand for {@code @MinCount(1)}.</p>
 *
 * <p>This annotation can be used directly on methods in frame interfaces where you want to ensure
 * that properties must have values.</p>
 *
 * @see MinCount Minimum cardinality constraint
 * @see com.metreeca.mesh.meta.jsonld.Frame Frame annotation for defining JSON-LD frame interfaces
 * @see <a href="https://www.w3.org/TR/shacl/#MinCountConstraintComponent">SHACL MinCount Constraint Component</a>
 */
@Target(METHOD)
@Retention(RUNTIME)
@Alias(MinCount=@MinCount(1))
public @interface Required { }
