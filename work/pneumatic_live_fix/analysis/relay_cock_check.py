import json
from pathlib import Path
from pneumatic_network import Reservoir, IdealRelay, LineCock, transfer

relay = IdealRelay(6, 6, .7, .001)
feed = Reservoir(150, 8)
excitation = Reservoir(1, 3)
upstream = Reservoir(20, 0)
cylinder = Reservoir(12, 3)
cock = LineCock(3, .7, opened=False)
initial_mass = feed.mass + excitation.mass + upstream.mass + cylinder.mass
initial_excitation = excitation.mass
exhausted = 0
for _ in range(1000):
    assert cock.step(upstream, excitation, .1) == 0
    exhausted += transfer(cylinder, None, .1, 1, .7)
    relay.step(feed, cylinder, excitation, .1)
assert excitation.mass == initial_excitation
assert abs(cylinder.gauge - 3) <= .001
assert feed.gauge < 8
assert abs(initial_mass - feed.mass - excitation.mass - upstream.mass - cylinder.mass - exhausted) < 1e-11

# Closing the feed stops replenishment, but does not block the atmospheric port.
unfilled = Reservoir(12, 0)
relay.step(feed, unfilled, excitation, 1000, feed_cock=False)
assert abs(unfilled.gauge) < 1e-10
release = Reservoir(1, 0)
before_feed = feed.mass
for _ in range(1000):
    relay.step(feed, cylinder, release, .1, feed_cock=False)
assert cylinder.gauge <= .001 and feed.mass == before_feed

# Reopening the control line permits chamber exhaust and hence TC release.
retained = Reservoir(1, 3)
tc = Reservoir(12, 3)
cock.opened = True
for _ in range(1000):
    cock.step(upstream, retained, .1)
    relay.step(feed, tc, retained, .1)
assert retained.gauge < .2 and tc.gauge < .2
out = {'passed': True, 'scope': 'plain isolation cock topology, not an atmospheric disconnect cock',
       'installed': False, 'factory_valve_parameters_verified': False,
       'trapped_chamber_retains_pressure': True, 'TC_leak_replenished_from_feed': True,
       'closed_feed_does_not_block_release': True, 'reopened_control_line_releases': True}
Path('outputs/Trainer_relay_cock_validation.json').write_text(json.dumps(out, indent=2), encoding='utf-8')
print('PASS: trapped excitation stays active; feed isolation and control isolation differ')
