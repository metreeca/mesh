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
 * Testing framework for {@linkplain com.metreeca.mesh.pipe.Store Store} implementations.
 *
 * <p>Provides a structured test suite that validates all aspects of store functionality
 * including data retrieval, CRUD operations, querying, filtering, sorting, and analytics.
 * Store implementations can extend the framework classes to automatically validate 
 * compliance with the Store interface contract.</p>
 *
 * <h2>Framework Structure</h2>
 *
 * <ul>
 *
 *     <li><strong>{@linkplain com.metreeca.mesh.test.stores.StoreTest}</strong> - Abstract base class providing
 *         comprehensive store testing infrastructure with test data and helper methods</li>
 *
 *     <li><strong>Retrieve Tests</strong> - {@linkplain com.metreeca.mesh.test.stores.StoreTestRetrieveValues}
 *         and specialized test classes validate object retrieval, value queries, and analytical operations</li>
 *
 *     <li><strong>CRUD Tests</strong> - Insert, remove, and other data modification 
 *         operations with validation and error handling scenarios</li>
 *
 * </ul>
 *
 * <h2>Test Data</h2>
 *
 * <p>The framework uses realistic Office and Employee data models with relationships,
 * providing comprehensive coverage of:</p>
 *
 * <ul>
 *
 *     <li>Basic object retrieval and navigation</li>
 *     <li>Complex queries with filtering, sorting, and pagination</li>
 *     <li>Analytical operations including aggregations and groupings</li>
 *     <li>Data validation and constraint checking</li>
 *     <li>Error handling and edge cases</li>
 *
 * </ul>
 *
 * <p>Store implementations should extend {@linkplain com.metreeca.mesh.test.stores.StoreTest} and provide a
 * concrete store instance to run the complete validation suite.</p>
 *
 * @see com.metreeca.mesh.pipe.Store Store interface specification
 * @see com.metreeca.mesh.toys Test data models and examples
 */
package com.metreeca.mesh.test.stores;
