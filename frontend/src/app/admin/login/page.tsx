'use client';

import React, { useState } from 'react';
import Link from 'next/link';
import { ShieldCheck, KeyRound, ArrowLeft, Shield, CheckCircle2, AlertCircle, ArrowRight, Lock, UserCheck, ShieldAlert } from 'lucide-react';
import { ApiClient } from '@/lib/api';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Card, CardHeader, CardTitle, CardDescription, CardContent, CardFooter } from '@/components/ui/card';
import { Badge } from '@/components/ui/badge';

export default function AdminLoginPage() {
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [totpCode, setTotpCode] = useState('');
  const [step, setStep] = useState<'credentials' | '2fa'>('credentials');
  const [loading, setLoading] = useState(false);
  const [errorMsg, setErrorMsg] = useState('');
  const [successMsg, setSuccessMsg] = useState('');

  const handleAdminLogin = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!username.trim() || !password.trim()) return;

    setLoading(true);
    setErrorMsg('');
    setSuccessMsg('');

    try {
      const authRes = await ApiClient.loginUser({ 
        username: username.trim(), 
        password: password.trim() 
      });

      // Role check: Only ROLE_ADMIN is allowed to enter here
      if (authRes.role !== 'ROLE_ADMIN') {
        throw new Error('Erişim Reddedildi: Bu hesaba Sistem Yöneticisi (ROLE_ADMIN) yetkisi tanımlanmamıştır.');
      }

      if (authRes.twoFactorRequired) {
        setStep('2fa');
        setSuccessMsg('2FA Koruması: Lütfen authenticator kodunuzu girin.');
      } else {
        completeLogin(authRes.username, authRes.role);
      }
    } catch (err: any) {
      setErrorMsg(err.message || 'Yönetici bilgileri hatalı veya yetkisiz erişim!');
    } finally {
      setLoading(false);
    }
  };

  const handleVerify2FA = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!totpCode.trim()) return;

    setLoading(true);
    setErrorMsg('');
    setSuccessMsg('');

    try {
      const res = await ApiClient.verify2FALogin({
        username: username.trim(),
        password: password.trim(),
        code: totpCode.trim(),
      });

      if (res.role !== 'ROLE_ADMIN') {
        throw new Error('Erişim Reddedildi: Yetersiz yetki düzeyi.');
      }

      completeLogin(res.username, res.role);
    } catch (err: any) {
      setErrorMsg(err.message || 'Geçersiz 2FA doğrulama kodu!');
    } finally {
      setLoading(false);
    }
  };

  const completeLogin = (userUsername: string, role: string) => {
    localStorage.setItem('klink_user', JSON.stringify({ u: userUsername, p: password.trim(), role }));
    localStorage.setItem('swiftlink_user', JSON.stringify({ u: userUsername, p: password.trim(), role }));
    setSuccessMsg('Yetkilendirme başarılı! Admin CRM paneline yönlendiriliyorsunuz...');
    setTimeout(() => {
      window.location.href = '/admin';
    }, 800);
  };

  const handleQuickFillAdmin = () => {
    setUsername('admin');
    setPassword('admin123');
    setStep('credentials');
    setErrorMsg('');
  };

  return (
    <div className="min-h-screen bg-zinc-950 text-zinc-100 flex flex-col justify-between p-4 sm:p-6 selection:bg-amber-500 selection:text-black">
      {/* Top Bar */}
      <header className="max-w-4xl mx-auto w-full flex items-center justify-between">
        <Link
          href="/"
          className="flex items-center gap-1.5 text-xs font-semibold text-zinc-400 hover:text-white transition-colors"
        >
          <ArrowLeft className="w-3.5 h-3.5" />
          <span>Ana Sayfaya Dön</span>
        </Link>

        <div className="flex items-center gap-2">
          <Badge variant="outline" className="border-amber-500/40 text-amber-400 bg-amber-500/10 text-[10px] font-mono px-2 py-0.5">
            Sistem Yönetim Portalı
          </Badge>
        </div>
      </header>

      {/* Main Admin Login Card */}
      <main className="my-auto py-8">
        <Card className="max-w-sm w-full mx-auto border-zinc-800 shadow-2xl p-2 bg-zinc-900/90 backdrop-blur-xl">
          <CardHeader className="text-center pb-4">
            <div className="w-12 h-12 rounded-2xl bg-amber-500/10 border border-amber-500/30 text-amber-400 flex items-center justify-center mx-auto mb-3 shadow-inner">
              <Shield className="w-6 h-6" />
            </div>
            <CardTitle className="text-xl font-black text-white tracking-tight">
              {step === '2fa' ? '2FA Yönetici Onayı' : 'Sistem Yöneticisi Girişi'}
            </CardTitle>
            <CardDescription className="text-xs text-zinc-400 mt-1.5">
              {step === '2fa'
                ? 'Sistem yöneticisi hesabınız için 6 haneli güvenlik kodunu girin'
                : 'Klink Core & CRM yönetim paneline erişmek için oturum açın'}
            </CardDescription>
          </CardHeader>

          <CardContent className="space-y-4">
            {step === 'credentials' ? (
              <>
                {/* Dev Mode Quick Admin Fill */}
                {process.env.NODE_ENV !== 'production' && (
                  <div className="p-2.5 rounded-xl bg-zinc-950/60 border border-zinc-800/80 text-center">
                    <p className="text-[10px] font-mono text-zinc-500 uppercase tracking-wider mb-1.5">
                      Geliştirme Modu: Varsayılan Admin
                    </p>
                    <Button
                      type="button"
                      variant="outline"
                      size="sm"
                      onClick={handleQuickFillAdmin}
                      className="w-full bg-zinc-900 border-zinc-700 hover:bg-zinc-800 text-amber-300 text-xs h-8 font-medium"
                    >
                      <UserCheck className="w-3.5 h-3.5 mr-1.5 text-amber-400" />
                      <span>Admin Bilgilerini Doldur (admin / admin123)</span>
                    </Button>
                  </div>
                )}

                <form onSubmit={handleAdminLogin} className="space-y-3.5 pt-1">
                  {/* Admin Username */}
                  <div className="space-y-1">
                    <label className="text-xs font-semibold text-zinc-300">Yönetici Kullanıcı Adı</label>
                    <div className="relative">
                      <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none text-zinc-500">
                        <Shield className="w-4 h-4" />
                      </div>
                      <Input
                        type="text"
                        required
                        autoFocus
                        value={username}
                        onChange={(e) => setUsername(e.target.value)}
                        placeholder="admin"
                        className="pl-9 text-xs h-10 bg-zinc-950 border-zinc-800 text-zinc-100 placeholder:text-zinc-600 focus:border-amber-500 focus:ring-1 focus:ring-amber-500"
                      />
                    </div>
                  </div>

                  {/* Password */}
                  <div className="space-y-1">
                    <label className="text-xs font-semibold text-zinc-300">Yönetici Şifresi</label>
                    <div className="relative">
                      <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none text-zinc-500">
                        <KeyRound className="w-4 h-4" />
                      </div>
                      <Input
                        type="password"
                        required
                        value={password}
                        onChange={(e) => setPassword(e.target.value)}
                        placeholder="••••••••"
                        className="pl-9 text-xs h-10 bg-zinc-950 border-zinc-800 text-zinc-100 placeholder:text-zinc-600 focus:border-amber-500 focus:ring-1 focus:ring-amber-500"
                      />
                    </div>
                  </div>

                  {errorMsg && (
                    <div className="p-3 rounded-xl bg-red-950/50 border border-red-800/80 text-red-300 text-xs font-medium flex items-start gap-2 animate-fadeIn">
                      <AlertCircle className="w-4 h-4 shrink-0 mt-0.5 text-red-400" />
                      <span>{errorMsg}</span>
                    </div>
                  )}

                  {successMsg && (
                    <div className="p-3 rounded-xl bg-emerald-950/50 border border-emerald-800/80 text-emerald-300 text-xs font-medium flex items-center gap-2 animate-fadeIn">
                      <CheckCircle2 className="w-4 h-4 shrink-0 text-emerald-400" />
                      <span>{successMsg}</span>
                    </div>
                  )}

                  <Button
                    type="submit"
                    disabled={loading || !username || !password}
                    className="w-full bg-amber-500 hover:bg-amber-400 text-black font-bold h-10 transition-colors"
                  >
                    {loading ? (
                      <span>Doğrulanıyor...</span>
                    ) : (
                      <>
                        <span>Yönetici Girişi Yap</span>
                        <ArrowRight className="w-4 h-4 ml-1.5" />
                      </>
                    )}
                  </Button>
                </form>
              </>
            ) : (
              /* STEP 2: 2FA CODE FORM */
              <form onSubmit={handleVerify2FA} className="space-y-4">
                <div className="p-3.5 rounded-2xl bg-zinc-950 border border-zinc-800 text-xs text-zinc-300 flex items-center gap-3">
                  <ShieldCheck className="w-5 h-5 text-emerald-400 shrink-0" />
                  <div>
                    <p className="font-bold text-white">Yönetici 2FA Koruması</p>
                    <p className="text-zinc-400 text-[11px]">
                      Authenticator uygulamanızdaki 6 haneli kodu girin.
                    </p>
                  </div>
                </div>

                <div className="space-y-1">
                  <label className="text-xs font-semibold text-zinc-300">6 Haneli Kod</label>
                  <div className="relative">
                    <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none text-zinc-500">
                      <Lock className="w-4 h-4" />
                    </div>
                    <Input
                      type="text"
                      maxLength={6}
                      required
                      autoFocus
                      value={totpCode}
                      onChange={(e) => setTotpCode(e.target.value)}
                      placeholder="123456"
                      className="pl-9 font-mono tracking-widest text-center text-sm h-10 bg-zinc-950 border-zinc-800 text-zinc-100 placeholder:text-zinc-600 focus:border-amber-500 focus:ring-1 focus:ring-amber-500"
                    />
                  </div>
                </div>

                {errorMsg && (
                  <div className="p-3 rounded-xl bg-red-950/50 border border-red-800/80 text-red-300 text-xs font-medium flex items-start gap-2">
                    <AlertCircle className="w-4 h-4 shrink-0 mt-0.5 text-red-400" />
                    <span>{errorMsg}</span>
                  </div>
                )}

                {successMsg && (
                  <div className="p-3 rounded-xl bg-emerald-950/50 border border-emerald-800/80 text-emerald-300 text-xs font-medium flex items-center gap-2">
                    <CheckCircle2 className="w-4 h-4 shrink-0 text-emerald-400" />
                    <span>{successMsg}</span>
                  </div>
                )}

                <Button
                  type="submit"
                  disabled={loading || totpCode.length < 6}
                  className="w-full bg-amber-500 hover:bg-amber-400 text-black font-bold h-10"
                >
                  {loading ? 'Doğrulanıyor...' : 'Doğrula ve Paneli Aç'}
                </Button>

                <Button
                  type="button"
                  variant="ghost"
                  size="sm"
                  onClick={() => { setStep('credentials'); setErrorMsg(''); setSuccessMsg(''); }}
                  className="w-full text-zinc-400 hover:text-white"
                >
                  Geri Dön
                </Button>
              </form>
            )}
          </CardContent>

          {/* Notice: No registration allowed in Admin portal */}
          <CardFooter className="justify-center border-t border-zinc-800/70 pt-4 flex-col gap-1 text-center">
            <div className="flex items-center gap-1.5 text-[11px] text-zinc-500 font-mono">
              <ShieldAlert className="w-3.5 h-3.5 text-amber-500/80" />
              <span>Bu alana dışarıdan kayıt kabul edilmez.</span>
            </div>
            <p className="text-[11px] text-zinc-600">
              Kullanıcı girişi için{' '}
              <Link href="/login" className="text-zinc-400 hover:text-white underline">
                Genel Giriş Sayfası
              </Link>
            </p>
          </CardFooter>
        </Card>
      </main>

      {/* Footer */}
      <footer className="text-center text-xs text-zinc-600 font-mono">
        &copy; 2026 Klink Enterprise Security Engine. Yalnızca Yetkili Sistem Yöneticileri İçindir.
      </footer>
    </div>
  );
}
