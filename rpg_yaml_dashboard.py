# rpg_yaml_dashboard.py
# Streamlit Dashboard für MineLauncherRPG YAMLs inkl. Item-Icons direkt aus dem Minecraft-Client-JAR
# PLUS: Editieren & Speichern (Power-Editor pro Datensatz) mit Backup + Atomic Write + Live-Refresh
# PLUS: Mob-Portrait (Spawn-Egg bevorzugt; ansonsten Entity-Textur -> Face-Crop; ansonsten generisches Spawn-Egg)
#
# Start (Windows / PowerShell, Einzeiler):
#   py -m pip install streamlit pyyaml pandas pillow && py -m streamlit run .\rpg_yaml_dashboard.py
#
# Hinweise:
# - Speichern mit PyYAML kann Format/Key-Reihenfolge/Kommentare verändern (Kommentare gehen i.d.R. verloren).
#   Wenn du Kommentare/Format 1:1 erhalten willst: ruamel.yaml (kann man nachrüsten).

from __future__ import annotations

import json
import re
import zipfile
from dataclasses import dataclass
from io import BytesIO
from pathlib import Path
from typing import Any, Iterable

import pandas as pd
import streamlit as st
import yaml
from PIL import Image

# =============================================================================
# Defaults / Konfiguration
# =============================================================================

DEFAULT_FILES = {
    "mobs": "mobs.yml",
    "skills": "skills.yml",
    "quests": "quests.yml",
    "npcs": "npcs.yml",
    "loot": "loot.yml",
    "enchantments": "enchantments.yml",
    "spawners": "spawners.yml",
}

# Minecraft-Formatcodes: &a, &4, &l, &r sowie §a usw.
MC_COLOR_CODE_RE = re.compile(r"(&[0-9a-fk-or])|(§[0-9a-fk-or])", re.IGNORECASE)

# =============================================================================
# Key-Factory (zentral, stabil, kollisionsfrei)
# =============================================================================


def wkey(*parts: str) -> str:
    """
    Erzeugt stabile, eindeutige Streamlit-Keys.
    Best Practice:
    - pro Widget eine eindeutige Kombination aus (Bereich, Widget-Name, optional Kontext)
    """
    return "__".join(parts)


# =============================================================================
# Utilities
# =============================================================================


def strip_mc_codes(text: str) -> str:
    return MC_COLOR_CODE_RE.sub("", text).strip()


def to_float(v: Any) -> float | None:
    if v is None:
        return None
    try:
        return float(v)
    except (TypeError, ValueError):
        return None


def to_int(v: Any) -> int | None:
    if v is None:
        return None
    try:
        return int(float(v))
    except (TypeError, ValueError):
        return None


def to_bool(v: Any) -> bool | None:
    if v is None:
        return None
    if isinstance(v, bool):
        return v
    if isinstance(v, (int, float)):
        return bool(v)
    if isinstance(v, str):
        s = v.strip().lower()
        if s in {"true", "yes", "y", "1"}:
            return True
        if s in {"false", "no", "n", "0"}:
            return False
    return None


def to_list(v: Any) -> list[str]:
    if v is None:
        return []
    if isinstance(v, list):
        return [str(x) for x in v]
    return [str(v)]


def safe_get_str(d: dict[str, Any], key: str) -> str | None:
    v = d.get(key)
    if v is None:
        return None
    s = str(v).strip()
    return s or None


def as_pretty_json(obj: Any) -> str:
    return json.dumps(obj, ensure_ascii=False, indent=2, sort_keys=True)


def unique_sorted(values: Iterable[Any]) -> list[str]:
    s = {str(v) for v in values if v is not None and str(v).strip()}
    return sorted(s)


# =============================================================================
# Launcher-Config (für Autodetektion)
# =============================================================================


@st.cache_data(show_spinner=False)
def read_launcher_config(project_root: str) -> dict[str, Any]:
    path = Path(project_root) / "launcher-config.json"
    if not path.exists():
        return {}
    try:
        return json.loads(path.read_text(encoding="utf-8"))
    except Exception:
        return {}


def guess_client_version(cfg: dict[str, Any]) -> str:
    return str((cfg.get("game") or {}).get("clientVersion") or "1.20.4")


def guess_install_root(cfg: dict[str, Any]) -> str:
    return str(cfg.get("installRoot") or "servers")


def guess_server_name(cfg: dict[str, Any]) -> str:
    return str((cfg.get("server") or {}).get("name") or "paper-server")


# =============================================================================
# YAML Loading (cached)
# =============================================================================


@st.cache_data(show_spinner=False)
def load_yaml_mapping(path: str) -> dict[str, Any]:
    p = Path(path)
    if not p.exists():
        raise FileNotFoundError(f"Datei nicht gefunden: {p.resolve()}")
    raw = yaml.safe_load(p.read_text(encoding="utf-8")) or {}
    if not isinstance(raw, dict):
        raise ValueError(f"{p.name}: Root muss ein Mapping sein (id -> object).")
    return {str(k): v for k, v in raw.items()}


@st.cache_data(show_spinner=False)
def load_all(files: dict[str, str]) -> dict[str, dict[str, Any]]:
    return {k: load_yaml_mapping(v) for k, v in files.items()}


# =============================================================================
# YAML Saving (Backup + Atomic Write)
# =============================================================================


def dump_yaml_mapping(data: dict[str, Any]) -> str:
    """
    YAML-Ausgabe für Root-Mappings (id -> dict).
    sort_keys=False ist wichtig, damit PyYAML Schlüssel nicht automatisch sortiert.
    """
    return yaml.safe_dump(
        data,
        allow_unicode=True,
        sort_keys=False,
        default_flow_style=False,
        width=120,
    )


def atomic_write_text(path: Path, text: str) -> None:
    """
    Sicheres Schreiben:
    - erst temp-Datei
    - dann Replace
    verhindert halb-geschriebene YAMLs bei Crash/Abbruch.
    """
    path.parent.mkdir(parents=True, exist_ok=True)
    tmp = path.with_suffix(path.suffix + ".tmp")
    tmp.write_text(text, encoding="utf-8")
    tmp.replace(path)


def save_yaml_mapping(path: str, data: dict[str, Any], make_backup: bool = True) -> None:
    p = Path(path)
    if make_backup and p.exists():
        bak = p.with_suffix(p.suffix + ".bak")
        bak.write_text(p.read_text(encoding="utf-8"), encoding="utf-8")
    atomic_write_text(p, dump_yaml_mapping(data))


# =============================================================================
# Cross-Reference Indexes (cached)
# =============================================================================


@dataclass(frozen=True)
class Indexes:
    spawner_mobs: dict[str, dict[str, float]]
    mob_to_spawners: dict[str, list[str]]
    npc_to_quests: dict[str, list[str]]


