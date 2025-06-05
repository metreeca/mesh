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

package com.metreeca.mesh.toys;

import com.metreeca.mesh.meta.jsonld.*;
import com.metreeca.mesh.meta.jsonld.Class;
import com.metreeca.mesh.meta.shacl.Required;

import java.net.URI;

/**
 * Base interface for all domain resources.
 *
 * <p>Defines the fundamental structure for domain entities including identity,
 * type classification, and human-readable labeling. All domain objects extend this interface to inherit consistent
 * identification and naming patterns.</p>
 */
@Frame
@Class
@Namespace(prefix="rdfs", value="http://www.w3.org/2000/01/rdf-schema#")
public interface Resource extends Toys {

    /**
     * Retrieves the unique identifier for this resource.
     *
     * @return the resource URI
     */
    @Id
    URI id();

    /**
     * Retrieves the resource type classification.
     *
     * @return the resource type identifier
     */
    @Type
    String type();


    /**
     * Retrieves the human-readable label for this resource.
     *
     * @return the resource label
     */
    @Required
    @Forward("rdfs:")
    String label();

}
