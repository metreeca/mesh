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
 * Metadata and code generation support for the Mesh framework.
 *
 * <p>This package provides the foundational metadata infrastructure for defining, validating, and
 * generating frame-based data models. It serves as the bridge between declarative data modeling
 * through annotations and runtime data binding capabilities.</p>
 *
 * <h2>Core Components</h2>
 *
 * <p>The package is organized into three main areas:</p>
 *
 * <ul>
 *     <li><strong>JSON-LD Annotations</strong> ({@link com.metreeca.mesh.meta.jsonld}) - Semantic
 *         data modeling annotations that enable linked data representation and context definition</li>
 *     <li><strong>SHACL Constraints</strong> ({@link com.metreeca.mesh.meta.shacl}) - Validation
 *         constraint annotations based on the W3C SHACL specification for data quality assurance</li>
 *     <li><strong>Runtime Support</strong> - Utilities for value conversion and frame code generation
 *         that power the compile-time to runtime bridge</li>
 * </ul>
 *
 * <h2>Design Principles</h2>
 *
 * <p>The metadata system is built on several key principles:</p>
 *
 * <ul>
 *     <li><strong>Standards Compliance</strong> - All annotations and constraints are aligned with
 *         established W3C standards (JSON-LD, SHACL) ensuring interoperability and semantic consistency</li>
 *     <li><strong>Code Generation</strong> - Annotations drive compile-time code generation, eliminating
 *         runtime reflection overhead while maintaining type safety</li>
 *     <li><strong>Declarative Modeling</strong> - Data models are defined declaratively through interfaces
 *         and annotations, separating structure from implementation concerns</li>
 *     <li><strong>Validation Integration</strong> - Constraint definitions are seamlessly integrated with
 *         data binding, ensuring data integrity throughout the application lifecycle</li>
 * </ul>
 *
 * <h2>Usage Patterns</h2>
 *
 * <p>The metadata annotations are typically used in combination to define complete data models:</p>
 *
 * <pre>{@code
 * @Frame
 * @Class("schema:Person")
 * @Namespace(prefix = "schema", value = "https://schema.org/")
 * public interface Person {
 *
 *     @Id
 *     URI id();
 *
 *     @Required
 *     @MinLength(1)
 *     @MaxLength(100)
 *     String name();
 *
 *     @Pattern("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$")
 *     String email();
 * }
 * }</pre>
 *
 * <p>This declarative approach enables the framework to generate efficient runtime implementations
 * while maintaining full semantic web compatibility and data validation capabilities.</p>
 *
 * @see com.metreeca.mesh.meta.jsonld JSON-LD semantic modeling annotations
 * @see com.metreeca.mesh.meta.shacl SHACL validation constraint annotations
 * @see com.metreeca.mesh.shapes Shape validation framework
 */
package com.metreeca.mesh.meta;
