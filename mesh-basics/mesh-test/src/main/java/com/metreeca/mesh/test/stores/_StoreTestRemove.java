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

package com.metreeca.mesh.test.stores;

import com.metreeca.mesh.tools.Store;
import com.metreeca.mesh.tools.StoreException;
import com.metreeca.mesh.toys.EmployeeFrame;
import com.metreeca.mesh.toys.EventFrame;
import com.metreeca.mesh.toys.OfficeFrame;

import org.junit.jupiter.api.Test;

import java.net.URI;

import static com.metreeca.mesh.Value.*;
import static com.metreeca.mesh.queries.Query.query;
import static com.metreeca.mesh.test.stores.StoreTest.*;
import static com.metreeca.mesh.toys.Resources.EMPLOYEES;
import static com.metreeca.shim.URIs.uri;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

abstract class _StoreTestRemove {

    protected abstract Store store();


    @Test void testRemoveEmptyArrays() {

        final Store store=populate(store());

        store.remove(array());

        assertThat(store.retrieve(new OfficeFrame().id(uri("/offices/1"))).isEmpty()).isFalse();
    }

    @Test void testRemoveArrays() {

        final Store store=populate(store());
        final URI id=item("/employees/1702");

        store.remove(array(employee(id)));

        assertThat(store.retrieve(new EmployeeFrame()
                .id(id)
                .seniority(0)
        ).isEmpty()).isTrue();

    }


    @Test void testRemoveExistingResources() {

        final Store store=populate(store());
        final URI id=item("/employees/1702");

        store.remove(employee(id));

        assertThat(store.retrieve(new EmployeeFrame()
                .id(id)
                .seniority(0)
        ).isEmpty()).isTrue();

    }

    @Test void testIgnoreUnknowResources() {

        final Store store=populate(store());
        final URI id=item("/employees/9999");

        store.remove(employee(id));

        assertThat(store.retrieve(new EmployeeFrame()
                .id(id)
        ).isEmpty()).isTrue();

    }

    @Test void testRemoveForeignProperties() {

        final Store store=populate(store());

        final URI supervisor=item("/employees/1102");
        final URI report=item("/employees/1702");

        store.remove(employee(supervisor));

        assertThat(store.retrieve(new EmployeeFrame()
                .id(report)
                .supervisor(new EmployeeFrame(true).id(uri()))
        )).extracting(EmployeeFrame::new).satisfies(employee -> {
            assertThat(employee.supervisor()).isNull();
        });

    }

    @Test void testRemoveEmbeddedFrames() {

        final Store store=store();

        final EmployeeFrame employee=EMPLOYEES.getFirst();

        store.insert(employee);
        store.remove(employee);

        assertThat(store.retrieve(new EmployeeFrame(true).id(employee.id())).isEmpty()).isTrue();
        assertThat(store.retrieve(value(query(new EventFrame(true)))).isEmpty()).isTrue();
    }


    @Test void testReportInvalidResources() {
        assertThatExceptionOfType(StoreException.class).isThrownBy(() -> store().remove(

                new EmployeeFrame(true) // missing id

        ));
    }

    @Test void testReportUnderspecifiedResources() {
        assertThatExceptionOfType(StoreException.class).isThrownBy(() -> store().remove(

                employee(item("/employees/1")).id(null)

        ));
    }

    @Test void testReportUnsupportedValues() {
        assertThatExceptionOfType(StoreException.class).isThrownBy(() -> store().remove(

                String()

        ));
    }

}
