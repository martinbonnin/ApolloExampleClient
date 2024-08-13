package com.example.apolloexampleclient

interface GraphQLErrorToExceptionResolver {
    fun resolve(requestMeta: GraphQLRequestMeta, error: GraphQLError): Exception?
}

object EmptyGraphQLErrorToExceptionResolver : GraphQLErrorToExceptionResolver {
    override fun resolve(requestMeta: GraphQLRequestMeta, error: GraphQLError): Exception? = null
}
