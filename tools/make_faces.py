#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Готовит лица людей для игры: убирает у исходных картинок ровный фон,
обрезает по содержимому и кладёт PNG с прозрачностью в ресурсы.

Исходники лежат в art/*.jpg — замените их своими и запустите снова:

    python3 tools/make_faces.py

Результат: app/src/main/res/drawable-nodpi/face_*.png
"""

import os
from collections import deque

from PIL import Image, ImageFilter

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
ART = os.path.join(ROOT, "art")
OUT = os.path.join(ROOT, "app", "src", "main", "res", "drawable-nodpi")

FACES = {
    "face_kira": "kira-source.jpg",
    "face_mama": "mama-source.jpg",
    "face_papa": "papa-source.jpg",
}

SIZE = 512          # сторона готовой картинки
TOLERANCE = 26      # насколько цвет может отличаться от фона и всё ещё считаться фоном
MARGIN = 0.03       # поля вокруг лица


def background_mask(image, tolerance=TOLERANCE):
    """Заливка от краёв: всё, что связано с краем и похоже на угловой цвет, — фон."""
    width, height = image.size
    pixels = image.load()
    base = pixels[0, 0][:3]
    seen = bytearray(width * height)
    queue = deque()

    def similar(color):
        return (
            abs(color[0] - base[0]) <= tolerance
            and abs(color[1] - base[1]) <= tolerance
            and abs(color[2] - base[2]) <= tolerance
        )

    for x in range(width):
        for y in (0, height - 1):
            if not seen[y * width + x] and similar(pixels[x, y][:3]):
                seen[y * width + x] = 1
                queue.append((x, y))
    for y in range(height):
        for x in (0, width - 1):
            if not seen[y * width + x] and similar(pixels[x, y][:3]):
                seen[y * width + x] = 1
                queue.append((x, y))

    while queue:
        x, y = queue.popleft()
        for dx, dy in ((1, 0), (-1, 0), (0, 1), (0, -1)):
            nx, ny = x + dx, y + dy
            if 0 <= nx < width and 0 <= ny < height and not seen[ny * width + nx]:
                if similar(pixels[nx, ny][:3]):
                    seen[ny * width + nx] = 1
                    queue.append((nx, ny))
    return seen


def prepare(source, target):
    image = Image.open(source).convert("RGB")
    width, height = image.size
    mask = background_mask(image)

    alpha = Image.new("L", (width, height), 255)
    alpha_pixels = alpha.load()
    for y in range(height):
        row = y * width
        for x in range(width):
            if mask[row + x]:
                alpha_pixels[x, y] = 0
    # Мягкий край: иначе по контуру остаётся светлая кайма.
    alpha = alpha.filter(ImageFilter.GaussianBlur(radius=0.8))

    result = image.convert("RGBA")
    result.putalpha(alpha)

    box = result.getbbox()
    if box:
        result = result.crop(box)

    side = int(max(result.size) * (1 + MARGIN * 2))
    canvas = Image.new("RGBA", (side, side), (0, 0, 0, 0))
    canvas.paste(
        result,
        ((side - result.size[0]) // 2, (side - result.size[1]) // 2),
        result,
    )
    canvas = canvas.resize((SIZE, SIZE), Image.LANCZOS)

    os.makedirs(OUT, exist_ok=True)
    path = os.path.join(OUT, target + ".png")
    canvas.save(path, "PNG", optimize=True)
    print("%-10s %s" % (target, os.path.getsize(path) // 1024))
    return canvas


def launcher_icon(face):
    """Иконка приложения: лицо Киры в безопасной зоне адаптивной иконки."""
    side = 432
    canvas = Image.new("RGBA", (side, side), (0, 0, 0, 0))
    inner = int(side * 0.62)
    resized = face.resize((inner, inner), Image.LANCZOS)
    canvas.paste(resized, ((side - inner) // 2, (side - inner) // 2), resized)
    path = os.path.join(OUT, "ic_launcher_foreground.png")
    canvas.save(path, "PNG", optimize=True)
    print("%-10s %s" % ("иконка", os.path.getsize(path) // 1024))


if __name__ == "__main__":
    faces = {}
    for name, source in FACES.items():
        faces[name] = prepare(os.path.join(ART, source), name)
    launcher_icon(faces["face_kira"])
    print("Готово:", OUT)
