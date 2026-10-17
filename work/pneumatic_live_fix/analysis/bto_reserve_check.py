"""Finite PR reserve tests; geometry/conductances remain test inputs."""
import json
from pathlib import Path
from pneumatic_network import Reservoir, transfer
from bto_section import BtoSection

params = dict(control_l=1, relay_output_l=1, tc_group_l=12,
              inlet_mm=6, exhaust_mm=6, line_mm=6, feed_line_mm=6,
              cd=.7, deadband_kgf=.001)
results = []
for initial_feed in (0, 8):
    section = BtoSection(**params, initial_feed_kgf=initial_feed)
    pm, tm, signal = Reservoir(2000, 0), Reservoir(100, 0), Reservoir(20, 4.1)
    nodes = [pm, tm, signal] + section.reservoirs
    initial_mass = sum(n.mass for n in nodes)
    exhaust = 0
    for _ in range(3000):
        exhaust += section.step(pm, tm, signal, .01)
    filled = [c.gauge for c in section.cylinders]
    pr_after_fill = section.feed.gauge
    assert abs(pm.gauge) < 1e-9
    assert abs(sum(n.mass for n in nodes) + exhaust - initial_mass) < 1e-9
    if initial_feed == 0:
        assert max(abs(p) for p in filled) < 1e-9
    else:
        assert min(filled) > 3
        assert 0 < pr_after_fill < initial_feed
        # A real atmospheric leak consumes the finite reserve; no refill from PM0.
        for _ in range(120000):
            exhaust += section.step(pm, tm, signal, .01)
            for c in section.cylinders:
                exhaust += transfer(c, None, .01, 2, .7)
        assert max(c.gauge for c in section.cylinders) < .1
        assert section.feed.gauge < .1
        assert abs(sum(n.mass for n in nodes) + exhaust - initial_mass) < 1e-9
    results.append(dict(initial_PR_kgf=initial_feed, filled_TC_kgf=filled,
                        PR_after_fill_kgf=pr_after_fill,
                        final_TC_kgf=[c.gauge for c in section.cylinders],
                        final_PR_kgf=section.feed.gauge,
                        mass_error_kg=abs(sum(n.mass for n in nodes)+exhaust-initial_mass)))
out = dict(passed=True, installed=False, calibrated_to_RE=False,
           scope='empty source, retained PR reserve, finite depletion and mass conservation',
           test_parameters=params, results=results)
Path('outputs/Trainer_BTO_reserve_validation.json').write_text(json.dumps(out,indent=2),encoding='utf-8')
print('PASS: empty PR cannot fill TC; charged PR brakes with PM0, then depletes under leakage')
