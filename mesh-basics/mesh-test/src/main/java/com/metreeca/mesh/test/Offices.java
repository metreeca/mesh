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

package com.metreeca.mesh.test;

import com.metreeca.mesh.meta.jsonld.Frame;

import java.net.URI;

import static com.metreeca.shim.URIs.uri;

@Frame
public interface Offices extends Catalog<Office> {

    @Override
    default URI id() {
        return uri("/offices/");
    }

    @Override
    default String label() {
        return "Offices";
    }

}
