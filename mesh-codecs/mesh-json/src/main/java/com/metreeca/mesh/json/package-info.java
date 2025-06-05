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
 * JSON-LD codec for value serialization.
 *
 * <p>This package provides comprehensive JSON-LD encoding and decoding capabilities
 * for the Metreeca/Mesh framework. The codec handles bidirectional conversion between
 * Java value objects and JSON-LD representations while preserving semantic meaning
 * and structural integrity.</p>
 *
 * <h2>Core Components</h2>
 *
 * <ul>
 *     <li><strong>JSONCodec</strong> - Main codec interface for JSON-LD value processing</li>
 *     <li><strong>JSONEncoder</strong> - Converts Java values to JSON-LD format</li>
 *     <li><strong>JSONDecoder</strong> - Converts JSON-LD format to Java values</li>
 *     <li><strong>JSONReader/JSONWriter</strong> - Low-level JSON streaming support</li>
 * </ul>
 *
 * <h2>JSON-LD Standards Support</h2>
 *
 * <p>The codec implementation fully supports JSON-LD 1.1 features including context
 * processing, frame-based data shaping, and semantic type preservation. The encoding
 * process respects JSON-LD keywords and maintains compatibility with standard JSON-LD
 * processors and semantic web tooling.</p>
 *
 * @see <a href="https://www.w3.org/TR/json-ld11/">JSON-LD 1.1 - A JSON-based Serialization for Linked Data</a>
 * @see com.metreeca.mesh.Value Value model for data representation
 */
package com.metreeca.mesh.json;
