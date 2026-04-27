package io.github.posaydone.kinopub.mobile.ui.screen.exploreScreen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import io.github.posaydone.kinopub.core.common.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExploreSearchBar(onSearch: (String) -> Unit) {
    var text by rememberSaveable { mutableStateOf("") }
    var expanded by rememberSaveable { mutableStateOf(false) }

    Box(Modifier.fillMaxWidth())
    {
        SearchBar(
            modifier = Modifier
                .align(Alignment.TopCenter),
            inputField = {
                SearchBarDefaults.InputField(
                    query = text,
                    onQueryChange = { text = it },
                    onSearch = { query ->
                        expanded = false
                        onSearch(query)
                    },
                    expanded = expanded,
                    onExpandedChange = { expanded = it },
                    placeholder = { Text(stringResource(R.string.search)) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    trailingIcon = {
                        AnimatedVisibility(expanded) {
                            IconButton(onClick = { expanded = false }) {
                                Icon(Icons.Filled.Close, contentDescription = null)
                            }
                        }
                    },
                )
            },
            expanded = expanded,
            onExpandedChange = { expanded = it },
        ) {
//            Column(Modifier.verticalScroll(rememberScrollState())) {
//                repeat(4) { idx ->
//                    val resultText = "Suggestion $idx"
//                    ListItem(
//                        headlineContent = { Text(resultText) },
//                        supportingContent = { Text("Additional info") },
//                        leadingContent = { Icon(Icons.Filled.Star, contentDescription = null) },
//                        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
//                        modifier =
//                        Modifier
//                            .clickable {
//                                text = resultText
//                                expanded = false
//                            }
//                            .fillMaxWidth()
//                            .padding(horizontal = 16.dp, vertical = 4.dp)
//                    )
//                }
//            }

        }
    }
}
