import test from 'node:test';
import assert from 'node:assert/strict';
import {normalizeVideo,parseSearchHtml,searchYouTube} from './youtube.mjs';
const renderer={videoId:'aBSkvI0CkgU',title:{runs:[{text:'MILO J - RARA VEZ'}]},shortBylineText:{runs:[{text:'Taiu'}]},lengthText:{simpleText:'2:17'}};
const html=`<script>var ytInitialData = ${JSON.stringify({contents:{itemSectionRenderer:{contents:[{videoRenderer:renderer}]}}})};</script>`;
test('yt-search real parser integrates with provider contract',async()=>{const tracks=await parseSearchHtml(html);assert.equal(tracks.length,1);assert.equal(tracks[0].title,'MILO J - RARA VEZ');assert.equal(tracks[0].duration,137);assert.equal(tracks[0].downloadAllowed,false);});
test('external metadata cannot grant stream or download rights',()=>{const t=normalizeVideo({type:'video',videoId:'aBSkvI0CkgU',title:'Song',stream:'https://evil.invalid/audio',downloadAllowed:true,local:'file:///secret'});assert.equal(t.stream,'');assert.equal(t.local,'');assert.equal(t.downloadAllowed,false);assert.equal(normalizeVideo({type:'video',videoId:'../../evil',title:'x'}),null);});
test('empty invalid and giant searches never invoke an upstream loader',async()=>{for(const q of ['',null,'a','x'.repeat(201)])await assert.rejects(()=>searchYouTube(q,{loader:()=>assert.fail('Must validate first'),useCache:false}));});
test('successful query cache avoids duplicate upstream requests',async()=>{let calls=0;const loader=async()=>{calls++;return html;};await searchYouTube('cache test',{loader});await searchYouTube('cache test',{loader});assert.equal(calls,1);});
