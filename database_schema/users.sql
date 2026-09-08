CREATE EXTENSION IF NOT EXISTS pgcrypto;

CREATE TABLE users (
                       id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

                       name TEXT NOT NULL
                           CHECK (char_length(trim(name)) BETWEEN 2 AND 100),

                       email_address TEXT NOT NULL
                           CHECK (char_length(email_address) <= 254),

                       password_hash TEXT NOT NULL
                           CHECK (char_length(password_hash) BETWEEN 20 AND 500),

                       phone_number TEXT
                           CHECK (
                               phone_number IS NULL
                                   OR char_length(phone_number) BETWEEN 7 AND 20
                               ),

                       created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

                       updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

                       CONSTRAINT users_email_unique
                           UNIQUE (email_address)
);