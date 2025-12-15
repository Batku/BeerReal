package handlers

import (
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
	userID, exists := middleware.GetUserID(c)
	if !exists {
		c.JSON(http.StatusUnauthorized, gin.H{"error": "User not authenticated"})
		return
	}

	var req models.CreatePostRequest
	if err := c.ShouldBindJSON(&req); err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"error": err.Error()})
		return
	}

	post, err := h.service.CreatePost(userID, &req)
	if err != nil {
		c.JSON(http.StatusInternalServerError, gin.H{"error": err.Error()})
		return
	}

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
	page, _ := strconv.Atoi(c.DefaultQuery("page", "1"))
	pageSize, _ := strconv.Atoi(c.DefaultQuery("pageSize", "20"))

	// Get user ID if authenticated (optional for viewing posts)
	userID, _ := middleware.GetUserID(c)

	response, err := h.service.GetPosts(userID, page, pageSize)
	if err != nil {
		c.JSON(http.StatusInternalServerError, gin.H{"error": err.Error()})
		return
	}

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
	if postID == "" {
		c.JSON(http.StatusBadRequest, gin.H{"error": "Post ID is required"})
		return
	}

	// Get user ID if authenticated (optional)
	userID, _ := middleware.GetUserID(c)

	post, err := h.service.GetPostByID(postID, userID)
	if err != nil {
		c.JSON(http.StatusNotFound, gin.H{"error": "Post not found"})
		return
	}

	c.JSON(http.StatusOK, post)
}

// RegisterRoutes registers all post-related routes
func (h *PostHandler) RegisterRoutes(router *gin.RouterGroup, authMiddleware gin.HandlerFunc) {
	posts := router.Group("/posts")
	{
		// Public routes (no auth required)
		posts.GET("", h.GetPosts)
		posts.GET("/:id", h.GetPost)

		// Protected routes (auth required)
		posts.POST("", authMiddleware, h.CreatePost)
	}
}
