package repository

import (
	"database/sql"
	"fmt"
	"time"

	"github.com/batku/beerreal/internal/models"
	"github.com/google/uuid"
)

type PostRepository interface {
	CreatePost(post *models.BeerPost) error
	GetPostByID(postID string, userID string) (*models.BeerPost, error)
	GetPosts(userID string, limit, offset int) ([]models.BeerPost, int, error)
	GetUserByID(userID string) (*models.User, error)
	CreateOrUpdateUser(user *models.User) error
	GetCommentsByPostID(postID string) ([]models.Comment, error)
	GetVoteByUserAndPost(userID, postID string) (*models.Vote, error)
}

type postRepository struct {
	db *sql.DB
}

func NewPostRepository(db *sql.DB) PostRepository {
	return &postRepository{db: db}
}

func (r *postRepository) CreatePost(post *models.BeerPost) error {
	query := `
		INSERT INTO beer_posts (id, user_id, caption, image_data, location, timestamp, upvotes, downvotes, created_at, updated_at)
		VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
	`

	post.ID = uuid.New().String()
	post.Timestamp = time.Now()
	post.CreatedAt = time.Now()
	post.UpdatedAt = time.Now()
	post.Upvotes = 0
	post.Downvotes = 0

	_, err := r.db.Exec(query, post.ID, post.UserID, post.Caption, post.ImageData,
		post.Location, post.Timestamp, post.Upvotes, post.Downvotes,
		post.CreatedAt, post.UpdatedAt)

	if err != nil {
		return fmt.Errorf("failed to create post: %w", err)
	}

	// Update user's total posts count
	updateUserQuery := `UPDATE users SET total_posts = total_posts + 1, updated_at = ? WHERE id = ?`
	_, err = r.db.Exec(updateUserQuery, time.Now(), post.UserID)
	if err != nil {
		return fmt.Errorf("failed to update user post count: %w", err)
	}

	return nil
}

func (r *postRepository) GetPostByID(postID string, userID string) (*models.BeerPost, error) {
	query := `
		SELECT 
			bp.id, bp.user_id, u.username, u.profile_image_url,
			bp.caption, bp.image_data, bp.location, bp.timestamp,
			bp.upvotes, bp.downvotes, bp.created_at, bp.updated_at
		FROM beer_posts bp
		JOIN users u ON bp.user_id = u.id
		WHERE bp.id = ?
	`

	post := &models.BeerPost{}
	err := r.db.QueryRow(query, postID).Scan(
		&post.ID, &post.UserID, &post.Username, &post.UserProfileImage,
		&post.Caption, &post.ImageData, &post.Location, &post.Timestamp,
		&post.Upvotes, &post.Downvotes, &post.CreatedAt, &post.UpdatedAt,
	)

	if err != nil {
		if err == sql.ErrNoRows {
			return nil, fmt.Errorf("post not found")
		}
		return nil, fmt.Errorf("failed to get post: %w", err)
	}

	// Get comments
	comments, err := r.GetCommentsByPostID(postID)
	if err != nil {
		return nil, err
	}
	post.Comments = comments

	// Get user's vote if they're authenticated
	if userID != "" {
		vote, _ := r.GetVoteByUserAndPost(userID, postID)
		if vote != nil {
			post.HasUserVoted = true
			post.UserVoteType = &vote.VoteType
		}
	}

	return post, nil
}

func (r *postRepository) GetPosts(userID string, limit, offset int) ([]models.BeerPost, int, error) {
	// Get total count
	var totalCount int
	countQuery := `SELECT COUNT(*) FROM beer_posts`
	err := r.db.QueryRow(countQuery).Scan(&totalCount)
	if err != nil {
		return nil, 0, fmt.Errorf("failed to count posts: %w", err)
	}

	// Get posts
	query := `
		SELECT 
			bp.id, bp.user_id, u.username, u.profile_image_url,
			bp.caption, bp.image_data, bp.location, bp.timestamp,
			bp.upvotes, bp.downvotes, bp.created_at, bp.updated_at
		FROM beer_posts bp
		JOIN users u ON bp.user_id = u.id
		ORDER BY bp.timestamp DESC
		LIMIT ? OFFSET ?
	`

	rows, err := r.db.Query(query, limit, offset)
	if err != nil {
		return nil, 0, fmt.Errorf("failed to get posts: %w", err)
	}
	defer rows.Close()

	posts := []models.BeerPost{}
	for rows.Next() {
		post := models.BeerPost{}
		err := rows.Scan(
			&post.ID, &post.UserID, &post.Username, &post.UserProfileImage,
			&post.Caption, &post.ImageData, &post.Location, &post.Timestamp,
			&post.Upvotes, &post.Downvotes, &post.CreatedAt, &post.UpdatedAt,
		)
		if err != nil {
			return nil, 0, fmt.Errorf("failed to scan post: %w", err)
		}

		// Get comments for each post
		comments, err := r.GetCommentsByPostID(post.ID)
		if err != nil {
			return nil, 0, err
		}
		post.Comments = comments

		// Get user's vote if authenticated
		if userID != "" {
			vote, _ := r.GetVoteByUserAndPost(userID, post.ID)
			if vote != nil {
				post.HasUserVoted = true
				post.UserVoteType = &vote.VoteType
			}
		}

		posts = append(posts, post)
	}

	return posts, totalCount, nil
}

