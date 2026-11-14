package br.com.lucad.nowinlsg.ui.state

import br.com.lucad.nowinlsg.data.model.PokemonEntry

data class PokemonListUiState(
    val pokemonList: List<PokemonEntry> = emptyList(),
    val favoriteIds: Set<Int> = emptySet(),
    val searchQuery: String = "",
    val isFavoritesOnly: Boolean = false,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)
