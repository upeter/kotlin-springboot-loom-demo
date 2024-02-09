ALTER SYSTEM SET max_connections ='200';
select pg_reload_conf();

create sequence if NOT EXISTS hibernate_sequence start 1 increment 1 cache 20;

CREATE TABLE IF NOT EXISTS users (
    id SERIAL PRIMARY KEY,
    user_name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL,
    email_verified BOOLEAN DEFAULT FALSE,
    avatar_url VARCHAR(255)
);

