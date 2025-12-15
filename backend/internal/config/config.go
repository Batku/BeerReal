package config

import (
	"log"
	"os"
)

type Config struct {
	Port                    string
	DatabasePath            string
	FirebaseCredentialsPath string
	GinMode                 string
}

func LoadConfig() *Config {
	return &Config{
		Port:                    getEnv("PORT", "8080"),
		DatabasePath:            getEnv("DATABASE_PATH", "./beerreal.db"),
		FirebaseCredentialsPath: getEnv("FIREBASE_CREDENTIALS_PATH", "./firebase-credentials.json"),
		GinMode:                 getEnv("GIN_MODE", "debug"),
	}
}

func getEnv(key, defaultValue string) string {
	value := os.Getenv(key)
	if value == "" {
		if defaultValue != "" {
			log.Printf("Using default value for %s: %s", key, defaultValue)
		}
		return defaultValue
	}
	return value
}
