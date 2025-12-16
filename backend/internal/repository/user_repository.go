package repository

import (
	"database/sql"
	"errors"

	"github.com/batku/beerreal/internal/models"
)

type UserRepository struct {
	db *sql.DB
}

func NewUserRepository(db *sql.DB) *UserRepository {
	return &UserRepository{db: db}
}

func (r *UserRepository) GetUserByID(id string) (*models.User, error) {
	query := `
		SELECT id, username, email, profile_image_url, taste_score, total_posts, friends_count, joined_date, bio, created_at, updated_at
		FROM users
		WHERE id = ?
	`
	row := r.db.QueryRow(query, id)

	var user models.User
	err := row.Scan(
		&user.ID, &user.Username, &user.Email, &user.ProfileImageURL,
		&user.TasteScore, &user.TotalPosts, &user.FriendsCount,
		&user.JoinedDate, &user.Bio, &user.CreatedAt, &user.UpdatedAt,
	)
	if err != nil {
		if errors.Is(err, sql.ErrNoRows) {
			return nil, nil
		}
		return nil, err
	}

	return &user, nil
}

func (r *UserRepository) CreateUser(user *models.User) error {
	query := `
		INSERT INTO users (id, username, email, profile_image_url, taste_score, total_posts, friends_count, joined_date, bio, created_at, updated_at)
		VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
	`
	_, err := r.db.Exec(query,
		user.ID, user.Username, user.Email, user.ProfileImageURL,
		user.TasteScore, user.TotalPosts, user.FriendsCount,
		user.JoinedDate, user.Bio, user.CreatedAt, user.UpdatedAt,
	)
	return err
}
