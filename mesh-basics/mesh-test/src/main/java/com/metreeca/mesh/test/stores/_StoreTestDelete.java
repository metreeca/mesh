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

import com.metreeca.mesh.pipe.Store;

/**
 * Test framework for store delete operations.
 *
 * <p>Validates data deletion capabilities for existing resources with proper
 * cascading behavior and constraint checking. Tests ensure complete resource removal and link cleanup for the
 * {@linkplain Store} implementation.</p>
 */
abstract class _StoreTestDelete {

    /**
     * Retrieves the store instance for testing.
     *
     * @return the store under test
     */
    protected abstract Store store();


    // @Test void testReportUnknownResources() {
    //
    //     final Store store=store();
    //
    //     final IRI id=item("/employees/9999");
    //
    //     assertThat(store.delete(id, Employee()))
    //             .isFalse();
    //
    // }
    //
    //
    // @Test void testDeleteResource() {
    //
    //     final Store store=populate(store());
    //
    //     final IRI id=item("/employees/1702");
    //
    //     assertThat(store.delete(id, Employee()))
    //             .isTrue();
    //
    //     assertThat(store.retrieve(id, Employee(), frame(
    //             field(RDFS.LABEL, literal(""))
    //     )))
    //             .isEmpty();
    //
    // }
    //
    // @Test void testDeleteIncomingLinks() {
    //
    //     final Store store=populate(store());
    //
    //     final IRI id=item("/employees/1702");
    //
    //     assertThat(store.delete(id, Employee()))
    //             .isTrue();
    //
    //     assertThat(store.retrieve(item("/employees/1102"), Employee(), frame(
    //             field(REPORT, iri())
    //     )))
    //             .hasFrameatisfying(supervisor -> assertThat(supervisor.values(REPORT))
    //                     .doesNotContain(id)
    //             );
    // }


    // @Test void testHandleNakedValuesQuery() { }

}
