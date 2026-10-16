"""Complete archive inventory, removing newly tracked sources and preserving empty dirs."""
import argparse, hashlib, json, os, pathlib, subprocess, zipfile
from pack_portable_environment import sha, SKIP, PRIVATE_DIRS

def main():
    ap=argparse.ArgumentParser(); ap.add_argument('--root',required=True); ap.add_argument('--out',required=True); ap.add_argument('--git',default='git'); args=ap.parse_args()
    root=pathlib.Path(args.root).resolve(); out=pathlib.Path(args.out).resolve()
    if root not in out.parents: raise ValueError('Output must stay inside workspace')
    mp=out/'portable-release.json'; manifest=json.loads(mp.read_text(encoding='utf8'))
    if 'source_commit' in manifest: manifest['payload_base_commit']=manifest.pop('source_commit')
    manifest['source_ref']='refs/tags/'+manifest['tag']
    inventory=json.loads((out/'environment-files.json').read_text(encoding='utf8'))
    tracked={p.decode('utf8').casefold() for p in subprocess.check_output([args.git,'-C',str(root),'ls-files','--cached','--others','--exclude-standard','-z']).split(b'\0') if p}
    for asset in manifest['assets']:
        dest=out/asset['name']
        with zipfile.ZipFile(dest) as old: unwanted={e.filename for e in old.infolist() if e.filename.casefold() in tracked}
        if not unwanted: continue
        temp=dest.with_suffix('.filtered.zip')
        with zipfile.ZipFile(dest) as old, zipfile.ZipFile(temp,'x',compression=zipfile.ZIP_DEFLATED,compresslevel=6,allowZip64=True) as new:
            for e in old.infolist():
                if e.filename in unwanted: continue
                with old.open(e) as inp,new.open(e,'w',force_zip64=True) as output:
                    for block in iter(lambda:inp.read(1024*1024),b''):output.write(block)
        with zipfile.ZipFile(temp) as archive:
            if archive.testzip(): raise ValueError('Filtered zip verification failed')
            entries=archive.infolist()
        backup=dest.with_suffix('.before-filter.zip')
        dest.rename(backup);temp.rename(dest)
        asset.update(size_bytes=dest.stat().st_size,sha256=sha(dest),files=len(entries),unpacked_bytes=sum(e.file_size for e in entries))
        inventory=[i for i in inventory if i['path'] not in unwanted]
        print('Removed source duplicates: '+','.join(sorted(unwanted)),flush=True)
    empty=[]
    for top in ('work','outputs'):
        for folder,dirs,files in os.walk(root/top,followlinks=False):
            dirs[:]=sorted(d for d in dirs if d not in PRIVATE_DIRS and not any(((pathlib.Path(folder)/d).relative_to(root).as_posix()+'/').startswith(p) for p in SKIP) and not (pathlib.Path(folder)/d).is_symlink())
            rel=pathlib.Path(folder).relative_to(root).as_posix()
            if not dirs and not files and not any((rel+'/').startswith(p) for p in SKIP):empty.append(rel+'/')
    if empty:
        name='trainer-empty-directories-001.zip'; dest=out/name
        if dest.exists():raise ValueError('Empty directory archive already exists')
        with zipfile.ZipFile(dest,'x',compression=zipfile.ZIP_DEFLATED) as archive:
            for rel in sorted(empty):
                info=zipfile.ZipInfo(rel,(2026,10,16,0,0,0));info.external_attr=(0o40755<<16)|0x10;archive.writestr(info,b'')
        manifest['assets'].append({'name':name,'size_bytes':dest.stat().st_size,'sha256':sha(dest),'files':0,'unpacked_bytes':0,'directories':len(empty)})
        manifest['empty_directories']=len(empty)
    (out/'environment-files.json').write_text(json.dumps(inventory,ensure_ascii=False,indent=2)+'\n',encoding='utf8')
    mp.write_text(json.dumps(manifest,ensure_ascii=False,indent=2)+'\n',encoding='utf8')
    print(json.dumps({'archives':len(manifest['assets']),'files':len(inventory),'empty_dirs':len(empty),'compressed_bytes':sum(a['size_bytes'] for a in manifest['assets'])}),flush=True)

if __name__=='__main__':main()
