---
title: Lorem Ipsum
excerpt: Lorem ipsum sit amet.
---

```typescript
type Literal=boolean | string | number

type Value=Literal | { [field: string]: Value } | Value[]

type Order="{expression}" | "<{expression}" | ">{expression}"

type Query=Partial<{

    "{field}": Literal | Query | Query[]

    "{expression}": Query
    "{alias}={expression}": Query

    "< {expression}": Literal // less than
  	"> {expression}": Literal // greater than

    "<= {expression}": Literal // less than or equal
    ">= {expression}": Literal // greater than or equal

    "~ {expression}": string // like (stemmed word search)

    "? {expression}": Literal | Literal[] // any
    "! {expression}": Literal | Literal[] // all

    "^": Order | Order[] // sorting
    "@": number // offset
    "#": number // limit

}>
```

# Items

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

# Stats

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

# Terms

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
                "id": "/offices/1",
                "label": "Paris"
            }
        }
    ]
}
```

# Links

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
                "id": "/offices/1",
                "label": "Paris"
            }
        }
    ]
}
```

# Analytics

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