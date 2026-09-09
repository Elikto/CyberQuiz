import unittest

from fastapi import HTTPException
from fastapi.testclient import TestClient
from pydantic import ValidationError

from app import progress_sync
from app.entry import app


class ProgressSyncTests(unittest.TestCase):
    def test_snapshot_requires_supported_version(self):
        with self.assertRaises(ValidationError):
            progress_sync.ProgressSnapshotWrite(
                baseRevision=0,
                snapshot={"version": 2},
            )

    def test_snapshot_size_is_bounded(self):
        with self.assertRaises(ValidationError):
            progress_sync.ProgressSnapshotWrite(
                baseRevision=0,
                snapshot={"version": 1, "padding": "x" * (513 * 1024)},
            )

    def test_revision_conflict_fails_closed(self):
        self.assertEqual(progress_sync._next_revision(4, 4), 5)
        with self.assertRaises(HTTPException) as ctx:
            progress_sync._next_revision(4, 3)
        self.assertEqual(ctx.exception.status_code, 409)

    def test_progress_router_is_mounted_on_render_entrypoint(self):
        paths = {route.path for route in app.routes}
        self.assertIn("/api/social/progress", paths)

    def test_progress_requires_authentication(self):
        response = TestClient(app).get("/api/social/progress")
        self.assertEqual(response.status_code, 401)


if __name__ == "__main__":
    unittest.main()
