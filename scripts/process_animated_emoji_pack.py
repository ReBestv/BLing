from __future__ import annotations

import argparse
import json
import math
import shutil
import subprocess
import tempfile
from pathlib import Path

from PIL import Image, ImageDraw, ImageFont


def parse_args() -> argparse.Namespace:
    parser = argparse.ArgumentParser(
        description="Convert transparent MOV emoji clips into animated 512x512 WebP stickers."
    )
    parser.add_argument("--ffmpeg", required=True, help="Absolute path to ffmpeg.exe")
    parser.add_argument("--input-dir", required=True, help="Directory containing source MOV files")
    parser.add_argument("--config", required=True, help="JSON config describing stickers to process")
    parser.add_argument("--output-dir", required=True, help="Directory for animated WebP outputs")
    parser.add_argument("--theme-json", required=True, help="Output path for the generated theme JSON")
    parser.add_argument("--summary-json", required=True, help="Output path for the conversion summary JSON")
    parser.add_argument("--preview", required=True, help="Output path for the contact-sheet preview PNG")
    parser.add_argument("--fps", type=int, default=12, help="Target animation FPS for extracted frames")
    parser.add_argument("--size", type=int, default=512, help="Square output size in pixels")
    parser.add_argument(
        "--quality",
        type=int,
        default=92,
        help="Animated WebP quality from 0-100",
    )
    return parser.parse_args()


def load_json(path: Path) -> dict:
    with path.open("r", encoding="utf-8") as fh:
        return json.load(fh)


def ensure_dir(path: Path) -> None:
    path.mkdir(parents=True, exist_ok=True)


def extract_frames(
    ffmpeg: Path,
    source_path: Path,
    temp_dir: Path,
    fps: int,
    size: int,
) -> list[Path]:
    frame_pattern = temp_dir / "frame_%04d.png"
    filter_graph = (
        f"fps={fps},"
        f"scale={size}:{size}:force_original_aspect_ratio=decrease:flags=lanczos,"
        f"pad={size}:{size}:(ow-iw)/2:(oh-ih)/2:color=0x00000000"
    )
    command = [
        str(ffmpeg),
        "-y",
        "-hide_banner",
        "-loglevel",
        "error",
        "-i",
        str(source_path),
        "-vf",
        filter_graph,
        str(frame_pattern),
    ]
    subprocess.run(command, check=True)
    return sorted(temp_dir.glob("frame_*.png"))


def save_animated_webp(
    frame_paths: list[Path],
    output_path: Path,
    duration_ms: int,
    quality: int,
) -> tuple[int, tuple[int, int]]:
    frames: list[Image.Image] = []
    for frame_path in frame_paths:
        with Image.open(frame_path) as frame:
            frames.append(frame.convert("RGBA").copy())

    if not frames:
        raise ValueError(f"No frames extracted for {output_path.name}")

    frames[0].save(
        output_path,
        format="WEBP",
        save_all=True,
        append_images=frames[1:],
        duration=duration_ms,
        loop=0,
        quality=quality,
        method=6,
    )
    size = frames[0].size
    for frame in frames:
        frame.close()
    return len(frame_paths), size


def load_font(size: int) -> ImageFont.FreeTypeFont | ImageFont.ImageFont:
    candidates = [
        Path("C:/Windows/Fonts/msyh.ttc"),
        Path("C:/Windows/Fonts/msyhbd.ttc"),
        Path("C:/Windows/Fonts/simhei.ttf"),
    ]
    for candidate in candidates:
        if candidate.exists():
            return ImageFont.truetype(str(candidate), size=size)
    return ImageFont.load_default()


