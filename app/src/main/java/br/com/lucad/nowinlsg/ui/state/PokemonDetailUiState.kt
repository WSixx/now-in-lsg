package br.com.lucad.nowinlsg.ui.state

import br.com.lucad.nowinlsg.data.model.PokemonDetailResponse

data class PokemonDetailUiState(
    val pokemonDetail: PokemonDetailResponse? = null,
    val isFavorite: Boolean = false,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)
