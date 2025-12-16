package service

import (
	"fmt"
	"log"

	"github.com/batku/beerreal/internal/models"
	"github.com/batku/beerreal/internal/repository"
)

type PostService interface {
	CreatePost(userID string, req *models.CreatePostRequest) (*models.BeerPost, error)
	GetPostByID(postID string, userID string) (*models.BeerPost, error)
	GetPosts(userID string, page, pageSize int) (*models.GetPostsResponse, error)
	EnsureUserExists(userID, email, username string) error
}

type postService struct {
	repo repository.PostRepository
}

func NewPostService(repo repository.PostRepository) PostService {
	return &postService{repo: repo}
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
		UserID:           userID,
		Username:         user.Username,
		UserProfileImage: user.ProfileImageURL,
		Caption:          req.Caption,
		ImageData:        req.ImageData,
		Location:         req.Location,
		Comments:         []models.Comment{},
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