def build_preview(preview_items: list[dict], output_path: Path) -> None:
    columns = 4
    cell_width = 260
    cell_height = 288
    padding = 24
    rows = math.ceil(len(preview_items) / columns)
    canvas = Image.new(
        "RGBA",
        (
            columns * cell_width + (columns + 1) * padding,
            rows * cell_height + (rows + 1) * padding,
        ),
        (255, 248, 243, 255),
    )
    draw = ImageDraw.Draw(canvas)
    title_font = load_font(24)

    for index, item in enumerate(preview_items):
        row = index // columns
        col = index % columns
        left = padding + col * (cell_width + padding)
        top = padding + row * (cell_height + padding)

        card = Image.new("RGBA", (cell_width, cell_height), (255, 255, 255, 235))
        card_draw = ImageDraw.Draw(card)
        card_draw.rounded_rectangle(
            (0, 0, cell_width - 1, cell_height - 1),
            radius=28,
            outline=(239, 226, 218, 255),
            width=2,
            fill=(255, 255, 255, 245),
        )

        thumb = item["image"].copy()
        thumb.thumbnail((200, 200))
        thumb_left = (cell_width - thumb.width) // 2
        thumb_top = 28
        card.alpha_composite(thumb, (thumb_left, thumb_top))

        label_text = item["label"]
        label_box = card_draw.textbbox((0, 0), label_text, font=title_font)
        card_draw.text(
            ((cell_width - (label_box[2] - label_box[0])) / 2, 236),
            label_text,
            font=title_font,
            fill=(90, 74, 66, 255),
        )
        canvas.alpha_composite(card, (left, top))

    canvas.save(output_path)


def main() -> None:
    args = parse_args()
    ffmpeg = Path(args.ffmpeg)
    input_dir = Path(args.input_dir)
    output_dir = Path(args.output_dir)
    theme_json_path = Path(args.theme_json)
    summary_json_path = Path(args.summary_json)
    preview_path = Path(args.preview)

    config = load_json(Path(args.config))
    ensure_dir(output_dir)
    ensure_dir(theme_json_path.parent)
    ensure_dir(summary_json_path.parent)
    ensure_dir(preview_path.parent)

    duration_ms = int(round(1000 / args.fps))
    summary_items: list[dict] = []
    preview_items: list[dict] = []

    for sticker in config["stickers"]:
        source_name = sticker["source"]
        source_path = input_dir / source_name
        if not source_path.exists():
            raise FileNotFoundError(f"Source file not found: {source_path}")

        output_name = f"{sticker['id']}.webp"
        output_path = output_dir / output_name

        with tempfile.TemporaryDirectory(prefix="emoji_motion_") as temp_root:
            temp_dir = Path(temp_root)
            frame_paths = extract_frames(ffmpeg, source_path, temp_dir, args.fps, args.size)
            frame_count, frame_size = save_animated_webp(
                frame_paths=frame_paths,
                output_path=output_path,
                duration_ms=duration_ms,
                quality=args.quality,
            )

            with Image.open(frame_paths[0]) as first_frame:
                preview_items.append(
                    {
                        "label": sticker["label"],
                        "asset": output_name,
                        "image": first_frame.convert("RGBA").copy(),
                    }
                )

        summary_items.append(
            {
                "id": sticker["id"],
                "label": sticker["label"],
                "source": source_name,
                "asset": output_name,
                "tags": sticker.get("tags", []),
                "frames": frame_count,
                "durationMsPerFrame": duration_ms,
                "size": {
                    "width": frame_size[0],
                    "height": frame_size[1],
                },
                "bytes": output_path.stat().st_size,
            }
        )

    theme_data = {
        "id": config["theme"]["id"],
        "name": config["theme"]["name"],
        "bucket": config["theme"]["bucket"],
        "icon": config["theme"]["icon"],
        "stickers": [
            {
                "id": item["id"],
                "label": item["label"],
                "asset": item["asset"],
                "tags": item["tags"],
            }
            for item in summary_items
        ],
        "feelings": [],
    }

    with theme_json_path.open("w", encoding="utf-8") as fh:
        json.dump(theme_data, fh, ensure_ascii=False, indent=2)
        fh.write("\n")

    with summary_json_path.open("w", encoding="utf-8") as fh:
        json.dump(
            {
                "theme": theme_data,
                "fps": args.fps,
                "quality": args.quality,
                "assets": summary_items,
            },
            fh,
            ensure_ascii=False,
            indent=2,
        )
        fh.write("\n")

    build_preview(preview_items, preview_path)


if __name__ == "__main__":
    main()
