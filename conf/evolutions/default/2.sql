# Add dummy_counter to user

# --- !Ups

ALTER TABLE "user" ADD COLUMN dummy_counter INT NOT NULL DEFAULT '0';


# --- !Downs

ALTER TABLE "user" DROP COLUMN dummy_counter;
