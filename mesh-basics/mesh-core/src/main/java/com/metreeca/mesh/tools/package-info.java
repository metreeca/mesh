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
 * Data persistence and REST API infrastructure.
 *
 * <p>This package provides the essential infrastructure components for handling data
 * persistence, serialization, and REST API operations within the Metreeca/Mesh framework.
 * The tools enable seamless integration between semantic data models and various storage
 * backends while supporting web service capabilities.</p>
 *
 * <h2>Core Components</h2>
 *
 * <ul>
 *
 *     <li><strong>{@link com.metreeca.mesh.tools.Store}</strong> - Primary persistence abstraction
 *         providing CRUD operations, query execution, and transaction management for semantic data</li>
 *
 *     <li><strong>{@link com.metreeca.mesh.tools.Codec}</strong> - Bidirectional serialization framework
 *         for converting between Value objects and various data formats</li>
 *
 *     <li><strong>{@link com.metreeca.mesh.tools.Agent}</strong> - REST API agent for handling
 *         HTTP-style operations with content negotiation and validation</li>
 *
 * </ul>
 *
 * <h2>Storage Abstraction</h2>
 *
 * <p>The {@link com.metreeca.mesh.tools.Store} interface provides a unified abstraction layer
 * over different storage backends, supporting:</p>
 *
 * <ul>
 *
 *     <li><strong>Resource Management</strong> - CRUD operations on identified resources with
 *         shape-based validation and constraint enforcement</li>
 *
 *     <li><strong>Query Execution</strong> - Complex query processing with filtering, sorting,
 *         pagination, and analytical operations</li>
 *
 *     <li><strong>Bulk Operations</strong> - Efficient batch processing for large datasets
 *         with transaction support and consistency guarantees</li>
 *
 *     <li><strong>Localization</strong> - Multi-language content support with locale-aware
 *         data retrieval and processing</li>
 *
 * </ul>
 *
 * <h2>Data Exchange</h2>
 *
 * <p>The {@link com.metreeca.mesh.tools.Codec} framework enables flexible data serialization
 * and deserialization across different formats while preserving semantic meaning and structural
 * integrity. Codecs handle format-specific concerns while maintaining type safety and
 * validation consistency.</p>
 *
 * <h2>REST API Operations</h2>
 *
 * <p>The {@link com.metreeca.mesh.tools.Agent} provides comprehensive REST API functionality for:</p>
 *
 * <ul>
 *
 *     <li><strong>HTTP Operations</strong> - Complete REST API implementation mapping HTTP
 *         methods to store operations with proper status codes</li>
 *
 *     <li><strong>Content Negotiation</strong> - Automatic handling of Accept headers,
 *         media types, and language preferences</li>
 *
 *     <li><strong>Request/Response</strong> - Type-safe communication patterns through
 *         {@link com.metreeca.mesh.tools.AgentRequest} and {@link com.metreeca.mesh.tools.AgentResponse}</li>
 *
 * </ul>
 *
 * <p>All tools are designed to work seamlessly with the broader Metreeca/Mesh ecosystem,
 * providing consistent error handling through {@link com.metreeca.mesh.tools.StoreException}
 * and {@link com.metreeca.mesh.tools.CodecException}, and supporting the framework's
 * commitment to semantic web standards and linked data principles.</p>
 *
 * @see com.metreeca.mesh.Value Core value model for data representation
 * @see com.metreeca.mesh.queries Query framework for data retrieval
 * @see com.metreeca.mesh.shapes Shape validation and constraint framework
 */
package com.metreeca.mesh.tools;
