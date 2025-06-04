---
title: "Tutorials"
---

This series of example-driven tutorials introduces the main building blocks of the Metreeca/Mesh model-driven linked
data framework. Basic familiarity with [linked data](https://www.w3.org/standards/semanticweb/data)
concepts, [JSON](https://www.json.org/) and [REST](https://en.wikipedia.org/wiki/Representational_state_transfer) APIs
is required.

In these tutorials we will work with a linked data subset of the [BIRT](http://www.eclipse.org/birt/phoenix/db/) sample
dataset. BIRT is a typical business database containing tables such as *offices*, *customers*, *products*, *orders*,
*order lines*, … for *Classic Models*, a fictional world-wide retailer of scale toy models.

- [Defining JSON-LD Data Models](defining-data-models.md)

  Learn the fundamentals of creating structured data models using JSON-LD specifications and shape constraints.
  This tutorial covers schema definition, property modeling, and data validation rules for linked data applications.

- [Defining JSON-LD Data Models with Annotated Java Interfaces](defining-data-models-with-java.md)

  Discover how to leverage Java annotations to define data models declaratively through type-safe interfaces.
  This approach provides compile-time DTO generation and seamless integration with modern Java development workflows.

- [Encoding and Decoding Data](serialising-data.md)

  Master the process of converting between different data formats and representations in linked data systems.
  Learn to transform RDF graphs to JSON-LD and implement custom codecs for application-specific serialization needs.

- [Persisting Data to Storage Backends](persisting-data.md)

  Explore data persistence strategies for linked data applications across various storage technologies.
  Cover RDF triple stores, relational databases, and hybrid approaches for scalable data management solutions.

- [Publishing Model-Driven REST/JSON-LD APIs](publishing-jsonld-apis.md)

  Build production-ready REST APIs that automatically support CRUD operations, faceted search, and role-based access
  control.
  Learn to create self-documenting APIs with built-in validation, data transformation, and comprehensive error handling.

- [Consuming Model-Driven REST/JSON-LD APIs](consuming-jsonld-apis.md)

  Master client-side integration with model-driven APIs, leveraging automatic faceted search and validation features.
  Covers authentication, error handling, data filtering, and building responsive applications with linked data backends.
