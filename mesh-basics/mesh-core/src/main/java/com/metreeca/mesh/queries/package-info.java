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
 * Query model for value filtering and analysis.
 *
 * <p>This package provides a comprehensive query framework for working with collections
 * of JSON-LD values. The query model enables declarative specification of search criteria,
 * sorting requirements, pagination parameters, and analytical operations on structured data.</p>
 *
 * <h2>Core Components</h2>
 *
 * <ul>
 *
 *     <li><strong>{@link com.metreeca.mesh.queries.Query}</strong> - Main query interface combining
 *         filtering, sorting, pagination, and projection capabilities</li>
 *
 *     <li><strong>{@link com.metreeca.mesh.queries.Expression}</strong> - Path-based property navigation
 *         and value extraction from complex object structures</li>
 *
 *     <li><strong>{@link com.metreeca.mesh.queries.Criterion}</strong> - Filtering predicates including
 *         comparison, pattern matching, and logical operations</li>
 *
 *     <li><strong>{@link com.metreeca.mesh.queries.Transform}</strong> - Value transformation operations
 *         for data projection and analytical processing</li>
 *
 *     <li><strong>{@link com.metreeca.mesh.queries.Probe}</strong> - Introspection utilities for
 *         analyzing query structure and optimization</li>
 *
 * </ul>
 *
 * <h2>Query Capabilities</h2>
 *
 * <p>The query model supports comprehensive data operations:</p>
 *
 * <ul>
 *
 *     <li><strong>Filtering</strong> - Complex predicate logic with type-aware comparisons,
 *         pattern matching, and range operations</li>
 *
 *     <li><strong>Sorting</strong> - Multi-level sorting with locale-aware string comparison
 *         and custom ordering semantics</li>
 *
 *     <li><strong>Pagination</strong> - Efficient result set navigation with offset and limit controls</li>
 *
 *     <li><strong>Projection</strong> - Property selection and value transformation for
 *         optimized data retrieval</li>
 *
 *     <li><strong>Aggregation</strong> - Statistical operations including grouping, counting,
 *         and numerical analysis through {@link com.metreeca.mesh.queries.Table} and
 *         {@link com.metreeca.mesh.queries.Tuple} abstractions</li>
 *
 * </ul>
 *
 * <h2>Expression Language</h2>
 *
 * <p>The {@link com.metreeca.mesh.queries.Expression} system provides a powerful path-based
 * language for navigating complex value structures. Expressions support property traversal,
 * array indexing, and value extraction with full type safety and semantic awareness.</p>
 *
 * <p>Query specifications are designed to be backend-agnostic, enabling the same query logic
 * to work across different storage implementations while maintaining semantic consistency
 * with JSON-LD and linked data principles.</p>
 *
 * @see com.metreeca.mesh.Value Core value model for query targets
 * @see com.metreeca.mesh.tools.Store Storage abstraction for query execution
 * @see com.metreeca.mesh.shapes Shape constraints for query validation
 */
package com.metreeca.mesh.queries;
