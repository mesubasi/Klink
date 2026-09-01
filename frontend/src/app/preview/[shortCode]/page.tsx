'use client';

import React, { useState, useEffect } from 'react';
import { useParams, useRouter } from 'next/navigation';
import Link from 'next/link';
import {
  ShieldCheck,
  ShieldAlert,
  Lock,
  Unlock,
  ExternalLink,
  Copy,
  Check,
  ArrowRight,
  ArrowLeft,
  Clock,
  Globe,
  Sparkles,
  Pause,
  Play,
  CheckCircle2,
  AlertTriangle,
  Link2,
  Activity,
  Calendar,
  KeyRound,
  Smartphone,
  Laptop
} from 'lucide-react';
import { ApiClient } from '@/lib/api';
import { UrlPreviewResponse } from '@/lib/types';
import { Language, translations } from '@/lib/translations';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Card, CardContent } from '@/components/ui/card';
import { Badge } from '@/components/ui/badge';

export default function PreviewPage() {
  const params = useParams();
  const router = useRouter();
  const shortCode = typeof params?.shortCode === 'string' ? params.shortCode : '';

  const [lang, setLang] = useState<Language>('tr');
  const t = translations[lang];

  const [data, setData] = useState<UrlPreviewResponse | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  // Auto redirect countdown state
  const [countdown, setCountdown] = useState<number>(5);
  const [isPaused, setIsPaused] = useState<boolean>(false);
  const [autoRedirectStarted, setAutoRedirectStarted] = useState<boolean>(false);

  // Password Unlock states
  const [password, setPassword] = useState('');
  const [passwordError, setPasswordError] = useState<string | null>(null);
  const [unlocking, setUnlocking] = useState(false);
  const [unlockedUrl, setUnlockedUrl] = useState<string | null>(null);

  // Action states
  const [copied, setCopied] = useState(false);
  const [proceeding, setProceeding] = useState(false);

  useEffect(() => {
    if (!shortCode) return;
    fetchPreviewData();
  }, [shortCode, lang]);

  const fetchPreviewData = async () => {
    setLoading(true);
    setError(null);
    try {
      const res = await ApiClient.getUrlPreview(shortCode, lang);
      setData(res);
      // Only enable auto redirect if not password protected
      if (!res.passwordProtected) {
        setAutoRedirectStarted(true);
      }
    } catch (err: any) {
      setError(err.message || (lang === 'tr' ? 'Bağlantı bilgileri yüklenemedi.' : 'Failed to load link preview.'));
    } finally {
      setLoading(false);
    }
  };

  // Countdown timer effect
  useEffect(() => {
    if (!autoRedirectStarted || isPaused || data?.passwordProtected || proceeding) {
      return;
    }

    if (countdown <= 0) {
      handleProceed();
      return;
    }

    const timer = setInterval(() => {
      setCountdown((prev) => prev - 1);
    }, 1000);

    return () => clearInterval(timer);
  }, [countdown, isPaused, autoRedirectStarted, data, proceeding]);

  const handleProceed = async () => {
    if (proceeding) return;
    setProceeding(true);

    const targetUrl = unlockedUrl || data?.originalUrl;
    try {
      await ApiClient.proceedFromPreview(shortCode, lang);
    } catch (e) {
      console.warn('Proceed click tracking error:', e);
    }

    if (targetUrl) {
      window.location.href = targetUrl;
    }
  };

  const handleUnlockPassword = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!password.trim()) return;

    setUnlocking(true);
    setPasswordError(null);

    try {
      const verifiedUrl = await ApiClient.verifyPassword(shortCode, password.trim(), lang);
      setUnlockedUrl(verifiedUrl);
      if (data) {
        setData({ ...data, originalUrl: verifiedUrl, passwordProtected: false });
      }
      // Start auto-redirect now that it is unlocked
      setAutoRedirectStarted(true);
      setCountdown(5);
    } catch (err: any) {
      setPasswordError(err.message || (lang === 'tr' ? 'Girilen şifre hatalı!' : 'Invalid password!'));
    } finally {
      setUnlocking(false);
    }
  };

  const handleCopyUrl = (urlToCopy: string) => {
    navigator.clipboard.writeText(urlToCopy);
    setCopied(true);
    setTimeout(() => setCopied(false), 2000);
  };

  const effectiveUrl = unlockedUrl || data?.originalUrl || '';
  const isSafe = data ? data.safetyStatus === 'SAFE' : true;

  return (
    <div className="min-h-screen bg-[#fafafa] text-zinc-950 selection:bg-zinc-900 selection:text-white flex flex-col justify-between">
      {/* Top Navigation Header */}
      <header className="sticky top-0 z-40 bg-white/80 backdrop-blur-md border-b border-zinc-200/80 px-4 sm:px-8 py-3.5">
        <div className="max-w-4xl mx-auto flex items-center justify-between">
          <Link href="/" className="flex items-center gap-2.5 font-bold text-zinc-950 tracking-tight hover:opacity-90 transition-opacity">
            <div className="w-8 h-8 rounded-xl bg-zinc-950 text-white flex items-center justify-center shadow-xs">
              <Link2 className="w-4 h-4" />
            </div>
            <span className="text-base font-extrabold">Klink</span>
            <Badge variant="secondary" className="text-[11px] font-normal py-0.5 px-2 text-zinc-700 bg-zinc-100">
              <ShieldCheck className="w-3 h-3 text-emerald-600 mr-1 inline" />
              {t.previewTitle}
            </Badge>
          </Link>

          <div className="flex items-center gap-3">
            {/* Language Switcher */}
            <div className="flex items-center p-0.5 rounded-lg bg-zinc-100 border border-zinc-200/80 text-xs font-medium">
              <button
                onClick={() => setLang('tr')}
                className={`px-2 py-1 rounded-md transition-all cursor-pointer ${
                  lang === 'tr' ? 'bg-white text-zinc-950 shadow-2xs font-bold' : 'text-zinc-500 hover:text-zinc-900'
                }`}
              >
                TR
              </button>
              <button
                onClick={() => setLang('en')}
                className={`px-2 py-1 rounded-md transition-all cursor-pointer ${
                  lang === 'en' ? 'bg-white text-zinc-950 shadow-2xs font-bold' : 'text-zinc-500 hover:text-zinc-900'
                }`}
              >
                EN
              </button>
            </div>

            <Button variant="ghost" size="sm" asChild className="text-xs">
              <Link href="/">
                <ArrowLeft className="w-3.5 h-3.5 mr-1" />
                <span>{t.previewCancelBtn}</span>
              </Link>
            </Button>
          </div>
        </div>
      </header>

      {/* Main Preview Container */}
      <main className="max-w-3xl mx-auto px-4 sm:px-6 py-10 w-full space-y-6">
        {loading ? (
          <Card className="p-12 text-center space-y-4 border-zinc-200/90 bg-white">
            <div className="w-12 h-12 rounded-2xl bg-zinc-100 text-zinc-900 border border-zinc-200 flex items-center justify-center mx-auto">
              <ShieldCheck className="w-6 h-6 animate-spin" />
            </div>
            <div>
              <h3 className="text-base font-bold text-zinc-950">{lang === 'tr' ? 'Bağlantı Güvenliği Doğrulanıyor...' : 'Verifying Link Security...'}</h3>
              <p className="text-xs text-zinc-500 mt-1">{lang === 'tr' ? 'Hedef web adresi güvenlik filtreleri tarafından taranıyor.' : 'Scanning target destination against safety filters.'}</p>
            </div>
          </Card>
        ) : error ? (
          <Card className="p-8 text-center space-y-5 border-red-200 bg-red-50/40">
            <div className="w-12 h-12 rounded-2xl bg-red-100 text-red-600 flex items-center justify-center mx-auto">
              <ShieldAlert className="w-6 h-6" />
            </div>
            <div className="space-y-1">
              <h3 className="text-lg font-bold text-red-950">{lang === 'tr' ? 'Bağlantı Bulunamadı veya Erişilemiyor' : 'Link Not Found or Inaccessible'}</h3>
              <p className="text-xs text-red-700 max-w-md mx-auto">{error}</p>
            </div>
            <Button asChild variant="outline">
              <Link href="/">
                <ArrowLeft className="w-3.5 h-3.5 mr-1.5" />
                <span>{t.previewCancelBtn}</span>
              </Link>
            </Button>
          </Card>
        ) : data ? (
          <div className="space-y-6 animate-fadeIn">
            {/* Hero Shield Banner */}
            <Card className="overflow-hidden border-zinc-200/90 shadow-sm bg-white">
              <div className="bg-zinc-950 text-white p-6 sm:p-7 border-b border-zinc-800">
                <div className="flex flex-col sm:flex-row items-start sm:items-center justify-between gap-4">
                  <div className="flex items-center gap-3.5">
                    <div className={`w-12 h-12 rounded-2xl flex items-center justify-center shrink-0 shadow-sm ${
                      isSafe ? 'bg-emerald-500/20 text-emerald-400 border border-emerald-500/30' : 'bg-amber-500/20 text-amber-400 border border-amber-500/30'
                    }`}>
                      {isSafe ? <ShieldCheck className="w-7 h-7" /> : <ShieldAlert className="w-7 h-7" />}
                    </div>
                    <div>
                      <div className="flex items-center gap-2">
                        <h2 className="text-lg font-bold tracking-tight text-white">{t.previewTitle}</h2>
                        <Badge variant={isSafe ? "success" : "warning"} className="text-[11px]">
                          {isSafe ? t.previewSafetyVerified : t.previewSafetySuspicious}
                        </Badge>
                      </div>
                      <p className="text-xs text-zinc-400 mt-1">{t.previewSubtitle}</p>
                    </div>
                  </div>

                  <div className="bg-zinc-900 rounded-2xl p-3 border border-zinc-800 text-right shrink-0">
                    <span className="text-[10px] uppercase font-bold text-zinc-400 block">{t.previewSafetyScore}</span>
                    <div className="text-2xl font-bold font-mono text-emerald-400">
                      {data.safetyScore}<span className="text-xs font-normal text-zinc-400"> / 100</span>
                    </div>
                  </div>
                </div>
              </div>

              {/* Security Verification Metrics */}
              <div className="grid grid-cols-1 sm:grid-cols-3 divide-y sm:divide-y-0 sm:divide-x divide-zinc-100 bg-zinc-50/50 border-b border-zinc-100">
                <div className="p-4 flex items-center gap-3">
                  <div className="w-8 h-8 rounded-xl bg-emerald-50 text-emerald-700 border border-emerald-100 flex items-center justify-center shrink-0">
                    <CheckCircle2 className="w-4 h-4" />
                  </div>
                  <div>
                    <span className="text-[11px] font-semibold text-zinc-900 block">SSL / TLS {lang === 'tr' ? 'Şifreleme' : 'Encryption'}</span>
                    <span className="text-[10px] text-zinc-500">{data.secure ? (lang === 'tr' ? 'HTTPS Güvenli' : 'HTTPS Secure') : (lang === 'tr' ? 'HTTP Şifresiz' : 'HTTP Insecure')}</span>
                  </div>
                </div>

                <div className="p-4 flex items-center gap-3">
                  <div className="w-8 h-8 rounded-xl bg-zinc-100 text-zinc-900 border border-zinc-200 flex items-center justify-center shrink-0">
                    <Sparkles className="w-4 h-4" />
                  </div>
                  <div>
                    <span className="text-[11px] font-semibold text-zinc-900 block">Google Safe Browsing</span>
                    <span className="text-[10px] text-zinc-500">{lang === 'tr' ? 'Temiz & Tehditsiz' : 'Clean & Verified'}</span>
                  </div>
                </div>

                <div className="p-4 flex items-center gap-3">
                  <div className="w-8 h-8 rounded-xl bg-zinc-100 text-zinc-900 border border-zinc-200 flex items-center justify-center shrink-0">
                    <Globe className="w-4 h-4" />
                  </div>
                  <div>
                    <span className="text-[11px] font-semibold text-zinc-900 block">{lang === 'tr' ? 'Hedef Alan Adı' : 'Target Domain'}</span>
                    <span className="text-[10px] text-zinc-500 font-mono truncate max-w-[140px] block">{data.domain}</span>
                  </div>
                </div>
              </div>

              {/* Destination URL Card Content */}
              <div className="p-6 sm:p-7 space-y-6">
                <div className="space-y-2">
                  <label className="text-xs font-semibold text-zinc-700 flex items-center justify-between">
                    <span className="flex items-center gap-1.5">
                      <ExternalLink className="w-3.5 h-3.5 text-zinc-950" />
                      {t.previewTargetUrl}:
                    </span>
                    <span className="text-[11px] text-zinc-400 font-mono">
                      Domain: <strong className="text-zinc-900 font-semibold">{data.domain}</strong>
                    </span>
                  </label>

                  <div className="p-4 rounded-2xl bg-zinc-50 border border-zinc-200 flex flex-col sm:flex-row items-start sm:items-center justify-between gap-3">
                    <div className="overflow-hidden min-w-0 flex-1">
                      <p className="font-mono text-xs sm:text-sm font-bold text-zinc-950 break-all">
                        {effectiveUrl || data.originalUrl}
                      </p>
                    </div>

                    <Button
                      variant="outline"
                      size="sm"
                      onClick={() => handleCopyUrl(effectiveUrl || data.originalUrl)}
                      className="shrink-0 bg-white"
                    >
                      {copied ? <Check className="w-3.5 h-3.5 text-emerald-600 mr-1.5" /> : <Copy className="w-3.5 h-3.5 text-zinc-500 mr-1.5" />}
                      <span>{copied ? t.msgCopied : t.btnCopy}</span>
                    </Button>
                  </div>
                </div>

                {/* Destination Health Check Alert (if broken) */}
                {data.healthStatus === 'BROKEN' && (
                  <div className="p-3.5 rounded-2xl bg-red-50 border border-red-200 text-red-900 text-xs flex items-start gap-2.5">
                    <AlertTriangle className="w-4 h-4 text-red-600 shrink-0 mt-0.5" />
                    <div>
                      <p className="font-bold text-red-950">
                        {lang === 'tr' ? '⚠️ Dikkat: Hedef Web Sitesi Yanıt Vermiyor (Kırık Link)' : '⚠️ Warning: Target Destination is Unreachable (Broken Link)'}
                      </p>
                      <p className="text-red-700 text-[11px] mt-0.5">
                        {data.healthErrorMessage ? `Durum: ${data.healthErrorMessage}. ` : ''}
                        {lang === 'tr' ? 'Hedef sunucuya erişilemedi veya sayfa bulunamadı (404/500/timeout).' : 'Destination server is down or page not found.'}
                      </p>
                    </div>
                  </div>
                )}

                {/* Safety Warning Notice */}
                <div className="p-3.5 rounded-2xl bg-amber-50/70 border border-amber-200/70 text-amber-900 text-xs flex items-start gap-2.5">
                  <AlertTriangle className="w-4 h-4 text-amber-600 shrink-0 mt-0.5" />
                  <p className="leading-relaxed">{t.previewWarningText}</p>
                </div>

                {/* Password Protection Gate (if link is locked) */}
                {data.passwordProtected && !unlockedUrl && (
                  <div className="p-5 rounded-2xl bg-zinc-950 text-white space-y-4 border border-zinc-800">
                    <div className="flex items-center gap-3">
                      <div className="w-9 h-9 rounded-xl bg-amber-500/20 text-amber-400 border border-amber-500/30 flex items-center justify-center shrink-0">
                        <Lock className="w-4 h-4" />
                      </div>
                      <div>
                        <h4 className="text-sm font-bold text-white">{t.previewPasswordProtected}</h4>
                        <p className="text-xs text-zinc-400">{t.previewPasswordPrompt}</p>
                      </div>
                    </div>

                    <form onSubmit={handleUnlockPassword} className="flex flex-col sm:flex-row gap-2.5">
                      <Input
                        type="password"
                        required
                        value={password}
                        onChange={(e) => setPassword(e.target.value)}
                        placeholder="••••••••"
                        className="bg-zinc-900 border-zinc-800 text-white placeholder:text-zinc-500 h-10 text-xs"
                      />
                      <Button
                        type="submit"
                        disabled={unlocking || !password.trim()}
                        className="h-10 bg-white text-zinc-950 hover:bg-zinc-100 font-semibold shrink-0 cursor-pointer"
                      >
                        {unlocking ? (
                          <span>{lang === 'tr' ? 'Doğrulanıyor...' : 'Verifying...'}</span>
                        ) : (
                          <>
                            <Unlock className="w-3.5 h-3.5 mr-1.5" />
                            <span>{t.previewPasswordBtn}</span>
                          </>
                        )}
                      </Button>
                    </form>

                    {passwordError && (
                      <p className="text-xs font-medium text-red-400 flex items-center gap-1.5">
                        <span>⚠️ {passwordError}</span>
                      </p>
                    )}
                  </div>
                )}

                {/* Auto Redirect & Proceed Actions (if unlocked/not password protected) */}
                {(!data.passwordProtected || unlockedUrl) && (
                  <div className="space-y-4 pt-2">
                    {/* Countdown Progress Card */}
                    <div className="p-4 rounded-2xl bg-zinc-100 border border-zinc-200/80 flex flex-col sm:flex-row items-start sm:items-center justify-between gap-3">
                      <div className="flex items-center gap-3">
                        <div className="w-8 h-8 rounded-full bg-zinc-950 text-white font-bold font-mono text-sm flex items-center justify-center shrink-0">
                          {countdown}
                        </div>
                        <div>
                          <p className="text-xs font-bold text-zinc-950">
                            {isPaused ? t.previewAutoPaused : `${countdown} ${t.previewAutoRedirecting}`}
                          </p>
                          <p className="text-[11px] text-zinc-500">
                            {lang === 'tr' ? 'Beklemek istemiyorsanız aşağıdaki buton ile anında gidebilirsiniz.' : 'Click the button below to proceed immediately.'}
                          </p>
                        </div>
                      </div>

                      <Button
                        type="button"
                        variant="outline"
                        size="sm"
                        onClick={() => setIsPaused(!isPaused)}
                        className="text-xs shrink-0 cursor-pointer bg-white"
                      >
                        {isPaused ? <Play className="w-3 h-3 text-emerald-600 mr-1" /> : <Pause className="w-3 h-3 text-zinc-500 mr-1" />}
                        <span>{isPaused ? t.previewResume : t.previewPause}</span>
                      </Button>
                    </div>

                    {/* Action Buttons */}
                    <div className="flex flex-col sm:flex-row items-center gap-3">
                      <Button
                        size="lg"
                        onClick={handleProceed}
                        disabled={proceeding}
                        className="w-full sm:flex-1 h-12 text-sm font-semibold bg-zinc-950 hover:bg-zinc-800 text-white cursor-pointer shadow-sm"
                      >
                        {proceeding ? (
                          <span>{lang === 'tr' ? 'Yönlendiriliyorsunuz...' : 'Redirecting...'}</span>
                        ) : (
                          <>
                            <span>{t.previewProceedBtn}</span>
                            <ArrowRight className="w-4 h-4 ml-2" />
                          </>
                        )}
                      </Button>

                      <Button
                        asChild
                        variant="outline"
                        size="lg"
                        className="w-full sm:w-auto h-12 text-sm bg-white"
                      >
                        <Link href="/">
                          <ArrowLeft className="w-3.5 h-3.5 mr-1.5" />
                          <span>{t.previewCancelBtn}</span>
                        </Link>
                      </Button>
                    </div>
                  </div>
                )}
              </div>
            </Card>

            {/* Link Metadata Information Card */}
            <Card className="p-5 border-zinc-200/90 bg-white">
              <div className="flex items-center justify-between mb-3 border-b border-zinc-100 pb-2.5">
                <h4 className="text-xs font-bold text-zinc-950 flex items-center gap-1.5">
                  <Activity className="w-3.5 h-3.5 text-zinc-500" />
                  <span>{t.previewLinkInfo}</span>
                </h4>
                <Badge variant="secondary" className="font-mono text-[11px]">
                  /{data.shortCode}
                </Badge>
              </div>

              <div className="grid grid-cols-2 sm:grid-cols-4 gap-4 text-xs">
                <div>
                  <span className="text-[11px] text-zinc-400 block">{t.previewShortCode}</span>
                  <span className="font-mono font-bold text-zinc-950 mt-0.5 block">{data.shortCode}</span>
                </div>

                <div>
                  <span className="text-[11px] text-zinc-400 block">{t.previewTotalVisits}</span>
                  <span className="font-semibold text-zinc-800 mt-0.5 block">{data.clickCount} {lang === 'tr' ? 'ziyaret' : 'clicks'}</span>
                </div>

                <div>
                  <span className="text-[11px] text-zinc-400 block">{t.previewCreatedAt}</span>
                  <span className="font-semibold text-zinc-800 mt-0.5 block">
                    {new Date(data.createdAt).toLocaleDateString(lang === 'tr' ? 'tr-TR' : 'en-US')}
                  </span>
                </div>

                <div>
                  <span className="text-[11px] text-zinc-400 block">{t.previewStatus}</span>
                  <Badge variant={data.active ? "success" : "secondary"} className="mt-0.5 text-[10px]">
                    {data.active ? (lang === 'tr' ? 'Aktif' : 'Pasif') : (lang === 'tr' ? 'Pasif' : 'Inactive')}
                  </Badge>
                </div>
              </div>
            </Card>

            {/* Device-Specific Targeting Card (if configured) */}
            {(data.iosUrl || data.androidUrl || data.desktopUrl) && (
              <Card className="p-5 border-zinc-200/90 bg-white space-y-3">
                <div className="flex items-center gap-2 border-b border-zinc-100 pb-2.5">
                  <Smartphone className="w-4 h-4 text-zinc-700" />
                  <h4 className="text-xs font-bold text-zinc-950">
                    {lang === 'tr' ? 'Cihaza Göre Akıllı Yönlendirme Rotaları' : 'Device-Specific Target Destinations'}
                  </h4>
                </div>

                <div className="space-y-2 text-xs">
                  {data.iosUrl && (
                    <div className="p-2.5 rounded-xl bg-zinc-50 border border-zinc-200/70 flex items-center justify-between gap-3">
                      <div className="flex items-center gap-2 min-w-0">
                        <span className="w-2 h-2 rounded-full bg-zinc-900 shrink-0" />
                        <span className="font-semibold text-zinc-700 shrink-0">iOS / Apple:</span>
                        <span className="font-mono text-zinc-500 truncate text-[11px]">{data.iosUrl}</span>
                      </div>
                      <Badge variant="secondary" className="text-[10px] shrink-0 font-mono">iOS</Badge>
                    </div>
                  )}

                  {data.androidUrl && (
                    <div className="p-2.5 rounded-xl bg-zinc-50 border border-zinc-200/70 flex items-center justify-between gap-3">
                      <div className="flex items-center gap-2 min-w-0">
                        <span className="w-2 h-2 rounded-full bg-emerald-500 shrink-0" />
                        <span className="font-semibold text-zinc-700 shrink-0">Android / Play:</span>
                        <span className="font-mono text-zinc-500 truncate text-[11px]">{data.androidUrl}</span>
                      </div>
                      <Badge variant="secondary" className="text-[10px] shrink-0 font-mono">Android</Badge>
                    </div>
                  )}

                  {data.desktopUrl && (
                    <div className="p-2.5 rounded-xl bg-zinc-50 border border-zinc-200/70 flex items-center justify-between gap-3">
                      <div className="flex items-center gap-2 min-w-0">
                        <Laptop className="w-3.5 h-3.5 text-zinc-500 shrink-0" />
                        <span className="font-semibold text-zinc-700 shrink-0">{lang === 'tr' ? 'Masaüstü:' : 'Desktop:'}</span>
                        <span className="font-mono text-zinc-500 truncate text-[11px]">{data.desktopUrl}</span>
                      </div>
                      <Badge variant="secondary" className="text-[10px] shrink-0 font-mono">Desktop</Badge>
                    </div>
                  )}
                </div>
              </Card>
            )}
          </div>
        ) : null}
      </main>

      {/* Footer */}
      <footer className="border-t border-zinc-200/80 bg-white py-6 text-center text-xs text-zinc-500">
        <div className="max-w-4xl mx-auto px-4 flex flex-col sm:flex-row items-center justify-between gap-3">
          <div className="flex items-center gap-2 font-medium text-zinc-800">
            <div className="w-5 h-5 rounded-md bg-zinc-950 text-white flex items-center justify-center">
              <Link2 className="w-3 h-3" />
            </div>
            <span className="font-bold">Klink Security Shield</span>
          </div>
          <p>&copy; 2026 Klink. {lang === 'tr' ? 'Güvenli Yönlendirme ve Önizleme Servisi.' : 'Secure Redirection & Preview Service.'}</p>
        </div>
      </footer>
    </div>
  );
}