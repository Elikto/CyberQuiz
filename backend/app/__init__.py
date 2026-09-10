"""CyberQuiz backend application package.

The account routers extend the existing social router so every supported
production entry point exposes the same authenticated APIs.
"""

from . import social
from .economy import router as economy_router
from .progress_sync import router as progress_sync_router
from .question_reports import router as question_reports_router
from .social_extensions import router as social_extensions_router
from .squad_dashboard import router as squad_dashboard_router

social.router.include_router(progress_sync_router)
social.router.include_router(social_extensions_router)
social.router.include_router(economy_router)
social.router.include_router(question_reports_router)
social.router.include_router(squad_dashboard_router)
