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

package com.metreeca.mesh.tools;

import com.metreeca.mesh.Valuable;
import com.metreeca.mesh.Value;
import com.metreeca.mesh.queries.Query;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;

import static com.metreeca.shim.Collections.list;

/**
 * Persistence store.
 *
 * <p>Handles CRUD and bulk operations on a data storage backend.</p>
 *
 * <p>In the context of store operations, a <em>resource</em> is defined as an {@linkplain Value#Object() object} value
 * with an {@linkplain Value#id() id} and a {@linkplain Value#shape() shape}.</p>
 */
public interface Store extends AutoCloseable {

    default Value retrieve(final Valuable model, final Locale... locales) {

        if ( model == null ) {
            throw new NullPointerException("null model");
        }

        if ( locales == null || Arrays.stream(locales).anyMatch(Objects::isNull) ) {
            throw new NullPointerException("null locales");
        }

        return retrieve(model, list(locales));
    }

    /**
     * @param model   a resource value, an array of resource values or a {@linkplain Query query} value
     * @param locales
     *
     * @return
     *
     * @throws NullPointerException     if {@code value} is {@code null}
     * @throws IllegalArgumentException if {@code value} is not a supported value or if it is not
     *                                  {@linkplain Value#validate() valid}
     */
    Value retrieve(Valuable model, List<Locale> locales) throws StoreException;


    /**
     * Creates a resource.
     *
     * <p>Embedded values are recursively included in the state of the new resource; other nested values are included
     * only by reference.</p>
     *
     * <pre>
     * !!! define embedded
     * !!! define by reference
     * </pre>
     *
     * @param value a resource value or an array of resource values
     *
     * @return
     *
     * @throws NullPointerException     if {@code value} is {@code null}
     * @throws IllegalArgumentException if {@code value} is not a supported value or if it is not
     *                                  {@linkplain Value#validate() valid}
     */
    int create(Valuable value) throws StoreException;

    int update(Valuable value) throws StoreException;

    int mutate(Valuable value) throws StoreException;

    /**
     * @param value a resource value, an array of resource values or a {@linkplain Query query} value returning an array
     *              of resource values
     *
     * @return
     *
     * @throws NullPointerException     if {@code value} is {@code null}
     * @throws IllegalArgumentException if {@code value} is not a supported value or if it is not
     *                                  {@linkplain Value#validate() valid}
     */
    int delete(Valuable value) throws StoreException;


    /**
     * Bulk insertion.
     *
     * <ul>
     *     <li>existing resources are {@linkplain #update(Valuable) updated}</li>
     *     <li>unknown resources are created</li>
     * </ul>
     *
     * @param value a resource value or an array of resource values
     *
     * @return the number of inserted resources
     *
     * @throws NullPointerException     if {@code value} is {@code null}
     * @throws IllegalArgumentException if  {@code value} is not a supported value or if it is not
     *                                  {@linkplain Value#validate() valid}
     * @throws StoreException           if the store is not able to complete the operation
     */
    int insert(final Valuable value) throws StoreException;

    /**
     * Bulk removal.
     *
     * <ul>
     *     <li>existing resources are {@linkplain #delete(Valuable) deleted}</li>
     *     <li>unknown resources are ignored</li>
     * </ul>
     *
     * @param value a resource value, an array of resource values or a {@linkplain Query query} value returning an array
     *              of resource values
     *
     * @return the number of removed resources
     *
     * @throws NullPointerException     if {@code value} is {@code null}
     * @throws IllegalArgumentException if {@code value} is not a supported value or if it is not
     *                                  {@linkplain Value#validate() valid}
     * @throws StoreException           if the store is not able to complete the operation
     */
    int remove(final Valuable value) throws StoreException;


    /**
     * Bulk modification.
     *
     * <p>Contextual {@linkplain #remove(Valuable) removal} and {@linkplain #insert(Valuable) insertion}: make it
     * convenient to precompute insert values on the basis of the current store state before removals take effect.</p>
     *
     * <p>Resources included in the {@code remove} value or matched by the {@code remove} query are ignored if they are
     * also included in the {@code insert} value.</p>
     *
     * @param insert a resource value or an array of resource values
     * @param remove a resource value, an array of resource values or a {@linkplain Query query} value returning an
     *               array of resource values
     *
     * @return the number of modified resources
     *
     * @throws NullPointerException     either {@code remove} or {@code insert} is {@code null}
     * @throws IllegalArgumentException if either {@code remove} or {@code insert} is not a supported value or if it is
     *                                  not {@linkplain Value#validate() valid}
     * @throws StoreException           if the store is not able to complete the operation
     */
    int modify(final Valuable insert, final Valuable remove) throws StoreException;


    //̸/////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    default void execute(final Consumer<Store> task) {

        if ( task == null ) {
            throw new NullPointerException("null task");
        }

        execute(store -> {

            task.accept(this);

            return null;

        });

    }

    <V> V execute(Function<Store, V> task);


    //̸/////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    default void close() throws Exception { }

}
