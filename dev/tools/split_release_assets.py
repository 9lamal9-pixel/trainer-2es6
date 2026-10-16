"""Add small, resumable byte parts without changing logical ZIPs or restored files."""
import argparse, hashlib, json, pathlib

def main():
    ap=argparse.ArgumentParser();ap.add_argument('--directory',required=True);ap.add_argument('--part-mib',type=int,default=8);args=ap.parse_args()
    root=pathlib.Path(__file__).resolve().parents[2];out=pathlib.Path(args.directory).resolve()
    if root not in out.parents or not 1<=args.part_mib<=16:raise ValueError('Use workspace directory and 1..16 MiB parts')
    path=out/'portable-release.json';manifest=json.loads(path.read_text(encoding='utf8'));limit=args.part_mib*1024*1024
    transfers=0
    for asset in manifest['assets']:
        source=out/asset['name'];whole=hashlib.sha256();parts=[]
        if source.stat().st_size!=asset['size_bytes']:raise ValueError('Original ZIP size changed')
        with source.open('rb') as stream:
            if asset['size_bytes']<=limit:
                for block in iter(lambda:stream.read(1024*1024),b''):whole.update(block)
            else:
                for number,block in enumerate(iter(lambda:stream.read(limit),b''),1):
                    if number>999:raise ValueError('Too many parts')
                    whole.update(block);digest=hashlib.sha256(block).hexdigest();name=asset['name']+'.part%03d'%number;target=out/name
                    if target.exists():
                        if target.stat().st_size!=len(block) or hashlib.sha256(target.read_bytes()).hexdigest()!=digest:raise ValueError('Existing part differs: '+name)
                    else:
                        with target.open('xb') as f:f.write(block)
                    parts.append({'name':name,'size_bytes':len(block),'sha256':digest})
        if whole.hexdigest()!=asset['sha256']:raise ValueError('Original ZIP digest changed')
        if parts:asset['parts']=parts;transfers+=len(parts)
        else:asset.pop('parts',None);transfers+=1
        print('PARTS '+asset['name']+' '+str(len(parts) or 1),flush=True)
    manifest['transfer_part_bytes']=limit
    manifest['transfer_note']='Large logical ZIPs are byte-split. Setup downloads verified parts, assembles the unchanged ZIP and verifies its full SHA256 before extraction.'
    path.write_text(json.dumps(manifest,ensure_ascii=False,indent=2)+'\n',encoding='utf8')
    print(json.dumps({'archives':len(manifest['assets']),'transfer_assets':transfers,'bytes':sum(a['size_bytes'] for a in manifest['assets'])}),flush=True)

if __name__=='__main__':main()
