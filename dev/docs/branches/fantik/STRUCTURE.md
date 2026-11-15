# Структура ветки `fantik`

[Описание ветки](README.md) · [каталог веток](../README.md)

Снимок: `593100b от 2026-11-15 — Фантомас 2ТЕ116: исходники (декомпиляция .NET/Java, Delphi) и Trainer2TE116_0.7.4`. Относительно `main`: +1 коммит(ов).

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
dev/  — 105 файлов
outputs/  — 85 файлов
setup.cmd
trainer.cmd
work/  — 4093 файлов
```

## Изменения относительно main

```text
├── .gitignore  — изменено
└── work/
    ├── Trainer2TE116_0.7.4/
    │   ├── README.md  — добавлено
    │   ├── RELEASE_NOTES_0.7.4.md  — добавлено
    │   ├── VALIDATION_0.7.4.md  — добавлено
    │   ├── assets/
    │   │   └── scheme008/  — каталог: добавлено 3
    │   ├── build.ps1  — добавлено
    │   ├── module_integration_notes.md  — добавлено
    │   ├── package.ps1  — добавлено
    │   ├── pult_pulse_integration_notes.md  — добавлено
    │   ├── runs.ps1  — добавлено
    │   ├── setup/
    │   │   ├── install.ps1  — добавлено
    │   │   ├── uninstall.ps1  — добавлено
    │   │   └── update.ps1  — добавлено
    │   ├── src/
    │   │   ├── AutoDriver.cs  — добавлено
    │   │   ├── BrakeTest.cs  — добавлено
    │   │   ├── Cassette.cs  — добавлено
    │   │   ├── Circuits.cs  — добавлено
    │   │   ├── Consist.cs  — добавлено
    │   │   ├── Documents.cs  — добавлено
    │   │   ├── Dynamics.cs  — добавлено
    │   │   ├── Journal.cs  — добавлено
    │   │   ├── Links.cs  — добавлено
    │   │   ├── Log.cs  — добавлено
    │   │   ├── MainForm.cs  — добавлено
    │   │   ├── MapView.cs  — добавлено
    │   │   ├── OriginalSchemeView.cs  — добавлено
    │   │   ├── Pneumatics.cs  — добавлено
    │   │   ├── Program.cs  — добавлено
    │   │   ├── Pult.cs  — добавлено
    │   │   ├── Route.cs  — добавлено
    │   │   ├── Scheme.cs  — добавлено
    │   │   ├── SchemeData.cs  — добавлено
    │   │   ├── SchemeLink.cs  — добавлено
    │   │   ├── SchemeView.cs  — добавлено
    │   │   ├── Simulation.cs  — добавлено
    │   │   ├── Traction.cs  — добавлено
    │   │   ├── TripLog.cs  — добавлено
    │   │   └── Ui.cs  — добавлено
    │   ├── test/
    │   │   ├── Bench.cs  — добавлено
    │   │   ├── Pneumatic074Bench.cs  — добавлено
    │   │   ├── PultPulseBench.cs  — добавлено
    │   │   ├── PultTest.cs  — добавлено
    │   │   ├── Release074Bench.cs  — добавлено
    │   │   ├── SchemeBench.cs  — добавлено
    │   │   ├── SchemeIntegrationBench.cs  — добавлено
    │   │   ├── SchemeUi.cs  — добавлено
    │   │   ├── UiSmoke.cs  — добавлено
    │   │   └── UiWalk.cs  — добавлено
    │   ├── test.ps1  — добавлено
    │   ├── tools/
    │   │   ├── ExportScheme.cs  — добавлено
    │   │   └── ValidateUpdate074.ps1  — добавлено
    │   └── traction_integration_notes.md  — добавлено
    └── fantomas/
        ├── DECOMPILED/
        │   ├── README.md  — добавлено
        │   ├── dotnet/  — каталог: добавлено 344
        │   ├── java/  — каталог: добавлено 3361
        │   └── tools/  — каталог: добавлено 6
        └── video_tren/
            ├── Videofrm.pas  — добавлено
            └── video.pas  — добавлено
```

Файл сформирован из дерева Git; при изменении состава ветки его нужно обновить.
