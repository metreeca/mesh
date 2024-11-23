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

package com.metreeca.mesh.util;

import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Stream;

import static com.metreeca.mesh.util.Exceptions.error;

import static java.util.stream.Collectors.toMap;

@SuppressWarnings("AssignmentOrReturnOfFieldWithMutableType")
public final class Collections {

    private static final Set<?> EMPTY_SET=set(Stream.empty());
    private static final List<?> EMPTY_LIST=list(Stream.empty());
    private static final Map<?, ?> EMPTY_MAP=map(Stream.empty());


    @SuppressWarnings("unchecked") public static <V> Set<V> set() {
        return (Set<V>)EMPTY_SET;
    }

    public static <V> Set<V> set(final V item) {

        if ( item == null ) {
            throw new NullPointerException("null item");
        }

        return set(Stream.of(item));
    }

    @SafeVarargs public static <V> Set<V> set(final V... items) {

        if ( items == null || Arrays.stream(items).anyMatch(Objects::isNull) ) {
            throw new NullPointerException("null items");
        }

        return set(Arrays.stream(items));
    }

    public static <V> Set<V> set(final Collection<V> items) {

        if ( items == null ) {
            throw new NullPointerException("null items");
        }

        return items instanceof final ImmutableSet<V> immutable ? immutable : set(items.stream());
    }

    public static <V> Set<V> set(final Stream<V> items) {

        if ( items == null ) {
            throw new NullPointerException("null items");
        }

        return new ImmutableSet<>(new LinkedHashSet<>(items
                .map((V item) -> immutable(item, "null item"))
                .toList()
        ));
    }


    @SuppressWarnings("unchecked") public static <V> List<V> list() {
        return (List<V>)EMPTY_LIST;
    }

    public static <V> List<V> list(final V item) {

        if ( item == null ) {
            throw new NullPointerException("null item");
        }

        return list(Stream.of(item));
    }

    @SafeVarargs public static <V> List<V> list(final V... items) {

        if ( items == null || Arrays.stream(items).anyMatch(Objects::isNull) ) {
            throw new NullPointerException("null items");
        }

        return list(Arrays.stream(items));
    }

    public static <V> List<V> list(final Collection<V> items) {

        if ( items == null ) {
            throw new NullPointerException("null items");
        }

        return items instanceof final ImmutableList<V> immutable ? immutable : list(items.stream());
    }

    public static <V> List<V> list(final Stream<V> items) {

        if ( items == null ) {
            throw new NullPointerException("null items");
        }

        return new ImmutableList<>(items
                .map((V item) -> immutable(item, "null item"))
                .toList()
        );
    }


    @SuppressWarnings("unchecked") public static <K, V> Map<K, V> map() {
        return (Map<K, V>)EMPTY_MAP;
    }

    @SafeVarargs public static <K, V> Map<K, V> map(final Entry<K, V>... entries) {

        if ( entries == null ) {
            throw new NullPointerException("null entries");
        }

        return map(Arrays.stream(entries));
    }

    public static <K, V> Map<K, V> map(final Map<K, V> entries) {

        if ( entries == null ) {
            throw new NullPointerException("null entries");
        }

        return entries instanceof final ImmutableMap<K, V> immutable ? immutable : map(entries.entrySet().stream());
    }

    public static <K, V> Map<K, V> map(final Stream<Entry<K, V>> entries) {

        if ( entries == null ) {
            throw new NullPointerException("null entries");
        }

        return new ImmutableMap<>(entries
                .collect(toMap(
                        e -> immutable(e.getKey(), "null entry key"),
                        e -> immutable(e.getValue(), "null entry value"),
                        (x, y) -> { throw new AssertionError("unexpected merge conflict"); },
                        LinkedHashMap::new
                ))
        );
    }


    public static <K, V> Entry<K, V> entry(final K key, final V value) {

        if ( key == null ) {
            throw new NullPointerException("null key");
        }

        if ( value == null ) {
            throw new NullPointerException("null value");
        }

        return new ImmutableEntry<>(
                immutable(key, "null key"),
                immutable(value, "null value")
        );
    }

