DROP TABLE IF EXISTS users;

CREATE TABLE users (
        id INT AUTO_INCREMENT PRIMARY KEY,
        username VARCHAR(50) NOT NULL,
        password VARCHAR(100) NOT NULL
);

INSERT INTO users (username, password)
VALUES ('teacher', '$2a$10$PnB.R0STF1v6E6iT7WpI6.UVqUKn8AvR28QIw2r6y3dKtqMKxZaIi');

