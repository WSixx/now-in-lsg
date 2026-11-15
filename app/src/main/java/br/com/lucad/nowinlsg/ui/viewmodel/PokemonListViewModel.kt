package br.com.lucad.nowinlsg.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.lucad.nowinlsg.data.model.PokemonEntry
import br.com.lucad.nowinlsg.data.repository.FavoritesRepository
import br.com.lucad.nowinlsg.data.repository.PokemonRepository
import br.com.lucad.nowinlsg.ui.state.PokemonListUiState
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class PokemonListViewModel(
    private val pokemonRepository: PokemonRepository,
    private val favoritesRepository: FavoritesRepository
) : ViewModel() {

    private val _rawPokemonList = MutableStateFlow<List<PokemonEntry>>(emptyList())
    private val _searchQuery = MutableStateFlow("")
    private val _isFavoritesOnly = MutableStateFlow(false)
    private val _isLoading = MutableStateFlow(false)
    private val _errorMessage = MutableStateFlow<String?>(null)
    private var loadJob: Job? = null

    val uiState: StateFlow<PokemonListUiState> = combine(
        _rawPokemonList,
        favoritesRepository.favoriteIds,
        _searchQuery,
        _isFavoritesOnly,
        _isLoading
    ) { rawList, favIds, query, favOnly, loading ->
        val trimmedQuery = query.trim()
        val filtered = rawList.filter { entry ->
            val matchesQuery = trimmedQuery.isEmpty() ||
                entry.name.contains(trimmedQuery, ignoreCase = true) ||
                entry.id.toString() == trimmedQuery
            val matchesFav = !favOnly || favIds.contains(entry.id)
            matchesQuery && matchesFav
        }
        PokemonListUiState(
            pokemonList = filtered,
            favoriteIds = favIds,
            searchQuery = query,
            isFavoritesOnly = favOnly,
            isLoading = loading
        )
    }.combine(_errorMessage) { state, error ->
        state.copy(errorMessage = error)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = PokemonListUiState(isLoading = true)
    )

    init {
        loadPokemonList()
    }

    fun loadPokemonList(limit: Int = 151, offset: Int = 0) {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            pokemonRepository.getPokemonList(limit, offset)
                .onSuccess { response ->
                    _rawPokemonList.value = response.results
                    _isLoading.value = false
                }
                .onFailure { exception ->
                    _errorMessage.value = exception.message ?: "Failed to load Pokémon list"
                    _isLoading.value = false
                }
        }
    }

    fun refresh() {
        loadPokemonList()
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun onSearchQueryChanged(query: String) {
        updateSearchQuery(query)
    }

    fun toggleFavoritesOnly() {
        _isFavoritesOnly.value = !_isFavoritesOnly.value
    }

    fun setFavoritesOnly(enabled: Boolean) {
        _isFavoritesOnly.value = enabled
    }

    fun toggleFavoriteFilter() {
        toggleFavoritesOnly()
    }

    fun updateFavoriteFilter(showOnlyFavorites: Boolean) {
        setFavoritesOnly(showOnlyFavorites)
    }

    fun toggleFavorite(pokemonId: Int) {
        viewModelScope.launch {
            favoritesRepository.toggleFavorite(pokemonId)
        }
    }
}
