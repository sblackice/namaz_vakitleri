import json, sys, unicodedata
IL = ["Adana","Adıyaman","Afyonkarahisar","Ağrı","Amasya","Ankara","Antalya","Artvin","Aydın","Balıkesir","Bilecik","Bingöl","Bitlis","Bolu","Burdur","Bursa","Çanakkale","Çankırı","Çorum","Denizli","Diyarbakır","Edirne","Elazığ","Erzincan","Erzurum","Eskişehir","Gaziantep","Giresun","Gümüşhane","Hakkari","Hatay","Isparta","Mersin","İstanbul","İzmir","Kars","Kastamonu","Kayseri","Kırklareli","Kırşehir","Kocaeli","Konya","Kütahya","Malatya","Manisa","Kahramanmaraş","Mardin","Muğla","Muş","Nevşehir","Niğde","Ordu","Rize","Sakarya","Samsun","Siirt","Sinop","Sivas","Tekirdağ","Tokat","Trabzon","Tunceli","Şanlıurfa","Uşak","Van","Yozgat","Zonguldak","Aksaray","Bayburt","Karaman","Kırıkkale","Batman","Şırnak","Bartın","Ardahan","Iğdır","Yalova","Karabük","Kilis","Osmaniye","Düzce"]
CENTER = {"Kocaeli":"İzmit","Sakarya":"Adapazarı","Hatay":"Antakya","Tekirdağ":"Tekirdağ","Mersin":"Mersin"}
def norm(s):
    s = s.replace("ı","i").replace("İ","I")
    return "".join(c for c in unicodedata.normalize("NFD", s) if unicodedata.category(c) != "Mn").lower()
d = json.load(open(sys.argv[1]))
tr = [c for c in d.values() if c["countrycode"] == "TR"]
out = []
for i, il in enumerate(IL, 1):
    want = norm(CENTER.get(il, il))
    cands = [c for c in tr if norm(c["name"]) == want or want in [norm(a) for a in c.get("alternatenames", [])[:0]]]
    if not cands:
        cands = [c for c in tr if want in [norm(a) for a in c.get("alternatenames", [])]]
    if not cands:
        print("MISSING", i, il, file=sys.stderr); continue
    c = max(cands, key=lambda c: c["population"])
    out.append((i, il, c["name"], c["latitude"], c["longitude"], c["geonameid"], c["population"], len(cands)))
for o in out: print(*o, sep="\t")
