'use client';

import React, { useState, useEffect } from 'react';
import { 
  LayoutDashboard, 
  Link2, 
  MousePointerClick, 
  Zap, 
  Activity, 
  ArrowLeft, 
  Layers, 
  ShieldCheck, 
  Shield, 
  Lock,
  PlusCircle,
  ExternalLink,
  Sparkles,
  KeyRound
} from 'lucide-react';
import Link from 'next/link';
import { Navbar } from '@/components/Navbar';
import { QuickShortenWidget } from '@/components/QuickShortenWidget';
import { MyLinksTable } from '@/components/MyLinksTable';
import { BulkShortenerWidget } from '@/components/BulkShortenerWidget';
import { BioPageEditor } from '@/components/BioPageEditor';
import { DeveloperApiWidget } from '@/components/DeveloperApiWidget';
import { QrCodeModal } from '@/components/QrCodeModal';
import { AnalyticsModal } from '@/components/AnalyticsModal';
import { PasswordVerifyModal } from '@/components/PasswordVerifyModal';
import { TwoFactorModal } from '@/components/TwoFactorModal';
import { Language, translations } from '@/lib/translations';
import { ShortenResponse, BulkShortenResponse } from '@/lib/types';
import { ApiClient } from '@/lib/api';
import { Card, CardContent } from '@/components/ui/card';
import { Tabs, TabsList, TabsTrigger, TabsContent } from '@/components/ui/tabs';
import { Button } from '@/components/ui/button';
import { Badge } from '@/components/ui/badge';

