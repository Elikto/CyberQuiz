"""CyberQuiz backend application package.

The progress sync and social lifecycle routers extend the existing social
router so every supported production entry point exposes the same authenticated
account API.
"""

from . import social
from .progress_sync import router as progress_sync_router
from .social_extensions import router as social_extensions_router

social.router.include_router(progress_sync_router)
social.router.include_router(social_extensions_router)
