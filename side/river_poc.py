import tkinter as tk
from PIL import Image, ImageTk
import random

WIDTH = 600
HEIGHT = 400
SCALE = 10

def generate_pattern(w, h, scale):
    grid_w = w // scale
    grid_h = h // scale

    grid = [[random.random() for _ in range(grid_w)] for _ in range(grid_h)]

    threshold = 0.5

    img = Image.new("RGB", (grid_w, grid_h), "black")
    pixels = img.load()

    for y in range(grid_h):
        for x in range(grid_w):
            if grid[y][x] > threshold:
                pixels[x, y] = (255, 255, 255)
            else:
                pixels[x, y] = (0, 0, 0)

    # simple smoothing (cellular automata)
    for _ in range(4):
        new_grid = [[0 for _ in range(grid_w)] for _ in range(grid_h)]
        for y in range(grid_h):
            for x in range(grid_w):
                count = 0
                for dy in [-1, 0, 1]:
                    for dx in [-1, 0, 1]:
                        nx, ny = x + dx, y + dy
                        if 0 <= nx < grid_w and 0 <= ny < grid_h:
                            if pixels[nx, ny] == (255, 255, 255):
                                count += 1
                if count >= 5:
                    new_grid[y][x] = 1
                else:
                    new_grid[y][x] = 0

        for y in range(grid_h):
            for x in range(grid_w):
                pixels[x, y] = (255, 255, 255) if new_grid[y][x] else (0, 0, 0)

    # detect borders
    border_img = Image.new("RGB", (grid_w, grid_h), "black")
    bpix = border_img.load()

    for y in range(grid_h):
        for x in range(grid_w):
            current = pixels[x, y]
            for dy in [-1, 0, 1]:
                for dx in [-1, 0, 1]:
                    nx, ny = x + dx, y + dy
                    if 0 <= nx < grid_w and 0 <= ny < grid_h:
                        if pixels[nx, ny] != current:
                            bpix[x, y] = (255, 255, 255)

    return border_img.resize((w, h), Image.NEAREST)


class App:
    def __init__(self, root):
        self.root = root
        self.canvas = tk.Label(root)
        self.canvas.pack()

        self.button = tk.Button(root, text="Generate", command=self.update_image)
        self.button.pack()

        self.update_image()

    def update_image(self):
        img = generate_pattern(WIDTH, HEIGHT, SCALE)
        self.tk_img = ImageTk.PhotoImage(img)
        self.canvas.config(image=self.tk_img)


root = tk.Tk()
root.title("Pattern Generator")
app = App(root)
root.mainloop()