package database

import (
	"database/sql"
	"fmt"
	"log"

	_ "github.com/mattn/go-sqlite3"
)

type Database struct {
	DB *sql.DB
}

func NewDatabase(dbPath string) (*Database, error) {
	db, err := sql.Open("sqlite3", dbPath)
	if err != nil {
		return nil, fmt.Errorf("failed to open database: %w", err)
	}

	if err := db.Ping(); err != nil {
		return nil, fmt.Errorf("failed to ping database: %w", err)
	}

	database := &Database{DB: db}

	if err := database.migrate(); err != nil {
		return nil, fmt.Errorf("failed to migrate database: %w", err)
	}

	log.Println("Database connection established and migrations applied")
	return database, nil
}

func (d *Database) migrate() error {
	migrations := []string{
		`CREATE TABLE IF NOT EXISTS users (
			id TEXT PRIMARY KEY,
			username TEXT NOT NULL UNIQUE,
			email TEXT NOT NULL UNIQUE,
			profile_image_url TEXT,
			taste_score INTEGER DEFAULT 0,
			total_posts INTEGER DEFAULT 0,
			friends_count INTEGER DEFAULT 0,
			joined_date DATETIME DEFAULT CURRENT_TIMESTAMP,
			bio TEXT,
			created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
			updated_at DATETIME DEFAULT CURRENT_TIMESTAMP
		)`,
		`CREATE TABLE IF NOT EXISTS beer_posts (
			id TEXT PRIMARY KEY,
			user_id TEXT NOT NULL,
			caption TEXT NOT NULL,
			image_url TEXT NOT NULL,
			location TEXT,
			timestamp DATETIME DEFAULT CURRENT_TIMESTAMP,
			upvotes INTEGER DEFAULT 0,
			downvotes INTEGER DEFAULT 0,
			created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
			updated_at DATETIME DEFAULT CURRENT_TIMESTAMP,
			FOREIGN KEY (user_id) REFERENCES users(id)
		)`,
		`CREATE INDEX IF NOT EXISTS idx_beer_posts_user_id ON beer_posts(user_id)`,
		`CREATE INDEX IF NOT EXISTS idx_beer_posts_timestamp ON beer_posts(timestamp DESC)`,
		`CREATE TABLE IF NOT EXISTS comments (
			id TEXT PRIMARY KEY,
			post_id TEXT NOT NULL,
			user_id TEXT NOT NULL,
			text TEXT NOT NULL,
			timestamp DATETIME DEFAULT CURRENT_TIMESTAMP,
			created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
			updated_at DATETIME DEFAULT CURRENT_TIMESTAMP,
			FOREIGN KEY (post_id) REFERENCES beer_posts(id) ON DELETE CASCADE,
			FOREIGN KEY (user_id) REFERENCES users(id)
		)`,
		`CREATE INDEX IF NOT EXISTS idx_comments_post_id ON comments(post_id)`,
		`CREATE TABLE IF NOT EXISTS votes (
			id TEXT PRIMARY KEY,
			post_id TEXT NOT NULL,
			user_id TEXT NOT NULL,
			vote_type TEXT NOT NULL CHECK(vote_type IN ('UPVOTE', 'DOWNVOTE')),
			created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
			updated_at DATETIME DEFAULT CURRENT_TIMESTAMP,
			FOREIGN KEY (post_id) REFERENCES beer_posts(id) ON DELETE CASCADE,
			FOREIGN KEY (user_id) REFERENCES users(id),
			UNIQUE(post_id, user_id)
		)`,
		`CREATE INDEX IF NOT EXISTS idx_votes_post_id ON votes(post_id)`,
		`CREATE INDEX IF NOT EXISTS idx_votes_user_id ON votes(user_id)`,
	}

	for _, migration := range migrations {
		if _, err := d.DB.Exec(migration); err != nil {
			return fmt.Errorf("migration failed: %w", err)
		}
	}

	return nil
}

func (d *Database) Close() error {
	return d.DB.Close()
}
