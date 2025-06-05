package com.metreeca.mesh.toys;

import com.metreeca.mesh.meta.shacl.Alias;
import com.metreeca.mesh.meta.shacl.LanguageIn;

@Alias(
        LanguageIn=@LanguageIn({ "en", "fr", "de" })
)
public @interface Localized { }
