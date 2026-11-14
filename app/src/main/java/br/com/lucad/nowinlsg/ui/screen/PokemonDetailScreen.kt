package br.com.lucad.nowinlsg.ui.screen

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.rounded.CatchingPokemon
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.FitnessCenter
import androidx.compose.material.icons.rounded.Height
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import br.com.lucad.nowinlsg.data.model.NamedApiResource
import br.com.lucad.nowinlsg.data.model.OfficialArtwork
import br.com.lucad.nowinlsg.data.model.OtherSprites
import br.com.lucad.nowinlsg.data.model.PokemonAbilitySlot
import br.com.lucad.nowinlsg.data.model.PokemonDetailResponse
import br.com.lucad.nowinlsg.data.model.PokemonSprites
import br.com.lucad.nowinlsg.data.model.PokemonStatSlot
import br.com.lucad.nowinlsg.data.model.PokemonTypeSlot
import br.com.lucad.nowinlsg.ui.state.PokemonDetailUiState
import br.com.lucad.nowinlsg.ui.theme.NowInLSGTheme
import coil.compose.AsyncImage
import coil.request.ImageRequest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PokemonDetailScreen(
    uiState: PokemonDetailUiState,
    showBackButton: Boolean,
    onBackClick: () -> Unit,
    onToggleFavorite: () -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
    onOpenWebInfo: (String) -> Unit = {}
) {
    val pokemon = uiState.pokemonDetail
    val titleText = pokemon?.name?.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
        ?: "Pokémon Detail"

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = titleText,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                navigationIcon = {
                    if (showBackButton) {
                        IconButton(onClick = onBackClick) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                                contentDescription = "Back"
                            )
                        }
                    }
                },
                actions = {
                    if (pokemon != null) {
                        IconButton(onClick = {
                            val url = "https://pokemondb.net/pokedex/${pokemon.name.lowercase()}"
                            onOpenWebInfo(url)
                        }) {
                            Icon(
                                imageVector = Icons.Default.Language,
                                contentDescription = "Web Info"
                            )
                        }
                        IconButton(onClick = onToggleFavorite) {
                            Icon(
                                imageVector = if (uiState.isFavorite) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder,
                                contentDescription = "Toggle Favorite",
                                tint = if (uiState.isFavorite) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when {
                uiState.isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                uiState.errorMessage != null -> {
                    Column(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = uiState.errorMessage,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.error,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = onRetry) {
                            Icon(
                                imageVector = Icons.Rounded.Refresh,
                                contentDescription = null
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Retry")
                        }
                    }
                }

                pokemon == null -> {
                    // Placeholder when no Pokémon selected
                    Column(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.CatchingPokemon,
                            contentDescription = null,
                            modifier = Modifier.size(96.dp),
                            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Select a Pokémon",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Choose a Pokémon from the list to view detailed stats, abilities, and artwork.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                else -> {
                    PokemonDetailContent(
                        pokemon = pokemon,
                        onOpenWebInfo = onOpenWebInfo,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }
}

@Composable
fun PokemonDetailContent(
    pokemon: PokemonDetailResponse,
    onOpenWebInfo: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    val formattedId = "#%03d".format(pokemon.id)
    val capitalizedName = pokemon.name.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }

    Column(
        modifier = modifier
            .verticalScroll(scrollState)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // High-res Image & Header Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Text(
                        text = formattedId,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.7f)
                        .aspectRatio(1f)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceContainerLowest),
                    contentAlignment = Alignment.Center
                ) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(pokemon.imageUrl)
                            .crossfade(true)
                            .build(),
                        contentDescription = capitalizedName,
                        contentScale = ContentScale.Fit,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = capitalizedName,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Elemental Types Badges
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    pokemon.types.forEach { typeSlot ->
                        val typeName = typeSlot.type.name
                        val typeColor = getPokemonTypeColor(typeName)
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = typeColor,
                            shadowElevation = 2.dp
                        ) {
                            Text(
                                text = typeName.uppercase(),
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                AssistChip(
                    onClick = {
                        val url = "https://pokemondb.net/pokedex/${pokemon.name.lowercase()}"
                        onOpenWebInfo(url)
                    },
                    label = { Text("Web Info") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Language,
                            contentDescription = "Web Info",
                            modifier = Modifier.size(18.dp)
                        )
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Physical Stats Card (Height & Weight)
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerLow
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Height in decimeters -> meters
                PhysicalStatItem(
                    icon = Icons.Rounded.Height,
                    label = "Height",
                    value = "%.1f m".format(pokemon.height / 10.0)
                )

                Surface(
                    modifier = Modifier
                        .width(1.dp)
                        .height(36.dp),
                    color = MaterialTheme.colorScheme.outlineVariant
                ) {}

                // Weight in hectograms -> kilograms
                PhysicalStatItem(
                    icon = Icons.Rounded.FitnessCenter,
                    label = "Weight",
                    value = "%.1f kg".format(pokemon.weight / 10.0)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Abilities Section
        if (pokemon.abilities.isNotEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Abilities",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        pokemon.abilities.forEach { abilitySlot ->
                            val abilityName = abilitySlot.ability.name
                                .replace("-", " ")
                                .replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
                            val label = if (abilitySlot.isHidden) "$abilityName (Hidden)" else abilityName

                            AssistChip(
                                onClick = {},
                                label = {
                                    Text(
                                        text = label,
                                        fontWeight = if (abilitySlot.isHidden) FontWeight.Normal else FontWeight.SemiBold
                                    )
                                },
                                colors = AssistChipDefaults.assistChipColors(
                                    containerColor = if (abilitySlot.isHidden) {
                                        MaterialTheme.colorScheme.surfaceVariant
                                    } else {
                                        MaterialTheme.colorScheme.secondaryContainer
                                    },
                                    labelColor = if (abilitySlot.isHidden) {
                                        MaterialTheme.colorScheme.onSurfaceVariant
                                    } else {
                                        MaterialTheme.colorScheme.onSecondaryContainer
                                    }
                                )
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }

        // Base Stats Section
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerLow
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = "Base Stats",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(12.dp))

                pokemon.stats.forEach { statSlot ->
                    StatBarItem(
                        statSlot = statSlot,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun PhysicalStatItem(
    icon: ImageVector,
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Column {
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun StatBarItem(
    statSlot: PokemonStatSlot,
    modifier: Modifier = Modifier
) {
    val statName = statSlot.stat.name
    val statValue = statSlot.baseStat
    val maxStatValue = 255f

    val (abbrev, statColor) = getStatInfo(statName)

    var progressTarget by remember { mutableFloatStateOf(0f) }
    LaunchedEffect(statValue) {
        progressTarget = (statValue / maxStatValue).coerceIn(0f, 1f)
    }

    val animatedProgress by animateFloatAsState(
        targetValue = progressTarget,
        animationSpec = tween(durationMillis = 800),
        label = "StatProgressAnimation"
    )

    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Label
        Text(
            text = abbrev,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = statColor,
            modifier = Modifier.width(64.dp)
        )

        // Numeric Value
        Text(
            text = statValue.toString(),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.End,
            modifier = Modifier.width(36.dp)
        )

        Spacer(modifier = Modifier.width(12.dp))

        // Progress Bar
        LinearProgressIndicator(
            progress = { animatedProgress },
            modifier = Modifier
                .weight(1f)
                .height(10.dp)
                .clip(RoundedCornerShape(5.dp)),
            color = statColor,
            trackColor = statColor.copy(alpha = 0.2f),
            strokeCap = StrokeCap.Round
        )
    }
}

fun getStatInfo(statName: String): Pair<String, Color> {
    return when (statName.lowercase()) {
        "hp" -> "HP" to Color(0xFFFF5959)
        "attack" -> "ATK" to Color(0xFFF5AC78)
        "defense" -> "DEF" to Color(0xFFFAE078)
        "special-attack" -> "Sp. ATK" to Color(0xFF9DB7F5)
        "special-defense" -> "Sp. DEF" to Color(0xFFA7DB8D)
        "speed" -> "SPD" to Color(0xFFFA92B2)
        else -> statName.uppercase() to Color(0xFF9EA0A9)
    }
}

fun getPokemonTypeColor(typeName: String): Color {
    return when (typeName.lowercase()) {
        "grass" -> Color(0xFF74CB48)
        "fire" -> Color(0xFFF57D31)
        "water" -> Color(0xFF6493EB)
        "electric" -> Color(0xFFF9CF30)
        "poison" -> Color(0xFFA43E9E)
        "flying" -> Color(0xFFA891EC)
        "bug" -> Color(0xFFA7B723)
        "normal" -> Color(0xFFAAA67F)
        "ground" -> Color(0xFFDEC16B)
        "fairy" -> Color(0xFFE69EAC)
        "fighting" -> Color(0xFFC12239)
        "psychic" -> Color(0xFFFB5584)
        "rock" -> Color(0xFFB69E31)
        "steel" -> Color(0xFFB7B9D0)
        "ice" -> Color(0xFF9AD6DF)
        "ghost" -> Color(0xFF70559B)
        "dragon" -> Color(0xFF7037F9)
        "dark" -> Color(0xFF75574C)
        else -> Color(0xFF68A090)
    }
}

@Preview(showBackground = true)
@Composable
fun PokemonDetailScreenPreview() {
    NowInLSGTheme {
        PokemonDetailScreen(
            uiState = PokemonDetailUiState(
                pokemonDetail = PokemonDetailResponse(
                    id = 25,
                    name = "pikachu",
                    height = 4,
                    weight = 60,
                    sprites = PokemonSprites(
                        frontDefault = "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/25.png",
                        other = OtherSprites(
                            officialArtwork = OfficialArtwork(
                                frontDefault = "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/other/official-artwork/25.png"
                            )
                        )
                    ),
                    types = listOf(
                        PokemonTypeSlot(1, NamedApiResource("electric"))
                    ),
                    stats = listOf(
                        PokemonStatSlot(35, stat = NamedApiResource("hp")),
                        PokemonStatSlot(55, stat = NamedApiResource("attack")),
                        PokemonStatSlot(40, stat = NamedApiResource("defense")),
                        PokemonStatSlot(50, stat = NamedApiResource("special-attack")),
                        PokemonStatSlot(50, stat = NamedApiResource("special-defense")),
                        PokemonStatSlot(90, stat = NamedApiResource("speed"))
                    ),
                    abilities = listOf(
                        PokemonAbilitySlot(false, 1, NamedApiResource("static")),
                        PokemonAbilitySlot(true, 2, NamedApiResource("lightning-rod"))
                    )
                ),
                isFavorite = true
            ),
            showBackButton = true,
            onBackClick = {},
            onToggleFavorite = {},
            onRetry = {},
            onOpenWebInfo = {}
        )
    }
}
