/**
 * Open Music adaptation of GFrancV/spotify-search-wrapper 0.2.1 (declared MIT).
 * Fixes: parameter encoding, missing/null tokens, timeout, status validation,
 * cache, token reuse and authenticated official-API fallback.
 * The web-page token extractor has been removed. Only official credentials are used.
 */
let cachedToken = null;
const cache = new Map();
export function normalizeTrack(t) {
  return { id: t.id, provider: 'spotify', title: t.name ?? '', artist: (t.artists ?? []).map(a=>a.name).join(', '), album: t.album?.name ?? '', duration: Math.floor((t.duration_ms ?? 0)/1000), artwork: t.album?.images?.[0]?.url ?? '', web: t.external_urls?.spotify ?? '', stream: '', download: '', local: '', downloadAllowed: false };
}
export function validateQuery(q) { if(typeof q!=='string'||q.trim().length<2||q.length>200) throw new Error('La búsqueda debe tener entre 2 y 200 caracteres'); return q.trim(); }
async function fetchSafe(url, options={}) {
  const r=await fetch(url,{...options,signal:AbortSignal.timeout(12000)});
  if(!r.ok) throw new Error(`Spotify respondió HTTP ${r.status}`);
  return r;
}
async function token() {
  if(cachedToken && cachedToken.expires>Date.now()+30000)return cachedToken.value;
  if(process.env.SPOTIFY_CLIENT_ID&&process.env.SPOTIFY_CLIENT_SECRET){
    const r=await fetchSafe('https://accounts.spotify.com/api/token',{method:'POST',headers:{Authorization:'Basic '+Buffer.from(process.env.SPOTIFY_CLIENT_ID+':'+process.env.SPOTIFY_CLIENT_SECRET).toString('base64'),'Content-Type':'application/x-www-form-urlencoded'},body:'grant_type=client_credentials'});
    const j=await r.json();if(!j.access_token)throw new Error('Spotify no devolvió un token');cachedToken={value:j.access_token,expires:Date.now()+Math.min(j.expires_in??3600,3600)*1000};return cachedToken.value;
  }
  throw new Error('Configurá SPOTIFY_CLIENT_ID y SPOTIFY_CLIENT_SECRET en el servidor');
}
export async function searchTracks(input) {
  const q=validateQuery(input);const key=q.toLowerCase();const previous=cache.get(key);if(previous&&previous.expires>Date.now())return previous.value;
  let tracks;
  {
    const access=await token();const url=new URL('https://api.spotify.com/v1/search');url.searchParams.set('q',q);url.searchParams.set('type','track');url.searchParams.set('limit','20');
    try{const j=await (await fetchSafe(url,{headers:{Authorization:`Bearer ${access}`}})).json();tracks=j.tracks?.items;}catch(e){cachedToken=null;throw e;}
  }
  if(!Array.isArray(tracks))throw new Error('Respuesta de Spotify inválida');const value={tracks:tracks.filter(Boolean).map(normalizeTrack),provider:'spotify',audio:false};
  if(cache.size>=100)cache.delete(cache.keys().next().value);cache.set(key,{value,expires:Date.now()+300000});return value;
}
