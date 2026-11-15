# Каталог веток

У каждой рабочей ветки есть своя папка здесь: `README.md` — описание области, `STRUCTURE.md` — структура ветки. Общая карта — в [корневом README](../../../README.md#карта-веток).

Состояние на 15.11.2026, `main` = `d53fece`.

## Тематические ветки 2ЭС6

Каждая содержит полный проект; отличие от `main` — один коммит с описанием области.

| Ветка | Область | Описание | Структура |
|---|---|---|---|
| `codex/traction` | Штатная тяга, команды контроллера, электрический цикл и НВ | [README](traction/README.md) | [STRUCTURE](traction/STRUCTURE.md) |
| `codex/mpsuid` | Показания, меню, диагностика и исправления экранов МСУЛ | [README](mpsuid/README.md) | [STRUCTURE](mpsuid/STRUCTURE.md) |
| `codex/pneumatics` | Воздушная сеть, компрессоры, тормоза, масса состава | [README](pneumatics/README.md) | [STRUCTURE](pneumatics/STRUCTURE.md) |
| `codex/safety` | КЛУБ-У, САУТ, бдительность, приоритеты торможения | [README](safety/README.md) | [STRUCTURE](safety/STRUCTURE.md) |
| `codex/routes` | Режимная карта, профиль пути, ограничения, сигналы, 3D | [README](routes/README.md) | [STRUCTURE](routes/STRUCTURE.md) |
| `codex/autovedenie` | Контроллер АВП, тяга/торможение, интерфейс, калибровки | [README](autovedenie/README.md) | [STRUCTURE](autovedenie/STRUCTURE.md) |
| `codex/environment` | Настройка Windows, сборка, пробы, упаковка Release | [README](environment/README.md) | [STRUCTURE](environment/STRUCTURE.md) |

## Ветки с собственной работой

| Ветка | Что в ней | Относительно `main` | Описание | Структура |
|---|---|---|---|---|
| `codex/re-part1` | Постраничное соответствие первой части РЭ, SA47, напряжение сети, продольная динамика, разрыв СА-3 | +14 коммитов, 310 файлов; выросла из `codex/autovedenie` | [README](re-part1/README.md) | [STRUCTURE](re-part1/STRUCTURE.md) |
| `fantik` | Тренажёр 2ТЭ116 «Фантомас»: декомпиляция и новый инструктор 0.7.4 | +1 коммит, 3768 файлов | [README](fantik/README.md) | [STRUCTURE](fantik/STRUCTURE.md) |

Описание `codex/re-part1` заканчивается на этапе SA47; более поздние коммиты ветки (напряжение сети, динамика, СА-3) в нём ещё не отражены — их состав виден в структуре.

## Служебные ветки и дубликаты

Своих папок не имеют: собственного содержимого в них нет или это одна правка.

| Ветка | Состояние | Что с ней делать |
|---|---|---|
| `main` | Общая база, тег `trainer-environment-2026-10-16` на `a04ed4b` | — |
| `0.7.4` | Указывает на тот же коммит, что `main`; своих коммитов нет. Материалы 0.7.4 лежат в `fantik` | Удалить или перенести на `fantik` |
| `claude/fantik-sources-to-git-0a3d7f` | Тот же коммит `593100b`, что и `fantik` | Удалить как дубликат |
| `claude/cool-thompson-c8wj97` | +1 коммит `666b01c`: `dev/scripts/TrainerDev.Common.ps1` приведён к CRLF по `.gitattributes` | Слить в `main`, затем удалить |
| `claude/focused-thompson-8fc7b6` | Этот каталог веток | Слить в `main`, затем удалить |

## Правила

- Имя: `codex/<область>` для подсистем 2ЭС6; отдельные продукты — без префикса (`fantik`); `claude/*` — временные ветки сеансов, после слияния удаляются.
- Новая ветка получает папку `dev/docs/branches/<имя>/` с `README.md` и `STRUCTURE.md` и строку в этом каталоге и в корневом README.
- Завершённые изменения сливаются в `main`, затем тематические ветки обновляются из `main`. README областей здесь побайтно совпадают с версиями в самих ветках, поэтому слияние `main` в ветку не даёт конфликта.
- `STRUCTURE.md` — снимок дерева Git на указанный коммит; после изменения состава ветки его нужно обновить.
