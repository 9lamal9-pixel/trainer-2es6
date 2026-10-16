// Maintainer utility: upload immutable payloads using credentials only in memory.
import fs from 'node:fs';
import fsp from 'node:fs/promises';
import path from 'node:path';
import https from 'node:https';
import { fileURLToPath } from 'node:url';
import crypto from 'node:crypto';
import { execFileSync } from 'node:child_process';

const repository='9lamal9-pixel/trainer-2es6';
const releaseTag='trainer-environment-2026-10-16';
const maxPartBytes=16*1024*1024;
const root=path.resolve(path.dirname(fileURLToPath(import.meta.url)),'../..');
const sleep=ms=>new Promise(resolve=>setTimeout(resolve,ms));
const digestMatches=(remote,asset)=>remote.size===asset.size_bytes&&remote.state==='uploaded'&&remote.digest==='sha256:'+asset.sha256.toLowerCase();
const transient=error=>(error.status===429||(error.status>=500&&error.status<=599))||['ECONNRESET','ETIMEDOUT','EPIPE','ECONNABORTED','ERR_STREAM_PREMATURE_CLOSE'].includes(error.code);
async function hash(file){const h=crypto.createHash('sha256');for await(const block of fs.createReadStream(file))h.update(block);return h.digest('hex');}

export function transferAssets(manifest){
 if(!Array.isArray(manifest.assets)||manifest.assets.length===0)throw Error('Missing local archives');
 const names=new Set(), transfers=[];
 function validate(asset,zip){
  if(typeof asset.name!=='string'||!(zip?/^[A-Za-z0-9][A-Za-z0-9._-]*\.zip$/:/^[A-Za-z0-9][A-Za-z0-9._-]*\.zip\.part[0-9]{3,}$/).test(asset.name)||names.has(asset.name.toLowerCase())||!/^[a-fA-F0-9]{64}$/.test(asset.sha256)||!Number.isSafeInteger(asset.size_bytes)||asset.size_bytes<=0)throw Error('Invalid local archive or part metadata');
  names.add(asset.name.toLowerCase());
 }
 for(const archive of manifest.assets){
  validate(archive,true);
  if(archive.parts!==undefined){
   if(!Array.isArray(archive.parts)||archive.parts.length===0)throw Error('Empty archive parts');
   let bytes=0;
   for(let i=0;i<archive.parts.length;i++){
    const part=archive.parts[i];validate(part,false);
    if(part.name!==archive.name+'.part'+String(i+1).padStart(3,'0')||part.size_bytes>maxPartBytes)throw Error('Invalid ordered archive parts (maximum 16 MiB)');
    bytes+=part.size_bytes;transfers.push(part);
   }
   if(!Number.isSafeInteger(bytes)||bytes!==archive.size_bytes)throw Error('Archive part byte sum mismatch');
  }else transfers.push(archive);
 }
 return transfers;
}

export async function listAll(api,url){
 const items=[];
 for(let page=1;;page++){
  const batch=await api(url+(url.includes('?')?'&':'?')+'per_page=100&page='+page);
  if(!Array.isArray(batch))throw Error('Unexpected GitHub list response');
  items.push(...batch);
  if(batch.length<100)return items;
  if(page>=1000)throw Error('GitHub pagination exceeded its safety bound');
 }
}

export async function findRelease(api,base,tag){
 let direct;
 try{direct=await api(base+'/releases/tags/'+encodeURIComponent(tag));}
 catch(error){if(error.status!==404)throw error;}
 // Draft releases can be absent from the by-tag endpoint, so inspect the authenticated list too.
 const matches=(await listAll(api,base+'/releases')).filter(item=>item.tag_name===tag);
 if(direct&&!matches.some(item=>item.id===direct.id))matches.push(direct);
 if(matches.length>1)throw Error('Multiple releases have this tag; refusing ambiguous mutation');
 return matches[0];
}

