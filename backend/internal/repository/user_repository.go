package repository

import (
	"database/sql"
	"errors"

	"github.com/batku/beerreal/internal/models"
)

type UserRepository interface {
	GetUserByID(id string) (*models.User, error)
	CreateOrUpdateUser(user *models.User) error
	UpdateUser(user *models.User) error
	UpdateTasteScore(userID string, scoreChange int) error
}

type userRepository struct {
	db *sql.DB
}

func NewUserRepository(db *sql.DB) UserRepository {
	return &userRepository{db: db}
}

func (r *userRepository) GetUserByID(id string) (*models.User, error) {
	query := `
		SELECT id, username, email, profile_image_data, taste_score, total_posts, friends_count, joined_date, bio, created_at, updated_at
		FROM users
		WHERE id = ?
	`
	row := r.db.QueryRow(query, id)

	var user models.User
	err := row.Scan(
		&user.ID, &user.Username, &user.Email, &user.ProfileImageData,
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

func (r *userRepository) CreateOrUpdateUser(user *models.User) error {
	query := `
		INSERT INTO users (id, username, email, profile_image_data, taste_score, total_posts, friends_count, joined_date, bio, created_at, updated_at)
		VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
		ON CONFLICT(id) DO UPDATE SET
			username = excluded.username,
			email = excluded.email,
			profile_image_data = excluded.profile_image_data,
			updated_at = excluded.updated_at
	`
	_, err := r.db.Exec(query,
		user.ID, user.Username, user.Email, user.ProfileImageData,
		user.TasteScore, user.TotalPosts, user.FriendsCount,
		user.JoinedDate, user.Bio, user.CreatedAt, user.UpdatedAt,
	)
	return err
}

func (r *userRepository) UpdateUser(user *models.User) error {
	query := `
		UPDATE users
		SET username = ?, profile_image_data = ?, bio = ?, updated_at = ?
		WHERE id = ?
	`
	_, err := r.db.Exec(query,
		user.Username, user.ProfileImageData, user.Bio, user.UpdatedAt, user.ID,
	)
	return err
}

func (r *userRepository) UpdateTasteScore(userID string, scoreChange int) error {
	query := `
		UPDATE users
		SET taste_score = taste_score + ?
		WHERE id = ?
	`
	_, err := r.db.Exec(query, scoreChange, userID)
	return err
}
