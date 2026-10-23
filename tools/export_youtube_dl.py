#!/usr/bin/env python3
"""Export public YouTube search/playlist metadata using youtube-dl, without downloading media."""
import argparse,json,sys
from pathlib import Path
from urllib.parse import urlparse
import youtube_dl

def metadata(info):
    entries=info.get('entries',[info]);clean=[]
    for value in entries:
        if not isinstance(value,dict):continue
        video_id=value.get('id','')
        import re
        if not re.fullmatch(r'[A-Za-z0-9_-]{11}',video_id):continue
        clean.append({'id':video_id,'title':value.get('title',''),'uploader':value.get('uploader') or value.get('channel') or 'YouTube','duration':value.get('duration') or 0,'extractor_key':'Youtube'})
        if len(clean)>=5000:break
    if not clean:raise ValueError('youtube-dl no devolvió videos públicos. No se generó un archivo vacío.')
    return {'_type':'playlist','extractor_key':'Youtube','entries':clean}

def main():
    p=argparse.ArgumentParser(description=__doc__);g=p.add_mutually_exclusive_group(required=True);g.add_argument('--query');g.add_argument('--url');dest=p.add_mutually_exclusive_group(required=True);dest.add_argument('-o','--output',type=Path);dest.add_argument('--stdout',action='store_true');args=p.parse_args()
    if args.query:
        if not 2<=len(args.query.strip())<=160:p.error('La búsqueda requiere entre 2 y 160 caracteres')
        source='ytsearch20:'+args.query.strip()
    else:
        u=urlparse(args.url)
        if u.scheme!='https' or u.hostname not in {'youtube.com','www.youtube.com','m.youtube.com','youtu.be'} or u.username or u.password:p.error('Usá una URL HTTPS de YouTube')
        source=args.url
    try:
        with youtube_dl.YoutubeDL({'extract_flat':True,'skip_download':True,'quiet':True,'socket_timeout':15,'geo_bypass':False,'ignoreerrors':False,'playlistend':5000}) as y:
            result=metadata(y.extract_info(source,download=False))
        if args.stdout:print(json.dumps(result,ensure_ascii=False))
        else:
            args.output.write_text(json.dumps(result,ensure_ascii=False,indent=2),encoding='utf-8');print(f"{len(result['entries'])} videos exportados a {args.output}")
    except Exception as e:print(f'No se pudo exportar con youtube-dl: {e}',file=sys.stderr);return 1
    return 0
if __name__=='__main__':raise SystemExit(main())
