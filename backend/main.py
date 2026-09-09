import uvicorn

from backend.app.main import app
from backend.app.social import router as social_router

app.include_router(social_router)

if __name__ == "__main__":
    uvicorn.run(app, host="0.0.0.0", port=8000)
