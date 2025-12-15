package main

import (
	"log"
	"os"

	"github.com/batku/beerreal/internal/config"
	"github.com/batku/beerreal/internal/database"
)

func main() {
	cfg := config.LoadConfig()

	// Initialize database
	db, err := database.NewDatabase(cfg.DatabasePath)
	if err != nil {
		log.Fatalf("Failed to initialize database: %v", err)
	}
	defer db.Close()

	log.Println("Database initialized successfully at:", cfg.DatabasePath)

	// Check if we should seed data
	if len(os.Args) > 1 && os.Args[1] == "seed" {
		log.Println("Seeding not implemented yet")
	}
}
