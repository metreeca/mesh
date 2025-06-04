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

abstract class _StoreTestUpdate {

    protected abstract Store store();

    //
    // @Test void testUpdateEmptyArrays() {
    //     assertThat(store().update(array())).isTrue();
    // }
    //
    // @Test void testUpdateArrays() {
    //
    //     final Store store=populate(store());
    //     final URI id=item("/employees/1702");
    //
    //     assertThat(store.update(array(employee(id)
    //             .seniority(5)
    //     ))).isTrue();
    //
    //     assertThat(store.retrieve(new EmployeeFrame()
    //             .id(id)
    //             .seniority(0)
    //             .supervisor(new EmployeeFrame(true).id(uri()))
    //     )).map(EmployeeFrame::new).hasValueSatisfying(updated -> {
    //         assertThat(updated.seniority()).isEqualTo(5);
    //         assertThat(updated.supervisor()).isNull(); // not specified in the update
    //     });
    //
    // }
    //
    // @Test void testUpdateResources() {
    //
    //     final Store store=populate(store());
    //     final URI id=item("/employees/1702");
    //
    //     assertThat(store.update(employee(id)
    //             .seniority(5)
    //     )).isTrue();
    //
    //     assertThat(store.retrieve(new EmployeeFrame()
    //             .id(id)
    //             .seniority(0)
    //             .supervisor(new EmployeeFrame(true).id(uri()))
    //     )).map(EmployeeFrame::new).hasValueSatisfying(updated -> {
    //         assertThat(updated.seniority()).isEqualTo(5);
    //         assertThat(updated.supervisor()).isNull(); // not specified in the update
    //     });
    //
    // }
    //
    //
    // @Test void testReportInvalidResources() {
    //     assertThatExceptionOfType(StoreException.class).isThrownBy(() -> store().update(
    //
    //             employee(item("/employees/1702"))
    //                     .code(null) // missing required properties
    //
    //     ));
    // }
    //
    // @Test void testReportUnderspecifiedResources() {
    //     assertThatExceptionOfType(StoreException.class).isThrownBy(() -> store().update(
    //
    //             employee(item("/employees/1702")).id(null)
    //
    //     ));
    // }
    //
    // @Test void testReportUnsupportedValues() {
    //     assertThatExceptionOfType(StoreException.class).isThrownBy(() -> store().update(
    //
    //             String()
    //
    //     ));
    // }
    //
    //
    // @Test void testPreserveForeignProperties() {
    //
    //     final Store store=populate(store());
    //
    //     final URI supervisor=item("/employees/1102");
    //     final URI report=item("/employees/1702");
    //
    //     assertThat(store.update(employee(supervisor)
    //             .seniority(5)
    //     )).isTrue();
    //
    //     assertThat(store.retrieve(new EmployeeFrame()
    //             .id(supervisor)
    //             .reports(stash(query(new EmployeeFrame(true).id(uri()))))
    //     )).map(EmployeeFrame::new).hasValueSatisfying(updated ->
    //             assertThat(updated.reports()).anyMatch(employee -> employee.id().equals(report))
    //     );
    //
    // }
    //
    //
    // @Test void testReportUnknownResources() {
    //
    //     final Store store=store();
    //
    //     final EmployeeFrame employee=employee(item("/employees/999"));
    //
    //     assertThat(store.update(employee)).isFalse();
    //     assertThat(store.retrieve(employee)).isEmpty();
    // }
    //
    // @Test void testUpdateUnknownResourcesIfForced() {
    //
    //     final Store store=store();
    //
    //     final EmployeeFrame employee=employee(item("/employees/999"));
    //
    //     assertThat(store.update(employee)).isTrue();
    //     assertThat(store.retrieve(employee)).isNotEmpty();
    //
    // }

}
