package br.com.lucad.nowinlsg.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import br.com.lucad.nowinlsg.data.RepositoryProvider
import br.com.lucad.nowinlsg.data.repository.FavoritesRepository
import br.com.lucad.nowinlsg.data.repository.PokemonRepository

class PokemonViewModelFactory(
    private val pokemonRepository: PokemonRepository,
    private val favoritesRepository: FavoritesRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
        return when {
            modelClass.isAssignableFrom(PokemonListViewModel::class.java) -> {
                PokemonListViewModel(pokemonRepository, favoritesRepository) as T
            }
            modelClass.isAssignableFrom(PokemonDetailViewModel::class.java) -> {
                PokemonDetailViewModel(pokemonRepository, favoritesRepository) as T
            }
            else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return create(modelClass, CreationExtras.Empty)
    }

    companion object {
        fun create(context: Context): PokemonViewModelFactory {
            val appContext = context.applicationContext
            val favoritesRepo = RepositoryProvider.getFavoritesRepository(appContext)
            val pokemonRepo = RepositoryProvider.getPokemonRepository(appContext)
            return PokemonViewModelFactory(pokemonRepo, favoritesRepo)
        }

        fun Factory(context: Context): ViewModelProvider.Factory {
            return create(context)
        }

        val AppFactory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = checkNotNull(this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY])
                PokemonListViewModel(
                    pokemonRepository = RepositoryProvider.getPokemonRepository(application),
                    favoritesRepository = RepositoryProvider.getFavoritesRepository(application)
                )
            }
            initializer {
                val application = checkNotNull(this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY])
                PokemonDetailViewModel(
                    pokemonRepository = RepositoryProvider.getPokemonRepository(application),
                    favoritesRepository = RepositoryProvider.getFavoritesRepository(application)
                )
            }
        }
    }
}
