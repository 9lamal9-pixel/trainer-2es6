from pathlib import Path
import struct

from PIL import Image


def decode(source):
    data = source.read_bytes()
    if data[0] != 10 or data[2:4] != bytes([1, 1]):
        raise ValueError("Expected PCX RLE with one bit per plane")
    decoded = bytearray()
    pos = 128
    while pos < len(data):
        value = data[pos]
        pos += 1
        if value >= 192:
            if pos >= len(data):
                raise ValueError("Truncated RLE")
            decoded.extend(bytes([data[pos]]) * (value & 63))
            pos += 1
        else:
            decoded.append(value)
    x1, y1, x2, y2 = struct.unpack("<4H", data[4:12])
    width, height = x2 - x1 + 1, y2 - y1 + 1
    stride = struct.unpack("<H", data[66:68])[0]
    planes = data[65]
    if len(decoded) != stride * planes * height:
        raise ValueError("Wrong number of decoded pixels")
    image = Image.new("P", (width, height))
    image.putpalette(list(data[16:64]) + [0] * 720)
    pixels = image.load()
    for y in range(height):
        for x in range(width):
            color = 0
            for plane in range(planes):
                at = (y * planes + plane) * stride + x // 8
                color |= ((decoded[at] >> (7 - x % 8)) & 1) << plane
            pixels[x, y] = color
    image.convert("RGB").save(source.parents[2] / (source.stem + "_decoded.png"))


if __name__ == "__main__":
    here = Path(__file__).resolve().parent
    for source in (here / "tren_vl10" / "VL10").glob("*.PCC"):
        decode(source)
