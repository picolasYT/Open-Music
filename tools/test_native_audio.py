import unittest,sys,json,time
from pathlib import Path
sys.path.insert(0,str(Path(__file__).resolve().parents[1]/'app/src/main/python'))
from openmusic_audio import select_audio,PublicSourceError
class AudioPolicy(unittest.TestCase):
    def format(self,**extra):
        return dict(url='https://rr1.googlevideo.com/videoplayback?expire=9999999999',vcodec='none',acodec='mp4a.40.2',ext='m4a',protocol='https',abr=128,**extra)
    def test_prefers_compatible_audio_and_filters_credentials(self):
        a=self.format();webm={**a,'ext':'webm','abr':160};video={**a,'vcodec':'avc1','abr':9999}
        out=select_audio({'formats':[webm,video,a],'http_headers':{'User-Agent':'youtube-dl','Cookie':'secret','Authorization':'secret'}})
        self.assertEqual(out['ext'],'m4a');self.assertEqual(out['headers'],{'User-Agent':'youtube-dl'});self.assertLessEqual(out['expires'],int(time.time())+900)
    def test_rejects_drm_auth_and_private_sources(self):
        for extra in [{'has_drm':True},{'age_limit':18},{'availability':'needs_auth'},{'availability':'private'}]:
            with self.assertRaises(PublicSourceError):select_audio({'formats':[self.format()],**extra})
    def test_no_audio_format_is_an_error(self):
        for values in [[],[{**self.format(),'vcodec':'avc1'}],[{**self.format(),'url':'https://private.invalid/audio'}],[{**self.format(),'url':'http://rr1.googlevideo.com/audio'}],[{**self.format(),'has_drm':True}]]:
            with self.assertRaises(PublicSourceError):select_audio({'formats':values})
    def test_rejects_injected_headers(self):
        with self.assertRaises(PublicSourceError):select_audio({'formats':[self.format()],'http_headers':{'User-Agent':'bad\r\nX: true'}})
    def test_accepts_only_complete_same_file_byte_ranges(self):
        url='https://rr1.googlevideo.com/videoplayback?expire=9999999999&clen=100'
        f={**self.format(),'protocol':'http_dash_segments','url':url,'fragments':[{'url':url+'&range=0-49'},{'url':url+'&range=50-99'}]}
        self.assertEqual(select_audio({'formats':[f]})['url'],url)
        for fragment in [{'url':url+'&range=60-99'},{'url':'https://another.googlevideo.com/segment?range=50-99'}]:
            broken={**f,'fragments':[f['fragments'][0],fragment]}
            with self.assertRaises(PublicSourceError):select_audio({'formats':[broken]})
if __name__=='__main__':unittest.main()
