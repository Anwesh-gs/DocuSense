# DocuSense 
### AI-Powered Semantic Document Organizer

> Upload a folder of PDFs → AI reads every document → Automatically sorts them into smart category folders → Search across all documents using natural language

##  What It Does

Most document management tools make you manually sort files into folders. DocuSense does it for you — automatically.

Upload an entire folder of PDFs. DocuSense reads each document, understands its content using AI, assigns it a meaningful category like **"Legal Documents"**, **"Payment Records"**, or **"Real Estate Listings"**, and organizes everything into a clean folder structure. No predefined categories. No manual work.

---

## Features

| Feature | Description |
|---|---|
| 📁 Smart Folder Organization | AI reads PDFs and groups them into dynamically generated categories |
| 🔍 Semantic Search | Search by meaning, not just keywords — finds relevant docs even if exact words don't match |
| ⚠️ Duplicate Detection | Detects similar or identical documents using cosine similarity on AI embeddings |
| 📄 In-Browser PDF Viewer | Click any document to open it directly in the browser |
| ✂️ Move Documents | Manually move a document to a different folder |
| 🗑️ Delete Documents | Remove documents from the library |
| 📦 Bulk Upload | Upload entire folders at once |

---

##  Architecture

```
┌─────────────────┐     HTTP      ┌──────────────────────┐     HTTP     ┌─────────────────────┐
│   React Frontend │ ──────────► │  Spring Boot Backend  │ ──────────► │  Python FastAPI AI   │
│   (Port 3000)   │ ◄──────────  │     (Port 8081)       │ ◄──────────  │    Service (8000)    │
└─────────────────┘             └──────────────────────┘              └─────────────────────┘
                                           │                                      │
                                           ▼                                      │
                                    ┌─────────────┐                    ┌──────────────────────┐
                                    │    MySQL     │                    │  Sentence Transformers│
                                    │  Database    │                    │  + Groq (Llama 3)     │
                                    └─────────────┘                    └──────────────────────┘
```

**Three microservices working together:**
- **React Frontend** — User interface for uploading, browsing and searching documents
- **Spring Boot Backend** — Core API, file handling, database operations, orchestration
- **Python FastAPI AI Service** — Generates embeddings (Sentence Transformers) and dynamic category names (Groq/Llama 3)

---

## Tech Stack

### Backend
- **Java 17 + Spring Boot 4** — REST API framework
- **Spring Data JPA + Hibernate** — ORM for database operations
- **Apache PDFBox** — PDF text extraction
- **MySQL** — Persistent storage for documents, categories, embeddings

### AI / NLP
- **Sentence Transformers** (`all-MiniLM-L6-v2`) — Converts text into 384-dimensional embedding vectors
- **Groq API (Llama 3)** — Dynamically generates category names from document content
- **Cosine Similarity** — Measures semantic similarity between document embeddings

### Frontend
- **React 18** — UI framework
- **Axios** — HTTP client for API calls
- **CSS3** — Custom dark theme with animations

### Infrastructure
- **FastAPI + Uvicorn** — Python web framework for AI microservice
- **Maven** — Java dependency management
- **Git** — Version control

---

## Project Structure

```
DocuSense/
├── src/                          # Spring Boot Backend
│   └── main/java/com/docusense/
│       ├── controller/
│       │   └── PdfController.java         # REST endpoints
│       ├── model/
│       │   ├── Document.java              # Document entity
│       │   ├── DocumentRepository.java    # Database operations
│       │   ├── Category.java              # Category entity
│       │   └── CategoryRepository.java   # Category DB operations
│       ├── service/
│       │   ├── PdfExtractorService.java   # PDF text extraction
│       │   ├── TextCleanerService.java    # Text preprocessing
│       │   ├── EmbeddingService.java      # Calls Python for embeddings
│       │   ├── DynamicClassificationService.java  # Calls Groq for categories
│       │   ├── DuplicateDetectionService.java     # Cosine similarity check
│       │   ├── DocumentService.java       # Core business logic
│       │   └── SearchService.java         # Semantic search
│       └── utils/
│           └── DataLoader.java            # Seeds initial data on startup
│
├── frontend/                     # React Frontend
│   └── src/
│       ├── App.js                # Main React component
│       └── App.css               # Styling
│
├── ai/                           # Python AI Microservice
│   └── main.py                   # FastAPI app with /embed and /classify endpoints
│
└── pom.xml                       # Maven dependencies
```

---

## How It Works — Step by Step

### 1. PDF Upload & Text Extraction
```
User uploads PDF
→ Apache PDFBox extracts raw text from all pages
→ TextCleanerService removes formatting noise, hyphenated line breaks, special characters
→ Clean text stored in MySQL
```

### 2. AI Embedding Generation
```
Clean text sent to Python FastAPI service → /embed endpoint
→ Sentence Transformers (all-MiniLM-L6-v2) encodes text into 384 numbers
→ These numbers represent the MEANING of the document
→ Embedding stored in MySQL
```

### 3. Dynamic Categorization
```
Clean text sent to Python FastAPI service → /classify endpoint
→ Groq API (Llama 3) reads the document
→ Generates a relevant folder name like "Legal Documents" or "Payment Records"
→ No predefined categories — fully dynamic
→ Category stored with document in MySQL
```

### 4. Duplicate Detection
```
New document embedding compared against all existing embeddings
→ Cosine similarity calculated for each pair
→ Similarity > 0.95 → flagged as duplicate
→ Warning shown in UI
```

### 5. Semantic Search
```
User types search query
→ Query converted to embedding via Python /embed endpoint
→ Query embedding compared with all document embeddings using cosine similarity
→ Top 5 most similar documents returned
→ Works by MEANING not exact keywords
```

---

## Getting Started

### Prerequisites
- Java 17+
- Python 3.10+
- MySQL 8.0+
- Node.js 18+
- Groq API key (free at console.groq.com)

### 1. Clone the repo
```bash
git clone https://github.com/Anwesh-gs/DocuSense.git
cd DocuSense
```

### 2. Setup MySQL
```sql
CREATE DATABASE docusense;
```

### 3. Configure Backend
Edit `src/main/resources/application.properties`:
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/docusense
spring.datasource.username=root
spring.datasource.password=your_password
```

### 4. Start Backend
```bash
./mvnw spring-boot:run
```

### 5. Setup Python AI Service
```bash
cd ai
python -m venv venv
venv\Scripts\activate        # Windows
pip install fastapi uvicorn sentence-transformers groq
```

Add your Groq API key to `main.py`:
```python
client = Groq(api_key="your_groq_api_key")
```

```bash
uvicorn main:app --reload --port 8000
```

### 6. Start Frontend
```bash
cd frontend
npm install
npm start
```

Open http://localhost:3000 


## Key Technical Concepts

**Embeddings** — Text converted into vectors of numbers where similar meanings produce similar vectors. `all-MiniLM-L6-v2` produces 384-dimensional vectors.

**Cosine Similarity** — Measures the angle between two vectors. Score of 1.0 = identical meaning, 0.0 = no relation. Used for both duplicate detection and semantic search.

**Zero-Shot Classification** — Llama 3 can categorize documents into any category without being specifically trained on those categories.

**RAG-adjacent architecture** — Documents are stored with their embeddings enabling fast semantic retrieval — similar to how RAG systems work.

---

## 👨‍💻 Author

**Anwesh** — [GitHub](https://github.com/Anwesh-gs)


