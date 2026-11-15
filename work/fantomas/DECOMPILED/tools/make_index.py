# README of фантомас/DECOMPILED: what was decompiled, with what, and where it is.
import os, sys, json, re, shutil, hashlib
sys.stdout.reconfigure(encoding='utf-8')
ROOT = r'C:\Users\sibir\Desktop\Макрос\Тренажер\фантомас'
OUT = os.path.join(ROOT, 'DECOMPILED')
HERE = os.path.dirname(os.path.abspath(__file__))
ORIG = r'C:\Users\sibir\Desktop\Макрос\Тренажер\ORIGINAL_DECOMPILED'

rows = json.load(open(os.path.join(HERE, 'fantomas_pe.json'), encoding='utf-8'))
bypath = {r['path']: r for r in rows}
own = [l.split(None, 3) for l in open(os.path.join(HERE, 'own.txt'), encoding='utf-8').read().splitlines() if l[:1] in 'mn' and len(l.split(None, 3)) == 4]

# native: last status per module from progress.log
status = {}
for l in open(os.path.join(OUT, 'native', 'progress.log'), encoding='utf-8'):
    p = l.split()
    if len(p) >= 5 and p[1] in ('OK', 'FAIL', 'SKIP_DISK'):
        status[p[-1]] = (p[1], [x for x in p if x.endswith('s') and x[:-1].replace('.', '').isdigit()])
inv = json.load(open(os.path.join(ORIG, 'ALL_BINARIES', 'inventory.json'), encoding='utf-8'))
known = {r['sha256'].upper(): r['path'] for r in inv}
full = os.path.join(ORIG, 'FULL', 'modules')
done2es6 = {x.upper(): x for x in os.listdir(full)} if os.path.isdir(full) else {}

native, dotnet = [], []
for kind, mach, size, rel in own:
    r = bypath[rel]
    if kind == 'managed':
        dotnet.append((rel, mach, int(size), r['sha256']))
        continue
    sha = r['sha256']
    c = os.path.join(OUT, 'native', rel.replace('/', os.sep) + '.c')
    rep = c[:-2] + '.export.json'
    info = json.load(open(rep, encoding='utf-8')) if os.path.exists(rep) else None
    if info and info.get('complete'):
        res = '%d функций%s, %.1f МБ' % (info['functions'], (', не восстановлено %d' % info['failed']) if info.get('failed') else '', os.path.getsize(c) / 2**20)
        where = '`native/' + rel + '.c`'
    elif sha in done2es6:
        res = 'совпадает с 2ЭС6'
        where = '`ORIGINAL_DECOMPILED/FULL/modules/%s/pseudocode.c` (%s)' % (done2es6[sha], known.get(sha, ''))
    elif re.search(r'(Unigine_double|FbxImporter|NCSEcw|Noesis|gdal|openal32|zlib\.dll|vccorlib|AppWall_double|Geodetics_double|Interface_double|Syncker_double|weather_double|jre-8u|vcredist|oalinst|Dxvideo8)', rel, re.I):
        res = 'сторонний, не декомпилировался'; where = '—'
    else:
        st = status.get(rel, ('нет', []))[0]
        res = {'FAIL': 'не получилось', 'SKIP_DISK': 'пропущен: мало места', 'нет': 'не запускался'}.get(st, st); where = '—'
    native.append((rel, mach, int(size), sha, res, where))

