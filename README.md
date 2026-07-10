# URL Shortener Application

A robust and efficient URL shortening service built with Spring Boot, MySQL, and Redis. This application converts long URLs into short, shareable links and provides fast redirection with caching.


## ✨ Features

- **URL Shortening**: Convert long URLs into compact short codes
- **Fast Redirection**: Redirect short URLs to original URLs
- **Redis Caching**: High-performance caching to reduce database queries
- **Base62 Encoding**: Efficient encoding for generating unique short codes
- **RESTful API**: Clean HTTP API for URL operations
- **Database Persistence**: MySQL storage for URL mappings

---

## 🛠 Tech Stack

| Component | Technology | Version |
|-----------|-----------|---------|
| Framework | Spring Boot | 4.1.0 |
| Language | Java | 21 |
| ORM | Spring Data JPA + Hibernate | 7.4.1 |
| Database | MySQL | 9.7.0 |
| Cache | Redis | 6.2+ |
| Build Tool | Maven | 3.8+ |
| Additional | Lombok | Latest |

---

## 📁 Project Structure

```
src/
├── main/
│   ├── java/com/url/shortner/
│   │   ├── ShortnerApplication.java          # Spring Boot entry point
│   │   ├── controller/
│   │   │   └── UrlShortnerController.java    # REST API endpoints
│   │   ├── service/
│   │   │   └── UrlService.java               # Business logic
│   │   ├── entity/
│   │   │   ├── UrlEntity.java                # JPA entity (database model)
│   │   │   ├── UrlRequest.java               # Request DTO
│   │   │   └── UrlResponse.java              # Response DTO
│   │   ├── repository/
│   │   │   └── UrlRepository.java            # Database access layer
│   │   └── utility/
│   │       └── Base62Encoder.java            # Encoding utility
│   └── resources/
│       └── application.properties            # Configuration file
└── test/
    └── java/com/url/shortner/
        └── ShortnerApplicationTests.java     # Unit tests
```

---

## 🏗 Architecture

### Layered Architecture (MVC Pattern)

```
┌─────────────────────────────────────────┐
│      REST API Layer (Controller)        │
│   UrlShortnerController                 │
│   • POST /v1/shorten                    │
│   • GET /{shortCode}                    │
└──────────────────┬──────────────────────┘
                   │
┌──────────────────▼──────────────────────┐
│     Business Logic Layer (Service)      │
│   UrlService                            │
│   • generateShortUrl()                  │
│   • redirectToOriginalUrl()             │
└──────────────────┬──────────────────────┘
                   │
        ┌──────────┴──────────┐
        │                     │
┌───────▼──────────┐  ┌──────▼────────────┐
│  Data Layer      │  │  Cache Layer      │
│  (Repository)    │  │  (Redis)          │
│  UrlRepository   │  │  StringRedisTemp  │
│  MySQL           │  │  Late             │
└──────────────────┘  └───────────────────┘
```

---

## 🚀 Setup & Installation

### Prerequisites
- Java 21+
- Maven 3.8+
- MySQL 8.0+
- Redis 6.2+

### Step 1: Clone Repository
```bash
git clone https://github.com/Intakhab08/Url_Shortner.git
cd Url_Shortner
```

### Step 2: Create MySQL Database in Mac using Terminal
```bash
mysql -u root -p
CREATE DATABASE url_shortener_db;
EXIT;
```

### Step 3: Configure Database Connection
Edit `src/main/resources/application.properties`:
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/url_shortener_db
spring.datasource.username=root
spring.datasource.password=your_password
```

### Step 4: Start Redis
```bash
redis-server
```

### Step 5: Run Application
```bash
mvn clean install
mvn spring-boot:run
```

The application will start on `http://localhost:8080`

---

## 📡 API Documentation

### 1. Shorten URL
**Endpoint:** `POST /v1/shorten`

**Request:**
```json
{
  "originalUrl": "https://www.example.com/very/long/url/path"
}
```

**Response (200 OK):**
```json
{
  "shortUrl": "http://localhost:8080/a9Kx2m"
}
```

**Error (400 Bad Request):**
```json
{
  "error": "Invalid URL"
}
```

---

### 2. Redirect to Original URL
**Endpoint:** `GET /{shortCode}`

**Example:** `GET /a9Kx2m`

**Response:** Redirects to the original URL (HTTP 301/302)

