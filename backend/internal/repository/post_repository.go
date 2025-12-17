package repository

import (
	"database/sql"
	"fmt"
	"log"
	"time"

	"github.com/batku/beerreal/internal/models"
	"github.com/google/uuid"
)

type PostRepository interface {
	CreatePost(post *models.BeerPost) error
	GetPostByID(postID string, userID string) (*models.BeerPost, error)
	GetPosts(userID string, limit, offset int) ([]models.BeerPost, int, error)
	GetUserPosts(targetUserID string, currentUserID string, limit, offset int) ([]models.BeerPost, int, error)
	GetUserByID(userID string) (*models.User, error)
	CreateOrUpdateUser(user *models.User) error
	GetCommentsByPostID(postID string) ([]models.Comment, error)
	GetVoteByUserAndPost(userID, postID string) (*models.Vote, error)
	AddVote(vote *models.Vote) error
	UpdateVote(vote *models.Vote) error
	DeleteVote(voteID string) error
	UpdatePostVotes(postID string, upvotes, downvotes int) error
	AddComment(comment *models.Comment) error
}

type postRepository struct {
	db *sql.DB
}

func NewPostRepository(db *sql.DB) PostRepository {
	return &postRepository{db: db}
}

func (r *postRepository) CreatePost(post *models.BeerPost) error {
	log.Printf("[Repository] CreatePost called for userID: %s", post.UserID)
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

	log.Printf("[Repository] Inserting post with ID: %s, imageDataLength: %d", post.ID, len(post.ImageData))
	_, err := r.db.Exec(query, post.ID, post.UserID, post.Caption, post.ImageData,
		post.Location, post.Timestamp, post.Upvotes, post.Downvotes,
		post.CreatedAt, post.UpdatedAt)

	if err != nil {
		log.Printf("[Repository] ERROR: Failed to insert post: %v", err)
		return fmt.Errorf("failed to create post: %w", err)
	}

	// Update user's total posts count
	log.Printf("[Repository] Updating user post count for userID: %s", post.UserID)
	updateUserQuery := `UPDATE users SET total_posts = total_posts + 1, updated_at = ? WHERE id = ?`
	_, err = r.db.Exec(updateUserQuery, time.Now(), post.UserID)
	if err != nil {
		log.Printf("[Repository] ERROR: Failed to update user post count: %v", err)
		return fmt.Errorf("failed to update user post count: %w", err)
	}

	log.Printf("[Repository] Post created successfully: %s", post.ID)
	return nil
}

