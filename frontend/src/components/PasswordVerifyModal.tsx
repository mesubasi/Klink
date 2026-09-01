'use client';

import React, { useState } from 'react';
import { Lock, KeyRound, ExternalLink, ShieldCheck, ArrowRight } from 'lucide-react';
import { Language, translations } from '@/lib/translations';
import { ApiClient } from '@/lib/api';
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
  DialogDescription,
} from '@/components/ui/dialog';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';

interface PasswordVerifyModalProps {
  shortCode: string | null;
  lang: Language;
  onClose: () => void;
}

export const PasswordVerifyModal: React.FC<PasswordVerifyModalProps> = ({
  shortCode,
  lang,
  onClose,
}) => {
  const [password, setPassword] = useState('');
  const [unlockedUrl, setUnlockedUrl] = useState<string | null>(null);
  const [errorMsg, setErrorMsg] = useState('');
  const [loading, setLoading] = useState(false);

  const t = translations[lang];

  const handleUnlock = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!password.trim() || !shortCode) return;

    setLoading(true);
    setErrorMsg('');
    setUnlockedUrl(null);

    try {
      const url = await ApiClient.verifyPassword(shortCode, password.trim(), lang);
      setUnlockedUrl(url);
    } catch (err: any) {
      setErrorMsg(err.message || t.msgError);
    } finally {
      setLoading(false);
    }
  };

  const handleOpenChange = (open: boolean) => {
    if (!open) {
      setPassword('');
      setUnlockedUrl(null);
      setErrorMsg('');
      onClose();
    }
  };

  return (
    <Dialog open={!!shortCode} onOpenChange={handleOpenChange}>
      <DialogContent className="max-w-sm p-6 border-zinc-200/90 shadow-2xl">
        <DialogHeader className="border-b border-zinc-100 pb-3">
          <div className="flex items-center gap-3 pr-6">
            <div className="w-9 h-9 rounded-xl bg-amber-50 text-amber-800 border border-amber-200/80 flex items-center justify-center">
              <Lock className="w-4 h-4" />
            </div>
            <div>
              <DialogTitle className="text-base font-bold text-zinc-950">{t.modalPasswordTitle}</DialogTitle>
              <DialogDescription className="font-mono text-xs text-zinc-500">
                swift.link/{shortCode}
              </DialogDescription>
            </div>
          </div>
        </DialogHeader>

        {unlockedUrl ? (
          <div className="p-4 rounded-2xl bg-emerald-50/80 border border-emerald-200 text-emerald-900 space-y-2.5 animate-fadeIn">
            <div className="flex items-center gap-1.5 text-xs font-bold text-emerald-800">
              <ShieldCheck className="w-4 h-4 text-emerald-600" />
              <span>{lang === 'tr' ? 'Parola Doğrulandı' : 'Password Verified'}</span>
            </div>
            <a
              href={unlockedUrl}
              target="_blank"
              rel="noreferrer"
              className="text-xs font-semibold text-zinc-900 hover:text-emerald-700 underline flex items-center gap-1 break-all"
            >
              <span>{unlockedUrl}</span>
              <ExternalLink className="w-3.5 h-3.5 shrink-0" />
            </a>
          </div>
        ) : (
          <form onSubmit={handleUnlock} className="space-y-4 pt-1">
            <div className="space-y-1.5">
              <label className="text-xs font-semibold text-zinc-700">
                {lang === 'tr' ? 'Erişim Parolası' : 'Access Password'}
              </label>
              <div className="relative">
                <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none text-zinc-400">
                  <KeyRound className="w-4 h-4" />
                </div>
                <Input
                  type="password"
                  required
                  autoFocus
                  value={password}
                  onChange={(e) => setPassword(e.target.value)}
                  placeholder="••••••••"
                  className="pl-9 text-xs h-9 bg-zinc-50 focus:bg-white"
                />
              </div>
            </div>

            {errorMsg && (
              <div className="p-2.5 rounded-xl bg-red-50 border border-red-200 text-red-700 text-xs font-semibold animate-fadeIn">
                <span>⚠️ {errorMsg}</span>
              </div>
            )}

            <Button
              type="submit"
              disabled={loading || !password.trim()}
              className="w-full bg-zinc-950 hover:bg-zinc-800 text-white font-semibold h-10"
            >
              {loading ? (
                <span>{lang === 'tr' ? 'Doğrulanıyor...' : 'Verifying...'}</span>
              ) : (
                <>
                  <span>{t.btnUnlock}</span>
                  <ArrowRight className="w-3.5 h-3.5 ml-1" />
                </>
              )}
            </Button>
          </form>
        )}
      </DialogContent>
    </Dialog>
  );
};

