package br.com.lucad.nowinlsg.ui.viewmodel

import br.com.lucad.nowinlsg.data.model.PokemonDetailResponse
import br.com.lucad.nowinlsg.data.model.PokemonSprites
import br.com.lucad.nowinlsg.data.repository.FavoritesRepository
import br.com.lucad.nowinlsg.data.repository.PokemonRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class PokemonDetailViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var fakeFavoritesRepository: FakeFavoritesRepository
    private lateinit var fakePokemonRepository: DetailFakePokemonRepository
    private lateinit var viewModel: PokemonDetailViewModel

    @Before
    fun setUp() {
        fakeFavoritesRepository = FakeFavoritesRepository()
        fakePokemonRepository = DetailFakePokemonRepository(fakeFavoritesRepository)
        viewModel = PokemonDetailViewModel(fakePokemonRepository, fakeFavoritesRepository)
    }

    @Test
    fun testLoadPokemonDetailSuccess() = runTest {
        backgroundScope.launch { viewModel.uiState.collect() }
        viewModel.loadPokemonDetail("pikachu")
        val state = viewModel.uiState.value

        assertFalse(state.isLoading)
        assertNull(state.errorMessage)
        assertNotNull(state.pokemonDetail)
        assertEquals("pikachu", state.pokemonDetail?.name)
        assertEquals(25, state.pokemonDetail?.id)
        assertFalse(state.isFavorite)
    }

    @Test
    fun testToggleFavoriteInDetail() = runTest {
        backgroundScope.launch { viewModel.uiState.collect() }
        viewModel.loadPokemonDetail("25")
        assertFalse(viewModel.uiState.value.isFavorite)

        viewModel.toggleFavorite()
        assertTrue(viewModel.uiState.value.isFavorite)

        viewModel.toggleFavorite()
        assertFalse(viewModel.uiState.value.isFavorite)
    }

    @Test
    fun testLoadPokemonDetailError() = runTest {
        backgroundScope.launch { viewModel.uiState.collect() }
        fakePokemonRepository.shouldReturnError = true
        viewModel.loadPokemonDetail("unknown")

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals("Detail load error", state.errorMessage)
        assertNull(state.pokemonDetail)
    }
}

class DetailFakePokemonRepository(
    private val fakeFavs: FavoritesRepository
) : PokemonRepository() {
    var shouldReturnError = false

    override suspend fun getPokemonDetail(nameOrId: String): Result<PokemonDetailResponse> {
        return if (shouldReturnError) {
            Result.failure(RuntimeException("Detail load error"))
        } else {
            val id = nameOrId.toIntOrNull() ?: 25
            val name = if (nameOrId == "25") "pikachu" else nameOrId
            Result.success(
                PokemonDetailResponse(
                    id = id,
                    name = name,
                    height = 4,
                    weight = 60,
                    sprites = PokemonSprites(frontDefault = "https://example.com/25.png")
                )
            )
        }
    }

    override val favoriteIds: Flow<Set<Int>> = fakeFavs.favoriteIds

    override suspend fun toggleFavorite(pokemonId: Int) {
        fakeFavs.toggleFavorite(pokemonId)
    }
}
