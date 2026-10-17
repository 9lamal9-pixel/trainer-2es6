# Independent check: two-section locomotive mass passed to POEZD

Checked the original bytes of copied `Trenager_3D.exe`, without execution. The machine-code listings in `work/brake6000/main_disasm` were reviewed against a separate PE read of the VMT/list routines (`mass_check.py`, `mass_check.txt`). Addresses here are VA; base=0x400000.

The constructor's class pointer at VA0x61d758 is0x61d7b0. Its actual first32 VMT bytes are:

`58f96100d8f961003cfa610014fc6100a00a6200f0fa6100b4f961007cf96100`

Therefore virtual+0→0x61f958(GetLength);+4→0x61f9d8(GetAxes);+0xc→0x61fc14(GetWeight). These match the indirect calls used by POEZD initialization, not merely debug-name guesses.

GetWeight VA0x61fc14 is a36-byte function:

```
mov eax,[self+0x138]    ; selected library
mov eax,[eax+0x1c]     ; Physics record
mov eax,[eax+4]        ; WightOneSec
return eax
```

It contains no multiplication by the section count. GetAxes uses the same chain with Physics+8; GetLength uses Physics+0xc and returns that Single. The selected forward2ЭС6 library data separately decoded by configs contains WightOneSec100, CountOsi4, LLoc15.965, Sec2. A reverse-library entry has96 instead of100, so the exact100-ton statement applies to the former entry.

Constructor VA0x61ebae–0x61ebb1 assigns the library directly to locomotive+0x138. VA0x61ebc9–0x61ebcf copies library+0x24 to locomotive+0x140 (Sec2). The section count therefore exists on this very object. The constructor even multiplies Torm+0x18 (VGR, main-reservoir volume; not CountTC at+0x10) by Sec at0x61ec7e–0x61ec8b. This confirms that Sec is relevant to the aggregate locomotive; GetWeight simply does not use it.

AddLoco VA0x62ca68 chooses between two branches that each construct one TLocomotive. The branches merge at0x62cac7. It then appends the same object once to train+8 at0x62cae2 and once to train+0x14 at0x62caf0. These are the all-elements list and the locomotive-only list, not two physical train elements. Independent disassembly shows:

- VA0x62dc94 returns `[train+8]+8` (all-elements count).
- VA0x62dcb0 returns `[train+0x14]+8` (locomotive-only count).
- VA0x62dde8 accesses `train+8` by index and performs no section expansion.
- The two append routines increment their respective counts by one.

InitPOEZD's ordinary-order loop indexes this all-elements list. At0x729522 it calls virtual+0xc (GetWeight), converts the returned integer to Single, pushes it, then calls the SetSostav pointer at0x729535. No multiplication or section argument intervenes. The reversed-order loop does the same at0x7295df–0x7295f2. GetAxes decides bogie-count parameters1/1 versus2/2 separately; it does not change the mass argument.

Configs separately verified that SetSostav receives this mass argument as-is and scales it by0.1 for force integration. No section-count field is passed to the DLL.

Conclusion: for a train entry consisting of **one forward2ЭС6 object (WightOneSec100, Sec2)** and6000tons of wagons, POEZD receives total masses summing to **6100tons**, although the task's full two-section locomotive assumption is200tons and therefore6200tons overall. This is a statically confirmed inconsistency in the aggregate locomotive physics, conditional on that train entry/library selection. It is not a measurement of which train is currently loaded at runtime. The missing100tons alone increases acceleration for identical net force by6200/6100−1≈1.639%, before other effects. Length and axle count also remain per-section values in these getters; their broader effect has not been quantified.
