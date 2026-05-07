import tkinter as tk
from PIL import Image, ImageTk
import random
import math

WIDTH = 500
HEIGHT = 500
RIVER_QUANTITY = 1     # how many rivers to draw across the image
RIVER_WIDTH = 0.08     # bright-band thickness, 0..1 (higher = wider)
SCALE = 0.003          # noise frequency — controls how tightly rivers meander

# Derived: keeps the existing threshold logic in generate_pattern unchanged
THRESHOLD = 1.0 - RIVER_WIDTH

_PERM_SIZE = 256
_perm = []

def _reseed():
    global _perm
    p = list(range(_PERM_SIZE))
    random.shuffle(p)
    _perm = p + p

_reseed()

def _fade(t):
    return t * t * t * (t * (t * 6 - 15) + 10)

def _lerp(a, b, t):
    return a + t * (b - a)

def _grad(h, x, y):
    h &= 3
    if h == 0: return x + y
    if h == 1: return -x + y
    if h == 2: return x - y
    return -x - y

def _perlin(x, y):
    xi = int(math.floor(x)) & (_PERM_SIZE - 1)
    yi = int(math.floor(y)) & (_PERM_SIZE - 1)
    xf = x - math.floor(x)
    yf = y - math.floor(y)
    u, v = _fade(xf), _fade(yf)
    aa = _perm[_perm[xi]     + yi]
    ab = _perm[_perm[xi]     + yi + 1]
    ba = _perm[_perm[xi + 1] + yi]
    bb = _perm[_perm[xi + 1] + yi + 1]
    x1 = _lerp(_grad(aa, xf,     yf),     _grad(ba, xf - 1, yf),     u)
    x2 = _lerp(_grad(ab, xf,     yf - 1), _grad(bb, xf - 1, yf - 1), u)
    return (_lerp(x1, x2, v) + 1) / 2

def river_noise(x, y):
    s = SCALE
    best = 0.0
    # Overlay RIVER_QUANTITY independent ridge fields; per-system offsets
    # decorrelate them, alternating tilt axis lets rivers cross.
    for i in range(RIVER_QUANTITY):
        ox = i * 17.3
        oy = i * 23.7
        wx = _perlin(x * s + ox,       y * s + oy)
        wy = _perlin(x * s + ox + 5.2, y * s + oy + 1.3)
        base = _perlin(x * s + 1.5 * wx + ox, y * s + 1.5 * wy + oy)
        tilt = (y / HEIGHT) if i % 2 == 0 else (x / WIDTH)
        directed = base * 0.6 + tilt * 0.4
        ridge = 1.0 - abs(2 * directed - 1)
        if ridge > best:
            best = ridge
    return best

def generate_pattern(width, height, scale):
    img = Image.new("L", (width, height))
    pixels = img.load()

    for x in range(width):
        for y in range(height):
            noise = river_noise(x, y)

            if(noise > THRESHOLD): pixels[x, y] = int(255-(noise-THRESHOLD)/(1-THRESHOLD)*255)
            else: pixels[x, y] = 255

    return img

class App:
    def __init__(self, root):
        self.root = root
        self.canvas = tk.Label(root)
        self.canvas.pack()

        self.button = tk.Button(root, text="Generate", command=self.update_image)
        self.button.pack()

        self.update_image()

    def update_image(self):
        _reseed()
        img = generate_pattern(WIDTH, HEIGHT, SCALE)
        self.tk_img = ImageTk.PhotoImage(img)
        self.canvas.config(image=self.tk_img)


root = tk.Tk()
root.title("Pattern Generator")
app = App(root)
root.mainloop()