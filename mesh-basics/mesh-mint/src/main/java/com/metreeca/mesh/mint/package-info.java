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
 * Frame code generation and annotation processing infrastructure.
 *
 * <p>This package provides the core annotation processor and supporting infrastructure
 * for generating runtime implementations of frame interfaces. The processor analyzes
 * annotated interfaces at compile time and generates corresponding Java classes that
 * handle JSON-LD serialization, SHACL validation, and semantic data binding.</p>
 *
 * <h2>Core Components</h2>
 *
 * <ul>
 *     <li><strong>FrameProcessor</strong> - Annotation processor that handles compile-time
 *         code generation for frame interfaces</li>
 *     <li><strong>FrameException</strong> - Exception type for frame processing errors</li>
 * </ul>
 *
 * <p>The annotation processor integrates with the Java compiler toolchain to automatically
 * generate efficient implementations of frame interfaces, eliminating the need for runtime
 * reflection while maintaining full semantic web compatibility.</p>
 *
 * @see com.metreeca.mesh.mint.ast Abstract syntax tree model for frame representation
 * @see com.metreeca.mesh.mint.tools Code generation and introspection tools
 * @see com.metreeca.mesh.meta Frame interface annotations
 */
package com.metreeca.mesh.mint;
