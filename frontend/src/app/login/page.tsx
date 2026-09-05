'use client';

import React, { useState } from 'react';
import Link from 'next/link';
import { User, KeyRound, ArrowLeft, Link2, ShieldCheck, CheckCircle2, AlertCircle, ArrowRight, Lock, Sparkles } from 'lucide-react';
import { ApiClient } from '@/lib/api';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Card, CardHeader, CardTitle, CardDescription, CardContent, CardFooter } from '@/components/ui/card';
import { Badge } from '@/components/ui/badge';

export default function LoginPage() {
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [totpCode, setTotpCode] = useState('');
  const [step, setStep] = useState<'credentials' | '2fa'>('credentials');
  const [loading, setLoading] = useState(false);
  const [errorMsg, setErrorMsg] = useState('');
  const [successMsg, setSuccessMsg] = useState('');

  const handleLogin = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!username.trim() || !password.trim()) return;

    setLoading(true);
    setErrorMsg('');
    setSuccessMsg('');

    try {
      // Perform initial login check
      const authRes = await ApiClient.loginUser({ username: username.trim(), password: password.trim() });

      if (authRes.twoFactorRequired) {
        setStep('2fa');
        setSuccessMsg('2FA Koruması: Lütfen 6 haneli doğrulama kodunuzu girin.');
      } else {
        completeLogin(authRes.username, authRes.role);
      }
    } catch (err: any) {
      setErrorMsg(err.message || 'Kullanıcı adı veya şifre hatalı!');
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
    setSuccessMsg(`Giriş başarılı! Yönlendiriliyorsunuz...`);
    setTimeout(() => {
      if (role === 'ROLE_ADMIN') {
        window.location.href = '/admin';
      } else {
        window.location.href = '/dashboard';
      }
    }, 1000);
  };

  const handleQuickFill = (u: string, p: string) => {
    setUsername(u);
    setPassword(p);
    setStep('credentials');
    setErrorMsg('');
  };

  return (
    <div className="min-h-screen bg-[#fafafa] text-zinc-950 flex flex-col justify-between p-4 sm:p-6 selection:bg-zinc-900 selection:text-white">
      {/* Header */}
      <header className="max-w-4xl mx-auto w-full flex items-center justify-between">
        <Link
          href="/"
          className="flex items-center gap-1.5 text-xs font-semibold text-zinc-500 hover:text-zinc-950 transition-colors"
        >
          <ArrowLeft className="w-3.5 h-3.5" />
          <span>Ana Sayfaya Dön</span>
        </Link>

        <div className="flex items-center gap-2">
          <div className="w-7 h-7 rounded-xl bg-zinc-950 text-white flex items-center justify-center">
            <Link2 className="w-3.5 h-3.5" />
          </div>
          <span className="font-bold text-sm text-zinc-950 tracking-tight">Klink</span>
        </div>
      </header>

      {/* Main Login Card */}
      <main className="my-auto py-8">
        <Card className="max-w-sm w-full mx-auto border-zinc-200/90 shadow-xl p-2 bg-white">
          <CardHeader className="text-center pb-4">
            <div className="w-10 h-10 rounded-2xl bg-zinc-950 text-white flex items-center justify-center mx-auto mb-2">
              <Lock className="w-5 h-5" />
            </div>
            <CardTitle className="text-xl font-extrabold text-zinc-950">
              {step === '2fa' ? '2FA Doğrulaması' : 'Hesabınıza Giriş Yapın'}
            </CardTitle>
            <CardDescription className="text-xs text-zinc-500 mt-1">
              {step === '2fa'
                ? 'Authenticator uygulamanızdaki 6 haneli kodu girin'
                : 'Linklerinizi yönetmek ve telemetriyi izlemek için giriş yapın'}
            </CardDescription>
          </CardHeader>

          <CardContent className="space-y-4">
            {step === 'credentials' ? (
              <>
                <form onSubmit={handleLogin} className="space-y-3.5 pt-1">
                  {/* Username Input */}
                  <div className="space-y-1">
                    <label className="text-xs font-semibold text-zinc-700">Kullanıcı Adı</label>
                    <div className="relative">
                      <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none text-zinc-400">
                        <User className="w-4 h-4" />
                      </div>
                      <Input
                        type="text"
                        required
                        value={username}
                        onChange={(e) => setUsername(e.target.value)}
                        placeholder="user"
                        className="pl-9 text-xs h-10 bg-zinc-50 focus:bg-white"
                      />
                    </div>
                  </div>

                  {/* Password Input */}
                  <div className="space-y-1">
                    <label className="text-xs font-semibold text-zinc-700">Şifre</label>
                    <div className="relative">
                      <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none text-zinc-400">
                        <KeyRound className="w-4 h-4" />
                      </div>
                      <Input
                        type="password"
                        required
                        value={password}
                        onChange={(e) => setPassword(e.target.value)}
                        placeholder="••••••••"
                        className="pl-9 text-xs h-10 bg-zinc-50 focus:bg-white"
                      />
                    </div>
                  </div>

                  {errorMsg && (
                    <div className="p-2.5 rounded-xl bg-red-50 border border-red-200 text-red-700 text-xs font-semibold flex items-center gap-2 animate-fadeIn">
                      <AlertCircle className="w-4 h-4 shrink-0" />
                      <span>{errorMsg}</span>
                    </div>
                  )}

                  {successMsg && (
                    <div className="p-2.5 rounded-xl bg-emerald-50 border border-emerald-200 text-emerald-700 text-xs font-semibold flex items-center gap-2 animate-fadeIn">
                      <CheckCircle2 className="w-4 h-4 shrink-0" />
                      <span>{successMsg}</span>
                    </div>
                  )}

                  <Button
                    type="submit"
                    disabled={loading || !username || !password}
                    className="w-full bg-zinc-950 hover:bg-zinc-800 text-white font-semibold h-10"
                  >
                    {loading ? (
                      <span>Giriş yapılıyor...</span>
                    ) : (
                      <>
                        <span>Giriş Yap</span>
                        <ArrowRight className="w-3.5 h-3.5 ml-1.5" />
                      </>
                    )}
                  </Button>
                </form>
              </>
            ) : (
              /* STEP 2: 2FA CODE FORM */
              <form onSubmit={handleVerify2FA} className="space-y-4">
                <div className="p-3.5 rounded-2xl bg-zinc-100 border border-zinc-200 text-xs text-zinc-900 flex items-center gap-3">
                  <ShieldCheck className="w-5 h-5 text-emerald-600 shrink-0" />
                  <div>
                    <p className="font-bold">@{username} Hesabı Korumalı</p>
                    <p className="text-zinc-500 text-[11px]">
                      Lütfen authenticator uygulamanızdaki 6 haneli kodu girin.
                    </p>
                  </div>
                </div>

                <div className="space-y-1">
                  <label className="text-xs font-semibold text-zinc-700">6 Haneli Kod</label>
                  <div className="relative">
                    <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none text-zinc-400">
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
                      className="pl-9 font-mono tracking-widest text-center text-sm h-10 bg-zinc-50 focus:bg-white"
                    />
                  </div>
                </div>

                {errorMsg && (
                  <div className="p-2.5 rounded-xl bg-red-50 border border-red-200 text-red-700 text-xs font-semibold flex items-center gap-2 animate-fadeIn">
                    <AlertCircle className="w-4 h-4 shrink-0" />
                    <span>{errorMsg}</span>
                  </div>
                )}

                {successMsg && (
                  <div className="p-2.5 rounded-xl bg-emerald-50 border border-emerald-200 text-emerald-700 text-xs font-semibold flex items-center gap-2 animate-fadeIn">
                    <CheckCircle2 className="w-4 h-4 shrink-0" />
                    <span>{successMsg}</span>
                  </div>
                )}

                <Button
                  type="submit"
                  disabled={loading || totpCode.length < 6}
                  className="w-full bg-zinc-950 hover:bg-zinc-800 text-white font-semibold h-10"
                >
                  {loading ? (
                    <span>Doğrulanıyor...</span>
                  ) : (
                    <>
                      <span>Doğrula ve Oturum Aç</span>
                      <ArrowRight className="w-4 h-4 ml-1.5" />
                    </>
                  )}
                </Button>

                <Button
                  type="button"
                  variant="ghost"
                  size="sm"
                  onClick={() => { setStep('credentials'); setErrorMsg(''); setSuccessMsg(''); }}
                  className="w-full text-zinc-600"
                >
                  Geri Dön
                </Button>
              </form>
            )}
          </CardContent>

          <CardFooter className="justify-center border-t border-zinc-100 pt-4 flex flex-col gap-2 text-center">
            <p className="text-xs text-zinc-500">
              Hesabınız yok mu?{' '}
              <Link href="/register" className="font-semibold text-zinc-950 hover:underline">
                Kayıt Olun
              </Link>
            </p>
            <p className="text-[11px] text-zinc-400">
              Sistem Yöneticisi misiniz?{' '}
              <Link href="/admin/login" className="font-semibold text-zinc-800 hover:underline">
                Yönetici Portalı
              </Link>
            </p>
          </CardFooter>
        </Card>
      </main>

      {/* Footer */}
      <footer className="text-center text-xs text-zinc-400">
        &copy; 2026 SwiftLink. Güvenli Giriş Portalı.
      </footer>
    </div>
  );
}

