// Maintainer utility: upload immutable payloads using credentials only in memory.
import fs from 'node:fs';
import fsp from 'node:fs/promises';
import path from 'node:path';
import https from 'node:https';
import { fileURLToPath } from 'node:url';
import crypto from 'node:crypto';
import { execFileSync } from 'node:child_process';
const argv = process.argv.slice(2);
function arg(name) { const i=argv.indexOf(name); if(i<0||!argv[i+1])throw Error('Missing '+name); return argv[i+1]; }
const root=path.resolve(path.dirname(fileURLToPath(import.meta.url)),'../..');
const directory=path.resolve(arg('--assets'));
const commit=arg('--commit');
const verifyOnly=argv.includes('--verify');
if(verifyOnly&&argv.includes('--publish'))throw Error('--verify cannot publish or mutate a release');
if(!/^[a-f0-9]{40}$/.test(commit))throw Error('Use a full commit SHA');
const manifest=JSON.parse(await fsp.readFile(path.join(directory,'portable-release.json'),'utf8'));
if(manifest.repository!=='9lamal9-pixel/trainer-2es6'||manifest.tag!=='trainer-environment-2026-10-16')throw Error('Unexpected destination');
const git=argv.includes('--git')?arg('--git'):'git';
let token=process.env.GH_TOKEN||process.env.GITHUB_TOKEN;
if(!token){
 let credential;
 try{credential=execFileSync(git,['-c','credential.interactive=never','credential','fill'],{cwd:root,windowsHide:true,input:'protocol=https\nhost=github.com\n\n',encoding:'utf8',stdio:['pipe','pipe','pipe'],env:{...process.env,GIT_TERMINAL_PROMPT:'0'}});}
 catch{throw Error('GitHub credential lookup failed; configure Git Credential Manager or GH_TOKEN. Credential output was suppressed.');}
 token=credential.split(/\r?\n/).find(x=>x.startsWith('password='))?.slice(9);
 credential=null;
}
if(!token)throw Error('No GitHub credential available');
const base='https://api.github.com/repos/'+manifest.repository;
async function hash(file){const h=crypto.createHash('sha256');for await(const block of fs.createReadStream(file))h.update(block);return h.digest('hex');}
function request(url,method='GET',payload=null,file=null){return new Promise((resolve,reject)=>{
 const u=new URL(url);if(!['api.github.com','uploads.github.com'].includes(u.hostname))return reject(Error('Unexpected API host'));
 const body=payload?Buffer.from(JSON.stringify(payload)):null;
 const headers={'User-Agent':'trainer-portable-release','Authorization':'Bearer '+token,'Accept':'application/vnd.github+json','X-GitHub-Api-Version':'2022-11-28'};
 if(body){headers['Content-Type']='application/json';headers['Content-Length']=body.length;}
 if(file){headers['Content-Type']='application/octet-stream';headers['Content-Length']=fs.statSync(file).size;}
 const req=https.request(u,{method,headers},res=>{let data='';res.setEncoding('utf8');res.on('data',x=>data+=x);res.on('end',()=>{
  let json;try{json=JSON.parse(data||'{}');}catch{return reject(Error('Non-JSON response '+res.statusCode));}
  if(res.statusCode<200||res.statusCode>=300){const error=Error('GitHub HTTP '+res.statusCode+': '+(json.message||'request failed'));error.status=res.statusCode;return reject(error);}
  resolve(json);
 });});
 req.on('error',reject);req.setTimeout(600000,()=>req.destroy(Error('GitHub request timed out')));
 if(file){const stream=fs.createReadStream(file);stream.on('error',e=>req.destroy(e));stream.pipe(req);}else req.end(body);
});}
async function verifyRemoteTag(allowMissing){
 let ref;
 try{ref=await request(base+'/git/ref/tags/'+encodeURIComponent(manifest.tag));}
 catch(e){if(e.status===404&&allowMissing)return false;throw e;}
 let object=ref.object;
 for(let depth=0;object?.type==='tag'&&depth<5;depth++){
  const tag=await request(base+'/git/tags/'+object.sha);object=tag.object;
 }
 if(object?.type!=='commit'||object.sha!==commit)throw Error('Remote release tag does not resolve to the requested commit');
 return true;
}
const remoteCommit=await request(base+'/commits/'+commit);
if(remoteCommit.sha!==commit)throw Error('Requested commit is not available in the GitHub repository');
const notes=await fsp.readFile(arg('--notes'),'utf8');
await verifyRemoteTag(true);
let release;try{release=await request(base+'/releases/tags/'+manifest.tag);}catch(e){if(e.status!==404)throw e;}
if(!release&&verifyOnly)throw Error('Release does not exist; --verify does not create it');
if(!release)release=await request(base+'/releases','POST',{tag_name:manifest.tag,target_commitish:commit,name:'Полная среда разработки 2ЭС6 — 16.10.2026',body:notes,draft:true,prerelease:true});
if(release.tag_name!==manifest.tag)throw Error('Tag mismatch');
if(release.draft && release.target_commitish!==commit){
 if(verifyOnly||!argv.includes('--update-draft-target'))throw Error('Draft targets a different commit; use --update-draft-target only after validating the new commit');
 if(await verifyRemoteTag(true))throw Error('Refusing to move an existing tag');
 release=await request(base+'/releases/'+release.id,'PATCH',{target_commitish:commit});
 if(release.target_commitish!==commit)throw Error('Draft target update failed');
}
if(!release.draft&&!verifyOnly)throw Error('Refusing to mutate a published release');
await verifyRemoteTag(release.draft);
const metadataNames=['portable-release.json','environment-files.json'];
const validationFile=path.join(directory,'validation-report.json');
const validationExists=await fsp.access(validationFile).then(()=>true,()=>false);
if(validationExists)metadataNames.push('validation-report.json');
if(argv.includes('--publish')){
 if(!validationExists)throw Error('A successful validation-report.json is required before publishing');
 const validation=JSON.parse(await fsp.readFile(validationFile,'utf8'));
 if(validation.passed!==true||validation.commit!==commit||validation.payload_manifest_sha256!==await hash(path.join(directory,'portable-release.json')))throw Error('Validation report does not accept this commit and manifest');
}
const names=new Set();
for(const asset of manifest.assets){
 if(!/^[A-Za-z0-9][A-Za-z0-9._-]*\.zip$/.test(asset.name)||names.has(asset.name.toLowerCase())||!/^[a-fA-F0-9]{64}$/.test(asset.sha256)||!Number.isSafeInteger(asset.size_bytes)||asset.size_bytes<=0)throw Error('Invalid local archive metadata');
 names.add(asset.name.toLowerCase());
}
const expected=[...manifest.assets];for(const name of metadataNames){const file=path.join(directory,name);expected.push({name,size_bytes:(await fsp.stat(file)).size,sha256:await hash(file)});}
for(const asset of expected){
 const file=path.join(directory,asset.name);
 if((await fsp.stat(file)).size!==asset.size_bytes||await hash(file)!==asset.sha256)throw Error('Local asset changed: '+asset.name);
 let remote=release.assets.find(x=>x.name===asset.name);
 if(!remote){if(verifyOnly)throw Error('Missing remote '+asset.name);console.log('UPLOAD '+asset.name+' '+asset.size_bytes+' bytes');
 remote=await request(release.upload_url.split('{')[0]+'?name='+encodeURIComponent(asset.name),'POST',null,file);}
 if(remote.size!==asset.size_bytes||remote.state!=='uploaded'||remote.digest!=='sha256:'+asset.sha256)throw Error('Remote digest/size mismatch: '+asset.name);
 console.log('VERIFIED '+asset.name+' '+asset.sha256);
}
if(argv.includes('--publish')){
 release=await request(base+'/releases/'+release.id,'PATCH',{draft:false,prerelease:true,body:notes,make_latest:'false'});
 if(release.draft)throw Error('GitHub did not publish the release');
 await verifyRemoteTag(false);
}
const result={release_url:release.html_url,release_id:release.id,tag:manifest.tag,draft:release.draft,prerelease:release.prerelease,commit,assets_verified:expected.length,archive_files:manifest.assets.reduce((n,a)=>n+a.files,0),archive_bytes:manifest.assets.reduce((n,a)=>n+a.size_bytes,0)};
await fsp.writeFile(path.join(directory,'publication-result.json'),JSON.stringify(result,null,2)+'\n');
console.log(JSON.stringify(result));
