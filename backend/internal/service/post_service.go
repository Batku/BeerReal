package service

import (
	"fmt"
	"log"
	"time"

	"github.com/batku/beerreal/internal/models"
	"github.com/batku/beerreal/internal/repository"
	"github.com/google/uuid"
)

type PostService interface {
	CreatePost(userID string, req *models.CreatePostRequest) (*models.BeerPost, error)
	GetPostByID(postID string, userID string) (*models.BeerPost, error)
	GetPosts(userID string, page, pageSize int) (*models.GetPostsResponse, error)
	EnsureUserExists(userID, email, username string) error
	VotePost(userID string, req *models.VoteRequest) (*models.VoteResponse, error)
	AddComment(userID string, req *models.AddCommentRequest) (*models.Comment, error)
}

type postService struct {
	repo     repository.PostRepository
	userRepo repository.UserRepository
}

func NewPostService(repo repository.PostRepository, userRepo repository.UserRepository) PostService {
	return &postService{
		repo:     repo,
		userRepo: userRepo,
	}
}

func (s *postService) CreatePost(userID string, req *models.CreatePostRequest) (*models.BeerPost, error) {
	log.Printf("[PostService] CreatePost called for userID: %s", userID)
	// Get user to populate post with user info
	user, err := s.repo.GetUserByID(userID)
	if err != nil {
		log.Printf("[PostService] ERROR: Failed to get user: %v", err)
		return nil, fmt.Errorf("failed to get user: %w", err)
	}
	if user == nil {
		log.Printf("[PostService] ERROR: User not found for ID: %s", userID)
		return nil, fmt.Errorf("user not found")
	}
	log.Printf("[PostService] User found: %s", user.Username)

	post := &models.BeerPost{
		UserID:               userID,
		Username:             user.Username,
		UserProfileImageData: user.ProfileImageData,
		Caption:              req.Caption,
		ImageData:            req.ImageData,
		Location:             req.Location,
		Comments:             []models.Comment{},
	}

	log.Println("[PostService] Calling repository to create post")
	err = s.repo.CreatePost(post)
	if err != nil {
		log.Printf("[PostService] ERROR: Repository failed to create post: %v", err)
		return nil, fmt.Errorf("failed to create post: %w", err)
	}

	log.Printf("[PostService] Post created successfully with ID: %s", post.ID)
	return post, nil
}

func (s *postService) GetPostByID(postID string, userID string) (*models.BeerPost, error) {
	log.Printf("[PostService] GetPostByID called - postID: %s, userID: %s", postID, userID)
	post, err := s.repo.GetPostByID(postID, userID)
	if err != nil {
		log.Printf("[PostService] ERROR: Failed to get post: %v", err)
		return nil, fmt.Errorf("failed to get post: %w", err)
	}
	log.Printf("[PostService] Post retrieved successfully: %s", post.ID)
	return post, nil
}

func (s *postService) GetPosts(userID string, page, pageSize int) (*models.GetPostsResponse, error) {
	log.Printf("[PostService] GetPosts called - userID: %s, page: %d, pageSize: %d", userID, page, pageSize)
	if page < 1 {
		page = 1
	}
	if pageSize < 1 || pageSize > 100 {
		pageSize = 20
	}

	offset := (page - 1) * pageSize
	log.Printf("[PostService] Fetching posts with limit: %d, offset: %d", pageSize, offset)

	posts, totalCount, err := s.repo.GetPosts(userID, pageSize, offset)
	if err != nil {
		log.Printf("[PostService] ERROR: Failed to get posts from repository: %v", err)
		return nil, fmt.Errorf("failed to get posts: %w", err)
	}

	log.Printf("[PostService] Successfully retrieved %d posts (total: %d)", len(posts), totalCount)
	return &models.GetPostsResponse{
		Posts:      posts,
		TotalCount: totalCount,
		Page:       page,
		PageSize:   pageSize,
	}, nil
}

func (s *postService) EnsureUserExists(userID, email, username string) error {
	user, err := s.repo.GetUserByID(userID)
	if err != nil {
		return err
	}

	if user == nil {
		// Create new user
		newUser := &models.User{
			ID:       userID,
			Username: username,
			Email:    email,
		}
		return s.repo.CreateOrUpdateUser(newUser)
	}

	return nil
}

