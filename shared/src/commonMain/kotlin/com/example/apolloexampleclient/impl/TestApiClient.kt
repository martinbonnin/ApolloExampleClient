package com.example.apolloexampleclient.impl

import com.apollographql.apollo3.ApolloClient
import com.apollographql.apollo3.api.ApolloRequest
import com.apollographql.apollo3.api.Operation
import com.example.apolloexampleclient.EmptyGraphQLErrorToExceptionResolver
import com.example.apolloexampleclient.GraphQLErrorToExceptionResolver
import com.example.apolloexampleclient.GraphQLRequestMeta
import com.example.apolloexampleclient.GraphQLResponseErrorsProvider
import com.example.apolloexampleclient.GraphQLResponseProvider
import kotlinx.coroutines.flow.single

class TestApiClient(
    private val serverUrl: String,
    private val errorResolver: GraphQLErrorToExceptionResolver?,
) {
    private val apolloClient = ApolloClient.Builder()
        .serverUrl(serverUrl)
        .build()

    suspend fun <D : Operation.Data, M : Any> perform(
        operation: Operation<D>,
        mapper: (GraphQLResponseProvider<D>) -> M,
    ): M {
        val operationName = operation.name()
        val meta = GraphQLRequestMeta(
            serverUrl = serverUrl,
            operationName = operationName,
            operationType = operation.operationType(),
        )

        val request = ApolloRequest.Builder(operation).build()
        val response = apolloClient.executeAsFlow(request).single()

        val errorsProvider = GraphQLResponseErrorsProvider(
            requestMeta = meta,
            errors = response.errors.toGraphQLErrors(),
            errorResolver = errorResolver ?: EmptyGraphQLErrorToExceptionResolver,
        )
        val responseProvider = GraphQLResponseProvider(
            data = response.data,
            errorsProvider = errorsProvider,
        )
        return mapper(responseProvider)
    }
}