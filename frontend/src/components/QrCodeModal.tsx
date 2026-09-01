'use client';

import React from 'react';
import { Download, QrCode } from 'lucide-react';
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

interface QrCodeModalProps {
  shortCode: string | null;
  lang: Language;
  onClose: () => void;
}

export const QrCodeModal: React.FC<QrCodeModalProps> = ({ shortCode, lang, onClose }) => {
  const t = translations[lang];
  const qrUrl = shortCode ? ApiClient.getQrCodeUrl(shortCode, 400, 400) : '';

  return (
    <Dialog open={!!shortCode} onOpenChange={(open) => !open && onClose()}>
      <DialogContent className="max-w-xs p-6 border-zinc-200/90 shadow-2xl">
        <DialogHeader className="border-b border-zinc-100 pb-3">
          <div className="flex items-center gap-3 pr-6">
            <div className="w-9 h-9 rounded-xl bg-zinc-950 text-white flex items-center justify-center">
              <QrCode className="w-4 h-4" />
            </div>
            <div>
              <DialogTitle className="text-base font-bold text-zinc-950">{t.modalQrTitle}</DialogTitle>
              <DialogDescription className="font-mono text-xs text-zinc-500">
                swift.link/{shortCode}
              </DialogDescription>
            </div>
          </div>
        </DialogHeader>

        {/* QR Image Frame */}
        <div className="p-4 rounded-2xl bg-zinc-50 border border-zinc-200/80 flex flex-col items-center justify-center space-y-2">
          {/* eslint-disable-next-line @next/next/no-img-element */}
          <img
            src={qrUrl}
            alt={`QR Code for ${shortCode}`}
            className="w-48 h-48 object-contain rounded-xl bg-white p-2.5 border border-zinc-200 shadow-2xs"
          />
          <p className="text-[10px] text-zinc-400 font-mono">
            {lang === 'tr' ? 'Vektörel PNG • 400x400' : 'Vector PNG • 400x400'}
          </p>
        </div>

        <Button asChild className="w-full bg-zinc-950 hover:bg-zinc-800 text-white font-semibold h-10">
          <a href={qrUrl} download={`qrcode-${shortCode}.png`} target="_blank" rel="noreferrer">
            <Download className="w-4 h-4 mr-1.5" />
            <span>{t.btnDownload}</span>
          </a>
        </Button>
      </DialogContent>
    </Dialog>
  );
};

