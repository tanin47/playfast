CREATE USER play_fast_dev_user WITH PASSWORD 'dev';
CREATE DATABASE play_fast_dev;
GRANT ALL PRIVILEGES ON DATABASE play_fast_dev to play_fast_dev_user;
ALTER ROLE play_fast_dev_user superuser;
      
CREATE USER play_fast_test_user_admin WITH PASSWORD 'test';
CREATE DATABASE play_fast_test;
GRANT ALL PRIVILEGES ON DATABASE play_fast_test to play_fast_dev_user;
ALTER ROLE play_fast_test_user_admin superuser;
