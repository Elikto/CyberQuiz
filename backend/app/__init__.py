"""CyberQuiz backend application package.

The progress sync router extends the existing social router so both supported
production entry points expose the same authenticated account API.
"""

from . import social
from .progress_sync import router as progress_sync_router

social.router.include_router(progress_sync_router)
