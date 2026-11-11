#!/usr/bin/env python3
"""Esmâ-ül Hüsnâ ses kayıtlarını böler: 11 isimlik 9 grup kaydı (dl/b1.mp3 … b9.mp3) sessizliklerden
ayırıp 99 dosyaya (parts/esma_01.mp3 … esma_99.mp3) çevirir; ses düzeyini eşitler. ffmpeg gerekir.
Kayıtlar ElevenLabs (eleven_v3, "Ashraf - Calm Documentary Narrator" sesi) ile üretildi.
Kullanım: python3 tools/esma_ses_bol.py <klasör>   (klasörde dl/ alt klasörü olmalı)"""
import subprocess,re,sys,os,json
D=sys.argv[1]; out=os.path.join(D,'parts'); os.makedirs(out,exist_ok=True)
report=[]
for b in range(1,10):
    f=os.path.join(D,'dl',f'b{b}.mp3')
    err=subprocess.run(['ffmpeg','-hide_banner','-i',f,'-af','silencedetect=noise=-38dB:d=0.45','-f','null','-'],capture_output=True,text=True).stderr
    dur=re.search(r'Duration: (\d+):(\d+):([\d.]+)',err); total=int(dur[1])*3600+int(dur[2])*60+float(dur[3])
    st=[float(x) for x in re.findall(r'silence_start: ([\d.]+)',err)]; en=[float(x) for x in re.findall(r'silence_end: ([\d.]+)',err)]
    sil=list(zip(st,en+[total]*(len(st)-len(en))))
    # speech segments = gaps between silences
    segs=[]; cur=0.0
    for s,e in sil:
        if s-cur>0.15: segs.append((cur,s))
        cur=e
    if total-cur>0.15: segs.append((cur,total))
    report.append((b,len(segs),[round(e-s,2) for s,e in segs]))
    if len(segs)==11:
        for k,(s,e) in enumerate(segs):
            n=(b-1)*11+k+1
            a=max(0,s-0.08); d=(e-s)+0.08+0.22
            subprocess.run(['ffmpeg','-hide_banner','-loglevel','error','-y','-ss',f'{a:.3f}','-t',f'{d:.3f}','-i',f,
              '-af',f'afade=t=in:d=0.03,afade=t=out:st={d-0.12:.3f}:d=0.12,loudnorm=I=-18:TP=-2:LRA=7','-ac','1','-ar','44100','-b:a','64k',
              os.path.join(out,f'esma_{n:02d}.mp3')],check=True)
for r in report: print(r)
