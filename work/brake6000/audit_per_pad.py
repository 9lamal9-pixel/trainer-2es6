"""Quantify the aggregate-load defect using native numeric units only.

This is a diagnostic, not an accepted 2ES6 friction implementation. Retains
native .00085 and pad1 coefficients to isolate the change of load argument.
"""
import json
import math
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
AREA = math.pi * 20.3 ** 2 / 4

def load_part(k):
    return .031 * k * (k + 6.25) / (k + 1.92)

def compare(pressures):
    total_k = AREA * 8.8 * .00085 * 4 * sum(pressures)
    old = load_part(total_k)
    # Four wheel cylinders per bogie, two pads per cylinder.
    pads = [AREA * 4.4 * .00085 * p for p in pressures]
    separate = 8 * sum(load_part(k) for k in pads)
    return dict(pressures=pressures, native_aggregate_load=total_k,
                aggregate_friction_part=old, per_pad_friction_part=separate,
                separate_over_aggregate=separate / old if old else None)

result = dict(
    accepted_for_production=False,
    scope='Same native numeric units and coefficients; isolates nonlinear load aggregation only',
    coefficients_source='https://prom-trans.kz/assets/files/zhurnal/1_50.pdf',
    caveats=['native force conversion and k units need independent confirmation',
             'RE specifies flanged cast-iron pads; generic cast-iron law not accepted',
             'native efficiency .85 retained for comparison; spring losses omitted'],
    cases=[compare(p) for p in ([0,0,0,0], [1,1,1,1], [3.9]*4,
                               [3.9,0,3.9,3.9], [3.9,1,3.9,1])])
(ROOT / 'outputs/Trainer_per_pad_friction_audit.json').write_text(
    json.dumps(result, ensure_ascii=False, indent=2), encoding='utf-8')
for case in result['cases']:
    print(case['pressures'], case['separate_over_aggregate'])
