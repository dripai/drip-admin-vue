#!/usr/bin/env python3
"""Database SQL maintenance helpers for drip-admin.

This script is intentionally outside the Java backend. It exports the baseline
SQL from MySQL and can apply a chosen SQL file when a developer wants to update
or initialize a local database manually.
"""

from __future__ import annotations

import argparse
import os
import subprocess
from pathlib import Path


SCRIPT_DIR = Path(__file__).resolve().parent
DEFAULT_SCHEMA = SCRIPT_DIR / "schema.sql"


def with_mysql_password(password: str) -> dict[str, str]:
    env = os.environ.copy()
    env["MYSQL_PWD"] = password
    return env


def export_baseline(args: argparse.Namespace) -> None:
    output = Path(args.output).resolve()
    output.parent.mkdir(parents=True, exist_ok=True)
    temp_output = output.with_suffix(output.suffix + ".tmp")

    command = [
        args.mysqldump,
        "--single-transaction",
        "--routines",
        "--triggers",
        "--events",
        "--default-character-set=utf8mb4",
        "--skip-comments",
        "--column-statistics=0",
        "--set-gtid-purged=OFF",
        f"--host={args.host}",
        f"--port={args.port}",
        f"--user={args.user}",
        f"--result-file={temp_output}",
        args.database,
    ]
    run(command, with_mysql_password(args.password))

    dump = temp_output.read_text(encoding="utf-8")
    header = (
        "-- Baseline generated from current drip-manager database.\n"
        "-- Fresh database initialization baseline.\n\n"
    )
    output.write_text(header + dump, encoding="utf-8")
    temp_output.unlink()
    print(f"Baseline exported: {output}")


def apply_sql(args: argparse.Namespace) -> None:
    sql_file = Path(args.sql_file).resolve()
    if not sql_file.is_file():
        raise SystemExit(f"SQL file not found: {sql_file}")

    command = [
        args.mysql,
        "--default-character-set=utf8mb4",
        f"--host={args.host}",
        f"--port={args.port}",
        f"--user={args.user}",
        args.database,
    ]
    with sql_file.open("rb") as stdin:
        run(command, with_mysql_password(args.password), stdin=stdin)
    print(f"SQL applied: {sql_file}")


def run(command: list[str], env: dict[str, str], stdin=None) -> None:
    try:
        subprocess.run(command, env=env, stdin=stdin, check=True)
    except FileNotFoundError as exc:
        raise SystemExit(f"Command not found: {command[0]}") from exc
    except subprocess.CalledProcessError as exc:
        raise SystemExit(f"Command failed with exit code {exc.returncode}: {command[0]}") from exc


def add_connection_args(parser: argparse.ArgumentParser) -> None:
    parser.add_argument("--host", default="127.0.0.1")
    parser.add_argument("--port", type=int, default=3307)
    parser.add_argument("--user", default="root")
    parser.add_argument("--password", default="root")
    parser.add_argument("--database", default="drip-manager")


def build_parser() -> argparse.ArgumentParser:
    parser = argparse.ArgumentParser(description="Maintain drip-admin database SQL files.")
    subparsers = parser.add_subparsers(dest="command", required=True)

    export_parser = subparsers.add_parser("export-baseline", help="Export baseline SQL from MySQL.")
    add_connection_args(export_parser)
    export_parser.add_argument("--mysqldump", default="mysqldump")
    export_parser.add_argument("--output", default=str(DEFAULT_SCHEMA))
    export_parser.set_defaults(func=export_baseline)

    apply_parser = subparsers.add_parser("apply", help="Apply one SQL file to MySQL.")
    add_connection_args(apply_parser)
    apply_parser.add_argument("--mysql", default="mysql")
    apply_parser.add_argument("sql_file")
    apply_parser.set_defaults(func=apply_sql)

    return parser


def main() -> None:
    args = build_parser().parse_args()
    args.func(args)


if __name__ == "__main__":
    main()
