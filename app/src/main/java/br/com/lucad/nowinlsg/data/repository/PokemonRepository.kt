package br.com.lucad.nowinlsg.data.repository

import br.com.lucad.nowinlsg.data.api.PokeApiService
import br.com.lucad.nowinlsg.data.model.PokemonDetailResponse
import br.com.lucad.nowinlsg.data.model.PokemonListResponse
import kotlinx.coroutines.flow.Flow

open class PokemonRepository(
    private val apiService: PokeApiService? = null,
    private val favoritesRepository: FavoritesRepository? = null
) {
    open suspend fun getPokemonList(limit: Int = 151, offset: Int = 0): Result<PokemonListResponse> {
        return runCatching {
            apiService!!.getPokemonList(limit, offset)
        }
    }

    open suspend fun getPokemonDetail(nameOrId: String): Result<PokemonDetailResponse> {
        return runCatching {
            apiService!!.getPokemonDetail(nameOrId.lowercase())
        }
    }

    open val favoriteIds: Flow<Set<Int>>
        get() = favoritesRepository?.favoriteIds ?: kotlinx.coroutines.flow.flowOf(emptySet())

    open suspend fun toggleFavorite(pokemonId: Int) {
        favoritesRepository?.toggleFavorite(pokemonId)
    }

    open fun isFavorite(pokemonId: Int): Flow<Boolean> =
        favoritesRepository?.isFavoriteFlow(pokemonId) ?: kotlinx.coroutines.flow.flowOf(false)
}
