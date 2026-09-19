#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Генератор звуков для приложения «РИСУЕМ».

Ни одного чужого файла: всё считается математикой, поэтому лицензий нет
и в проекте не лежит ничего скачанного.

Звуки нарочно короткие и мягкие: ребёнок услышит их пятьдесят раз подряд,
и ни один не должен раздражать ни его, ни взрослого рядом.

Запуск:  python3 tools/make_sounds.py
Результат: app/src/main/res/raw/*.wav  (моно, 22050 Гц, 16 бит)

Хотите другой звук — правьте параметры в функциях ниже и перезапустите скрипт.
"""

import math
import os
import random
import struct
import wave

RATE = 22050
OUT_DIR = os.path.join(
    os.path.dirname(os.path.dirname(os.path.abspath(__file__))),
    "app", "src", "main", "res", "raw",
)

random.seed(4)  # шум одинаковый от запуска к запуску — сборка воспроизводима


def silence(duration):
    return [0.0] * int(RATE * duration)


def mix(base, addition, at=0.0):
    start = int(at * RATE)
    need = start + len(addition)
    if len(base) < need:
        base.extend([0.0] * (need - len(base)))
    for i, value in enumerate(addition):
        base[start + i] += value
    return base


def envelope(length, attack=0.006, release=0.5, curve=3.0):
    """Мягкая атака и экспоненциальный спад — без щелчков на краях."""
    out = []
    attack_n = max(1, int(RATE * attack))
    release_n = max(1, int(length * release))
    for i in range(length):
        if i < attack_n:
            value = i / attack_n
        else:
            t = (i - attack_n) / max(1, length - attack_n)
            value = math.exp(-curve * t)
        if i > length - release_n:
            value *= (length - i) / release_n
        out.append(value)
    return out


def tone(freq_from, freq_to, duration, amp=0.3, curve=3.0, harmonic=0.0):
    """Синус с плавно едущей частотой. Гармоника добавляет «деревянности»."""
    length = int(RATE * duration)
    env = envelope(length, curve=curve)
    out = []
    phase = 0.0
    phase2 = 0.0
    for i in range(length):
        k = i / max(1, length - 1)
        freq = freq_from + (freq_to - freq_from) * k
        phase += 2 * math.pi * freq / RATE
        phase2 += 2 * math.pi * freq * 2 / RATE
        value = math.sin(phase) + harmonic * math.sin(phase2)
        out.append(amp * env[i] * value)
    return out


def noise(duration, amp=0.2, low=0.25, curve=4.0):
    """Шум, сглаженный однополюсным фильтром: получается шорох, а не треск."""
    length = int(RATE * duration)
    env = envelope(length, curve=curve)
    out = []
    state = 0.0
    for i in range(length):
        state += low * (random.uniform(-1.0, 1.0) - state)
        out.append(amp * env[i] * state)
    return out


def save(name, samples):
    os.makedirs(OUT_DIR, exist_ok=True)
    path = os.path.join(OUT_DIR, name + ".wav")
    peak = max(1e-6, max(abs(s) for s in samples))
    gain = min(1.0, 0.85 / peak)
    frames = b"".join(
        struct.pack("<h", int(max(-1.0, min(1.0, s * gain)) * 32767)) for s in samples
    )
    with wave.open(path, "wb") as f:
        f.setnchannels(1)
        f.setsampwidth(2)
        f.setframerate(RATE)
        f.writeframes(frames)
    print("  %-10s %5.0f мс" % (name + ".wav", 1000.0 * len(samples) / RATE))


def blup():
    """Касание: короткая мягкая капля вверх-вниз."""
    s = tone(660, 440, 0.11, amp=0.30, curve=5.0, harmonic=0.12)
    return mix(s, tone(880, 660, 0.05, amp=0.10, curve=6.0), at=0.005)


def splash():
    """Заливка: «шшлёп» — мягкий шум плюс низкий тон вниз."""
    s = noise(0.20, amp=0.22, low=0.18, curve=5.0)
    mix(s, tone(300, 150, 0.18, amp=0.22, curve=4.0, harmonic=0.2))
    return s


def erase():
    """Ластик: сухой шорох, тише остальных звуков."""
    return noise(0.16, amp=0.16, low=0.45, curve=3.0)


def pop():
    """Наклейка приземлилась: щелчок с отскоком."""
    s = tone(520, 760, 0.06, amp=0.26, curve=6.0)
    return mix(s, tone(760, 520, 0.05, amp=0.16, curve=7.0), at=0.06)


def fanfare():
    """Готовая работа: три ноты вверх, коротко и радостно (до-ми-соль-до)."""
    s = silence(0.0)
    notes = [(523.25, 0.0), (659.25, 0.11), (783.99, 0.22), (1046.50, 0.33)]
    for freq, at in notes:
        mix(s, tone(freq, freq, 0.34, amp=0.22, curve=3.0, harmonic=0.25), at=at)
    return s


def wow():
    """Все области закрашены: короткий переливчик перед голосом «Как красиво!»."""
    s = silence(0.0)
    for i, freq in enumerate([784, 988, 1175]):
        mix(s, tone(freq, freq * 1.02, 0.18, amp=0.16, curve=4.0), at=0.06 * i)
    return s


if __name__ == "__main__":
    print("Считаю звуки:")
    save("blup", blup())
    save("splash", splash())
    save("erase", erase())
    save("pop", pop())
    save("fanfare", fanfare())
    save("wow", wow())
    print("Готово:", OUT_DIR)
