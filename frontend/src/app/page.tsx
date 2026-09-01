'use client';

import React, { useState, useEffect } from 'react';
import Link from 'next/link';
import { 
  Link2,
  Lock, 
  QrCode, 
  BarChart3, 
  ArrowRight, 
  User, 
  Sparkles,
  Zap,
  ShieldCheck,
  Globe2,
  Layers,
  CheckCircle2,
  Cpu,
  MousePointerClick
} from 'lucide-react';
import { QuickShortenWidget } from '@/components/QuickShortenWidget';
import { QrCodeModal } from '@/components/QrCodeModal';
import { Language, translations } from '@/lib/translations';
import { Button } from '@/components/ui/button';
import { Badge } from '@/components/ui/badge';

export default function HomePage() {
  const [lang, setLang] = useState<Language>('tr');
  const [qrModal, setQrModal] = useState<string | null>(null);
  const [currentUser, setCurrentUser] = useState<{ u: string; role?: string } | null>(null);

  useEffect(() => {
    const saved = localStorage.getItem('klink_user') || localStorage.getItem('swiftlink_user');
    if (saved) {
      try {
        const parsed = JSON.parse(saved);
        if (parsed && parsed.u) {
          setCurrentUser(parsed);
        }
      } catch (e) {
        console.error(e);
      }
    }
  }, []);

  const t = translations[lang];

  return (
    <div className="min-h-screen bg-[#fafafa] text-zinc-950 selection:bg-zinc-900 selection:text-white flex flex-col justify-between">
      {/* Sticky Glass Header */}
      <header className="sticky top-0 z-40 bg-white/80 backdrop-blur-md border-b border-zinc-200/80 px-4 sm:px-8 py-3 transition-all">
        <div className="max-w-6xl mx-auto flex items-center justify-between">
          <div className="flex items-center gap-3">
            <div className="w-8 h-8 rounded-xl bg-zinc-950 text-white flex items-center justify-center shadow-xs">
              <Link2 className="w-4 h-4" />
            </div>
            <div>
              <div className="flex items-center gap-1.5">
                <h1 className="text-sm font-bold text-zinc-950 tracking-tight">
                  Klink
                </h1>
                <span className="inline-flex items-center px-1.5 py-0.2 rounded text-[10px] font-mono font-semibold bg-zinc-100 text-zinc-700 border border-zinc-200/80">
                  v2.4
                </span>
              </div>
              <p className="text-[11px] text-zinc-400 font-medium hidden sm:block">
                {lang === 'tr' ? 'Link Yönetimi & Analitik Platformu' : 'Link Management & Analytics Engine'}
              </p>
            </div>
          </div>

          {/* Navigation Links */}
          <div className="flex items-center gap-2 sm:gap-2.5">
            {/* Language Switcher */}
            <div className="flex items-center p-0.5 rounded-lg bg-zinc-100 border border-zinc-200/80 text-xs font-semibold">
              <button
                onClick={() => setLang('tr')}
                className={`px-2 py-0.5 rounded-md transition-all cursor-pointer text-[11px] ${
                  lang === 'tr' ? 'bg-white text-zinc-950 shadow-2xs font-bold' : 'text-zinc-500 hover:text-zinc-900'
                }`}
              >
                TR
              </button>
              <button
                onClick={() => setLang('en')}
                className={`px-2 py-0.5 rounded-md transition-all cursor-pointer text-[11px] ${
                  lang === 'en' ? 'bg-white text-zinc-950 shadow-2xs font-bold' : 'text-zinc-500 hover:text-zinc-900'
                }`}
              >
                EN
              </button>
            </div>

            {currentUser ? (
              <>
                <div className="px-2.5 py-1 rounded-lg bg-zinc-100 border border-zinc-200 text-xs font-semibold text-zinc-800 hidden sm:flex items-center gap-1.5">
                  <User className="w-3.5 h-3.5 text-zinc-500" />
                  <span>@{currentUser.u}</span>
                </div>
                <Button size="sm" asChild className="text-xs bg-zinc-950 hover:bg-zinc-800 text-white font-semibold">
                  <Link href={currentUser.role === 'ROLE_ADMIN' ? '/admin' : '/dashboard'}>
                    <span>{currentUser.role === 'ROLE_ADMIN' ? 'Yönetim Paneli' : 'Panele Git'}</span>
                    <ArrowRight className="w-3.5 h-3.5 ml-1" />
                  </Link>
                </Button>
              </>
            ) : (
              <>
                <Button variant="ghost" size="sm" asChild className="text-xs text-zinc-700">
                  <Link href="/login">
                    <User className="w-3.5 h-3.5 mr-1" />
                    <span>{t.btnLogin}</span>
                  </Link>
                </Button>

                <Button size="sm" asChild className="text-xs bg-zinc-950 hover:bg-zinc-800 text-white font-semibold">
                  <Link href="/register">
                    <span>{t.btnRegister}</span>
                    <ArrowRight className="w-3.5 h-3.5 ml-1" />
                  </Link>
                </Button>
              </>
            )}
          </div>
        </div>
      </header>

      {/* Main Hero & Content */}
      <main className="relative flex-1 w-full max-w-6xl mx-auto px-4 sm:px-6 pt-10 sm:pt-16 pb-20 space-y-16">
        {/* Subtle Background Radial / Grid */}
        <div className="absolute inset-x-0 top-0 h-96 -z-10 bg-gradient-to-b from-zinc-100/70 via-transparent to-transparent pointer-events-none" />

        {/* Hero Header Section */}
        <div className="text-center space-y-4 max-w-3xl mx-auto">
          <Badge variant="secondary" className="px-3 py-1 text-xs gap-1.5 text-zinc-800 bg-zinc-100 border-zinc-200/90 shadow-2xs">
            <span className="w-1.5 h-1.5 rounded-full bg-emerald-500 animate-pulse" />
            <span>{lang === 'tr' ? 'Redis & RabbitMQ Destekli Yeni Nesil Link Motoru' : 'Powered by Redis & RabbitMQ Telemetry'}</span>
          </Badge>

          <h2 className="text-4xl sm:text-5xl lg:text-6xl font-black text-zinc-950 tracking-tight leading-[1.1]">
            {lang === 'tr' ? (
              <>
                Bağlantılarınızı <span className="underline decoration-zinc-300 decoration-wavy underline-offset-8">Akıllı</span> & Güvenli Kılın
              </>
            ) : (
              <>
                Shorten, Track & <span className="underline decoration-zinc-300 decoration-wavy underline-offset-8">Supercharge</span> Links
              </>
            )}
          </h2>

          <p className="text-zinc-500 text-sm sm:text-base max-w-2xl mx-auto leading-relaxed">
            {lang === 'tr'
              ? 'Uzun ve karmaşık web adreslerinizi anında kısaltın, parola ile koruyun, yüksek çözünürlüklü QR kod üretin ve her tıklamayı gerçek zamanlı izleyin.'
              : 'Transform long URLs into memorable short links with deep click telemetry, automated high-res QR codes, and password protection.'}
          </p>
        </div>

        {/* Centerpiece Shortener Command Bar */}
        <div className="pt-2">
          <QuickShortenWidget
            lang={lang}
            authUser={currentUser ? (currentUser as any) : null}
            onSuccess={() => {}}
            onOpenQr={(code) => setQrModal(code)}
          />
        </div>

        {/* Live Metrics / Speed Ticker Bar */}
        <div className="grid grid-cols-2 md:grid-cols-4 gap-3.5 pt-4">
          <div className="p-4 rounded-2xl bg-white border border-zinc-200/80 shadow-2xs text-center">
            <div className="flex items-center justify-center gap-1 text-emerald-600 mb-1">
              <Zap className="w-4 h-4" />
              <span className="text-lg sm:text-xl font-bold font-mono text-zinc-950">&lt; 2ms</span>
            </div>
            <p className="text-[11px] text-zinc-500 font-medium">{lang === 'tr' ? 'Yönlendirme Gecikmesi' : 'Redirect Latency'}</p>
          </div>

          <div className="p-4 rounded-2xl bg-white border border-zinc-200/80 shadow-2xs text-center">
            <div className="flex items-center justify-center gap-1 text-blue-600 mb-1">
              <ShieldCheck className="w-4 h-4" />
              <span className="text-lg sm:text-xl font-bold font-mono text-zinc-950">%100</span>
            </div>
            <p className="text-[11px] text-zinc-500 font-medium">{lang === 'tr' ? 'Güvenlik Doğrulaması' : 'Safety Verified'}</p>
          </div>

          <div className="p-4 rounded-2xl bg-white border border-zinc-200/80 shadow-2xs text-center">
            <div className="flex items-center justify-center gap-1 text-zinc-900 mb-1">
              <MousePointerClick className="w-4 h-4" />
              <span className="text-lg sm:text-xl font-bold font-mono text-zinc-950">100K+</span>
            </div>
            <p className="text-[11px] text-zinc-500 font-medium">{lang === 'tr' ? 'Günlük Tıklama Kapasitesi' : 'Daily Click Capacity'}</p>
          </div>

          <div className="p-4 rounded-2xl bg-white border border-zinc-200/80 shadow-2xs text-center">
            <div className="flex items-center justify-center gap-1 text-emerald-600 mb-1">
              <Cpu className="w-4 h-4" />
              <span className="text-lg sm:text-xl font-bold font-mono text-zinc-950">%99.99</span>
            </div>
            <p className="text-[11px] text-zinc-500 font-medium">{lang === 'tr' ? 'Kesintisiz Çalışma' : 'Uptime SLA'}</p>
          </div>
        </div>

        {/* Modern Bento-Grid Features Section */}
        <div className="space-y-6 pt-6">
          <div className="text-center space-y-1">
            <h3 className="text-2xl sm:text-3xl font-extrabold text-zinc-950 tracking-tight">
              {lang === 'tr' ? 'Profesyonel Link Yönetim Araçları' : 'Enterprise-Grade Link Tools'}
            </h3>
            <p className="text-xs sm:text-sm text-zinc-500">
              {lang === 'tr' ? 'Gelişmiş analitik, güvenlik ve ölçeklenebilir altyapı özellikleri' : 'Advanced telemetry, security, and scalable infrastructure features'}
            </p>
          </div>

          <div className="grid grid-cols-1 md:grid-cols-3 gap-5">
            {/* Feature 1: Password Gate */}
            <div className="rounded-3xl border border-zinc-200/80 bg-white p-6 shadow-2xs transition-all hover:border-zinc-300 hover:shadow-md space-y-3">
              <div className="w-10 h-10 rounded-2xl bg-zinc-100 border border-zinc-200 flex items-center justify-center text-zinc-900">
                <Lock className="w-5 h-5" />
              </div>
              <h4 className="text-sm font-bold text-zinc-950">
                {lang === 'tr' ? 'Şifre Korumalı Geçit' : 'Password Protection'}
              </h4>
              <p className="text-xs text-zinc-500 leading-relaxed">
                {lang === 'tr'
                  ? 'Özel veya gizli bağlantılarınıza parola ekleyin. Ziyaretçiler doğru şifreyi girmeden hedef adrese yönlendirilmez.'
                  : 'Protect sensitive links with a PIN or password. Only authorized visitors can unlock the target destination.'}
              </p>
            </div>

            {/* Feature 2: QR Code */}
            <div className="rounded-3xl border border-zinc-200/80 bg-white p-6 shadow-2xs transition-all hover:border-zinc-300 hover:shadow-md space-y-3">
              <div className="w-10 h-10 rounded-2xl bg-zinc-100 border border-zinc-200 flex items-center justify-center text-zinc-900">
                <QrCode className="w-5 h-5" />
              </div>
              <h4 className="text-sm font-bold text-zinc-950">
                {lang === 'tr' ? 'Otomatik Vektörel QR Kodlar' : 'Automated Crisp QR Codes'}
              </h4>
              <p className="text-xs text-zinc-500 leading-relaxed">
                {lang === 'tr'
                  ? 'Kısaltılan her link için saniyeler içinde yüksek çözünürlüklü QR kod üretin, tek tıkla cihazınıza indirin ve paylaşın.'
                  : 'Instantly generate crisp, high-resolution QR codes for every link and export them with a single click.'}
              </p>
            </div>

            {/* Feature 3: Real-time Telemetry */}
            <div className="rounded-3xl border border-zinc-200/80 bg-white p-6 shadow-2xs transition-all hover:border-zinc-300 hover:shadow-md space-y-3">
              <div className="w-10 h-10 rounded-2xl bg-zinc-100 border border-zinc-200 flex items-center justify-center text-zinc-900">
                <BarChart3 className="w-5 h-5" />
              </div>
              <h4 className="text-sm font-bold text-zinc-950">
                {lang === 'tr' ? 'Derin Ziyaretçi Telemetrisi' : 'Intelligent Telemetry'}
              </h4>
              <p className="text-xs text-zinc-500 leading-relaxed">
                {lang === 'tr'
                  ? 'Bot ve tarayıcı trafiğini gerçek kullanıcılardan ayırt edin; ülke, şehir, cihaz ve yönlendiren kaynak dağılımını izleyin.'
                  : 'Filter bot crawlers from human visits, analyze geographic country/city data, device types, and top referrers.'}
              </p>
            </div>
          </div>
        </div>

        {/* User / Admin Callout Banner */}
        <div className="p-7 sm:p-9 rounded-3xl bg-zinc-950 text-white flex flex-col sm:flex-row items-center justify-between gap-6 shadow-xl border border-zinc-800">
          <div className="space-y-1.5 text-center sm:text-left">
            <Badge variant="secondary" className="bg-zinc-800 text-zinc-300 border-zinc-700 text-[10px]">
              {lang === 'tr' ? 'Ücretsiz Başlayın' : 'Free & Instant'}
            </Badge>
            <h3 className="text-lg sm:text-xl font-bold text-white tracking-tight">
              {lang === 'tr' ? 'Tüm linklerinizi tek panelden yönetin' : 'Manage all links from a unified dashboard'}
            </h3>
            <p className="text-xs text-zinc-400 max-w-lg leading-relaxed">
              {lang === 'tr'
                ? 'Ücretsiz hesabınızı oluşturun; toplu link kısaltma, 2FA güvenliği ve detaylı analitik raporlarına anında erişin.'
                : 'Create your account to unlock bulk link processing, 2FA authentication, and detailed analytics exports.'}
            </p>
          </div>

          <div className="flex items-center gap-2.5 shrink-0">
            <Button size="sm" asChild className="bg-white text-zinc-950 hover:bg-zinc-100 font-semibold px-4">
              <Link href="/register">{t.btnRegister}</Link>
            </Button>

            <Button
              variant="outline"
              size="sm"
              asChild
              className="bg-zinc-900 border-zinc-700 text-zinc-200 hover:bg-zinc-800 hover:text-white"
            >
              <Link href="/dashboard">
                <span>{lang === 'tr' ? 'Panele Git' : 'Dashboard'}</span>
                <ArrowRight className="w-3.5 h-3.5 ml-1" />
              </Link>
            </Button>
          </div>
        </div>
      </main>

      {/* Modern Footer */}
      <footer className="border-t border-zinc-200/80 bg-white py-6 text-center text-xs text-zinc-500">
        <div className="max-w-6xl mx-auto px-4 flex flex-col sm:flex-row items-center justify-between gap-3">
          <div className="flex items-center gap-2 font-medium text-zinc-800">
            <div className="w-5 h-5 rounded-md bg-zinc-950 text-white flex items-center justify-center">
              <Link2 className="w-3 h-3" />
            </div>
            <span className="font-bold">Klink</span>
            <span className="text-zinc-300">•</span>
            <span className="text-zinc-500 text-[11px]">Spring Boot + Redis + RabbitMQ + Next.js</span>
          </div>
          <p className="text-[11px] text-zinc-400">&copy; 2026 Klink. {lang === 'tr' ? 'Tüm hakları saklıdır.' : 'All rights reserved.'}</p>
        </div>
      </footer>

      <QrCodeModal
        shortCode={qrModal}
        lang={lang}
        onClose={() => setQrModal(null)}
      />
    </div>
  );
}
