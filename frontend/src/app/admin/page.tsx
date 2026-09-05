'use client';

import React, { useState, useEffect } from 'react';
import { 
  Users, 
  Search, 
  Trash2, 
  ArrowLeft, 
  RefreshCw, 
  Globe2, 
  LogOut, 
  Shield, 
  CheckCircle2, 
  AlertCircle,
  Database,
  Layers,
  Activity,
  HardDrive,
  Cpu,
  Radio,
  Zap,
  RotateCcw,
  Server,
  ArrowRight,
  Sparkles,
  Link2,
  Lock,
  KeyRound,
  Check,
  X,
  ExternalLink,
  Code2
} from 'lucide-react';
import Link from 'next/link';
import { Language } from '@/lib/translations';
import { ShortenResponse, UserDto, SystemStatusResponse, ApiKeyResponse, ApiKeyStatus } from '@/lib/types';
import { ApiClient } from '@/lib/api';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Badge } from '@/components/ui/badge';
import { Card, CardHeader, CardTitle, CardDescription, CardContent } from '@/components/ui/card';
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from '@/components/ui/table';
import { Tabs, TabsList, TabsTrigger, TabsContent } from '@/components/ui/tabs';

export default function AdminCrmPage() {
  const [lang] = useState<Language>('tr');
  const [adminAuth, setAdminAuth] = useState<{ u: string; p: string; token?: string; role?: string } | null>(null);
  const [authChecked, setAuthChecked] = useState(false);
  const [activeSubTab, setActiveSubTab] = useState<'system' | 'users' | 'links' | 'api-keys'>('system');

  const [allLinks, setAllLinks] = useState<ShortenResponse[]>([]);
  const [apiKeys, setApiKeys] = useState<ApiKeyResponse[]>([]);
  const [searchLink, setSearchLink] = useState('');
  const [searchUser, setSearchUser] = useState('');
  const [searchApiKey, setSearchApiKey] = useState('');
  const [apiKeyStatusFilter, setApiKeyStatusFilter] = useState<'ALL' | 'PENDING' | 'APPROVED' | 'REJECTED' | 'REVOKED'>('ALL');
  const [actionModal, setActionModal] = useState<{ type: 'approve' | 'reject'; key: ApiKeyResponse } | null>(null);
  const [customRateLimit, setCustomRateLimit] = useState(120);
  const [rejectionReasonText, setRejectionReasonText] = useState('');
  const [loading, setLoading] = useState(false);
  const [errorMsg, setErrorMsg] = useState('');
  const [actionSuccessMsg, setActionSuccessMsg] = useState('');

  const [systemStatus, setSystemStatus] = useState<SystemStatusResponse | null>(null);
  const [usersList, setUsersList] = useState<UserDto[]>([]);

  useEffect(() => {
    const saved = localStorage.getItem('klink_user') || localStorage.getItem('swiftlink_user');
    if (saved) {
      try {
        const parsed = JSON.parse(saved);
        if (parsed && parsed.u && parsed.role === 'ROLE_ADMIN') {
          setAdminAuth({ u: parsed.u, p: parsed.p || '', token: parsed.token, role: parsed.role });
          setAuthChecked(true);
          return;
        }
      } catch (e) {
        console.error(e);
      }
    }

    // Yetkisiz veya giris yapmamis kullanicilari kesinlikle Admin Login'e yonlendir
    window.location.href = '/admin/login';
  }, []);

  const loadAdminData = async () => {
    if (!adminAuth) return;
    setLoading(true);
    setErrorMsg('');
    try {
      const [links, telemetry, keys, users] = await Promise.all([
        ApiClient.getAllUrls(lang, adminAuth as any),
        ApiClient.getSystemStatus(lang, adminAuth as any),
        ApiClient.getAdminApiKeys(undefined, lang, adminAuth as any),
        ApiClient.getAdminUsers(lang, adminAuth as any)
      ]);
      setAllLinks(links);
      if (telemetry) {
        setSystemStatus(telemetry);
      }
      if (keys) {
        setApiKeys(keys);
      }
      if (users && users.length > 0) {
        setUsersList(users);
      }
    } catch (e: any) {
      setErrorMsg(e.message || 'API bağlantı hatası!');
    } finally {
      setLoading(false);
    }
  };

  const handleApproveKey = async (keyId: string) => {
    try {
      const res = await ApiClient.approveAdminApiKey(
        keyId,
        { rateLimitPerMinute: customRateLimit },
        lang,
        adminAuth as any
      );
      setApiKeys(prev => prev.map(k => k.id === keyId ? res : k));
      setActionModal(null);
      setActionSuccessMsg('API başvurusu başarıyla onaylandı ve anahtar üretildi.');
      setTimeout(() => setActionSuccessMsg(''), 3500);
    } catch (e: any) {
      setErrorMsg(e.message || 'Onaylama işlemi başarısız oldu.');
    }
  };

  const handleRejectKey = async (keyId: string) => {
    try {
      const res = await ApiClient.rejectAdminApiKey(
        keyId,
        { rejectionReason: rejectionReasonText.trim() || 'Başvuru şartları karşılanamadı.' },
        lang,
        adminAuth as any
      );
      setApiKeys(prev => prev.map(k => k.id === keyId ? res : k));
      setActionModal(null);
      setRejectionReasonText('');
      setActionSuccessMsg('API başvurusu reddedildi.');
      setTimeout(() => setActionSuccessMsg(''), 3500);
    } catch (e: any) {
      setErrorMsg(e.message || 'Reddetme işlemi başarısız oldu.');
    }
  };

  const handleRevokeKey = async (keyId: string) => {
    if (!confirm('Bu API anahtarını iptal etmek (askıya almak) istediğinize emin misiniz?')) {
      return;
    }
    try {
      const res = await ApiClient.revokeAdminApiKey(keyId, lang, adminAuth as any);
      setApiKeys(prev => prev.map(k => k.id === keyId ? res : k));
      setActionSuccessMsg('API anahtarı askıya alındı / iptal edildi.');
      setTimeout(() => setActionSuccessMsg(''), 3500);
    } catch (e: any) {
      setErrorMsg(e.message || 'İptal işlemi başarısız oldu.');
    }
  };

  useEffect(() => {
    loadAdminData();
    const interval = setInterval(() => {
      ApiClient.getSystemStatus(lang, adminAuth as any)
        .then((data) => {
          if (data) setSystemStatus(data);
        })
        .catch(() => {});
    }, 15000);
    return () => clearInterval(interval);
  }, [lang, adminAuth]);

  const handleAdminLogout = async () => {
    try {
      if (adminAuth) {
        await ApiClient.logoutUser(lang, adminAuth as any);
      }
    } catch (e) {
      console.error(e);
    } finally {
      localStorage.removeItem('klink_user');
      localStorage.removeItem('swiftlink_user');
      window.location.href = '/admin/login';
    }
  };

  const handleToggleAdminRole = async (userId: string) => {
    const user = usersList.find((u) => u.id === userId);
    if (!user) return;
    const targetRole = user.role === 'ROLE_ADMIN' ? 'ROLE_USER' : 'ROLE_ADMIN';
    try {
      const updated = await ApiClient.updateAdminUserRole(userId, targetRole, lang, adminAuth as any);
      setUsersList((prev) =>
        prev.map((u) => (u.id === userId ? { ...u, role: updated.role } : u))
      );
      setActionSuccessMsg(`@${user.username} kullanıcısının rolü başarıyla veritabanında güncellendi.`);
      setTimeout(() => setActionSuccessMsg(''), 4000);
    } catch (err: any) {
      setErrorMsg(err.message || 'Rol güncellenirken hata oluştu.');
      setTimeout(() => setErrorMsg(''), 5000);
    }
  };

  const handleDeleteUser = async (userId: string) => {
    const user = usersList.find((u) => u.id === userId);
    if (!user) return;
    if (!confirm(`@${user.username} kullanıcısını sistemden kalıcı olarak silmek istediğinize emin misiniz?`)) return;
    try {
      await ApiClient.deleteAdminUser(userId, lang, adminAuth as any);
      setUsersList((prev) => prev.filter((u) => u.id !== userId));
      setActionSuccessMsg(`@${user.username} kullanıcısı sistemden kalıcı olarak silindi.`);
      setTimeout(() => setActionSuccessMsg(''), 4000);
    } catch (err: any) {
      setErrorMsg(err.message || 'Kullanıcı silinemedi.');
      setTimeout(() => setErrorMsg(''), 5000);
    }
  };

  const handleAdminDeleteLink = async (shortCode: string) => {
    try {
      await ApiClient.deleteUrl(shortCode, lang, adminAuth as any);
      setAllLinks((prev) => prev.filter((l) => l.shortCode !== shortCode));
    } catch (e: any) {
      alert(e.message || 'Silme işlemi esnasında hata oluştu.');
    }
  };

  const handleFlushCache = async () => {
    try {
      const result = await ApiClient.flushRedisCache(lang, adminAuth as any);
      setActionSuccessMsg(result.message || 'Redis önbelleği başarıyla temizlendi.');
      setTimeout(() => setActionSuccessMsg(''), 4000);
      loadAdminData();
    } catch (e: any) {
      setErrorMsg(e.message || 'Önbellek temizlenirken hata oluştu.');
    }
  };

  const filteredUsers = usersList.filter(
    (u) =>
      u.username.toLowerCase().includes(searchUser.toLowerCase()) ||
      u.email.toLowerCase().includes(searchUser.toLowerCase())
  );

  const filteredSystemLinks = allLinks.filter(
    (l) =>
      l.shortCode.toLowerCase().includes(searchLink.toLowerCase()) ||
      l.originalUrl.toLowerCase().includes(searchLink.toLowerCase())
  );

  const isRedisConnected = systemStatus?.redis?.status === 'CONNECTED';
  const isRabbitConnected = systemStatus?.rabbitMq?.status === 'CONNECTED';

  if (!authChecked || !adminAuth) {
    return (
      <div className="min-h-screen bg-[#fafafa] text-zinc-950 flex flex-col items-center justify-center space-y-3">
        <div className="w-9 h-9 rounded-2xl bg-zinc-950 text-white flex items-center justify-center animate-pulse">
          <Shield className="w-5 h-5" />
        </div>
        <p className="text-xs font-semibold text-zinc-500">Yönetici yetkisi doğrulanıyor...</p>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-[#fafafa] text-zinc-950 selection:bg-zinc-900 selection:text-white flex flex-col pb-20">
      {/* Admin CRM Header */}
      <header className="sticky top-0 z-40 bg-white/80 backdrop-blur-md border-b border-zinc-200/80 px-4 sm:px-8 py-3.5">
        <div className="max-w-7xl mx-auto flex items-center justify-between gap-4">
          <div className="flex items-center gap-3">
            <Button variant="outline" size="iconSm" asChild className="h-8 w-8 bg-white">
              <Link href="/dashboard" title="Kullanıcı Paneline Dön">
                <ArrowLeft className="w-3.5 h-3.5" />
              </Link>
            </Button>
            <div className="w-8 h-8 rounded-xl bg-zinc-950 text-white flex items-center justify-center shadow-xs">
              <Shield className="w-4 h-4" />
            </div>
            <div>
              <div className="flex items-center gap-2">
                <h1 className="text-sm sm:text-base font-extrabold text-zinc-950 tracking-tight">
                  Yönetim CRM & Telemetri
                </h1>
                <Badge variant="secondary" className="font-mono text-[10px]">Admin Root</Badge>
              </div>
              <p className="text-[11px] text-zinc-500 hidden sm:block">Altyapı (Redis & RabbitMQ), kullanıcılar ve tüm bağlantı izleme</p>
            </div>
          </div>

          <div className="flex items-center gap-2 sm:gap-2.5">
            <Button
              variant="outline"
              size="sm"
              onClick={loadAdminData}
              className="text-xs h-8 bg-white"
            >
              <RefreshCw className={`w-3 h-3 mr-1.5 ${loading ? 'animate-spin' : ''}`} />
              <span className="hidden sm:inline">Yenile</span>
            </Button>

            <div className="px-2.5 py-1 rounded-lg bg-zinc-100 border border-zinc-200 text-xs font-semibold text-zinc-800 hidden md:flex items-center gap-1.5">
              <span>@admin</span>
            </div>

            <Button
              variant="outline"
              size="sm"
              onClick={handleAdminLogout}
              className="text-xs h-8 text-zinc-600 hover:text-red-600 hover:bg-red-50 bg-white"
              title="Admin Oturumunu Kapat"
            >
              <LogOut className="w-3.5 h-3.5 mr-1" />
              <span className="hidden sm:inline">Çıkış</span>
            </Button>
          </div>
        </div>
      </header>

      {/* Main Content */}
      <main className="max-w-7xl mx-auto px-4 sm:px-6 pt-6 space-y-6 w-full flex-1">
        {errorMsg && (
          <div className="p-3.5 rounded-2xl bg-red-50 border border-red-200 text-red-700 text-xs font-semibold flex items-center gap-2 animate-fadeIn">
            <AlertCircle className="w-4 h-4 shrink-0" />
            <span>{errorMsg}</span>
          </div>
        )}

        {actionSuccessMsg && (
          <div className="p-3.5 rounded-2xl bg-emerald-50 border border-emerald-200 text-emerald-800 text-xs font-semibold flex items-center gap-2 animate-fadeIn">
            <CheckCircle2 className="w-4 h-4 shrink-0 text-emerald-600" />
            <span>{actionSuccessMsg}</span>
          </div>
        )}

        {/* Metric Overview Cards */}
        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-3.5">
          {/* Card 1: Redis Status */}
          <div className="p-4 sm:p-5 rounded-2xl bg-white border border-zinc-200/90 shadow-2xs space-y-2.5">
            <div className="flex items-center justify-between">
              <p className="text-xs font-semibold text-zinc-500">Redis Önbellek</p>
              <Badge variant={isRedisConnected ? "success" : "destructive"} className="text-[10px]">
                {isRedisConnected ? 'Aktif' : 'Kapalı'}
              </Badge>
            </div>
            <div>
              <h4 className="text-2xl font-black font-mono text-zinc-950">
                {isRedisConnected ? `${systemStatus?.redis.pingLatencyMs ?? '< 2'} ms` : 'Offline'}
              </h4>
              <p className="text-[11px] text-zinc-400 font-medium mt-0.5">
                {systemStatus?.redis.totalKeys ?? 0} önbellek anahtarı
              </p>
            </div>
          </div>

          {/* Card 2: RabbitMQ Status */}
          <div className="p-4 sm:p-5 rounded-2xl bg-white border border-zinc-200/90 shadow-2xs space-y-2.5">
            <div className="flex items-center justify-between">
              <p className="text-xs font-semibold text-zinc-500">RabbitMQ Kuyruğu</p>
              <Badge variant={isRabbitConnected ? "success" : "destructive"} className="text-[10px]">
                {isRabbitConnected ? 'Dinliyor' : 'Kesildi'}
              </Badge>
            </div>
            <div>
              <h4 className="text-2xl font-black font-mono text-zinc-950">
                {systemStatus?.rabbitMq.messageCount ?? 0}
                <span className="text-xs font-normal text-zinc-400 ml-1.5">bekleyen ileti</span>
              </h4>
              <p className="text-[11px] text-zinc-400 font-medium mt-0.5">
                {systemStatus?.rabbitMq.consumerCount ?? 1} aktif tüketici (listener)
              </p>
            </div>
          </div>

          {/* Card 3: Registered Users */}
          <div className="p-4 sm:p-5 rounded-2xl bg-white border border-zinc-200/90 shadow-2xs space-y-2.5">
            <div className="flex items-center justify-between">
              <p className="text-xs font-semibold text-zinc-500">Kayıtlı Kullanıcılar</p>
              <div className="w-7 h-7 rounded-lg bg-zinc-100 text-zinc-900 flex items-center justify-center">
                <Users className="w-3.5 h-3.5" />
              </div>
            </div>
            <div>
              <h4 className="text-2xl font-black font-mono text-zinc-950">{usersList.length}</h4>
              <p className="text-[11px] text-zinc-400 font-medium mt-0.5">
                {usersList.filter(u => u.role === 'ROLE_ADMIN').length} Admin, {usersList.filter(u => u.role !== 'ROLE_ADMIN').length} Standart Üye
              </p>
            </div>
          </div>

          {/* Card 4: Total Short Links */}
          <div className="p-4 sm:p-5 rounded-2xl bg-white border border-zinc-200/90 shadow-2xs space-y-2.5">
            <div className="flex items-center justify-between">
              <p className="text-xs font-semibold text-zinc-500">Toplam Kısa Link</p>
              <div className="w-7 h-7 rounded-lg bg-zinc-100 text-zinc-900 flex items-center justify-center">
                <Globe2 className="w-3.5 h-3.5" />
              </div>
            </div>
            <div>
              <h4 className="text-2xl font-black font-mono text-zinc-950">{allLinks.length}</h4>
              <p className="text-[11px] text-zinc-400 font-medium mt-0.5">Sistem geneli bağlantılar</p>
            </div>
          </div>
        </div>

        {/* CRM Sub-tabs */}
        <Tabs
          value={activeSubTab}
          onValueChange={(val) => setActiveSubTab(val as 'system' | 'users' | 'links' | 'api-keys')}
          className="space-y-6"
        >
          <TabsList className="grid w-full max-w-2xl grid-cols-4 bg-zinc-100 p-1 rounded-xl">
            <TabsTrigger value="system" className="flex items-center gap-1.5 text-xs font-semibold">
              <Activity className="w-3.5 h-3.5" />
              <span>Altyapı (Sistem)</span>
            </TabsTrigger>
            <TabsTrigger value="users" className="flex items-center gap-1.5 text-xs font-semibold">
              <Users className="w-3.5 h-3.5" />
              <span>Kullanıcılar ({usersList.length})</span>
            </TabsTrigger>
            <TabsTrigger value="links" className="flex items-center gap-1.5 text-xs font-semibold">
              <Globe2 className="w-3.5 h-3.5" />
              <span>Tüm Linkler ({allLinks.length})</span>
            </TabsTrigger>
            <TabsTrigger value="api-keys" className="flex items-center gap-1.5 text-xs font-semibold data-[state=active]:bg-amber-500 data-[state=active]:text-zinc-950">
              <KeyRound className="w-3.5 h-3.5" />
              <span>API Başvuruları</span>
              {apiKeys.filter(k => k.status === 'PENDING').length > 0 && (
                <span className="w-4 h-4 rounded-full bg-red-600 text-white text-[9px] font-bold flex items-center justify-center">
                  {apiKeys.filter(k => k.status === 'PENDING').length}
                </span>
              )}
            </TabsTrigger>
          </TabsList>

          {/* Sub-tab 1: Infrastructure */}
          <TabsContent value="system" className="space-y-6">
            {/* System Status Banner */}
            <div className="bg-zinc-950 text-white rounded-3xl p-6 sm:p-7 shadow-xl border border-zinc-800">
              <div className="flex flex-col sm:flex-row items-start sm:items-center justify-between gap-4">
                <div className="flex items-center gap-3.5">
                  <div className="w-11 h-11 rounded-2xl bg-zinc-900 text-white border border-zinc-800 flex items-center justify-center shrink-0">
                    <Server className="w-5 h-5" />
                  </div>
                  <div>
                    <div className="flex items-center gap-2">
                      <h3 className="text-base font-bold text-white">Sistem Altyapısı ve Canlı Telemetri</h3>
                      <Badge className={
                        systemStatus?.overallStatus === 'HEALTHY' 
                          ? 'bg-emerald-500/20 text-emerald-300 border-emerald-500/40 text-[10px]' 
                          : 'bg-amber-500/20 text-amber-300 border-amber-500/40 text-[10px]'
                      }>
                        {systemStatus?.overallStatus === 'HEALTHY' ? 'Tüm Servisler Stabil' : 'Kısmi Servis Kesintisi'}
                      </Badge>
                    </div>
                    <p className="text-xs text-zinc-400 mt-0.5">
                      Redis In-Memory Cache ve RabbitMQ Asenkron Tıklama Kuyruğu telemetri verileri
                    </p>
                  </div>
                </div>

                <div className="flex items-center gap-2 self-end sm:self-auto">
                  <span className="text-[11px] text-zinc-400 font-mono hidden md:inline">
                    Son Kontrol: {systemStatus?.timestamp ? new Date(systemStatus.timestamp).toLocaleTimeString('tr-TR') : '-'}
                  </span>
                  <Button
                    size="sm"
                    variant="outline"
                    onClick={loadAdminData}
                    className="bg-zinc-900 hover:bg-zinc-800 text-white border-zinc-700 text-xs h-8"
                  >
                    <RefreshCw className={`w-3 h-3 mr-1.5 ${loading ? 'animate-spin' : ''}`} />
                    Anlık Yenile
                  </Button>
                </div>
              </div>
            </div>

            {/* Detailed Redis & RabbitMQ Cards Grid */}
            <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
              {/* Card 1: Redis Details */}
              <Card className="shadow-sm border-zinc-200/90 overflow-hidden bg-white">
                <CardHeader className="border-b border-zinc-100 pb-4 bg-zinc-50/50">
                  <div className="flex items-center justify-between">
                    <div className="flex items-center gap-3">
                      <div className="w-10 h-10 rounded-2xl bg-zinc-950 text-white flex items-center justify-center">
                        <Database className="w-5 h-5" />
                      </div>
                      <div>
                        <CardTitle className="text-base font-bold text-zinc-950 flex items-center gap-2">
                          Redis Cache Durumu
                          <Badge variant={isRedisConnected ? "success" : "destructive"} className="text-[10px]">
                            {isRedisConnected ? 'CONNECTED' : 'DISCONNECTED'}
                          </Badge>
                        </CardTitle>
                        <CardDescription className="text-xs mt-0.5">
                          Ultra hızlı kısa link yönlendirmeleri için bellek içi önbellek
                        </CardDescription>
                      </div>
                    </div>
                  </div>
                </CardHeader>

                <CardContent className="pt-5 space-y-4">
                  <div className={`p-3.5 rounded-2xl text-xs font-medium border flex items-start gap-2.5 ${
                    isRedisConnected ? 'bg-zinc-50 border-zinc-200 text-zinc-900' : 'bg-red-50 border-red-200 text-red-800'
                  }`}>
                    <Zap className="w-4 h-4 shrink-0 mt-0.5 text-zinc-900" />
                    <div>
                      <p className="font-bold">{systemStatus?.redis.message}</p>
                      <p className="text-[11px] text-zinc-500 mt-0.5">
                        Tıklama yönlendirme istekleri ilk olarak Redis'ten aranır (Ort. gecikme: &lt;2ms).
                      </p>
                    </div>
                  </div>

                  {/* Redis Metric Grid */}
                  <div className="grid grid-cols-2 sm:grid-cols-3 gap-3">
                    <div className="p-3 rounded-2xl bg-zinc-50 border border-zinc-200/80">
                      <span className="text-[11px] text-zinc-500 block font-semibold">Toplam Anahtar</span>
                      <span className="text-xl font-bold font-mono text-zinc-950 mt-0.5 block">
                        {systemStatus?.redis.totalKeys ?? 0}
                      </span>
                      <span className="text-[10px] text-zinc-400 font-mono">short_url:*</span>
                    </div>

                    <div className="p-3 rounded-2xl bg-zinc-50 border border-zinc-200/80">
                      <span className="text-[11px] text-zinc-500 block font-semibold">Ping Gecikmesi</span>
                      <span className="text-xl font-bold font-mono text-emerald-600 mt-0.5 block">
                        {systemStatus?.redis.pingLatencyMs !== null ? `${systemStatus?.redis.pingLatencyMs} ms` : '-'}
                      </span>
                      <span className="text-[10px] text-emerald-600 font-medium">Ultra Düşük</span>
                    </div>

                    <div className="p-3 rounded-2xl bg-zinc-50 border border-zinc-200/80">
                      <span className="text-[11px] text-zinc-500 block font-semibold">Bellek Kullanımı</span>
                      <span className="text-xl font-bold font-mono text-zinc-950 mt-0.5 block">
                        {systemStatus?.redis.usedMemory ?? 'N/A'}
                      </span>
                      <span className="text-[10px] text-zinc-400">RAM Tüketimi</span>
                    </div>

                    <div className="p-3 rounded-2xl bg-zinc-50 border border-zinc-200/80">
                      <span className="text-[11px] text-zinc-500 block font-semibold">Sunucu / Host</span>
                      <span className="text-xs font-bold font-mono text-zinc-900 mt-1 block truncate">
                        {systemStatus?.redis.host}:{systemStatus?.redis.port}
                      </span>
                      <span className="text-[10px] text-zinc-400">Standart TCP Portu</span>
                    </div>

                    <div className="p-3 rounded-2xl bg-zinc-50 border border-zinc-200/80">
                      <span className="text-[11px] text-zinc-500 block font-semibold">Redis Sürümü</span>
                      <span className="text-xs font-bold font-mono text-zinc-900 mt-1 block">
                        v{systemStatus?.redis.redisVersion ?? '7.x'}
                      </span>
                      <span className="text-[10px] text-zinc-400">Standalone Engine</span>
                    </div>

                    <div className="p-3 rounded-2xl bg-zinc-50 border border-zinc-200/80">
                      <span className="text-[11px] text-zinc-500 block font-semibold">TTL Politikası</span>
                      <span className="text-xs font-bold font-mono text-zinc-900 mt-1 block">
                        24 Saat
                      </span>
                      <span className="text-[10px] text-zinc-400">Otomatik Temizleme</span>
                    </div>
                  </div>

                  {/* Actions */}
                  <div className="pt-2 flex items-center justify-between border-t border-zinc-100">
                    <p className="text-xs text-zinc-500">
                      Önbellekte tutulan yönlendirmeleri yenilemek için:
                    </p>
                    <Button
                      variant="outline"
                      size="sm"
                      onClick={handleFlushCache}
                      className="text-xs border-zinc-200 text-zinc-800 hover:bg-zinc-100"
                    >
                      <RotateCcw className="w-3.5 h-3.5 mr-1" />
                      Önbelleği Temizle
                    </Button>
                  </div>
                </CardContent>
              </Card>

              {/* Card 2: RabbitMQ Details */}
              <Card className="shadow-sm border-zinc-200/90 overflow-hidden bg-white">
                <CardHeader className="border-b border-zinc-100 pb-4 bg-zinc-50/50">
                  <div className="flex items-center justify-between">
                    <div className="flex items-center gap-3">
                      <div className="w-10 h-10 rounded-2xl bg-zinc-950 text-white flex items-center justify-center">
                        <Layers className="w-5 h-5" />
                      </div>
                      <div>
                        <CardTitle className="text-base font-bold text-zinc-950 flex items-center gap-2">
                          RabbitMQ Mesaj Kuyruğu
                          <Badge variant={isRabbitConnected ? "success" : "destructive"} className="text-[10px]">
                            {isRabbitConnected ? 'CONNECTED' : 'DISCONNECTED'}
                          </Badge>
                        </CardTitle>
                        <CardDescription className="text-xs mt-0.5">
                          Tıklama analitiklerinin kayıpsız ve asenkron işlendiği mesaj hattı
                        </CardDescription>
                      </div>
                    </div>
                  </div>
                </CardHeader>

                <CardContent className="pt-5 space-y-4">
                  {/* Hero Queue Counter Box */}
                  <div className="p-4 rounded-2xl bg-zinc-50 border border-zinc-200/80 flex items-center justify-between">
                    <div>
                      <div className="flex items-center gap-1.5">
                        <Radio className="w-3.5 h-3.5 text-zinc-900 animate-pulse" />
                        <span className="text-xs font-bold uppercase tracking-wider text-zinc-700">
                          Kuyrukta Bekleyen Mesaj Sayısı
                        </span>
                      </div>
                      <div className="flex items-baseline gap-2 mt-1">
                        <span className="text-3xl font-black font-mono text-zinc-950">
                          {systemStatus?.rabbitMq.messageCount ?? 0}
                        </span>
                        <span className="text-xs font-semibold text-zinc-500">adet ileti kuyrukta</span>
                      </div>
                      <p className="text-[11px] text-zinc-500 mt-1">
                        {systemStatus?.rabbitMq.messageCount === 0 
                          ? '✅ Kuyruk boş: Tüm tıklama olayları başarıyla anında tüketildi.' 
                          : '⚡ Kuyrukta bekleyen tıklamalar arka planda tüketici tarafından işleniyor.'}
                      </p>
                    </div>

                    <div className="text-right">
                      <div className="px-3 py-1.5 rounded-xl bg-zinc-900 text-white text-xs font-bold inline-flex items-center gap-1">
                        <Cpu className="w-3.5 h-3.5" />
                        <span>{systemStatus?.rabbitMq.consumerCount ?? 1} Consumer</span>
                      </div>
                      <p className="text-[10px] text-zinc-400 mt-1 font-mono">ClickEventConsumer</p>
                    </div>
                  </div>

                  {/* RabbitMQ Metric Grid */}
                  <div className="grid grid-cols-2 sm:grid-cols-3 gap-3">
                    <div className="p-3 rounded-2xl bg-zinc-50 border border-zinc-200/80">
                      <span className="text-[11px] text-zinc-500 block font-semibold">Kuyruk Adı (Queue)</span>
                      <span className="text-xs font-bold font-mono text-zinc-900 mt-1 block truncate" title={systemStatus?.rabbitMq.queueName}>
                        {systemStatus?.rabbitMq.queueName ?? 'url.click.queue'}
                      </span>
                      <span className="text-[10px] text-zinc-500 font-medium">Durable (Kalıcı)</span>
                    </div>

                    <div className="p-3 rounded-2xl bg-zinc-50 border border-zinc-200/80">
                      <span className="text-[11px] text-zinc-500 block font-semibold">Exchange Adı</span>
                      <span className="text-xs font-bold font-mono text-zinc-900 mt-1 block truncate" title={systemStatus?.rabbitMq.exchangeName}>
                        {systemStatus?.rabbitMq.exchangeName ?? 'url.click.exchange'}
                      </span>
                      <span className="text-[10px] text-zinc-400">Topic Exchange</span>
                    </div>

                    <div className="p-3 rounded-2xl bg-zinc-50 border border-zinc-200/80">
                      <span className="text-[11px] text-zinc-500 block font-semibold">Routing Key</span>
                      <span className="text-xs font-bold font-mono text-zinc-900 mt-1 block truncate" title={systemStatus?.rabbitMq.routingKey}>
                        {systemStatus?.rabbitMq.routingKey ?? 'url.click.routingKey'}
                      </span>
                      <span className="text-[10px] text-zinc-400">Eşleştirme Anahtarı</span>
                    </div>

                    <div className="p-3 rounded-2xl bg-zinc-50 border border-zinc-200/80">
                      <span className="text-[11px] text-zinc-500 block font-semibold">Broker Host:Port</span>
                      <span className="text-xs font-bold font-mono text-zinc-900 mt-1 block">
                        {systemStatus?.rabbitMq.host}:{systemStatus?.rabbitMq.port}
                      </span>
                      <span className="text-[10px] text-zinc-400">AMQP 0-9-1</span>
                    </div>

                    <div className="p-3 rounded-2xl bg-zinc-50 border border-zinc-200/80">
                      <span className="text-[11px] text-zinc-500 block font-semibold">Virtual Host</span>
                      <span className="text-xs font-bold font-mono text-zinc-900 mt-1 block">
                        {systemStatus?.rabbitMq.virtualHost ?? '/'}
                      </span>
                      <span className="text-[10px] text-zinc-400">Root VHost</span>
                    </div>

                    <div className="p-3 rounded-2xl bg-zinc-50 border border-zinc-200/80">
                      <span className="text-[11px] text-zinc-500 block font-semibold">Tüketici Durumu</span>
                      <span className="text-xs font-bold font-mono text-emerald-700 mt-1 block">
                        Dinliyor (Listening)
                      </span>
                      <span className="text-[10px] text-emerald-600 font-medium">Auto-Reconnect</span>
                    </div>
                  </div>

                  <div className="p-3 rounded-2xl bg-zinc-50 border border-zinc-200/80 text-[11px] text-zinc-600 leading-relaxed">
                    <span className="font-bold text-zinc-950">Asenkron Mimari: </span>
                    Kullanıcı kısa linke tıkladığında yönlendirme beklemeden anında gerçekleşir. Tıklama olayı (IP, User-Agent, Ülke/Şehir, Bot tespiti) RabbitMQ kuyruğuna atılır ve arka planda güvenle kaydedilir.
                  </div>
                </CardContent>
              </Card>
            </div>

            {/* Visual Architecture Flow Diagram */}
            <Card className="p-5 bg-white border-zinc-200/90 shadow-sm">
              <h4 className="text-xs font-bold uppercase tracking-wider text-zinc-500 mb-3">
                Canlı Trafik ve Olay İşleme Hattı (Pipeline)
              </h4>
              <div className="grid grid-cols-1 md:grid-cols-4 gap-3 text-center">
                <div className="p-3.5 rounded-2xl bg-zinc-50 border border-zinc-200/80 flex flex-col items-center justify-center">
                  <Globe2 className="w-5 h-5 text-zinc-950 mb-1" />
                  <span className="text-xs font-bold text-zinc-950">1. Ziyaretçi Tıklaması</span>
                  <span className="text-[10px] text-zinc-500 mt-0.5">HTTP GET /{'{code}'}</span>
                </div>

                <div className="p-3.5 rounded-2xl bg-zinc-50 border border-zinc-200/80 flex flex-col items-center justify-center">
                  <Database className="w-5 h-5 text-zinc-950 mb-1" />
                  <span className="text-xs font-bold text-zinc-950">2. Redis Önbellek</span>
                  <span className="text-[10px] text-emerald-600 font-medium mt-0.5">HTTP 302 Yönlendirme (&lt;2ms)</span>
                </div>

                <div className="p-3.5 rounded-2xl bg-zinc-50 border border-zinc-200/80 flex flex-col items-center justify-center">
                  <Layers className="w-5 h-5 text-zinc-950 mb-1" />
                  <span className="text-xs font-bold text-zinc-950">3. RabbitMQ Kuyruğu</span>
                  <span className="text-[10px] text-zinc-600 font-mono mt-0.5">{systemStatus?.rabbitMq.queueName}</span>
                </div>

                <div className="p-3.5 rounded-2xl bg-zinc-50 border border-zinc-200/80 flex flex-col items-center justify-center">
                  <HardDrive className="w-5 h-5 text-zinc-950 mb-1" />
                  <span className="text-xs font-bold text-zinc-950">4. DB Analitik Kaydı</span>
                  <span className="text-[10px] text-zinc-500 font-medium mt-0.5">Bot Filtreleme & Coğrafi Veri</span>
                </div>
              </div>
            </Card>
          </TabsContent>

          {/* Sub-tab 2: User CRM Table */}
          <TabsContent value="users">
            <Card className="border-zinc-200/90 shadow-sm overflow-hidden bg-white">
              <CardHeader className="border-b border-zinc-100 pb-4 bg-zinc-50/40">
                <div className="flex flex-col sm:flex-row items-start sm:items-center justify-between gap-3">
                  <div>
                    <CardTitle className="text-base font-bold text-zinc-950">Kullanıcı Hesap Yönetimi</CardTitle>
                    <CardDescription className="text-xs mt-0.5 text-zinc-500">
                      Kayıtlı hesapları inceleyin, rol değiştirin veya silin
                    </CardDescription>
                  </div>

                  <div className="relative w-full sm:w-64">
                    <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none text-zinc-400">
                      <Search className="w-3.5 h-3.5" />
                    </div>
                    <Input
                      type="text"
                      value={searchUser}
                      onChange={(e) => setSearchUser(e.target.value)}
                      placeholder="Kullanıcı veya e-posta ara..."
                      className="pl-9 h-8 text-xs bg-white"
                    />
                  </div>
                </div>
              </CardHeader>

              <CardContent className="p-0">
                <Table>
                  <TableHeader>
                    <TableRow>
                      <TableHead>ID</TableHead>
                      <TableHead>Kullanıcı Adı</TableHead>
                      <TableHead>E-posta</TableHead>
                      <TableHead>Rol</TableHead>
                      <TableHead>Kayıt Tarihi</TableHead>
                      <TableHead className="text-right">İşlemler</TableHead>
                    </TableRow>
                  </TableHeader>
                  <TableBody>
                    {filteredUsers.map((u) => (
                      <TableRow key={u.id} className="hover:bg-zinc-50/70 transition-colors">
                        <TableCell className="font-mono text-zinc-400 text-xs">#{u.id.substring(0, 8)}...</TableCell>
                        <TableCell className="font-bold text-zinc-950 text-xs">@{u.username}</TableCell>
                        <TableCell className="text-zinc-600 text-xs">{u.email}</TableCell>
                        <TableCell>
                          <Badge variant={u.role === 'ROLE_ADMIN' ? 'default' : 'secondary'} className="text-[10px]">
                            {u.role}
                          </Badge>
                        </TableCell>
                        <TableCell className="font-mono text-zinc-500 text-[11px]">
                          {new Date(u.createdAt).toLocaleDateString('tr-TR')}
                        </TableCell>
                        <TableCell className="text-right space-x-1.5">
                          <Button
                            variant="outline"
                            size="sm"
                            onClick={() => handleToggleAdminRole(u.id)}
                            className="text-xs h-7 bg-white"
                          >
                            {u.role === 'ROLE_ADMIN' ? 'User Yap' : 'Admin Yap'}
                          </Button>
                          <Button
                            variant="ghost"
                            size="iconSm"
                            onClick={() => handleDeleteUser(u.id)}
                            className="text-zinc-400 hover:text-red-600 hover:bg-red-50"
                            title="Hesabı Sil"
                          >
                            <Trash2 className="w-3.5 h-3.5" />
                          </Button>
                        </TableCell>
                      </TableRow>
                    ))}
                  </TableBody>
                </Table>
              </CardContent>
            </Card>
          </TabsContent>

          {/* Sub-tab 3: Global Link Overseer Table */}
          <TabsContent value="links">
            <Card className="border-zinc-200/90 shadow-sm overflow-hidden bg-white">
              <CardHeader className="border-b border-zinc-100 pb-4 bg-zinc-50/40">
                <div className="flex flex-col sm:flex-row items-start sm:items-center justify-between gap-3">
                  <div>
                    <CardTitle className="text-base font-bold text-zinc-950">Sistemdeki Tüm Kısa Linkler</CardTitle>
                    <CardDescription className="text-xs mt-0.5 text-zinc-500">
                      Tüm kullanıcılar tarafından üretilen bağlantılar
                    </CardDescription>
                  </div>

                  <div className="relative w-full sm:w-64">
                    <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none text-zinc-400">
                      <Search className="w-3.5 h-3.5" />
                    </div>
                    <Input
                      type="text"
                      value={searchLink}
                      onChange={(e) => setSearchLink(e.target.value)}
                      placeholder="Kısa kod veya URL ara..."
                      className="pl-9 h-8 text-xs bg-white"
                    />
                  </div>
                </div>
              </CardHeader>

              <CardContent className="p-0">
                <Table>
                  <TableHeader>
                    <TableRow>
                      <TableHead>Kısa Kod</TableHead>
                      <TableHead>Hedef URL</TableHead>
                      <TableHead>Tıklama</TableHead>
                      <TableHead>Güvenlik Durumu</TableHead>
                      <TableHead className="text-right">İşlemler</TableHead>
                    </TableRow>
                  </TableHeader>
                  <TableBody>
                    {filteredSystemLinks.length === 0 ? (
                      <TableRow>
                        <TableCell colSpan={5} className="h-24 text-center text-zinc-400 font-medium">
                          Sistemde kayıtlı link bulunamadı.
                        </TableCell>
                      </TableRow>
                    ) : (
                      filteredSystemLinks.map((link) => (
                        <TableRow key={link.shortCode} className="hover:bg-zinc-50/70 transition-colors">
                          <TableCell className="font-mono font-bold text-zinc-950 text-xs">{link.shortCode}</TableCell>
                          <TableCell className="max-w-xs truncate text-zinc-600 text-xs font-mono">{link.originalUrl}</TableCell>
                          <TableCell className="font-mono font-bold text-zinc-950 text-xs">{link.clickCount}</TableCell>
                          <TableCell>
                            <div className="flex items-center gap-1.5">
                              <Badge variant={link.passwordProtected ? 'warning' : 'success'} className="text-[10px]">
                                {link.passwordProtected ? 'Şifreli' : 'Şifresiz'}
                              </Badge>
                              {link.previewEnabled && (
                                <Badge variant="outline" className="text-[10px] text-emerald-800 bg-emerald-50 border-emerald-200">
                                  Kalkanlı
                                </Badge>
                              )}
                            </div>
                          </TableCell>
                          <TableCell className="text-right">
                            <Button
                              variant="ghost"
                              size="sm"
                              onClick={() => handleAdminDeleteLink(link.shortCode)}
                              className="text-zinc-400 hover:text-red-600 hover:bg-red-50 text-xs h-7"
                            >
                              <Trash2 className="w-3.5 h-3.5 mr-1" />
                              <span>Sil</span>
                            </Button>
                          </TableCell>
                        </TableRow>
                      ))
                    )}
                  </TableBody>
                </Table>
              </CardContent>
            </Card>
          </TabsContent>

          {/* Sub-tab 4: Developer API Key Applications */}
          <TabsContent value="api-keys" className="space-y-6">
            {/* Status Summary Banner */}
            <div className="grid grid-cols-1 sm:grid-cols-4 gap-3.5">
              <div className="p-4 rounded-2xl bg-white border border-zinc-200/90 shadow-2xs">
                <span className="text-xs font-semibold text-zinc-500">Toplam Başvuru</span>
                <h4 className="text-2xl font-black font-mono text-zinc-950 mt-1">{apiKeys.length}</h4>
              </div>

              <div className="p-4 rounded-2xl bg-amber-50 border border-amber-200 shadow-2xs">
                <div className="flex items-center justify-between">
                  <span className="text-xs font-bold text-amber-900">Onay Bekleyenler</span>
                  <Badge variant="warning" className="text-[10px]">İnceleme</Badge>
                </div>
                <h4 className="text-2xl font-black font-mono text-amber-950 mt-1">
                  {apiKeys.filter(k => k.status === 'PENDING').length}
                </h4>
              </div>

              <div className="p-4 rounded-2xl bg-emerald-50 border border-emerald-200 shadow-2xs">
                <div className="flex items-center justify-between">
                  <span className="text-xs font-bold text-emerald-900">Aktif Onaylananlar</span>
                  <Badge variant="success" className="text-[10px]">Aktif</Badge>
                </div>
                <h4 className="text-2xl font-black font-mono text-emerald-950 mt-1">
                  {apiKeys.filter(k => k.status === 'APPROVED').length}
                </h4>
              </div>

              <div className="p-4 rounded-2xl bg-red-50 border border-red-200 shadow-2xs">
                <div className="flex items-center justify-between">
                  <span className="text-xs font-bold text-red-900">Reddedilen / İptal</span>
                  <Badge variant="destructive" className="text-[10px]">Pasif</Badge>
                </div>
                <h4 className="text-2xl font-black font-mono text-red-950 mt-1">
                  {apiKeys.filter(k => k.status === 'REJECTED' || k.status === 'REVOKED').length}
                </h4>
              </div>
            </div>

            {/* Applications Table Card */}
            <Card className="shadow-sm border-zinc-200/90 overflow-hidden bg-white">
              <CardHeader className="p-5 border-b border-zinc-100 bg-zinc-50/50">
                <div className="flex flex-col md:flex-row md:items-center justify-between gap-4">
                  <div>
                    <CardTitle className="text-base font-bold text-zinc-950 flex items-center gap-2">
                      <KeyRound className="w-4 h-4 text-amber-500" />
                      <span>Geliştirici API Anahtarı Başvuruları & Onay Masası</span>
                    </CardTitle>
                    <CardDescription className="text-xs mt-0.5 text-zinc-500">
                      Kullanıcıların harici entegrasyonlar için talep ettiği API anahtarlarını inceleyin ve onaylayın
                    </CardDescription>
                  </div>

                  {/* Filters & Search */}
                  <div className="flex flex-wrap items-center gap-2">
                    <div className="flex items-center p-1 rounded-xl bg-zinc-200/60 border border-zinc-200 text-xs font-semibold gap-1">
                      {(['ALL', 'PENDING', 'APPROVED', 'REJECTED', 'REVOKED'] as const).map((st) => (
                        <button
                          key={st}
                          type="button"
                          onClick={() => setApiKeyStatusFilter(st)}
                          className={`px-2.5 py-1 rounded-lg text-xs transition-all cursor-pointer ${
                            apiKeyStatusFilter === st ? 'bg-white text-zinc-950 shadow-2xs font-bold' : 'text-zinc-600 hover:text-zinc-950'
                          }`}
                        >
                          {st === 'ALL' ? 'Tümü' : st === 'PENDING' ? 'Bekleyenler' : st === 'APPROVED' ? 'Onaylananlar' : st === 'REJECTED' ? 'Reddedilenler' : 'İptal'}
                        </button>
                      ))}
                    </div>

                    <div className="relative w-full sm:w-56">
                      <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none text-zinc-400">
                        <Search className="w-3.5 h-3.5" />
                      </div>
                      <Input
                        type="text"
                        value={searchApiKey}
                        onChange={(e) => setSearchApiKey(e.target.value)}
                        placeholder="Uygulama veya kullanıcı ara..."
                        className="pl-9 h-8 text-xs bg-white"
                      />
                    </div>
                  </div>
                </div>
              </CardHeader>

              <CardContent className="p-0">
                <Table>
                  <TableHeader>
                    <TableRow>
                      <TableHead>Kullanıcı</TableHead>
                      <TableHead>Uygulama & Web Sitesi</TableHead>
                      <TableHead>Kullanım Amacı & Hacim</TableHead>
                      <TableHead>Durum</TableHead>
                      <TableHead>Kota & Kullanım</TableHead>
                      <TableHead className="text-right">Admin İşlemleri</TableHead>
                    </TableRow>
                  </TableHeader>
                  <TableBody>
                    {apiKeys.filter((k) => {
                      const matchFilter = apiKeyStatusFilter === 'ALL' || k.status === apiKeyStatusFilter;
                      const matchSearch =
                        (k.appName || '').toLowerCase().includes(searchApiKey.toLowerCase()) ||
                        (k.username || '').toLowerCase().includes(searchApiKey.toLowerCase()) ||
                        (k.purpose || '').toLowerCase().includes(searchApiKey.toLowerCase()) ||
                        (k.keyPrefix || '').toLowerCase().includes(searchApiKey.toLowerCase());
                      return matchFilter && matchSearch;
                    }).length === 0 ? (
                      <TableRow>
                        <TableCell colSpan={6} className="h-24 text-center text-zinc-400 font-medium">
                          Kriterlere uygun API başvurusu bulunamadı.
                        </TableCell>
                      </TableRow>
                    ) : (
                      apiKeys.filter((k) => {
                        const matchFilter = apiKeyStatusFilter === 'ALL' || k.status === apiKeyStatusFilter;
                        const matchSearch =
                          (k.appName || '').toLowerCase().includes(searchApiKey.toLowerCase()) ||
                          (k.username || '').toLowerCase().includes(searchApiKey.toLowerCase()) ||
                          (k.purpose || '').toLowerCase().includes(searchApiKey.toLowerCase()) ||
                          (k.keyPrefix || '').toLowerCase().includes(searchApiKey.toLowerCase());
                        return matchFilter && matchSearch;
                      }).map((keyItem) => (
                        <TableRow key={keyItem.id} className="hover:bg-zinc-50/70 transition-colors">
                          {/* User */}
                          <TableCell>
                            <div>
                              <span className="font-bold text-xs text-zinc-950 block">@{keyItem.username || 'user'}</span>
                              <span className="text-[11px] text-zinc-400">{keyItem.userEmail || '-'}</span>
                            </div>
                          </TableCell>

                          {/* App & Website */}
                          <TableCell className="max-w-xs">
                            <div className="space-y-0.5">
                              <span className="font-bold text-xs text-zinc-900 block">{keyItem.appName}</span>
                              {keyItem.websiteUrl ? (
                                <a
                                  href={keyItem.websiteUrl}
                                  target="_blank"
                                  rel="noreferrer"
                                  className="text-[11px] text-purple-600 hover:underline flex items-center gap-1 font-mono truncate"
                                >
                                  <span>{keyItem.websiteUrl}</span>
                                  <ExternalLink className="w-2.5 h-2.5 shrink-0" />
                                </a>
                              ) : (
                                <span className="text-[11px] text-zinc-400 italic">Web sitesi belirtilmedi</span>
                              )}
                            </div>
                          </TableCell>

                          {/* Purpose & Volume */}
                          <TableCell className="max-w-xs">
                            <div className="space-y-1">
                              <p className="text-xs text-zinc-700 line-clamp-2" title={keyItem.purpose}>
                                {keyItem.purpose}
                              </p>
                              <Badge variant="outline" className="text-[10px] bg-zinc-50 text-zinc-600">
                                📊 {keyItem.expectedMonthlyClicks || '1.000 - 10.000 / Ay'}
                              </Badge>
                              {keyItem.ipWhitelist && (
                                <span className="text-[10px] font-mono text-zinc-500 block truncate">
                                  IP: {keyItem.ipWhitelist}
                                </span>
                              )}
                            </div>
                          </TableCell>

                          {/* Status */}
                          <TableCell>
                            {keyItem.status === 'PENDING' && (
                              <Badge variant="warning" className="text-[10px] animate-pulse">
                                ONAY BEKLİYOR
                              </Badge>
                            )}
                            {keyItem.status === 'APPROVED' && (
                              <Badge variant="success" className="text-[10px]">
                                ONAYLANDI
                              </Badge>
                            )}
                            {keyItem.status === 'REJECTED' && (
                              <div className="space-y-0.5">
                                <Badge variant="destructive" className="text-[10px]">
                                  REDDEDİLDİ
                                </Badge>
                                {keyItem.rejectionReason && (
                                  <span className="text-[10px] text-red-600 block line-clamp-1" title={keyItem.rejectionReason}>
                                    {keyItem.rejectionReason}
                                  </span>
                                )}
                              </div>
                            )}
                            {keyItem.status === 'REVOKED' && (
                              <Badge variant="secondary" className="text-[10px] bg-zinc-200 text-zinc-700">
                                İPTAL EDİLDİ
                              </Badge>
                            )}
                          </TableCell>

                          {/* Quota & Usage */}
                          <TableCell>
                            <div className="space-y-0.5 font-mono text-xs">
                              <span className="font-semibold text-zinc-900 block">{keyItem.rateLimitPerMinute} req/dk</span>
                              <span className="text-[11px] text-zinc-500">{keyItem.totalCalls.toLocaleString()} çağrı</span>
                            </div>
                          </TableCell>

                          {/* Actions */}
                          <TableCell className="text-right">
                            <div className="flex items-center justify-end gap-1.5">
                              {keyItem.status === 'PENDING' && (
                                <>
                                  <Button
                                    size="sm"
                                    onClick={() => {
                                      setActionModal({ type: 'approve', key: keyItem });
                                      setCustomRateLimit(120);
                                    }}
                                    className="bg-emerald-600 hover:bg-emerald-700 text-white text-xs h-7 px-2.5 rounded-lg font-bold gap-1"
                                  >
                                    <Check className="w-3 h-3" />
                                    <span>Onayla</span>
                                  </Button>

                                  <Button
                                    size="sm"
                                    variant="outline"
                                    onClick={() => {
                                      setActionModal({ type: 'reject', key: keyItem });
                                      setRejectionReasonText('');
                                    }}
                                    className="text-red-600 border-red-200 hover:bg-red-50 text-xs h-7 px-2.5 rounded-lg font-bold gap-1"
                                  >
                                    <X className="w-3 h-3" />
                                    <span>Reddet</span>
                                  </Button>
                                </>
                              )}

                              {keyItem.status === 'APPROVED' && (
                                <Button
                                  size="sm"
                                  variant="ghost"
                                  onClick={() => handleRevokeKey(keyItem.id)}
                                  className="text-zinc-500 hover:text-red-600 hover:bg-red-50 text-xs h-7 px-2 rounded-lg"
                                  title="API Anahtarını Askıya Al / İptal Et"
                                >
                                  <span>Askıya Al</span>
                                </Button>
                              )}
                            </div>
                          </TableCell>
                        </TableRow>
                      ))
                    )}
                  </TableBody>
                </Table>
              </CardContent>
            </Card>
          </TabsContent>
        </Tabs>
      </main>

      {/* Admin Action Modal (Approve / Reject) */}
      {actionModal && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/60 backdrop-blur-xs p-4 animate-fadeIn">
          <div className="bg-white rounded-3xl border border-zinc-200 max-w-md w-full p-6 space-y-4 shadow-2xl relative">
            <button
              onClick={() => setActionModal(null)}
              className="absolute top-4 right-4 text-zinc-400 hover:text-zinc-950 transition-colors cursor-pointer"
            >
              <X className="w-5 h-5" />
            </button>

            <div className="space-y-1">
              <div className="flex items-center gap-2">
                <div className={`w-8 h-8 rounded-xl flex items-center justify-center ${
                  actionModal.type === 'approve' ? 'bg-emerald-100 text-emerald-700' : 'bg-red-100 text-red-700'
                }`}>
                  {actionModal.type === 'approve' ? <Check className="w-4 h-4" /> : <X className="w-4 h-4" />}
                </div>
                <h3 className="font-extrabold text-base text-zinc-950">
                  {actionModal.type === 'approve' ? 'API Başvurusunu Onayla' : 'API Başvurusunu Reddet'}
                </h3>
              </div>
              <p className="text-xs text-zinc-500">
                <strong>{actionModal.key.appName}</strong> &mdash; @{actionModal.key.username}
              </p>
            </div>

            {actionModal.type === 'approve' ? (
              <div className="space-y-3 pt-2">
                <label className="text-xs font-bold text-zinc-800 block">
                  Hız Limiti Kotası (Rate Limit Per Minute)
                </label>
                <div className="grid grid-cols-3 gap-2 text-xs">
                  {[60, 120, 300].map((rate) => (
                    <button
                      key={rate}
                      type="button"
                      onClick={() => setCustomRateLimit(rate)}
                      className={`p-2.5 rounded-xl border text-center font-bold transition-all cursor-pointer ${
                        customRateLimit === rate
                          ? 'border-emerald-600 bg-emerald-50 text-emerald-950 ring-2 ring-emerald-600/20'
                          : 'border-zinc-200 hover:border-zinc-300 text-zinc-700'
                      }`}
                    >
                      {rate} istek/dk
                    </button>
                  ))}
                </div>
                <p className="text-[11px] text-zinc-500">
                  Onaylandığında güvenli <code>kl_live_...</code> anahtarı üretilecek ve kullanıcı portalında aktif hale gelecektir.
                </p>
              </div>
            ) : (
              <div className="space-y-3 pt-2">
                <label className="text-xs font-bold text-zinc-800 block">
                  Reddetme Gerekçesi
                </label>
                <textarea
                  value={rejectionReasonText}
                  onChange={(e) => setRejectionReasonText(e.target.value)}
                  rows={3}
                  placeholder="Örn: Belirtilen web sitesi doğrulanamadı veya kullanım amacı politikalarımıza uygun bulunmadı."
                  className="w-full text-xs p-3 rounded-xl border border-zinc-200 focus:outline-none focus:ring-2 focus:ring-red-500"
                />
              </div>
            )}

            <div className="flex items-center justify-end gap-2 pt-3 border-t border-zinc-100">
              <Button
                variant="outline"
                size="sm"
                onClick={() => setActionModal(null)}
                className="text-xs h-9 px-4 rounded-xl"
              >
                Vazgeç
              </Button>

              {actionModal.type === 'approve' ? (
                <Button
                  size="sm"
                  onClick={() => handleApproveKey(actionModal.key.id)}
                  className="bg-emerald-600 hover:bg-emerald-700 text-white font-bold text-xs h-9 px-4 rounded-xl gap-1.5"
                >
                  <Check className="w-3.5 h-3.5" />
                  <span>Onayla & Anahtarı Üret</span>
                </Button>
              ) : (
                <Button
                  size="sm"
                  onClick={() => handleRejectKey(actionModal.key.id)}
                  className="bg-red-600 hover:bg-red-700 text-white font-bold text-xs h-9 px-4 rounded-xl gap-1.5"
                >
                  <X className="w-3.5 h-3.5" />
                  <span>Başvuruyu Reddet</span>
                </Button>
              )}
            </div>
          </div>
        </div>
      )}
    </div>
  );
}

