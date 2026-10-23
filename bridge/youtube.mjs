import yts from 'yt-search';
import {validateQuery} from './search.mjs';
import {execFile} from 'node:child_process';
import {promisify} from 'node:util';
import {fileURLToPath} from 'node:url';
const run=promisify(execFile);
export async function searchWithYoutubeDl(query){
  if(!process.env.YOUTUBE_DL_PYTHON)throw new Error('youtube-dl no configurado');
  const script=fileURLToPath(new URL('../tools/export_youtube_dl.py',import.meta.url));
  const {stdout}=await run(process.env.YOUTUBE_DL_PYTHON,[script,'--query',validateQuery(query),'--stdout'],{timeout:20000,maxBuffer:2*1024*1024,windowsHide:true});
  const info=JSON.parse(stdout);return (info.entries??[]).map(v=>normalizeVideo({type:'video',videoId:v.id,title:v.title,seconds:v.duration,author:{name:v.uploader}})).filter(Boolean);
}
const cache=new Map();
export function normalizeVideo(v){
  const id=v?.videoId;if(v?.type!=='video'||typeof id!=='string'||!/[A-Za-z0-9_-]{11}/.test(id??'')||id.length!==11||typeof v.title!=='string'||!v.title.trim())return null;
  return {provider:'youtube',id,title:v.title.trim(),artist:String(v.author?.name??'YouTube').slice(0,500),duration:Number.isFinite(v.seconds)?Math.max(0,Math.min(86400,Math.floor(v.seconds))):0,artwork:`https://i.ytimg.com/vi/${id}/hqdefault.jpg`,web:`https://www.youtube.com/watch?v=${id}`,stream:'',download:'',local:'',downloadAllowed:false};
}
export async function loadSearchHtml(query){
  const url=new URL('https://www.youtube.com/results');url.search=new URLSearchParams({search_query:query,hl:'es',gl:'AR'});
  const response=await fetch(url,{signal:AbortSignal.timeout(15000),headers:{'User-Agent':'Mozilla/5.0 AppleWebKit/537.36 Chrome/131.0.0.0 Safari/537.36 OpenMusic/1.2','Accept':'text/html','Accept-Language':'es-AR,es;q=0.9'}});
  if(!response.ok)throw new Error(`YouTube respondió HTTP ${response.status}`);
  const chunks=[];let size=0;for await(const chunk of response.body){size+=chunk.length;if(size>6*1024*1024)throw new Error('Respuesta demasiado grande');chunks.push(Buffer.from(chunk));}return Buffer.concat(chunks).toString('utf8');
}
export function parseSearchHtml(html){return new Promise((resolve,reject)=>{try{yts._parseSearchResultInitialData(html,(error,results)=>{const tracks=(results??[]).map(normalizeVideo).filter(Boolean).slice(0,30);if(error&&!tracks.length)return reject(new Error('No se pudo interpretar la búsqueda de YouTube'));resolve([...new Map(tracks.map(t=>[t.id,t])).values()]);});}catch{reject(new Error('Formato de YouTube no compatible'));}});}
export async function searchYouTube(query,{loader=loadSearchHtml,useCache=true}={}){
  validateQuery(query);if(query.length>160)throw new Error('Máximo 160 caracteres');const q=query.trim();const key=q.toLowerCase();const saved=cache.get(key);if(useCache&&saved&&Date.now()-saved.time<300000)return {tracks:saved.tracks};
  let tracks;try{tracks=await parseSearchHtml(await loader(q));}catch(error){if(!process.env.YOUTUBE_DL_PYTHON)throw error;tracks=await searchWithYoutubeDl(q);}if(useCache){cache.set(key,{time:Date.now(),tracks});if(cache.size>100)cache.delete(cache.keys().next().value);}return {tracks};
}
