---
id: schema-generator-getting-started
title: Getting Started with the Schema Generator
---

`graphql-kotlin-schema-generator` provides a single function, `toSchema`, to generate a schema from Kotlin objects.

```kotlin
data class Widget(val id: Int, val value: String)

class WidgetQuery {
  fun widgetById(id: Int): Widget? {
    // grabs widget from a data source
  }
}

class WidgetMutation {
  fun saveWidget(value: String): Widget {
    // some logic for saving widget
  }
}

val widgetQuery = WidgetQuery()
val widgetMutation = WidgetMutation()
val schema = toSchema(
  config = yourCustomConfig(),
  queries = listOf(TopLevelObject(widgetQuery)),
  mutations = listOf(TopLevelObject(widgetMutation))
)
```

will generate:

```graphql
schema {
  query: Query
  mutation: Mutation
}

type Query {
  widgetById(id: Int!): Widget
}

type Mutation {
  saveWidget(value: String!): Widget!
}

type Widget {
  id: Int!
  value: String!
}
```

Any `public` functions defined on a query, mutation, or subscription Kotlin class will be translated into GraphQL fields on the object
type. `toSchema` will then recursively apply Kotlin reflection on the specified classes to generate all
remaining object types, their properties, functions, and function arguments.

The generated `GraphQLSchema` can then be used to expose a GraphQL API endpoint.

## `toSchema`

This function accepts four arguments: `config`, `queries`, `mutations` and `subscriptions`. The `queries`, `mutations`
and `subscriptions` are a list of `TopLevelObject`s and will be used to generate corresponding GraphQL root types. See
below on why we use this wrapper class. The `config` contains all the extra information you need to pass, including
custom hooks, supported packages, and name overrides. See the [Generator Configuration](../customizing-schemas/generator-config) documentation for more information.

You can see the definition for `toSchema` [in the
source](https://github.com/ExpediaGroup/graphql-kotlin/blob/master/graphql-kotlin-schema-generator/src/main/kotlin/com/expediagroup/graphql/toSchema.kt)

## Class `TopLevelObject`

`toSchema` uses Kotlin reflection to build a GraphQL schema from given classes using `graphql-java`'s schema builder. We
don't just pass a `KClass` though, we have to actually pass an object, because the functions on the object are
transformed into the data fetchers. In most cases, a `TopLevelObject` can be constructed with just an object:

```kotlin
class Query {
  fun getNumber() = 1
}

val topLevelObject = TopLevelObject(Query())

toSchema(config = config, queries = listOf(topLevelObject))
```

In the above case, `toSchema` will use `topLevelObject::class` as the reflection target, and `Query` as the data fetcher
target.

In a lot of cases, such as with Spring AOP, the object (or bean) being used to generate a schema is a dynamic proxy. In
this case, `topLevelObject::class` is not `Query`, but rather a generated class that will confuse the schema generator.
To specify the `KClass` to use for reflection on a proxy, pass the class to `TopLevelObject`:

```kotlin
@Component
class Query {
  @Timed
  fun getNumber() = 1
}

val query = getObjectFromBean()
val customDef = TopLevelObject(query, Query::class)

toSchema(config, listOf(customDef))
```
