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

import org.junit.jupiter.api.Test;

import static com.metreeca.mesh.Value.array;
import static com.metreeca.mesh.test.stores.StoreTest.Employees;
import static com.metreeca.mesh.test.stores.StoreTest.Offices;

import static org.assertj.core.api.Assertions.assertThat;

final class StoreTestTest {

    @Test void testOffices() {
        assertThat(Offices.size()).isEqualTo(7);
        assertThat(array(Offices).validate()).isEmpty();
    }

    @Test void testEmployees() {
        assertThat(Employees.size()).isEqualTo(23);
        assertThat(array(Employees).validate()).isEmpty();
    }

}
