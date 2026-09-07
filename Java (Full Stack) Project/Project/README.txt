EcoTrack - Final Professional VS Code Project

Requirements:
- JDK 11+ recommended
- VS Code or any text editor
- Any modern web browser
- No MySQL, Node.js, Maven, Spring Boot or XAMPP required

Run in VS Code / PowerShell:
1. Open this folder in VS Code.
2. Open PowerShell in the folder containing EcoTrackServer.java.
3. Run: javac EcoTrackServer.java
4. Run: java EcoTrackServer
5. Open: http://localhost:8787

Java clean routes:
- /              -> index.html
- /home          -> home.html
- /register      -> register.html
- /login         -> login.html
- /about         -> about.html
- /learn-more    -> learn-more.html
- /tracking      -> tracking.html
- /transport     -> transport.html
- /home-energy   -> home-energy.html
- /food          -> food.html
- /shopping      -> shopping.html
- /dashboard     -> dashboard.html

Old .html URLs are redirected to the clean Java routes.

Final UI requirements included:
- Bootstrap 5.3.3 is actually used on every HTML page.
- One consistent EcoTrack background across the site.
- Professional responsive Bootstrap cards on Home, About Us, Learn More, Register, Login and Dashboard.
- Dashboard has proper cards for Total Carbon Footprint, Emissions Breakdown, Global Benchmarks, AI-Powered Personalized Insights and Take Action.
- Global Benchmarks uses a balanced responsive three-column layout for You, India avg. and World avg.
- How It Works has three equal responsive cards.
- Start Tracking arrow changes color on hover/focus.
- Account dropdown is hidden before login and appears only after successful login.
- Account dropdown contains Delete Account and Logout.
- Delete Account and Logout both show a confirmation modal before the action.
- Success notifications for registration, logout and account deletion are professional Bootstrap-style notices and disappear automatically after 3 seconds.
- User names are stored/displayed with the first letter capitalized.
- Passwords are stored as SHA-256 hashes in data/users.json; the original password is never written to the file.
- Each user's calculation is saved separately by email in data/calculations.json.
- Dashboard values are generated from the user's entered tracker data; no fixed dashboard result is used.

Project files are plain VS Code-friendly source files: HTML, CSS, Java and JavaScript, plus JSON data and run.bat.
