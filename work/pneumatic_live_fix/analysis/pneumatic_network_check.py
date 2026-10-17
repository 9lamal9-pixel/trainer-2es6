import json
from pathlib import Path
from pneumatic_network import Reservoir, transfer

results = []
for dt in (0.001, 0.01, 0.1, 10.0):
    feed, receiver = Reservoir(150, 8), Reservoir(20, 0)
    initial_mass = feed.mass + receiver.mass
    equilibrium = (150 * 8 + 20 * 0) / 170
    for _ in range(int(200 / dt)):
        transfer(feed, receiver, dt, 6, .7)
        assert feed.pressure + 1e-8 >= receiver.pressure
        assert abs(feed.mass + receiver.mass - initial_mass) < 1e-12
    assert abs(feed.gauge - equilibrium) < .001
    assert abs(receiver.gauge - equilibrium) < .001
    results.append({'scenario': 'closed equalisation', 'dt': dt,
                    'expected_equilibrium_kgf': equilibrium,
                    'source_final_kgf': feed.gauge, 'target_final_kgf': receiver.gauge,
                    'mass_error_kg': abs(feed.mass + receiver.mass - initial_mass)})

# A check valve must retain the section reserve when PM loses pressure.
pm, reserve = Reservoir(2000, 0), Reservoir(150, 8)
mass = reserve.mass
assert transfer(pm, reserve, 1000, 20, .7) == 0
assert reserve.mass == mass
results.append({'scenario': 'reverse feed blocked', 'reserve_final_kgf': reserve.gauge})

# A disconnected section must remain unaffected by another section exhausting.
sections = [Reservoir(150, 8), Reservoir(150, 8)]
unaffected_mass = sections[1].mass
original_mass = sections[0].mass
exhaust = 0
for _ in range(1000):
    exhaust += transfer(sections[0], None, .1, 20, .7)
assert sections[1].mass == unaffected_mass
assert abs(original_mass - sections[0].mass - exhaust) < 1e-12
assert abs(sections[0].gauge) < 1e-8
results.append({'scenario': 'independent section exhaust',
                'exhausted_section_kgf': sections[0].gauge,
                'retained_section_kgf': sections[1].gauge,
                'mass_error_kg': abs(original_mass - sections[0].mass - exhaust)})

# Bidirectional connection follows the current gradient without creating mass.
low, high = Reservoir(20, 1), Reservoir(20, 5)
mass = low.mass + high.mass
assert transfer(low, high, 1000, 20, .7, check_valve=False) < 0
assert abs(low.gauge - 3) < 1e-10 and abs(high.gauge - 3) < 1e-10
assert abs(low.mass + high.mass - mass) < 1e-12
results.append({'scenario': 'bidirectional equalisation', 'equilibrium_kgf': low.gauge})

out = {'passed': True, 'scope': 'mass and pressure invariants of new isothermal flow foundation',
       'installed_in_host': False, 'physical_valve_calibration_verified': False,
       'diameters_and_cd': 'test inputs only, not equipment specifications', 'results': results}
Path('outputs/Trainer_pneumatic_network_foundation_validation.json').write_text(json.dumps(out, indent=2), encoding='utf-8')
print('PASS: closed mass conservation, analytic equalisation, reverse isolation, independent sections, exhaust accounting')