@st.cache_data(show_spinner=False)
def build_indexes(all_data: dict[str, dict[str, Any]]) -> Indexes:
    spawners = all_data.get("spawners", {})
    npcs = all_data.get("npcs", {})

    spawner_mobs: dict[str, dict[str, float]] = {}
    mob_to_spawners: dict[str, list[str]] = {}

    for spawner_id, obj in spawners.items():
        if not isinstance(obj, dict):
            continue
        mobs_map = obj.get("mobs")
        if not isinstance(mobs_map, dict):
            continue
        cleaned: dict[str, float] = {}
        for mob_id, w in mobs_map.items():
            f = to_float(w)
            if f is None:
                continue
            cleaned[str(mob_id)] = f
            mob_to_spawners.setdefault(str(mob_id), []).append(spawner_id)
        spawner_mobs[spawner_id] = cleaned

    npc_to_quests: dict[str, list[str]] = {}
    for npc_id, obj in npcs.items():
        if not isinstance(obj, dict):
            continue

        quests: set[str] = set()

        ql = obj.get("questLink")
        if ql:
            quests.add(str(ql))

        nodes = obj.get("dialogueNodes")
        if isinstance(nodes, list):
            for node in nodes:
                if not isinstance(node, dict):
                    continue
                opts = node.get("options")
                if isinstance(opts, list):
                    for opt in opts:
                        if not isinstance(opt, dict):
                            continue
                        gq = opt.get("grantQuestId")
                        if gq:
                            quests.add(str(gq))

        npc_to_quests[npc_id] = sorted(quests)

    return Indexes(
        spawner_mobs=spawner_mobs,
        mob_to_spawners=mob_to_spawners,
        npc_to_quests=npc_to_quests,
    )


# =============================================================================
# Minecraft Item-Icons direkt aus dem Client-JAR
# =============================================================================


def material_to_texture_basename(material: str) -> str:
    """
    Bukkit/Spigot Material -> Vanilla Texture-Basename
    Beispiel: DIAMOND_SWORD -> diamond_sword
    """
    s = material.strip().lower()
    s = s.replace("minecraft:", "")
    s = s.replace(" ", "_")
    return s


@st.cache_data(show_spinner=False)
def find_client_jar(client_root: str, client_version: str) -> str | None:
    """
    CmlLib Layout:
      client_files/versions/<version>/<version>.jar
    """
    vdir = Path(client_root) / "versions" / client_version
    jar = vdir / f"{client_version}.jar"
    if jar.exists():
        return str(jar)

    # Fallback: irgendein jar im Versionsordner
    if vdir.exists():
        jars = sorted(vdir.glob("*.jar"))
        if jars:
            return str(jars[0])

    return None


@st.cache_data(show_spinner=False)
def read_zip_entry_bytes(jar_path: str, entry_name: str) -> bytes | None:
    try:
        with zipfile.ZipFile(jar_path, "r") as zf:
            with zf.open(entry_name) as fp:
                return fp.read()
    except Exception:
        return None


@st.cache_data(show_spinner=False)
def jar_png_index(jar_path: str) -> set[str]:
    """
    Index aller PNG-Dateien im JAR.
    Gibt ein Set mit exakten Entry-Namen zurück (case-sensitive).
    """
    try:
        with zipfile.ZipFile(jar_path, "r") as zf:
            return {n for n in zf.namelist() if n.lower().endswith(".png")}
    except Exception:
        return set()


@st.cache_data(show_spinner=False)
def resolve_icon_bytes_from_jar(jar_path: str, material: str) -> tuple[bytes | None, str | None]:
    """
    Versucht, Icon-Bytes aus dem Client-JAR zu laden.
    Robust:
    - erst bekannte Kandidaten (item/block)
    - dann Fallback: End-Suffix-Suche im PNG-Index
    - Spawn-Egg Fallback: generisches spawn_egg.png
    Rückgabe: (png_bytes, matched_entry_name)
    """
    if not material:
        return None, None

    base = material_to_texture_basename(material)

    # Primäre Kandidaten
    candidates = [
        f"assets/minecraft/textures/item/{base}.png",
        f"assets/minecraft/textures/block/{base}.png",
    ]

    # Optional: Special-Map (falls du später mal Sonderfälle brauchst)
    special: dict[str, str] = {}
    if material in special:
        s = special[material]
        candidates = [
            f"assets/minecraft/textures/item/{s}.png",
            f"assets/minecraft/textures/block/{s}.png",
        ] + candidates

    idx = jar_png_index(jar_path)

    # 1) Schnellcheck via Index + direkt lesen
    for entry in candidates:
        if entry in idx:
            b = read_zip_entry_bytes(jar_path, entry)
            if b:
                return b, entry

    # 2) Fallback: irgendwas, das auf "/<base>.png" endet
    suffix = f"/{base}.png"
    for entry in idx:
        if entry.endswith(suffix):
            b = read_zip_entry_bytes(jar_path, entry)
            if b:
                return b, entry

    # 3) Spawn-Egg Fallback: generisches Spawn-Egg (besser als nix)
    if base.endswith("_spawn_egg"):
        generic = "assets/minecraft/textures/item/spawn_egg.png"
        if generic in idx:
            b = read_zip_entry_bytes(jar_path, generic)
            if b:
                return b, generic

    return None, None


# =============================================================================
# Mob-Portraits: Spawn-Egg bevorzugt, sonst Entity-Textur (Face-Crop), sonst generisch
# =============================================================================


def entity_type_to_spawn_egg_material(entity_type: str | None) -> str | None:
    """
    Bukkit EntityType -> Material für Spawn-Egg.
    Beispiel: ENDERMAN -> ENDERMAN_SPAWN_EGG
    Einige Entities haben kein Spawn-Egg (z.B. ENDER_DRAGON). Dann None.
    """
    if not entity_type:
        return None

    et = entity_type.strip().upper()

    # Kein Spawn-Egg in Vanilla:
    no_egg = {
        "ENDER_DRAGON",
        "WITHER",
        "PLAYER",
        "GIANT",  # je nach Version/Server
        "LIGHTNING",
    }
    if et in no_egg:
        return None

    return f"{et}_SPAWN_EGG"


@st.cache_data(show_spinner=False)
def resolve_entity_texture_bytes(client_jar: str, entity_type: str | None) -> tuple[bytes | None, str | None]:
    """
    Versucht Entity-Texturen robust zu finden.
    Strategie:
    - bekannte Pfade (häufigste Fälle)
    - dann Suffix-Suche im PNG-Index nach ".../<entity>.png"
    """
    if not entity_type:
        return None, None

    et = entity_type.strip().lower()

    # Häufige Vanilla-Struktur:
    # assets/minecraft/textures/entity/<mob>/<mob>.png
    candidates = [
        f"assets/minecraft/textures/entity/{et}/{et}.png",
        f"assets/minecraft/textures/entity/{et}.png",
        # Skeleton-Familie
        f"assets/minecraft/textures/entity/skeleton/{et}.png",
        # Slime-Familie
        f"assets/minecraft/textures/entity/slime/{et}.png",
        # Zombie-Familie
        f"assets/minecraft/textures/entity/zombie/{et}.png",
        # Enderman hat eigenes Unterverzeichnis
        f"assets/minecraft/textures/entity/enderman/{et}.png",
    ]

    idx = jar_png_index(client_jar)

    for entry in candidates:
        if entry in idx:
            b = read_zip_entry_bytes(client_jar, entry)
            if b:
                return b, entry

    # Suffix-Fallback: irgendwo im JAR eine entity-Textur, die auf "/<et>.png" endet
    suffix = f"/{et}.png"
    best: str | None = None
    for entry in idx:
        if "/textures/entity/" in entry and entry.endswith(suffix):
            best = entry
            break

    if best:
        b = read_zip_entry_bytes(client_jar, best)
        if b:
            return b, best

    return None, None


