"""KuranMetni.java dosyasını üretir (Dualar ekranındaki sure ve ayetlerin Arapçası ve meali).

Kaynak: npm "quran-cloud" 1.0.0 paketi (CC BY 4.0). Arapça metin QuranEnc.com'un Medine Mushafı
(Uthmani) metnidir; Türkçe meal aynı paketteki QuranEnc Türkçe çevirisidir. Metinler değiştirilmez;
yalnızca bazı ayetleri birlikte çeviren mealde art arda tekrar eden cümleler bir kez yazılır ve
cümle sonlarına noktalama eklenir (veride ayet sonlarında noktalama yoktur).

Kullanım:
  npm pack quran-cloud@1.0.0 && tar -xzf quran-cloud-1.0.0.tgz
  python3 -I tools/kuran_metni_uret.py package/dist/quran_tr.json > app/src/main/java/tr/kolay/namazvakti/KuranMetni.java
"""
import json
import sys

q = json.load(open(sys.argv[1], encoding='utf8'))
ARABIC_DIGITS = '٠١٢٣٤٥٦٧٨٩'


def ar_num(n):
    return ''.join(ARABIC_DIGITS[int(d)] for d in str(n))


def verse(s, a):
    return q[s - 1]['verses'][a - 1]


def jstr(s):
    return '"' + s.replace('\\', '\\\\').replace('"', '\\"').replace('\n', '\\n') + '"'


def surah(s):
    ch = q[s - 1]
    lines = []
    meal = []
    for v in ch['verses']:
        lines.append(v['text'] + ' (' + ar_num(v['id']) + ')')
        t = v['translation'].strip()
        if not meal or meal[-1] != t:
            meal.append(t)
    return '\n'.join(lines), punctuate(meal), ch['translation']


def punctuate(parts):
    """Veride ayet sonlarında noktalama yok: soru eki (mi/mı/mu/mü) ile bitene '?', diğerlerine '.'
    konur; açılıp kapanmayan tırnak sonda kapatılır. Kelimeler değiştirilmez."""
    out = []
    for m in parts:
        if m.endswith(('.', '?', '!')):
            out.append(m)
        elif m.split(' ')[-1] in ('mi', 'mı', 'mu', 'mü'):
            out.append(m + '?')
        else:
            out.append(m + '.')
    text = ' '.join(out)
    if text.count('"') % 2 == 1:
        text += '"'
    return text


out = []
out.append('package tr.kolay.namazvakti;\n')
out.append('/**')
out.append(' * BU DOSYA OTOMATİK ÜRETİLDİ: tools/kuran_metni_uret.py')
out.append(' * Arapça: QuranEnc.com Medine Mushafı metni; meal: QuranEnc Türkçe çevirisi')
out.append(' * (npm "quran-cloud" 1.0.0, CC BY 4.0). Elle değiştirmeyin.')
out.append(' */')
out.append('final class KuranMetni {')
out.append('    private KuranMetni() {}\n')

names = {1: 'FATIHA', 105: 'FIL', 106: 'KUREYS', 107: 'MAUN', 108: 'KEVSER', 109: 'KAFIRUN',
         110: 'NASR', 111: 'TEBBET', 112: 'IHLAS', 113: 'FELAK', 114: 'NAS'}
for s, n in names.items():
    ar, meal, tr_name = surah(s)
    out.append('    /** %s Sûresi (%d) */' % (tr_name, s))
    out.append('    static final String %s_AR = %s;' % (n, jstr(ar)))
    out.append('    static final String %s_MEAL = %s;\n' % (n, jstr(meal)))

# Bakara 201: yalnızca dua kısmı ("Rabbenâ âtinâ ..."), ayetin başındaki "onlardan ... diyenler" hariç.
b201 = verse(2, 201)['text']
words = b201.split(' ')
start = [k for k, w in enumerate(words) if w.startswith('ر')][0]  # "رَبَّنَآ" kelimesinden itibaren
out.append('    /** Bakara 2:201 ayetinin dua kısmı */')
out.append('    static final String RABBENA_ATINA_AR = %s;\n' % jstr(' '.join(words[start:])))
out.append('    /** İbrâhîm 14:41 */')
out.append('    static final String RABBENAGFIRLI_AR = %s;' % jstr(verse(14, 41)['text']))
out.append('    static final String RABBENAGFIRLI_MEAL = %s;\n' % jstr(verse(14, 41)['translation'].strip() + '.'))
out.append('    /** Bakara 2:255 (Âyetü\'l-Kürsî) */')
out.append('    static final String AYETEL_KURSI_AR = %s;' % jstr(verse(2, 255)['text']))
out.append('    static final String AYETEL_KURSI_MEAL = %s;' % jstr(verse(2, 255)['translation'].strip() + '.'))
out.append('}')
print('\n'.join(out))