func (r *postRepository) GetPostByID(postID string, userID string) (*models.BeerPost, error) {
	log.Printf("[Repository] GetPostByID called - postID: %s, userID: %s", postID, userID)
	query := `
		SELECT 
			bp.id, bp.user_id, u.username, u.profile_image_data,
			bp.caption, bp.image_data, bp.location, bp.timestamp,
			bp.upvotes, bp.downvotes, bp.created_at, bp.updated_at
		FROM beer_posts bp
		JOIN users u ON bp.user_id = u.id
		WHERE bp.id = ?
	`

	post := &models.BeerPost{}
	err := r.db.QueryRow(query, postID).Scan(
		&post.ID, &post.UserID, &post.Username, &post.UserProfileImageData,
		&post.Caption, &post.ImageData, &post.Location, &post.Timestamp,
		&post.Upvotes, &post.Downvotes, &post.CreatedAt, &post.UpdatedAt,
	)

	if err != nil {
		if err == sql.ErrNoRows {
			log.Printf("[Repository] Post not found: %s", postID)
			return nil, fmt.Errorf("post not found")
		}
		log.Printf("[Repository] ERROR: Database query failed: %v", err)
		return nil, fmt.Errorf("failed to get post: %w", err)
	}

	log.Printf("[Repository] Post found, fetching comments for post: %s", postID)
	// Get comments
	comments, err := r.GetCommentsByPostID(postID)
	if err != nil {
		log.Printf("[Repository] ERROR: Failed to get comments: %v", err)
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

	log.Printf("[Repository] GetPostByID successful for post: %s", postID)
	return post, nil
}

func (r *postRepository) GetPosts(userID string, limit, offset int) ([]models.BeerPost, int, error) {
	log.Printf("[Repository] GetPosts called - userID: %s, limit: %d, offset: %d", userID, limit, offset)
	// Get total count
	var totalCount int
	countQuery := `SELECT COUNT(*) FROM beer_posts`
	err := r.db.QueryRow(countQuery).Scan(&totalCount)
	if err != nil {
		log.Printf("[Repository] ERROR: Failed to count posts: %v", err)
		return nil, 0, fmt.Errorf("failed to count posts: %w", err)
	}
	log.Printf("[Repository] Total posts in DB: %d", totalCount)

	// Get posts
	query := `
		SELECT 
			bp.id, bp.user_id, u.username, u.profile_image_data,
			bp.caption, bp.image_data, bp.location, bp.timestamp,
			bp.upvotes, bp.downvotes, bp.created_at, bp.updated_at
		FROM beer_posts bp
		JOIN users u ON bp.user_id = u.id
		ORDER BY bp.timestamp DESC
		LIMIT ? OFFSET ?
	`

	log.Printf("[Repository] Executing query with limit: %d, offset: %d", limit, offset)
	rows, err := r.db.Query(query, limit, offset)
	if err != nil {
		log.Printf("[Repository] ERROR: Query execution failed: %v", err)
		return nil, 0, fmt.Errorf("failed to get posts: %w", err)
	}
	defer rows.Close()

	posts := []models.BeerPost{}
	postCount := 0
	for rows.Next() {
		postCount++
		post := models.BeerPost{}
		err := rows.Scan(
			&post.ID, &post.UserID, &post.Username, &post.UserProfileImageData,
			&post.Caption, &post.ImageData, &post.Location, &post.Timestamp,
			&post.Upvotes, &post.Downvotes, &post.CreatedAt, &post.UpdatedAt,
		)
		if err != nil {
			log.Printf("[Repository] ERROR: Failed to scan post row %d: %v", postCount, err)
			return nil, 0, fmt.Errorf("failed to scan post: %w", err)
		}
		log.Printf("[Repository] Scanned post %d - ID: %s", postCount, post.ID)

		// Get comments for each post
		comments, err := r.GetCommentsByPostID(post.ID)
		if err != nil {
			log.Printf("[Repository] ERROR: Failed to get comments for post %s: %v", post.ID, err)
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

	log.Printf("[Repository] GetPosts successful - returning %d posts (total: %d)", len(posts), totalCount)
	return posts, totalCount, nil
}

func (r *postRepository) GetUserPosts(targetUserID string, currentUserID string, limit, offset int) ([]models.BeerPost, int, error) {
	log.Printf("[Repository] GetUserPosts called - targetUserID: %s, currentUserID: %s, limit: %d, offset: %d", targetUserID, currentUserID, limit, offset)
	
	// Get total count for this user
	var totalCount int
	countQuery := `SELECT COUNT(*) FROM beer_posts WHERE user_id = ?`
	err := r.db.QueryRow(countQuery, targetUserID).Scan(&totalCount)
	if err != nil {
		log.Printf("[Repository] ERROR: Failed to count user posts: %v", err)
		return nil, 0, fmt.Errorf("failed to count user posts: %w", err)
	}
	log.Printf("[Repository] Total posts for user %s: %d", targetUserID, totalCount)

	// Get posts
	query := `
		SELECT 
			bp.id, bp.user_id, u.username, u.profile_image_data,
			bp.caption, bp.image_data, bp.location, bp.timestamp,
			bp.upvotes, bp.downvotes, bp.created_at, bp.updated_at
		FROM beer_posts bp
		JOIN users u ON bp.user_id = u.id
		WHERE bp.user_id = ?
		ORDER BY bp.timestamp DESC
		LIMIT ? OFFSET ?
	`

	log.Printf("[Repository] Executing query with limit: %d, offset: %d", limit, offset)
	rows, err := r.db.Query(query, targetUserID, limit, offset)
	if err != nil {
		log.Printf("[Repository] ERROR: Query execution failed: %v", err)
		return nil, 0, fmt.Errorf("failed to get user posts: %w", err)
	}
	defer rows.Close()

	posts := []models.BeerPost{}
	postCount := 0
	for rows.Next() {
		postCount++
		post := models.BeerPost{}
		err := rows.Scan(
			&post.ID, &post.UserID, &post.Username, &post.UserProfileImageData,
			&post.Caption, &post.ImageData, &post.Location, &post.Timestamp,
			&post.Upvotes, &post.Downvotes, &post.CreatedAt, &post.UpdatedAt,
		)
		if err != nil {
			log.Printf("[Repository] ERROR: Failed to scan post row %d: %v", postCount, err)
			return nil, 0, fmt.Errorf("failed to scan post: %w", err)
		}

		// Get comments for each post
		comments, err := r.GetCommentsByPostID(post.ID)
		if err != nil {
			log.Printf("[Repository] ERROR: Failed to get comments for post %s: %v", post.ID, err)
			return nil, 0, err
		}
		post.Comments = comments

		// Get user's vote if authenticated
		if currentUserID != "" {
			vote, _ := r.GetVoteByUserAndPost(currentUserID, post.ID)
			if vote != nil {
				post.HasUserVoted = true
				post.UserVoteType = &vote.VoteType
			}
		}

		posts = append(posts, post)
	}

	log.Printf("[Repository] GetUserPosts successful - returning %d posts", len(posts))
	return posts, totalCount, nil
}

func (r *postRepository) GetUserByID(userID string) (*models.User, error) {
	query := `
		SELECT id, username, email, profile_image_data, taste_score, 
		       total_posts, friends_count, joined_date, bio, created_at, updated_at
		FROM users WHERE id = ?
	`

	user := &models.User{}
	err := r.db.QueryRow(query, userID).Scan(
		&user.ID, &user.Username, &user.Email, &user.ProfileImageData,
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
			INSERT INTO users (id, username, email, profile_image_data, taste_score, 
			                   total_posts, friends_count, joined_date, bio, created_at, updated_at)
			VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
		`
		user.JoinedDate = time.Now()
		user.CreatedAt = time.Now()
		user.UpdatedAt = time.Now()

		_, err = r.db.Exec(query, user.ID, user.Username, user.Email, user.ProfileImageData,
			user.TasteScore, user.TotalPosts, user.FriendsCount, user.JoinedDate,
			user.Bio, user.CreatedAt, user.UpdatedAt)

		return err
	}

	// Update existing user
	query := `
		UPDATE users 
		SET username = ?, email = ?, profile_image_data = ?, updated_at = ?
		WHERE id = ?
	`
	user.UpdatedAt = time.Now()
	_, err = r.db.Exec(query, user.Username, user.Email, user.ProfileImageData, user.UpdatedAt, user.ID)

	return err
}

func (r *postRepository) GetCommentsByPostID(postID string) ([]models.Comment, error) {
	query := `
		SELECT c.id, c.post_id, c.user_id, u.username, u.profile_image_data,
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
			&comment.UserProfileImageData, &comment.Text, &comment.Timestamp,
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

func (r *postRepository) AddVote(vote *models.Vote) error {
	query := `
		INSERT INTO votes (id, post_id, user_id, vote_type, created_at, updated_at)
		VALUES (?, ?, ?, ?, ?, ?)
	`
	_, err := r.db.Exec(query, vote.ID, vote.PostID, vote.UserID, vote.VoteType, vote.CreatedAt, vote.UpdatedAt)
	if err != nil {
		return fmt.Errorf("failed to add vote: %w", err)
	}
	return nil
}

func (r *postRepository) UpdateVote(vote *models.Vote) error {
	query := `
		UPDATE votes SET vote_type = ?, updated_at = ?
		WHERE id = ?
	`
	_, err := r.db.Exec(query, vote.VoteType, vote.UpdatedAt, vote.ID)
	if err != nil {
		return fmt.Errorf("failed to update vote: %w", err)
	}
	return nil
}

func (r *postRepository) DeleteVote(voteID string) error {
	query := `DELETE FROM votes WHERE id = ?`
	_, err := r.db.Exec(query, voteID)
	if err != nil {
		return fmt.Errorf("failed to delete vote: %w", err)
	}
	return nil
}

func (r *postRepository) UpdatePostVotes(postID string, upvotes, downvotes int) error {
	query := `UPDATE beer_posts SET upvotes = ?, downvotes = ? WHERE id = ?`
	_, err := r.db.Exec(query, upvotes, downvotes, postID)
	if err != nil {
		return fmt.Errorf("failed to update post votes: %w", err)
	}
	return nil
}

func (r *postRepository) AddComment(comment *models.Comment) error {
	query := `
		INSERT INTO comments (id, post_id, user_id, text, timestamp, created_at, updated_at)
		VALUES (?, ?, ?, ?, ?, ?, ?)
	`
	_, err := r.db.Exec(query, comment.ID, comment.PostID, comment.UserID, comment.Text, comment.Timestamp, comment.CreatedAt, comment.UpdatedAt)
	if err != nil {
		return fmt.Errorf("failed to add comment: %w", err)
	}
	return nil
}
