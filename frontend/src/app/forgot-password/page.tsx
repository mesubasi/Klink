'use client';

import React, { useState } from 'react';
import Link from 'next/link';
import { ArrowLeft, CheckCircle2, AlertCircle, Link2, Mail } from 'lucide-react';
import { ApiClient } from '@/lib/api';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';

export default function ForgotPasswordPage() {
  const [email, setEmail] = useState('');
  const [loading, setLoading] = useState(false);
  const [done, setDone] = useState(false);
  const [error, setError] = useState('');

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!email.trim()) return;
    setLoading(true);
    setError('');
    try {
      await ApiClient.forgotPassword(email.trim());
      setDone(true);
    } catch (err: any) {
      setError(err.message || 'İstek gönderilemedi.');
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
            <CardTitle className="text-lg font-bold">Parolamı unuttum</CardTitle>
            <CardDescription className="text-xs">E-posta adresinizi girin, size bir sıfırlama bağlantısı gönderelim.</CardDescription>
          </CardHeader>
          <CardContent className="space-y-4">
            {done ? (
              <div className="flex items-start gap-2 rounded-xl border border-emerald-200 bg-emerald-50 p-3 text-xs text-emerald-800">
                <CheckCircle2 className="w-4 h-4 shrink-0 mt-0.5" />
                <span>E-posta adresi kayıtlıysa parola sıfırlama bağlantısı gönderildi. Bağlantı 1 saat geçerlidir.</span>
              </div>
            ) : (
              <form onSubmit={handleSubmit} className="space-y-3">
                {error && (
                  <div className="flex items-start gap-2 rounded-xl border border-red-200 bg-red-50 p-3 text-xs text-red-700">
                    <AlertCircle className="w-4 h-4 shrink-0 mt-0.5" /><span>{error}</span>
                  </div>
                )}
                <div className="relative">
                  <Mail className="absolute left-3 top-1/2 -translate-y-1/2 w-3.5 h-3.5 text-zinc-400" />
                  <Input
                    type="email"
                    value={email}
                    onChange={(e) => setEmail(e.target.value)}
                    placeholder="ornek@alanadi.com"
                    required
                    className="pl-9 text-xs"
                  />
                </div>
                <Button type="submit" disabled={loading || !email.trim()} className="w-full text-xs">
                  {loading ? 'Gönderiliyor...' : 'Sıfırlama Bağlantısı Gönder'}
                </Button>
              </form>
            )}
            <Link href="/login" className="flex items-center justify-center gap-1 text-xs text-zinc-500 hover:text-zinc-900">
              <ArrowLeft className="w-3 h-3" /> Girişe dön
            </Link>
          </CardContent>
        </Card>
      </div>
    </div>
  );
}
