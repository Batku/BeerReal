package ee.mips.beerreal.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import ee.mips.beerreal.ui.screens.add.AddBeerScreen
import ee.mips.beerreal.ui.screens.home.HomeScreen
import ee.mips.beerreal.ui.screens.post.PostDetailScreen
import ee.mips.beerreal.ui.screens.profile.ProfileScreen

sealed class Screen(val route: String, val title: String, val icon: androidx.compose.ui.graphics.vector.ImageVector? = null) {
    object Home : Screen("home", "Home", Icons.Filled.Home)
    object Add : Screen("add", "Add", Icons.Filled.Add)
    object Profile : Screen("profile", "Profile", Icons.Filled.Person)
    object PostDetail : Screen("post_detail/{postId}", "Post Detail")
    
    fun createPostDetailRoute(postId: String) = "post_detail/$postId"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BeerRealApp() {
    val navController = rememberNavController()
    var homeScrollToTop by remember { mutableStateOf(0) }
    var profileScrollToTop by remember { mutableStateOf(0) }
    
    Scaffold(
        bottomBar = {
            BottomNavigation(
                navController = navController,
                onHomeScrollToTop = { homeScrollToTop++ },
                onProfileScrollToTop = { profileScrollToTop++ }
            )
        }
    ) { innerPadding ->
        BeerRealNavHost(
            navController = navController,
            modifier = Modifier.padding(innerPadding),
            homeScrollToTop = homeScrollToTop,
            profileScrollToTop = profileScrollToTop
        )
    }
}

@Composable
fun BottomNavigation(
    navController: NavHostController,
    onHomeScrollToTop: () -> Unit,
    onProfileScrollToTop: () -> Unit
) {
    val screens = listOf(
        Screen.Home,
        Screen.Add,
        Screen.Profile
    ).filter { it.icon != null }
    
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    
    NavigationBar {
        screens.forEach { screen ->
            val isSelected = currentDestination?.hierarchy?.any { it.route == screen.route } == true
            
            NavigationBarItem(
                icon = { Icon(screen.icon!!, contentDescription = screen.title) },
                label = { Text(screen.title) },
                selected = isSelected,
                onClick = {
                    if (isSelected) {
                        // If already on this screen, scroll to top
                        when (screen.route) {
                            Screen.Home.route -> onHomeScrollToTop()
                            Screen.Profile.route -> onProfileScrollToTop()
                        }
                    } else {
                        navController.navigate(screen.route) {
                            // Pop up to the start destination of the graph to
                            // avoid building up a large stack of destinations
                            // on the back stack as users select items
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            // Avoid multiple copies of the same destination when
                            // reselecting the same item
                            launchSingleTop = true
                            // Restore state when reselecting a previously selected item
                            restoreState = true
                        }
                    }
                }
            )
        }
    }
}

@Composable
fun BeerRealNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    homeScrollToTop: Int,
    profileScrollToTop: Int
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Home.route,
        modifier = modifier
    ) {
        composable(Screen.Home.route) {
            HomeScreen(
                scrollToTopTrigger = homeScrollToTop,
                onNavigateToPost = { postId ->
                    navController.navigate(Screen.PostDetail.createPostDetailRoute(postId))
                }
            )
        }
        composable(Screen.Add.route) {
            AddBeerScreen(
                onNavigateBack = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Home.route) {
                            inclusive = false
                        }
                    }
                }
            )
        }
        composable(Screen.Profile.route) {
            ProfileScreen(scrollToTopTrigger = profileScrollToTop)
        }
        composable(Screen.PostDetail.route) { backStackEntry ->
            val postId = backStackEntry.arguments?.getString("postId") ?: ""
            PostDetailScreen(
                postId = postId,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}