# Import FastAPI framework to build our API
from fastapi import FastAPI

# Import sentence transformers to generate embeddings
from sentence_transformers import SentenceTransformer

# Import Groq client to use Llama 3 for dynamic categorization
from groq import Groq

# Import BaseModel to define request/response structure
from pydantic import BaseModel

# Create the FastAPI app
app = FastAPI()

# Load the AI embedding model
model = SentenceTransformer("all-MiniLM-L6-v2")

# Initialize Groq client with your API key
# Replace YOUR_API_KEY with your actual Groq API key
client = Groq(api_key="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")

# Define request models
class TextRequest(BaseModel):
    text: str  # Text to convert to embedding

class ClassifyRequest(BaseModel):
    text: str  # Text to classify into dynamic category

# POST /embed → takes text and returns its embedding vector
@app.post("/embed")
def get_embedding(request: TextRequest):

    # Convert text into 384 numbers representing its meaning
    embedding = model.encode(request.text).tolist()

    return {"embedding": embedding}

# POST /classify → uses Groq/Llama3 to generate dynamic category name
@app.post("/classify")
def classify_text(request: ClassifyRequest):

    # Call Groq API with Llama 3 model
    response = client.chat.completions.create(
        model="llama-3.3-70b-versatile",

        messages=[
            {
                "role": "system",
                # Tell the AI exactly what we want
                "content": """You are a document categorization expert. 
                Given a document's text, generate a short folder name (2-4 words) 
                that best describes the document's topic.
                Rules:
                - Return ONLY the folder name, nothing else
                - No explanations, no punctuation
                - Use title case (e.g. "Legal Documents", "Financial Reports")
                - Be specific but broad enough to group similar documents"""
            },
            {
                "role": "user",
                # Send first 500 characters of the document
                "content": f"Categorize this document:\n\n{request.text[:500]}"
            }
        ],
        max_tokens=20, # We only need a short category name
        temperature=0.3 # Low temperature = more consistent results
    )

    # Extract the category name from the response
    category = response.choices[0].message.content.strip()

    return {"category": category}

# GET / → health check
@app.get("/")
def health_check():
    return {"status": "AI service is running!"}