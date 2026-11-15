package br.com.lucad.nowinlsg.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PokemonDetailResponse(
    @SerialName("id") val id: Int,
    @SerialName("name") val name: String,
    @SerialName("height") val height: Int,
    @SerialName("weight") val weight: Int,
    @SerialName("sprites") val sprites: PokemonSprites,
    @SerialName("types") val types: List<PokemonTypeSlot> = emptyList(),
    @SerialName("stats") val stats: List<PokemonStatSlot> = emptyList(),
    @SerialName("abilities") val abilities: List<PokemonAbilitySlot> = emptyList()
) {
    val imageUrl: String
        get() = sprites.officialArtworkUrl
            ?: "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/other/official-artwork/$id.png"
}

@Serializable
data class PokemonSprites(
    @SerialName("front_default") val frontDefault: String? = null,
    @SerialName("other") val other: OtherSprites? = null
) {
    val officialArtworkUrl: String?
        get() = other?.officialArtwork?.frontDefault ?: other?.home?.frontDefault ?: frontDefault
}

@Serializable
data class OtherSprites(
    @SerialName("official-artwork") val officialArtwork: OfficialArtwork? = null,
    @SerialName("home") val home: HomeSprites? = null
)

@Serializable
data class OfficialArtwork(
    @SerialName("front_default") val frontDefault: String? = null
)

@Serializable
data class HomeSprites(
    @SerialName("front_default") val frontDefault: String? = null
)

@Serializable
data class PokemonTypeSlot(
    @SerialName("slot") val slot: Int,
    @SerialName("type") val type: NamedApiResource
)

@Serializable
data class PokemonStatSlot(
    @SerialName("base_stat") val baseStat: Int,
    @SerialName("effort") val effort: Int = 0,
    @SerialName("stat") val stat: NamedApiResource
)

@Serializable
data class PokemonAbilitySlot(
    @SerialName("is_hidden") val isHidden: Boolean = false,
    @SerialName("slot") val slot: Int,
    @SerialName("ability") val ability: NamedApiResource
)

@Serializable
data class NamedApiResource(
    @SerialName("name") val name: String,
    @SerialName("url") val url: String = ""
)
