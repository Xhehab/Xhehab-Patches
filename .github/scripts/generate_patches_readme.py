#!/usr/bin/env python3
"""
Generates the patches section of README.md from patches-list.json
and injects it between <!-- PATCHES_START --> / <!-- PATCHES_END --> markers.

Spoilers are collapsed by default. They are expanded only when the README
marker explicitly says: <!-- PATCHES_START EXPANDED -->

App icons are loaded from .github/assets/icons/<package>.<ext>. If an icon is
missing, the script tries to download it from Google Play and cache it there.
Set NO_ICONS=1 to skip network access.

App authors are loaded from .github/app-authors.json, keyed by package name.
App section order is loaded from .github/app-order.json.

python3 generate_patches_readme.py <owner/repo> <branch> [patches-list.json] [README.md]
"""

import json
import os
import re
import sys
import urllib.error
import urllib.request
from html import escape
from pathlib import Path


if len(sys.argv) < 3:
    print("Usage: generate_patches_readme.py <owner/repo> <branch> [json] [readme]")
    sys.exit(1)

repo_full   = sys.argv[1]
branch      = sys.argv[2]
json_path   = Path(sys.argv[3]) if len(sys.argv) > 3 else Path("patches-list.json")
readme_path = Path(sys.argv[4]) if len(sys.argv) > 4 else Path("README.md")


if "/" not in repo_full:
    raise ValueError(f"Invalid repo format: {repo_full} (expected owner/repo)")

owner, repo = repo_full.split("/", 1)


with open(json_path, encoding="utf-8") as f:
    data = json.load(f)

try:
    authors = json.loads(Path(".github/app-authors.json").read_text(encoding="utf-8"))
except (OSError, ValueError):
    authors = {}

try:
    app_order = json.loads(Path(".github/app-order.json").read_text(encoding="utf-8"))
    if not isinstance(app_order, list):
        app_order = []
except (OSError, ValueError):
    app_order = []


# Group patches by package; patches with no compatiblePackages are universal.
# JSON structure: compatiblePackages is a list of objects with
# { packageName, name, targets: [{ version, isExperimental, description }] }
by_pkg = {}   # packageName -> { name, patches, targets }
universal = {}

for patch in data["patches"]:
    cp = patch.get("compatiblePackages")
    if not cp:
        # Deduplicate universal patches by name
        if patch["name"] not in universal:
            universal[patch["name"]] = patch
        continue
    for pkg_entry in cp:
        pkg  = pkg_entry["packageName"]
        name = pkg_entry.get("name") or pkg  # fall back to package name if no label
        if pkg not in by_pkg:
            by_pkg[pkg] = {
                "name":    name,
                "patches": {},
                "targets": pkg_entry.get("targets", []),
            }
        # Deduplicate patches that appear across multiple packages
        if patch["name"] not in by_pkg[pkg]["patches"]:
            by_pkg[pkg]["patches"][patch["name"]] = patch


def anchor(name):
    """Convert a patch name to a GitHub-compatible anchor slug."""
    return re.sub(r"-+", "-", re.sub(r"[^a-z0-9]+", "-", name.lower())).strip("-")


ICON_DIR  = Path(".github/assets/icons")
ICON_EXTS = ("png", "jpg", "jpeg", "webp", "svg")
NO_ICONS  = os.environ.get("NO_ICONS") == "1"
PLAY_URL  = "https://play.google.com/store/apps/details?id={}&hl=en&gl=US"


def icon_file(pkg):
    """Return the cached icon filename for a package, if one exists."""
    for ext in ICON_EXTS:
        name = f"{pkg}.{ext}"
        if (ICON_DIR / name).is_file():
            return name
    return None


def image_ext(blob):
    if blob.startswith(b"\x89PNG\r\n\x1a\n"):
        return "png"
    if blob.startswith(b"\xff\xd8\xff"):
        return "jpg"
    if blob[:4] == b"RIFF" and blob[8:12] == b"WEBP":
        return "webp"
    return None


def http_get(url):
    request = urllib.request.Request(url, headers={"User-Agent": "Mozilla/5.0"})
    try:
        with urllib.request.urlopen(request, timeout=20) as response:
            return response.read()
    except (urllib.error.URLError, OSError, ValueError):
        return None


def play_icon(pkg):
    """Fetch the app's icon from its Google Play listing."""
    page = http_get(PLAY_URL.format(pkg))
    if not page:
        return None, None
    match = re.search(
        r'"image":"(https://play-lh\.googleusercontent\.com/[^"]+)"',
        page.decode("utf-8", "replace"),
    )
    if not match:
        return None, None
    blob = http_get(f"{match.group(1)}=s96-cc")
    return (blob, image_ext(blob)) if blob else (None, None)


def ensure_icon(pkg):
    """Use a cached icon or download one from Google Play."""
    if icon_file(pkg) or NO_ICONS:
        return
    blob, ext = play_icon(pkg)
    if not ext:
        return
    ICON_DIR.mkdir(parents=True, exist_ok=True)
    (ICON_DIR / f"{pkg}.{ext}").write_bytes(blob)


def icon_img(pkg):
    name = icon_file(pkg) if pkg else None
    if not name:
        return "📦&nbsp;"
    return f'<img src=".github/assets/icons/{name}" width="18" align="absmiddle" alt="">&nbsp;&nbsp;'


