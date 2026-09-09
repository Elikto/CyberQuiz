from .main import app
from .progress_sync import router as progress_sync_router

app.include_router(progress_sync_router)
