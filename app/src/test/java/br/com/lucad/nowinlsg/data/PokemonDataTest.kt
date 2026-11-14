package br.com.lucad.nowinlsg.data

import br.com.lucad.nowinlsg.data.model.PokemonEntry
import br.com.lucad.nowinlsg.data.model.PokemonListResponse
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class PokemonDataTest {

    private val moshi: Moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    @Test
    fun testPokemonEntryIdParsing() {
        val entry = PokemonEntry(
            name = "bulbasaur",
            url = "https://pokeapi.co/api/v2/pokemon/1/"
        )
        assertEquals(1, entry.id)
        assertEquals(
            "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/other/official-artwork/1.png",
            entry.imageUrl
        )
    }

    @Test
    fun testPokemonListResponseParsing() {
        val json = """
            {
                "count": 1302,
                "next": "https://pokeapi.co/api/v2/pokemon?offset=20&limit=20",
                "previous": null,
                "results": [
                    {
                        "name": "bulbasaur",
                        "url": "https://pokeapi.co/api/v2/pokemon/1/"
                    },
                    {
                        "name": "ivysaur",
                        "url": "https://pokeapi.co/api/v2/pokemon/2/"
                    }
                ]
            }
        """.trimIndent()

        val adapter = moshi.adapter(PokemonListResponse::class.java)
        val response = adapter.fromJson(json)

        assertNotNull(response)
        assertEquals(1302, response?.count)
        assertEquals(2, response?.results?.size)
        assertEquals("bulbasaur", response?.results?.get(0)?.name)
        assertEquals(1, response?.results?.get(0)?.id)
        assertEquals("ivysaur", response?.results?.get(1)?.name)
        assertEquals(2, response?.results?.get(1)?.id)
    }
}
