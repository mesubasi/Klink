'use client';

import React, { useState } from 'react';
import { useParams } from 'next/navigation';
import Link from 'next/link';
import { AlertCircle, CheckCircle2, KeyRound, Link2 } from 'lucide-react';
import { ApiClient } from '@/lib/api';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';

export default function ResetPasswordPage() {
  const params = useParams();
  const token = Array.isArray(params.token) ? params.token[0] : (params.token as string);

  const [password, setPassword] = useState('');
  const [confirm, setConfirm] = useState('');
  const [loading, setLoading] = useState(false);
  const [done, setDone] = useState(false);
  const [error, setError] = useState('');

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError('');
    if (password.length < 6) {
      setError('Parola en az 6 karakter olmalıdır.');
      return;
    }
    if (password !== confirm) {
      setError('Parolalar eşleşmiyor.');
      return;
    }
    setLoading(true);
    try {
      await ApiClient.resetPassword(token, password);
      setDone(true);
      setTimeout(() => {
        window.location.href = '/login';
      }, 2000);
    } catch (err: any) {
      setError(err.message || 'Parola sıfırlanamadı.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-screen bg-[#fafafa] text-zinc-950 flex items-center justify-center px-4 py-10">
      <div className="w-full max-w-md space-y-4">
        <Link href="/" className="flex items-center justify-center gap-2 text-sm font-bold">
          <span className="w-7 h-7 rounded-lg bg-zinc-950 text-white flex items-center justify-center"><Link2 className="w-4 h-4" /></span>
          Klink
        </Link>
        <Card className="border-zinc-200/90 shadow-sm">
          <CardHeader className="text-center">
            <CardTitle className="text-lg font-bold">Yeni parola belirleyin</CardTitle>
            <CardDescription className="text-xs">Bu bağlantı tek kullanımlıktır.</CardDescription>
          </CardHeader>
          <CardContent className="space-y-4">
            {done ? (
              <div className="flex items-start gap-2 rounded-xl border border-emerald-200 bg-emerald-50 p-3 text-xs text-emerald-800">
                <CheckCircle2 className="w-4 h-4 shrink-0 mt-0.5" />
                <span>Parolanız güncellendi. Giriş sayfasına yönlendiriliyorsunuz...</span>
              </div>
            ) : (
              <form onSubmit={handleSubmit} className="space-y-3">
                {error && (
                  <div className="flex items-start gap-2 rounded-xl border border-red-200 bg-red-50 p-3 text-xs text-red-700">
                    <AlertCircle className="w-4 h-4 shrink-0 mt-0.5" />
                    <span>{error} {error.includes('geçersiz') && <Link href="/forgot-password" className="underline">Yeni bağlantı iste</Link>}</span>
                  </div>
                )}
                <div className="relative">
                  <KeyRound className="absolute left-3 top-1/2 -translate-y-1/2 w-3.5 h-3.5 text-zinc-400" />
                  <Input type="password" value={password} onChange={(e) => setPassword(e.target.value)} placeholder="Yeni parola" required className="pl-9 text-xs" />
                </div>
                <div className="relative">
                  <KeyRound className="absolute left-3 top-1/2 -translate-y-1/2 w-3.5 h-3.5 text-zinc-400" />
                  <Input type="password" value={confirm} onChange={(e) => setConfirm(e.target.value)} placeholder="Yeni parola (tekrar)" required className="pl-9 text-xs" />
                </div>
                <Button type="submit" disabled={loading || !password || !confirm} className="w-full text-xs">
                  {loading ? 'Kaydediliyor...' : 'Parolayı Güncelle'}
                </Button>
              </form>
            )}
          </CardContent>
        </Card>
      </div>
    </div>
  );
}
