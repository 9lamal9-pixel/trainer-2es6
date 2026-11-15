# Структура ветки `codex/re-part1`

[Описание ветки](README.md) · [каталог веток](../README.md)

Снимок: `0282b42 от 2026-10-25 — Тренажёр: sa3-fracture-and-split`. Относительно `main`: +14 коммит(ов).

## Корень ветки

```text
.gitattributes
.gitignore
.rgignore
.vscode/  — 1 файлов
AGENTS.md
BOT_PROMPT.txt
README.md
Trainer.code-workspace
dev/  — 352 файлов
outputs/  — 112 файлов
setup.cmd
trainer.cmd
work/  — 339 файлов
```

## Область ветки (файлы и каталоги из описания)

```text
└── dev/
    ├── config/
    │   ├── re-part1-audit.json
    │   ├── re-part1-page-review.json
    │   └── sa47-release-policy.json
    ├── docs/
    │   ├── RE_PART1_AUDIT.md
    │   └── audit_parts/
    │       └── re_part1/
    │           ├── sa47-reset-current-validation-20261025.json
    │           └── sa47-reset-final-receipt-20261025.json
    └── environment-context.json
```

## Изменения относительно main

```text
├── README.md  — изменено
├── dev/
│   ├── CHANGELOG.jsonl  — изменено
│   ├── TrainerIntegrationHost.csproj  — изменено
│   ├── config/
│   │   ├── brake-rules-compliance.json  — изменено
│   │   ├── environment.json  — изменено
│   │   ├── modules.json  — изменено
│   │   ├── native-calibration-map.json  — добавлено
│   │   ├── probes.json  — изменено
│   │   ├── re-part1-audit.json  — добавлено
│   │   ├── re-part1-page-review.json  — добавлено
│   │   ├── references.json  — изменено
│   │   ├── sa47-release-policy.json  — добавлено
│   │   └── train-mechanics.json  — добавлено
│   ├── docs/
│   │   ├── 2ES6_SIMULATOR_DYNAMICS_AND_MPSUID_REPORT.md  — добавлено
│   │   ├── NATIVE_AND_MANAGED_MODULES_GUIDE.md  — добавлено
│   │   ├── RE_PART1_AUDIT.md  — добавлено
│   │   ├── SA3_FRACTURE_MODEL.md  — добавлено
│   │   ├── SCIENTIFIC_MECHANICS_PROFILE.md  — добавлено
│   │   ├── TRAIN_DYNAMICS_IMPLEMENTATION.md  — добавлено
│   │   ├── TRAIN_FORCE_VIEW.md  — добавлено
│   │   ├── audit_parts/
│   │   │   ├── re_part1/  — каталог: добавлено 74
│   │   │   ├── sa3_fracture_20261025/  — каталог: добавлено 35
│   │   │   ├── scientific_mechanics_20261025/  — каталог: добавлено 57
│   │   │   ├── train_dynamics_20261025/  — каталог: добавлено 37
│   │   │   └── train_force_view_20261025/  — каталог: добавлено 30
│   │   └── branches/
│   │       ├── autovedenie/  — каталог: добавлено 1
│   │       └── re-part1/  — каталог: добавлено 1
│   ├── environment-context.json  — изменено
│   └── scripts/
│       └── TrainerDev.Common.ps1  — изменено
├── outputs/
│   ├── TrainerAutovedenieAdverseBrakeProbe.cs  — изменено
│   ├── TrainerAutovedenieHost.cs  — изменено
│   ├── TrainerAuxSystems.cs  — изменено
│   ├── TrainerConsistSeparationNative.cs  — добавлено
│   ├── TrainerConsistSeparationProbe.cs  — добавлено
│   ├── TrainerContactNetwork.cs  — добавлено
│   ├── TrainerContactVoltagePatch.cs  — добавлено
│   ├── TrainerCouplerFractureProbe.cs  — добавлено
│   ├── TrainerFrictionDraftGear.cs  — добавлено
│   ├── TrainerIndependentExcitation.cs  — добавлено
│   ├── TrainerIntegrationHost.cs  — изменено
│   ├── TrainerLongitudinalDynamics.cs  — добавлено
│   ├── TrainerManagedFloatingScope.cs  — добавлено
│   ├── TrainerManagedMotion.cs  — добавлено
│   ├── TrainerManagedMotionProbe.cs  — добавлено
│   ├── TrainerModernTractionIntegration.cs  — добавлено
│   ├── TrainerModernTractionPhysics.cs  — добавлено
│   ├── TrainerNativeBrakeForceProvider.cs  — добавлено
│   ├── TrainerNativeBrakeRelease.cs  — добавлено
│   ├── TrainerNativeSystems.cs  — изменено
│   ├── TrainerPneumaticNetworkCandidate.cs  — изменено
│   ├── TrainerPolachAdhesion.cs  — добавлено
│   ├── TrainerRotatingInertia.cs  — добавлено
│   ├── TrainerRouteDOS.cs  — изменено
│   ├── TrainerRouteView.cs  — изменено
│   ├── TrainerSa47MemoryPatch.cs  — добавлено
│   ├── TrainerScientificBrakeForces.cs  — добавлено
│   ├── TrainerScientificBrakeValidationProbe.cs  — добавлено
│   ├── TrainerScientificMechanics.cs  — добавлено
│   ├── TrainerScientificMechanicsValidationProbe.cs  — добавлено
│   ├── TrainerTrainForceData.cs  — добавлено
│   ├── TrainerTrainForceDataProbe.cs  — добавлено
│   ├── TrainerTrainForceHost.cs  — добавлено
│   ├── TrainerTrainForceLiveProbe.cs  — добавлено
│   └── TrainerTrainForceView.cs  — добавлено
└── work/
    ├── pneumatic_live_fix/
    │   └── analysis/
    │       ├── CandidateAirHostProbe.cs  — изменено
    │       ├── ComputationalTrainDynamicsProbe.cs  — добавлено
    │       ├── ContactAndCouplerValidationProbe.cs  — добавлено
    │       ├── ContactNetworkVoltageProbe.cs  — добавлено
    │       ├── IndependentExcitationValidationProbe.cs  — добавлено
    │       ├── ManagedFloatingScopeValidationProbe.cs  — добавлено
    │       ├── ModernTractionIntegrationProbe.cs  — добавлено
    │       ├── ModernTractionValidationProbe.cs  — добавлено
    │       ├── RotatingInertiaValidationProbe.cs  — добавлено
    │       ├── Sa47ReleaseProbe.cs  — добавлено
    │       ├── ScientificCouplerValidationProbe.cs  — добавлено
    │       ├── SectionPantographPressureProbe.cs  — изменено
    │       ├── TrainDynamicsKernelValidationProbe.cs  — добавлено
    │       ├── Tyga750ClampObserver.cs  — добавлено
    │       ├── Tyga750ClampProbe.cs  — добавлено
    │       └── VcuAirSequenceProbe.cs  — изменено
    └── traction6000/
        ├── evidence.md  — изменено
        └── numeric.py  — изменено
```

Файл сформирован из дерева Git; при изменении состава ветки его нужно обновить.
