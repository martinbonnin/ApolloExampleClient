package com.example.apolloexampleclient

import kotlin.jvm.JvmName
import com.example.apolloexampleclient.exception.MappingException
import com.example.apolloexampleclient.exception.getMutationIsNull
import com.example.apolloexampleclient.exception.getValueIsNull

class GraphQLValueProvider<V : Any>(
    val value: V,
    val path: GraphQLValuePath,
    internal val errorsProvider: GraphQLResponseErrorsProvider,
) {
    val requestMeta: GraphQLRequestMeta get() = errorsProvider.requestMeta

    // region Values
    @Throws(Exception::class)
    fun <T> valueOrThrow(subPathName: String, subValue: V.() -> T?): T =
        value.subValue() ?: throw getDefaultException(subPathName)

    @Throws(Exception::class)
    fun <T : Any> valueWithErrorResolver(
        subPathName: String,
        subValue: V.() -> T?,
        errorResolver: (GraphQLError) -> Exception?,
    ): T {
        val result = value.subValue()
        if (result != null) {
            return result
        } else {
            val error = findError(subPathName)
            throw error?.let(errorResolver) ?: getDefaultException(subPathName, error)
        }
    }

    fun <T : Any> valueProvider(subPathName: String, subValue: V.() -> T) =
        GraphQLValueProvider(
            value = value.subValue(),
            path = this.path + subPathName,
            errorsProvider = errorsProvider,
        )

    fun <T : Any> valueProviderOrNull(subPathName: String, subValue: V.() -> T?) =
        value.subValue()?.let {
            GraphQLValueProvider(
                value = it,
                path = this.path + subPathName,
                errorsProvider = errorsProvider,
            )
        }

    @Throws(Exception::class)
    fun <T : Any> valueProviderOrThrow(subPathName: String, subValue: V.() -> T?) =
        GraphQLValueProvider(
            value = valueOrThrow(
                subPathName = subPathName,
                subValue = subValue,
            ),
            path = this.path + subPathName,
            errorsProvider = errorsProvider,
        )

    @Throws(Exception::class)
    fun <T : Any> valueProviderWithErrorResolver(
        subPathName: String,
        subValue: V.() -> T?,
        errorResolver: (GraphQLError) -> Exception?,
    ) = GraphQLValueProvider(
        value = valueWithErrorResolver(
            subPathName = subPathName,
            subValue = subValue,
            errorResolver = errorResolver,
        ),
        path = this.path + subPathName,
        errorsProvider = errorsProvider,
    )
    // endregion

    // region Errors
    fun findError(subPath: GraphQLValuePath): GraphQLError? =
        errorsProvider.findError(this.path + subPath)

    fun findError(subPathName: String): GraphQLError? =
        errorsProvider.findError(this.path + subPathName)

    fun findErrorInValuePath(predicate: (GraphQLError) -> Boolean): GraphQLError? =
        errorsProvider.findError { error ->
            error.path.startsWith(this.path) && predicate(error)
        }

    fun <T : Any> findErrorInValuePathWithMapNotNull(transform: (GraphQLError) -> T?): T? =
        errorsProvider.findErrorWithMapNotNull { error ->
            if (error.path.startsWith(this.path)) {
                transform(error)
            } else {
                null
            }
        }

    fun findErrorListInValuePath(predicate: (GraphQLError) -> Boolean): List<GraphQLError> =
        errorsProvider.findErrorList { error ->
            error.path.startsWith(this.path) && predicate(error)
        }

    fun findErrorEverywhere(predicate: (GraphQLError) -> Boolean): GraphQLError? =
        errorsProvider.findError(predicate)

    fun <T : Any> findErrorEverywhereWithMapNotNull(transform: (GraphQLError) -> T?): T? =
        errorsProvider.findErrorWithMapNotNull(transform)

    fun findErrorListEverywhere(predicate: (GraphQLError) -> Boolean): List<GraphQLError> =
        errorsProvider.findErrorList(predicate)

    fun resolveExceptionOrNull(error: GraphQLError): Exception? =
        errorsProvider.resolveExceptionOrNull(error)
    // endregion

    fun getDefaultException(subPathName: String): Exception =
        getDefaultException(subPathName, findError(subPathName))

    internal fun getDefaultException(subPathName: String, error: GraphQLError?): Exception {
        val resolvedException = error?.let(errorsProvider::resolveExceptionOrNull)
        if (resolvedException != null) {
            return resolvedException
        }

        val path = this.path + subPathName
        return when (errorsProvider.requestMeta.operationType) {
            GraphQLOperationType.Query -> MappingException.getValueIsNull(path.name, error)
            GraphQLOperationType.Mutation -> MappingException.getMutationIsNull(path.name, error)
        }
    }
}

fun <V : List<I>, I : Any, R> GraphQLValueProvider<V>.map(
    transform: (GraphQLValueProvider<I>) -> R,
): List<R> = value.mapIndexed { index, item ->
    val provider = GraphQLValueProvider(
        value = item,
        path = this.path + index,
        errorsProvider = errorsProvider,
    )
    transform(provider)
}

@JvmName("mapNullable")
fun <V : List<I?>, I : Any, R> GraphQLValueProvider<V>.map(
    transform: (GraphQLValueProvider<I>?) -> R,
): List<R> = value.mapIndexed { index, item ->
    val provider = item?.let {
        GraphQLValueProvider(
            value = it,
            path = this.path + index,
            errorsProvider = errorsProvider,
        )
    }
    transform(provider)
}

@Throws(MappingException::class)
fun <V : List<I?>, I : Any, R> GraphQLValueProvider<V>.mapOrThrow(
    transform: (GraphQLValueProvider<I>) -> R,
): List<R> = value.mapIndexed { index, item ->
    val itemPath = this.path + index
    val provider = item?.let {
        GraphQLValueProvider(
            value = it,
            path = itemPath,
            errorsProvider = errorsProvider,
        )
    } ?: throw getDefaultException(GraphQLValuePath.subPathOf(index).name)
    transform(provider)
}

@Throws(Exception::class)
fun <V : List<I?>, I : Any, R> GraphQLValueProvider<V>.mapWithErrorResolver(
    transform: (GraphQLValueProvider<I>) -> R,
    errorResolver: (GraphQLError) -> Exception?,
): List<R> = value.mapIndexed { index, item ->
    val itemPath = this.path + index
    val provider = item?.let {
        GraphQLValueProvider(
            value = it,
            path = itemPath,
            errorsProvider = errorsProvider,
        )
    }
    if (provider != null) {
        transform(provider)
    } else {
        val error = errorsProvider.findError(itemPath)
        throw error?.let(errorResolver)
            ?: getDefaultException(GraphQLValuePath.subPathOf(index).name, error)
    }
}

fun <V : List<I>, I : Any, R> GraphQLValueProvider<V>.mapNotNull(
    transform: (GraphQLValueProvider<I>) -> R?,
): List<R> = value.mapIndexedNotNull { index, item ->
    val provider = GraphQLValueProvider(
        value = item,
        path = this.path + index,
        errorsProvider = errorsProvider,
    )
    transform(provider)
}

@JvmName("mapNotNullNullable")
fun <V : List<I?>, I : Any, R> GraphQLValueProvider<V>.mapNotNull(
    transform: (GraphQLValueProvider<I>?) -> R?,
): List<R> = value.mapIndexedNotNull { index, item ->
    val provider = item?.let {
        GraphQLValueProvider(
            value = it,
            path = this.path + index,
            errorsProvider = errorsProvider,
        )
    }
    transform(provider)
}
