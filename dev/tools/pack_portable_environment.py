"""Build immutable, size-bounded release payloads from this workspace (stdlib only)."""
import argparse, hashlib, json, os, pathlib, subprocess, zipfile

SKIP = ('work/3dfab_domain_audit/', 'dev/archives/', 'dev/state/')
PRIVATE_DIRS = {'.git', '.codex', '.agents', '.aws', '.ssh', '__pycache__'}

def sha(path):
    h = hashlib.sha256()
    with open(path, 'rb') as stream:
        for block in iter(lambda: stream.read(1024 * 1024), b''): h.update(block)
    return h.hexdigest()

def main():
    ap = argparse.ArgumentParser()
    ap.add_argument('--root', required=True)
    ap.add_argument('--out', required=True)
    ap.add_argument('--git', default='git')
    ap.add_argument('--resume', action='store_true')
    ap.add_argument('--tag', default='trainer-environment-2026-10-16')
    args = ap.parse_args()
    root, out = pathlib.Path(args.root).resolve(), pathlib.Path(args.out).resolve()
    if root not in out.parents: raise ValueError('Output must stay in the workspace')
    out.mkdir(parents=True, exist_ok=True)
    if any(out.glob('*.zip')) and not args.resume: raise ValueError('Use a new folder or --resume')
    tracked_raw = subprocess.check_output([args.git, '-C', str(root), 'ls-files', '--cached', '--others', '--exclude-standard', '-z'])
    tracked = {p.decode('utf-8').casefold() for p in tracked_raw.split(b'\0') if p}
    print('CHECK native DLL guards', flush=True)
    native = json.loads((root / 'dev/config/native-lock.json').read_text(encoding='utf-8-sig'))
    for item in native['files']:
        if sha(root / item['path']).lower() != item['sha256'].lower():
            raise ValueError('Native DLL guard failed: ' + item['path'])
    print('SCAN payload', flush=True)
    reused_assets, inventory, reused_paths = [], [], set()
    if args.resume:
        for dest in sorted(out.glob('trainer-*.zip')):
            if not zipfile.is_zipfile(dest):
                dest.rename(dest.with_suffix('.incomplete'))
                continue
            print('RESUME ' + dest.name, flush=True)
            count, length = 0, 0
            with zipfile.ZipFile(dest) as archive:
                for entry in archive.infolist():
                    if entry.is_dir(): continue
                    if entry.filename.casefold() in tracked: raise ValueError('Tracked source in reused archive: ' + entry.filename)
                    h = hashlib.sha256()
                    with archive.open(entry) as stream:
                        for block in iter(lambda: stream.read(1024 * 1024), b''): h.update(block)
                    count += 1; length += entry.file_size; reused_paths.add(entry.filename.casefold())
                    inventory.append({'path':entry.filename,'bytes':entry.file_size,'sha256':h.hexdigest(),'asset':dest.name})
            reused_assets.append({'name':dest.name,'size_bytes':dest.stat().st_size,'sha256':sha(dest),'files':count,'unpacked_bytes':length})
    groups = {}
    for top in ('work', 'outputs', 'dev'):
        for folder, dirs, files in os.walk(root / top, followlinks=False):
            dirs[:] = sorted(d for d in dirs if d not in PRIVATE_DIRS and not any(((pathlib.Path(folder) / d).relative_to(root).as_posix() + '/').startswith(p) for p in SKIP) and not (pathlib.Path(folder) / d).is_symlink())
            for filename in sorted(files):
                file = pathlib.Path(folder) / filename
                rel = file.relative_to(root).as_posix()
                if rel.casefold() in tracked or rel.casefold() in reused_paths or any(rel.startswith(p) for p in SKIP): continue
                if top == 'dev' and not rel.startswith(('dev/runs/', 'dev/builds/', 'dev/checkpoints/', 'dev/history/')): continue
                if file.is_symlink(): raise ValueError('Symbolic link not packaged: ' + rel)
                if filename.lower() in ('.env', '.git-credentials', 'id_rsa', 'id_ed25519') or file.suffix.lower() in ('.pfx', '.pem', '.key'):
                    raise ValueError('Review sensitive-looking file before packaging: ' + rel)
                if rel.startswith('work/native_distribution/'): group = 'native-distribution'
                elif rel.startswith('work/trenager_static/'): group = 'native-runtime'
                elif top == 'work': group = 'research-materials'
                elif top == 'outputs': group = 'outputs-data'
                else: group = 'development-history'
                groups.setdefault(group, []).append((rel, file.stat().st_size))
    manifest = {'schema_version': 1, 'repository': '9lamal9-pixel/trainer-2es6', 'tag': args.tag,
                'payload_base_commit': subprocess.check_output([args.git, '-C', str(root), 'rev-parse', 'HEAD'], text=True).strip(),
                'source_ref': 'refs/tags/' + args.tag,
                'scope': 'All untracked data in work, outputs, dev; tracked files stay in Git. Excludes dev/state, dev/archives, work/3dfab_domain_audit, caches and credential directories. Historical binaries/results are reference snapshots, not acceptance of current sources.',
                'assets': reused_assets}
    limit = 512 * 1024 * 1024
    for group in sorted(groups):
        chunks, chunk, size = [], [], 0
        for rel, length in sorted(groups[group]):
            if chunk and size + length > limit: chunks.append(chunk); chunk, size = [], 0
            if length > limit: raise ValueError('Single file larger than partition: ' + rel)
            chunk.append((rel, length)); size += length
        if chunk: chunks.append(chunk)
        for number, items in enumerate(chunks, 1):
            name = 'trainer-' + group + '-%03d.zip' % number
            dest = out / name
            while dest.exists():
                number += 1; name = 'trainer-' + group + '-%03d.zip' % number; dest = out / name
            print('PACK ' + name + ' files=' + str(len(items)), flush=True)
            with zipfile.ZipFile(dest, 'x', compression=zipfile.ZIP_DEFLATED, compresslevel=6, allowZip64=True) as archive:
                for rel, expected_size in items:
                    source = root / rel
                    before = source.stat()
                    digest = hashlib.sha256()
                    info = zipfile.ZipInfo(rel, (2026, 10, 16, 0, 0, 0))
                    info.compress_type = zipfile.ZIP_DEFLATED
                    info.external_attr = 0o100644 << 16
                    with source.open('rb') as stream, archive.open(info, 'w', force_zip64=True) as output:
                        for block in iter(lambda: stream.read(1024 * 1024), b''):
                            digest.update(block); output.write(block)
                    after = source.stat()
                    if before.st_size != expected_size or before.st_size != after.st_size or before.st_mtime_ns != after.st_mtime_ns:
                        raise ValueError('File changed during snapshot: ' + rel)
                    if len(inventory) % 1000 == 0: print('FILES ' + str(len(inventory)) + ' latest=' + rel, flush=True)
                    inventory.append({'path': rel, 'bytes': expected_size, 'sha256': digest.hexdigest(), 'asset': name})
            with zipfile.ZipFile(dest, 'r') as archive:
                if archive.testzip() is not None: raise ValueError('ZIP CRC validation failed: ' + name)
            asset = {'name': name, 'size_bytes': dest.stat().st_size, 'sha256': sha(dest), 'files': len(items), 'unpacked_bytes': sum(n for _, n in items)}
            if asset['size_bytes'] >= 1900 * 1024 * 1024: raise ValueError('Asset exceeds safe GitHub limit')
            manifest['assets'].append(asset)
            (out / 'portable-release.partial.json').write_text(json.dumps(manifest, ensure_ascii=False, indent=2), encoding='utf-8')
            print('READY ' + json.dumps(asset), flush=True)
    (out / 'environment-files.json').write_text(json.dumps(inventory, ensure_ascii=False, indent=2) + '\n', encoding='utf-8')
    (out / 'portable-release.json').write_text(json.dumps(manifest, ensure_ascii=False, indent=2) + '\n', encoding='utf-8')
    print('DONE ' + json.dumps({'files': len(inventory), 'bytes': sum(i['bytes'] for i in inventory), 'assets': len(manifest['assets'])}), flush=True)

if __name__ == '__main__': main()
