#!/usr/bin/env python3
import json
import re
import sys
from pathlib import Path

CATEGORY_LABELS = {
    "RESEAUX": "Réseaux",
    "LINUX": "Linux",
    "WINDOWS": "Windows",
    "CRYPTO": "Cryptographie",
    "WEB": "Sécurité Web",
    "MALWARE": "Malware",
    "SOCIAL": "Ingénierie sociale",
    "OSINT": "OSINT",
    "FORENSICS": "Forensics",
    "PENTEST": "Pentest",
    "AD": "Active Directory",
    "CLOUD": "Cloud Security",
    "MOBILE": "Mobile Security",
    "SYSTEM": "Sécurité système",
}

ROOT = Path(__file__).resolve().parents[1]
BASE_SOURCE = ROOT / "app/src/main/java/com/example/cyberquiz/data/repository/QuizRepository.kt"
EXPANSION_SOURCE = ROOT / "app/src/main/java/com/example/cyberquiz/data/repository/CyberQuestionExpansion.kt"
OUTPUT = Path(sys.argv[1]) if len(sys.argv) > 1 else Path(__file__).with_name("questions.json")

def decode_string(token: str) -> str:
    token = token.strip()
    if not (token.startswith('"') and token.endswith('"')):
        raise ValueError(f"Expected Kotlin string literal, got: {token[:80]}")
    return json.loads(token)

def find_call(text: str, start: int):
    open_pos = text.find("(", start)
    if open_pos < 0:
        raise ValueError("Missing opening parenthesis")
    depth = 0
    in_string = False
    escaped = False
    for i in range(open_pos, len(text)):
        ch = text[i]
        if in_string:
            if escaped:
                escaped = False
            elif ch == "\\":
                escaped = True
            elif ch == '"':
                in_string = False
            continue
        if ch == '"':
            in_string = True
        elif ch == "(":
            depth += 1
        elif ch == ")":
            depth -= 1
            if depth == 0:
                return text[open_pos + 1:i], i + 1
    raise ValueError("Unclosed call")

def split_args(body: str):
    args = []
    current = []
    depth = 0
    in_string = False
    escaped = False
    for ch in body:
        if in_string:
            current.append(ch)
            if escaped:
                escaped = False
            elif ch == "\\":
                escaped = True
            elif ch == '"':
                in_string = False
            continue
        if ch == '"':
            in_string = True
            current.append(ch)
        elif ch in "([{":
            depth += 1
            current.append(ch)
        elif ch in ")]}":
            depth -= 1
            current.append(ch)
        elif ch == "," and depth == 0:
            args.append("".join(current).strip())
            current = []
        else:
            current.append(ch)
    if current:
        args.append("".join(current).strip())
    return args

def extract_questions(text: str, call_name: str):
    pattern = re.compile(rf"\b{re.escape(call_name)}\s*\(\s*Category\.([A-Z_]+)", re.MULTILINE)
    out = []
    pos = 0
    while True:
        match = pattern.search(text, pos)
        if not match:
            break
        body, end = find_call(text, match.start())
        args = split_args(body)
        pos = end
        if len(args) != 9:
            raise ValueError(f"{call_name} call has {len(args)} args, expected 9 near {match.start()}")
        category_key = args[0].split(".")[-1].strip()
        difficulty = args[1].split(".")[-1].strip()
        out.append({
            "category": CATEGORY_LABELS.get(category_key, category_key.title()),
            "difficulty": difficulty,
            "question": decode_string(args[2]),
            "answers": [decode_string(args[3]), decode_string(args[4]), decode_string(args[5]), decode_string(args[6])],
            "correctIndex": int(args[7].strip()),
            "explanation": decode_string(args[8]),
        })
    return out

def main():
    base_text = BASE_SOURCE.read_text(encoding="utf-8")
    expansion_text = EXPANSION_SOURCE.read_text(encoding="utf-8")
    start = base_text.index("private fun cybersecurityQuestions()")
    end = base_text.index("private fun nutritionQuestions()")
    base_cyber = base_text[start:end]
    questions = extract_questions(base_cyber, "cyber")
    questions += extract_questions(expansion_text, "q")
    for idx, q in enumerate(questions, 1):
        q["id"] = idx
    if len(questions) < 50:
        raise SystemExit(f"Expected at least 50 Cyber questions, found {len(questions)}")
    OUTPUT.parent.mkdir(parents=True, exist_ok=True)
    OUTPUT.write_text(json.dumps(questions, ensure_ascii=False, indent=2), encoding="utf-8")
    print(f"Generated {len(questions)} Cyber questions -> {OUTPUT}")

if __name__ == "__main__":
    main()
