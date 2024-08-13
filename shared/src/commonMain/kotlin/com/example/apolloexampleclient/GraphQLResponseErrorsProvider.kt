package com.example.apolloexampleclient

class GraphQLResponseErrorsProvider(
    val requestMeta: GraphQLRequestMeta,
    private val errors: List<GraphQLError>,
    private val errorResolver: GraphQLErrorToExceptionResolver,
) {
    fun findError(path: GraphQLValuePath) = findError { error ->
        path.startsWith(error.path)
    }

    fun findError(predicate: (GraphQLError) -> Boolean): GraphQLError? =
        errors.firstOrNull(predicate)

    fun findErrorWithErrorResolver(errorResolver: (GraphQLError) -> Exception?): Exception? {
        for (error in errors) {
            val exception = errorResolver(error)
            if (exception != null) {
                return exception
            }
        }
        return null
    }

    fun <T : Any> findErrorWithMapNotNull(transform: (GraphQLError) -> T?): T? {
        for (error in errors) {
            val result = transform(error)
            if (result != null) {
                return result
            }
        }
        return null
    }

    fun findErrorList(predicate: (GraphQLError) -> Boolean): List<GraphQLError> =
        errors.filter(predicate)

    fun resolveExceptionOrNull(error: GraphQLError): Exception? =
        errorResolver.resolve(requestMeta, error)
}