function createApi(token){
 function once(url,method='GET',payload=null,file=null,label=''){
  return new Promise((resolve,reject)=>{
   const u=new URL(url);
   if(u.protocol!=='https:'||u.username||u.password||!['api.github.com','uploads.github.com'].includes(u.hostname)||u.port) return reject(Error('Unexpected API host'));
   const body=payload?Buffer.from(JSON.stringify(payload)):null;
   const headers={'User-Agent':'trainer-portable-release','Authorization':'Bearer '+token,'Accept':'application/vnd.github+json','X-GitHub-Api-Version':'2022-11-28'};
   if(body){headers['Content-Type']='application/json';headers['Content-Length']=body.length;}
   const size=file?fs.statSync(file).size:0;
   if(file){headers['Content-Type']='application/octet-stream';headers['Content-Length']=size;}
   let stream,progressTimer,sent=0,settled=false;
   const finish=(error,value)=>{
    if(settled)return;settled=true;clearInterval(progressTimer);clearTimeout(deadline);
    if(error){if(stream)stream.destroy();reject(error);}else resolve(value);
   };
   const req=https.request(u,{method,headers},res=>{
    let data='';res.setEncoding('utf8');
    res.on('data',chunk=>{data+=chunk;if(data.length>4*1024*1024)req.destroy(Error('GitHub response too large'));});
    res.on('error',error=>finish(error));
    res.on('aborted',()=>{const error=Error('GitHub response interrupted');error.code='ECONNRESET';finish(error);});
    res.on('end',()=>{
     let json;try{json=JSON.parse(data||'{}');}catch{const error=Error('Non-JSON GitHub response '+res.statusCode);error.status=res.statusCode;return finish(error);}
     if(res.statusCode<200||res.statusCode>=300){const error=Error('GitHub HTTP '+res.statusCode+': '+(json.message||'request failed'));error.status=res.statusCode;const retry=Number(res.headers['retry-after']);error.retryMs=Number.isFinite(retry)?Math.min(30000,Math.max(0,retry*1000)):null;return finish(error);}
     finish(null,json);
    });
   });
   const deadline=setTimeout(()=>{const error=Error('GitHub request timed out');error.code='ETIMEDOUT';req.destroy(error);},600000);
   req.on('socket',socket=>socket.setNoDelay(true));
   req.on('error',error=>finish(error));
   if(file){
    stream=fs.createReadStream(file);stream.on('data',chunk=>{sent+=chunk.length;});stream.on('error',error=>req.destroy(error));
    progressTimer=setInterval(()=>console.log('PROGRESS '+label+' '+sent+'/'+size+' bytes streamed'),20000);progressTimer.unref();
    stream.pipe(req);
   }else req.end(body);
  });
 }
 const api=async(url,method='GET',payload=null,file=null,label='')=>{
  // POST uploads and creation need outcome reconciliation, rather than blind request retries.
  for(let attempt=1;;attempt++){
   try{return await once(url,method,payload,file,label);}
   catch(error){if(method==='POST'||!transient(error)||attempt>=3)throw error;await sleep(error.retryMs??attempt*2000);}
  }
 };
 return api;
}

export async function uploadAsset({asset,file,api,release,base,verifyOnly,refresh,lookup,log=console.log,wait=sleep}){
 async function confirmed(remote,suffix=''){
  if(!verifyOnly&&typeof remote.label==='string'&&remote.label.startsWith('trainer-portable-attempt-')){
   if(!Number.isSafeInteger(remote.id))throw Error('Invalid remote asset ID');
   await api(base+'/releases/assets/'+remote.id,'PATCH',{label:''});
  }
  log('VERIFIED '+asset.name+' '+asset.sha256+suffix);
 }
 let remote=lookup(asset.name);
 if(remote){
  if(!digestMatches(remote,asset))throw Error('Remote digest/size/state mismatch: '+asset.name);
  await confirmed(remote);return;
 }
 if(verifyOnly)throw Error('Missing remote '+asset.name);
 for(let attempt=1;attempt<=3;attempt++){
  const ownedLabel='trainer-portable-attempt-'+crypto.randomUUID();
  log('UPLOAD '+asset.name+' '+asset.size_bytes+' bytes attempt='+attempt);
  try{
   remote=await api(release.upload_url.split('{')[0]+'?name='+encodeURIComponent(asset.name)+'&label='+encodeURIComponent(ownedLabel),'POST',null,file,asset.name);
   if(!digestMatches(remote,asset))throw Error('Remote digest/size/state mismatch: '+asset.name);
   await confirmed(remote);return;
  }catch(error){
   // A connection reset may occur after GitHub committed the upload. Reconcile before retrying.
   await refresh();remote=lookup(asset.name);
   if(remote&&digestMatches(remote,asset)){await confirmed(remote,' after reconciliation');return;}
   const ownedStarter=remote&&remote.name===asset.name&&remote.state==='starter'&&remote.label===ownedLabel&&Number.isSafeInteger(remote.id)&&Number.isSafeInteger(remote.size)&&remote.size>=0&&remote.size<=asset.size_bytes;
   if(remote&&!ownedStarter)throw Error('Remote asset conflict preserved: '+asset.name);
   if(ownedStarter){
    await api(base+'/releases/assets/'+remote.id,'DELETE');
    await refresh();if(lookup(asset.name))throw Error('Incomplete owned asset still present after cleanup: '+asset.name);
   }
   if(!transient(error)||attempt===3)throw error;
   log('RETRY '+asset.name+' after '+(error.code||error.status));
   await wait(error.retryMs??attempt*2000);
  }
 }
}

