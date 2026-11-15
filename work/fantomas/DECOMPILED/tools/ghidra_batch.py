# Ghidra headless batch: each binary of a queue -> <out_root>/<rel>.c (ExportCore.java from the 2ES6 pipeline),
# with .log and .json beside it. Usage: ghidra_batch.py <src_root> <out_root> <workers> <heap> <rel path> [<rel path> ...]
import os, sys, json, subprocess, time, hashlib, tempfile
from concurrent.futures import ThreadPoolExecutor
TEMP = r'C:\Users\sibir\AppData\Local\Temp\opencode'
GHIDRA = os.path.join(TEMP, r'ghidra\ghidra_12.2_PUBLIC\support\analyzeHeadless.bat')
JAVA = os.path.join(TEMP, r'jdk25\jdk-25.0.5+8')
SCRIPTS = r'C:\Users\sibir\Desktop\Макрос\Тренажер\ORIGINAL_DECOMPILED\tools'
src_root, out_root, workers, heap = sys.argv[1], sys.argv[2], int(sys.argv[3]), sys.argv[4]
queue = sys.argv[5:]
if len(queue) == 1 and queue[0].endswith('.json'):
    queue = json.load(open(queue[0], encoding='utf-8'))
import shutil
RESERVE = 4 * 1024 ** 3
proj_root = tempfile.mkdtemp(prefix='ghproj_', dir=os.path.dirname(os.path.abspath(__file__)))

def one(rel):
    src = os.path.join(src_root, rel)
    out = os.path.join(out_root, rel + '.c')
    os.makedirs(os.path.dirname(out), exist_ok=True)
    rep = out[:-2] + '.export.json'
    if os.path.exists(out) and os.path.exists(rep):
        try:
            if json.load(open(rep, encoding='utf-8')).get('complete'): return rel, 'cached'
        except Exception: pass
    free = shutil.disk_usage(out_root).free
    if free < RESERVE + 40 * os.path.getsize(src):
        with open(os.path.join(out_root, 'progress.log'), 'a', encoding='utf-8') as p:
            p.write('%s SKIP_DISK free=%dMB %s' % (time.strftime('%H:%M:%S'), free // 2**20, rel) + chr(10))
        return rel, 'skip_disk'
    sha = hashlib.sha256(open(src, 'rb').read()).hexdigest()
    name = 'p_' + sha[:16]
    cmd = ['cmd.exe', '/d', '/c', GHIDRA, proj_root, name, '-import', src, '-overwrite', '-deleteProject', '-max-cpu', '2',
           '-analysisTimeoutPerFile', '1800', '-scriptPath', SCRIPTS, '-postScript', 'ExportCore.java', out, rep]
    env = dict(os.environ, JAVA_HOME=JAVA, GHIDRA_HEADLESS_MAXMEM=heap)
    env['PATH'] = JAVA + r'\bin;' + env.get('PATH', '')
    t = time.time()
    with open(out[:-2] + '.ghidra.log', 'w', encoding='utf-8', errors='replace') as log:
        r = subprocess.run(cmd, env=env, cwd=proj_root, stdout=log, stderr=subprocess.STDOUT, stdin=subprocess.DEVNULL)
    ok = os.path.exists(rep) and json.load(open(rep, encoding='utf-8')).get('complete')
    with open(os.path.join(out_root, 'progress.log'), 'a', encoding='utf-8') as p:
        p.write('%s %s rc=%s %.0fs sha256=%s %s\n' % (time.strftime('%H:%M:%S'), 'OK' if ok else 'FAIL', r.returncode, time.time() - t, sha.upper(), rel))
    return rel, ok

with ThreadPoolExecutor(workers) as ex:
    for rel, st in ex.map(one, queue):
        print(st, rel, flush=True)
