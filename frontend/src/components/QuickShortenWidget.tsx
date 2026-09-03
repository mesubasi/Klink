'use client';

import React, { useState } from 'react';
import { 
  Link2, 
  Copy, 
  Check, 
  QrCode, 
  ArrowRight, 
  SlidersHorizontal, 
  Lock, 
  Clock, 
  Tag, 
  ShieldCheck, 
  Eye, 
  ClipboardPaste,
  Sparkles,
  Globe,
  ExternalLink,
  Share2,
  X,
  Smartphone,
  Laptop,
  ChevronDown,
  ChevronUp,
  Webhook,
  Radio,
  Split,
  Plus,
  Trash2,
  PieChart
} from 'lucide-react';
import { Language, translations } from '@/lib/translations';
import { ShortenRequest, ShortenResponse } from '@/lib/types';
import { ApiClient } from '@/lib/api';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Card } from '@/components/ui/card';
import { Badge } from '@/components/ui/badge';

interface QuickShortenWidgetProps {
  lang: Language;
  onSuccess: (res: ShortenResponse) => void;
  onOpenQr: (shortCode: string) => void;
  authUser?: { u?: string; p?: string; token?: string } | null;
}

export const QuickShortenWidget: React.FC<QuickShortenWidgetProps> = ({
  lang,
  onSuccess,
  onOpenQr,
  authUser,
}) => {
  const t = translations[lang];

  const [originalUrl, setOriginalUrl] = useState('');
  const [customAlias, setCustomAlias] = useState('');
  const [expirationDays, setExpirationDays] = useState<number | ''>('');
  const [password, setPassword] = useState('');
  const [previewEnabled, setPreviewEnabled] = useState(false);
  const [iosUrl, setIosUrl] = useState('');
  const [androidUrl, setAndroidUrl] = useState('');
  const [desktopUrl, setDesktopUrl] = useState('');
  const [webhookUrl, setWebhookUrl] = useState('');
  const [webhookSecret, setWebhookSecret] = useState('');
  const [loading, setLoading] = useState(false);
  const [errorMsg, setErrorMsg] = useState('');
  const [result, setResult] = useState<ShortenResponse | null>(null);
  const [copied, setCopied] = useState(false);
  const [showAdvanced, setShowAdvanced] = useState(false);
  const [showDeviceTargeting, setShowDeviceTargeting] = useState(false);
  const [showWebhook, setShowWebhook] = useState(false);
  const [showAbTest, setShowAbTest] = useState(false);
  const [abTestingEnabled, setAbTestingEnabled] = useState(false);
  const [variants, setVariants] = useState<Array<{ label: string; targetUrl: string; weightPercent: number }>>([
    { label: 'Varyant A', targetUrl: '', weightPercent: 50 },
    { label: 'Varyant B', targetUrl: '', weightPercent: 50 },
  ]);

  // Extract domain preview
  const getDomainPreview = (url: string) => {
    try {
      const parsed = new URL(url.startsWith('http') ? url : `https://${url}`);
      return parsed.hostname;
    } catch {
      return null;
    }
  };

  const handlePasteClipboard = async () => {
    try {
      const text = await navigator.clipboard.readText();
      if (text) {
        setOriginalUrl(text.trim());
      }
    } catch (e) {
      console.warn('Clipboard read failed', e);
    }
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!originalUrl.trim()) return;

    setLoading(true);
    setErrorMsg('');
    setResult(null);

    if (abTestingEnabled) {
      if (variants.length < 2) {
        setErrorMsg(lang === 'tr' ? 'A/B testi için en az 2 varyant tanımlamalısınız.' : 'Define at least 2 variants for A/B testing.');
        setLoading(false);
        return;
      }
      const totalW = variants.reduce((acc, v) => acc + (Number(v.weightPercent) || 0), 0);
      if (totalW !== 100) {
        setErrorMsg(lang === 'tr' ? `A/B varyant ağırlıklarının toplamı tam olarak %100 olmalıdır. Şu anki toplam: %${totalW}` : `Total variant weights must equal 100%. Current: ${totalW}%`);
        setLoading(false);
        return;
      }
      for (let i = 0; i < variants.length; i++) {
        if (!variants[i].targetUrl.trim()) {
          setErrorMsg(lang === 'tr' ? `Lütfen ${variants[i].label} için geçerli bir hedef URL girin.` : `Please enter a target URL for ${variants[i].label}.`);
          setLoading(false);
          return;
        }
      }
    }

    const req: ShortenRequest = {
      originalUrl: originalUrl.trim() || (abTestingEnabled && variants[0]?.targetUrl ? variants[0].targetUrl.trim() : ''),
      customAlias: customAlias.trim() || undefined,
      expirationDays: expirationDays ? Number(expirationDays) : undefined,
      password: password.trim() || undefined,
      previewEnabled: previewEnabled,
      iosUrl: iosUrl.trim() || undefined,
      androidUrl: androidUrl.trim() || undefined,
      desktopUrl: desktopUrl.trim() || undefined,
      webhookUrl: webhookUrl.trim() || undefined,
      webhookSecret: webhookSecret.trim() || undefined,
      abTestingEnabled: abTestingEnabled,
      variants: abTestingEnabled ? variants.map(v => ({
        label: v.label.trim(),
        targetUrl: v.targetUrl.trim(),
        weightPercent: Number(v.weightPercent)
      })) : undefined,
    };

    try {
      const res = await ApiClient.shortenUrl(req, lang, authUser);
      setResult(res);
      onSuccess(res);
      setOriginalUrl('');
      setCustomAlias('');
      setExpirationDays('');
      setPassword('');
      setIosUrl('');
      setAndroidUrl('');
      setDesktopUrl('');
      setWebhookUrl('');
      setWebhookSecret('');
      setAbTestingEnabled(false);
      setVariants([
        { label: 'Varyant A', targetUrl: '', weightPercent: 50 },
        { label: 'Varyant B', targetUrl: '', weightPercent: 50 },
      ]);
    } catch (err: any) {
      setErrorMsg(err.message || t.msgError);
    } finally {
      setLoading(false);
    }
  };

  const handleCopy = () => {
    if (!result) return;
    navigator.clipboard.writeText(result.shortUrl);
    setCopied(true);
    setTimeout(() => setCopied(false), 2000);
  };

  const handleShareTwitter = () => {
    if (!result) return;
    const url = encodeURIComponent(result.shortUrl);
    const text = encodeURIComponent(lang === 'tr' ? 'SwiftLink ile oluşturduğum link:' : 'Check out this link via SwiftLink:');
    window.open(`https://twitter.com/intent/tweet?url=${url}&text=${text}`, '_blank');
  };

  const handleShareWhatsApp = () => {
    if (!result) return;
    const url = encodeURIComponent(result.shortUrl);
    window.open(`https://api.whatsapp.com/send?text=${url}`, '_blank');
  };

  const domainPreview = originalUrl ? getDomainPreview(originalUrl) : null;

  return (
    <div className="w-full max-w-3xl mx-auto space-y-4">
      {/* Modern Command Card */}
      <div className="rounded-3xl border border-zinc-200/90 bg-white/90 backdrop-blur-xl p-5 sm:p-7 shadow-[0_10px_35px_-10px_rgba(0,0,0,0.06),0_1px_3px_0_rgba(0,0,0,0.04)] transition-all">
        <form onSubmit={handleSubmit} className="space-y-4">
          {/* Main URL Input Container */}
          <div className="relative flex flex-col sm:flex-row items-stretch gap-2.5 p-1.5 rounded-2xl bg-zinc-50 border border-zinc-200/80 focus-within:border-zinc-900 focus-within:bg-white focus-within:ring-3 focus-within:ring-zinc-900/5 transition-all">
            <div className="relative flex-1 flex items-center min-w-0 pl-3">
              <Link2 className="w-5 h-5 text-zinc-400 shrink-0 mr-2.5" />
              
              <input
                type="url"
                required
                value={originalUrl}
                onChange={(e) => setOriginalUrl(e.target.value)}
                placeholder={t.inputUrlPlaceholder}
                className="w-full bg-transparent text-xs sm:text-sm text-zinc-900 placeholder:text-zinc-400 focus:outline-none py-2.5 pr-2 font-medium"
              />

              {originalUrl && (
                <button
                  type="button"
                  onClick={() => setOriginalUrl('')}
                  className="text-zinc-400 hover:text-zinc-700 p-1 mr-1 transition-colors cursor-pointer"
                  title="Temizle"
                >
                  <X className="w-4 h-4" />
                </button>
              )}

              {!originalUrl && (
                <button
                  type="button"
                  onClick={handlePasteClipboard}
                  className="hidden sm:inline-flex items-center gap-1 px-2 py-1 rounded-md bg-white border border-zinc-200/90 text-[11px] font-medium text-zinc-600 hover:text-zinc-900 hover:border-zinc-300 shadow-2xs mr-1 transition-all cursor-pointer"
                >
                  <ClipboardPaste className="w-3 h-3 text-zinc-500" />
                  <span>{lang === 'tr' ? 'Yapıştır' : 'Paste'}</span>
                </button>
              )}
            </div>

            <Button
              type="submit"
              size="lg"
              disabled={loading || !originalUrl.trim()}
              className="h-11 px-6 rounded-xl bg-zinc-950 hover:bg-zinc-800 text-white font-semibold text-xs sm:text-sm shrink-0 shadow-xs cursor-pointer"
            >
              {loading ? (
                <span>{t.btnShortening}</span>
              ) : (
                <>
                  <span>{t.btnShorten}</span>
                  <ArrowRight className="w-4 h-4 ml-1" />
                </>
              )}
            </Button>
          </div>

          {/* Real-time Domain Chip */}
          {domainPreview && (
            <div className="flex items-center gap-1.5 text-[11px] text-zinc-500 pl-1 animate-fadeIn">
              <Globe className="w-3 h-3 text-zinc-400" />
              <span>{lang === 'tr' ? 'Hedef Alan Adı:' : 'Target Domain:'}</span>
              <span className="font-mono font-semibold text-zinc-800 bg-zinc-100 px-1.5 py-0.2 rounded border border-zinc-200/70">
                {domainPreview}
              </span>
            </div>
          )}

          {/* Toggle Advanced Options Bar */}
          <div className="flex items-center justify-between pt-1 border-t border-zinc-100">
            <button
              type="button"
              onClick={() => setShowAdvanced(!showAdvanced)}
              className="text-xs font-semibold text-zinc-600 hover:text-zinc-950 flex items-center gap-1.5 transition-colors cursor-pointer py-1"
            >
              <SlidersHorizontal className={`w-3.5 h-3.5 transition-transform ${showAdvanced ? 'rotate-90 text-zinc-900' : ''}`} />
              <span>
                {showAdvanced
                  ? (lang === 'tr' ? 'Seçenekleri Gizle' : 'Hide Custom Options')
                  : (lang === 'tr' ? 'Özel Kod, Şifre, Süre & Güvenlik Ayarla' : 'Custom Slug, Password & Expiration')}
              </span>
            </button>

            {/* Quick Feature Pills */}
            <div className="hidden sm:flex items-center gap-2 text-[11px] text-zinc-400">
              <span className="flex items-center gap-1">
                <Lock className="w-3 h-3 text-zinc-400" />
                {lang === 'tr' ? 'Şifreli' : 'Password'}
              </span>
              <span>•</span>
              <span className="flex items-center gap-1">
                <QrCode className="w-3 h-3 text-zinc-400" />
                QR
              </span>
              <span>•</span>
              <span className="flex items-center gap-1">
                <ShieldCheck className="w-3 h-3 text-emerald-600" />
                {lang === 'tr' ? 'Kalkan' : 'Shield'}
              </span>
            </div>
          </div>

          {/* Advanced Inputs Container */}
          {showAdvanced && (
            <div className="p-4 rounded-2xl bg-zinc-50/70 border border-zinc-200/80 space-y-4 animate-fadeIn">
              <div className="grid grid-cols-1 sm:grid-cols-3 gap-3.5">
                {/* Custom Alias */}
                <div className="space-y-1">
                  <label className="text-xs font-semibold text-zinc-700 flex items-center gap-1">
                    <Tag className="w-3 h-3 text-zinc-400" />
                    <span>{lang === 'tr' ? 'Özel Kod (Slug)' : 'Custom Slug'}</span>
                  </label>
                  <div className="relative">
                    <Input
                      type="text"
                      value={customAlias}
                      onChange={(e) => setCustomAlias(e.target.value)}
                      placeholder="ozel-kampanya"
                      className="font-mono text-xs h-9 bg-white"
                    />
                  </div>
                  <p className="text-[10px] text-zinc-400 font-mono">klink.to/{customAlias || '...'}</p>
                </div>

                {/* Expiration Days & Presets */}
                <div className="space-y-1">
                  <label className="text-xs font-semibold text-zinc-700 flex items-center gap-1">
                    <Clock className="w-3 h-3 text-zinc-400" />
                    <span>{lang === 'tr' ? 'Geçerlilik Süresi' : 'Expiration'}</span>
                  </label>
                  <div className="flex items-center gap-1.5">
                    <Input
                      type="number"
                      min="1"
                      max="365"
                      value={expirationDays}
                      onChange={(e) => setExpirationDays(e.target.value ? parseInt(e.target.value) : '')}
                      placeholder={lang === 'tr' ? 'Gün' : 'Days'}
                      className="text-xs h-9 bg-white flex-1"
                    />
                    <div className="flex items-center gap-1 shrink-0">
                      {[7, 30].map((preset) => (
                        <button
                          key={preset}
                          type="button"
                          onClick={() => setExpirationDays(preset)}
                          className={`px-2 py-1.5 rounded-lg text-[10px] font-mono font-medium border transition-colors cursor-pointer ${
                            expirationDays === preset
                              ? 'bg-zinc-900 text-white border-zinc-900'
                              : 'bg-white text-zinc-600 border-zinc-200/90 hover:bg-zinc-100'
                          }`}
                        >
                          {preset}G
                        </button>
                      ))}
                    </div>
                  </div>
                </div>

                {/* Password Protection */}
                <div className="space-y-1">
                  <label className="text-xs font-semibold text-zinc-700 flex items-center gap-1">
                    <Lock className="w-3 h-3 text-zinc-400" />
                    <span>{lang === 'tr' ? 'Erişim Parolası' : 'Password Lock'}</span>
                  </label>
                  <Input
                    type="password"
                    value={password}
                    onChange={(e) => setPassword(e.target.value)}
                    placeholder="••••••••"
                    className="text-xs h-9 bg-white"
                  />
                  <p className="text-[10px] text-zinc-400">{lang === 'tr' ? 'Yalnızca şifreyi bilenler açabilir' : 'Protect with access PIN'}</p>
                </div>
              </div>

              {/* Security Preview Toggle Option */}
              <div className="pt-3 border-t border-zinc-200/70 flex items-center justify-between">
                <label className="flex items-start gap-3 cursor-pointer select-none">
                  <input
                    type="checkbox"
                    checked={previewEnabled}
                    onChange={(e) => setPreviewEnabled(e.target.checked)}
                    className="w-4 h-4 mt-0.5 rounded text-zinc-900 border-zinc-300 focus:ring-zinc-900 cursor-pointer"
                  />
                  <div>
                    <span className="text-xs font-bold text-zinc-900 flex items-center gap-1.5">
                      <ShieldCheck className="w-3.5 h-3.5 text-emerald-600" />
                      {t.previewToggleOpt}
                    </span>
                    <p className="text-[11px] text-zinc-500 leading-normal">{t.previewToggleDesc}</p>
                  </div>
                </label>
              </div>

              {/* Device Targeting Section (Cihaza Göre Yönlendirme) */}
              <div className="pt-3 border-t border-zinc-200/70 space-y-2.5">
                <button
                  type="button"
                  onClick={() => setShowDeviceTargeting(!showDeviceTargeting)}
                  className="w-full flex items-center justify-between text-xs font-bold text-zinc-900 hover:text-zinc-950 py-1 transition-colors cursor-pointer"
                >
                  <div className="flex items-center gap-1.5">
                    <Smartphone className="w-4 h-4 text-zinc-700" />
                    <span>{lang === 'tr' ? '📱 Cihaza Göre Akıllı Yönlendirme (Device Targeting)' : '📱 Device-Specific Targeting'}</span>
                    {(iosUrl || androidUrl || desktopUrl) && (
                      <Badge variant="secondary" className="text-[9px] bg-emerald-100 text-emerald-800 border-emerald-200">
                        Aktif
                      </Badge>
                    )}
                  </div>
                  {showDeviceTargeting ? <ChevronUp className="w-4 h-4 text-zinc-500" /> : <ChevronDown className="w-4 h-4 text-zinc-500" />}
                </button>

                {showDeviceTargeting && (
                  <div className="p-3.5 rounded-xl bg-white border border-zinc-200/80 space-y-3 animate-fadeIn">
                    <p className="text-[11px] text-zinc-500">
                      {lang === 'tr'
                        ? 'Ziyaretçinin cihaz işletim sistemine göre farklı hedef sayfalara yönlendirin. Belirtilmeyen cihazlar ana hedefe gider.'
                        : 'Redirect visitors to tailored destination URLs based on their device OS. Unspecified devices go to the default URL.'}
                    </p>

                    {/* iOS URL */}
                    <div className="space-y-1">
                      <label className="text-[11px] font-semibold text-zinc-700 flex items-center gap-1.5">
                        <span className="w-2 h-2 rounded-full bg-zinc-900" />
                        <span>iOS (iPhone / iPad / App Store)</span>
                      </label>
                      <Input
                        type="url"
                        value={iosUrl}
                        onChange={(e) => setIosUrl(e.target.value)}
                        placeholder="https://apps.apple.com/app/id123456789"
                        className="text-xs h-8.5 font-mono bg-zinc-50/50"
                      />
                    </div>

                    {/* Android URL */}
                    <div className="space-y-1">
                      <label className="text-[11px] font-semibold text-zinc-700 flex items-center gap-1.5">
                        <span className="w-2 h-2 rounded-full bg-emerald-500" />
                        <span>Android (Google Play Store)</span>
                      </label>
                      <Input
                        type="url"
                        value={androidUrl}
                        onChange={(e) => setAndroidUrl(e.target.value)}
                        placeholder="https://play.google.com/store/apps/details?id=com.example.app"
                        className="text-xs h-8.5 font-mono bg-zinc-50/50"
                      />
                    </div>

                    {/* Desktop URL */}
                    <div className="space-y-1">
                      <label className="text-[11px] font-semibold text-zinc-700 flex items-center gap-1.5">
                        <Laptop className="w-3.5 h-3.5 text-zinc-500" />
                        <span>{lang === 'tr' ? 'Masaüstü / Web (Mac, Windows, Linux)' : 'Desktop / Web'}</span>
                      </label>
                      <Input
                        type="url"
                        value={desktopUrl}
                        onChange={(e) => setDesktopUrl(e.target.value)}
                        placeholder="https://example.com/desktop-landing"
                        className="text-xs h-8.5 font-mono bg-zinc-50/50"
                      />
                    </div>
                  </div>
                )}
              </div>

              {/* Webhook Notifications Section (Webhook Desteği) */}
              <div className="pt-3 border-t border-zinc-200/70 space-y-2.5">
                <button
                  type="button"
                  onClick={() => setShowWebhook(!showWebhook)}
                  className="w-full flex items-center justify-between text-xs font-bold text-zinc-900 hover:text-zinc-950 py-1 transition-colors cursor-pointer"
                >
                  <div className="flex items-center gap-1.5">
                    <Webhook className="w-4 h-4 text-purple-600" />
                    <span>{lang === 'tr' ? '⚡ Anlık Webhook Bildirimi (Tıklama Eventi)' : '⚡ Real-time Webhook Dispatcher'}</span>
                    {webhookUrl && (
                      <Badge variant="secondary" className="text-[9px] bg-purple-100 text-purple-800 border-purple-200">
                        Aktif
                      </Badge>
                    )}
                  </div>
                  {showWebhook ? <ChevronUp className="w-4 h-4 text-zinc-500" /> : <ChevronDown className="w-4 h-4 text-zinc-500" />}
                </button>

                {showWebhook && (
                  <div className="p-3.5 rounded-xl bg-white border border-zinc-200/80 space-y-3 animate-fadeIn">
                    <p className="text-[11px] text-zinc-500">
                      {lang === 'tr'
                        ? 'Linkinize tıklandığı anda Discord, Slack veya kendi sunucunuza anlık HTTP POST JSON telemetrisi gönderilir.'
                        : 'Receive real-time HTTP POST JSON telemetry to your Discord, Slack or custom server whenever this link is visited.'}
                    </p>

                    {/* Webhook Endpoint URL */}
                    <div className="space-y-1">
                      <label className="text-[11px] font-semibold text-zinc-700 flex items-center gap-1.5">
                        <Radio className="w-3 h-3 text-purple-600" />
                        <span>Webhook Endpoint URL</span>
                      </label>
                      <Input
                        type="url"
                        value={webhookUrl}
                        onChange={(e) => setWebhookUrl(e.target.value)}
                        placeholder="https://discord.com/api/webhooks/... veya https://api.mysite.com/hook"
                        className="text-xs h-8.5 font-mono bg-zinc-50/50"
                      />
                    </div>

                    {/* Webhook Secret Key */}
                    <div className="space-y-1">
                      <label className="text-[11px] font-semibold text-zinc-700 flex items-center justify-between">
                        <span>HMAC-SHA256 Gizli Anahtarı (Opsiyonel)</span>
                        <span className="text-[10px] text-zinc-400 font-mono">X-Klink-Signature</span>
                      </label>
                      <Input
                        type="password"
                        value={webhookSecret}
                        onChange={(e) => setWebhookSecret(e.target.value)}
                        placeholder="whsec_xxxxxxxxxxxxxxxxxxxx"
                        className="text-xs h-8.5 font-mono bg-zinc-50/50"
                      />
                    </div>
                  </div>
                )}
              </div>

              {/* A/B Split Test Section (A/B Trafik Dağıtımı) */}
              <div className="border border-zinc-200/80 rounded-2xl overflow-hidden bg-white shadow-xs">
                <button
                  type="button"
                  onClick={() => setShowAbTest(!showAbTest)}
                  className="w-full px-4 py-3 bg-zinc-50/50 hover:bg-zinc-100/50 flex items-center justify-between transition-colors text-left"
                >
                  <div className="flex items-center gap-2">
                    <Split className="w-4 h-4 text-emerald-600" />
                    <span className="text-xs font-bold text-zinc-900">
                      {lang === 'tr' ? '🧪 A/B Split Test (Çoklu Hedef URL Trafik Dağıtımı)' : '🧪 A/B Split Traffic Distribution'}
                    </span>
                    {abTestingEnabled && (
                      <Badge className="text-[10px] bg-emerald-600 text-white font-medium">
                        {lang === 'tr' ? 'Aktif' : 'Active'} ({variants.length} Varyant)
                      </Badge>
                    )}
                  </div>
                  {showAbTest ? <ChevronUp className="w-4 h-4 text-zinc-500" /> : <ChevronDown className="w-4 h-4 text-zinc-500" />}
                </button>

                {showAbTest && (
                  <div className="p-4 bg-white border-t border-zinc-100 space-y-4 animate-fadeIn">
                    <div className="flex items-center justify-between pb-3 border-b border-zinc-100">
                      <div>
                        <p className="text-xs font-bold text-zinc-900">
                          {lang === 'tr' ? 'A/B Test Modunu Aç' : 'Enable A/B Split Testing'}
                        </p>
                        <p className="text-[11px] text-zinc-500">
                          {lang === 'tr' ? 'Gelen ziyaretçileri yüzdelik oranlarla farklı sayfalara paylaştırın.' : 'Distribute incoming traffic across multiple landing pages.'}
                        </p>
                      </div>
                      <input
                        type="checkbox"
                        checked={abTestingEnabled}
                        onChange={(e) => setAbTestingEnabled(e.target.checked)}
                        className="w-4 h-4 rounded text-emerald-600 focus:ring-emerald-500 cursor-pointer"
                      />
                    </div>

                    {abTestingEnabled && (
                      <div className="space-y-3">
                        <div className="flex items-center justify-between">
                          <span className="text-xs font-semibold text-zinc-700">
                            {lang === 'tr' ? 'Hedef Varyantlar & Yüzdelik Ağırlıklar' : 'Target Variants & Weights'}
                          </span>
                          {(() => {
                            const totalW = variants.reduce((s, v) => s + (Number(v.weightPercent) || 0), 0);
                            return (
                              <Badge
                                variant={totalW === 100 ? 'default' : 'outline'}
                                className={`text-[11px] font-mono font-bold ${
                                  totalW === 100
                                    ? 'bg-emerald-600 text-white'
                                    : 'text-amber-600 border-amber-300 bg-amber-50'
                                }`}
                              >
                                {totalW === 100
                                  ? (lang === 'tr' ? 'Toplam: %100 ✓' : 'Total: 100% ✓')
                                  : (lang === 'tr' ? `Toplam: %${totalW} (Kalan: %${100 - totalW})` : `Total: ${totalW}% (Remaining: ${100 - totalW}%)`)}
                              </Badge>
                            );
                          })()}
                        </div>

                        {variants.map((variant, idx) => (
                          <div key={idx} className="p-3 rounded-xl bg-zinc-50/80 border border-zinc-200/80 space-y-2.5">
                            <div className="flex items-center justify-between gap-2">
                              <Input
                                type="text"
                                value={variant.label}
                                onChange={(e) => {
                                  const updated = [...variants];
                                  updated[idx].label = e.target.value;
                                  setVariants(updated);
                                }}
                                placeholder={`Varyant ${String.fromCharCode(65 + idx)}`}
                                className="text-xs font-semibold h-7.5 max-w-[160px] bg-white"
                              />
                              <div className="flex items-center gap-2">
                                <span className="text-[11px] font-medium text-zinc-500 font-mono">%</span>
                                <Input
                                  type="number"
                                  min={1}
                                  max={100}
                                  value={variant.weightPercent}
                                  onChange={(e) => {
                                    const val = Number(e.target.value);
                                    const updated = [...variants];
                                    updated[idx].weightPercent = val;
                                    setVariants(updated);
                                  }}
                                  className="w-16 h-7.5 text-xs text-center font-bold font-mono bg-white"
                                />
                                {variants.length > 2 && (
                                  <Button
                                    type="button"
                                    variant="ghost"
                                    size="icon"
                                    onClick={() => {
                                      setVariants(variants.filter((_, i) => i !== idx));
                                    }}
                                    className="h-7 w-7 text-red-500 hover:text-red-700 hover:bg-red-50"
                                  >
                                    <Trash2 className="w-3.5 h-3.5" />
                                  </Button>
                                )}
                              </div>
                            </div>

                            <Input
                              type="url"
                              value={variant.targetUrl}
                              onChange={(e) => {
                                const updated = [...variants];
                                updated[idx].targetUrl = e.target.value;
                                setVariants(updated);
                              }}
                              placeholder="https://sirketim.com/kampanya-sayfasi"
                              className="text-xs font-mono h-8 bg-white"
                            />
                          </div>
                        ))}

                        {variants.length < 5 && (
                          <Button
                            type="button"
                            variant="outline"
                            size="sm"
                            onClick={() => {
                              const nextLetter = String.fromCharCode(65 + variants.length);
                              setVariants([
                                ...variants,
                                { label: `Varyant ${nextLetter}`, targetUrl: '', weightPercent: 0 }
                              ]);
                            }}
                            className="w-full text-xs font-semibold border-dashed text-zinc-600 hover:text-zinc-900 h-8 cursor-pointer"
                          >
                            <Plus className="w-3.5 h-3.5 mr-1" />
                            {lang === 'tr' ? 'Yeni Hedef Varyant Ekle' : 'Add New Variant'}
                          </Button>
                        )}
                      </div>
                    )}
                  </div>
                )}
              </div>
            </div>
          )}

          {errorMsg && (
            <div className="p-3 rounded-xl bg-red-50 border border-red-200 text-red-700 text-xs font-semibold flex items-center gap-2 animate-fadeIn">
              <span>⚠️ {errorMsg}</span>
            </div>
          )}
        </form>
      </div>

      {/* Success Result Showcase Card */}
      {result && (
        <div className="p-5 sm:p-6 rounded-3xl bg-zinc-950 text-white shadow-xl border border-zinc-800 space-y-4 animate-fadeIn">
          <div className="flex flex-col sm:flex-row items-start sm:items-center justify-between gap-3">
            <div className="flex items-center gap-2.5">
              <div className="w-8 h-8 rounded-xl bg-emerald-500/20 text-emerald-400 border border-emerald-500/30 flex items-center justify-center shrink-0">
                <Check className="w-4 h-4" />
              </div>
              <div>
                <p className="text-xs font-semibold text-zinc-400">{lang === 'tr' ? 'Kısa Bağlantınız Başarıyla Üretildi' : 'Short Link Ready'}:</p>
                <a
                  href={result.shortUrl}
                  target="_blank"
                  rel="noreferrer"
                  className="text-lg sm:text-xl font-bold font-mono text-white hover:text-emerald-400 transition-colors truncate block mt-0.5"
                >
                  {result.shortUrl}
                </a>
              </div>
            </div>

            {/* Quick Action Buttons */}
            <div className="flex items-center gap-2 w-full sm:w-auto justify-end shrink-0 pt-2 sm:pt-0">
              <Button
                variant="outline"
                size="sm"
                onClick={handleCopy}
                className="bg-white text-zinc-950 hover:bg-zinc-100 border-white font-semibold text-xs h-9 px-4"
              >
                {copied ? <Check className="w-3.5 h-3.5 text-emerald-600 mr-1" /> : <Copy className="w-3.5 h-3.5 text-zinc-900 mr-1" />}
                <span>{copied ? t.msgCopied : t.btnCopy}</span>
              </Button>

              <Button
                variant="outline"
                size="sm"
                onClick={() => onOpenQr(result.shortCode)}
                className="bg-zinc-900 border-zinc-700 text-zinc-200 hover:bg-zinc-800 hover:text-white h-9 px-3"
                title="QR Kod İndir"
              >
                <QrCode className="w-3.5 h-3.5 mr-1" />
                <span>QR</span>
              </Button>

              <Button
                variant="outline"
                size="sm"
                asChild
                className="bg-zinc-900 border-zinc-700 text-zinc-200 hover:bg-zinc-800 hover:text-white h-9 px-3"
                title="Önizleme Sayfası"
              >
                <a href={`/preview/${result.shortCode}`} target="_blank" rel="noreferrer">
                  <Eye className="w-3.5 h-3.5 mr-1" />
                  <span className="hidden sm:inline">{t.btnPreview}</span>
                </a>
              </Button>

              <Button
                variant="outline"
                size="sm"
                asChild
                className="bg-zinc-900 border-zinc-700 text-zinc-200 hover:bg-zinc-800 hover:text-white h-9 px-3"
                title="Bağlantıya Git"
              >
                <a href={result.shortUrl} target="_blank" rel="noreferrer">
                  <ExternalLink className="w-3.5 h-3.5" />
                </a>
              </Button>
            </div>
          </div>

          {/* A/B Test Variant Banner in Result */}
          {result.abTestingEnabled && result.variants && result.variants.length > 0 && (
            <div className="p-3 rounded-2xl bg-zinc-900 border border-zinc-800 space-y-2">
              <div className="flex items-center justify-between text-xs">
                <span className="font-bold text-emerald-400 flex items-center gap-1.5">
                  <Split className="w-3.5 h-3.5" />
                  {lang === 'tr' ? 'Aktif A/B Trafik Dağıtımı' : 'Active A/B Traffic Split'}
                </span>
                <span className="text-[11px] text-zinc-400">
                  {result.variants.length} {lang === 'tr' ? 'Hedef Sayfa' : 'Targets'}
                </span>
              </div>
              <div className="grid grid-cols-1 sm:grid-cols-2 gap-2">
                {result.variants.map((v, i) => (
                  <div key={i} className="p-2 rounded-xl bg-zinc-950/80 border border-zinc-800/80 text-[11px] flex items-center justify-between">
                    <span className="font-semibold text-zinc-200 truncate">{v.label}</span>
                    <Badge variant="outline" className="text-[10px] text-emerald-400 border-emerald-500/30 bg-emerald-950/40">
                      %{v.weightPercent}
                    </Badge>
                  </div>
                ))}
              </div>
            </div>
          )}

          {/* Social Share & Details Bar */}
          <div className="pt-3 border-t border-zinc-800 flex flex-col sm:flex-row items-start sm:items-center justify-between gap-2 text-xs text-zinc-400">
            <div className="flex items-center gap-2 truncate max-w-md">
              <span className="text-[11px] text-zinc-500 font-mono">Hedef:</span>
              <span className="truncate text-zinc-300 font-mono text-[11px]">{result.originalUrl}</span>
            </div>

            <div className="flex items-center gap-2 self-end sm:self-auto">
              <span className="text-[11px] text-zinc-500">Paylaş:</span>
              <button
                type="button"
                onClick={handleShareTwitter}
                className="px-2 py-1 rounded-md bg-zinc-900 hover:bg-zinc-800 text-zinc-300 text-[11px] border border-zinc-800 transition-colors cursor-pointer"
              >
                Twitter/X
              </button>
              <button
                type="button"
                onClick={handleShareWhatsApp}
                className="px-2 py-1 rounded-md bg-zinc-900 hover:bg-zinc-800 text-zinc-300 text-[11px] border border-zinc-800 transition-colors cursor-pointer"
              >
                WhatsApp
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

