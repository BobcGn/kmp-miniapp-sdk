#!/usr/bin/env python3
"""Execute the actual release guard shell blocks with isolated git/curl responses.

Uses only Python's standard library, no publication commands, credentials or network.
"""
import os
from pathlib import Path
import re
import subprocess
import tempfile
import unittest

WORKFLOW = Path(__file__).resolve().parents[1] / "workflows/release.yml"
TEXT = WORKFLOW.read_text()
COMMIT = re.search(r"RELEASE_COMMIT: ([0-9a-f]{40})", TEXT).group(1)


def shell_block(name):
    """Read a literal run block by step name; fail if the workflow shape changes."""
    lines = TEXT.splitlines()
    start = lines.index("      - name: " + name)
    end = next((i for i in range(start + 1, len(lines))
                if lines[i].startswith("      - name:")), len(lines))
    run = next(i for i in range(start, end) if lines[i] == "        run: |")
    result = []
    for line in lines[run + 1:end]:
        if line and not line.startswith("          "):
            break
        result.append(line[10:] if line else "")
    return "\n".join(result)


IDENTITY = shell_block("Verify the release identity")
CENTRAL = shell_block("Check whether the runtime is already on Maven Central")
PORTAL = shell_block("Check whether the plugin is already on the Gradle Plugin Portal")
RECOVERY = shell_block("Require the runtime for plugin-only recovery")
VISIBLE = shell_block("Confirm the runtime is visible before publishing the plugin")