async function verifyLocal(manifest,directory){
 for(const archive of manifest.assets){
  const combined=crypto.createHash('sha256');
  for(const item of archive.parts??[archive]){
   const file=path.join(directory,item.name),digest=crypto.createHash('sha256');
   if((await fsp.stat(file)).size!==item.size_bytes)throw Error('Local asset size changed: '+item.name);
   for await(const block of fs.createReadStream(file)){digest.update(block);combined.update(block);}
   if(digest.digest('hex')!==item.sha256.toLowerCase())throw Error('Local asset SHA changed: '+item.name);
  }
  if(combined.digest('hex')!==archive.sha256.toLowerCase())throw Error('Reassembled archive SHA mismatch: '+archive.name);
 }
}

export async function main(argv=process.argv.slice(2)){
 function arg(name){const i=argv.indexOf(name);if(i<0||!argv[i+1])throw Error('Missing '+name);return argv[i+1];}
 const directory=path.resolve(arg('--assets')),commit=arg('--commit'),verifyOnly=argv.includes('--verify');
 if(verifyOnly&&(argv.includes('--publish')||argv.includes('--update-draft-target')))throw Error('--verify cannot publish or mutate a release');
 if(!/^[a-f0-9]{40}$/.test(commit))throw Error('Use a full commit SHA');
 const jobs=argv.includes('--jobs')?Number(arg('--jobs')):3;
 if(!Number.isSafeInteger(jobs)||jobs<1||jobs>8)throw Error('--jobs must be an integer from 1 to 8');
 const manifestPath=path.join(directory,'portable-release.json');
 const manifest=JSON.parse(await fsp.readFile(manifestPath,'utf8'));
 if(manifest.repository!==repository||manifest.tag!==releaseTag)throw Error('Unexpected destination');
 const expected=transferAssets(manifest);
 await verifyLocal(manifest,directory);
 const metadataNames=['portable-release.json','environment-files.json'];
 const validationFile=path.join(directory,'validation-report.json');
 const validationExists=await fsp.access(validationFile).then(()=>true,()=>false);
 if(validationExists)metadataNames.push('validation-report.json');
 if(argv.includes('--publish')){
  if(!validationExists)throw Error('A successful validation-report.json is required before publishing');
  const validation=JSON.parse(await fsp.readFile(validationFile,'utf8'));
  if(validation.passed!==true||validation.commit!==commit||validation.payload_manifest_sha256!==await hash(manifestPath))throw Error('Validation report does not accept this commit and manifest');
 }
 for(const name of metadataNames){const file=path.join(directory,name);expected.push({name,size_bytes:(await fsp.stat(file)).size,sha256:await hash(file)});}
 const git=argv.includes('--git')?arg('--git'):'git';
 let token=process.env.GH_TOKEN||process.env.GITHUB_TOKEN;
 if(!token){
  let credential;
  try{credential=execFileSync(git,['-c','credential.interactive=never','credential','fill'],{cwd:root,windowsHide:true,input:'protocol=https\nhost=github.com\n\n',encoding:'utf8',stdio:['pipe','pipe','pipe'],env:{...process.env,GIT_TERMINAL_PROMPT:'0'}});}
  catch{throw Error('GitHub credential lookup failed; configure Git Credential Manager or GH_TOKEN. Credential output was suppressed.');}
  token=credential.split(/\r?\n/).find(line=>line.startsWith('password='))?.slice(9);credential=null;
 }
 if(!token)throw Error('No GitHub credential available');
 const api=createApi(token),base='https://api.github.com/repos/'+repository;
 async function verifyRemoteTag(allowMissing){
  let ref;try{ref=await api(base+'/git/ref/tags/'+encodeURIComponent(releaseTag));}
  catch(error){if(error.status===404&&allowMissing)return false;throw error;}
  let object=ref.object;
  for(let depth=0;object?.type==='tag'&&depth<5;depth++){const tag=await api(base+'/git/tags/'+object.sha);object=tag.object;}
  if(object?.type!=='commit'||object.sha!==commit)throw Error('Remote release tag does not resolve to the requested commit');
  return true;
 }
 const remoteCommit=await api(base+'/commits/'+commit);
 if(remoteCommit.sha!==commit)throw Error('Requested commit is not available in the GitHub repository');
 const notes=await fsp.readFile(arg('--notes'),'utf8');
 await verifyRemoteTag(true);
 let release=await findRelease(api,base,releaseTag);
 if(!release&&verifyOnly)throw Error('Release does not exist; --verify does not create it');
 if(!release)release=await api(base+'/releases','POST',{tag_name:releaseTag,target_commitish:commit,name:'Полная среда разработки 2ЭС6 — 16.10.2026',body:notes,draft:true,prerelease:true});
 if(release.tag_name!==releaseTag)throw Error('Tag mismatch');
 if(release.draft&&release.target_commitish!==commit){
  if(verifyOnly||!argv.includes('--update-draft-target'))throw Error('Draft targets a different commit; use --update-draft-target only after validating the new commit');
  if(await verifyRemoteTag(true))throw Error('Refusing to move an existing tag');
  release=await api(base+'/releases/'+release.id,'PATCH',{target_commitish:commit});
  if(release.target_commitish!==commit)throw Error('Draft target update failed');
 }
 if(!release.draft&&!verifyOnly)throw Error('Refusing to mutate a published release');
 await verifyRemoteTag(release.draft);
 let cache=new Map();
 async function refresh(){
  const assets=await listAll(api,base+'/releases/'+release.id+'/assets'),next=new Map();
  for(const asset of assets){if(next.has(asset.name))throw Error('Duplicate remote asset names');next.set(asset.name,asset);}
  cache=next;
 }
 await refresh();
 const lookup=name=>cache.get(name);
 let cursor=0,stopping=false;
 async function worker(){while(!stopping&&cursor<expected.length){try{const asset=expected[cursor++],file=path.join(directory,asset.name);if((await fsp.stat(file)).size!==asset.size_bytes||await hash(file)!==asset.sha256.toLowerCase())throw Error('Local asset changed before upload: '+asset.name);await uploadAsset({asset,file,api,release,base,verifyOnly,refresh,lookup});}catch(error){stopping=true;throw error;}}}
 // Wait for all started transfers even if a worker fails, so another run cannot overlap them.
 const outcomes=await Promise.allSettled(Array.from({length:Math.min(jobs,expected.length)},()=>worker()));
 const failure=outcomes.find(result=>result.status==='rejected');if(failure)throw failure.reason;
 await refresh();
 for(const asset of expected){if(!digestMatches(lookup(asset.name)??{},asset))throw Error('Final remote verification failed: '+asset.name);}
 if(argv.includes('--publish')){
  release=await api(base+'/releases/'+release.id,'PATCH',{draft:false,prerelease:true,body:notes,make_latest:'false'});
  if(release.draft)throw Error('GitHub did not publish the release');await verifyRemoteTag(false);
 }
 const result={release_url:release.html_url,release_id:release.id,tag:releaseTag,draft:release.draft,prerelease:release.prerelease,commit,assets_verified:expected.length,archive_files:manifest.assets.reduce((n,a)=>n+a.files,0),archive_bytes:manifest.assets.reduce((n,a)=>n+a.size_bytes,0)};
 await fsp.writeFile(path.join(directory,'publication-result.json'),JSON.stringify(result,null,2)+'\n');
 console.log(JSON.stringify(result));
}

if(process.argv[1]&&path.resolve(process.argv[1])===fileURLToPath(import.meta.url)){
 main().catch(error=>{console.error(error.message);process.exitCode=1;});
}
