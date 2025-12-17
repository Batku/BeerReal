package ee.mips.beerreal.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Comment
import androidx.compose.material.icons.filled.ThumbDown
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material.icons.outlined.ThumbDown
import androidx.compose.material.icons.outlined.ThumbUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import ee.mips.beerreal.data.model.BeerPost
import ee.mips.beerreal.data.model.VoteType
import java.text.SimpleDateFormat
import java.util.*

import androidx.compose.foundation.Image
import ee.mips.beerreal.util.rememberBase64Image

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BeerPostCard(
    post: BeerPost,
    onVote: (String, VoteType) -> Unit = { _, _ -> },
    showExpandableComments: Boolean = true,
    onCommentsClick: (String) -> Unit = { },
    modifier: Modifier = Modifier
) {
    var showComments by remember { mutableStateOf(false) }
    val postImageBitmap = rememberBase64Image(post.imageData)
    val profileImageBitmap = rememberBase64Image(post.userProfileImageData)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable { 
                if (!showExpandableComments) {
                    onCommentsClick(post.id)
                }
            },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // User info header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Profile picture
                if (profileImageBitmap != null) {
                    Image(
                        bitmap = profileImageBitmap,
                        contentDescription = "Profile Picture",
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = post.username.take(1).uppercase(),
                            color = MaterialTheme.colorScheme.onPrimary,
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                }
                
                Spacer(modifier = Modifier.width(12.dp))
                
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = post.username,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = formatTimestamp(post.timestamp),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                if (post.location != null) {
                    Text(
                        text = post.location,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Beer image
            if (postImageBitmap != null) {
                Image(
                    bitmap = postImageBitmap,
                    contentDescription = "Beer Photo",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Image not available")
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Caption
            Text(
                text = post.caption,
                style = MaterialTheme.typography.bodyLarge,
                maxLines = 4,
                overflow = TextOverflow.Ellipsis
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Voting and comments
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Upvote
                    IconButton(
                        onClick = { onVote(post.id, VoteType.UPVOTE) }
                    ) {
                        Icon(
                            imageVector = if (post.userVoteType == VoteType.UPVOTE) {
                                Icons.Filled.ThumbUp
                            } else {
                                Icons.Outlined.ThumbUp
                            },
                            contentDescription = "Upvote",
                            tint = if (post.userVoteType == VoteType.UPVOTE) {
                                Color.Green
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            }
                        )
                    }
                    Text(
                        text = post.upvotes.toString(),
                        style = MaterialTheme.typography.bodyMedium
                    )
                    
                    Spacer(modifier = Modifier.width(16.dp))
                    
                    // Downvote
                    IconButton(
                        onClick = { onVote(post.id, VoteType.DOWNVOTE) }
                    ) {
                        Icon(
                            imageVector = if (post.userVoteType == VoteType.DOWNVOTE) {
                                Icons.Filled.ThumbDown
                            } else {
                                Icons.Outlined.ThumbDown
                            },
                            contentDescription = "Downvote",
                            tint = if (post.userVoteType == VoteType.DOWNVOTE) {
                                Color.Red
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            }
                        )
                    }
                    Text(
                        text = post.downvotes.toString(),
                        style = MaterialTheme.typography.bodyMedium
                    )
                    
                    Spacer(modifier = Modifier.width(16.dp))
                    
                    // Comments
                    IconButton(
                        onClick = { 
                            if (showExpandableComments) {
                                showComments = !showComments
                            } else {
                                onCommentsClick(post.id)
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Comment,
                            contentDescription = "Comments",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Text(
                        text = post.comments.size.toString(),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
            
            // Show comments based on mode
            if (showExpandableComments) {
                // Home screen mode - show expandable comments
                if (showComments && post.comments.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Divider()
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    post.comments.forEach { comment ->
                        CommentItem(comment = comment)
                    }
                } else if (showComments && post.comments.isEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Divider()
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "No comments yet. Be the first to comment!",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
            } else {
                // Home screen preview mode - show only first comment + view all button
                if (post.comments.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Divider()
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Show first comment
                    CommentItem(comment = post.comments.first())
                    
                    // Show "View all comments" button if there are more
                    if (post.comments.size > 1) {
                        TextButton(
                            onClick = { onCommentsClick(post.id) },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "View all ${post.comments.size} comments",
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun formatTimestamp(timestamp: Date): String {
    val now = Date()
    val diff = now.time - timestamp.time
    
    return when {
        diff < 60 * 1000 -> "Just now"
        diff < 60 * 60 * 1000 -> "${diff / (60 * 1000)}m ago"
        diff < 24 * 60 * 60 * 1000 -> "${diff / (60 * 60 * 1000)}h ago"
        else -> SimpleDateFormat("MMM dd", Locale.getDefault()).format(timestamp)
    }
}