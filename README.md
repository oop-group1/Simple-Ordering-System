# Simple-Ordering-System

after you git clone this, make sure to do these first.

open and sign in to your sql.
after you launch eclipse make sure to set the directory to where you cloned the repo.
put your sql credentials in com.ordering.system.dao.SQLConn.java.
go to sql workbench File>Open SQL Script then select dborsys.sql
run dborsys.sql
create a new query and run these codes:

USE dborsys;

-- Create a dummy admin

INSERT INTO tblemployees (emp_name) VALUES ('Admin User');
INSERT INTO tbluserstaff (usr_staff_id, staff_email, staff_password) VALUES (1, 'admin@system.com', 'admin123');

-- Creates a dummy customer record

INSERT INTO tblcustomer (cust_name) VALUES ('Test Customer');

-- Creates a dummy user account 


INSERT INTO tblusercust (usr_cust_id, usr_email, usr_password) 
VALUES (1, 'customer@test.com', 'pass123');

-- More customers so the order screen's customer dropdown (1-5) all work

INSERT INTO tblcustomer (cust_name) VALUES
('Juan Dela Cruz'), ('Maria Santos'), ('Pedro Reyes'), ('Ana Lim');

INSERT INTO tblusercust (usr_cust_id, usr_email, usr_password) VALUES
(2, 'juan@test.com', 'pass123'),
(3, 'maria@test.com', 'pass123'),
(4, 'pedro@test.com', 'pass123'),
(5, 'ana@test.com', 'pass123');

everything should be working now.

The design used is the MVC design pattern. Summary of the files:  
1. The Model Layer (Data Objects)  
	These files are simple blueprints. No logic, just data.  
		-User.java: Holds user information (ID, email, password, and role).  
		-Product.java: Holds product details (ID, name, description, price, and quantity).  
		-Order.java: Represents an order (Order ID, the Customer ID who bought it, and the total price).  
		-OrderItem.java: Represents a specific item within an order (which product, how many, and the price at the time of purchase).  

2. The DAO Layer (Data Access Objects)  
	These files communicates to the MySQL database using SQL queries.  
		-SQLConn.java: Handles the connection to MySQL and provides helper methods to run queries without repeating code.  
		-UserDAO.java: Handles SQL queries related to users (e.g., SELECT to check if a staff member exists for login).  
		-ProductDAO.java: Handles all CRUD operations for products (INSERT to add, SELECT to view/search, UPDATE for stock, and DELETE to remove).  
		-OrderDAO.java: Handles the Transaction. It ensures that if an order is placed, the order is recorded AND the stock is deducted. If one fails, it rolls back both.  

3. The Service Layer (Business Logic)  
	Validates data before it reaches the DAO.  
		-AbstractService.java: A base class (Abstraction) that forces all other services to have validation and calculation methods.  
		-UserService.java: Handles the logic for authentication (verifying if the password matches the database record).  
		-ProductService.java: Validates product data (e.g., ensuring price isn't negative) before calling the ProductDAO.  
		-OrderService.java: Manages the checkout process. It calculates totals and checks if there is enough stock before allowing the OrderDAO to save the order.  

4. The UI Layer (User Interface)    
	The actual frontend is a Java Swing GUI built on top of the existing services (no backend logic was changed).    
		-OrderingSystemApp.java: the main entry point. Run this to start the GUI.    
		-LoginFrame.java: the login window (staff sign in).  
		-MainFrame.java: the window after login; holds the Inventory, New Order and Reports tabs.  
		-InventoryPanel.java: add, view, search (live), update stock, and delete products.  
		-OrderPanel.java: pick products, build a cart, and place an order.  
		-ReportPanel.java: inventory summary stats and a full product list.  
		-Theme.java: all the colors, fonts and shared button/field styling.  
		-ReadOnlyTableModel.java: a small table model that makes the tables display-only.  
		-OrderingSystemGUI.java: the old console version, kept only as a backup for testing.  



How to run the GUI:  
	-Make sure the MySQL Connector/J .jar is on the project's Build Path  
	 (Project > Properties > Java Build Path > Libraries). If you get  
	 "No suitable driver" or "ClassNotFoundException: com.mysql.cj.jdbc.Driver",  
	 the connector jar is not on the build path.  
	-Right-click OrderingSystemApp.java > Run As > Java Application.  
	-Login with the default admin: admin@system.com / admin123  
	-The order screen's Customer dropdown uses customers 1-5 from the seed above.  


What can the backend do (currently):  
Staff Authentication: login for staff/admins using database records.  
Full Product Management (CRUD):  
	-Create: Add new products to the inventory.  
	-Read: View all products or search for a specific one by name.  
	-Update: Manually adjust stock levels (increase or decrease).  
	-Delete: Remove products from the system.  

Ordering (Transaction):  
	-Place an order for a specific customer.  
	-Automatically deduct stock from tblproducts when an order is placed.  
	-if the database crashes mid-order, no partial orders are saved.  
	
Input Validation: Prevents the system from crashing if you enter a letter where a number is expected (via try-catch).  
OOP Compliance: Demonstrates Abstraction, Inheritance, Polymorphism, and Encapsulation.  

What the Backend cannot do (currently, the we may or may not add):  
    Payment Processing  
    User Management GUI: Currently, to add Customers and Staff is done manually via MySQL Workbench.  
    Order History/Reporting: Can place orders, but cannot currently "View all orders for Customer X" or "Generate a monthly sales report." or whatever.  
    Order Status Updates  
    Customer Portal 
