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
 * RDF4J-based graph store implementation for semantic data storage.
 *
 * <p>Provides a comprehensive {@linkplain com.metreeca.mesh.tools.Store Store} implementation
 * built on Eclipse RDF4J infrastructure, enabling persistent storage and querying of 
 * semantic graph data with full SPARQL 1.1 support.</p>
 *
 * <h2>Core Components</h2>
 *
 * <ul>
 *
 *     <li><strong>{@linkplain com.metreeca.mesh.rdf4j.RDF4JStore}</strong> - Main store implementation providing
 *         CRUD operations and transaction management with full SPARQL support</li>
 *
 * </ul>
 *
 * <h2>Query Processing</h2>
 *
 * <p>The implementation includes sophisticated query processing capabilities
 * including complex query translation, asynchronous property fetching, efficient
 * bulk operations, and query optimization.</p>
 *
 * <h2>Features</h2>
 *
 * <p>Key features include:</p>
 *
 * <ul>
 *
 *     <li>Full SPARQL 1.1 support including aggregates, property paths, and complex filters</li>
 *     <li>Named graph support for multi-tenant data organization</li>  
 *     <li>Asynchronous and batched operations for optimal performance</li>
 *     <li>Transaction management with connection pooling and resource cleanup</li>
 *     <li>Comprehensive datatype support including temporal types and localized text</li>
 *
 * </ul>
 *
 * @see <a href="https://rdf4j.org">Eclipse RDF4J</a>
 * @see <a href="https://www.w3.org/TR/sparql11-query/">SPARQL 1.1 Query Language</a>
 * @see com.metreeca.mesh.tools.Store Store interface specification
 */
package com.metreeca.mesh.rdf4j;
