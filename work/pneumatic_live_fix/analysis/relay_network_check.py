import json
from pathlib import Path
from pneumatic_network import Reservoir, IdealRelay, transfer

# These volumes/diameters are test inputs, not a statement of factory calibration.
feed = Reservoir(150, 8)
control = Reservoir(1, 3)
cylinders = [Reservoir(12, 0), Reservoir(12, 0)]
relay = IdealRelay(6, 6, .7, .001)
initial_control_mass = control.mass
initial_mass = feed.mass + sum(c.mass for c in cylinders)
for _ in range(1000):
    for c in cylinders:
        relay.step(feed, c, control, .1)
        assert c.gauge <= 3 + 1e-9
        assert abs(feed.mass + sum(v.mass for v in cylinders) - initial_mass) < 1e-12
assert all(abs(c.gauge - 3) <= .001 for c in cylinders)
assert control.mass == initial_control_mass
assert feed.gauge < 8
filled_pressure = [c.gauge for c in cylinders]
mass_before_hold = feed.mass
for _ in range(3000):
    for c in cylinders:
        relay.step(feed, c, control, .1)
assert feed.mass == mass_before_hold

# Control pressure reduction is an external command fixture, not stolen TC air.
released_control = Reservoir(1, 0)
exhaust = 0
for _ in range(1000):
    for c in cylinders:
        exhaust += relay.step(feed, c, released_control, .1)['exhaust_mass']
assert all(c.gauge <= .001 for c in cylinders)
assert abs(feed.mass + sum(c.mass for c in cylinders) + exhaust - initial_mass) < 1e-12

# No feed pressure: a control command alone cannot create TC pressure.
empty_feed, empty_tc = Reservoir(150, 0), Reservoir(12, 0)
for _ in range(100):
    relay.step(empty_feed, empty_tc, control, 1)
assert abs(empty_tc.gauge) < 1e-10

out = {'passed': True, 'scope': 'ideal relay mass accounting and two independent cylinder groups',
       'installed': False, 'factory_calibration_verified': False,
       'filled_tc_kgf': filled_pressure, 'feed_after_filling_kgf': feed.gauge,
       'control_mass_unchanged': control.mass == initial_control_mass,
       'release_mass_error_kg': abs(feed.mass + sum(c.mass for c in cylinders) + exhaust - initial_mass),
       'empty_feed_tc_kgf': empty_tc.gauge}
Path('outputs/Trainer_ideal_relay_network_validation.json').write_text(json.dumps(out, indent=2), encoding='utf-8')
print('PASS: TC air comes from feed; control mass unchanged; hold, release and empty-feed invariants')
