LIFELINE
========

Professional BCA Final-Year Project

TECHNOLOGY
----------
Frontend : HTML, CSS, JavaScript
Backend  : Core Java HttpServer
Storage  : JSON
Runtime  : JDK
Deploy   : Docker / Render-ready

PROJECT STRUCTURE
-----------------
LifeLine/
|-- src/
|   `-- LifeLineServer.java
|
|-- public/
|   |-- index.html
|   |-- login.html
|   |-- register.html
|   |-- recovery.html
|   |-- dashboard.html
|   |-- create.html
|   |-- card.html
|   |-- profile.html
|   |-- about.html
|   |-- style.css
|   |-- app.js
|   `-- print.css
|
|-- data/
|   |-- users.json
|   `-- cards.json
|
|-- run.bat
|-- Dockerfile
`-- README.txt

RUN IN VS CODE
--------------
1. Open the LifeLine folder in VS Code.
2. Open Terminal.
3. Run:

   .\run.bat

Or manually:

   javac -d . src\LifeLineServer.java
   java LifeLineServer

4. Open:

   http://localhost:8787

MAIN FEATURES
-------------
- Registration and Login
- Change Password / Account Recovery on a separate centered page
- Recovery fields: Email Address, New Password, Confirm Password
- Plain-text password storage in users.json as required for this academic build
- Profile page showing only User Name, Email and User ID
- Profile actions: Change Password and Delete Account
- Username dropdown with My Profile, Delete Account and Logout
- Custom Delete Account confirmation with Cancel / Delete Account
- Custom Logout confirmation
- One emergency card per user
- Full Name, Date of Birth / Age, Phone Number and Home Address
- One emergency contact
- Blood group, allergies, medical conditions, medications and important notes
- Optional primary doctor information
- Add / edit / delete emergency card
- Custom card-delete confirmation
- Emergency contact call button on mobile
- Save emergency card as PDF through browser print dialog
- Single-page A4 print layout for the emergency card
- Times New Roman typography in the printed PDF
- Responsive dark professional UI
- Dockerfile for deployment

SUCCESS MESSAGE FLOW
--------------------
All success notifications stay visible for about 2.5 seconds before redirecting.

Registration:
Registration successful -> about 2.5 seconds -> Login page

Login:
Login successful -> about 2.5 seconds -> Dashboard

Card add:
Card added successfully -> about 2.5 seconds -> Emergency Card

Card update:
Card updated successfully -> about 2.5 seconds -> Emergency Card

Card delete:
Card deleted successfully -> about 2.5 seconds -> Dashboard

Password change:
Password changed successfully -> about 2.5 seconds -> Login page

Account delete:
Account deleted successfully -> about 2.5 seconds -> Login page

Logout:
Logged out successfully -> about 2.5 seconds -> Login page

PASSWORD STORAGE NOTE
---------------------
This academic/demo build stores the password directly in users.json because
that is the project requirement.

Example:

{
  "id": "USR001",
  "name": "Seema",
  "email": "seema@gmail.com",
  "password": "1234"
}

No SHA-32, SHA-256 or other password hashing is used in this academic build.
Plain-text password storage is NOT recommended for a real production
application. A production system should use secure password hashing.

PDF
---
Open the Emergency Card and click:

Download / Save as PDF

The browser print dialog opens. Select "Save as PDF".
The print stylesheet is configured for:
- A4 portrait
- One page
- Times New Roman
- Compact readable spacing
- Home Address included
- No second/blank page by the page layout

RENDER NOTE
-----------
The project includes a Dockerfile and uses the PORT environment variable.

JSON storage is suitable for the academic/demo project, but on cloud
platforms such as Render, local files may not persist across all
restarts/redeployments unless persistent storage is configured.

VIVA SUMMARY
------------
Frontend:
HTML + CSS + JavaScript

Backend:
Core Java using HttpServer

Storage:
JSON files

Authentication:
Register, Login, Change Password, Logout and Delete Account

CRUD:
Create, Read, Update and Delete emergency card

PDF:
Browser print stylesheet + Save as PDF

Deployment:
Docker-ready and Render-compatible
