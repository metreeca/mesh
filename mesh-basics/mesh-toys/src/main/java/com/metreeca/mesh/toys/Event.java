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

import com.metreeca.mesh.meta.jsonld.Class;
import com.metreeca.mesh.meta.jsonld.Frame;
import com.metreeca.mesh.meta.shacl.Required;

import java.time.LocalDate;

/**
 * Career events and milestones within employee records.
 *
 * <p>Represents significant career moments including hiring, promotions,
 * terminations, and retirements. Events are embedded within employee records to track career progression and
 * organizational changes over time.</p>
 */
@Frame
@Class
public interface Event extends Toys {

    /**
     * Types of career actions and organizational changes.
     */
    enum Action { HIRED, PROMOTED, TERMINATED, RETIRED }


    @Override
    default String label() {
        return action() != null && date() != null
                ? "%s %s".formatted(date(), action())
                : null;
    }


    @Required
    Action action();

    /**
     * Retrieves when this event occurred.
     *
     * @return the event date
     */
    @Required
    LocalDate date();

    /**
     * Retrieves additional notes about this event.
     *
     * @return the event notes, or {@code null} if none
     */
    String notes();

}
