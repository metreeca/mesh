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
 * Sample data models for testing and demonstration purposes.
 *
 * <p>Provides a realistic domain model representing an organization with offices,
 * employees, and career events. The model demonstrates comprehensive usage of
 * Metreeca/Mesh frame annotations including relationships, constraints, and
 * data modeling patterns.</p>
 *
 * <h2>Domain Model</h2>
 *
 * <ul>
 *     <li><strong>{@linkplain com.metreeca.mesh.toys.Resource}</strong> - Base interface for all domain entities
 *         with identity, type, and labeling capabilities</li>
 *     <li><strong>{@linkplain com.metreeca.mesh.toys.Office}</strong> - Organizational locations with geographical
 *         information and employee associations</li>
 *     <li><strong>{@linkplain com.metreeca.mesh.toys.Employee}</strong> - Personnel records with personal details,
 *         organizational relationships, and career tracking</li>
 *     <li><strong>{@linkplain Event}</strong> - Career milestones and organizational changes
 *         embedded within employee records</li>
 *     <li><strong>{@linkplain Catalog}</strong> - Generic collection interface for
 *         organizing domain entities</li>
 * </ul>
 *
 * <h2>Test Data</h2>
 *
 * <p>The {@linkplain Resources} interface provides access to realistic test datasets
 * including 7 offices and 23 employees with complete relationships, constraints,
 * and multilingual content. Data is loaded from TSV files and automatically
 * converted to frame instances.</p>
 *
 * @see com.metreeca.mesh.meta.jsonld Frame annotation specification
 * @see com.metreeca.mesh.meta.shacl SHACL constraint annotations
 */
package com.metreeca.mesh.toys;
