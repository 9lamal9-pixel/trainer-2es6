"""BTO topology composition, not a calibrated/installed locomotive model."""
from pneumatic_network import Reservoir, IdealRelay, LineCock, ExhaustingCock, transfer

class BtoSection:
    def __init__(self, control_l, relay_output_l, tc_group_l, inlet_mm,
                 exhaust_mm, line_mm, feed_line_mm, cd, deadband_kgf,
                 initial_feed_kgf=0):
        # PR volume is documented per section. Other volumes remain explicit.
        self.feed = Reservoir(150, initial_feed_kgf)
        self.controls = [Reservoir(control_l, 0) for _ in range(2)]
        self.outputs = [Reservoir(relay_output_l, 0) for _ in range(2)]
        self.cylinders = [Reservoir(tc_group_l, 0) for _ in range(2)]
        self.relay = IdealRelay(inlet_mm, exhaust_mm, cd, deadband_kgf)
        self.control_cocks = [LineCock(line_mm, cd) for _ in range(2)]
        self.tc_cocks = [ExhaustingCock(line_mm, exhaust_mm, cd) for _ in range(2)]
        self.feed_cocks = [True, True]
        self.feed_line_mm = feed_line_mm
        self.cd = cd
        self.pm_feed_open = True
        self.cold_transport_cock_open = False

    @property
    def reservoirs(self):
        return [self.feed] + self.controls + self.outputs + self.cylinders

    def step(self, pm, tm, control_signal, dt):
        # KO2 and KO1 preserve PR pressure if their supplying line empties.
        if self.pm_feed_open:
            transfer(pm, self.feed, dt, self.feed_line_mm, self.cd)
        if self.cold_transport_cock_open:
            transfer(tm, self.feed, dt, self.feed_line_mm, self.cd)
        exhaust = 0.0
        for i in range(2):
            self.control_cocks[i].step(control_signal, self.controls[i], dt)
            exhaust += self.relay.step(self.feed, self.outputs[i], self.controls[i],
                                       dt, feed_cock=self.feed_cocks[i])['exhaust_mass']
            exhaust += self.tc_cocks[i].step(self.outputs[i], self.cylinders[i], dt)['exhaust_mass']
        return exhaust

    def disconnect_bogie(self, bogie):
        if bogie not in (1, 2):
            raise ValueError('Bogie must be1 or2')
        i = bogie - 1
        self.feed_cocks[i] = False
        self.control_cocks[i].opened = False
        self.tc_cocks[i].opened = False
