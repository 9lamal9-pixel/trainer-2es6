"""Isothermal reservoir/flow foundation; not yet installed in the simulator.

Volumes and conductances are explicit inputs, not hidden calibrations.
All internal pressures are absolute Pa, all masses kg and volumes m3.
"""
import math
from dataclasses import dataclass

R = 287.05
GAMMA = 1.4
ATM = 101325.0
KGF_CM2 = 98066.5

@dataclass
class Reservoir:
    litres: float
    gauge_kgf: float
    temperature: float = 293.15

    def __post_init__(self):
        if not all(math.isfinite(v) for v in (self.litres, self.gauge_kgf, self.temperature)):
            raise ValueError("Nonfinite reservoir input")
        if self.litres <= 0 or self.gauge_kgf < 0 or self.temperature <= 0:
            raise ValueError("Invalid reservoir state")
        self.volume = self.litres / 1000
        self.mass = (ATM + self.gauge_kgf * KGF_CM2) * self.volume / (R * self.temperature)

    @property
    def pressure(self):
        return self.mass * R * self.temperature / self.volume

    @property
    def gauge(self):
        return (self.pressure - ATM) / KGF_CM2

def nozzle(up, down, diameter_mm, cd, temperature):
    if not all(math.isfinite(v) for v in (up, down, diameter_mm, cd, temperature)):
        raise ValueError("Nonfinite nozzle input")
    if up <= 0 or down <= 0 or diameter_mm < 0 or not 0 <= cd <= 1 or temperature <= 0:
        raise ValueError("Invalid nozzle parameters")
    if up <= down or diameter_mm == 0 or cd == 0:
        return 0.0
    ratio = down / up
    critical = (2 / (GAMMA + 1)) ** (GAMMA / (GAMMA - 1))
    area = math.pi * (diameter_mm / 1000) ** 2 / 4
    if ratio <= critical:
        factor = math.sqrt(GAMMA / (R * temperature)) * (2 / (GAMMA + 1)) ** ((GAMMA + 1) / (2 * (GAMMA - 1)))
    else:
        factor = math.sqrt(max(0, 2 * GAMMA / (R * temperature * (GAMMA - 1)) * (ratio ** (2 / GAMMA) - ratio ** ((GAMMA + 1) / GAMMA))))
    return cd * area * up * factor

def transfer(source, target, dt, diameter_mm, cd, check_valve=True):
    """Return signed transferred mass; target=None represents atmosphere.

    Clamp to equalisation mass so a long numerical step cannot reverse the
    pressure gradient. This is a numerical guard, not a valve flow calibration.
    """
    if not math.isfinite(dt) or dt < 0:
        raise ValueError("Invalid time step")
    if target is source:
        raise ValueError("Self connection")
    down = ATM if target is None else target.pressure
    if source.pressure < down and not check_valve and target is not None:
        return -transfer(target, source, dt, diameter_mm, cd, True)
    rate = nozzle(source.pressure, down, diameter_mm, cd, source.temperature)
    compliance = R * source.temperature / source.volume
    if target is not None:
        compliance += R * target.temperature / target.volume
    equalisation_mass = max(0, (source.pressure - down) / compliance)
    amount = min(rate * dt, equalisation_mass)
    source.mass -= amount
    if target is not None:
        target.mass += amount
    return amount

@dataclass
class IdealRelay:
    """Flow-accounted ideal repeater; mechanical calibration is still absent.

    The control pressure moves valves only; it does not supply cylinder mass.
    Diameters and deadband must be supplied by the caller. This is not yet a
    calibrated model of 042 or of the locomotive BTO.
    """
    inlet_mm: float
    exhaust_mm: float
    cd: float
    deadband_kgf: float

    def __post_init__(self):
        if not all(math.isfinite(v) for v in (self.inlet_mm, self.exhaust_mm, self.cd, self.deadband_kgf)):
            raise ValueError("Nonfinite relay parameters")
        if self.inlet_mm < 0 or self.exhaust_mm < 0 or not 0 <= self.cd <= 1 or self.deadband_kgf < 0:
            raise ValueError("Invalid relay parameters")

    def step(self, feed, cylinder, control, dt, feed_cock=True):
        if not math.isfinite(dt) or dt < 0:
            raise ValueError("Invalid time step")
        if feed is cylinder or control is cylinder or feed is control:
            raise ValueError("Relay ports must be separate reservoirs")
        # An isolated excitation chamber retains its pressure. Its line cock
        # stops line flow, not diaphragm action or replenishment of TC leaks.
        target = max(ATM, control.pressure)
        delta = target - cylinder.pressure
        if abs(delta) <= self.deadband_kgf * KGF_CM2:
            return {'feed_mass': 0.0, 'exhaust_mass': 0.0, 'state': 'hold'}
        if delta > 0:
            if not feed_cock:
                return {'feed_mass': 0.0, 'exhaust_mass': 0.0, 'state': 'feed isolated'}
            rate = nozzle(feed.pressure, cylinder.pressure, self.inlet_mm, self.cd, feed.temperature)
            need = delta * cylinder.volume / (R * cylinder.temperature)
            used_dt = min(dt, need / rate) if rate else 0
            amount = transfer(feed, cylinder, used_dt, self.inlet_mm, self.cd)
            return {'feed_mass': amount, 'exhaust_mass': 0.0, 'state': 'fill'}
        rate = nozzle(cylinder.pressure, ATM, self.exhaust_mm, self.cd, cylinder.temperature)
        need = -delta * cylinder.volume / (R * cylinder.temperature)
        used_dt = min(dt, need / rate) if rate else 0
        amount = transfer(cylinder, None, used_dt, self.exhaust_mm, self.cd)
        return {'feed_mass': 0.0, 'exhaust_mass': amount, 'state': 'exhaust'}

@dataclass
class LineCock:
    diameter_mm: float
    cd: float
    opened: bool = True

    def __post_init__(self):
        if not math.isfinite(self.diameter_mm) or self.diameter_mm < 0 or not math.isfinite(self.cd) or not 0 <= self.cd <= 1:
            raise ValueError("Invalid line cock parameters")

    def step(self, source, target, dt):
        if not math.isfinite(dt) or dt < 0:
            raise ValueError("Invalid time step")
        if not self.opened:
            return 0.0
        return transfer(source, target, dt, self.diameter_mm, self.cd, check_valve=False)

@dataclass
class ExhaustingCock:
    """Explicit three-port disconnect; downstream port vents when closed.

    Port orientation must be verified against the actual schematic before use.
    This generic element does not assert that every KrRSh has this topology.
    """
    line_mm: float
    exhaust_mm: float
    cd: float
    opened: bool = True

    def __post_init__(self):
        if not all(math.isfinite(v) for v in (self.line_mm, self.exhaust_mm, self.cd)):
            raise ValueError("Nonfinite cock parameters")
        if self.line_mm < 0 or self.exhaust_mm < 0 or not 0 <= self.cd <= 1:
            raise ValueError("Invalid cock parameters")

    def step(self, source, downstream, dt):
        if source is downstream:
            raise ValueError("Cock ports must be separate")
        if not math.isfinite(dt) or dt < 0:
            raise ValueError("Invalid time step")
        if self.opened:
            amount = transfer(source, downstream, dt, self.line_mm, self.cd, check_valve=False)
            return {'line_mass': amount, 'exhaust_mass': 0.0}
        amount = transfer(downstream, None, dt, self.exhaust_mm, self.cd)
        return {'line_mass': 0.0, 'exhaust_mass': amount}
