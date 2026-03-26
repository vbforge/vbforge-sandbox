CREATE TABLE IF NOT EXISTS users (
                                     id BIGINT PRIMARY KEY AUTO_INCREMENT,
                                     username VARCHAR(100) NOT NULL UNIQUE,
                                     password VARCHAR(255) NOT NULL,
                                     email VARCHAR(100) NOT NULL UNIQUE,
                                     role VARCHAR(20) NOT NULL,
                                     registration_date DATETIME NOT NULL,
                                     last_login_date DATETIME NOT NULL
);