    public static <K, V> Entry<K, V> entry(final Entry<K, V> entry) {

        if ( entry == null ) {
            throw new NullPointerException("null entry");
        }

        if ( entry instanceof final ImmutableEntry<K, V> immutable ) { return immutable; } else {

            return new ImmutableEntry<>(
                    immutable(entry.getKey(), "null key"),
                    immutable(entry.getValue(), "null value")
            );


        }
    }


    @SuppressWarnings("unchecked") private static <V> V immutable(final V item, final String message) {
        return item == null ? error(new NullPointerException(message))

                : item instanceof ImmutableSet<?> ? item
                : item instanceof ImmutableList<?> ? item
                : item instanceof ImmutableMap<?, ?> ? item
                : item instanceof ImmutableEntry<?, ?> ? item

                : item instanceof final Set<?> set ? (V)set(set.stream())
                : item instanceof final List<?> list ? (V)list(list.stream())
                : item instanceof final Map<?, ?> map ? (V)map(map.entrySet().stream())
                : item instanceof final Entry<?, ?> entry ? (V)entry(entry)

                : item;
    }


    //̸/////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private Collections() { }


    //̸/////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private static record ImmutableSet<V>(Collection<V> delegate) implements Set<V> {

        @Override
        public boolean isEmpty() {
            return delegate.isEmpty();
        }

        @Override
        public boolean contains(final Object object) {
            return delegate.contains(object);
        }

        @Override
        public boolean containsAll(final Collection<?> collection) {
            return delegate.containsAll(collection);
        }

        @Override
        public int size() {
            return delegate.size();
        }

        @Override
        public Iterator<V> iterator() {
            return new ImmutableIterator<>(delegate.iterator());
        }

        @Override
        public Object[] toArray() {
            return delegate.toArray();
        }

        @Override
        public <A> A[] toArray(final A[] array) {
            return delegate.toArray(array);
        }


        @Override
        public void clear() {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean add(final V item) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean addAll(final Collection<? extends V> collection) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean retainAll(final Collection<?> collection) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean remove(final Object object) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean removeAll(final Collection<?> collection) {
            throw new UnsupportedOperationException();
        }


        @Override public boolean equals(final Object object) {
            return this == object
                   || object instanceof ImmutableSet<?>(final Set<?> set) && delegate.equals(set)
                   || delegate.equals(object);
        }

        @Override public int hashCode() {
            return delegate.hashCode();
        }

        @Override public String toString() {
            return getClass().getSimpleName()+delegate;
        }

    }

    private static record ImmutableList<V>(List<V> delegate) implements List<V> {

        @Override
        public boolean isEmpty() {
            return delegate.isEmpty();
        }

        @Override
        public boolean contains(final Object o) {
            return delegate.contains(o);
        }

        @Override
        public boolean containsAll(final Collection<?> collection) {
            return new HashSet<>(delegate).containsAll(collection);
        }

        @Override
        public int size() {
            return delegate.size();
        }

        @Override
        public Iterator<V> iterator() {
            return new ImmutableIterator<>(delegate.iterator());
        }

        @Override
        public ListIterator<V> listIterator() {
            return new ImmutableListIterator<>(delegate.listIterator());
        }

        @Override
        public ListIterator<V> listIterator(final int index) {
            return new ImmutableListIterator<>(delegate.listIterator(index));
        }

        @Override
        public V get(final int index) {
            return delegate.get(index);
        }

        @Override
        public int indexOf(final Object object) {
            return delegate.indexOf(object);
        }

        @Override
        public int lastIndexOf(final Object object) {
            return delegate.lastIndexOf(object);
        }

        @Override
        public List<V> subList(final int fromIndex, final int toIndex) {
            return new ImmutableList<>(delegate.subList(fromIndex, toIndex));
        }

        @Override
        public Object[] toArray() {
            return delegate.toArray();
        }

        @Override
        public <A> A[] toArray(final A[] array) {
            return delegate.toArray(array);
        }


        @Override
        public boolean add(final V item) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean remove(final Object object) {
            throw new UnsupportedOperationException();
        }

        @Override public boolean addAll(final Collection<? extends V> collection) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean addAll(final int index, final Collection<? extends V> c) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean removeAll(final Collection<?> c) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean retainAll(final Collection<?> c) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void clear() {
            throw new UnsupportedOperationException();
        }

