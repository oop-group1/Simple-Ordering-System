DROP DATABASE IF EXISTS dborsys;

CREATE DATABASE dborsys;
USE dborsys;

CREATE TABLE tblcustomer(
	cust_id SMALLINT UNSIGNED PRIMARY KEY AUTO_INCREMENT NOT NULL,
    cust_name VARCHAR(256),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE tblemployees(
	emp_id SMALLINT UNSIGNED PRIMARY KEY AUTO_INCREMENT NOT NULL,
    emp_name VARCHAR(256)
);

CREATE TABLE tblusercust(
	usr_id SMALLINT UNSIGNED PRIMARY KEY AUTO_INCREMENT NOT NULL,
    usr_cust_id SMALLINT UNSIGNED NOT NULL,
    usr_email VARCHAR(256),
    usr_password VARCHAR(256),
    
    FOREIGN KEY (usr_cust_id) REFERENCES tblcustomer(cust_id)
);

CREATE TABLE tbluserstaff(
	usrs_id SMALLINT UNSIGNED PRIMARY KEY AUTO_INCREMENT NOT NULL,
    usr_staff_id SMALLINT UNSIGNED NOT NULL,
    staff_email VARCHAR(256),
    staff_password VARCHAR(256),
    
    FOREIGN KEY (usr_staff_id) REFERENCES tblemployees(emp_id)
);

CREATE TABLE tblproducts(
	item_id SMALLINT UNSIGNED PRIMARY KEY AUTO_INCREMENT NOT NULL,
    item_name VARCHAR(256),
    item_desc VARCHAR(500),
    item_qty INT UNSIGNED DEFAULT 0,
    item_price DECIMAL DEFAULT 0.0
);

CREATE TABLE tblorders(
	order_id SMALLINT UNSIGNED PRIMARY KEY AUTO_INCREMENT NOT NULL,
    user_id SMALLINT UNSIGNED NOT NULL,
    staff_id SMALLINT UNSIGNED NOT NULL,
    total_amount DECIMAL DEFAULT 0.0,
    status ENUM('PENDING', 'PREPARING', 'OUT_FOR_DELIVERY', 'COMPLETED', 'CANCELLED') DEFAULT 'PENDING',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (user_id) REFERENCES tblusercust (usr_id),
    FOREIGN KEY (staff_id) REFERENCES tbluserstaff (usrs_id)
);

CREATE TABLE tblorder_items(
	order_item_id SMALLINT UNSIGNED PRIMARY KEY AUTO_INCREMENT NOT NULL,
    order_id SMALLINT UNSIGNED NOT NULL,
    item_id SMALLINT UNSIGNED NOT NULL,
    quantity INT UNSIGNED DEFAULT 0,
    price_at_purchase DECIMAL DEFAULT 0.0,
    
    FOREIGN KEY (order_id) REFERENCES tblorders (order_id),
    FOREIGN KEY (item_id) REFERENCES tblproducts (item_id)
);

CREATE TABLE tblpayments(
	payment_id SMALLINT UNSIGNED PRIMARY KEY AUTO_INCREMENT NOT NULL,
    order_id SMALLINT UNSIGNED NOT NULL,
    payment_method ENUM('CASH', 'CARD', 'DIGITAL_BANK') DEFAULT 'CASH',
    payment_status ENUM('PENDING', 'AUTHORIZED', 'FAILED') DEFAULT 'PENDING',
    
    FOREIGN KEY (order_id) REFERENCES tblorders (order_id)
);

