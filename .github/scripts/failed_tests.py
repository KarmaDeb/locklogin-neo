#!/usr/bin/env python3
"""
Process JUnit XML test results and create/reopen GitHub issues for failed tests.

Usage:
    python3 failed_tests.py <test_file1.xml> [test_file2.xml ...]

Environment variables:
    GITHUB_TOKEN: GitHub personal access token or GitHub Actions token
    GH_TOKEN: Alternative name for GITHUB_TOKEN
    GITHUB_REPOSITORY: Repository in format "owner/repo" (auto-detected in GitHub Actions)
"""

import sys
import os
import re
import xml.etree.ElementTree as ET
import subprocess
import json
from pathlib import Path
from typing import List, Dict, Optional, Tuple


class FailedTest:
    """Represents a failed test case."""

    def __init__(self, module: str, classname: str, name: str, time: str,
                 message: str, stacktrace: str):
        self.module = module
        self.classname = classname
        self.name = name
        self.time = time
        self.message = message
        self.stacktrace = stacktrace

    @property
    def fingerprint(self) -> str:
        """Unique identifier for this test: module:classname#name"""
        return f"{self.module}:{self.classname}#{self.name}"

    def __repr__(self):
        return f"FailedTest({self.fingerprint})"


def extract_module_from_path(test_file: str) -> str:
    """Extract module name from the test file path.

    Expected path format: ./<module>/build/test-results/test/TEST-*.xml
    """
    # Try to find the module name in the path
    parts = Path(test_file).parts
    for i, part in enumerate(parts):
        if part == 'build' and i > 0:
            return parts[i - 1]
    # Fallback: use the filename
    return Path(test_file).stem


def parse_test_results(test_files: List[str]) -> List[FailedTest]:
    """Parse JUnit XML test result files and extract failed tests."""
    failed_tests: List[FailedTest] = []

    for test_file in test_files:
        if not os.path.exists(test_file):
            print(f"Warning: Test file not found: {test_file}")
            continue

        try:
            tree = ET.parse(test_file)
            root = tree.getroot()
        except ET.ParseError as e:
            print(f"Warning: Failed to parse {test_file}: {e}")
            continue

        module = extract_module_from_path(test_file)

        for testcase in root.iter('testcase'):
            failure = testcase.find('failure')
            error = testcase.find('error')

            if failure is not None or error is not None:
                name = testcase.get('name', 'unknown')
                classname = testcase.get('classname', 'unknown')
                time = testcase.get('time', '0')

                # Get the failure/error message
                fail_node = failure if failure is not None else error
                message = fail_node.get('message', '')
                text = fail_node.text if fail_node.text else ''

                failed_tests.append(FailedTest(
                    module=module,
                    classname=classname,
                    name=name,
                    time=time,
                    message=message,
                    stacktrace=text
                ))

    return failed_tests


def get_github_token() -> Optional[str]:
    """Get GitHub token from environment variables."""
    return os.environ.get('GITHUB_TOKEN') or os.environ.get('GH_TOKEN')

def get_repository() -> Optional[str]:
    """Get repository name from environment variables."""
    return os.environ.get('GITHUB_REPOSITORY')

def run_gh_command(args: List[str], token: str) -> Tuple[int, str, str]:
    """Run a gh CLI command and return (returncode, stdout, stderr)."""
    env = os.environ.copy()
    env['GH_TOKEN'] = token

    result = subprocess.run(
        ['gh'] + args,
        capture_output=True,
        text=True,
        env=env
    )
    return result.returncode, result.stdout, result.stderr

def find_existing_issue(fingerprint: str, token: str) -> Optional[Dict]:
    """Search for an existing issue with the given fingerprint.

    Returns the issue dict if found, None otherwise.
    """
    # Search for issues with the fingerprint in the body
    returncode, stdout, stderr = run_gh_command([
        'issue', 'list',
        '--search', f'in:body "{fingerprint}"',
        '--state', 'all',
        '--json', 'number,title,state',
        '--limit', '10'
    ], token)

    if returncode != 0:
        print(f"Warning: Failed to search issues: {stderr}")
        return None

    try:
        issues = json.loads(stdout)
        for issue in issues:
            # Check if the issue is closed or resolved
            if issue.get('state') in ('closed', 'resolved'):
                return issue
    except json.JSONDecodeError as e:
        print(f"Warning: Failed to parse issue list: {e}")

    return None

def reopen_issue(issue_number: int, token: str) -> bool:
    """Reopen a closed issue."""
    returncode, stdout, stderr = run_gh_command([
        'issue', 'reopen', str(issue_number)
    ], token)

    if returncode == 0:
        print(f"✓ Reopened issue #{issue_number}")
        return True
    else:
        print(f"✗ Failed to reopen issue #{issue_number}: {stderr}")
        return False

def create_issue(test: FailedTest, token: str) -> bool:
    """Create a new GitHub issue for a failed test."""
    title = f"Test failure: {test.classname}#{test.name}"

    body = f"""## Failed Test

**Module:** {test.module}
**Class:** {test.classname}
**Method:** {test.name}
**Duration:** {test.time}s

### Error Message
```
{test.message}
```

### Stack Trace
```
{test.stacktrace}
```

---
*Fingerprint: {test.fingerprint}*
*Auto-generated by GitHub Actions*
"""

    returncode, stdout, stderr = run_gh_command([
        'issue', 'create',
        '--title', title,
        '--body', body,
        '--label', 'failing-test'
    ], token)

    if returncode == 0:
        # Extract issue number from output
        match = re.search(r'#(\d+)', stdout)
        if match:
            issue_num = match.group(1)
            print(f"✓ Created issue #{issue_num}: {title}")
        else:
            print(f"✓ Created issue: {title}")
        return True
    else:
        print(f"✗ Failed to create issue: {stderr}")
        return False


def process_failed_tests(failed_tests: List[FailedTest], token: str) -> None:
    """Process a list of failed tests and create/reopen issues."""
    if not failed_tests:
        print("No failed tests found.")
        return

    print(f"\nFound {len(failed_tests)} failed test(s):")
    for test in failed_tests:
        print(f"  - {test.fingerprint}")

    print("\nProcessing failed tests...")

    for test in failed_tests:
        print(f"\nProcessing: {test.fingerprint}")

        # Check if there's an existing closed issue
        existing = find_existing_issue(test.fingerprint, token)
        if existing:
            reopen_issue(existing['number'], token)
        else:
            create_issue(test, token)


def main():
    """Main entry point."""
    if len(sys.argv) < 2:
        print("Usage: python3 failed_tests.py <test_file1.xml> [test_file2.xml ...]")
        sys.exit(1)

    # Get GitHub token
    token = get_github_token()
    if not token:
        print("Error: GITHUB_TOKEN or GH_TOKEN environment variable not set")
        sys.exit(1)

    # Get repository
    repo = get_repository()
    if repo:
        print(f"Repository: {repo}")

    # Parse test files
    test_files = sys.argv[1:]
    print(f"Processing {len(test_files)} test file(s)...")

    # Parse failed tests
    failed_tests = parse_test_results(test_files)

    # Process failed tests
    process_failed_tests(failed_tests, token)


if __name__ == '__main__':
    main()
