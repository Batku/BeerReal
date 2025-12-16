package handlers

import (
	"net/http"

	"github.com/batku/beerreal/internal/models"
	"github.com/batku/beerreal/internal/service"
	"github.com/gin-gonic/gin"
)

type UserHandler struct {
	service *service.UserService
}

func NewUserHandler(service *service.UserService) *UserHandler {
	return &UserHandler{service: service}
}

func (h *UserHandler) GetMe(c *gin.Context) {
	userID, exists := c.Get("userID")
	if !exists {
		c.JSON(http.StatusUnauthorized, gin.H{"error": "User ID not found in context"})
		return
	}

	email, exists := c.Get("email")
	if !exists {
		// Email might not be available depending on provider, but for now let's assume it is or handle it
		email = "" 
	}

	user, err := h.service.GetOrCreateUser(userID.(string), email.(string))
	if err != nil {
		c.JSON(http.StatusInternalServerError, gin.H{"error": "Failed to get or create user"})
		return
	}

	c.JSON(http.StatusOK, user)
}

func (h *UserHandler) UpdateUser(c *gin.Context) {
	userID, exists := c.Get("userID")
	if !exists {
		c.JSON(http.StatusUnauthorized, gin.H{"error": "User ID not found in context"})
		return
	}

	var req models.UpdateUserRequest
	if err := c.ShouldBindJSON(&req); err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"error": err.Error()})
		return
	}

	user, err := h.service.UpdateUser(userID.(string), &req)
	if err != nil {
		c.JSON(http.StatusInternalServerError, gin.H{"error": err.Error()})
		return
	}

	c.JSON(http.StatusOK, user)
}

func (h *UserHandler) RegisterRoutes(router *gin.RouterGroup, authMiddleware gin.HandlerFunc) {
	// Protected routes
	router.GET("/me", authMiddleware, h.GetMe)
	router.PUT("/me", authMiddleware, h.UpdateUser)
}

