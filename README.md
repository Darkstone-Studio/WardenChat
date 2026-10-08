# Warden Chat

Telefon numarası veya e-posta gerektirmeyen, anonim kimlik kodlu mesajlaşma uygulaması.

> ⚠️ Bu proje aktif geliştirme aşamasındadır. Bu README geçicidir, detaylı dokümantasyon ilerleyen aşamalarda eklenecektir.

## Konsept

Warden Chat'e giren her kullanıcıya, herhangi bir kayıt işlemi olmadan rastgele 9 karakterlik bir kimlik kodu verilir (örn. `45H-68Y-U8T`). Kullanıcılar birbirleriyle bu kodları paylaşarak mesajlaşabilir.

## Özellikler

-  Kayıt yok, telefon numarası yok, e-posta yok
-  Rastgele üretilen, kalıcı anonim kimlik kodu
-  Kod paylaşarak anında mesajlaşma
-  Mesajlar teslim edilene kadar geçici olarak bekler, teslim olunca sunucudan silinir
-  Self-destruct mesaj modu (5 saniyede otomatik silinme)
-  Arka plan bildirimleri
-  Koyu tema, minimalist arayüz

## Teknik Yapı

- **Platform:** Android (Kotlin, Jetpack Compose)
- **Veritabanı / Mesaj İletimi:** Firebase Firestore (geçici posta kutusu mantığı)
- **Yerel Depolama:** Room (kişi listesi), DataStore (kimlik kodu)
- **Arka Plan Görevleri:** WorkManager

## Durum

Mimari olarak tam P2P (peer-to-peer) değildir — mesajlar, teslim edilene kadar Firebase Firestore üzerinde geçici olarak bekletilir ve görüldüğü anda sunucudan silinir. Hiçbir mesaj kalıcı olarak saklanmaz.

## Lisans

Bu proje [PolyForm Noncommercial 1.0.0](./LICENSE.md) lisansı altında yayınlanmıştır — ticari olmayan kullanım, kişisel/hobi projeleri ve eğitim/araştırma amaçlı kullanım serbesttir. Ticari kullanım için izin gerekir.

> Required Notice: Copyright Warden Chat (https://github.com/Darkstone-Studio/WardenChat)
> 
---

## Güvenlik & Gizlilik

[Gizlilik Politikası](https://darkstone-studio.github.io/WardenChat/privacy-policy.html)

---

## İletişim

Öneri, talep veya sorularınız için: **darkstonestudio.dev@gmail.com**

## Destek

Projeyi desteklemek isterseniz: https://github.com/sponsors/mazyLeyn
