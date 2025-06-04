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

/**
 * SHACL validation constraint annotations for data quality assurance.
 *
 * <p>This package provides annotations that implement the W3C Shapes Constraint Language (SHACL)
 * specification for Java frame interfaces. The annotations enable declarative definition of data
 * validation rules that are enforced during data binding, serialization, and persistence operations.</p>
 *
 * <h2>Constraint Categories</h2>
 *
 * <p>The SHACL constraint annotations are organized into several functional categories:</p>
 *
 * <ul>
 *     <li><strong>Value Range Constraints</strong> - Minimum and maximum bounds for numeric and comparable values</li>
 *     <li><strong>String Constraints</strong> - Length limits and pattern matching for textual data</li>
 *     <li><strong>Cardinality Constraints</strong> - Minimum and maximum occurrence counts for properties</li>
 *     <li><strong>Value Set Constraints</strong> - Enumeration and required value specifications</li>
 *     <li><strong>Language Constraints</strong> - Language tag validation for internationalized text</li>
 *     <li><strong>Custom Constraints</strong> - Extensible validation through custom constraint functions</li>
 * </ul>
 *
 * <h2>Constraint Composition</h2>
 *
 * <p>Individual constraints can be combined using alias annotations to create reusable constraint
 * sets. This compositional approach enables consistent validation patterns across multiple properties
 * and frame interfaces while maintaining clear separation of concerns.</p>
 *
 * <h2>SHACL Standards Compliance</h2>
 *
 * <p>All constraint annotations map directly to SHACL constraint components, ensuring compatibility
 * with external SHACL validators and semantic web tooling. The framework automatically generates
 * SHACL shapes that correspond to the annotation-based constraints.</p>
 *
 * <p>Example usage combining multiple constraint types:</p>
 *
 * <pre>{@code
 * @Frame
 * public interface Product {
 *
 *     @Required
 *     @MinLength(3)
 *     @MaxLength(50)
 *     String name();
 *
 *     @MinInclusive("0.01")
 *     @MaxInclusive("99999.99")
 *     BigDecimal price();
 *
 *     @In({"ACTIVE", "DISCONTINUED", "PENDING"})
 *     String status();
 * }
 * }</pre>
 *
 * <p>This declarative approach to validation ensures data quality while maintaining clean separation
 * between business logic and validation concerns.</p>
 *
 * @see <a href="https://www.w3.org/TR/shacl/">Shapes Constraint Language (SHACL)</a>
 * @see <a href="https://www.w3.org/TR/shacl/#core-components">SHACL Core Constraint Components</a>
 * @see com.metreeca.mesh.meta.jsonld JSON-LD semantic modeling annotations
 */
package com.metreeca.mesh.meta.shacl;
