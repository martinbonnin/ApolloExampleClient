package com.example.apolloexampleclient.impl

import com.apollographql.apollo.api.Error
import com.apollographql.apollo.api.Mutation
import com.apollographql.apollo.api.Operation
import com.apollographql.apollo.api.Query
import com.example.apolloexampleclient.GraphQLError
import com.example.apolloexampleclient.GraphQLOperationType
import com.example.apolloexampleclient.GraphQLValuePath

internal fun <D : Operation.Data> Operation<D>.operationType() = when (this) {
    is Query -> GraphQLOperationType.Query
    is Mutation -> GraphQLOperationType.Mutation
    else -> error("Unknown operation type: $this")
}

internal fun Error.toGraphQLError() = GraphQLError(
    message = message,
    path = GraphQLValuePath(path?.map { it.toString() }.orEmpty()),
    type = extensions?.get("type")?.toString().orEmpty(),
    description = toString(),
    extensions = extensions.orEmpty(),
)

internal fun List<Error>?.toGraphQLErrors() = this?.map { it.toGraphQLError() }.orEmpty()
