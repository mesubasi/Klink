'use client';

import React, { useEffect, useRef, useState } from 'react';
import { useParams } from 'next/navigation';
import Link from 'next/link';
import { CheckCircle2, AlertCircle, Link2, Loader2 } from 'lucide-react';
import { ApiClient } from '@/lib/api';
import { Button } from '@/components/ui/button';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';

export default function VerifyEmailPage() {
  const params = useParams();
  const token = Array.isArray(params.token) ? params.token[0] : (params.token as string);
  const [state, setState] = useState<'loading' | 'ok' | 'error'>('loading');
  const [message, setMessage] = useState('');
  // React strict mode runs effects twice in development; the token is single-use, so only call once.
  const started = useRef(false);

  useEffect(() => {
    if (started.current) return;
    started.current = true;
    ApiClient.verifyEmail(token)
      .then((msg) => {
        setMessage(msg || 'E-posta adresiniz doğrulandı.');
        setState('ok');
      })
      .catch((e: Error) => {
        setMessage(e.message);
        setState('error');
      });
  }, [token]);

  return (
    <div className="min-h-screen bg-[#fafafa] text-zinc-950 flex items-center justify-center px-4 py-10">
      <div className="w-full max-w-md space-y-4">
        <Link href="/" className="flex items-center justify-center gap-2 text-sm font-bold">
          <span className="w-7 h-7 rounded-lg bg-zinc-950 text-white flex items-center justify-center"><Link2 className="w-4 h-4" /></span>
          Klink
        </Link>
        <Card className="border-zinc-200/90 shadow-sm">
          <CardHeader className="text-center">
            <CardTitle className="text-lg font-bold">E-posta doğrulama</CardTitle>
            <CardDescription className="text-xs">Hesabınızın e-posta adresi kontrol ediliyor.</CardDescription>
          </CardHeader>
          <CardContent className="space-y-4">
            {state === 'loading' && (
              <div className="flex justify-center text-zinc-500"><Loader2 className="w-5 h-5 animate-spin" /></div>
            )}
            {state === 'ok' && (
              <div className="flex items-start gap-2 rounded-xl border border-emerald-200 bg-emerald-50 p-3 text-xs text-emerald-800">
                <CheckCircle2 className="w-4 h-4 shrink-0 mt-0.5" /><span>{message}</span>
              </div>
            )}
            {state === 'error' && (
              <div className="flex items-start gap-2 rounded-xl border border-red-200 bg-red-50 p-3 text-xs text-red-700">
                <AlertCircle className="w-4 h-4 shrink-0 mt-0.5" />
                <span>{message} Panelinizdeki uyarıdan yeni bir doğrulama e-postası isteyebilirsiniz.</span>
              </div>
            )}
            {state !== 'loading' && (
              <Link href="/dashboard" className="block">
                <Button className="w-full text-xs">Panele Git</Button>
              </Link>
            )}
          </CardContent>
        </Card>
      </div>
    </div>
  );
}
