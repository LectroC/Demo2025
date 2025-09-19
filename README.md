Online Code Snippet Sharing Tool
================================

A full-stack web application that enables users to share, view, and collaborate on code snippets with instant syntax highlighting. Features user authentication, persistent storage, and a modern responsive UI with interactive carousel showcasing project capabilities.

Tech Stack
----------
- **Backend**: Spring Boot 3 (Java 17), Spring Data JPA, Spring Web, Validation (Jakarta), springdoc-openapi
- **Database**: PostgreSQL with Flyway migrations
- **Frontend**: AngularJS 1.8 (CDN), Prism.js (CDN), Owl Carousel, Okaidia theme
- **Containerization**: Docker Compose for PostgreSQL
- **Packaging**: Maven (single executable JAR)

Project Layout
--------------
```
MyFirstProject/
├── server/                          # Spring Boot application
│   ├── src/main/java/com/example/snippetshare/
│   │   ├── entity/                  # JPA entities (UserEntity, SnippetEntity)
│   │   ├── repository/              # JPA repositories
│   │   ├── snippet/                 # Snippet domain logic
│   │   ├── user/                    # User authentication
│   │   └── SnippetShareApplication.java
│   ├── src/main/resources/
│   │   ├── db/migration/            # Flyway database migrations
│   │   ├── static/                  # Frontend assets
│   │   │   ├── images/              # Carousel images
│   │   │   ├── tpl/                 # AngularJS templates
│   │   │   ├── app.js               # AngularJS application
│   │   │   ├── styles.css           # Custom styling
│   │   │   └── index.html           # Main HTML file
│   │   └── application.yml          # Spring Boot configuration
│   └── pom.xml                      # Maven dependencies
├── docker-compose.yml               # PostgreSQL container setup
└── README.md                        # This file
```

Getting Started
---------------

### Prerequisites
- Java 17+ and Maven installed
- Docker and Docker Compose (for PostgreSQL)
- Git

### Quick Start (Recommended)

1) **Start PostgreSQL with Docker:**
```bash
docker-compose up -d
```

2) **Build and run the application:**
```bash
cd server
mvn clean package
java -jar target/snippet-share-0.0.1-SNAPSHOT.jar
```

3) **Access the application:**
- **Main UI**: http://localhost:8080/
- **API Documentation**: http://localhost:8080/swagger-ui/index.html

### Alternative Setup (Local PostgreSQL)

If you prefer to use a local PostgreSQL installation:

1) **Create database and user:**
```bash
createdb snippetdb
psql snippetdb -c "CREATE USER snippet WITH PASSWORD 'snippet';"
psql snippetdb -c "GRANT ALL PRIVILEGES ON DATABASE snippetdb TO snippet;"
```

2) **Build and run:**
```bash
cd server
mvn clean package
java -jar target/snippet-share-0.0.1-SNAPSHOT.jar
```

Features
--------

### 🔐 User Authentication
- **User Registration**: Create accounts with name and password
- **Login System**: Secure authentication with session management
- **Guest Mode**: Access snippets without registration
- **Smart Navigation**: Dynamic navbar based on login status

### 💾 Persistent Storage
- **PostgreSQL Database**: Reliable data persistence
- **JPA Entities**: UserEntity and SnippetEntity with relationships
- **Flyway Migrations**: Version-controlled database schema
- **Data Relationships**: Users can have multiple snippets

### 🎨 Modern UI/UX
- **Responsive Design**: Works on desktop, tablet, and mobile
- **Interactive Carousel**: Showcases project features with auto-slide
- **Syntax Highlighting**: 13+ programming languages supported
- **Sticky Navigation**: Persistent navbar with hover effects
- **Custom Styling**: Crimson theme with modern aesthetics

### 🚀 Technical Features
- **RESTful API**: Clean, documented endpoints
- **Real-time Updates**: Instant UI updates after operations
- **Error Handling**: User-friendly error messages
- **Performance**: Optimized queries with database indexes

### 🔔 UX Improvements (Recent)
- Alerts for registration success/failure, login success/failure, and sign-out (includes user name)
- Sign-out now redirects to `#/welcome`
- UI shows separate lists: Guest Snippets and Your Snippets
- Hide Guest Snippets when logged in; show "Shared Snippets" section for recipients
- Add multi-select Share menu; Edit Snippet available in guest and user modes

