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

abstract class StoreTestUpdate {

    // // !!! cascade composite shapes
    //
    // protected abstract Store store();
    //
    //
    // @Test void testReportUnknownResources() {
    //
    //     final Store store=store();
    //
    //     final IRI id=item("/employees/9999");
    //
    //     assertThat(store.update(id, Employee(), frame()))
    //             .isFalse();
    //
    // }
    //
    //
    // @Test void testUpdateResource() {
    //
    //     final Store store=populate(store());
    //
    //     final IRI id=item("/employees/1702");
    //
    //
    //     final Frame initial=Employee(id).orElseThrow();
    //
    //     final Frame updated=initial.update(
    //             field(SENIORITY, literal(5))
    //     );
    //
    //
    //     assertThat(store.update(id, Employee(), updated))
    //             .isTrue();
    //
    //
    //     assertThat(store.retrieve(id, Employee(), frame(
    //             field(SENIORITY, literal(0))
    //     )))
    //
    //             .hasValueSatisfying(actual -> assertThat(actual.values(SENIORITY))
    //                     .containsExactly(literal(5))
    //             );
    //
    // }
    //
    // @Test void testPreserveIncomingLinks() {
    //
    //     final Store store=populate(store());
    //
    //     final IRI id=item("/employees/1702");
    //
    //
    //     final Frame initial=Employee(id).orElseThrow();
    //
    //     final Frame updated=initial.update(
    //             field(SENIORITY, literal(5))
    //     );
    //
    //
    //     assertThat(store.update(id, Employee(), updated))
    //             .isTrue();
    //
    //     assertThat(store.retrieve(item("/employees/1102"), Employee(), frame(
    //             field(REPORT, iri())
    //     )))
    //             .hasValueSatisfying(supervisor -> assertThat(supervisor.values(REPORT))
    //                     .contains(id)
    //             );
    // }
    //
    // // !!! updates to incoming links

}
