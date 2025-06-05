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
 * Code generation and introspection tools for frame interfaces.
 *
 * <p>This package provides the core infrastructure for analyzing annotated Java interfaces
 * and generating corresponding data binding implementations. The tools support both
 * compile-time code generation and runtime introspection capabilities.</p>
 *
 * <h2>Core Components</h2>
 *
 * <ul>
 *     <li><strong>Introspector</strong> - Analyzes frame interface annotations and produces
 *         abstract syntax tree representations for code generation</li>
 *     <li><strong>Generator</strong> - Transforms AST models into executable Java code that
 *         implements frame interface contracts with full semantic web support</li>
 * </ul>
 *
 * <p>The code generation process transforms declarative frame interfaces into efficient
 * runtime implementations that handle JSON-LD serialization, SHACL validation, and
 * semantic data binding automatically.</p>
 *
 * @see com.metreeca.mesh.mint.ast Abstract syntax tree model for frame representation
 * @see com.metreeca.mesh.meta Frame interface annotations
 */
package com.metreeca.mesh.mint.tools;
