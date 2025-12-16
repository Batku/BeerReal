package service

import (
	"strings"
	"time"

	"github.com/batku/beerreal/internal/models"
	"github.com/batku/beerreal/internal/repository"
)

type UserService struct {
	repo *repository.UserRepository
}

func NewUserService(repo *repository.UserRepository) *UserService {
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
	username := strings.Split(email, "@")[0]
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

	err = s.repo.CreateUser(newUser)
	if err != nil {
		return nil, err
	}

	return newUser, nil
}