L = []
L.append('# «Фантомас»: декомпилированный код\n')
L.append('Получено 07.11.2026 из копии тренажёра `фантомас/` (исходная установка не менялась). Это восстановленный код, а не исходники разработчика: имена локальных переменных, комментарии и часть типов утрачены. Открытых исходников в интернете по уникальным именам (`ScenarioCreater`, `TrainModuleImport`, `Te116ModuleImport`, `CargoPnevmoSystem`) не нашлось.\n')
L.append('| Папка | Что | Инструмент |\n|---|---|---|')
L.append('| `dotnet/` | C#-проекты .NET-сборок (по файлу на тип) | ILSpy `ilspycmd` 11.2.0.10050 (nuget.org, `ilspycmd.win-x64`): `ilspycmd -p -o <папка> --nested-directories -r <папка сборки> <сборка>` |')
L.append('| `java/Block_v0.2.6/` | Java-код БЛОК (`block/Block_v0.2.6.jar`); собственный код — `base/`, остальное — встроенный JavaFX | CFR 0.152 (Maven Central, SHA-1 48ef4892…): `java -jar cfr-0.152.jar Block_v0.2.6.jar --outputdir <папка>` |')
L.append('| `native/` | Псевдо-C нативных DLL и EXE, все функции; рядом `.ghidra.log` и `.export.json` | Ghidra 12.2 headless + `ExportCore.java` из конвейера 2ЭС6 (`tools/ghidra_batch.py`) |')
L.append('')
L.append('Главное для модели 2ТЭ116 — `native/work/Release/Lib/TrainModule.dll.c` (`FUN_180002f90` — шаг движения поезда), `dotnet/work_Release__instructor/IWP/TrainMovementController.cs`, `Train.cs`, `Wagon.cs`; разбор — в `dev/fantomas/brake_proxy/README.md`, раздел «Движение поезда».\n')
L.append('## .NET\n\n| Сборка | Платформа | Размер | SHA-256 | Проект |\n|---|---|---:|---|---|')
names = {'work/Release/instructor.exe': 'work_Release__instructor', 'work/Release/instructor20211119.exe': 'work_Release__instructor20211119', 'work/Release/instructor_.exe': 'work_Release__instructor_',
         'work/Release/ScenarioCreater.exe': 'work_Release__ScenarioCreater', 'work/Release/NetLib.dll': 'work_Release__NetLib', 'work/Release/TcpAsyncNetLib.dll': 'work_Release__TcpAsyncNetLib',
         'DatabaseManager/ATR.exe': 'DatabaseManager__ATR', 'DatabaseManager/ELS.exe': 'DatabaseManager__ELS', 'video_tren/Calibrator V2.exe': 'video_tren__Calibrator_V2'}
for rel, mach, size, sha in sorted(dotnet):
    d = names.get(rel)
    if d and os.path.isdir(os.path.join(OUT, 'dotnet', d)):
        n = sum(1 for a, b, fs in os.walk(os.path.join(OUT, 'dotnet', d)) for f in fs if f.endswith('.cs'))
        proj = '`dotnet/%s/` (%d .cs)' % (d, n)
    elif 'FantomasBrakes' in rel or rel.endswith('Lib/2Te116.dll'):
        proj = 'наша модель, исходники в `dev/fantomas/brake_proxy`'
    else:
        proj = 'совпадает с `video_tren/Calibrator V2.exe`' if 'Calibrator' in rel else '—'
    L.append('| `%s` | %s | %d | %s… | %s |' % (rel, mach, size, sha[:16], proj))
L.append('\nВерсии инструктора:\n- `instructor.exe` и `instructor_.exe` — один и тот же код (различаются только метаданные сборки);\n- `instructor20211119.exe` — отладочная сборка: в `Train.Update` после тяги вставлено `if (Speed < 250f) WagonSetPull(0, 2000f)`, поезд разгоняется сам. Для занятий её не запускать.')
L.append('\n## Нативные модули\n\nОчередь — от маленьких к большим, 4 потока. Модули с тем же SHA-256, что у 2ЭС6, взяты из `ORIGINAL_DECOMPILED`.\n')
L.append('| Модуль | Платформа | Размер | SHA-256 | Результат | Где |\n|---|---|---:|---|---|---|')
for rel, mach, size, sha, res, where in sorted(native):
    L.append('| `%s` | %s | %d | %s… | %s | %s |' % (rel, mach, size, sha[:16], res, where))
ok = sum(1 for x in native if 'функций' in x[4]); same = sum(1 for x in native if 'совпадает' in x[4]); bad = [x for x in native if x[4] in ('не получилось', 'не запускался', 'пропущен: мало места')]
L.append('\nИтого нативных: %d декомпилировано, %d совпадают с 2ЭС6, %d сторонних, не получилось — %d.' % (ok, same, sum(1 for x in native if 'сторонний' in x[4]), len(bad)))
L.append('\n## Как повторить\n\n`tools/`: `inventory.py` (опись PE: управляемый или нет, платформа, SHA-256), `ghidra_batch.py` (очередь Ghidra с защитой свободного места на диске), `pedis.py` (быстрый дизассемблер capstone с подписью констант), `queue_native.json`, `fantomas_pe.json`. Ghidra 12.2 и JDK 25 — в `%TEMP%\\opencode` (из конвейера 2ЭС6).')
open(os.path.join(OUT, 'README.md'), 'w', encoding='utf-8', newline='\n').write('\n'.join(L) + '\n')
os.makedirs(os.path.join(OUT, 'tools'), exist_ok=True)
for f in ['inventory.py', 'ghidra_batch.py', 'pedis.py', 'queue_native.json', 'fantomas_pe.json', 'make_index.py']:
    shutil.copy2(os.path.join(HERE, f), os.path.join(OUT, 'tools', f))
print('native', len(native), 'ok', ok, 'same', same, 'bad', [x[0] for x in bad])
