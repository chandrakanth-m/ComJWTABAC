-- Insert default roles
INSERT INTO roles(name) VALUES('ROLE_USER');
INSERT INTO roles(name) VALUES('ROLE_MODERATOR');
INSERT INTO roles(name) VALUES('ROLE_ADMIN');

-- Insert default admin user (password: 123456)
INSERT INTO users(username, email, password) VALUES('admin', 'admin@example.com', '$2a$10$slYQmyNdGzTn7ZLBXBChFOC9f6kFjAqPhccnP6DxlWXx2lPk1C3G6');

-- Assign admin role to admin user
INSERT INTO user_roles(user_id, role_id) VALUES(1, 3);

-- Insert sample moderator user (password: 123456)
INSERT INTO users(username, email, password) VALUES('moderator', 'mod@example.com', '$2a$10$slYQmyNdGzTn7ZLBXBChFOC9f6kFjAqPhccnP6DxlWXx2lPk1C3G6');

-- Assign moderator role to moderator user
INSERT INTO user_roles(user_id, role_id) VALUES(2, 2);

-- Insert sample regular user (password: 123456)
INSERT INTO users(username, email, password) VALUES('user', 'user@example.com', '$2a$10$slYQmyNdGzTn7ZLBXBChFOC9f6kFjAqPhccnP6DxlWXx2lPk1C3G6');

-- Assign user role to regular user
INSERT INTO user_roles(user_id, role_id) VALUES(3, 1);

-- Insert sample tasks with owners
INSERT INTO TASK (title, description, completed, owner_id) VALUES ('Learn Spring Boot', 'Study Spring Boot fundamentals and create a REST API', false, 1);
INSERT INTO TASK (title, description, completed, owner_id) VALUES ('Learn Angular', 'Study Angular fundamentals and create a frontend application', false, 2);
INSERT INTO TASK (title, description, completed, owner_id) VALUES ('Integrate Spring Boot with Angular', 'Connect Angular frontend with Spring Boot backend', false, 3);