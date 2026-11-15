package br.com.lucad.nowinlsg.ui.viewmodel

import br.com.lucad.nowinlsg.data.model.PokemonEntry
import br.com.lucad.nowinlsg.data.model.PokemonListResponse
import br.com.lucad.nowinlsg.data.repository.FavoritesRepository
import br.com.lucad.nowinlsg.data.repository.PokemonRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class PokemonListViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var fakeFavoritesRepository: FakeFavoritesRepository
    private lateinit var fakePokemonRepository: FakePokemonRepository
    private lateinit var viewModel: PokemonListViewModel

    @Before
    fun setUp() {
        fakeFavoritesRepository = FakeFavoritesRepository()
        fakePokemonRepository = FakePokemonRepository(fakeFavoritesRepository)
        viewModel = PokemonListViewModel(fakePokemonRepository, fakeFavoritesRepository)
    }

    @Test
    fun testInitialLoadSuccess() = runTest {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.uiState.collect() }
        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertNull(state.errorMessage)
        assertEquals(3, state.pokemonList.size)
        assertEquals("bulbasaur", state.pokemonList[0].name)
    }

    @Test
    fun testSearchQueryFilteringByName() = runTest {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.uiState.collect() }
        viewModel.onSearchQueryChanged("pika")
        val state = viewModel.uiState.value
        assertEquals(1, state.pokemonList.size)
        assertEquals("pikachu", state.pokemonList[0].name)
    }

    @Test
    fun testSearchQueryFilteringById() = runTest {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.uiState.collect() }
        viewModel.updateSearchQuery("2")
        val state = viewModel.uiState.value
        assertEquals(1, state.pokemonList.size)
        assertEquals("ivysaur", state.pokemonList[0].name)
    }

    @Test
    fun testFavoriteFilterToggling() = runTest {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.uiState.collect() }
        fakeFavoritesRepository.toggleFavorite(25) // Favorite Pikachu (ID 25)

        viewModel.setFavoritesOnly(true)
        val state = viewModel.uiState.value
        assertTrue(state.isFavoritesOnly)
        assertEquals(1, state.pokemonList.size)
        assertEquals("pikachu", state.pokemonList[0].name)

        viewModel.toggleFavoriteFilter()
        assertFalse(viewModel.uiState.value.isFavoritesOnly)
        assertEquals(3, viewModel.uiState.value.pokemonList.size)
    }

    @Test
    fun testToggleFavoriteStatus() = runTest {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.uiState.collect() }
        viewModel.toggleFavorite(1) // Bulbasaur ID 1
        val state = viewModel.uiState.value
        assertTrue(state.favoriteIds.contains(1))

        viewModel.toggleFavorite(1)
        val updatedState = viewModel.uiState.value
        assertFalse(updatedState.favoriteIds.contains(1))
    }

    @Test
    fun testLoadErrorState() = runTest {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.uiState.collect() }
        fakePokemonRepository.shouldReturnError = true
        viewModel.refresh()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals("Network error", state.errorMessage)
    }
}

class FakeFavoritesRepository : FavoritesRepository() {
    private val _favorites = MutableStateFlow<Set<Int>>(emptySet())
    override val favoriteIds: Flow<Set<Int>> = _favorites.asStateFlow()

    override fun isFavoriteFlow(pokemonId: Int): Flow<Boolean> {
        return _favorites.map { it.contains(pokemonId) }
    }

    override suspend fun isFavorite(pokemonId: Int): Boolean {
        return _favorites.value.contains(pokemonId)
    }

    override suspend fun toggleFavorite(pokemonId: Int) {
        val current = _favorites.value
        _favorites.value = if (current.contains(pokemonId)) current - pokemonId else current + pokemonId
    }

    override suspend fun addFavorite(pokemonId: Int) {
        _favorites.value = _favorites.value + pokemonId
    }

    override suspend fun removeFavorite(pokemonId: Int) {
        _favorites.value = _favorites.value - pokemonId
    }
}

class FakePokemonRepository(
    private val fakeFavoritesRepo: FavoritesRepository
) : PokemonRepository() {
    var shouldReturnError = false
    var pokemonListResponse = PokemonListResponse(
        count = 3,
        results = listOf(
            PokemonEntry(name = "bulbasaur", url = "https://pokeapi.co/api/v2/pokemon/1/"),
            PokemonEntry(name = "ivysaur", url = "https://pokeapi.co/api/v2/pokemon/2/"),
            PokemonEntry(name = "pikachu", url = "https://pokeapi.co/api/v2/pokemon/25/")
        )
    )

    override suspend fun getPokemonList(limit: Int, offset: Int): Result<PokemonListResponse> {
        return if (shouldReturnError) {
            Result.failure(RuntimeException("Network error"))
        } else {
            Result.success(pokemonListResponse)
        }
    }

    override val favoriteIds: Flow<Set<Int>> = fakeFavoritesRepo.favoriteIds

    override suspend fun toggleFavorite(pokemonId: Int) {
        fakeFavoritesRepo.toggleFavorite(pokemonId)
    }
}
