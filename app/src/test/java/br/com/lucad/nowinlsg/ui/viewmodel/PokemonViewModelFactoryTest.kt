package br.com.lucad.nowinlsg.ui.viewmodel

import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class PokemonViewModelFactoryTest {

    @Test
    fun testFactoryCreatesViewModels() {
        val fakeFavs = FakeFavoritesRepository()
        val fakePokemon = FakePokemonRepository(fakeFavs)
        val factory = PokemonViewModelFactory(fakePokemon, fakeFavs)

        val listVm = factory.create(PokemonListViewModel::class.java)
        assertNotNull(listVm)
        assertTrue(listVm is PokemonListViewModel)

        val detailVm = factory.create(PokemonDetailViewModel::class.java)
        assertNotNull(detailVm)
        assertTrue(detailVm is PokemonDetailViewModel)
    }
}
