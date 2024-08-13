package com.example.apolloexampleclient.exception

import com.example.apolloexampleclient.GraphQLError
import com.example.apolloexampleclient.GraphQLRequestMeta

class GraphQLErrorException(
    val requestMeta: GraphQLRequestMeta,
    val error: GraphQLError,
) : Exception(
    error.detailMessageWithOperation(requestMeta.operationName),
)

fun GraphQLError.toException(requestMeta: GraphQLRequestMeta) = GraphQLErrorException(requestMeta, this)
