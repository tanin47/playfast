# Create the user table

# --- !Ups

CREATE TABLE "user"
(
    id TEXT PRIMARY KEY DEFAULT ('user-' || gen_random_uuid()),
    email TEXT NOT NULL,
    hashed_password TEXT,
    preferred_lang TEXT,
    should_receive_newsletter BOOLEAN,
    is_email_verified BOOLEAN NOT NULL,
    created_at TIMESTAMP NOT NULL
);

CREATE UNIQUE INDEX user__email ON "user" (email);

CREATE TABLE "forgot_password_token"
(
    user_id TEXT NOT NULL,
    token TEXT NOT NULL,
    created_at TIMESTAMP NOT NULL
);

CREATE TABLE "email_verification_token"
(
    user_id TEXT NOT NULL,
    token TEXT NOT NULL,
    created_at TIMESTAMP NOT NULL
);

# --- !Downs

DROP TABLE "user";

DROP TABLE "forgot_password_token";

DROP TABLE "email_verification_token";
