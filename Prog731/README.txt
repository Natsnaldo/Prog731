PHARMACY INVENTORY MANAGEMENT SYSTEM (PIMS)
HealthFirst Pharmacy
Programming 732 Assignment - Richfield Graduate Institute of Technology
============================================================

1. WHAT THIS IS
----------------
A Java desktop application (Swing/AWT + JDBC) that lets HealthFirst
Pharmacy manage stock, process sales at a point-of-sale till, and
generate management reports, with separate logins for Administrators
and Cashiers.

2. REQUIREMENTS
----------------
- Java JDK 17 or later
- MySQL Server 8.x
- MySQL Connector/J (JDBC driver) on the classpath
  Download: https://dev.mysql.com/downloads/connector/j/
  (choose the "Platform Independent" .zip/.tar.gz, use the .jar inside)

3. DATABASE SETUP
------------------
1. Start MySQL and open a client (MySQL Workbench / mysql CLI).
2. Run the script in database.sql:
       mysql -u root -p < database.sql
   This creates the "pims" database, all five tables, and inserts
   sample data (an admin, a cashier, three suppliers, several
   medicines).
3. By default the app connects to:
       jdbc:mysql://localhost:3306/pims
       username: root
       password: (blank)
   Edit these three values at the top of
   src/com/healthfirst/pims/db/DBConnection.java if your MySQL
   username/password/host differ.

4. DEFAULT LOGIN CREDENTIALS
------------------------------
   Administrator : admin    / admin123
   Cashier       : cashier  / cash123

(Passwords are stored in the database as SHA-256 hashes, not plain
text - see util/PasswordUtil.java. The hashes for the two accounts
above are already inserted by database.sql.)

5. COMPILING AND RUNNING (command line)
------------------------------------------
From the project root, with mysql-connector-j-x.x.x.jar copied into
a "lib" folder:

    javac -d bin -cp . src/com/healthfirst/pims/**/*.java src/com/healthfirst/pims/*.java
    java -cp bin;lib/mysql-connector-j-9.1.0.jar com.healthfirst.pims.Main      (Windows)
    java -cp bin:lib/mysql-connector-j-9.1.0.jar com.healthfirst.pims.Main      (macOS/Linux)

(Or simply open the project in NetBeans/IntelliJ/Eclipse, add the
connector jar as a library, and run Main.java.)

6. PACKAGING AS AN .EXE
--------------------------
Use a wrapper such as Launch4j or jpackage to bundle the compiled
classes + connector jar + a bundled JRE into
yourname_pims.exe, as required by the assignment brief.
Example with jpackage (JDK 17+):

    jpackage --input bin --main-jar pims.jar --main-class com.healthfirst.pims.Main ^
              --name yourname_pims --type exe --win-console

7. PROJECT STRUCTURE
------------------------
src/com/healthfirst/pims/
    Main.java                 - application entry point
    db/DBConnection.java      - single point of JDBC connection setup
    model/                    - plain data classes (User, Medicine, Supplier, Sale, SaleItem)
    dao/                      - JDBC data-access classes (one per table)
    util/PasswordUtil.java    - SHA-256 password hashing helper
    gui/LoginFrame.java       - login screen, role-based redirection
    gui/admin/                - AdminDashboard + Medicines/Suppliers/Users/Reports tabs
    gui/cashier/              - CashierDashboard + POS, Bill window, Stock Check
database.sql                  - CREATE DATABASE / TABLES / sample data
screenshots/                  - place required screenshots here before zipping

8. REPORTS PROVIDED
-----------------------
- Sales Report        (transactions with totals over a date range)
- Item-Wise Report     (quantity and revenue sold per medicine)
- Low Stock Report     (quantity_in_stock <= reorder_level)
- Expiry Report        (medicines expiring within the next month)

9. PUSHING THIS PROJECT TO YOUR GITHUB (git@github.com:Natsnaldo/Prog731.git)
---------------------------------------------------------------------------------
This folder is already a git repository with a full commit history
showing the project being built up step by step. To push it to your
own GitHub repo:

    cd Prog731
    git remote add origin git@github.com:Natsnaldo/Prog731.git
    git branch -M main
    git push -u origin main

If you get a permission/authentication error, it means your SSH key
isn't set up with GitHub on this machine yet:
    1. ssh-keygen -t ed25519 -C "your_email@example.com"   (press Enter for defaults)
    2. cat ~/.ssh/id_ed25519.pub                            (copy the output)
    3. On GitHub: Settings -> SSH and GPG keys -> New SSH key -> paste it
    4. Try the git push command again.

If your repo on GitHub already has a README/license (non-empty), pull
first to avoid a rejected push:
    git pull origin main --allow-unrelated-histories
    git push -u origin main

From here on, keep committing as you customise the code (fix a bug,
add validation, tweak the UI, add your name to the header comments,
etc.) so your push/commit history keeps growing beyond the required
10 commits - each small, real change as its own commit is better than
one huge commit at the end.

10. NOTES ON ACADEMIC INTEGRITY
----------------------------------
This is starter/reference code generated to help you learn the
concepts covered in the Programming 732 study guide (Swing/AWT,
JDBC, layout managers, event handling). Before submitting, read
through every file, make sure you understand it, adapt it (comments,
variable names, extra validation, your own touches), fill in your
name/student number on the cover page, and only submit work you can
explain and defend if asked.
