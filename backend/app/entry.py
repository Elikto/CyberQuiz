from .main import app
from .social import router as social_router

app.include_router(social_router)
