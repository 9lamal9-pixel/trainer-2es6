import json
from pathlib import Path
from pneumatic_network import Reservoir, ExhaustingCock, IdealRelay

source, chamber = Reservoir(20, 3), Reservoir(1, 3)
feed, cylinder = Reservoir(150, 8), Reservoir(12, 3)
cock = ExhaustingCock(3, 3, .7, opened=False)
relay = IdealRelay(6, 6, .7, .001)
initial_source = source.mass
initial_mass = source.mass + chamber.mass + feed.mass + cylinder.mass
exhaust = 0
for _ in range(1000):
    flow = cock.step(source, chamber, .1)
    exhaust += flow['exhaust_mass']
    assert flow['line_mass'] == 0
    exhaust += relay.step(feed, cylinder, chamber, .1)['exhaust_mass']
    assert source.mass == initial_source
assert chamber.gauge < .001 and cylinder.gauge < .001
mass_error = abs(initial_mass - source.mass - chamber.mass - feed.mass - cylinder.mass - exhaust)
assert mass_error < 1e-11

# Opening closes the atmospheric path and reconnects the line.
cock.opened = True
before_mass = source.mass + chamber.mass
for _ in range(1000):
    flow = cock.step(source, chamber, .1)
    assert flow['exhaust_mass'] == 0
assert abs(source.mass + chamber.mass - before_mass) < 1e-12
expected = 20 * 3 / 21
assert abs(source.gauge - expected) < .001
assert abs(chamber.gauge - expected) < .001
out = {'passed': True, 'scope': 'generic oriented three-port disconnect, not installed equipment calibration',
       'installed': False, 'source_retained_during_exhaust': True,
       'closed_control_releases_TC': True, 'atmospheric_path_closed_when_open': True,
       'mass_error_kg': mass_error, 'reconnected_equilibrium_kgf': chamber.gauge}
Path('outputs/Trainer_exhausting_cock_validation.json').write_text(json.dumps(out, indent=2), encoding='utf-8')
print('PASS: closed downstream vents, source retained; open reconnects without atmospheric loss')
