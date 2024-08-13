package com.example.apolloexampleclient.exception

import com.example.apolloexampleclient.GraphQLError

class MappingException(message: String, cause: Throwable? = null) : Exception(message, cause) {
    companion object
}

fun MappingException.Companion.getNotFoundData() =
    MappingException("Not found data in response")

fun MappingException.Companion.getValueIsNull(pathName: String, error: GraphQLError? = null) =
    MappingException("$pathName is null${error?.suffixMessageForMapping().orEmpty()}")

fun MappingException.Companion.getMutationIsNull(pathName: String, error: GraphQLError? = null) =
    MappingException("mutation $pathName is null${error?.suffixMessageForMapping().orEmpty()}")

fun MappingException.Companion.getMutationIsNull(subject: String? = null) =
    MappingException("mutation result is null${subject?.let { " for $it" }.orEmpty()}")

private fun GraphQLError.suffixMessageForMapping() = ", $detailMessage"