        @Override
        public V set(final int index, final V element) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void add(final int index, final V element) {
            throw new UnsupportedOperationException();
        }

        @Override
        public V remove(final int index) {
            throw new UnsupportedOperationException();
        }


        @Override public boolean equals(final Object object) {
            return this == object
                   || object instanceof ImmutableList<?>(final List<?> list) && delegate.equals(list)
                   || delegate.equals(object);
        }

        @Override public int hashCode() {
            return delegate.hashCode();
        }

        @Override public String toString() {
            return getClass().getSimpleName()+delegate;
        }

    }

    private static record ImmutableMap<K, V>(Map<K, V> delegate) implements Map<K, V> {

        @Override
        public boolean isEmpty() {
            return delegate.isEmpty();
        }

        @Override
        public boolean containsKey(final Object key) {
            return delegate.containsKey(key);
        }

        @Override
        public boolean containsValue(final Object value) {
            return delegate.containsValue(value);
        }

        @Override
        public int size() {
            return delegate.size();
        }

        @Override
        public V get(final Object key) {
            return delegate.get(key);
        }

        @Override
        public Set<K> keySet() {
            return new ImmutableSet<>(delegate.keySet());
        }

        @Override
        public Collection<V> values() {
            return new ImmutableList<>(delegate.values().stream().toList());
        }

        @Override
        public Set<Entry<K, V>> entrySet() {
            return new ImmutableSet<>(delegate.entrySet());
        }


        @Override
        public void clear() { throw new UnsupportedOperationException(); }

        @Override
        public V put(final K key, final V value) { throw new UnsupportedOperationException(); }

        @Override
        public void putAll(final Map<? extends K, ? extends V> m) { throw new UnsupportedOperationException(); }

        @Override
        public V remove(final Object key) { throw new UnsupportedOperationException(); }


        @Override public boolean equals(final Object object) {
            return this == object
                   || object instanceof ImmutableMap<?, ?>(final Map<?, ?> map) && delegate.equals(map)
                   || delegate.equals(object);
        }

        @Override public int hashCode() {
            return delegate.hashCode();
        }

        @Override public String toString() {
            return getClass().getSimpleName()+delegate;
        }

    }


    private static record ImmutableEntry<K, V>(K key, V value) implements Entry<K, V> {

        @Override public K getKey() {
            return key;
        }

        @Override public V getValue() {
            return value;
        }

        @Override public V setValue(final V value) {
            throw new UnsupportedOperationException();
        }


        @Override public boolean equals(final Object object) {
            return this == object || object instanceof final Entry<?, ?> entry
                                     && key.equals(entry.getKey())
                                     && value.equals(entry.getValue());
        }

        @Override public int hashCode() {
            return key.hashCode()
                   ^value.hashCode();
        }

        @Override public String toString() {
            return "%s{%s: %s}".formatted(getClass().getSimpleName(), key, value);
        }

    }


    private static record ImmutableIterator<V>(Iterator<V> delegate) implements Iterator<V> {

        @Override
        public boolean hasNext() {
            return delegate.hasNext();
        }

        @Override
        public V next() {

            if ( !hasNext() ) {
                throw new NoSuchElementException();
            }

            return delegate.next();
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }

    }

    private static record ImmutableListIterator<V>(ListIterator<V> delegate) implements ListIterator<V> {

        @Override
        public boolean hasNext() {
            return delegate.hasNext();
        }

        @Override
        public V next() {

            if ( !hasNext() ) {
                throw new NoSuchElementException();
            }

            return delegate.next();
        }

        @Override
        public boolean hasPrevious() {
            return delegate.hasPrevious();
        }

        @Override
        public V previous() {

            if ( !hasPrevious() ) {
                throw new NoSuchElementException();
            }

            return delegate.previous();
        }

        @Override
        public int nextIndex() {
            return delegate.nextIndex();
        }

        @Override
        public int previousIndex() {
            return delegate.previousIndex();
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }

        @Override
        public void set(final V item) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void add(final V item) {
            throw new UnsupportedOperationException();
        }

    }

}
