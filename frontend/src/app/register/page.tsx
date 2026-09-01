'use client';

import React, { useState } from 'react';
import Link from 'next/link';
import { User, Mail, KeyRound, ArrowLeft, Link2, CheckCircle2, AlertCircle, ArrowRight, Sparkles } from 'lucide-react';
import { ApiClient } from '@/lib/api';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Card, CardHeader, CardTitle, CardDescription, CardContent, CardFooter } from '@/components/ui/card';

export default function RegisterPage() {
  const [username, setUsername] = useState('');
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  const [loading, setLoading] = useState(false);
  const [errorMsg, setErrorMsg] = useState('');
  const [successMsg, setSuccessMsg] = useState('');

  const handleRegister = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!username.trim() || !email.trim() || !password.trim()) return;

    if (password !== confirmPassword) {
      setErrorMsg('Şifreler birbiriyle uyuşmuyor!');
      return;
    }

    setLoading(true);
    setErrorMsg('');
    setSuccessMsg('');

    try {
      const res = await ApiClient.registerUser({
        username: username.trim(),
        email: email.trim(),
        password: password.trim(),
      });

      setSuccessMsg(res.message || 'Hesabınız oluşturuldu! Giriş sayfasına yönlendiriliyorsunuz...');
      setTimeout(() => {
        window.location.href = '/login';
      }, 1500);
    } catch (err: any) {
      setErrorMsg(err.message || 'Üye olma işlemi sırasında hata oluştu.');
    } finally {
      setLoading(false);
    }
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

      {/* Main Register Card */}
      <main className="my-auto py-8">
        <Card className="max-w-sm w-full mx-auto border-zinc-200/90 shadow-xl p-2 bg-white">
          <CardHeader className="text-center pb-4">
            <div className="w-10 h-10 rounded-2xl bg-zinc-950 text-white flex items-center justify-center mx-auto mb-2">
              <Sparkles className="w-5 h-5" />
            </div>
            <CardTitle className="text-xl font-extrabold text-zinc-950">Hesap Oluşturun</CardTitle>
            <CardDescription className="text-xs text-zinc-500 mt-1">
              Ücretsiz kayıt olarak tüm linklerinizi ve analitiklerinizi yönetin
            </CardDescription>
          </CardHeader>

          <CardContent>
            <form onSubmit={handleRegister} className="space-y-3.5">
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
                    placeholder="kullanici_adi"
                    className="pl-9 text-xs h-10 bg-zinc-50 focus:bg-white"
                  />
                </div>
              </div>

              {/* Email Input */}
              <div className="space-y-1">
                <label className="text-xs font-semibold text-zinc-700">E-posta Adresi</label>
                <div className="relative">
                  <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none text-zinc-400">
                    <Mail className="w-4 h-4" />
                  </div>
                  <Input
                    type="email"
                    required
                    value={email}
                    onChange={(e) => setEmail(e.target.value)}
                    placeholder="ornek@alanadi.com"
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

              {/* Confirm Password Input */}
              <div className="space-y-1">
                <label className="text-xs font-semibold text-zinc-700">Şifre Tekrarı</label>
                <div className="relative">
                  <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none text-zinc-400">
                    <KeyRound className="w-4 h-4" />
                  </div>
                  <Input
                    type="password"
                    required
                    value={confirmPassword}
                    onChange={(e) => setConfirmPassword(e.target.value)}
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
                disabled={loading || !username || !email || !password}
                className="w-full bg-zinc-950 hover:bg-zinc-800 text-white font-semibold h-10 cursor-pointer"
              >
                {loading ? (
                  <span>Kayıt oluşturuluyor...</span>
                ) : (
                  <>
                    <span>Ücretsiz Kayıt Ol</span>
                    <ArrowRight className="w-4 h-4 ml-1.5" />
                  </>
                )}
              </Button>
            </form>
          </CardContent>

          <CardFooter className="justify-center border-t border-zinc-100 pt-4">
            <p className="text-xs text-zinc-500">
              Zaten hesabınız var mı?{' '}
              <Link href="/login" className="font-semibold text-zinc-950 hover:underline">
                Giriş Yapın
              </Link>
            </p>
          </CardFooter>
        </Card>
      </main>

      {/* Footer */}
      <footer className="text-center text-xs text-zinc-400">
        &copy; 2026 SwiftLink. Güvenli Kayıt Portalı.
      </footer>
    </div>
  );
}

