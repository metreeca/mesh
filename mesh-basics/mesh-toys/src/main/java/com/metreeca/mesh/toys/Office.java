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
import com.metreeca.mesh.meta.shacl.Pattern;
import com.metreeca.mesh.meta.shacl.Required;

import java.time.ZoneId;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

@Frame
@Class
public interface Office extends Resource {

    @Override
    default String label() {
        return city() != null && country() != null
                ? "%s - %s".formatted(city(), country())
                : null;
    }


    @Required
    @Pattern("^\\d+$")
    String code();


    @Required
    String city();

    @Required
    Map<Locale, String> country();


    @Foreign
    @Reverse("office")
    Set<Employee> employees();


    @Internal
    ZoneId zone();

}
