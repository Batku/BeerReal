#!/bin/bash

# BeerReal Backend Setup Script

echo "🍺 Setting up BeerReal Backend..."

# Install Go dependencies
echo "📦 Installing dependencies..."
go mod tidy

# Create .env file if it doesn't exist
if [ ! -f .env ]; then
    echo "📝 Creating .env file..."
    cp .env.example .env
    echo "⚠️  Please update .env with your Firebase credentials path"
fi

echo ""
echo "✅ Setup complete!"
echo ""
echo "Next steps:"
echo "1. Add your firebase-credentials.json file to the project root"
echo "2. Update .env file if needed"
echo "3. Run the server with: go run cmd/server/main.go"
echo ""
