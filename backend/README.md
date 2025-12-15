# BeerReal API Documentation

REST API for BeerReal - a BeReal-style mobile app for beer enthusiasts.

**Base URL:** `http://localhost:8080`

---

## 🔐 Authentication

Protected endpoints require a Firebase ID token in the Authorization header:

```http
Authorization: Bearer <firebase-id-token>
```

Get the token from your Firebase client SDK after user authentication.

---

## 📡 Endpoints

### Health Check

Check if the API is running.

```http
GET /health
```

**Response:**
```json
{
  "status": "ok",
  "message": "BeerReal API is running"
}
```

---

### Get All Posts

Retrieve a paginated list of beer posts.

```http
GET /api/posts?page=1&pageSize=20
```

**Query Parameters:**
| Parameter | Type | Default | Description |
|-----------|------|---------|-------------|
| `page` | integer | 1 | Page number |
| `pageSize` | integer | 20 | Number of posts per page (max: 100) |

**Response:** `200 OK`
```json
{
  "posts": [
    {
      "id": "uuid-string",
      "userId": "firebase-user-id",
      "username": "johndoe",
      "userProfileImage": "https://example.com/profile.jpg",
      "caption": "Best IPA ever!",
      "imageUrl": "https://example.com/beer.jpg",
      "location": "Berlin, Germany",
      "timestamp": "2025-12-15T10:30:00Z",
      "upvotes": 42,
      "downvotes": 3,
      "createdAt": "2025-12-15T10:30:00Z",
      "updatedAt": "2025-12-15T10:30:00Z",
      "comments": [
        {
          "id": "comment-uuid",
          "postId": "post-uuid",
          "userId": "user-id",
          "username": "janedoe",
          "userProfileImage": "https://example.com/jane.jpg",
          "text": "Looks amazing!",
          "timestamp": "2025-12-15T11:00:00Z",
          "createdAt": "2025-12-15T11:00:00Z",
          "updatedAt": "2025-12-15T11:00:00Z"
        }
      ],
      "hasUserVoted": false,
      "userVoteType": null
    }
  ],
  "totalCount": 150,
  "page": 1,
  "pageSize": 20
}
```

**Notes:**
- `hasUserVoted` and `userVoteType` are only populated if request includes auth token
- `userVoteType` can be: `"UPVOTE"`, `"DOWNVOTE"`, or `null`

---

### Get Single Post

Retrieve a specific beer post by ID.

```http
GET /api/posts/:id
```

**Path Parameters:**
| Parameter | Type | Description |
|-----------|------|-------------|
| `id` | string | Post UUID |

**Response:** `200 OK`
```json
{
  "id": "uuid-string",
  "userId": "firebase-user-id",
  "username": "johndoe",
  "userProfileImage": "https://example.com/profile.jpg",
  "caption": "Best IPA ever!",
  "imageUrl": "https://example.com/beer.jpg",
  "location": "Berlin, Germany",
  "timestamp": "2025-12-15T10:30:00Z",
  "upvotes": 42,
  "downvotes": 3,
  "comments": [...],
  "hasUserVoted": true,
  "userVoteType": "UPVOTE"
}
```

**Errors:**
- `404 Not Found` - Post doesn't exist

---

### Create Post

Create a new beer post. **Requires authentication.**

```http
POST /api/posts
Authorization: Bearer <firebase-token>
Content-Type: application/json
```

**Request Body:**
```json
{
  "caption": "Best IPA ever!",
  "imageUrl": "https://example.com/beer.jpg",
  "location": "Berlin, Germany"
}
```

**Fields:**
| Field | Type | Required | Description |
|-------|------|----------|-------------|
| `caption` | string | ✅ | Post caption/description |
| `imageUrl` | string | ✅ | URL to the beer image |
| `location` | string | ❌ | Optional location string |

**Response:** `201 Created`
```json
{
  "id": "new-uuid",
  "userId": "firebase-user-id",
  "username": "johndoe",
  "userProfileImage": "https://example.com/profile.jpg",
  "caption": "Best IPA ever!",
  "imageUrl": "https://example.com/beer.jpg",
  "location": "Berlin, Germany",
  "timestamp": "2025-12-15T10:30:00Z",
  "upvotes": 0,
  "downvotes": 0,
  "comments": [],
  "hasUserVoted": false,
  "userVoteType": null
}
```

**Errors:**
- `400 Bad Request` - Invalid request body
- `401 Unauthorized` - Missing or invalid Firebase token
- `500 Internal Server Error` - Server error

---

## 📊 Data Models

### BeerPost
```json
{
  "id": "string (UUID)",
  "userId": "string",
  "username": "string",
  "userProfileImage": "string | null",
  "caption": "string",
  "imageUrl": "string",
  "location": "string | null",
  "timestamp": "string (ISO 8601)",
  "upvotes": "integer",
  "downvotes": "integer",
  "comments": "Comment[]",
  "hasUserVoted": "boolean",
  "userVoteType": "UPVOTE | DOWNVOTE | null"
}
```

### Comment
```json
{
  "id": "string (UUID)",
  "postId": "string",
  "userId": "string",
  "username": "string",
  "userProfileImage": "string | null",
  "text": "string",
  "timestamp": "string (ISO 8601)"
}
```

### User
```json
{
  "id": "string (Firebase UID)",
  "username": "string",
  "email": "string",
  "profileImageUrl": "string | null",
  "tasteScore": "integer",
  "totalPosts": "integer",
  "friendsCount": "integer",
  "joinedDate": "string (ISO 8601)",
  "bio": "string | null"
}
```

---

## ⚠️ Error Responses

All errors follow this format:

```json
{
  "error": "Error message describing what went wrong"
}
```

**Common HTTP Status Codes:**
- `400` - Bad Request (invalid input)
- `401` - Unauthorized (missing/invalid auth token)
- `404` - Not Found (resource doesn't exist)
- `500` - Internal Server Error

---

## 🚀 Getting Started

1. **Get Firebase credentials** from Firebase Console (Service Account key)
2. **Save as** `firebase-credentials.json` in backend root
3. **Run:** `go run cmd/server/main.go`
4. **API available at:** `http://localhost:8080`

---

## 🔧 Configuration

Set via environment variables or `.env` file:

| Variable | Default | Description |
|----------|---------|-------------|
| `PORT` | 8080 | Server port |
| `DATABASE_PATH` | ./beerreal.db | SQLite database path |
| `FIREBASE_CREDENTIALS_PATH` | ./firebase-credentials.json | Firebase Admin SDK credentials |
| `GIN_MODE` | debug | Gin mode: `debug` or `release` |