@st.cache_data(show_spinner=False)
def make_face_portrait(png_bytes: bytes, size: int = 64) -> bytes | None:
    """
    Erzeugt ein kleines Portrait aus einer Entity-Textur.
    Heuristik (robust, ohne mob-spezifische Tabellen):
    - nimmt oben links einen 16x16 Bereich (oder kleiner, falls Textur kleiner ist)
    - skaliert per NEAREST hoch, damit es "pixelig" bleibt
    """
    try:
        img = Image.open(BytesIO(png_bytes)).convert("RGBA")
        w, h = img.size
        cw = min(16, w)
        ch = min(16, h)
        crop = img.crop((0, 0, cw, ch))
        crop = crop.resize((size, size), Image.NEAREST)

        out = BytesIO()
        crop.save(out, format="PNG")
        return out.getvalue()
    except Exception:
        return None


@st.cache_data(show_spinner=False)
def resolve_mob_portrait_bytes(client_jar: str, entity_type: str | None) -> tuple[bytes | None, str | None]:
    """
    Portrait-Pipeline:
    1) spezifisches Spawn-Egg (wenn vorhanden)
    2) Entity-Textur -> Face-Crop (Fallback)
    3) generisches Spawn-Egg (Fallback)
    """
    egg = entity_type_to_spawn_egg_material(entity_type)

    # 1) spezifisches Spawn-Egg (nicht generisch)
    if egg:
        png, entry = resolve_icon_bytes_from_jar(client_jar, egg)
        if png and entry and not entry.endswith("/spawn_egg.png"):
            return png, f"specific_spawn_egg:{egg} ({entry})"

    # 2) Entity-Textur -> Face-Crop
    ent_png, ent_entry = resolve_entity_texture_bytes(client_jar, entity_type)
    if ent_png and ent_entry:
        face = make_face_portrait(ent_png, size=64)
        if face:
            return face, f"entity_face:{entity_type} ({ent_entry})"
        return ent_png, f"entity_texture:{entity_type} ({ent_entry})"

    # 3) generisches Spawn-Egg fallback
    if egg:
        png, entry = resolve_icon_bytes_from_jar(client_jar, egg)
        if png:
            return png, f"generic_spawn_egg:{egg} ({entry})"

    return None, None


def render_material_icon_line(jar_path: str | None, material: str | None, label: str, key_prefix: str) -> None:
    """
    Zeigt Item/Icon + Materialname in einer Zeile (2 Spalten).
    """
    if not material:
        st.write(f"**{label}:** —")
        return

    cols = st.columns([1, 7])
    with cols[0]:
        if jar_path:
            png, _ = resolve_icon_bytes_from_jar(jar_path, material)
            if png:
                st.image(png, width=42)
            else:
                st.write("—")
        else:
            st.write("—")

    with cols[1]:
        st.write(f"**{label}:** {material}")
        if jar_path:
            png, _ = resolve_icon_bytes_from_jar(jar_path, material)
            if not png:
                st.caption("Kein Icon gefunden (Materialname/Mapping/Version prüfen).")
        else:
            st.caption("Client-JAR nicht gefunden. Pfad/Version prüfen.")


# =============================================================================
# Normalisierte Views (DataFrames)
# =============================================================================


def mobs_df(mobs: dict[str, Any]) -> pd.DataFrame:
    rows: list[dict[str, Any]] = []
    for mob_id, obj in mobs.items():
        if not isinstance(obj, dict):
            continue
        name_raw = safe_get_str(obj, "name")
        rows.append(
            {
                "id": mob_id,
                "name": strip_mc_codes(name_raw) if name_raw else mob_id,
                "type": safe_get_str(obj, "type"),
                "boss": to_bool(obj.get("boss")),
                "health": to_float(obj.get("health")),
                "damage": to_float(obj.get("damage")),
                "xp": to_int(obj.get("xp")),
                "lootTable": safe_get_str(obj, "lootTable"),
                "skillIntervalSeconds": to_int(obj.get("skillIntervalSeconds")),
                "skills": ", ".join(to_list(obj.get("skills"))),
            }
        )
    df = pd.DataFrame(rows)
    if not df.empty:
        df["boss_sort"] = df["boss"].fillna(False).astype(bool)
        df = df.sort_values(by=["boss_sort", "type", "name"], ascending=[False, True, True]).drop(columns=["boss_sort"])
    return df


def skills_df(skills: dict[str, Any]) -> pd.DataFrame:
    rows: list[dict[str, Any]] = []
    for sid, obj in skills.items():
        if not isinstance(obj, dict):
            continue
        rows.append(
            {
                "id": sid,
                "name": safe_get_str(obj, "name") or sid,
                "type": safe_get_str(obj, "type"),
                "category": safe_get_str(obj, "category"),
                "cooldown": to_int(obj.get("cooldown")),
                "manaCost": to_int(obj.get("manaCost")),
                "effectsCount": len(obj.get("effects")) if isinstance(obj.get("effects"), list) else 0,
            }
        )
    df = pd.DataFrame(rows)
    if not df.empty:
        df = df.sort_values(by=["category", "name"], ascending=[True, True])
    return df


def quests_df(quests: dict[str, Any]) -> pd.DataFrame:
    rows: list[dict[str, Any]] = []
    for qid, obj in quests.items():
        if not isinstance(obj, dict):
            continue
        reward = obj.get("reward") if isinstance(obj.get("reward"), dict) else {}
        steps = obj.get("steps") if isinstance(obj.get("steps"), list) else []
        rows.append(
            {
                "id": qid,
                "name": safe_get_str(obj, "name") or qid,
                "minLevel": to_int(obj.get("minLevel")),
                "repeatable": to_bool(obj.get("repeatable")),
                "reward_xp": to_int(reward.get("xp")) if isinstance(reward, dict) else None,
                "reward_skillPoints": to_int(reward.get("skillPoints")) if isinstance(reward, dict) else None,
                "stepsCount": len(steps),
            }
        )
    df = pd.DataFrame(rows)
    if not df.empty:
        df = df.sort_values(by=["minLevel", "name"], ascending=[True, True])
    return df


def npcs_df(npcs: dict[str, Any]) -> pd.DataFrame:
    rows: list[dict[str, Any]] = []
    for nid, obj in npcs.items():
        if not isinstance(obj, dict):
            continue
        rows.append(
            {
                "id": nid,
                "name": safe_get_str(obj, "name") or nid,
                "role": safe_get_str(obj, "role"),
                "factionId": safe_get_str(obj, "factionId"),
                "world": safe_get_str(obj, "world"),
                "questLink": safe_get_str(obj, "questLink"),
            }
        )
    df = pd.DataFrame(rows)
    if not df.empty:
        df = df.sort_values(by=["role", "name"], ascending=[True, True])
    return df


def loot_df(loot: dict[str, Any]) -> pd.DataFrame:
    rows: list[dict[str, Any]] = []
    for lid, obj in loot.items():
        if not isinstance(obj, dict):
            continue
        entries = obj.get("entries") if isinstance(obj.get("entries"), list) else []
        rows.append(
            {
                "id": lid,
                "appliesTo": safe_get_str(obj, "appliesTo"),
                "entriesCount": len(entries),
            }
        )
    df = pd.DataFrame(rows)
    if not df.empty:
        df = df.sort_values(by=["appliesTo", "id"], ascending=[True, True])
    return df


