'use client';

import React, { useState, useEffect } from 'react';
import {
  Key,
  KeyRound,
  ShieldCheck,
  Clock,
  CheckCircle2,
  AlertCircle,
  Copy,
  Check,
  RotateCcw,
  Code2,
  Terminal,
  Globe,
  ExternalLink,
  Layers,
  Sparkles,
  Send,
  Zap,
  Activity,
  Eye,
  EyeOff,
  Building2,
  HelpCircle
} from 'lucide-react';
import { Language, translations } from '@/lib/translations';
import { ApiKeyResponse, ApiKeyApplyRequest } from '@/lib/types';
import { ApiClient } from '@/lib/api';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Card, CardHeader, CardTitle, CardDescription, CardContent } from '@/components/ui/card';
import { Badge } from '@/components/ui/badge';

interface DeveloperApiWidgetProps {
  lang: Language;
  authUser?: { u?: string; p?: string; token?: string } | null;
}

export const DeveloperApiWidget: React.FC<DeveloperApiWidgetProps> = ({ lang, authUser }) => {
  const [loading, setLoading] = useState(true);
  const [submitting, setSubmitting] = useState(false);
  const [apiKeys, setApiKeys] = useState<ApiKeyResponse[]>([]);
  const [errorMsg, setErrorMsg] = useState('');
  const [successMsg, setSuccessMsg] = useState('');

  // Application form states
  const [appName, setAppName] = useState('');
  const [purpose, setPurpose] = useState('');
  const [websiteUrl, setWebsiteUrl] = useState('');
  const [expectedMonthlyClicks, setExpectedMonthlyClicks] = useState('1.000 - 10.000');
  const [ipWhitelist, setIpWhitelist] = useState('');

  // Key visibility & copy state
  const [revealedKeys, setRevealedKeys] = useState<Record<string, boolean>>({});
  const [copiedKeyId, setCopiedKeyId] = useState<string | null>(null);
  const [selectedSnippet, setSelectedSnippet] = useState<'curl' | 'javascript' | 'python'>('curl');

  useEffect(() => {
    loadMyKeys();
  }, [authUser]);

  const loadMyKeys = async () => {
    setLoading(true);
    try {
      const list = await ApiClient.getMyApiKeys(lang, authUser || undefined);
      setApiKeys(list);
    } catch (e: any) {
      console.warn('API keys load error', e);
    } finally {
      setLoading(false);
    }
  };

  const handleApply = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!appName.trim() || !purpose.trim()) {
      setErrorMsg('Lütfen uygulama adını ve kullanım amacını eksiksiz doldurunuz.');
      return;
    }

    setSubmitting(true);
    setErrorMsg('');
    setSuccessMsg('');

    const req: ApiKeyApplyRequest = {
      appName: appName.trim(),
      purpose: purpose.trim(),
      websiteUrl: websiteUrl.trim() || undefined,
      expectedMonthlyClicks: expectedMonthlyClicks,
      ipWhitelist: ipWhitelist.trim() || undefined,
    };

    try {
      const res = await ApiClient.applyForApiKey(req, lang, authUser || undefined);
      setApiKeys([res, ...apiKeys]);
      setSuccessMsg('API anahtarı başvurunuz başarıyla alındı. Admin ekibimiz inceledikten sonra onaylanacaktır.');
      setAppName('');
      setPurpose('');
      setWebsiteUrl('');
      setIpWhitelist('');
    } catch (err: any) {
      setErrorMsg(err.message || 'Başvuru gönderilirken bir hata oluştu.');
    } finally {
      setSubmitting(false);
    }
  };

  const handleCopyKey = (keyId: string, keyValue: string) => {
    navigator.clipboard.writeText(keyValue);
    setCopiedKeyId(keyId);
    setTimeout(() => setCopiedKeyId(null), 2000);
  };

  const toggleReveal = (keyId: string) => {
    setRevealedKeys(prev => ({ ...prev, [keyId]: !prev[keyId] }));
  };

  const handleRegenerate = async (keyId: string) => {
    if (!confirm('Mevcut API anahtarınız iptal edilecek ve yeni bir anahtar üretilecektir. Devam etmek istiyor musunuz?')) {
      return;
    }

    try {
      const updated = await ApiClient.regenerateApiKey(keyId, lang, authUser || undefined);
      setApiKeys(apiKeys.map(k => k.id === keyId ? updated : k));
      setSuccessMsg('API anahtarınız başarıyla yenilendi.');
      setTimeout(() => setSuccessMsg(''), 3000);
    } catch (e: any) {
      setErrorMsg(e.message || 'Anahtar yenilenemedi.');
    }
  };

  const activeKey = apiKeys.find(k => k.status === 'APPROVED');
  const pendingKey = apiKeys.find(k => k.status === 'PENDING');
  const rejectedKey = apiKeys.find(k => k.status === 'REJECTED');

  const displayKeyString = activeKey?.rawKey || activeKey?.keyPrefix || 'kl_live_9a8b7c6d5e4f3a2b1c0d9e8f7a6b5c4d';

  // Code snippets generator
  const getSnippet = () => {
    const keyVal = activeKey ? displayKeyString : 'kl_live_YOUR_API_KEY_HERE';
    if (selectedSnippet === 'curl') {
      return `curl -X POST https://klink.to/api/v1/urls/shorten \\
  -H "Content-Type: application/json" \\
  -H "X-API-KEY: ${keyVal}" \\
  -d '{
    "originalUrl": "https://example.com/target-page",
    "customAlias": "my-promo-link"
  }'`;
    } else if (selectedSnippet === 'javascript') {
      return `const response = await fetch('https://klink.to/api/v1/urls/shorten', {
  method: 'POST',
  headers: {
    'Content-Type': 'application/json',
    'X-API-KEY': '${keyVal}'
  },
  body: JSON.stringify({
    originalUrl: 'https://example.com/target-page',
    customAlias: 'my-promo-link'
  })
});

const data = await response.json();
console.log('Kısaltılmış Link:', data.shortUrl);`;
    } else {
      return `import requests

url = "https://klink.to/api/v1/urls/shorten"
headers = {
    "Content-Type": "application/json",
    "X-API-KEY": "${keyVal}"
}
payload = {
    "originalUrl": "https://example.com/target-page",
    "customAlias": "my-promo-link"
}

response = requests.post(url, json=payload, headers=headers)
print("Kısaltılmış Link:", response.json().get("shortUrl"))`;
    }
  };

  if (loading) {
    return (
      <div className="p-12 text-center text-zinc-500">
        <div className="w-8 h-8 rounded-full border-2 border-zinc-950 border-t-transparent animate-spin mx-auto mb-3" />
        <p className="text-xs font-semibold">Geliştirici API bilgileri yükleniyor...</p>
      </div>
    );
  }

  return (
    <div className="space-y-6">
      {/* Top Banner */}
      <div className="p-5 rounded-2xl bg-gradient-to-r from-zinc-900 via-zinc-950 to-zinc-900 text-white border border-zinc-800 flex flex-col sm:flex-row items-start sm:items-center justify-between gap-4 shadow-xl">
        <div className="flex items-center gap-3.5">
          <div className="w-11 h-11 rounded-2xl bg-amber-500/20 text-amber-400 border border-amber-500/30 flex items-center justify-center shadow-inner shrink-0">
            <KeyRound className="w-5 h-5" />
          </div>
          <div>
            <div className="flex items-center gap-2">
              <h3 className="font-extrabold text-sm sm:text-base tracking-tight">Klink REST API & Geliştirici Portalı</h3>
              <Badge variant="secondary" className="bg-amber-400/10 text-amber-300 border-amber-400/20 text-[10px]">
                v1.0 Canlı
              </Badge>
            </div>
            <p className="text-xs text-zinc-400 mt-0.5 max-w-xl">
              Uygulamalarınızı, botlarınızı ve backend servislerinizi Klink link kısaltma ve analitik motoruna doğrudan entegre edin.
            </p>
          </div>
        </div>

        <div className="flex items-center gap-2 w-full sm:w-auto">
          <Button
            variant="outline"
            asChild
            className="text-xs h-9 border-zinc-700 bg-zinc-800/80 hover:bg-zinc-700 text-white gap-1.5"
          >
            <a href="/swagger-ui/index.html" target="_blank" rel="noreferrer">
              <Code2 className="w-3.5 h-3.5 text-amber-400" />
              <span>Swagger Dokümantasyonu</span>
              <ExternalLink className="w-3 h-3 text-zinc-400" />
            </a>
          </Button>
        </div>
      </div>

      {successMsg && (
        <div className="p-3.5 rounded-xl bg-emerald-50 border border-emerald-200 text-emerald-800 text-xs font-semibold flex items-center gap-2 animate-fadeIn">
          <CheckCircle2 className="w-4 h-4 text-emerald-600 shrink-0" />
          <span>{successMsg}</span>
        </div>
      )}

      {errorMsg && (
        <div className="p-3.5 rounded-xl bg-red-50 border border-red-200 text-red-800 text-xs font-semibold flex items-center gap-2 animate-fadeIn">
          <AlertCircle className="w-4 h-4 text-red-600 shrink-0" />
          <span>{errorMsg}</span>
        </div>
      )}

      {/* Case 1: Active Approved API Key */}
      {activeKey && (
        <div className="space-y-6 animate-fadeIn">
          {/* Active Key Showcase Card */}
          <Card className="p-5 sm:p-6 border-zinc-200/90 bg-white space-y-4 shadow-sm">
            <div className="flex flex-col sm:flex-row items-start sm:items-center justify-between gap-3 border-b border-zinc-100 pb-3.5">
              <div className="flex items-center gap-2.5">
                <div className="w-3 h-3 rounded-full bg-emerald-500 animate-pulse" />
                <h4 className="font-extrabold text-sm text-zinc-950">
                  {activeKey.appName} &mdash; Aktif API Anahtarı
                </h4>
                <Badge variant="success" className="text-[10px]">
                  ONAYLANDI (ACTIVE)
                </Badge>
              </div>

              <div className="flex items-center gap-2">
                <Button
                  variant="outline"
                  size="sm"
                  onClick={() => handleRegenerate(activeKey.id)}
                  className="text-xs h-8 gap-1.5 text-zinc-600 hover:text-zinc-950"
                >
                  <RotateCcw className="w-3 h-3" />
                  <span>Anahtarı Yenile</span>
                </Button>
              </div>
            </div>

            {/* API Key Box */}
            <div className="space-y-1.5">
              <label className="text-[11px] font-bold text-zinc-600 uppercase tracking-wider block">
                API Secret Key (X-API-KEY Header)
              </label>
              <div className="flex items-center gap-2">
                <div className="flex-1 p-3 rounded-xl bg-zinc-950 text-white font-mono text-xs flex items-center justify-between border border-zinc-800 overflow-hidden">
                  <span className="truncate tracking-wide">
                    {revealedKeys[activeKey.id] ? (activeKey.rawKey || activeKey.keyPrefix) : (activeKey.keyPrefix || 'kl_live_••••••••••••••••••••••••••••')}
                  </span>
                  <div className="flex items-center gap-1.5 ml-2">
                    <button
                      type="button"
                      onClick={() => toggleReveal(activeKey.id)}
                      className="p-1 rounded-lg hover:bg-zinc-800 text-zinc-400 hover:text-white transition-colors cursor-pointer"
                      title={revealedKeys[activeKey.id] ? 'Gizle' : 'Göster'}
                    >
                      {revealedKeys[activeKey.id] ? <EyeOff className="w-3.5 h-3.5" /> : <Eye className="w-3.5 h-3.5" />}
                    </button>
                  </div>
                </div>

                <Button
                  onClick={() => handleCopyKey(activeKey.id, activeKey.rawKey || activeKey.keyPrefix || '')}
                  className="h-11 px-4 bg-zinc-950 hover:bg-zinc-800 text-white rounded-xl text-xs font-bold gap-1.5 shrink-0"
                >
                  {copiedKeyId === activeKey.id ? (
                    <>
                      <Check className="w-3.5 h-3.5 text-emerald-400" />
                      <span>Kopyalandı</span>
                    </>
                  ) : (
                    <>
                      <Copy className="w-3.5 h-3.5" />
                      <span>Kopyala</span>
                    </>
                  )}
                </Button>
              </div>
              <p className="text-[11px] text-zinc-500">
                🔒 Bu anahtarı istemci tarafında (tarayıcı JavaScript kodu) asla paylaşmayınız. Yalnızca backend sunucunuzda saklayınız.
              </p>
            </div>

            {/* Key Metadata Stats */}
            <div className="grid grid-cols-2 sm:grid-cols-4 gap-3 pt-2">
              <div className="p-3 rounded-xl bg-zinc-50 border border-zinc-200/70">
                <span className="text-[11px] text-zinc-500 block">Hız Limiti (Rate Limit)</span>
                <span className="text-sm font-extrabold text-zinc-950 mt-0.5 block">
                  {activeKey.rateLimitPerMinute} istek / dk
                </span>
              </div>

              <div className="p-3 rounded-xl bg-zinc-50 border border-zinc-200/70">
                <span className="text-[11px] text-zinc-500 block">Toplam İstek Sayısı</span>
                <span className="text-sm font-extrabold text-zinc-950 mt-0.5 block">
                  {activeKey.totalCalls.toLocaleString()}
                </span>
              </div>

              <div className="p-3 rounded-xl bg-zinc-50 border border-zinc-200/70">
                <span className="text-[11px] text-zinc-500 block">Onaylanma Tarihi</span>
                <span className="text-xs font-semibold text-zinc-800 mt-0.5 block">
                  {activeKey.approvedAt ? new Date(activeKey.approvedAt).toLocaleDateString('tr-TR') : 'Bugün'}
                </span>
              </div>

              <div className="p-3 rounded-xl bg-zinc-50 border border-zinc-200/70">
                <span className="text-[11px] text-zinc-500 block">Son Kullanım</span>
                <span className="text-xs font-semibold text-zinc-800 mt-0.5 block">
                  {activeKey.lastUsedAt ? new Date(activeKey.lastUsedAt).toLocaleTimeString('tr-TR') : 'Henüz kullanılmadı'}
                </span>
              </div>
            </div>
          </Card>

          {/* Interactive Code Snippets / Quickstart */}
          <Card className="p-5 sm:p-6 border-zinc-200/90 bg-white space-y-4 shadow-sm">
            <div className="flex flex-col sm:flex-row items-start sm:items-center justify-between gap-3 border-b border-zinc-100 pb-3">
              <div>
                <h4 className="font-extrabold text-sm text-zinc-950 flex items-center gap-2">
                  <Terminal className="w-4 h-4 text-amber-500" />
                  <span>Entegrasyon ve Hızlı Kod Örnekleri</span>
                </h4>
                <p className="text-xs text-zinc-500 mt-0.5">
                  Link kısaltma endpoint'ine API anahtarınız ile istek atın.
                </p>
              </div>

              {/* Language Switcher */}
              <div className="flex items-center p-1 rounded-xl bg-zinc-100 border border-zinc-200/80 gap-1">
                <button
                  type="button"
                  onClick={() => setSelectedSnippet('curl')}
                  className={`px-2.5 py-1 rounded-lg text-xs font-bold transition-all cursor-pointer ${
                    selectedSnippet === 'curl' ? 'bg-white text-zinc-950 shadow-2xs' : 'text-zinc-600 hover:text-zinc-950'
                  }`}
                >
                  cURL
                </button>
                <button
                  type="button"
                  onClick={() => setSelectedSnippet('javascript')}
                  className={`px-2.5 py-1 rounded-lg text-xs font-bold transition-all cursor-pointer ${
                    selectedSnippet === 'javascript' ? 'bg-white text-zinc-950 shadow-2xs' : 'text-zinc-600 hover:text-zinc-950'
                  }`}
                >
                  Node.js / JS
                </button>
                <button
                  type="button"
                  onClick={() => setSelectedSnippet('python')}
                  className={`px-2.5 py-1 rounded-lg text-xs font-bold transition-all cursor-pointer ${
                    selectedSnippet === 'python' ? 'bg-white text-zinc-950 shadow-2xs' : 'text-zinc-600 hover:text-zinc-950'
                  }`}
                >
                  Python
                </button>
              </div>
            </div>

            {/* Code Block Container */}
            <div className="relative rounded-2xl bg-zinc-950 text-zinc-200 p-4 font-mono text-xs overflow-x-auto border border-zinc-800 shadow-inner">
              <pre>{getSnippet()}</pre>
              <button
                onClick={() => handleCopyKey('snippet', getSnippet())}
                className="absolute top-3 right-3 p-2 rounded-lg bg-zinc-800/80 hover:bg-zinc-700 text-zinc-300 hover:text-white transition-colors cursor-pointer"
                title="Kodu Kopyala"
              >
                {copiedKeyId === 'snippet' ? <Check className="w-3.5 h-3.5 text-emerald-400" /> : <Copy className="w-3.5 h-3.5" />}
              </button>
            </div>
          </Card>
        </div>
      )}

      {/* Case 2: Pending Application Review in Progress */}
      {pendingKey && !activeKey && (
        <Card className="p-6 border-amber-200 bg-amber-50/40 space-y-4 animate-fadeIn">
          <div className="flex items-start gap-4">
            <div className="w-10 h-10 rounded-2xl bg-amber-100 text-amber-700 border border-amber-300 flex items-center justify-center shrink-0">
              <Clock className="w-5 h-5 animate-spin" />
            </div>
            <div className="space-y-1">
              <div className="flex items-center gap-2">
                <h4 className="font-extrabold text-sm text-amber-950">
                  API Anahtarı Başvurunuz İnceleniyor
                </h4>
                <Badge variant="warning" className="text-[10px]">
                  ONAY BEKLİYOR (PENDING)
                </Badge>
              </div>
              <p className="text-xs text-amber-900 leading-relaxed">
                <strong>{pendingKey.appName}</strong> adlı projeniz için yaptığınız API talebi admin ekibimize iletildi. Güvenlik ve kota kontrolünün ardından onaylanarak anahtarınız otomatik üretilecektir.
              </p>
            </div>
          </div>

          <div className="p-4 rounded-xl bg-white border border-amber-200/80 grid grid-cols-1 sm:grid-cols-3 gap-3 text-xs">
            <div>
              <span className="text-[11px] text-zinc-400 block">Kullanım Amacı</span>
              <span className="font-semibold text-zinc-800">{pendingKey.purpose}</span>
            </div>
            <div>
              <span className="text-[11px] text-zinc-400 block">Tahmini Hacim</span>
              <span className="font-semibold text-zinc-800">{pendingKey.expectedMonthlyClicks}</span>
            </div>
            <div>
              <span className="text-[11px] text-zinc-400 block">Başvuru Tarihi</span>
              <span className="font-semibold text-zinc-800">
                {new Date(pendingKey.createdAt).toLocaleDateString('tr-TR')}
              </span>
            </div>
          </div>
        </Card>
      )}

      {/* Case 3: Rejected Application Notice */}
      {rejectedKey && !activeKey && !pendingKey && (
        <Card className="p-6 border-red-200 bg-red-50/40 space-y-4 animate-fadeIn">
          <div className="flex items-start gap-4">
            <div className="w-10 h-10 rounded-2xl bg-red-100 text-red-700 border border-red-300 flex items-center justify-center shrink-0">
              <AlertCircle className="w-5 h-5" />
            </div>
            <div className="space-y-1">
              <div className="flex items-center gap-2">
                <h4 className="font-extrabold text-sm text-red-950">
                  API Anahtarı Başvurunuz Reddedildi
                </h4>
                <Badge variant="destructive" className="text-[10px]">
                  REDDEDİLDİ
                </Badge>
              </div>
              <p className="text-xs text-red-900 leading-relaxed">
                <strong>Red Sebebi:</strong> {rejectedKey.rejectionReason || 'Eksik veya uygunsuz başvuru bilgisi.'}
              </p>
            </div>
          </div>
        </Card>
      )}

      {/* Case 4: Application Form (When no active key and no pending key) */}
      {!activeKey && !pendingKey && (
        <Card className="p-6 border-zinc-200/90 bg-white space-y-5 shadow-sm animate-fadeIn">
          <div className="border-b border-zinc-100 pb-3">
            <h4 className="font-extrabold text-base text-zinc-950 flex items-center gap-2">
              <Send className="w-4 h-4 text-purple-600" />
              <span>Geliştirici API Anahtarı Başvuru Formu</span>
            </h4>
            <p className="text-xs text-zinc-500 mt-0.5">
              API anahtarınızı oluşturmak için lütfen projenizle ilgili detayları doldurunuz. Başvurunuz admin onayından sonra derhal aktif hale gelecektir.
            </p>
          </div>

          <form onSubmit={handleApply} className="space-y-4">
            <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
              {/* App Name */}
              <div className="space-y-1.5">
                <label className="text-xs font-bold text-zinc-800">
                  Uygulama / Proje Adı <span className="text-red-500">*</span>
                </label>
                <Input
                  value={appName}
                  onChange={(e) => setAppName(e.target.value)}
                  placeholder="Örn: E-Ticaret Sipariş Botu, CRM Entegrasyonu"
                  className="text-xs"
                  required
                />
              </div>

              {/* Website URL */}
              <div className="space-y-1.5">
                <label className="text-xs font-bold text-zinc-800">
                  Web Sitesi veya GitHub Repo Linki (Opsiyonel)
                </label>
                <Input
                  type="url"
                  value={websiteUrl}
                  onChange={(e) => setWebsiteUrl(e.target.value)}
                  placeholder="https://mycompany.com veya https://github.com/..."
                  className="text-xs font-mono"
                />
              </div>
            </div>

            {/* Purpose */}
            <div className="space-y-1.5">
              <label className="text-xs font-bold text-zinc-800">
                Kullanım Amacı ve Entegrasyon Senaryosu <span className="text-red-500">*</span>
              </label>
              <textarea
                value={purpose}
                onChange={(e) => setPurpose(e.target.value)}
                rows={3}
                placeholder="Örn: Müşterilerimize SMS ile gönderilen sipariş takip linklerini kısaltmak ve tıklanma istatistiklerini takip etmek amacıyla kullanacağız."
                className="w-full text-xs p-3 rounded-xl border border-zinc-200 bg-white focus:outline-none focus:ring-2 focus:ring-purple-600 leading-relaxed"
                required
              />
            </div>

            <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
              {/* Expected Volume */}
              <div className="space-y-1.5">
                <label className="text-xs font-bold text-zinc-800">
                  Tahmini Aylık Link / İstek Sayısı
                </label>
                <select
                  value={expectedMonthlyClicks}
                  onChange={(e) => setExpectedMonthlyClicks(e.target.value)}
                  className="w-full text-xs h-9 px-3 rounded-xl border border-zinc-200 bg-white focus:outline-none focus:ring-2 focus:ring-purple-600 font-medium"
                >
                  <option value="1.000 - 10.000">1.000 - 10.000 İstek / Ay (Başlangıç)</option>
                  <option value="10.000 - 50.000">10.000 - 50.000 İstek / Ay (Büyüme)</option>
                  <option value="50.000 - 250.000">50.000 - 250.000 İstek / Ay (Gelişmiş)</option>
                  <option value="250.000+">250.000+ İstek / Ay (Kurumsal Enterprise)</option>
                </select>
              </div>

              {/* IP Whitelist */}
              <div className="space-y-1.5">
                <label className="text-xs font-bold text-zinc-800 flex items-center justify-between">
                  <span>IP Beyaz Listesi (Opsiyonel)</span>
                  <span className="text-[10px] text-zinc-400 font-normal">Virgülle ayırın</span>
                </label>
                <Input
                  value={ipWhitelist}
                  onChange={(e) => setIpWhitelist(e.target.value)}
                  placeholder="195.175.10.20, 88.240.15.5"
                  className="text-xs font-mono"
                />
              </div>
            </div>

            {/* Submit Button */}
            <div className="pt-2 flex items-center justify-between">
              <p className="text-[11px] text-zinc-400">
                🔒 Başvurunuz admin ekibimiz tarafından güvenlik politikalarına göre incelenecektir.
              </p>
              <Button
                type="submit"
                disabled={submitting || !appName.trim() || !purpose.trim()}
                className="bg-purple-600 hover:bg-purple-700 text-white font-bold text-xs h-10 px-5 rounded-xl gap-1.5"
              >
                {submitting ? (
                  <span>Gönderiliyor...</span>
                ) : (
                  <>
                    <Send className="w-3.5 h-3.5" />
                    <span>Başvuruyu Gönder</span>
                  </>
                )}
              </Button>
            </div>
          </form>
        </Card>
      )}
    </div>
  );
};
