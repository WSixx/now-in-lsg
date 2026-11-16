package br.com.lucad.nowinlsg.ui.screen

import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.layout.AnimatedPane
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffoldRole
import androidx.compose.material3.adaptive.layout.PaneAdaptedValue
import androidx.compose.material3.adaptive.navigation.NavigableListDetailPaneScaffold
import androidx.compose.material3.adaptive.navigation.rememberListDetailPaneScaffoldNavigator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import br.com.lucad.nowinlsg.ui.viewmodel.PokemonDetailViewModel
import br.com.lucad.nowinlsg.ui.viewmodel.PokemonListViewModel
import br.com.lucad.nowinlsg.ui.viewmodel.PokemonViewModelFactory
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
fun PokemonAdaptiveApp(
    modifier: Modifier = Modifier,
    listViewModel: PokemonListViewModel = viewModel(
        factory = PokemonViewModelFactory.AppFactory
    ),
    detailViewModel: PokemonDetailViewModel = viewModel(
        factory = PokemonViewModelFactory.AppFactory
    ),
    onWebViewVisibilityChanged: (Boolean) -> Unit = {},
) {
    val navigator = rememberListDetailPaneScaffoldNavigator<String>()
    val coroutineScope = rememberCoroutineScope()

    val listUiState by listViewModel.uiState.collectAsStateWithLifecycle()
    val detailUiState by detailViewModel.uiState.collectAsStateWithLifecycle()

    var currentWebUrl by rememberSaveable { mutableStateOf<String?>(null) }
    var currentWebTitle by rememberSaveable { mutableStateOf("Web Info") }

    val selectedPokemonKey = navigator.currentDestination?.contentKey
    LaunchedEffect(selectedPokemonKey) {
        if (!selectedPokemonKey.isNullOrBlank()) {
            detailViewModel.loadPokemonDetail(selectedPokemonKey)
        }
    }

    val webUrl = currentWebUrl
    LaunchedEffect(webUrl != null) {
        onWebViewVisibilityChanged(webUrl != null)
    }

    if (webUrl != null) {
        PokemonWebViewScreen(
            url = webUrl,
            title = currentWebTitle,
            onBack = { currentWebUrl = null },
            modifier = modifier
        )
    } else {
        NavigableListDetailPaneScaffold(
            navigator = navigator,
            modifier = modifier,
            listPane = {
                AnimatedPane {
                    PokemonListScreen(
                        uiState = listUiState,
                        onSearchQueryChanged = listViewModel::onSearchQueryChanged,
                        onToggleFavoriteFilter = listViewModel::toggleFavoriteFilter,
                        onToggleFavorite = listViewModel::toggleFavorite,
                        onPokemonSelected = { pokemonId ->
                            detailViewModel.loadPokemonDetail(pokemonId)
                            coroutineScope.launch {
                                navigator.navigateTo(
                                    pane = ListDetailPaneScaffoldRole.Detail,
                                    contentKey = pokemonId
                                )
                            }
                        },
                        onRetry = listViewModel::refresh
                    )
                }
            },
            detailPane = {
                AnimatedPane {
                    val isListPaneHidden = navigator.scaffoldValue[ListDetailPaneScaffoldRole.List] == PaneAdaptedValue.Hidden

                    PokemonDetailScreen(
                        uiState = detailUiState,
                        showBackButton = isListPaneHidden,
                        onBackClick = {
                            coroutineScope.launch {
                                navigator.navigateBack()
                            }
                        },
                        onToggleFavorite = detailViewModel::toggleFavorite,
                        onRetry = detailViewModel::refresh,
                        onOpenWebInfo = { url ->
                            val pokemonName = detailUiState.pokemonDetail?.name?.replaceFirstChar {
                                if (it.isLowerCase()) it.titlecase() else it.toString()
                            } ?: "Web Info"
                            currentWebTitle = "$pokemonName - Pokédex"
                            currentWebUrl = url
                        }
                    )
                }
            }
        )
    }
}