def ench_df(ench: dict[str, Any]) -> pd.DataFrame:
    rows: list[dict[str, Any]] = []
    for eid, obj in ench.items():
        if not isinstance(obj, dict):
            continue
        rows.append(
            {
                "id": eid,
                "affix": safe_get_str(obj, "affix"),
                "type": safe_get_str(obj, "type"),
                "targetSlot": safe_get_str(obj, "targetSlot"),
                "minLevel": to_int(obj.get("minLevel")),
                "costGold": to_int(obj.get("costGold")),
                "statToImprove": safe_get_str(obj, "statToImprove"),
            }
        )
    df = pd.DataFrame(rows)
    if not df.empty:
        df = df.sort_values(by=["targetSlot", "minLevel", "affix"], ascending=[True, True, True])
    return df


def spawners_df(spawners: dict[str, Any]) -> pd.DataFrame:
    rows: list[dict[str, Any]] = []
    for sid, obj in spawners.items():
        if not isinstance(obj, dict):
            continue
        mobs_map = obj.get("mobs") if isinstance(obj.get("mobs"), dict) else {}
        rows.append(
            {
                "id": sid,
                "zoneId": safe_get_str(obj, "zoneId"),
                "maxMobs": to_int(obj.get("maxMobs")),
                "spawnInterval": to_int(obj.get("spawnInterval")),
                "mobsCount": len(mobs_map),
            }
        )
    df = pd.DataFrame(rows)
    if not df.empty:
        df = df.sort_values(by=["zoneId", "id"], ascending=[True, True])
    return df


# =============================================================================
# UI building blocks
# =============================================================================


def selection_from_df(df: pd.DataFrame, label_col: str, key_prefix: str) -> str:
    labels = [f"{row[label_col]}  —  ({row['id']})" for _, row in df.iterrows()]
    ids = df["id"].tolist()
    chosen_label = st.selectbox(
        "Auswahl",
        options=labels,
        index=0,
        key=wkey(key_prefix, "selectbox", "choice"),
    )
    pos = labels.index(chosen_label)
    return ids[pos]


def render_kv(label: str, value: Any, suffix: str = "") -> None:
    if value is None or (isinstance(value, str) and not value.strip()):
        st.write(f"**{label}:** —")
    else:
        st.write(f"**{label}:** {value}{suffix}")


def render_raw_expander(obj: Any) -> None:
    with st.expander("Rohdaten (YAML → JSON)", expanded=False):
        st.code(as_pretty_json(obj), language="json")


# =============================================================================
# Power-Editor: Datensatz editieren + speichern + live refresh
# =============================================================================


def render_record_yaml_editor(
    *,
    title: str,
    file_path: str,
    root_mapping: dict[str, Any],
    record_id: str,
    key_prefix: str,
) -> None:
    obj = root_mapping.get(record_id)

    with st.expander(f"{title} bearbeiten (YAML)", expanded=False):
        if obj is None:
            st.warning("Datensatz nicht gefunden.")
            return

        yaml_text = yaml.safe_dump(
            obj,
            allow_unicode=True,
            sort_keys=False,
            default_flow_style=False,
            width=120,
        )

        edited = st.text_area(
            "YAML dieses Datensatzes (nur Inhalt, ohne ID-Key):",
            value=yaml_text,
            height=360,
            key=wkey(key_prefix, record_id, "editor_textarea"),
        )

        cols = st.columns([1, 1, 4])
        with cols[0]:
            do_save = st.button("Speichern", key=wkey(key_prefix, record_id, "save_btn"))
        with cols[1]:
            do_reset = st.button("Zurücksetzen", key=wkey(key_prefix, record_id, "reset_btn"))

        if do_reset:
            st.cache_data.clear()
            st.rerun()

        if do_save:
            try:
                parsed = yaml.safe_load(edited)
            except Exception as e:
                st.error(f"YAML-Parse-Fehler: {e}")
                return

            if not isinstance(parsed, dict):
                st.error("Ungültig: Der Datensatz muss ein YAML-Mapping (dict) sein.")
                return

            root_mapping[record_id] = parsed

            try:
                save_yaml_mapping(file_path, root_mapping, make_backup=True)
            except Exception as e:
                st.error(f"Speicherfehler: {e}")
                return

            st.success("Gespeichert. Cache wird geleert und Seite neu geladen …")
            st.cache_data.clear()
            st.rerun()


# =============================================================================
# Detail Renderers
# =============================================================================


def render_mob_detail(
    mob_id: str,
    mobs: dict[str, Any],
    skills: dict[str, Any],
    loot: dict[str, Any],
    idx: Indexes,
    client_jar: str | None,
) -> None:
    obj = mobs.get(mob_id, {})
    if not isinstance(obj, dict):
        st.error("Mob-Daten sind nicht im erwarteten Format.")
        return

    name_raw = safe_get_str(obj, "name")
    name = strip_mc_codes(name_raw) if name_raw else mob_id

    # --- Header mit Mob-Portrait ---
    hcols = st.columns([1, 8])
    with hcols[0]:
        if client_jar:
            portrait, hint = resolve_mob_portrait_bytes(client_jar, safe_get_str(obj, "type"))
            if portrait:
                st.image(portrait, width=64)
            else:
                st.write("—")
                et = safe_get_str(obj, "type")
                egg = entity_type_to_spawn_egg_material(et)
                st.caption(f"Kein Portrait gefunden. type={et}, egg={egg}, hint={hint}")
        else:
            st.write("—")
    with hcols[1]:
        st.title(name)
        st.caption(f"Mob-ID: `{mob_id}`")

    cols = st.columns(3)
    with cols[0]:
        render_kv("Typ", safe_get_str(obj, "type"))
        boss = to_bool(obj.get("boss"))
        render_kv("Boss", "Ja" if boss else ("Nein" if boss is not None else None))
    with cols[1]:
        render_kv("Health", to_float(obj.get("health")))
        render_kv("Damage", to_float(obj.get("damage")))
    with cols[2]:
        render_kv("XP", to_int(obj.get("xp")))
        render_kv("LootTable", safe_get_str(obj, "lootTable"))

    st.subheader("Ausrüstung (mit Icons)")
    eq_cols = st.columns(2)
    with eq_cols[0]:
        render_material_icon_line(client_jar, safe_get_str(obj, "mainHand"), "MainHand", wkey("mob", mob_id, "mainHand"))
        render_material_icon_line(client_jar, safe_get_str(obj, "offHand"), "OffHand", wkey("mob", mob_id, "offHand"))
        render_material_icon_line(client_jar, safe_get_str(obj, "helmet"), "Helmet", wkey("mob", mob_id, "helmet"))
    with eq_cols[1]:
        render_material_icon_line(
            client_jar, safe_get_str(obj, "chestplate"), "Chestplate", wkey("mob", mob_id, "chestplate")
        )
        render_material_icon_line(client_jar, safe_get_str(obj, "leggings"), "Leggings", wkey("mob", mob_id, "leggings"))
        render_material_icon_line(client_jar, safe_get_str(obj, "boots"), "Boots", wkey("mob", mob_id, "boots"))

    st.subheader("Skills")
    mob_skills = to_list(obj.get("skills"))
    render_kv("Skill-Intervall", to_int(obj.get("skillIntervalSeconds")), " s")

    if mob_skills:
        st.write(", ".join(mob_skills))
        with st.expander("Skill-Details anzeigen", expanded=False):
            for sid in mob_skills:
                s_obj = skills.get(sid)
                if not isinstance(s_obj, dict):
                    st.warning(f"Skill `{sid}` nicht gefunden.")
                    continue
                st.markdown(f"**{safe_get_str(s_obj, 'name') or sid}** (`{sid}`)")
                s_cols = st.columns(4)
                with s_cols[0]:
                    render_kv("Type", safe_get_str(s_obj, "type"))
                with s_cols[1]:
                    render_kv("Category", safe_get_str(s_obj, "category"))
                with s_cols[2]:
                    render_kv("Cooldown", to_int(s_obj.get("cooldown")), " s")
                with s_cols[3]:
                    render_kv("Mana", to_int(s_obj.get("manaCost")))
                effects = s_obj.get("effects")
                if isinstance(effects, list) and effects:
                    st.write("Effects:")
                    st.code(as_pretty_json(effects), language="json")
                st.divider()
    else:
        st.write("—")

    st.subheader("Loot (mit Icons)")
    lt = safe_get_str(obj, "lootTable")
    if lt and lt in loot and isinstance(loot[lt], dict):
        lt_obj = loot[lt]
        render_kv("LootTable-ID", lt)
        render_kv("AppliesTo", safe_get_str(lt_obj, "appliesTo"))

        entries = lt_obj.get("entries") if isinstance(lt_obj.get("entries"), list) else []
        if entries:
            for e in entries:
                if not isinstance(e, dict):
                    continue
                mat = safe_get_str(e, "material")
                cols2 = st.columns([1, 5, 2, 2, 2])
                with cols2[0]:
                    if client_jar and mat:
                        png, _ = resolve_icon_bytes_from_jar(client_jar, mat)
                        if png:
                            st.image(png, width=34)
                        else:
                            st.write("—")
                    else:
                        st.write("—")
                with cols2[1]:
                    st.write(mat or "—")
                with cols2[2]:
                    st.write(f"Chance: {to_float(e.get('chance'))}")
                with cols2[3]:
                    st.write(f"Min: {to_int(e.get('minAmount'))}")
                with cols2[4]:
                    st.write(f"Max: {to_int(e.get('maxAmount'))}")
        else:
            st.write("Keine Einträge.")

        render_raw_expander(lt_obj)
    else:
        st.write("—")

    st.subheader("Spawner, die diesen Mob nutzen")
    sp_list = idx.mob_to_spawners.get(mob_id, [])
    if sp_list:
        for sp_id in sp_list:
            st.markdown(f"- `{sp_id}`")
    else:
        st.write("—")

    if name_raw and name_raw != name:
        with st.expander("Original-Name (mit Minecraft-Codes)", expanded=False):
            st.code(name_raw, language="text")

    render_raw_expander(obj)


