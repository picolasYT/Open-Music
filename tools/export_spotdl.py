#!/usr/bin/env python3
"""Export real Spotify metadata using spotDL's official-API mode; never download audio."""
import argparse
import json
import os
from dataclasses import asdict
from pathlib import Path
from urllib.parse import urlparse

def main():
    parser=argparse.ArgumentParser(description=__doc__)
    parser.add_argument('spotify_url',help='URL pública de canción, álbum o playlist de Spotify')
    parser.add_argument('-o','--output',default='playlist.spotdl')
    args=parser.parse_args()
    parsed=urlparse(args.spotify_url)
    if parsed.scheme!='https' or parsed.hostname!='open.spotify.com' or not parsed.path.startswith(('/track/','/album/','/playlist/')):
        parser.error('Ingresá una URL HTTPS válida de Spotify')
    client=os.getenv('SPOTIFY_CLIENT_ID','');secret=os.getenv('SPOTIFY_CLIENT_SECRET','')
    if not client or not secret:parser.error('Configurá SPOTIFY_CLIENT_ID y SPOTIFY_CLIENT_SECRET. No se extraen tokens de páginas web.')
    from spotdl.utils.spotify import SpotifyClient
    from spotdl.utils.search import parse_query
    SpotifyClient.init(client_id=client,client_secret=secret,use_official_api=True,no_cache=True,headless=True)
    songs=parse_query([args.spotify_url],threads=1,use_ytm_data=False)
    if not songs:raise SystemExit('Spotify no devolvió canciones. Revisá los permisos y la disponibilidad de la playlist.')
    path=Path(args.output)
    # Exclusive creation protects an existing export from accidental overwrite.
    with path.open('x',encoding='utf-8') as output:json.dump([asdict(s) for s in songs],output,ensure_ascii=False,indent=2)
    print(f'{len(songs)} canciones exportadas a {path}. Importá el archivo desde Biblioteca en Open Music.')

if __name__=='__main__':main()
