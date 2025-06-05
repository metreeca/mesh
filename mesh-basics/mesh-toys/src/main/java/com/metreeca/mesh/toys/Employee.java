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
import com.metreeca.mesh.meta.shacl.MaxInclusive;
import com.metreeca.mesh.meta.shacl.MinInclusive;
import com.metreeca.mesh.meta.shacl.Pattern;
import com.metreeca.mesh.meta.shacl.Required;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Set;

/**
 * Employee records with personal, organizational, and career information.
 *
 * <p>Represents personnel within the organization including personal details,
 * job information, organizational relationships, performance metrics, and career history. Employees are linked to
 * offices and may have supervisory relationships with other employees.</p>
 */
@Frame
@Class
public interface Employee extends Resource {

    @Override
    default String label() {
        return forename() != null && surname() != null
                ? "%s %s".formatted(forename(), surname())
                : null;
    }


    /**
     * Retrieves the employee identification code.
     *
     * @return the numeric employee code
     */
    @Required
    @Pattern("^\\d+$")
    String code();


    /**
     * Retrieves the employee's first name.
     *
     * @return the employee forename
     */
    @Required
    String forename();

    /**
     * Retrieves the employee's last name.
     *
     * @return the employee surname
     */
    @Required
    String surname();

    /**
     * Retrieves the employee's birth date.
     *
     * @return the employee birthdate
     */
    @Required
    LocalDate birthdate();


    /**
     * Retrieves the employee's job title.
     *
     * @return the employee job title
     */
    @Required
    String title();

    /**
     * Retrieves the employee's seniority level.
     *
     * @return the seniority level (1-5)
     */
    @Required
    @MinInclusive("1")
    @MaxInclusive("5")
    int seniority();

    /**
     * Retrieves the employee's email address.
     *
     * @return the employee email
     */
    @Required
    @Pattern("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")
    String email();

    /**
     * Retrieves when the employee became active.
     *
     * @return the activation timestamp
     */
    @Required
    Instant active();


    /**
     * Retrieves year-to-date performance metrics.
     *
     * @return the YTD value
     */
    @MinInclusive("0")
    double ytd();

    /**
     * Retrieves last period performance metrics.
     *
     * @return the last period value
     */
    @MinInclusive("0")
    double last();

    /**
     * Retrieves performance change metrics.
     *
     * @return the performance delta
     */
    double delta();


    /**
     * Retrieves the office where this employee works.
     *
     * @return the employee's assigned office
     */
    @Required
    Office office();

    /**
     * Retrieves the employee's direct supervisor.
     *
     * @return the supervising employee, or {@code null} if none
     */
    Employee supervisor();

    /**
     * Retrieves employees reporting to this employee.
     *
     * @return the set of direct reports
     */
    @Foreign
    @Reverse("supervisor")
    Set<Employee> reports();


    /**
     * Retrieves career events for this employee.
     *
     * @return the set of career milestones
     */
    @Embedded
    Set<Event> career();

}
