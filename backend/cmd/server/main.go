package main

import (
	"log"

	"github.com/batku/beerreal/internal/config"
	"github.com/batku/beerreal/internal/database"
	"github.com/batku/beerreal/internal/handlers"
	"github.com/batku/beerreal/internal/middleware"
	"github.com/batku/beerreal/internal/repository"
	"github.com/batku/beerreal/internal/service"
	"github.com/gin-gonic/gin"
)

func main() {
	// Load configuration
	cfg := config.LoadConfig()

	// Set Gin mode
	gin.SetMode(cfg.GinMode)

	// Initialize database
	db, err := database.NewDatabase(cfg.DatabasePath)
	if err != nil {
		log.Fatalf("Failed to initialize database: %v", err)
	}
	defer db.Close()

	// Initialize Firebase Auth
	firebaseAuth, err := middleware.NewFirebaseAuth(cfg.FirebaseCredentialsPath)
	if err != nil {
		log.Fatalf("Failed to initialize Firebase Auth: %v", err)
	}

	// Initialize repository, service, and handler layers
	postRepo := repository.NewPostRepository(db.DB)
	postService := service.NewPostService(postRepo)
	postHandler := handlers.NewPostHandler(postService)

	// Setup router
	router := gin.Default()

	// CORS middleware
	router.Use(func(c *gin.Context) {
		c.Writer.Header().Set("Access-Control-Allow-Origin", "*")
		c.Writer.Header().Set("Access-Control-Allow-Credentials", "true")
		c.Writer.Header().Set("Access-Control-Allow-Headers", "Content-Type, Content-Length, Accept-Encoding, X-CSRF-Token, Authorization, accept, origin, Cache-Control, X-Requested-With")
		c.Writer.Header().Set("Access-Control-Allow-Methods", "POST, OPTIONS, GET, PUT, DELETE, PATCH")

		if c.Request.Method == "OPTIONS" {
			c.AbortWithStatus(204)
			return
		}

		c.Next()
	})

	// Health check endpoint
	router.GET("/health", func(c *gin.Context) {
		c.JSON(200, gin.H{
			"status":  "ok",
			"message": "BeerReal API is running",
		})
	})

	// API routes
	api := router.Group("/api")
	{
		// Register post routes
		postHandler.RegisterRoutes(api, firebaseAuth.AuthMiddleware())
	}

	// Start server
	log.Printf("Starting BeerReal server on port %s", cfg.Port)
	if err := router.Run(":" + cfg.Port); err != nil {
		log.Fatalf("Failed to start server: %v", err)
	}
}