func (r *postRepository) GetUserByID(userID string) (*models.User, error) {
	query := `
		SELECT id, username, email, profile_image_url, taste_score, 
		       total_posts, friends_count, joined_date, bio, created_at, updated_at
		FROM users WHERE id = ?
	`

	user := &models.User{}
	err := r.db.QueryRow(query, userID).Scan(
		&user.ID, &user.Username, &user.Email, &user.ProfileImageURL,
		&user.TasteScore, &user.TotalPosts, &user.FriendsCount,
		&user.JoinedDate, &user.Bio, &user.CreatedAt, &user.UpdatedAt,
	)

	if err != nil {
		if err == sql.ErrNoRows {
			return nil, nil
		}
		return nil, fmt.Errorf("failed to get user: %w", err)
	}

	return user, nil
}

func (r *postRepository) CreateOrUpdateUser(user *models.User) error {
	existingUser, err := r.GetUserByID(user.ID)
	if err != nil {
		return err
	}

	if existingUser == nil {
		// Create new user
		query := `
			INSERT INTO users (id, username, email, profile_image_url, taste_score, 
			                   total_posts, friends_count, joined_date, bio, created_at, updated_at)
			VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
		`
		user.JoinedDate = time.Now()
		user.CreatedAt = time.Now()
		user.UpdatedAt = time.Now()

		_, err = r.db.Exec(query, user.ID, user.Username, user.Email, user.ProfileImageURL,
			user.TasteScore, user.TotalPosts, user.FriendsCount, user.JoinedDate,
			user.Bio, user.CreatedAt, user.UpdatedAt)

		return err
	}

	// Update existing user
	query := `
		UPDATE users 
		SET username = ?, email = ?, profile_image_url = ?, updated_at = ?
		WHERE id = ?
	`
	user.UpdatedAt = time.Now()
	_, err = r.db.Exec(query, user.Username, user.Email, user.ProfileImageURL, user.UpdatedAt, user.ID)

	return err
}

func (r *postRepository) GetCommentsByPostID(postID string) ([]models.Comment, error) {
	query := `
		SELECT c.id, c.post_id, c.user_id, u.username, u.profile_image_url,
		       c.text, c.timestamp, c.created_at, c.updated_at
		FROM comments c
		JOIN users u ON c.user_id = u.id
		WHERE c.post_id = ?
		ORDER BY c.timestamp ASC
	`

	rows, err := r.db.Query(query, postID)
	if err != nil {
		return nil, fmt.Errorf("failed to get comments: %w", err)
	}
	defer rows.Close()

	comments := []models.Comment{}
	for rows.Next() {
		comment := models.Comment{}
		err := rows.Scan(
			&comment.ID, &comment.PostID, &comment.UserID, &comment.Username,
			&comment.UserProfileImage, &comment.Text, &comment.Timestamp,
			&comment.CreatedAt, &comment.UpdatedAt,
		)
		if err != nil {
			return nil, fmt.Errorf("failed to scan comment: %w", err)
		}
		comments = append(comments, comment)
	}

	return comments, nil
}

func (r *postRepository) GetVoteByUserAndPost(userID, postID string) (*models.Vote, error) {
	query := `SELECT id, post_id, user_id, vote_type, created_at, updated_at FROM votes WHERE user_id = ? AND post_id = ?`

	vote := &models.Vote{}
	err := r.db.QueryRow(query, userID, postID).Scan(
		&vote.ID, &vote.PostID, &vote.UserID, &vote.VoteType,
		&vote.CreatedAt, &vote.UpdatedAt,
	)

	if err != nil {
		if err == sql.ErrNoRows {
			return nil, nil
		}
		return nil, fmt.Errorf("failed to get vote: %w", err)
	}

	return vote, nil
}
