package br.com.lucad.nowinlsg.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "pokemon_favorites")

open class FavoritesRepository(private val context: Context? = null) {

    private val favoritesKey = stringSetPreferencesKey("favorite_pokemon_ids")

    open val favoriteIds: Flow<Set<Int>> by lazy {
        context?.dataStore?.data?.map { preferences ->
            val stringSet = preferences[favoritesKey] ?: emptySet()
            stringSet.mapNotNull { it.toIntOrNull() }.toSet()
        } ?: kotlinx.coroutines.flow.flowOf(emptySet())
    }

    open suspend fun isFavorite(pokemonId: Int): Boolean {
        val ctx = context ?: return false
        val currentSet = ctx.dataStore.data.map { preferences ->
            preferences[favoritesKey] ?: emptySet()
        }.first()
        return currentSet.contains(pokemonId.toString())
    }

    open fun isFavoriteFlow(pokemonId: Int): Flow<Boolean> {
        return favoriteIds.map { ids -> ids.contains(pokemonId) }
    }

    open suspend fun addFavorite(pokemonId: Int) {
        val ctx = context ?: return
        ctx.dataStore.edit { preferences ->
            val current = preferences[favoritesKey] ?: emptySet()
            preferences[favoritesKey] = current + pokemonId.toString()
        }
    }

    open suspend fun removeFavorite(pokemonId: Int) {
        val ctx = context ?: return
        ctx.dataStore.edit { preferences ->
            val current = preferences[favoritesKey] ?: emptySet()
            preferences[favoritesKey] = current - pokemonId.toString()
        }
    }

    open suspend fun toggleFavorite(pokemonId: Int) {
        val ctx = context ?: return
        ctx.dataStore.edit { preferences ->
            val current = preferences[favoritesKey] ?: emptySet()
            val idStr = pokemonId.toString()
            if (current.contains(idStr)) {
                preferences[favoritesKey] = current - idStr
            } else {
                preferences[favoritesKey] = current + idStr
            }
        }
    }
}
