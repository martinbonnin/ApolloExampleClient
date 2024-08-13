package com.example.apolloexampleclient

data class GraphQLValuePath(val raw: List<String>) {
    val name: String get() = raw.joinToString(".")

    fun isEmpty() = raw.isEmpty()

    operator fun plus(subPath: GraphQLValuePath) =
        GraphQLValuePath(raw + subPath.raw)

    operator fun plus(subPathName: String) =
        GraphQLValuePath(raw + subPathName.toRawPath())

    operator fun plus(index: Int) =
        GraphQLValuePath(raw + index.toString())

    fun startsWith(subPath: GraphQLValuePath): Boolean =
        if (raw.isEmpty() || subPath.raw.isEmpty() || subPath.raw.size > raw.size) {
            false
        } else if (raw.size == subPath.raw.size) {
            subPath.raw == raw
        } else {
            subPath.raw == raw.subList(0, subPath.raw.size)
        }

    companion object {
        fun of(pathName: String) =
            GraphQLValuePath(pathName.toRawPath())

        fun subPathOf(index: Int) =
            GraphQLValuePath(listOf(index.toString()))

        private fun String.toRawPath() = split(".").filter { it.isNotEmpty() }
    }
}
