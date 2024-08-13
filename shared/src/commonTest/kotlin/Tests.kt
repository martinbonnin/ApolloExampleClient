import catch.CatchQuery
import com.apollographql.apollo.api.Error
import com.apollographql.apollo.api.getOrThrow
import com.apollographql.apollo.api.json.jsonReader
import com.apollographql.apollo.api.parseResponse
import com.apollographql.apollo.exception.DefaultApolloException
import com.apollographql.mockserver.MockServer
import com.apollographql.mockserver.enqueueString
import com.example.apolloexampleclient.EmptyGraphQLErrorToExceptionResolver
import com.example.apolloexampleclient.GraphQLError
import com.example.apolloexampleclient.GraphQLErrorToExceptionResolver
import com.example.apolloexampleclient.GraphQLRequestMeta
import com.example.apolloexampleclient.GraphQLResponseErrorsProvider
import com.example.apolloexampleclient.GraphQLResponseProvider
import com.example.apolloexampleclient.GraphQLValuePath
import com.example.apolloexampleclient.GraphQLValueProvider
import com.example.apolloexampleclient.impl.operationType
import com.example.apolloexampleclient.impl.toGraphQLErrors
import default.DefaultQuery
import okio.Buffer
import okio.use
import kotlin.test.Test
import kotlin.test.assertFailsWith

class Tests {
    // language=JSON
    val responseJson = """
            {
                "errors": [
                    {
                        "message": "Cannot fetch duration",
                        "path": ["user", "content", 0, "duration"]
                    }
                ],
                "data": {
                    "user": {
                        "__typename": "User",
                        "id": "Movie0",
                        "name": "John",
                        "content": [
                            {
                                "__typename": "Movie",
                                "id": "Movie0",
                                "title": "My Movie",
                                "duration": null
                            }
                        ]
                    }
                }
               
            }
        """.trimIndent()


    @Test
    fun defaultTest() {

        val operation = DefaultQuery()
        val response = operation.parseResponse(responseJson.jsonReader())

        val meta = GraphQLRequestMeta(
            serverUrl = "https://example.com",
            operationName = operation.name(),
            operationType = operation.operationType(),
        )

        val errorsProvider = GraphQLResponseErrorsProvider(
            requestMeta = meta,
            errors = response.errors.toGraphQLErrors(),
            errorResolver = object : GraphQLErrorToExceptionResolver {
                override fun resolve(requestMeta: GraphQLRequestMeta, error: GraphQLError): Exception {
                    return MyException(error)
                }
            }
        )
        val responseProvider = GraphQLResponseProvider(
            data = response.data,
            errorsProvider = errorsProvider,
        )

        assertFailsWith<MyException> {
            val duration =
                responseProvider.valueOrThrow("user.content.0.duration") { user?.content?.get(0)?.movieFragment?.duration }
        }
    }

    private fun String.jsonReader() = Buffer().writeUtf8(this).jsonReader()

    @Test
    fun catchTest() {
        val operation = CatchQuery()
        val response = operation.parseResponse(responseJson.jsonReader())

        assertFailsWith<DefaultApolloException> {
            val duration = response.data?.user?.content?.get(0)?.movieFragment?.duration?.getOrThrow()
        }
    }
}

class MyException(val error: GraphQLError) : Exception(error.message)