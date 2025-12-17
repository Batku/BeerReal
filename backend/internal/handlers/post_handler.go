package handlers

import (
	"log"
	"net/http"
	"strconv"

	"github.com/batku/beerreal/internal/middleware"
	"github.com/batku/beerreal/internal/models"
	"github.com/batku/beerreal/internal/service"
	"github.com/gin-gonic/gin"
)

type PostHandler struct {
	service service.PostService
}

func NewPostHandler(service service.PostService) *PostHandler {
	return &PostHandler{service: service}
}

// CreatePost godoc
// @Summary Create a new beer post
// @Description Create a new post with caption and image
// @Tags posts
// @Accept json
// @Produce json
// @Security BearerAuth
// @Param post body models.CreatePostRequest true "Post details"
// @Success 201 {object} models.BeerPost
// @Failure 400 {object} map[string]string
// @Failure 401 {object} map[string]string
// @Failure 500 {object} map[string]string
// @Router /api/posts [post]
func (h *PostHandler) CreatePost(c *gin.Context) {
	log.Println("[CreatePost] Request received")
	userID, exists := middleware.GetUserID(c)
	if !exists {
		log.Println("[CreatePost] ERROR: User not authenticated")
		c.JSON(http.StatusUnauthorized, gin.H{"error": "User not authenticated"})
		return
	}
	log.Printf("[CreatePost] UserID: %s", userID)

	var req models.CreatePostRequest
	if err := c.ShouldBindJSON(&req); err != nil {
		log.Printf("[CreatePost] ERROR: Invalid request body: %v", err)
		c.JSON(http.StatusBadRequest, gin.H{"error": err.Error()})
		return
	}
	log.Printf("[CreatePost] Request data - Caption: %s, Location: %v, ImageDataLength: %d", req.Caption, req.Location, len(req.ImageData))

	post, err := h.service.CreatePost(userID, &req)
	if err != nil {
		log.Printf("[CreatePost] ERROR: Failed to create post: %v", err)
		c.JSON(http.StatusInternalServerError, gin.H{"error": err.Error()})
		return
	}

	log.Printf("[CreatePost] SUCCESS: Post created with ID: %s", post.ID)
	c.JSON(http.StatusCreated, post)
}

// GetPosts godoc
// @Summary Get all beer posts
// @Description Get paginated list of beer posts
// @Tags posts
// @Produce json
// @Param page query int false "Page number" default(1)
// @Param pageSize query int false "Page size" default(20)
// @Success 200 {object} models.GetPostsResponse
// @Failure 500 {object} map[string]string
// @Router /api/posts [get]
func (h *PostHandler) GetPosts(c *gin.Context) {
	log.Println("[GetPosts] Request received")
	page, _ := strconv.Atoi(c.DefaultQuery("page", "1"))
	pageSize, _ := strconv.Atoi(c.DefaultQuery("pageSize", "20"))
	log.Printf("[GetPosts] Parameters - Page: %d, PageSize: %d", page, pageSize)

	// Get user ID if authenticated (optional for viewing posts)
	userID, _ := middleware.GetUserID(c)
	if userID != "" {
		log.Printf("[GetPosts] Authenticated user: %s", userID)
	} else {
		log.Println("[GetPosts] Unauthenticated request")
	}

	response, err := h.service.GetPosts(userID, page, pageSize)
	if err != nil {
		log.Printf("[GetPosts] ERROR: Failed to get posts: %v", err)
		c.JSON(http.StatusInternalServerError, gin.H{"error": err.Error()})
		return
	}

	log.Printf("[GetPosts] SUCCESS: Returning %d posts (total: %d)", len(response.Posts), response.TotalCount)
	c.JSON(http.StatusOK, response)
}