def patches_table(patches):
    """Render a sorted markdown table of patches with name, description, and options."""
    rows = [
        "| 💊&nbsp;Patch | 📜&nbsp;Description | ⚙️&nbsp;Options |",
        "|----------|----------------|-----------|",
    ]
    for p in sorted(patches, key=lambda x: x["name"]):
        a = anchor(p["name"])
        options = p.get("options") or []
        if options:
            # Show only option titles as a bullet list
            parts = [opt.get("title") or opt.get("key") or "" for opt in options]
            opts_cell = "<br>".join(f"• {t}" for t in parts)
        else:
            opts_cell = ""
        desc = (p.get("description") or "").replace("\n", "<br>")
        rows.append(f"| [{p['name']}](#{a}) | {desc} | {opts_cell} |")
    return "\n".join(rows)


def versions_table(targets):
    """Render a markdown table of supported versions.
    Experimental versions get a 🧪 prefix.
    Versions with a description get it shown in a second row below.
    """
    if not targets:
        return ""

    cells = []
    for t in targets:
        ver   = t["version"]
        if ver is None:
            continue
        label = f"🧪&nbsp;{ver}" if t.get("isExperimental") else ver
        cells.append(label)

    if not cells:
        return ""

    header = "| " + " | ".join(cells) + " |"
    sep = "| " + " | ".join(":---:" for _ in cells) + " |"
    rows = [header, sep]

    # Optional description row — only rendered if at least one target has one
    descs = [(t.get("description") or "").replace("\n", "<br>") for t in targets]
    if any(descs):
        rows.append("| " + " | ".join(descs) + " |")

    return "\n".join(rows)


def spoiler(label, count, targets, tbl, expanded=False, pkg=None, author=None):
    """Wrap a patches table in a <details> spoiler with a versions sub-table.
    If expanded=True, the spoiler is open by default.
    """
    noun = "patch" if count == 1 else "patches"
    vtbl = versions_table(targets)
    versions_section = f"**🎯 Supported versions:**\n\n{vtbl}\n\n" if vtbl else ""
    tag = "<details open>" if expanded else "<details>"
    summary_parts = [f"{icon_img(pkg)}{label}"]
    if author:
        summary_parts.append(escape(author, quote=False))
    summary_parts.append(f"{count} {noun}")
    summary = "&nbsp;&nbsp;•&nbsp;&nbsp;".join(summary_parts)
    return f"""{tag}
<summary>{summary}</summary>
<br>

{versions_section}{tbl}

</details>"""


def build_content(expanded=False):
    """Build the full generated patches section."""
    lines = [
        f"> **[v{ver}](https://github.com/{owner}/{repo}/releases/tag/v{ver})**"
        f"&nbsp;&nbsp;•&nbsp;&nbsp;`{branch}`&nbsp;&nbsp;•&nbsp;&nbsp;"
        f"{total} patches total"
    ]

    ordered_pkgs = [pkg for pkg in app_order if pkg in by_pkg]
    ordered_pkgs += [pkg for pkg in by_pkg if pkg not in app_order]

    if not NO_ICONS:
        for pkg in ordered_pkgs:
            ensure_icon(pkg)

    # One spoiler per app, following app-order.json
    for pkg in ordered_pkgs:
        entry = by_pkg[pkg]
        patches = list(entry["patches"].values())
        lines.append(
            spoiler(
                entry["name"],
                len(patches),
                entry["targets"],
                patches_table(patches),
                expanded,
                pkg,
                authors.get(pkg),
            )
        )
        lines.append("")

    # Universal patches (no specific app)
    if universal:
        uni_patches = list(universal.values())
        noun = "patch" if len(uni_patches) == 1 else "patches"
        tag  = "<details open>" if expanded else "<details>"
        lines.append(f"""{tag}
<summary>🌐 Universal&nbsp;&nbsp;•&nbsp;&nbsp;{len(uni_patches)} {noun}</summary>
<br>

{patches_table(uni_patches)}

</details>""")
        lines.append("")

    return "\n".join(lines)


# Build and inject
raw_ver = data["version"]
# Strip leading "v" if present
ver   = raw_ver.lstrip("v")
total = sum(len(e["patches"]) for e in by_pkg.values()) + len(universal)

readme = readme_path.read_text(encoding="utf-8")

# Marker pattern — matches both <!-- PATCHES_START --> and <!-- PATCHES_START EXPANDED -->
START_PATTERN = r"<!-- PATCHES_START(?:\s+EXPANDED)?\s*-->"
END_MARKER    = "<!-- PATCHES_END -->"

marker_match = re.search(START_PATTERN, readme)

if not marker_match or END_MARKER not in readme:
    # Fallback: print to stdout so CI can catch the issue
    print(build_content(expanded=False))
    sys.stderr.write(
        f"⚠️  Markers <!-- PATCHES_START [EXPANDED] --> / {END_MARKER} not found in {readme_path}. "
        "Printed to stdout instead.\n"
    )
    sys.exit(1)

actual_start = marker_match.group(0)

# Spoilers are collapsed by default and expanded only when the README marker
# explicitly requests it.
expanded = "EXPANDED" in actual_start

generated  = build_content(expanded=expanded)
new_readme = re.sub(
    rf"{START_PATTERN}.*?{re.escape(END_MARKER)}",
    f"{actual_start}\n{generated}\n{END_MARKER}",
    readme,
    flags=re.DOTALL,
)
readme_path.write_text(new_readme, encoding="utf-8")
print(f"✅ Injected patches section into {readme_path} (v{ver}, branch={branch}, {total} patches, expanded={expanded})")
