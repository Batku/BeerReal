package ee.mips.beerreal.ui.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import ee.mips.beerreal.ui.screens.friends.FriendsDialog
import kotlinx.coroutines.launch
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.net.Uri
import ee.mips.beerreal.data.model.User

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel = viewModel(),
    scrollToTopTrigger: Int = 0,
    onSettingsClick: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val gridState = rememberLazyGridState()
    val coroutineScope = rememberCoroutineScope()
    var showEditDialog by remember { mutableStateOf(false) }
    
    LaunchedEffect(scrollToTopTrigger) {
        if (scrollToTopTrigger > 0) {
            coroutineScope.launch {
                gridState.animateScrollToItem(0)
            }
        }
    }
    
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        TopAppBar(
            title = {
                Text(
                    text = "Profile",
                    fontWeight = FontWeight.Bold
                )
            },
            actions = {
                IconButton(onClick = { viewModel.onFindFriendsClick() }) {
                    Icon(
                        imageVector = Icons.Default.PersonAdd,
                        contentDescription = "Find Friends"
                    )
                }
                IconButton(onClick = { viewModel.refreshProfile() }) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Refresh"
                    )
                }
                IconButton(onClick = { onSettingsClick() }) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Settings"
                    )
                }
            }
        )
        
        uiState.errorMessage?.let { errorMessage ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Text(
                    text = errorMessage,
                    modifier = Modifier.padding(16.dp),
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
            }
        }
        
        Box(modifier = Modifier.fillMaxSize()) {
            when {
                uiState.isLoading && uiState.user == null -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                else -> {
                    uiState.user?.let { user ->
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(3),
                            state = gridState,
                            modifier = Modifier.fillMaxSize(),
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                            contentPadding = PaddingValues(bottom = 16.dp)
                        ) {
                            // Profile header
                            item(span = { GridItemSpan(3) }) {
                                ProfileHeader(user = user)
                            }
                            
                            // Posts section header
                            item(span = { GridItemSpan(3) }) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp)
                                ) {
                                    Text(
                                        text = "Posts",
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.height(16.dp))
                                }
                            }
                            
                            // Loading state
                            if (uiState.isLoadingPosts) {
                                item(span = { GridItemSpan(3) }) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(200.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        CircularProgressIndicator()
                                    }
                                }
                            }
                            // Empty state
                            else if (uiState.userPosts.isEmpty()) {
                                item(span = { GridItemSpan(3) }) {
                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(200.dp)
                                            .padding(horizontal = 16.dp),
                                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                                    ) {
                                        Column(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .padding(24.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.Center
                                        ) {
                                            Text(
                                                text = "🍺",
                                                style = MaterialTheme.typography.displayMedium
                                            )
                                            Spacer(modifier = Modifier.height(8.dp))
                                            Text(
                                                text = "No posts yet",
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Text(
                                                text = "Share your first beer!",
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }
                            }
                            else {
                                items(uiState.userPosts) { post ->
                                    PostGridItem(
                                        post = post,
                                        modifier = Modifier.padding(horizontal = 2.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
        
        if (showEditDialog) {
            uiState.user?.let { user ->
                EditProfileDialog(
                    user = user,
                    onDismiss = { showEditDialog = false },
                    onSave = { username, uri, bio ->
                        viewModel.updateProfile(username, uri, bio)
                        showEditDialog = false
                    }
                )
            }
        }

        if (uiState.showFriendsDialog) {
            FriendsDialog(
                users = uiState.allUsers,
                onDismiss = viewModel::onDismissFriendsDialog
            )
        }
    }
}

@Composable
@Composable
private fun ProfileHeader(
    user: ee.mips.beerreal.data.model.User,
    onEditClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Profile picture
        if (user.profileImageData != null) {
            AsyncImage(
                model = user.profileImageData,
                contentDescription = "Profile Picture",
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentScale = ContentScale.Crop
            )
        } else {
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = user.username.take(1).uppercase(),
                    style = MaterialTheme.typography.headlineLarge,
                    color = MaterialTheme.colorScheme.onPrimary,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = user.username,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        if (user.bio != null) {
            Text(
                text = user.bio,
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(16.dp))
        }
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatCard(
                title = "Taste Score",
                value = user.tasteScore.toString(),
                modifier = Modifier.weight(1f)
            )
            StatCard(
                title = "Posts",
                value = user.totalPosts.toString(),
                modifier = Modifier.weight(1f)
            )
            StatCard(
                title = "Friends",
                value = user.friendsCount.toString(),
                modifier = Modifier.weight(1f)
            )
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Edit Profile Button
        Button(
            onClick = onEditClick,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                imageVector = Icons.Default.Edit,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Edit Profile")
        }
    }
}

@Composable
private fun StatCard(
    title: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun InfoRow(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun PostGridItem(
    post: ee.mips.beerreal.data.model.BeerPost,
    modifier: Modifier = Modifier
) {
    // just text for now, will be the actual images later
    Card(
        modifier = modifier
            .aspectRatio(1f)
            .fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        if (post.imageData.isNotEmpty()) {
             AsyncImage(
                model = post.imageData,
                contentDescription = "Beer Photo",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "🍺",
                        style = MaterialTheme.typography.headlineSmall
                    )
                    Text(
                        text = "❤️ ${post.upvotes}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileDialog(
    user: User,
    onDismiss: () -> Unit,
    onSave: (String, Uri?, String) -> Unit
) {
    var username by remember { mutableStateOf(user.username) }
    var bio by remember { mutableStateOf(user.bio ?: "") }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        selectedImageUri = uri
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Profile") },
        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .clickable { launcher.launch("image/*") },
                    contentAlignment = Alignment.Center
                ) {
                    if (selectedImageUri != null) {
                        AsyncImage(
                            model = selectedImageUri,
                            contentDescription = "Selected Image",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else if (user.profileImageData != null) {
                        AsyncImage(
                            model = user.profileImageData,
                            contentDescription = "Current Image",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Icon(Icons.Default.Edit, contentDescription = "Edit Image")
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = username,
                    onValueChange = { username = it },
                    label = { Text("Username") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = bio,
                    onValueChange = { bio = it },
                    label = { Text("Bio") },
                    maxLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onSave(username, selectedImageUri, bio) }
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

