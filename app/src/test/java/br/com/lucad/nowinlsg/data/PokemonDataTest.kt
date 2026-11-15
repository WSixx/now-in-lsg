package br.com.lucad.nowinlsg.data

import br.com.lucad.nowinlsg.data.api.RetrofitClient
import br.com.lucad.nowinlsg.data.model.PokemonEntry
import br.com.lucad.nowinlsg.data.model.PokemonListResponse
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class PokemonDataTest {

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

        val response = RetrofitClient.json.decodeFromString<PokemonListResponse>(json)

        assertNotNull(response)
        assertEquals(1302, response.count)
        assertEquals(2, response.results.size)
        assertEquals("bulbasaur", response.results[0].name)
        assertEquals(1, response.results[0].id)
        assertEquals("ivysaur", response.results[1].name)
        assertEquals(2, response.results[1].id)
    }
}
