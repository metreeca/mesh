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

package com.metreeca.mesh.queries;

import com.metreeca.mesh.Value;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static com.metreeca.mesh.Value.*;
import static com.metreeca.mesh.queries.Criterion.criterion;
import static com.metreeca.shim.Collections.set;

import static org.assertj.core.api.Assertions.*;

final class CriterionTest {

    @Nested
    final class ConfigureTest {

        @Test void testConfigureOrder() {
            assertThat(criterion().order()).isEmpty();
            assertThat(criterion().order(1).order()).contains(1);
        }

        @Test void testConfigureFocus() {
            assertThat(criterion().focus()).isEmpty();
            assertThat(criterion().focus(set()).focus()).isEmpty();
            assertThat(criterion().focus(set(Nil(), integer(1))).focus()).contains(set(new Value[]{ Nil(), integer(1) }));
        }

        @Test void testConfigureLt() {
            assertThat(criterion().lt()).isEmpty();
            assertThat(criterion().lt(integer(1)).lt()).contains(integer(1));
        }

        @Test void testConfigureGt() {
            assertThat(criterion().gt()).isEmpty();
            assertThat(criterion().gt(integer(1)).gt()).contains(integer(1));
        }

        @Test void testConfigureLte() {
            assertThat(criterion().lte()).isEmpty();
            assertThat(criterion().lte(integer(1)).lte()).contains(integer(1));
        }

        @Test void testConfigureGte() {
            assertThat(criterion().gte()).isEmpty();
            assertThat(criterion().gte(integer(1)).gte()).contains(integer(1));
        }

        @Test void testReportIncomparableLimits() {

            assertThatIllegalArgumentException().isThrownBy(() -> criterion().lt(bit(true)).lte(string("")));
            assertThatIllegalArgumentException().isThrownBy(() -> criterion().lt(bit(true)).gt(string("")));
            assertThatIllegalArgumentException().isThrownBy(() -> criterion().lt(bit(true)).gte(string("")));

            assertThatIllegalArgumentException().isThrownBy(() -> criterion().gt(bit(true)).lt(string("")));
            assertThatIllegalArgumentException().isThrownBy(() -> criterion().gt(bit(true)).lte(string("")));
            assertThatIllegalArgumentException().isThrownBy(() -> criterion().gt(bit(true)).gte(string("")));

            assertThatIllegalArgumentException().isThrownBy(() -> criterion().lte(bit(true)).lt(string("")));
            assertThatIllegalArgumentException().isThrownBy(() -> criterion().lte(bit(true)).gt(string("")));
            assertThatIllegalArgumentException().isThrownBy(() -> criterion().lte(bit(true)).gte(string("")));

            assertThatIllegalArgumentException().isThrownBy(() -> criterion().gte(bit(true)).lt(string("")));
            assertThatIllegalArgumentException().isThrownBy(() -> criterion().gte(bit(true)).lte(string("")));
            assertThatIllegalArgumentException().isThrownBy(() -> criterion().gte(bit(true)).gt(string("")));

        }

        @Test void testReportInconsistentLimits() {

            assertThatIllegalArgumentException().isThrownBy(() -> criterion().lt(integer(1)).lte(integer(1)));
            assertThatIllegalArgumentException().isThrownBy(() -> criterion().gt(integer(1)).gte(integer(1)));

            assertThatNoException().isThrownBy(() -> criterion().gt(integer(1)).lt(integer(10)));
            assertThatIllegalArgumentException().isThrownBy(() -> criterion().gt(integer(1)).lt(integer(1)));

            assertThatNoException().isThrownBy(() -> criterion().gt(integer(1)).lte(integer(1)));
            assertThatIllegalArgumentException().isThrownBy(() -> criterion().gt(integer(10)).lte(integer(1)));

            assertThatNoException().isThrownBy(() -> criterion().gte(integer(1)).lt(integer(1)));
            assertThatIllegalArgumentException().isThrownBy(() -> criterion().gte(integer(10)).lt(integer(1)));

            assertThatNoException().isThrownBy(() -> criterion().gte(integer(1)).lte(integer(1)));
            assertThatIllegalArgumentException().isThrownBy(() -> criterion().gte(integer(10)).lte(integer(1)));

        }

        @Test void testConfigureLike() {
            assertThat(criterion().like()).isEmpty();
            assertThat(criterion().like("").like()).isEmpty();
            assertThat(criterion().like("pattern").like()).contains("pattern");
        }