// GetPost godoc
// @Summary Get a single beer post
// @Description Get a beer post by ID
// @Tags posts
// @Produce json
// @Param id path string true "Post ID"
// @Success 200 {object} models.BeerPost
// @Failure 404 {object} map[string]string
// @Failure 500 {object} map[string]string
// @Router /api/posts/{id} [get]
func (h *PostHandler) GetPost(c *gin.Context) {
	postID := c.Param("id")
	log.Printf("[GetPost] Request received for post ID: %s", postID)
	if postID == "" {
		log.Println("[GetPost] ERROR: Post ID is empty")
		c.JSON(http.StatusBadRequest, gin.H{"error": "Post ID is required"})
		return
	}

	// Get user ID if authenticated (optional)
	userID, _ := middleware.GetUserID(c)
	if userID != "" {
		log.Printf("[GetPost] Authenticated user: %s", userID)
	} else {
		log.Println("[GetPost] Unauthenticated request")
	}

	post, err := h.service.GetPostByID(postID, userID)
	if err != nil {
		log.Printf("[GetPost] ERROR: Failed to get post: %v", err)
		c.JSON(http.StatusNotFound, gin.H{"error": "Post not found"})
		return
	}

	log.Printf("[GetPost] SUCCESS: Returning post ID: %s", post.ID)
	c.JSON(http.StatusOK, post)
}

// RegisterRoutes registers all post-related routes
func (h *PostHandler) RegisterRoutes(router *gin.RouterGroup, authMiddleware gin.HandlerFunc, optionalAuthMiddleware gin.HandlerFunc) {
	posts := router.Group("/posts")
	{
		// Public routes (no auth required, but optional auth for user context)
		posts.GET("", optionalAuthMiddleware, h.GetPosts)
		posts.GET("/:id", optionalAuthMiddleware, h.GetPost)

		// Protected routes (auth required)
		posts.POST("", authMiddleware, h.CreatePost)
		posts.POST("/vote", authMiddleware, h.VotePost)
		posts.POST("/comment", authMiddleware, h.AddComment)
	}
}

// VotePost godoc
// @Summary Vote on a post
// @Description Upvote or downvote a post
// @Tags posts
// @Accept json
// @Produce json
// @Security BearerAuth
// @Param vote body models.VoteRequest true "Vote details"
// @Success 200 {object} models.VoteResponse
// @Failure 400 {object} map[string]string
// @Failure 401 {object} map[string]string
// @Failure 500 {object} map[string]string
// @Router /api/posts/vote [post]
func (h *PostHandler) VotePost(c *gin.Context) {
	userID, exists := middleware.GetUserID(c)
	if !exists {
		c.JSON(http.StatusUnauthorized, gin.H{"error": "User not authenticated"})
		return
	}

	var req models.VoteRequest
	if err := c.ShouldBindJSON(&req); err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"error": err.Error()})
		return
	}

	response, err := h.service.VotePost(userID, &req)
	if err != nil {
		c.JSON(http.StatusInternalServerError, gin.H{"error": err.Error()})
		return
	}

	c.JSON(http.StatusOK, response)
}

// AddComment godoc
// @Summary Add a comment to a post
// @Description Add a comment to a post
// @Tags posts
// @Accept json
// @Produce json
// @Security BearerAuth
// @Param comment body models.AddCommentRequest true "Comment details"
// @Success 201 {object} models.Comment
// @Failure 400 {object} map[string]string
// @Failure 401 {object} map[string]string
// @Failure 500 {object} map[string]string
// @Router /api/posts/comment [post]
func (h *PostHandler) AddComment(c *gin.Context) {
	userID, exists := middleware.GetUserID(c)
	if !exists {
		c.JSON(http.StatusUnauthorized, gin.H{"error": "User not authenticated"})
		return
	}

	var req models.AddCommentRequest
	if err := c.ShouldBindJSON(&req); err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"error": err.Error()})
		return
	}

	comment, err := h.service.AddComment(userID, &req)
	if err != nil {
		c.JSON(http.StatusInternalServerError, gin.H{"error": err.Error()})
		return
	}

	c.JSON(http.StatusCreated, comment)
}
