package br.com.lucad.nowinlsg.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class PokemonDetailResponse(
    @Json(name = "id") val id: Int,
    @Json(name = "name") val name: String,
    @Json(name = "height") val height: Int,
    @Json(name = "weight") val weight: Int,
    @Json(name = "sprites") val sprites: PokemonSprites,
    @Json(name = "types") val types: List<PokemonTypeSlot> = emptyList(),
    @Json(name = "stats") val stats: List<PokemonStatSlot> = emptyList(),
    @Json(name = "abilities") val abilities: List<PokemonAbilitySlot> = emptyList()
) {
    val imageUrl: String
        get() = sprites.officialArtworkUrl
            ?: "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/other/official-artwork/$id.png"
}

@JsonClass(generateAdapter = true)
data class PokemonSprites(
    @Json(name = "front_default") val frontDefault: String? = null,
    @Json(name = "other") val other: OtherSprites? = null
) {
    val officialArtworkUrl: String?
        get() = other?.officialArtwork?.frontDefault ?: other?.home?.frontDefault ?: frontDefault
}

@JsonClass(generateAdapter = true)
data class OtherSprites(
    @Json(name = "official-artwork") val officialArtwork: OfficialArtwork? = null,
    @Json(name = "home") val home: HomeSprites? = null
)

@JsonClass(generateAdapter = true)
data class OfficialArtwork(
    @Json(name = "front_default") val frontDefault: String? = null
)

@JsonClass(generateAdapter = true)
data class HomeSprites(
    @Json(name = "front_default") val frontDefault: String? = null
)

@JsonClass(generateAdapter = true)
data class PokemonTypeSlot(
    @Json(name = "slot") val slot: Int,
    @Json(name = "type") val type: NamedApiResource
)

@JsonClass(generateAdapter = true)
data class PokemonStatSlot(
    @Json(name = "base_stat") val baseStat: Int,
    @Json(name = "effort") val effort: Int = 0,
    @Json(name = "stat") val stat: NamedApiResource
)

@JsonClass(generateAdapter = true)
data class PokemonAbilitySlot(
    @Json(name = "is_hidden") val isHidden: Boolean = false,
    @Json(name = "slot") val slot: Int,
    @Json(name = "ability") val ability: NamedApiResource
)

@JsonClass(generateAdapter = true)
data class NamedApiResource(
    @Json(name = "name") val name: String,
    @Json(name = "url") val url: String = ""
)
