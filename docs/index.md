<!--- # Metreeca/Mesh --->

[![Maven Central](https://img.shields.io/maven-central/v/com.metreeca/mesh.svg)](https://central.sonatype.com/artifact/com.metreeca/mesh/)

Metreeca/Mesh is a lightweight [linked data](https://www.w3.org/2013/data/) processing framework.

[JavaBeans](https://download.oracle.com/otndocs/jcp/7224-javabeans-1.01-fr-spec-oth-JSpec/)

Its core provide intuitive annotation sets for:

- mapping JavaBeans classes and properties to the [JSON-LD](https://www.w3.org/TR/json-ld11/) interchange data model;
- defining validation rules for JavaBeans properties, loosely based on the [SHACL](https://www.w3.org/TR/shacl/) shapes
  constraint language.

Model-driven processing engines leverage JavaBeans conventions and annotations to:

* validate JavaBeans objects;
* convert JavaBean object networks to / from idiomatic serialised representations;
* persist JavaBeans object networks to storage backends, providing out of the box support for data validation, CRUD
  operations, faceted search and client-driven queries on a par with [GraphQL](https://graphql.org/learn/queries/).

# Modules

|      area | javadocs                                                     | description                            |
|----------:|:-------------------------------------------------------------|:---------------------------------------|
| framework | [mesh-core](https://javadoc.io/doc/com.metreeca/mesh-core)   | JSON-LD data model                     |
|           | [mesh-bean](https://javadoc.io/doc/com.metreeca/mesh-bean)   | JavaBeans mapping                      |
|    codecs | [mesh-json](https://javadoc.io/doc/com.metreeca/mesh-json)   | JSON codec                             |
|    stores | [edge‑rdf4j](https://javadoc.io/doc/com.metreeca/mesh-rdf4j) | [RDF4J](https://rdf4j.org) graph store |

# Getting Started

1. Add the framework BOM and the relevant serialisation/persistence modules to your Maven dependencies, for instance:

```xml 
<project>

    <dependencyManagement>

        <dependencies>

            <dependency>
                <groupId>com.metreeca</groupId>
                <artifactId>mesh</artifactId>
                <version>{{meta.version}}</version>
            </dependency>

        </dependencies>

    </dependencyManagement>

    <dependencies>

        <dependency>
            <groupId>com.metreeca</groupId>
            <artifactId>mesh-jsonld</artifactId>
        </dependency>

        <dependency>
            <groupId>com.metreeca</groupId>
            <artifactId>mesh-rdf4j</artifactId>
        </dependency>

    </dependencies>

</project>
```

2. Define a JavaBeans application data model, for instance:

```java
import jsonld.com.metreeca.mesh.bean.Namespace;
import shacl.com.metreeca.mesh.bean.Optional;
import shacl.com.metreeca.mesh.bean.Pattern;
import shacl.com.metreeca.mesh.bean.Required;

import java.beans.JavaBean;

@JavaBean
@Namespace("https://schema.org/")
public final class Person {

    @Id
    private URI identifier;

    @Required
    private String givenName;

    @Required
    private String familyName;

    // getters and setters…

}
```

3. Create a JavaBeans codec to convert your data to / from a serialised representation, for instance:

```java
public final class Codecs {

    public static void main(final String... args) {

        final Person person=create(Person.class);

        person.setGivenName("Tino");
        person.setFamilyName("Faussone");

        final JSONCodec codec=new JSONCodec().indent(4);

        final String encoded=codec.encode(person);

        final Person decoded=codec.decode(
                """{
                  "givenName": "Tino",
                  "familyName": "Faussone"
                }""",
                Person.class
        );

    }

}
```

4. Create a JavaBeans store to persist your data to a storage backend, for instance:

```java
public final class Stores {

    public static void main(final String... args) {

        final Store store=new RDF4JStore();

        final URI id=URI.create("/persons/123");

        final Person person=create(Person.class);

        person.setIdentifier(id);
        person.setGivenName("Tino");
        person.setFamilyName("Faussone");

        store.create(person);

        final Person model=create(Person.class);

        model.setIdentifier(id);

        final Person retrieved=store.retrieve(model);

    }

}
```

5. Delve into the [docs](https://metreeca.github.io/bean/) to learn the details about:

    - defining and annotating JavaBeans data models;
    - converting data to / from serialisation formats;
    - persisting data to storage backends.

# Support

- open an [issue](https://github.com/metreeca/bean/issues) to report a problem or to suggest a new feature
- start a [discussion](https://github.com/metreeca/bean/discussions) to ask a how-to question or to share an idea

# License

This project is licensed under the Apache 2.0 License –
see [LICENSE](https://github.com/metreeca/bean/blob/main/LICENSE) file for details.
