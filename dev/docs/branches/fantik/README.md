# «Фантомас» 2ТЭ116: декомпиляция и новый инструктор

Ветка: `fantik`. [Общая карта проекта](../../../../README.md#карта-веток) · [структура ветки](STRUCTURE.md).

Ветка содержит полный проект 2ЭС6 из `main` и поверх него материалы по другому тренажёру — 2ТЭ116 «Фантомас». Код 2ЭС6 (`outputs`, `dev`) в ней не менялся; все добавления лежат в `work/fantomas` и `work/Trainer2TE116_0.7.4`.

## Состав

- [`work/fantomas/DECOMPILED`](../../../../work/fantomas/DECOMPILED) — восстановленный код штатного ПО «Фантомаса»; описание и таблица сборок с SHA-256 — в [README каталога](../../../../work/fantomas/DECOMPILED/README.md).
  - `dotnet/` — 9 C#-проектов (ILSpy): `instructor` в трёх вариантах, `ScenarioCreater`, `NetLib`, `TcpAsyncNetLib`, `DatabaseManager` (ATR, ELS), `Calibrator V2`.
  - `java/Block_v0.2.6/` — Java-код БЛОК (CFR); собственный код — `base/` (29 файлов), остальное — встроенные библиотеки JavaFX и log4j.
  - `tools/` — скрипты инвентаризации и пакетной декомпиляции (`inventory.py`, `ghidra_batch.py`, `make_index.py`, `pedis.py`) и их очереди.
- [`work/fantomas/video_tren`](../../../../work/fantomas/video_tren) — Delphi-исходники видеосистемы (`video.pas`, `Videofrm.pas`).
- [`work/Trainer2TE116_0.7.4`](../../../../work/Trainer2TE116_0.7.4) — новый инструктор 2ТЭ116, написанный с нуля: `src/` (25 модулей C#), `test/` (10 стендов), `setup/` (установка, обновление, удаление), `tools/`, `assets/scheme008`, скрипты `build.ps1`, `test.ps1`, `package.ps1`, `runs.ps1`. Документы: [README](../../../../work/Trainer2TE116_0.7.4/README.md), [изменения 0.7.4](../../../../work/Trainer2TE116_0.7.4/RELEASE_NOTES_0.7.4.md), [проверка 0.7.4](../../../../work/Trainer2TE116_0.7.4/VALIDATION_0.7.4.md).
- `.gitignore` — добавлены исключения, разрешающие хранить эти исходники.

## Чего в ветке нет

- `work/fantomas/DECOMPILED/native` (псевдо-C нативных DLL и EXE) — исключён из Git, хотя README каталога на него ссылается.
- `dev/fantomas/brake_proxy` и `docs/fantomas/*.md` (ТЗ, описание схемы) — на них ссылаются README, но в ветку они не добавлены.
- Собранные EXE, пакеты `out/dist` и отчёты `validation/` — остаются вне Git.

## Проверки на Windows

Стенды инструктора запускаются из `work/Trainer2TE116_0.7.4`:

```powershell
.\test.ps1
```

По [VALIDATION_0.7.4.md](../../../../work/Trainer2TE116_0.7.4/VALIDATION_0.7.4.md) от 15.11.2026 все 10 стендов завершились с кодом 0. Это сведение из документа ветки, а не новый прогон при её описании.

## Текущее состояние

Один коммит поверх `main` (`593100b`). Работа с настоящей кабиной, видеосистемой и БЛОК не проверялась. Ветка не предназначена для слияния в `main` в текущем виде: она добавляет около 3770 файлов чужого декомпилированного кода и отдельный продукт.
