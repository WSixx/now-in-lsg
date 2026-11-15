package br.com.lucad.nowinlsg.data.repository

import br.com.lucad.nowinlsg.data.api.PokeApiService
import br.com.lucad.nowinlsg.data.model.PokemonDetailResponse
import br.com.lucad.nowinlsg.data.model.PokemonListResponse
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

open class PokemonRepository(
    private val apiService: PokeApiService? = null,
    private val favoritesRepository: FavoritesRepository? = null
) {
    open suspend fun getPokemonList(limit: Int = 151, offset: Int = 0): Result<PokemonListResponse> {
        return suspendRunCatching {
            val service = requireNotNull(apiService) { "PokeApiService is not initialized" }
            service.getPokemonList(limit, offset)
        }
    }

    open suspend fun getPokemonDetail(nameOrId: String): Result<PokemonDetailResponse> {
        return suspendRunCatching {
            val service = requireNotNull(apiService) { "PokeApiService is not initialized" }
            service.getPokemonDetail(nameOrId.lowercase())
        }
    }

    open val favoriteIds: Flow<Set<Int>>
        get() = favoritesRepository?.favoriteIds ?: flowOf(emptySet())

    open suspend fun toggleFavorite(pokemonId: Int) {
        favoritesRepository?.toggleFavorite(pokemonId)
    }

    open fun isFavorite(pokemonId: Int): Flow<Boolean> =
        favoritesRepository?.isFavoriteFlow(pokemonId) ?: flowOf(false)
}

private suspend inline fun <T> suspendRunCatching(block: suspend () -> T): Result<T> {
    return try {
        Result.success(block())
    } catch (cancellationException: CancellationException) {
        throw cancellationException
    } catch (exception: Throwable) {
        Result.failure(exception)
    }
}
