"""Verify restored files against the release's per-file SHA inventory."""
import argparse, concurrent.futures, hashlib, json, pathlib, zipfile

def main():
    ap=argparse.ArgumentParser();ap.add_argument('--root',required=True);ap.add_argument('--assets',required=True);ap.add_argument('--report',required=True);args=ap.parse_args()
    root=pathlib.Path(args.root).resolve(); assets=pathlib.Path(args.assets).resolve()
    inventory=json.loads((assets/'environment-files.json').read_text(encoding='utf8'))
    manifest=json.loads((assets/'portable-release.json').read_text(encoding='utf8'))
    def check(item):
        p=(root/item['path']).resolve()
        if root not in p.parents:return item['path']
        try:
            if not p.is_file() or p.stat().st_size!=item['bytes']:return item['path']
            h=hashlib.sha256()
            with p.open('rb') as stream:
                for b in iter(lambda:stream.read(1024*1024),b''):h.update(b)
            return None if h.hexdigest()==item['sha256'] else item['path']
        except OSError:return item['path']
    failed=[];count=0
    with concurrent.futures.ThreadPoolExecutor(max_workers=8) as pool:
        for result in pool.map(check,inventory):
            count+=1
            if result:failed.append(result)
            if count%5000==0:print('VERIFIED files='+str(count),flush=True)
    directories=0
    for asset in manifest['assets']:
        if not asset.get('directories'):continue
        with zipfile.ZipFile(assets/asset['name']) as archive:
            for entry in archive.infolist():
                if entry.is_dir():
                    p=(root/entry.filename).resolve();directories+=1
                    if root not in p.parents or not p.is_dir():failed.append(entry.filename)
    report={'passed':not failed,'files_verified':count,'directories_verified':directories,'bytes_verified':sum(i['bytes'] for i in inventory),'failed_count':len(failed),'failed_paths':failed[:30]}
    target=pathlib.Path(args.report).resolve()
    if root not in target.parents:raise ValueError('Report must remain inside restored workspace')
    target.parent.mkdir(parents=True,exist_ok=True);target.write_text(json.dumps(report,indent=2)+'\n',encoding='utf8')
    print(json.dumps(report),flush=True)
    raise SystemExit(0 if report['passed'] else 1)

if __name__=='__main__':main()
