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

import java.util.Locale;
import java.util.Optional;
import java.util.regex.Pattern;

import static java.util.Locale.ROOT;

public final class Locales {

    /**
     * A wildcard language tag used in models to match any locale.
     */
    public static final String ANY="*";

    /**
     * A wildcard variant of the {@linkplain Locale#ROOT root locale} used in models to match any locale.
     */
    public static final Locale ANY_LOCALE=new Locale.Builder()
            .setLocale(ROOT)
            .setVariant("wildcard")
            .build();

    private static final Pattern TAG_PATTERN=Pattern.compile("[a-z]{2}(?:-[a-zA-Z0-9]+)*");


    public static Locale locale(final String locale) {

        if ( locale == null ) {
            throw new NullPointerException("null locale");
        }

        return locale.isEmpty() ? ROOT : locale.equals(ANY) ? ANY_LOCALE : Optional.of(locale)
                .filter(TAG_PATTERN.asMatchPredicate())
                .map(Locale::forLanguageTag)
                .orElseThrow(() -> new IllegalArgumentException("malformed locale <%s>".formatted(locale)));
    }

    public static String locale(final Locale locale) {

        if ( locale == null ) {
            throw new NullPointerException("null locale");
        }
        return locale.equals(ROOT) ? "" : locale.equals(ANY_LOCALE) ? ANY : locale.getLanguage();
    }


    //̸/////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private Locales() { }

}
