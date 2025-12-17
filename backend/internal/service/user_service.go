package service

import (
	"fmt"
	"strings"
	"time"

	"github.com/batku/beerreal/internal/models"
	"github.com/batku/beerreal/internal/repository"
)

type UserService struct {
	repo repository.UserRepository
}

func NewUserService(repo repository.UserRepository) *UserService {
	return &UserService{repo: repo}
}

func (s *UserService) GetOrCreateUser(id, email string) (*models.User, error) {
	user, err := s.repo.GetUserByID(id)
	if err != nil {
		return nil, err
	}

	if user != nil {
		return user, nil
	}

	// User doesn't exist, create new one
	var username string
	if email == "" {
		// Generate placeholder data for users without email (e.g. anonymous auth)
		shortID := id
		if len(id) > 8 {
			shortID = id[:8]
		}
		username = fmt.Sprintf("user_%s", shortID)
		email = fmt.Sprintf("%s@anonymous.beerreal", username)
	} else {
		username = strings.Split(email, "@")[0]
	}

	now := time.Now()
	newUser := &models.User{
		ID:           id,
		Username:     username,
		Email:        email,
		TasteScore:   0,
		TotalPosts:   0,
		FriendsCount: 0,
		JoinedDate:   now,
		CreatedAt:    now,
		UpdatedAt:    now,
	}

	err = s.repo.CreateOrUpdateUser(newUser)
	if err != nil {
		return nil, err
	}

	return newUser, nil
}

func (s *UserService) UpdateUser(userID string, req *models.UpdateUserRequest) (*models.User, error) {
	user, err := s.repo.GetUserByID(userID)
	if err != nil {
		return nil, err
	}
	if user == nil {
		return nil, fmt.Errorf("user not found")
	}

	if req.Username != "" {
		user.Username = req.Username
	}
	if req.ProfileImageData != "" {
		val := req.ProfileImageData
		user.ProfileImageData = &val
	}

	user.UpdatedAt = time.Now()

	if err := s.repo.UpdateUser(user); err != nil {
		return nil, err
	}

	return user, nil
}
