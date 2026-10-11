"""Cylinder swept volume, distinct from total pneumatic working volume."""
import math

def swept_litres(diameter_mm, stroke_mm, count=1):
    if not math.isfinite(diameter_mm) or diameter_mm <= 0:
        raise ValueError('Positive finite bore required')
    if not math.isfinite(stroke_mm) or stroke_mm < 0:
        raise ValueError('Nonnegative finite stroke required')
    if isinstance(count, bool) or not isinstance(count, int) or count <= 0:
        raise ValueError('Positive integer cylinder count required')
    return math.pi * diameter_mm ** 2 / 4 * stroke_mm * count / 1e6

def working_litres(diameter_mm, stroke_mm, count, dead_litres_per_cylinder,
                   connecting_pipe_litres):
    # Caller must supply dead space and pipes; they cannot be inferred from stroke.
    for value in (dead_litres_per_cylinder, connecting_pipe_litres):
        if not math.isfinite(value) or value < 0:
            raise ValueError('Nonnegative finite extra volume required')
    return (swept_litres(diameter_mm, stroke_mm, count)
            + count * dead_litres_per_cylinder + connecting_pipe_litres)
