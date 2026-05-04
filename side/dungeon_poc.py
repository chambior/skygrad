import math
from PIL import Image, ImageDraw
import tkinter as tk
from PIL import ImageTk
from random import randint

def fill_polygon(points):
    pixels = []

    if len(points) < 3:
        return

    min_y = min(y for (_, y) in points)
    max_y = max(y for (_, y) in points)

    for y in range(min_y, max_y + 1):
        intersections = []

        for i in range(len(points)):
            x1, y1 = points[i]
            x2, y2 = points[(i + 1) % len(points)]

            if y1 == y2:
                continue

            if y < min(y1, y2) or y >= max(y1, y2):
                continue

            x = x1 + (y - y1) * (x2 - x1) / (y2 - y1)
            intersections.append(x)

        intersections.sort()

        for i in range(0, len(intersections), 2):
            if i + 1 >= len(intersections):
                break

            x_start = int(math.ceil(intersections[i]))
            x_end = int(math.floor(intersections[i + 1]))

            for x in range(x_start, x_end + 1):
                pixels.append((x, y))

    return pixels

def line(x1, y1, x2, y2):
    points = []
    dx = x2 - x1
    dy = y2 - y1

    steps = max(abs(dx), abs(dy))
    if steps == 0:
        return [(x1, y1)]

    x_inc = dx / steps
    y_inc = dy / steps

    x = x1
    y = y1

    for _ in range(steps + 1):
        points.append((round(x), round(y)))
        x += x_inc
        y += y_inc

    return points


def generate_towers(size):
    num_towers = randint(3, 7)
    towers = []

    cx = size // 2
    cy = size // 2

    for t in range(num_towers):
        minAngle = int(360 * (t + 1 / 4) / num_towers)
        maxAngle = int(360 * (t + 3 / 4) / num_towers)

        minR = size // 4
        maxR = size // 3

        r = randint(minR, maxR)
        omega = randint(minAngle, maxAngle)

        tx = cx + int(r * math.cos(math.radians(omega)))
        ty = cy + int(r * math.sin(math.radians(omega)))

        towers.append((tx, ty))

    return towers

def generate_image(size):
    img = Image.new("RGB", (size, size), "white")
    draw = ImageDraw.Draw(img)

    towers = generate_towers(size)

    fillin = fill_polygon(towers)

    for (x, y) in fillin:
        draw.point((x, y), fill="lightgray")

    prevTower = towers[-1]
    for (tx, ty) in towers:
        linePoints = line(prevTower[0], prevTower[1], tx, ty)

        wall_angle = math.atan2(ty - prevTower[1], tx - prevTower[0])

        if(math.degrees(wall_angle) >= 45 and math.degrees(wall_angle) <= 135):
            direction = "east"
        elif(math.degrees(wall_angle) >= 135 or math.degrees(wall_angle) <= -135):
            direction = "south"
        elif(math.degrees(wall_angle) >= -135 and math.degrees(wall_angle) <= -45):
            direction = "west"
        else:
            direction = "north" 
        
        color = "black"

        print(f"Wall {tx} {ty} angle {math.degrees(wall_angle):.2f} color {color} direction {direction}")

        for (x, y) in linePoints:
            draw.point((x, y), fill=color)

            if(direction == "north"):
                draw.point((x, y-1), fill="green")
            if(direction == "east"):
                draw.point((x+1, y), fill="green")
            if(direction == "south"):
                draw.point((x, y+1), fill="green")
            if(direction == "west"):
                draw.point((x-1, y), fill="green")

        prevTower = (tx, ty)

    for (tx, ty) in towers:
        draw.point((tx, ty), fill="magenta")

    draw.point((size//2, size//2), fill="blue")

    return img

def show_preview(img):
    window_size = 720

    scale = min(window_size / img.width, window_size / img.height)
    new_size = (int(img.width * scale), int(img.height * scale))
    resized = img.resize(new_size, Image.NEAREST)

    root = tk.Tk()
    root.title("Polygon Preview")
    root.geometry(f"{window_size}x{window_size}")

    canvas = tk.Canvas(root, width=window_size, height=window_size)
    canvas.pack()

    tk_img = ImageTk.PhotoImage(resized)
    canvas.create_image(window_size // 2, window_size // 2, image=tk_img, anchor="center")

    root.mainloop()

if __name__ == "__main__":
    size = 64

    img = generate_image(size)
    show_preview(img)