REST API
--------

### Authentication Endpoints
- `POST /api/auth/register` — Register new user
  - Body: `{ "name": string, "password": string }`
- `POST /api/auth/login` — User login
  - Body: `{ "name": string, "password": string }`

### Snippet Endpoints
- `GET /api/snippets` — List all snippets (ordered by creation date)
- `GET /api/snippets/guest` — List only guest snippets (no `user_id`)
- `GET /api/snippets/me` — List snippets for current user (provide header `X-User-Name`)
- `GET /api/snippets/{id}` — Fetch specific snippet
- `POST /api/snippets` — Create new snippet
  - Body: `{ "title": string, "code": string, "language": enum(Language) }`
- `DELETE /api/snippets/{id}` — Delete snippet
- `GET /api/snippets/languages` — List supported programming languages
- `PUT /api/snippets/{id}` — Update snippet (title, code, language)
- `POST /api/snippets/{id}/share-to` — Share a snippet to specific users
  - Body: `{ "userNames": string[] }`
- `GET /api/snippets/shared` — List all shared snippets (public shared)
- `GET /api/snippets/shared/me` — List snippets shared with the current user (header `X-User-Name`)

### API Documentation
Explore the complete API documentation at:
```text
http://localhost:8080/swagger-ui/index.html
```

Application Flow
----------------

### 🏠 Welcome Page
1. **Landing Experience**: Interactive carousel showcasing project features
2. **Navigation Options**: Login, Register, or continue as Guest
3. **Responsive Design**: Adapts to different screen sizes

### 🔐 Authentication Flow
1. **Registration**: Users create accounts with name and password
2. **Login**: Secure authentication with session management
3. **Guest Access**: Browse snippets without registration
4. **Smart Navigation**: Navbar adapts based on login status

### 📝 Snippet Management
1. **Create Snippets**: Users enter title, select language, and paste code
2. **Syntax Highlighting**: Prism.js provides beautiful code highlighting
3. **Persistent Storage**: All data saved to PostgreSQL database
4. **Real-time Updates**: UI refreshes immediately after operations
5. **Delete Functionality**: Users can remove their snippets

### 🎨 Frontend Architecture
- **AngularJS SPA**: Single-page application with routing
- **Template System**: Modular HTML templates for different views
- **Responsive Styling**: Custom CSS with crimson theme
- **Interactive Elements**: Carousel, hover effects, and smooth transitions

Database Schema
---------------

### Users Table
```sql
CREATE TABLE users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
```

### Snippets Table
```sql
CREATE TABLE snippets (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    title VARCHAR(120) NOT NULL,
    code TEXT NOT NULL,
    language VARCHAR(50) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    user_id UUID REFERENCES users(id) ON DELETE SET NULL,
    is_shared BOOLEAN NOT NULL DEFAULT FALSE,
    shared_by_user_id UUID REFERENCES users(id) ON DELETE SET NULL
);
```

### Snippet Recipients Table
```sql
CREATE TABLE snippet_recipients (
    snippet_id UUID NOT NULL REFERENCES snippets(id) ON DELETE CASCADE,
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (snippet_id, user_id)
);
```

### Views
- `guest_snippets` — all snippets with `user_id IS NULL`.
- Per-user views are created automatically on registration with the pattern:
  - `snippets_user_<userUUIDNoDashes>`
  - Example: `snippets_user_1f2e3d4c5b6a7...`
  - Definition: `CREATE OR REPLACE VIEW snippets_user_<id> AS SELECT * FROM snippets WHERE user_id = '<uuid>'`.

Notes:
- Views are created for convenience; the canonical source remains the `snippets` table.
- For querying “my snippets” via the API, use `GET /api/snippets/me` with header `X-User-Name: <name>`.

### Frontend Changes
- `index.html`: Navbar sign-out uses a function that clears storage, shows alert with user name, and redirects to `#/welcome`.
- `app.js`:
  - Alerts on register/login success/failure.
  - `window.signOut()` added for sign-out alert and redirect.
  - Snippet lists split into `snippetsGuest`, `snippetsUser`, and `snippetsShared`.
  - Share menu: loads users from `GET /api/auth/users`, then `POST /api/snippets/{id}/share-to`.
  - Edit flow: click "Edit Snippet" populates form; "Update Snippet" issues PUT.
