package br.com.lucad.nowinlsg.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class PokemonListResponse(
    @Json(name = "count") val count: Int = 0,
    @Json(name = "next") val next: String? = null,
    @Json(name = "previous") val previous: String? = null,
    @Json(name = "results") val results: List<PokemonEntry> = emptyList()
)

@JsonClass(generateAdapter = true)
data class PokemonEntry(
    @Json(name = "name") val name: String,
    @Json(name = "url") val url: String
) {
    val id: Int
        get() {
            return url.trimEnd('/').split("/").lastOrNull()?.toIntOrNull() ?: 0
        }

    val imageUrl: String
        get() = "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/other/official-artwork/$id.png"

    val spriteUrl: String
        get() = "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/$id.png"
}