export default function UserDashboardPage() {
  const [lang, setLang] = useState<Language>('tr');
  const [activeTab, setActiveTab] = useState<'overview' | 'vault' | 'bulk' | 'bio' | 'api'>('overview');
  const [authUser, setAuthUser] = useState<{ u: string; p: string; token?: string; role?: string } | null>(null);
  const [authChecked, setAuthChecked] = useState(false);

  const [links, setLinks] = useState<ShortenResponse[]>([]);
  const [loadingLinks, setLoadingLinks] = useState(false);
  const [is2FAEnabled, setIs2FAEnabled] = useState(false);

  // Modal States
  const [qrCodeModal, setQrCodeModal] = useState<string | null>(null);
  const [analyticsModal, setAnalyticsModal] = useState<string | null>(null);
  const [passwordModal, setPasswordModal] = useState<string | null>(null);
  const [twoFactorModalOpen, setTwoFactorModalOpen] = useState(false);

  const t = translations[lang];

  useEffect(() => {
    // Check localStorage user session
    const saved = localStorage.getItem('klink_user') || localStorage.getItem('swiftlink_user');
    if (saved) {
      try {
        const parsed = JSON.parse(saved);
        if (parsed && parsed.u) {
          setAuthUser({ u: parsed.u, p: parsed.p || 'password', token: parsed.token, role: parsed.role });
          setAuthChecked(true);
          return;
        }
      } catch (e) {
        console.error(e);
      }
    }

    // Production ortamında default veri ile girişe izin verilmez -> Login'e yönlendir
    if (process.env.NODE_ENV === 'production') {
      window.location.href = '/login?redirect=/dashboard';
    } else {
      // Yalnızca yerel geliştirme (development) modunda test kullanıcısı fallback'i
      setAuthUser({ u: 'user', p: 'password', role: 'ROLE_USER' });
      setAuthChecked(true);
    }
  }, []);

  const fetchUserData = async () => {
    if (!authUser) return;
    try {
      const user = await ApiClient.getCurrentUser(lang, authUser as any);
      setIs2FAEnabled(!!user.twoFactorEnabled);
    } catch (e) {
      console.error(e);
    }
  };

  const fetchMyLinks = async () => {
    if (!authUser) return;
    setLoadingLinks(true);
    try {
      const data = await ApiClient.getMyUrls(lang, authUser as any);
      setLinks(data);
    } catch (e) {
      console.error(e);
    } finally {
      setLoadingLinks(false);
    }
  };

  useEffect(() => {
    fetchUserData();
    fetchMyLinks();
  }, [lang, authUser]);

  const handleShortenSuccess = (newLink: ShortenResponse) => {
    setLinks((prev) => [newLink, ...prev]);
  };

  const handleBulkSuccess = (batch: BulkShortenResponse) => {
    setLinks((prev) => [...batch.shortenedUrls, ...prev]);
  };

  const handleToggleStatus = async (shortCode: string, currentActive: boolean) => {
    try {
      await ApiClient.toggleStatus(shortCode, !currentActive, lang, authUser as any);
      setLinks((prev) =>
        prev.map((item) => (item.shortCode === shortCode ? { ...item, active: !currentActive } : item))
      );
    } catch (e) {
      console.error(e);
    }
  };

  const handleDeleteLink = (shortCode: string) => {
    setLinks((prev) => prev.filter((item) => item.shortCode !== shortCode));
  };

  const totalClicks = links.reduce((acc, curr) => acc + (curr.clickCount || 0), 0);
  const activeCount = links.length;
  const protectedCount = links.filter((l) => l.passwordProtected).length;

  if (!authChecked || !authUser) {
    return (
      <div className="min-h-screen bg-[#fafafa] text-zinc-950 flex flex-col items-center justify-center space-y-3">
        <div className="w-9 h-9 rounded-2xl bg-zinc-950 text-white flex items-center justify-center animate-pulse">
          <Link2 className="w-5 h-5" />
        </div>
        <p className="text-xs font-semibold text-zinc-500">Oturum kontrol ediliyor...</p>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-[#fafafa] text-zinc-950 selection:bg-zinc-900 selection:text-white flex flex-col pb-20">
      {/* Sleek Navbar */}
      <Navbar
        lang={lang}
        onLanguageChange={(newLang) => setLang(newLang)}
        username={authUser.u}
        is2FAEnabled={is2FAEnabled}
        onOpen2FA={() => setTwoFactorModalOpen(true)}
      />

      <main className="max-w-6xl mx-auto px-4 sm:px-6 pt-6 space-y-6 w-full flex-1">
        {/* Top Breadcrumb & Action Toolbar */}
        <div className="flex flex-col sm:flex-row items-start sm:items-center justify-between gap-3 text-xs">
          <div className="flex items-center gap-2 text-zinc-500">
            <Link
              href="/"
              className="flex items-center gap-1.5 font-medium hover:text-zinc-950 transition-colors"
            >
              <ArrowLeft className="w-3.5 h-3.5" />
              <span>{lang === 'tr' ? 'Ana Sayfa' : 'Home'}</span>
            </Link>
            <span>/</span>
            <span className="font-bold text-zinc-950">{lang === 'tr' ? 'Link Yönetim Paneli' : 'Link Dashboard'}</span>
          </div>

          <div className="flex items-center gap-2 self-end sm:self-auto">
            <Button
              variant={is2FAEnabled ? "secondary" : "outline"}
              size="sm"
              onClick={() => setTwoFactorModalOpen(true)}
              className="text-xs h-8"
            >
              {is2FAEnabled ? (
                <ShieldCheck className="w-3.5 h-3.5 text-emerald-600 mr-1" />
              ) : (
                <Shield className="w-3.5 h-3.5 text-zinc-400 mr-1" />
              )}
              <span>{is2FAEnabled ? (lang === 'tr' ? '2FA Korumalı' : '2FA Active') : (lang === 'tr' ? '2FA Kur' : 'Enable 2FA')}</span>
            </Button>

            <Button variant="outline" size="sm" asChild className="text-xs h-8">
              <Link href="/admin">
                <span>{lang === 'tr' ? 'Admin CRM' : 'Admin CRM'}</span>
              </Link>
            </Button>
          </div>
        </div>

        {/* High-End Metric Cards Grid */}
        <div className="grid grid-cols-2 lg:grid-cols-4 gap-3.5">
          {/* Card 1: Total Links */}
          <div className="p-4 sm:p-5 rounded-2xl bg-white border border-zinc-200/90 shadow-2xs space-y-3">
            <div className="flex items-center justify-between">
              <p className="text-xs font-semibold text-zinc-500">{t.cardTotalLinks}</p>
              <div className="w-8 h-8 rounded-xl bg-zinc-100 text-zinc-900 flex items-center justify-center">
                <Link2 className="w-4 h-4" />
              </div>
            </div>
            <div>
              <h4 className="text-2xl sm:text-3xl font-black font-mono text-zinc-950">{links.length}</h4>
              <p className="text-[11px] text-zinc-400 font-medium mt-0.5">
                {protectedCount} {lang === 'tr' ? 'şifre korumalı' : 'password protected'}
              </p>
            </div>
          </div>

          {/* Card 2: Total Clicks */}
          <div className="p-4 sm:p-5 rounded-2xl bg-white border border-zinc-200/90 shadow-2xs space-y-3">
            <div className="flex items-center justify-between">
              <p className="text-xs font-semibold text-zinc-500">{t.cardTotalClicks}</p>
              <div className="w-8 h-8 rounded-xl bg-emerald-50 text-emerald-700 border border-emerald-100 flex items-center justify-center">
                <MousePointerClick className="w-4 h-4" />
              </div>
            </div>
            <div>
              <h4 className="text-2xl sm:text-3xl font-black font-mono text-zinc-950">{totalClicks}</h4>
              <div className="flex items-center gap-1 text-[11px] text-emerald-600 font-medium mt-0.5">
                <span className="w-1.5 h-1.5 rounded-full bg-emerald-500 animate-pulse" />
                <span>{lang === 'tr' ? 'Canlı Telemetri' : 'Real-time'}</span>
              </div>
            </div>
          </div>

          {/* Card 3: Active Links */}
          <div className="p-4 sm:p-5 rounded-2xl bg-white border border-zinc-200/90 shadow-2xs space-y-3">
            <div className="flex items-center justify-between">
              <p className="text-xs font-semibold text-zinc-500">{t.cardActiveLinks}</p>
              <div className="w-8 h-8 rounded-xl bg-zinc-100 text-zinc-900 flex items-center justify-center">
                <Zap className="w-4 h-4" />
              </div>
            </div>
            <div>
              <h4 className="text-2xl sm:text-3xl font-black font-mono text-zinc-950">{activeCount}</h4>
              <p className="text-[11px] text-zinc-400 font-medium mt-0.5">
                {lang === 'tr' ? '%100 Erişilebilir' : '100% Online'}
              </p>
            </div>
          </div>

          {/* Card 4: Avg CTR */}
          <div className="p-4 sm:p-5 rounded-2xl bg-white border border-zinc-200/90 shadow-2xs space-y-3">
            <div className="flex items-center justify-between">
              <p className="text-xs font-semibold text-zinc-500">{t.cardAvgCtr}</p>
              <div className="w-8 h-8 rounded-xl bg-blue-50 text-blue-700 border border-blue-100 flex items-center justify-center">
                <Activity className="w-4 h-4" />
              </div>
            </div>
            <div>
              <h4 className="text-2xl sm:text-3xl font-black font-mono text-zinc-950">
                {links.length > 0 ? (totalClicks / links.length).toFixed(1) : '0.0'}
              </h4>
              <p className="text-[11px] text-zinc-400 font-medium mt-0.5">
                {lang === 'tr' ? 'Tıklama / Link Ort.' : 'Avg per Link'}
              </p>
            </div>
          </div>
        </div>

        {/* Segmented Navigation Tabs */}
        <Tabs
          value={activeTab}
          onValueChange={(val) => setActiveTab(val as 'overview' | 'vault' | 'bulk' | 'bio' | 'api')}
          className="space-y-6"
        >
          <TabsList className="grid w-full max-w-2xl grid-cols-5 bg-zinc-100 p-1 rounded-xl">
            <TabsTrigger value="overview" className="flex items-center gap-1.5 text-xs font-semibold">
              <LayoutDashboard className="w-3.5 h-3.5" />
              <span>{t.tabOverview}</span>
            </TabsTrigger>
            <TabsTrigger value="vault" className="flex items-center gap-1.5 text-xs font-semibold">
              <Link2 className="w-3.5 h-3.5" />
              <span>{t.tabMyLinks}</span>
            </TabsTrigger>
            <TabsTrigger value="bulk" className="flex items-center gap-1.5 text-xs font-semibold">
              <Layers className="w-3.5 h-3.5" />
              <span>{t.tabBulk}</span>
            </TabsTrigger>
            <TabsTrigger value="bio" className="flex items-center gap-1.5 text-xs font-semibold data-[state=active]:bg-purple-600 data-[state=active]:text-white">
              <Sparkles className="w-3.5 h-3.5 text-amber-300" />
              <span>{lang === 'tr' ? '✨ Bio Sayfam' : '✨ Bio Page'}</span>
            </TabsTrigger>
            <TabsTrigger value="api" className="flex items-center gap-1.5 text-xs font-semibold data-[state=active]:bg-zinc-950 data-[state=active]:text-white">
              <KeyRound className="w-3.5 h-3.5 text-amber-400" />
              <span>{lang === 'tr' ? '🔑 API Keys' : '🔑 API Keys'}</span>
            </TabsTrigger>
          </TabsList>

          {/* Tab 1: Overview */}
          <TabsContent value="overview" className="space-y-6">
            <QuickShortenWidget
              lang={lang}
              authUser={authUser}
              onSuccess={handleShortenSuccess}
              onOpenQr={(shortCode) => setQrCodeModal(shortCode)}
            />
            <MyLinksTable
              lang={lang}
              links={links}
              onToggleStatus={handleToggleStatus}
              onOpenQr={(shortCode) => setQrCodeModal(shortCode)}
              onOpenPasswordModal={(shortCode) => setPasswordModal(shortCode)}
              onOpenAnalyticsModal={(shortCode) => setAnalyticsModal(shortCode)}
              onDeleteLink={handleDeleteLink}
            />
          </TabsContent>

          {/* Tab 2: Vault */}
          <TabsContent value="vault">
            <MyLinksTable
              lang={lang}
              links={links}
              onToggleStatus={handleToggleStatus}
              onOpenQr={(shortCode) => setQrCodeModal(shortCode)}
              onOpenPasswordModal={(shortCode) => setPasswordModal(shortCode)}
              onOpenAnalyticsModal={(shortCode) => setAnalyticsModal(shortCode)}
              onDeleteLink={handleDeleteLink}
            />
          </TabsContent>

          {/* Tab 3: Bulk Shortener */}
          <TabsContent value="bulk">
            <BulkShortenerWidget
              lang={lang}
              authUser={authUser}
              onSuccessBatch={handleBulkSuccess}
            />
          </TabsContent>

          {/* Tab 4: Bio Page Editor */}
          <TabsContent value="bio">
            <BioPageEditor
              lang={lang}
              authUser={authUser}
            />
          </TabsContent>

          {/* Tab 5: Developer API Key Management */}
          <TabsContent value="api">
            <DeveloperApiWidget
              lang={lang}
              authUser={authUser}
            />
          </TabsContent>
        </Tabs>
      </main>

      {/* Popups & Modals */}
      <QrCodeModal
        shortCode={qrCodeModal}
        lang={lang}
        onClose={() => setQrCodeModal(null)}
      />

      <AnalyticsModal
        shortCode={analyticsModal}
        lang={lang}
        authUser={authUser}
        onClose={() => setAnalyticsModal(null)}
      />

      <PasswordVerifyModal
        shortCode={passwordModal}
        lang={lang}
        onClose={() => setPasswordModal(null)}
      />

      <TwoFactorModal
        isOpen={twoFactorModalOpen}
        onClose={() => setTwoFactorModalOpen(false)}
        lang={lang}
        authUser={authUser}
        isEnabled={is2FAEnabled}
        onStatusChange={(status) => setIs2FAEnabled(status)}
      />
    </div>
  );
}

