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

import com.metreeca.mesh.Value;
import com.metreeca.mesh.meta.jsonld.*;
import com.metreeca.mesh.meta.jsonld.Class;
import com.metreeca.mesh.meta.shacl.*;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Queue;
import java.util.Set;
import java.util.function.Function;

import static com.metreeca.mesh.Value.*;

import static java.util.Collections.singleton;

/**
 * Employee records with personal, organizational, and career information.
 *
 * <p>Represents personnel within the organization including personal details,
 * job information, organizational relationships, performance metrics, and career history. Employees are linked to
 * offices and may have supervisory relationships with other employees.</p>
 */
@Frame
@Class
@Constraint(Employee.Integrity.class)
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


    //̸/////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public final class Integrity implements Function<Employee, Value> {

        @Override public Value apply(final Employee employee) {

            return object(

                    field("supervisor", supervisors(employee).contains(employee)
                            ? string("reference loop")
                            : Nil()
                    ),

                    field("reports", reports(employee).contains(employee)
                            ? string("reference loop")
                            : Nil()
                    )

            );
        }


        private Set<Employee> supervisors(final Employee employee) {

            final Set<Employee> supervisors=new HashSet<>();
            final Set<Employee> visited=new HashSet<>();

            Employee current=employee.supervisor();

            while ( current != null && visited.add(current) ) {
                supervisors.add(current);
                current=current.supervisor();
            }

            return supervisors;
        }

        private Set<Employee> reports(final Employee employee) {

            final Set<Employee> reports=new HashSet<>();

            final Set<Employee> visited=new HashSet<>(singleton(employee));
            final Queue<Employee> pending=new ArrayDeque<>(singleton(employee));

            while ( !pending.isEmpty() ) {

                final Employee current=pending.poll();

                for (final Employee report : current.reports()) {
                    if ( visited.add(report) ) {
                        reports.add(report);
                        pending.offer(report);
                    }
                }

            }

            return reports;
        }

    }

}