def render_skill_detail(skill_id: str, skills: dict[str, Any]) -> None:
    obj = skills.get(skill_id, {})
    if not isinstance(obj, dict):
        st.error("Skill-Daten sind nicht im erwarteten Format.")
        return

    st.title(safe_get_str(obj, "name") or skill_id)
    st.caption(f"Skill-ID: `{skill_id}`")

    cols = st.columns(4)
    with cols[0]:
        render_kv("Type", safe_get_str(obj, "type"))
    with cols[1]:
        render_kv("Category", safe_get_str(obj, "category"))
    with cols[2]:
        render_kv("Cooldown", to_int(obj.get("cooldown")), " s")
    with cols[3]:
        render_kv("ManaCost", to_int(obj.get("manaCost")))

    st.subheader("Effects")
    effects = obj.get("effects")
    if isinstance(effects, list) and effects:
        for i, e in enumerate(effects, start=1):
            st.markdown(f"**Effect {i}**")
            st.code(as_pretty_json(e), language="json")
    else:
        st.write("—")

    render_raw_expander(obj)


def render_quest_detail(quest_id: str, quests: dict[str, Any]) -> None:
    obj = quests.get(quest_id, {})
    if not isinstance(obj, dict):
        st.error("Quest-Daten sind nicht im erwarteten Format.")
        return

    st.title(safe_get_str(obj, "name") or quest_id)
    st.caption(f"Quest-ID: `{quest_id}`")

    cols = st.columns(3)
    with cols[0]:
        render_kv("MinLevel", to_int(obj.get("minLevel")))
    with cols[1]:
        rep = to_bool(obj.get("repeatable"))
        render_kv("Repeatable", "Ja" if rep else ("Nein" if rep is not None else None))
    with cols[2]:
        render_kv("Beschreibung", safe_get_str(obj, "description"))

    st.subheader("Reward")
    reward = obj.get("reward")
    if isinstance(reward, dict):
        r_cols = st.columns(4)
        with r_cols[0]:
            render_kv("XP", to_int(reward.get("xp")))
        with r_cols[1]:
            render_kv("SkillPoints", to_int(reward.get("skillPoints")))
        extras = {k: v for k, v in reward.items() if k not in {"xp", "skillPoints"}}
        if extras:
            with st.expander("Weitere Reward-Felder", expanded=False):
                st.code(as_pretty_json(extras), language="json")
    else:
        st.write("—")

    st.subheader("Steps")
    steps = obj.get("steps")
    if isinstance(steps, list) and steps:
        rows = []
        for s in steps:
            if not isinstance(s, dict):
                continue
            rows.append(
                {
                    "type": safe_get_str(s, "type"),
                    "target": safe_get_str(s, "target"),
                    "amount": to_int(s.get("amount")),
                }
            )
        st.dataframe(pd.DataFrame(rows), use_container_width=True, hide_index=True)
    else:
        st.write("—")

    render_raw_expander(obj)


def render_npc_detail(npc_id: str, npcs: dict[str, Any], quests: dict[str, Any], idx: Indexes) -> None:
    obj = npcs.get(npc_id, {})
    if not isinstance(obj, dict):
        st.error("NPC-Daten sind nicht im erwarteten Format.")
        return

    st.title(safe_get_str(obj, "name") or npc_id)
    st.caption(f"NPC-ID: `{npc_id}`")

    cols = st.columns(3)
    with cols[0]:
        render_kv("Role", safe_get_str(obj, "role"))
        render_kv("Faction", safe_get_str(obj, "factionId"))
    with cols[1]:
        render_kv("World", safe_get_str(obj, "world"))
        render_kv("QuestLink", safe_get_str(obj, "questLink"))
    with cols[2]:
        render_kv("Position", f"x={obj.get('x')}, y={obj.get('y')}, z={obj.get('z')}")
        render_kv("Yaw/Pitch", f"yaw={obj.get('yaw')}, pitch={obj.get('pitch')}")

    st.subheader("Dialog")
    dialog = obj.get("dialog")
    if isinstance(dialog, list) and dialog:
        for line in dialog:
            st.write(f"- {line}")
    else:
        st.write("—")

    st.subheader("Referenzierte Quests")
    qrefs = idx.npc_to_quests.get(npc_id, [])
    if qrefs:
        for qid in qrefs:
            if qid in quests and isinstance(quests[qid], dict):
                st.markdown(f"- `{qid}`: **{quests[qid].get('name', qid)}**")
            else:
                st.markdown(f"- `{qid}` (nicht in quests.yml gefunden)")
    else:
        st.write("—")

    render_raw_expander(obj)


