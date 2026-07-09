package io.oryxen.mobile.ui.community

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import io.oryxen.mobile.data.CommunityRepository
import io.oryxen.mobile.data.remote.CommunityCommentResponse
import io.oryxen.mobile.data.remote.CommunityLikeResponse
import io.oryxen.mobile.data.remote.CommunityPostResponse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class CommunityUiState(
    val posts: List<CommunityPostResponse> = emptyList(),
    val loading: Boolean = false,
    val error: String? = null,
    val showCreateForm: Boolean = false,
    val creatingTitle: String = "",
    val creatingContent: String = "",
    val creatingImageBytes: ByteArray? = null,
    val creatingImageName: String? = null,
    val submitting: Boolean = false,
    val expandedPostId: String? = null,
    val commentText: String = "",
)

class CommunityViewModel : ViewModel() {
    private val _state = MutableStateFlow(CommunityUiState())
    val state: StateFlow<CommunityUiState> = _state.asStateFlow()

    init {
        loadFeed()
    }

    fun loadFeed() {
        viewModelScope.launch {
            _state.value = _state.value.copy(loading = true, error = null)
            try {
                val posts = CommunityRepository.getFeed()
                _state.value = _state.value.copy(posts = posts, loading = false)
            } catch (e: Exception) {
                _state.value = _state.value.copy(loading = false, error = e.message)
            }
        }
    }

    fun toggleCreateForm() {
        _state.value = _state.value.copy(showCreateForm = !_state.value.showCreateForm)
    }

    fun updateCreatingTitle(title: String) {
        _state.value = _state.value.copy(creatingTitle = title)
    }

    fun updateCreatingContent(content: String) {
        _state.value = _state.value.copy(creatingContent = content)
    }

    fun createPost(title: String, content: String, imageBytes: ByteArray?, imageName: String?) {
        viewModelScope.launch {
            _state.value = _state.value.copy(submitting = true)
            try {
                CommunityRepository.createPost(title, content, imageBytes, imageName)
                _state.value = _state.value.copy(
                    submitting = false,
                    showCreateForm = false,
                    creatingTitle = "",
                    creatingContent = "",
                    creatingImageBytes = null,
                    creatingImageName = null,
                )
                loadFeed()
            } catch (e: Exception) {
                _state.value = _state.value.copy(submitting = false, error = e.message)
            }
        }
    }

    fun toggleExpand(postId: String) {
        val current = _state.value.expandedPostId
        _state.value = _state.value.copy(
            expandedPostId = if (current == postId) null else postId,
            commentText = "",
        )
    }

    fun updateCommentText(text: String) {
        _state.value = _state.value.copy(commentText = text)
    }

    fun addComment(postId: String, content: String) {
        viewModelScope.launch {
            try {
                val comment = CommunityRepository.addComment(postId, content)
                val updatedPosts = _state.value.posts.map { post ->
                    if (post.id == postId) {
                        post.copy(comments = post.comments + comment)
                    } else post
                }
                _state.value = _state.value.copy(posts = updatedPosts, commentText = "")
            } catch (e: Exception) {
                _state.value = _state.value.copy(error = e.message)
            }
        }
    }

    fun toggleLike(postId: String) {
        viewModelScope.launch {
            try {
                val result = CommunityRepository.toggleLike(postId)
                val updatedPosts = _state.value.posts.map { post ->
                    if (post.id == postId) {
                        post.copy(
                            likesCount = result.likesCount,
                            likedByCurrentUser = result.likedByCurrentUser,
                        )
                    } else post
                }
                _state.value = _state.value.copy(posts = updatedPosts)
            } catch (e: Exception) {
                _state.value = _state.value.copy(error = e.message)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommunityScreen(
    viewModel: CommunityViewModel = viewModel(),
) {
    val state by viewModel.state.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Comunidad Agrícola") },

                actions = {
                    TextButton(onClick = { viewModel.toggleCreateForm() }) {
                        Text(if (state.showCreateForm) "Cancelar" else "Nuevo")
                    }
                },
            )
        },
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            if (state.showCreateForm) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        OutlinedTextField(
                            value = state.creatingTitle,
                            onValueChange = { viewModel.updateCreatingTitle(it) },
                            label = { Text("Título") },
                            modifier = Modifier.fillMaxWidth(),
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = state.creatingContent,
                            onValueChange = { viewModel.updateCreatingContent(it) },
                            label = { Text("Contenido") },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 2,
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = {
                                viewModel.createPost(
                                    state.creatingTitle,
                                    state.creatingContent,
                                    null, null,
                                )
                            },
                            enabled = state.creatingTitle.isNotBlank() && state.creatingContent.isNotBlank() && !state.submitting,
                            modifier = Modifier.align(Alignment.End),
                        ) {
                            if (state.submitting) {
                                CircularProgressIndicator(modifier = Modifier.size(20.dp))
                            } else {
                                Text("Publicar")
                            }
                        }
                    }
                }
            }

            if (state.loading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator()
                }
            } else if (state.posts.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        "Aún no hay publicaciones. ¡Sé el primero en compartir!",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    items(state.posts, key = { it.id }) { post ->
                        PostCard(
                            post = post,
                            expanded = state.expandedPostId == post.id,
                            commentText = if (state.expandedPostId == post.id) state.commentText else "",
                            onToggleExpand = { viewModel.toggleExpand(post.id) },
                            onToggleLike = { viewModel.toggleLike(post.id) },
                            onCommentTextChange = { viewModel.updateCommentText(it) },
                            onAddComment = { viewModel.addComment(post.id, it) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PostCard(
    post: CommunityPostResponse,
    expanded: Boolean,
    commentText: String,
    onToggleExpand: () -> Unit,
    onToggleLike: () -> Unit,
    onCommentTextChange: (String) -> Unit,
    onAddComment: (String) -> Unit,
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column {
                    Text(
                        post.authorName,
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.primary,
                    )
                    Text(
                        formatDate(post.createdAt),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                if (post.likesCount > 0) {
                    AssistChip(
                        onClick = {},
                        label = { Text("${post.likesCount} 👍") },
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                post.title,
                style = MaterialTheme.typography.titleMedium,
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                post.content,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                IconButton(onClick = onToggleLike) {
                    Icon(
                        if (post.likedByCurrentUser) Icons.Filled.Favorite
                        else Icons.Filled.FavoriteBorder,
                        contentDescription = if (post.likedByCurrentUser) "Quitar me gusta" else "Me gusta",
                        tint = if (post.likedByCurrentUser) MaterialTheme.colorScheme.error
                        else MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                TextButton(onClick = onToggleExpand) {
                    Text("${post.comments.size} comentarios")
                }
            }

            if (expanded) {
                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                post.comments.forEach { comment ->
                    CommentItem(comment)
                }

                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    OutlinedTextField(
                        value = commentText,
                        onValueChange = onCommentTextChange,
                        placeholder = { Text("Añadir comentario...") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                    )
                    IconButton(
                        onClick = { onAddComment(commentText) },
                        enabled = commentText.isNotBlank(),
                    ) {
                        Icon(Icons.Filled.Send, contentDescription = "Enviar")
                    }
                }
            }
        }
    }
}

@Composable
private fun CommentItem(comment: CommunityCommentResponse) {
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                comment.authorName,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
            )
            Text(
                formatDate(comment.createdAt),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Text(
            comment.content,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

private fun formatDate(iso: String): String {
    return try {
        val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
        val date = parser.parse(iso.take(19)) ?: return iso
        SimpleDateFormat("dd MMM yyyy HH:mm", Locale.getDefault()).format(date)
    } catch (_: Exception) {
        iso.take(16)
    }
}
