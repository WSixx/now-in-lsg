package br.com.lucad.nowinlsg.ui.screen

import org.junit.Assert.assertEquals
import org.junit.Test

class PokemonWebViewUrlTest {

    private fun buildPokemonWebUrl(pokemonName: String): String {
        return "https://pokemondb.net/pokedex/${pokemonName.lowercase()}"
    }

    @Test
    fun testBuildPokemonWebUrlStandardName() {
        val url = buildPokemonWebUrl("pikachu")
        assertEquals("https://pokemondb.net/pokedex/pikachu", url)
    }

    @Test
    fun testBuildPokemonWebUrlCapitalizedName() {
        val url = buildPokemonWebUrl("Charizard")
        assertEquals("https://pokemondb.net/pokedex/charizard", url)
    }

    @Test
    fun testBuildPokemonWebUrlSpecialName() {
        val url = buildPokemonWebUrl("Mr-Mime")
        assertEquals("https://pokemondb.net/pokedex/mr-mime", url)
    }
}
