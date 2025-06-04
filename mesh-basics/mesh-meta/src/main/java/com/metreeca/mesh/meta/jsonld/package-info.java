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
 * JSON-LD semantic modeling annotations for linked data representation.
 *
 * <p>This package provides annotations for defining JSON-LD context mappings, semantic relationships,
 * and linked data structure within Java frame interfaces. The annotations enable seamless conversion
 * between Java objects and JSON-LD documents while maintaining semantic web compatibility.</p>
 *
 * <h2>Semantic Web Integration</h2>
 *
 * <p>The annotations in this package directly correspond to JSON-LD keywords and concepts:</p>
 *
 * <ul>
 *     <li><strong>Context Definition</strong> - Namespace mappings and base URI resolution for compact URI (CURIE) usage</li>
 *     <li><strong>Resource Identity</strong> - URI-based resource identification following linked data principles</li>
 *     <li><strong>Property Mapping</strong> - RDF predicate mappings for both forward and reverse relationships</li>
 *     <li><strong>Type Semantics</strong> - RDF class associations and type hierarchy support</li>
 *     <li><strong>Data Management</strong> - Control over serialization behavior and relationship management</li>
 * </ul>
 *
 * <h2>Frame Interface Design</h2>
 *
 * <p>Frame interfaces annotated with these annotations serve as data binding contracts that define
 * both the structure and semantics of linked data resources. The annotation-driven approach enables
 * compile-time code generation while ensuring runtime semantic consistency.</p>
 *
 * <h2>JSON-LD Standards Compliance</h2>
 *
 * <p>All annotations are designed to produce valid JSON-LD output that conforms to W3C specifications.
 * The framework handles context generation, URI resolution, and semantic validation automatically
 * based on the annotation configuration.</p>
 *
 * <p>Common usage patterns combine multiple annotations to define complete linked data models:</p>
 *
 * <pre>{@code
 * @Frame
 * @Base("https://example.org/")
 * @Namespace(prefix = "foaf", value = "http://xmlns.com/foaf/0.1/")
 * @Class("foaf:Person")
 * public interface Person {
 *
 *     @Id URI id();
 *     @Forward("foaf:name") String name();
 *     @Reverse("foaf:knows") Set<Person> knownBy();
 * }
 * }</pre>
 *
 * <p>This approach enables developers to work with strongly-typed Java interfaces while automatically
 * generating semantically-correct JSON-LD representations for data interchange and persistence.</p>
 *
 * @see <a href="https://www.w3.org/TR/json-ld11/">JSON-LD 1.1 - A JSON-based Serialization for Linked Data</a>
 * @see <a href="https://www.w3.org/TR/2010/NOTE-curie-20101216/">CURIE Syntax 1.0 - Compact URI Expression</a>
 * @see com.metreeca.mesh.meta.shacl SHACL validation constraint annotations
 */
package com.metreeca.mesh.meta.jsonld;
