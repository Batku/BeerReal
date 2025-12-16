package ee.mips.beerreal.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.LocationOn
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
import androidx.lifecycle.viewmodel.compose.viewModel
import ee.mips.beerreal.ui.screens.add.AddBeerScreen
import ee.mips.beerreal.ui.screens.add.AddBeerViewModel
import ee.mips.beerreal.ui.screens.camera.CameraScreen
import ee.mips.beerreal.ui.screens.home.HomeScreen
import ee.mips.beerreal.ui.screens.post.PostDetailScreen
import ee.mips.beerreal.ui.screens.profile.ProfileScreen
import ee.mips.beerreal.ui.screens.settings.SettingsScreen
import ee.mips.beerreal.ui.screens.map.MapScreen
import ee.mips.beerreal.ui.screens.login.LoginScreen
import com.google.firebase.auth.FirebaseAuth
import androidx.compose.ui.platform.LocalContext
import ee.mips.beerreal.data.repository.BeerRepository
import ee.mips.beerreal.ui.screens.home.HomeViewModel
import ee.mips.beerreal.ui.screens.post.PostDetailViewModel
import ee.mips.beerreal.ui.screens.profile.ProfileViewModel

sealed class Screen(val route: String, val title: String, val icon: androidx.compose.ui.graphics.vector.ImageVector? = null) {
    object Login : Screen("login", "Login")
    object Map : Screen("map", "Map", Icons.Filled.LocationOn)
    object Home : Screen("home", "Home", Icons.Filled.Home)
    object Add : Screen("add", "Add", Icons.Filled.Add)
    object Profile : Screen("profile", "Profile", Icons.Filled.Person)
    object Settings : Screen("settings", "Settings")
    object Camera : Screen("camera", "Camera")
    object PostDetail : Screen("post_detail/{postId}", "Post Detail")
    
    fun createPostDetailRoute(postId: String) = "post_detail/$postId"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BeerRealApp() {
    val navController = rememberNavController()
    var homeScrollToTop by remember { mutableStateOf(0) }
    var profileScrollToTop by remember { mutableStateOf(0) }
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    
    Scaffold(
        bottomBar = {
            if (currentRoute != Screen.Login.route && currentRoute != Screen.Camera.route) {
                BottomNavigation(
                    navController = navController,
                    onHomeScrollToTop = { homeScrollToTop++ },
                    onProfileScrollToTop = { profileScrollToTop++ }
                )
            }
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
        Screen.Map,
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
    val context = LocalContext.current
    val repository = remember { BeerRepository(context) }

    val startDestination = if (FirebaseAuth.getInstance().currentUser != null) {
        Screen.Home.route
    } else {
        Screen.Login.route
    }

    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        composable(Screen.Login.route) {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }
        composable(Screen.Map.route) {
            MapScreen()
        }
        composable(Screen.Home.route) {
            HomeScreen(
                scrollToTopTrigger = homeScrollToTop,
                onNavigateToPost = { postId ->
                    navController.navigate(Screen.PostDetail.createPostDetailRoute(postId))
                }
            )
        }
        composable(Screen.Add.route) { backStackEntry ->
            val viewModel: AddBeerViewModel = viewModel(
                factory = object : androidx.lifecycle.ViewModelProvider.Factory {
                    override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                        return AddBeerViewModel(repository) as T
                    }
                }
            )
            
            val savedStateHandle = backStackEntry.savedStateHandle
            val imageUriString = savedStateHandle.get<String>("imageUri")
            if (imageUriString != null) {
                val imageUri = android.net.Uri.parse(imageUriString)
                viewModel.updateImageUri(imageUri)
                savedStateHandle.remove<String>("imageUri")
            }

            AddBeerScreen(
                onNavigateBack = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Home.route) {
                            inclusive = false
                        }
                    }
                },
                onTakePhotoClick = {
                    navController.navigate(Screen.Camera.route)
                },
                viewModel = viewModel
            )
        }
        composable(Screen.Camera.route) {
            CameraScreen(
                onImageCaptured = { uri ->
                    navController.previousBackStackEntry?.savedStateHandle?.set("imageUri", uri.toString())
                    navController.popBackStack()
                },
                onError = {
                    navController.popBackStack()
                }
            )
        }
        composable(Screen.Profile.route) {
            ProfileScreen(
                scrollToTopTrigger = profileScrollToTop,
                onSettingsClick = { navController.navigate(Screen.Settings.route) }
            )
        }
        composable(Screen.Settings.route) {
            SettingsScreen(
                onNavigateBack = { navController.popBackStack() },
                onLogout = {
                    FirebaseAuth.getInstance().signOut()
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
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