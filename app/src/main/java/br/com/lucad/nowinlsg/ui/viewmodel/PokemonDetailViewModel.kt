package br.com.lucad.nowinlsg.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.lucad.nowinlsg.data.model.PokemonDetailResponse
import br.com.lucad.nowinlsg.data.repository.FavoritesRepository
import br.com.lucad.nowinlsg.data.repository.PokemonRepository
import br.com.lucad.nowinlsg.ui.state.PokemonDetailUiState
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class PokemonDetailViewModel(
    private val pokemonRepository: PokemonRepository,
    private val favoritesRepository: FavoritesRepository
) : ViewModel() {

    private val _pokemonDetail = MutableStateFlow<PokemonDetailResponse?>(null)
    private val _isLoading = MutableStateFlow(false)
    private val _errorMessage = MutableStateFlow<String?>(null)
    private val _currentPokemonId = MutableStateFlow<Int?>(null)
    private var lastRequestedNameOrId: String? = null
    private var detailJob: Job? = null

    private val isFavoriteFlow: Flow<Boolean> = _currentPokemonId.flatMapLatest { id ->
        if (id != null) {
            favoritesRepository.isFavoriteFlow(id)
        } else {
            flowOf(false)
        }
    }

    val uiState: StateFlow<PokemonDetailUiState> = combine(
        _pokemonDetail,
        isFavoriteFlow,
        _isLoading,
        _errorMessage
    ) { detail, isFav, loading, error ->
        PokemonDetailUiState(
            pokemonDetail = detail,
            isFavorite = isFav,
            isLoading = loading,
            errorMessage = error
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = PokemonDetailUiState()
    )

    fun loadPokemonDetail(nameOrId: String, forceReload: Boolean = false) {
        val currentDetail = _pokemonDetail.value
        if (!forceReload &&
            lastRequestedNameOrId == nameOrId &&
            (currentDetail != null || _isLoading.value)
        ) {
            return
        }

        lastRequestedNameOrId = nameOrId
        nameOrId.toIntOrNull()?.let { numericId ->
            _currentPokemonId.value = numericId
        }

        detailJob?.cancel()
        detailJob = viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            pokemonRepository.getPokemonDetail(nameOrId)
                .onSuccess { detail ->
                    _pokemonDetail.value = detail
                    _currentPokemonId.value = detail.id
                    _isLoading.value = false
                }
                .onFailure { exception ->
                    _errorMessage.value = exception.message ?: "Failed to load Pokémon details"
                    _isLoading.value = false
                }
        }
    }

    fun toggleFavorite() {
        val currentId = _currentPokemonId.value ?: _pokemonDetail.value?.id ?: return
        viewModelScope.launch {
            favoritesRepository.toggleFavorite(currentId)
        }
    }

    fun toggleFavorite(pokemonId: Int) {
        viewModelScope.launch {
            favoritesRepository.toggleFavorite(pokemonId)
        }
    }

    fun refresh() {
        lastRequestedNameOrId?.let { loadPokemonDetail(it, forceReload = true) }
    }
}
