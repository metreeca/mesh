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
 * REST/JSON-LD API publishing, data serialization, and persistence infrastructure.
 *
 * <p>This package provides the essential infrastructure components for handling
 * REST/JSON-LD API publishing, data serialization, and persistence.</p>
 *
 * <h2>Core Components</h2>
 *
 * <ul>
 *
 *     <li><strong>{@link com.metreeca.mesh.pipe.Agent}</strong> - REST/JSON-LD API agent for handling
 *         HTTP-based CRUD operations with content negotiation and validation</li>
 *
 *     <li><strong>{@link com.metreeca.mesh.pipe.AgentProcessor}</strong> - Customization interface
 *         for transforming request and response data during HTTP processing</li>
 *
 *     <li><strong>{@link com.metreeca.mesh.pipe.Codec}</strong> - Bidirectional serialization framework
 *         for converting between Value objects and various data formats</li>
 *
 *     <li><strong>{@link com.metreeca.mesh.pipe.Store}</strong> - Primary persistence abstraction
 *         providing CRUD operations, query execution, and transaction management for semantic data</li>
 *
 * </ul>
 *
 *
 * <h2>REST/JSON-LD API Operations</h2>
 *
 * <p>The {@link com.metreeca.mesh.pipe.Agent} orchestrates HTTP processing, codec, and store operations
 * to provide comprehensive REST/JSON-LD API functionality for:</p>
 *
 * <ul>
 *
 *     <li><strong>HTTP Operations</strong> - Complete REST/JSON-LD API implementation mapping HTTP
 *         methods to store operations with proper status codes</li>
 *
 *     <li><strong>Content Negotiation</strong> - Automatic handling of Accept headers,
 *         media types, and language preferences</li>
 *
 *     <li><strong>Request/Response</strong> - Type-safe communication patterns through
 *         {@link com.metreeca.mesh.pipe.AgentRequest} and {@link com.metreeca.mesh.pipe.AgentResponse}</li>
 *
 *     <li><strong>Customization</strong> - Extensible processing through
 *         {@link com.metreeca.mesh.pipe.AgentProcessor} for validation, transformation, and access control</li>
 *
 * </ul>
 *
 *
 * <h2>Data Exchange</h2>
 *
 * <p>The {@link com.metreeca.mesh.pipe.Codec} framework enables flexible data serialization
 * and deserialization across different formats while preserving semantic meaning and structural
 * integrity. Codecs handle format-specific concerns while maintaining type safety and
 * validation consistency.</p>
 *
 *
 * <h2>Storage Abstraction</h2>
 *
 * <p>The {@link com.metreeca.mesh.pipe.Store} interface provides a unified abstraction layer
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
 * @see com.metreeca.mesh.Value Core value model for data representation
 * @see com.metreeca.mesh.queries Query framework for data retrieval
 * @see com.metreeca.mesh.shapes Shape validation and constraint framework
 */
package com.metreeca.mesh.pipe;
