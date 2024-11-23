# JSONDecoder Format

The `JSONDecoder` component parses JSON data into the Metreeca/Mesh data model. This document describes the JSON format
accepted by the decoder.

## Basic Values

- `null`: Decoded as a nil value
- `true`/`false`: Decoded as boolean values
- Numbers: Decoded as numeric values, with datatype inference based on the shape context
- Strings: Decoded as string values, with datatype inference based on the shape context

## Arrays

Arrays are decoded as collections of values. Special cases:

- Arrays containing a single query object are decoded as that query
- Arrays containing a single specs object are decoded as a query with that model
- Arrays cannot contain null values when a shape is provided
- Arrays cannot contain nested arrays when a shape is provided

## Objects

### Resource Objects

Standard objects are decoded as resources with properties:

```json
{
  "@id": "resource-uri",
  "property1": "value1",
  "property2": [
    "value2a",
    "value2b"
  ],
  "@type": "type-uri"
}
```

- `@id`: Resource identifier (URI)
- Properties: Name-value pairs representing resource properties
- `@type`: Optional resource type (URI)

### Literal Objects

Objects with specific keyword patterns are decoded as literal values:

```json
{
  "@value": "literal-value",
  "@type": "datatype-uri"
}
```

```json
{
  "@value": "literal-value",
  "@language": "language-tag"
}
```

### Text Objects

Objects with language-tagged text values:

```json
{
  "en": "English text",
  "fr": "French text",
  "it": [
    "Italian text 1",
    "Italian text 2"
  ]
}
```

### Query Objects

Objects with filter expressions, used within arrays:

```json
[
  {
    "property": "value",
    "<=path": value,
    ">=path": value,
    "<path": value,
    ">path": value,
    "~path": "keyword",
    "?path": [
      "value1",
      "value2"
    ],
    "^path": "increasing",
    "$path": [
      "focus1",
      "focus2"
    ],
    "@": 10,
    "#": 20
  }
]
```

Filter expressions:

- `<=path`: Less than or equal (max inclusive)
- `>=path`: Greater than or equal (min inclusive)
- `<path`: Less than (max exclusive)
- `>path`: Greater than (min exclusive)
- `~path`: Like (stemmed word search)
- `?path`: Any of the values
- `^path`: Order by (with priority: "increasing", "decreasing", or numeric value)
- `$path`: Focus on specific values
- `@`: Offset (pagination)
- `#`: Limit (pagination)

### Tabular Model Objects

Objects with computed expressions:

```json
[
  {
    "column=path": model,
    "property": model
  }
]
```

Where:

- `column=path`: Defines a column with the given name and expression path
- `property`: Shorthand for a column with the same name as the property

## Expression Paths

Paths used in filter expressions and computed columns:

```
path ::= (label ('.' label)*)?
```

Examples:

- `"property"`: Direct property
- `"property.subproperty"`: Nested property path

## Priority Values

Used with the `^path` filter for ordering:

- `"increasing"`: Ascending order (+1)
- `"decreasing"`: Descending order (-1)
- Numeric values: Custom priority

## Pagination Parameters

- `@`: Offset (integer)
- `#`: Limit (integer)