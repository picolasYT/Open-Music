import {createServer} from 'node:http';
import {searchTracks,validateQuery} from './search.mjs';
import {searchYouTube} from './youtube.mjs';
const requests=new Map();let inflight=0;
const server=createServer(async(req,res)=>{
  res.setHeader('Content-Type','application/json; charset=utf-8');res.setHeader('X-Content-Type-Options','nosniff');res.setHeader('Cache-Control','no-store');
  const reply=(code,body)=>{res.writeHead(code);res.end(JSON.stringify(body));};
  if(req.method!=='GET')return reply(405,{error:'Solo GET'});
  const url=new URL(req.url,'http://localhost');
  if(url.pathname==='/health')return reply(200,{status:'ok',service:'Open Music metadata bridge',youtubeSearch:'yt-search 2.13.1',youtubeDlFallbackConfigured:!!process.env.YOUTUBE_DL_PYTHON,credentialsConfigured:!!(process.env.SPOTIFY_CLIENT_ID&&process.env.SPOTIFY_CLIENT_SECRET)});
  if(!['/search','/youtube/search'].includes(url.pathname))return reply(404,{error:'Ruta no encontrada'});
  try{validateQuery(url.searchParams.get('q'));}catch(e){return reply(400,{error:e.message});}
  const ip=req.socket.remoteAddress;const now=Date.now();const r=requests.get(ip);if(!r||now-r.start>60000)requests.set(ip,{start:now,count:1});else if(++r.count>30)return reply(429,{error:'Demasiadas solicitudes. Esperá un minuto.'});
  if(requests.size>10000)for(const [key,v] of requests)if(now-v.start>60000)requests.delete(key);
  if(inflight>=8)return reply(503,{error:'Servidor ocupado. Reintentá en un momento.'});
  inflight++;try{return reply(200,await (url.pathname==='/youtube/search'?searchYouTube:searchTracks)(url.searchParams.get('q')));}catch(e){return reply(502,{error:e.message});}finally{inflight--;}
});
server.requestTimeout=15000;server.headersTimeout=10000;
server.listen(Number(process.env.PORT??3000),'0.0.0.0',()=>console.log('Open Music metadata bridge iniciado'));
