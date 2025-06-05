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
 * JSON-LD value model and type-safe data handling.
 *
 * <p>This package provides the fundamental data model for representing and manipulating
 * JSON-LD values within the Metreeca/Mesh framework. The core abstractions enable
 * type-safe handling of linked data values with full semantic web compatibility.</p>
 *
 * <h2>Core Components</h2>
 *
 * <ul>
 *
 *     <li><strong>{@link com.metreeca.mesh.Value}</strong> - Immutable value representation supporting
 *         all JSON-LD data types including literals, objects, arrays, and language-tagged strings</li>
 *
 *     <li><strong>{@link com.metreeca.mesh.Valuable}</strong> - Functional interface for types that
 *         can be converted to Value representations</li>
 *
 *
 * </ul>
 *
 * <h2>JSON-LD Compatibility</h2>
 *
 * <p>The value model directly supports JSON-LD concepts and keywords:</p>
 *
 * <ul>
 *
 *     <li><strong>URI Resources</strong> - First-class support for URI identification and referencing</li>
 *
 *     <li><strong>Literals</strong> - Typed literals with datatype and language tag support</li>
 *
 *     <li><strong>Objects</strong> - Property-value mappings representing JSON-LD objects</li>
 *
 *     <li><strong>Arrays</strong> - Ordered collections of values with type preservation</li>
 *
 *     <li><strong>Context</strong> - Semantic context handling for compact URI (CURIE) resolution</li>
 *
 * </ul>
 *
 * <p>All value operations maintain semantic consistency and preserve JSON-LD structure,
 * enabling seamless integration with semantic web tools and linked data workflows.</p>
 *
 * @see <a href="https://www.w3.org/TR/json-ld11/">JSON-LD 1.1 - A JSON-based Serialization for Linked Data</a>
 * @see com.metreeca.mesh.shapes Shape validation framework
 * @see com.metreeca.mesh.queries Query model for value collections
 */
package com.metreeca.mesh;
