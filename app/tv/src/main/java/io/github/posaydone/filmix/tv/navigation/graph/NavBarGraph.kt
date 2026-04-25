package io.github.posaydone.filmix.tv.navigation.graph

import androidx.compose.foundation.background
import androidx.compose.foundation.focusGroup
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.focusRestorer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import io.github.posaydone.filmix.shared.graphData.NavBarGraphData
import androidx.tv.material3.DrawerValue
import androidx.tv.material3.Icon
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.ModalNavigationDrawer
import androidx.tv.material3.NavigationDrawerItem
import androidx.tv.material3.Text
import androidx.tv.material3.rememberDrawerState
import io.github.posaydone.filmix.shared.graphData.navBarScreenItems
import io.github.posaydone.filmix.shared.util.TopLevelBackStack

private val ClosedDrawerWidth = 80.dp

@Composable
fun NavBarGraph(
    topLevelBackStack: TopLevelBackStack<Any>,
    drawerFocusRequester: FocusRequester,
    contentFocusRequester: FocusRequester,
    content: @Composable () -> Unit,
) {
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val isActive = topLevelBackStack.backStack.last() is NavBarGraphData

    Box(
        modifier = Modifier
            .fillMaxSize()
            .focusGroup()
            .focusProperties {
                onEnter = { if (isActive) FocusRequester.Default else FocusRequester.Cancel }
            }
    ) {
        ModalNavigationDrawer(
            modifier = Modifier.focusRestorer(contentFocusRequester),
            drawerState = drawerState,
            drawerContent = {
                LazyColumn(
                    userScrollEnabled = false,
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(12.dp)
                        .fillMaxHeight(),
                    verticalArrangement = Arrangement.spacedBy(
                        8.dp, alignment = Alignment.CenterVertically
                    ),
                ) {
                    itemsIndexed(navBarScreenItems) { _, item ->
                        val context = LocalContext.current
                        val text = getLocalizedTitle(context, item)
                        val icon = getIcon(item.icon)
                        val isSelected = item == topLevelBackStack.topLevelKey

                        NavigationDrawerItem(
                            selected = isSelected,
                            onClick = {
                                topLevelBackStack.addTopLevel(item)
                                drawerState.setValue(DrawerValue.Closed)
                                contentFocusRequester.requestFocus()
                            },
                            leadingContent = {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = null,
                                )
                            },
                        ) {
                            Text(text)
                        }
                    }
                }
            },
        ) {
            Box(modifier = Modifier.padding(start = ClosedDrawerWidth)) {
                content()
            }
        }
    }
}
