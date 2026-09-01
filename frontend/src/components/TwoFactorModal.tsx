'use client';

import React, { useState, useEffect } from 'react';
import { ShieldCheck, Copy, Check, Lock, KeyRound, AlertCircle, ArrowRight, ShieldAlert } from 'lucide-react';
import { ApiClient } from '@/lib/api';
import { TotpSetupResponse } from '@/lib/types';
import { Language } from '@/lib/translations';
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
  DialogDescription,
} from '@/components/ui/dialog';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Badge } from '@/components/ui/badge';

interface TwoFactorModalProps {
  isOpen: boolean;
  onClose: () => void;
  lang?: Language;
  authUser: { u: string; p: string };
  isEnabled: boolean;
  onStatusChange: (newStatus: boolean) => void;
}

export const TwoFactorModal: React.FC<TwoFactorModalProps> = ({
  isOpen,
  onClose,
  lang = 'tr',
  authUser,
  isEnabled,
  onStatusChange,
}) => {
  const [setupData, setSetupData] = useState<TotpSetupResponse | null>(null);
  const [code, setCode] = useState('');
  const [loading, setLoading] = useState(false);
  const [errorMsg, setErrorMsg] = useState('');
  const [successMsg, setSuccessMsg] = useState('');
  const [copied, setCopied] = useState(false);

  useEffect(() => {
    if (isOpen) {
      setCode('');
      setErrorMsg('');
      setSuccessMsg('');
      setCopied(false);
      if (!isEnabled) {
        loadSetupData();
      }
    }
  }, [isOpen, isEnabled]);

  const loadSetupData = async () => {
    setLoading(true);
    try {
      const data = await ApiClient.setup2FA(lang, authUser);
      setSetupData(data);
    } catch (err: any) {
      setErrorMsg(err.message || (lang === 'tr' ? '2FA kurulum bilgileri yüklenemedi!' : 'Failed to load 2FA setup!'));
    } finally {
      setLoading(false);
    }
  };

  const handleCopySecret = () => {
    if (setupData?.secretKey) {
      navigator.clipboard.writeText(setupData.secretKey);
      setCopied(true);
      setTimeout(() => setCopied(false), 2000);
    }
  };

  const handleEnable2FA = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!code.trim() || !setupData?.secretKey) return;

    setLoading(true);
    setErrorMsg('');
    setSuccessMsg('');

    try {
      const res = await ApiClient.enable2FA(code.trim(), setupData.secretKey, lang, authUser);
      setSuccessMsg(res.message || (lang === 'tr' ? '2FA başarıyla aktifleştirildi!' : '2FA activated successfully!'));
      onStatusChange(true);
      setTimeout(() => {
        onClose();
      }, 1500);
    } catch (err: any) {
      setErrorMsg(err.message || (lang === 'tr' ? 'Geçersiz 2FA doğrulama kodu!' : 'Invalid 2FA code!'));
    } finally {
      setLoading(false);
    }
  };

  const handleDisable2FA = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!code.trim()) return;

    setLoading(true);
    setErrorMsg('');
    setSuccessMsg('');

    try {
      const res = await ApiClient.disable2FA(code.trim(), lang, authUser);
      setSuccessMsg(res.message || (lang === 'tr' ? '2FA başarıyla devre dışı bırakıldı.' : '2FA disabled.'));
      onStatusChange(false);
      setTimeout(() => {
        onClose();
      }, 1500);
    } catch (err: any) {
      setErrorMsg(err.message || (lang === 'tr' ? 'Geçersiz 2FA kodu!' : 'Invalid 2FA code!'));
    } finally {
      setLoading(false);
    }
  };

  return (
    <Dialog open={isOpen} onOpenChange={(open) => !open && onClose()}>
      <DialogContent className="max-w-md p-6 border-zinc-200/90 shadow-2xl">
        <DialogHeader className="border-b border-zinc-100 pb-3">
          <div className="flex items-center gap-3 pr-6">
            <div className={`w-10 h-10 rounded-2xl flex items-center justify-center ${isEnabled ? 'bg-emerald-50 text-emerald-700 border border-emerald-200' : 'bg-zinc-950 text-white'}`}>
              {isEnabled ? <ShieldCheck className="w-5 h-5" /> : <Lock className="w-5 h-5" />}
            </div>
            <div>
              <DialogTitle className="text-base font-bold text-zinc-950">
                {lang === 'tr' ? 'İki Aşamalı Doğrulama (2FA)' : 'Two-Factor Authentication (2FA)'}
              </DialogTitle>
              <DialogDescription className="text-xs text-zinc-500 mt-0.5">
                {isEnabled
                  ? (lang === 'tr' ? 'Hesabınız TOTP 2FA ile tam korunmaktadır.' : 'Your account is secured with 2FA.')
                  : (lang === 'tr' ? 'Google Authenticator ile hesabınızı güvenceye alın.' : 'Secure your account with an Authenticator app.')}
              </DialogDescription>
            </div>
          </div>
        </DialogHeader>

        {/* Dynamic Content */}
        {isEnabled ? (
          /* DISABLE 2FA VIEW */
          <form onSubmit={handleDisable2FA} className="space-y-4 pt-1">
            <div className="p-4 rounded-2xl bg-emerald-50/80 border border-emerald-200 text-xs text-emerald-900 flex items-start gap-3">
              <ShieldCheck className="w-5 h-5 shrink-0 text-emerald-600 mt-0.5" />
              <div>
                <p className="font-bold">{lang === 'tr' ? '2FA Koruması Aktif' : '2FA is Active'}</p>
                <p className="text-emerald-800 text-[11px] mt-0.5 leading-relaxed">
                  {lang === 'tr'
                    ? 'Her oturum açılışında 6 haneli dinamik doğrulama kodu istenir.'
                    : 'A 6-digit verification code is required at every login.'}
                </p>
              </div>
            </div>

            <div className="space-y-1.5">
              <label className="text-xs font-semibold text-zinc-700">
                {lang === 'tr' ? 'Kapatmak İçin Güncel 6 Haneli Kodu Girin' : 'Enter Current Code to Disable'}
              </label>
              <div className="relative">
                <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none text-zinc-400">
                  <KeyRound className="w-4 h-4" />
                </div>
                <Input
                  type="text"
                  maxLength={6}
                  required
                  value={code}
                  onChange={(e) => setCode(e.target.value)}
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
                <Check className="w-4 h-4 shrink-0" />
                <span>{successMsg}</span>
              </div>
            )}

            <Button
              type="submit"
              variant="destructive"
              disabled={loading || code.length < 6}
              className="w-full h-10 font-semibold cursor-pointer"
            >
              {loading ? (lang === 'tr' ? 'Devre Dışı Bırakılıyor...' : 'Disabling...') : (lang === 'tr' ? '2FA Korumasını Kapat' : 'Disable 2FA')}
            </Button>
          </form>
        ) : (
          /* ENABLE 2FA VIEW */
          <form onSubmit={handleEnable2FA} className="space-y-4 pt-1">
            {/* Step 1: Scan QR Code */}
            <div className="space-y-2">
              <div className="flex items-center gap-2 text-xs font-bold text-zinc-950">
                <span className="w-5 h-5 rounded-full bg-zinc-950 text-white flex items-center justify-center text-[10px] font-bold">1</span>
                <span>{lang === 'tr' ? 'QR Kodu Tarayın' : 'Scan the QR Code'}</span>
              </div>

              {setupData ? (
                <div className="flex flex-col sm:flex-row items-center gap-3 p-3.5 rounded-2xl bg-zinc-50 border border-zinc-200/80">
                  <div className="bg-white p-2 rounded-xl border border-zinc-200 shrink-0 shadow-2xs">
                    <img src={setupData.qrCodeUrl} alt="2FA QR Code" className="w-24 h-24" />
                  </div>
                  <div className="space-y-2 text-left w-full">
                    <p className="text-[11px] text-zinc-500 leading-relaxed">
                      {lang === 'tr'
                        ? 'Google Authenticator veya 1Password ile QR kodu okutun.'
                        : 'Scan with Google Authenticator or 1Password.'}
                    </p>
                    <div className="space-y-1">
                      <span className="text-[10px] uppercase font-bold text-zinc-400">
                        {lang === 'tr' ? 'Manuel Kurulum Anahtarı' : 'Manual Key'}
                      </span>
                      <div className="flex items-center justify-between p-2 rounded-lg bg-white border border-zinc-200 font-mono text-[11px] text-zinc-900">
                        <span className="truncate pr-2 font-bold">{setupData.secretKey}</span>
                        <button
                          type="button"
                          onClick={handleCopySecret}
                          className="p-1 rounded-md hover:bg-zinc-100 text-zinc-500 hover:text-zinc-900 transition-colors shrink-0 cursor-pointer"
                          title="Anahtarı kopyala"
                        >
                          {copied ? <Check className="w-3.5 h-3.5 text-emerald-600" /> : <Copy className="w-3.5 h-3.5" />}
                        </button>
                      </div>
                    </div>
                  </div>
                </div>
              ) : (
                <div className="p-6 text-center text-xs text-zinc-400 space-y-2">
                  <div className="w-6 h-6 border-2 border-zinc-900 border-t-transparent rounded-full animate-spin mx-auto" />
                  <p>{lang === 'tr' ? 'Kurulum anahtarı hazırlanıyor...' : 'Preparing setup...'}</p>
                </div>
              )}
            </div>

            {/* Step 2: Verify Code */}
            <div className="space-y-1.5">
              <div className="flex items-center gap-2 text-xs font-bold text-zinc-950">
                <span className="w-5 h-5 rounded-full bg-zinc-950 text-white flex items-center justify-center text-[10px] font-bold">2</span>
                <span>{lang === 'tr' ? 'Uygulamadaki 6 Haneli Kodu Girin' : 'Enter 6-Digit Code'}</span>
              </div>
              <div className="relative">
                <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none text-zinc-400">
                  <KeyRound className="w-4 h-4" />
                </div>
                <Input
                  type="text"
                  maxLength={6}
                  required
                  value={code}
                  onChange={(e) => setCode(e.target.value)}
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
                <Check className="w-4 h-4 shrink-0" />
                <span>{successMsg}</span>
              </div>
            )}

            <Button
              type="submit"
              disabled={loading || code.length < 6 || !setupData}
              className="w-full bg-zinc-950 hover:bg-zinc-800 text-white font-semibold h-10"
            >
              {loading ? (
                <span>{lang === 'tr' ? 'Doğrulanıyor...' : 'Verifying...'}</span>
              ) : (
                <>
                  <span>{lang === 'tr' ? '2FA Doğrula ve Etkinleştir' : 'Verify & Enable 2FA'}</span>
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

