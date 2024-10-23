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
   customer_id INT,
   FOREIGN KEY (customer_id) REFERENCES "Customer"(id) ON DELETE SET NULL
);

CREATE TABLE IF NOT EXISTS "OrderEntry" (
     id INT AUTO_INCREMENT PRIMARY KEY,
     order_id INT,
     product_id INT,
     quantity INT NOT NULL,
     price DOUBLE NOT NULL,
     FOREIGN KEY (order_id) REFERENCES "Order"(id) ON DELETE CASCADE,
     FOREIGN KEY (product_id) REFERENCES "Product"(id) ON DELETE CASCADE
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
