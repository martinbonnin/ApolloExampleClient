package com.example.apolloexampleclient

import com.apollographql.apollo3.api.Operation
import com.example.apolloexampleclient.exception.MappingException
import com.example.apolloexampleclient.exception.getMutationIsNull
import com.example.apolloexampleclient.exception.getNotFoundData
import com.example.apolloexampleclient.exception.getValueIsNull

class GraphQLResponseProvider<D : Operation.Data>(
    private val data: D?,
    private val errorsProvider: GraphQLResponseErrorsProvider,
) {
    val requestMeta: GraphQLRequestMeta get() = errorsProvider.requestMeta

    // region Values
    @Throws(MappingException::class)
    fun dataOrThrow(): D = data ?: throw MappingException.getNotFoundData()

    @Throws(Exception::class)
    fun <T> valueOrThrow(pathName: String, value: D.() -> T?): T =
        data?.value() ?: throw getDefaultException(pathName, findError(pathName))

    @Throws(Exception::class)
    fun <T> valueWithErrorResolver(
        pathName: String,
        value: D.() -> T?,
        errorResolver: (GraphQLError) -> Exception?,
    ): T {
        val result = data?.value()
        if (result != null) {
            return result
        } else {
            val error = findError(pathName)
            throw error?.let(errorResolver) ?: getDefaultException(pathName, error)
        }
    }

    @Throws(Exception::class)
    fun dataProviderOrThrow() = GraphQLValueProvider(
        value = dataOrThrow(),
        path = GraphQLValuePath(emptyList()),
        errorsProvider = errorsProvider,
    )

    @Throws(Exception::class)
    fun <T : Any> valueProviderOrThrow(pathName: String, value: D.() -> T?) = GraphQLValueProvider(
        value = valueOrThrow(pathName, value),
        path = GraphQLValuePath.of(pathName),
        errorsProvider = errorsProvider,
    )

    @Throws(Exception::class)
    fun <T : Any> valueProviderWithErrorResolver(
        pathName: String,
        value: D.() -> T?,
        errorResolver: (GraphQLError) -> Exception?,
    ) = GraphQLValueProvider(
        value = valueWithErrorResolver(pathName, value, errorResolver),
        path = GraphQLValuePath.of(pathName),
        errorsProvider = errorsProvider,
    )

    @Throws(MappingException::class)
    fun <T : Any> mutationValueOrThrow(subject: String? = null, value: D.() -> T?): T =
        dataOrThrow().value() ?: throw MappingException.getMutationIsNull(subject)
    // endregion

    // region Errors
    fun findError(path: GraphQLValuePath): GraphQLError? =
        errorsProvider.findError(path)

    fun findError(pathName: String): GraphQLError? =
        errorsProvider.findError(GraphQLValuePath.of(pathName))

    fun findError(predicate: (GraphQLError) -> Boolean): GraphQLError? =
        errorsProvider.findError(predicate)

    fun findErrorList(predicate: (GraphQLError) -> Boolean): List<GraphQLError> =
        errorsProvider.findErrorList(predicate)

    fun <T : Any> findErrorWithMapNotNull(transform: (GraphQLError) -> T?): T? =
        errorsProvider.findErrorWithMapNotNull(transform)

    fun resolveExceptionOrNull(error: GraphQLError): Exception? =
        errorsProvider.resolveExceptionOrNull(error)
    // endregion

    private fun getDefaultException(pathName: String, error: GraphQLError?): Exception {
        val resolvedException = error?.let(errorsProvider::resolveExceptionOrNull)
        return resolvedException
            ?: if (data == null) {
                MappingException.getNotFoundData()
            } else {
                when (errorsProvider.requestMeta.operationType) {
                    GraphQLOperationType.Query -> MappingException.getValueIsNull(pathName, error)
                    GraphQLOperationType.Mutation -> MappingException.getMutationIsNull(pathName, error)
                }
            }
    }
}
