package models

import "time"

type User struct {
	ID              string    `json:"id" db:"id"`
	Username        string    `json:"username" db:"username"`
	Email           string    `json:"email" db:"email"`
	ProfileImageURL *string   `json:"profileImageUrl" db:"profile_image_url"`
	TasteScore      int       `json:"tasteScore" db:"taste_score"`
	TotalPosts      int       `json:"totalPosts" db:"total_posts"`
	FriendsCount    int       `json:"friendsCount" db:"friends_count"`
	JoinedDate      time.Time `json:"joinedDate" db:"joined_date"`
	Bio             *string   `json:"bio" db:"bio"`
	CreatedAt       time.Time `json:"createdAt" db:"created_at"`
	UpdatedAt       time.Time `json:"updatedAt" db:"updated_at"`
}

type BeerPost struct {
	ID               string    `json:"id" db:"id"`
	UserID           string    `json:"userId" db:"user_id"`
	Username         string    `json:"username" db:"username"`
	UserProfileImage *string   `json:"userProfileImage" db:"user_profile_image"`
	Caption          string    `json:"caption" db:"caption"`
	ImageURL         string    `json:"imageUrl" db:"image_url"`
	Location         *string   `json:"location" db:"location"`
	Timestamp        time.Time `json:"timestamp" db:"timestamp"`
	Upvotes          int       `json:"upvotes" db:"upvotes"`
	Downvotes        int       `json:"downvotes" db:"downvotes"`
	CreatedAt        time.Time `json:"createdAt" db:"created_at"`
	UpdatedAt        time.Time `json:"updatedAt" db:"updated_at"`
	Comments         []Comment `json:"comments"`
	HasUserVoted     bool      `json:"hasUserVoted"`
	UserVoteType     *VoteType `json:"userVoteType"`
}

type Comment struct {
	ID               string    `json:"id" db:"id"`
	PostID           string    `json:"postId" db:"post_id"`
	UserID           string    `json:"userId" db:"user_id"`
	Username         string    `json:"username" db:"username"`
	UserProfileImage *string   `json:"userProfileImage" db:"user_profile_image"`
	Text             string    `json:"text" db:"text"`
	Timestamp        time.Time `json:"timestamp" db:"timestamp"`
	CreatedAt        time.Time `json:"createdAt" db:"created_at"`
	UpdatedAt        time.Time `json:"updatedAt" db:"updated_at"`
}

type Vote struct {
	ID        string    `json:"id" db:"id"`
	PostID    string    `json:"postId" db:"post_id"`
	UserID    string    `json:"userId" db:"user_id"`
	VoteType  VoteType  `json:"voteType" db:"vote_type"`
	CreatedAt time.Time `json:"createdAt" db:"created_at"`
	UpdatedAt time.Time `json:"updatedAt" db:"updated_at"`
}

type VoteType string

const (
	VoteTypeUpvote   VoteType = "UPVOTE"
	VoteTypeDownvote VoteType = "DOWNVOTE"
)

// Request/Response DTOs
type CreatePostRequest struct {
	Caption  string  `json:"caption" binding:"required"`
	ImageURL string  `json:"imageUrl" binding:"required"`
	Location *string `json:"location"`
}

type GetPostsResponse struct {
	Posts      []BeerPost `json:"posts"`
	TotalCount int        `json:"totalCount"`
	Page       int        `json:"page"`
	PageSize   int        `json:"pageSize"`
}
