import json
from pathlib import Path
from pneumatic_network import Reservoir
from bto_section import BtoSection

# Test inputs, not factory valve calibration or actual cylinder dimensions.
params = dict(control_l=1, relay_output_l=1, tc_group_l=12,
              inlet_mm=6, exhaust_mm=6, line_mm=6, feed_line_mm=6,
              cd=.7, deadband_kgf=.001)
sections = [BtoSection(**params), BtoSection(**params)]
pm, tm = Reservoir(2000, 8), Reservoir(100, 5)
signals = [Reservoir(20, 3), Reservoir(20, 3)]
all_nodes = [pm, tm] + signals + sum([s.reservoirs for s in sections], [])
original_mass = sum(v.mass for v in all_nodes)
exhaust = 0
for _ in range(3000):
    for i, section in enumerate(sections):
        exhaust += section.step(pm, tm, signals[i], .01)
    assert abs(sum(v.mass for v in all_nodes) + exhaust - original_mass) < 1e-10
pressures = [[c.gauge for c in s.cylinders] for s in sections]
assert all(p > 2.5 for row in pressures for p in row)
assert pm.gauge < 8
assert abs(tm.gauge - 5) < 1e-10  # Cold-transport cocks remain closed.

sections[0].disconnect_bogie(1)
for _ in range(3000):
    for i, section in enumerate(sections):
        exhaust += section.step(pm, tm, signals[i], .01)
    assert abs(sum(v.mass for v in all_nodes) + exhaust - original_mass) < 1e-10
final = [[c.gauge for c in s.cylinders] for s in sections]
assert final[0][0] < .001
assert final[0][1] > 2.5 and all(p > 2.5 for p in final[1])
out = {'passed': True, 'installed': False, 'calibrated_to_RE': False,
       'scope': 'two-section four-bogie topology and mass conservation',
       'test_parameters': params, 'initial_TC_kgf': pressures,
       'after_bogie1_disconnect_TC_kgf': final, 'PM_final_kgf': pm.gauge,
       'mass_error_kg': abs(sum(v.mass for v in all_nodes) + exhaust - original_mass),
       'exhausted_mass_kg': exhaust}
Path('outputs/Trainer_BTO_two_section_validation.json').write_text(json.dumps(out, indent=2), encoding='utf-8')
print('PASS: two sections, four TC groups, PM consumption and selective bogie disconnect; mass conserved')
