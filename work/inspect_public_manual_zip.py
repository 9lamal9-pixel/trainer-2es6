import io, json, pathlib, urllib.request, zipfile, sys

API = 'https://cloud-api.yandex.net/v1/disk/public/resources?public_key=https%3A%2F%2Fyadi.sk%2Fd%2FBgsyV7dp3KwbS4'
meta = json.load(urllib.request.urlopen(API, timeout=30))

class RemoteFile(io.RawIOBase):
    def __init__(self, url, size):
        self.url, self.size, self.pos = url, size, 0
    def seekable(self): return True
    def readable(self): return True
    def tell(self): return self.pos
    def seek(self, offset, whence=0):
        self.pos = offset + (self.pos if whence == 1 else self.size if whence == 2 else 0)
        return self.pos
    def read(self, n=-1):
        if n < 0: n = self.size-self.pos
        n = min(n, self.size-self.pos)
        if n <= 0: return b''
        req=urllib.request.Request(self.url, headers={'Range':f'bytes={self.pos}-{self.pos+n-1}'})
        with urllib.request.urlopen(req,timeout=60) as r:
            if r.status != 206: raise RuntimeError(f'range ignored: {r.status}')
            out=r.read(n)
        self.pos+=len(out)
        return out

z = zipfile.ZipFile(RemoteFile(meta['file'], meta['size']))
entries = z.infolist()
for i, entry in enumerate(entries):
    print(i, entry.filename, entry.file_size, entry.compress_size)
if len(sys.argv) > 1:
    for chosen in map(int,sys.argv[1:]):
        entry=entries[chosen]
        if not entry.filename.lower().endswith('.pdf'): raise ValueError('Only PDF extraction permitted')
        out=pathlib.Path('work') / f'public_2es6_{chosen}.pdf'
        out.write_bytes(z.read(entry))
        print('Saved:',out)
