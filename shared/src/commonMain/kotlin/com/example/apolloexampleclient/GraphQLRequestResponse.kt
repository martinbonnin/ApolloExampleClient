package com.example.apolloexampleclient

data class GraphQLRequestMeta(
    val serverUrl: String,
    val operationName: String,
    val operationType: GraphQLOperationType,
)

enum class GraphQLOperationType(val value: String) {
    Query("query"),
    Mutation("mutation");
}

data class GraphQLError(
    val message: String,
    val path: GraphQLValuePath,
    val type: String,
    val description: String,
    val extensions: Map<String, Any?>,
) {
    val detailMessage: String get() = "$type [${path.name}]: ${message.trim()}"

    fun detailMessageWithOperation(operationName: String) =
        "GraphQL $detailMessage for operation: $operationName"
}