        @Test void testConfigureAny() {
            assertThat(criterion().any()).isEmpty();
            assertThat(criterion().any(set()).any()).isNotEmpty();
            assertThat(criterion().any(set(Nil(), integer(1))).any()).contains(set(new Value[]{ Nil(), integer(1) }));
        }

    }

    @Nested
    final class MergeTest {

        private static final Value _1=integer(1);
        private static final Value _2=integer(2);
        private static final Value _3=integer(3);


        @Test void testMergeOrder() {
            assertThat(criterion().order(1).merge(criterion())).isEqualTo(criterion().order(1));
            assertThat(criterion().merge(criterion().order(1))).isEqualTo(criterion().order(1));
            assertThat(criterion().order(1).merge(criterion().order(1))).isEqualTo(criterion().order(1));
        }

        @Test void testReportInconsistentOrders() {
            assertThatIllegalArgumentException().isThrownBy(() ->
                    criterion().order(1).merge(criterion().order(2))
            );
        }

        @Test void testMergeFocus() {
            assertThat(criterion().focus(_1).merge(criterion())).isEqualTo(criterion().focus(_1));
            assertThat(criterion().merge(criterion().focus(_1))).isEqualTo(criterion().focus(_1));
            assertThat(criterion().focus(_1, _2).merge(criterion().focus(_1, _3))).isEqualTo(criterion().focus(_1, _2, _3));
        }

        @Test void testMergeLt() {
            assertThat(criterion().lt(_1).merge(criterion())).isEqualTo(criterion().lt(_1));
            assertThat(criterion().merge(criterion().lt(_1))).isEqualTo(criterion().lt(_1));
            assertThat(criterion().lt(_1).merge(criterion().lt(_2))).isEqualTo(criterion().lt(_1));
        }

        @Test void testMergeLte() {
            assertThat(criterion().lte(_1).merge(criterion())).isEqualTo(criterion().lte(_1));
            assertThat(criterion().merge(criterion().lte(_1))).isEqualTo(criterion().lte(_1));
            assertThat(criterion().lte(_1).merge(criterion().lte(_2))).isEqualTo(criterion().lte(_1));
        }

        @Test void testMergeGt() {
            assertThat(criterion().gt(_1).merge(criterion())).isEqualTo(criterion().gt(_1));
            assertThat(criterion().merge(criterion().gt(_1))).isEqualTo(criterion().gt(_1));
            assertThat(criterion().gt(_1).merge(criterion().gt(_2))).isEqualTo(criterion().gt(_2));
        }

        @Test void testMergeGte() {
            assertThat(criterion().gte(_1).merge(criterion())).isEqualTo(criterion().gte(_1));
            assertThat(criterion().merge(criterion().gte(_1))).isEqualTo(criterion().gte(_1));
            assertThat(criterion().gte(_1).merge(criterion().gte(_2))).isEqualTo(criterion().gte(_2));
        }

        @Test void testMergeLike() {
            assertThat(criterion().like("x").merge(criterion())).isEqualTo(criterion().like("x"));
            assertThat(criterion().merge(criterion().like("x"))).isEqualTo(criterion().like("x"));
            assertThat(criterion().like("x").merge(criterion().like("x"))).isEqualTo(criterion().like("x"));
        }

        @Test void testReportInconsistentLikes() {
            assertThatIllegalArgumentException().isThrownBy(() ->
                    criterion().like("x").merge(criterion().like("y"))
            );
        }

        @Test void testMergeAny() {
            assertThat(criterion().any(_1).merge(criterion())).isEqualTo(criterion().any(_1));
            assertThat(criterion().merge(criterion().any(_1))).isEqualTo(criterion().any(_1));
            assertThat(criterion().any(set()).merge(criterion().any(set()))).isEqualTo(criterion().any(set()));
            assertThat(criterion().any(_1).merge(criterion().any(_1))).isEqualTo(criterion().any(_1));
            assertThat(criterion().any(_1, _2, _3).merge(criterion().any(_1, _2))).isEqualTo(criterion().any(_1, _2));
        }

        @Test void testReportInconsistentAnys() {
            assertThatIllegalArgumentException().isThrownBy(() ->
                    criterion().any(_1, _2).merge(criterion().any(_2, _3))
            );
        }

    }

}
