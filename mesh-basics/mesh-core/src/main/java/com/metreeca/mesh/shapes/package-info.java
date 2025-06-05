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

/**
 * Value modeling and validation framework.
 *
 * <p>This package provides a comprehensive framework for defining  data structures using
 * shape-based constraints. The core abstractions enable declarative modeling of data validation rules that can be
 * applied to various data formats and storage backends.</p>
 *
 * <h2>Core Components</h2>
 *
 * <ul>
 *     <li>{@link com.metreeca.mesh.shapes.Shape} - Defines validation constraints and structural rules for values</li>
 *     <li>{@link com.metreeca.mesh.shapes.Type} - Defines class types for object validation</li>
 *     <li>{@link com.metreeca.mesh.shapes.Property} - Represents properties with their own shape constraints</li>
 * </ul>
 *
 * <h2>Standards Alignment</h2>
 *
 * <p>The modelling and validation is built on established W3C standards:</p>
 *
 * <ul>
 *
 *     <li><strong>JSON-LD</strong> - Enables semantic data modeling with linked data principles.
 *         Shapes can define JSON-LD context mappings, type hierarchies, and property relationships
 *         for structured data interchange.</li>
 *
 *     <li><strong>SHACL (Shapes Constraint Language)</strong> - Provides the foundational constraint model
 *         for validating RDF graphs and data structures. The framework implements core SHACL constraint
 *         components including cardinality, datatype, value range, and property path constraints.</li>
 *
 * </ul>
 *
 * <p>This standards-based approach ensures interoperability with existing semantic web tools
 * and enables validation of both JSON-LD documents and arbitrary data structures using
 * consistent constraint definitions.</p>
 *
 *  @see <a href="https://www.w3.org/TR/json-ld/">JSON-LD 1.1 - A JSON-based Serialization for Linked Data</a>
 *
 * @see <a href="https://www.w3.org/TR/shacl/#core-components">Shapes Constraint Language (SHACL) - 4. Core
 *         Constraint Components</a>
 */
package com.metreeca.mesh.shapes;
