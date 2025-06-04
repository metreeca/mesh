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

package com.metreeca.mesh.test.stores;

import com.metreeca.mesh.test.EmployeeFrame;
import com.metreeca.mesh.test.EventFrame;
import com.metreeca.mesh.test.OfficeFrame;
import com.metreeca.mesh.tools.Store;
import com.metreeca.mesh.tools.StoreException;

import org.junit.jupiter.api.Test;

import java.net.URI;
import java.time.LocalDate;

import static com.metreeca.mesh.Value.*;
import static com.metreeca.mesh.queries.Query.query;
import static com.metreeca.mesh.test.Event.Action.*;
import static com.metreeca.mesh.test.Resources.EMPLOYEES;
import static com.metreeca.mesh.test.stores.StoreTest.*;
import static com.metreeca.shim.Collections.stash;
import static com.metreeca.shim.URIs.uri;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.util.Sets.set;

abstract class _StoreTestInsert {

    protected abstract Store store();


    @Test void testInsertEmptyArrays() {

        store().insert(array());

        assertThat(store().retrieve(new OfficeFrame().id(uri("/offices/1"))).isEmpty()).isTrue();
    }

    @Test void testInsertArrays() {

        final Store store=store();
        final URI id=item("/employees/1");

        store.insert(array(employee(id)));

        assertThat(store.retrieve(new EmployeeFrame()
                .id(id)
                .seniority(0)
        )).extracting(EmployeeFrame::new).satisfies(inserted -> {
            assertThat(inserted.seniority()).isEqualTo(1);
        });

    }


    @Test void testCreateUnknownResources() {

        final Store store=populate(store());
        final URI id=item("/employees/1702");

        store.insert(employee(id));

        assertThat(store.retrieve(new EmployeeFrame()
                .id(id)
                .seniority(0)
        )).extracting(EmployeeFrame::new).satisfies(inserted -> {
            assertThat(inserted.seniority()).isEqualTo(1);
        });

    }

    @Test void testUpdateExistingResources() {

        final Store store=populate(store());
        final URI id=item("/employees/1702");

        store.insert(employee(id)
                .seniority(5)
        );

        assertThat(store.retrieve(new EmployeeFrame()
                .id(id)
                .seniority(5)
        )).extracting(EmployeeFrame::new).satisfies(inserted -> {
            assertThat(inserted.seniority()).isEqualTo(5);
        });


    }

    @Test void testPreserveForeignProperties() {

        final Store store=populate(store());

        final URI supervisor=item("/employees/1102");
        final URI report=item("/employees/1702");

        store.insert(employee(supervisor)
                .seniority(5)
        );

        assertThat(store.retrieve(new EmployeeFrame()
                .id(supervisor)
                .reports(stash(query(new EmployeeFrame(true).id(uri()))))
        )).extracting(EmployeeFrame::new).satisfies(inserted -> {
            assertThat(inserted.reports()).anyMatch(employee -> BASE.resolve(employee.id()).equals(report));
        });

    }


    @Test void testCreateUnknownEmbeddedFrames() {

        final Store store=store();

        final EmployeeFrame employee=EMPLOYEES.getFirst();

        store.insert(employee);

        assertThat(store.retrieve(new EmployeeFrame(true)
                .id(employee.id())
                .career(stash(query(new EventFrame(true)
                        .action(HIRED)
                        .date(LocalDate.EPOCH)
                )))
        )).extracting(EmployeeFrame::new).satisfies(inserted ->
                assertThat(inserted.career())
                        .hasSize(2)
                        .anyMatch(e -> e.action() == HIRED && e.date().equals(LocalDate.parse("2001-03-15")))
                        .anyMatch(e -> e.action() == PROMOTED && e.date().equals(LocalDate.parse("2005-06-10")))
        );

    }

    @Test void testUpdateExistingEmbeddedFrames() {

        final Store store=store();

        final EmployeeFrame employee=EMPLOYEES.getFirst();

        store.insert(employee);
        store.insert(employee.career(set(new EventFrame().action(RETIRED).date(LocalDate.parse("2025-10-21")))));

        assertThat(store.retrieve(new EmployeeFrame(true)
                .id(employee.id())
                .career(stash(query(new EventFrame(true)
                        .action(HIRED)
                        .date(LocalDate.EPOCH)
                )))
        )).extracting(EmployeeFrame::new).satisfies(inserted ->
                assertThat(inserted.career())
                        .hasSize(1)
                        .anyMatch(e -> e.action() == RETIRED && e.date().equals(LocalDate.parse("2025-10-21")))
        );

        assertThat(store.retrieve(value(query(new EventFrame(true)))))
                .extracting(v -> v.array().orElseThrow())
                .as("previous embedded values are removed")
                .satisfies(v -> assertThat(v).hasSize(1));

    }


    @Test void testReportInvalidResources() {
        assertThatExceptionOfType(StoreException.class).isThrownBy(() -> store().insert(

                employee(item("/employees/1"))
                        .code(null) // missing required properties

        ));
    }

    @Test void testReportUnderspecifiedResources() {
        assertThatExceptionOfType(StoreException.class).isThrownBy(() -> store().insert(

                employee(item("/employees/1")).id(null)

        ));
    }

    @Test void testReportUnsupportedValues() {
        assertThatExceptionOfType(StoreException.class).isThrownBy(() -> store().insert(

                String()

        ));
    }

}
