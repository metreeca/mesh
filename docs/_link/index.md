<!-- Metreeca/Link -->

[![Maven Central](https://img.shields.io/maven-central/v/com.metreeca/link.svg)](https://central.sonatype.com/artifact/com.metreeca/link/)

Metreeca/Link is a model‑driven Java engine for rapid REST/JSON‑LD API development.

> **⚠️** Work in progress / Watch this repo to stay updated…

Its automatically converts high-level declarative JSON-LD models into extended REST/JSON-LD APIs with *out of the box*
support for data validation, CRUD operations and faceted search, relieving backend developers from low-level
chores and completely shielding frontend developers from linked data technicalities.

[![Maven Central](https://img.shields.io/maven-central/v/com.metreeca/link.svg)](https://central.sonatype.com/artifact/com.metreeca/link/)

# Modules

|                 area | javadocs                                                       | description                                     |
|---------------------:|:---------------------------------------------------------------|:------------------------------------------------|
|            framework | [link-core](https://javadoc.io/doc/com.metreeca/link-core)     | JSON-LD data model                              |
|   wire format codecs | [link-jsonld](https://javadoc.io/doc/com.metreeca/link-jsonld) | JSON-LD wire format codec                       |
| data storage engines | [link‑rdf4j](https://javadoc.io/doc/com.metreeca/link-rdf4j)   | [RDF4J](https://rdf4j.org) graph storage engine |

# Getting Started

> [!WARNING]
> To be completed…

1. Add the framework to your Maven configuration

```xml 

<project>

    <dependencyManagement>
        <dependencies>

            <dependency>
                <groupId>com.metreeca</groupId>
                <artifactId>link</artifactId>
                <version>{{meta.version}}</version>
                <type>pom</type>
                <scope>import</scope>
            </dependency>

        </dependencies>
    </dependencyManagement>

    <dependencies>

        …

    </dependencies>

</project>
```

2. …

# Support

- open an [issue](https://github.com/metreeca/link/issues) to report a problem or to suggest a new feature
- start a [discussion](https://github.com/metreeca/link/discussions) to ask a how-to question or to share an idea

# License

This project is licensed under the Apache 2.0 License –
see [LICENSE](https://github.com/metreeca/link/blob/main/LICENSE) file for details.