- `tpl/app.html`: Composer actions show Add/Share vs Update/Cancel; includes multi-select share menu; Guest Snippets hidden when logged in; Shared Snippets section.

### Performance Indexes
- `idx_snippets_created_at` - Fast ordering by creation date
- `idx_snippets_language` - Language-based filtering
- `idx_snippets_user_id` - User snippet queries
- `idx_users_name` - Fast user lookups
- `idx_snippets_is_shared` - Shared snippet queries
- `idx_snippets_shared_by_user_id` - Shared-by queries
- `idx_snippet_recipients_user_id` - Recipient lookups
- `idx_snippet_recipients_snippet_id` - Snippet recipient lookups

Migrations
----------
- `V3__Add_sharing_columns.sql` — Adds `is_shared` and `shared_by_user_id` to `snippets`
- `V4__Add_snippet_recipients_table.sql` — Adds `snippet_recipients` join table

Usage Notes
-----------
- Guest snippets are visible only when not logged in.
- To share: log in, compose snippet, click "Share Snippet", select one or more users, then Share.
- Recipients see items under "Shared Snippets" and can Edit or re-Share.
- Editing is available for both guests (their local compose) and logged-in users (their own or shared snippets).

Configuration
-------------

### Database Configuration
The application uses PostgreSQL with the following default settings:
- **Database**: `snippetDb`
- **Username**: `snippet`
- **Password**: `snippet`
- **Port**: `5432`

### Environment Variables
You can override database settings using environment variables:
```bash
export SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/snippetdb
export SPRING_DATASOURCE_USERNAME=snippet
export SPRING_DATASOURCE_PASSWORD=snippet
```

Future Enhancements
-------------------

### 🔗 Shareable URLs
- Public snippet URLs with `/#/s/:id` routing
- Copy-to-clipboard functionality for sharing
- Embeddable snippet views

### 🔒 Enhanced Security
- OAuth2 integration (GitHub, Google)
- Password encryption/hashing
- Rate limiting and abuse protection
- JWT token-based authentication

### 🔍 Advanced Features
- Full-text search across snippets
- Tag system for categorization
- Snippet expiration and privacy settings
- Export/import functionality (GitHub Gists, files)

### 🎨 UI/UX Improvements
- Line numbers in code blocks
- Copy-to-clipboard buttons
- Dark/light theme toggle
- Advanced syntax highlighting options

### 🧪 Testing & Quality
- Unit tests for services and controllers
- Integration tests for API endpoints
- End-to-end testing with Playwright/Cypress
- Performance monitoring and optimization

### 🚀 DevOps
- Multi-stage Dockerfile for production
- CI/CD pipeline setup
- Health checks and monitoring
- Horizontal scaling support

Why AngularJS?
--------------
AngularJS was chosen for this project to create a lightweight, dependency-minimal SPA that can be served directly by Spring Boot without requiring a Node.js build process. This approach simplifies deployment and reduces complexity while still providing a modern single-page application experience.

For production applications, consider migrating to:
- **Angular 2+** with TypeScript
- **React** with modern tooling
- **Vue.js** for a progressive framework approach

Troubleshooting
---------------

### Common Issues

**Database Connection Failed**
- Ensure PostgreSQL is running: `docker-compose ps`
- Check database credentials in `application.yml`
- Verify port 5432 is not blocked by firewall

**Carousel Not Loading**
- Hard refresh browser (Ctrl+F5)
- Check browser console for JavaScript errors
- Ensure jQuery and Owl Carousel CDN links are accessible

**Images Not Displaying**
- Verify images are in `server/src/main/resources/static/images/`
- Check image paths use `/images/filename.jpg` format
- Ensure image files are not corrupted

**Build Failures**
- Ensure Java 17+ is installed: `java -version`
- Clean Maven cache: `mvn clean`
- Check for port conflicts (8080, 5432)

### Support
For issues and questions:
1. Check the browser console for errors
2. Review application logs in the terminal
3. Verify all prerequisites are installed
4. Ensure Docker containers are running properly

---

**Built with ❤️ using Spring Boot, AngularJS, and PostgreSQL**