---

## 🔄 How It Works

### URL Shortening Flow

```
1. User sends POST /v1/shorten with originalUrl
                          ↓
2. UrlShortnerController receives request
                          ↓
3. UrlService.generateShortUrl() is invoked
                          ↓
4. UrlEntity is created and saved to MySQL
   (Auto-generated ID: e.g., 12345)
                          ↓
5. ID is encoded using Base62Encoder
   (12345 → "a9Kx2")
                          ↓
6. Short URL created: "http://localhost:8080/a9Kx2"
                          ↓
7. Updated UrlEntity saved back to MySQL
                          ↓
8. Response returned with shortUrl
```

### URL Redirection Flow with Caching

```
1. User accesses GET /a9Kx2m
                          ↓
2. UrlService.redirectToOriginalUrl() is called
                          ↓
3. Redis cache is checked
   ├─ If found → Return originalUrl immediately ✓ (Fast!)
   └─ If not found → Continue to step 4
                          ↓
4. Query MySQL for matching shortUrl
                          ↓
5. If found:
   ├─ Store in Redis with expiration (60 minutes)
   ├─ Return originalUrl
   └─ Next access will hit cache!
                          ↓
6. If not found → Return null (404 Error)
```

---

## 🗄 Database Schema

### URLs Table
```sql
CREATE TABLE urls (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    originalUrl VARCHAR(2048) NOT NULL,
    shortUrl VARCHAR(25) UNIQUE,
    createdAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_short_url (shortUrl)
);
```

**Fields:**
- `id`: Auto-generated unique identifier (used for encoding)
- `originalUrl`: The long URL provided by user
- `shortUrl`: Generated short URL (unique)
- `createdAt`: Creation timestamp (immutable)
- `idx_short_url`: Index on shortUrl for fast lookups

---

## 📊 Key Components Explained

### 1. **Base62Encoder** (Utility)
Converts numeric IDs to alphanumeric codes for short URLs.
- **Input:** `12345` (database ID)
- **Output:** `a9Kx` (Base62 encoded)
- **Charset:** 0-9, a-z, A-Z (62 characters)
- **Advantage:** Compact representation, human-readable

### 2. **UrlService** (Business Logic)
- **generateShortUrl()**: Creates a new URL mapping
- **redirectToOriginalUrl()**: Retrieves original URL with caching

### 3. **Redis Caching**
- **Reduces database load** by caching frequently accessed mappings
- **Default expiration:** 60 minutes (configurable)
- **Key format:** `http://localhost:8080/{shortCode}`
- **Value:** Original URL

### 4. **Spring Data JPA**
- Simplifies database operations with Hibernate ORM
- Auto-generates SQL queries
- Type-safe data access

---

## ⚙️ Configuration

`application.properties` settings:

```properties
# Application
spring.application.name=shortner
server.port=8080

# Database
spring.datasource.url=jdbc:mysql://localhost:3306/url_shortener_db
spring.datasource.username=root
spring.datasource.password=your_password
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQLDialect

# Redis
spring.redis.host=localhost
spring.redis.port=6379

# Cache
cache.expiration.seconds=60
```

---

## 🧪 Testing the API

### Using cURL
```bash
# Shorten a URL
curl -X POST http://localhost:8080/v1/shorten \
  -H "Content-Type: application/json" \
  -d '{"originalUrl":"https://www.google.com"}'

# Access shortened URL
curl -i http://localhost:8080/a9Kx2
```

### Using Postman
1. **Method:** POST
2. **URL:** `http://localhost:8080/v1/shorten`
3. **Headers:** `Content-Type: application/json`
4. **Body:** 
```json
{
  "originalUrl": "https://example.com/very/long/url"
}
```

---

## 🔧 Troubleshooting

| Issue | Solution |
|-------|----------|
| MySQL connection error | Verify MySQL is running and credentials are correct |
| Redis connection error | Start Redis server: `redis-server` |
| Port 8080 already in use | Change `server.port` in application.properties |
| Database not created | Run: `CREATE DATABASE url_shortener_db;` |

---

## 📝 License

This project is open source and available under the MIT License.

---

## 👤 Author

- **Developed by:** Md Intakhab
- **Repository:** [GitHub](https://github.com/Intakhab08/Url_Shortner)

---

## 🤝 Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

---

**Happy URL Shortening! 🎉**
