import json
from pathlib import Path
from pneumatic_network import Reservoir, IdealRelay, ExhaustingCock

# Test volumes and conductances only; equipment calibration remains separate.
feed = Reservoir(150, 8)
controls = [Reservoir(1, 3), Reservoir(1, 3)]
relay_outputs = [Reservoir(1, 3), Reservoir(1, 3)]
tc = [Reservoir(12, 3), Reservoir(12, 3)]
relay = IdealRelay(6, 6, .7, .001)
outlet_cocks = [ExhaustingCock(6, 3, .7), ExhaustingCock(6, 3, .7)]
for _ in range(100):
    # Feed/control lines to bogie1 isolated; retained chamber remains at3.
    relay.step(feed, relay_outputs[0], controls[0], .1, feed_cock=False)
    outlet_cocks[0].step(relay_outputs[0], tc[0], .1)
assert abs(tc[0].gauge - 3) < 1e-10

outlet_cocks[0].opened = False
untouched_mass = tc[1].mass
initial_mass = feed.mass + sum(v.mass for v in relay_outputs + tc + controls)
exhaust = 0
for _ in range(1000):
    for i in range(2):
        exhaust += relay.step(feed, relay_outputs[i], controls[i], .1, feed_cock=i==1)['exhaust_mass']
        exhaust += outlet_cocks[i].step(relay_outputs[i], tc[i], .1)['exhaust_mass']
    assert tc[1].mass == untouched_mass
assert tc[0].gauge < .001
assert abs(relay_outputs[0].gauge - 3) < 1e-10
error = abs(initial_mass - feed.mass - sum(v.mass for v in relay_outputs + tc + controls) - exhaust)
assert error < 1e-11
out = {'passed': True, 'installed': False, 'scope': 'bogie disconnect topology; calibration absent',
       'isolating_feed_control_does_not_release_TC': True,
       'closing_TC_cock_releases_only_selected_bogie': True,
       'relay_output_retains_pressure': True,
       'final_TC_kgf': [v.gauge for v in tc], 'mass_error_kg': error}
Path('outputs/Trainer_bogie_disconnect_validation.json').write_text(json.dumps(out, indent=2), encoding='utf-8')
print('PASS: feed/control isolation retains brake; KR6 vent releases only bogie1')