class ReleaseGuardsTest(unittest.TestCase):
    def run_guard(self, script, codes=("200",), **overrides):
        with tempfile.TemporaryDirectory(prefix="miniapp-release-guards-") as directory:
            root = Path(directory)
            (root / "gradle").mkdir()
            (root / "gradle/libs.versions.toml").write_text('miniapp = "0.1.1"\n')
            binaries = root / "bin"
            binaries.mkdir()
            mocks = {
                "git": '#!/usr/bin/env python3\nimport os, sys\nprint(os.environ["TAG_SHA" if "^{commit}" in sys.argv[-1] else "HEAD_SHA"])\n',
                "curl": '#!/usr/bin/env python3\nimport os, pathlib, sys\np = pathlib.Path(os.environ["CURL_RESPONSES"])\nlines = p.read_text().splitlines()\nvalue = lines.pop(0) if len(lines) > 1 else lines[0]\np.write_text("\\n".join(lines))\nif value == "error": sys.exit(6)\nprint(value, end="")\n',
                "sleep": '#!/bin/sh\nexit 0\n',
            }
            for name, content in mocks.items():
                executable = binaries / name
                executable.write_text(content)
                executable.chmod(0o755)
            responses = root / "responses"
            responses.write_text("\n".join(codes))
            output = root / "output"
            env = {key: value for key, value in os.environ.items()
                   if key not in {"BASH_ENV", "ORG_GRADLE_PROJECT_miniappLocalPublicationWithoutSigning"}}
            env.update(PATH=str(binaries) + os.pathsep + env["PATH"],
                       RELEASE_TAG="v0.1.1", RELEASE_COMMIT=COMMIT,
                       HEAD_SHA=COMMIT, TAG_SHA=COMMIT, GITHUB_SHA=COMMIT,
                       GITHUB_EVENT_NAME="workflow_dispatch", GITHUB_REF_TYPE="branch",
                       GITHUB_REF_NAME="main", CONFIRMATION="publish-0.1.1",
                       GITHUB_OUTPUT=str(output), CURL_RESPONSES=str(responses),
                       RUNTIME_PUBLISHED="true")
            env.update(overrides)
            result = subprocess.run(["bash", "-c", script], cwd=root, env=env,
                                    capture_output=True, text=True, timeout=10)
            self.assertFalse((root / "injected").exists(), "confirmation was executed as shell code")
            return result, output.read_text() if output.exists() else ""

    def assert_guard(self, script, success, codes=("200",), **env):
        result, output = self.run_guard(script, codes, **env)
        self.assertEqual(result.returncode == 0, success, result.stdout + result.stderr)
        return output

    def test_manual_confirmation(self):
        self.assert_guard(IDENTITY, True)
        self.assert_guard(IDENTITY, False, CONFIRMATION="wrong")

    def test_confirmation_on_manual_tag_run(self):
        self.assert_guard(IDENTITY, False, GITHUB_REF_TYPE="tag", CONFIRMATION="wrong")

    def test_confirmation_is_data(self):
        self.assert_guard(IDENTITY, False, CONFIRMATION="$(touch injected)")

    def test_wrong_checkout_and_moved_tag(self):
        self.assert_guard(IDENTITY, False, HEAD_SHA="0" * 40)
        self.assert_guard(IDENTITY, False, TAG_SHA="0" * 40)

    def test_tag_push_identity(self):
        env = dict(GITHUB_EVENT_NAME="push", GITHUB_REF_TYPE="tag", GITHUB_REF_NAME="v0.1.1")
        self.assert_guard(IDENTITY, True, **env)
        self.assert_guard(IDENTITY, False, **dict(env, GITHUB_SHA="0" * 40))
        self.assert_guard(IDENTITY, False, **dict(env, GITHUB_REF_NAME="v0.1.2"))
        self.assert_guard(IDENTITY, False, **dict(env, GITHUB_REF_TYPE="branch"))

    def test_unsigned_publication_is_refused_even_for_empty_property(self):
        self.assert_guard(IDENTITY, False, ORG_GRADLE_PROJECT_miniappLocalPublicationWithoutSigning="")

    def test_runtime_both_present_or_missing(self):
        self.assertEqual(self.assert_guard(CENTRAL, True, ("200", "200")), "published=true\n")
        self.assertEqual(self.assert_guard(CENTRAL, True, ("404", "404")), "published=false\n")

    def test_partial_runtime_is_not_missing(self):
        for codes in [("200", "404"), ("404", "200")]:
            self.assert_guard(CENTRAL, False, codes)

    def test_registry_errors_never_allow_upload(self):
        for script in [CENTRAL, PORTAL]:
            for code in ["500", "403", "429", "error"]:
                result, output = self.run_guard(script, (code,))
                self.assertNotEqual(result.returncode, 0)
                self.assertNotIn("published=false", output)

    def test_portal_present_or_missing(self):
        self.assertEqual(self.assert_guard(PORTAL, True), "published=true\n")
        self.assertEqual(self.assert_guard(PORTAL, True, ("404",)), "published=false\n")

    def test_plugin_recovery_requires_runtime(self):
        self.assert_guard(RECOVERY, True)
        self.assert_guard(RECOVERY, False, RUNTIME_PUBLISHED="false")
        self.assert_guard(RECOVERY, False, RUNTIME_PUBLISHED="")

    def test_visibility_waits_for_both_runtime_modules(self):
        self.assert_guard(VISIBLE, True, ("404", "200", "200", "200"))

    def test_visibility_timeout_and_query_failure(self):
        self.assert_guard(VISIBLE, False, ("404",))
        self.assert_guard(VISIBLE, False, ("500",))
        self.assert_guard(VISIBLE, False, ("error",))

    def test_publication_order_and_pinned_checkout(self):
        self.assertIn("          ref: " + COMMIT, TEXT)
        names = re.findall(r"^      - name: (.+)$", TEXT, re.M)
        self.assertLess(names.index("Require the runtime for plugin-only recovery"),
                        names.index("Publish runtime to Maven Central"))
        self.assertLess(names.index("Publish runtime to Maven Central"),
                        names.index("Confirm the runtime is visible before publishing the plugin"))
        self.assertLess(names.index("Confirm the runtime is visible before publishing the plugin"),
                        names.index("Validate the Gradle plugin publication"))
        self.assertLess(names.index("Validate the Gradle plugin publication"),
                        names.index("Publish Gradle plugin to the Gradle Plugin Portal"))
        self.assertNotIn("${{", IDENTITY)


if __name__ == "__main__":
    unittest.main(verbosity=2)
