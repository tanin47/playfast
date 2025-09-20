# Create the user table

# --- !Ups

CREATE TABLE "user"
(
    id TEXT PRIMARY KEY DEFAULT ('user-' || gen_random_uuid()),
    email TEXT NOT NULL,
    hashed_password TEXT NOT NULL,
    preferred_lang TEXT,
    should_receive_newsletter BOOLEAN,
    created_at TIMESTAMP NOT NULL
);

CREATE UNIQUE INDEX user__email ON "user" (email)

# --- !Downs

DROP TABLE "user";
