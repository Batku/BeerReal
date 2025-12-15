package service

import (
	"fmt"

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
	// Get user to populate post with user info
	user, err := s.repo.GetUserByID(userID)
	if err != nil {
		return nil, fmt.Errorf("failed to get user: %w", err)
	}
	if user == nil {
		return nil, fmt.Errorf("user not found")
	}

	post := &models.BeerPost{
		UserID:           userID,
		Username:         user.Username,
		UserProfileImage: user.ProfileImageURL,
		Caption:          req.Caption,
		ImageData:         req.ImageData,
		Location:         req.Location,
		Comments:         []models.Comment{},
	}

	err = s.repo.CreatePost(post)
	if err != nil {
		return nil, fmt.Errorf("failed to create post: %w", err)
	}

	return post, nil
}

func (s *postService) GetPostByID(postID string, userID string) (*models.BeerPost, error) {
	post, err := s.repo.GetPostByID(postID, userID)
	if err != nil {
		return nil, fmt.Errorf("failed to get post: %w", err)
	}
	return post, nil
}

func (s *postService) GetPosts(userID string, page, pageSize int) (*models.GetPostsResponse, error) {
	if page < 1 {
		page = 1
	}
	if pageSize < 1 || pageSize > 100 {
		pageSize = 20
	}

	offset := (page - 1) * pageSize

	posts, totalCount, err := s.repo.GetPosts(userID, pageSize, offset)
	if err != nil {
		return nil, fmt.Errorf("failed to get posts: %w", err)
	}

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