func (s *postService) VotePost(userID string, req *models.VoteRequest) (*models.VoteResponse, error) {
	// Check if post exists
	post, err := s.repo.GetPostByID(req.PostID)
	if err != nil {
		return nil, fmt.Errorf("failed to get post: %w", err)
	}
	if post == nil {
		return nil, fmt.Errorf("post not found")
	}

	// Check if user already voted
	existingVote, err := s.repo.GetVoteByUserAndPost(userID, req.PostID)
	if err != nil {
		return nil, fmt.Errorf("failed to check existing vote: %w", err)
	}

	var scoreChange int
	var upvoteChange, downvoteChange int

	if existingVote != nil {
		if existingVote.VoteType == req.VoteType {
			// Remove vote (toggle off)
			if err := s.repo.DeleteVote(existingVote.ID); err != nil {
				return nil, fmt.Errorf("failed to delete vote: %w", err)
			}
			if req.VoteType == "upvote" {
				scoreChange = -1
				upvoteChange = -1
			} else {
				scoreChange = 1
				downvoteChange = -1
			}
		} else {
			// Change vote type
			existingVote.VoteType = req.VoteType
			existingVote.UpdatedAt = time.Now()
			if err := s.repo.UpdateVote(existingVote); err != nil {
				return nil, fmt.Errorf("failed to update vote: %w", err)
			}
			if req.VoteType == "upvote" {
				scoreChange = 2 // -(-1) + 1 = 2
				upvoteChange = 1
				downvoteChange = -1
			} else {
				scoreChange = -2 // -(1) + (-1) = -2
				upvoteChange = -1
				downvoteChange = 1
			}
		}
	} else {
		// New vote
		newVote := &models.Vote{
			ID:        uuid.New().String(),
			PostID:    req.PostID,
			UserID:    userID,
			VoteType:  req.VoteType,
			CreatedAt: time.Now(),
			UpdatedAt: time.Now(),
		}
		if err := s.repo.AddVote(newVote); err != nil {
			return nil, fmt.Errorf("failed to add vote: %w", err)
		}
		if req.VoteType == "upvote" {
			scoreChange = 1
			upvoteChange = 1
		} else {
			scoreChange = -1
			downvoteChange = 1
		}
	}

	// Update post vote counts
	newUpvotes := post.Upvotes + upvoteChange
	newDownvotes := post.Downvotes + downvoteChange
	if err := s.repo.UpdatePostVotes(post.ID, newUpvotes, newDownvotes); err != nil {
		return nil, fmt.Errorf("failed to update post votes: %w", err)
	}

	// Update user taste score (author of the post)
	if scoreChange != 0 {
		if err := s.userRepo.UpdateTasteScore(post.UserID, scoreChange); err != nil {
			log.Printf("[PostService] ERROR: Failed to update taste score for user %s: %v", post.UserID, err)
			// Don't fail the request if taste score update fails
		}
	}

	return &models.VoteResponse{
		Upvotes:   newUpvotes,
		Downvotes: newDownvotes,
	}, nil
}

func (s *postService) AddComment(userID string, req *models.AddCommentRequest) (*models.Comment, error) {
	// Check if post exists
	post, err := s.repo.GetPostByID(req.PostID)
	if err != nil {
		return nil, fmt.Errorf("failed to get post: %w", err)
	}
	if post == nil {
		return nil, fmt.Errorf("post not found")
	}

	user, err := s.userRepo.GetUserByID(userID)
	if err != nil {
		return nil, fmt.Errorf("failed to get user: %w", err)
	}

	comment := &models.Comment{
		ID:                   uuid.New().String(),
		PostID:               req.PostID,
		UserID:               userID,
		Username:             user.Username,
		UserProfileImageData: user.ProfileImageData,
		Text:                 req.Text,
		Timestamp:            time.Now(),
		CreatedAt:            time.Now(),
		UpdatedAt:            time.Now(),
	}

	if err := s.repo.AddComment(comment); err != nil {
		return nil, fmt.Errorf("failed to add comment: %w", err)
	}

	return comment, nil
}
