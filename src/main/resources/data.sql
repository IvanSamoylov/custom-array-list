DROP TABLE IF EXISTS "OrderEntry";
DROP TABLE IF EXISTS "Order";
DROP TABLE IF EXISTS "Product";
DROP TABLE IF EXISTS "Customer";


CREATE TABLE "Customer"
(
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE,
    address VARCHAR(255) NOT NULL
);

CREATE TABLE "Product"
(
    id    INT AUTO_INCREMENT PRIMARY KEY,
    code  VARCHAR(50)    NOT NULL,
    name  VARCHAR(100)   NOT NULL,
    price DECIMAL(10, 2) NOT NULL
);

CREATE TABLE IF NOT EXISTS "Order" (
   id INT AUTO_INCREMENT PRIMARY KEY,
   customerId INT,
   FOREIGN KEY (customerId) REFERENCES "Customer"(id) ON DELETE SET NULL
);

CREATE TABLE IF NOT EXISTS "OrderEntry" (
     id INT AUTO_INCREMENT PRIMARY KEY,
     orderId INT,
     productId INT,
     quantity INT NOT NULL,
     price DECIMAL NOT NULL,
     FOREIGN KEY (orderId) REFERENCES "Order"(id) ON DELETE CASCADE,
     FOREIGN KEY (productId) REFERENCES "Product"(id) ON DELETE CASCADE
    );

INSERT INTO "Product"(code, name, price)
VALUES ('T001', 'Chocolate Cake', 20.50),
       ('T002', 'Vanilla Cake', 18.00),
       ('T003', 'Strawberry Cake', 22.75),
       ('T004', 'Lemon Cake', 19.50),
       ('T005', 'Red Velvet Cake', 25.00);

--
INSERT INTO "Customer"(name, address)
VALUES ('vanya','Chelaybinsk'),
       ('aston','Ekaterinburg');

--
INSERT INTO "Order" (customerId) VALUES (1);
INSERT INTO "Order" (customerId) VALUES (2);

INSERT INTO "OrderEntry" (orderId, productId, quantity, price) VALUES (1, 1, 2, 100.0);
INSERT INTO "OrderEntry" (orderId, productId, quantity, price) VALUES (1, 2, 1, 200.0);

INSERT INTO "OrderEntry" (orderId, productId, quantity, price) VALUES (2, 1, 3, 100.0);
INSERT INTO "OrderEntry" (orderId, productId, quantity, price) VALUES (2, 2, 2, 200.0);