-- Script de création de la base de données pour le système de gestion de stock JADE

-- Table des produits
CREATE TABLE IF NOT EXISTS products (
    id INT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    description VARCHAR(255),
    price DECIMAL(10,2) NOT NULL,
    category VARCHAR(50),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Table des stocks
CREATE TABLE IF NOT EXISTS stock (
    id INT PRIMARY KEY AUTO_INCREMENT,
    product_id INT NOT NULL,
    quantity INT NOT NULL DEFAULT 0,
    min_quantity INT NOT NULL DEFAULT 10,
    max_quantity INT NOT NULL DEFAULT 100,
    location VARCHAR(50),
    last_updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (product_id) REFERENCES products(id)
);

-- Table des fournisseurs
CREATE TABLE IF NOT EXISTS suppliers (
    id INT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    contact_email VARCHAR(100),
    contact_phone VARCHAR(20),
    address VARCHAR(255),
    reliability_score DECIMAL(3,2) DEFAULT 5.00
);

-- Table des commandes
CREATE TABLE IF NOT EXISTS orders (
    id INT PRIMARY KEY AUTO_INCREMENT,
    customer_name VARCHAR(100) NOT NULL,
    order_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    status VARCHAR(20) DEFAULT 'PENDING',
    total_amount DECIMAL(10,2),
    delivery_address VARCHAR(255)
);

-- Table des détails de commande
CREATE TABLE IF NOT EXISTS order_items (
    id INT PRIMARY KEY AUTO_INCREMENT,
    order_id INT NOT NULL,
    product_id INT NOT NULL,
    quantity INT NOT NULL,
    unit_price DECIMAL(10,2) NOT NULL,
    FOREIGN KEY (order_id) REFERENCES orders(id),
    FOREIGN KEY (product_id) REFERENCES products(id)
);

-- Table des livraisons
CREATE TABLE IF NOT EXISTS deliveries (
    id INT PRIMARY KEY AUTO_INCREMENT,
    order_id INT NOT NULL,
    delivery_date TIMESTAMP,
    status VARCHAR(20) DEFAULT 'SCHEDULED',
    carrier VARCHAR(50),
    tracking_number VARCHAR(100),
    estimated_delivery TIMESTAMP,
    FOREIGN KEY (order_id) REFERENCES orders(id)
);

-- Insertion des données de test
INSERT INTO products (name, description, price, category) VALUES 
('Ordinateur Portable', 'HP EliteBook 15 pouces', 899.99, 'Informatique'),
('Souris Wireless', 'Logitech MX Master 3', 79.99, 'Accessoires'),
('Clavier Mécanique', 'Corsair K95 RGB', 149.99, 'Accessoires'),
('Écran 24 pouces', 'Dell UltraSharp U2419H', 299.99, 'Moniteurs'),
('Webcam HD', 'Logitech C920', 69.99, 'Accessoires');

INSERT INTO stock (product_id, quantity, min_quantity, max_quantity, location) VALUES 
(1, 25, 5, 50, 'Entrepôt A'),
(2, 100, 20, 200, 'Entrepôt B'),
(3, 45, 10, 80, 'Entrepôt B'),
(4, 30, 8, 60, 'Entrepôt A'),
(5, 75, 15, 120, 'Entrepôt C');

INSERT INTO suppliers (name, contact_email, contact_phone, address, reliability_score) VALUES 
('TechDistrib', 'contact@techdistrib.com', '+33123456789', '123 Rue de la Tech, Paris', 4.5),
('ComputerWorld', 'sales@computerworld.com', '+33987654321', '456 Avenue des Ordinateurs, Lyon', 4.2),
('AccessoirePro', 'info@accessoirepro.fr', '+33555666777', '789 Boulevard des Accessoires, Marseille', 4.8);