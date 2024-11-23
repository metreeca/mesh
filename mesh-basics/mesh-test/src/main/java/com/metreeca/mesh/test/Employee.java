/*
 * Copyright © 2025 Metreeca srl
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

import com.metreeca.mesh.meta.jsonld.*;
import com.metreeca.mesh.meta.jsonld.Class;
import com.metreeca.mesh.meta.shacl.MaxInclusive;
import com.metreeca.mesh.meta.shacl.MinInclusive;
import com.metreeca.mesh.meta.shacl.Pattern;
import com.metreeca.mesh.meta.shacl.Required;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Set;

@Frame
@Class
public interface Employee extends Resource {

    @Override
    default String label() {
        return forename() != null && surname() != null
                ? "%s %s".formatted(forename(), surname())
                : null;
    }


    @Required
    @Pattern("^\\d+$")
    String code();


    @Required
    String forename();

    @Required
    String surname();

    @Required
    LocalDate birthdate();


    @Required
    String title();

    @Required
    @MinInclusive("1")
    @MaxInclusive("5")
    int seniority();

    @Required
    @Pattern("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")
    String email();

    @Required
    Instant active();


    @MinInclusive("0")
    double ytd();

    @MinInclusive("0")
    double last();

    double delta();


    @Required
    Office office();

    Employee supervisor();

    @Foreign
    @Reverse("supervisor")
    Set<Employee> reports();


    @Embedded
    Set<Event> career();

}