def render_loot_detail(loot_id: str, loot: dict[str, Any], client_jar: str | None) -> None:
    obj = loot.get(loot_id, {})
    if not isinstance(obj, dict):
        st.error("Loot-Daten sind nicht im erwarteten Format.")
        return

    st.title(f"LootTable `{loot_id}`")
    render_kv("AppliesTo", safe_get_str(obj, "appliesTo"))

    entries = obj.get("entries")
    if isinstance(entries, list) and entries:
        for e in entries:
            if not isinstance(e, dict):
                continue
            mat = safe_get_str(e, "material")
            cols2 = st.columns([1, 5, 2, 2, 2])
            with cols2[0]:
                if client_jar and mat:
                    png, _ = resolve_icon_bytes_from_jar(client_jar, mat)
                    if png:
                        st.image(png, width=34)
                    else:
                        st.write("—")
                else:
                    st.write("—")
            with cols2[1]:
                st.write(mat or "—")
            with cols2[2]:
                st.write(f"Chance: {to_float(e.get('chance'))}")
            with cols2[3]:
                st.write(f"Min: {to_int(e.get('minAmount'))}")
            with cols2[4]:
                st.write(f"Max: {to_int(e.get('maxAmount'))}")
    else:
        st.write("Keine Einträge.")

    render_raw_expander(obj)


def render_enchant_detail(eid: str, ench: dict[str, Any]) -> None:
    obj = ench.get(eid, {})
    if not isinstance(obj, dict):
        st.error("Enchant-Daten sind nicht im erwarteten Format.")
        return

    st.title(f"Enchant `{eid}`")
    cols = st.columns(3)
    with cols[0]:
        render_kv("Affix", safe_get_str(obj, "affix"))
        render_kv("Type", safe_get_str(obj, "type"))
    with cols[1]:
        render_kv("TargetSlot", safe_get_str(obj, "targetSlot"))
        render_kv("MinLevel", to_int(obj.get("minLevel")))
    with cols[2]:
        render_kv("CostGold", to_int(obj.get("costGold")))
        render_kv("StatToImprove", safe_get_str(obj, "statToImprove"))

    st.subheader("Effects")
    effects = obj.get("effects")
    if isinstance(effects, list) and effects:
        st.code(as_pretty_json(effects), language="json")
    else:
        st.write("—")

    render_raw_expander(obj)


def render_spawner_detail(spawner_id: str, spawners: dict[str, Any], mobs: dict[str, Any]) -> None:
    obj = spawners.get(spawner_id, {})
    if not isinstance(obj, dict):
        st.error("Spawner-Daten sind nicht im erwarteten Format.")
        return

    st.title(f"Spawner `{spawner_id}`")
    cols = st.columns(3)
    with cols[0]:
        render_kv("ZoneId", safe_get_str(obj, "zoneId"))
    with cols[1]:
        render_kv("MaxMobs", to_int(obj.get("maxMobs")))
    with cols[2]:
        render_kv("SpawnInterval", to_int(obj.get("spawnInterval")))

    st.subheader("Mobs (Gewichte)")
    mobs_map = obj.get("mobs")
    if isinstance(mobs_map, dict) and mobs_map:
        rows = []
        for mid, w in mobs_map.items():
            mobj = mobs.get(str(mid))
            mname = None
            if isinstance(mobj, dict):
                mname = strip_mc_codes(safe_get_str(mobj, "name") or str(mid))
            rows.append(
                {
                    "mob_id": str(mid),
                    "mob_name": mname or str(mid),
                    "weight": to_float(w),
                }
            )
        df = pd.DataFrame(rows).sort_values(by=["weight", "mob_name"], ascending=[False, True])
        st.dataframe(df, use_container_width=True, hide_index=True)
    else:
        st.write("—")

    render_raw_expander(obj)


# =============================================================================
# Icon-Check (Trefferquote) – optional, aber sehr hilfreich beim Debuggen
# =============================================================================


@st.cache_data(show_spinner=False)
def icon_hit_rate_for_materials(client_jar: str, materials: list[str]) -> tuple[int, int]:
    hits = 0
    for m in materials:
        png, _ = resolve_icon_bytes_from_jar(client_jar, m)
        if png:
            hits += 1
    return hits, len(materials)


def collect_materials_from_yaml(all_data: dict[str, dict[str, Any]]) -> list[str]:
    mats: set[str] = set()

    mobs = all_data.get("mobs", {})
    loot = all_data.get("loot", {})

    for _, obj in mobs.items():
        if not isinstance(obj, dict):
            continue
        for k in ["mainHand", "offHand", "helmet", "chestplate", "leggings", "boots"]:
            v = safe_get_str(obj, k)
            if v:
                mats.add(v)

    for _, obj in loot.items():
        if not isinstance(obj, dict):
            continue
        entries = obj.get("entries")
        if isinstance(entries, list):
            for e in entries:
                if not isinstance(e, dict):
                    continue
                v = safe_get_str(e, "material")
                if v:
                    mats.add(v)

    return sorted(mats)


# =============================================================================
# Main App
# =============================================================================


