'use client';

import React, { useEffect, useState } from 'react';
import { useParams } from 'next/navigation';
import Link from 'next/link';
import { Building2, CheckCircle2, AlertCircle, Link2, LogIn, UserPlus, Clock } from 'lucide-react';
import { ApiClient } from '@/lib/api';
import { InvitationPreviewResponse } from '@/lib/types';
import { Button } from '@/components/ui/button';
import { Badge } from '@/components/ui/badge';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';

type StoredUser = { u: string; p?: string; token?: string; role?: string };

const ROLE_LABELS: Record<string, string> = {
  ADMIN: 'Yönetici — üye davet edebilir, rolleri ve izinleri yönetebilir',
  MEMBER: 'Üye — çalışma alanında link oluşturabilir ve yönetebilir',
  VIEWER: 'İzleyici — linkleri ve analitikleri görüntüleyebilir',
};

export default function InvitePage() {
  const params = useParams();
  const token = Array.isArray(params.token) ? params.token[0] : (params.token as string);

  const [invitation, setInvitation] = useState<InvitationPreviewResponse | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [accepting, setAccepting] = useState(false);
  const [accepted, setAccepted] = useState<string | null>(null);
  const [user, setUser] = useState<StoredUser | null>(null);

  useEffect(() => {
    try {
      const saved = localStorage.getItem('klink_user') || localStorage.getItem('swiftlink_user');
      if (saved) {
        const parsed = JSON.parse(saved);
        if (parsed && parsed.u) setUser(parsed);
      }
    } catch {
      // ignore unreadable session data; the visitor simply appears logged out
    }

    ApiClient.getInvitationPreview(token)
      .then(setInvitation)
      .catch((e: Error) => setError(e.message))
      .finally(() => setLoading(false));
  }, [token]);

  const handleAccept = async () => {
    if (!user) return;
    setAccepting(true);
    setError('');
    try {
      const result = await ApiClient.acceptInvitation(token, 'tr', user);
      setAccepted(result.workspaceName);
      setTimeout(() => {
        window.location.href = '/dashboard';
      }, 1500);
    } catch (e: any) {
      setError(e.message || 'Davet kabul edilemedi.');
    } finally {
      setAccepting(false);
    }
  };

  const handleSwitchAccount = () => {
    localStorage.removeItem('klink_user');
    localStorage.removeItem('swiftlink_user');
    setUser(null);
    setError('');
  };

  const redirectParam = encodeURIComponent(`/invite/${token}`);

  return (
    <div className="min-h-screen bg-[#fafafa] text-zinc-950 flex items-center justify-center px-4 py-10">
      <div className="w-full max-w-md space-y-4">
        <Link href="/" className="flex items-center justify-center gap-2 text-sm font-bold">
          <span className="w-7 h-7 rounded-lg bg-zinc-950 text-white flex items-center justify-center">
            <Link2 className="w-4 h-4" />
          </span>
          Klink
        </Link>

        <Card className="border-zinc-200/90 shadow-sm">
          <CardHeader className="text-center">
            <div className="mx-auto w-11 h-11 rounded-2xl bg-zinc-100 flex items-center justify-center text-zinc-700">
              <Building2 className="w-5 h-5" />
            </div>
            <CardTitle className="text-lg font-bold">Çalışma alanı daveti</CardTitle>
            <CardDescription className="text-xs">
              {loading ? 'Davet kontrol ediliyor...' : invitation ? `${invitation.invitedBy ? '@' + invitation.invitedBy + ' sizi' : 'Sizi'} bir çalışma alanına davet etti.` : ''}
            </CardDescription>
          </CardHeader>

          <CardContent className="space-y-4">
            {accepted && (
              <div className="flex items-start gap-2 rounded-xl border border-emerald-200 bg-emerald-50 p-3 text-xs text-emerald-800">
                <CheckCircle2 className="w-4 h-4 shrink-0 mt-0.5" />
                <span><strong>{accepted}</strong> çalışma alanına katıldınız. Panele yönlendiriliyorsunuz...</span>
              </div>
            )}

            {error && (
              <div className="flex items-start gap-2 rounded-xl border border-red-200 bg-red-50 p-3 text-xs text-red-700">
                <AlertCircle className="w-4 h-4 shrink-0 mt-0.5" />
                <span>{error}</span>
              </div>
            )}

            {invitation && !accepted && (
              <>
                <div className="rounded-xl border border-zinc-200 bg-zinc-50/60 p-4 space-y-2.5">
                  <div className="flex items-center justify-between gap-2">
                    <span className="text-sm font-bold">{invitation.workspaceName}</span>
                    <Badge variant="secondary" className="font-mono text-[10px]">{invitation.role}</Badge>
                  </div>
                  <p className="text-xs text-zinc-600">{ROLE_LABELS[invitation.role] || invitation.role}</p>
                  <p className="text-xs text-zinc-500">
                    Davet edilen e-posta: <span className="font-mono text-zinc-800">{invitation.email}</span>
                  </p>
                  <p className="flex items-center gap-1 text-[11px] text-zinc-400">
                    <Clock className="w-3 h-3" />
                    Son geçerlilik: {new Date(invitation.expiresAt).toLocaleString('tr-TR')}
                  </p>
                </div>

                {user ? (
                  <div className="space-y-2">
                    <p className="text-xs text-zinc-500 text-center">
                      <span className="font-semibold text-zinc-800">@{user.u}</span> olarak giriş yapıldı.
                    </p>
                    <Button onClick={handleAccept} disabled={accepting} className="w-full text-xs">
                      {accepting ? 'Katılınıyor...' : 'Daveti Kabul Et'}
                    </Button>
                    <button onClick={handleSwitchAccount} className="w-full text-[11px] text-zinc-500 hover:text-zinc-900 underline">
                      Farklı bir hesapla devam et
                    </button>
                  </div>
                ) : (
                  <div className="space-y-2">
                    <p className="text-xs text-zinc-500 text-center">
                      Katılmak için <span className="font-mono text-zinc-800">{invitation.email}</span> adresiyle giriş yapın veya hesap oluşturun.
                    </p>
                    <Link href={`/register?redirect=${redirectParam}&email=${encodeURIComponent(invitation.email)}`} className="block">
                      <Button className="w-full text-xs gap-1.5"><UserPlus className="w-3.5 h-3.5" /> Hesap Oluştur</Button>
                    </Link>
                    <Link href={`/login?redirect=${redirectParam}`} className="block">
                      <Button variant="outline" className="w-full text-xs gap-1.5"><LogIn className="w-3.5 h-3.5" /> Giriş Yap</Button>
                    </Link>
                  </div>
                )}
              </>
            )}

            {!loading && !invitation && !error && (
              <p className="text-xs text-zinc-500 text-center">Davet bulunamadı.</p>
            )}
          </CardContent>
        </Card>
      </div>
    </div>
  );
}
