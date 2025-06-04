---
title: Data Model and Query Language
---

The Mesh query language provides a comprehensive model for filtering, sorting, and analyzing data through JSON-based
query expressions and response envelopes.

## Core Data Types

```typescript
type Literal=boolean | string | number

type Value=Literal | { [field: string]: Value } | Value[]

type Order="{expression}" | "<{expression}" | ">{expression}"
```

## Query Structure

The core query object supports filtering, sorting, pagination, and field selection:

```typescript
type Query=Partial<{

	// Field filtering - exact match or nested query

	"{field}": Literal | Query | Query[]

	// Expression-based filtering
	"{expression}": Query
	"{alias}={expression}": Query

	// Comparison operators
	"< {expression}": Literal              // less than
	"> {expression}": Literal              // greater than
	"<= {expression}": Literal             // less than or equal
	">= {expression}": Literal             // greater than or equal

	// Text search
	"~ {expression}": string               // like (stemmed word search)

	// Set operations
	"? {expression}": Literal | Literal[]  // any (OR)
	"! {expression}": Literal | Literal[]  // all (AND)

	// Focus filtering
	"$ {expression}": Literal | Literal[]  // focus on specific values

	// Sorting by expression with priority
	"^{expression}": string | number       // sort by expression with priority

	// Result control
	"^": Order | Order[]                   // sorting
	"@": number                            // offset (pagination)
	"#": number                            // limit (max 100)
}>
```

## Data Envelope Patterns

### Items Envelope

For retrieving individual records with optional filtering and pagination:

**Request:**

```json
{
  "members": [
    {
      "id": "",
      "label": "",
      ">=seniority": 3,
      "@": 100,
      "#": 10
    }
  ]
}
```

**Response:**

```json
{
  "members": [
    {
      "id": "/employees/123",
      "label": "Tino Faussone"
    },
    {
      "id": "/employees/098",
      "label": "Memmo Cancelli"
    }
  ]
}
```

### Stats Envelope

For retrieving statistical aggregations (min, max, count, etc.):

**Request:**

```json
{
  "members": [
    {
      "lower=min(seniority)": 0,
      "upper=max(seniority)": 0
    }
  ]
}
```

**Response:**

```json
{
  "members": [
    {
      "lower": 1,
      "upper": 6
    }
  ]
}
```

### Terms Envelope

For faceted search and term aggregation with filtering:

**Request:**

```json
{
  "members": [
    {
      "~office.label": "US",
      "^": [
        "count()"
      ],
      "#": 10,
      "count()": 0,
      "office": {
        "id": "",
        "label": ""
      }
    }
  ]
}
```

**Response:**

```json
{
  "members": [
    {
      "count()": 10,
      "office": {
        "id": "/offices/1",
        "label": "Paris"
      }
    },
    {
      "count()": 3,
      "office": {
        "id": "/offices/2",
        "label": "London"
      }
    }
  ]
}
```

### Links Envelope

For retrieving related entities with count information:

**Request:**

```json
{
  "members": [
    {
      "count=count()": 0,
      "*": {
        "id": "",
        "label": ""
      }
    }
  ]
}
```

**Response:**

```json
{
  "members": [
    {
      "count": 10,
      "office": {
        "id": "/offices/1",
        "label": "Paris"
      }
    },
    {
      "count": 3,
      "office": {
        "id": "/offices/2",
        "label": "London"
      }
    }
  ]
}
```

### Analytics Envelope

For complex analytical queries with grouping and aggregation:

**Request:**

```json
{
  "members": [
    {
      "count=count(customer)": 0,
      "country=customer.country": {
        "id": "",
        "label": ""
      },
      "^": "count",
      "#": 10
    }
  ]
}
```

**Response:**

```json
{
  "members": [
    {
      "count": 10,
      "country": {
        "id": "/countries/1",
        "label": "Italy"
      }
    },
    {
      "count": 3,
      "country": {
        "id": "/countries/2",
        "label": "Germany"
      }
    }
  ]
}
```

## Advanced Features

### Expression Syntax

Expressions support property paths and function calls:

- `property` - Direct property access
- `property.nested` - Nested property access
- `function()` - Function calls (count, min, max, avg, etc.)
- `function(property)` - Function with argument

### Sorting Options

Multiple sorting formats are supported:

**Priority-based sorting with `^{expression}`:**

- `"^{expression}": priority` - Sort by expression with custom priority
- Priority values:
    - `"increasing"` or `1` - Ascending order
    - `"decreasing"` or `-1` - Descending order
    - Any numeric value for custom priority ordering (positive = ascending, negative = descending)

**Direct ordering with `^` field:**

- `"^": "{expression}"` - Sort by expression (default ascending)
- `"^": "<{expression}"` - Sort by expression descending
- `"^": ">{expression}"` - Sort by expression ascending
- `"^": [Order, Order, ...]` - Multiple sort orders

Examples:

```json
{
  "^count()": "decreasing",
  "^name": 1
}
```

```json
{
  "^": "salary"
}
```

```json
{
  "^": "<created_date"
}
```

```json
{
  "^": [
    ">priority",
    "name"
  ]
}
```

### Aliases

Create computed fields with aliases using the `=` operator:

```json
{
  "total=count()": 0,
  "average=avg(salary)": 0
}
```

### Focus Filtering

Use `$` prefix to focus on specific values in result sets:

```json
{
  "$category": [
    "electronics",
    "books"
  ]
}
```

### Comparison Operators

Support for range and comparison filtering:

- `<` - Less than
- `>` - Greater than
- `<=` - Less than or equal
- `>=` - Greater than or equal
- `~` - Text search with stemming
- `?` - Any of the specified values (OR)
- `!` - All of the specified values (AND)

### Pagination

Control result sets with offset and limit:

- `@` - Skip specified number of results
- `#` - Limit results (maximum 100)

## Implementation Notes

- Query objects must be wrapped in arrays when used in request envelopes
- Multiple query objects in a single array are not supported
- Empty string IDs are preserved as default model values
- The system enforces a maximum limit of 100 results per query
- Null values are not permitted in typed arrays
- Nested arrays are not supported in shaped contexts