def main() -> None:
    st.set_page_config(page_title="RPG YAML Dashboard (JAR-Icons + Edit)", layout="wide")

    # ---------------- Sidebar: Projekt & Pfade ----------------
    st.sidebar.header("Projekt & Pfade")

    project_root = st.sidebar.text_input(
        "Projekt-Root (Ordner mit launcher-config.json)",
        value=str(Path.cwd()),
        key=wkey("sidebar", "project_root"),
    )

    cfg = read_launcher_config(project_root)

    client_version = st.sidebar.text_input(
        "Client-Version (für Icons)",
        value=guess_client_version(cfg),
        key=wkey("sidebar", "client_version"),
    )

    client_root = st.sidebar.text_input(
        "Client-Ordner (CmlLib: client_files)",
        value=str(Path(project_root) / "client_files"),
        key=wkey("sidebar", "client_root"),
    )

    client_jar = find_client_jar(client_root, client_version)
    st.sidebar.caption(f"Client-JAR: {client_jar or 'nicht gefunden'}")

    st.sidebar.divider()
    st.sidebar.header("Server & Plugin")

    install_root = guess_install_root(cfg)
    server_name = guess_server_name(cfg)

    server_root = st.sidebar.text_input(
        "Server-Root (Ordner mit server.jar)",
        value=str(Path(project_root) / install_root / server_name),
        key=wkey("sidebar", "server_root"),
    )

    plugin_root = st.sidebar.text_input(
        r"Plugin-Root (z.B. <server>\plugins\MineLauncherRPG)",
        value=str(Path(server_root) / "plugins" / "MineLauncherRPG"),
        key=wkey("sidebar", "plugin_root"),
    )

    files = {
        "mobs": str(Path(plugin_root) / "mobs.yml"),
        "skills": str(Path(plugin_root) / "skills.yml"),
        "quests": str(Path(plugin_root) / "quests.yml"),
        "npcs": str(Path(plugin_root) / "npcs.yml"),
        "loot": str(Path(plugin_root) / "loot.yml"),
        "enchantments": str(Path(plugin_root) / "enchantments.yml"),
        "spawners": str(Path(plugin_root) / "spawners.yml"),
    }

    st.sidebar.divider()
    if st.sidebar.button("Cache leeren / neu laden", key=wkey("sidebar", "clear_cache")):
        st.cache_data.clear()
        st.rerun()

    # ---------------- Load YAMLs ----------------
    try:
        all_data = load_all(files)
    except Exception as e:
        st.error(f"Fehler beim Laden: {e}")
        st.stop()

    idx = build_indexes(all_data)

    mobs = all_data["mobs"]
    skills = all_data["skills"]
    quests = all_data["quests"]
    npcs = all_data["npcs"]
    loot = all_data["loot"]
    ench = all_data["enchantments"]
    spawners = all_data["spawners"]

    # ---------------- Overview ----------------
    st.title("RPG YAML Dashboard (mit Item-Icons aus Client-JAR) + Edit/Save")

    mcols = st.columns(7)
    mcols[0].metric("Mobs", f"{len(mobs):,}")
    mcols[1].metric("Skills", f"{len(skills):,}")
    mcols[2].metric("Quests", f"{len(quests):,}")
    mcols[3].metric("NPCs", f"{len(npcs):,}")
    mcols[4].metric("LootTables", f"{len(loot):,}")
    mcols[5].metric("Enchantments", f"{len(ench):,}")
    mcols[6].metric("Spawners", f"{len(spawners):,}")

    # Trefferquote-Check (optional)
    with st.expander("Icon-Check (Trefferquote)", expanded=False):
        if not client_jar:
            st.warning("Kein Client-JAR gefunden. Icons können nicht geladen werden.")
        else:
            mats = collect_materials_from_yaml(all_data)
            hits, total = icon_hit_rate_for_materials(client_jar, mats)
            pct = (hits / total * 100.0) if total else 0.0
            st.write(f"Materialien mit potentiellen Icons: **{total}**")
            st.write(f"Icons gefunden: **{hits}**")
            st.write(f"Trefferquote: **{pct:.1f}%**")
            with st.expander("Material-Liste"):
                st.code("\n".join(mats), language="text")

    tabs = st.tabs(["Mobs", "Skills", "Quests", "NPCs", "Loot", "Enchantments", "Spawners"])

    # ---------------- MOBS ----------------
    with tabs[0]:
        df = mobs_df(mobs)
        if df.empty:
            st.info("Keine Mobs geladen.")
        else:
            fcols = st.columns(5)
            with fcols[0]:
                type_opt = ["(Alle)"] + unique_sorted(df["type"].dropna().unique())
                tsel = st.selectbox("Type", options=type_opt, index=0, key=wkey("mobs", "filter", "type"))
            with fcols[1]:
                boss_opt = ["(Alle)", "Nur Boss", "Keine Boss"]
                bsel = st.selectbox("Boss", options=boss_opt, index=0, key=wkey("mobs", "filter", "boss"))
            with fcols[2]:
                lt_opt = ["(Alle)"] + unique_sorted(df["lootTable"].dropna().unique())
                ltsel = st.selectbox("LootTable", options=lt_opt, index=0, key=wkey("mobs", "filter", "loottable"))
            with fcols[3]:
                q = st.text_input("Suche (Name/ID enthält)", value="", key=wkey("mobs", "filter", "search")).strip().lower()
            with fcols[4]:
                min_xp = st.number_input("Min XP", value=0, step=10, key=wkey("mobs", "filter", "min_xp"))

            filtered = df.copy()
            if tsel != "(Alle)":
                filtered = filtered[filtered["type"] == tsel]
            if bsel == "Nur Boss":
                filtered = filtered[filtered["boss"] == True]  # noqa: E712
            elif bsel == "Keine Boss":
                filtered = filtered[filtered["boss"] == False]  # noqa: E712
            if ltsel != "(Alle)":
                filtered = filtered[filtered["lootTable"] == ltsel]
            if q:
                filtered = filtered[
                    filtered["id"].str.lower().str.contains(q, na=False) | filtered["name"].str.lower().str.contains(q, na=False)
                ]
            filtered = filtered[filtered["xp"].fillna(0) >= int(min_xp)]

            st.write(f"**Treffer:** {len(filtered):,} / {len(df):,}")

            if filtered.empty:
                st.info("Keine Treffer.")
            else:
                selected_id = selection_from_df(filtered, label_col="name", key_prefix="mobs")
                st.divider()

                render_mob_detail(selected_id, mobs, skills, loot, idx, client_jar)

                render_record_yaml_editor(
                    title="Mob",
                    file_path=files["mobs"],
                    root_mapping=mobs,
                    record_id=selected_id,
                    key_prefix="edit_mob",
                )

                with st.expander("Trefferliste (Tabelle)", expanded=False):
                    st.dataframe(filtered, use_container_width=True, hide_index=True)

    # ---------------- SKILLS ----------------
    with tabs[1]:
        df = skills_df(skills)
        if df.empty:
            st.info("Keine Skills geladen.")
        else:
            fcols = st.columns(3)
            with fcols[0]:
                cat_opt = ["(Alle)"] + unique_sorted(df["category"].dropna().unique())
                csel = st.selectbox("Category", options=cat_opt, index=0, key=wkey("skills", "filter", "category"))
            with fcols[1]:
                t_opt = ["(Alle)"] + unique_sorted(df["type"].dropna().unique())
                tsel = st.selectbox("Type", options=t_opt, index=0, key=wkey("skills", "filter", "type"))
            with fcols[2]:
                q = st.text_input("Suche (Name/ID enthält)", value="", key=wkey("skills", "filter", "search")).strip().lower()

            filtered = df.copy()
            if csel != "(Alle)":
                filtered = filtered[filtered["category"] == csel]
            if tsel != "(Alle)":
                filtered = filtered[filtered["type"] == tsel]
            if q:
                filtered = filtered[
                    filtered["id"].str.lower().str.contains(q, na=False) | filtered["name"].str.lower().str.contains(q, na=False)
                ]

            st.write(f"**Treffer:** {len(filtered):,} / {len(df):,}")
            if filtered.empty:
                st.info("Keine Treffer.")
            else:
                sid = selection_from_df(filtered, label_col="name", key_prefix="skills")
                st.divider()

                render_skill_detail(sid, skills)

                render_record_yaml_editor(
                    title="Skill",
                    file_path=files["skills"],
                    root_mapping=skills,
                    record_id=sid,
                    key_prefix="edit_skill",
                )

                with st.expander("Trefferliste (Tabelle)", expanded=False):
                    st.dataframe(filtered, use_container_width=True, hide_index=True)

    # ---------------- QUESTS ----------------
    with tabs[2]:
        df = quests_df(quests)
        if df.empty:
            st.info("Keine Quests geladen.")
        else:
            fcols = st.columns(3)
            with fcols[0]:
                min_lvl = st.number_input("MinLevel ≥", value=0, step=1, key=wkey("quests", "filter", "min_level"))
            with fcols[1]:
                rep_opt = ["(Alle)", "Repeatable", "Nicht repeatable"]
                rsel = st.selectbox("Repeatable", options=rep_opt, index=0, key=wkey("quests", "filter", "repeatable"))
            with fcols[2]:
                q = st.text_input("Suche (Name/ID enthält)", value="", key=wkey("quests", "filter", "search")).strip().lower()

            filtered = df.copy()
            filtered = filtered[filtered["minLevel"].fillna(0) >= int(min_lvl)]
            if rsel == "Repeatable":
                filtered = filtered[filtered["repeatable"] == True]  # noqa: E712
            elif rsel == "Nicht repeatable":
                filtered = filtered[filtered["repeatable"] == False]  # noqa: E712
            if q:
                filtered = filtered[
                    filtered["id"].str.lower().str.contains(q, na=False) | filtered["name"].str.lower().str.contains(q, na=False)
                ]

            st.write(f"**Treffer:** {len(filtered):,} / {len(df):,}")
            if filtered.empty:
                st.info("Keine Treffer.")
            else:
                qid = selection_from_df(filtered, label_col="name", key_prefix="quests")
                st.divider()

                render_quest_detail(qid, quests)

                render_record_yaml_editor(
                    title="Quest",
                    file_path=files["quests"],
                    root_mapping=quests,
                    record_id=qid,
                    key_prefix="edit_quest",
                )

                with st.expander("Trefferliste (Tabelle)", expanded=False):
                    st.dataframe(filtered, use_container_width=True, hide_index=True)

    # ---------------- NPCS ----------------
    with tabs[3]:
        df = npcs_df(npcs)
        if df.empty:
            st.info("Keine NPCs geladen.")
        else:
            fcols = st.columns(3)
            with fcols[0]:
                role_opt = ["(Alle)"] + unique_sorted(df["role"].dropna().unique())
                rsel = st.selectbox("Role", options=role_opt, index=0, key=wkey("npcs", "filter", "role"))
            with fcols[1]:
                fac_opt = ["(Alle)"] + unique_sorted(df["factionId"].dropna().unique())
                fsel = st.selectbox("Faction", options=fac_opt, index=0, key=wkey("npcs", "filter", "faction"))
            with fcols[2]:
                q = st.text_input("Suche (Name/ID enthält)", value="", key=wkey("npcs", "filter", "search")).strip().lower()

            filtered = df.copy()
            if rsel != "(Alle)":
                filtered = filtered[filtered["role"] == rsel]
            if fsel != "(Alle)":
                filtered = filtered[filtered["factionId"] == fsel]
            if q:
                filtered = filtered[
                    filtered["id"].str.lower().str.contains(q, na=False) | filtered["name"].str.lower().str.contains(q, na=False)
                ]

            st.write(f"**Treffer:** {len(filtered):,} / {len(df):,}")
            if filtered.empty:
                st.info("Keine Treffer.")
            else:
                nid = selection_from_df(filtered, label_col="name", key_prefix="npcs")
                st.divider()

                render_npc_detail(nid, npcs, quests, idx)

                render_record_yaml_editor(
                    title="NPC",
                    file_path=files["npcs"],
                    root_mapping=npcs,
                    record_id=nid,
                    key_prefix="edit_npc",
                )

                with st.expander("Trefferliste (Tabelle)", expanded=False):
                    st.dataframe(filtered, use_container_width=True, hide_index=True)

    # ---------------- LOOT ----------------
    with tabs[4]:
        df = loot_df(loot)
        if df.empty:
            st.info("Keine LootTables geladen.")
        else:
            fcols = st.columns(2)
            with fcols[0]:
                app_opt = ["(Alle)"] + unique_sorted(df["appliesTo"].dropna().unique())
                asel = st.selectbox("AppliesTo", options=app_opt, index=0, key=wkey("loot", "filter", "applies_to"))
            with fcols[1]:
                q = st.text_input("Suche (ID enthält)", value="", key=wkey("loot", "filter", "search")).strip().lower()

            filtered = df.copy()
            if asel != "(Alle)":
                filtered = filtered[filtered["appliesTo"] == asel]
            if q:
                filtered = filtered[filtered["id"].str.lower().str.contains(q, na=False)]

            st.write(f"**Treffer:** {len(filtered):,} / {len(df):,}")
            if filtered.empty:
                st.info("Keine Treffer.")
            else:
                lid = st.selectbox(
                    "LootTable auswählen",
                    options=filtered["id"].tolist(),
                    index=0,
                    key=wkey("loot", "select", "loottable"),
                )
                st.divider()

                render_loot_detail(lid, loot, client_jar)

                render_record_yaml_editor(
                    title="LootTable",
                    file_path=files["loot"],
                    root_mapping=loot,
                    record_id=lid,
                    key_prefix="edit_loot",
                )

                with st.expander("Trefferliste (Tabelle)", expanded=False):
                    st.dataframe(filtered, use_container_width=True, hide_index=True)

    # ---------------- ENCHANTMENTS ----------------
    with tabs[5]:
        df = ench_df(ench)
        if df.empty:
            st.info("Keine Enchantments geladen.")
        else:
            fcols = st.columns(3)
            with fcols[0]:
                slot_opt = ["(Alle)"] + unique_sorted(df["targetSlot"].dropna().unique())
                ssel = st.selectbox("TargetSlot", options=slot_opt, index=0, key=wkey("enchantments", "filter", "target_slot"))
            with fcols[1]:
                typ_opt = ["(Alle)"] + unique_sorted(df["type"].dropna().unique())
                tsel = st.selectbox("Type", options=typ_opt, index=0, key=wkey("enchantments", "filter", "type"))
            with fcols[2]:
                q = st.text_input("Suche (Affix/ID enthält)", value="", key=wkey("enchantments", "filter", "search")).strip().lower()

            filtered = df.copy()
            if ssel != "(Alle)":
                filtered = filtered[filtered["targetSlot"] == ssel]
            if tsel != "(Alle)":
                filtered = filtered[filtered["type"] == tsel]
            if q:
                filtered = filtered[
                    filtered["id"].str.lower().str.contains(q, na=False)
                    | filtered["affix"].fillna("").str.lower().str.contains(q, na=False)
                ]

            st.write(f"**Treffer:** {len(filtered):,} / {len(df):,}")
            if filtered.empty:
                st.info("Keine Treffer.")
            else:
                eid = st.selectbox(
                    "Enchant auswählen",
                    options=filtered["id"].tolist(),
                    index=0,
                    key=wkey("enchantments", "select", "enchant"),
                )
                st.divider()

                render_enchant_detail(eid, ench)

                render_record_yaml_editor(
                    title="Enchantment",
                    file_path=files["enchantments"],
                    root_mapping=ench,
                    record_id=eid,
                    key_prefix="edit_enchant",
                )

                with st.expander("Trefferliste (Tabelle)", expanded=False):
                    st.dataframe(filtered, use_container_width=True, hide_index=True)

    # ---------------- SPAWNERS ----------------
    with tabs[6]:
        df = spawners_df(spawners)
        if df.empty:
            st.info("Keine Spawner geladen.")
        else:
            fcols = st.columns(2)
            with fcols[0]:
                zone_opt = ["(Alle)"] + unique_sorted(df["zoneId"].dropna().unique())
                zsel = st.selectbox("ZoneId", options=zone_opt, index=0, key=wkey("spawners", "filter", "zone"))
            with fcols[1]:
                q = st.text_input("Suche (ID enthält)", value="", key=wkey("spawners", "filter", "search")).strip().lower()

            filtered = df.copy()
            if zsel != "(Alle)":
                filtered = filtered[filtered["zoneId"] == zsel]
            if q:
                filtered = filtered[filtered["id"].str.lower().str.contains(q, na=False)]

            st.write(f"**Treffer:** {len(filtered):,} / {len(df):,}")
            if filtered.empty:
                st.info("Keine Treffer.")
            else:
                spid = st.selectbox(
                    "Spawner auswählen",
                    options=filtered["id"].tolist(),
                    index=0,
                    key=wkey("spawners", "select", "spawner"),
                )
                st.divider()

                render_spawner_detail(spid, spawners, mobs)

                render_record_yaml_editor(
                    title="Spawner",
                    file_path=files["spawners"],
                    root_mapping=spawners,
                    record_id=spid,
                    key_prefix="edit_spawner",
                )

                with st.expander("Trefferliste (Tabelle)", expanded=False):
                    st.dataframe(filtered, use_container_width=True, hide_index=True)


if __name__ == "__main__":
    main()
