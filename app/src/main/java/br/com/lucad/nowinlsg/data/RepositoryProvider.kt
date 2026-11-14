package br.com.lucad.nowinlsg.data

import android.content.Context
import br.com.lucad.nowinlsg.data.api.RetrofitClient
import br.com.lucad.nowinlsg.data.repository.FavoritesRepository
import br.com.lucad.nowinlsg.data.repository.PokemonRepository

object RepositoryProvider {
    @Volatile
    private var favoritesRepository: FavoritesRepository? = null

    @Volatile
    private var pokemonRepository: PokemonRepository? = null

    fun getFavoritesRepository(context: Context): FavoritesRepository {
        return favoritesRepository ?: synchronized(this) {
            favoritesRepository ?: FavoritesRepository(context.applicationContext).also {
                favoritesRepository = it
            }
        }
    }

    fun getPokemonRepository(context: Context): PokemonRepository {
        return pokemonRepository ?: synchronized(this) {
            pokemonRepository ?: PokemonRepository(
                apiService = RetrofitClient.pokeApiService,
                favoritesRepository = getFavoritesRepository(context)
            ).also {
                pokemonRepository = it
            }
        }
    }
}
